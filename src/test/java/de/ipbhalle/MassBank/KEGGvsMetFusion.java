/**
 * created by Michael Gerlich, Jun 5, 2012 - 10:33:14 AM
 */ 

package de.ipbhalle.MassBank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;


/**
 * Testdatensatz mit 1099 Spektren nach Einträgen filtern, die KEGG ID besitzen und anhanbd derer
 * die Extrapolation der MetFusion Leistung auf KEGG gesamt testen.
 * 
 * Unter {@link /home/mgerlich/Datasets/allSpectra} findet sich der Testdatensatz mit 1099 Spektren,
 * von denen 381 KEGG ID's besitzen, davon 186 unique.
 * 
 * @author mgerlich
 *
 */
public class KEGGvsMetFusion {

	public static List<String> ids;
	public static Map<String, String> fileToID;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean createfiles = false;
		boolean evalute = true;
		boolean uniqueOnly = false;
		
		String spectraPath = "/home/mgerlich/Datasets/allSpectra/";
		String outDir = "/home/mgerlich/projects/KEGG_vs_Massbank/";
		String outDirunique = "/home/mgerlich/projects/KEGG_vs_Massbank/unique/";
		
		if (createfiles) {
			File[] list = pickKEGGSpectra(spectraPath, uniqueOnly);
			System.out.println("#files -> " + list.length);

			for (int i = 0; i < list.length; i++) {
				boolean success = writeShellScript(list[i], outDirunique);
				if (!success)
					System.err.println("Error writing shell script for [" + list[i].getName() + "]");
			}
		}
		
		if(evalute) {
			evaluateResults(spectraPath, uniqueOnly, outDir);
		}
	}

	public static boolean writeShellScript(File f, String outDir) {
		boolean success = false;
		
		String fileName = "KEGG_" + f.getName().substring(0, f.getName().lastIndexOf("."));
		String ending = ".sh";
		
		String output = "java -jar -Dproperty.file.path=/home/mgerlich/workspace_new/MetFusion/WebContent/WEB-INF/ "
			+ "/home/mgerlich/projects/metfusion_batch_testdata.jar -record " + f.getAbsolutePath() + " -out " + outDir;
		
		try {
			FileWriter fw = new FileWriter(new File(outDir, fileName + ending));
			fw.write(output);
			fw.close();
			success = true;
		} catch (IOException e) {
			return success;
		}
		return success;
	}
	
	public static File[] pickKEGGSpectra(String path, boolean uniqueOnly) {
		File dir = new File(path);
		KEGGFileFilter ff = new KEGGFileFilter(uniqueOnly);	// FileFilter implementation für Suche nach KEGG ID in der Datei
		File[] list = dir.listFiles(ff);
		
		ids = ff.getIds();				// Liste von KEGG ID's
		fileToID = ff.getIdMap();		// Map die für jeden Dateinamen die zugehörige KEGG ID speichert
		
		return list;
	}
	
	public static void evaluateResults(String spectraDir, boolean uniqueOnly, String outDir) {
		File specDir = new File(spectraDir);
		KEGGFileFilter ff = new KEGGFileFilter(uniqueOnly);	// FileFilter implementation für Suche nach KEGG ID in der Datei
		File[] list = specDir.listFiles(ff);
		Map<String, String> recordToID = ff.getIdMap();		// Map die zu jedem Record die KEGG ID speichert
		
		File dir = new File(outDir);
		KEGGFilenameFilter fnf = new KEGGFilenameFilter("resultsOrig");		// resultsNew,   resultsCluster
		File[] results = dir.listFiles(fnf);
		
		KEGGFilenameFilter fnf_new = new KEGGFilenameFilter("resultsNew");		// resultsNew,   resultsCluster
		File[] resultsNew = dir.listFiles(fnf_new);
		
		// sort arrays by name
//		Arrays.sort(list);
//		Arrays.sort(results);
//		Arrays.sort(resultsNew);
		
		if(results.length != resultsNew.length) {
			System.err.println("#resultsOrig = " + results.length + " != #resultsNew = " + resultsNew.length);
			System.err.println("Abbruch!");
			return;
		}
		
		MassBankUtilities mbu = new MassBankUtilities();
		
		for (int i = 0; i < results.length; i++) {
			File f = results[i];
			String record = f.getName().split("_")[0];
			String KEGGID = recordToID.get(record);
			String CID = "";
			System.out.println(record + " -> " + KEGGID);
			int rankOld = 0;
			int rankNew = 0;
			int delta = 0;
			
			// Suche nach korrektem Spektrum da nicht alle Spektren auch Ergebnisse liefern (Peaks nicht in MassBank gefunden)
			Map<String, String> links = new HashMap<String, String>();
			for (int j = 0; j < list.length; j++) {
				String spectrumName = list[j].getName();
				spectrumName = spectrumName.substring(0, spectrumName.lastIndexOf("."));
				//if(list[j].getName().startsWith(record)) {
				if(spectrumName.equals(record)) {
					links = mbu.retrieveLinks(list[j]);
					break;
				}
			}
			CID = links.get("PUBCHEM");
			if(CID == null || CID.isEmpty())
				CID = "";
			else CID = CID.substring(CID.indexOf(":") + 1);
			
			Set<String> keys = links.keySet();
//			for (String key : keys) {
//				System.out.println("key [" + key + "] -> " + links.get(key));
//			}
		}
	}
	
	/**
	 * PubChem CID in Ergebnisliste suchen
	 * 
	 * @param id
	 * @param f
	 * @return
	 */
	public static int getRankOfCorrect(String id, File f) {
		int rank = 0;
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while((line = br.readLine()) != null) {
				if(line.startsWith("##"))
					continue;
				
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rank;
	}
}
