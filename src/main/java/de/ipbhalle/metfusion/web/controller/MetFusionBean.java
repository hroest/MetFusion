/**
 * created by Michael Gerlich on May 21, 2010
 * last modified May 21, 2010 - 4:07:10 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.web.controller;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.math.linear.RealMatrix;
import org.icefaces.application.PortableRenderer;
import org.icefaces.application.PushRenderer;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.icesoft.faces.component.outputresource.OutputResource;
import com.icesoft.faces.context.Resource;
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Highlight;

import de.ipbhalle.MassBank.MassBankLookupBean;
import de.ipbhalle.metfrag.tools.renderer.StructureToFile;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegration;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;
import de.ipbhalle.metfusion.threading.ColoredMatrixGeneratorThread;
import de.ipbhalle.metfusion.threading.ImageGeneratorThread;
import de.ipbhalle.metfusion.wrapper.ColorcodedMatrix;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.wrapper.XLSOutputHandler;
import de.ipbhalle.metfusion.wrapper.XLSResource;


//@ManagedBean(name = "appBean", eager = true)
//@ApplicationScoped
@ManagedBean(name="appBean")
//@CustomScoped(value = "#{window}")
@SessionScoped
public class MetFusionBean implements Serializable {
	
	/**
	 * TODO: kombiniere Felder von MassBank und MetFrag Bean hierein, füge einzelne start methode hinzu
	 * die beide abfragen parallel via threads abwickelt und dann die ergebnisslisten präsentiert
	 * 
	 * später parallelisierung der matrixoperation falls möglich...
	 */
	
	private Highlight effectOutputText = new Highlight("#FFA500");
	
	/** default location for storing MetFrag structure images */
	private static final String DEFAULT_IMAGE_CACHE = "/vol/metfrag/images/";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * bean for MassBank lookup and result retrieval
	 */
	private MassBankLookupBean mblb;
	
	/**
	 * bean for MetFrag query and result retrieval
	 */
	private MetFragBean mfb;
	
	/**
	 * result list of threshold approach
	 */
	private List<ResultExt> newOrder;
	
	/**
	 * result list of weighted approach
	 */
	private List<ResultExt> secondOrder;
	
	
	private List<ResultExtGroupBean> tanimotoClusters;
	
	/**
	 * color coded matrix of Tanimoto values for MassBank vs. MetFrag
	 */
	private ColorcodedMatrix colorMatrix;
	
	private ColorcodedMatrix colorMatrixAfter;
	/**
	 * boolean value to indicate whether to show or hide result tables
	 */
	private boolean showTable;
	
	private boolean showResultTable;
	private boolean showResultsDatabase;
	private boolean showResultsFragmenter;
	private boolean showClusterResults;
	
	private String selectedTab = "0";
	private String selectedMatrixPanel = "origSimMatrix";
	
	/**
	 * peaklist of input spectrum, containing pairwise mz and intensity values
	 */
	private String inputSpectrum = "119.051 46\n123.044 37\n147.044 607\n153.019 999\n179.036 14\n189.058 17\n273.076 999\n274.083 31"; 
		//"273.096 22\n289.086 107\n290.118 14\n291.096 999\n292.113 162\n293.054 34\n579.169 37\n580.179 15";
	
	/**
	 * boolean value to indicate whether to use clustering or not
	 */
	private boolean useClustering = true;
	
	private String selectedResult = "cluster";	// allows switching of panels in panelStacking of ICEFaces
	// other values are "fragmenter", "database", "list"
	
	// chart variables
	private List<Color> chartColors;
	private List<double[]> chartValues;	// double[] for each pair value
	
	private final int numThreads = 4;

	private HttpSession session;
	private String sessionString;	// = session.getId();
	private ServletContext scontext;
	private final String sep = System.getProperty("file.separator");
	private String webRoot;
	
	private String errorMessage = "";
	
	/** output resource for all workflow results, will be stored in xls file */
    private Resource outputResource;
    private XLSOutputHandler exporter;
    private boolean createdResource = false;
    

    public MetFusionBean() {
		setMblb(new MassBankLookupBean());
		setMfb(new MetFragBean());
		
		chartColors = new ArrayList<Color>();
		chartColors.add(this.decode("#FF0000"));
		chartColors.add(this.decode("#00FF00"));
		chartColors.add(this.decode("#0000FF"));
		
		chartValues = new ArrayList<double[]>();
		
		FacesContext fc = FacesContext.getCurrentInstance();
//		ELResolver el = fc.getApplication().getELResolver();
//        ELContext elc = fc.getELContext();
		session = (HttpSession) fc.getExternalContext().getSession(false);
		this.sessionString = session.getId();
		System.out.println("MetFusionBean sessionID -> " + sessionString);
		
		this.mfb.setSessionID(sessionString);
//		MassBankLookupBean mblb = (MassBankLookupBean) el.getValue(elc, null, "massBankLookupBean");
//		MetFragBean mfb = (MetFragBean) el.getValue(elc, null, "metFragBean");
//		setMblb(mblb);
//		setMfb(mfb);
	}
	
    // ActionEvent event
	public String runBoth() throws InterruptedException {
		String navigate = "error";
		
		long time1 = System.currentTimeMillis();
		mblb.collectInstruments();
		System.out.println("clustering -> " + useClustering);
		System.out.println("inputSpectrum -> ");
		System.out.println(inputSpectrum);
		
		// hide result tables
		setShowTable(false);
		mfb.setShowResult(false);
		mfb.setShowResult(false);
		
		// set peaklist
		mblb.setInputSpectrum(inputSpectrum);
		mfb.setInputSpectrum(inputSpectrum);
		
		FacesContext fc = FacesContext.getCurrentInstance();
		// set context environment
		session = (HttpSession) fc.getExternalContext().getSession(false);
		scontext = (ServletContext) fc.getExternalContext().getContext();
		webRoot = scontext.getRealPath(sep);
		//String sessionString = session.getId();
		String sessionPath = webRoot + sep + "temp" + sep + sessionString + sep;
		System.out.println("tempPath -> " + sessionPath);
		System.out.println("sessionID -> " + sessionString);
		mfb.setSessionPath(sessionPath);
		mblb.setSessionPath(sessionPath);
		System.out.println("Massbank tempPath -> " + mblb.getSessionPath() + "\tMetFrag tempPath -> " + mfb.getSessionPath());
		String tempDir = sep + "temp" + sep + sessionString + sep;
		
        //ELResolver el = fc.getApplication().getELResolver();
        //ELContext elc = fc.getELContext();
        ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
        System.out.println("Servlet context path -> " + sc.getContextPath());
        
        int mode = Integer.parseInt(mblb.getSelectedIon());
		if(mode == 0) // MassBank uses "both" ionizations
			mode = 1;	// switch to "positive" mode
        mfb.setMode(mode);
        
        System.out.println("runBoth started!!!");
        String[] insts = mblb.getSelectedInstruments();
        // check if instruments were selected
        if(insts == null || insts.length == 0) {
        	String errMessage = "Error - no instruments were selected!";
            System.err.println(errMessage);
            FacesMessage currentMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, errMessage, errMessage);
            fc.addMessage("inputForm:errMsgInst", currentMessage);
            
            setShowResultsDatabase(false);
            setShowTable(true);
            //return;
            return navigate;
        }
        for (int i = 0; i < insts.length; i++) {
			System.out.print(insts[i] + "  ");
		}
        System.out.println();
        
