package org.adho.dhconvalidator.conftool;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.adho.dhconvalidator.properties.PropertyKey;

public class ConfToolCacheLoaderInitializerServlet extends HttpServlet {
	
    @Override
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);

        try {
        	Timer timer = new Timer();
        	final ConfToolCache confToolCache = 
        		new ConfToolCache(
        			PropertyKey.conftool_url.getValue(),
        			PropertyKey.conftool_shared_pass.getValue().toCharArray());
        	ConfToolCacheProvider.INSTANCE.setConfToolCache(confToolCache);
        	timer.schedule(new TimerTask() {
        		@Override
        		public void run() {
        			try {
						confToolCache.load();
					} catch (IOException e) {
						Logger.getLogger(
							ConfToolCacheProvider.class.getName()).log(
								Level.SEVERE, "error loading conftool cache", e);
					}
        		}
        	}, 1000, 3600000);
        }
        catch (Exception e) {
        	throw new ServletException(e);
        }
	}
}
