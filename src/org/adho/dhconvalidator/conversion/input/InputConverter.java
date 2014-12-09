package org.adho.dhconvalidator.conversion.input;

import java.io.IOException;

import org.adho.dhconvalidator.conftool.Paper;

public interface InputConverter {

	byte[] convert(byte[] sourceData) throws IOException;
	public byte[] getPersonalizedTemplate(Paper paper) throws IOException;
	String getFileExtension();

}
