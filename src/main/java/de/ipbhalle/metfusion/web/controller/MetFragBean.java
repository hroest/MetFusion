/**
 * created by Michael Gerlich on May 21, 2010
 * last modified May 21, 2010 - 10:06:51 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.web.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.icefaces.component.fileentry.FileEntry;
import org.icefaces.component.fileentry.FileEntryEvent;
import org.icefaces.component.fileentry.FileEntryResults;
import org.icefaces.component.fileentry.FileEntryResults.FileInfo;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.chemspider.www.ExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPISoapProxy;

import de.ipbhalle.CDK.AtomContainerHandler;
import de.ipbhalle.metfrag.keggWebservice.KeggWebservice;
import de.ipbhalle.metfrag.main.MetFrag;
import de.ipbhalle.metfrag.main.MetFragResult;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfusion.wrapper.Result;


@ManagedBean(name="fragmenterBean")
@SessionScoped
//@CustomScoped(value = "#{window}")
public class MetFragBean implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_IMAGE_CACHE = "/vol/metfrag/images/";
	
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
	private boolean validSDF = Boolean.FALSE;
	private String noteSDF = "";
	
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
	private int limit = 500;		// result limit for upstream DB lookup
	
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
	 * the list of results
	 */
	private List<Result> results;
	
	/** original list of MetFrag results */
	private List<MetFragResult> mfResults;
	
	/**
	 * the separate thread container for this class, allows parallel execution
	 */
	private Thread t;
	
	private int progress = 0;	
	private boolean done = Boolean.FALSE;
	
//	private FacesContext fc = FacesContext.getCurrentInstance();
//    private HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
//    private String sessionString = session.getId();
//    private ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
    private final String sep = System.getProperty("file.separator");
