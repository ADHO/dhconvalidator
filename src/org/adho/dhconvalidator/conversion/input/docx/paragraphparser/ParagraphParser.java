/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

/**
 * This FSM parser loops through the paragraphs of a docx document and strips the readonly sections
 * (those betwween permStart and permEnd).
 *
 * @author marco.petris@web.de
 */
public class ParagraphParser {

  public void stripTemplateSections(Document document, XPathContext xPathContext) {
    // grab all paragraphs and loose permEnds (permStart is always inside a paragraph)
    Nodes searchResult =
        document.query(
            "/w:document/w:body/w:p | /w:document/w:body/w:permEnd", xPathContext); // $NON-NLS-1$

    // we start by looking for the first editable section start
    State currentState = State.SEEKPERMSTART;

    // loop over the search result
    for (int i = 0; i < searchResult.size(); i++) {
      Element paragraphElement = (Element) searchResult.get(i);
      currentState =
          currentState.getStateHandler().handleParagraph(paragraphElement, document, xPathContext);
    }
  }
}
