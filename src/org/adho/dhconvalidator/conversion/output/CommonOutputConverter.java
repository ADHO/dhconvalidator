/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.output;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.XPathContext;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.TeiNamespace;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.DocumentUtil;

/**
 * An OutputConverter that does modifications common to all supported formats (so far).
 *
 * @author marco.petris@web.de
 */
public class CommonOutputConverter implements OutputConverter {

  protected XPathContext xPathContext;

  public CommonOutputConverter() {
    xPathContext = new XPathContext();
    xPathContext.addNamespace(TeiNamespace.TEI.getName(), TeiNamespace.TEI.toUri());
    xPathContext.addNamespace("mml", "http://www.w3.org/1998/Math/MathML");
  }

  /* (non-Javadoc)
   * @see org.adho.dhconvalidator.conversion.output.OutputConverter#convert(nu.xom.Document, org.adho.dhconvalidator.conftool.User, org.adho.dhconvalidator.conftool.Paper)
   */
  @Override
  public void convert(Document document, User user, Paper paper) throws IOException {
    makeAuthorStatement(document, paper);
    makePublicationStmt(document);
    makeEncodingDesc(document);
    makeProfileDesc(document, paper);
    makeFigureHead(document);
    removeRevisions(document);
    adjustTableHeads(document);
  }

  private void adjustTableHeads(Document document) {
    Nodes searchResult = document.query("//tei:table/tei:head", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element tableHeadElement = (Element) searchResult.get(i);
      Element parentElement = (Element) tableHeadElement.getParent();
      parentElement.removeChild(tableHeadElement);
      parentElement.insertChild(tableHeadElement, 0);
    }
  }

  /**
   * Puts all image descriptions in a head element.
   *
   * @param document
   */
  private void makeFigureHead(Document document) {
    Nodes searchResult = document.query("//tei:figure", xPathContext);
    for (int i = 0; i < searchResult.size(); i++) {
      Element figureElement = (Element) searchResult.get(i);
      StringBuilder descBuilder = new StringBuilder();
      String conc = "";
      for (int j = 0; j < figureElement.getChildCount(); j++) {
        Node child = figureElement.getChild(j);
        if (child instanceof Text) {
          descBuilder.append(conc);
          descBuilder.append(((Text) child).getValue());
          conc = " ";
          child.getParent().removeChild(child);
        }
      }

      if (descBuilder.length() > 0) {
        Element headElement = figureElement.getFirstChildElement("head", TeiNamespace.TEI.toUri());
        if (headElement == null) {
          headElement = new Element("head", TeiNamespace.TEI.toUri());
          figureElement.appendChild(headElement);
        }
        headElement.appendChild(descBuilder.toString());
      }
    }
  }

