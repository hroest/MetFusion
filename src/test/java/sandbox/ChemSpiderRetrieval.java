/**
 * created by Michael Gerlich, Oct 22, 2012 - 4:58:26 PM
 */ 

package sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;

import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfrag.chemspiderClient.ChemSpider;
import de.ipbhalle.metfrag.tools.PPMTool;
import de.ipbhalle.metfusion.main.MetFusionBatchFileHandler;

public class ChemSpiderRetrieval implements Runnable {

	double exactmass = 0.0d;
	double searchPPM = 30.0d;
	String infile = "";
	String outfile = "";
	String errfile = "";
	
	public ChemSpiderRetrieval(String infile, String outfile, String errfile, double exactmass, double searchPPM) {
		this.infile = infile;
		this.outfile = outfile;
		this.errfile = errfile;
		this.exactmass = exactmass;
		this.searchPPM = searchPPM;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/mgerlich/projects/jan_stanstrup/");
		File[] list = dir.listFiles();
		String outputdir = "/home/mgerlich/projects/jan_stanstrup/results/";
		
		ExecutorService threadExecutor = Executors.newFixedThreadPool(4);
		
		for (int i = 0; i < list.length; i++) {		// for each directory
			if(list[i].isDirectory() & list[i].getName().startsWith("rungroup")) {
				File[] mffiles = list[i].listFiles(new FileNameFilterImpl("", "mf"));
				for (int j = 0; j < mffiles.length; j++) {		// for each mf file
					MetFusionBatchFileHandler mbfh = new MetFusionBatchFileHandler(mffiles[j]);
					mbfh.readFile();
					String outfile = mffiles[j].getName();
					outfile = outfile.replace(".mf", ".sdf");
					File out = new File(outputdir, outfile);
					String errfile = outfile.replace(".sdf", ".err");
					File err = new File(outputdir, errfile);
					Thread t = new Thread(new ChemSpiderRetrieval(mbfh.getBatchFile().getAbsolutePath(), out.getAbsolutePath(), err.getAbsolutePath(),
							mbfh.getBatchSettings().getMfExactMass(), mbfh.getBatchSettings().getMfSearchPPM()));
					
					// execute the threads in parallel
					threadExecutor.execute(t);
					
					System.out.println(list[i].getName() + " -> " + mffiles[j].getName() + " is running");
				}
			}
		}
		threadExecutor.shutdown();
		
		
//		Vector<String> candidates = new Vector<String>();
//		double exactMass = 368.1468d;
//		double searchPPM = 30.0d;
//
//		candidates = ChemSpider.getChemspiderByMass(exactMass, (PPMTool.getPPMDeviation(exactMass, searchPPM)));
//
//		int counter = 0;
//		IAtomContainer molecule = null;
//		SDFWriter sdfw = new SDFWriter(new FileOutputStream(new File("/home/mgerlich/2_84.sdf"), true));
//		FileWriter errlog = new FileWriter(new File("/home/mgerlich/2_84.err"));
//		
//		for (int i = 0; i < candidates.size(); i++) {
//			if(i % 100 == 0)
//				System.out.println(i + "/" + candidates.size());
//			
//			try {
//				molecule = ChemSpider.getMol(candidates.get(i), true);
//				sdfw.write(molecule);
//			} catch (CDKException e) {
//				System.err.println("Error retrieving molecule [" + i + "] -> " + candidates.get(i));
//				errlog.write(candidates.get(i));
//			}
//			counter++;
//		}
//		
//		try {
//			sdfw.close();
//			
//			errlog.flush();
//			errlog.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	public void run() {
		Vector<String> candidates = new Vector<String>();
		double exactMass = this.exactmass;
		double searchPPM = this.searchPPM;

		FileWriter errlog = null;
		try {
			errlog = new FileWriter(new File(this.errfile));
		} catch (IOException e2) {
			System.err.println("Error creating error log file!");
		}
		
		try {
			candidates = ChemSpider.getChemspiderByMass(exactMass, (PPMTool.getPPMDeviation(exactMass, searchPPM)));
		} catch (RemoteException e1) {
			try {
				errlog.write("Error retrieving candidates for [" + this.infile + "]. Aborting.");
				System.exit(-1);
			} catch (IOException e) {
				System.err.println("Error writing to error log.");
			}
		}

		int counter = 0;
		IAtomContainer molecule = null;
		SDFWriter sdfw = null;
		try {
			sdfw = new SDFWriter(new FileOutputStream(new File(this.outfile), true));
		} catch (FileNotFoundException e1) {
			System.err.println("Error creating output file [" + this.outfile + "].");
		}
		
		for (int i = 0; i < candidates.size(); i++) {
			if(i % 100 == 0)
				System.out.println(i + "/" + candidates.size());
			
			try {
				molecule = ChemSpider.getMol(candidates.get(i), true);
				sdfw.write(molecule);
			} catch (CDKException e) {
				System.err.println("Error retrieving molecule [" + i + "] -> " + candidates.get(i));
				try {
					errlog.write(candidates.get(i));
				} catch (IOException e1) {
					System.err.println("Error writing to error log!");
				}
			} catch (RemoteException e) {
				System.err.println("Error retrieving molecule [" + i + "] -> " + candidates.get(i));
				try {
					errlog.write("RemoteException occured\n");
					errlog.write(e.getMessage());
				} catch (IOException e1) {
					System.err.println("Error writing to error log!");
				}
				
			}
			counter++;
		}
		
		try {
			sdfw.close();
			
			errlog.flush();
			errlog.close();
		} catch (IOException e) {
			System.err.println("Error finalising output file [" + this.outfile + "] and logger file [" + this.errfile + "]");
		}
		
	}

	
}
