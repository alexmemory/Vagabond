package org.vagabond.test.explanations;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.InfluenceSourceExplanationGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.InfluenceSourceError;
import org.vagabond.test.AbstractVagabondTest;

public class TestInflExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestInflExplGen.class);
	
	private static InfluenceSourceExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		gen = new InfluenceSourceExplanationGenerator();
	}
	
	@Test
	public void testInfluenceExplGen () throws Exception {
		IAttributeValueMarker err = MarkerFactory.
				newAttrMarker("employee", "2|2", "city");
		IExplanationSet result;
		InfluenceSourceError expl;
		
		result = gen.findExplanations(err);
		log.debug(result);
		
	}
}
