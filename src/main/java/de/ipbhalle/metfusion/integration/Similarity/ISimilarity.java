/**
 * created by Michael Gerlich on May 26, 2010
 * last modified May 26, 2010 - 2:42:39 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration.Similarity;

import java.io.File;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;

import de.ipbhalle.metfusion.wrapper.Result;

public interface ISimilarity {

	/**
	 * standard gradient from red to green in 21 steps
	 */
	public String[] gradient = {"#FF0000", "#FF1900", "#FF3300", "#FF4C00", "#FF6600", "#FF7F00", "#FF9900", "#FFB200", "#FFCC00", "#FFE500",
		"#FFFF00", "#E5FF00", "#CCFF00", "#B2FF00", "#99FF00", "#7FFF00", "#66FF00", "#4CFF00", "#32FF00", "#19FF00", "#00FF00"};
	
	public float thresh = 0.95f;

	/**
	 * create a comparison matrix for similarity testing according to an
	 * two-dimensional array of doubles
	 * 
	 * @param data - the two-dimensional double array
	 * @return a RealMatrix interface, providing more convinience methods
	 */
	RealMatrix createComparisonMatrix(Double[][] data);
	
	/**
	 * persistenly write a matrix into a text file on filesystem
	 * 
	 * @param matrix - the matrix to be written
	 * @param file - the file to be written to
	 * @return a boolean indicating if writing was successful (true) or not (false)
	 */
	boolean writeMatrix(RealMatrix matrix, File file);
	
	/**
	 * create a colorized image from a matrix depending on the cell values
	 * 
	 * @param matrix - to create an image for this matrix 
	 * @return a boolean indicating if creation was successful (true) or not (false)
	 */
	boolean createImageFromMatrix(RealMatrix matrix);
	
	void printMatrix();
	
	public double[] generateListScores(List<Result> list);
	
	public RealMatrix getMatrix();
	public void setMatrix(RealMatrix matrix);
	
	public double[][] getData();
	public void setData(double[][] data);
	
	public List<Result> getPrimaries();
	public void setPrimaries(List<Result> primaries);
	
	public List<Result> getCandidates();
	public void setCandidates(List<Result> candidates);
	
	public double[] getRowScores();
	public void setRowScores(double[] scores);
	
	public double[] getColScores();
	public void setColScores(double[] scores);
	
	public float getThreshold();
	public void setThreshold(float threshold);
	
	public int getNumHits();
	public void setNumHits(int numHits);
}
