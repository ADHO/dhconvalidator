package org.adho.dhconvalidator.util;

import org.adho.dhconvalidator.properties.PropertyKey;

public class DocumentLog {
	
	public static void logConversionStepOutput(String step, String output) {
		if (PropertyKey.logConversionStepOutput.isTrue()) {
			System.out.println(step);
			System.out.println(output);
		}
	}
}
