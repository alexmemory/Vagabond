package org.vagabond.test.explanations.model;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.explanation.ranking.AStarExplanationRanker;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;

public class TestExplanationCollection extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestExplanationCollection.class);
	
	@BeforeClass
	public static void setUp () throws Exception {
		ModelLoader.getInstance().loadToInst("resource/test/simpleTest.xml");
		ScenarioDictionary.getInstance().initFromScenario();
	}
	
	@Test
	public void testSingleSetCol () throws Exception {
		CopySourceError e1;
		CorrespondenceError e2;
		IExplanationSet set;
		IExplanationSet resultSet;
		IAttributeValueMarker error = 
				MarkerFactory.newAttrMarker("employee", "2|2", "city");
		ExplanationCollection col, col2;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		// copy error
		e1 = new CopySourceError();
		e1.setExplains(error);
		e1.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e1.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "4|2")
				));
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e2 = new CorrespondenceError();
		e2.setExplains(error);
		e2.setCorrespondences(corrs);
		e2.setMapSE(maps);
		e2.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
		));
		
		set = ExplanationFactory.newExplanationSet(e1,e2);
		
		
		// *** collection
		col = new ExplanationCollection();
		col.addExplSet(error, set);
		col.createRanker(new DummyRanker());
		
		assertEquals(col.getNumCombinations(),2); 
		assertEquals(col.getDimensions().size(),1);
		assertEquals(col.getDimensions().get(0),new Integer(2));
		col.resetIter();
		while(col.hasNext()) {
			resultSet = col.next();
			assertTrue(resultSet.getExplains().contains(error));
			assertTrue(resultSet.getExplanationsSet().contains(e1)
					|| resultSet.getExplanationsSet().contains(e2));
		}
		
		col2 = new ExplanationCollection();
		col2.addExplSet(error, set);
		
		assertEquals(col, col2);
	}
	
	@Test
	public void testMultiSetCol () throws Exception {
		CopySourceError e1;
		CorrespondenceError e2;
		IExplanationSet set, set2;
		IAttributeValueMarker error = 
				MarkerFactory.newAttrMarker("employee", "2|2", "city");
		ExplanationCollection col, col2;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		// copy error
		e1 = new CopySourceError();
		e1.setExplains(error);
		e1.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e1.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "4|2")
				));
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e2 = new CorrespondenceError();
		e2.setExplains(error);
		e2.setCorrespondences(corrs);
		e2.setMapSE(maps);
		e2.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
		));
		
		set = ExplanationFactory.newExplanationSet(e1);
		set2 = ExplanationFactory.newExplanationSet(e2);
		
		// *** collection
		col = ExplanationFactory.newExplanationCollection(set, set2);
		col2 = ExplanationFactory.newExplanationCollection(set, set2);
		
		assertEquals(col, col2);
	}
	
	@Test
	public void testDummyRanking () throws Exception {
		CopySourceError e11, e12, e22;
		CorrespondenceError e21;
		IExplanationSet set1, set2;
		IAttributeValueMarker error1, error2; 
				
		ExplanationCollection col;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		error1 = MarkerFactory.newAttrMarker("employee", "2|2", "city");
		error2 = MarkerFactory.newAttrMarker("employee", "1|2", "city");
		
		// copy error
		e11 = new CopySourceError();
		e11.setExplains(error1);
		e11.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e11.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
				));
	
		e12 = new CopySourceError();
		e12.setExplains(error1);
		e12.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "3")
				));
		e12.setTargetSE(MarkerFactory.newMarkerSet(
				));
		
		// first set
		set1 = ExplanationFactory.newExplanationSet(e11, e12);
		
		///////////////////////////////////////////////////////////////////////
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e21 = new CorrespondenceError();
		e21.setExplains(error2);
		e21.setCorrespondences(corrs);
		e21.setMapSE(maps);
		e21.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
		));
	
		e22 = new CopySourceError();
		e22.setExplains(error2);
		e22.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "3")
				));
		e22.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|6", "city"),
				MarkerFactory.newAttrMarker("employee", "1|5", "city"),
				MarkerFactory.newAttrMarker("employee", "1|4", "city"),
				MarkerFactory.newAttrMarker("employee", "5|4", "city")
				));
		
		// set 2
		set2 = ExplanationFactory.newExplanationSet(e21,e22);
		
		/* ensure order in sets */
		set1.getExplanations();
		set2.getExplanations();
		
		// *** collection
		col = ExplanationFactory.newExplanationCollection(set1, set2);
		
		/* create Dummy Ranker */
		col.createRanker(new DummyRanker());
		
		col.resetIter();
		assertEquals(4, col.getNumCombinations());
		assertEquals(0, col.getIterPos());
		assertEquals ("0,0", ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(0)),
				col.next());
		assertEquals(1, col.getIterPos());
		assertEquals ("1,0",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(1), 
				set2.getExplanations().get(0)),
				col.next());
		assertEquals(2, col.getIterPos());
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(1)),
				col.next());
		assertEquals(3, col.getIterPos());
		assertTrue(col.hasNext());
		assertEquals ("1,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(1), 
				set2.getExplanations().get(1)),
				col.next());
		assertFalse(col.hasNext());
		assertEquals(4,col.getIterPos());
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(1)),
				col.previous());
		assertEquals(3,col.getIterPos());
		
		col.confirmExplanation(set2.getExplanations().get(1));
		col.resetIter();
		
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(1)),
				col.next());
		assertEquals(1, col.getIterPos());
		assertEquals ("1,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(1), 
				set2.getExplanations().get(1)),
				col.next());
		assertEquals(2, col.getIterPos());
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(1)),
				col.previous());
		assertEquals(1, col.getIterPos());
	}
	
	@Test
	public void testSideEffectRanking () throws Exception {
		CopySourceError e11, e12, e22;
		CorrespondenceError e21;
		IExplanationSet set1, set2;
		IAttributeValueMarker error1, error2; 
				
		ExplanationCollection col;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		error1 = MarkerFactory.newAttrMarker("employee", "8|8", "city");
		error2 = MarkerFactory.newAttrMarker("employee", "9|9", "city");
		
		// copy error
		e11 = new CopySourceError();
		e11.setExplains(error1);
		e11.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e11.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
				));
	
		e12 = new CopySourceError();
		e12.setExplains(error1);
		e12.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "3")
				));
		e12.setTargetSE(MarkerFactory.newMarkerSet(
				));
		
		// first set
		set1 = ExplanationFactory.newExplanationSet(e11, e12);
		
		///////////////////////////////////////////////////////////////////////
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e21 = new CorrespondenceError();
		e21.setExplains(error2);
		e21.setCorrespondences(corrs);
		e21.setMapSE(maps);
		e21.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "7|2", "city")
		));
	
		e22 = new CopySourceError();
		e22.setExplains(error2);
		e22.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "4")
				));
		e22.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|6", "city"),
				MarkerFactory.newAttrMarker("employee", "1|5", "city"),
				MarkerFactory.newAttrMarker("employee", "1|4", "city"),
				MarkerFactory.newAttrMarker("employee", "f|4", "city")
				));
		
		// set 2
		set2 = ExplanationFactory.newExplanationSet(e21,e22);
		
		/* ensure order in sets */
		set1.getExplanations();
		set2.getExplanations();
		
		// *** collection
		col = ExplanationFactory.newExplanationCollection(set1, set2);
		
		/* create Dummy Ranker */
		col.createRanker(new AStarExplanationRanker(SideEffectSizeScore.inst));
		col.resetIter();
		
		assertEquals (0, col.getIterPos());
		assertEquals ("1,0", ExplanationFactory.newExplanationSet(e12,e21),
				col.next());
		assertEquals (1, col.getIterPos());
		assertEquals ("0,0",ExplanationFactory.newExplanationSet(e11,e21),
				col.next());
		assertEquals (2, col.getIterPos());
		assertEquals ("1,1",ExplanationFactory.newExplanationSet(e12,e22),
				col.next());
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(e11,e22),
				col.next());		
	}
	
	@Test
	public void testResetAndAdvanceOverBorders () throws Exception {
		CopySourceError e11, e12, e22;
		CorrespondenceError e21;
		IExplanationSet set1, set2;
		IAttributeValueMarker error1, error2; 
				
		ExplanationCollection col;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		error1 = MarkerFactory.newAttrMarker("employee", "8|8", "city");
		error2 = MarkerFactory.newAttrMarker("employee", "9|9", "city");
		
		// copy error
		e11 = new CopySourceError();
		e11.setExplains(error1);
		e11.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e11.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
				));
	
		e12 = new CopySourceError();
		e12.setExplains(error1);
		e12.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "3")
				));
		e12.setTargetSE(MarkerFactory.newMarkerSet(
				));
		
		// first set
		set1 = ExplanationFactory.newExplanationSet(e11, e12);
		
		///////////////////////////////////////////////////////////////////////
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e21 = new CorrespondenceError();
		e21.setExplains(error2);
		e21.setCorrespondences(corrs);
		e21.setMapSE(maps);
		e21.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "7|2", "city")
		));
	
		e22 = new CopySourceError();
		e22.setExplains(error2);
		e22.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "5")
				));
		e22.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|6", "city"),
				MarkerFactory.newAttrMarker("employee", "1|5", "city"),
				MarkerFactory.newAttrMarker("employee", "1|4", "city"),
				MarkerFactory.newAttrMarker("employee", "f|4", "city")
				));
		
		// set 2
		set2 = ExplanationFactory.newExplanationSet(e21,e22);
		
		/* ensure order in sets */
		set1.getExplanations();
		set2.getExplanations();
		
		// *** collection
		col = ExplanationFactory.newExplanationCollection(set1, set2);
		
		/* create Dummy Ranker */
		col.createRanker(new AStarExplanationRanker(SideEffectSizeScore.inst));
		col.resetIter();
		
		assertEquals ("1,0", ExplanationFactory.newExplanationSet(e12,e21),
				col.next());
		assertEquals (1, col.getIterPos());
		col.resetIter();
		assertEquals (0, col.getIterPos());
		assertEquals ("1,0", ExplanationFactory.newExplanationSet(e12,e21),
				col.next());
		col.next();
		assertEquals (2, col.getIterPos());
		assertEquals ("1,0", ExplanationFactory.newExplanationSet(e12,e21),
				col.previous());
		assertEquals (1, col.getIterPos());
		assertEquals ("0,0",ExplanationFactory.newExplanationSet(e11,e21),
				col.next());
		assertEquals (2, col.getIterPos());
		assertEquals ("1,1",ExplanationFactory.newExplanationSet(e12,e22),
				col.next());
		assertTrue(col.hasNext());
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(e11,e22),
				col.next());
		assertFalse(col.hasNext());
		
		try {
			col.next();
			assertTrue(false);
		} catch (NoSuchElementException e) {
			
		}
		
		try {
			col.resetIter();
			col.previous();
			assertTrue(false);
		} catch (NoSuchElementException e) {
			
		}
		
	}

	@Test
	public void testUnqiueness () throws Exception {
		CopySourceError e11, e22;
		CorrespondenceError e21, e13;
		IExplanationSet set1, set2;
		IAttributeValueMarker error1, error2; 
				
		ExplanationCollection col;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		error1 = MarkerFactory.newAttrMarker("employee", "8|8", "city");
		error2 = MarkerFactory.newAttrMarker("employee", "9|9", "city");
		
		// copy error
		e11 = new CopySourceError();
		e11.setExplains(error1);
		e11.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e11.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
				));
	
