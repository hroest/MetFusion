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

/**
 * utility class for creating Grid Engine shell scripts for evaluation purposes
 * 
 * @author mgerlich
 *
 */
public class GenerateSGEScripts {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		if(args.length != 2) {
			System.out.println("Missing parameters!\nUsage: /path/to/data /output/path");
			System.exit(-1);
		}
		
		File dir = new File(args[0]);
		if(!dir.isDirectory()) {
			System.out.println("/path/to/data is no valid directory.");
			System.exit(-1);
		}
			
		File[] data = dir.listFiles(new MyFileFilter(".txt"));
		/**
		 * modification for HILL data
		 */
//		if(data.length != 102) {
//			System.err.println("Number of Hill files not equal to 102! - Aborting...");
//			System.exit(-1);
//		}
			
		//String sep = System.getProperty("file.separator");
		
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String date = sdf.format(now);
		File output = new File(args[1]);
		output.mkdir();
		
		for (File file : data) {
			/**
			 * HILL - single or merged Spectra, with/without Hill?
			 */
			//String s = args[1] + date + "_HILL_ESIMS2_wHill_MF+MBscore_mergedSpectra_MSBI_MassBank/";
			
			/**
			 * RIKEN - single or merged Spectra, with/without Riken?
			 */
			//String s = args[1] + date + "_RIKEN_ESIMS2_wRiken_MF+MBscore_mergedSpectra_MSBI_MassBank/";
			
			/**
			 * QSTAR - single or merged Spectra, with/without Qstar?
			 */
			String s = args[1] + date + "_QSTAR_ESIMS2_wQstar_MF+MBscore_singleSpectra_MSBI_MassBank/";
			
			//String s = args[1] + date + "_RIKEN_ESIMS2_woRiken/";
			s = s.replace("scripts", "results");
			File fs = new File(s);
			fs.mkdirs();
			String name = file.getName().substring(0, file.getName().indexOf("."));
			/**
			 * modification for HILL data with 102 merged spectra
			 */
			//name = name.substring(0, name.indexOf("_10"));
			
			/**
			 * modification for HILL data with 510 single spectra
			 */
			//name = name.substring(0, name.indexOf("."));
			
			File out = new File(output, "SGE_" + name + ".sh");
			FileWriter fw = new FileWriter(out);
			
			fw.write("#!/bin/bash\n");
			fw.write("\n");			//-Dproperty.file.path=/home/mgerlich/MassBankData/MetFragSunGrid/
			//fw.write("java -Dproperty.file.path=/home/swolf/src/MetFragNew/ -Xms1000m -Xmx4000m -jar /home/mgerlich/workspace-3.5/MetFusion2/testdata/SGEScript.jar ");
			
			/**
			 * HILL
			 */
			//fw.write("java -Dproperty.file.path=/home/swolf/src/MetFragNew/ -Xms1000m -Xmx4000m -jar /home/mgerlich/evaluation/MetFusion/SGEScript_HILL.jar ");
			
			/**
			 * RIKEN
			 */
			//fw.write("java -Dproperty.file.path=/home/swolf/src/MetFragNew/ -Xms1000m -Xmx4000m -jar /home/mgerlich/evaluation/MetFusion/SGEScript_RIKEN.jar ");
			
			/**
			 * QSTAR
			 */
			fw.write("java -Dproperty.file.path=/home/swolf/src/MetFragNew/ -Xms1000m -Xmx4000m -jar /home/mgerlich/evaluation/MetFusion/SGEScript_QSTAR.jar ");
			fw.write(args[0] + file.getName() + " " + s);
			
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
