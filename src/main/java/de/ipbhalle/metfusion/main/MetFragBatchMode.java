/**
 * created by Michael Gerlich, Oct 20, 2011 - 10:48:13 AM
 */ 

package de.ipbhalle.metfusion.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.faces.model.SelectItem;

import massbank.BatchService;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import chemaxon.descriptors.ECFP;

import com.chemspider.www.ExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPISoapProxy;

import de.ipbhalle.CDK.AtomContainerHandler;
import de.ipbhalle.enumerations.Databases;
import de.ipbhalle.enumerations.Fingerprints;
import de.ipbhalle.metfrag.keggWebservice.KeggWebservice;
import de.ipbhalle.metfrag.main.MetFrag;
import de.ipbhalle.metfrag.main.MetFragResult;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfusion.utilities.chemaxon.ChemAxonUtilities;
import de.ipbhalle.metfusion.wrapper.Result;

public class MetFragBatchMode implements Runnable {

	private static final String DEFAULT_IMAGE_CACHE = "/vol/metfrag/images/";
	public final String DEFAULT_IMAGE_ENDING = ".png";
	
	/**
	 * identifier for KEGG
	 */
	private final String dbKEGG = "kegg";
	/**
	 * identifier for PubChem
	 */
	private final String dbPUBCHEM = "pubchem";
	/**
	 * identifier for ChemSpider
	 */
	private final String dbCHEMSPIDER = "chemspider";
	/**
	 * identifier for SDF Upload
	 */
	private final String dbSDF = "sdf";
	/** boolean flag indicating whether SDF upload details are rendered or not */
	private boolean renderSDF = Boolean.FALSE;
	private String selectedSDF = "";
	
	/**
	 * define selected upstream DB - defaults to pubchem
	 */
	private String selectedDB = dbKEGG; 
		//"pubchem";
	
	/**
	 * SelectItem options for upstream DB
	 */
	private SelectItem[] databases = {new SelectItem(dbKEGG, "KEGG"), new SelectItem(dbPUBCHEM, "PubChem"),
			new SelectItem(dbCHEMSPIDER, "ChemSpider"), new SelectItem(dbSDF, "SDF Upload")};
	
	/**
	 * constant which identifes part of a string to be replaced with corresponding DB ID
	 */
	private final String replaceID = "$ID";
	
	/**
	 * map containing standard outgoing links for refering to upstream DB entries
	 * key - upstream DB
	 * value - link  
	 */
	private Map<String, String> linkMap;
	
	/**
	 * variable holding comma-separated ID's for specific DB lookup
	 */
	private String databaseID = "";
	
	/**
	 * variable holding a specified molecular Formula, e.g. C6H12O6
	 */
	private String molecularFormula = "";
	
	/**
	 * variable holding the query spectrum - is filled via the MetFusion (app) bean
	 */
	private String inputSpectrum = "";
	
	/**
	 * variable defining the exact mass for database lookup
	 */
	private double exactMass = 272.06847;
	
	/**
	 * variable holding the allowed absolute deviation for peak matching
	 */
	private double mzabs = 0.01;			// 0.00 for HILL, 0.01 for RIKEN & QSTAR
	
	/**
	 * variable holding relative deviation for peak matching 
	 */
	private double mzppm = 10;
	
	/**
	 * deviation allowed while searching DB with exactMass
	 * it defines a mass range from [exactMass - searchppm] to [exactMass + searchppm]
	 */
	private double searchppm = 10;
	
	/**
	 * the depth of the generated fragment tree. Increasing depth results in longer computational time.
	 */
	private int treeDepth = 2;		// fragmentation tree depth
	
	/**
	 * variable holding the limit of result entries for database lookup
	 */
	private int limit = 5000;		// result limit for upstream DB lookup
	
	/**
	 * variable describing the mode of ionization
	 * 1 - positive mode
	 * -1 - negative mode
	 * 0 - [M+]+ mode for GC-MS
	 */
	private int mode = 1;			// ionization mode
	
	/**
	 * should the inhouse proxy be used?
	 * default: false
	 */
	private boolean proxy = false;
	
	/**
	 * should a redundancy check be performed?
	 * default: true
	 */
	private boolean redundancyCheck = true;
	
	/**
	 * should aromatic rings be broken?
	 * default: true
	 */
	private boolean breakAromaticRings = true;
	
	/**
	 * should a variable number of hydrogen atoms be allowed for matching?
	 * default: true
	 */
	private boolean hydrogenTest = true;
	
