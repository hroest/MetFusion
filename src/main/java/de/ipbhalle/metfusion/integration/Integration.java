/**
 * created by Michael Gerlich on May 26, 2010
 * last modified May 26, 2010 - 3:07:10 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;

import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.wrapper.ResultExt;

public class Integration implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int defaultNumHits = 3;
	public static final float defaultThresh = 0.5f;
	
	protected RealMatrix matrix;
	protected double[] rowScores;
	protected double[] colScores;
	
	protected float thresh;
	protected int numHits;
	
	protected List<Result> originalOrder;
	protected List<ResultExt> resultingOrder;
	
	protected Thread thread;
	
	public Integration() {
		this.thresh = defaultThresh;
		this.numHits = defaultNumHits;
		
		this.thread = new Thread(this, "Integration");
	}
	
	public Integration(RealMatrix matrix, double[] rowScores, double[] colScores, float threshold, int numHits) {
		this.matrix = matrix;
		this.rowScores = rowScores;
		this.colScores = colScores;
		this.thresh = (((threshold > 0.0f) && (threshold <= 1.0f)) ? threshold : 0.5f);
		this.numHits = numHits;
		
		this.thread = new Thread(this, "Integration");
	}
	
	@Override
	public void run() {
		List<ResultExt> newOrder = computeNewOrdering();
		setResultingOrder(newOrder);
	}
	
	public void start() {
		thread.start();
	}
		
	public List<ResultExt> computeNewOrdering() {
		
		return resultingOrder;
	}
	
	public boolean writeResult(File f) {
		//if(f.canWrite()) {
			try {
				FileWriter fw = new FileWriter(f);
				for (ResultExt r : resultingOrder) {
					fw.write(r.getId() + "\t" + r.getScore() + "\t" + r.getResultScore() + "\t" + r.getPosBefore() + "\t" + r.getPosAfter() + "\n");
				}
				fw.flush();
				fw.close();
				
				return true;
			} catch (IOException e) {
				System.err.println("Error writing new ordering to " + f);
				return false;
			}
		//}
		//else return false;
	}
	
	public void persistentWrite(String filePath) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(filePath);
			
			ObjectOutput oo = new ObjectOutputStream(os);
			oo.writeObject(this);
			oo.close();
			
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error writing file " + filePath + " to disk! - File not found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error writing file " + filePath + " to disk! - IO exception occured.");
			e.printStackTrace();
		}
		
	}
	
	public Integration persistentRead(String filePath) {
		InputStream is = null;
		try {
			is = new FileInputStream(filePath);
			ObjectInput oi = new ObjectInputStream(is);
			Integration newObj = (Integration) oi.readObject();
			oi.close();
			is.close();
			return newObj;
		} catch (FileNotFoundException e) {
			System.err.println("File not found " + filePath);
			e.printStackTrace();
			return new Integration();
		} catch (IOException e) {
			System.err.println("IO Exception occured for file " + filePath);
			e.printStackTrace();
			return new Integration();
		} catch (ClassNotFoundException e) {
			System.err.println("Error - class not found! Creating dummy object for file " + filePath);
			e.printStackTrace();
			return new Integration();
		}
	}
	
	public void setMatrix(RealMatrix matrix) {
		this.matrix = matrix;
	}

	public RealMatrix getMatrix() {
		return matrix;
	}

	public double[] getRowScores() {
		return rowScores;
	}

	public double[] getColScores() {
		return colScores;
	}

	public float getThresh() {
		return thresh;
	}

	public int getNumHits() {
		return numHits;
	}

	public void setRowScores(double[] rowScores) {
		this.rowScores = rowScores;
	}

	public void setColScores(double[] colScores) {
		this.colScores = colScores;
	}

	public void setThresh(float thresh) {
		this.thresh = thresh;
	}

	public void setNumHits(int numHits) {
		this.numHits = numHits;
	}

	public void setOriginalOrder(List<Result> originalOrder) {
		this.originalOrder = originalOrder;
	}

	public List<Result> getOriginalOrder() {
		return originalOrder;
	}

	public void setResultingOrder(List<ResultExt> resultingOrder) {
		this.resultingOrder = resultingOrder;
	}

	public List<ResultExt> getResultingOrder() {
		return resultingOrder;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}
	
	public Thread getThread() {
		return this.thread;
	}
}