//		e12 = new CopySourceError();
//		e12.setExplains(error1);
//		e12.setSourceSE(MarkerFactory.newMarkerSet(
//				MarkerFactory.newTupleMarker("address", "3")
//				));
//		e12.setTargetSE(MarkerFactory.newMarkerSet(
//				error2
//				));
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e13 = new CorrespondenceError();
		e13.setExplains(error1);
		e13.setCorrespondences(corrs);
		e13.setMapSE(maps);
		e13.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "7|2", "city"),
				error2
		));
		
		// first set
		set1 = ExplanationFactory.newExplanationSet(e11, e13);
		
		///////////////////////////////////////////////////////////////////////
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e21 = new CorrespondenceError();
		e21.setExplains(error2);
		e21.setCorrespondences(corrs);
		e21.setMapSE(maps);
		e21.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "7|2", "city"),
				error1
		));
	
		e22 = new CopySourceError();
		e22.setExplains(error2);
		e22.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "3")
				));
		e22.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|6", "city"),
				MarkerFactory.newAttrMarker("employee", "1|5", "city"),
				MarkerFactory.newAttrMarker("employee", "1|4", "city"),
				MarkerFactory.newAttrMarker("employee", "f|4", "city")
				));
		
		// set 2
		set2 = ExplanationFactory.newExplanationSet(e21,e22);
		
		/* ensure order in sets */
		set1.getExplanations();
		set2.getExplanations();
		
		// *** collection
		col = ExplanationFactory.newExplanationCollection(set1, set2);
		
		/* create SideEffect Ranker */
		col.createRanker(new AStarExplanationRanker(SideEffectSizeScore.inst));
		col.resetIter();
		
		assertEquals (0, col.getIterPos());
		assertTrue ("e13", ExplanationComparators.sameElemComp.compare(col.next().iterator().next(),
				e13) == 0);
		assertEquals (1, col.getIterPos());
		assertEquals ("e11,e22",ExplanationFactory.newExplanationSet(e11,e22),
				col.next());
		assertFalse(col.hasNext()); 
	}
	
	@Test
	public void testMeanOverlap () throws Exception {
		CopySourceError e11, e12, e21, e22, e23, e31;
		IExplanationSet set1, set2, set3;
		IAttributeValueMarker error1, error2, error3; 
				
		ExplanationCollection col;
		
		error1 = MarkerFactory.newAttrMarker("employee", "1", "city");
		error2 = MarkerFactory.newAttrMarker("employee", "2", "city");
		error3 = MarkerFactory.newAttrMarker("employee", "3", "city");
		
		// copy error
		e11 = new CopySourceError();
		e11.setExplains(error1);
		e11.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "1")
				));
		e11.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "E1a", "city"),
				MarkerFactory.newAttrMarker("employee", "E1b", "city"),
				error2
				));
	
		e12 = new CopySourceError();
		e12.setExplains(error1);
		e12.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "3")
				));
		e12.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "E2", "city"),
				error2
				));
		
		// first set
		set1 = ExplanationFactory.newExplanationSet(e11, e12);
		
		///////////////////////////////////////////////////////////////////////
		
		e21 = new CopySourceError();
		e21.setExplains(error2);
		e21.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "1")
				));
		e21.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "E1a", "city"),
				MarkerFactory.newAttrMarker("employee", "E1b", "city"),
				error1
				));
	
		e22 = new CopySourceError();
		e22.setExplains(error2);
		e22.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "3")
				));
		e22.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "E2", "city"),
				error1
				));
		
		e23 = new CopySourceError();
		e23.setExplains(error2);
		e23.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e23.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "E3", "city"),
				error3
				));
		
		// set 2
		set2 = ExplanationFactory.newExplanationSet(e21,e22,e23);

		
		e31 = new CopySourceError();
		e31.setExplains(error3);
		e31.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e31.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "E3", "city"),
				error2
				));
		
		set3 = ExplanationFactory.newExplanationSet(e31);
		
		/* ensure order in sets */
		set1.getExplanations();
		set2.getExplanations();
		set3.getExplanations();
		
		// *** collection
		col = ExplanationFactory.newExplanationCollection(set1, set2, set3);
		
		/* create Dummy Ranker */
		col.createRanker(new AStarExplanationRanker(SideEffectSizeScore.inst));
		col.resetIter();
		
		IExplanationSet s1,s2,s3,e1,e2,e3;
		
		assertTrue(col.getRanker().hasAtLeast(2));
		assertFalse(col.getRanker().hasAtLeast(3));
		
		assertEquals (0, col.getIterPos());
		s1 = col.next();
		e1 = ExplanationFactory.newExplanationSet(e12,e31);
		assertTrue ("1", ExplanationComparators.setSameElemComp.compare(e1, s1) == 0);
		
		assertEquals (1, col.getIterPos());
		assertTrue (col.hasNext());
		s2 = col.next();
		e2 = ExplanationFactory.newExplanationSet(e11,e31);
		e3 = ExplanationFactory.newExplanationSet(e11,e31);
		assertTrue ("2b:\n\n" + e2 + s2, 
				ExplanationComparators.setSameElemComp.compare(e2, s2) == 0);
		
		assertFalse(col.hasNext()); 
	}
}
