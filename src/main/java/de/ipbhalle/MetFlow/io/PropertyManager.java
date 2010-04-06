package de.ipbhalle.MetFlow.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import edu.emory.mathcs.backport.java.util.Collections;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertyManager.
 * Convenience class to load a properties file which stores specific settings.
 */
public class PropertyManager {

	/** The values. A Map of String keys and List of String values, as it is possible that one key has multiple values like supported DB's. */
	private Map<String, List<String>> values;
	
	/** The path. */
	private String path;
	
	/** The Constant SETTINGS_PATH. */
	public static final String SETTINGS_PATH = "de.ipbhalle.MetFlow.io.settings";
	
	/** The system's file separator. */
	private final String sep = System.getProperty("file.separator");
	
	
	/**
	 * Instantiates a new property manager.
	 */
	public PropertyManager() {
		this.setValues(new HashMap<String, List<String>>());
		this.path = ".";
		
		readProperties();
	}
	
	/**
	 * Instantiates a new property manager.
	 * 
	 * @param path the path
	 */
	public PropertyManager(String path) {
		this.path = (path.isEmpty() ? "." : path);
		
		readProperties();
	}
	
	/**
	 * Read properties.
	 */
	public void readProperties() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
		
		File propFile = null;
		if(path.equals(".")) {
			propFile = new File(sc.getRealPath("conf" + sep + "settings.properties"));
		}
		else 
			propFile = new File(path);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(propFile));
			String line = "";
			while ((line = br.readLine()) != null) {
				// skip comments and empty lines
				if(line.startsWith("#") || line.isEmpty())
					continue;
				
				String[] split = line.split("=");
				if(this.values.containsKey(split[0])) {	// add value to existing key
					List<String> keys = this.values.get(split[0]);
					keys.add(split[1]);
					this.values.put(split[0], keys);
				}
				else {
					List<String> keys = new ArrayList<String>();
					keys.add(split[1]);
					this.values.put(split[0], keys);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the list of values. If the key is present, the corresponding values are returned 
	 * inside a list of Strings. If the key is not present, a single item list will be returned
	 * containing the key as element.
	 * 
	 * @param key the key
	 * 
	 * @return the list of values for the key - if the key is not present, 
	 * return a list with singe item containing the key as value for error handling
	 */
	public List<String> getAllValues(String key) {
		if(values.containsKey(key)) {
			return values.get(key);
		}
		else {
			List<String> list = new ArrayList<String>();
			list.add(key);
			return list;
		}
	}
	
	/**
	 * Gets the first/single value. If the key is present, the corresponding value is returned 
	 * as String. If the key is not present, the key will be returned
	 * 
	 * @param key the key
	 * 
	 * @return the single value for the key - if the key is not present, 
	 * return the key as value for error handling
	 */
	public String getSingleValue(String key) {
		if(values.containsKey(key)) {
			return values.get(key).get(0);
		}
		else {
			return key;
		}
	}
	
	/**
	 * Store key-value pair. 
	 * If the key already exists, add the new values to the existing set of values.
	 * 
	 * @param key the key
	 * @param value the list of values
	 */
	public void storeKeyValue(String key, List<String> value) {
		// add key-value pair if key is not inside the map
		if(!values.containsKey(key)) {
			values.put(key, value);
		}
		else { // update key-value pair with pairs, do not remove any existing entries!
			List<String> entries = values.get(key);
			// if both lists are disjoint, add all elements from the new list into the existing list
			if(Collections.disjoint(entries, value))
				entries.addAll(value);
			else {	// if both list have items in common, only add new ones
				for (Iterator<String> it = value.iterator(); it.hasNext();) {
					String val = (String) it.next();
					if(!entries.contains(val))
						entries.add(val);					
				}
			}
		}
	}
	
	/**
	 * Sets the values.
	 * 
	 * @param values the values
	 */
	public void setValues(Map<String, List<String>> values) {
		this.values = values;
	}

	/**
	 * Gets the values.
	 * 
	 * @return the values
	 */
	public Map<String, List<String>> getValues() {
		return values;
	}

	/**
	 * Sets the path.
	 * 
	 * @param path the new path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Gets the path.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
}
