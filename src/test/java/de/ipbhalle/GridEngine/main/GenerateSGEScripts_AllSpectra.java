/**
 * created by Michael Gerlich, Nov 25, 2010 - 9:39:32 AM
 */ 

package de.ipbhalle.GridEngine.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenerateSGEScripts_AllSpectra {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String pathHillSingle = "/home/mgerlich/Datasets/Hill/Single/recdata/";
		String pathHillMerged = "/home/mgerlich/Datasets/Hill/Merged/recdata/";
		String pathQstarSingle = "/home/mgerlich/Datasets/IPB_QSTAR/Single/";
		String pathQstarMerged = "/home/mgerlich/Datasets/IPB_QSTAR/Merged/";
		String pathRiken = "/home/mgerlich/Datasets/Riken/Merged/";
		
		String outPath = "/home/mgerlich/evaluation/MetFusion/allSpectra/scripts/";
		//String statisticsFile = "/home/mgerlich/evaluation/statistics/eval_allSpectra.txt";
		
		List<String> paths = new ArrayList<String>();
		paths.add("/home/mgerlich/Datasets/allSpectra");
//		paths.add(pathHillSingle);
//		paths.add(pathHillMerged);
//		paths.add(pathQstarSingle);
//		paths.add(pathQstarMerged);
//		paths.add(pathRiken);
		
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String date = sdf.format(now);
		File output = new File(outPath);
		output.mkdir();
		
		for (String path : paths) {
			File dir = new File(path);
			File[] files = dir.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".txt")) {
					String s = outPath + date + "_ESIMS2_withCorrect_MSBI_MassBank/";
					
					//String s = args[1] + date + "_RIKEN_ESIMS2_woRiken/";
					s = s.replace("scripts", "results");
					File fs = new File(s);
					fs.mkdirs();
					String name = file.getName().substring(0, file.getName().indexOf("."));
				
					File out = new File(output, "SGE_" + name + ".sh");
					FileWriter fw = new FileWriter(out);
					
					fw.write("#!/bin/bash\n");
					fw.write("\n");			//-Dproperty.file.path=/home/mgerlich/MassBankData/MetFragSunGrid/
					fw.write("java -Dproperty.file.path=/home/swolf/src/MetFragNew/ -Xms1000m -Xmx4000m -jar " +
							"/home/mgerlich/evaluation/MetFusion/SGEScript_allSpectra.jar ");
					fw.write(file.getAbsolutePath() + " " + s);
					
					fw.flush();
					fw.close();
				}
			}
		}
		
//		cd /home/mgerlich/evaluation/MetFusion/allSpectra/scripts/
//		chmod +x *.sh
//
//		for F in *.sh ; do qsub -e /home/mgerlich/evaluation/MetFusion/allSpectra/error/ -o /home/mgerlich/evaluation/MetFusion/allSpectra/output/ -l qname=MSBI $F ; done

	}

}
