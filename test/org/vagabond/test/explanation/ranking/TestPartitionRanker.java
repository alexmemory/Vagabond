package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.vagabond.explanation.ranking.IPartitionRanker;
import org.vagabond.explanation.ranking.PartitionRanker.FullExplSummary;
import org.vagabond.explanation.ranking.PartitionRanker;
import org.vagabond.explanation.ranking.RankerFactory;
import org.vagabond.explanation.ranking.AStarExplanationRanker;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.test.AbstractVagabondTest;

public class TestPartitionRanker extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestPartitionRanker.class);

	private ErrorPartitionGraph g;
	private PartitionExplanationGenerator explGen =
			new PartitionExplanationGenerator();
	private ExplanationSetGenerator explSetGen = new ExplanationSetGenerator();
	private ErrorPartitioner parter = new ErrorPartitioner();
	private IPartitionRanker ranker;
	private PartitionRanker pr;

	private void setUp(String filename) throws Exception {
		loadToDB(filename);
		g = new ErrorPartitionGraph();
		explGen.init();
	}

	@Test
	public void testFullExplSummarys() {
		FullExplSummary e1, e2, e3;

		pr = new PartitionRanker(SideEffectSizeScore.inst);

		e1 = pr.new FullExplSummary(new int[] { 1, 2, 3 });
		e2 = pr.new FullExplSummary(new int[] { 1, 2, 3 });
		e3 = pr.new FullExplSummary(new int[] { 2, 2, 3 });

		assertEquals(e1, e2);
		assertEquals(e2, e1);
		assertEquals(0, e1.compareTo(e2));
		assertEquals(0, e2.compareTo(e1));

		assertFalse(e1.equals(e3));
		assertEquals(-1, e1.compareTo(e3));
		assertEquals(1, e3.compareTo(e1));
	}

	@Test
	public void testSimplePartitionSideEffect() throws Exception {
		setUp("resource/test/severalComps.xml");

		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1");
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");

		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);

		PartitionedMarkerSet mPart = parter.partitionMarkers(g, m);

		ExplPartition ex = new ExplPartition(mPart);

		ExplanationCollection col1 =
				explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		ex.add(col1);
		col1.createRanker(RankerFactory.createRanker("SideEffect"));
		ExplanationCollection col2 =
				explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		ex.add(col2);
		col2.createRanker(RankerFactory.createRanker("SideEffect"));

		ExplPartition e = explGen.findExplanations(m);

		if (log.isDebugEnabled()) {log.debug(e);};

		ranker = RankerFactory.createPartRanker("SideEffect", e);

		AStarExplanationRanker er1 = (AStarExplanationRanker) col1.getRanker();
		AStarExplanationRanker er2 = (AStarExplanationRanker) col2.getRanker();

		AStarExplanationRanker r1 =
				(AStarExplanationRanker) (((PartitionRanker) ranker)
						.getRankerForPart(0));
		AStarExplanationRanker r2 =
				(AStarExplanationRanker) (((PartitionRanker) ranker)
						.getRankerForPart(1));

		// check individual rankers
		er1.resetIter();
		r1.resetIter();

		while (er1.hasNext())
			assertEquals(er1.next().getSideEffectSize(), r1.next()
					.getSideEffectSize());

		er2.resetIter();
		r2.resetIter();

		while (er2.hasNext())
			assertEquals(er2.next().getSideEffectSize(), r2.next()
					.getSideEffectSize());

		// check number of explanations
		assertTrue(r1.isFullyRanked());
		assertEquals(5, r1.getNumberPrefetched());

		assertTrue(r2.isFullyRanked());
		assertEquals(3, r2.getNumberPrefetched());

		// rank completely
		while (ranker.hasNext())
			ranker.next();
		ranker.resetIter();

		assertTrue(ranker.hasNext());

		ArrayList<IExplanationSet> results = new ArrayList<IExplanationSet>();
		while (ranker.hasNext())
			results.add(ranker.next());

		assertTrue(ranker.isFullyRanked());

		assertEquals(ranker.toString(), 15, ranker.getNumberOfExplSets());

		for (int i = 1; i < results.size(); i++) {
			IExplanationSet s1, s2;
			s1 = results.get(i - 1);
			s2 = results.get(i);
			assertTrue("For Expls: " + s1 + "\n\nand\n\n" + s2,
					s2.getSideEffectSize() >= s1.getSideEffectSize());
		}

		if (log.isDebugEnabled()) {log.debug(ranker);};
	}

	@Test
	public void testSimplePartitionExplSize() throws Exception {
		setUp("resource/test/severalComps.xml");

		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1");
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");

		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);

		PartitionedMarkerSet mPart = parter.partitionMarkers(g, m);

		ExplPartition ex = new ExplPartition(mPart);

		ExplanationCollection col1 =
				explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		ex.add(col1);
		col1.createRanker(RankerFactory.createRanker("ExplSize"));
		ExplanationCollection col2 =
				explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		ex.add(col2);
		col2.createRanker(RankerFactory.createRanker("ExplSize"));

		ExplPartition e = explGen.findExplanations(m);

		if (log.isDebugEnabled()) {log.debug(e);};

		ranker = RankerFactory.createPartRanker("ExplSize", e);

		AStarExplanationRanker er1 = (AStarExplanationRanker) col1.getRanker();
		AStarExplanationRanker er2 = (AStarExplanationRanker) col2.getRanker();

		AStarExplanationRanker r1 =
				(AStarExplanationRanker) (((PartitionRanker) ranker)
						.getRankerForPart(0));
		AStarExplanationRanker r2 =
				(AStarExplanationRanker) (((PartitionRanker) ranker)
						.getRankerForPart(1));

		// check individual rankers
		er1.resetIter();
		r1.resetIter();

		while (er1.hasNext())
			assertEquals(er1.next().getSideEffectSize(), r1.next()
					.getSideEffectSize());

		er2.resetIter();
		r2.resetIter();

		while (er2.hasNext())
			assertEquals(er2.next().getSideEffectSize(), r2.next()
					.getSideEffectSize());

		// rank completely
		while (ranker.hasNext())
			ranker.next();
		ranker.resetIter();

		assertTrue(ranker.hasNext());

		ArrayList<IExplanationSet> results = new ArrayList<IExplanationSet>();
		while (ranker.hasNext())
			results.add(ranker.next());

		assertTrue(ranker.isFullyRanked());

		assertEquals(ranker.toString(), 15, ranker.getNumberOfExplSets());
		assertEquals(ranker.toString(), 15, ranker.getNumberPrefetched());

		for (int i = 1; i < results.size(); i++) {
			IExplanationSet s1, s2;
			s1 = results.get(i - 1);
			s2 = results.get(i);
			assertTrue("For Expls: " + s1 + "\n\nand\n\n" + s2,
					s2.size() >= s1.size());
		}

		if (log.isDebugEnabled()) {log.debug(ranker);};
	}

	@Test
	public void testGenerateUpTo() throws Exception {
		setUp("resource/test/severalComps.xml");

		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1");
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");

		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);

		PartitionedMarkerSet mPart = parter.partitionMarkers(g, m);

		ExplPartition ex = new ExplPartition(mPart);

		ExplanationCollection col1 =
				explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		ex.add(col1);
		col1.createRanker(RankerFactory.createRanker("ExplSize"));
		ExplanationCollection col2 =
				explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		ex.add(col2);
		col2.createRanker(RankerFactory.createRanker("ExplSize"));

		ExplPartition e = explGen.findExplanations(m);

		if (log.isDebugEnabled()) {log.debug(e);};

		ranker = RankerFactory.createPartRanker("ExplSize", e);

		AStarExplanationRanker er1 = (AStarExplanationRanker) col1.getRanker();
		AStarExplanationRanker er2 = (AStarExplanationRanker) col2.getRanker();

		AStarExplanationRanker r1 =
				(AStarExplanationRanker) (((PartitionRanker) ranker)
						.getRankerForPart(0));
		AStarExplanationRanker r2 =
				(AStarExplanationRanker) (((PartitionRanker) ranker)
						.getRankerForPart(1));

		// check individual rankers
		er1.resetIter();
		r1.resetIter();

		while (er1.hasNext())
			assertEquals(er1.next().getSideEffectSize(), r1.next()
					.getSideEffectSize());

		er2.resetIter();
		r2.resetIter();

		while (er2.hasNext())
			assertEquals(er2.next().getSideEffectSize(), r2.next()
					.getSideEffectSize());

		// rank completely
		assertTrue(ranker.hasNext());

		if (log.isDebugEnabled()) {log.debug(ranker);};
	}

	@Test
	public void useTwoPartRankers() throws Exception {
		setUp("resource/test/severalComps.xml");

		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1");
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");

		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);

		ExplPartition e = explGen.findExplanations(m);

		if (log.isDebugEnabled()) {log.debug(e);};

		PartitionRanker pr1 =
				(PartitionRanker) RankerFactory.createPartRanker("ExplSize", e);
		pr1.rankFull();
		PartitionRanker pr2 =
				(PartitionRanker) RankerFactory.createPartRanker("SideEffect",
						e);
		pr2.rankFull();

		assertTrue(pr1.isFullyRanked());
		assertTrue(pr2.isFullyRanked());

		assertEquals(15, pr1.getNumberOfExplSets());
		assertEquals(15, pr1.getNumberPrefetched());
		assertEquals(15, pr2.getNumberPrefetched());

		assertEquals(pr1.getNumberOfExplSets(), pr2.getNumberOfExplSets());

		pr1.resetIter();
		pr2.resetIter();
		Set<IExplanationSet> res1, res2;
		res1 = new HashSet<IExplanationSet>();
		res2 = new HashSet<IExplanationSet>();

		while (pr1.hasNext()) {
			res1.add(pr1.next());
			res2.add(pr2.next());
		}

		assertTrue(res1.equals(res2));

		if (log.isDebugEnabled()) {log.debug(pr1 + "\n\n" + pr2);};
	}

	@Test
	public void testSetIterAndGetHigherScore() throws Exception {
		setUp("resource/test/severalComps.xml");

		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1");
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");

		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);

		ExplPartition e = explGen.findExplanations(m);

		if (log.isDebugEnabled()) {log.debug(e);};

		ranker = RankerFactory.createPartRanker("SideEffect", e);

		// rank completely
		List<IExplanationSet> sol = new ArrayList<IExplanationSet>();

		ranker.rankFull();
		ranker.resetIter();
		while (ranker.hasNext()) {
			sol.add(ranker.next());
		}

		// get list of ranked explanations and check that it contains the same
		// explanation sets as the tree ranking
		Field rankedExplField =
				PartitionRanker.class.getDeclaredField("rankedExpls");
		rankedExplField.setAccessible(true);
		ArrayList<FullExplSummary> rankedExpl =
				(ArrayList<FullExplSummary>) rankedExplField.get(ranker);

		if (log.isDebugEnabled()) {log.debug(rankedExpl);};

		Field rankingField = PartitionRanker.class.getDeclaredField("ranking");
		rankingField.setAccessible(true);
		TreeSet<FullExplSummary> ranking =
				(TreeSet<FullExplSummary>) rankingField.get(ranker);

		if (log.isDebugEnabled()) {log.debug(ranking);};

		for (FullExplSummary s : ranking) {
			assertTrue("" + s, rankedExpl.contains(s));
		}

		// test get higher score
		IExplanationSet e1 = ranker.getExplWithHigherScore(0);
		assertEquals(sol.get(1), e1);

		IExplanationSet e2 = ranker.getExplWithHigherScore(1);
		assertEquals(sol.get(7), e2);
		if (log.isDebugEnabled()) {log.debug(sol);};

		try {
			IExplanationSet e3 = ranker.getExplWithHigherScore(2);
			assertFalse(true);
		}
		catch (NoSuchElementException ex) {
			if (log.isDebugEnabled()) {log.debug(ex);};
		}
	}

}
