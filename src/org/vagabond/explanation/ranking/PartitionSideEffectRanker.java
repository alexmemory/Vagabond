package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;

public class PartitionSideEffectRanker implements IPartitionRanker {

	static Logger log = Logger.getLogger(PartitionSideEffectRanker.class);
	
	private String rankScheme = "SideEffect";

	// store an iterator position
	private class FullExplanation implements Comparable {
		
		protected int[] iterPos;
		protected int[] seSizes;
		protected int totalSe;
		protected boolean seInit = false;
		protected int lastSet = -1;
		private int hash = -1;
		
		public FullExplanation (int size) {
			iterPos = new int[size];
			this.seSizes = new int[size];
		}
		
		public FullExplanation (FullExplanation e, int exPos) {
			this(e.iterPos.length);
			System.arraycopy(e.iterPos, 0, iterPos, 0, iterPos.length);
			iterPos[exPos]++;
			lastSet = exPos;
		}
		
		public FullExplanation (int size, int defaultVal) {
			this(size);
			Arrays.fill(this.iterPos, 0, size, defaultVal);
		}
		
		@Override
		public boolean equals (Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			
			if (other instanceof FullExplanation) {
				FullExplanation e = (FullExplanation) other;
				
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
				hash = 1;
				for(int i = 0; i < iterPos.length; i++)
					hash = 13 * hash + iterPos[i];
			}
				
			return hash;
		}

		@Override
		public int compareTo(Object other) {
			if (other == null)
				return Integer.MAX_VALUE;
			if (other == this)
				return 0;
			
			if (other instanceof FullExplanation) {
				FullExplanation o = (FullExplanation) other;
				boolean oDom = true, thisDom = true;
				
				if (seInit && o.seInit) {
					if (totalSe < o.totalSe)
						return -1;
					if (totalSe > o.totalSe)
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
				
				if (oDom)
					return -1;
				if (thisDom)
					return 1;
				
				// else need to get size effect sizes
				if (!seInit)
					getSe();
				if (!o.seInit)
					o.getSe();
				
				return this.compareTo(o); // ok done initialization, do again
			}
			
			throw new ClassCastException();
		}
		
		private void getSe () {
			log.debug("Compute SE for: " + this.toString());
			totalSe = 0;
			for(int i = 0; i < iterPos.length; i++) {
				ExplanationCollection col = part.get(i); 
				seSizes[i] = col.getRankedExpl(iterPos[i]).getSideEffectSize(); 
				totalSe += seSizes[i];	
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
				result.append(Arrays.toString(seSizes));
				result.append(")");
			}
			
			return result.toString();
		}
	}
	
	// fields
	private FullExplanation iterHead = null;
	
	private long iterPos = -1L;
	private long iterDone = 0L;
	private FullExplanation iterDoneElem = null;
	private FullExplanation curIterElem = null;
	private TreeSet<FullExplanation> ranking;
	
	private ArrayList<IExplanationSet> rankedExpls;
	private boolean cacheFullExpl = false;	
	private boolean rankDone = false;
	
	private long numExplSets = -1; //do not know this upfront without exhausting every individual ranker
	private ExplPartition part;
	
	public PartitionSideEffectRanker () {
		rankedExpls = new ArrayList<IExplanationSet> ();
		ranking = new TreeSet<FullExplanation> ();
	}
	
	public PartitionSideEffectRanker (boolean cacheFullExpl) {
		this();
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

	private IExplanationSet generateExplanation (FullExplanation iterElPos) {
		IExplanationSet result;
		
		if (cacheFullExpl && rankedExpls.size() > iterPos)
			return rankedExpls.get((int) iterPos);
	
		result = getFullExpl(iterElPos);
		
		if (cacheFullExpl)
			rankedExpls.add(result);
		
		return result;
	}

	private void generateUpTo (long pos) {
		if (!rankDone) {
			while(iterDone < pos && (iterDoneElem !=  null || iterDone == -1)) {
				if (!addExtended(iterDoneElem)) {
					iterDoneElem = ranking.higher(iterDoneElem);
					if (iterDoneElem != null)
						iterDone++;
				}
			}
			if (iterDoneElem == null) {
				rankDone = true;
				numExplSets = ranking.size();
			}
		}
	}
	
	private boolean addExtended (FullExplanation elem) {
		boolean doneWork = false;
		
		// create first ranked
		if (elem == null) {
			iterDoneElem = new FullExplanation(part.size(), 0);
			ranking.add(iterDoneElem);
			iterDone++;
			
			return true;
		}
		
		for(int i = Math.max(0, elem.lastSet); i < elem.iterPos.length; i ++) {
			FullExplanation ex = new FullExplanation(elem, i);
			if (part.get(i).getRanker().hasAtLeast(ex.iterPos[i] + 1)) {
				doneWork = true;
				ranking.add(ex);
			}
		}
		
		if (doneWork) {
			iterDone++;
			iterDoneElem = ranking.higher(iterDoneElem);
		}
		
		return doneWork;
	}

	private IExplanationSet getFullExpl (FullExplanation pos) {
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
		int numParts;
		
		this.part = part;
		numParts = part.size();
		
		iterPos = -1;
		iterDone = -1;
		iterHead = new FullExplanation(numParts, -1);
		curIterElem = iterHead;
		
		for(ExplanationCollection col: part)
			col.createRanker(RankerFactory.createRanker("SideEffect"));
		
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
	
}
