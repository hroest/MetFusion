package de.ipbhalle.MetFlow.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * The Class T2WorkflowListing.
 */
public class T2WorkflowListing {

	/** The urls. */
	private List<String> urls = null;
	
	/** The path. */
	private String path;

	/** The system's file separator. */
	private final String sep = System.getProperty("file.separator");
	
	/**
	 * Instantiates a new t2 workflow listing.
	 */
	public T2WorkflowListing() {
		this.path = ".";
	}
	
	/**
	 * Instantiates a new t2 workflow listing.
	 * 
	 * @param path the path
	 */
	public T2WorkflowListing(String path) {
		this.path = path;
	}


	/**
	 * List workflows.
	 * 
	 * @param infix the infix
	 * @param ending the ending
	 * 
	 * @return the list< string>
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<String> listWorkflows(String infix, String ending)
			throws IOException {
		File f = new File(this.path);		
		String[] files = f.list(new MyFilter(infix, ending));
		
		if (files.length > 0) {
			this.urls = new ArrayList<String>(files.length);

			for (int i = 0; i < files.length; i++) {
				File temp = new File(f + sep + files[i]);
				String t = temp.getName();
				//t = "http://msbi.ipb-halle.de/taverna/" + t;
				this.urls.add(t);
			}
			Collections.sort(urls);

			return urls;
		}
		else {
			// return empty list set rather than a null object
			return new ArrayList<String>();
		}
	}


	/**
	 * Sets the urls.
	 * 
	 * @param urls the new urls
	 */
	public void setUrls(List<String> urls) {
		this.urls = urls;
	}


	/**
	 * Gets the urls.
	 * 
	 * @return the urls
	 */
	public List<String> getUrls() {
		return urls;
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
