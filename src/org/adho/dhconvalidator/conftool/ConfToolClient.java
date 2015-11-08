/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.paper.PaperProvider;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.ui.PropertyProvider;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.user.UserProvider;
import org.adho.dhconvalidator.util.DocumentUtil;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.XPathContext;

/**
 * A client that talks to the ConfTool REST interface.
 * 
 * @author marco.petris@web.de
 *
 */
public class ConfToolClient implements UserProvider, PaperProvider {
	
	private static final boolean LOGIN_SUCCESS = true;
	private static final Logger LOGGER = Logger.getLogger(ConfToolClient.class.getName());
	
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
				getExportData(ExportType.users, loginUser, null));
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
				getExportData(ExportType.papers, user, 
					PropertyKey.showOnlyAcceptedPapers.isTrue()?"p":null));
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
	
	private ZipFs getDhcFile(User user, Paper paper) throws IOException {
		String nonce = getNonce();
		
		// see: ConfTool REST interface specification

		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=downloadPaper"); //$NON-NLS-1$
		urlBuilder.append("&nonce="); //$NON-NLS-1$
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash="); //$NON-NLS-1$
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&form_id="); //$NON-NLS-1$
		urlBuilder.append(paper.getPaperId());
		
		ClientResource client = 
				new ClientResource(Context.getCurrent(), Method.GET, urlBuilder.toString());
		
		Representation result = client.get();
		
		try (InputStream resultStream = result.getStream()) {
			return new ZipFs(resultStream);
		}
		catch (Exception e) {
			throw new IOException(e);
		}

	}
	
	private Document getExportData(ExportType type, User user, String status) throws IOException {
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
		urlBuilder.append("&cmd_create_export=Create Export File"); //$NON-NLS-1$
		if (type.equals(ExportType.papers)) {
			urlBuilder.append("&form_export_papers_options[]=authors_extended_columns"); //$NON-NLS-1$
			urlBuilder.append("&form_export_papers_options[]=authors_extended_email"); //$NON-NLS-1$
			urlBuilder.append("&form_export_papers_options[]=authors_extended_organisations"); //$NON-NLS-1$
		}
		
		if (status != null) {
			urlBuilder.append("&form_status=");
			urlBuilder.append(status);
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
			LOGGER.info(resultDoc.toXML());
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
	 * @throws UserProvider.AuthenticationException in case of authentication failur
	 * @throws IOException in case of any other failure
	 */
	public User authenticate(String user, char[] pass) 
			throws IOException, UserProvider.AuthenticationException {
		String nonce = getNonce();
		
		// see: ConfTool REST interface specification
		
		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=remoteLogin"); //$NON-NLS-1$
		urlBuilder.append("&nonce="); //$NON-NLS-1$
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash="); //$NON-NLS-1$
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&user="); //$NON-NLS-1$
		urlBuilder.append(URLEncoder.encode(user, "UTF-8"));
		urlBuilder.append("&command=login"); //$NON-NLS-1$
		urlBuilder.append("&password="); //$NON-NLS-1$
		urlBuilder.append(URLEncoder.encode(String.valueOf(pass), "UTF-8"));
		
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
				throw new UserProvider.AuthenticationException(getMessage(resultDoc));
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
	
	public List<User> getUsers() {
		try {
			DocumentToUserMapper documentToUserMapper = new DocumentToUserMapper();
			
			List<User> allUsers = documentToUserMapper.getUsers(
					getExportData(ExportType.users, null, null));
			
			List<User> acceptedUsers = documentToUserMapper.getSubmittingAuthors(
					getExportData(ExportType.subsumed_authors, null, "p"));
					
			List<User> result = new ArrayList<>();
			
			for (User user : allUsers) {
				if (user.isAdmin() || acceptedUsers.contains(user) || !PropertyKey.showOnlyAcceptedUsers.isTrue()) {
					result.add(user);
				}
			}
			
			return result;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	// testing
	public static void main(String[] args) throws Exception {
		XPathContext context = new XPathContext("xhtml", "http://www.w3.org/1999/xhtml");
		Properties properties = new Properties();
		properties.load(new FileInputStream(args[0]));
		PropertyProvider.setProperties(properties);
		
		ConfToolClient client = new ConfToolClient();
		
		List<User> users = client.getUsers();
		for (User user : users) {
			List<Paper> papers = client.getPapers(user);
			for (Paper paper : papers) {
				String inputFilename = 
						user.getLastName().toUpperCase() + "_" + user.getFirstName() 
						+ "_" + paper.getTitle();
				inputFilename = inputFilename.replaceAll("[^a-zA-Z_0-9]", "_");

				if (inputFilename.length() > 60) {
					System.out.println(user);
					System.out.println(paper);
					
					ZipFs zipFs = client.getDhcFile(user, paper);
					try {
						String newTitle = inputFilename.substring(0, 60);
	
						Document htmlFile = zipFs.getDocument(inputFilename + ".html");
						Element linkElement = DocumentUtil.getFirstMatch(
							htmlFile, "//xhtml:a[@href='"
							+ inputFilename + ".xml" 
							+"']", context);
						
						linkElement.getAttribute("href").setValue(newTitle+".xml");
						
						zipFs.putDocument(inputFilename +".html", htmlFile);
						zipFs.rename(inputFilename+".html", newTitle + ".html");
						zipFs.rename(inputFilename+".xml", newTitle + ".xml");
						
						try (FileOutputStream fos = new FileOutputStream(new File(args[1], newTitle + ".dhc"))) {
							fos.write(zipFs.toZipData());
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						try (FileOutputStream fos = new FileOutputStream(new File(args[1]+"failed/", inputFilename + ".dhc"))) {
							fos.write(zipFs.toZipData());
						}
					}
				}
			}
		}
		
		
//		try {
//			System.out.println(
//				new ConfToolClient(args[0], args[1].toCharArray()).getExportData(ExportType.subsumed_authors, null, "p");
//				.toXML());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
