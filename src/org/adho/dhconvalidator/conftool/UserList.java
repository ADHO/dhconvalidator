/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A list of all ConfTool users.
 * 
 * @author marco.petris@web.de
 *
 */
public enum UserList {
	
	/**
	 * Singleton instance.
	 */
	INSTANCE,
	;
	
	private List<User> users;
	private Lock lock = new ReentrantLock();
	
	private UserList() {
		users = new ArrayList<>();
		load();
	}

	private void load() {
		ConfToolClient confToolClient = new ConfToolClient();
		users.clear();
		users.addAll(confToolClient.getUsers());
	}

	/**
	 * Threadsafe!
	 * @return a copy of the user list
	 */
	public List<User> getUsers() {
		lock.lock();
		try {
			return new ArrayList<>(users);
		}
		finally {
			lock.unlock();
		}
	}
	
	/**
	 * Reloads the list of users.
	 * (Threadsafe)
	 */
	public void reload() {
		lock.lock();
		try {
			load();
		}
		finally {
			lock.unlock();
		}
	}
}
