/**
 * created by Michael Gerlich on May 26, 2010
 * last modified May 26, 2010 - 3:09:13 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration.Tanimoto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.ranking.NaNStrategy;
import org.apache.commons.math.stat.ranking.NaturalRanking;
import org.apache.commons.math.stat.ranking.TiesStrategy;

import de.ipbhalle.metfusion.integration.Integration;
import de.ipbhalle.metfusion.integration.MatrixUtils;
import de.ipbhalle.metfusion.integration.Similarity.ISimilarity;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.wrapper.ScoreRankPair;


public class TanimotoIntegration extends Integration implements Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ISimilarity similarity;
	
	private double[] resultScores;
	private double[] resultScoresWeighted;
	
	// speichere auf jeder Position den Rank >= 1 und die liste aller Ergebnisse die den gleichen score haben
	private Map<Integer, List<ResultExt>> rankResults;
	
	private Queue<ScoreRankPair> resultRanking;
	
	private String cid;
	
	public TanimotoIntegration(ISimilarity similarity) {
		super(similarity.getMatrix(), similarity.getRowScores(), similarity.getColScores(), similarity.getThreshold(), similarity.getNumHits());
		this.similarity = similarity;
		this.originalOrder = similarity.getCandidates();
		super.setThread(new Thread(this, "TanimotoIntegration"));
		computeTiedRanks();
	}
	
	public TanimotoIntegration(ISimilarity similarity, int numHits, float threshold) {
		this(similarity);
		this.numHits = ((numHits > 0 && numHits < similarity.getMatrix().getColumnDimension() 
				&& numHits < similarity.getMatrix().getRowDimension()) ? numHits : 1);
		super.setThresh(threshold);
		
		super.setThread(new Thread(this, "TanimotoIntegration"));
		computeTiedRanks();
	}
	
	private void computeTiedRanks() {
		List<Result> candidates = similarity.getCandidates();
		List<Result> primaries = similarity.getPrimaries();
		
		double[] candScores = new double[candidates.size()];
		for (int i = 0; i < candScores.length; i++) {
			candScores[i] = candidates.get(i).getScore();
		}
		
		double[] primScores = new double[primaries.size()];
		for (int i = 0; i < primScores.length; i++) {
			primScores[i] = primaries.get(i).getScore();
		}
		
		Queue<ScoreRankPair> candQueue = MatrixUtils.ranking(candScores);
		Queue<ScoreRankPair> primQueue = MatrixUtils.ranking(primScores);
		List<Result> newCandidates = new ArrayList<Result>();
		List<Result> newPrimaries = new ArrayList<Result>();
		
		for (int i = 0; i < candidates.size(); i++) {
			ScoreRankPair srp = candQueue.poll();
			newCandidates.add(new Result(candidates.get(srp.getIndex()), srp.getRank()));
		}
		
		for (int i = 0; i < primaries.size(); i++) {
			ScoreRankPair srp = primQueue.poll();
			newPrimaries.add(new Result(primaries.get(srp.getIndex()), srp.getRank()));
		}
		
		similarity.setCandidates(newCandidates);
		similarity.setPrimaries(newPrimaries);
	}
	
	public static void main(String[] args) {
		//String matrixFile = "src/test/resources/testMat.csv";
		String matrixFile = "src/test/resources/mat.txt";
		TanimotoIntegration test;
		TanimotoSimilarity sim = null;

		int numHits = 3;
		float thresh = 0.5f;
		
		
//		double[] colScores = { 0.975d, 0.965d, 0.956d, 0.916d, 0.599d, 0.520d,
//				0.502d, 0.468d, 0.418d, 0.413d, 0.404d, 0.385d, 0.373d, 0.365d,
//				0.354d, 0.352d, 0.345d, 0.328d, 0.314d };
//		String[] colNames = { "C00509", "C06561", "C09099", "C09789", "C03406",
//				"C04577", "C00158", "C10107", "C00311", "-----", "-----",
//				"-----", "-----", "-----", "-----", "-----", "-----", "-----",
//				"-----" };
//		double[] rowScores = { 1.000d, 1.000d, 0.966d, 0.966d, 0.966d, 0.909d,
//				0.462d, 0.462d, 0.443d, 0.426d, 0.426d, 0.409d, 0.350d, 0.133d,
//				0.110d };
//		String[] rowNames = { "C00509", "C16232", "C06561", "C12087", "C14458",
//				"C09826", "C03567", "C09614", "C09751", "C09047", "C17673",
//				"C15567", "C01263", "C01592", "C08578" };
		
		File mat = new File(matrixFile);
		try {
			sim = new TanimotoSimilarity(mat);
			double[] colScores = sim.readVector(new File("src/test/resources/colScores.txt"));
			double[] rowScores = sim.readVector(new File("src/test/resources/rowScores.txt"));
			
			List<Result> resultMassBank = new ArrayList<Result>(rowScores.length);
			List<Result> resultMetFrag = new ArrayList<Result>(colScores.length);
			
			for (int i = 0; i < rowScores.length; i++) {
				resultMetFrag.add(new Result("MetFrag", String.valueOf(i), String.valueOf(i), rowScores[i], null));
			}
			
			for (int i = 0; i < colScores.length; i++) {
				resultMassBank.add(new Result("MassBank", String.valueOf(i), String.valueOf(i), colScores[i], null));
			}
			
			sim.setCandidates(resultMetFrag);
			sim.setPrimaries(resultMassBank);
			sim.setColScores(colScores);
			sim.setRowScores(rowScores);
			sim.setThreshold(thresh);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		RealMatrix tan = sim.getMatrix();
		System.out.println("Tanimoto matrix with " + tan.getRowDimension() + " rows and " + tan.getColumnDimension() + " columns.");
		test = new TanimotoIntegration(sim);
		test.setNumHits(numHits);
		
		List<List<ResultExt>> resultLists = new ArrayList<List<ResultExt>>();
		resultLists.add(test.computeNewOrdering());
		
		resultLists.add(test.computeNewOrderingFromIndices(test.weightedApproach()));
	}
	
	@Override
	public List<ResultExt> computeNewOrdering() {
		int[] rank_new = thresholdApproach();
		//int[] rank_new = newApproach();
		List<ResultExt> newList = new ArrayList<ResultExt>(rank_new.length);
		
//		Map<Integer, ResultExt> mapping = new HashMap<Integer, ResultExt>();

		// map indices to Results
//		for (int i = 0; i < rank_new.length; i++) {
//			//mapping.put((rank_new[i] - 1), similarity.getCandidates().get(i));
//			//mapping.put((rank_new[i] - 1), new ResultExt(similarity.getCandidates().get(i), i, (rank_new[i] - 1), resultScores[i]));
//			mapping.put(i, new ResultExt(similarity.getCandidates().get((rank_new[i] - 1)), (rank_new[i] - 1), i, resultScores[(rank_new[i] - 1)]));
//		}
		
		List<Result> candidates = similarity.getCandidates();
		// add Results in correct order into list
		for (int i = 0; i < rank_new.length; i++) {
			//newList.add(mapping.get(i));
			//newList.add(new ResultExt(similarity.getCandidates().get((rank_new[i] - 1)), (rank_new[i] - 1), i, resultScores[(rank_new[i] - 1)]));
			ScoreRankPair srp = resultRanking.poll();
			Result r = candidates.get(srp.getIndex());
			newList.add(new ResultExt(r, r.getTiedRank(), srp.getRank(), resultScores[srp.getIndex()], r.getMatchingPeaks()));
		}
		
		this.resultingOrder = newList;
		
		return newList;
	}

	@Override
	public void run() {
		System.out.println("start threshold integration via run()");
		List<ResultExt> newOrder = computeNewOrdering();
		setResultingOrder(newOrder);
		System.out.println("finished threshold integration via run()");
	}
	
	@Override
	public void start() {
		thread.start();
	}
	
	/**
	 * use weighted scores for ranking !!!
	 * @param indices
	 * @return
	 */
	@Deprecated
	public List<ResultExt> computeNewOrderingFromIndices(int[] indices) {
		List<ResultExt> newList = new ArrayList<ResultExt>(indices.length);
		
		for (int i = 0; i < indices.length; i++) {
			//newList.add(mapping.get(i));
			newList.add(new ResultExt(similarity.getCandidates().get((indices[i] - 1)), (indices[i] - 1), i, 
					resultScoresWeighted[(indices[i] - 1)], similarity.getCandidates().get((indices[i] - 1)).getMatchingPeaks()));
		}
		
		//this.resultingOrder = newList;
		return newList;
	}
	
	@Deprecated
	public int[] newApproach() {
		double[] rowScores = similarity.getRowScores();
		double[] colScores = similarity.getColScores();
		RealMatrix tanimoto = similarity.getMatrix();
		float thresh = similarity.getThreshold();
		
		// perform R-like which()
		int[] idx = MatrixUtils.which(colScores, thresh, true, MatrixUtils.Ways.GREATER);
		double[] result = new double[tanimoto.getRowDimension()];
		double idx_score = 0;
		Map<Double, Integer> pos = new HashMap<Double, Integer>();
		
		if(idx.length > 0 && idx.length < numHits) {	// found indices > thresh, but less than numHits
			for (int i = 0; i < tanimoto.getRowDimension(); i++) {
				double[] row = tanimoto.getRow(i);
				for (int j : idx) {
					result[i] += row[j];
				}
			}
			idx_score = thresh * numHits;
		}
		else if(idx.length == 0 || idx == null) {	// found no indices
			/**
			 * do nothing 
			 */
			int[] rank_new = new int[result.length];
			for (int i = 0; i < rank_new.length; i++) {
				rank_new[i] = i+1;	// set ranks starting with 1
			}
			setResultScores(rowScores);	// set unmodified row scores as result scores because nothing changed
			return rank_new;
		}
		else {			// found more indices than numHits
			
			for (int i = 0; i < tanimoto.getRowDimension(); i++) {
				double[] row = tanimoto.getRow(i);
				double[] idx_vals = new double[idx.length];
				List<Double> lv = new ArrayList<Double>();
				int counter = 0;
				
				for (int j : idx) {
					idx_vals[counter] = row[j];
					lv.add(counter, row[j]);
					counter++;
				}
				
				//Arrays.sort(idx_vals);	
				Collections.sort(lv);		// equals R order(x[idx]) - ascending!!!
				Collections.reverse(lv);	// descending
				int[] idx_best = new int[idx.length];
				
				for (int j = 0; j < idx_best.length; j++) {
					double tan = lv.remove(0);
					for (int k = 0; k < row.length; k++) {
						if(tan == row[k]) {
							idx_best[j] = k;
							break;
						}
					}
				}
				for (int j = 0; j < numHits; j++) {
					result[i] += row[idx_best[j]];
				}
				pos.put(result[i], i);
				System.out.println("result["+i+"] = " + result[i]);
			}
			idx_score = thresh * numHits;
		}
		System.out.println("idx_score -> " + idx_score);
		// indices of rows with maximum sum of scores
		//int[] idx_max_result = MatrixUtils.whichMax(result);
		
		// set integration scores to field for further usage
		this.setResultScores(result);
		
		// Compute statistics directly from the array
		// assume values is a double[] array
		double mean = StatUtils.mean(result);
		double std = StatUtils.variance(result);
		double median = StatUtils.percentile(result, 50);
		double max = StatUtils.max(result);
		System.out.println("result mean:" + mean + "\tstd:" + std + "\tmedian:" + median + "\tmax:" + max);
		
		double max_result = MatrixUtils.max(result);
		System.out.println("max result -> " + max_result);
		
		// tied ranking of new scores
		NaturalRanking ranking = new NaturalRanking(NaNStrategy.REMOVED, TiesStrategy.MAXIMUM);
		double[] tied_ranks_orig = ranking.rank(rowScores); 
		double[] tied_ranks = ranking.rank(result);
		double[] tied_ranks_delta = null; //= ranking.rank(rowScores);
		
		if(tied_ranks_orig.length == tied_ranks.length) {
			tied_ranks_delta = new double[tied_ranks.length];
			for (int i = 0; i < tied_ranks.length; i++) {
				int tr = MatrixUtils.getTiedRank(result, result[i]);
				
				if(originalOrder.get(i).getId().equals(this.cid)) {	// tied rank rausschreiben
					try {
						File log = new File("/home/mgerlich/evaluation/tiedranks_thresh.txt");
						FileWriter tied = new FileWriter(log);
						if(!log.exists()) {		// neu erstellen und schreiben
							tied.write(cid + "\t" + tr + "\n");
							tied.flush();
							tied.close();
						}
						else {		// append
							tied.append(cid + "\t" + tr + "\n");
							tied.flush();
							tied.close();
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				System.out.println("rowScore tied_rank_orig = " + tied_ranks_orig[i] + "\tresult = " + result[i] + "\tresult tied_rank = " + tr);
				pos.put(result[i], tr);
				//tied_ranks_delta[i] = tied_ranks_orig[i] - tied_ranks[i];
				tied_ranks_delta[i] = tied_ranks_orig[i] - tr;
				System.out.println("\ttied_rank_delta = " + tied_ranks_delta[i]);
			}
		}
		System.out.println("length orig = " + tied_ranks_orig.length + "  length new = " + tied_ranks.length);
		
		// new ranking for tanimoto matrix
		int[] rank_new = MatrixUtils.order(result, MatrixUtils.Order.DESCENDING);
		return rank_new;
	}
	
	public int[] thresholdApproach() {
		double[] rowScores = similarity.getRowScores();
		double[] colScores = similarity.getColScores();
		RealMatrix tanimoto = similarity.getMatrix();
		float thresh = similarity.getThreshold();
		
		// perform R-like which()
		int[] idx = MatrixUtils.which(colScores, thresh, true, MatrixUtils.Ways.GREATER);
		double[] result = new double[tanimoto.getRowDimension()];
		double idx_score = 0;
		Map<Double, Integer> pos = new HashMap<Double, Integer>();
		
		if(idx.length > 0 && idx.length < numHits) {	// found indices > thresh, but less than numHits
			for (int i = 0; i < tanimoto.getRowDimension(); i++) {
				double[] row = tanimoto.getRow(i);
				for (int j : idx) {
					result[i] += row[j];
				}
			}
			idx_score = thresh * numHits;
		}
		else if(idx.length == 0 || idx == null) {	// found no indices
//			for (int i = 0; i < result.length; i++) {
//				result[i] = 0;
//			}
//			idx_score = 10^6;
			/**
			 * do nothing 
			 */
			int[] rank_new = new int[result.length];
			for (int i = 0; i < rank_new.length; i++) {
				rank_new[i] = i+1;	// set ranks starting with 1
			}
			setResultScores(rowScores);	// set unmodified row scores as result scores because nothing changed
			resultRanking = MatrixUtils.ranking(rowScores);
			return rank_new;
		}
		else {			// found more indices than numHits
			
			for (int i = 0; i < tanimoto.getRowDimension(); i++) {
				double[] row = tanimoto.getRow(i);
				double[] idx_vals = new double[idx.length];
				List<Double> lv = new ArrayList<Double>();
				int counter = 0;
				
				for (int j : idx) {
					idx_vals[counter] = row[j];
					lv.add(counter, row[j]);
					counter++;
				}
				
				//Arrays.sort(idx_vals);	
				Collections.sort(lv);		// equals R order(x[idx]) - ascending!!!
				Collections.reverse(lv);	// descending
				int[] idx_best = new int[idx.length];
				
				for (int j = 0; j < idx_best.length; j++) {
					double tan = lv.remove(0);
					for (int k = 0; k < row.length; k++) {
						if(tan == row[k]) {
							idx_best[j] = k;
							break;
						}
					}
				}
				for (int j = 0; j < numHits; j++) {
					result[i] += row[idx_best[j]];
				}
				pos.put(result[i], i);
				System.out.println("result["+i+"] = " + result[i]);
			}
			idx_score = thresh * numHits;
		}
		System.out.println("idx_score -> " + idx_score);
		// indices of rows with maximum sum of scores
		//int[] idx_max_result = MatrixUtils.whichMax(result);
		
		double max_result = MatrixUtils.max(result);
		System.out.println("max result -> " + max_result);
		
		/**
		 * only change ordering if max result score is larger than idx_score
		 */
		if(!(max_result > idx_score)) {
			System.out.println("no rank change due to max_result < idx_score!");
			System.out.println("max_result -> " + max_result + "\tidx_score -> " + idx_score);
			int[] rank_new = new int[result.length];
			for (int i = 0; i < rank_new.length; i++) {
				rank_new[i] = i+1;	// set ranks starting with 1
			}
			setResultScores(rowScores);	// set unmodified row scores as result scores because nothing changed
			resultRanking = MatrixUtils.ranking(rowScores);
			return rank_new;
		}
		
		// set integration scores to field for further usage
		this.setResultScores(result);
		resultRanking = MatrixUtils.ranking(result);
		
		/**
		 * TODO: eigene Funktionen durch Funktionen aus commons-math ersetzen
		 * DescriptiveStatistics, StatUtils und NaturalRanking
		 */
		// Compute statistics directly from the array
		// assume values is a double[] array
		double mean = StatUtils.mean(result);
		double std = StatUtils.variance(result);
		double median = StatUtils.percentile(result, 50);
		double max = StatUtils.max(result);
		System.out.println("result mean:" + mean + "\tstd:" + std + "\tmedian:" + median + "\tmax:" + max);
		
		
		// tied ranking of new scores
//		NaturalRanking ranking = new NaturalRanking(NaNStrategy.MINIMAL, TiesStrategy.MAXIMUM);
//		double[] d_tied_ranks_orig = ranking.rank(rowScores); 
//		double[] d_tied_ranks = ranking.rank(result);
//		double[] d_tied_ranks_delta = ranking.rank(rowScores);
		
		int[] tied_ranks_orig = MatrixUtils.rank(rowScores, MatrixUtils.Order.DESCENDING);
		Queue<ScoreRankPair> orig = MatrixUtils.ranking(rowScores);
		for (ScoreRankPair srp : orig) {
			System.out.println("[orig] index -> " + srp.getIndex() + "\trank -> " + srp.getRank() + "\tscore -> " + srp.getScore());
		}
		int[] tied_ranks = MatrixUtils.rank(result, MatrixUtils.Order.DESCENDING);
		Queue<ScoreRankPair> resultRank = MatrixUtils.ranking(result);
		for (ScoreRankPair srp : resultRank) {
			System.out.println("[new] index -> " + srp.getIndex() + "\trank -> " + srp.getRank() + "\tscore -> " + srp.getScore());
		}
		
		/**
		 * skip computation of delta ranks as it is only required for evaluation in R
		 */
//		int[] tied_ranks_delta = null;
//		if(tied_ranks_orig.length == tied_ranks.length) {
//			tied_ranks_delta = new int[tied_ranks.length];
//			for (int i = 0; i < tied_ranks.length; i++) {
//				int tr = MatrixUtils.getTiedRank(result, result[i]);
//				
//				if(originalOrder.get(i).getId().equals(this.cid)) {	// tied rank rausschreiben
//					try {
//						File log = new File("/home/mgerlich/evaluation/tiedranks_thresh.txt");
//						FileWriter tied = new FileWriter(log);
//						if(!log.exists()) {		// neu erstellen und schreiben
//							tied.write(cid + "\t" + tr + "\n");
//							tied.flush();
//							tied.close();
//						}
//						else {		// append
//							tied.append(cid + "\t" + tr + "\n");
//							tied.flush();
//							tied.close();
//						}
//						
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				
//				System.out.println("rowScore tied_rank_orig = " + tied_ranks_orig[i] + "\tresult = " + result[i] + "\tresult tied_rank = " + tr);
//				pos.put(result[i], tr);
//				//tied_ranks_delta[i] = tied_ranks_orig[i] - tied_ranks[i];
//				tied_ranks_delta[i] = tied_ranks_orig[i] - tr;
//				System.out.println("\ttied_rank_delta = " + tied_ranks_delta[i]);
//			}
//		}
		System.out.println("length orig = " + tied_ranks_orig.length + "  length new = " + tied_ranks.length);
		
		// new ranking for tanimoto matrix
		int[] rank_new = MatrixUtils.order(result, MatrixUtils.Order.DESCENDING);
		
//		System.out.println("rank_new");
//		for (int i = 0; i < rank_new.length; i++) {
//			System.out.print("["+i+"]=" + rank_new[i] + "  ");
//		}
		
		return rank_new;
	}
	
	@Deprecated
	public int[] weightedApproach() {
		double[] rowScores = similarity.getRowScores();
		double[] colScores = similarity.getColScores();
		RealMatrix tanimoto = similarity.getMatrix();
		
		int[] tied_ranks_orig = MatrixUtils.rank(rowScores, MatrixUtils.Order.DESCENDING);
		double[] weights = new double[rowScores.length];
		
		for (int i = 0; i < weights.length; i++) {
			double sum = 0;
			double[] row = tanimoto.getRow(i);
			for (int j = 0; j < row.length; j++) {	//w_i <- apply(mat, 1, function(x) {sum(colScores * x)})	
				sum += colScores[j] * row[j];		//# Summe ueber alle Produkte MassBank_score * tanimoto_score
			}
			weights[i] = sum * rowScores[i];		// Summe * MetFrag score
		}
		
		// set integration scores to field for further usage
		this.setResultScoresWeighted(weights);
		
		Map<Double, Integer> pos = new HashMap<Double, Integer>();
		int[] tied_ranks = MatrixUtils.rank(weights, MatrixUtils.Order.DESCENDING);
		int[] tied_ranks_delta = null;
		if(tied_ranks_orig.length == tied_ranks.length) {
			tied_ranks_delta = new int[tied_ranks.length];
			for (int i = 0; i < tied_ranks.length; i++) {
				int tr = MatrixUtils.getTiedRank(weights, weights[i]);
				System.out.println("rowScore tied_rank_orig = " + tied_ranks_orig[i] + "\tresult = " + weights[i] + "\tresult tied_rank = " + tr);
				pos.put(weights[i], tr);
				//tied_ranks_delta[i] = tied_ranks_orig[i] - tied_ranks[i];
				tied_ranks_delta[i] = tied_ranks_orig[i] - tr;
				System.out.println("\ttied_rank_delta = " + tied_ranks_delta[i]);
			}
		}
		System.out.println("length orig = " + tied_ranks_orig.length + "  length new = " + tied_ranks.length);
		
		// new ranking for tanimoto matrix
		int[] rank_new = MatrixUtils.order(weights, MatrixUtils.Order.DESCENDING);
		
//		System.out.println("rank_new");
//		for (int i = 0; i < rank_new.length; i++) {
//			System.out.print("weighted ["+i+"]=" + rank_new[i] + "  ");
//		}
		
		return rank_new;
	}

	public void setSimilarity(ISimilarity similarity) {
		this.similarity = similarity;
	}

	public ISimilarity getSimilarity() {
		return similarity;
	}

	public void setResultScores(double[] resultScores) {
		this.resultScores = resultScores;
	}

	public double[] getResultScores() {
		return resultScores;
	}

	public void setResultScoresWeighted(double[] resultScoresWeighted) {
		this.resultScoresWeighted = resultScoresWeighted;
	}

	public double[] getResultScoresWeighted() {
		return resultScoresWeighted;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getCid() {
		return cid;
	}

	public void setRankResults(Map<Integer, List<ResultExt>> rankResults) {
		this.rankResults = rankResults;
	}

	public Map<Integer, List<ResultExt>> getRankResults() {
		return rankResults;
	}

	public Queue<ScoreRankPair> getResultRanking() {
		return resultRanking;
	}

	public void setResultRanking(Queue<ScoreRankPair> resultRanking) {
		this.resultRanking = resultRanking;
	}

}
