package de.ipbhalle.metfusion.integration.Similarity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfrag.similarity.Similarity;
import de.ipbhalle.metfrag.similarity.SimilarityCompound;
import de.ipbhalle.metfrag.similarity.SimilarityGroup;
import de.ipbhalle.metfrag.similarity.TanimotoClusterer;
import de.ipbhalle.metfusion.web.controller.ResultExtGroupBean;
import de.ipbhalle.metfusion.web.controller.StyleBean;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.wrapper.ResultExtGroup;

public class SimilarityMetFusion {
	
	private List<SimilarityGroup> groupedCandidates = new ArrayList<SimilarityGroup>();
	
	private List<ResultExtGroup> clusterGroups;
	
	// Expandable rows setup
	// css style related constants
	public static final String GROUP_INDENT_STYLE_CLASS = "groupRowIndentStyle";
	public static final String GROUP_ROW_STYLE_CLASS = "groupRowStyle";
	public static final String CHILD_INDENT_STYLE_CLASS = "childRowIndentStyle";
	public static final String CHILD_ROW_STYLE_CLASS = "childRowStyle";
	
	// toggle for expand contract
	public static final String CONTRACT_IMAGE = "tree_nav_top_close_no_siblings.gif";
	public static final String EXPAND_IMAGE = "tree_nav_top_open_no_siblings.gif";
	protected static final String SPACER_IMAGE = "tree_line_blank.gif";
	
	private List<ResultExtGroupBean> resultRowGroupedBeans;

	public SimilarityMetFusion() {
		// TODO Auto-generated constructor stub
	}

	public List<ResultExtGroupBean> computeScoresCluster(List<ResultExt> results, StyleBean styleBean) {
		// initalize the list
		resultRowGroupedBeans = new ArrayList<ResultExtGroupBean>();
		
//		FacesContext fc = FacesContext.getCurrentInstance();
//        ELResolver el = fc.getApplication().getELResolver();
//        ELContext elc = fc.getELContext();
//        StyleBean styleBean = (StyleBean) el.getValue(elc, null, "styleBean");
        
		if(styleBean == null || !(styleBean instanceof StyleBean)) {
			System.err.println("stylebean not found!");
			//return new ArrayList<ResultExtGroupBean>();
		}
        
		Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
		Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
		Map<String, ResultExt> mapPositions = new HashMap<String, ResultExt>();
		
		for (int i = 0; i < results.size(); i++) {
			String id = results.get(i).getId();
			IAtomContainer molecule = results.get(i).getMol();
			mapPositions.put(id, results.get(i));
			
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
		        hAdder.addImplicitHydrogens(molecule);
		        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
			} catch (CDKException e) {
				System.err.println("error manipulating mol for " + id);
				continue;
			}
	        
	        candidateToStructure.put(id, molecule);
			Double currentScore = results.get(i).getResultScore();
			
			if(realScoreMap.containsKey(currentScore))
	        {
	        	Vector<String> tempList = realScoreMap.get(currentScore);
	        	tempList.add(id);
	        	realScoreMap.put(currentScore, tempList);
	        }
	        else
	        {
	        	Vector<String> temp = new Vector<String>();
	        	temp.add(id);
	        	realScoreMap.put(currentScore, temp);
	        }
		}
		
		Double[] keysScore = new Double[realScoreMap.keySet().size()];
		keysScore = realScoreMap.keySet().toArray(keysScore);
		Arrays.sort(keysScore);
		
		//Map<String, Integer> mapGroups = new HashMap<String, Integer>();
		//List<ResultExt> newRanking = new ArrayList<ResultExt>();
		