//    private String webRoot = scontext.getRealPath(sep);
    
    public final String DEFAULT_IMAGE_ENDING = ".png";
    
	private final String molPath = "/home/mgerlich/workspace-3.5/MetFusion2/testdata/Hill/mol/";
	
	private String sessionPath;
	private String sessionID = "";
	
	private final String landingPage = "http://msbi.ipb-halle.de/MetFrag/LandingPage.jspx?";	
	//"http://msbi.ipb-halle.de/MetFragBeta/LandingPage.jspx?";
	
	private List<SelectItem> adductList;
	private double selectedAdduct;
	private double parentIon;
	
	
	public MetFragBean() {
		//t = new Thread(this, "metfrag");
		fillLinkMap();
		fillAdductList();
		this.parentIon = this.exactMass;
		this.selectedAdduct = (Double) this.adductList.get(0).getValue();	// set to neutral adduct
		this.exactMass = this.selectedAdduct + this.parentIon;
		
		//FacesContext fc = FacesContext.getCurrentInstance();
//		ELResolver el = fc.getApplication().getELResolver();
//        ELContext elc = fc.getELContext();
        //HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
		//String sessionString = session.getId();
		//System.out.println("MetFragBean sessionID -> " + sessionString);
	}

	private String formatLandingURL(String peaks, String database, String id, double mass, String formula) {
		if(formula == null || formula.isEmpty())
			return String.format("peaks=%1$s&database=%2$s&databaseID=%3$s&mass=%4$f", peaks, database, id, mass);
		else return String.format("peaks=%1$s&database=%2$s&databaseID=%3$s&mass=%4$f&formula=%5$s", peaks, database, id, mass, formula);
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
	
	public void changeAdduct(ValueChangeEvent event) {
		double adduct = (Double) event.getNewValue();
		setSelectedAdduct(adduct);
		
		this.exactMass = getSelectedAdduct() + getParentIon();
	}
	
	public void changeParentIon(ValueChangeEvent event) {
		double parent = (Double) event.getNewValue();
		setParentIon(parent);
		
		this.exactMass = getSelectedAdduct() + getParentIon();
	}
	
	public void changeDatabase(ValueChangeEvent event) {
		String newVal = (String) event.getNewValue();
		if(newVal.equals(dbSDF)) {
			renderSDF = Boolean.TRUE;
		}
		else renderSDF = Boolean.FALSE;
	}
	
	public void listener(FileEntryEvent event) {
	    FileEntry fileEntry = (FileEntry) event.getSource();
	    FileEntryResults results = fileEntry.getResults();
	    this.validSDF = Boolean.FALSE;
	    this.selectedSDF = "";
	    System.out.println("#uploads -> " + results.getFiles().size());
	    if(results.getFiles().size() == 0)	{// nothing was uploaded
	    	System.err.println("Nothing was uploaded.");
        	this.selectedSDF = "";
        	this.validSDF = Boolean.FALSE;
        	this.noteSDF = "Please specify a SDF file for upload!";
	    }
	    
	    for (FileInfo fileInfo : results.getFiles()) {	// JSF restriction to 1 uploaded file...
	        if (fileInfo.isSaved() && fileInfo.getFileName().endsWith("sdf")) {		// TODO: check for valid SDF
	            // Process the file. Only save cloned copies of results or fileInfo
	        	System.out.println(fileInfo.getFileName() + "\t" + fileInfo.getContentType());
	        	this.selectedSDF = fileInfo.getFileName();
	        	this.validSDF = Boolean.TRUE;
	        	this.noteSDF = "SDF Upload successful!";
	        	break;
	        }
	        else {
	        	System.err.println("No valid SDF file.");
	        	this.selectedSDF = "";
	        	this.validSDF = Boolean.FALSE;
	        	this.noteSDF = "Error during SDF Upload!";
	        }
	    }
	}
	
	public void submit(ActionEvent event) {
		this.progress = 0;
		this.done = Boolean.FALSE;
		
		// handle wrong SDF
		if(selectedDB.equals(dbSDF) && !validSDF) {
			this.done = Boolean.TRUE;
			this.progress = 100;
			this.showResult = false;
			this.noteSDF = "No valid SDF uploaded - canceled run.";
			return;
		}
		
		WrapperSpectrum spectrum = new WrapperSpectrum(inputSpectrum, mode, exactMass, true);
		
//		FacesContext fc = FacesContext.getCurrentInstance();
//	    HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
//	    String sessionString = session.getId();
//	    ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
//	    String webRoot = scontext.getRealPath(sep);
	    
		/**sessionPath
		 * TODO: auskommentiert für Evaluationsläufe
		 */
		String currentFolder = sep + "temp" + sep + sessionID + sep;
		//getSessionPath();	//webRoot + sep + "temp" + sep + sessionString + sep;
		System.out.println("currentFolder -> " + currentFolder);
		String tempPath = currentFolder;	//sep + "temp" + sep;
//		
//	    new File(currentFolder).mkdirs();
//	    StructureToFile stf = null;
//		try {
//			stf = new StructureToFile(200, 200, currentFolder, false, false);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		String spectrumURLEncoded = "";
		try {
			spectrumURLEncoded = URLEncoder.encode(inputSpectrum, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Error while encoding input spectrum for URL use!");
		}
		
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
			
			String jdbc, username, password = "";
			jdbc = "jdbc:mysql://rdbms/MetFrag";
			username = "swolf";
			password = "populusromanus";
//			List<MetFragResult> result = MetFrag.startConvenience(database, databaseID, molecularFormula, exactMass, 
//					spectrum, useProxy, mzabs, mzppm, searchPPM, molecularFormulaRedundancyCheck, 
//					breakAromaticRings, treeDepth, hydrogenTest, neutralLossInEveryLayer,
//					bondEnergyScoring, breakOnlySelectedBonds, limit, false);
			List<MetFragResult> result = new ArrayList<MetFragResult>();
			if(database.equals(dbSDF))
				result = MetFrag.startConvenienceSDF(spectrum, useProxy, mzabs, mzppm, searchPPM, molecularFormulaRedundancyCheck,
						breakAromaticRings, treeDepth, hydrogenTest, neutralLossInEveryLayer, bondEnergyScoring, 
						breakOnlySelectedBonds, limit, Boolean.FALSE, sessionPath + selectedSDF);
			else  result = MetFrag.startConvenienceMetFusion(database, databaseID, 
					molecularFormula, exactMass, spectrum, useProxy, mzabs, mzppm, searchPPM, 
					molecularFormulaRedundancyCheck, breakAromaticRings, treeDepth, hydrogenTest,
					neutralLossInEveryLayer, bondEnergyScoring, breakOnlySelectedBonds, limit, jdbc, username, password);
			this.mfResults = result;
			System.out.println("MetFrag result#: " + result.size() + "\n");
			this.results = new ArrayList<Result>();
			this.results.clear();
			
			MassSpecAPISoapProxy chemSpiderProxy = null;
			if(database.equals(dbCHEMSPIDER))
				chemSpiderProxy = new MassSpecAPISoapProxy();
			
			int current = 0;
			
			for (MetFragResult mfr : result) {
				if(mfr.getStructure() != null) {
					IAtomContainer container = mfr.getStructure();

					// compute molecular formula
					IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(container);
					String formula = MolecularFormulaManipulator.getHTML(iformula);
					// compute molecular mass
					double emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
					emass = Double.valueOf(threeDForm.format(emass));	// shorten exact mass to 3 decimal places
					
					/**
		             *  hydrogen handling
		             */
		            try {
		                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
		                CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
		                hAdder.addImplicitHydrogens(container);
		                AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
	                	
		                //container = AtomContainerHandler.addExplicitHydrogens(container);
					} catch (CDKException e) {
						System.err.println("error manipulating mol for " + mfr.getCandidateID());
						continue;
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
						// TODO: properties einlesen und nutzen
						PubChemLocal pl = new PubChemLocal("jdbc:mysql://rdbms/MetFrag", "swolf", "populusromanus");
						names = pl.getNames(mfr.getCandidateID());
						if(names.size() > 0)
							name = names.get(0);	
					}
					else if(database.equals(dbCHEMSPIDER)) {
						int id = Integer.parseInt(mfr.getCandidateID());
						String token = "eeca1d0f-4c03-4d81-aa96-328cdccf171a";
						ExtendedCompoundInfo cpdInfo = chemSpiderProxy.getExtendedCompoundInfo(id, token);
						name = cpdInfo.getCommonName();
					}
					else {
						System.err.println("unknown database - or not yet supported!");
					}
					
					if(name == null || name.isEmpty())
						name = mfr.getCandidateID();
					
					// add reference link to MetFrag landing page for further information about each entry and its fragments
					String params = formatLandingURL(spectrumURLEncoded, database, mfr.getCandidateID(), exactMass, molecularFormula);
					String landingURL = landingPage + params;
					
					System.out.println("candidate ID -> " + mfr.getCandidateID());
					//results.add(new Result("MetFrag", mfr.getCandidateID(), name, mfr.getScore(), container, url, tempPath + filename, landingURL));
					results.add(new Result("MetFrag", mfr.getCandidateID(), name, mfr.getScore(), container, url, tempPath + filename,
							landingURL, formula, emass, mfr.getPeaksExplained()));
					//results.add(new Result("MetFrag", mfr.getCandidateID(), mfr.getCandidateID(), mfr.getScore(), container, url, ""));
					
					// write 
//					File f = new File(molPath, mfr.getCandidateID() + ".mol");
//					MassBankUtilities.writeContainer(f, container);
					
					// update search progress
					updateSearchProgress(current);
					current++;
				}
			}
			current = mfResults.size();
			updateSearchProgress(current);
			done = Boolean.TRUE;
			showResult = true;
		} catch (Exception e) {
			this.progress = 100;
			done = Boolean.TRUE;
			e.printStackTrace();
			showResult = false;
		}
	}
	
	/**
	 * Updates counter for progress bar.
	 */
	public void updateSearchProgress(int current) {
		int maximum = this.mfResults.size();
		int border = (limit >= maximum) ? maximum : limit;
		if(border == 0) {
			this.progress = 100;
			return;
		}
		
		float result = (((float) current / (float) border) * 100f);
		this.progress = Math.round(result);
		
		System.out.println("Called updateSearchProgress MetFragBean -> " + progress);
		// Ensure the new percent is within the valid 0-100 range
        if (progress < 0) {
        	progress = 0;
        }
        if (progress > 100) {
        	progress = 100;
        }
	}
	
	@Override
	public void run() {
		submit(null);
	}
	
	public void start() {
		t.start();
	}
	
	public void validateExactMass(FacesContext context, UIComponent validate, Object value){
	    double emass = (Double) value;

	    if(emass < 1.0 || emass > 10000.0){
	        ((UIInput)validate).setValid(false);
	        FacesMessage msg = new FacesMessage("Invalid mass. Please enter a value between 1.0 and 10000.0!");
	        context.addMessage(validate.getClientId(context), msg);
	    }
	}
	
	public void reset(ActionEvent event) {
		this.showResult = false;
		this.results = new ArrayList<Result>();
		this.results.clear();
	}
	
	public void setSelectedDB(String selectedDB) {
		this.selectedDB = selectedDB;
	}

	public String getSelectedDB() {
		return selectedDB;
	}

	public void setDatabases(SelectItem[] databases) {
		this.databases = databases;
	}

	public SelectItem[] getDatabases() {
		return databases;
	}

	public String getDatabaseID() {
		return databaseID;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public double getExactMass() {
		return exactMass;
	}

	public double getMzabs() {
		return mzabs;
	}

	public double getMzppm() {
		return mzppm;
	}

	public int getTreeDepth() {
		return treeDepth;
	}

	public boolean isProxy() {
		return proxy;
	}

	public boolean isRedundancyCheck() {
		return redundancyCheck;
	}

	public boolean isBreakAromaticRings() {
		return breakAromaticRings;
	}

	public boolean isHydrogenTest() {
		return hydrogenTest;
	}

	public boolean isNeutralLossLayer() {
		return neutralLossLayer;
	}

	public boolean isBondEnergyScoring() {
		return bondEnergyScoring;
	}

	public boolean isBreakOnlySelectedBonds() {
		return breakOnlySelectedBonds;
	}

	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public void setExactMass(double exactMass) {
		this.exactMass = exactMass;
	}

	public void setMzabs(double mzabs) {
		this.mzabs = mzabs;
	}

	public void setMzppm(double mzppm) {
		this.mzppm = mzppm;
	}

	public void setTreeDepth(int treeDepth) {
		this.treeDepth = treeDepth;
	}

	public void setProxy(boolean proxy) {
		this.proxy = proxy;
	}

	public void setRedundancyCheck(boolean redundancyCheck) {
		this.redundancyCheck = redundancyCheck;
	}

	public void setBreakAromaticRings(boolean breakAromaticRings) {
		this.breakAromaticRings = breakAromaticRings;
	}

	public void setHydrogenTest(boolean hydrogenTest) {
		this.hydrogenTest = hydrogenTest;
	}

	public void setNeutralLossLayer(boolean neutralLossLayer) {
		this.neutralLossLayer = neutralLossLayer;
	}

	public void setBondEnergyScoring(boolean bondEnergyScoring) {
		this.bondEnergyScoring = bondEnergyScoring;
	}

	public void setBreakOnlySelectedBonds(boolean breakOnlySelectedBonds) {
		this.breakOnlySelectedBonds = breakOnlySelectedBonds;
	}

	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}

	public boolean isShowResult() {
		return showResult;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public List<Result> getResults() {
		return results;
	}

	public Thread getT() {
		return t;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	public void setInputSpectrum(String inputSpectrum) {
		this.inputSpectrum = inputSpectrum;
	}

	public String getInputSpectrum() {
		return inputSpectrum;
	}

	public void setSearchppm(double searchppm) {
		this.searchppm = searchppm;
	}

	public double getSearchppm() {
		return searchppm;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	public String getSessionPath() {
		return sessionPath;
	}

	public Map<String, String> getLinkMap() {
		return linkMap;
	}

	public void setLinkMap(Map<String, String> linkMap) {
		this.linkMap = linkMap;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setAdductList(List<SelectItem> adductList) {
		this.adductList = adductList;
	}

	public List<SelectItem> getAdductList() {
		return adductList;
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

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}

	public void setMfResults(List<MetFragResult> mfResults) {
		this.mfResults = mfResults;
	}

	public List<MetFragResult> getMfResults() {
		return mfResults;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}

	public void setRenderSDF(boolean renderSDF) {
		this.renderSDF = renderSDF;
	}

	public boolean isRenderSDF() {
		return renderSDF;
	}

	public void setSelectedSDF(String selectedSDF) {
		this.selectedSDF = selectedSDF;
	}

	public String getSelectedSDF() {
		return selectedSDF;
	}

	public void setValidSDF(boolean validSDF) {
		this.validSDF = validSDF;
	}

	public boolean isValidSDF() {
		return validSDF;
	}

	public void setNoteSDF(String noteSDF) {
		this.noteSDF = noteSDF;
	}

	public String getNoteSDF() {
		return noteSDF;
	}

}