//		MassBankLookupBean mblb = (MassBankLookupBean) el.getValue(elc, null, "massBankLookupBean");
//		MetFragBean mfb = (MetFragBean) el.getValue(elc, null, "metFragBean");
		
		// start both threads in parallel
        ExecutorService threadExecutor = null;
        threadExecutor = Executors.newFixedThreadPool(numThreads);
        threadExecutor.execute(mblb);
        threadExecutor.execute(mfb);
        threadExecutor.shutdown();
        
        do {
        	Thread.sleep(1000);
        }while(!threadExecutor.isTerminated());
        
        // start both threads
//		mblb.start();
//		mfb.start();
		
		System.out.println("massbank thread is alive -> " + mblb.getT().isAlive());
		System.out.println("metfrag thread is alive -> " + mfb.getT().isAlive());
		
		// wait for threads to finish
//		try {
//			System.out.println("Waiting for threads to finish.");
//			mblb.getT().join();
//			mfb.getT().join();
//		} catch (InterruptedException e) {
//			System.out.println("Main thread Interrupted");
//		}
		
		System.out.println("massbank thread is alive -> " + mblb.getT().isAlive());
		System.out.println("metfrag thread is alive -> " + mfb.getT().isAlive());
		System.out.println("runBoth finished!!!");

        if(mblb.getResults() == null || mblb.getResults().size() == 0) {
        	//String errMessage = "EMPTY MassBank result! - Check settings.";
        	String errMessage = "Peak(s) not found in MassBank - check the settings and try again.";
        	this.errorMessage = errMessage;
        	
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
            fc.addMessage("form:command", curentMessage);
            
            setShowResultsDatabase(false);
            setShowClusterResults(false);
            setShowTable(true);
            setSelectedTab("1");	// output and matrix tab are not displayed, so error tab is next
            //return;
            return navigate;
        }
        else if(mblb.getResults() != null) {
            System.out.println("# MassBank results: " + mblb.getResults().size());
            setShowResultsDatabase(true);
        }
        else {      // abort run and return
            //String errMessage = "EMPTY MassBank result! - Check settings.";
        	String errMessage = "Peak(s) not found in MassBank - check the settings and try again.";
            this.errorMessage = errMessage;
            
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
            fc.addMessage("form:command", curentMessage);
            
            setShowResultsDatabase(false);
            setShowClusterResults(false);
            setShowTable(true);
            setSelectedTab("1");	// output and matrix tab are not displayed, so error tab is next
            //return;
            return navigate;
        }
                
        if(mfb.getResults() == null || mfb.getResults().size() == 0) {
        	String errMessage = "EMPTY MetFrag result! - Check settings.";
        	this.errorMessage = errMessage;
        	
            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
            fc.addMessage("form:command", curentMessage);
            
            setShowResultsFragmenter(false);
            setShowClusterResults(false);
            setShowTable(true);
            setSelectedTab("2");	// matrix tab are not displayed, so error tab is next
            //return;
            return navigate;
        }
        else if(mfb.getResults() != null) {
        	System.out.println("# MetFrag results: " + mfb.getResults().size());
        	setShowResultsFragmenter(true);
        }
        else {      // abort run and return
            String errMessage = "EMPTY MetFrag result! - Check settings.";
            this.errorMessage = errMessage;

            System.err.println(errMessage);
            FacesMessage curentMessage = new FacesMessage(errMessage, errMessage);
            curentMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            //Mark as ERROR
            fc.addMessage("form:command", curentMessage);
            
            setShowResultsFragmenter(false);
            setShowClusterResults(false);
            setShowTable(true);
            setSelectedTab("2");	// matrix tab are not displayed, so error tab is next
            //return;
            return navigate;
        }
                
        
		// create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = mblb.getResults();
		List<Result> listMetFrag = mfb.getResults();
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag);	//, 3, 0.5f);
		
		// fork new thread for generating ColorCodedMatrix
		ColoredMatrixGeneratorThread cmT = new ColoredMatrixGeneratorThread(sim);
		
		/**
		 * disabled threshold approach
		 */
		//TanimotoIntegration integration = new TanimotoIntegration(sim);
		//setNewOrder(integration.computeNewOrdering());
		
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
		//setSecondOrder(tiw.computeNewOrdering());
		
		// fork new thread for generating compound images
		ImageGeneratorThread igT = new ImageGeneratorThread(listMetFrag, sessionPath, tempDir);
		ImageGeneratorThread igT2 = new ImageGeneratorThread(listMassBank, sessionPath, tempDir);
