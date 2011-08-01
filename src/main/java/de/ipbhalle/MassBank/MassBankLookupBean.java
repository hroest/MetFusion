/**
 * created by Michael Gerlich on May 18, 2010
 * last modified May 18, 2010 - 12:41:12 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MassBank;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.print.attribute.standard.MediaSize.Other;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.tools.renderer.StructureToFile;
import de.ipbhalle.metfusion.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.metfusion.web.controller.MetFragBean;
import de.ipbhalle.metfusion.wrapper.Result;

import massbank.GetConfig;
import massbank.GetInstInfo;
import massbank.MassBankCommon;

//@ManamassBankLookupBeangedBean(eager=true)
@ManagedBean(name="databaseBean")
@SessionScoped
//@CustomScoped(value = "#{window}")
public class MassBankLookupBean implements Runnable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String serverUrl;
	private MassBankCommon mbCommon;
	private GetConfig config;
	private GetInstInfo instInfo;
	private Map<String, List<String>> instruments;
	
	private int limit = 100;
	private int cutoff = 5;
	
	private String[] selectedInstruments;
	private List<String[]> selectedGroupInstruments;
	private SelectItem[] insts;
	private List<SelectItemGroup> groupInstruments;
	private List<SelectItem[]> instTest;
	
	// boolean indicators to mimic selection of different instrument types
	// and combinations -> LC-ESI-... or only EI-...
	private boolean useEIOnly = false;
	private boolean useESIOnly = true;
	private boolean useOtherOnly = false;
	private boolean useLC = false;
	private boolean useGC = false;
	private static final String EI = "EI";
	private static final String ESI = "ESI";
	private static final String OTHER = "Others";
	private Map<String, List<String>> instGroups;
	
	private String selectedIon = "1";
	private SelectItem[] ionisations = {new SelectItem("1", "positive"), new SelectItem("-1", "negative"), new SelectItem("0", "both")};
	
	private static final String massbankJP = "http://www.massbank.jp/";
	
	private String inputSpectrum = "273.096 22\n289.086 107\n290.118 14\n291.096 999\n292.113 162\n293.054 34\n579.169 37\n580.179 15";
	
	/** MassBank parameter qmz which denotes the mz values from the inputSpectrum that should be highlighted inside a MassBank record.	 */
	private String qmz;
	
	private List<String> queryResults;
	private boolean showResult;
	private List<Result> results;
	private List<String> originalResults;
	
	private List<Result> unused;
	
	private Thread t;
	private static final String cacheMassBank = "/vol/massbank/Cache/";
	
//	private FacesContext fc = FacesContext.getCurrentInstance();
//    private HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
//    private String sessionString = session.getId();
//    private ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
    private final String sep = System.getProperty("file.separator");
