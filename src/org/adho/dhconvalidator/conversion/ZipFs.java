package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;

import org.apache.commons.io.IOUtils;

public class ZipFs {
	
	public Map<String, byte[]> content = new HashMap<String, byte[]>();

	public ZipFs(byte[] zipData) throws IOException {
		super();
		load(zipData);
	}

	private void load(byte[] zipData) throws IOException {
		ZipInputStream zipInputStream = 
				new ZipInputStream(new ByteArrayInputStream(zipData));
		
		ZipEntry entry = null;
		while ((entry = zipInputStream.getNextEntry()) !=null) {
			ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
			IOUtils.copy(zipInputStream, entryBuffer);
			content.put(entry.getName(), entryBuffer.toByteArray());
		}
	}
	
	public Document getDocument(String path) throws IOException {
		Builder builder = new Builder();
		try {
			return builder.build(new ByteArrayInputStream(content.get(path)));
		} catch (ParsingException e) {
			throw new IOException(e);
		}		
	}
	
	
	public void putDocument(String path, Document document) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(bos);
		serializer.setIndent(2);
		serializer.write(document);

		content.put(path, bos.toByteArray());
	}
	
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
}
