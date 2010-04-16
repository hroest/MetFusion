/**
 * 
 */
/**
 * created by Michael Gerlich on Apr 7, 2010
 * last modified Apr 7, 2010 - 11:11:41 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.integration;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;

import de.ipbhalle.MetFlow.io.Node;


/**
 * @author mgerlich
 *
 */
public class MatrixTest {

	static int numHits = 3;
	static float thresh = 0.5f;
	
	static double[] expectedResult = {2.35, 2.23, 1.55, 0.81, 1.57, 2.23, 1.50, 2.23,
									  2.23, 1.12, 1.00, 1.48, 1.46, 1.18, 1.50};
	
	// ranks start with 1
	static int[] expectedTiedRanksOrig = {2, 2, 5, 5, 5, 6, 8, 8, 9, 11, 11, 12, 13, 14, 15};
	static int[] expectedTiedRanks = {1, 5, 5, 5, 5, 6, 7, 9, 9, 10, 11, 12, 13, 14, 15};
	static int[] expectedTiedRanksDelta = {1, -3, -2, -10, -1, 1, -1, 3, 4, -2, -3, 2, 2, 2, 6};
	static int[] expectedRankNew = {1, 2, 6, 8, 9, 5, 3, 7, 15, 12, 13, 14, 10, 11, 4};
	
	static double expectedIdxScore = 1.5d;
	static double expectedMaxResult = 2.35d;
	
	// indices start with 0, while in R they start with 1
	static int[] expectedIdx = {0, 1, 2, 3, 4, 5, 6};	// R -> 1,2,3,4,5,6,7
	static int[] expectedIdxMaxResult = {0};	// R -> 1
	
	
	
	/**
	 * Test method for {@link org.apache.commons.math.linear.RealMatrix}.
	 */
	@Test
	public void testMatrix() {
		// Create a real matrix with two rows and three columns
		double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
		RealMatrix m = new Array2DRowRealMatrix(matrixData);

		// One more with three rows, two columns
		double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
		RealMatrix n = new Array2DRowRealMatrix(matrixData2);

		// Note: The constructor copies  the input double[][] array.

		// Now multiply m by n
		RealMatrix p = m.multiply(n);
		//System.out.println(p.getRowDimension());    // 2
		//System.out.println(p.getColumnDimension()); // 2
		assertSame(2, p.getRowDimension());
		assertSame(2, p.getColumnDimension());
		
		// Invert p, using LU decomposition
		RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
		
		//System.out.println(pInverse.getColumnDimension());
		//System.out.println(pInverse.getRowDimension());
		assertSame(2, pInverse.getRowDimension());
		assertSame(2, pInverse.getColumnDimension());
	}
	
	@Test
	public void testBigMatrix() throws IOException {
		double[][] matrixData = { {1d,0.8d,0.4d,0.2d}, {0.7d,1.0d,0.5d,0.1d}};
		RealMatrix m = new BlockRealMatrix(matrixData);
		
		//System.out.println(m.getRowDimension());    // 2
		//System.out.println(m.getColumnDimension()); // 4
		
		assertSame(2, m.getRowDimension());
		assertSame(4, m.getColumnDimension());
		
		File temp = new File("/home/mgerlich/Documents/testMat.csv");
		BufferedReader bufRdr  = new BufferedReader(new FileReader(temp));
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
		
		RealMatrix tan = new BlockRealMatrix(data);
		double[] colScores = { 0.975d, 0.965d, 0.956d, 0.916d, 0.599d, 0.520d, 0.502d, 0.468d, 0.418d,
							   0.413d, 0.404d, 0.385d, 0.373d, 0.365d, 0.354d, 0.352d, 0.345d, 0.328d, 0.314d };
		String[] colNames =  { "C00509", "C06561", "C09099", "C09789", "C03406", "C04577", "C00158", "C10107",
							   "C00311", "-----", "-----", "-----", "-----", "-----", "-----", "-----", "-----", "-----", "-----"};
		double[] rowScores = { 1.000d, 1.000d, 0.966d, 0.966d, 0.966d, 0.909d, 0.462d, 0.462d, 0.443d,
							   0.426d, 0.426d, 0.409d, 0.350d, 0.133d, 0.110d};
		String[] rowNames = { "C00509", "C16232", "C06561", "C12087", "C14458", "C09826", "C03567",
							  "C09614", "C09751", "C09047", "C17673", "C15567", "C01263", "C01592", "C08578" };
		
		Map<Integer, Node> colMap = new TreeMap<Integer, Node>();
		if(colScores.length == colNames.length) {
			for (int i = 0; i < colNames.length; i++) {
				Node n = new Node(colNames[i], colScores[i]);
				colMap.put(i, n);
			}
		}
		
		Map<Integer, Node> rowMap = new TreeMap<Integer, Node>();
		if(rowScores.length == rowNames.length) {
			for (int i = 0; i < rowNames.length; i++) {
				Node n = new Node(rowNames[i], rowScores[i]);
				rowMap.put(i, n);
			}
		}

		approachThreshold(tan, rowScores, colScores, numHits, thresh);
	}
	
