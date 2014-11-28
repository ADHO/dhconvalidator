package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

public class HTMLToXHTMLConverter {

	public void convert(InputStream in, OutputStream out) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		Tidy tidy = new Tidy();
		tidy.setShowWarnings(true);
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setXHTML(true);
		tidy.setMakeClean(true);
		System.out.println("first pass");
		tidy.parseDOM(in, buffer);
		System.out.println("second pass");
		tidy.setXmlTags(true);
		Document xmlDoc = tidy.parseDOM(new ByteArrayInputStream(buffer.toByteArray()), out);
//		tidy.pprint(xmlDoc, out);
	}
}
