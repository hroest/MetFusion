/**
 * created by Michael Gerlich on Jan 8, 2010 - 10:54:39 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.GridEngine.main;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.ipbhalle.enumerations.Dataset;
import de.ipbhalle.enumerations.DatasetUsage;
import de.ipbhalle.enumerations.Instruments;

/**
 * utility class for creating Grid Engine shell scripts for evaluation purposes
 * 
 * @author mgerlich
 *
 */
public class SGEscripts {

	/**
	 * enumeration dataset contains possible datasets for evaluation
	 */
	//private static enum dataset {HILL, RIKEN, BOTH};
	//private static  Dataset dataset;

	/**
	 * enumeration usage contains possible values to accept (yes) or reject (no)
	 * data from datasets in result list from database retrieval
	 */
	//private static enum usage {no, yes};
	
	/**
	 * enumeration instruments contains possible instrument selections for evaluation
	 * @author mgerlich
	 *
	 */
	//private static enum instruments {ALL, ESI, ESIMS2, EI, OTHER};
	
	/**
	 * the number of valid Hill data files - should be 102
	 */
	private static final int countHill = 102;
	
	/**
	 * the number of valid Riken data files - should be 240
	 */
	private static final int countRiken = 240;
	
	/**
	 * the number of valid data files in both datasets - should be 342
	 */
	private static final int countBoth = countHill + countRiken;
	
	/**
	 * the file separator as defined by the System property
	 */
	private static final String sep = System.getProperty("file.separator");
	
	private static final int numParams = 6;
	/**
	 * @param args - dataset, with or without, path to data, outputdir
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

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
		
		// add trailing separator for directory entry
		if(!args[3].endsWith(sep))
			args[3] = args[3] + sep;
		
		// check if data directory is directory as it should contain a certain number of data files
		File dir = new File(args[3]);
		if(!dir.isDirectory()) {
			System.err.println("[" + args[3] + "] is not a valid directory!");
			System.exit(-1);
		}
		
		// check if output directory is directory as it should be writable for script files	
		File output = new File(args[4]);
		if(!output.isDirectory() || !output.exists()) {
			System.err.println("[" + args[4] + "] is not a valid directory!");
			System.exit(-1);
		}
		output.mkdir();		// create output directory
		
		/**
		 * parameters triggering generation of scripts
		 */
		String usedDataset = "";
		String usedCase = "";
		String usedInst = "";
		/**
		 * 
		 */
		// get file listing from input directory with matching filenames ending on .txt
		File[] data = dir.listFiles(new MyFileFilter(".txt"));
		
		Dataset ds = null;
		try {
			ds = Dataset.valueOf(args[0].toUpperCase());
		} catch(IllegalArgumentException e) {
			System.err.println("Error: [" + args[0] + "] does not match predefined datasets!");
			System.out.println("Please choose one of the following:");
			Dataset[] values = Dataset.values();
			for (int i = 0; i < values.length; i++) {
				System.out.print("[" + values[i].toString() + "]  ");
			}
			System.out.println();
			System.exit(-1);
		}
		switch(ds) {
		case HILL:
			System.out.println("HILL dataset chosen -> expecting 102 compound files!");
			if(data.length != countHill) {
				System.err.println("Number of Hill files is " + data.length + ", should be " + countHill + " -> Aborting...");
				System.exit(-1);
			}
			else {
				usedDataset = Dataset.HILL.toString();
			}
			break;
			
		case RIKEN:
			System.out.println("RIKEN dataset chosen -> expecting 240 compound files!");
			if(data.length != countRiken) {
				System.err.println("Number of Riken files is " + data.length + ", should be " + countRiken + " -> Aborting...");
				System.exit(-1);
			}
			else {
				usedDataset = Dataset.RIKEN.toString();
			}
			break;
			
		case BOTH:
			System.out.println("BOTH datasets (Hill & Riken) were chosen -> expecting 342 compound files!");
			if(data.length != countBoth) {
				System.err.println("Number of both filesets is " + data.length + ", should be " + countBoth + " -> Aborting...");
				System.exit(-1);
			}
			else {
				usedDataset = Dataset.BOTH.toString();
			}
			break;
			
		default:			// no matching dataset found
			System.err.println("Error: " + ds.toString() + " does not match predefined datasets!");
			System.out.println("Please choose one of the following:");
			Dataset[] values = Dataset.values();
			for (int i = 0; i < values.length; i++) {
				System.out.print(values[i].toString() + "  ");
			}
			System.out.println();
			System.exit(-1);
			break;
		}
		
