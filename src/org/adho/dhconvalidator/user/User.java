/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.user;



/**
 * A User as delivered by ConfTool.
 * 
 * @author marco.petris@web.de
 *
 */
public class User {
	
	private Integer userId;
	private String userName;
	private String firstName;
	private String lastName;
	private String organizations;
	private String email;
	private boolean admin;
	
	/**
	 * ConfTool user with id and username as delivered by authentication.
	 * @param userId
	 * @param userName
	 */
	public User(Integer userId, String userName) {
		super();
		this.userId = userId;
		this.userName = userName;
	}

	/**
	 * Detailed user as delivered by {@link ExportType#users}.
	 * @param userId
	 * @param firstName
	 * @param lastName
	 * @param admin 
	 */
	public User(Integer userId, String firstName, String lastName, boolean admin) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.admin = admin;
	}
	
	/**
	 * Detailed user as delivered by {@link ExportType#users}
	 * and copy constructor for user switching.
	 * @param userId
	 * @param userName
	 * @param firstName
	 * @param lastName
	 * @param admin
	 */
	public User(Integer userId, String firstName,
			String lastName, String email, boolean admin) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.admin = admin;
	}

	/**
	 * An author as delivered by {@link ExportType#papers}.
	 * @param firstName
	 * @param lastName
	 * @param organizations
	 * @param email
	 */
	public User(String firstName,
			String lastName, String organizations, String email) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.organizations = organizations;
		this.email = email;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	@Override
	public String toString() {
		return "#"+userId+"["+userName+","+firstName+","+lastName+","+email+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getOrganizations() {
		return organizations;
	}

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
}
