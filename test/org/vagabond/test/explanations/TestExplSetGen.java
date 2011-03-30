package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.explanation.model.basic.InfluenceSourceError;
import org.vagabond.explanation.model.basic.SourceSkeletonMappingError;
import org.vagabond.explanation.model.basic.SuperflousMappingError;
import org.vagabond.explanation.model.basic.TargetSkeletonMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class TestExplSetGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestExplSetGen.class);
	
	private ExplanationSetGenerator gen;
	
	public TestExplSetGen () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
		
		gen = new ExplanationSetGenerator();
	}
	
	@Test
	public void testSimpleTestSingleError () throws Exception {
		IMarkerSet m;
		ExplanationCollection col;
		CopySourceError e1;
		CorrespondenceError e2;
		SuperflousMappingError e3;
		InfluenceSourceError e4;
		SourceSkeletonMappingError e5;
		TargetSkeletonMappingError e6;
		IExplanationSet set;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		HashSet<TransformationType> trans;
		
		m = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "2|2", "city")
				);
		
		e1 = new CopySourceError();
		e1.setExplains(m.getElemList().get(0));
		e1.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e1.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "4|2")
				));
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		trans = new HashSet<TransformationType> ();
		trans.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		e2 = new CorrespondenceError();
		e2.setExplains((IAttributeValueMarker) m.getElemList().get(0));
		e2.setCorrespondences(corrs);
		e2.setMapSE(maps);
		e2.setTransSE(trans);
		e2.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
		));
		
		e3 = new SuperflousMappingError();
		e3.setExplains(m.getElemList().get(0));
		e3.setMapSE(maps);
		e3.setTransSE(trans);
		e3.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "4|2"),
				MarkerFactory.newTupleMarker("employee", "1|1")
		));
		
		e4 = new InfluenceSourceError();
		e4.setExplains(m.getElemList().get(0));
		
		e5 = new SourceSkeletonMappingError();
		e5.setExplains(m.getElemList().get(0));
		e5.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
		));
		e5.addMap(MapScenarioHolder.getInstance().getMapping("M2"));
		e5.setTransSE(trans);
		
		set = ExplanationFactory.newExplanationSet(e1,e2,e3,e4,e5);

		col = gen.findExplanations(m);
		log.debug(col);
		
		assertEquals(set, col.getExplSets().iterator().next());
	}
	
	@Test
	public void testHomelessScenario () throws Exception {
		IMarkerSet m;
		ExplanationCollection col;
		CopySourceError e1;
		CorrespondenceError e2;
		SuperflousMappingError e3;
		InfluenceSourceError e4;
		SourceSkeletonMappingError e5;
		IExplanationSet set;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		HashSet<TransformationType> trans;
		
		loadToDB("resource/exampleScenarios/homeless.xml");
		
		m = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("person", "1|1", "livesin")
				);
		
		e1 = new CopySourceError();
		e1.setExplains(m.getElemList().get(0));
		e1.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("soupkitchen", "1")
				));
		e1.setTargetSE(MarkerParser.getInstance().parserSet(
				"{T(person,2|1),T(person,2|1|2|1),T(person,1|3|1|2)}"));
				
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c3"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M1"));
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		trans = new HashSet<TransformationType> ();
		trans.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		
		e2 = new CorrespondenceError();
		e2.setExplains((IAttributeValueMarker) m.getElemList().get(0));
		e2.setCorrespondences(corrs);
		e2.setMapSE(maps);
		e2.setTransSE(trans);
		e2.setTargetSE(MarkerParser.getInstance().parserSet(
				"{A(person,2|1,livesin),A(person,2|1|2|1,livesin),A(person,1|3|1|2,livesin),A(person,3|2,livesin)}"));
		
		e3 = new SuperflousMappingError();
		e3.setExplains(m.getElemList().get(0));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e3.setMapSE(maps);
		e3.setTransSE(trans);
		e3.setTargetSE(MarkerParser.getInstance().parserSet(
				"{T(person,2|1),T(person,3|2)}"));
		
		e4 = new InfluenceSourceError();
		e4.setExplains(m.getElemList().get(0));
		
		e5 = new SourceSkeletonMappingError();
		e5.setExplains(m.getElemList().get(0));
		e5.setTargetSE(MarkerParser.getInstance().parserSet(
				"{A(person,3|2,livesin),A(person,2|1,livesin)}"));
		e5.addMap(MapScenarioHolder.getInstance().getMapping("M2"));
		e5.setTransSE(trans);
		
		set = ExplanationFactory.newExplanationSet(e1,e2,e3,e4,e5);

		col = gen.findExplanations(m);
		log.debug(col);
		
		assertEquals(set, col.getExplSets().iterator().next());
	}
	
}
