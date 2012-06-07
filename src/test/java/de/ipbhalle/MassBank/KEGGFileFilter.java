/**
 * created by Michael Gerlich, Jun 6, 2012 - 11:10:04 AM
 */

package de.ipbhalle.MassBank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KEGGFileFilter implements FileFilter {

	public List<String> ids = new ArrayList<String>();
	public Map<String, String> idMap = new HashMap<String, String>();
	private boolean unique = false;
	
	public KEGGFileFilter(boolean uniqueOnly) {
		this.unique = uniqueOnly;
	}
	
	@Override
	public boolean accept(File pathname) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(pathname));
			String line = "";
			boolean found = false;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("CH$LINK") && line.contains("KEGG")) {
					String id = line.substring(line.indexOf("KEGG") + 5).trim();
					idMap.put(pathname.getName().substring(0, pathname.getName().lastIndexOf(".")), id);
					if(this.unique) {
						if(!ids.contains(id)) {		// only add unique identifiers
							ids.add(id);
							found = true;
							break;
						}
					}
					else {							// add all identifier, non-unique
						ids.add(id);
						found = true;
						break;
					}
				}
			}
			br.close();
			if (found)
				return true;
			else
				return false;
		} catch (FileNotFoundException e) {
			System.err.println("File " + pathname.getAbsolutePath() + " not found!");
			return false;
		} catch (IOException e) {
			System.err.println("IOException for " + pathname.getAbsolutePath());
			return false;
		}
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public Map<String, String> getIdMap() {
		return idMap;
	}

	public void setIdMap(Map<String, String> idMap) {
		this.idMap = idMap;
	}

}
