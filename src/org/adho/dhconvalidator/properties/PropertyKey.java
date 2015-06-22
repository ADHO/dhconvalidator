/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.properties;

import org.adho.dhconvalidator.paper.PaperProvider;
import org.adho.dhconvalidator.ui.PropertyProvider;
import org.adho.dhconvalidator.user.UserProvider;

/**
 * All supported property keys.
 * 
 * @author marco.petris@web.de
 *
 */
public enum PropertyKey {
	/**
	 * The ConfTool REST web service shared password.
	 */
	conftool_shared_pass,
	/**
	 * The ConfTool Login page URL. 
	 */
	conftool_login_url,
	/**
	 * The ConfTool REST web service URL
	 */
	conftool_rest_url,
	/**
	 * The OxGarage REST web service URL.
	 */
	oxgarage_url,
	/**
	 * The location (folder) of images referenced in the TEI.
	 */
	tei_image_location,
	/**
	 * The &lt;publicationStmt&gt;.
	 */
	publicationStmt, 
	/**
	 * The &lt;encodingDesc&gt;.
	 */
	encodingDesc,
	/**
	 * The DHConvalidator version.
	 */
	version,
	/**
	 * Image minimum resolutin width.
	 */
	image_min_resolution_width,
	/**
	 * Image minimum resolution height.
	 */
	image_min_resolution_height, 
	/**
	 * <code>true</code>->log the result of conversion steps
	 */
	logConversionStepOutput, 
	/**
	 * <code>true</code>->perform a schema validation against the DHConvalidator schema.
	 */
	performSchemaValidation,
	/**
	 * the base url of the DHConvalidator, e. g. http://localhost/dhconvalidator/
	 */
	base_url,
	/**
	 * <code>true</code> -> use DHConvalidator address generation with email for
	 * HTML output.
	 */
	html_address_generation, 
	/**
	 * <code>true</code> -> a link to the corresponding (same name) xml file is
	 * added to the HTML output.
	 */
	html_to_xml_link, 
	/**
	 * <code>true</code> -> list only accepted submissions
	 */
	showOnlyAcceptedPapers,
	/**
	 * Implementation of {@link UserProvider}.
	 */
	userProviderClass,
	/**
	 * Impelementation of {@link PaperProvider}
	 */
	paperProviderClass, 
	/**
	 * an extra admin account for development purposes, bypassing user management
	 */
	developeradmin,
	;

	
	/**
	 * @param defaultOnNullValue dafault value returned instead of <code>null</code>
	 * @return the value of this property or if value is <code>null</code> the defaultOnNullValue
	 */
	public String getValue(String defaultOnNullValue) {
		String value = getValue();
		if (value==null) {
			return defaultOnNullValue;
		}
		else {
			return value;
		}
	}
	
	/**
	 * @return the value of this property or <code>null</code>
	 */
	public String getValue() {
		return PropertyProvider.getProperties().getProperty(this.name());
	}
	
	/**
	 * @return <code>true</code> if the value of this property is true.
	 */
	public boolean isTrue() {
		return Boolean.valueOf(PropertyProvider.getProperties().getProperty(this.name()));
	}
	
	/**
	 * @return an instance of UserProvider using the implementation specified with {@link #userProviderClass}.
	 * @throws IllegalStateException in case of instantiation failure
	 */
	public static UserProvider getUserProviderInstance() throws IllegalStateException {
		try {
			Class<?> clazz = 
					Class.forName(
						userProviderClass.getValue(), 
						true, 
						PropertyKey.class.getClassLoader());
			return (UserProvider) clazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * @return an instance of PaperProvider using the implementation specified with {@link #paperProviderClass}.
	 * @throws IllegalStateException in case of instantiation failure
	 */
	public static PaperProvider getPaperProviderInstance() throws IllegalStateException {
		try {
			Class<?> clazz = 
					Class.forName(
						paperProviderClass.getValue(), 
						true, 
						PropertyKey.class.getClassLoader());
			return (PaperProvider) clazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
