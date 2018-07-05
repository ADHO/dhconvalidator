/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.XPathContext;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.InputConverterFactory;
import org.adho.dhconvalidator.conversion.output.OutputConverter;
import org.adho.dhconvalidator.conversion.output.OutputConverterFactory;
import org.adho.dhconvalidator.conversion.oxgarage.OxGarageConversionClient;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.DocumentLog;
import org.adho.dhconvalidator.util.DocumentUtil;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * The converter handles all steps to convert input data to the final {@link ZipResult}.
 *
 * @author marco.petris@web.de
 */
public class Converter {

  public static final int DEFAULT_MAX_FILE_LENGTH = 60;

  private static String XHTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";
  private static String XHTML_NAMESPACE = "xhtml";
  private XPathContext xPathContext;

  private String contentAsXhtml;
  private Document document;
  private String oxGarageBaseURL;

  /**
   * @param oxGarageBaseURL the URL for the OxGarage web service
   * @throws IOException in case of any failure
   */
  public Converter(String oxGarageBaseURL) throws IOException {
    this.oxGarageBaseURL = oxGarageBaseURL;
    xPathContext = new XPathContext(XHTML_NAMESPACE, XHTML_NAMESPACE_URI);
  }

  /**
   * @param sourceData the source data
   * @param toTeiConversionPath the path to convert the input to TEI
   * @param user the user who initiated the conversion
   * @param inputFilename the name of the inputfile
   * @param progressListener a listener that can be notified about progress
   * @return the result package
   * @throws IOException in case of any failure
   */
  public ZipResult convert(
      byte[] sourceData,
      ConversionPath toTeiConversionPath,
      User user,
      String inputFilename,
      ConversionProgressListener progressListener)
      throws IOException {

    progressListener.setProgress(Messages.getString("Converter.progress1"));
    // do input conversion to prepare the data for the OxGarage service
    InputConverterFactory inputConverterFactory = toTeiConversionPath.getInputConverterFactory();
    InputConverter inputConverter = inputConverterFactory.createInputConverter();
    byte[] convertedInputData = inputConverter.convert(sourceData, user);
    Paper paper = inputConverter.getPaper();

    String inputFilenameExtension = getFilenameExtension(inputFilename);

    String computedFilename =
        user.getLastName().toUpperCase() + "_" + user.getFirstName() + "_" + paper.getTitle();
    computedFilename = computedFilename.replaceAll("[^a-zA-Z_0-9]", "_");

    int maxfilenamelength = PropertyKey.maxfilenamelength.getValue(DEFAULT_MAX_FILE_LENGTH);

    if (computedFilename.length() > maxfilenamelength) {
      computedFilename = computedFilename.substring(0, maxfilenamelength);
    }

    progressListener.setProgress(Messages.getString("Converter.progress2"));
    // do the OxGarage conversion
    OxGarageConversionClient oxGarageConversionClient =
        new OxGarageConversionClient(oxGarageBaseURL);
    String xmlFileName = computedFilename + ".xml";
    ZipResult zipResult =
        new ZipResult(
            oxGarageConversionClient.convert(
                convertedInputData,
                toTeiConversionPath,
                toTeiConversionPath.getDefaultProperties()),
            xmlFileName);

    document = zipResult.getDocument();

    DocumentLog.logConversionStepOutput(Messages.getString("Converter.log1"), document.toXML());

    progressListener.setProgress(Messages.getString("Converter.progress3"));
    // do the post processing to tweak the resulting TEI
    OutputConverterFactory outputConverterFactory = toTeiConversionPath.getOutputConverterFactory();
    OutputConverter outputConverter = outputConverterFactory.createOutputConverter();
    outputConverter.convert(document, user, paper);
    outputConverter.convert(zipResult);

    ByteArrayOutputStream conversionResultDocBuffer = new ByteArrayOutputStream();

    Serializer serializer = new Serializer(conversionResultDocBuffer);
    serializer.setIndent(2);
    serializer.write(document);

    DocumentLog.logConversionStepOutput(
        Messages.getString("Converter.log2"), conversionResultDocBuffer.toString("UTF-8"));
    validateDocument(conversionResultDocBuffer, progressListener);

    progressListener.setProgress(Messages.getString("Converter.progress4"));

    // do the conversion to HTML via OxGarage
    Document xHtmlDoc =
        oxGarageConversionClient.convertToDocument(
            conversionResultDocBuffer.toByteArray(),
            ConversionPath.TEI_TO_XHTML,
            ConversionPath.TEI_TO_XHTML.getDefaultProperties());

    setImageSizes(xHtmlDoc);
    if (PropertyKey.html_address_generation.isTrue()) {
      setHtmlAddress(xHtmlDoc, paper);
    }
    if (PropertyKey.html_to_xml_link.isTrue()) {
      setHtmlToXmlLink(xHtmlDoc, xmlFileName);
    }

    ByteArrayOutputStream xHtmlBuffer = new ByteArrayOutputStream();
    serializer = new Serializer(xHtmlBuffer);
    serializer.setIndent(2);
    serializer.write(xHtmlDoc);
    this.contentAsXhtml = xHtmlBuffer.toString("UTF-8");

    DocumentLog.logConversionStepOutput(Messages.getString("Converter.log3"), contentAsXhtml);

    // add the HTML to the result
    zipResult.putExternalResource(computedFilename + ".html", contentAsXhtml.getBytes("UTF-8"));

    // add original input file to the result
    zipResult.putExternalResource(computedFilename + "." + inputFilenameExtension, sourceData);

    return zipResult;
  }

