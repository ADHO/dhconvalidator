package org.adho.dhconvalidator.conversion;

import java.io.IOException;
import java.util.Properties;

@SuppressWarnings("unchecked")
public enum ConversionPath {

	ODT_TO_TEI( 
		Type.ODT.getIdentifier()+Type.TEI.getIdentifier(), 
		new OdtConverter(),
		new Pair<>("oxgarage.textOnly", "false"), 
		new Pair<>("oxgarage.getImages", "true"),
		new Pair<>("oxgarage.getOnlineImages", "true"),
		new Pair<>("pl.psnc.dl.ege.tei.profileNames", "adhoconf")
	),
	XHTML_TO_TEI( 
		Type.XHTML.getIdentifier()+Type.TEI.getIdentifier(), 
		new Pair<>("oxgarage.textOnly", "false"), 
		new Pair<>("oxgarage.getImages", "false"),
		new Pair<>("oxgarage.getOnlineImages", "false")
	),
	DOCX_TO_TEI( 
		Type.DOCX.getIdentifier()+Type.TEI.getIdentifier()
	),
	TEI_TO_XHTML(
		Type.TEI.getIdentifier()+Type.XHTML.getIdentifier(),
		new Pair<>("oxgarage.textOnly", "false"), 
		new Pair<>("oxgarage.getImages", "false"),
		new Pair<>("oxgarage.getOnlineImages", "false")
	),
	;
	
	private String path;
	private Properties properties;
	private InputConverter inputConverter;
	
	private ConversionPath(
			String path, InputConverter inputConverter, Pair<String,String>... pairs) {
		this.path = path;
		this.inputConverter = inputConverter;
		this.properties = new Properties();
		if (pairs != null) {
			for (Pair<String,String> pair : pairs) {
				this.properties.setProperty(pair.getFirst(), pair.getSecond());
			}
		}
		
	}
	
	private ConversionPath(String path, Pair<String,String>... pairs) {
		this(path, null, pairs);
	}

	public String getPath() {
		return path;
	}
	
	public Properties getDefaultProperties() {
		return properties;
	}

	public byte[] applyInputFormatConversions(byte[] sourceData) throws IOException {
		if (inputConverter !=null) {
			return inputConverter.convert(sourceData);
		}
		return sourceData;
	}
}
