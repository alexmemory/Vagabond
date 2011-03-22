package org.vagabond.test.explanations;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.test.AbstractVagabondTest;

public class TestExplSetGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestExplSetGen.class);
	
	private ExplanationSetGenerator gen;
	
	public TestExplSetGen () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		
		gen = new ExplanationSetGenerator();
	}
	
	@Test
	public void testExplSetGenSingleEror () throws Exception {
		IMarkerSet m = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "2|2", "city")
				);
		ExplanationCollection col;
		
		col = gen.findExplanations(m);
		
	}
	
}
