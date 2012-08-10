/**
 * created by Michael Gerlich, Aug 10, 2012 - 10:01:20 AM
 */ 

package de.ipbhalle.GridEngine.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class GenerateArrayJob {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//File spectraDir = new File("/home/mgerlich/Datasets/allSpectra/");
		File spectraDir = new File("/home/mgerlich/Datasets/Uni_Jena/recdata/");
		File[] files = spectraDir.listFiles();
		Arrays.sort(files);
		
		String outputDir = "/home/mgerlich/projects/metfusion_CE_spectra/";

		String workDir = "/home/mgerlich/projects/";
		File jobInfo = new File(workDir, "CE_sge_metfusion.sh");
		jobInfo.setExecutable(true);
		
		File paramFile = new File(workDir, "CE_sge_metfusion.params");
		generateParametersFile(paramFile, files, outputDir);
		
		File jarName = new File(workDir, "metfusion_batch_latest.jar");
		File scriptName = new File(workDir, "CE_sge_metfusion.q");
		generateShellScript(scriptName, paramFile.getAbsolutePath(), jarName.getAbsolutePath());
		
		generateQSUB(jobInfo, scriptName.getAbsolutePath(), "MSBIoffice", 1, files.length);
	}

	public static void generateShellScript(File f, String paramFile, String jarName) throws IOException {
		FileWriter fw = new FileWriter(f);
		fw.write("#!/bin/bash\n\n");	// shell header
		
		// adjust according to number of parameters
		fw.write("awk \"NR==$SGE_TASK_ID\" " + paramFile + " | while read A B ; do echo $A/$B\n");
		fw.write("java -jar -Dproperty.file.path=/home/mgerlich/workspace_new/MetFusion/WebContent/WEB-INF/ " + jarName + " -record $A -out $B\n");
		//fw.write("EOF\n");
		fw.write("done");
		
		fw.flush();
		fw.close();
	}
	
	public static void generateParametersFile(File f, File[] files, String parameter) throws IOException {
		FileWriter fw = new FileWriter(f);
		//fw.write("#!/bin/bash\n\n");	// shell header
		
		for (int i = 0; i < files.length; i++) {
			fw.write(files[i].getAbsolutePath() + " " + parameter + "\n");
		}
		
		fw.flush();
		fw.close();
	}
	
	public static void generateQSUB(File f, String qFile, String queue, int start, int end) throws IOException {
		FileWriter fw = new FileWriter(f);
		fw.write("#!/bin/bash\n\n");	// shell header
		
		fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/ -o /home/mgerlich/SGE/output/ -q " + queue + " " + qFile + "\n");
		
		fw.flush();
		fw.close();
		
	}
}
