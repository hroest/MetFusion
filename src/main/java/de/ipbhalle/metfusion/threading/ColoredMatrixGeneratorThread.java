/**
 * created by Michael Gerlich, Nov 16, 2010 - 12:48:17 PM
 */ 

package de.ipbhalle.metfusion.threading;

import org.apache.commons.math.linear.RealMatrix;

import de.ipbhalle.metfusion.integration.Tanimoto.TanimotoSimilarity;
import de.ipbhalle.metfusion.wrapper.ColorcodedMatrix;

public class ColoredMatrixGeneratorThread extends Thread {

	private TanimotoSimilarity sim;
	private ColorcodedMatrix ccm;
	private boolean done = Boolean.FALSE;
	
	public ColoredMatrixGeneratorThread(TanimotoSimilarity sim) {
		this.sim = sim;
	}

	@Override
	public void run() {
		this.done = Boolean.FALSE;
		RealMatrix rm = sim.getMatrix();
		System.out.println("tanimoto matrix is [" + rm.getRowDimension() + "x" + rm.getColumnDimension() + "]");
		setCcm(new ColorcodedMatrix(rm, sim.getPrimaries(), sim.getCandidates()));
		this.done = Boolean.TRUE;
	}
	
	public void setSim(TanimotoSimilarity sim) {
		this.sim = sim;
	}

	public TanimotoSimilarity getSim() {
		return sim;
	}

	public void setCcm(ColorcodedMatrix ccm) {
		this.ccm = ccm;
	}

	public ColorcodedMatrix getCcm() {
		return ccm;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}
}