//		igT.start();
//		igT.join();
		
		threadExecutor = Executors.newFixedThreadPool(numThreads);
        threadExecutor.execute(tiw);
        threadExecutor.execute(cmT);
        threadExecutor.execute(igT);
        threadExecutor.execute(igT2);
        threadExecutor.shutdown();
        
        do {
        	Thread.sleep(400);
        }while(!threadExecutor.isTerminated());
        
		//integration.start();
//		tiw.start();
//		cmT.start();
//		// wait for threads to finish
//		try {
//			System.out.println("Waiting for threads to finish.");
//			//integration.getThread().join();
//			tiw.getThread().join();
//			cmT.join();
//		} catch (InterruptedException e) {
//			System.out.println("Main thread Interrupted - using empty lists for output!");
//			setNewOrder(new ArrayList<ResultExt>());
//			setSecondOrder(new ArrayList<ResultExt>());
//			// TODO: add JSF error message
//			return;
//		}
		//setNewOrder(integration.getResultingOrder());
		setSecondOrder(tiw.getResultingOrder());
		setColorMatrix(cmT.getCcm());
		setShowResultTable(true);
		
		List<ResultExt> resultingOrder = tiw.getResultingOrder();
		List<Result> redraw = new ArrayList<Result>();
		for (int i = 0; i < resultingOrder.size(); i++) {
			ResultExt r = resultingOrder.get(i);
			redraw.add(new Result(r));
//			redraw.add(new Result(r.getPort(), r.getId(), r.getName(), r.getResultScore(), r.getMol(), r.getUrl(), 
//					r.getImagePath(), r.getSumFormula(), r.getExactMass()));
		}
		
		/**
		 *  new colored similarity matrix after metfusion
		 */
		TanimotoSimilarity after = new TanimotoSimilarity(listMassBank, redraw);	//, 3, 0.5f);
		// fork new thread for generating ColorCodedMatrix
		ColoredMatrixGeneratorThread cmtAfter = new ColoredMatrixGeneratorThread(after);
		
		threadExecutor = Executors.newFixedThreadPool(numThreads);
        threadExecutor.execute(cmtAfter);
        threadExecutor.shutdown();
        do {
        	Thread.sleep(1000);
        }while(!threadExecutor.isTerminated());
        setColorMatrixAfter(cmtAfter.getCcm());
        
		/**
		 * 
		 */
		
		// fork new thread for generating compound images
