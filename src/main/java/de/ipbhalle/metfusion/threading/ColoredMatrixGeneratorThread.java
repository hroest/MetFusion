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
	
	public ColoredMatrixGeneratorThread(TanimotoSimilarity sim) {
		this.sim = sim;
	}

	@Override
	public void run() {
		RealMatrix rm = sim.getMatrix();
		System.out.println("tanimoto matrix is [" + rm.getRowDimension() + "x" + rm.getColumnDimension() + "]");
		setCcm(new ColorcodedMatrix(rm, sim.getPrimaries(), sim.getCandidates()));
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
}
