package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.ewah.IBitSet.BitsetType;

public class PartitionRanker implements IPartitionRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(PartitionRanker.class);
	
	public static final int MAX_BITSET_SIZE = (int) (64 * Math.pow(1024, 3)); // 64 MB
	
	
	
	// store an iterator position
	public class FullExplSummary implements Comparable<FullExplSummary> {
		
		protected int[] iterPos;
		protected int[] scores;
		protected int totalScore;
		protected boolean seInit = false;
		protected int lastSet = -1;
		protected IExplanationSet solution = null;
		private int hash = -1;
		
		public FullExplSummary (int[] iterPos) {
			this.iterPos = iterPos;
			this.scores = new int[iterPos.length];
		}
		
		public FullExplSummary (int size) {
			iterPos = new int[size];
			this.scores = new int[size];
		}
		
		public FullExplSummary (FullExplSummary e, int exPos) {
			this(e.iterPos.length);
			System.arraycopy(e.iterPos, 0, iterPos, 0, iterPos.length);
			iterPos[exPos]++;
			lastSet = exPos;
		}
		
		public FullExplSummary (int size, int defaultVal) {
			this(size);
			Arrays.fill(this.iterPos, 0, size, defaultVal);
		}
		
		@Override
		public boolean equals (Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			
			if (other instanceof FullExplSummary) {
				FullExplSummary e = (FullExplSummary) other;
				assert(iterPos.length == e.iterPos.length);	
				for(int i = 0; i < iterPos.length; i++) {
					if (iterPos[i] != e.iterPos[i])
						return false;
				}
				return true;
			}
			return false;
		}
		
		@Override
		public int hashCode () {
			if (hash == -1) {
				hash = 0;
				for(int i = 0; i < iterPos.length; i++)
					hash ^= iterPos[i];
			}
			return hash;
		}

		
		
		private void computeScore () {
			if (log.isDebugEnabled()) {log.debug("Compute Score for: " + this.toString());};
			totalScore = 0;
			for(int i = 0; i < iterPos.length; i++) {
//				ExplanationCollection col = part.get(i); 
				scores[i] = scoreF.getScore(rankers[i].getRankedExpl(iterPos[i])); 
				totalScore += scores[i];	
			}
			seInit = true;
		}
		
		@Override
		public String toString() {
			StringBuffer result = new StringBuffer();
			
			result.append("[F(");
			result.append(Arrays.toString(iterPos));
			result.append(")");
			
			if (seInit) {
				result.append(" and Score (");
				result.append(Arrays.toString(scores));
				result.append(")]");
			} 
			else 
				result.append(']');
			
			return result.toString();
		}

		@Override
		public int compareTo(FullExplSummary o) {
			if (o == null)
				return Integer.MAX_VALUE;
			if (o == this)
				return 0;	
			
			boolean oDom = true, thisDom = true;
				
			if (seInit && o.seInit) {
				if (totalScore < o.totalScore)
					return -1;
				if (totalScore > o.totalScore)
					return 1;

				// break ties
				for(int i = 0; i < iterPos.length; i++) {
					if (iterPos[i] < o.iterPos[i])
						return -1;
					if (iterPos[i] > o.iterPos[i])
						return 1;
				}
				return 0;
			}

			// check if one is clearly dominated by the other (all positions smaller/bigger)
			for(int i = 0; i < iterPos.length; i++) {
				if (iterPos[i] < o.iterPos[i])
					thisDom = false;
				if (iterPos[i] > o.iterPos[i])
					oDom = false;
			}

			if (oDom && thisDom)
				return 0;
			if (oDom)
				return -1;
			if (thisDom)
				return 1;

			// else need to get size effect sizes
			if (!seInit)
				computeScore();
			if (!o.seInit)
				o.computeScore();

			return this.compareTo(o); // ok done initialization, do again
		}
	}
	
	// fields
	private String rankScheme = "SideEffect";
	private FullExplSummary iterHead = null;
	
	private long iterPos = -1L;
	private long iterDone = 0L;
	private FullExplSummary iterDoneElem = null;
	private FullExplSummary curIterElem = null;
	private TreeSet<FullExplSummary> ranking;
	private HashSet<FullExplSummary> createdTest;
	
	private ArrayList<FullExplSummary> rankedExpls;
	private boolean cacheFullExpl = false;	
	private boolean rankDone = false;
	
	private long numExplSets = -1; //do not know this upfront without exhausting every individual ranker
	private ExplPartition part;
	private IExplanationRanker[] rankers;
	private IScoringFunction scoreF;
	
	public PartitionRanker (IScoringFunction scoreF) {
		rankedExpls = new ArrayList<FullExplSummary> ();
		ranking = new TreeSet<FullExplSummary> ();
		this.scoreF = scoreF;
	}
	
	public PartitionRanker (IScoringFunction scoreF, boolean cacheFullExpl) {
		this(scoreF);
		this.cacheFullExpl = cacheFullExpl;
	}
	
	@Override
	public boolean hasNext() {
		if (iterPos < iterDone)
			return true;
		
		generateUpTo(iterPos + 1);
		
		return iterPos < iterDone;
	}

	@Override
	public IExplanationSet next() {
		assert(iterPos < numExplSets - 1);
		if (iterDone < ++iterPos)
			generateUpTo(iterPos);
		
		curIterElem = ranking.higher(curIterElem);
		
		if (log.isDebugEnabled()) {log.debug("cur elem: " + curIterElem.toString());};
		
		return generateExplanation (curIterElem);
	}

	private IExplanationSet generateExplanation (FullExplSummary iterElPos) {
		IExplanationSet result;
		
		if (iterElPos.solution != null)
			return iterElPos.solution;
		
//		if (cacheFullExpl && rankedExpls.size() > iterPos)
//			return rankedExpls.get((int) iterPos);
	
		result = getFullExpl(iterElPos);
		
		if (cacheFullExpl)
			iterElPos.solution = result;
		
		return result;
	}

	private void generateUpTo (long pos) {
		if (!rankDone) {
			while(iterDone < pos && (iterDoneElem !=  null || iterDone == -1)) {
				if (!addExtended(iterDoneElem)) {
					iterDoneElem = ranking.higher(iterDoneElem);
					if (iterDoneElem != null) {
						iterDone++;
						rankedExpls.add(iterDoneElem);
					}
				}
			}
			updateRankedExpls();
		}
	}
	
	private void generateUpToScore (long pos) {
		assert(pos >= 0);
		
		if (!rankDone) {
			while(iterDone == -1 || (iterDoneElem !=  null && iterDoneElem.totalScore < pos)) {
				if (!addExtended(iterDoneElem)) {
					iterDoneElem = ranking.higher(iterDoneElem);
					if (iterDoneElem != null) {
						iterDone++;
					}
				}
			}
			updateRankedExpls();
		}
	}
	
	private void updateRankedExpls () {
		FullExplSummary sum;
		
		if (iterDoneElem == null)
			finishRanking();
		
		sum = (rankedExpls.size() == 0) ? iterHead 
				: rankedExpls.get(rankedExpls.size() - 1);
		
		while (iterDone >= rankedExpls.size()) {
			sum = ranking.higher(sum);
			rankedExpls.add(sum);
		}
	}
	
	private void finishRanking () {
		rankDone = true;
		numExplSets = ranking.size();
		FullExplSummary s = ranking.last();
		while (!s.seInit) {
			s.computeScore();
			s = ranking.lower(s);
		}
	}
	
	private boolean addExtended (FullExplSummary elem) {
		boolean doneWork = false;
		
		// create first ranked
		if (elem == null) {
			iterDoneElem = new FullExplSummary(part.size(), 0);
			iterDoneElem.computeScore();
			ranking.add(iterDoneElem);
			createdTest.add(iterDoneElem);
			iterDone++;
			
			return true;
		}
		
		// generate all extensions
		for(int i = 0; i < elem.iterPos.length; i ++) {
			FullExplSummary ex = new FullExplSummary(elem, i); 
			// if extension is not outside the solution space and has
			// not been produced before (this is test by first checking the
			// hash set (O(1))) 
			if (rankers[i].hasAtLeast(ex.iterPos[i] + 1)   
					&& !createdTest.contains(ex)) {
				doneWork = true;
				ranking.add(ex);
				createdTest.add(ex);
			}
		}
		
		if (doneWork) {
			iterDone++;
			iterDoneElem = ranking.higher(iterDoneElem);
		}
		
		return doneWork;
	}

	private IExplanationSet getFullExpl (FullExplSummary pos) {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		
		for(int i = 0; i < pos.iterPos.length; i++)
			result.union(rankers[i].getRankedExpl(pos.iterPos[i]));
		
		return result;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initialize(ExplPartition part) {
		initialize(part, BitsetType.JavaBitSet);
	}

	public void initialize(ExplPartition part, BitsetType type) {
		int numParts;
		
		this.part = part;
		numParts = part.size();
		
		iterPos = -1;
		iterDone = -1;
		iterHead = new FullExplSummary(numParts, -1);
		curIterElem = iterHead;
		
		rankers = new IExplanationRanker[numParts];
		
		for(int i = 0; i < part.size(); i++) {
			ExplanationCollection col = part.get(i);
			if (log.isDebugEnabled())
				log.debug("number of errors for part " + i + " is: " + col.getNumErrors());
			rankers[i] = RankerFactory.createRanker(rankScheme);
			rankers[i].initializeCollection(col);
		}
		
		createdTest = new HashSet<FullExplSummary> ();
		
		generateUpTo(0);
	}

	
	@Override
	public void setPerPartitionRanker(String rankScheme) {
		this.rankScheme = rankScheme;
		
	}

	@Override
	public String getPerPartitionRanker() {
		return rankScheme;
	}
	
	public IExplanationRanker getRankerForPart (int part) {
		assert(part > 0 && part < rankers.length);
		
		return rankers[part];
	}

	@Override
	public long getIterPos() {
		return iterPos;
	}

	@Override
	public IExplanationSet previous() {
		assert(hasPrevious());
		
		curIterElem = ranking.lower(curIterElem);
		iterPos--;
		return generateExplanation (curIterElem);
	}

	@Override
	public boolean hasPrevious() {
		return iterPos > 0;
	}

	@Override
	public long getNumberOfExplSets() {
		if (numExplSets != -1) 
			return numExplSets;
		
		if (rankDone) {
			numExplSets = rankedExpls.size();
			return numExplSets;
		}
		
		long result = 1L;
		
		for(int i = 0; i < part.size(); i++) {
			if (!rankers[i].isFullyRanked())
				return -1L;
			else 
				result *= rankers[i].getNumberOfExplSets();
		}
		
		if (result != -1)
			numExplSets = result;
		
		return numExplSets;
	}

	@Override
	public long getNumberPrefetched() {
		return iterDone + 1;
	}

	@Override
	public void resetIter() {
		iterPos = -1;
		curIterElem = iterHead;
	}

	@Override
	public boolean isFullyRanked() {
		return rankDone;
	}

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer();
		
		buf.append("PARTITIONED SE RANKER:\n\n");
		buf.append(ranking.toString());
		
		return buf.toString();
	}

	@Override
	public IExplanationSet getRankedExpl(int pos) {
		FullExplSummary sum;
		
		if (rankDone && numExplSets <= pos)
			throw new NoSuchElementException();
		
		if (iterDone < pos)
			generateUpTo(pos);
		
		if (iterDone < pos)
			throw new NoSuchElementException();
		
		sum = rankedExpls.get(pos);
		
		return generateExplanation (sum);
	}
	
	/**
	 * 
	 * @see org.vagabond.explanation.ranking.IPartitionRanker#getExplWithHigherScore(int)
	 */
	@Override
	public IExplanationSet getExplWithHigherScore(int score) {
		return generateExplanation(rankedExpls.get(getCeilingScore(score + 1)));
	}
	
	/**
	 * 
	 * @param score
	 * @return
	 */
	private int getCeilingScore (int score) {
		FullExplSummary highest = null;
		int min, max, mid;
		
		generateUpToScore(score + 1);
		
		if (iterDone >= 0)
			highest = rankedExpls.get(rankedExpls.size() - 1);
		
		// do not have 
		if (highest == null || highest.totalScore < score)
			throw new NoSuchElementException();
		
		max = rankedExpls.size();
		min = 0;
		
		do {
			mid = ((max - min) / 2) + min ;
			int value = rankedExpls.get(mid).totalScore; 
			
			if (value < score)
				min = mid + 1;
			if (value > score)
				max = mid;
			// if we found a match, find first ranked element with this score
			if (value == score) {
				while (mid > 0 && rankedExpls.get(mid - 1).totalScore == score)
					mid--;
				return mid;
			}
		} while(min != max);
		
		// no element with same score, but higher one, return this one
		while(mid < rankedExpls.size()) {
			if (rankedExpls.get(mid).totalScore > score)
				return mid;
			mid++;
		}
		
		throw new NoSuchElementException ();
	}

	@Override
	public void iterToScore(int score) {
		int pos = getCeilingScore(score);
		FullExplSummary h  = rankedExpls.get(pos);
		iterPos = pos;
		curIterElem = h;
	}

	@Override
	public void rankFull() {
		for(int i = 0; i < rankers.length; i++) {
			rankers[i].rankFull();
			rankers[i].resetIter();
		}
		
		while(hasNext())
			next();
		
		resetIter();
	}
	
	@Override
	public int getScore(int rank) { //TODO improve perf by 
		return scoreF.getScore(getRankedExpl(rank));
	}

	/* (non-Javadoc)
	 * @see org.vagabond.explanation.ranking.IPartitionRanker#getScoreF()
	 */
	@Override
	public IScoringFunction getScoringFunction() {
		return this.scoreF;
	}

}
