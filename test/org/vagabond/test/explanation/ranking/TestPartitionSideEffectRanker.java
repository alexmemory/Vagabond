package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

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
import org.vagabond.explanation.ranking.IPartitionRanker;
import org.vagabond.explanation.ranking.RankerFactory;
import org.vagabond.explanation.ranking.SideEffectExplanationRanker;
import org.vagabond.test.AbstractVagabondTest;

public class TestPartitionSideEffectRanker extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestPartitionSideEffectRanker.class);
	
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

	
	@Test
	public void testSimplePartition () throws Exception {
		setUp("resource/test/severalComps.xml");
		
		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1"); 
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");
		
		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);
		
		PartitionedMarkerSet mPart = parter.partitionMarkers(g, m); 
		
		ExplPartition ex = new ExplPartition(mPart);
		
		ExplanationCollection col1 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		ex.add(col1);
		col1.createRanker(RankerFactory.createRanker("SideEffect"));
		ExplanationCollection col2 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		ex.add(col2);		
		col2.createRanker(RankerFactory.createRanker("SideEffect"));
		
		ExplPartition e = explGen.findExplanations(m);
		
		log.debug(e);
		
		ranker = RankerFactory.createPartRanker("SideEffect", e);		
		
		SideEffectExplanationRanker er1 = (SideEffectExplanationRanker) col1.getRanker();
		SideEffectExplanationRanker er2 = (SideEffectExplanationRanker) col2.getRanker();
		
		SideEffectExplanationRanker r1 = (SideEffectExplanationRanker) e.getCol(0).getRanker();
		SideEffectExplanationRanker r2 = (SideEffectExplanationRanker) e.getCol(1).getRanker();
		
		// check individual rankers
		er1.resetIter();
		r1.resetIter();
		
		while(er1.hasNext())
			assertEquals(er1.next().getSideEffectSize(), r1.next().getSideEffectSize());
	
		er2.resetIter();
		r2.resetIter();
		
		while(er2.hasNext())
			assertEquals(er2.next().getSideEffectSize(), r2.next().getSideEffectSize());
		
		// rank completely
		while(ranker.hasNext())
			ranker.next();
		ranker.resetIter();
		
		assertTrue(ranker.hasNext());
		
		ArrayList<IExplanationSet> results = new ArrayList<IExplanationSet> ();
		while(ranker.hasNext())
			results.add(ranker.next());
		
		assertTrue(ranker.isFullyRanked());
		
		assertEquals(15, ranker.getNumberOfExplSets());
		
		for(int i = 1; i < results.size(); i++) {
			IExplanationSet s1,s2;
			s1 = results.get(i - 1);
			s2 = results.get(i);
			assertTrue("For Expls: " + s1 + "\n\nand\n\n" + s2, s2.getSideEffectSize() >= s1.getSideEffectSize());
		}
		
		log.debug(ranker);
	}
	
	
	
}
