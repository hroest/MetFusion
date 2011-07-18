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
import java.util.ArrayList;
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
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.math.linear.RealMatrix;
import org.icefaces.application.PortableRenderer;
import org.icefaces.application.PushRenderer;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

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
	
	public void runBoth(ActionEvent event) throws InterruptedException {
		long time1 = System.currentTimeMillis();
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
		
        ELResolver el = fc.getApplication().getELResolver();
        ELContext elc = fc.getELContext();
        ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
        System.out.println(sc.getContextPath());
        
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
            FacesMessage curentMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, errMessage, errMessage);
            fc.addMessage("inputForm:instruments", curentMessage);
            
            setShowResultsDatabase(false);
            setShowTable(true);
            return;
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
        	String errMessage = "EMPTY MassBank result! - Check settings.";
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
            return;
        }
        else if(mblb.getResults() != null) {
            System.out.println("# MassBank results: " + mblb.getResults().size());
            setShowResultsDatabase(true);
        }
        else {      // abort run and return
            String errMessage = "EMPTY MassBank result! - Check settings.";
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
            return;
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
            return;
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
            return;
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
			redraw.add(new Result(r.getPort(), r.getId(), r.getName(), r.getResultScore(), r.getMol(), r.getUrl(), r.getImagePath()));
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
		else 
			selectedResult = "list";	// show MetFusion list instead of cluster results
		
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
		
		long time2 = System.currentTimeMillis() - time1;
		System.out.println("time spended -> " + time2 + " ms");
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

}
