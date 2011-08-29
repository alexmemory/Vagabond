package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.SourceSkeletonMappingExplanationGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SourceSkeletonMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class TestSrcSkeMapExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSuperMapExplGen.class);
	
	private static SourceSkeletonMappingExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		gen = new SourceSkeletonMappingExplanationGenerator();
	}
	
	@Test
	public void testSimpleTestScen () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		
		ISingleMarker err = MarkerFactory.newAttrMarker("employee", "2|2", "city");
		IExplanationSet result;
		SourceSkeletonMappingError expl;
		Set<MappingType> m1;
		Set<TransformationType> t1;
		IMarkerSet exp;
		
		
		m1 = new HashSet<MappingType> ();
		m1.add(MapScenarioHolder.getInstance().getMapping("M2"));
		
		t1 = new HashSet<TransformationType> ();
		t1.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		
		exp = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1","city"),
				MarkerFactory.newAttrMarker("employee", "4|2","city")
				);
		
		result = gen.findExplanations(err);
		expl = (SourceSkeletonMappingError) result.getExplanations().get(0);
		log.debug(result);
		
		assertEquals(m1, expl.getMappingSideEffects());
		assertEquals(t1, expl.getTransformationSideEffects());
		assertEquals(exp, expl.getTargetSideEffects());
	}
	
	@Test
	public void testHomelessDebugged () throws Exception {
		ISingleMarker m;
		IExplanationSet exp, result;
		
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		
		m = MarkerParser.getInstance().parseMarker("A(person,1,name)");
		exp = ExplanationFactory.newExplanationSet();
		
		result = gen.findExplanations(m);
		assertEquals(exp,result);
	}
	
}
