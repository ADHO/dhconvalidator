package org.adho.dhconvalidator.conversion;

import java.io.IOException;
import java.util.Properties;

import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.OdtInputConverter;
import org.adho.dhconvalidator.util.Pair;

@SuppressWarnings("unchecked")
public enum ConversionPath {

	ODT_TO_TEI( 
		Type.ODT.getIdentifier()+Type.TEI.getIdentifier(), 
		"odt",
		new OdtInputConverter(),
		new Pair<>("oxgarage.textOnly", "false"), 
		new Pair<>("oxgarage.getImages", "true"),
		new Pair<>("oxgarage.getOnlineImages", "true"),
		new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")
	),
	XHTML_TO_TEI( 
		Type.XHTML.getIdentifier()+Type.TEI.getIdentifier(), 
		"xhtml",
		new Pair<>("oxgarage.textOnly", "false"), 
		new Pair<>("oxgarage.getImages", "false"),
		new Pair<>("oxgarage.getOnlineImages", "false")
	),
	DOCX_TO_TEI( 
		Type.DOCX.getIdentifier()+Type.TEI.getIdentifier(),
		"docx",
		new Pair<>("oxgarage.textOnly", "false"), 
		new Pair<>("oxgarage.getImages", "true"),
		new Pair<>("oxgarage.getOnlineImages", "true"),
		new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")
	),
	TEI_TO_XHTML(
		Type.TEI.getIdentifier()+Type.XHTML.getIdentifier(),
		"tei",
		new Pair<>("oxgarage.textOnly", "false"), 
		new Pair<>("oxgarage.getImages", "false"),
		new Pair<>("oxgarage.getOnlineImages", "false")
	),
	;
	
	private String path;
	private Properties properties;
	private InputConverter inputConverter;
	private String defaultFileExt;
	
	private ConversionPath(
			String path, String defaultFileExt, InputConverter inputConverter, Pair<String,String>... pairs) {
		this.path = path;
		this.defaultFileExt = defaultFileExt;
		this.inputConverter = inputConverter;
		this.properties = new Properties();
		if (pairs != null) {
			for (Pair<String,String> pair : pairs) {
				this.properties.setProperty(pair.getFirst(), pair.getSecond());
			}
		}
		
	}
	
	private ConversionPath(String path, String defaultFileExt, Pair<String,String>... pairs) {
		this(path, defaultFileExt, null, pairs);
	}

	public String getPath() {
		return path;
	}
	
	public Properties getDefaultProperties() {
		return properties;
	}

	public byte[] applyInputConversions(byte[] sourceData) throws IOException {
		if (inputConverter !=null) {
			return inputConverter.convert(sourceData);
		}
		return sourceData;
	}
	
	public String getDefaultFileExt() {
		return defaultFileExt;
	}
	
	public static ConversionPath getConvertionPathByFilename(String filename) {
		for (ConversionPath path : values()) {
			if (path.getDefaultFileExt().equals(filename.substring(filename.lastIndexOf('.')+1))) {
				return path;
			}
		}
		
		throw new IllegalArgumentException("no conversion path found for " + filename);
	}
}