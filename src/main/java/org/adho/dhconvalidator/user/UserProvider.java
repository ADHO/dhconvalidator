/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.user;

import java.io.IOException;
import java.util.List;

/**
 * User management module including authentication.
 *
 * @author marco.petris@web.de
 */
public interface UserProvider {

  public static final class AuthenticationException extends Exception {

    public AuthenticationException() {
      super();
    }

    public AuthenticationException(String message) {
      super(message);
    }
  }

  /**
   * @param loginUser a User
   * @return a user enriched by first name and last name.
   * @throws IOException in case of any failure
   */
  public User getDetailedUser(User loginUser) throws IOException;

  /**
   * @param user a username
   * @param pass a password
   * @return the User
   * @throws UserProvider.AuthenticationException in case of authentication failur
   * @throws IOException in case of any other failure
   */
  public User authenticate(String user, char[] pass)
      throws IOException, UserProvider.AuthenticationException;

  /** @return a read only list of all users, this can be a cost intensive operation use wisely */
  public List<User> getUsers();
}
