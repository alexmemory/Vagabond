/**
 * 
 */
package org.vagabond.test.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vagabond.util.PropertyWrapper;

/**
 * @author lord_pretzel
 *
 */
public class TestPropertyWrapper {

	@Test
	public void testGetInt () {
		PropertyWrapper p = new PropertyWrapper();
		p.setProperty("A.B", "1");
		p.setProperty("C", "1");
		
		assertEquals(p.getInt("C"),1);
		assertEquals(p.getInt("D",-1),-1);
		p.setPrefix("A");
		assertEquals(p.getInt("B"),1);
		assertEquals(p.getInt("F", -1), -1);
		p.resetPrefix();
		p.setPrefix("X");
		assertEquals(p.getInt("F", -1), -1);
		p.resetPrefix();
	}
	
}
