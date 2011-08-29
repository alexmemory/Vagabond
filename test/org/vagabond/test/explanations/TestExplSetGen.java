package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;

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
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public class TestExplSetGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestExplSetGen.class);
	
	private ExplanationSetGenerator gen;
	
	public TestExplSetGen () throws Exception {
		gen = new ExplanationSetGenerator();
	}
		
	@Test
	public void testSimpleTestSingleError () throws Exception {
		IMarkerSet m;
		ExplanationCollection col, expCol;
		CopySourceError e1;
		CorrespondenceError e2;
		SuperflousMappingError e3;
		InfluenceSourceError e4;
		SourceSkeletonMappingError e5;
		IExplanationSet set;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		HashSet<TransformationType> trans;
	
		loadToDB("resource/test/simpleTest.xml");
		
		m = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "2|2", "city")
				);
		
		e1 = new CopySourceError();
		e1.setExplains(m.getElemList().get(0));
		e1.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(address,2,city)}"));
		e1.setTargetSE(MarkerParser.getInstance()
				.parseSet("{A(employee,4|2,city)}"));
				
		
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
		e4.setSourceSE(MarkerParser.getInstance().parseSet("{A(person,2,address)}"));
		
		e5 = new SourceSkeletonMappingError();
		e5.setExplains(m.getElemList().get(0));
		e5.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
		));
		e5.addMap(MapScenarioHolder.getInstance().getMapping("M2"));
		e5.setTransSE(trans);
		
		set = ExplanationFactory.newExplanationSet(e1,e2,e3,e4,e5);
		expCol = ExplanationFactory.newExplanationCollection(set);
		
		col = gen.findExplanations(m);
		log.debug(col);
		
		assertEquals(set, col.getExplSets().iterator().next());
		assertEquals(expCol, col);
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
		
		log.debug(m);
		
		e1 = new CopySourceError();
		e1.setExplains(m.getElemList().get(0));
		e1.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(soupkitchen,1,city)}"));
		
		e1.setTargetSE(MarkerParser.getInstance().parseSet(
				"{A(person,2|1,livesin),A(person,1|3|1|2,livesin)}"));
				//T(person,2|1|2|1),
		
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
		e2.setTargetSE(MarkerParser.getInstance().parseSet(
				"{A(person,2|1,livesin),A(person,2|1|2|1,livesin)," +
				"A(person,1|3|1|2,livesin),A(person,3|2,livesin)}"));
		
		e3 = new SuperflousMappingError();
		e3.setExplains(m.getElemList().get(0));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e3.setMapSE(maps);
		e3.setTransSE(trans);
		e3.setTargetSE(MarkerParser.getInstance().parseSet(
				"{T(person,2|1),T(person,3|2)}"));
		
		e4 = new InfluenceSourceError();
		e4.setExplains(m.getElemList().get(0));
		e4.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(socialworker,1,worksfor)}"));
		e4.setTargetSE(MarkerParser.getInstance()
				.parseSet("{A(person,2|1|2|1,livesin),A(person,2|1|2|1,name)}"));
		
		e5 = new SourceSkeletonMappingError();
		e5.setExplains(m.getElemList().get(0));
		e5.setTargetSE(MarkerParser.getInstance().parseSet(
				"{A(person,3|2,livesin),A(person,2|1,livesin)}"));
		e5.addMap(MapScenarioHolder.getInstance().getMapping("M2"));
		e5.setTransSE(trans);
		
		set = ExplanationFactory.newExplanationSet(e1,e2,e3,e4,e5);

		col = gen.findExplanations(m);
		log.debug(col);
		
		col.resetIter();
		assertEquals(set, col.getExplSets().iterator().next());
	}
	
	@Test
	public void testHomelessDebugged () throws Exception {
		IMarkerSet m;
		ExplanationCollection col;
		ExplanationCollection expCol;
		CopySourceError e1;
		CorrespondenceError e2;
		SuperflousMappingError e3;
		IExplanationSet set;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		HashSet<TransformationType> trans;
		
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		
		m = MarkerParser.getInstance().parseSet("{A(person,1,name)}");
		
		e1 = new CopySourceError();
		e1.setExplains(m.getElemList().get(0));
		e1.setSourceSE(MarkerParser.getInstance().parseSet(
				"{A(socialworker,1,name)}"));
		e1.setTargetSE(MarkerParser.getInstance().parseSet(
				"{}"));
				
		
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
		e2.setTargetSE(MarkerParser.getInstance().parseSet(
				"{A(person,3,name),A(person,2,name)}"));
		
		e3 = new SuperflousMappingError();
		e3.setExplains(m.getElemList().get(0));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e3.setMapSE(maps);
		e3.setTransSE(trans);
		e3.setTargetSE(MarkerParser.getInstance().parseSet(
				"{T(person,3),T(person,2)}"));

		set = ExplanationFactory.newExplanationSet(e1,e2,e3);
		expCol = ExplanationFactory.newExplanationCollection(set);
		
		col = gen.findExplanations(m);
		log.debug(col);
		
		col.resetIter();
		assertEquals(set, col.getExplSets().iterator().next());
		
		assertEquals(expCol, col);
	}
	
	@Test
	public void testHomelessDebuggedMultiple () throws Exception {
		IMarkerSet errSet;
		IAttributeValueMarker e1, e2;
		IExplanationSet set1, set2;
		ExplanationCollection col, expCol;
		
		CopySourceError c1, c2;
		CorrespondenceError r1, r2;
		SuperflousMappingError sm1, sm2;
		InfluenceSourceError  i2a,i2b,i2c;
		SourceSkeletonMappingError ss1;
		
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		HashSet<TransformationType> trans;
		
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		
		// errors
		e1 = (IAttributeValueMarker) MarkerParser.getInstance()
				.parseMarker("A(person,1,name)");
		e2 = (IAttributeValueMarker) MarkerParser.getInstance()
				.parseMarker("A(person,2|1|1,livesin)");
		errSet = MarkerFactory.newMarkerSet(e1,e2);
		
		// ************** set 1	
		c1 = new CopySourceError();
		c1.setExplains(e1);
		c1.setSourceSE(MarkerParser.getInstance().parseSet(
				"{A(socialworker,1,name)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet(
				"{}"));
				
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		trans = new HashSet<TransformationType> ();
		trans.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		
		r1 = new CorrespondenceError();
		r1.setExplains(e1);
		r1.setCorrespondences(corrs);
		r1.setMapSE(maps);
		r1.setTransSE(trans);
		r1.setTargetSE(MarkerParser.getInstance().parseSet(
				"{A(person,3,name),A(person,2,name)}"));
		
		sm1 = new SuperflousMappingError();
		sm1.setExplains(e1);
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		sm1.setMapSE(maps);
		sm1.setTransSE(trans);
		sm1.setTargetSE(MarkerParser.getInstance().parseSet(
				"{T(person,3),T(person,2)}"));
		
		set1 = ExplanationFactory.newExplanationSet(c1,r1,sm1);
		
		// ****************** set 2

		c2 = new CopySourceError();
		c2.setExplains(e2);
		c2.setSourceSE(MarkerParser.getInstance().parseSet(
				"{A(soupkitchen,1,city)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet(
				"{}"));
				
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c3"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M1"));
		trans = new HashSet<TransformationType> ();
		trans.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		
		r2 = new CorrespondenceError();
		r2.setExplains(e2);
		r2.setCorrespondences(corrs);
		r2.setMapSE(maps);
		r2.setTransSE(trans);
		r2.setTargetSE(MarkerParser.getInstance().parseSet(
				"{A(person,1|3|2,livesin)}"));
		
		sm2 = new SuperflousMappingError();
		sm2.setExplains(e2);
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M1"));
		sm2.setMapSE(maps);
		sm2.setTransSE(trans);
		sm2.setTargetSE(MarkerParser.getInstance().parseSet(
				"{T(person,1|3|2)}"));
		
		i2a = new InfluenceSourceError(e2);
		i2a.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(tramp,2,caredforby)}"));
		i2a.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		i2b = new InfluenceSourceError(e2);
		i2b.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(socialworker,1,worksfor)}"));
		i2b.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,2|1|1,name)}"));

		i2c = new InfluenceSourceError(e2);
		i2c.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(socialworker,1,ssn)}"));
		i2c.setTargetSE(MarkerParser.getInstance().parseSet("{A(person,2|1|1,name)}"));
		

		ss1 = new SourceSkeletonMappingError();
		ss1.setExplains(e2);
		ss1.setTargetSE(MarkerParser.getInstance().parseSet(
				"{A(person,1|3|2,livesin)}"));
		ss1.addMap(MapScenarioHolder.getInstance().getMapping("M1"));
		ss1.setTransSE(trans);
		
		set2 = ExplanationFactory.newExplanationSet(c2,r2,sm2,i2a,i2b,i2c,ss1);

		
		// complet collection
		expCol = ExplanationFactory.newExplanationCollection(set1, set2);
		
		col = gen.findExplanations(errSet);
		log.debug(col);
		
		col.resetIter();
		
		assertEquals(expCol, col);
	}
	
	@Test
	public void testGenForNullValue () throws Exception {
		IMarkerSet m;
		ExplanationCollection col;
		ExplanationCollection expCol;
		SuperflousMappingError e3;
		IExplanationSet set;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		HashSet<TransformationType> trans;
		
		loadToDB("resource/exampleScenarios/homelessDebugged.xml");
		
		m = MarkerParser.getInstance().parseSet("{A(person,1,livesin)}");
		
				
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		trans = new HashSet<TransformationType> ();
		trans.add(MapScenarioHolder.getInstance().getTransformation("T1"));
				
		e3 = new SuperflousMappingError();
		e3.setExplains(m.getElemList().get(0));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e3.setMapSE(maps);
		e3.setTransSE(trans);
		e3.setTargetSE(MarkerParser.getInstance().parseSet(
				"{T(person,3),T(person,2)}"));

		set = ExplanationFactory.newExplanationSet(e3);
		expCol = ExplanationFactory.newExplanationCollection(set);
		
		col = gen.findExplanations(m);
		log.debug(col);
		
		col.resetIter();
		assertEquals(set, col.getExplSets().iterator().next());
		
		assertEquals(expCol, col);
	}
	
	@Test
	public void testTargetSkeletonScen () throws Exception {
		IMarkerSet errSet;
		IAttributeValueMarker e1;
		IExplanationSet set1;
		ExplanationCollection col, expCol;
		CopySourceError c1;
		CorrespondenceError r1;
		SuperflousMappingError sm1;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		HashSet<TransformationType> trans;
		
		loadToDB("resource/test/targetSkeletonError.xml");
		
		// errors
		e1 = (IAttributeValueMarker) MarkerParser.getInstance()
				.parseMarker("A(person,1,name)");
		errSet = MarkerFactory.newMarkerSet(e1);
		
		// expls
		c1 = new CopySourceError(e1);
		c1.setSourceSE(MarkerParser.getInstance()
				.parseSet("{A(employee,1,name)}"));
		c1.setTargetSE(MarkerParser.getInstance()
				.parseSet("{}"));
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c1"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M1"));
		trans = new HashSet<TransformationType> ();
		trans.add(MapScenarioHolder.getInstance().getTransformation("T1"));
		
		r1 = new CorrespondenceError(e1);
		r1.setCorrespondences(corrs);
		r1.setMapSE(maps);
		r1.setTargetSE(MarkerParser.getInstance()
				.parseSet("{A(person,2,name),A(person,3,name),A(person,4,name)}"));
		r1.setTransSE(trans);
		
//		i1 = new InfluenceSourceError();
//		i1.setExplains(e1);
		
		sm1 = new SuperflousMappingError();
		sm1.setExplains(e1);
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M1"));
		sm1.setMapSE(maps);
		trans.add(MapScenarioHolder.getInstance().getTransformation("T2"));
		sm1.setTransSE(trans);
		sm1.setTargetSE(MarkerParser.getInstance()
				.parseSet("{T(person,2),T(person,3),T(person,4)," +
						"T(address,1),T(address,2),T(address,3),T(address,4)}"));
		
		// complete collection
		set1 = ExplanationFactory.newExplanationSet(c1,r1,sm1);
		expCol = ExplanationFactory.newExplanationCollection(set1);
		
		col = gen.findExplanations(errSet);
		log.debug(col);
		
		col.resetIter();
		
		assertEquals(expCol, col);		
	}
	
}
