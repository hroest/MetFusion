/**
 * created by Michael Gerlich, Aug 16, 2011 - 10:39:14 AM
 */ 

package de.ipbhalle.metfusion.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

@ManagedBean(eager=true)
@ApplicationScoped
public class PropertiesBean {
	
	public final String sep = System.getProperty("file.separator");
	
	private Properties properties;
	private String webRoot;
	private String propFile;
	
	public PropertiesBean() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
		this.webRoot = scontext.getRealPath(sep);
		this.propFile = webRoot + "WEB-INF/settings.properties";
		getConfig();
	}
	
	private void getConfig()
	{
		this.properties = new Properties();
		File propFile = new File(this.propFile);
		InputStream is = null;
		try {
			is = new FileInputStream(propFile);
			properties.load(is);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Properties file [" + propFile.getAbsolutePath() + "] not found!");
		}
		finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Property file has been successfully loaded!");
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}
}