package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.*;

import java.util.Comparator;

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
import org.vagabond.explanation.ranking.AStarExplanationRanker;
import org.vagabond.explanation.ranking.IPartitionRanker;
import org.vagabond.explanation.ranking.PartitionRanker;
import org.vagabond.explanation.ranking.RankerFactory;
import org.vagabond.explanation.ranking.SkylineRanker;
import org.vagabond.explanation.ranking.SkylineRanker.SkyPoint;
import org.vagabond.test.AbstractVagabondTest;

public class TestSkylineRanker extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestSkylineRanker.class);
	
	private ErrorPartitionGraph g;
	private PartitionExplanationGenerator explGen = new PartitionExplanationGenerator();
	private ExplanationSetGenerator explSetGen = new ExplanationSetGenerator();
	private ErrorPartitioner parter = new ErrorPartitioner();
	private IPartitionRanker seR, esR;
	private SkylineRanker r;
	
	private void setUp (String filename) throws Exception {
		loadToDB(filename);
		g = new ErrorPartitionGraph();
		explGen.init();
	}

	@Test
	public void testSkyPoints () throws Exception {
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
		
		PartitionRanker ranker = (PartitionRanker) RankerFactory.createPartRanker("ExplSize", e);		
		
		AStarExplanationRanker er1 = (AStarExplanationRanker) col1.getRanker();

		r = new SkylineRanker (new String[] {"SideEffect", "ExplSize"}, "SideEffect");
		r.initialize(e);
		
		// test skypoints
		SkyPoint x,y,z,a;

		x = r.newSkyPoint (er1.next());
		y = r.newSkyPoint (er1.next());
		z = r.newSkyPoint (er1.next());
		er1.resetIter();
		a = r.newSkyPoint(er1.next());
		
		log.debug(x.toVerboseString());
		log.debug(y.toVerboseString());
		log.debug(z.toVerboseString());
		
		assertTrue(x.equals(a));
		assertFalse(x.equals(y));
		assertFalse(z.equals(y));
		assertTrue(x == a);
		
		assertEquals(-1, x.compareTo(y));
		assertEquals(0, y.compareTo(z));
		
		assertTrue(x.dominates(y));
		assertFalse(y.dominates(x));
		
		// test comparators
		Comparator<SkyPoint> c1 = r.getDimComparator(0);
		Comparator<SkyPoint> c2 = r.getDimComparator(1);
		Comparator<SkyPoint> cf = r.getFinalSortComparator();
		
		assertEquals(-1, c1.compare(x, y));
		assertEquals(1, c1.compare(y, x));
		assertEquals(0, c1.compare(y, z));
		
		assertEquals(0, c2.compare(x, y));
		assertEquals(0, c2.compare(x, z));
		
		
		
		assertEquals(0, c2.compare(x, y));
	}
	
	@Test
	public void testSkylineRanker () throws Exception {
		setUp("resource/test/severalComps.xml");
		
		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1"); 
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");
		
		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);
		
		// find solutions
		ExplPartition e = explGen.findExplanations(m);
		
		// ranking
		r  = new SkylineRanker(new String[] {"SideEffect", "ExplSize"}, "SideEffect");
		r.initialize(e);
		
		// compare
		while(r.hasNext()) {
 			IExplanationSet s = r.next();
			log.debug(s);
		}
		
		log.debug(e);
	}

	@Test
	public void testSkylineRankerFullRanking () throws Exception {
		setUp("resource/test/severalComps.xml");
		
		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1"); 
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");
		
		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);
		
		// find solutions
		ExplPartition e = explGen.findExplanations(m);
		
		// ranking
		r  = new SkylineRanker(new String[] {"SideEffect", "ExplSize"}, "SideEffect");
		r.initialize(e);
		r.rankFull();
		
		// compare
		while(r.hasNext()) {
 			IExplanationSet s = r.next();
			log.debug(s);
		}
		
		log.debug(e);
	}

	
}
