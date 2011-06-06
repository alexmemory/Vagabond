package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.InfluenceSourceExplanationGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.InfluenceSourceError;
import org.vagabond.test.AbstractVagabondTest;

public class TestInflExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestInflExplGen.class);
	
	private static InfluenceSourceExplanationGenerator gen;
	
	@BeforeClass
	public static void load () throws Exception {
		gen = new InfluenceSourceExplanationGenerator();
	}
	
	@Test
	public void testOnSimple () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		
		IAttributeValueMarker err = MarkerFactory.
				newAttrMarker("employee", "4|2", "city");
		IExplanationSet result, expect;
		InfluenceSourceError expl;
		
		expl = new InfluenceSourceError(err);
		expl.setSourceSE(MarkerParser.getInstance().parseSet("{A(person,4,address)}"));
		expl.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		expect = ExplanationFactory.newExplanationSet(expl);
		
		result = gen.findExplanations(err);
		log.debug(result);
		
		assertEquals(expect,result);
	}
	
	@Test
	public void testOnHomelessDebugged () throws Exception {
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		
		IAttributeValueMarker err = MarkerFactory.
				newAttrMarker("person", "1|3|2", "livesin");
		IExplanationSet result, expect;
		InfluenceSourceError expl1, expl2, expl3;
		
		expl1 = new InfluenceSourceError(err);
		expl1.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(tramp,1,caredforby)}"));
		expl1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		expl2 = new InfluenceSourceError(err);
		expl2.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(socialworker,3,worksfor)}"));
		expl2.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,1|3|2,name)}"));
//		expl2.setTargetSE(MarkerParser.getInstance().parseSet("{T(person,3)}"));

		expl3 = new InfluenceSourceError(err);
		expl3.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(socialworker,3,ssn)}"));
		expl3.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,1|3|2,name)}"));
//		expl3.setTargetSE(MarkerParser.getInstance().parseSet("{T(person,3)}"));
		
		expect = ExplanationFactory.newExplanationSet(expl1, expl2, expl3);
		
		result = gen.findExplanations(err);
		log.debug(result);
		
		assertEquals(expect,result);
	}
}
