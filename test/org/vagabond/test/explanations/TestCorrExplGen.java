package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.CorrespondencExplanationGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.test.TestOptions;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class TestCorrExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestCopyExplGen.class);
	
	private static CorrespondencExplanationGenerator gen;
	
	@BeforeClass
	public static void setUp () throws Exception {
		gen = new CorrespondencExplanationGenerator();
	}
	
	public TestCorrExplGen () {
		
	}
	
	@Test
	public void testGenCorrExplanation () throws Exception {
		IAttributeValueMarker error;
		IExplanationSet expls;
		CorrespondenceError err, expect;
		Set<MappingType> maps;
		Set<CorrespondenceType> corrs;
		IMarkerSet tSE;
		Set<TransformationType> transSE;
		
		loadToDB("resource/test/simpleTest.xml");
		
		error = MarkerFactory.newAttrMarker("employee", "2|2", "city");
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		tSE = MarkerParser.getInstance()
				.parseSet("{A(employee,1|1,city),A(employee,4|2,city)}");
		transSE = new HashSet<TransformationType> ();
		transSE.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		
		expect = new CorrespondenceError(error);
		expect.setCorrespondences(corrs);
		expect.setMapSE(maps);
		expect.setTargetSE(tSE);
		expect.setTransSE(transSE);
		
		expls = gen.findExplanations(error);
		err = (CorrespondenceError) expls.getExplanations().get(0);
		log.debug(expls);
		
		assertEquals(corrs, err.getCorrespondenceSideEffects());
		assertEquals(tSE, err.getTargetSideEffects());		
		assertEquals(maps, err.getMappingSideEffects());

		assertEquals(expect, err);
	}
	
	@Test
	public void testNormalizeGenExplanation () throws Exception {
		IAttributeValueMarker error;
		IExplanationSet expls;
		CorrespondenceError err, expect;
		Set<MappingType> maps;
		Set<CorrespondenceType> corrs;
		IMarkerSet tSE;
		Set<TransformationType> transSE;
		
		loadToDB("resource/test/targetSkeletonError.xml");
		
		error = MarkerFactory.newAttrMarker("person", "1", "name");
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c1"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M1"));
		tSE = MarkerParser.getInstance()
				.parseSet("{A(person,2,name),A(person,3,name),A(person,4,name)}");
		transSE = new HashSet<TransformationType> ();
		transSE.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		
		expect = new CorrespondenceError(error);
		expect.setCorrespondences(corrs);
		expect.setMapSE(maps);
		expect.setTargetSE(tSE);
		expect.setTransSE(transSE);
		
		expls = gen.findExplanations(error);
		err = (CorrespondenceError) expls.getExplanations().get(0);
		log.debug(expls);
		
		assertEquals(corrs, err.getCorrespondenceSideEffects());
		assertEquals(tSE, err.getTargetSideEffects());		
		assertEquals(maps, err.getMappingSideEffects());

		assertEquals(expect, err);
	}
	
}
