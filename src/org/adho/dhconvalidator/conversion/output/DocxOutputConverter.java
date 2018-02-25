/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;
import java.util.regex.Pattern;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import org.adho.dhconvalidator.conversion.SubmissionLanguage;
import org.adho.dhconvalidator.conversion.TeiNamespace;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.DocumentUtil;

/**
 * Converts the TEI that results from docx conversion.
 *
 * @author marco.petris@web.de
 */
public class DocxOutputConverter extends CommonOutputConverter {

  @Override
  public void convert(Document document, User user, Paper paper) throws IOException {
    super.convert(document, user, paper);

    makeComplexTitleStatement(document, paper);
    makeChapterAttributes(document);
    cleanupParagraphRendAttribute(document);
    cleanupGraphics(document);

    makeBibliography(document, paper.getSubmissionLanguage());
    cleanupBoldAndItalicsRendition(document);
    removeFrontSection(document);
    makeQuotations(document);
    renameImageDir(document);
    fixFormulae(document);
    removeSegs(document);
  }

  /**
   * Segments are not allowed so far, so we convert them to hi elements or remove the completely.
   *
   * @param document
   */
  private void removeSegs(Document document) {
    Nodes searchResult = document.query("//tei:seg", xPathContext);
    for (int i = 0; i < searchResult.size(); i++) {
      Element segElement = (Element) searchResult.get(i);
      Element parentElement = (Element) segElement.getParent();
      if (segElement.getAttribute("rend") != null) {
        segElement.setLocalName("hi");
      } else {
        int position = parentElement.indexOf(segElement);
        for (int j = 0; j < segElement.getChildCount(); j++) {
          parentElement.insertChild(segElement.getChild(j).copy(), position);
          position++;
        }
        parentElement.removeChild(segElement);
      }
    }
  }

  // TODO: this should probably be fixed in the stylesheets
  /**
   * Ensures that mathml formulae appear always within a formula element
   *
   * @param document
   */
  private void fixFormulae(Document document) {
    Nodes searchResult = document.query("//mml:math", xPathContext);
    for (int i = 0; i < searchResult.size(); i++) {
      Element mathElement = (Element) searchResult.get(i);
      Element parentElement = (Element) mathElement.getParent();
      if (!parentElement.getLocalName().equals("formula")) {
        Element formulaElement = new Element("formula", TeiNamespace.TEI.toUri());
        parentElement.replaceChild(mathElement, formulaElement);
        formulaElement.appendChild(mathElement.copy());
      }
    }
  }

  private void renameImageDir(Document document) {
    Nodes searchResult = document.query("//tei:*[starts-with(@url, 'media/')]", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element element = (Element) searchResult.get(i);
      Attribute urlAttr = element.getAttribute("url");
      urlAttr.setValue(
          urlAttr
              .getValue()
              .replaceFirst(
                  Pattern.quote("media/"),
                  PropertyKey.tei_image_location.getValue().substring(1) // skip leading slash
                      + "/"));
    }
  }

  private void makeQuotations(Document document) {
    Nodes searchResult = document.query("//tei:p[@rend='DH-Quotation']", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element element = (Element) searchResult.get(i);
      element.setLocalName("quote");
      element.removeAttribute(element.getAttribute("rend"));
    }
  }

  private void removeFrontSection(Document document) {
    Element frontElement = DocumentUtil.tryFirstMatch(document, "//tei:front", xPathContext);
    if (frontElement != null) {
      frontElement.getParent().removeChild(frontElement);
    }
  }

  private void cleanupBoldAndItalicsRendition(Document document) {
    Nodes searchResult = document.query("//*[@rend='Strong']", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element element = (Element) searchResult.get(i);
      element.getAttribute("rend").setValue("bold");
    }

    searchResult = document.query("//*[@rend='Emphasis']", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element element = (Element) searchResult.get(i);
      element.getAttribute("rend").setValue("italic");
    }
  }

  private void cleanupGraphics(Document document) {
    Nodes searchResult = document.query("//tei:graphic/tei:desc", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element descElement = (Element) searchResult.get(i);
      descElement.getParent().removeChild(descElement);
    }
  }

