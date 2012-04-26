/**
 * created by Michael Gerlich, Oct 20, 2011 - 10:48:28 AM
 */

package de.ipbhalle.metfusion.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.enumerations.Ionizations;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.wrapper.Result;

import massbank.GetConfig;
import massbank.GetInstInfo;
import massbank.MassBankCommon;

public class MassBankBatchMode implements Runnable {

	private String serverUrl = "http://www.massbank.jp/";	//"http://www.massbank.jp/";
	private String cacheMassBank = "/vol/massbank/Cache/";
	private static final String fileSeparator = System.getProperty("file.separator");
	private static final String os = System.getProperty("os.name");
	private static final String currentDir = System.getProperty("user.dir");
	private static final String tempDir = System.getProperty("java.io.tmpdir");
	
	private MassBankCommon mbCommon;
	private GetConfig config;
	private GetInstInfo instInfo;
	private Map<String, List<String>> instruments;

	private String inputSpectrum;

	private ArrayList<String> queryResults;

	private ArrayList<Result> unused;

	private static final String EI = "EI";
	private static final String ESI = "ESI";
	private static final String OTHER = "Others";

	private static final String RECORDS = "records";
	private static final String MOL = "mol";
	
	private int limit = 100;
	private int cutoff = 5;

	private List<Result> results;
	private String sessionPath;
	private String selectedIon;
	private String selectedInstruments;
	
	private boolean done = Boolean.FALSE;
	
