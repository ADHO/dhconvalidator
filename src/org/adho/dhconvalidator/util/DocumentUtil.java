/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.util;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.adho.dhconvalidator.Messages;

public class DocumentUtil {
	/**
	 * @param document the document to search in
	 * @param query the search query
	 * @return the first match
	 * @throws IllegalStateException if there is no such match
	 */
	public static Element getFirstMatch(Document document, String query) {
		return getFirstMatch(document, query, null);
	}

	/**
	 * @param document the document to search in
	 * @param query the search query
	 * @param xPathContext the context for the query
	 * @return the first match
	 * @throws IllegalStateException if there is no such match
	 */
	public static Element getFirstMatch(Document document, String query,
			XPathContext xPathContext) {
		Nodes nodes = document.query(query, xPathContext);
		if ((nodes.size() > 0) && (nodes.get(0) instanceof Element)) {
			return (Element)nodes.get(0);
		}
		
		throw new IllegalStateException(
			Messages.getString("DocumentUtil.unexpectedResult")); //$NON-NLS-1$
	}
	
	/**
	 * @param subtreeRoot the subtree to search in
	 * @param query the search query
	 * @param xPathContext the context for the query
	 * @return the first match
	 * @throws IllegalStateException if there is no such match
	 */
	public static Element getFirstMatch(Element subtreeRoot, String query,
			XPathContext xPathContext) {
		Nodes nodes = subtreeRoot.query(query, xPathContext);
		if ((nodes.size() > 0) && (nodes.get(0) instanceof Element)) {
			return (Element)nodes.get(0);
		}
		
		throw new IllegalStateException(
			Messages.getString("DocumentUtil.unexpectedResult")); //$NON-NLS-1$
	}
	
	/**
	 * @param document the document to search in
	 * @param query the search query
	 * @param xPathContext the context for the query
	 * @return the first match or <code>null</code> if there is no match
	 */
	public static Element tryFirstMatch(Document document, String query,
			XPathContext xPathContext) {
		Nodes nodes = document.query(query, xPathContext);
		if ((nodes.size() > 0) && (nodes.get(0) instanceof Element)) {
			return (Element)nodes.get(0);
		}
		else {
			return null;
		}
	}
	
	/**
	 * @param subtreeRoot the subtree to search in
	 * @param query the search query
	 * @param xPathContext the context for the query
	 * @return the first match or <code>null</code> if there is no match	 */
	public static Element tryFirstMatch(Element subtreeRoot, String query,
			XPathContext xPathContext) {
		Nodes nodes = subtreeRoot.query(query, xPathContext);
		if ((nodes.size() > 0) && (nodes.get(0) instanceof Element)) {
			return (Element)nodes.get(0);
		}
		else {
			return null;
		}
	}
	
	/**
	 * @param subtreeRoot the subtree to search in
	 * @param query the search query
	 * @param xPathContext the context for the query
	 * @return <code>true</code> if there is a match
	 */
	public static boolean hasMatch(Element subtreeRoot, String query,
			XPathContext xPathContext) {
		return tryFirstMatch(subtreeRoot, query, xPathContext) != null;
	}

}
