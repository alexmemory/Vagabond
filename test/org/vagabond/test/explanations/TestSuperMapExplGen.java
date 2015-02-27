package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.SuperfluousMappingExplanationGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SuperflousMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;

public class TestSuperMapExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSuperMapExplGen.class);
	
	private static SuperfluousMappingExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		
	}
	
	@Before
	public void createGen () {
		gen = new SuperfluousMappingExplanationGenerator();
	}
	
	@Test
	public void testSuperMapExplGen () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		
		ISingleMarker err = MarkerFactory.newAttrMarker("employee", "2|2", "city");
		IExplanationSet result;
		SuperflousMappingError expl;
		Set<MappingType> m1;
		IMarkerSet exp;
		
		m1 = new HashSet<MappingType> ();
		m1.add(MapScenarioHolder.getInstance().getMapping("M2"));
		
		exp = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "name"),
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "name"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city"),
				MarkerFactory.newAttrMarker("employee", "2|2", "name")
				);
		
		result = gen.findExplanations(err);
		expl = (SuperflousMappingError) result.getExplanations().get(0);
		if (log.isDebugEnabled()) {log.debug(result);};
		
		assertEquals(m1, expl.getMappingSideEffects());
		assertEquals(exp, expl.getTargetSideEffects());
	}
	
	@Test
	public void testNullValueExplGen () throws Exception {
		IExplanationSet result, expec;
		IAttributeValueMarker error;
		SuperflousMappingError expl;
		Set<MappingType> m1;
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");

		error = (IAttributeValueMarker) MarkerParser.getInstance()
				.parseMarker("A(person,1,livesin)");

		expl = new SuperflousMappingError(error);
		m1 = new HashSet<MappingType> ();
		m1.add(MapScenarioHolder.getInstance().getMapping("M2"));
		expl.setMapSE(m1);
		expl.setTransSE(CollectionUtils.makeSet(MapScenarioHolder
				.getInstance().getTransformation("T1")));
		expl.setTargetSE(MarkerParser.getInstance().parseSet("{T(person,3),T(person,2)}"));
		expec = ExplanationFactory.newExplanationSet(expl);
		
		result = gen.findExplanations(error);
		
		assertEquals(expec, result);
	}
	
}