		//int rankTanimotoGroup = 0;
		for (int i = keysScore.length-1; i >= 0; i--) {
			List<String> candidateGroup = new ArrayList<String>();
			Map<String, IAtomContainer> candidateToStructureTemp = new HashMap<String, IAtomContainer>();
			
			for (int j = 0; j < realScoreMap.get(keysScore[i]).size(); j++) {
				candidateGroup.add(realScoreMap.get(keysScore[i]).get(j));
				candidateToStructureTemp.put(realScoreMap.get(keysScore[i]).get(j), candidateToStructure.get(realScoreMap.get(keysScore[i]).get(j)));
			}
			
			Similarity sim = null;
			try {
				sim = new Similarity(candidateToStructureTemp, true, false);
			} catch (CDKException e) {
				e.printStackTrace();
				return new ArrayList<ResultExtGroupBean>();
			}
			TanimotoClusterer tanimoto = new TanimotoClusterer(sim.getSimilarityMatrix(), sim.getCandidateToPosition());
			List<SimilarityGroup> clusteredCpds = tanimoto.clusterCandididates(candidateGroup, 0.95f);
			List<SimilarityGroup> groupedCandidates = tanimoto.getCleanedClusters(clusteredCpds);
		
			for (SimilarityGroup similarityGroup : groupedCandidates) {
			    //cluster
			    if(similarityGroup.getSimilarCompounds().size() > 1)
			    {
			    	ResultExtGroupBean filesRecordGroup = new ResultExtGroupBean(
							GROUP_INDENT_STYLE_CLASS, GROUP_ROW_STYLE_CLASS,
							styleBean, EXPAND_IMAGE, CONTRACT_IMAGE,
							resultRowGroupedBeans, false);
					//String baseCand = similarityGroup.getCandidateTocompare();
					ResultExt parent = mapPositions.get(similarityGroup.getCandidateTocompare());
					addToResultsList(parent, filesRecordGroup);

			     for (int k = 0; k < similarityGroup.getSimilarCompounds().size(); k++) {
					if(similarityGroup.getCandidateTocompare().equals(similarityGroup.getSimilarCompounds().get(k).getCompoundID()))
						continue;
			    	 ResultExtGroupBean childFilesGroup = new ResultExtGroupBean(CHILD_INDENT_STYLE_CLASS, CHILD_ROW_STYLE_CLASS);
			    	 ResultExt child = mapPositions.get(similarityGroup.getSimilarCompounds().get(k).getCompoundID());
			    	 addToResultsList(child, childFilesGroup);
			    	 filesRecordGroup.addChildFilesGroupRecord(childFilesGroup);
			     }
			    }
			    //single
			    else
			    {
			    	ResultExtGroupBean filesRecordGroup = new ResultExtGroupBean(
							"", "", styleBean, SPACER_IMAGE, SPACER_IMAGE,
							resultRowGroupedBeans, false);
					
					// retrieve current result and store it in cluster list
					ResultExt single = mapPositions.get(similarityGroup.getCandidateTocompare());
					if(!(single == null))
						addToResultsList(single, filesRecordGroup);
			    }
			}
		}
		return resultRowGroupedBeans;
	}

	/**
	 * Cluster version without StyleBean.
	 * 
	 * @param results
	 * @return
	 */
	public List<ResultExtGroupBean> computeScoresCluster(List<ResultExt> results) {
		// initalize the list
		resultRowGroupedBeans = new ArrayList<ResultExtGroupBean>();

		Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
		Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
		Map<String, ResultExt> mapPositions = new HashMap<String, ResultExt>();

		for (int i = 0; i < results.size(); i++) {
			String id = results.get(i).getId();
			IAtomContainer molecule = results.get(i).getMol();
			mapPositions.put(id, results.get(i));

			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
				hAdder.addImplicitHydrogens(molecule);
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
			} catch (CDKException e) {
				System.err.println("error manipulating mol for " + id);
				continue;
			}

			candidateToStructure.put(id, molecule);
			Double currentScore = results.get(i).getResultScore();

			if (realScoreMap.containsKey(currentScore)) {
				Vector<String> tempList = realScoreMap.get(currentScore);
				tempList.add(id);
				realScoreMap.put(currentScore, tempList);
			} else {
				Vector<String> temp = new Vector<String>();
				temp.add(id);
				realScoreMap.put(currentScore, temp);
			}
		}

		Double[] keysScore = new Double[realScoreMap.keySet().size()];
		keysScore = realScoreMap.keySet().toArray(keysScore);
		Arrays.sort(keysScore);

		// Map<String, Integer> mapGroups = new HashMap<String, Integer>();
		// List<ResultExt> newRanking = new ArrayList<ResultExt>();

		int rankTanimotoGroup = 0;
		int rankBefore = 0;
		int clusterSize = 0;
		for (int i = keysScore.length - 1; i >= 0; i--) {
			List<String> candidateGroup = new ArrayList<String>();
			Map<String, IAtomContainer> candidateToStructureTemp = new HashMap<String, IAtomContainer>();

			for (int j = 0; j < realScoreMap.get(keysScore[i]).size(); j++) {
				candidateGroup.add(realScoreMap.get(keysScore[i]).get(j));
				candidateToStructureTemp.put(realScoreMap.get(keysScore[i]).get(j), 
						candidateToStructure.get(realScoreMap.get(keysScore[i]).get(j)));
			}

			Similarity sim = null;
			try {
				sim = new Similarity(candidateToStructureTemp, true, false);
			} catch (CDKException e) {
				e.printStackTrace();
				return new ArrayList<ResultExtGroupBean>();
			}
			TanimotoClusterer tanimoto = new TanimotoClusterer(sim.getSimilarityMatrix(), sim.getCandidateToPosition());
			List<SimilarityGroup> clusteredCpds = tanimoto.clusterCandididates(candidateGroup, 0.95f);
			List<SimilarityGroup> groupedCandidates = tanimoto.getCleanedClusters(clusteredCpds);

			for (SimilarityGroup similarityGroup : groupedCandidates) {
				clusterSize = similarityGroup.getSimilarCompounds().size();
				rankTanimotoGroup = rankBefore + clusterSize;
				// cluster
				if (similarityGroup.getSimilarCompounds().size() > 1) {
					ResultExtGroupBean filesRecordGroup = new ResultExtGroupBean(GROUP_INDENT_STYLE_CLASS, GROUP_ROW_STYLE_CLASS,
							null, EXPAND_IMAGE, CONTRACT_IMAGE,	resultRowGroupedBeans, false);
					// String baseCand =
					// similarityGroup.getCandidateTocompare();
					ResultExt parent = mapPositions.get(similarityGroup.getCandidateTocompare());
					parent.setClusterRank(rankTanimotoGroup);
					addToResultsList(parent, filesRecordGroup);

					for (int k = 0; k < similarityGroup.getSimilarCompounds().size(); k++) {
						if (similarityGroup.getCandidateTocompare().equals(similarityGroup.getSimilarCompounds().get(k).getCompoundID()))
							continue;
						
						ResultExtGroupBean childFilesGroup = new ResultExtGroupBean(CHILD_INDENT_STYLE_CLASS, CHILD_ROW_STYLE_CLASS);
						ResultExt child = mapPositions.get(similarityGroup.getSimilarCompounds().get(k).getCompoundID());
						child.setClusterRank(rankTanimotoGroup);
						addToResultsList(child, childFilesGroup);
						filesRecordGroup.addChildFilesGroupRecord(childFilesGroup);
					}
				}
				// single
				else {
					ResultExtGroupBean filesRecordGroup = new ResultExtGroupBean("", "", null, SPACER_IMAGE, SPACER_IMAGE, resultRowGroupedBeans, false);

					// retrieve current result and store it in cluster list
					ResultExt single = mapPositions.get(similarityGroup.getCandidateTocompare());
					single.setClusterRank(rankTanimotoGroup);
					if (!(single == null))
						addToResultsList(single, filesRecordGroup);
				}
				rankBefore = rankBefore + clusterSize;
			}
		}
		return resultRowGroupedBeans;
	}
	
	/**
	 * Cluster version without StyleBean.
	 * 
	 * @param results
	 * @return
	 */
	public List<ResultExt> computeClusterRank(List<ResultExt> results) {
		List<ResultExt> clusteredResults = new ArrayList<ResultExt>();
		
		Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
		Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
		Map<String, ResultExt> mapPositions = new HashMap<String, ResultExt>();

		for (int i = 0; i < results.size(); i++) {
			String id = results.get(i).getId();
			IAtomContainer molecule = results.get(i).getMol();
			mapPositions.put(id, results.get(i));

			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
				hAdder.addImplicitHydrogens(molecule);
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
			} catch (CDKException e) {
				System.err.println("error manipulating mol for " + id);
				continue;
			}

			candidateToStructure.put(id, molecule);
			Double currentScore = results.get(i).getResultScore();

			if (realScoreMap.containsKey(currentScore)) {
				Vector<String> tempList = realScoreMap.get(currentScore);
				tempList.add(id);
				realScoreMap.put(currentScore, tempList);
			} else {
				Vector<String> temp = new Vector<String>();
				temp.add(id);
				realScoreMap.put(currentScore, temp);
			}
		}

		Double[] keysScore = new Double[realScoreMap.keySet().size()];
		keysScore = realScoreMap.keySet().toArray(keysScore);
		Arrays.sort(keysScore);

		int rankTanimotoGroup = 0;
		int rankBefore = 0;
		int clusterSize = 0;
		for (int i = keysScore.length - 1; i >= 0; i--) {
			List<String> candidateGroup = new ArrayList<String>();
			Map<String, IAtomContainer> candidateToStructureTemp = new HashMap<String, IAtomContainer>();

			for (int j = 0; j < realScoreMap.get(keysScore[i]).size(); j++) {
				candidateGroup.add(realScoreMap.get(keysScore[i]).get(j));
				candidateToStructureTemp.put(realScoreMap.get(keysScore[i]).get(j), 
						candidateToStructure.get(realScoreMap.get(keysScore[i]).get(j)));
			}

			Similarity sim = null;
			try {
				sim = new Similarity(candidateToStructureTemp, true, false);
			} catch (CDKException e) {
				e.printStackTrace();
				continue;		// skip scores group if similarity can not be computed
			}
			TanimotoClusterer tanimoto = new TanimotoClusterer(sim.getSimilarityMatrix(), sim.getCandidateToPosition());
			List<SimilarityGroup> clusteredCpds = tanimoto.clusterCandididates(candidateGroup, 0.95f);
			List<SimilarityGroup> groupedCandidates = tanimoto.getCleanedClusters(clusteredCpds);

			for (SimilarityGroup similarityGroup : groupedCandidates) {
				clusterSize = similarityGroup.getSimilarCompounds().size();
				rankTanimotoGroup = rankBefore + clusterSize;
				// cluster
				if (similarityGroup.getSimilarCompounds().size() > 1) {
					ResultExt parent = mapPositions.get(similarityGroup.getCandidateTocompare());
					parent.setClusterRank(rankTanimotoGroup);
					clusteredResults.add(parent);

					for (int k = 0; k < similarityGroup.getSimilarCompounds().size(); k++) {
						if (similarityGroup.getCandidateTocompare().equals(similarityGroup.getSimilarCompounds().get(k).getCompoundID()))
							continue;
						
						ResultExt child = mapPositions.get(similarityGroup.getSimilarCompounds().get(k).getCompoundID());
						child.setClusterRank(rankTanimotoGroup);
						clusteredResults.add(child);
					}
				}
				// single
				else {
					// retrieve current result and store it in cluster list
					ResultExt single = mapPositions.get(similarityGroup.getCandidateTocompare());
					single.setClusterRank(rankTanimotoGroup);
					
					clusteredResults.add(single);
				}
				rankBefore = rankBefore + clusterSize;
			}
		}
		return clusteredResults;
	}
	
	/**
	 * method to cluster results by Tanimoto score
	 * @param results
	 * @return
	 */
	public List<ResultExt> computeScores(List<ResultExt> results) {
		Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
		Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
		Map<String, ResultExt> mapPositions = new HashMap<String, ResultExt>();
		
		for (int i = 0; i < results.size(); i++) {
			String id = results.get(i).getId();
			IAtomContainer molecule = results.get(i).getMol();
			mapPositions.put(id, results.get(i));
			
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
		        hAdder.addImplicitHydrogens(molecule);
		        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
			} catch (CDKException e) {
				System.err.println("error manipulating mol for " + id);
				continue;
			}
	        
	        candidateToStructure.put(id, molecule);
			Double currentScore = results.get(i).getResultScore();
			
			if(realScoreMap.containsKey(currentScore))
	        {
	        	Vector<String> tempList = realScoreMap.get(currentScore);
	        	tempList.add(id);
	        	realScoreMap.put(currentScore, tempList);
	        }
	        else
	        {
	        	Vector<String> temp = new Vector<String>();
	        	temp.add(id);
	        	realScoreMap.put(currentScore, temp);
	        }
		}
		
		Double[] keysScore = new Double[realScoreMap.keySet().size()];
		keysScore = realScoreMap.keySet().toArray(keysScore);
		Arrays.sort(keysScore);
		
		//Map<String, Integer> mapGroups = new HashMap<String, Integer>();
		List<ResultExt> newRanking = new ArrayList<ResultExt>();
		
		int rankTanimotoGroup = 0;
		for (int i = keysScore.length-1; i >= 0; i--) {
			List<String> candidateGroup = new ArrayList<String>();
			Map<String, IAtomContainer> candidateToStructureTemp = new HashMap<String, IAtomContainer>();
			
			for (int j = 0; j < realScoreMap.get(keysScore[i]).size(); j++) {
				candidateGroup.add(realScoreMap.get(keysScore[i]).get(j));
				candidateToStructureTemp.put(realScoreMap.get(keysScore[i]).get(j), candidateToStructure.get(realScoreMap.get(keysScore[i]).get(j)));
			}
			
			//			Similarity sim = null;
//			try {
//				sim = new Similarity(candidateToStructureTemp, (float)0.95, true);
//			} catch (CDKException e) {
//				System.err.println("error creating Similarity object");
//				return new ArrayList<ResultExt>();
//			}
			Similarity sim = null;
			try {
				sim = new Similarity(candidateToStructureTemp, true, false);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TanimotoClusterer tanimoto = new TanimotoClusterer(sim.getSimilarityMatrix(), sim.getCandidateToPosition());
			List<SimilarityGroup> clusteredCpds = tanimoto.clusterCandididates(candidateGroup, 0.95f);
			List<SimilarityGroup> groupedCandidates = tanimoto.getCleanedClusters(clusteredCpds);
			
			// set result List
			setGroupedCandidates(groupedCandidates);
			
			
			//List<SimilarityGroup> groupedCandidates = sim.getTanimotoDistanceList(candidateGroup);
			List<String> groups = new ArrayList<String>();
			for (SimilarityGroup similarityGroup : groupedCandidates) {				
				List<SimilarityCompound> tempSimilar = similarityGroup.getSimilarCompounds();
				//List<Float> tempSimilarTanimoto = similarityGroup.getSimilarCompoundsTanimoto();
				
				//groups.add(similarityGroup.getCandidateTocompare());

				for (int k = 0; k < tempSimilar.size(); k++) {
					groups.add(tempSimilar.get(k).getCompoundID());
				}	
				rankTanimotoGroup++;
			}
			for (String string : groups) {
				//DAS IST ENDERGEBNISTABELLE
				System.out.println(string + "\t" + rankTanimotoGroup);
				
				//mapGroups.put(string, rankTanimotoGroup);
				
				/**
				 * add clusterRank
				 */
				ResultExt r = mapPositions.get(string);
				r.setClusterRank(rankTanimotoGroup);
				newRanking.add(r);
			}
		}
		
		return newRanking;
	}

	/**
	 * original approach
	 */
	public static List<ResultExt> computeScores(List<ResultExt> results, File f) {
		Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
		Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
		Map<String, ResultExt> mapPositions = new HashMap<String, ResultExt>();
		
		for (int i = 0; i < results.size(); i++) {
			String id = results.get(i).getId();
			IAtomContainer molecule = results.get(i).getMol();
			mapPositions.put(id, results.get(i));
			
			try {
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
		        hAdder.addImplicitHydrogens(molecule);
		        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
			} catch (CDKException e) {
				System.err.println("error manipulating mol for " + id);
				continue;
			}
	        
	        candidateToStructure.put(id, molecule);
			Double currentScore = results.get(i).getResultScore();
			
			if(realScoreMap.containsKey(currentScore))
	        {
	        	Vector<String> tempList = realScoreMap.get(currentScore);
	        	tempList.add(id);
	        	realScoreMap.put(currentScore, tempList);
	        }
	        else
	        {
	        	Vector<String> temp = new Vector<String>();
	        	temp.add(id);
	        	realScoreMap.put(currentScore, temp);
	        }
		}
		
		Double[] keysScore = new Double[realScoreMap.keySet().size()];
		keysScore = realScoreMap.keySet().toArray(keysScore);
		Arrays.sort(keysScore);
		
		//Map<String, Integer> mapGroups = new HashMap<String, Integer>();
		List<ResultExt> newRanking = new ArrayList<ResultExt>();
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
		} catch (IOException e) {
			System.err.println("Error creating FileWriter for " + f);
		}
		
		
		int rankTanimotoGroup = 0;
		for (int i = keysScore.length-1; i >= 0; i--) {
			List<String> candidateGroup = new ArrayList<String>();
			Map<String, IAtomContainer> candidateToStructureTemp = new HashMap<String, IAtomContainer>();
			
			for (int j = 0; j < realScoreMap.get(keysScore[i]).size(); j++) {
				candidateGroup.add(realScoreMap.get(keysScore[i]).get(j));
				candidateToStructureTemp.put(realScoreMap.get(keysScore[i]).get(j), candidateToStructure.get(realScoreMap.get(keysScore[i]).get(j)));
			}
			
//			Similarity sim = null;
//			try {
//				sim = new Similarity(candidateToStructureTemp, (float)0.95, true);
//			} catch (CDKException e) {
//				System.err.println("error creating Similarity object");
//				return new ArrayList<ResultExt>();
//			}
			Similarity sim = null;
			try {
				sim = new Similarity(candidateToStructureTemp, true, false);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TanimotoClusterer tanimoto = new TanimotoClusterer(sim.getSimilarityMatrix(), sim.getCandidateToPosition());
			List<SimilarityGroup> clusteredCpds = tanimoto.clusterCandididates(candidateGroup, 0.95f);
			List<SimilarityGroup> groupedCandidates = tanimoto.getCleanedClusters(clusteredCpds);
			
			//List<SimilarityGroup> groupedCandidates = sim.getTanimotoDistanceList(candidateGroup);
			List<String> groups = new ArrayList<String>();
			for (SimilarityGroup similarityGroup : groupedCandidates) {				
				List<SimilarityCompound> tempSimilar = similarityGroup.getSimilarCompounds();
				//List<Float> tempSimilarTanimoto = similarityGroup.getSimilarCompoundsTanimoto();

				for (int k = 0; k < tempSimilar.size(); k++) {
					groups.add(tempSimilar.get(k).getCompoundID());
				}	
				rankTanimotoGroup++;
			}
			for (String string : groups) {
				//DAS IST ENDERGEBNISTABELLE
				System.out.println(string + "\t" + rankTanimotoGroup);
				
				try {
					fw.write(string + "\t" + rankTanimotoGroup + "\n");
				} catch (IOException e) {
					System.err.println("Error while writing to file " + f);
				}
				//mapGroups.put(string, rankTanimotoGroup);
				
				/**
				 * add clusterRank
				 */
				ResultExt r = mapPositions.get(string);
				r.setClusterRank(rankTanimotoGroup);
				newRanking.add(r);
			}
		}
		
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Error flushing/closing stream to " + f);
		}
		
		
		return newRanking;
	}

	public void setGroupedCandidates(List<SimilarityGroup> groupedCandidates) {
		this.groupedCandidates = groupedCandidates;
	}

	public List<SimilarityGroup> getGroupedCandidates() {
		return groupedCandidates;
	}

	private void addToResultsList(ResultExt r, ResultExtGroupBean group) {
		group.setId(r.getId());
		group.setClusterRank(r.getClusterRank());
		group.setFlag(r.getFlag());
		group.setMol(r.getMol());
		group.setName(r.getName());
		group.setPort(r.getPort());
		group.setPosAfter(r.getPosAfter());
		group.setPosBefore(r.getPosBefore());
		group.setResultScore(r.getResultScore());
		group.setScore(r.getScore());
		group.setScoreShort(r.getScoreShort());
		group.setTiedRank(r.getTiedRank());
		group.setUrl(r.getUrl());
		group.setImagePath(r.getImagePath());
		group.setLandingURL(r.getLandingURL());
		group.setSumFormula(r.getSumFormula());
		group.setExactMass(r.getExactMass());
		group.setMatchingPeaks(r.getMatchingPeaks());
	}

	public void setClusterGroups(List<ResultExtGroup> clusterGroups) {
		this.clusterGroups = clusterGroups;
	}

	public List<ResultExtGroup> getClusterGroups() {
		return clusterGroups;
	}
	
	/**
	 * original main method
	 */
