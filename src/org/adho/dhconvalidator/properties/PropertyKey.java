package org.adho.dhconvalidator.properties;

import org.adho.dhconvalidator.ui.PropertyProvider;

public enum PropertyKey {
	conftool_shared_pass,
	conftool_url,
	oxgarage_url,
	tei_image_location,
	publicationStmt, 
	encodingDesc,
	version,
	image_min_resolution_width,
	image_min_resolution_height, 
	logConversionStepOutput, 
	performSchemaValidation,
	;
	
	public String getValue() {
		return PropertyProvider.getProperties().getProperty(this.name());
	}
	
	public boolean isTrue() {
		return Boolean.valueOf(PropertyProvider.getProperties().getProperty(this.name()));
	}
}