	/**
	 * threshold apporach for chemicical similarity-based evaluation
	 * 
	 * @param tanimoto
	 * @param rowScores
	 * @param colScores
	 * @param numHits
	 * @param thresh
	 */
	public void approachThreshold(RealMatrix tanimoto, double[] rowScores, double[] colScores, int numHits, double thresh) {
		if(numHits < 0)
			numHits = 3;
		if(thresh < 0d || thresh > 1.0d) {
			thresh = 0.5d;
		}
		
		// perform R-like which()
		int[] idx = MatrixUtils.which(colScores, thresh, true, MatrixUtils.Ways.GREATER);
		
		// test equality of arrays
		assertArrayEquals(expectedIdx, idx);
		//
		
		double[] result = new double[tanimoto.getRowDimension()];
		double idx_score = 0;
		Map<Double, Integer> pos = new HashMap<Double, Integer>();
		
		if(idx.length > 0 && idx.length < numHits) {	// found indices > thresh, but less than numHits
			for (int i = 0; i < tanimoto.getRowDimension(); i++) {
				double[] row = tanimoto.getRow(i);
				for (int j : idx) {
					result[i] += row[j];
				}
			}
			idx_score = thresh * numHits;
		}
		else if(idx.length == 0 || idx == null) {	// found no indices
			for (int i = 0; i < result.length; i++) {
				result[i] = 0;
			}
			idx_score = 10^6;
		}
		else {			// found more indices than numHits
			for (int i = 0; i < tanimoto.getRowDimension(); i++) {
				double[] row = tanimoto.getRow(i);
				double[] idx_vals = new double[idx.length];
				List<Double> lv = new ArrayList<Double>();
				
				for (int j : idx) {
					idx_vals[j] = row[j];
					lv.add(j, row[j]);
				}
				
				//Arrays.sort(idx_vals);	
				Collections.sort(lv);		// equals R order(x[idx]) - ascending!!!
				Collections.reverse(lv);	// descending
				int[] idx_best = new int[idx.length];
				
				for (int j = 0; j < idx_best.length; j++) {
					double tan = lv.remove(0);
					for (int k = 0; k < row.length; k++) {
						if(tan == row[k]) {
							idx_best[j] = k;
							break;
						}
					}
				}
				for (int j = 0; j < numHits; j++) {
					result[i] += row[idx_best[j]];
				}
				pos.put(result[i], i);
				System.out.println("result["+i+"] = " + result[i]);
			}
			idx_score = thresh * numHits;
		}
		
		// tests
		assertArrayEquals(expectedResult, result, 0.0d);
		assertEquals(expectedIdxScore, idx_score, 0d);
		//
		
		// indices of rows with maximum sum of scores
		int[] idx_max_result = MatrixUtils.whichMax(result);
		double max_result = MatrixUtils.max(result);
		
		// tests
		assertArrayEquals(expectedIdxMaxResult, idx_max_result);
		assertEquals(expectedMaxResult, max_result, 0d);
		//
		
		// tied ranking of new scores
		int[] tied_ranks_orig = MatrixUtils.rank(rowScores, MatrixUtils.Order.DESCENDING);
		int[] tied_ranks = MatrixUtils.rank(result, MatrixUtils.Order.DESCENDING);
		int[] tied_ranks_delta = null;
		if(tied_ranks_orig.length == tied_ranks.length) {
			tied_ranks_delta = new int[tied_ranks.length];
			for (int i = 0; i < tied_ranks.length; i++) {
				int tr = MatrixUtils.getTiedRank(result, result[i]);
				System.out.println("rowScore tied_rank_orig = " + tied_ranks_orig[i] + "\tresult = " + result[i] + "\tresult tied_rank = " + tr);
				pos.put(result[i], tr);
				//tied_ranks_delta[i] = tied_ranks_orig[i] - tied_ranks[i];
				tied_ranks_delta[i] = tied_ranks_orig[i] - tr;
				System.out.println("\ttied_rank_delta = " + tied_ranks_delta[i]);
			}
		}
		System.out.println("length orig = " + tied_ranks_orig.length + "  length new = " + tied_ranks.length);
		
		// tests
		assertArrayEquals(expectedTiedRanksOrig, tied_ranks_orig);
		assertArrayEquals(expectedTiedRanks, tied_ranks);
		assertArrayEquals(expectedTiedRanksDelta, tied_ranks_delta);
		//
		
		// new ranking for tanimoto matrix
		int[] rank_new = MatrixUtils.order(result, MatrixUtils.Order.DESCENDING);
		
		// test
		assertArrayEquals(expectedRankNew, rank_new);
		//
		
		System.out.println("rank_new");
		for (int i = 0; i < rank_new.length; i++) {
			System.out.print("["+i+"]=" + rank_new[i] + "  ");
		}
		
		if(max_result > idx_score) {
			assertTrue(max_result > idx_score);
		}
		else {
			assertFalse(max_result > idx_score);
		}
	}
}
