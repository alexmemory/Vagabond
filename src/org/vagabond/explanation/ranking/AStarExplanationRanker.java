package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerComparators;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.BitMatrix;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.ewah.IntIterator;

/**
 * Incremental A*-search based ranking of explanations. We sort the markers for
 * which we have to create full explanations (which ordering is used is
 * unimportant for correctness). Starting from all possible explanations for the
 * first marker in order (say e1) we iteratively extend these partial solutions
 * with possible explanations for the next marker in order. Partial solutions
 * are stored in a priority queue. We keep a pointer {@code lastDoneElem} into
 * the queue that stores the position of the currently last fully ranked
 * explanation. In each step we consider the direct successor of
 * {@code lastDoneElem}. If it is a partial solution, lets say for errors up to
 * e_i, then we extend this explanations set with all explanations for e_i+1 and
 * insert each of these (partial) solutions into the queue. If the direct
 * successor of {@code lastDoneElem} is a full solution then we have found the
 * next ranking result and increase {@code lastDoneElem}.
 * 
 * @author lord_pretzel
 *
 */

//TODO add methods IExplaantioRanker interface and implement them here for accessing confirmed explanations
//and errors

public class AStarExplanationRanker implements IExplanationRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			AStarExplanationRanker.class);

	// represents a set of explanations
	public class RankedListElement {
		public int[] elem;
		public int min;
		public int max;
		public int realScore = 0;
		public int countSet;
		public int firstUnset;

		public RankedListElement(int[] elem) {
			this.elem = elem;
			countSet = elem.length;

			updateFirstUnset();

			computeScore(this);
		}

		public RankedListElement(int maxSize, int... elems) {
			elem = new int[maxSize];
			countSet = elems.length;

			// initialize unset elements with -1
			for (int i = elems.length; i < maxSize; i++)
				elem[i] = -1;

			// set elements and implied elements
			for (int i = 0; i < elems.length; i++) {
				int newElem = elems[i];
				elem[i] = newElem;

				for (int j = 0; j < explainsMatrix[i][newElem].length; j++) {
					int pos = explainsMatrix[i][newElem][j];

					if (this.elem[pos] == -1) {
						this.elem[pos] = -2;
						countSet++;
					}
				}
			}

			updateFirstUnset();

			computeScore(this);
		}

		public RankedListElement(RankedListElement prefix, int newElem) {
			this.elem = Arrays.copyOf(prefix.elem, prefix.elem.length);
			this.min = prefix.min;
			this.max = prefix.max;
			this.countSet = prefix.countSet;
			this.realScore = prefix.realScore;
			this.firstUnset = prefix.firstUnset;
			this.elem[prefix.firstUnset] = newElem;
			countSet++;

			// set implied explanations and new element
			for (int i = 0; i < explainsMatrix[prefix.firstUnset][newElem].length; i++) {
				int pos = explainsMatrix[prefix.firstUnset][newElem][i];
				if (this.elem[pos] == -1) {
					this.elem[pos] = -2;
					countSet++;
				}
			}

			// adapt the side effect size
			updateFirstUnset();
			computeScore(this);

			if (log.isDebugEnabled()) {
				if (log.isDebugEnabled()) {
					log.debug("extended set " + prefix.toString() + " to "
							+ toString());
				}
				;
			}
		}

		private void updateFirstUnset() {
			for (int i = 0; i < elem.length; i++)
				if (elem[i] == -1) {
					firstUnset = i;
					return;
				}
		}

		public boolean isDone() {
			return (countSet == elem.length);
		}

		public String toString() {
			StringBuilder result = new StringBuilder();

			result.append("SORTED[");
			for (int i : elem) {
				result.append(i);
				result.append(',');
			}
			result.deleteCharAt(result.length() - 1);
			result.append("] of length " + countSet);
			result.append(" with min " + min);
			result.append(" and max " + max);

			return result.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof RankedListElement))
				return false;
			return rankComp.compare(this, (RankedListElement) o) == 0;
		}

		public boolean extensionWithoutOverlap() {
			for (int i = 0; i < explainsMatrix[firstUnset].length; i++) {
				boolean hasOverlap = false;
				int[] overlap = explainsMatrix[firstUnset][i];

				for (int j : overlap)
					if (j < firstUnset)
						hasOverlap = true;
				if (!hasOverlap)
					return true;
			}
			return false;
		}

		public boolean lastAdditionHasOverlap() {
			for (int i = elem.length - 1; i > 0; i--) {
				if (elem[i] > -1) {
					for (int j = 0; j < explainsMatrix[i][elem[i]].length; j++) {
						if (explainsMatrix[i][elem[i]][j] < i)
							return true;
					}
					return false;
				}
			}
			return false;
		}

		public boolean hasSame(RankedListElement o, int pos) {
			int exNum, offset = 0;
			IntIterator iter;

			// get ids of same explanations
			for (int i = 0; i < pos; i++)
				offset += errorExpl.get(i).size();
			exNum = offset + o.elem[pos];

			iter = sameExpl.getRowIntIter(exNum);

			// is any of the same explanations used in this ranked list element?
			while (iter.hasNext()) {
				int elemNum = iter.next();
				int candExNum = elemNum, errorPos = 0;
				while (candExNum >= errorExpl.get(errorPos).size()) {
					candExNum -= errorExpl.get(errorPos).size();
					errorPos++;
				}
				if (elem[errorPos] == candExNum)
					return true;
			}

			return false;
		}
	}

	public static final Comparator<RankedListElement> rankComp = new Comparator<RankedListElement>() {

		@Override
		public int compare(RankedListElement o1, RankedListElement o2) {
			if (o1 == o2)
				return 0;
			if (o1.min != o2.min)
				return o1.min - o2.min;
			if (o1.max != o2.max)
				return o1.max - o2.max;
			if (o1.countSet != o2.countSet)
				return o2.countSet - o1.countSet;
			for (int i = 0; i < o1.elem.length; i++) {
				// not the same element, and not elements are the same
				if (o1.elem[i] != o2.elem[i]) {
					// check with real element and not placeholder -2 for
					// element set elsewhere
					RankedListElement checker = o1.elem[i] == -2 ? o1 : o2;
					RankedListElement other = (checker == o1) ? o2 : o1;
					if (!checker.hasSame(other, i))
						return o1.elem[i] - o2.elem[i];
				}
			}

			return 0;
		}
	};

	// represents an ordering of the explanations for one error and min/max
	// side-effect sizes
	protected class OneErrorExplSet extends IdMap<IBasicExplanation> {
		private int minSE;
		private int maxSE;
		private ISingleMarker error;

		public OneErrorExplSet(IExplanationSet explSet, ISingleMarker error) {
			super();
			this.error = error;
			List<IBasicExplanation> expList;

			expList = explSet.getExplanations();

			// sort according scoring function
			Collections.sort(expList,
					RankerFactory.getScoreTotalOrderComparator(scoringFunction));
			for (IBasicExplanation e : expList)
				put(e);
			minSE = scoringFunction.getScore(expList.get(0));
			maxSE = scoringFunction.getScore(expList.get(expList.size() - 1));
		}

		public int getMinSE() {
			return minSE;
		}

		public void setMinSE(int minSE) {
			this.minSE = minSE;
		}

		public int getMaxSE() {
			return maxSE;
		}

		public void setMaxSE(int maxSE) {
			this.maxSE = maxSE;
		}

		public ISingleMarker getError() {
			return error;
		}

		public void setError(ISingleMarker error) {
			this.error = error;
		}

		public String toString() {
			StringBuilder result = new StringBuilder();

			result.append("ONE ERROR EXPL for <");
			result.append(error);
			result.append(" subsuming expls \n");
			result.append(this.idToObj.values().toString());
			result.append("\nwith min " + minSE + " and max " + maxSE);

			return result.toString();
		}

	}

	public static final Comparator<OneErrorExplSet> oneElemComp = new Comparator<AStarExplanationRanker.OneErrorExplSet>() {

		@Override
		public int compare(OneErrorExplSet arg0, OneErrorExplSet arg1) {
			int span1, span2, comp;

			span1 = arg0.maxSE - arg0.minSE;
			span2 = arg1.maxSE - arg1.minSE;
			comp = span2 - span1;
			if (comp != 0)
				return comp;

			comp = arg0.minSE - arg1.minSE;
			if (comp != 0)
				return comp;

			comp = arg0.maxSE - arg1.maxSE;
			if (comp != 0)
				return comp;

			return MarkerComparators.singleMarkerComp.compare(arg0.error,
					arg1.error);
		}
	};

	// fields
	private int iterPos = -1;
	private int iterDone = -1;
	private int numSets = -1;
	private int numErrors = -1;
	private RankedListElement lastDoneElem;
	private RankedListElement curIterElem;
	private TreeSet<RankedListElement> sortedSets;
	private List<OneErrorExplSet> errorExpl;
	private IMarkerSet errors;
	private List<ISingleMarker> errorList;
	private int[][][] explainsMatrix;
	private ExplanationCollection explCollection;
	private int[] combinedMin;
	private int[] combinedMax;
	private boolean init = false;
	private boolean rankingDone = false;
	private IScoringFunction scoringFunction;
	private BitMatrix sameExpl;

	public AStarExplanationRanker(IScoringFunction function) {
		sortedSets = new TreeSet<RankedListElement>(rankComp);
		errorExpl = new ArrayList<OneErrorExplSet>();
		errors = MarkerFactory.newMarkerSet();
		errorList = new ArrayList<ISingleMarker>();
		this.scoringFunction = function;
	}

	/**
	 * Generate OneErrorExplSets for each error and initialize the sortedSets
	 */

	@Override
	public void initialize(ExplanationCollection collection) {
		int j, numExpl;

		numSets = 1;
		this.explCollection = collection;

		numExpl = 0;
		for (int i : collection.getNumExpls())
			numExpl += i;

		sameExpl = new BitMatrix(numExpl, numExpl);

		explainsMatrix = new int[explCollection.getErrorExplMap().keySet().size()][][];

		// create set of errors
		for (ISingleMarker m : explCollection.getErrorExplMap().keySet()) {
			errors.add(m);
		}

		collection.computeRealSEAndExplains();

		// create data structures for the explanation sets for each error
		for (ISingleMarker m : explCollection.getErrorExplMap().keySet()) {
			IExplanationSet e = explCollection.getErrorExplMap().get(m);
			OneErrorExplSet newOne = new OneErrorExplSet(e, m);
			errorExpl.add(newOne);
			numSets *= newOne.size();
		}

		// sort on min-max span to improve pruning
		Collections.sort(errorExpl, oneElemComp);
		for (OneErrorExplSet newOne : errorExpl)
			errorList.add(newOne.error);

		// remove errors from side-effect to guarantee correct ranking
		j = 0;
		for (OneErrorExplSet newOne : errorExpl) {
			generateExplainsMatrix(newOne, j);
			j++;
		}

		// find out which explanations are the same
		List<IBasicExplanation> allExpl = new ArrayList<IBasicExplanation>();

		for (OneErrorExplSet newOne : errorExpl) {
			for (int k = 0; k < newOne.size(); k++)
				allExpl.add(newOne.get(k));
		}

		for (int i = 0; i < allExpl.size(); i++) {
			for (int k = 0; k < allExpl.size(); k++) {
				if (i != k && allExpl.get(i).equals(allExpl.get(k)))
					sameExpl.setSym(i, k);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("set same explanations: " + sameExpl.toString());
		}
		;

		numErrors = errors.size();

		// initialize min/max sums
		combinedMin = new int[numErrors];
		combinedMax = new int[numErrors];

		for (int i = 0; i < combinedMin.length; i++) {
			OneErrorExplSet one = errorExpl.get(numErrors - i - 1);
			combinedMin[numErrors - i - 1] = one.minSE;
			combinedMax[numErrors - i - 1] = one.maxSE;
		}

		if (log.isDebugEnabled()) {
			for (int i = 0; i < numErrors; i++) {
				if (log.isDebugEnabled()) {
					log.debug("min " + combinedMin[i] + " max "
							+ combinedMax[i] + " for " + errorList.get(i)
							+ "\n" + errorExpl.get(i));
				}
				;
			}
		}

		// initialize the sorted list with one expl, explanations
		for (int i = 0; i < errorExpl.get(0).size(); i++) {
			sortedSets.add(new RankedListElement(numErrors, i));
		}

		init = true;
	}

	public TreeSet<RankedListElement> getRanking() {
		return sortedSets;
	}

	private void generateExplainsMatrix(OneErrorExplSet expl, int pos) {
		List<Integer> overlaps;
		IBasicExplanation e;
		IMarkerSet overlap;

		explainsMatrix[pos] = new int[expl.size()][];

		for (int i = 0; i < expl.size(); i++) {
			overlaps = new ArrayList<Integer>();
			e = expl.get(i);

			// get other errors that are covered
			overlap = errors.cloneSet().intersect(e.getRealExplains());
			for (ISingleMarker error : overlap) {
				int errorPos = getPosForError(error);
				if (errorPos != pos)
					overlaps.add(errorPos);
			}
			Collections.sort(overlaps);
			explainsMatrix[pos][i] = new int[overlaps.size()];
			for (int j = 0; j < overlaps.size(); j++)
				explainsMatrix[pos][i][j] = overlaps.get(j);

			if (log.isDebugEnabled())
				LoggerUtil.logArray(log, explainsMatrix[pos][i], "[" + pos
						+ "," + i + "] is ");
		}
	}

	private int getPosForError(ISingleMarker error) {
		return errorList.indexOf(error);
	}

	private void generateUpTo(int upTo) {

		while (iterDone <= upTo) {
			RankedListElement curCand;

			// skip ranked, complete elements
			if (iterDone == -1)
				curCand = sortedSets.first();
			else
				curCand = sortedSets.higher(lastDoneElem);

			if (curCand == null)
				throw new NoSuchElementException(
						"trying to access beyond last " + "element of ranking");

			// current best candidate is not complete, expand it
			if (!curCand.isDone())
				expandAndInsert(curCand);
			
			// current best candidate is complete, check if best incomplete candidate
			// cannot be better than this one (best.max <= incomplete.min),
			// if so, increase iterDone until this condition does not hold anymore
			else {
				int curPos = iterDone + 1;
				RankedListElement inclCand = curCand;

				// find first incomplete set if exists
				while (curPos != sortedSets.size() && inclCand.isDone()) {
					inclCand = sortedSets.higher(inclCand);
					curPos++;
				}

				// everything complete -> we are done with ranking
				if (inclCand == null) {
					rankingDone = true;
					numSets = sortedSets.size();
					iterDone = sortedSets.size() - 1;
					lastDoneElem = sortedSets.last();
					// requested non existing set?
					if (iterDone < upTo)
						throw new NoSuchElementException(
								"trying to access beyond last "
										+ "element of ranking");
					return;
				}

				lastDoneElem = sortedSets.lower(inclCand);
				iterDone = curPos - 1;

				// expand the best incomplete element
				expandAndInsert(inclCand);
			}
		}

	}

	/*
	 * Add all possible sets from one error to a set, remove the original set
	 * from the list and add the extended sets to the list.
	 */
	private void expandAndInsert(RankedListElement curExp) {
		boolean disOverlap;

		disOverlap = curExp.extensionWithoutOverlap();
		sortedSets.remove(curExp);

		for (int i = 0; i < errorExpl.get(curExp.firstUnset).size(); i++) {
			RankedListElement newOne = new RankedListElement(curExp, i);
			if (log.isDebugEnabled()) {
				log.debug("Was included? : " + sortedSets.contains(newOne)
						+ "\n" + newOne);
			}
			;
			if ((!disOverlap || !newOne.lastAdditionHasOverlap())
					&& !sortedSets.contains(newOne))
				sortedSets.add(newOne);
		}
	}

	@Override
	public IExplanationSet next() {
		advanceIter();
		return getSetForRankedListElem(curIterElem);
	}

	private void advanceIter() {
		if (rankingDone && iterPos + 1 >= numSets)
			throw new NoSuchElementException("only " + numSets + " elements");

		if (iterPos + 1 > iterDone)
			generateUpTo(iterPos + 1);

		if (iterPos + 1 > iterDone)
			throw new NoSuchElementException("only " + numSets + " elements");

		iterPos++;
		if (curIterElem == null)
			curIterElem = sortedSets.first();
		else
			curIterElem = sortedSets.higher(curIterElem);
	}

	private IExplanationSet getSetForRankedListElem(RankedListElement elem) {
		IExplanationSet result = ExplanationFactory
				.newExplanationSet(ExplanationComparators.sameElemComp);

		for (int i = 0; i < elem.elem.length; i++)
			if (elem.elem[i] > -1)
				result.addUnique(errorExpl.get(i).get(elem.elem[i]));

		if (log.isDebugEnabled())
			if (log.isDebugEnabled()) {
				log.debug("set for iter is \n" + result.toString());
			}
		;

		return result;
	}

	//TODO @BSMP: this is what you need to change
	//add the confirmed explanations here to compute real score involving confirmed explanations
	private void computeScore(RankedListElement elem) {
		ArrayList<IBasicExplanation> sets = new ArrayList<IBasicExplanation>();

		elem.min = 0;
		elem.max = 0;
		for (int i = 0; i < elem.elem.length; i++) {
			if (elem.elem[i] > -1)
				sets.add(errorExpl.get(i).get(elem.elem[i]));
			else if (elem.elem[i] != -2) {
				elem.min = Math.max(combinedMin[i], elem.min);
				elem.max += combinedMax[i];
			}
		}

		elem.realScore = scoringFunction.getScore(sets);
		if (elem.isDone()) {
			elem.min = elem.realScore;
			elem.max = elem.realScore;
		} else {
			elem.min = Math.max(elem.realScore, elem.min);
			elem.max = elem.realScore + elem.max;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		if (rankingDone && iterPos < iterDone)
			return true;
		if (!rankingDone && iterPos == iterDone)
			generateUpTo(iterDone + 1);

		return iterPos < iterDone;
	}

	@Override
	public boolean ready() {
		return init;
	}

	@Override
	public void confirmExplanation(IBasicExplanation correctExpl) {
		//TODO Auto-generated method stub
		
		//TODO 1) keep confirmed explanations and explained errors in separate fields
		// find all 
		
		//TODO 2) NOT THAT (store away current explanation set (at current position)) restart next
		
		//TODO 3) adapt ExplanationCollection
		
		//TODO 4) wipe internal data structures off the ranker 
		
		//TODO 5) call initialize to restart the ranker
		
		//TODO 6) merge confirmed explanations for scoring
	}

	@Override
	public int getNumberOfExplSets() {
		return numSets;
	}

	@Override
	public void resetIter() {
		iterPos = -1;
		curIterElem = null;
	}

	@Override
	public boolean isFullyRanked() {
		return rankingDone;
	}

	@Override
	public int getIterPos() {
		return iterPos + 1;
	}

	@Override
	public IExplanationSet previous() {
		if (--iterPos < 0)
			throw new NoSuchElementException("try to get element before first");
		curIterElem = sortedSets.lower(curIterElem);
		return getSetForRankedListElem(curIterElem);
	}

	@Override
	public boolean hasPrevious() {
		return iterPos > 0;
	}

	@Override
	public int getNumberPrefetched() {
		if (log.isDebugEnabled()){
			if (log.isDebugEnabled()) {
				log.debug("ITER DONE " + (iterDone + 1) + " incomplete "
						+ (sortedSets.size() - iterDone - 1));
			}
		}
		return iterDone + 1;
	}

	@Override
	public IExplanationSet getRankedExpl(int rank) {
		int oldIterPos = iterPos;
		IExplanationSet result;

		assert (rank > 0 && (!rankingDone || iterDone >= rank));
		if (!rankingDone)
			generateUpTo(rank);

		if (iterPos > rank)
			resetIter();

		while (iterPos < rank)
			advanceIter();

		result = getSetForRankedListElem(curIterElem);

		resetIter();
		while (iterPos < oldIterPos)
			advanceIter();

		return result;
	}

	@Override
	public boolean hasAtLeast(int numElem) {
		if (rankingDone)
			return numSets >= numElem;
		try {
			generateUpTo(numElem - 1);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	@Override
	public int getScore(int rank) { // TODO improve performance by score
		return scoringFunction.getScore(getRankedExpl(rank));
	}

	public IScoringFunction getF() {
		return scoringFunction;
	}

	public void setF(IScoringFunction scoringFunction) {
		this.scoringFunction = scoringFunction;
	}

	@Override
	public void rankFull() {
		try {
			generateUpTo(Integer.MAX_VALUE);
		} catch (NoSuchElementException e) {
			if (log.isDebugEnabled()) {
				log.debug("ranking done");
			}
		}
		resetIter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vagabond.explanation.ranking.IExplanationRanker#getScoreF()
	 */
	@Override
	public IScoringFunction getScoringFunction() {
		return this.scoringFunction;
	}

}
