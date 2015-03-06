/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.util.DocumentUtil;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

/**
 * A client that talks to the ConfTool REST interface.
 * 
 * @author marco.petris@web.de
 *
 */
public class ConfToolClient {
	private static final boolean LOGIN_SUCCESS = true;
	public static final class AuthenticationException extends Exception {

		public AuthenticationException() {
			super();
		}

		public AuthenticationException(String message) {
			super(message);
		}
	}
	
	private char[] restSharedPass;
	private String confToolUrl;

	/**
	 * @param confToolUrl ConfTool REST interface
	 * @param restSharedPass ConfTool REST shared pass
	 */
	public ConfToolClient(String confToolUrl, char[] restSharedPass) {
		this.confToolUrl = confToolUrl;
		this.restSharedPass = restSharedPass;
	}
	
	/**
	 * Loads arguments from properties via {@link PropertyKey}.
	 */
	public ConfToolClient() {
		this(
			PropertyKey.conftool_rest_url.getValue(),
			PropertyKey.conftool_shared_pass.getValue().toCharArray());
	}
	
	private String getPassHash(String nonce) {
		return Hashing.sha256().hashString(
			nonce+new String(restSharedPass), Charsets.UTF_8).toString();
	}
	
	@SuppressWarnings("unused")
	private String getDetails(String user) throws IOException {
		String nonce = getNonce();
		
		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=remoteLogin"); //$NON-NLS-1$
		urlBuilder.append("&nonce="); //$NON-NLS-1$
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash="); //$NON-NLS-1$
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&user="); //$NON-NLS-1$
		urlBuilder.append(user);
		urlBuilder.append("&command=request"); //$NON-NLS-1$
		
		ClientResource client = 
				new ClientResource(Context.getCurrent(), Method.GET, urlBuilder.toString());
		
		Representation result = client.get();

		try (InputStream resultStream = result.getStream()) {
			Builder builder = new Builder();
			Document resultDoc = builder.build(resultStream);
			return resultDoc.toXML();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * @param loginUser a User
	 * @return a user enriched by first name and last name.
	 * @throws IOException in case of any failure
	 */
	public User getDetailedUser(User loginUser) throws IOException {
		User detailedUser = new DocumentToUserMapper().getUser(
				getExportData(ExportType.users, loginUser));
		if (detailedUser != null) {
			loginUser.setFirstName(detailedUser.getFirstName());
			loginUser.setLastName(detailedUser.getLastName());
			loginUser.setEmail(detailedUser.getEmail());
			loginUser.setAdmin(detailedUser.isAdmin());
		}
		return loginUser;
	}
	
	/**
	 * @param user a User
	 * @return a list of all submmissions of the given user
	 * @throws IOException in case of any failure
	 */
	public List<Paper> getPapers(User user) throws IOException {
		return new DocumentToPaperMapper().getPaperList(
				getExportData(ExportType.papers, user));
	}

	/**
	 * @param user a User
	 * @param paperId the ConfTool paperID
	 * @return the Paper with the given ID or <code>null</code>
	 * @throws IOException  in case of any failure
	 */
	public Paper getPaper(User user, Integer paperId) throws IOException {
		List<Paper> papers = getPapers(user);
		if (papers != null) {
			for (Paper paper : papers) {
				if (paper.getPaperId().equals(paperId)) {
					return paper;
				}
			}
		}
		return null;
	}

	
	private String getNonce() {
		Date date = new Date(new Date().getTime()*60);
		return String.valueOf(date.getTime());
	}
	
	private Document getExportData(ExportType type, User user) throws IOException {
		String nonce = getNonce();
		
		// see: ConfTool REST interface specification

		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=adminExport"); //$NON-NLS-1$
		urlBuilder.append("&nonce="); //$NON-NLS-1$
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash="); //$NON-NLS-1$
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&export_select="); //$NON-NLS-1$
		urlBuilder.append(type.name());
		urlBuilder.append("&form_include_deleted=0"); //$NON-NLS-1$
		urlBuilder.append("&form_export_format=xml"); //$NON-NLS-1$
		urlBuilder.append("&form_export_header=default"); //$NON-NLS-1$
		urlBuilder.append("&cmd_create_export=true"); //$NON-NLS-1$
		if (type.equals(ExportType.papers)) {
			urlBuilder.append("&form_export_papers_options[]=authors_extended_columns"); //$NON-NLS-1$
			urlBuilder.append("&form_export_papers_options[]=authors_extended_email"); //$NON-NLS-1$
			urlBuilder.append("&form_export_papers_options[]=authors_extended_organisations"); //$NON-NLS-1$
		}
		
		
		if (user != null) {
			urlBuilder.append("&form_userID="); //$NON-NLS-1$
			urlBuilder.append(user.getUserId());
		}
		
		ClientResource client = 
				new ClientResource(Context.getCurrent(), Method.GET, urlBuilder.toString());
		
		Representation result = client.get();
		
		try (InputStream resultStream = result.getStream()) {
			Builder builder = new Builder();
			Document resultDoc = builder.build(resultStream);
			System.out.println(resultDoc.toXML());

			return resultDoc;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * @param user a username
	 * @param pass a password
	 * @return the User
	 * @throws AuthenticationException in case of authentication failur
	 * @throws IOException in case of any other failure
	 */
	public User authenticate(String user, char[] pass) 
			throws IOException, AuthenticationException {
		String nonce = getNonce();
		
		// see: ConfTool REST interface specification
		
		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=remoteLogin"); //$NON-NLS-1$
		urlBuilder.append("&nonce="); //$NON-NLS-1$
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash="); //$NON-NLS-1$
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&user="); //$NON-NLS-1$
		urlBuilder.append(user);
		urlBuilder.append("&command=login"); //$NON-NLS-1$
		urlBuilder.append("&password="); //$NON-NLS-1$
		urlBuilder.append(pass);
		
		ClientResource client = 
				new ClientResource(Context.getCurrent(), Method.GET, urlBuilder.toString());
		
		Representation result = client.get();

		try (InputStream resultStream = result.getStream()) {
			Builder builder = new Builder();
			Document resultDoc = builder.build(resultStream);
			
			if (getLoginResult(resultDoc) == LOGIN_SUCCESS) {
				return getUser(resultDoc);
			}
			else {
				throw new AuthenticationException(getMessage(resultDoc));
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	private String getUserId(Document resultDoc) {
		Element resultElement = DocumentUtil.getFirstMatch(resultDoc, "/login/id"); //$NON-NLS-1$
		return resultElement.getValue();
	}	
	
	private String getUserName(Document resultDoc) {
		Element resultElement = DocumentUtil.getFirstMatch(resultDoc, "/login/username"); //$NON-NLS-1$
		return resultElement.getValue();
	}
	
	private User getUser(Document resultDoc) {
		return new User(Integer.valueOf(getUserId(resultDoc)), getUserName(resultDoc));
	}
	
	private boolean getLoginResult(Document resultDoc) {
		Element resultElement = DocumentUtil.getFirstMatch(resultDoc, "/login/result"); //$NON-NLS-1$
		return Boolean.valueOf(resultElement.getValue());
	}
	
	private String getMessage(Document resultDoc) {
		Element resultElement = DocumentUtil.getFirstMatch(resultDoc, "/login/message"); //$NON-NLS-1$
		return resultElement.getValue();
	}
	
	List<User> getUsers() {
		try {
			return new DocumentToUserMapper().getUsers(
					getExportData(ExportType.users, null));
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(
				new ConfToolClient("https://www.conftool.net/demo/dh2015_26a/rest.php", "DHhed8QD15".toCharArray()).getExportData(ExportType.users, null).toXML());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
