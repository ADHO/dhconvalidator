/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.user.User;

/**
 * Maps a ConfTool Paper result document to a {@link Paper}.
 *
 * @author marco.petris@web.de
 */
class DocumentToPaperMapper {

  public List<Paper> getPaperList(Document document) {

    List<Paper> result = new ArrayList<>();

    Elements paperElements = document.getRootElement().getChildElements("paper"); // $NON-NLS-1$

    for (int i = 0; i < paperElements.size(); i++) {
      Element paperElement = paperElements.get(i);
      Integer paperId =
          Integer.valueOf(paperElement.getFirstChildElement("paperID").getValue()); // $NON-NLS-1$

      List<User> authors = Lists.newArrayList();

      int authorIndex = 1;
      Element authorsNameElement = null;

      while ((authorsNameElement =
              paperElement.getFirstChildElement("authors_formatted_" + authorIndex + "_name"))
          != null) { // $NON-NLS-1$ //$NON-NLS-2$
        String name = authorsNameElement.getValue();

        if ((name != null) && (!name.isEmpty())) { // ConfTool sometimes reports empty authors...
          String organisations =
              paperElement
                  .getFirstChildElement("authors_formatted_" + authorIndex + "_organisation")
                  .getValue(); // $NON-NLS-1$ //$NON-NLS-2$

          String email =
              paperElement
                  .getFirstChildElement("authors_formatted_" + authorIndex + "_email")
                  .getValue(); // $NON-NLS-1$ //$NON-NLS-2$

          String firstname = "";

          int splitterIndex = name.indexOf(',');
          if (splitterIndex != -1) {
            if (splitterIndex < name.length() - 1) {
              firstname = name.substring(splitterIndex + 1).trim();
            }
            name = name.substring(0, splitterIndex);
          }

          authors.add(new User(firstname, name, organisations, email));
        }
        authorIndex++;
      }

      String title = paperElement.getFirstChildElement("title").getValue(); // $NON-NLS-1$
      String keywords = paperElement.getFirstChildElement("keywords").getValue(); // $NON-NLS-1$
      String topics = paperElement.getFirstChildElement("topics").getValue(); // $NON-NLS-1$
      String contributionType =
          paperElement.getFirstChildElement("contribution_type").getValue(); // $NON-NLS-1$

      result.add(new Paper(paperId, title, authors, keywords, topics, contributionType));
    }

    return result;
  }
}
