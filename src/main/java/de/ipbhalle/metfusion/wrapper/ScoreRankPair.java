package de.ipbhalle.metfusion.wrapper;

public class ScoreRankPair {

	private double score;
	private int rank;
	private int index;
	
	
	public ScoreRankPair() {
		this.setScore(0.0d);
		this.setRank(1);
		this.setIndex(0);
	}
	
	public ScoreRankPair(double score, int rank, int index) {
		this.setScore(score);
		this.setRank(rank);
		this.setIndex(index);
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
	
	
}