  /**
   * @param filename
   * @return the extension present in the given filename if it is one of {@link Type Type's}
   *     extensions, "unknown" otherwise.
   */
  private String getFilenameExtension(String filename) {
    if (filename.contains(".")) {
      int extensionStart = filename.lastIndexOf(".") + 1;

      if (filename.length() > extensionStart) {
        String extension = filename.substring(extensionStart);
        if (Type.hasExtension(extension)) {
          return extension;
        }
      }
    }
    return "unknown";
  }

  private void setHtmlToXmlLink(Document xHtmlDoc, String xmlFileName) {
    Element titleDivElement =
        DocumentUtil.getFirstMatch(
            xHtmlDoc, "//xhtml:div[@class='stdheader autogenerated']", xPathContext);
    int titleDivPos = titleDivElement.getParent().indexOf(titleDivElement) + 1;

    Element linkDivElement = new Element("div", XHTML_NAMESPACE_URI);
    linkDivElement.addAttribute(new Attribute("class", "dhconvalidator-xml-link"));

    Element linkElement = new Element("a", XHTML_NAMESPACE_URI);
    linkElement.addAttribute(new Attribute("href", xmlFileName));
    linkElement.appendChild("XML");
    linkDivElement.appendChild(linkElement);

    titleDivElement.getParent().insertChild(linkDivElement, titleDivPos);
  }

  private void setHtmlAddress(Document xHtmlDoc, Paper paper) {
    Element addressElement = DocumentUtil.tryFirstMatch(xHtmlDoc, "//xhtml:address", xPathContext);
    if (addressElement != null) {
      StringBuilder builder = new StringBuilder();
      String conc = "";

      for (User author : paper.getAuthorsAndAffiliations()) {
        builder.append(conc);
        builder.append(author.getFirstName());
        builder.append(" ");
        builder.append(author.getLastName());
        builder.append(" (");
        builder.append(author.getEmail());
        builder.append("), ");
        builder.append(author.getOrganizations());
        conc = " " + Messages.getString("Converter.addressConc") + " ";
      }
      addressElement.removeChildren();
      addressElement.appendChild(builder.toString());
    }
  }

  /**
   * Sets the size of the contained images to 100%.
   *
   * @param xHtmlDoc
   */
  private void setImageSizes(Document xHtmlDoc) {
    Nodes imgElements = xHtmlDoc.query("//xhtml:img[@class='graphic']", xPathContext);
    for (int i = 0; i < imgElements.size(); i++) {
      Element imgElement = (Element) imgElements.get(i);
      imgElement.addAttribute(new Attribute("width", "100%"));
    }
  }

  /**
   * Validates the given data against the DHConvalidator schema.
   *
   * @param bos the data to be validated
   * @param progressListener a listner that can be notified about progress
   * @throws IOException in case of any failure
   */
  private void validateDocument(
      ByteArrayOutputStream bos, ConversionProgressListener progressListener) throws IOException {
    if (PropertyKey.performSchemaValidation.isTrue()) {
      try {
        progressListener.setProgress(Messages.getString("Converter.progress5"));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        URL xsdResource =
            Thread.currentThread()
                .getContextClassLoader()
                .getResource("/schema/dhconvalidator.xsd");

        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        factory.setSchema(schemaFactory.newSchema(xsdResource));

        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new ValidateConversionErrorHandler());

        Builder builder = new Builder(reader);
        builder.build(new ByteArrayInputStream(bos.toByteArray()));
      } catch (ParsingException | ParserConfigurationException | SAXException e) {
        throw new IOException(e);
      }
    }
  }

  /** @return the TEI document of the conversion result */
  public Document getDocument() {
    return document;
  }

  /** @return the TEI->HTML conversionn result for visual feedback */
  public String getContentAsXhtml() {
    return contentAsXhtml;
  }
}