		DatasetUsage u = null;
		try {
			u = DatasetUsage.valueOf(args[1].toLowerCase());
		} catch(IllegalArgumentException e) {
			System.err.println("Error: [" + args[1] + "] does not match predefined usage modes!");
			System.out.println("Please choose one of the following:");
			DatasetUsage[] values = DatasetUsage.values();
			for (int i = 0; i < values.length; i++) {
				System.out.print("[" + values[i].toString() + "]  ");
			}
			System.out.println();
			System.exit(-1);
		}
		switch(u) {
		case yes:			// use dataset for database lookup and keep in results
			System.out.println("use dataset -> yes");
			//usedCase = DatasetUsage.yes.toString();
			usedCase = "w" + usedDataset;
			break;
		
		case no:			// skip dataset in results
			System.out.println("use dataset -> no");
			//usedCase = DatasetUsage.no.toString();
			usedCase = "wo" + usedDataset;
			break;
		
		default:			// default to skip dataset in results
			System.out.println("No matching usecase was found - skipping dataset entries for result set!");
			//usedCase = DatasetUsage.no.toString();
			usedCase = "wo" + usedDataset;
			break;
		}
		
		Instruments inst = null;
		try {
			inst = Instruments.valueOf(args[2].toUpperCase());
		} catch(IllegalArgumentException e) {
			System.err.println("Error: [" + args[1] + "] does not match predefined usage modes!");
			System.out.println("Please choose one of the following:");
			Instruments[] values = Instruments.values();
			for (int i = 0; i < values.length; i++) {
				System.out.print("[" + values[i].toString() + "]  ");
			}
			System.out.println();
			System.exit(-1);
		}
		switch(inst) {
		case ALL:			// use all instruments for database lookup
			System.out.println("using all instruments");
			usedInst = Instruments.ALL.toString();
			break;
		
		case ESI:			// only use ESI instruments for database lookup
			System.out.println("using ESI instruments only");
			usedInst = Instruments.ESI.toString();
			break;
		
		case ESIMS2:			// only use ESIMS instruments for database lookup
			System.out.println("using ESIMS2 instruments only");
			usedInst = Instruments.ESIMS2.toString();
			break;
			
		case OTHER:			// only use Other instruments for database lookup
			System.out.println("using OTHER instruments only");
			usedInst = Instruments.OTHER.toString();
			break;
			
		case EI:			// only use EI instruments for database lookup
			System.out.println("using EI instruments only");
			usedInst = Instruments.EI.toString();
			break;
			
		default:			// default to ESIMS2 instruments for database lookup
			System.out.println("No matching instrument set was found - defaulting to ESIMS2 instruments!");
			usedInst = Instruments.ESIMS2.toString();
			break;
		}
		
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String date = sdf.format(now);
		System.out.println("output dir -> " + output.getAbsolutePath());
		
		for (File file : data) {
			String s = output.getAbsolutePath() + sep + date + "_" + usedDataset + "_" + usedInst + "_" + usedCase + sep; //"_HILL_ESIMS2_woHill/";
			s = s.replace("scripts", "results");
			File fs = new File(s);
			fs.mkdirs();
			String name = file.getName().substring(0, file.getName().indexOf("."));
			/**
			 * dataset specific modifications
			 */
			switch(ds) {
				case HILL:
					name = name.substring(0, name.indexOf("_10"));
					break;
					
				case RIKEN:
					break;
					
				case BOTH:
					break;
			}
			
			File outFile = new File(output, "SGE_" + name + ".sh");
			FileWriter fw = new FileWriter(outFile);
			
			fw.write("#!/bin/bash\n");
			fw.write("\n");			//-Dproperty.file.path=/home/mgerlich/MassBankData/MetFragSunGrid/
			fw.write("java -Dproperty.file.path=/home/swolf/src/MetFragNew/ -Xms1000m -Xmx4000m -jar /home/mgerlich/evaluation/MetFusion/evaluation.jar ");
			fw.write(usedDataset + " " + usedInst + " " + usedCase + " " + args[3] + file.getName() + " " + s + " " + args[numParams-1] + "\n");
			
			fw.flush();
			fw.close();
		}
		
		System.out.println("Generating SGE scripts finished!");
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
		
//		public MyFileFilter() {
//			suffix = ".txt";
//		}
		
		public MyFileFilter(String suffix) {
			this.suffix = (suffix.isEmpty() ? ".txt" : suffix);
		}

//		public MyFileFilter(String prefix, String suffix) {
//			this(suffix);
//			this.prefix = prefix;
//		}
		
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
