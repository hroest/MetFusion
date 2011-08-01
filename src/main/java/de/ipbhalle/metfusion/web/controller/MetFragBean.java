/**
 * created by Michael Gerlich on May 21, 2010
 * last modified May 21, 2010 - 10:06:51 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.web.controller;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.application.FacesMessage;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.chemspider.www.ExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPISoapProxy;
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Highlight;

import de.ipbhalle.MassBank.MassBankLookupBean;
import de.ipbhalle.metfrag.chemspiderClient.ChemSpider;
import de.ipbhalle.metfrag.keggWebservice.KeggWebservice;
import de.ipbhalle.metfrag.main.MetFrag;
import de.ipbhalle.metfrag.main.MetFragResult;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;
import de.ipbhalle.metfrag.pubchem.PubChemWebService;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfrag.tools.renderer.StructureToFile;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
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
	 * define selected upstream DB - defaults to pubchem
	 */
	private String selectedDB = "pubchem";
	
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
	 * SelectItem options for upstream DB
	 */
	private SelectItem[] databases = {new SelectItem(dbKEGG, "KEGG"), new SelectItem(dbPUBCHEM, "PubChem"), new SelectItem(dbCHEMSPIDER, "ChemSpider")};
	
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
	 * TODO: add possibility to define a modified mass (e.g. [M+H]+, [M+Na]+, and so on)
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
	
	/**
	 * the separate thread container for this class, allows parallel execution
	 */
	private Thread t;
	
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
	
	private final String landingPage = "http://msbi.ipb-halle.de/MetFragBeta/LandingPage.jspx?";
	
	public MetFragBean() {
		t = new Thread(this, "metfrag");
		fillLinkMap();
		
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
	
	public void submit(ActionEvent event) {
//		String peakString = "119.051 467.616\n123.044 370.662\n147.044 6078.145\n"
//				+ "153.019 10000.0\n179.036 141.192\n189.058 176.358\n273.076 10000.000\n"
//				+ "274.083 318.003";
		//WrapperSpectrum spectrum = new WrapperSpectrum(peakString, 1, 272.06847);
		
		WrapperSpectrum spectrum = new WrapperSpectrum(inputSpectrum, mode, exactMass, true);
		//new WrapperSpectrum(inputSpectrum, mode, exactMass);
		
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
			List<MetFragResult> result = MetFrag.startConvenienceMetFusion(database, databaseID, 
					molecularFormula, exactMass, spectrum, useProxy, mzabs, mzppm, searchPPM, 
					molecularFormulaRedundancyCheck, breakAromaticRings, treeDepth, hydrogenTest,
					neutralLossInEveryLayer, bondEnergyScoring, breakOnlySelectedBonds, limit, jdbc, username, password);
			System.out.println("MetFrag result#: " + result.size() + "\n");
			
			this.results = new ArrayList<Result>();
			this.results.clear();
			
			MassSpecAPISoapProxy chemSpiderProxy = null;
			if(database.equals(dbCHEMSPIDER))
				chemSpiderProxy = new MassSpecAPISoapProxy();
			
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
//					try {
//						AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
//						CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
//				        hAdder.addImplicitHydrogens(container);
//				        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
//					} catch (CDKException e) {
//						System.err.println("error manipulating mol for " + mfr.getCandidateID());
//						continue;
//					}
					
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
						String token = "4d6c67db-65d0-474e-9f5c-f70f5c85111c";
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
					
					
					//results.add(new Result("MetFrag", mfr.getCandidateID(), name, mfr.getScore(), container, url, tempPath + filename, landingURL));
					results.add(new Result("MetFrag", mfr.getCandidateID(), name, mfr.getScore(), container, url, tempPath + filename,
							landingURL, formula, emass));
					//results.add(new Result("MetFrag", mfr.getCandidateID(), mfr.getCandidateID(), mfr.getScore(), container, url, ""));
					
					// write 
//					File f = new File(molPath, mfr.getCandidateID() + ".mol");
//					MassBankUtilities.writeContainer(f, container);
				}
			}
			showResult = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showResult = false;
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
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

}
