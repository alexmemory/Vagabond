package org.vagabond.test.explanations.model;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.explanation.ranking.SideEffectExplanationRanker;
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
		SchemaResolver.getInstance().setSchemas();
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
		
		assertEquals ("0,0", ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(0)),
				col.next());
		assertEquals ("1,0",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(1), 
				set2.getExplanations().get(0)),
				col.next());
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(1)),
				col.next());
		assertEquals ("1,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(1), 
				set2.getExplanations().get(1)),
				col.next());
		
		col.confirmExplanation(set2.getExplanations().get(1));
		col.resetIter();
		
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(0), 
				set2.getExplanations().get(1)),
				col.next());
		assertEquals ("1,1",ExplanationFactory.newExplanationSet(
				set1.getExplanations().get(1), 
				set2.getExplanations().get(1)),
				col.next());
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
		
		/* create Dummy Ranker */
		col.createRanker(new SideEffectExplanationRanker());
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
		
		/* create Dummy Ranker */
		col.createRanker(new SideEffectExplanationRanker());
		col.resetIter();
		
		assertEquals ("1,0", ExplanationFactory.newExplanationSet(e12,e21),
				col.next());
		assertEquals (1, col.getIterPos());
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
		assertTrue(col.hasNext());
		assertEquals ("0,1",ExplanationFactory.newExplanationSet(e11,e22),
				col.next());
		assertFalse(col.hasNext());
		
		try {
			col.next();
			assertTrue(false);
		} catch (NoSuchElementException e) {
			
		}
		
	}
	
}
