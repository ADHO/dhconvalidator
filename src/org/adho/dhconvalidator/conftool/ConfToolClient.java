package org.adho.dhconvalidator.conftool;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.adho.dhconvalidator.util.DocumentUtil;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

public class ConfToolClient {
	public static final class AuthenticationException extends Exception {

		public AuthenticationException() {
			super();
		}
	}
	
	private char[] restSharedPass;
	private String confToolUrl;

	public ConfToolClient(String confToolUrl, char[] restSharedPass) {
		this.confToolUrl = confToolUrl;
		this.restSharedPass = restSharedPass;
	}
	
	private String getPassHash(String nonce) {
		return Hashing.sha256().hashString(
			nonce+new String(restSharedPass), Charsets.UTF_8).toString();
	}
	
	public String getDetails(String user) throws IOException {
		String nonce = String.valueOf(System.nanoTime());
		
		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=remoteLogin");
		urlBuilder.append("&nonce=");
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash=");
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&user=");
		urlBuilder.append(user);
		urlBuilder.append("&command=request");
		
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
	
	public List<User> getAuthors() throws IOException {
		return new DocumentToUserMapper().getUserList(
					getExportData(ExportType.subsumed_authors));
	}
	
	public List<Paper> getPapers() throws IOException {
		return new DocumentToPaperMapper().getPaperList(
				getExportData(ExportType.papers));
	}
	
	private Document getExportData(ExportType type) throws IOException {
		String nonce = String.valueOf(System.nanoTime());
		
		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=adminExport");
		urlBuilder.append("&nonce=");
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash=");
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&export_select=");
		urlBuilder.append(type.name());
		urlBuilder.append("&form_include_deleted=0");
		urlBuilder.append("&form_export_format=xml");
		urlBuilder.append("&form_export_header=default");
		urlBuilder.append("&cmd_create_export=true");
		
		ClientResource client = 
				new ClientResource(Context.getCurrent(), Method.GET, urlBuilder.toString());
		
		Representation result = client.get();

		try (InputStream resultStream = result.getStream()) {
			Builder builder = new Builder();
			Document resultDoc = builder.build(resultStream);
			return resultDoc;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public User authenticate(String user, char[] pass) 
			throws IOException, AuthenticationException {
		String nonce = String.valueOf(System.nanoTime());
		
		StringBuilder urlBuilder = new StringBuilder(confToolUrl);
		urlBuilder.append("?page=remoteLogin");
		urlBuilder.append("&nonce=");
		urlBuilder.append(nonce);
		urlBuilder.append("&passhash=");
		urlBuilder.append(getPassHash(nonce));
		urlBuilder.append("&user=");
		urlBuilder.append(user);
		urlBuilder.append("&command=login");
		urlBuilder.append("&password=");
		urlBuilder.append(pass);
		
		ClientResource client = 
				new ClientResource(Context.getCurrent(), Method.GET, urlBuilder.toString());
		
		Representation result = client.get();

		try (InputStream resultStream = result.getStream()) {
			Builder builder = new Builder();
			Document resultDoc = builder.build(resultStream);
			System.out.println(resultDoc.toXML());
			if (getLoginResult(resultDoc)) {
				return getUser(resultDoc);
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}

		throw new AuthenticationException();
	}
	
	private String getUserId(Document resultDoc) {
		Element resultElement = DocumentUtil.getFirstMatch(resultDoc, "/login/id");
		return resultElement.getValue();
	}	
	
	private String getUserName(Document resultDoc) {
		Element resultElement = DocumentUtil.getFirstMatch(resultDoc, "/login/username");
		return resultElement.getValue();
	}
	
	private User getUser(Document resultDoc) {
		return new User(Integer.valueOf(getUserId(resultDoc)), getUserName(resultDoc));
	}
	
	private boolean getLoginResult(Document resultDoc) {
		Element resultElement = DocumentUtil.getFirstMatch(resultDoc, "/login/result");
		return Boolean.valueOf(resultElement.getValue());
	}
	
}
