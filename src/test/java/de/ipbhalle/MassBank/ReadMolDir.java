/**
 * created by Michael Gerlich, Jan 28, 2011 - 1:32:21 PM
 */ 

package de.ipbhalle.MassBank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.wrapper.Result;

/**
 * Class for parallel loading of molfiles from different directories to
 * create List of Result entries to create giant similarity Matrix.
 * 
 * @author mgerlich
 *
 */
public class ReadMolDir extends Thread {
	
	private String dir;
	private List<Result> results;
	private List<File> filtered;
	
	public ReadMolDir(String dir) {
		this.dir = dir;
		this.results = new ArrayList<Result>();
		setName(dir);
	}
	
	public boolean checkFile(File f) {
		boolean result = false;
		
		return result;
	}
	
	public void run() {
		System.out.println("Started run() for [" + dir + "]");
		String recDir = dir.replace("mol", "records");
		File recordDir = new File(recDir);
		this.filtered = new ArrayList<File>();
		
		boolean isKegg = false;
		if(dir.contains("kegg"))
			isKegg = true;
		
		File[] list = new File(dir).listFiles();
		for (File mol : list) {
			boolean check = false;
			
			if(!mol.getName().endsWith(".mol"))
				continue;
			
			String id = mol.getName().substring(0, mol.getName().indexOf("."));
			File record = new File(recordDir, id + ".txt");
			// StringBuffer for record check
			StringBuffer sb = new StringBuffer();
			if(record.exists()) {
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(record));
					String line = "";
					while((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					
					String content = sb.toString();
					if(content.contains("MS/MS"))
						check = true;
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// if current directory is KEGG directory
			if(!record.exists() && isKegg)
				check = true;
				
			if(check) {
				//IAtomContainer container = MassBankUtilities.getContainer(id, dir);
				//IAtomContainer container = MassBankUtilities.getContainerUnmodified(id, dir);
				MassBankUtilities mbu = new MassBankUtilities();
				IAtomContainer container = mbu.getContainerUnmodified(id, dir);
				if(container != null) {
					results.add(new Result("MassBank", id, id, 0, container));
					filtered.add(mol);
				}
			}
			
		}
		System.out.println("Finished run() for [" + dir + "] -> " + list.length + " entries");
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getDir() {
		return dir;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setFiltered(List<File> filtered) {
		this.filtered = filtered;
	}

	public List<File> getFiltered() {
		return filtered;
	}
}