	public MassBankBatchMode(String workDir, Ionizations ion, String serverUrl) {
		this.sessionPath = workDir;
		this.selectedIon = String.valueOf(ion.getValue());
		this.serverUrl = serverUrl;
		
		this.setMbCommon(new MassBankCommon());
		this.setConfig(new GetConfig(this.serverUrl));
		this.setInstInfo(new GetInstInfo(this.serverUrl));
		this.setInstruments(this.instInfo.getTypeGroup());

		Map<String, List<String>> instGroup = instInfo.getTypeGroup();
		Iterator<String> it = instGroup.keySet().iterator();
		int counter = 0;
		StringBuilder sb = new StringBuilder();

		// iterate over instrument groups
		while (it.hasNext()) {
			String next = it.next();
			// sig[counter] = new SelectItemGroup(next);
			List<String> items = instGroup.get(next); // retrieve instruments
														// from current
														// instrument group
			String[] instruments = new String[items.size()];

			SelectItem[] si = new SelectItem[items.size()];
			for (int i = 0; i < si.length; i++) {
				String s = items.get(i);
				si[i] = new SelectItem(s, s);
				sb.append(s).append(",");

				// add instrument to list for corresponding group
				if (s.contains(ESI))
					instruments[i] = s; // preselect all ESI instruments
				else
					instruments[i] = ""; // deselect all remaining instruments
											// (EI, Others)
			}
			counter++;
			this.instruments.put(next, items);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MassBankBatchMode mbbm = new MassBankBatchMode("/home/mgerlich/Desktop/testBatchmode/", Ionizations.pos, "http://www.massbank.jp/");
		
		MetFusionBatchFileHandler mbfr = new MetFusionBatchFileHandler(new File("/home/mgerlich/Documents/metfusion_param_default.mf"));
		try {
			mbfr.readFile();
		} catch (IOException e) {
			//System.err.println("Error while reading/accessing batch file [" + arg_mf + "]!");
			e.printStackTrace();
			//System.exit(-1);
		}
		mbfr.printSettings();
		mbbm.setInputSpectrum(mbfr.getBatchSettings().getPeaks());
		mbbm.setSelectedInstruments(mbfr.getBatchSettings().getMbInstruments());
		mbbm.setLimit(mbfr.getBatchSettings().getMbLimit());
		
		mbbm.run();
	}

	private String formatPeaks() {
		StringBuilder peaklist = new StringBuilder();
		String temp = inputSpectrum.trim();

		// assume that validation took place -> one peak per line, mz space int
		String[] split = temp.split("\n");
		for (int i = 0; i < split.length; i++) {
			String[] line = split[i].split("\\s");

			String mz = "";
			String inte = "";

			if (line.length == 0) {
				System.err.println("Error parsing peaklist!");
				continue;
			} else if (line.length == 1) {
				// assume that only mz values are given
				mz = line[0];
				inte = "100";
			} else if (line.length == 2) {
				mz = line[0];
				inte = line[1];
			} else if (line.length == 3) {
				// assume that first mz, then rel.int, then int
				mz = line[0];
				inte = line[2];
			} else {
				// assume that first mz, then rel.int, then int
				mz = line[0];
				inte = line[2];
			}

			if (i == (split.length - 1))
				peaklist.append(mz).append(",").append(inte);
			else
				peaklist.append(mz).append(",").append(inte).append("@");
		}

		return peaklist.toString();
	}

	@Override
	public void run() {
        String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
        StringBuilder sb = new StringBuilder();
        sb.append("&INST=");
        sb.append(selectedInstruments);
        
        String inst = sb.toString();
        if(inst.endsWith(","))		// remove trailing comma
        	inst = inst.substring(0, inst.length() - 1);

        /**
         * build up parameter string for MassBank search
         */
        String ion = "&ION=" + selectedIon;
        inst += ion;
        
        //String paramPeak = "273.096,22@289.086,107@290.118,14@291.096,999@292.113,162@293.054,34@579.169,37@580.179,15";
        String paramPeak = formatPeaks();
		String param = "quick=true&CEILING=1000&WEIGHT=SQUARE&NORM=SQRT&START=1&TOLUNIT=unit"
				+ "&CORTYPE=COSINE&FLOOR=0&NUMTHRESHOLD=3&CORTHRESHOLD=0.8&TOLERANCE=0.3"
				+ "&CUTOFF=" + cutoff + "&NUM=0&VAL=" + paramPeak.toString();
		param += inst;
		System.out.println(param);
		/**
		 * 
		 */
		
		// retrieve result list
		ArrayList<String> result = mbCommon.execMultiDispatcher(serverUrl, typeName, param);
		this.queryResults = new ArrayList<String>();
		queryResults = result;
		
		if(queryResults.size() == 0)
			return;
		
		this.unused = new ArrayList<Result>();
		
		//this.queryResults = result;
		System.out.println("MassBank results# = " + result.size() + "\n");
		
		wrapResults();
	}

	private void wrapResults() {
		String relImagePath = sessionPath; 	//sep + "temp" + sep + sessionString + sep;
		System.out.println("relImagePath -> " + relImagePath);
		String tempPath = relImagePath;
		
        List<Result> results = new ArrayList<Result>();
        List<String> duplicates = new ArrayList<String>();

        MassBankUtilities mbu = new MassBankUtilities(serverUrl, cacheMassBank);
        
        String name = "";
        String id = "";
        double score = 0.0d;
        String site = "";
        String sumFormula = "";
        
        int limitCounter = 0;
        int resultLimit = (limit >= queryResults.size()) ? (queryResults.size()-1) : limit;
        SmilesGenerator sg = new SmilesGenerator();		// SmilesGenerator for IAtomContainer
        
        for(int i = 0; i < queryResults.size(); i++) {
            String s = queryResults.get(i);
            
            /**
             *  create results only till the given limit
             */
            if(limitCounter == resultLimit)
            	break;

            String[] split = s.split("\t");
            if(split.length == 6) {
                // name; instrument
                // id
                // ionization mode
                // sum formula
                // score
                // site
                name = split[0].substring(0, split[0].indexOf(";"));
                id = split[1].trim();
                sumFormula = split[3].trim();
                score = Double.parseDouble(split[4].substring(split[4].indexOf(".")));
                site = split[5];

                //String record = MassBankUtilities.retrieveRecord(id, site);
//                MassBankUtilities.fetchRecord(id, site);
                //String mol = MassBankUtilities.retrieveMol(name, site, id);

                String prefix = id.substring(0, 2);
                File dir = null;
                File cache = null;
        		if(os.startsWith("Windows")) {
        			dir = new File(tempDir);
        			cache = new File(dir, prefix);
        		}
        		else {
        			dir = new File(cacheMassBank);
        			
        			String[] institutes = dir.list();
        			if(institutes != null) {
        				for (int inst = 0; inst < institutes.length; inst++) {
        					if(institutes[inst].startsWith(prefix)) {
        						cache = new File(dir, institutes[inst]);		//  + fileSeparator + "mol" + fileSeparator
        						break;
        					}
        				}
        			}
        			else cache = new File(dir, prefix);
        		}
        		//else dir = new File(tempDir);
        		
                String basePath = "";
                boolean createDir = false;
                if(!cache.exists()) {		// cache folder does not exist, create it
                	createDir = cache.mkdirs();
                	if(createDir) {
                		System.out.println("created directory [" + cache.getAbsolutePath() + "] -> " + createDir);
                		File molDir = new File(cache, MOL);
                        File recDir = new File(cache, RECORDS);
                        System.out.println("created molDir ? " + molDir.mkdir() + "\treated recDir ? " + recDir.mkdir());	// create subdirectories
                        basePath = molDir.getAbsolutePath();
                	}
                }
                else {		// cache folder already exists 
                	File molDir = new File(cache, fileSeparator + MOL + fileSeparator);
                	if(!molDir.isDirectory())
                		molDir.mkdirs();
                	
                	basePath = molDir.getAbsolutePath();
                }

                boolean fetch = false;

                // create AtomContainer via SMILES
                //Map<String, String> links = MassBankUtilities.retrieveLinks(id, site);
                Map<String, String> links = mbu.retrieveLinks(id, site);
                String smiles = links.get("smiles");
                if(smiles == null)
                	smiles = "";
                
                //System.out.println("smiles -> " + smiles);
                IAtomContainer container = null;
                // first look if container is present, then download if not
                //container = MassBankUtilities.getContainer(id, basePath);
                container = mbu.getContainer(id, basePath);
                if(container == null) {
                    //fetch = MassBankUtilities.fetchMol(name, id, site, basePath);
                	fetch = mbu.fetchMol(name, id, site, basePath);
                    if(fetch) {
                        System.out.println("container via fetch");
                        //container = MassBankUtilities.getMolFromAny(id, basePath, smiles);
                        //container = MassBankUtilities.getContainer(id, basePath);
                        container = mbu.getContainer(id, basePath);
                    }
                    else {
                        System.out.println("container via smiles");
                        //container = MassBankUtilities.getMolFromSmiles(smiles);
                        container = mbu.getMolFromSmiles(smiles);

                        if(container != null) {
                            // write out molfile
                            File mol = new File(basePath, id + ".mol");
                            //MassBankUtilities.writeContainer(mol, container);
                            mbu.writeContainer(mol, container);
                        }
                    }
                }

                // hydrogen handling
//                if(container != null) {
                	// remove hydrogens
//        			container = AtomContainerManipulator.removeHydrogens(container);
        			
//                    try {
//	                	container = AtomContainerHandler.addExplicitHydrogens(container);
	                	
//                        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
//                        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
//                        hAdder.addImplicitHydrogens(container);
//                        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
//                    } catch (CDKException e) {
//                        System.err.println("error manipulating mol for " + id);
//                        // add erroneous record to list of unuses entries
//                        Result r = new Result("MassBank", id, name, score, container, "", tempPath + id + ".png");
//                    	r.setSmiles(smiles);
//                    	unused.add(r);
//                    	
//                        continue;
//                    }
//                }

                /**
                 *  if entry is not present yet, add it - else don't
                 */
                if(container != null) {	// removed duplicate check -> !duplicates.contains(name) &&
                	// compute molecular formula
					IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(container);
					if(iformula == null)	// fallback to MassBank sum formula
						iformula = MolecularFormulaManipulator.getMolecularFormula(sumFormula, NoNotificationChemObjectBuilder.getInstance());
					String formula = MolecularFormulaManipulator.getHTML(iformula);
					// compute molecular mass
					double emass = 0.0d;
					if(!formula.contains("R"))	// compute exact mass from formula only if NO residues "R" are present
						emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
					//else emass = MassBankUtilities.retrieveExactMass(id, site);
					else emass = mbu.retrieveExactMass(id, site);

					if(smiles.isEmpty())	// create SMILES if empty
						smiles = sg.createSMILES(container);
					
					duplicates.add(name);
                    //results.add(new Result("MassBank", id, name, score, container, url, relImagePath + id + ".png"));
					Result r = new Result("MassBank", id, name, score, container, "", tempPath + id + ".png", formula, emass);
					r.setSmiles(smiles);
					
                    results.add(r);
                    limitCounter++;
                }

                // add unused results (duplicate or no mol container) to list
                if(!fetch && container == null) {
                    //unused.add(new Result("MassBank", id, name, score, container, url, relImagePath + id + ".png"));
                	Result r = new Result("MassBank", id, name, score, container, "", tempPath + id + ".png");
                	r.setSmiles(smiles);
                	
                	unused.add(r);
                }
            }
            else if(split.length == 7) {
                    System.err.println("length == 7");
            }
            else {
            	System.err.println("unknown split length! - time to update MassBank format!?!");
            }
        }
        System.out.println("entries after duplicate removal -> " + results.size());
        this.results = results;
	}
	
	public MassBankCommon getMbCommon() {
		return mbCommon;
	}

	public void setMbCommon(MassBankCommon mbCommon) {
		this.mbCommon = mbCommon;
	}

	public GetConfig getConfig() {
		return config;
	}

	public void setConfig(GetConfig config) {
		this.config = config;
	}

	public GetInstInfo getInstInfo() {
		return instInfo;
	}

	public void setInstInfo(GetInstInfo instInfo) {
		this.instInfo = instInfo;
	}

	public Map<String, List<String>> getInstruments() {
		return instruments;
	}

	public void setInstruments(Map<String, List<String>> instruments) {
		this.instruments = instruments;
	}

	public String getInputSpectrum() {
		return inputSpectrum;
	}

	public void setInputSpectrum(String inputSpectrum) {
		this.inputSpectrum = inputSpectrum;
		formatPeaks();
	}

	public ArrayList<String> getQueryResults() {
		return queryResults;
	}

	public void setQueryResults(ArrayList<String> queryResults) {
		this.queryResults = queryResults;
	}

	public ArrayList<Result> getUnused() {
		return unused;
	}

	public void setUnused(ArrayList<Result> unused) {
		this.unused = unused;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public String getSessionPath() {
		return sessionPath;
	}

	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	public String getSelectedIon() {
		return selectedIon;
	}

	public void setSelectedIon(String selectedIon) {
		this.selectedIon = selectedIon;
	}

	public String getSelectedInstruments() {
		return selectedInstruments;
	}

	public void setSelectedInstruments(String selectedInstruments) {
		this.selectedInstruments = selectedInstruments;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getCacheMassBank() {
		return cacheMassBank;
	}

	public void setCacheMassBank(String cacheMassBank) {
		this.cacheMassBank = cacheMassBank;
	}

}
