/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.representation.StreamRepresentation;

/**
 * A byte[] backed {@link StreamRepresentation}.
 * 
 * @author marco.petris@web.de
 *
 */
public class ByteArrayStreamRepresentation extends StreamRepresentation {
	
	private byte[] streamData;
	
	/**
	 * @param streamData the stream data
	 */
	public ByteArrayStreamRepresentation(byte[] streamData) {
		super(MediaType.APPLICATION_ALL);
		this.streamData = streamData;
	}
	/* (non-Javadoc)
	 * @see org.restlet.representation.Representation#write(java.io.OutputStream)
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(streamData);
	}
	
	/* (non-Javadoc)
	 * @see org.restlet.representation.Representation#getStream()
	 */
	@Override
	public InputStream getStream() throws IOException {
		return new ByteArrayInputStream(streamData);
	}
}