  private void makeBibliography(Document document, SubmissionLanguage submissionLanguage) {
    Nodes searchResult = document.query("//tei:p[@rend='DH-BibliographyHeading']", xPathContext);

    if (searchResult.size() == 1) {

      Element bibParagraphHeaderElement = (Element) searchResult.get(0);

      Element parent = (Element) bibParagraphHeaderElement.getParent();
      int startPosition = parent.indexOf(bibParagraphHeaderElement) + 1;

      Elements children = parent.getChildElements();

      if (children.size() > startPosition) {
        Element textElement =
            DocumentUtil.getFirstMatch(document, "/tei:TEI/tei:text", xPathContext);

        Element backElement = new Element("back", TeiNamespace.TEI.toUri());
        textElement.appendChild(backElement);

        Element divBibliogrElement = new Element("div", TeiNamespace.TEI.toUri());
        divBibliogrElement.addAttribute(new Attribute("type", "bibliogr"));
        backElement.appendChild(divBibliogrElement);

        Element listBiblElement = new Element("listBibl", TeiNamespace.TEI.toUri());
        divBibliogrElement.appendChild(listBiblElement);

        Element listBiblHeadElement = new Element("head", TeiNamespace.TEI.toUri());
        listBiblHeadElement.appendChild(submissionLanguage.getBibliographyTranslation());
        listBiblElement.appendChild(listBiblHeadElement);

        for (int i = startPosition; i < children.size(); i++) {
          Element bibEntryParagraphElement = children.get(i);

          bibEntryParagraphElement.getParent().removeChild(bibEntryParagraphElement);
          bibEntryParagraphElement.setLocalName("bibl");
          listBiblElement.appendChild(bibEntryParagraphElement);
        }
      }

      bibParagraphHeaderElement.getParent().removeChild(bibParagraphHeaderElement);
    }
  }

  /**
   * We support only our styles.
   *
   * @param document
   */
  private void cleanupParagraphRendAttribute(Document document) {
    Nodes searchResult = document.query("//tei:p[@rend='DH-Default']", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element paragraphElement = (Element) searchResult.get(i);
      paragraphElement.removeAttribute(paragraphElement.getAttribute("rend"));
    }
  }

  /**
   * We want numbered chapters.
   *
   * @param document
   */
  private void makeChapterAttributes(Document document) {
    Element bodyElement =
        DocumentUtil.getFirstMatch(document, "/tei:TEI/tei:text/tei:body", xPathContext);

    insertChapterAttributes(bodyElement, 1);
  }

  private void insertChapterAttributes(Element parentElemnt, int depth) {

    Nodes searchResult = parentElemnt.query("tei:div/tei:head", xPathContext);

    for (int i = 0; i < searchResult.size(); i++) {
      Element chapterElement = (Element) searchResult.get(i).getParent();
      chapterElement.addAttribute(new Attribute("type", "div" + depth));
      chapterElement.addAttribute(new Attribute("rend", "DH-Heading" + depth));
      insertChapterAttributes(chapterElement, depth + 1);
    }
  }

  private void makeComplexTitleStatement(Document document, Paper paper) {

    Element titleElement =
        DocumentUtil.getFirstMatch(
            document, "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title", xPathContext);

    Nodes subTitleResults = document.query("//tei:p[@rend='DH-Subtitle']", xPathContext);

    if (subTitleResults.size() > 0) {
      String title = paper.getTitle();

      Element titleStmtElement = (Element) titleElement.getParent();
      Element complexTitle = new Element("title", TeiNamespace.TEI.toUri());
      complexTitle.addAttribute(new Attribute("type", "full"));
      titleStmtElement.insertChild(complexTitle, 0);

      titleElement.addAttribute(new Attribute("type", "main"));
      titleElement.appendChild(title);

      titleElement.getParent().removeChild(titleElement);
      complexTitle.appendChild(titleElement);

      for (int i = 0; i < subTitleResults.size(); i++) {
        Node subtitleNode = subTitleResults.get(i);

        Element subtitleElement = new Element("title", TeiNamespace.TEI.toUri());
        subtitleElement.addAttribute(new Attribute("type", "sub"));
        subtitleElement.appendChild(subtitleNode.getValue());
        complexTitle.appendChild(subtitleElement);
        subtitleNode.getParent().removeChild(subtitleNode);
      }
    } else {
      titleElement.appendChild(paper.getTitle());
    }
  }

  /* (non-Javadoc)
   * @see org.adho.dhconvalidator.conversion.output.CommonOutputConverter#convert(org.adho.dhconvalidator.conversion.oxgarage.ZipResult)
   */
  @Override
  public void convert(ZipResult zipResult) throws IOException {
    adjustImagePath(zipResult, "media", PropertyKey.tei_image_location.getValue().substring(1));
    super.convert(zipResult);
  }
}