//    private String webRoot = scontext.getRealPath(sep);
    
    private String sessionPath;
    
    private boolean brokenMassBank = false;
    
    /** EI, ESI, Other */
    public static final int NUM_INST_GROUPS = 3;
    
    
    /**
     * nur für Auswertung gedacht!
     */
    private String currentRecord = "";
    
	public MassBankLookupBean() {
		//this(massbankJP);
		this("http://msbi.ipb-halle.de/MassBank/");
		t = new Thread(this, "massbank");
		
		//FacesContext fc = FacesContext.getCurrentInstance();
//		ELResolver el = fc.getApplication().getELResolver();
//        ELContext elc = fc.getELContext();
        //HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
		//String sessionString = session.getId();
		//System.out.println("MassBankLookupBean sessionID -> " + sessionString);
	}
	
	public MassBankLookupBean(String serverUrl) {
            this.serverUrl = (serverUrl.isEmpty() | !serverUrl.startsWith("http://") ? massbankJP : serverUrl);
            this.setMbCommon(new MassBankCommon());
            this.setConfig(new GetConfig(this.serverUrl));
            this.setInstInfo(new GetInstInfo(this.serverUrl));
            this.setInstruments(this.instInfo.getTypeGroup());
            showResult = false;
            System.out.println("serverUrl: " + this.serverUrl);
            
            Map<String, List<String>> instGroup = instInfo.getTypeGroup();
            // store instrument groups with group identifier
            this.instGroups = instGroup;
            instTest = new ArrayList<SelectItem[]>();
            
            Iterator<String> it = instGroup.keySet().iterator();
            int counter = 0;
            StringBuilder sb = new StringBuilder();
            //SelectItemGroup[] sig = new SelectItemGroup[instGroup.keySet().size()];
            List<SelectItemGroup> sig = new ArrayList<SelectItemGroup>();
            this.selectedGroupInstruments = new ArrayList<String[]>();
            
            // iterate over instrument groups
            while(it.hasNext()) {
	        	String next = it.next();
	        	//sig[counter] = new SelectItemGroup(next);
	        	SelectItemGroup sigcur = new SelectItemGroup(next);
	        	//System.out.println("next -> " + next);
	        	List<String> items = instGroup.get(next);	// retrieve instruments from current instrument group
	        	String[] instruments = new String[items.size()];
	        	
	        	SelectItem[] si = new SelectItem[items.size()];
	        	for (int i = 0; i < si.length; i++) {
	        		String s = items.get(i);
					si[i] = new SelectItem(s, s);
					
					//System.out.println(s);
					sb.append(s).append(",");
					
					instruments[i] = "";	// add instrument to list for corresponding group
				}
	        	instTest.add(si);
	        	selectedGroupInstruments.add(instruments);
	        	
	        	//sig[counter].setSelectItems(si);
	        	sigcur.setSelectItems(si);
	        	//if(next.equals(ESI))
	        	//	sigcur.setDisabled(false);
	        	//else sigcur.setDisabled(true);
	        	
	        	sig.add(sigcur);
	        	//System.out.println();
	        	counter++;
	        	this.instruments.put(next, items);
            }
            
            String temp = sb.toString();
            if(temp.endsWith(","))
                    temp = temp.substring(0, temp.length());
            String[] split = sb.toString().split(",");
            this.insts = new SelectItem[sb.toString().split(",").length];
            this.selectedInstruments = new String[split.length];	//split;
            for (int i = 0; i < this.insts.length; i++) {
           		this.insts[i] = new SelectItem(split[i], split[i]);
            	
                // let only be ESI instruments be preselected
                if(split[i].contains(ESI))
                	this.selectedInstruments[i] = split[i];
            }

            // check MassBank availability - check if all instrument groups are present - EI, ESI, Other
            if(instGroup.keySet().size() < NUM_INST_GROUPS)
            	this.brokenMassBank = true;
            
            this.groupInstruments = sig;

            t = new Thread(this, "massbank");
	}

	public void changeInstruments(ValueChangeEvent event) {
		String[] newInstruments = (String[]) event.getNewValue();
		for (int i = 0; i < newInstruments.length; i++) {
			System.out.println(newInstruments[i]);
		}
		this.selectedInstruments = newInstruments;
	}
	
	public void collectInstruments() {
		String[] current = getSelectedInstruments();
		for (int i = 0; i < current.length; i++) {
			System.out.println("current -> " + current[i]);
		}
		
		List<String[]> currentSelected = getSelectedGroupInstruments();
		for (int i = 0; i < currentSelected.size(); i++) {
			String[] temp = currentSelected.get(i);
			for (int j = 0; j < temp.length; j++) {
				System.out.println("currentSelected -> " + temp[j]);
			}
		}
	}
	
	public void toggleInstrumentsEI(ValueChangeEvent event) {
		boolean newVal = (Boolean) event.getNewValue();
		List<String> instruments = instGroups.get(EI);
		String[] newInstruments = new String[instruments.size()];
		
		if(newVal) {	// let only be EI instruments be preselected
			for (int i = 0; i < newInstruments.length; i++) {
				newInstruments[i] = instruments.get(i);
			}
		}
		else {	// let only be EI instruments be deselected
			for (int i = 0; i < newInstruments.length; i++) {
				newInstruments[i] = "";
			}
		}
		selectedGroupInstruments.set(0, newInstruments);
	}
	
	public void toggleInstrumentsESI(ValueChangeEvent event) {
		boolean newVal = (Boolean) event.getNewValue();
		List<String> instruments = instGroups.get(ESI);
		String[] newInstruments = new String[instruments.size()];
		
		if(newVal) {	// let only be EI instruments be preselected
			for (int i = 0; i < newInstruments.length; i++) {
				newInstruments[i] = instruments.get(i);
			}
		}
		else {	// let only be EI instruments be deselected
			for (int i = 0; i < newInstruments.length; i++) {
				newInstruments[i] = "";
			}
		}
		selectedGroupInstruments.set(1, newInstruments);
		collectInstruments();
	}

	public void toggleInstrumentsOther(ValueChangeEvent event) {
		boolean newVal = (Boolean) event.getNewValue();
		List<String> instruments = instGroups.get(OTHER);
		String[] newInstruments = new String[instruments.size()];
		
		if(newVal) {	// let only be EI instruments be preselected
			for (int i = 0; i < newInstruments.length; i++) {
				newInstruments[i] = instruments.get(i);
			}
		}
		else {	// let only be EI instruments be deselected
			for (int i = 0; i < newInstruments.length; i++) {
				newInstruments[i] = "";
			}
		}
		selectedGroupInstruments.set(2, newInstruments);
	}

	public void changeInstrumentGroups(ValueChangeEvent event) {
		String group = (String) event.getComponent().getAttributes().get("EI");
		System.out.println("group -> " + group);
	}
	
	@Override
	public void run() {
            submit(null);
	}
	
	public void start() {
            t.start();
	}
	
	public void submit(ActionEvent event) {
//		FacesContext fc = FacesContext.getCurrentInstance();
//        ELResolver el = fc.getApplication().getELResolver();
//        ELContext elc = fc.getELContext();
//        ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
//        System.out.println(sc.getContextPath());
        
        /**
         * 2 Wege um an einen Bean zu kommen, beide funktionieren !!!
         */
        //MetFragBean mfb = (MetFragBean) el.getValue(elc, null, "metFragBean");
        //MetFragBean obj = (MetFragBean) elc.getELResolver().getValue(elc, null, "metFragBean");
        /**
         * 
         */
        
        String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
        StringBuilder sb = new StringBuilder();
        sb.append("&INST=");
        if(selectedInstruments != null && selectedInstruments.length > 0) {	// use chosen instruments
        	for (int i = 0; i < selectedInstruments.length; i++) {
    			sb.append(selectedInstruments[i]).append(",");
    		}
        }
        else {	// use all available instruments if none selected as none is prohibited
        	for (int i = 0; i < insts.length; i++) {
				sb.append(insts[i].getLabel()).append(",");
			}
        }
        
        String inst = sb.toString();
        if(inst.endsWith(","))		// remove trailing comma
        	inst = inst.substring(0, inst.length() - 1);
        
        // set ionization mode to positive if none selected
        if(selectedIon == null || selectedIon.isEmpty() || selectedIon.length() == 0)
        	selectedIon = (String) ionisations[0].getValue();
        
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
		
		// only provide non-Hill records to result set
		this.queryResults = new ArrayList<String>();
		/**
		 * konservativer Ansatz für Auswertung -> richtige compounds rauslassen
		 */
//		String prefix = getCurrentRecord();
//		prefix = prefix.substring(0, 2);	// ersten zwei zeichen als präfix nutzen um zu filtern
		for (int i = 0; i < result.size(); i++) {
			/**
			 * not needed if all spectra are added
			 */
//			String line = result.get(i);
//			String[] split = line.split("\t");
			
			/**
			 * skip HILL spectra for evaluation
			 */
//			if(!split[1].trim().startsWith("CO")) {	//&& !split[0].substring(0, split[0].indexOf(";")).equals("Salbutamol")
//				this.queryResults.add(result.get(i));
//			}
			
			/**
			 * add all spectra for evaluation to show correct working on complete database 
			 */
			this.queryResults.add(result.get(i));
			
			/**
			 * skip RIKEN spectra for evaluation - result set might get empty!
			 */
//			if(!split[1].trim().startsWith("PR10"))	//  && !(result.size() == 1)
//				this.queryResults.add(result.get(i));
			
//			}
			
			/**
			 * konservativer Ansatz für Auswertung -> richtige compounds rauslassen
			 */
//			if(!split[1].trim().startsWith(prefix))
//				this.queryResults.add(result.get(i));
			
		}
		
		// if there are no entries after filtering, add all filtered entries back
		/**
		 * TODO: handle empty result set
		 */
		if(queryResults.size() == 0)
			return;
//		if(queryResults.size() == 0) {
//			System.err.println("MassBank filtering resulted in 0 (zero) entries - adding all original entries\n");
//			queryResults.addAll(result);
//		}
		/**
		 * 
		 */
		
		this.originalResults = result;
		this.unused = new ArrayList<Result>();
		
		//this.queryResults = result;
		this.showResult = true;
		System.out.println(result.size() + "\n");
		//for (String s : result) {
		//	System.out.println(s);
		//}
		
		wrapResults();
	}
	
	public void instrumentListener(ValueChangeEvent event) {
		System.out.println("old -> " + event.getOldValue());
		System.out.println("new -> " + event.getNewValue());
		if(event.getNewValue() instanceof String[]) {
			System.out.println("new value == string[]");
			String[] newVals = (String[]) event.getNewValue();
			int counter = 0;
			this.selectedInstruments = new String[this.insts.length];
			for (int i = 0; i < newVals.length; i++) {
				System.out.println(newVals[i]);
				if(newVals[i].contains(EI)) {
					System.out.println("newVals contains EI");
					List<String> insts = instGroups.get(EI);
					System.out.println("#insts -> " + insts.size());
					for (String inst : insts) {
						this.selectedInstruments[counter] = inst;
						counter++;
					}
				}
				if(newVals[i].contains(ESI)) {
					System.out.println("newVals contains ESI");
					List<String> insts = instGroups.get(ESI);
					System.out.println("#insts -> " + insts.size());
					for (String inst : insts) {
						this.selectedInstruments[counter] = inst;
						counter++;
					}
				}
				if(newVals[i].contains(OTHER)) {
					System.out.println("newVals contains Other");
					List<String> insts = instGroups.get(OTHER);
					System.out.println("#insts -> " + insts.size());
					for (String inst : insts) {
						this.selectedInstruments[counter] = inst;
						counter++;
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param param - a fully functional and qualified MassBank QuickSearch parameter string
	 */
	public void runQuery(String param) {
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
		// retrieve result list
		ArrayList<String> result = mbCommon.execMultiDispatcher(serverUrl, typeName, param);
		this.originalResults = result;
	}
	
	private void wrapResults() {
//		FacesContext fc = FacesContext.getCurrentInstance();
//	    HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
//	    String sessionString = session.getId();
//	    ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
//	    String webRoot = scontext.getRealPath(sep);
	    
		//String currentFolder = "";	//webRoot + sep + "temp" + sep + sessionString + sep;
		String relImagePath = getSessionPath(); 	//sep + "temp" + sep + sessionString + sep;
		System.out.println("relImagePath -> " + relImagePath);
		String tempPath = relImagePath.substring(relImagePath.indexOf("/temp"));
		if(!tempPath.endsWith("/"))
			tempPath += "/";
		
		/**
		 * TODO: auskommentiert für Gridengine Evaluationsläufe
		 */
//	    new File(relImagePath).mkdirs();
//		StructureToFile stf = null;
//		try {
//			stf = new StructureToFile(200, 200, relImagePath, false, false);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
            List<Result> results = new ArrayList<Result>();
            List<String> duplicates = new ArrayList<String>();

            String name = "";
            String id = "";
            double score = 0.0d;
            String site = "";

            int limitCounter = 0;
            //for (String s : queryResults) {
            for(int i = 0; i < queryResults.size(); i++) {
                String s = queryResults.get(i);
//			int idx = results.indexOf(s);

                /**
                 *  create results only till the given limit
                 */
                if(limitCounter == limit)
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
                    score = Double.parseDouble(split[4].substring(split[4].indexOf(".")));
                    site = split[5];

                    String url = getServerUrl();
                    if(!url.contains("Dispatcher.jsp") && url.endsWith("/"))
                    	url += "jsp/Dispatcher.jsp?type=disp&id=" + id + "&site=" + site + "&qmz=" + getQmz() + "&CUTOFF=5";
                    
                    //String record = MassBankUtilities.retrieveRecord(id, site);
                    MassBankUtilities.fetchRecord(id, site);
                    //String mol = MassBankUtilities.retrieveMol(name, site, id);

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
                            //System.out.println("basePath for " + id + " -> " + basePath);
                            break;
                        }
                    }
                    //boolean fetch = MassBankUtilities.fetchMol(name, id, site, basePath);
                    boolean fetch = false;
                    //boolean write = MassBankUtilities.writeMolFile(id, mol, basePath);

                    // create AtomContainer via SMILES
                    Map<String, String> links = MassBankUtilities.retrieveLinks(id, site);
                    String smiles = links.get("smiles");
                    //System.out.println("smiles -> " + smiles);
                    IAtomContainer container = null;
                    // first look if container is present, then download if not
                    container = MassBankUtilities.getContainer(id, basePath);
                    if(container == null) {
                        fetch = MassBankUtilities.fetchMol(name, id, site, basePath);
                        if(fetch) {
                            System.out.println("container via fetch");
                            //container = MassBankUtilities.getMolFromAny(id, basePath, smiles);
                            container = MassBankUtilities.getContainer(id, basePath);
                        }
                        else {
                            System.out.println("container via smiles");
                            container = MassBankUtilities.getMolFromSmiles(smiles);

                            if(container != null) {
                                // write out molfile
                                File mol = new File(basePath, id + ".mol");
                                MassBankUtilities.writeContainer(mol, container);
                            }
                        }
                    }

                    // hydrogen handling
                    if(container != null) {
                        try {
                            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
                            CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
                            hAdder.addImplicitHydrogens(container);
                            AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
                        } catch (CDKException e) {
                            System.err.println("error manipulating mol for " + id);
                            continue;
                        }
                    }

//				if(container == null && !fetch && smiles != null && !smiles.isEmpty()) {
//					if(fetch) {
//						System.out.println("container via fetch");
//						//container = MassBankUtilities.getMolFromAny(id, basePath, smiles);
//						container = MassBankUtilities.getContainer(id, basePath);
//					}
//					else {
//						System.out.println("container via smiles");
//						container = MassBankUtilities.getMolFromSmiles(smiles);
//					}
//				}

//				IAtomContainer container = MassBankUtilities.getContainer(id, basePath);
//				IAtomContainer container = MassBankUtilities.getContainer(mol);
//				results.add(new Result("MassBank", id, name, score, container));

                    /**
                     *  if entry is not present yet, add it - else don't
                     */
                    if(container != null) {	// removed duplicate check -> !duplicates.contains(name) &&
                    	
                    	// compute molecular formula
    					IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(container);
    					String formula = MolecularFormulaManipulator.getHTML(iformula);
    					// compute molecular mass
    					double emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
                    	
                    	/**
                    	 * TODO: auskommentiert für Gridengine Evaluationsläufe
                    	 */
//                    	if(stf != null)
//							try {
//								stf.writeMOL2PNGFile(container, id + ".png");
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
                        duplicates.add(name);
                        //results.add(new Result("MassBank", id, name, score, container, url, relImagePath + id + ".png"));
                        results.add(new Result("MassBank", id, name, score, container, url, tempPath + id + ".png", formula, emass));
                        limitCounter++;
                    }

                    // add unused results (duplicate or no mol container) to list
                    if(!fetch && container == null) {
                        //unused.add(new Result("MassBank", id, name, score, container, url, relImagePath + id + ".png"));
                    	unused.add(new Result("MassBank", id, name, score, container, url, tempPath + id + ".png"));
                    }
                }
                else if(split.length == 7) {
                        System.err.println("length == 7");
                }
            }
            System.out.println("entries after duplicate removal -> " + results.size());
            this.results = results;
	}
	
	private String formatPeaks() {
		StringBuilder peaklist =  new StringBuilder();
		String temp = inputSpectrum.trim();
		StringBuilder qmz = new StringBuilder();	// builder for mz values only
		
		// assume that validation took place -> one peak per line, mz space int
		String[] split = temp.split("\n");
		for (int i = 0; i < split.length; i++) {
			String[] line = split[i].split("\\s");
			
			String mz = "";
			String inte = "";
			
			if(line.length == 0) {
				System.err.println("Error parsing peaklist!");
				continue;
			}
			else if(line.length == 1) {
				// assume that only mz values are given
				mz = line[0];
				inte = "100";
			}
			else if(line.length == 2) {
				mz = line[0];
				inte = line[1];
			}
			else if(line.length == 3) {
				// assume that first mz, then rel.int, then int
				mz = line[0];
				inte = line[2];
			}
			else {
				// assume that first mz, then rel.int, then int
				mz = line[0];
				inte = line[2];
			}
			
			if(i == (split.length - 1))
				peaklist.append(mz).append(",").append(inte);	
			else
				peaklist.append(mz).append(",").append(inte).append("@");
			
			qmz.append(mz).append(",");
		}
		
		// set qmz field
		setQmz(qmz.toString());
		
		return peaklist.toString();
	}
	
	public void reset(ActionEvent event) {
		this.showResult = false;
		this.queryResults = new ArrayList<String>();
		
		FacesContext fc = FacesContext.getCurrentInstance();
        ELResolver el = fc.getApplication().getELResolver();
        ELContext elc = fc.getELContext();
        MetFragBean mfb = (MetFragBean) el.getValue(elc, null, "metFragBean");
        mfb.reset(null);
	}
	
	public String getServerUrl() {
		return serverUrl;
	}


	public Map<String, List<String>> getInstruments() {
		return instruments;
	}


	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}


	public void setInstruments(Map<String, List<String>> instruments) {
		this.instruments = instruments;
	}


	public void setMbCommon(MassBankCommon mbCommon) {
		this.mbCommon = mbCommon;
	}


	public MassBankCommon getMbCommon() {
		return mbCommon;
	}


	public void setConfig(GetConfig config) {
		this.config = config;
	}


	public GetConfig getConfig() {
		return config;
	}


	public GetInstInfo getInstInfo() {
		return instInfo;
	}


	public void setInstInfo(GetInstInfo instInfo) {
		this.instInfo = instInfo;
	}

	public void setInputSpectrum(String inputSpectrum) {
		this.inputSpectrum = inputSpectrum.trim();
	}

	public String getInputSpectrum() {
		return inputSpectrum.trim();
	}

	public void setInsts(SelectItem[] insts) {
		this.insts = insts;
	}

	public SelectItem[] getInsts() {
		return insts;
	}

	public void setSelectedInstruments(String[] selectedInstruments) {
		this.selectedInstruments = selectedInstruments;
	}

	public String[] getSelectedInstruments() {
		return selectedInstruments;
	}

	public void setGroupInstruments(List<SelectItemGroup> groupInstruments) {
		this.groupInstruments = groupInstruments;
	}

	public List<SelectItemGroup> getGroupInstruments() {
		return groupInstruments;
	}

	public void setIonisations(SelectItem[] ionisations) {
		this.ionisations = ionisations;
	}

	public SelectItem[] getIonisations() {
		return ionisations;
	}

	public void setSelectedIon(String selectedIon) {
		this.selectedIon = selectedIon;
	}

	public String getSelectedIon() {
		return selectedIon;
	}

	public List<String> getQueryResults() {
		return queryResults;
	}

	public void setQueryResults(List<String> queryResults) {
		this.queryResults = queryResults;
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

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setOriginalResults(List<String> originalResults) {
		this.originalResults = originalResults;
	}

	public List<String> getOriginalResults() {
		return originalResults;
	}

	public void setUnused(List<Result> unused) {
		this.unused = unused;
	}

	public List<Result> getUnused() {
		return unused;
	}

	public void setQmz(String qmz) {
		this.qmz = qmz;
	}

	public String getQmz() {
		return qmz;
	}

	public void setSessionPath(String sessionPath) {
		this.sessionPath = sessionPath;
	}

	public String getSessionPath() {
		return sessionPath;
	}

	public boolean isUseEIOnly() {
		return useEIOnly;
	}

	public void setUseEIOnly(boolean useEIOnly) {
		this.useEIOnly = useEIOnly;
	}

	public boolean isUseESIOnly() {
		return useESIOnly;
	}

	public void setUseESIOnly(boolean useESIOnly) {
		this.useESIOnly = useESIOnly;
	}

	public boolean isUseOtherOnly() {
		return useOtherOnly;
	}

	public void setUseOtherOnly(boolean useOtherOnly) {
		this.useOtherOnly = useOtherOnly;
	}

	public boolean isUseLC() {
		return useLC;
	}

	public void setUseLC(boolean useLC) {
		this.useLC = useLC;
	}

	public boolean isUseGC() {
		return useGC;
	}

	public void setUseGC(boolean useGC) {
		this.useGC = useGC;
	}

	public void setInstGroups(Map<String, List<String>> instGroups) {
		this.instGroups = instGroups;
	}

	public Map<String, List<String>> getInstGroups() {
		return instGroups;
	}

	public void setCurrentRecord(String currentRecord) {
		this.currentRecord = currentRecord;
	}

	public String getCurrentRecord() {
		return currentRecord;
	}

	public void setCutoff(int cutoff) {
		this.cutoff = cutoff;
	}

	public int getCutoff() {
		return cutoff;
	}

	public void setBrokenMassBank(boolean brokenMassBank) {
		this.brokenMassBank = brokenMassBank;
	}

	public boolean isBrokenMassBank() {
		return brokenMassBank;
	}

	public void setInstTest(List<SelectItem[]> instTest) {
		this.instTest = instTest;
	}

	public List<SelectItem[]> getInstTest() {
		return instTest;
	}

	public void setSelectedGroupInstruments(List<String[]> selectedGroupInstruments) {
		this.selectedGroupInstruments = selectedGroupInstruments;
	}

	public List<String[]> getSelectedGroupInstruments() {
		return selectedGroupInstruments;
	}

}
