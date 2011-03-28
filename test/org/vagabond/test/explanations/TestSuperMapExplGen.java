package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.SuperfluousMappingExplanationGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SuperflousMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.MappingType;

public class TestSuperMapExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSuperMapExplGen.class);
	
	private static SuperfluousMappingExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		gen = new SuperfluousMappingExplanationGenerator();
	}
	
	@Test
	public void testSuperMapExplGen () throws Exception {
		ISingleMarker err = MarkerFactory.newAttrMarker("employee", "2|2", "city");
		IExplanationSet result;
		SuperflousMappingError expl;
		Set<MappingType> m1;
		IMarkerSet exp;
		
		m1 = new HashSet<MappingType> ();
		m1.add(MapScenarioHolder.getInstance().getMapping("M2"));
		
		exp = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "1|1"),
				MarkerFactory.newTupleMarker("employee", "4|2")
				);
		
		result = gen.findExplanations(err);
		expl = (SuperflousMappingError) result.getExplanations().get(0);
		log.debug(result);
		
		assertEquals(m1, expl.getMappingSideEffects());
		assertEquals(exp, expl.getTargetSideEffects());
	}
	
}
