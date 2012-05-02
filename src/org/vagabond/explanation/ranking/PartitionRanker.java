package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.ewah.JavaUtilBitSet;
import org.vagabond.util.ewah.IBitSet.BitsetType;

import com.skjegstad.utils.BloomFilter;

public class PartitionRanker implements IPartitionRanker {

	static Logger log = Logger.getLogger(PartitionRanker.class);
	
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
			log.debug("Compute Score for: " + this.toString());
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
			
			result.append("F(");
			result.append(Arrays.toString(iterPos));
			result.append(")");
			
			if (seInit) {
				result.append(" and SE (");
				result.append(Arrays.toString(scores));
				result.append(")");
			}
			
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
	private BloomFilter<FullExplSummary> createdTest;
	
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
		
		log.debug("cur elem: " + curIterElem.toString());
		
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
			if (iterDoneElem == null) {
				rankDone = true;
				numExplSets = ranking.size();
			}
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
						rankedExpls.add(iterDoneElem);
					}
				}
			}
			if (iterDoneElem == null) {
				rankDone = true;
				numExplSets = ranking.size();
			}
		}
	}
	
	private boolean addExtended (FullExplSummary elem) {
		boolean doneWork = false;
		
		// create first ranked
		if (elem == null) {
			iterDoneElem = new FullExplSummary(part.size(), 0);
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
			// bloom filter (O(1)) and in case the bloom filter answers yes we make sure
			// by checking the ranking (O(log n)).
			if (rankers[i].hasAtLeast(ex.iterPos[i] + 1)   
					&& !(createdTest.contains(ex) && ranking.contains(ex))) {
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
			result.union(part.getCol(i).getRankedExpl(pos.iterPos[i]));
		
		return result;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initialize(ExplPartition part) {
		initialize(part, 10.0d, 3, BitsetType.JavaBitSet);
	}

	public void initialize(ExplPartition part, double bitPerKey, int k, BitsetType type) {
		int numParts;
		int exNumElem = 1;
		
		this.part = part;
		numParts = part.size();
		
		iterPos = -1;
		iterDone = -1;
		iterHead = new FullExplSummary(numParts, -1);
		curIterElem = iterHead;
		
		rankers = new IExplanationRanker[numParts];
		
		for(int i = 0; i < part.size(); i++) {
			ExplanationCollection col = part.get(i);
			rankers[i] = RankerFactory.createRanker(rankScheme);
			rankers[i].initialize(col);
			
			exNumElem *= CollectionUtils.product(col.getDimensions()); //TODO check boundaries
		}
		
		createdTest = new BloomFilter<FullExplSummary> (bitPerKey, exNumElem, k, type); //TODO implement dynamic bloom filter?
		
		log.debug("Bloom Filter false positive rate: " + createdTest.getFalsePositiveProbability());
		
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
		
		long result = 1L;
		
		for(int i = 0; i < part.size(); i++) {
			if (part.getCol(i).getRanker().isFullyRanked())
				return -1L;
			else 
				result *= part.getCol(i).getRanker().getNumberOfExplSets();
		}
		
		if (result != -1)
			numExplSets = result;
		
		return numExplSets;
	}

	@Override
	public long getNumberPrefetched() {
		return iterDone;
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

	@Override
	public IExplanationSet getExplWithHigherScore(int score) {
		return generateExplanation(rankedExpls.get(getSumWithHigherScore(score)));
	}
	
	private int getSumWithHigherScore (int score) {
		FullExplSummary highest = null;
		int pos, step;
		
		if (rankedExpls.size() > 0)
			highest = rankedExpls.get(rankedExpls.size() - 1);
		
		// do not have 
		if (highest == null || highest.totalScore < score)
			generateUpToScore(score + 1);
		
		highest = rankedExpls.get(rankedExpls.size() - 1);
		if (highest.totalScore < score)
			throw new NoSuchElementException();
		
		pos = rankedExpls.size() / 2;
		step = rankedExpls.size() / 4;
		
		do {
			int value = rankedExpls.get(pos).totalScore; 
			
			if (value < score)
				pos -= step;
			if (value > score)
				pos += step;
			if (value == score)
				return pos;
			
			step /= 2;
		} while(step > 0);
		
		// no element with same score, but higher one, return this one
		if (pos == 0)
			return 0;
		if (pos < rankedExpls.size() - 1)
			return ++pos;
		
		throw new NoSuchElementException ();
	}

	@Override
	public void iterToScore(int score) {
		int pos = getSumWithHigherScore(score);
		FullExplSummary h  = rankedExpls.get(pos);
		iterPos = pos;
		curIterElem = h;
	}

	@Override
	public void rankFull() {
		generateUpTo(getNumberOfExplSets());
	}
	
}
