/**
 * created by Michael Gerlich on May 28, 2010
 * last modified May 28, 2010 - 10:35:46 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntegrationTest {

	@Test
	public void testIntegration() {
		Integration test = new Integration();
		assertEquals(Integration.defaultNumHits, test.numHits);
		assertSame(Integration.defaultThresh, test.thresh);
	}

	@Test
	public void testIntegrationRealMatrixDoubleArrayDoubleArrayFloatInt() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testComputeNewOrdering() {
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
