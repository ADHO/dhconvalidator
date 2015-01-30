/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input;

import java.io.IOException;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;

/**
 * A converter that makes changes to the input file but keeps the input format.
 *  
 * @author marco.petris@web.de
 */
public interface InputConverter {

	/**
	 * Receives an input file in one of the supported formats, makes adjustments
	 * and returns the modified data in the original format.
	 * @param sourceData the input file
	 * @param user the user that initiated the conversion
	 * @return the modified input file
	 * @throws IOException in case of any failure
	 */
	byte[] convert(byte[] sourceData, User user) throws IOException;
	/**
	 * @param paper
	 * @return a template file for the given paper.
	 * @throws IOException in case of any failure
	 */
	public byte[] getPersonalizedTemplate(Paper paper) throws IOException;
	/**
	 * @return the file extension of the input format
	 */
	String getFileExtension();
	
	/** 
	 * @return the paper that gets loaded during conversion 
	 * or <code>null</code> if not yet available (prior to conversion)
	 */
	public Paper getPaper();
	
	/**
	 * @return a short info about what Editing systems are support by the file type of this converter.
	 */
	public String getTextEditorDescription();

}
