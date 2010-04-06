package de.ipbhalle.MetFlow.io;

import java.io.File;
import java.io.FilenameFilter;


// TODO: Auto-generated Javadoc
/**
 * The Class MyFilter.
 */
public class MyFilter implements FilenameFilter{

	/** The infix. */
	private String infix;
	
	/** The end. */
	private String end;

	
	/**
	 * Instantiates a new my filter.
	 */
	public MyFilter() {
		this.infix = "";
		this.end = "";
	}
	
	/**
	 * Instantiates a new my filter.
	 * 
	 * @param _infix the _infix
	 * @param _end the _end
	 */
	public MyFilter(String _infix, String _end) {
		this.infix = _infix;
		this.end = _end;
	}

	/* (non-Javadoc)
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File f, String name) {
		
		return (name.startsWith(this.infix) && name.endsWith(this.end));
	}
	
	/**
	 * Sets the infix.
	 * 
	 * @param infix the new infix
	 */
	public void setInfix(String infix) {
		this.infix = infix;
	}

	/**
	 * Gets the infix.
	 * 
	 * @return the infix
	 */
	public String getInfix() {
		return infix;
	}

	/**
	 * Sets the end.
	 * 
	 * @param end the new end
	 */
	public void setEnd(String end) {
		this.end = end;
	}

	/**
	 * Gets the end.
	 * 
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}
}
