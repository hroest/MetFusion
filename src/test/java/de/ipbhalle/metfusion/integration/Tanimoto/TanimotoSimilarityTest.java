/**
 * created by Michael Gerlich on May 28, 2010
 * last modified May 28, 2010 - 10:57:57 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration.Tanimoto;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Before;
import org.junit.Test;

public class TanimotoSimilarityTest {

	RealMatrix tan;
	double[] colScores;
	double[] rowScores;
	int numHits;
	float thresh;
	
	@Before
	public void setUp() throws NumberFormatException, IOException {
		File temp = new File("/home/mgerlich/Documents/testMat.csv");
		BufferedReader bufRdr = null;
		bufRdr = new BufferedReader(new FileReader(temp));
		String line = null;
		int row = 0;
		int col = 0;

		List<double[]> list = new ArrayList<double[]>();
		
		// read each line of text file
		while ((line = bufRdr.readLine()) != null) {
			String[] split = line.split(",");
			int num = split.length;
			double[] values = new double[num];
			for (int i = 0; i < split.length; i++) {
				values[col] = Double.parseDouble(split[i]);
				col++;
			}
			col = 0;
			list.add(values);
			row++;
		}
		// close the file
		bufRdr.close();
		
		double[][] data = new double[row][list.get(0).length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = list.get(i)[j];
			}
		}
		
		tan = new BlockRealMatrix(data);
		double[] colScores = { 0.975d, 0.965d, 0.956d, 0.916d, 0.599d, 0.520d, 0.502d, 0.468d, 0.418d,
							   0.413d, 0.404d, 0.385d, 0.373d, 0.365d, 0.354d, 0.352d, 0.345d, 0.328d, 0.314d };
		this.colScores = colScores;
//		String[] colNames =  { "C00509", "C06561", "C09099", "C09789", "C03406", "C04577", "C00158", "C10107",
//							   "C00311", "-----", "-----", "-----", "-----", "-----", "-----", "-----", "-----", "-----", "-----"};
		double[] rowScores = { 1.000d, 1.000d, 0.966d, 0.966d, 0.966d, 0.909d, 0.462d, 0.462d, 0.443d,
							   0.426d, 0.426d, 0.409d, 0.350d, 0.133d, 0.110d};
		this.rowScores = rowScores;
//		String[] rowNames = { "C00509", "C16232", "C06561", "C12087", "C14458", "C09826", "C03567",
//							  "C09614", "C09751", "C09047", "C17673", "C15567", "C01263", "C01592", "C08578" };
		numHits = 3;
		thresh = 0.5f;
	}
	
	@Test
	public void testTanimotoSimilarity() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGenerateListScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateComparisonMatrix() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateImageFromMatrix() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testWriteMatrix() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testPrintMatrix() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetData() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetData() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetPrimaries() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetPrimaries() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetCandidates() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetCandidates() {
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
	public void testGetColScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetRowScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetColScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetRowScores() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetThreshold() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetThreshold() {
		fail("Not yet implemented"); // TODO
	}

}
