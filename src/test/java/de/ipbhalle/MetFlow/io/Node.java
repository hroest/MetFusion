/**
 * created by Michael Gerlich on Apr 7, 2010
 * last modified Apr 7, 2010 - 2:20:26 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.io;

public class Node {

	private double score;
	private String id;
	
	public Node(String id, double score) {
		this.setScore(score);
		this.setId(id);
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
