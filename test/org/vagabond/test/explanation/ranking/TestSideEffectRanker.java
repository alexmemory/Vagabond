package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.generation.PartitionExplanationGenerator;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph;
import org.vagabond.explanation.generation.partition.ErrorPartitioner;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.PartitionedMarkerSet;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.IExplanationRanker;
import org.vagabond.explanation.ranking.IPartitionRanker;
import org.vagabond.explanation.ranking.SideEffectExplanationRanker;
import org.vagabond.test.AbstractVagabondTest;


public class TestSideEffectRanker extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSideEffectRanker.class);
	
	private ErrorPartitionGraph g;
	private PartitionExplanationGenerator explGen = new PartitionExplanationGenerator();
	private ExplanationSetGenerator explSetGen = new ExplanationSetGenerator();
	private ErrorPartitioner parter = new ErrorPartitioner();
	private IPartitionRanker ranker;

	
	private void setUp (String filename) throws Exception {
		loadToDB(filename);
		g = new ErrorPartitionGraph();
		explGen.init();
	}

	public void advanceIter (IExplanationRanker r, int num) {
		for(int i = 0; i < num; i++) {
			assertTrue(r.hasNext());
			r.next();
		}
	}
	
	public void decreaseIter (IExplanationRanker r, int num) {
		for(int i = 0; i < num; i++) {
			assertTrue(r.hasPrevious());
			r.previous();
		}
	}
	
	public void testPosAndIter (IExplanationRanker r, int num) {
		IExplanationSet one, two;
		r.resetIter();
		for(int i = 0; i < num; i++)
			r.next();
		
		one = r.next();
		two = r.getRankedExpl(num);
		assertEquals("IterPos " + num, one, two);
	}
	
	private void testIterAndDirect(SideEffectExplanationRanker r1) {
		for(int i = 0; i < r1.getNumberOfExplSets(); i++)
			testPosAndIter(r1,i);
	}
	
	@Test
	public void testSimplePartition () throws Exception {
		setUp("resource/test/severalComps.xml");
		
		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1"); 
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");
		
		
		ExplanationCollection col1 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		log.debug(col1);
		col1.createRanker(new SideEffectExplanationRanker());
		
		SideEffectExplanationRanker r1 = (SideEffectExplanationRanker) col1.getRanker();
		
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
		
		// check that all explanations are valid
		IExplanationSet ex = col1.getExplSets().iterator().next();
		
		while(col1.getRanker().hasNext()) {
			IExplanationSet set1 = col1.getRanker().next();
			IBasicExplanation e = set1.getExplanationsSet().iterator().next();
			
			assertTrue("First Col: " + e.toString(), ex.contains(e));
		}
		
		// second collection
		ExplanationCollection col2 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		col2.createRanker(new SideEffectExplanationRanker());		
		log.debug(col2);
		SideEffectExplanationRanker r2 = (SideEffectExplanationRanker) col2.getRanker();

		// check ranker
		testIterAndDirect(r2);
		
		// check that all explanation are valid
		ex = col2.getExplSets().iterator().next();
		
		while(col2.getRanker().hasNext()) {
			IExplanationSet set2 = col2.getRanker().next();
			IBasicExplanation e = set2.getExplanationsSet().iterator().next();
			
			assertTrue("Second Col: " + e.toString(), ex.contains(e));
		}
		
		
	}


	
}
