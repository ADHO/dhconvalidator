/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.demo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.user.UserProvider;

/**
 * Provides a single user for demo purposes.
 * 
 * @author marco.petris@web.de
 *
 */
public class DemoUserProvider implements UserProvider {

	@Override
	public User getDetailedUser(User loginUser) throws IOException {
		loginUser.setFirstName("Sam");
		loginUser.setLastName("Tester");
		loginUser.setEmail("sam.tester@digitalhumanities.it");
		loginUser.setAdmin(false);
		return loginUser;
	}

	@Override
	public User authenticate(String user, char[] pass) throws IOException,
			AuthenticationException {
		return new User(1, "demo-user");
	}

	@Override
	public List<User> getUsers() {
		return Collections.singletonList(
			new User(1, "Sam", "Tester", "sam.tester@digitalhumanities.it", false));
	}

}