//		ImageGeneratorThread igT = new ImageGeneratorThread(getSecondOrder(), sessionPath, tempDir);
//        //ImageGeneratorThread igT = new ImageGeneratorThread(getSecondOrder(), DEFAULT_IMAGE_CACHE, tempDir);
//		igT.start();
//		igT.join();
		
		//int[] second = integration.weightedApproach();
		//setSecondOrder(integration.computeNewOrderingFromIndices(second));
		
//		RealMatrix rm = sim.getMatrix();
//		System.out.println("tanimoto matrix is [" + rm.getRowDimension() + "x" + rm.getColumnDimension() + "]");
//		ColorcodedMatrix ccm = new ColorcodedMatrix(rm, listMassBank, listMetFrag);
//		setColorMatrix(ccm);
		
		
		/**
		 * MetFrag cluster ranks
		 */
//		File newRankWeight = new File(outputDir, cname + "_rank_weight.txt");
//		File newRankThresh = new File(outputDir, cname + "_rank_thresh.txt");
		if(useClustering) {
			//File newRankThresh = new File(outputDir, cname + "_rank_thresh.txt");
			//List<ResultExt> clusterThresh = SimilarityMetFusion.computeScores(newOrder);
			SimilarityMetFusion sm = new SimilarityMetFusion();
			//List<ResultExt> clusterWeight =	sm.computeScores(secondOrder);
			
			System.out.println("Started clustering");
			List<ResultExtGroupBean> clusters = sm.computeScoresCluster(secondOrder);
			this.tanimotoClusters = clusters;
			System.out.println("Finished clustering");
			System.out.println("list size -> " + clusters.size());
			
			setShowClusterResults(true);
			selectedResult = "cluster";
		}
		else selectedResult = "cluster";
			//selectedResult = "list";	// show MetFusion list instead of cluster results
		
//		SimilarityMetFusion.computeScores(newOrder, newRankThresh);
//		SimilarityMetFusion.computeScores(secondOrder, newRankWeight);
		/**
		 * 
		 */
		
		/**
		 *  write SVGs
		 */
