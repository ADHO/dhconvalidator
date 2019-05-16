/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.properties;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.adho.dhconvalidator.ui.PropertyProvider;

/**
 * A servlet that loads the properties and makes them available via {@link PropertyKey#getValue()}
 *
 * @author marco.petris@web.de
 */
public class PropertiesInitializerServlet extends HttpServlet {

  @Override
  public void init(ServletConfig cfg) throws ServletException {
    super.init(cfg);

    try {
      String propertiesFile =
          System.getProperties().containsKey("dhconvalidatorproperties")
              ? System.getProperties().getProperty("dhconvalidatorproperties")
              : "dhconvalidator.properties";

      Properties properties = new Properties();

      properties.load(new InputStreamReader(new FileInputStream(cfg.getServletContext().getRealPath(propertiesFile)), "UTF8"));

      HashMap<Object, Object> propertyBuffer = new HashMap<>();

      for (@SuppressWarnings("rawtypes") Entry entry : properties.entrySet()) {
        propertyBuffer.put(entry.getKey(), entry.getValue());
      }

      PropertyProvider.setProperties(new NonModifiableProperties(propertyBuffer));
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
