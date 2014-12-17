package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Document;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;

public interface OutputConverter {
	public void convert(Document document, User user, Paper paper) throws IOException;
}
