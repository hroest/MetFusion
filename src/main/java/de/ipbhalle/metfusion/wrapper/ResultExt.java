/**
 * created by Michael Gerlich on Jun 1, 2010
 * last modified Jun 1, 2010 - 12:58:29 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.wrapper;

import org.openscience.cdk.interfaces.IAtomContainer;

public class ResultExt extends Result {

	private static final String UP = "./images/overlay/1uparrow.png";
	private static final String DOWN = "./images/overlay/1downarrow.png";
	private static final String EQUAL = "./images/overlay/1equalarrow.png";
	
	private double resultScore;
	
	private int posBefore;
	private int posAfter;
	
	private int listRank;		// einfacher Listenrang 
	private int clusterRank;	// endgueltiger Clusterrang durch Tanimoto > 0.95
	private int tiedRank;		// positionsrang auf grund gleichen ergebnisscores
	
	private String flag = EQUAL;
	
	public ResultExt() {
		super();
	}
	
	public ResultExt(Result r) {
		super(r.getPort(), r.getId(), r.getName(), r.getScore(), r.getMol());
		setUrl(r.getUrl());
		setImagePath(r.getImagePath());
		setSumFormula(r.getSumFormula());
		setExactMass(r.getExactMass());
	}
	
	public ResultExt(String port, String id, String name, double score,
			IAtomContainer mol) {
		super(port, id, name, score, mol);
		// TODO Auto-generated constructor stub
	}

	public ResultExt(Result r, int before, int after) {
		super(r.getPort(), r.getId(), r.getName(), r.getScore(), r.getMol());
		this.setPosBefore(before);
		this.setPosAfter(after);
		setUrl(r.getUrl());
		setImagePath(r.getImagePath());
		this.setSumFormula(r.getSumFormula());
		this.setExactMass(r.getExactMass());
		flagValue();
	}
	
	public ResultExt(Result r, int tiedRank) {
		this(r, 0, 0);
		this.tiedRank = tiedRank;
		setUrl(r.getUrl());
		setImagePath(r.getImagePath());
		
	}
	
	public ResultExt(Result r, int before, int after, double resultScore) {
		this(r, before, after);
		this.resultScore = resultScore;
		this.landingURL = r.landingURL;
	}
	
	public ResultExt(Result r, int before, int after, double resultScore, 
			int listRank, int clusterRank, int tiedRank) {
		this(r, before, after);
		this.resultScore = resultScore;
		this.listRank = (listRank >= 1 ? listRank : 1);
		this.setClusterRank((clusterRank >= 1 ? clusterRank : 1));
		this.tiedRank = (tiedRank >= 1 ? tiedRank : 1);
	}
	
	public ResultExt(String port, String id, String name, double score,
			IAtomContainer mol, int before, int after) {
		super(port, id, name, score, mol);
		this.setPosBefore(before);
		this.setPosAfter(after);
		
		flagValue();
	}
	
	private void flagValue() {
		if(this.posBefore < this.posAfter) {
			this.flag = DOWN;
		}
		else if(this.posBefore == this.posAfter) {
			this.flag = EQUAL;
		}
		else {
			this.flag = UP;
		}
	}

	public void setPosBefore(int posBefore) {
		this.posBefore = posBefore;
	}

	public int getPosBefore() {
		return posBefore;
	}

	public void setPosAfter(int posAfter) {
		this.posAfter = posAfter;
	}

	public int getPosAfter() {
		return posAfter;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getFlag() {
		return flag;
	}

	public void setResultScore(double resultScore) {
		this.resultScore = resultScore;
	}

	public double getResultScore() {
		return resultScore;
	}

	public void setClusterRank(int clusterRank) {
		this.clusterRank = clusterRank;
	}

	public int getClusterRank() {
		return clusterRank;
	}
}
