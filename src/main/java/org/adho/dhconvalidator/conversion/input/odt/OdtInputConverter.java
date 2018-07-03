/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input.odt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.SubmissionLanguage;
import org.adho.dhconvalidator.conversion.Type;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.DocumentUtil;

/**
 * An InputConverter for the OASIS odt format.
 *
 * @author marco.petris@web.de
 */
public class OdtInputConverter implements InputConverter {
  /** Namespaces used during conversion. */
  private enum Namespace {
    STYLE("style", "urn:oasis:names:tc:opendocument:xmlns:style:1.0"),
    TEXT("text", "urn:oasis:names:tc:opendocument:xmlns:text:1.0"),
    DC("dc", "http://purl.org/dc/elements/1.1/"),
    OFFICE("office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0"),
    META("meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0"),
    XLINK("xlink", "http://www.w3.org/1999/xlink"),
    DRAW("draw", "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"),
    SVG("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"),
    ;
    private String name;
    private String uri;

    private Namespace(String name, String uri) {
      this.name = name;
      this.uri = uri;
    }

    public String toUri() {
      return uri;
    }

    public String getName() {
      return name;
    }
  }

  private static final String TEMPLATE_SUFFIX = ".ott";
  private static final String CONFTOOLPAPERID_ATTRIBUTENAME = "ConfToolPaperID";
  private static final String SUBMISSIONLANGUAGE_ATTRIBUTENAME = "SubmissioLanguage";

  private XPathContext xPathContext;
  private Paper paper; // holds the paper loaded during conversion

  public OdtInputConverter() {
    // create xpathcontext with all the necessar namespaces
    xPathContext = new XPathContext();
    for (Namespace ns : Namespace.values()) {
      xPathContext.addNamespace(ns.getName(), ns.toUri());
    }
  }

  /* (non-Javadoc)
   * @see org.adho.dhconvalidator.conversion.input.InputConverter#convert(byte[], org.adho.dhconvalidator.conftool.User)
   */
  @Override
  public byte[] convert(byte[] sourceData, User user) throws IOException {
    // unzip data
    ZipFs zipFs = new ZipFs(sourceData);
    Document contentDoc = zipFs.getDocument("content.xml");

    cleanupParagraphStyles(contentDoc);
    makeHeaderElement(contentDoc);
    stripTemplateSections(contentDoc);
    makeReferencesChapter(contentDoc);
    cleanupFigureDescriptions(contentDoc);
    embedExternalFormulae(contentDoc, zipFs);

    Document metaDoc = zipFs.getDocument("meta.xml");
    Integer paperId = getPaperIdFromMeta(metaDoc);
    paper = PropertyKey.getPaperProviderInstance().getPaper(user, paperId);
    paper.setSubmissionLanguage(getSubmissionLanguageFromMeta(metaDoc));

    injectTitleIntoMeta(metaDoc, paper.getTitle());
    injectAuthorsIntoMeta(metaDoc, paper.getAuthorsAndAffiliations());

    zipFs.putDocument("content.xml", contentDoc);
    return zipFs.toZipData();
  }

  /**
   * We do not support svg descriptions as they are not directly visible in the original document
   * and therefore tend to confuse users.
   *
   * @param contentDoc
   */
  private void cleanupFigureDescriptions(Document contentDoc) {
    Nodes searchResult = contentDoc.query("//text:p/draw:frame/svg:desc", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element svgDescElement = (Element) searchResult.get(i);
      svgDescElement.getParent().removeChild(svgDescElement);
    }
  }

