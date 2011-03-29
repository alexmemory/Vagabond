package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.TargetSkeletonMappingExplanationGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SourceSkeletonMappingError;
import org.vagabond.explanation.model.basic.TargetSkeletonMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.MappingType;

public class TestTgtSkeMapExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSuperMapExplGen.class);
	
	private static TargetSkeletonMappingExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		loadToDB("resource/test/targetSkeletonError.xml");
		gen = new TargetSkeletonMappingExplanationGenerator();
	}
	
	@Test
	public void testTgtSkeMapExplGen () throws Exception {
		ISingleMarker err = MarkerFactory.newAttrMarker("person", "2", "address");
		IExplanationSet result;
		TargetSkeletonMappingError expl;
		Set<MappingType> m1;
		IMarkerSet exp;
		
		m1 = new HashSet<MappingType> ();
		m1.add(MapScenarioHolder.getInstance().getMapping("M1"));
		
		exp = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("person", "1","address"),
				MarkerFactory.newAttrMarker("person", "4","address")
				);
		
		result = gen.findExplanations(err);
		expl = (TargetSkeletonMappingError) result.getExplanations().get(0);
		log.debug(result);
		
		assertEquals(m1, expl.getMappingSideEffects());
		assertEquals(exp, expl.getTargetSideEffects());
	}
	
}
