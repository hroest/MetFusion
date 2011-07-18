/**
 * created by Michael Gerlich, Jan 27, 2011 - 6:08:10 PM
 */ 

package de.ipbhalle.MassBank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;
import de.ipbhalle.metfusion.wrapper.Result;


public class MassBankVSMassBank {
	
	private static final int TIMEOUT_SEC = 60;
	static public List<Result> entries;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File cache = new File("/vol/massbank/Cache/");
		File[] dirList = cache.listFiles();
		List<ReadMolDir> results = new ArrayList<ReadMolDir>();
		
		for (File file : dirList) {
			if(file.isDirectory()) {
				File molDir = new File(file, "mol");
				
				results.add(new ReadMolDir(molDir.getAbsolutePath()));
				results.get(results.size() - 1).start();
			}
		}
		
		// add KEGG mol
		results.add(new ReadMolDir("/vol/mirrors/kegg/mol"));
		results.get(results.size() - 1).start();
		//
		
		int num = results.size();
		long until = System.currentTimeMillis() + TIMEOUT_SEC * 1000;
		boolean isRunning = true;
		while ( isRunning && System.currentTimeMillis() < until ) {
			isRunning = false;
			for ( int i = 0; i < num; i++ ) {
				try {
					if ( results.get(i).isAlive() ) {
						results.get(i).join();
						isRunning = true;
					}
				}
				catch ( Exception e ) {
					System.err.println("Error joining thread for [" + results.get(i).getName() + "]");
				}
			}
		}
		
		// create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = new ArrayList<Result>();

		// kegg entries
		List<Result> kegg = new ArrayList<Result>();
		//
		
		for (ReadMolDir r : results) {
			if(r.getResults().size() == 0)
				continue;
			
			// add kegg entries only to kegg list!
			if(r.getDir().contains("kegg")) {
				kegg.addAll(r.getResults());
				continue;
			}
			
			listMassBank.addAll(r.getResults());
		}
		
		System.out.println("MassBank entries: " + listMassBank.size());
		System.out.println("KEGG entries: " + kegg.size());
		if(kegg.size() == 0 || listMassBank.size() == 0)
			System.exit(-1);
		
		try {
			FileWriter fw = new FileWriter(new File("massbank.files"));
			
			for (int i = 0; i < results.size() - 1; i++) {	// fetch filtered entries for all MassBank folder
				List<File> files = results.get(i).getFiltered();
				for (File file : files) {
					fw.write(file.getAbsolutePath());
					fw.write("\n");
				}
			}
			
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileWriter fw = new FileWriter(new File("kegg.files"));
			
			// fetch filtered entries for KEGG folder
			List<File> files = results.get(results.size() - 1).getFiltered();
			for (File file : files) {
				fw.write(file.getAbsolutePath());
				fw.write("\n");
			}
			
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMassBank, 3, 0.5f);
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, kegg, 3, 0.5f);
		sim.writeMatrix(sim.getMatrix(), new File("MB_vs_KEGG_MS2.mat"));
		System.out.println("DONE!");
	}

}