  /**
   * Formulae that are kept externally are getting embedded into the content.xml.
   *
   * @param contentDoc
   * @param zipFs
   * @throws IOException
   */
  private void embedExternalFormulae(Document contentDoc, ZipFs zipFs) throws IOException {
    Nodes searchResult = contentDoc.query("//draw:object", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element drawObjectElement = (Element) searchResult.get(i);
      String contentPath =
          drawObjectElement.getAttributeValue("href", Namespace.XLINK.toUri()).substring(2)
              + "/content.xml";

      Element parent = (Element) drawObjectElement.getParent();

      Document externalContentDoc = zipFs.getDocument(contentPath);

      if (!externalContentDoc.getRootElement().getLocalName().equals("math")) {
        throw new IOException(
            Messages.getString(
                "OdtInputConverter.matherror", externalContentDoc.getRootElement().getLocalName()));
      }

      Element drawImageElement = parent.getFirstChildElement("image", Namespace.DRAW.toUri());
      if (drawImageElement != null) {
        parent.removeChild(drawImageElement);
      }

      Element svgDescElement = parent.getFirstChildElement("desc", Namespace.SVG.toUri());
      if (svgDescElement != null) {
        parent.removeChild(svgDescElement);
      }
      parent.replaceChild(drawObjectElement, externalContentDoc.getRootElement().copy());
    }
  }

  /**
   * Removes empty References section or makes non empty References section a proper chapter.
   *
   * @param contentDoc
   * @throws IOException
   */
  private void makeReferencesChapter(Document contentDoc) throws IOException {
    Nodes searchResult = contentDoc.query("//text:section[@text:name='References']", xPathContext);
    if (searchResult.size() == 1) {
      Element referencesSectionElement = (Element) searchResult.get(0);
      Element parent = (Element) referencesSectionElement.getParent();
      int position = parent.indexOf(referencesSectionElement);
      // remove empty references section
      if (position == parent.getChildCount() - 1) {
        parent.removeChild(referencesSectionElement);
      } else { // or make it a proper chapter
        Element headElement = new Element("text:h", Namespace.TEXT.toUri());
        headElement.addAttribute(new Attribute("text:outline-level", Namespace.TEXT.toUri(), "1"));
        headElement.addAttribute(
            new Attribute("text:style-name", Namespace.TEXT.toUri(), "DH-BibliographyHeading"));
        headElement.appendChild("Bibliography");
        parent.replaceChild(referencesSectionElement, headElement);
      }
    } else {
      throw new IOException(
          Messages.getString("OdtInputConverter.sectionerror", searchResult.size()));
    }
  }

  private void makeHeaderElement(Document contentDoc) {
    Nodes searchResult =
        contentDoc.query(
            "//office:text/text:p[starts-with(@text:style-name,'DH-Heading')]", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element headElement = (Element) searchResult.get(i);
      String styleName = headElement.getAttributeValue("style-name", Namespace.TEXT.toUri());
      Integer level = 1;
      if (!styleName.equals("DH-Heading")) {
        level = Integer.valueOf(styleName.substring("DH-Heading".length()));
      }
      headElement.setLocalName("h");
      headElement.addAttribute(
          new Attribute("text:outline-level", Namespace.TEXT.toUri(), level.toString()));
    }
  }

  /**
   * Removes all template sections
   *
   * @param contentDoc
   */
  private void stripTemplateSections(Document contentDoc) {
    Nodes searchResult =
        contentDoc.query("//text:section[@text:name='Authors from ConfTool']", xPathContext);
    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }

