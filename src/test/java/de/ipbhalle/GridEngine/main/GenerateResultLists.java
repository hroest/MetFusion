/**
 * created by Michael Gerlich on Jan 8, 2010 - 11:10:47 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.GridEngine.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;


public class GenerateResultLists {

	public final static String MassBankRecords = "MBCache";
	public final static String MassBankResults = "MassBankQueries/2010-02-12_09-35-51";	//"MassBankQueries";
	public final static String MetFragResults  = "MetFragResults";
	
	/** The Constant CHEMSPIDER. */
	public static final String CHEMSPIDER = "CHEMSPIDER";
	
	/** The Constant KEGG. */
	public static final String KEGG = "KEGG compound";
	
	/** The Constant PubChemC. */
	public static final String PubChemC = "pccompound";
	
	public static final int resultLimit = 100;
	
	/**
	 * @param args
	 * @throws CDKException 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException, CDKException {

		// standard directory for comparison
		String dir = "/home/mgerlich/workspace-3.5/MassBankComparison";
		// file handler for MassBank records dir
		File MBRecordDir = new File(dir, MassBankResults);
		// file handler for MetFrag results dir
		File MFResultsDir = new File(dir, MetFragResults);
		
		File[] list = MFResultsDir.listFiles(new MyFileFilter(".log"));
		System.out.println("MFResultsDir #log -> " + list.length);
		Arrays.sort(list);
//		for (File f : list) {
//			System.out.println(f.getName());
//		}
		
		File[] list2 = MBRecordDir.listFiles(new MyFileFilter(".log"));
		System.out.println("MBResultsDir #log -> " + list2.length);
		Arrays.sort(list2);
		
		// file handler for MassBank results dir
		File MBResultsDir = new File(dir, MassBankResults);
		
		File scoring = new File(MFResultsDir, "2009-08-04_15-43-23_histRealScoring_BioCpd_NoCH_PAPER_TD2_86AVG.csv");
		//File result = new File("./MetFragResults/results.csv");			
		List<Result> mfResults = new ArrayList<Result>();
		
		BufferedReader br = new BufferedReader(new FileReader(scoring));
		
		String metfragDB = "pubchem";
		String line = "";
		String header = "";
		List<String> compName = new ArrayList<String>(); // stores the compound names from MassBank records
		Map<String, String> dbs = new HashMap<String, String>(); // stores all DB entries from MassBank records
		String prevComp = ""; // previous compound
		Double score = 0.0;
		String prevRecord = "";	// previous record ID
		String port = "";
		String compoundName = "";
		String db = "";
		String id = "";
		String record = "";
		int counter = 0;
		int counterMB = 0; // only increased for MassBank records
		int worstRank = 0;
		
		
		/**
		 * MetFrag results
		 */
		boolean isHeader = false;
		// worst rank der hill compounds finden + jeweilige pubchem cid
		while(((line = br.readLine()) != null)) {
			if(!isHeader) {
				isHeader = true;
				header = line;
			}
			else {
				String[] split = line.split("\t");
				compoundName = split[0];
				id = split[1];
				worstRank = Integer.parseInt(split[2]);
				
				//String temp = split[2].substring(split[2].indexOf("=") + 1);
				//score = Double.valueOf(temp.trim());
				
				IAtomContainer ac = null;
				
				if(metfragDB.equals("kegg") || split[1].matches("C[0-9]{5}")) {
					db = KEGG;
					//ac = MetFlowUtilities.KEGGGetMolFromID(id);
				}
				else if(metfragDB.equals("pubchem")) {
					db = PubChemC;
					//ac = MetFlowUtilities.PubChemGetMolFromID(id);
				}
				else if(metfragDB.equals("chemspider")) {
					db = CHEMSPIDER;
					//ac =  MetFlowUtilities.ChemSpiderGetMolFromID(id);
				}
				record = id;
				dbs.put(db, id);
				
//				MDLV2000Reader reader;
//	    		List<IAtomContainer> containersList;
//	            reader = new MDLV2000Reader(new FileReader(new File("./testdata/" + record + ".mol")));
//	            ChemFile chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
//	            containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
//	            ac = containersList.get(0);

				//File out = new File("./testdata/" + record + ".mol");
				//FileWriter fw = new FileWriter(out);
				//MDLWriter writer = new MDLWriter(fw);
				//writer.writeMolecule(ac);
				//writer.close();
				
				compoundName = compoundName.replaceAll(" ", "_");
				compoundName = compoundName.replaceAll("-", "_");
				System.out.println("record -> " + compoundName);
				
				File log = new File(MFResultsDir, compoundName + ".log");
				if(!log.exists()) {
					System.out.println(log.getName() + " does not exist!");
					counter++;
					continue;
				}
				
				if(worstRank < 100)
					worstRank = 100;
				
				writeResults(compoundName, list[counter], list2[counter], worstRank);
				//writeMassBankResults(compoundName, list2[counter], worstRank);
				
				counter++;
	            //new WorkflowOutputAlignment(port, KEGG, score, db, id, compoundName, dbs, true, record, ac)
				mfResults.add(new Result("MetFrag", id, compoundName, score, ac));
			}
		}
		br.close();
		
		System.out.println("mfResults size -> " + mfResults.size());
		
		/**
		 * MassBank result list
		 */
	}
	
	public static void writeResults(String name, File f, File f2, int limit) throws NumberFormatException, IOException {
		String logSplit = "============================================================================";
		String logScoring = "*****************Scoring*****************************";
		String logScoringR = "*****************Scoring(Real)*****************************";
		String endScoring = "*****************************************************";
		String startResults = "RESULTS:";
		String next = "###";
		
		File log = new File("./testdata/Hill", name + ".txt");
		FileWriter fw = new FileWriter(log);
		fw.write("## MetFrag\n");
		
		// lokale PubChem suche
		PubChemLocal pcl = new PubChemLocal("jdbc:mysql://rdbms/MetFrag", "swolf", "populusromanus");
		
		/**
		 * MetFrag
		 */
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = "";
		while((line = br.readLine()) != null) {
			if(line.startsWith(logScoringR)) {
				line = br.readLine();			// time		
				for (int j = 0; j < limit; j++) {		// alle eintraege bis zum worst rank anschauen
					line = br.readLine();
					if(!line.contains("-") || line.equals(endScoring))
						break;
					String[] split = line.split("-");
					double score = Double.parseDouble(split[0].trim());
					if(score > 1.0d)
						score = 1.0d;
					String id = split[1].substring(0, split[1].indexOf("[")).trim();
					
					List<String> names = null;
					try {
						names = pcl.getNames(id);
					} catch (InvalidSmilesException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(names == null || names.size() == 0)
						fw.write(id + "\t" + id + "\t" + "score = " + score + "\n");
					else if(names.size() == 1) {
						fw.write(names.get(names.size() - 1) + "\t" + id + "\t" + "score = " + score + "\n");
					}
					else {
						for (int i = 0; i < names.size() - 1; i++) {
							fw.write(names.get(i) + "#");
						}
						fw.write(names.get(names.size() - 1) + "\t" + id + "\t" + "score = " + score + "\n");
					}
				}
				fw.flush();
				//fw.close();
				break;
			}					
		}
		
		/**
		 * MassBank
		 */
		fw.write("\n");
		fw.write("## MassBank\n");
		BufferedReader br2 = new BufferedReader(new FileReader(f2));
		line = "";
		List<String> duplicates = new ArrayList<String>();
		boolean fetchmol = false;
		MassBankUtilities mbu = new MassBankUtilities();
		
		while((line = br2.readLine()) != null) {
			if(line.startsWith(startResults)) {
				line = br2.readLine();			// time		
				for (int j = 0; j < limit; j++) {		// alle eintraege bis zum worst rank anschauen
					line = br2.readLine();
					if(line.contains(next))
						line = br2.readLine();
					
					String[] split = line.split("\t");
					String compoundName = split[7].substring(split[7].indexOf("=") + 1).trim();
					String record = split[2].substring(split[2].indexOf("=") + 1).trim();
					String temp = split[5].substring(split[5].indexOf("=") + 1).trim();
					double score = Double.parseDouble(temp);
					if(score > 1.0d)
						score = 1.0d;
					String site = split[8].substring(split[8].indexOf("=") + 1).trim();
					
					// only use unique compounds
					if(!duplicates.contains(compoundName))
						duplicates.add(compoundName);
					else {	// reduce counter by 2 to compensate duplicate entry
						j--;
						continue;
					}
					
					//String MBrecord = MassBankUtilities.retrieveRecord(record, site);
					Map<String, String> dbs = mbu.retrieveLinks(record, site);
					Set<String> keys = dbs.keySet();
					for (Iterator<String> it = keys.iterator(); it.hasNext();) {
						String db = (String) it.next();
						String val = dbs.get(db);
						
						fw.write(compoundName + "\t" + db + "\t" + val + "\tscore=" + score + "\t" + record + "\n");
					}
					
					if(!record.startsWith("CO") && fetchmol) {
						site = mbu.retrieveSite(record);
					}
					//site = MassBankUtilities.retrieveSite(record);
					File fmol = new File("./testdata/Hill/mol/" + record + ".mol");
					if(!fmol.exists() && fetchmol) {
						String mol = mbu.retrieveMol(compoundName, site, record);
						IAtomContainer ac = mbu.getContainer(mol);
						boolean write = mbu.writeMolFile(record, mol, "./testdata/Hill/mol/");
						if(write)
							System.out.println("molfile written -> " + record);
						else System.out.println("molfile not written -> " + record);
					}
					
				}
				fw.flush();
				break;
			}					
		}
		
		fw.close();
		br.close();
	}
	
	public static void writeMassBankResults(String name, File f, int limit) throws IOException {
		String startResults = "RESULTS:";
		String next = "###";
		
		File log = new File("./testdata/Hill", name + ".txt");
		if(!log.exists()) {
			System.out.println(log.getName() + " does not exist!");
		}
		
		FileWriter fw = new FileWriter(log);
		fw.write("\n");
		fw.write("## MassBank\n");
		
		MassBankUtilities mbu = new MassBankUtilities();
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = "";
		while((line = br.readLine()) != null) {
			if(line.startsWith(startResults)) {
				line = br.readLine();			// time		
				for (int j = 0; j < limit; j++) {		// alle eintraege bis zum worst rank anschauen
					line = br.readLine();
					if(line.contains(next))
						line = br.readLine();
					
					String[] split = line.split("\t");
					String compoundName = split[7].substring(split[7].indexOf("=") + 1).trim();
					String record = split[2].substring(split[2].indexOf("=") + 1).trim();
					String temp = split[5].substring(split[5].indexOf("=") + 1).trim();
					double score = Double.parseDouble(temp);
					if(score > 1.0d)
						score = 1.0d;
					String site = split[8].substring(split[8].indexOf("=") + 1).trim();
					
					//String MBrecord = MassBankUtilities.retrieveRecord(record, site);
					Map<String, String> dbs = mbu.retrieveLinks(record, site);
					Set<String> keys = dbs.keySet();
					for (Iterator<String> it = keys.iterator(); it.hasNext();) {
						String db = (String) it.next();
						String val = dbs.get(db);
						
						fw.write(compoundName + "\t" + db + "\t" + val + "\tscore=" + score + "\t" + record + "\n");
					}
				}
				fw.flush();
				fw.close();
				break;
			}					
		}
		br.close();
	}
	
	/**
	 * inner class which provides implementation of FilenameFilter interface
	 * 
	 * @author mgerlich
	 *
	 */
	private static class MyFileFilter implements FilenameFilter {
		
		private String suffix = "";
		private String prefix = "";
		
		public MyFileFilter() {
			suffix = ".txt";
		}
		
		public MyFileFilter(String suffix) {
			this.suffix = (suffix.isEmpty() ? ".txt" : suffix);
		}

		public MyFileFilter(String prefix, String suffix) {
			this(suffix);
			this.prefix = prefix;
		}
		
		@Override
		public boolean accept(File dir, String name) {
			if(suffix.isEmpty() && prefix.isEmpty())
				return false;
			else if(!suffix.isEmpty() && name.endsWith(suffix))
				return true;
			else if(!suffix.isEmpty() && !prefix.isEmpty() && 
					name.startsWith(prefix) && name.endsWith(suffix))
				return true;
			else return false;
		}
	}
}
