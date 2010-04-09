/**
 * 
 */
/**
 * created by Michael Gerlich on Apr 6, 2010
 * last modified Apr 6, 2010 - 2:36:09 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.io;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author mgerlich
 *
 */
public class MyFilterTest {
	
	/**
	 * Test method for {@link de.ipbhalle.MetFlow.io.MyFilter#MyFilter()}.
	 */
	@Test
	public void testMyFilter() {
		MyFilter mf = new MyFilter();
		assertTrue(mf instanceof MyFilter);
		assertTrue(mf.getInfix().isEmpty());
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ipbhalle.MetFlow.io.MyFilter#MyFilter(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testMyFilterStringString() {
		MyFilter mf = new MyFilter("infix", "suffix");
		assertSame("infix", mf.getInfix());
		assertSame("suffix", mf.getEnd());
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ipbhalle.MetFlow.io.MyFilter#accept(java.io.File, java.lang.String)}.
	 */
	@Test
	public void testAccept() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ipbhalle.MetFlow.io.MyFilter#setInfix(java.lang.String)}.
	 */
	@Test
	public void testSetInfix() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ipbhalle.MetFlow.io.MyFilter#getInfix()}.
	 */
	@Test
	public void testGetInfix() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ipbhalle.MetFlow.io.MyFilter#setEnd(java.lang.String)}.
	 */
	@Test
	public void testSetEnd() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ipbhalle.MetFlow.io.MyFilter#getEnd()}.
	 */
	@Test
	public void testGetEnd() {
		fail("Not yet implemented");
	}

}
