package org.adho.dhconvalidator.conversion;

import java.util.Properties;

import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.InputConverterFactory;
import org.adho.dhconvalidator.conversion.input.OdtInputConverter;
import org.adho.dhconvalidator.conversion.output.OdtOutputConverter;
import org.adho.dhconvalidator.conversion.output.OutputConverter;
import org.adho.dhconvalidator.conversion.output.OutputConverterFactory;
import org.adho.dhconvalidator.util.Pair;

@SuppressWarnings("unchecked")
public enum ConversionPath {

	ODT_TO_TEI( 
		Type.ODT.getIdentifier()+Type.TEI.getIdentifier(), 
		"odt",
		new InputConverterFactory() {
			public InputConverter createInputConverter() {
				return new OdtInputConverter();
			}
		},
		new OutputConverterFactory() {

			@Override
			public OutputConverter createOutputConverter() {
				return new OdtOutputConverter();
			}
		},
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
		new Pair<>("oxgarage.getOnlineImages", "false"),
		new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")
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
		new Pair<>("oxgarage.getOnlineImages", "false"),
		new Pair<>("pl.psnc.dl.ege.tei.profileNames", "dhconvalidator")
	),
	;
	
	private String path;
	private Properties properties;
	private InputConverterFactory inputConverterFactory;
	private OutputConverterFactory outputConverterFactory;
	private String defaultFileExt;
	
	private ConversionPath(
			String path,
			String defaultFileExt, 
			InputConverterFactory inputConverterFactory, 
			OutputConverterFactory outputConverterFactory,
			Pair<String,String>... propertyPairs) {
		
		this.path = path;
		this.defaultFileExt = defaultFileExt;
		this.inputConverterFactory = inputConverterFactory;
		this.outputConverterFactory = outputConverterFactory;
		this.properties = new Properties();
		if (propertyPairs != null) {
			for (Pair<String,String> pair : propertyPairs) {
				this.properties.setProperty(pair.getFirst(), pair.getSecond());
			}
		}
		
	}

	private ConversionPath(
			String path, String defaultFileExt,  InputConverterFactory inputConverter,
			Pair<String,String>... propertyPairs) {
		this(path, defaultFileExt, inputConverter, null, propertyPairs);
	}
	
	private ConversionPath(String path, String defaultFileExt,
			Pair<String,String>... propertyPairs) {
		this(path, defaultFileExt, null, null, propertyPairs);
	}

	public String getPath() {
		return path;
	}
	
	public Properties getDefaultProperties() {
		return properties;
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
	
	public InputConverterFactory getInputConverterFactory() {
		return inputConverterFactory;
	}
	
	public OutputConverterFactory getOutputConverterFactory() {
		return outputConverterFactory;
	}
}
