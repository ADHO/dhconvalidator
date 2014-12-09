package org.adho.dhconvalidator.ui;

import java.util.Properties;

public enum PropertyProvider {
	
	INSTANCE;
	
	private volatile Properties properties;
	
	public static Properties getProperties() {
		return INSTANCE.properties;
	}
	
	public static void setProperties(Properties properties) {
		INSTANCE.properties = properties;
	}

}
