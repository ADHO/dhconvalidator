package org.adho.dhconvalidator.conversion;

import java.util.Properties;

@SuppressWarnings("unchecked")
public enum ConversionPath {

	ODT_TO_TEI( 
		Type.ODT.getIdentifier()+Type.TEI.getIdentifier(), 
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

	private ConversionPath(String path, Pair<String,String>... pairs) {
		this.path = path;
		properties = new Properties();
		if (pairs != null) {
			for (Pair<String,String> pair : pairs) {
				properties.setProperty(pair.getFirst(), pair.getSecond());
			}
		}
		
	}
	
	public String getPath() {
		return path;
	}
	
	public Properties getDefaultProperties() {
		return properties;
	}
}
