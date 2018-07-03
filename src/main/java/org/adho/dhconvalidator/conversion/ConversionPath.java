/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

import java.util.Properties;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.InputConverterFactory;
import org.adho.dhconvalidator.conversion.input.docx.DocxInputConverter;
import org.adho.dhconvalidator.conversion.input.odt.OdtInputConverter;
import org.adho.dhconvalidator.conversion.output.DocxOutputConverter;
import org.adho.dhconvalidator.conversion.output.OdtOutputConverter;
import org.adho.dhconvalidator.conversion.output.OutputConverter;
import org.adho.dhconvalidator.conversion.output.OutputConverterFactory;
import org.adho.dhconvalidator.util.Pair;

/**
 * All conversion paths that are supported by the DHConvalidator.
 *
 * @author marco.petris@web.de
 */
@SuppressWarnings("unchecked")
public enum ConversionPath {

  /** .odt to TEI */
  ODT_TO_TEI(
      Type.ODT.getIdentifier() + Type.TEI.getIdentifier(),
      "odt",
      new InputConverterFactory() {
        public InputConverter createInputConverter() {
          return new OdtInputConverter();
        }
      },
      new OutputConverterFactory() {

        @Override
        public OutputConverter createOutputConverter() {
          return new OdtOutputConverter();
        }
      },
      new Pair<>("oxgarage.textOnly", "false"),
      new Pair<>("oxgarage.getImages", "true"),
      new Pair<>("oxgarage.getOnlineImages", "true"),
      new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")),
  /** xHTML to TEI (not used currently) */
  XHTML_TO_TEI(
      Type.XHTML.getIdentifier() + Type.TEI.getIdentifier(),
      "xhtml",
      new Pair<>("oxgarage.textOnly", "false"),
      new Pair<>("oxgarage.getImages", "false"),
      new Pair<>("oxgarage.getOnlineImages", "false"),
      new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")),
  /** .docx to TEI */
  DOCX_TO_TEI(
      Type.DOCX.getIdentifier() + Type.TEI.getIdentifier(),
      "docx",
      new InputConverterFactory() {
        public InputConverter createInputConverter() {
          return new DocxInputConverter();
        }
      },
      new OutputConverterFactory() {

        @Override
        public OutputConverter createOutputConverter() {
          return new DocxOutputConverter();
        }
      },
      new Pair<>("oxgarage.textOnly", "false"),
      new Pair<>("oxgarage.getImages", "true"),
      new Pair<>("oxgarage.getOnlineImages", "true"),
      new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")),
  /** TEI to xHTML */
  TEI_TO_XHTML(
      Type.TEI.getIdentifier() + Type.XHTML.getIdentifier(),
      "tei",
      new Pair<>("oxgarage.textOnly", "false"),
      new Pair<>("oxgarage.getImages", "false"),
      new Pair<>("oxgarage.getOnlineImages", "false"),
      new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")),
  ;

  private String path;
  private Properties properties;
  private InputConverterFactory inputConverterFactory;
  private OutputConverterFactory outputConverterFactory;
  private String defaultFileExt;

  /**
   * @param path the actual path
   * @param defaultFileExt the extension of the source type
   * @param inputConverterFactory the factory for the {@link InputConverter}
   * @param outputConverterFactory the factory for the {@link OutputConverter}
   * @param propertyPairs key/value properties
   */
  private ConversionPath(
      String path,
      String defaultFileExt,
      InputConverterFactory inputConverterFactory,
      OutputConverterFactory outputConverterFactory,
      Pair<String, String>... propertyPairs) {

    this.path = path;
    this.defaultFileExt = defaultFileExt;
    this.inputConverterFactory = inputConverterFactory;
    this.outputConverterFactory = outputConverterFactory;
    this.properties = new Properties();
    if (propertyPairs != null) {
      for (Pair<String, String> pair : propertyPairs) {
        this.properties.setProperty(pair.getFirst(), pair.getSecond());
      }
    }
  }

  /**
   * @param path the actual path
   * @param defaultFileExt the extension of the source type
   * @param inputConverterFactory the factory for the {@link InputConverter}
   * @param propertyPairs key/value properties
   */
  private ConversionPath(
      String path,
      String defaultFileExt,
      InputConverterFactory inputConverter,
      Pair<String, String>... propertyPairs) {
    this(path, defaultFileExt, inputConverter, null, propertyPairs);
  }

  /**
   * @param path the actual path
   * @param defaultFileExt the extension of the source type
   * @param propertyPairs key/value properties
   */
  private ConversionPath(
      String path, String defaultFileExt, Pair<String, String>... propertyPairs) {
    this(path, defaultFileExt, null, null, propertyPairs);
  }

  /** @return the conversion path */
  public String getPath() {
    return path;
  }

  /** @return the default conversion properties */
  public Properties getDefaultProperties() {
    return properties;
  }

  /** @return the file extension of the source type */
  public String getDefaultFileExt() {
    return defaultFileExt;
  }

  /**
   * @param filename the name of the source file
   * @return the ConversionPath for the given source file
   * @throws IllegalArgumentException if there is no path for this file type
   */
  public static ConversionPath getConversionPathByFilename(String filename) {
    for (ConversionPath path : values()) {
      if (path.getDefaultFileExt().equals(filename.substring(filename.lastIndexOf('.') + 1))) {
        return path;
      }
    }

    throw new IllegalArgumentException(
        Messages.getString("ConversionPath.noConversionPathFound", filename));
  }

  /** @return the factory for the {@link InputConverter} */
  public InputConverterFactory getInputConverterFactory() {
    return inputConverterFactory;
  }

  /** @return the factory for the {@link OutputConverter} */
  public OutputConverterFactory getOutputConverterFactory() {
    return outputConverterFactory;
  }
}
