/**
 * created by Michael Gerlich, Sep 1, 2011 - 4:15:14 PM
 */ 

package de.ipbhalle.metfusion.threading;

import java.util.ArrayList;
import java.util.List;

import de.ipbhalle.MassBank.MassBankLookupBean;
import de.ipbhalle.metfusion.integration.Similarity.SimilarityMetFusion;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoIntegrationWeighted;
import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;
import de.ipbhalle.metfusion.web.controller.MetFragBean;
import de.ipbhalle.metfusion.web.controller.MetFusionBean;
import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.web.controller.StyleBean;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class MetFusionThread implements Runnable {

	private MetFusionBean metfusion;
	private MassBankLookupBean massbank;
	private MetFragBean metfrag;
	private StyleBean styleBean;
	private String tempPath;
	private int progress;
	private boolean active = Boolean.FALSE;
	private int steps = 0;
	private final int totalSteps = 8;
	
	public MetFusionThread(MetFusionBean app, MassBankLookupBean database, MetFragBean fragmenter, StyleBean styleBean, String tempPath) {
		this.metfusion = app;
		this.massbank = database;
		this.metfrag = fragmenter;
		this.styleBean = styleBean;
		this.tempPath = tempPath;
	}
	
	/**
	 * execute MassBank and MetFrag retrieval threads
	 */
	public synchronized void guardedRetrieval() {
		
	}
	
	/**
	 * execute matrix computation thread
	 */
	public synchronized void guardedMatrix() {
		
	}
	
	/**
	 * execute image generation threads
	 */
	public synchronized void guardedImageGeneration() {
		
	}
	
	@Override
	public void run() {
		long time1 = System.currentTimeMillis();
		setActive(Boolean.TRUE);
		setProgress(steps);
		metfusion.setStatus("Retrieval");
		metfusion.toggleEffect();		// let progress bars appear
		
		massbank.run();
		metfrag.run();
		while(!massbank.isDone() && !metfrag.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		steps += 2;
		stepsDonePercent(steps);
		metfusion.setShowResultsDatabase(true);
		System.out.println("done threading");
		metfusion.toggleEffect();		// let progress bars fade away
		
		metfusion.setStatus("Images + Matrix");
		// create tanimoto matrix and perform chemical-similarity based integration
		List<Result> listMassBank = massbank.getResults();
		List<Result> listMetFrag = metfrag.getResults();
		TanimotoSimilarity sim = new TanimotoSimilarity(listMassBank, listMetFrag);	//, 3, 0.5f);
		String sessionPath = massbank.getSessionPath();
		// fork new thread for generating ColorCodedMatrix
		ColoredMatrixGeneratorThread cmT = new ColoredMatrixGeneratorThread(sim);
		TanimotoIntegrationWeighted tiw = new TanimotoIntegrationWeighted(sim);
		// fork new thread for generating compound images
		ImageGeneratorThread igT = new ImageGeneratorThread(listMetFrag, sessionPath, tempPath);
		ImageGeneratorThread igT2 = new ImageGeneratorThread(listMassBank, sessionPath, tempPath);
		
//		ExecutorService threadExecutor = Executors.newFixedThreadPool(4);
//        threadExecutor.execute(tiw);
//        threadExecutor.execute(cmT);
//        threadExecutor.execute(igT);
//        threadExecutor.execute(igT2);
//        threadExecutor.shutdown();
        
        tiw.run();
        cmT.run();
        igT.run();
        igT2.run();
        
        /**
		 * MetFrag cluster ranks
		 */
        while(!tiw.isDone() && !cmT.isDone() && !igT.isDone() && !igT2.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
        steps += 4;
        stepsDonePercent(steps);
        metfusion.setColorMatrix(cmT.getCcm());
        
        List<ResultExt> resultingOrder = tiw.getResultingOrder();
		List<Result> redraw = new ArrayList<Result>();
		for (int i = 0; i < resultingOrder.size(); i++) {
			ResultExt r = resultingOrder.get(i);
			redraw.add(new Result(r));
		}
		
		/**
		 *  new colored similarity matrix after metfusion
		 */
		TanimotoSimilarity after = new TanimotoSimilarity(listMassBank, redraw);	//, 3, 0.5f);
		// fork new thread for generating ColorCodedMatrix
		ColoredMatrixGeneratorThread cmtAfter = new ColoredMatrixGeneratorThread(after);
		cmtAfter.run();
		while(!cmtAfter.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		steps++;
		stepsDonePercent(steps);
        metfusion.setColorMatrixAfter(cmtAfter.getCcm());
        metfusion.setStatus("Clustering");
        
        metfusion.setSecondOrder(resultingOrder);	// assign results to metfusion bean
		SimilarityMetFusion sm = new SimilarityMetFusion();
		System.out.println("Started clustering");
		List<ResultExtGroupBean> clusters = sm.computeScoresCluster(resultingOrder, styleBean);
		metfusion.setTanimotoClusters(clusters);
		System.out.println("Finished clustering");
		metfusion.generateOutputResource();
		metfusion.setShowTable(true);
		metfusion.setSelectedTab("1");
		metfusion.setShowResultTable(true);
		metfusion.setShowClusterResults(true);
		
		steps++;
		stepsDonePercent(steps);
		//setProgress(100);
		System.out.println("list size -> " + clusters.size());
		setActive(Boolean.FALSE);
		metfusion.setEnableStart(Boolean.TRUE);
		long time2 = System.currentTimeMillis() - time1;
		System.out.println("time spended -> " + time2 + " ms");
	}

	/**
	 * Compute the percentage progress depending on the steps accomplished.
	 * If steps is larger than totalSteps, it is set to totalSteps, thus leading
	 * to 100%.
	 * 
	 * @param steps - the number of steps already taken
	 */
	public void stepsDonePercent(int steps) {
		if(steps > totalSteps)	// ensure upper bound
			steps = totalSteps;
		float temp = ((float) steps  * 100f) / (float) totalSteps;
		setProgress(Math.round(temp));
	}
	
	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}
