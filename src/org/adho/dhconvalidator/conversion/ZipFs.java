/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;

import org.adho.dhconvalidator.Messages;
import org.apache.commons.io.IOUtils;

/**
 * A ZIP file system that handles ZIP based input files (.odt, .docx)
 * @author marco.petris@web.de
 *
 */
public class ZipFs {
	
	public Map<String, byte[]> content = new HashMap<String, byte[]>();

	/**
	 * @param zipData the zipped data
	 * @throws IOException in case of any failure
	 */
	public ZipFs(byte[] zipData) throws IOException {
		super();
		load(new ZipInputStream(new ByteArrayInputStream(zipData)));
	}

	/**
	 * @param zipData the zipped data
	 * @throws IOException in case of any failure
	 */
	public ZipFs(InputStream zipData) throws IOException {
		super();
		load(new ZipInputStream(zipData));
	}
	
	/**
	 * Start from scratch without input  data.
	 */
	public ZipFs() {
	}

	/**
	 * @param zipInputStream the zipped data 
	 * @throws IOException in case of any failure
	 */
	private void load(ZipInputStream zipInputStream) throws IOException {
		ZipEntry entry = null;
		while ((entry = zipInputStream.getNextEntry()) !=null) {
			ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
			IOUtils.copy(zipInputStream, entryBuffer);
			content.put(entry.getName(), entryBuffer.toByteArray());
		}
	}
	
	/**
	 * @param path the path of the requested document
	 * @return the document that lives under the given path in the ZIP fs.
	 * @throws IOException in case of any failure
	 */
	public Document getDocument(String path) throws IOException {
		Builder builder = new Builder();
		try {
			byte[] documentData = content.get(path);
			if (documentData == null) {
				throw new IOException(
					Messages.getString(
							"ZipFs.invalidDocument",//$NON-NLS-1$
							path)); 
			}
			return builder.build(new ByteArrayInputStream(content.get(path)));
		} catch (ParsingException e) {
			throw new IOException(e);
		}		
	}
	
	
	/**
	 * @param path the path of the document within the ZIP filesystem.
	 * @param document the document that should be added or that should replace an older
	 * version with the same path
	 * @throws IOException in case of any failure
	 */
	public void putDocument(String path, Document document) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(bos);
		serializer.setIndent(4);
		serializer.write(document);

		putDocument(path, bos.toByteArray());
	}
	
	/**
	 * @return the zipped data of this ZIP filesystem.
	 * @throws IOException in case of any failure
	 */
	public byte[] toZipData() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(bos)) {
			for (Map.Entry<String, byte[]> entry : content.entrySet()) {
				ZipEntry zipEntry = new ZipEntry(entry.getKey());
				zipOutputStream.putNextEntry(zipEntry);
				zipOutputStream.write(entry.getValue());
				zipOutputStream.closeEntry();
			}
		}
		
		return bos.toByteArray();
	}

	/**
	 * @param path the path of the document within the ZIP filesystem.
	 * @param document the document that should be added or that should replace an older
	 * version with the same path
	 */
	public void putDocument(String path, byte[] document) {
		content.put(path, document);
	}
}