//	public static void main(String[] args) {
//		
//		if(args == null || args.length < 2)
//		{
//			System.err.println("not all parameters given");
//			System.exit(1);
//		}
//		
//		String folder = args[0];
//		String file = args[1];
//		Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
//		Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
//
//		try 
//		{
//			//use buffering, reading one line at a time
//			BufferedReader input =  new BufferedReader(new FileReader(new File(folder + file)));
//			try {
//				String line = null; 
//				boolean first = true;
//				while (( line = input.readLine()) != null){
//					if(first)
//						first = false;
//					else
//					{
//						String[] lineArr = line.split("\t");
//						
//						try
//						{
//							IAtomContainer molecule = Candidates.getCompoundLocally("pubchem", lineArr[4], "jdbc:mysql://rdbms/MetFrag", "swolf", "populusromanus", true);
//							//add hydrogens
//							
//							if(molecule == null)
//								continue;
//							
//					        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
//					        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
//					        hAdder.addImplicitHydrogens(molecule);
//					        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
//							candidateToStructure.put(lineArr[4], molecule);
//						}
//						//there is a bug in cdk??
//				        catch(IllegalArgumentException e)
//			            {
//				        	System.err.println("Error with mol");
//			            }
//						
//						
//						Double currentScore = Double.parseDouble(lineArr[6]);
//						if(realScoreMap.containsKey(currentScore))
//				        {
//				        	Vector<String> tempList = realScoreMap.get(currentScore);
//				        	tempList.add(lineArr[4]);
//				        	realScoreMap.put(currentScore, tempList);
//				        }
//				        else
//				        {
//				        	Vector<String> temp = new Vector<String>();
//				        	temp.add(lineArr[4]);
//				        	realScoreMap.put(currentScore, temp);
//				        }
//						
//					}
//				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (CDKException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			finally {
//				input.close();
//			}
//		}
//	    catch (IOException ex){
//	      ex.printStackTrace();
//		}
//		    
//		    		    
//	    Double[] keysScore = new Double[realScoreMap.keySet().size()];
//		keysScore = realScoreMap.keySet().toArray(keysScore);
//		Arrays.sort(keysScore);
//	    Similarity sim = null;
//		try {
//			sim = new Similarity(candidateToStructure, (float)0.95, true);
//		} catch (CDKException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    boolean stop = false;
//	    int rankTanimotoGroup = 0;
//	    
//	    System.out.println("\n\n\n");
//	    
//		for (int i = keysScore.length-1; i >= 0; i--) {
//			
////			System.out.println("\nScore: " + keysScore[i] + "\n");
//			
//			List<String> candidateGroup = new ArrayList<String>();
//			for (int j = 0; j < realScoreMap.get(keysScore[i]).size(); j++) {
//				candidateGroup.add(realScoreMap.get(keysScore[i]).get(j));
//			}
//
//			List<SimilarityGroup> groupedCandidates = sim.getTanimotoDistanceList(candidateGroup);
//			List<String> groups = new ArrayList<String>();
//			for (SimilarityGroup similarityGroup : groupedCandidates) {				
//				List<String> tempSimilar = similarityGroup.getSimilarCompounds();
//				//List<Float> tempSimilarTanimoto = similarityGroup.getSimilarCompoundsTanimoto();
//				
//				groups.add(similarityGroup.getCandidateTocompare());
////				System.out.print(similarityGroup.getCandidateTocompare() + ": ");
//
//				for (int k = 0; k < tempSimilar.size(); k++) {
////					System.out.print(tempSimilar.get(k) + "(" +  tempSimilarTanimoto.get(k) + ")");
////					System.out.println(tempSimilar.get(k) + "\t" + rankTanimotoGroup);
//					groups.add(tempSimilar.get(k));
//				}	
////				System.out.println("");
//				rankTanimotoGroup++;
//			}
//			for (String string : groups) {
//				//DAS IST ENDERGEBNISTABELLE
//				System.out.println(string + "\t" + rankTanimotoGroup);
//			}
//		}
//		
////		System.out.println("Tanimoto rank: " + rankTanimotoGroup);
//
//	}

}
