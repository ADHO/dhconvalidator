package org.adho.dhconvalidator.conversion.input;

import java.io.IOException;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;

public interface InputConverter {

	byte[] convert(byte[] sourceData, User user) throws IOException;
	public byte[] getPersonalizedTemplate(Paper paper) throws IOException;
	String getFileExtension();
	public Paper getPaper();

}
