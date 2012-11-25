package org.openntf.logging.listeners;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.openntf.logging.config.SystemConfiguration;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener;

public class InitializationListener implements ApplicationListener {

	public void applicationCreated(ApplicationEx arg0) {
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		try {
			Enumeration<URL> x = cl.getResources("logging-appenders.properties");
			
			if (x.hasMoreElements()) {
				URL url = x.nextElement();
				InputStream stream = url.openStream();
				try {
					if (stream != null) {
						Properties props = new Properties();
						props.load(stream);
						String appenderName = "appender.A1";
						int i = 1;
						ArrayList<String> classNames = new ArrayList<String>();
						while (props.containsKey(appenderName)) {
							String propertyName = (String) props.get(appenderName);
							classNames.add(propertyName);
							i++;
							appenderName = appenderName.substring(0, appenderName.length()-1) + i;
						}
						
						SystemConfiguration.setImplementations(classNames);
						if (props.containsKey("config.diagnostic")) {
							Boolean diag = Boolean.valueOf(props.get("config.diagnostic").toString());
							SystemConfiguration.setDiagnositc(diag);
						}
					} else {
						System.out.println("Couldn't find property file logging-appenders.properties");
					}
				} finally {
					stream.close();
				}
			} else {
				System.out.println("Couldn't find property file logging-appenders.properties");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	public void applicationDestroyed(ApplicationEx arg0) {
		
	}
}