  /**
   * Make a &lt;profileDesc&gt;
   *
   * @param document
   * @param paper
   */
  private void makeProfileDesc(Document document, Paper paper) {
    Element headerElement =
        DocumentUtil.getFirstMatch(document, "/tei:TEI/tei:teiHeader", xPathContext);

    Element oldProfileDescElement =
        headerElement.getFirstChildElement("profileDesc", TeiNamespace.TEI.toUri());

    Element profileDescElement = new Element("profileDesc", TeiNamespace.TEI.toUri());

    Element textClassElement = new Element("textClass", TeiNamespace.TEI.toUri());
    profileDescElement.appendChild(textClassElement);

    Element keywordsCategoryElement = new Element("keywords", TeiNamespace.TEI.toUri());
    keywordsCategoryElement.addAttribute(new Attribute("scheme", "ConfTool"));
    keywordsCategoryElement.addAttribute(new Attribute("n", "category"));
    Element paperTermElement = new Element("term", TeiNamespace.TEI.toUri());
    paperTermElement.appendChild("Paper");
    keywordsCategoryElement.appendChild(paperTermElement);
    textClassElement.appendChild(keywordsCategoryElement);

    Element keywordsSubcategoryElement = new Element("keywords", TeiNamespace.TEI.toUri());
    keywordsSubcategoryElement.addAttribute(new Attribute("scheme", "ConfTool"));
    keywordsSubcategoryElement.addAttribute(new Attribute("n", "subcategory"));
    Element confToolTypeTermElement = new Element("term", TeiNamespace.TEI.toUri());
    confToolTypeTermElement.appendChild(paper.getContributionType());
    keywordsSubcategoryElement.appendChild(confToolTypeTermElement);
    textClassElement.appendChild(keywordsSubcategoryElement);

    if (paper.getKeywords().size() > 0) {
      Element confToolKeywordsElement = new Element("keywords", TeiNamespace.TEI.toUri());
      confToolKeywordsElement.addAttribute(new Attribute("scheme", "ConfTool"));
      confToolKeywordsElement.addAttribute(new Attribute("n", "keywords"));
      textClassElement.appendChild(confToolKeywordsElement);

      for (String keyword : paper.getKeywords()) {
        Element confToolKeywordsTermElement = new Element("term", TeiNamespace.TEI.toUri());
        confToolKeywordsTermElement.appendChild(keyword);
        confToolKeywordsElement.appendChild(confToolKeywordsTermElement);
      }
    }

    if (paper.getTopics().size() > 0) {
      Element confToolTopicsElement = new Element("keywords", TeiNamespace.TEI.toUri());
      confToolTopicsElement.addAttribute(new Attribute("scheme", "ConfTool"));
      confToolTopicsElement.addAttribute(new Attribute("n", "topics"));
      textClassElement.appendChild(confToolTopicsElement);

      for (String topic : paper.getTopics()) {
        Element confToolTopicsTermElement = new Element("term", TeiNamespace.TEI.toUri());
        confToolTopicsTermElement.appendChild(topic);
        confToolTopicsElement.appendChild(confToolTopicsTermElement);
      }
    }

    if (oldProfileDescElement != null) {
      headerElement.replaceChild(oldProfileDescElement, profileDescElement);
    } else {
      headerElement.appendChild(profileDescElement);
    }
  }

  /**
   * Make an &lt;encodingDesc&gt;
   *
   * @param document
   * @throws IOException
   */
  private void makeEncodingDesc(Document document) throws IOException {
    String version = PropertyKey.version.getValue();

    Element headerElement =
        DocumentUtil.getFirstMatch(document, "/tei:TEI/tei:teiHeader", xPathContext);

    Element oldEncodingDesc =
        headerElement.getFirstChildElement("encodingDesc", TeiNamespace.TEI.toUri());

    try {
      Document encodingDescDoc =
          new Builder()
              .build(
                  PropertyKey.encodingDesc.getValue().replace("{VERSION}", version),
                  TeiNamespace.TEI.toUri());
      if (oldEncodingDesc != null) {
        headerElement.replaceChild(oldEncodingDesc, encodingDescDoc.getRootElement().copy());
      } else {
        headerElement.appendChild(encodingDescDoc.getRootElement().copy());
      }
    } catch (ParsingException pe) {
      throw new IOException(pe);
    }
  }

  /**
   * We do not support revisions.
   *
   * @param document
   */
  private void removeRevisions(Document document) {
    Nodes searchResult = document.query("/tei:TEI/tei:teiHeader/tei:revisionDesc", xPathContext);

    if (searchResult.size() > 0) {
      searchResult.get(0).getParent().removeChild(searchResult.get(0));
    }
  }

  /**
   * Make a &lt;publicationStmt&gt;
   *
   * @param document
   * @throws IOException
   */
  private void makePublicationStmt(Document document) throws IOException {

    Element publicationStmtElement =
        DocumentUtil.getFirstMatch(
            document, "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt", xPathContext);

    publicationStmtElement.removeChildren();
    try {
      Document publicationStmtDoc =
          new Builder().build(PropertyKey.publicationStmt.getValue(), TeiNamespace.TEI.toUri());
      publicationStmtElement
          .getParent()
          .replaceChild(publicationStmtElement, publicationStmtDoc.getRootElement().copy());
    } catch (ParsingException pe) {
      throw new IOException(pe);
    }
  }

