/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;

/**
 * Maps a ConfTool User result document to a {@link User} or a list of Users.
 *
 * @author marco.petris@web.de
 */
class DocumentToUserMapper {

  public User getUser(Document document) {
    List<User> users = getUsers(document);

    if (users.size() == 1) {
      return users.get(0);
    }

    Logger.getLogger(DocumentToUserMapper.class.getName())
        .warning(
            Messages.getString(
                "DocumentToUserMapper.warning", // $NON-NLS-1$
                users.size(),
                document.toString()));

    return null;
  }

  public List<User> getUsers(Document document) {
    Elements userElements = document.getRootElement().getChildElements("user"); // $NON-NLS-1$
    ArrayList<User> result = new ArrayList<>();

    for (int i = 0; i < userElements.size(); i++) {
      result.add(getUser(userElements.get(i)));
    }

    return result;
  }

  public List<User> getSubmittingAuthors(Document document) {
    Elements userElements =
        document.getRootElement().getChildElements("subsumed_author"); // $NON-NLS-1$
    ArrayList<User> result = new ArrayList<>();

    for (int i = 0; i < userElements.size(); i++) {
      User author = getSubmittingAuthor(userElements.get(i));
      if (author != null) {
        result.add(author);
      }
    }

    return result;
  }

  private User getUser(Element userElement) {
    Integer userId =
        Integer.valueOf(userElement.getFirstChildElement("personID").getValue()); // $NON-NLS-1$
    String firstName = userElement.getFirstChildElement("firstname").getValue(); // $NON-NLS-1$
    String lastName = userElement.getFirstChildElement("name").getValue(); // $NON-NLS-1$
    String email = userElement.getFirstChildElement("email").getValue(); // $NON-NLS-1$
    String statusList = userElement.getFirstChildElement("status").getValue(); // $NON-NLS-1$

    return new User(
        userId,
        firstName,
        lastName,
        email,
        statusList.contains("admin") // $NON-NLS-1$
            || email.equals(PropertyKey.developeradmin.getValue("not set"))); // $NON-NLS-1$
  }

  private User getSubmittingAuthor(Element userElement) {
    String userIdValue = userElement.getFirstChildElement("personID").getValue(); // $NON-NLS-1$
    // is this author also a ConfTool user with ID?
    if ((userIdValue == null) || userIdValue.isEmpty()) {
      return null; // no, so skip this one
    }

    userIdValue = userIdValue.replaceAll("\\D", ""); // the ID contains comments sometimes...

    Integer userId = Integer.valueOf(userIdValue);
    String firstName = userElement.getFirstChildElement("firstname").getValue(); // $NON-NLS-1$
    String lastName = userElement.getFirstChildElement("lastname").getValue(); // $NON-NLS-1$
    String email = userElement.getFirstChildElement("email").getValue(); // $NON-NLS-1$

    String numberOfSubmissionsValue =
        userElement.getFirstChildElement("submitter_of").getValue(); // $NON-NLS-1$

    if ((numberOfSubmissionsValue == null)
        || numberOfSubmissionsValue.isEmpty()
        || (Integer.valueOf(numberOfSubmissionsValue).intValue() < 1)) {
      return null; // no submissions from this author
    }

    return new User(userId, firstName, lastName, email, false);
  }
}
