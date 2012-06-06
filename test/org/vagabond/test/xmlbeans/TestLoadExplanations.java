package org.vagabond.test.xmlbeans;

import org.junit.Before;
import org.junit.Test;
import org.vagabond.test.AbstractVagabondTest;

public class TestLoadExplanations extends AbstractVagabondTest {
	
	@Before
	public void setUp () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
	}

	@Test
	public void testExplForSimple () {
		
	}
	
}
