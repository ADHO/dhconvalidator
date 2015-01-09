package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Document;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;

public interface OutputConverter {
	public void convert(Document document, User user, Paper paper) throws IOException;
	public void convert(ZipResult zipResult) throws IOException;
}