    searchResult = contentDoc.query("//text:section[@text:name='Guidelines']", xPathContext);

    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }

    searchResult =
        contentDoc.query("//text:section[@text:name='Title from ConfTool']", xPathContext);

    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }

    searchResult = contentDoc.query("//text:section[@text:name='TitleInfoPre']", xPathContext);

    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }

    searchResult = contentDoc.query("//text:section[@text:name='TitleInfoPost']", xPathContext);

    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }

    searchResult = contentDoc.query("//text:section[@text:name='AuthorsInfoPre']", xPathContext);

    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }

    searchResult = contentDoc.query("//text:section[@text:name='AuthorsInfoPost']", xPathContext);

    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }

    searchResult = contentDoc.query("//text:section[@text:name='EndOfDocInfo']", xPathContext);

    if (searchResult.size() > 0) {
      removeNodes(searchResult);
    }
  }

  private void removeNodes(Nodes nodes) {
    for (int i = 0; i < nodes.size(); i++) {
      Node n = nodes.get(i);
      n.getParent().removeChild(n);
    }
  }

  private Integer getPaperIdFromMeta(Document metaDoc) throws IOException {
    Nodes searchResult =
        metaDoc.query(
            "/office:document-meta/office:meta/meta:user-defined[@meta:name='"
                + CONFTOOLPAPERID_ATTRIBUTENAME
                + "']",
            xPathContext);

    if (searchResult.size() == 1) {
      Element confToolPaperIdElement = (Element) searchResult.get(0);
      return Integer.valueOf(confToolPaperIdElement.getValue());
    } else {
      throw new IOException(Messages.getString("OdtInputConverter.invalidmeta"));
    }
  }

  private SubmissionLanguage getSubmissionLanguageFromMeta(Document metaDoc) throws IOException {
    Nodes searchResult =
        metaDoc.query(
            "/office:document-meta/office:meta/meta:user-defined[@meta:name='"
                + SUBMISSIONLANGUAGE_ATTRIBUTENAME
                + "']",
            xPathContext);

    if (searchResult.size() == 1) {
      Element submissionLanguageElement = (Element) searchResult.get(0);
      return SubmissionLanguage.valueOf(submissionLanguageElement.getValue());
    } else {
      return SubmissionLanguage.valueOf(PropertyKey.defaultSubmissionLanguage.getValue("ENGLISH"));
    }
  }
  /**
   * We remove all adhoc paragraph styles as they are not supported and might be used to create fake
   * chapter titles.
   *
   * @param contentDoc
   */
  private void cleanupParagraphStyles(Document contentDoc) {
    Map<String, String> paragraphStyleMapping = new HashMap<>();

    Nodes styleResult =
        contentDoc.query(
            "/office:document-content/office:automatic-styles/style:style[@style:family='paragraph']",
            xPathContext);

    for (int i = 0; i < styleResult.size(); i++) {
      Element styleNode = (Element) styleResult.get(i);
      String adhocName = styleNode.getAttributeValue("name", Namespace.STYLE.toUri());
      String definedName =
          styleNode.getAttributeValue("parent-style-name", Namespace.STYLE.toUri());
      paragraphStyleMapping.put(adhocName, definedName);
    }

    Nodes textResult =
        contentDoc.query("/office:document-content/office:body/office:text/text:*", xPathContext);

    for (int i = 0; i < textResult.size(); i++) {
      Element textNode = (Element) textResult.get(i);
      String styleName = textNode.getAttributeValue("style-name", Namespace.TEXT.toUri());
      if (styleName != null) {
        String definedName = paragraphStyleMapping.get(styleName);
        if (definedName != null) {
          textNode.getAttribute("style-name", Namespace.TEXT.toUri()).setValue(definedName);
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.adho.dhconvalidator.conversion.input.InputConverter#getPersonalizedTemplate(org.adho.dhconvalidator.paper.Paper, org.adho.dhconvalidator.conversion.SubmissionLanguage)
   */
  public byte[] getPersonalizedTemplate(Paper paper, SubmissionLanguage submissionLanguage)
      throws IOException {
    String templateFile = submissionLanguage.getTemplatePropertyKey().getValue() + TEMPLATE_SUFFIX;
    ZipFs zipFs =
        new ZipFs(Thread.currentThread().getContextClassLoader().getResourceAsStream(templateFile));
    Document contentDoc = zipFs.getDocument("content.xml");

    injectTitleIntoContent(contentDoc, paper.getTitle());
    injectAuthorsIntoContent(contentDoc, paper.getAuthorsAndAffiliations());
    updateLinkToConverter(contentDoc, PropertyKey.base_url.getValue());

    zipFs.putDocument("content.xml", contentDoc);

    Document metaDoc = zipFs.getDocument("meta.xml");
    injectTitleIntoMeta(metaDoc, paper.getTitle());
    injectAuthorsIntoMeta(metaDoc, paper.getAuthorsAndAffiliations());
    injectPaperIdIntoMeta(metaDoc, paper.getPaperId());
    injectSubmissionLanguageIntoMeta(metaDoc, submissionLanguage);
    zipFs.putDocument("meta.xml", metaDoc);

    return zipFs.toZipData();
  }

  private void updateLinkToConverter(Document contentDoc, String baseURL) {
    Element converterLinkElement =
        DocumentUtil.getFirstMatch(
            contentDoc,
            "//text:a[starts-with(@xlink:href, 'http://localhost:8080/dhconvalidator')]",
            xPathContext);

    Attribute targetAttr = converterLinkElement.getAttribute("href", Namespace.XLINK.toUri());
    targetAttr.setValue(
        targetAttr.getValue().replace("http://localhost:8080/dhconvalidator/", baseURL));
  }

  /**
   * Injects the ConfTool paperId into the meta data of the template.
   *
   * @param metaDoc
   * @param paperId
   */
  private void injectPaperIdIntoMeta(Document metaDoc, Integer paperId) {
    Nodes searchResult =
        metaDoc.query(
            "/office:document-meta/office:meta/meta:user-defined[@meta:name='"
                + CONFTOOLPAPERID_ATTRIBUTENAME
                + "']",
            xPathContext);

    if (searchResult.size() != 0) {
      for (int i = 0; i < searchResult.size(); i++) {
        Node n = searchResult.get(i);
        n.getParent().removeChild(n);
      }
    }

    Element confToolPaperIdElement = new Element("meta:user-defined", Namespace.META.toUri());
    confToolPaperIdElement.addAttribute(
        new Attribute("meta:name", Namespace.META.toUri(), CONFTOOLPAPERID_ATTRIBUTENAME));
    confToolPaperIdElement.appendChild(String.valueOf(paperId));

    Element metaElement =
        metaDoc.getRootElement().getFirstChildElement("meta", Namespace.OFFICE.toUri());
    metaElement.appendChild(confToolPaperIdElement);
  }

  /**
   * Injects the Submission language into the meta data of the template.
   *
   * @param metaDoc
   * @param paperId
   */
  private void injectSubmissionLanguageIntoMeta(
      Document metaDoc, SubmissionLanguage submissionLanguage) {
    Nodes searchResult =
        metaDoc.query(
            "/office:document-meta/office:meta/meta:user-defined[@meta:name='"
                + SUBMISSIONLANGUAGE_ATTRIBUTENAME
                + "']",
            xPathContext);

    if (searchResult.size() != 0) {
      for (int i = 0; i < searchResult.size(); i++) {
        Node n = searchResult.get(i);
        n.getParent().removeChild(n);
      }
    }

    Element submissionLanguageElement = new Element("meta:user-defined", Namespace.META.toUri());
    submissionLanguageElement.addAttribute(
        new Attribute("meta:name", Namespace.META.toUri(), SUBMISSIONLANGUAGE_ATTRIBUTENAME));
    submissionLanguageElement.appendChild(submissionLanguage.name());

    Element metaElement =
        metaDoc.getRootElement().getFirstChildElement("meta", Namespace.OFFICE.toUri());
    metaElement.appendChild(submissionLanguageElement);
  }
  /**
   * Injects the authors of the paper into the meta data.
   *
   * @param metaDoc
   * @param authorsAndAffiliations
   */
  private void injectAuthorsIntoMeta(Document metaDoc, List<User> authorsAndAffiliations) {
    Nodes searchResult =
        metaDoc.query("/office:document-meta/office:meta/meta:initial-creator", xPathContext);
    Element initialCreatorElement = null;
    if (searchResult.size() > 0) {
      initialCreatorElement = (Element) searchResult.get(0);

    } else {
      initialCreatorElement = new Element("meta:initial-creator", Namespace.META.toUri());
      Element metaElement =
          metaDoc.getRootElement().getFirstChildElement("meta", Namespace.OFFICE.toUri());
      metaElement.appendChild(initialCreatorElement);
    }

    initialCreatorElement.removeChildren();
    StringBuilder builder = new StringBuilder();
    String conc = "";
    for (User authorAffiliation : authorsAndAffiliations) {
      builder.append(conc);
      builder.append(authorAffiliation.getFirstName() + " " + authorAffiliation.getLastName());
      builder.append(" (");
      builder.append(authorAffiliation.getEmail());
      builder.append("), ");
      builder.append(authorAffiliation.getOrganizations());
      conc = "; ";
    }
    initialCreatorElement.appendChild(builder.toString());

    Nodes creatorSearchResult =
        metaDoc.query("/office:document-meta/office:meta/dc:creator", xPathContext);
    if (creatorSearchResult.size() > 0) {
      creatorSearchResult.get(0).getParent().removeChild(creatorSearchResult.get(0));
    }
  }

  /**
   * Injects the title of the paper into the metadata
   *
   * @param metaDoc
   * @param title
   */
  private void injectTitleIntoMeta(Document metaDoc, String title) {
    Nodes searchResult = metaDoc.query("/office:document-meta/office:meta/dc:title", xPathContext);
    Element titleElement = null;
    if (searchResult.size() > 0) {
      titleElement = (Element) searchResult.get(0);

    } else {
      titleElement = new Element("dc:title", Namespace.DC.toUri());
      Element metaElement =
          metaDoc.getRootElement().getFirstChildElement("meta", Namespace.OFFICE.toUri());
      metaElement.appendChild(titleElement);
    }
    titleElement.removeChildren();
    titleElement.appendChild(title);
  }

  /**
   * Injects authors into the readonly authors section.
   *
   * @param contentDoc
   * @param authorsAndAffiliations
   * @throws IOException
   */
  private void injectAuthorsIntoContent(Document contentDoc, List<User> authorsAndAffiliations)
      throws IOException {
    Nodes searchResult =
        contentDoc.query("//text:section[@text:name='Authors from ConfTool']", xPathContext);

    if (searchResult.size() != 1) {
      throw new IOException(
          Messages.getString("OdtInputConverter.sectionerror2", searchResult.size()));
    }

    if (!(searchResult.get(0) instanceof Element)) {
      throw new IllegalStateException(Messages.getString("OdtInputConverter.sectionerror3"));
    }

    Element authorSectionElement = (Element) searchResult.get(0);

    authorSectionElement.removeChildren();
    for (User authorAffiliation : authorsAndAffiliations) {
      Element authorParagraphElement = new Element("p", Namespace.TEXT.toUri());
      authorSectionElement.appendChild(authorParagraphElement);
      authorParagraphElement.appendChild(
          authorAffiliation.getFirstName()
              + " "
              + authorAffiliation.getLastName()
              + " ("
              + authorAffiliation.getEmail()
              + ")"
              + ", "
              + authorAffiliation.getOrganizations());
      authorParagraphElement.addAttribute(
          new Attribute("text:style-name", Namespace.TEXT.toUri(), "P6"));
    }
  }

  /**
   * Injects title into the read only title section.
   *
   * @param contentDoc
   * @param title
   * @throws IOException
   */
  private void injectTitleIntoContent(Document contentDoc, String title) throws IOException {
    Nodes searchResult =
        contentDoc.query("//text:section[@text:name='Title from ConfTool']", xPathContext);

    if (searchResult.size() != 1) {
      throw new IOException(
          Messages.getString("OdtInputConverter.titleerror", searchResult.size()));
    }

    if (!(searchResult.get(0) instanceof Element)) {
      throw new IllegalStateException(Messages.getString("OdtInputConverter.titleerror2"));
    }

    Element titleSectionElement = (Element) searchResult.get(0);

    titleSectionElement.removeChildren();
    Element titleParagraphElement = new Element("p", Namespace.TEXT.toUri());
    titleSectionElement.appendChild(titleParagraphElement);
    titleParagraphElement.appendChild(title);
    titleParagraphElement.addAttribute(
        new Attribute("text:style-name", Namespace.TEXT.toUri(), "P1"));
  }

  /* (non-Javadoc)
   * @see org.adho.dhconvalidator.conversion.input.InputConverter#getFileExtension()
   */
  @Override
  public String getFileExtension() {
    return Type.ODT.getExtension();
  }

  /* (non-Javadoc)
   * @see org.adho.dhconvalidator.conversion.input.InputConverter#getPaper()
   */
  @Override
  public Paper getPaper() {
    return paper;
  }

  @Override
  public String getTextEditorDescription() {
    return Messages.getString("OdtInputConverter.editors");
  }
}
