package org.vagabond.explanation.ranking;

import static org.vagabond.util.HashFNV.fnv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.SimpleExplanationSet;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;

/**
 * An incremental skyline ranker for explanation sets. Given a set of scoring 
 * functions and incremental rankers for each scoring function, this class
 * computes the skyline.
 * 
 * @author lord_pretzel
 *
 */
public class SkylineRanker implements IPartitionRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(SkylineRanker.class);
	
	private static final int NO_SKYLINE = Integer.MAX_VALUE;
	
	// store a point in the space we are computing the skyline and the 
	// explanation set associated with this point
	public class SkyPoint implements Comparable<SkyPoint>{
		
		public int[] scores;
		public IExplanationSet solution = null;
		public int skyLine = NO_SKYLINE; // belongs to skyline X
		private int hashCode = -1;
		
		public SkyPoint () {
			this.scores = new int[rankers.length];
			Arrays.fill(this.scores, -1);			
		}
		
		public SkyPoint (IExplanationSet solution, int dimension, int score) {
			this();
			this.solution = solution;
			this.scores[dimension] = score;
			computeScores();
		}
		
		public SkyPoint (IExplanationSet solution) {
			this();
			this.solution = solution;
			computeScores();
		}
		
		@Override
		public int hashCode () {
			if (hashCode == -1) {
				hashCode = fnv(scores);
				hashCode = fnv(solution, hashCode);
			}
			return hashCode;
		}
		
		@Override
		public boolean equals (Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other instanceof SkyPoint) {
				SkyPoint o = (SkyPoint) other;
				
				for (int i = 0; i < scores.length; i++)
					if (scores[i] != o.scores[i])
						return false;
				
				return solution.equals(o.solution);
			}
			
			return false;
		}
		
		public boolean equalRank (SkyPoint other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			for (int i = 0; i < scores.length; i++)
				if (scores[i] != other.scores[i])
					return false;
		
			return true;
		}
		
		@Override
		public int compareTo(SkyPoint o) {
			if (o == null)
				throw new NullPointerException();
			
			for(int i = 0; i < scores.length; i++) {
				if (scores[i] < o.scores[i])
					return -1;
				if (scores[i] > o.scores[i])
					return 1;
			}
			
			return 0;
		}
		
		public void computeScores () {
			assert(solution != null);
			for(int i = 0; i < scores.length; i++) {
				if (scores[i] == -1)
					scores[i] = funcs[i].getScore(solution);
			}
		}
		
		public int dominationCompare (SkyPoint o) {
			boolean thisDom = true, oDom = true;
			
			for(int i = 0; i < scores.length; i++) {
				if (scores[i] < o.scores[i])
					oDom = false;
				if (scores[i] > o.scores[i])
					thisDom = false;
			}
			
			if (thisDom && oDom)
				return 0;
			if (thisDom)
				return -1;
			if (oDom)
				return 1;
			return 0;
		}
		
		public boolean dominates (SkyPoint o) {
			return dominationCompare(o) == -1;
		}
		
		@Override
		public String toString () {
			StringBuffer result = new StringBuffer();
			
			result.append("SP(");
			result.append(Arrays.toString(scores));
			result.append("| ");
			result.append(skyLine);
			result.append("| ");
			result.append(((SimpleExplanationSet) solution).toSummaryString());
			result.append(")");
			
			return result.toString();
		}
		
		public String toVerboseString () {
			StringBuffer result = new StringBuffer();
			
			result.append(toString());
			result.append("\n-----------------------\n");
			result.append(solution.toString());
			
			return result.toString();
		}
		
	}
	
	// skypoint factory data
	private HashMap<IExplanationSet, SkyPoint> points;
	private final SkyPoint least;
	
	// fields
	private IPartitionRanker[] rankers;
	private IScoringFunction[] funcs;
	private int dim;
	private Comparator<IExplanationSet> finalRanker;
	
	private int numSkylines = 0;
	private List<Integer> SLsizes;
	private List<Integer> SLpos;
	private List<TreeSet<SkyPoint>> sortedDims;
	private TreeSet<SkyPoint> solutions; 
	private List<SkyPoint> ranking;
	private int numRanked = 0;
	private boolean rankDone = false;
	
	private SkyPoint[] dimCandIterPos;
	private SkyPoint lastSkylinePoint;
	private int[] dimUpperBounds;
	private int[] dimLowerBounds; 
	private boolean[] useCands;
	
	private int iterPos;
	
	public SkylineRanker (String[] rankSchemes, String finalScheme) {
		dim = rankSchemes.length;
		points = new HashMap<IExplanationSet, SkyPoint> ();
		rankers = new IPartitionRanker[dim];
		funcs = new IScoringFunction[dim];
		sortedDims = new ArrayList<TreeSet<SkyPoint>> ();
		ranking = new ArrayList<SkyPoint> ();
		dimUpperBounds = new int[dim];
		dimLowerBounds = new int[dim];
		Arrays.fill(dimLowerBounds, -1);
		dimCandIterPos = new SkyPoint[dim];
		least = new SkyPoint();
		Arrays.fill(dimCandIterPos, least);
		useCands = new boolean[dim];
		Arrays.fill(useCands, true);
		initFields();
		
		for(int i = 0; i < dim; i++) {
			rankers[i] = RankerFactory.createPartRanker(rankSchemes[i]);
			funcs[i] = RankerFactory.getScoreFunction(rankSchemes[i]);
			sortedDims.add(new TreeSet<SkyPoint> (getDimComparator(i)));
		}
		
		finalRanker = RankerFactory.getScoreExplSetComparator(finalScheme);
		solutions = new TreeSet<SkyPoint> (getFinalSortComparator());
	}

	private void initFields() {
		iterPos = -1;
		
		rankDone = false;
		SLsizes = new ArrayList<Integer> ();
		SLpos = new ArrayList<Integer> ();
		numRanked = 0;
		numSkylines = 0;
		lastSkylinePoint = null;
		Arrays.fill(dimUpperBounds, Integer.MAX_VALUE);
	}

	@Override
	public void initialize(ExplPartition part) {
		for(int i = 0; i < dim; i++)
			rankers[i].initialize(part);
		
		initFields();
	}

	public SkyPoint newSkyPoint (IExplanationSet expl) {
		if (!points.containsKey(expl)) {
			points.put(expl, this.new SkyPoint(expl));
		}
				
		return points.get(expl);
	}

	private void resetSkyPointFactory () {
		points.clear();
	}

	public Comparator<SkyPoint> getDimComparator (final int dimension) {
		return new Comparator<SkyPoint> () {
	
			private int dim = dimension;
			
			@Override
			public int compare(SkyPoint o1, SkyPoint o2) {
				int lVal = o1.scores[dim], rVal = o2.scores[dim];
				if (lVal < rVal)
					return -1;
				if (lVal > rVal)
					return 1;
				
				int comp = o1.compareTo(o2);
				if (comp != 0)
					return comp;
				
				comp = ExplanationComparators.setIndElementComp.compare(o1.solution, o2.solution);
				
				return comp;
			}
			
		};
	}

	public Comparator<SkyPoint> getFinalSortComparator () {
		return new Comparator<SkyPoint> () {
	
			@Override
			public int compare(SkyPoint o1, SkyPoint o2) {
				if (o1 == o2)
					return 0;
				
				if (o1.skyLine < o2.skyLine)
					return -1;
				if (o1.skyLine > o2.skyLine)
					return 1;
				
				int fComp  = finalRanker.compare(o1.solution, o2.solution);
				
				if (fComp != 0)
					return fComp;
				
				// incomparable points compare hash to enforce order
				int pointComp = o1.compareTo(o2);
				
				if (pointComp != 0)
					return pointComp;
				
				if (o1.hashCode() < o2.hashCode())
					return -1;
				if (o1.hashCode() > o2.hashCode())
					return 1;
	
				return ExplanationComparators.setIndElementComp.compare(o1.solution, o2.solution);
			}
			
		};
	}

	@Override
	public boolean hasNext() {
		if (rankDone) 
			return iterPos < numRanked - 1;
		
		// try to add a new skyline
		if (iterPos == numRanked - 1)
			addSkyline();	
		
		return iterPos < numRanked;
	}
	
	private void addSkyline () {
		numSkylines++;
		
		// 2 dim is easier
		if (dim == 2) {
			add2DimSkyline();
		}
		else {
//TODO
		}
		
		// update skyline info
		if (numSkylines != 1) {
			SLsizes.add(solutions.size() - SLsizes.get(numSkylines - 2));
			SLpos.add(SLpos.get(numSkylines - 2) + SLsizes.get(numSkylines - 2));
		}
		else {
			SLpos.add(0);
			SLsizes.add(solutions.size());
		}
		numRanked = solutions.size();
		
		resetAfterSkyline();
	}
	
	private void add2DimSkyline () { 		
		// contiune to produce candiates for skyline points until we computed
		// the whole skyline
		while (getNextDimCand(0) && getNextDimCand(1)) 
			;
	}
	
	private boolean getNextDimCand (int dimen) {
		int oDim = 1 - dimen;
		SkyPoint x;
		
		x = getNextPoint(dimen);
		if (x != null) {
			// has lower value than the current best one in this dimension
			// add to skyline
			if (x.scores[0] < dimUpperBounds[0] && x.scores[1] < dimUpperBounds[1]) {
				// update boundaries
				dimUpperBounds[oDim] = x.scores[oDim];
				addSkylinePoint(x);
			} 
			// has excatly the same scores as the last skyline point
			// add to skyline
			else if (x.equalRank(lastSkylinePoint)) {
				addSkylinePoint(x);
			}
			// breached the upper bound, finished with this skyline
			else if (x.scores[dimen] > dimUpperBounds[dimen])
				return false;
			
			return true;
		}
		if (solutions.size() == rankers[dimen].getNumberOfExplSets())
			rankDone = true;
		return false;
	}
	
	private void addNextSkylineCand (SkyPoint x) {
		if (!solutions.contains(x))
			for(int i = 0; i < dim; i++)
				sortedDims.get(i).add(x);
	}
	
	private void addSkylinePoint (SkyPoint x) {
		if (solutions.contains(x))
			return;
		
		x.skyLine = numSkylines - 1;
		solutions.add(x);
		// remove from candidate set
		for(int i = 0; i < dim; i++)
			sortedDims.get(i).remove(x);
		for(int i = 0; i < dim; i++)
			dimLowerBounds[i] = Math.max(dimLowerBounds[i], x.scores[i]);
		lastSkylinePoint = x;
	}
	
	private SkyPoint getNextPoint (int dimen) {
		SkyPoint x;
		IExplanationSet expl;
		TreeSet<SkyPoint> cands = sortedDims.get(dimen); 
		
		// still have candidates produced during last skyline?
		try {
			while(useCands[dimen] && (cands.ceiling(dimCandIterPos[dimen]) != null)) {
				x = cands.higher(dimCandIterPos[dimen]);
				dimCandIterPos[dimen] = x;
				return x;
			}
			
//			if (useCands[dimen] && dimLowerBounds[dimen] != -1)
				useCands[dimen] = false;
			
			if (rankers[dimen].hasNext()) {
				expl = rankers[dimen].next();
				x = newSkyPoint(expl);
				addNextSkylineCand(x);
				return x;
			}
		} catch (NoSuchElementException e) {
			LoggerUtil.logDebugException(e, log);
		}
		
		return null;
	}

	private void resetAfterSkyline () {
		Arrays.fill(dimUpperBounds, Integer.MAX_VALUE);
		Arrays.fill(useCands, true);
		
		for(int i = 0; i < dim; i++)
			dimCandIterPos[i] = least;			
		
		// add new skyline points to total ranking using the correct ordering
		List<SkyPoint> newSols = new ArrayList<SkyPoint> ();
		Iterator<SkyPoint> iter = solutions.descendingIterator();
		SkyPoint x;
		
		while (iter.hasNext()) {
			x = iter.next();
			if (x.skyLine != numSkylines - 1)
				break;
			newSols.add(x);
		}
		
		for(int i = newSols.size() -1; i >= 0; i--)
			ranking.add(newSols.get(i));
	}
	
	@Override
	public IExplanationSet next() {
		assert(iterPos < numRanked - 1 || !rankDone);
		generateUpTo(iterPos + 1);
		return getRankedExpl(++iterPos);
	}

	private void generateUpTo (int pos) {
		while(!rankDone && pos >= numRanked)
			addSkyline();
		if (numRanked <= pos)
			throw new NoSuchElementException("no more solutions there: " + numRanked);
	}
	
	@Override
	public long getIterPos() {
		return iterPos;
	}

	@Override
	public IExplanationSet previous() {
		if (hasPrevious())
			return getRankedExpl(--iterPos);
		throw new NoSuchElementException();
	}

	@Override
	public boolean hasPrevious() {
		return iterPos >= 0;
	}

	@Override
	public long getNumberOfExplSets() {
		if (rankDone)
			return numRanked;
		
		for(IPartitionRanker rank: rankers) {
			if(rank.isFullyRanked())
				return rank.getNumberOfExplSets();
		}
		
		return -1L;
	}

	@Override
	public boolean isFullyRanked() {
		return rankDone;
	}

	@Override
	public long getNumberPrefetched() {
		return numRanked;
	}

	@Override
	public void resetIter() {
		iterPos = -1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPerPartitionRanker(String rankScheme) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPerPartitionRanker() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IExplanationSet getRankedExpl(int pos) {
		assert(pos < ranking.size());
		return ranking.get(pos).solution;
	}

	/**
	 * Skyline score is the rank for now
	 */
	@Override
	public IExplanationSet getExplWithHigherScore(int score) {
		return getRankedExpl(score);
	}

	@Override
	public void iterToScore(int score) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public String toString () {
		StringBuffer result = new StringBuffer();
		
		result.append("Skyline ranker:\n");
		result.append(ranking);
		
		return result.toString();
	}

	@Override
	public void rankFull() {
		for(int i = 0; i < rankers.length; i++)
			rankers[i].rankFull();
		
		if (log.isDebugEnabled()) {log.debug("Number of expl in full ranking " + rankers[0].getNumberPrefetched());};
		
		generateUpTo((int) rankers[0].getNumberPrefetched() - 1);
	}

	/* (non-Javadoc)
	 * @see org.vagabond.explanation.ranking.IPartitionRanker#getScore(int)
	 */
	@Override
	public int getScore(int rank) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.vagabond.explanation.ranking.IPartitionRanker#getScoreF()
	 */
	@Override
	public IScoringFunction getScoreF() {
		return null;
	}
	
}
