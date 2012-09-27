package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.TargetSkeletonMappingExplanationGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.TargetSkeletonMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class TestTgtSkeMapExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSuperMapExplGen.class);
	
	private static TargetSkeletonMappingExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		gen = new TargetSkeletonMappingExplanationGenerator();
	}
	
	@Test
	public void testTgtSkeMapExplGen () throws Exception {
		loadToDB("resource/test/targetSkeletonError.xml");
		
		ISingleMarker err = MarkerFactory.newAttrMarker("person", "2", "address");
		IExplanationSet result;
		TargetSkeletonMappingError expl, expect;
		Set<MappingType> m1;
		Set<TransformationType> t;
		IMarkerSet exp;
		
		m1 = new HashSet<MappingType> ();
		m1.add(MapScenarioHolder.getInstance().getMapping("M1"));
		
		exp = MarkerParser.getInstance().parseSet(
				"{A(person,1,address),A(person,3,address),A(person,4,address)}");
		
		t = new HashSet<TransformationType>();
		t.add((MapScenarioHolder.getInstance()
				.getTransformation("T1")));
		
		expect = new TargetSkeletonMappingError((IAttributeValueMarker) 
				MarkerParser.getInstance()
						.parseMarker("A(person,2,address)"));
		expect.setMap(m1);
		expect.setTargetSE(exp);
		expect.setTransSE(t);
		
		result = gen.findExplanations(err);
		expl = (TargetSkeletonMappingError) result.getExplanations().get(0);
		if (log.isDebugEnabled()) {log.debug(result);};
		
		assertEquals(m1, expl.getMappingSideEffects());
		assertEquals(exp, expl.getTargetSideEffects());
		assertEquals(expl, expect);
	}
	
	@Test
	public void testNullValueExplGen () throws Exception {
		IExplanationSet result, expec;
		IAttributeValueMarker error;
		
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		
		error = (IAttributeValueMarker) MarkerParser.getInstance()
				.parseMarker("A(person,1,livesin)");
		
		expec = ExplanationFactory.newExplanationSet();
		
		result = gen.findExplanations(error);
		
		assertEquals(expec, result);
	}
	
}
