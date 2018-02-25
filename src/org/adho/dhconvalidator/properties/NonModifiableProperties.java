/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.adho.dhconvalidator.Messages;

/**
 * Read only {@link Properties}.
 *
 * @author marco.petris@web.de
 */
public class NonModifiableProperties extends Properties {

  private Map<Object, Object> delegate;

  /** @param delegate the original properties */
  public NonModifiableProperties(Map<Object, Object> delegate) {
    super();
    this.delegate = delegate;
  }

  // suppress modification

  @Override
  public synchronized Object setProperty(String key, String value) {
    throw new UnsupportedOperationException(Messages.getString("NonModifiableProperties.0"));
  }

  @Override
  public synchronized void load(Reader reader) throws IOException {
    throw new UnsupportedOperationException(Messages.getString("NonModifiableProperties.0"));
  }

  @Override
  public synchronized void load(InputStream inStream) throws IOException {
    throw new UnsupportedOperationException(Messages.getString("NonModifiableProperties.0"));
  }

  @Override
  public synchronized void loadFromXML(InputStream in)
      throws IOException, InvalidPropertiesFormatException {
    throw new UnsupportedOperationException(Messages.getString("NonModifiableProperties.0"));
  }

  // delegation

  @Override
  public String getProperty(String key) {
    Object oval = get(key);
    String sval = (oval instanceof String) ? (String) oval : null;
    return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
  }

  public int size() {
    return delegate.size();
  }

  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  public Object get(Object key) {
    return delegate.get(key);
  }

  public Object put(Object key, Object value) {
    return delegate.put(key, value);
  }

  public Object remove(Object key) {
    return delegate.remove(key);
  }

  public void putAll(Map<? extends Object, ? extends Object> m) {
    delegate.putAll(m);
  }

  public void clear() {
    delegate.clear();
  }

  public Set<Object> keySet() {
    return delegate.keySet();
  }

  public Collection<Object> values() {
    return delegate.values();
  }

  public Set<java.util.Map.Entry<Object, Object>> entrySet() {
    return delegate.entrySet();
  }

  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  public int hashCode() {
    return delegate.hashCode();
  }
}