	/**
	 * @deprecated
	 */
	private boolean neutralLossLayer = false;
	
	/**
	 * should bond dissociation energy (BDE) scoring impact peak matching formula?
	 * default: true
	 * @deprecated
	 */
	private boolean bondEnergyScoring = true;
	
	/**
	 * should only those bonds be broken with matching partial charges
	 * @deprecated
	 */
	private boolean breakOnlySelectedBonds = false;
	
	/**
	 * boolean indicating if the result list should be shown (true - results available)
	 * or not (false - empty result set or error occured)
	 */
	private boolean showResult = false;
	
	/**
	 * boolean indicating whether candidates should be filtered according to their InChI-Key 1 (true)
	 * or not (false). This filter step would reduce the list of candidates by removing stereoisomers.
	 */
	private boolean uniqueInchi = false;
	
	/** boolean indicating whether to use only compounds containing C,H,N,O,P,S or not. */
	private boolean onlyCHNOPS = true;
	
	/**
	 * the list of results
	 */
	private List<Result> results;
	
	/** original list of MetFrag results */
	private List<MetFragResult> mfResults;
	
	
	private boolean done;
	private String sessionPath;
	private String sessionID = "";
	private ArrayList<SelectItem> adductList;
	private double selectedAdduct;
	private double parentIon;
	
	private String fileSep = System.getProperty("file.separator");
	
	
	private Fingerprints fingerprinter = Fingerprints.CDK;		// default to CDK standard fingerprinter
	
	public MetFragBatchMode(String workDir) {
		sessionPath = workDir;
		fillAdductList();
		fillLinkMap();
	}
	
	private void fillAdductList() {
		this.adductList = new ArrayList<SelectItem>();
		adductList.add(new SelectItem(0d, "Neutral"));
		adductList.add(new SelectItem(0.00054858d, "M+"));
		adductList.add(new SelectItem(-1.007276455d, "[M+H+]"));
		adductList.add(new SelectItem(-22.98921912d, "[M+Na]+"));
		adductList.add(new SelectItem(-38.96315882d, "[M+K]+"));
		adductList.add(new SelectItem(-0.00054858d, "M-"));
		adductList.add(new SelectItem(1.007276455d, "[M-H]-"));
		adductList.add(new SelectItem(22.98921912d, "[M-Na]-"));
		adductList.add(new SelectItem(38.96315882d, "[M-K]-"));
	}
	
