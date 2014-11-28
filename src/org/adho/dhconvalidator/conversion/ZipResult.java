package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import org.apache.commons.io.IOUtils;

public class ZipResult {
	
	private Document document;
	private Map<String, byte[]> externalResources;
	
	public ZipResult(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		IOUtils.copy(is, buffer);
		
		if (isZipFile(buffer)) {
			externalResources = new HashMap<String, byte[]>();
			handleZipFile(buffer);
		}
		else {
			externalResources = Collections.emptyMap();

			buildDocument(buffer);
		}
	}

	private void buildDocument(ByteArrayOutputStream buffer) throws IOException {
		Builder builder = new Builder();
		try {
			document = builder.build(new ByteArrayInputStream(buffer.toByteArray()));
		} catch (ParsingException e) {
			throw new IOException(e);
		}		
	}

	private void handleZipFile(ByteArrayOutputStream buffer) throws IOException {
		ZipInputStream zipInputStream = 
				new ZipInputStream(new ByteArrayInputStream(buffer.toByteArray()));
		
		ZipEntry entry = null;
		while ((entry = zipInputStream.getNextEntry()) !=null) {
			ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
			IOUtils.copy(zipInputStream, entryBuffer);
			if (entry.getName().contains("/")) {
				externalResources.put(entry.getName(), entryBuffer.toByteArray());
			}
			else {
				buildDocument(entryBuffer);
			}
		}
		
	}

	private boolean isZipFile(ByteArrayOutputStream buffer) {
		if (buffer.size() > 4) {
			byte[] testArray = buffer.toByteArray();
			return testArray[0]==0x50&&testArray[1]==0x4b
					&&testArray[2]==0x03&&testArray[3]==0x04;
		}
		return false;
	}

	public Document getDocument() {
		return document;
	}

	public byte[] getExternalResource(String resourceKey) {
		return externalResources.get(resourceKey);
	}
}
