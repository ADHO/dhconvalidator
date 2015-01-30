/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.properties;

import org.adho.dhconvalidator.ui.PropertyProvider;

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
	;
	
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
}
