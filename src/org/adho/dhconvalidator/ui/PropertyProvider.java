/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import java.util.Properties;

/**
 * Property container.
 * 
 * @author marco.petris@web.de
 *
 */
public enum PropertyProvider {
	
	INSTANCE;
	
	private volatile Properties properties;
	
	/**
	 * threadsafe
	 * 
	 * @return the readonly properties
	 */
	public static Properties getProperties() {
		return INSTANCE.properties;
	}
	
	/**
	 * not threadsafe
	 * 
	 * @param properties
	 */
	public static void setProperties(Properties properties) {
		INSTANCE.properties = properties;
	}

}
