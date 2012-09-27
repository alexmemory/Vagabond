package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
		
		if (log.isDebugEnabled()) {log.debug(e);};
		
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
		
		if (log.isDebugEnabled()) {log.debug(x.toVerboseString());};
		if (log.isDebugEnabled()) {log.debug(y.toVerboseString());};
		if (log.isDebugEnabled()) {log.debug(z.toVerboseString());};
		
		if (log.isDebugEnabled()) {log.debug(x);};
		if (log.isDebugEnabled()) {log.debug(y);};
		if (log.isDebugEnabled()) {log.debug(z);};
		
		// test equality and native compare
		assertTrue(x.equals(a));
		assertFalse(x.equals(y));
		assertFalse(z.equals(y));
		assertTrue(x == a);
		
		assertEquals(-1, x.compareTo(y));
		assertEquals(1, y.compareTo(x));
		assertEquals(0, y.compareTo(z));
		assertEquals(0, z.compareTo(y));
		assertEquals(-1, x.compareTo(z));
		assertEquals(1, z.compareTo(x));
		
		assertTrue(x.dominates(y));
		assertFalse(y.dominates(x));
		
		// test comparators
		Comparator<SkyPoint> c1 = r.getDimComparator(0);
		Comparator<SkyPoint> c2 = r.getDimComparator(1);
		Comparator<SkyPoint> cf = r.getFinalSortComparator();
		
		// C1
		assertEquals(0, c1.compare(x, x));
		assertEquals(0, c1.compare(x, a));
		assertEquals(0, c1.compare(a, x));
		assertEquals(0, c1.compare(y, y));
		assertEquals(0, c1.compare(z, z));
		
		
		assertEquals(-1, c1.compare(x, y));
		assertEquals(1, c1.compare(y, x));
		
		assertEquals(-1, c1.compare(y, z));
		assertEquals(1, c1.compare(z, y));
		
		assertEquals(-1, c1.compare(x, z));
		assertEquals(1, c1.compare(z, x));
		
		// C2
		assertEquals(-1, c2.compare(x, y));
		assertEquals(1, c2.compare(y, x));
		
		assertEquals(-1, c2.compare(x, z));
		assertEquals(1, c2.compare(z, x));
		
		assertEquals(-1, c2.compare(y, z));
		assertEquals(1, c2.compare(z, y));
		
		assertEquals(0, c2.compare(x, x));
		assertEquals(0, c2.compare(x, a));
		assertEquals(0, c2.compare(a, x));
		assertEquals(0, c2.compare(y, y));
		assertEquals(0, c2.compare(z, z));
		
		// CF
		assertEquals(-1, cf.compare(x, y));
		assertEquals(1, cf.compare(y,x));
		
		assertEquals(0, cf.compare(x, x));
		assertEquals(0, cf.compare(y, y));
		assertEquals(0, cf.compare(z, z));
		
		// test sorted sets
		TreeSet<SkyPoint> se = new TreeSet<SkyPoint>(r.getDimComparator(0));
		TreeSet<SkyPoint> es = new TreeSet<SkyPoint>(r.getDimComparator(1));
		se.add(x);
		es.add(x);
		se.add(y);
		es.add(y);
		se.add(z);
		es.add(z);
		se.add(a);
		es.add(a);
		
		assertEquals(3, se.size());
		assertEquals(3, es.size());
		
		Set<SkyPoint> seSet = new HashSet<SkyPoint> (se);
		Set<SkyPoint> esSet = new HashSet<SkyPoint> (es);
		assertEquals(seSet, esSet);
		
		if (log.isDebugEnabled()) {log.debug(se);};
		if (log.isDebugEnabled()) {log.debug(es);};
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
		
		// generate ranking
		while(r.hasNext()) {
 			IExplanationSet s = r.next();
			if (log.isDebugEnabled()) {log.debug(s);};
		}
		
		// compare
		Field rankingField = SkylineRanker.class.getDeclaredField("ranking");
		rankingField.setAccessible(true);
		List<SkyPoint> ranking = (List<SkyPoint>) rankingField.get(r);  
		
		Field solutionsField = SkylineRanker.class.getDeclaredField("solutions");
		solutionsField.setAccessible(true);
		TreeSet<SkyPoint> solutions = (TreeSet<SkyPoint>) solutionsField.get(r);
		
		if (log.isDebugEnabled()) {log.debug(ranking);};
		if (log.isDebugEnabled()) {log.debug(solutions);};
		
		Iterator<SkyPoint> ra = ranking.iterator(), so = solutions.iterator();
		while(ra.hasNext() || so.hasNext()) {
			assertTrue(ra.hasNext());
			assertTrue(so.hasNext());
			assertEquals(ra.next(), so.next());
		}
		
		if (log.isDebugEnabled()) {log.debug(ranking);};
		
		// compare solutions with single ranker
		Field rankersField = SkylineRanker.class.getDeclaredField("rankers");
		rankersField.setAccessible(true);
		IPartitionRanker[] rankers = (IPartitionRanker[]) rankersField.get(r);
		IPartitionRanker p = rankers[0];
		assertEquals(p.getNumberPrefetched(), r.getNumberPrefetched());
		
		Set<IExplanationSet> skySol = new HashSet<IExplanationSet> ();
		Set<IExplanationSet> seSol = new HashSet<IExplanationSet> ();
		
		r.resetIter();
		p.resetIter();
		
		while(p.hasNext()) {
			skySol.add(r.next());
			seSol.add(p.next());
		}
		
		assertEquals("" + skySol + seSol, skySol, seSol);
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
		
		// rank partions fully
		Field rankersField = SkylineRanker.class.getDeclaredField("rankers");
		rankersField.setAccessible(true);
		IPartitionRanker[] rankers = (IPartitionRanker[]) rankersField.get(r);
		for (IPartitionRanker pr: rankers)
			pr.rankFull();
		
		// rank r
		r.rankFull();
		
		assertEquals(rankers[0].getNumberPrefetched(), r.getNumberPrefetched());
		
		// compare
		while(r.hasNext()) {
 			IExplanationSet s = r.next();
			if (log.isDebugEnabled()) {log.debug(s);};
		}
		
		if (log.isDebugEnabled()) {log.debug(e);};
	}

	
}