  /**
   * Make an &lt;author&gt; statement.
   *
   * @param document
   * @param paper
   * @throws IOException
   */
  private void makeAuthorStatement(Document document, Paper paper) throws IOException {
    Element titleStmtElement =
        DocumentUtil.getFirstMatch(
            document, "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt", xPathContext);

    Element oldAuthor = titleStmtElement.getFirstChildElement("author", TeiNamespace.TEI.toUri());
    if (oldAuthor != null) {
      oldAuthor.getParent().removeChild(oldAuthor);
    }

    for (User authorAffiliation : paper.getAuthorsAndAffiliations()) {
      String surname = authorAffiliation.getLastName();
      String forename = authorAffiliation.getFirstName();

      Element authorElement = new Element("author", TeiNamespace.TEI.toUri());
      Element persNameElement = new Element("persName", TeiNamespace.TEI.toUri());
      authorElement.appendChild(persNameElement);

      Element surnameElement = new Element("surname", TeiNamespace.TEI.toUri());
      surnameElement.appendChild(surname);
      persNameElement.appendChild(surnameElement);
      Element forenameElement = new Element("forename", TeiNamespace.TEI.toUri());
      forenameElement.appendChild(forename);
      persNameElement.appendChild(forenameElement);

      Element affiliationElement = new Element("affiliation", TeiNamespace.TEI.toUri());
      affiliationElement.appendChild(authorAffiliation.getOrganizations());
      authorElement.appendChild(affiliationElement);

      Element emailElement = new Element("email", TeiNamespace.TEI.toUri());
      emailElement.appendChild(authorAffiliation.getEmail());
      authorElement.appendChild(emailElement);

      titleStmtElement.appendChild(authorElement);
    }
  }

  /* (non-Javadoc)
   * @see org.adho.dhconvalidator.conversion.output.OutputConverter#convert(org.adho.dhconvalidator.conversion.oxgarage.ZipResult)
   */
  @Override
  public void convert(ZipResult zipResult) throws IOException {
    checkImages(zipResult);
  }

  /**
   * Checks image resolution.
   *
   * @param zipResult
   * @throws IOException
   */
  private void checkImages(ZipResult zipResult) throws IOException {

    List<String> externalPicturesPaths =
        zipResult.getExternalResourcePathsStartsWith(
            PropertyKey.tei_image_location.getValue().substring(1)); // strip leading slash
    if (!externalPicturesPaths.isEmpty()) {
      int minWidth = Integer.valueOf(PropertyKey.image_min_resolution_width.getValue());
      int minHeight = Integer.valueOf(PropertyKey.image_min_resolution_height.getValue());

      for (String picturePath : externalPicturesPaths) {
        byte[] pictureData = zipResult.getExternalResource(picturePath);
        BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(pictureData));

        if (bimg == null) {
          throw new IOException(Messages.getString("CommonOutputConverter.imageparsingerror"));
        }

        int width = bimg.getWidth();
        int height = bimg.getHeight();

        if ((width < minWidth) || (height < minHeight)) {
          throw new IOException(
              Messages.getString("CommonOutputConverter.imageerror", minWidth, minHeight));
        }
      }
    }
  }

  /**
   * Moves images from one location to another.
   *
   * @param zipResult
   * @param oldPathPart
   * @param newPathPart
   */
  protected void adjustImagePath(ZipResult zipResult, String oldPathPart, String newPathPart) {
    if (!oldPathPart.equals(newPathPart)) {
      List<String> externalResourceNames =
          zipResult.getExternalResourcePathsStartsWith(oldPathPart);
      for (String oldName : externalResourceNames) {
        zipResult.moveExternalResource(
            oldName, oldName.replaceFirst(Pattern.quote(oldPathPart), newPathPart));
      }
    }
  }
}
