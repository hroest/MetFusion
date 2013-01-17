/**
 * created by Michael Gerlich, Aug 10, 2012 - 10:01:20 AM
 */ 

package de.ipbhalle.GridEngine.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import de.ipbhalle.io.FileNameFilterImpl;

public class GenerateArrayJob {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File spectraDir = new File("/home/mgerlich/Datasets/allSpectra/");
		//File spectraDir = new File("/home/mgerlich/Datasets/Eawag_IPB_TP_all/recdata/");
		//File spectraDir = new File("/home/mgerlich/Datasets/Uni_Jena/recdata/");
		//File spectraDir = new File("/home/mgerlich/Datasets/Pesticides_new/Pesticides_111028/recdata/");
		//File spectraDir = new File("/vol/data_extern/ryona@psc.riken.jp/queries20121003/lipid_MS2/120918_008P_pos_ammonium_adduct/");
		//File spectraDir = new File("/home/mgerlich/projects/jan_stanstrup/rungroup6/");
		//File spectraDir = new File("/vol/data_extern/ryona@psc.riken.jp/queries20121003/secmet_QTOF/root_n_secmet_QTOF/");
		//File spectraDir = new File("/home/mgerlich/projects/MTBLSFarag/profile/mf/");
		File[] files = spectraDir.listFiles(new FileNameFilterImpl("", "txt"));	// TODO txt oder mf entsprechend switch -mf oder -record!
		Arrays.sort(files);
		
		// the output directory of the grid engine runs
		String outputDir = "/home/mgerlich/projects/metfusion_benchmark/";
		//String outputDir = "/home/mgerlich/projects/metfusion_CE_spectra/";
		//String outputDir = "/home/mgerlich/projects/eval_metfusion_ECFP/exactMass_noFilter/";
		//String outputDir = "/home/mgerlich/projects/metfusion_pesticide_spectra/";
		//String outputDir = "/home/mgerlich/projects/eval_metfusion_uniqueFilter_14092012/";
		//String outputDir = "/home/mgerlich/projects/metfusion_tp/results_18-10-2012";
		//String outputDir = "/vol/data_extern/ryona@psc.riken.jp/queries20121003/results/120918_008P_pos_ammonium_adduct/";
		//String outputDir = "/home/mgerlich/projects/jan_stanstrup/results/rungroup6/";
		//String outputDir = "/vol/data_extern/ryona@psc.riken.jp/queries20121003/secmet_QTOF/root_n_secmet_QTOF/results_chebi/";
		//String outputDir = "/home/mgerlich/projects/metfusion_tp/results_dbchemspider_Massbank_wo_Eawag_but_wUFZ_formula/";
		//String outputDir = "/home/mgerlich/projects/metfusion_chebi/";
		//String outputDir = "/home/mgerlich/projects/metfusion_tp/results_dbchemspider_Massbank_w_Eawag/";
		//String outputDir = "/home/mgerlich/projects/farag_profile/results_03-01-2013/";
		
		// the directory in which the jar file is located
		String projectDir = "/home/mgerlich/projects/";

		// the directory where the shell scripts are stored
		//String workDir = "/home/mgerlich/projects/eval_metfusion_ECFP/";
		String workDir = projectDir;
		String prefix = "benchmark";
		File jobInfo = new File(workDir, prefix + "_sge_metfusion.sh");
		jobInfo.createNewFile();
		jobInfo.setExecutable(true);
		
		// the parameter string in addition to the filenames
		// TODO: adjust generateShellScript() accordingly for number of parameters to read
		//String params = outputDir + " " + "ECFP";
		String params = outputDir;
		File paramFile = new File(workDir, prefix + "_sge_metfusion.params");
		generateParametersFile(paramFile, files, params);
		
		/**
		 * TODO: change jar name
		 */
		File jarName = new File(projectDir, "metfusion_batch_latest.jar");
		//File jarName = new File(projectDir, "metfusion_batch_latest.jar");
		File scriptName = new File(workDir, prefix + "_sge_metfusion.q");
		generateShellScript(scriptName, paramFile.getAbsolutePath(), jarName.getAbsolutePath());
		
		generateQSUB(jobInfo, scriptName.getAbsolutePath(), "MSBI", 1, files.length);
	}

	public static void generateShellScript(File f, String paramFile, String jarName) throws IOException {
		FileWriter fw = new FileWriter(f);
		fw.write("#!/bin/bash\n\n");	// shell header
		
		// adjust according to number of parameters
		fw.write("awk \"NR==$SGE_TASK_ID\" " + paramFile + " | while read A B; do echo $A/$B\n");
		//fw.write("java -jar -Dproperty.file.path=/home/mgerlich/workspace_new/MetFusion/WebContent/WEB-INF/ " + jarName + " -mf $A -out $B -unique\n");
		
//		fw.write("java -jar -Dproperty.file.path=/home/mgerlich/workspace_new/MetFusion/WebContent/WEB-INF/ " + jarName + 
//				" -mf $A -out $B -unique -format SDF -db chebi\n");
		
		fw.write("java -jar -Dproperty.file.path=/home/mgerlich/workspace_new/MetFusion/WebContent/WEB-INF/ " + jarName + 
		" -record $A -out $B -unique -format SDF \n");
		
//		fw.write("java -jar -Xmx4000m -Dproperty.file.path=/home/mgerlich/workspace_new/MetFusion/WebContent/WEB-INF/ " + jarName 
//				+ " -record $A -out $B -unique -format SDF -server http://msbi.ipb-halle.de/MassBank/\n");
		
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
		
		//fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/ryona/ -o /home/mgerlich/SGE/output/ryona/ -q " + queue + " " + qFile + "\n");
		//fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/secmet/ -o /home/mgerlich/SGE/output/secmet/ -q " + queue + " " + qFile + "\n");
		//fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/lipid/ -o /home/mgerlich/SGE/output/lipid/ -q " + queue + " -pe orte2 6 " + qFile + "\n");
		
//		fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/farag/profile/ -o /home/mgerlich/SGE/output/farag/profile/ -q " + queue + 
//				" -pe orte2 6 -tc 20 " + qFile + "\n");
		
		fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/benchmark/ -o /home/mgerlich/SGE/output/benchmark/ -q " + queue + 
				" -pe orte2 6 -tc 20 " + qFile + "\n");
		
//		fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/tp/results_dbchemspider_Massbank_wo_Eawag_but_wUFZ_formula/" +
//				" -o /home/mgerlich/SGE/output/tp/results_dbchemspider_Massbank_wo_Eawag_but_wUFZ_formula/ -q " + queue + " -pe orte2 6 " + qFile + "\n");
		
//		fw.write("qsub -t " + start + "-" + end + " -e /home/mgerlich/SGE/error/tp/results_dbchemspider_Massbank_w_Eawag/" +
//				" -o /home/mgerlich/SGE/output/tp/results_dbchemspider_Massbank_w_Eawag/ -q " + queue + " -pe orte2 6 " + qFile + "\n");
		
		fw.flush();
		fw.close();
		
	}
}
