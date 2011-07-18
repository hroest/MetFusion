/**
 * created by Michael Gerlich on Jun 17, 2010
 * last modified Jun 17, 2010 - 3:39:29 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration.Tanimoto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math.linear.RealMatrix;

import de.ipbhalle.metfusion.integration.Integration;
import de.ipbhalle.metfusion.integration.MatrixUtils;
import de.ipbhalle.metfusion.integration.Similarity.ISimilarity;
import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;
import de.ipbhalle.metfusion.wrapper.ScoreRankPair;

public class TanimotoIntegrationWeighted extends Integration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ISimilarity similarity;
	
	private Queue<ScoreRankPair> resultRanking;
	
	private double[] resultScores;
	private String cid;
	private final double alpha = 0.3;	//old: 0.1694915d;
	private final double beta = -9;
	private final double gamma = 0.6;
		
	//private final double power = 3.686441d;
	
	
	public TanimotoIntegrationWeighted(ISimilarity similarity) {
		super(similarity.getMatrix(), similarity.getRowScores(), similarity.getColScores(), similarity.getThreshold(), similarity.getNumHits());
		this.similarity = similarity;
		this.originalOrder = similarity.getCandidates();
		super.setThread(new Thread(this, "TanimotoIntegrationWeighted"));
		computeTiedRanks();
	}
	
	public TanimotoIntegrationWeighted(ISimilarity similarity, int numHits, float threshold) {
		this(similarity);
		this.numHits = ((numHits > 0 && numHits < similarity.getMatrix().getColumnDimension() 
				&& numHits < similarity.getMatrix().getRowDimension()) ? numHits : 1);
		super.setThresh(threshold);
		
		super.setThread(new Thread(this, "TanimotoIntegrationWeighted"));
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
			//newCandidates.add(new Result(candidates.get(srp.getIndex()), srp.getRank()));
			Result r = candidates.get(srp.getIndex());
			r.setTiedRank(srp.getRank());
			newCandidates.add(r);
		}
		
		for (int i = 0; i < primaries.size(); i++) {
			ScoreRankPair srp = primQueue.poll();
			//newPrimaries.add(new Result(primaries.get(srp.getIndex()), srp.getRank()));
			Result r = primaries.get(srp.getIndex());
			r.setTiedRank(srp.getRank());
			newPrimaries.add(r);
		}
		
		similarity.setCandidates(newCandidates);
		similarity.setPrimaries(newPrimaries);
	}
	
	@Override
	public List<ResultExt> computeNewOrdering() {
		int[] rank_new = weightedApproach();
		List<ResultExt> newList = new ArrayList<ResultExt>(rank_new.length);
		
		List<Result> candidates = similarity.getCandidates();
		// add Results in correct order into list
		for (int i = 0; i < rank_new.length; i++) {
			//newList.add(mapping.get(i));
			//newList.add(new ResultExt(similarity.getCandidates().get((rank_new[i] - 1)), (rank_new[i] - 1), i, resultScores[(rank_new[i] - 1)]));
			ScoreRankPair srp = resultRanking.poll();
			Result r = candidates.get(srp.getIndex());
			//System.out.println("r.url -> " + r.getUrl() + "  r.image -> " + r.getImagePath());
			newList.add(new ResultExt(r, r.getTiedRank(), srp.getRank(), resultScores[srp.getIndex()]));
		}
		
		this.resultingOrder = newList;
		
		return newList;
	}

	@Override
	public void run() {
		System.out.println("start weighted integration via run()");
		List<ResultExt> newOrder = computeNewOrdering();
		setResultingOrder(newOrder);
		System.out.println("finished weighted integration via run()");
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
	public List<Result> computeNewOrderingFromIndices(int[] indices) {
		List<Result> newList = new ArrayList<Result>(indices.length);
		
		for (int i = 0; i < indices.length; i++) {
			//newList.add(mapping.get(i));
			//newList.add(new ResultExt(similarity.getCandidates().get((indices[i] - 1)), (indices[i] - 1), i, resultScores[(indices[i] - 1)]));
			ScoreRankPair srp = resultRanking.poll();
			newList.add(new ResultExt(similarity.getCandidates().get(srp.getIndex()), srp.getRank(), srp.getRank(), resultScores[srp.getIndex()]));
		}
		
		//this.resultingOrder = newList;
		return newList;
	}

	/**
	 * Sigmoid function used to compute the result score for each candidate.
	 * 
	 * @param database - the score from the current spectral database entry
	 * @param similarity - the similarity score from the current entry of candidate and database
	 * @return the computed score with respect of the sigmoid function described by <code>beta</code>
	 * and <code>gamma</code>.
	 */
	private double sigmoid(double database, double similarity) {
		double result = 0d;
		double product = database * similarity;
		result = 1 / (1 + Math.exp(beta * (product - gamma)));
		return result;
	}
	
	public int[] weightedApproach() {
		double[] rowScores = similarity.getRowScores();
		double[] colScores = similarity.getColScores();
		RealMatrix tanimoto = similarity.getMatrix();
		
//		int[] tied_ranks_orig = MatrixUtils.rank(rowScores, MatrixUtils.Order.DESCENDING);
		double[] weights = new double[rowScores.length];
		
		for (int i = 0; i < weights.length; i++) {
			double sum = 0;
			double[] row = tanimoto.getRow(i);
			for (int j = 0; j < row.length; j++) {	//w_i <- apply(mat, 1, function(x) {sum(colScores * x)})	
				//sum += colScores[j] * row[j];		//# Summe ueber alle Produkte MassBank_score * tanimoto_score
				/**
				 * TODO: modifizierte Formel
				 */
				sum += sigmoid(colScores[j], row[j]);
					
				//old: sum += Math.pow(colScores[j], power) * row[j];		//# Summe ueber alle Produkte MassBank_score^2 * tanimoto_score
			}
			/**
			 * TODO: modifizierte Formel
			 */
			//weights[i] = sum * rowScores[i];		// Summe * MetFrag score
			weights[i] = alpha*rowScores[i] + (1-alpha)*sum;		// Summe + MetFrag score
		}
		
		// set integration scores to field for further usage
		this.setResultScores(weights);
		resultRanking = MatrixUtils.ranking(weights);
		
//		Map<Double, Integer> pos = new HashMap<Double, Integer>();
//		int[] tied_ranks = MatrixUtils.rank(weights, MatrixUtils.Order.DESCENDING);
//		int[] tied_ranks_delta = null;
//		if(tied_ranks_orig.length == tied_ranks.length) {
//			tied_ranks_delta = new int[tied_ranks.length];
//			for (int i = 0; i < tied_ranks.length; i++) {
//				int tr = MatrixUtils.getTiedRank(weights, weights[i]);
//				if(originalOrder.get(i).getId().equals(this.cid)) {	// tied rank rausschreiben
//					try {
//						File log = new File("/home/mgerlich/evaluation/tiedranks_weight.txt");
//						FileWriter tied = new FileWriter(log, true);
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
//						e.printStackTrace();
//						System.err.println("Error creating FileWriter for " + log.getAbsolutePath());
//					}
//				}
//				
//				System.out.println("rowScore tied_rank_orig = " + tied_ranks_orig[i] + "\tresult = " + weights[i] + "\tresult tied_rank = " + tr);
//				pos.put(weights[i], tr);
//				//tied_ranks_delta[i] = tied_ranks_orig[i] - tied_ranks[i];
//				tied_ranks_delta[i] = tied_ranks_orig[i] - tr;
//				System.out.println("\ttied_rank_delta = " + tied_ranks_delta[i]);
//			}
//		}
//		System.out.println("length orig = " + tied_ranks_orig.length + "  length new = " + tied_ranks.length);
		
		// new ranking for tanimoto matrix
		int[] rank_new = MatrixUtils.order(weights, MatrixUtils.Order.DESCENDING);
		
		//Queue<ScoreRankPair> orig = MatrixUtils.ranking(rowScores);
		//Queue<ScoreRankPair> resultRank = MatrixUtils.ranking(weights);
		
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

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getCid() {
		return cid;
	}

	public void setResultRanking(Queue<ScoreRankPair> resultRanking) {
		this.resultRanking = resultRanking;
	}

	public Queue<ScoreRankPair> getResultRanking() {
		return resultRanking;
	}

}
