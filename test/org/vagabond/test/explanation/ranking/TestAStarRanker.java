package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.AStarExplanationRanker;
import org.vagabond.explanation.ranking.AStarExplanationRanker.RankedListElement;
import org.vagabond.explanation.ranking.IExplanationRanker;
import org.vagabond.explanation.ranking.RankerFactory;
import org.vagabond.explanation.ranking.scoring.ExplanationSizeScore;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.explanation.ranking.scoring.ScoreExplSetComparator;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.test.AbstractVagabondTest;


public class TestAStarRanker extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestAStarRanker.class);
	
	private ExplanationSetGenerator explSetGen = new ExplanationSetGenerator();

	
	private void setUp (String filename) throws Exception {
		loadToDB(filename);
	}

	private void advanceIter (IExplanationRanker r, int num) {
		for(int i = 0; i < num; i++) {
			assertTrue(r.hasNext());
			r.next();
		}
	}
	
	private void decreaseIter (IExplanationRanker r, int num) {
		for(int i = 0; i < num; i++) {
			assertTrue(r.hasPrevious());
			r.previous();
		}
	}
	
	private void testSortedOnScore (IExplanationRanker r, IScoringFunction f) {
		IExplanationSet cur = null, next = null;
		Comparator<IExplanationSet> comp = new ScoreExplSetComparator(f);
		r.resetIter();
		if (!r.hasNext())
			return;
		cur = r.next();
		
		while(r.hasNext()) {
			int res;
			
			next = r.next();
			res = comp.compare(cur, next);
			assertTrue("Elem " + cur.toString() + "\n\n\n" + next.toString() + "\n\n comped to: " + res, res != 1);
			cur = next;
		}
		
	}
	
	private void testPosAndIter (AStarExplanationRanker aStarRanker, int num) {
		IExplanationSet one, two;
		IScoringFunction scoringFunction = aStarRanker.getScoringFunction();
		aStarRanker.resetIter();
		
		assertTrue("has at least " + num, aStarRanker.hasAtLeast(num - 1));
		for(int i = 0; i < num; i++)
			aStarRanker.next();
		
		one = aStarRanker.next();
		two = aStarRanker.getRankedExpl(num);
		assertEquals("IterPos " + num, one, two);
		assertEquals("Score for iter " + num, aStarRanker.getScore(num), scoringFunction.getScore(two));
	}
	
	private void testIterAndDirect(AStarExplanationRanker r1) {
		for(int i = 0; i < r1.getNumberOfExplSets(); i++)
			testPosAndIter(r1,i);
	}
	
	@Test
	public void testSideEffectRanking () throws Exception {
		IScoringFunction f = RankerFactory.getScoreFunction("SideEffect");
		setUp("resource/test/severalComps.xml");
		
		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1"); 
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");
		
		// first collection
		ExplanationCollection col1 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		if (log.isDebugEnabled()) {log.debug(col1);};
		col1.createRanker(new AStarExplanationRanker(SideEffectSizeScore.inst));
		
		AStarExplanationRanker r1 = (AStarExplanationRanker) col1.getRanker();
		
		assertEquals("num sets: " + r1.getNumberOfExplSets(), 5, r1.getNumberOfExplSets());
		assertTrue(r1.hasNext());
		
		assertEquals(5, r1.getNumberPrefetched());
		
		advanceIter(r1,5);
		assertFalse(r1.hasNext());
		assertTrue(r1.isFullyRanked());
		decreaseIter(r1,4);
		assertFalse(r1.hasPrevious());
		
		testIterAndDirect(r1);
		
		assertEquals(5, r1.getNumberOfExplSets());
		
		testSortedOnScore(r1, f);
		
		// check that all explanations are valid
		IExplanationSet ex = col1.getExplanationSets().iterator().next();
		
		while(col1.getRanker().hasNext()) {
			IExplanationSet set1 = col1.getRanker().next();
			IBasicExplanation e = set1.getExplanationsSet().iterator().next();
			
			assertTrue("First Col: " + e.toString(), ex.contains(e));
		}
		
		// second collection
		ExplanationCollection col2 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		col2.createRanker(new AStarExplanationRanker(SideEffectSizeScore.inst));		
		if (log.isDebugEnabled()) {log.debug(col2);};
		AStarExplanationRanker r2 = (AStarExplanationRanker) col2.getRanker();

		// check ranker
		testIterAndDirect(r2);
		
		testSortedOnScore(r2, f);
		
		// check that all explanation are valid
		ex = col2.getExplanationSets().iterator().next();
		
		while(col2.getRanker().hasNext()) {
			IExplanationSet set2 = col2.getRanker().next();
			IBasicExplanation e = set2.getExplanationsSet().iterator().next();
			
			assertTrue("Second Col: " + e.toString(), ex.contains(e));
		}
		
		
	}

	@Test
	public void testExplSizeRanking () throws Exception {
		IScoringFunction f = RankerFactory.getScoreFunction("ExplSize");
		
		setUp("resource/test/severalComps.xml");
		
		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1"); 
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");
				
		ExplanationCollection col1 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		if (log.isDebugEnabled()) {log.debug(col1);};
		col1.createRanker(new AStarExplanationRanker(ExplanationSizeScore.inst));
		
		AStarExplanationRanker r1 = (AStarExplanationRanker) col1.getRanker();
		
		assertEquals("num sets: " + r1.getNumberOfExplSets(), 5, r1.getNumberOfExplSets());
		assertTrue(r1.hasNext());
		
		assertEquals(5, r1.getNumberPrefetched());
		
		advanceIter(r1,5);
		assertFalse(r1.hasNext());
		assertTrue(r1.isFullyRanked());
		decreaseIter(r1,4);
		assertFalse(r1.hasPrevious());
		
		testIterAndDirect(r1);
		
		assertEquals(5, r1.getNumberOfExplSets());
		
		testSortedOnScore(r1, f);
		
		// check that all explanations are valid
		IExplanationSet ex = col1.getExplanationSets().iterator().next();
		
		while(col1.getRanker().hasNext()) {
			IExplanationSet set1 = col1.getRanker().next();
			IBasicExplanation e = set1.getExplanationsSet().iterator().next();
			
			assertTrue("First Col: " + e.toString(), ex.contains(e));
		}
		
		// second collection
		ExplanationCollection col2 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		col2.createRanker(new AStarExplanationRanker(ExplanationSizeScore.inst));		
		if (log.isDebugEnabled()) {log.debug(col2);};
		AStarExplanationRanker r2 = (AStarExplanationRanker) col2.getRanker();

		// check ranker
		testIterAndDirect(r2);
		
		testSortedOnScore(r2, f);
		
		// check that all explanation are valid
		ex = col2.getExplanationSets().iterator().next();
		
		while(col2.getRanker().hasNext()) {
			IExplanationSet set2 = col2.getRanker().next();
			IBasicExplanation e = set2.getExplanationsSet().iterator().next();
			
			assertTrue("Second Col: " + e.toString(), ex.contains(e));
		}
	}
	
	@Test
	public void testComparisonOp () throws Exception {
		setUp("resource/test/simpleTest.xml");

		setTids("employee", new String[] {"1","2","3","E1a","E1b","E2","E3"});		
		setTids("address", new String[] { "1", "2", "3"});
		
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
		AStarExplanationRanker r = (AStarExplanationRanker) new AStarExplanationRanker(SideEffectSizeScore.inst);
		r.initializeCollection(col);
		Comparator<RankedListElement> comp = AStarExplanationRanker.rankComp;
		
		RankedListElement r1,r2,r3;
		r1 = r.new RankedListElement(new int[] {0, -2, 0});
		r2 = r.new RankedListElement(new int[] {1, -2, 0});
		r3 = r.new RankedListElement(new int[] {-2, 0, 0});
		
		if (log.isDebugEnabled()) {log.debug(r1);};
		if (log.isDebugEnabled()) {log.debug(r2);};
		if (log.isDebugEnabled()) {log.debug(r3);};
			
		assertEquals(-1, comp.compare(r1, r2));
		assertEquals(1, comp.compare(r2, r1));
		assertEquals(0, comp.compare(r1, r3));
		assertEquals(0, comp.compare(r3, r1));
		assertEquals(1, comp.compare(r2, r3));
		assertEquals(-1, comp.compare(r3, r2));
	}
	
	@Test
	public void testMeanOverlap () throws Exception {
		setUp("resource/test/simpleTest.xml");		
		
		setTids("employee", new String[] {"1","2","3","E1a","E1b","E2","E3"});		
		setTids("address", new String[] { "1", "2", "3"});

		
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
		AStarExplanationRanker r = (AStarExplanationRanker) col.getRanker();
		
		IExplanationSet s1,s2,e1,e2;
		
		r.rankFull();
		if (log.isDebugEnabled()) {log.debug(r.getRanking());};
		
		r.hasAtLeast(2);
		assertTrue(r.hasAtLeast(2));
		r.hasAtLeast(3);
		assertFalse(r.hasAtLeast(3));
		
		assertEquals (0, r.getIteratorPosition());
		s1 = r.next();
		e1 = ExplanationFactory.newExplanationSet(e12,e31);
		assertTrue ("1", ExplanationComparators.setSameElemComp.compare(e1, s1) == 0);
		
		assertEquals (1, r.getIteratorPosition());
		assertTrue (r.hasNext());
		s2 = r.next();
		e2 = ExplanationFactory.newExplanationSet(e11,e31);
		assertTrue ("2:\n\n" + e2 + s2, 
				ExplanationComparators.setSameElemComp.compare(e2, s2) == 0);
		
		assertFalse(r.hasNext()); 
	}
	
	@Test
	public void testRankFull () throws Exception {
		setUp("resource/test/simpleTest.xml");

		setTids("employee", new String[] {"1","2","3","E1a","E1b","E2","E3"});		
		setTids("address", new String[] { "1", "2", "3"});
		
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
		AStarExplanationRanker r1 = new AStarExplanationRanker(SideEffectSizeScore.inst);
		r1.initializeCollection(col);
		AStarExplanationRanker r2 = new AStarExplanationRanker(SideEffectSizeScore.inst);
		r2.initializeCollection(col);
		
		Comparator<RankedListElement> comp = AStarExplanationRanker.rankComp;
		
		while(r1.hasNext())
			r1.next();
		r2.rankFull();
		
		assertEquals(r1.getNumberOfExplSets(), r2.getNumberOfExplSets());
		assertEquals(r1.getNumberPrefetched(), r2.getNumberPrefetched());
		
		r2.resetIter();
		r1.resetIter();
		while(r1.hasNext())
			assertEquals(r1.next(), r2.next());
	}
	
}
