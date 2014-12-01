package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.representation.StreamRepresentation;

public class ByteArrayStreamRepresentation extends StreamRepresentation {
	
	private byte[] streamData;
	
	public ByteArrayStreamRepresentation(byte[] streamData) {
		super(MediaType.APPLICATION_ALL);
		this.streamData = streamData;
	}
	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(streamData);
	}
	
	@Override
	public InputStream getStream() throws IOException {
		return new ByteArrayInputStream(streamData);
	}
}
