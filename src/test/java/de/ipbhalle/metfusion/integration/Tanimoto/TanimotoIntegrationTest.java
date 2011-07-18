/**
 * created by Michael Gerlich on May 28, 2010
 * last modified May 28, 2010 - 1:01:25 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration.Tanimoto;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfusion.wrapper.Result;
import de.ipbhalle.metfusion.integration.Similarity.ISimilarity;

public class TanimotoIntegrationTest {

	//public static final String matrixFile = "src/test/resources/testMat.csv";
	public static final String matrixFile = "src/test/resources/mat.txt";
	TanimotoIntegration test;
	ISimilarity sim;

	RealMatrix tan;
	double[] colScores;
	double[] rowScores;
	int numHits;
	float thresh;
	
	@Before
	public void setUp() {
		double[] colScores = { 0.975d, 0.965d, 0.956d, 0.916d, 0.599d, 0.520d,
				0.502d, 0.468d, 0.418d, 0.413d, 0.404d, 0.385d, 0.373d, 0.365d,
				0.354d, 0.352d, 0.345d, 0.328d, 0.314d };
		this.colScores = colScores;
		String[] colNames = { "C00509", "C06561", "C09099", "C09789", "C03406",
				"C04577", "C00158", "C10107", "C00311", "-----", "-----",
				"-----", "-----", "-----", "-----", "-----", "-----", "-----",
				"-----" };
		double[] rowScores = { 1.000d, 1.000d, 0.966d, 0.966d, 0.966d, 0.909d,
				0.462d, 0.462d, 0.443d, 0.426d, 0.426d, 0.409d, 0.350d, 0.133d,
				0.110d };
		this.rowScores = rowScores;
		String[] rowNames = { "C00509", "C16232", "C06561", "C12087", "C14458",
				"C09826", "C03567", "C09614", "C09751", "C09047", "C17673",
				"C15567", "C01263", "C01592", "C08578" };
		numHits = 3;
		thresh = 0.5f;
		
		List<Result> resultMassBank = new ArrayList<Result>();
		List<Result> resultMetFrag = new ArrayList<Result>();
		
		for (int i = 0; i < rowNames.length; i++) {
			resultMetFrag.add(new Result("MetFrag", rowNames[i], rowNames[i], rowScores[i], null));
		}
		
		for (int i = 0; i < colNames.length; i++) {
			resultMassBank.add(new Result("MassBank", colNames[i], colNames[i], colScores[i], null));
		}
		
		File mat = new File(matrixFile);
		try {
			sim = new TanimotoSimilarity(mat);
			sim.setColScores(colScores);
			sim.setRowScores(rowScores);
			sim.setThreshold(thresh);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sim.setCandidates(resultMetFrag);
		sim.setPrimaries(resultMassBank);
		tan = sim.getMatrix();
		
		test = new TanimotoIntegration(sim);
//		test.setColScores(colScores);
//		test.setRowScores(rowScores);
//		test.setMatrix(tan);
		test.setNumHits(numHits);
//		test.setThresh(thresh);
	}

	@Test
	public void testComputeNewOrdering() {
		test.computeNewOrdering();
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testTanimotoIntegrationISimilarity() {
		test = new TanimotoIntegration(sim);
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testTanimotoIntegrationISimilarityIntFloat() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testThresholdApproach() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testWeightedApproach() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetSimilarity() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetSimilarity() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIntegration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIntegrationRealMatrixDoubleArrayDoubleArrayFloatInt() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetMatrix() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetMatrix() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetRowScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetColScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetThresh() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetNumHits() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetRowScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetColScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetThresh() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetNumHits() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetOriginalOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetOriginalOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetResultingOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetResultingOrder() {
		fail("Not yet implemented"); // TODO
	}

}
