/**
 * created by Michael Gerlich on Jun 14, 2010
 * last modified Jun 14, 2010 - 10:51:45 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.MetFlow.utilities.MetFrag.ESearchDownload;
import de.ipbhalle.metfusion.wrapper.Result;

public class EvalLog implements Runnable{

	private File f;
	private List<Result> primaries;
	private List<Result> candidates;
	private List<List<Result>> log;

	private static final String cacheMassBank = "/vol/massbank/Cache/";
	//private static final String molPath = "/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/mol/";
	//private static final String molPath = "/vol/mol/";
	
	public EvalLog(File f) {
		this.f = f;
		
		try {
			this.log = readLog();
			this.primaries = log.get(0);
			this.candidates = log.get(1);
			
			System.out.println("#primaries -> " + primaries.size());
			System.out.println("#candidates -> " + candidates.size());
		} catch (FileNotFoundException e) {
			this.log = new ArrayList<List<Result>>();
			System.err.println("Error generating Lists for " + f + "\t-> FileNotFoundException");
		} catch (IOException e) {
			this.log = new ArrayList<List<Result>>();
			System.err.println("Error generating Lists for " + f + "\t-> IOException");
		}
	}

	public EvalLog(File f, boolean runnable) {
		this.f = f;
		if(!runnable) {	// wait for run() call
			try {
				this.log = readLog();
				this.primaries = log.get(0);
				this.candidates = log.get(1);
				
				System.out.println("#primaries -> " + primaries.size());
				System.out.println("#candidates -> " + candidates.size());
			} catch (FileNotFoundException e) {
				this.log = new ArrayList<List<Result>>();
				System.err.println("Error generating Lists for " + f + "\t-> FileNotFoundException");
			} catch (IOException e) {
				this.log = new ArrayList<List<Result>>();
				System.err.println("Error generating Lists for " + f + "\t-> IOException");
			}
		}
	}
	
	private List<List<Result>> readLog() throws IOException {
		List<List<Result>> result = new ArrayList<List<Result>>();
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = "";
		String port = "";
		
		int rank = 1;
		List<Result> candidates = new ArrayList<Result>();
		List<Result> primaries = new ArrayList<Result>();
		List<String> idList = new ArrayList<String>();	// list for storing PubChem ID's for moldata fetching
		while((line = br.readLine()) != null) {
			if(line.isEmpty() || line.equals("\n"))
				continue;
			
			if(line.startsWith("##")) {
				port = line.substring(2).trim();
				rank = 1;
			}
			else {
				String[] split = line.split("\t");
				if(split.length == 6) {		// ## MassBank
					String name = split[0].split(";")[0];
					String id = split[1];
					String s = split[4];
					//s = s.replace(s.substring(0, s.indexOf(".")), "0");
					double score = Double.parseDouble(s.substring(s.indexOf(".")));	//.substring(split[4].indexOf(".") + 1)
					//DecimalFormat df = new DecimalFormat("#.####");
					//System.out.println(df.format(score));
					//System.out.println(score);
					
					String prefix = id.substring(0, 2);
					File dir = new File(cacheMassBank);
					String[] institutes = dir.list();
					File f = null;
					String basePath = "";
					for (int j = 0; j < institutes.length; j++) {
						if(institutes[j].startsWith(prefix)) {
							f = new File(dir, institutes[j] + "/mol/");
							basePath = f.getAbsolutePath();
							if(!basePath.endsWith("/"))
								basePath += "/";
							System.out.println("basePath for " + id + " -> " + basePath);
						}
					}
					MassBankUtilities mbu = new MassBankUtilities();
					IAtomContainer mol = mbu.getContainer(id, basePath);
					if(mol == null)
						continue;
					
					primaries.add(new Result(port, id, name, score, mol));
				}
				else {						// ## MetFrag
					
					// collect ID's and fetch moldata from PubChem
					String id = split[0];
					idList.add(id);
					//Map<String, IAtomContainer> idToStructure = ESearchDownload.ESearchDownloadPubChemIDs(idList);
					
					double score = Double.parseDouble(split[2]);
//					IAtomContainer mol = MassBankUtilities.getContainer(id, molPath);
//					if(mol == null)
//						continue;
					IAtomContainer mol = null;
					candidates.add(new Result(port, id, id, score, mol));
				}
				
				rank++;
			}
		}
		br.close();
		
		// fetch moldata
		try {
			Map<String, IAtomContainer> idToStructure = ESearchDownload.ESearchDownloadPubChemIDs(idList);
			for (Result r : candidates) {
				String id = r.getId();
				IAtomContainer mol = idToStructure.get(id);
				r.setMol(mol);
			}
		} catch (Exception e) {
			System.err.println("Error fetching Pubchem moldata for candidates! - Aborting.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		result.add(primaries);
		result.add(candidates);
		
		return result;
	}
	
	@Override
	public void run() {
		try {
			this.log = readLog();
			this.primaries = log.get(0);
			this.candidates = log.get(1);
			
			System.out.println("#primaries -> " + primaries.size());
			System.out.println("#candidates -> " + candidates.size());
		} catch (FileNotFoundException e) {
			this.log = new ArrayList<List<Result>>();
			System.err.println("Error generating Lists for " + f + "\t-> FileNotFoundException");
		} catch (IOException e) {
			this.log = new ArrayList<List<Result>>();
			System.err.println("Error generating Lists for " + f + "\t-> IOException");
		}
		
		/**
		 * old
		 */
		// create tanimoto matrix and perform chemical-similarity based integration
//		List<Result> listMassBank = primaries;
//		List<Result> listMetFrag = candidates;
//		
//		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag, 3, 0.5f);
//		File temp = new File("/tmp/mat.txt");
//		boolean write = sim.writeMatrix(sim.getMatrix(), temp);
//		System.out.println("writing matrix successful ? " + write);
		
//		TanimotoIntegration integration = new TanimotoIntegration(sim);
//		integration.computeNewOrdering();
		
//		int[] second = integration.weightedApproach();
//		integration.computeNewOrderingFromIndices(second);
		
//		RealMatrix rm = sim.getMatrix();
//		ColorcodedMatrix ccm = new ColorcodedMatrix(rm, listMassBank, listMetFrag);
	}
	

	public List<Result> getPrimaries() {
		return primaries;
	}

	public void setPrimaries(List<Result> primaries) {
		this.primaries = primaries;
	}

	public List<Result> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<Result> candidates) {
		this.candidates = candidates;
	}

	public List<List<Result>> getLog() {
		return log;
	}

	public void setLog(List<List<Result>> log) {
		this.log = log;
	}
}
