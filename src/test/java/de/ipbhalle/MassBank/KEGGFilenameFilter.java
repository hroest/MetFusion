/**
 * created by Michael Gerlich, Jun 6, 2012 - 1:39:22 PM
 */ 

package de.ipbhalle.MassBank;

import java.io.File;
import java.io.FilenameFilter;

public class KEGGFilenameFilter implements FilenameFilter {

	private String filter = "";
	
	public KEGGFilenameFilter(String filter) {
		this.filter = filter;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		if(!filter.isEmpty()) {
			if(name.contains(filter))
				return true;
			else return false;
		}
		else return true;
	}

}
