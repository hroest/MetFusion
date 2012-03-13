package de.ipbhalle.MetFusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.ipbhalle.metfusion.web.controller.MetFragBean;
import de.ipbhalle.enumerations.Dataset;
import de.ipbhalle.enumerations.DatasetUsage;
import de.ipbhalle.enumerations.Instruments;
import de.ipbhalle.metfusion.evaluation.QueryMassBank;

/**
 * The Class EvaluateDataset.
 * Runs an evaluation on an input dataset and creates:
 * <ul>
 *  <li> a <b>.log file</b> containing all unmodified outputs from each data source
 *  <li> a <b>.mat file</b> containing the Tanimoto matrix between all valid items in both 
 *  result sets, meaning they have a corresponding mol file and IAtomContainer
 *  <li> <b>integration_thresh and integration_weight files</b> giving 
 *  the ID, the original score, the result score, the position before and after evaluation for both
 *  threshold and weighted approach
 *  <li> <b>rank_thresh and rank_weight files</b> giving the ID and the corresponding Tanimoto cluster rank
 *  based on similar Tanimoto score (>= 0.95) for the structures
 *  <li> <b>_thresh.list_tied and _weight.list_tied files</b> containing tied ranks for both approaches 
 *  position before, the ID, original score, result score and position after as tied rank
 *  <li> <b>_threshScores.vec and _weightScores.vec</b> files containing candidate scores as vector
 *  for use in R
 * </ul>
 */
public class EvaluateDataset {

	private final static String massbankJP = "http://www.massbank.jp/";
	private static final int numParams = 6;
	
	/**
	 * The main method.
	 *
	 * @param args - dataset, with or without, instrument set, datadir, outputdir
	 */
	public static void main(String[] args) {
		if(args.length != numParams) {
			System.out.println("Missing parameters!\n");
			System.out.println("1. argument: dataset -> {Hill, Riken, both}");
			System.out.println("2. argument: use data -> {no, yes}");
			System.out.println("3. argument: instrument set -> {ALL, ESI, ESIMS2, EI, OTHER}");
			System.out.println("4. argument: /path/to/data_dir");
			System.out.println("5. argument: /path/to/output_dir");
			System.out.println("6. argument: /path/to/statistics_file");
			System.exit(-1);
		}
		/**
		 * store arguments
		 */
		String dataset = args[0];
		String instruments = args[1];
		String usage = args[2];
		String dataFile = args[3];
		String outputDir = args[4];
		String statFile = args[5];
		
		/**
		 * TODO: MassBank mit entsprechenden Parametern aufrufen
		 * je nach Datensatz modifikationen beachten!
		 * MetFrag mit MetFragBean aufrufen
		 */
		File statistics = new File(statFile);
		if(!statistics.exists()) {
			System.err.println("Statistics file does not exist! -> " + statistics.getAbsolutePath() + "\naborting...");
			System.exit(-1);
		}
		
		BufferedReader br;
		Map<String, String> statMap = new HashMap<String, String>();
		try {
			br = new BufferedReader(new FileReader(statistics));
			String line = "";
			line = br.readLine();	// skip header
			while((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				String key = "";
				if(args[0].equals(Dataset.HILL))		// modification for HILL data
					key = split[0].split("_10")[0];
				else if(args[0].equals(Dataset.RIKEN))	// modification for RIKEN data
					//key = split[0].substring(0, 8);
					key = split[0].substring(0, split[0].indexOf("."));
				else {
					System.out.println("Dataset -> " + args[0] + " modification for statistics file?");
					// TODO
					System.exit(-1);
				}
				key = key.replaceAll(" ", "_");
				key = key.replaceAll("-", "_");
				statMap.put(key, line);	// mergeName	CID		worstRank	mass	time
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error reading statistics file " + statistics.getAbsolutePath() + " -> File not found.");
			System.exit(-1);
		}
		catch (IOException e) {
			System.err.println("Error processing statistics file " + statistics.getAbsolutePath());
			System.exit(-1);
		}
		/**
		 * 
		 */ 
		
		MetFragBean mfb = new MetFragBean();
		QueryMassBank qm = new QueryMassBank(massbankJP, Instruments.valueOf(instruments), Dataset.valueOf(dataset), DatasetUsage.valueOf(usage));
	}
}
