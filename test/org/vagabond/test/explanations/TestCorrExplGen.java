package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.CorrespondencExplanationGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.test.TestOptions;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;

public class TestCorrExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestCopyExplGen.class);
	
	private static CorrespondencExplanationGenerator gen;
	
	@BeforeClass
	public static void setUp () throws Exception {
		gen = new CorrespondencExplanationGenerator();
		
		loadToDB("resource/test/simpleTest.xml");
	}
	
	public TestCorrExplGen () {
		
	}
	
	@Test
	public void testGenCorrExplanation () throws Exception {
		IAttributeValueMarker error = MarkerFactory.
				newAttrMarker("employee", "2|2", "city");
		IExplanationSet expls;
		CorrespondenceError err;
		
		CorrespondenceType c2 = MapScenarioHolder.getInstance().getCorr("c2");
		MappingType m1 = MapScenarioHolder.getInstance().getMapping("M1");
		MappingType m2 = MapScenarioHolder.getInstance().getMapping("M2");
		IAttributeValueMarker a1 = MarkerFactory.
				newAttrMarker("employee", "1|1", "city");
		IAttributeValueMarker a2 = MarkerFactory.
				newAttrMarker("employee", "4|2", "city");
		
		expls = gen.findExplanations(error);
		err = (CorrespondenceError) expls.getExplanations().get(0);
		log.debug(expls);
		
		assertTrue(err.getCorrespondences().contains(c2));
				
		assertTrue(err.getSideEffects().contains(a1));
		assertTrue(err.getSideEffects().contains(a2));
		
		assertTrue(!err.getMapSE().contains(m1));
		assertTrue(err.getMapSE().contains(m2));
	}
	
}