	private void fillLinkMap() {
		if(this.linkMap == null) 
			setLinkMap(new HashMap<String, String>());
		else this.linkMap.clear();
		
		for (SelectItem si : databases) {
			String db = si.getValue().toString();
			if(db.equals(dbKEGG)) {
				linkMap.put(db, "http://www.kegg.jp/dbget-bin/www_bget?");
			}
			else if(db.equals(dbPUBCHEM)) {
				linkMap.put(db, "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=");
			}
			else if(db.equals(dbCHEMSPIDER)) {
				linkMap.put(dbCHEMSPIDER, "http://www.chemspider.com/Chemical-Structure." + replaceID + ".html");	// + candidateID + ".html"
			}
			else {
				System.err.println("No link currently available for [" + db + "].");
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MetFragBatchMode metfragbm = new MetFragBatchMode("/home/mgerlich/Desktop/testBatchmode/");
		
		MetFusionBatchFileHandler mbfr = new MetFusionBatchFileHandler(new File("/home/mgerlich/Documents/metfusion_param_default.mf"));
		try {
			mbfr.readFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mbfr.printSettings();
		
		MetFusionBatchSettings settings = mbfr.getBatchSettings();
		metfragbm.setInputSpectrum(settings.getPeaks());
		metfragbm.setSelectedDB(settings.getMfDatabase().toString());
		metfragbm.setMolecularFormula(settings.getMfFormula());
		metfragbm.setSelectedAdduct(settings.getMfAdduct().getDifference());
		metfragbm.setParentIon(settings.getMfParentIon());
		metfragbm.setExactMass(settings.getMfExactMass());
		metfragbm.setMzabs(settings.getMfMZabs());
		metfragbm.setMzppm(settings.getMfMZppm());
		metfragbm.setSearchppm(settings.getMfSearchPPM());
		metfragbm.setLimit(settings.getMfLimit());
		metfragbm.setDatabaseID(settings.getMfDatabaseIDs());

		metfragbm.run();
	}

	@Override
	public void run() {
		this.setDone(Boolean.FALSE);
		WrapperSpectrum spectrum = new WrapperSpectrum(inputSpectrum, mode, exactMass, true);
		
		/**sessionPath
		 * TODO: auskommentiert für Evaluationsläufe
		 */
		String currentFolder = sessionPath;
		System.out.println("currentFolder -> " + currentFolder);
		String tempPath = currentFolder;	//sep + "temp" + sep;
		
		Properties props = new Properties();
		FileInputStream in;
		try {
			String file = System.getProperty("property.file.path");
			if(file == null)
				file = "";
			if(!file.endsWith(fileSep))
				file += fileSep;
				
			in = new FileInputStream(file + "settings.properties");
			props.load(in);
			in.close();
		} catch (FileNotFoundException e1) {
			System.err.println("Error loading properties file! - File not found.");
		} catch (IOException e) {
			System.err.println("Error reading properties file!");
		}
		// load required property values
		String jdbc = "", username = "", password = "", token = "";
		jdbc = props.getProperty("mfjdbc");
		username = props.getProperty("mfusername");
		password = props.getProperty("mfpassword");
		token = props.getProperty("token");
		
		// short decimal format for score and/or exact mass
		DecimalFormat threeDForm = new DecimalFormat("#.###");
		
		try {
			String database = getSelectedDB();
			String databaseID = getDatabaseID();
			String molecularFormula = getMolecularFormula();
			double exactMass = getExactMass();
			boolean useProxy = isProxy();
			double mzabs = getMzabs();
			double mzppm = getMzppm();
			double searchPPM = getSearchppm();
			boolean molecularFormulaRedundancyCheck = isRedundancyCheck();
			boolean breakAromaticRings = isBreakAromaticRings();
			int treeDepth = 2;
			int limit = getLimit();
			boolean hydrogenTest = isHydrogenTest();
			boolean neutralLossInEveryLayer = isNeutralLossLayer();
			boolean bondEnergyScoring = isBondEnergyScoring();
			boolean breakOnlySelectedBonds = isBreakOnlySelectedBonds();
			boolean uniqueInchi = isUniqueInchi();
			boolean onlyCHNOPS = isOnlyCHNOPS();
				
			List<MetFragResult> result = new ArrayList<MetFragResult>();
			if(database.equals(dbSDF))
				result = MetFrag.startConvenienceSDF(spectrum, useProxy, mzabs, mzppm, searchPPM, molecularFormulaRedundancyCheck,
						breakAromaticRings, treeDepth, hydrogenTest, neutralLossInEveryLayer, bondEnergyScoring, 
						breakOnlySelectedBonds, limit, Boolean.FALSE, selectedSDF);
			else if(database.equals(Databases.chebi.toString())) {
				jdbc = "jdbc:postgresql://rdbms2:5432/metchem";
				username = "mgerlich";
				password = "unreal0";
				result = MetFrag.startConvenienceLocal(database, databaseID, molecularFormula, exactMass, spectrum,
						useProxy, mzabs, mzppm, searchPPM, molecularFormulaRedundancyCheck, breakAromaticRings, treeDepth, 
						hydrogenTest, neutralLossInEveryLayer, bondEnergyScoring, breakOnlySelectedBonds, limit, 
						jdbc, username, password, 2, onlyCHNOPS);
			}
			else  result = MetFrag.startConvenienceMetFusion(database, databaseID, 
					molecularFormula, exactMass, spectrum, useProxy, mzabs, mzppm, searchPPM, 
					molecularFormulaRedundancyCheck, breakAromaticRings, treeDepth, hydrogenTest,
					neutralLossInEveryLayer, bondEnergyScoring, breakOnlySelectedBonds, limit, jdbc, username, password, uniqueInchi, onlyCHNOPS);
			this.mfResults = result;
			System.out.println("MetFrag result#: " + result.size() + "\n");
			this.results = new ArrayList<Result>();
			this.results.clear();
			
			MassSpecAPISoapProxy chemSpiderProxy = null;
			if(database.equals(dbCHEMSPIDER))
				chemSpiderProxy = new MassSpecAPISoapProxy();
			
			int current = 0;
			SmilesGenerator sg = new SmilesGenerator();
			ChemAxonUtilities cau = null;	// instantiate ChemAxon utilities only when appropriate Fingerprinter is used
	        boolean useChemAxon = Boolean.FALSE;
	        if(getFingerprinter().equals(Fingerprints.ECFP)) {
	        	cau = new ChemAxonUtilities(Boolean.FALSE);
	        	useChemAxon = Boolean.TRUE;
	        }
	        else if(getFingerprinter().equals(Fingerprints.FCFP)) {
	        	cau = new ChemAxonUtilities(Boolean.TRUE);
	        	useChemAxon = Boolean.TRUE;
	        }
			
			for (MetFragResult mfr : result) {
				if(mfr.getStructure() != null) {
					IAtomContainer container = mfr.getStructure();

					// compute molecular formula
					IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(container);
					String formula = MolecularFormulaManipulator.getString(iformula);
					// compute molecular mass
					double emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
					emass = roundThreeDecimals(emass);	//Double.valueOf(threeDForm.format(emass));	// shorten exact mass to 3 decimal places
					
					// hydrogen handling
	                if(container != null) {
	                	try {
	                		// first method to add explicit hydrogens
	                		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
	                        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
	                        hAdder.addImplicitHydrogens(container);
	                        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
	                        
	                        // second method to add explicit hydrogens
	                		//container = AtomContainerHandler.addExplicitHydrogens(container);
	                	} catch (CDKException e) {
	                		System.err.println("error manipulating mol for " + mfr.getCandidateID());
	                		continue;
	                	}
	                }
					
					// remove hydrogens
					//container = AtomContainerManipulator.removeHydrogens(container);
					
					String filename = mfr.getCandidateID() + DEFAULT_IMAGE_ENDING;
//					File image = new File(currentFolder, filename);
//					if(stf != null && !image.exists())
//						stf.writeMOL2PNGFile(container, filename);
					
					String url = linkMap.get(selectedDB);
					if(selectedDB.equals(dbCHEMSPIDER))
						url = url.replace(replaceID, mfr.getCandidateID());
					url += mfr.getCandidateID();
					
					List<String> names = new ArrayList<String>();
					String name = "";
					if(database.equals(dbKEGG)) {
						String[] temp = KeggWebservice.KEGGgetNameByCpd(mfr.getCandidateID());
						if(temp != null && temp.length > 0) {
							for (int i = 0; i < temp.length; i++) {
								names.add(temp[i]);
							}
						}
						if(names.size() > 0)
							name = names.get(0);
					}
					else if(database.equals(dbPUBCHEM)) {
						/**
						 * local pubchem
						 */
						PubChemLocal pl = new PubChemLocal(jdbc, username, password);
						names = pl.getNames(mfr.getCandidateID());
						
						/**
						 * online pubchem
						 */
						
						if(names.size() > 0)
							name = names.get(0);	
					}
					else if(database.equals(dbCHEMSPIDER)) {
						int id = Integer.parseInt(mfr.getCandidateID());
						ExtendedCompoundInfo cpdInfo = chemSpiderProxy.getExtendedCompoundInfo(id, token);
						name = cpdInfo.getCommonName();
					}
					else {
						System.err.println("unknown database [" + database + "] - or not yet supported!");
					}
					
					if(name == null || name.isEmpty())
						name = mfr.getCandidateID();
					
					// create SMILES from IAtomContainer
					String smiles = sg.createSMILES(container);
					
					Result r = new Result("MetFrag", mfr.getCandidateID(), name, mfr.getScore(), container, url, tempPath + filename,
							"", formula, emass, mfr.getPeaksExplained());
					r.setSmiles(smiles);
					
					// create ECFP from SMILES
					if(useChemAxon) {
						ECFP ecfp = cau.generateECFPFromName(smiles);
						r.setEcfp(ecfp);
					}
					
					results.add(r);

					current++;
				}
				else {
					System.err.println(mfr.getCandidateID() + " no structure!");
				}
			}
			current = mfResults.size();
			setDone(Boolean.TRUE);
		} catch (Exception e) {
			setDone(Boolean.TRUE);
			e.printStackTrace();
		}
		
	}

	/**
	 * Round an input do score to three decimals.
	 * 
	 * @param d the double value to be rounded
	 * 
	 * @return the resulting three decimal double
	 */
	private double roundThreeDecimals(double d) {
		NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
		if (f instanceof DecimalFormat) {
			((DecimalFormat) f).applyPattern("#.###");
			return Double.valueOf(((DecimalFormat) f).format(d));
		}
		
		DecimalFormat threeDForm = new DecimalFormat("#.###");
		try {
			Double newD = Double.valueOf(threeDForm.format(d));
			return newD;
		} catch(NumberFormatException e) {
			return d;
		}
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}

	public boolean isRenderSDF() {
		return renderSDF;
	}

	public void setRenderSDF(boolean renderSDF) {
		this.renderSDF = renderSDF;
	}

	public String getSelectedSDF() {
		return selectedSDF;
	}

	public void setSelectedSDF(String selectedSDF) {
		this.selectedSDF = selectedSDF;
	}

	public String getSelectedDB() {
		return selectedDB;
	}

	public void setSelectedDB(String selectedDB) {
		this.selectedDB = selectedDB;
	}

	public SelectItem[] getDatabases() {
		return databases;
	}

	public void setDatabases(SelectItem[] databases) {
		this.databases = databases;
	}

	public Map<String, String> getLinkMap() {
		return linkMap;
	}

	public void setLinkMap(Map<String, String> linkMap) {
		this.linkMap = linkMap;
	}

	public String getDatabaseID() {
		return databaseID;
	}

	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public String getInputSpectrum() {
		return inputSpectrum;
	}

	public void setInputSpectrum(String inputSpectrum) {
		this.inputSpectrum = inputSpectrum;
	}

	public double getExactMass() {
		return exactMass;
	}

	public void setExactMass(double exactMass) {
		this.exactMass = exactMass;
	}

	public double getMzabs() {
		return mzabs;
	}

	public void setMzabs(double mzabs) {
		this.mzabs = mzabs;
	}

	public double getMzppm() {
		return mzppm;
	}

	public void setMzppm(double mzppm) {
		this.mzppm = mzppm;
	}

	public double getSearchppm() {
		return searchppm;
	}

	public void setSearchppm(double searchppm) {
		this.searchppm = searchppm;
	}

	public int getTreeDepth() {
		return treeDepth;
	}

	public void setTreeDepth(int treeDepth) {
		this.treeDepth = treeDepth;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public boolean isProxy() {
		return proxy;
	}

	public void setProxy(boolean proxy) {
		this.proxy = proxy;
	}

	public boolean isRedundancyCheck() {
		return redundancyCheck;
	}

	public void setRedundancyCheck(boolean redundancyCheck) {
		this.redundancyCheck = redundancyCheck;
	}

	public boolean isBreakAromaticRings() {
		return breakAromaticRings;
	}

	public void setBreakAromaticRings(boolean breakAromaticRings) {
		this.breakAromaticRings = breakAromaticRings;
	}

	public boolean isHydrogenTest() {
		return hydrogenTest;
	}

	public void setHydrogenTest(boolean hydrogenTest) {
		this.hydrogenTest = hydrogenTest;
	}

	public boolean isNeutralLossLayer() {
		return neutralLossLayer;
	}

	public void setNeutralLossLayer(boolean neutralLossLayer) {
		this.neutralLossLayer = neutralLossLayer;
	}

	public boolean isBondEnergyScoring() {
		return bondEnergyScoring;
	}

	public void setBondEnergyScoring(boolean bondEnergyScoring) {
		this.bondEnergyScoring = bondEnergyScoring;
	}

	public boolean isBreakOnlySelectedBonds() {
		return breakOnlySelectedBonds;
	}

	public void setBreakOnlySelectedBonds(boolean breakOnlySelectedBonds) {
		this.breakOnlySelectedBonds = breakOnlySelectedBonds;
	}

	public boolean isShowResult() {
		return showResult;
	}

	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public List<MetFragResult> getMfResults() {
		return mfResults;
	}

	public void setMfResults(List<MetFragResult> mfResults) {
		this.mfResults = mfResults;
	}

	public String getSessionPath() {
		return sessionPath;
	}

	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getReplaceID() {
		return replaceID;
	}

	public double getSelectedAdduct() {
		return selectedAdduct;
	}

	public void setSelectedAdduct(double selectedAdduct) {
		this.selectedAdduct = selectedAdduct;
	}

	public double getParentIon() {
		return parentIon;
	}

	public void setParentIon(double parentIon) {
		this.parentIon = parentIon;
	}

	public boolean isUniqueInchi() {
		return uniqueInchi;
	}

	public void setUniqueInchi(boolean uniqueInchi) {
		this.uniqueInchi = uniqueInchi;
	}

	public void setFingerprinter(Fingerprints fingerprinter) {
		this.fingerprinter = fingerprinter;
	}

	public Fingerprints getFingerprinter() {
		return fingerprinter;
	}

	public boolean isOnlyCHNOPS() {
		return onlyCHNOPS;
	}

	public void setOnlyCHNOPS(boolean onlyCHNOPS) {
		this.onlyCHNOPS = onlyCHNOPS;
	}

}