//		System.out.println("start writing SVGs");
//		writeSVG();
//		System.out.println("finished writing SVGs");
		
		setShowTable(true);
		setSelectedTab("1");	// set the "second" tab as selected tab -> this is the output tab
		/**
		 * TODO: set selected tab to 2 or 3 if errors occur
		 */
		
		// create output resource for all workflow outputs
		generateOutputResource();
		
		long time2 = System.currentTimeMillis() - time1;
		System.out.println("time spended -> " + time2 + " ms");
		
		navigate = "success";
		return navigate;
	}
	
	private void writeSVG() {
		StructureToFile sr = null;
		/**
		 *  write out massbank results
		 */
//		FileWriter fw = null;
//		try {
//			fw = new FileWriter(new File("/home/mgerlich/Desktop/integration/massbank.txt"));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		for (int i = 0; i < newOrder.size(); i++) {
			String id = newOrder.get(i).getId();
	    	IAtomContainer container = newOrder.get(i).getMol();
	    	container = AtomContainerManipulator.removeHydrogens(container);
	    	IMolecule mol = new Molecule(container);
			
			try {
				sr = new StructureToFile(200,200, "/home/mgerlich/Desktop/integration/aktuell/threshold/", false, false);
				sr.writeMOL2SVGFile(mol, id + ".svg");
			} catch (Exception e) {
				System.err.println("Error writing SVG for " + id);
			}
		}
		
		List<Result> massbank = mblb.getResults();
		for (int i = 0; i < massbank.size(); i++) {
			String id = massbank.get(i).getId();
	    	IAtomContainer container = massbank.get(i).getMol();
	    	container = AtomContainerManipulator.removeHydrogens(container);
	    	IMolecule mol = new Molecule(container);
			
			try {
				sr = new StructureToFile(200,200, "/home/mgerlich/Desktop/integration/aktuell/massbank/", false, false);
				sr.writeMOL2SVGFile(mol, id + ".svg");
				
//				if(i < 100)
//					fw.write(id + "\t" + massbank.get(i).getName() + "\t" + id + ".svg\n");
//				else {
//					fw.flush();
//					fw.close();
//				}
			} catch (Exception e) {
				System.err.println("Error writing SVG for " + id);
			}
		}
		
	}
	
	/** generates an output resource for the current workflow results, everything is stored inside a single Excel xls file
	 *  where each workflow output ports is stored as a separate sheet  */
	private void generateOutputResource() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(false);
		String sessionString = session.getId();
		ServletContext sc = (ServletContext) ec.getContext();
		String appPath = sc.getRealPath(".");
		
		//long time = new Date().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_k-m-s");
		String time = sdf.format(new Date());
		String path = appPath + sep + "temp" + sep + sessionString + sep;
		System.out.println("resource path -> " + path);
		
		File dir = new File(path);
		if(!dir.exists())
			dir.mkdirs();
		// skip creation of output resource if file access is denied
		if(!dir.canWrite())
			return;
		
		
		String resourceName = "MetFusion_Results_" + time +  ".xls";
		String folder = "./temp" + sep + sessionString + sep;
		File f = new File(dir, resourceName);
		System.out.println("outputresource -> " + f.getAbsolutePath());
		boolean createFile = false;
		try {
			createFile = f.createNewFile();
			if(!createFile) {
				System.err.println("Error creating new file for Excel output [" + f.getAbsolutePath() + "]!");
				return;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// create new Excel file
		WritableSheet sheet = null;
		WritableWorkbook workbook = null;
		WorkbookSettings settings = new WorkbookSettings();
		settings.setLocale(FacesContext.getCurrentInstance().getViewRoot().getLocale());
		try {
			workbook = Workbook.createWorkbook(f);
		} catch (IOException e) {
			e.printStackTrace();
			createdResource = false;
			return;
		}
//		String mimeType = "application/vnd.ms-excel";
		
		// write labels
		WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12);
		WritableCellFormat arial12format = new WritableCellFormat(arial12font);
		try {
			arial12font.setBoldStyle(WritableFont.BOLD);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			createdResource = false;
			return;
		}
		
		// font for text
		WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
		WritableCellFormat arial10format = new WritableCellFormat(arial10font);
		int currentSheet = 0;
		WritableImage wi = null;
		
		if(secondOrder.size() > 0) {
			// set sheet name (output port) and position
			sheet = workbook.createSheet("MetFusion Results", currentSheet);//, outputs.indexOf(port));
			currentSheet++;
			WritableCell headerRank = new Label(0, 0, "Rank", arial12format);
			WritableCell headerID = new Label(1, 0, "ID", arial12format);
			WritableCell headerName = new Label(2, 0, "Compound Name", arial12format);
			WritableCell headerOrigScore = new Label(3, 0, "Original Score", arial12format);
			WritableCell headerNewScore = new Label(4, 0, "MetFusion Score", arial12format);
			WritableCell headerStructure = new Label(5, 0, "Structure", arial12format);
			try
			{
				sheet.addCell(headerRank);
				sheet.addCell(headerID);
				sheet.addCell(headerName);
				sheet.addCell(headerOrigScore);
				sheet.addCell(headerNewScore);
				sheet.addCell(headerStructure);
			} catch (WriteException e) {
				System.out.println("Could not write Excel sheet headers!");
				e.printStackTrace();
//				createdResource = false;
//				return;
			}
			
			int currentRow = 1;
			int currentCol = 0;
			int counter = 0;
			// write MetFusion results
			for (ResultExt result : secondOrder) {
				currentRow = counter*4 + 1;
				
				// output is text
				WritableCell cellRank = new Number(0, currentRow, result.getTiedRank(), arial10format);
				WritableCell cellID = new Label(1, currentRow, result.getId(), arial10format);
				WritableCell cellName = new Label(2, currentRow, result.getName(), arial10format);
				WritableCell cellOrigScore = new Number(3, currentRow, result.getScoreShort(), arial10format);
				WritableCell cellNewScore = new Number(4, currentRow, result.getResultScore(), arial10format);
				wi = new WritableImage(5, currentRow, 1, 3, new File(appPath, result.getImagePath()));
				
				try
				{
					sheet.addCell(cellRank);
					sheet.addCell(cellID);
					sheet.addCell(cellName);
					sheet.addCell(cellOrigScore);
					sheet.addCell(cellNewScore);
					sheet.addImage(wi);
				} catch (WriteException e) {
					System.out.println("Could not write excel cell");
					e.printStackTrace();
//					createdResource = false;
//					return;
				}
				
				counter++;
			}
		}
		else {
//			createdResource = false;
//			return;
		}
			
		if(mblb.getResults().size() > 0) {
			// set sheet name (output port) and position
			sheet = workbook.createSheet("MassBank Results", currentSheet);
			currentSheet++;
			WritableCell headerRank = new Label(0, 0, "Rank", arial12format);
			WritableCell headerID = new Label(1, 0, "ID", arial12format);
			WritableCell headerName = new Label(2, 0, "Compound Name", arial12format);
			WritableCell headerOrigScore = new Label(3, 0, "Original Score", arial12format);
			WritableCell headerStructure = new Label(4, 0, "Structure", arial12format);
			try
			{
				sheet.addCell(headerRank);
				sheet.addCell(headerID);
				sheet.addCell(headerName);
				sheet.addCell(headerOrigScore);
				sheet.addCell(headerStructure);
			} catch (WriteException e) {
				System.out.println("Could not write Excel sheet headers!");
				e.printStackTrace();
//				createdResource = false;
//				return;
			}
			
			int currentRow = 1;
			int currentCol = 0;
			int counter = 0;
			
			// write MassBank results
			for (Result result : mblb.getResults()) {
				currentRow = counter*4 + 1;
				
				// output is text
				WritableCell cellRank = new Number(0, currentRow, result.getTiedRank(), arial10format);
				WritableCell cellID = new Label(1, currentRow, result.getId(), arial10format);
				WritableCell cellName = new Label(2, currentRow, result.getName(), arial10format);
				WritableCell cellOrigScore = new Number(3, currentRow, result.getScoreShort(), arial10format);
				wi = new WritableImage(4, currentRow, 1, 3, new File(appPath, result.getImagePath()));
				
				try
				{
					sheet.addCell(cellRank);
					sheet.addCell(cellID);
					sheet.addCell(cellName);
					sheet.addCell(cellOrigScore);
					sheet.addImage(wi);
				} catch (WriteException e) {
					System.out.println("Could not write excel cell");
					e.printStackTrace();
//					createdResource = false;
//					return;
				}
				
				counter++;
			}
		}
		else {
//			createdResource = false;
//			return;
		}
		
		if(mfb.getResults().size() > 0) {
			// set sheet name (output port) and position
			sheet = workbook.createSheet("MetFrag Results", currentSheet);
			currentSheet++;
			WritableCell headerRank = new Label(0, 0, "Rank", arial12format);
			WritableCell headerID = new Label(1, 0, "ID", arial12format);
			WritableCell headerName = new Label(2, 0, "Compound Name", arial12format);
			WritableCell headerOrigScore = new Label(3, 0, "Original Score", arial12format);
			WritableCell headerStructure = new Label(4, 0, "Structure", arial12format);
			try
			{
				sheet.addCell(headerRank);
				sheet.addCell(headerID);
				sheet.addCell(headerName);
				sheet.addCell(headerOrigScore);
				sheet.addCell(headerStructure);
			} catch (WriteException e) {
				System.out.println("Could not write Excel sheet headers!");
				e.printStackTrace();
			}
			
			int currentRow = 1;
			int currentCol = 0;
			int counter = 0;
			// write MetFrag results
			for (Result result : mfb.getResults()) {
				currentRow = counter*4 + 1;
				
				// output is text
				WritableCell cellRank = new Number(0, currentRow, result.getTiedRank(), arial10format);
				WritableCell cellID = new Label(1, currentRow, result.getId(), arial10format);
				WritableCell cellName = new Label(2, currentRow, result.getName(), arial10format);
				WritableCell cellOrigScore = new Number(3, currentRow, result.getScoreShort(), arial10format);
				wi = new WritableImage(4, currentRow, 1, 3, new File(appPath, result.getImagePath()));
				
				try
				{
					sheet.addCell(cellRank);
					sheet.addCell(cellID);
					sheet.addCell(cellName);
					sheet.addCell(cellOrigScore);
					sheet.addImage(wi);
				} catch (WriteException e) {
					System.out.println("Could not write excel cell");
					e.printStackTrace();
//					createdResource = false;
//					return;
				}
				
				counter++;
			}
		}
		else {
//			createdResource = false;
//			return;
		}
		
		
		
		// for each workflow output port, create new sheet inside Excel file and store results
//		for (WorkflowOutput port : outputs) {
//			// set sheet name (output port) and position
//			sheet = workbook.createSheet(port.getName(), outputs.indexOf(port));
//			ArrayList<WorkflowOutput> elements = port.getElements();
//			
//			// set header for sheet, name it after output port name 
//			try {
//				WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
//				WritableCellFormat arial10format = new WritableCellFormat(
//						arial10font);
//				arial10font.setBoldStyle(WritableFont.BOLD);
//				Label label = new Label(0, 0, port.getName(), arial10format);
//				sheet.addCell(label);
//			} catch (WriteException we) {
//				we.printStackTrace();
//			}
//			
//			// for all output elements, store their result inside the current sheet
//			// either store the image or the value part of an output 
//			for (int i = 0; i < elements.size(); i++) {
//				WritableCell cell = null;
//				WritableImage wi = null;
//				if(elements.get(i).isImage()) {		// output is image
//					String imgPath = appPath + elements.get(i).getPath();
//					File image = new File(imgPath);
//					// write each image into the second column, leave one row space between them and 
//					// resize the image to 1 column width and 2 rows height
//					wi = new WritableImage(1, (i*3) + 1, 1, 2, image);
//					sheet.addImage(wi);
//				}
//				else if(!elements.get(i).isImage()) {	// output is text
//					cell = new Label(1, i, elements.get(i).getValue());
//					try {
//						sheet.addCell(cell);
//					} catch (WriteException e) {
//						System.out.println("Could not write excel cell");
//						e.printStackTrace();
//					}
//				}
//			}
//		}
		
		// write the Excel file
		try {
			workbook.write();
			workbook.close();
		} catch (WriteException ioe) {
			ioe.printStackTrace();
			createdResource = false;
			return;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			createdResource = false;
			return;
		}
		
//		OutputResource out = new OutputResource();
//		out.setFileName(f.getAbsolutePath());
//		out.setMimeType(mimeType);
		// store the current Excel file as output resource
		XLSResource xls = new XLSResource(ec, resourceName, folder);
		setOutputResource(xls);
		setCreatedResource(Boolean.TRUE);
		
		//this.exporter = new XLSOutputHandler(folder + resourceName);//, FacesContext.getCurrentInstance(), "Results");
	}
	
	/**
	 * Decode HTML-attribute style of color to {@link Color}
	 * 
	 * @param color - color name or #RRGGBB string
	 * @return - color for this value.
	 */
	public Color decode(String color) {
		if (null == color) {
			throw new IllegalArgumentException("NULL_COLOR_PARAMETER_ERROR");
		}
		Color c = null;
		try {
			System.out.println("color code -> " + color);
			c = Color.decode(color.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("DECODE_COLOR_PARAMETER_ERROR");
		}
		return c;
	}

	public void reset(ActionEvent event) {
		System.out.println("started reset procedures");
		this.newOrder = new ArrayList<ResultExt>();
		this.secondOrder = new ArrayList<ResultExt>();
		this.colorMatrix = null;
		this.showTable = false;
		//this.useClustering = false;
		
		this.mblb.setOriginalResults(null);
		this.mblb.setQueryResults(null);
		this.mblb.setResults(null);
		this.mblb.setShowResult(false);
		
		this.mfb.setResults(null);
		setShowResultsFragmenter(false);
		setShowResultsDatabase(false);
		setShowTable(false);
		
		this.mfb.setShowResult(false);
		setShowClusterResults(false);
		this.tanimotoClusters = new ArrayList<ResultExtGroupBean>();
		
		this.selectedTab = "0";
		//setMblb(new MassBankLookupBean());
		//setMfb(new MetFragBean());
		System.out.println("finished reset procedures");
	}
	
	public MassBankLookupBean getMblb() {
		return mblb;
	}

	public void setMblb(MassBankLookupBean mblb) {
		this.mblb = mblb;
	}

	public MetFragBean getMfb() {
		return mfb;
	}

	public void setMfb(MetFragBean mfb) {
		this.mfb = mfb;
	}

	public void setNewOrder(List<ResultExt> newOrder) {
		this.newOrder = newOrder;
	}

	public List<ResultExt> getNewOrder() {
		return newOrder;
	}

	public void setColorMatrix(ColorcodedMatrix colorMatrix) {
		this.colorMatrix = colorMatrix;
	}

	public ColorcodedMatrix getColorMatrix() {
		return colorMatrix;
	}

	public void setSecondOrder(List<ResultExt> secondOrder) {
		this.secondOrder = secondOrder;
	}

	public List<ResultExt> getSecondOrder() {
		return secondOrder;
	}

	public void setChartValues(List<double[]> chartValues) {
		this.chartValues = chartValues;
	}

	public List<double[]> getChartValues() {
		return chartValues;
	}

	public void setChartColors(List<Color> chartColors) {
		this.chartColors = chartColors;
	}

	public List<Color> getChartColors() {
		return chartColors;
	}

	public void setShowTable(boolean showTable) {
		this.showTable = showTable;
	}

	public boolean isShowTable() {
		return showTable;
	}

	public void setInputSpectrum(String inputSpectrum) {
		this.inputSpectrum = inputSpectrum;
	}

	public String getInputSpectrum() {
		return inputSpectrum;
	}

	public void setUseClustering(boolean useClustering) {
		this.useClustering = useClustering;
	}

	public boolean isUseClustering() {
		return useClustering;
	}

	public void setShowResultTable(boolean showResultTable) {
		this.showResultTable = showResultTable;
	}

	public boolean isShowResultTable() {
		return showResultTable;
	}

	public void setShowResultsDatabase(boolean showResultsDatabase) {
		this.showResultsDatabase = showResultsDatabase;
	}

	public boolean isShowResultsDatabase() {
		return showResultsDatabase;
	}

	public void setShowResultsFragmenter(boolean showResultsFragmenter) {
		this.showResultsFragmenter = showResultsFragmenter;
	}

	public boolean isShowResultsFragmenter() {
		return showResultsFragmenter;
	}

	public void setShowClusterResults(boolean showClusterResults) {
		this.showClusterResults = showClusterResults;
	}

	public boolean isShowClusterResults() {
		return showClusterResults;
	}

	public void setTanimotoClusters(List<ResultExtGroupBean> tanimotoClusters) {
		this.tanimotoClusters = tanimotoClusters;
	}

	public List<ResultExtGroupBean> getTanimotoClusters() {
		return tanimotoClusters;
	}

	/** Returns the text effect
     * @return Effect EffectOutputText
     */
   public Effect getEffectOutputText() {
	   effectOutputText = new Highlight("#FFA500");
       effectOutputText.setFired(false);
       
       return effectOutputText;
   }

   /**
     * Sets the output text effect
     * @param Effect effectOutputText
     */
   public void setEffectOutputText(Effect effectOutputText) {
       this.effectOutputText = (Highlight) effectOutputText;
   }

	public void setSelectedResult(String selectedResult) {
		this.selectedResult = selectedResult;
	}
	
	public String getSelectedResult() {
		return selectedResult;
	}
	
	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}
	
	public String getSelectedTab() {
		return selectedTab;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setColorMatrixAfter(ColorcodedMatrix colorMatrixAfter) {
		this.colorMatrixAfter = colorMatrixAfter;
	}
	
	public ColorcodedMatrix getColorMatrixAfter() {
		return colorMatrixAfter;
	}
	
	public void setOutputResource(Resource outputResource) {
		this.outputResource = outputResource;
	}
	
	public Resource getOutputResource() {
		return outputResource;
	}
	
	public void setExporter(XLSOutputHandler exporter) {
		this.exporter = exporter;
	}
	
	public XLSOutputHandler getExporter() {
		return exporter;
	}

	public void setSelectedMatrixPanel(String selectedMatrixPanel) {
		this.selectedMatrixPanel = selectedMatrixPanel;
	}

	public String getSelectedMatrixPanel() {
		return selectedMatrixPanel;
	}

	public void setCreatedResource(boolean createdResource) {
		this.createdResource = createdResource;
	}

	public boolean isCreatedResource() {
		return createdResource;
	}

}
