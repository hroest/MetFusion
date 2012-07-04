/**
 * created by Michael Gerlich, Jun 18, 2012 - 4:13:51 PM
 */ 

package de.ipbhalle.metfusion.main;

import java.util.List;

public class EvalMassBankScores {

	private float hit50;
	private float hit100;
	private float avg50to100;
	private List<Float> scores;
	
	public EvalMassBankScores(List<Float> scores) {
		this.scores = scores;
		computeAverage();
	}
	
	public EvalMassBankScores(float hit50, float hit100) {
		this.hit50 = hit50;
		this.hit100 = hit100;
	}
	
	public EvalMassBankScores(float hit50, float hit100, float avg) {
		this.hit50 = hit50;
		this.hit100 = hit100;
		this.avg50to100 = avg;
	}
	
	public float computeAverage() {
		float avg = 0.0f;
		int maxPosition = 51;	// from position 50 to 100 = 51 entries
		if(scores.size() < maxPosition)
			maxPosition = scores.size();
		
		for (int i = 0; i < maxPosition; i++) {
			if(i == 0)
				hit50 = scores.get(i);
				
			if((i+1) == maxPosition)
				hit100 = scores.get(i);
				
			avg += scores.get(i);
		}
		avg = avg / (float) maxPosition;
		
		this.avg50to100 = avg;
		return avg;
	}

	public float getHit50() {
		return hit50;
	}

	public void setHit50(float hit50) {
		this.hit50 = hit50;
	}

	public float getHit100() {
		return hit100;
	}

	public void setHit100(float hit100) {
		this.hit100 = hit100;
	}

	public float getAvg50to100() {
		return avg50to100;
	}

	public void setAvg50to100(float avg50to100) {
		this.avg50to100 = avg50to100;
	}

	public List<Float> getScores() {
		return scores;
	}

	public void setScores(List<Float> scores) {
		this.scores = scores;
	}
}
