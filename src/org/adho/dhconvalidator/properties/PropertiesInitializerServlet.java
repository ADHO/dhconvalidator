package org.adho.dhconvalidator.properties;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.adho.dhconvalidator.ui.PropertyProvider;

public class PropertiesInitializerServlet extends HttpServlet {
	
    @Override
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);

        try {
			String propertiesFile = 
					System.getProperties().containsKey("dhconvalidatorproperties") ? System.getProperties().getProperty( //$NON-NLS-1$
					"dhconvalidatorproperties") : "dhconvalidator.properties"; //$NON-NLS-1$ //$NON-NLS-2$

			Properties properties = new Properties();
	
			properties.load(
				new FileInputStream(
					cfg.getServletContext().getRealPath(propertiesFile)));
	
			HashMap<Object,Object> propertyBuffer = new HashMap<>();
			
			for (@SuppressWarnings("rawtypes") Entry entry : properties.entrySet()) {
				propertyBuffer.put(entry.getKey(), entry.getValue());
			}
			
			PropertyProvider.setProperties(new NonModifiableProperties(propertyBuffer));
        }
        catch (Exception e) {
        	throw new ServletException(e);
        }
	}
}
