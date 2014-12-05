package org.adho.dhconvalidator.util;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

public class DocumentUtil {
	public static Element getFirstMatch(Document resultDoc, String query) {
		Nodes nodes = resultDoc.query(query);
		if ((nodes.size() > 0) && (nodes.get(0) instanceof Element)) {
			return (Element)nodes.get(0);
		}
		
		throw new IllegalStateException("unexpected result");
	}
}
