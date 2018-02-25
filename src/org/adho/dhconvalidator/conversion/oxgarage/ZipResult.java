/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.oxgarage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import org.apache.commons.io.IOUtils;

/**
 * A container for the result files to be returned to the user as a ZIP file with .dhc extension.
 *
 * @author marco.petris@web.de
 */
public class ZipResult {

  private Document document;
  private Map<String, byte[]> externalResources;
  private String documentName;

  /**
   * @param is the source to read from (zipped input stream)
   * @throws IOException in case of any failure
   */
  public ZipResult(InputStream is) throws IOException {
    this(is, null);
  }

  /**
   * @param is the source to read from (zipped input stream)
   * @param documentName the name that should be used for the TEI file.
   * @throws IOException in case of any failure
   */
  public ZipResult(InputStream is, String documentName) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    IOUtils.copy(is, buffer);

    externalResources = new HashMap<String, byte[]>();
    if (isZipFile(buffer)) { // is the source a ZIP file or ...
      extractZipFile(buffer);
    } else { // ... do we have a single file?
      buildDocument(buffer);
    }
    if (documentName != null) {
      this.documentName = documentName;
    }
  }

  /**
   * @param buffer the source for the TEI file
   * @throws IOException in case of any failure
   */
  private void buildDocument(ByteArrayOutputStream buffer) throws IOException {
    Builder builder = new Builder();
    try {
      document = builder.build(new ByteArrayInputStream(buffer.toByteArray()));
    } catch (ParsingException e) {
      throw new IOException(e);
    }
  }

  /**
   * Decompress content.
   *
   * @param buffer zipped source
   * @throws IOException in case of any failure
   */
  private void extractZipFile(ByteArrayOutputStream buffer) throws IOException {
    ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(buffer.toByteArray()));

    ZipEntry entry = null;
    while ((entry = zipInputStream.getNextEntry()) != null) {
      ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
      IOUtils.copy(zipInputStream, entryBuffer);
      if (entry.getName().contains("/")) { // $NON-NLS-1$
        externalResources.put(entry.getName(), entryBuffer.toByteArray());
      } else {
        documentName = entry.getName();
        buildDocument(entryBuffer);
      }
    }
  }

  /**
   * @param buffer
   * @return <code>true</code> if the given buffer contains a ZIP file
   */
  private boolean isZipFile(ByteArrayOutputStream buffer) {
    if (buffer.size() > 4) {
      byte[] testArray = buffer.toByteArray();
      return testArray[0] == 0x50
          && testArray[1] == 0x4b
          && testArray[2] == 0x03
          && testArray[3] == 0x04;
    }
    return false;
  }

  /** @return the TEI document */
  public Document getDocument() {
    return document;
  }

  /**
   * @param resourceKey path+name of the external resource
   * @return the resource data
   */
  public byte[] getExternalResource(String resourceKey) {
    return externalResources.get(resourceKey);
  }

  /**
   * Moves the resource that can be found at oldResourceKey to newResourceKey
   *
   * @param oldResourceKey
   * @param newResourceKey
   */
  public void moveExternalResource(String oldResourceKey, String newResourceKey) {
    externalResources.put(newResourceKey, externalResources.remove(oldResourceKey));
  }

  /**
   * @return this container as compressed data
   * @throws IOException in case of any failure
   */
  public byte[] toZipData() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(bos)) {

      ZipEntry docEntry = new ZipEntry(documentName);
      zipOutputStream.putNextEntry(docEntry);
      Serializer serializer = new Serializer(zipOutputStream);
      serializer.setIndent(4);
      serializer.write(document);
      zipOutputStream.closeEntry();

      for (Map.Entry<String, byte[]> entry : externalResources.entrySet()) {
        ZipEntry extResourceEntry = new ZipEntry(entry.getKey());
        zipOutputStream.putNextEntry(extResourceEntry);
        zipOutputStream.write(entry.getValue());
        zipOutputStream.closeEntry();
      }
    }

    return bos.toByteArray();
  }

  /**
   * @param pathPart the beginning of the path
   * @return all resources that have a path that matches the given path part
   */
  public List<String> getExternalResourcePathsStartsWith(String pathPart) {
    ArrayList<String> result = new ArrayList<>();
    for (String path : externalResources.keySet()) {
      if (path.startsWith(pathPart)) {
        result.add(path);
      }
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Adds a resource with the given path and the given data.
   *
   * @param path path+name
   * @param data
   */
  public void putResource(String path, byte[] data) {
    externalResources.put(path, data);
  }

  public String getDocumentName() {
    return documentName;
  }
}
