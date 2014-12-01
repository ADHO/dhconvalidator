package org.adho.dhconvalidator.conversion;

import java.io.IOException;

public interface InputConverter {

	byte[] convert(byte[] sourceData) throws IOException;

}
