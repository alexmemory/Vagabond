package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.SourceSkeletonMappingExplanationGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SourceSkeletonMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.MappingType;

public class TestSrcSkeMapExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSuperMapExplGen.class);
	
	private static SourceSkeletonMappingExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		gen = new SourceSkeletonMappingExplanationGenerator();
	}
	
	@Test
	public void testSrcSkeMapExplGen () throws Exception {
		ISingleMarker err = MarkerFactory.newAttrMarker("employee", "2|2", "city");
		IExplanationSet result;
		SourceSkeletonMappingError expl;
		Set<MappingType> m1;
		IMarkerSet exp;
		
		m1 = new HashSet<MappingType> ();
		m1.add(MapScenarioHolder.getInstance().getMapping("M2"));
		
		exp = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "1|1"),
				MarkerFactory.newTupleMarker("employee", "4|2")
				);
		
		result = gen.findExplanations(err);
		expl = (SourceSkeletonMappingError) result.getExplanations().get(0);
		log.debug(result);
		
		assertEquals(m1, expl.getMappingSideEffects());
		assertEquals(exp, expl.getTargetSideEffects());
	}
	
}
