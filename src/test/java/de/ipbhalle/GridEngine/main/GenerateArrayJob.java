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
		File spectraDir = new File("/home/mgerlich/Datasets/allSpectra/");
		//File spectraDir = new File("/home/mgerlich/Datasets/Uni_Jena/recdata/");
		//File spectraDir = new File("/home/mgerlich/Datasets/Pesticides_new/Pesticides_111028/recdata/");
		File[] files = spectraDir.listFiles();
		Arrays.sort(files);
		
		// the output directory of the grid engine runs
		//String outputDir = "/home/mgerlich/projects/metfusion_CE_spectra/";
		//String outputDir = "/home/mgerlich/projects/eval_metfusion_ECFP/exactMass_noFilter/";
		//String outputDir = "/home/mgerlich/projects/metfusion_pesticide_spectra/";
		String outputDir = "/home/mgerlich/projects/eval_metfusion_uniqueFilter_14092012/";

		// the directory in which the jar file is located
		String projectDir = "/home/mgerlich/projects/";

		// the directory where the shell scripts are stored
		//String workDir = "/home/mgerlich/projects/eval_metfusion_ECFP/";
		String workDir = projectDir;
		String prefix = "uniqueFilter";
		File jobInfo = new File(workDir, prefix + "_sge_metfusion.sh");
		jobInfo.createNewFile();
		jobInfo.setExecutable(true);
		
		// the parameter string in addition to the filenames
		// TODO: adjust generateShellScript() accordingly for number of parameters to read
		//String params = outputDir + " " + "ECFP";
		String params = outputDir;
		File paramFile = new File(workDir, prefix + "_sge_metfusion.params");
		generateParametersFile(paramFile, files, params);
		
		File jarName = new File(projectDir, "metfusion_batch_latest.jar");
		File scriptName = new File(workDir, prefix + "_sge_metfusion.q");
		generateShellScript(scriptName, paramFile.getAbsolutePath(), jarName.getAbsolutePath());
		
		generateQSUB(jobInfo, scriptName.getAbsolutePath(), "MSBIoffice", 1, files.length);
	}

	public static void generateShellScript(File f, String paramFile, String jarName) throws IOException {
		FileWriter fw = new FileWriter(f);
		fw.write("#!/bin/bash\n\n");	// shell header
		
		// adjust according to number of parameters
		fw.write("awk \"NR==$SGE_TASK_ID\" " + paramFile + " | while read A B; do echo $A/$B\n");
		fw.write("java -jar -Dproperty.file.path=/home/mgerlich/workspace_new/MetFusion/WebContent/WEB-INF/ " + jarName + " -record $A -out $B -unique\n");
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
