package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
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

public class AStarExplanationRanker implements IExplanationRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(AStarExplanationRanker.class);

	// Represents a set of explanations
	
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

			// Initialize unset elements with -1
			for (int i = elems.length; i < maxSize; i++)
				elem[i] = -1;

			// Set elements and implied elements
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

		public RankedListElement(RankedListElement prefix, int newElement) {
			this.elem = Arrays.copyOf(prefix.elem, prefix.elem.length);
			this.min = prefix.min;
			this.max = prefix.max;
			this.countSet = prefix.countSet;
			this.realScore = prefix.realScore;
			this.firstUnset = prefix.firstUnset;
			this.elem[prefix.firstUnset] = newElement;
			countSet++;

			// Set implied explanations and new element
			for (int i = 0; i < explainsMatrix[prefix.firstUnset][newElement].length; i++) {
				int pos = explainsMatrix[prefix.firstUnset][newElement][i];
				if (this.elem[pos] == -1) {
					this.elem[pos] = -2;
					countSet++;
				}
			}

			// Adapt the side effect size
			updateFirstUnset();
			computeScore(this);

			if (log.isDebugEnabled()) {
				if (log.isDebugEnabled()) {
					log.debug("extended set " + prefix.toString() + " to " + toString());
				}
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
			IntIterator iterator;

			// get id's for the same explanation
			for (int i = 0; i < pos; i++)
				offset += errorExpl.get(i).size();
			
			exNum = offset + o.elem[pos];

			iterator = sameExplanations.getRowIntIter(exNum);

			// Is any of the same explanations used in this ranked list element?
			
			while (iterator.hasNext()) {
				int elementNumber = iterator.next();
				int candidateExplNumber = elementNumber, errorPos = 0;
				while (candidateExplNumber >= errorExpl.get(errorPos).size()) {
					candidateExplNumber -= errorExpl.get(errorPos).size();
					errorPos++;
				}
				if (elem[errorPos] == candidateExplNumber)
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
			List<IBasicExplanation> explanationList;

			explanationList = explSet.getExplanations();

			// Sort according the scoring function
			
			Collections.sort(explanationList,
					RankerFactory.getScoreTotalOrderComparator(scoringFunction));
			for (IBasicExplanation explanation : explanationList)
				put(explanation);
			minSE = scoringFunction.getScore(explanationList.get(0));
			maxSE = scoringFunction.getScore(explanationList.get(explanationList.size() - 1));
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
			int span1 = arg0.maxSE - arg0.minSE;
			int span2 = arg1.maxSE - arg1.minSE;
			int comp = span2 - span1;
			
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

	// TODO Reposition the Fields below
	
	private int iteratorPosition = -1;
	private int iterationDone = -1;
	private int numberOfSets = -1;
	private int numberOfErrors = -1;
	private RankedListElement lastDoneElem;
	private RankedListElement currentIteratorElement;
	private TreeSet<RankedListElement> sortedSets;
	private List<OneErrorExplSet> errorExpl;
	private IMarkerSet errors;
	private List<ISingleMarker> errorList;
	private int[][][] explainsMatrix;
	private ExplanationCollection explCollection;
	private int[] combinedMin;
	private int[] combinedMax;
	private boolean init = false;
	private boolean rankingIsDone = false;
	private IScoringFunction scoringFunction;
	private BitMatrix sameExplanations;
	private Set<IBasicExplanation> confirmedExplanations;
	private Set<ISingleMarker> confirmedMarkers;

	public AStarExplanationRanker(IScoringFunction function) {
		initializeListsAndSets();
		initializeConfirmations();
		this.scoringFunction = function;
	}
	
	private void initializeListsAndSets(){
		sortedSets = new TreeSet<RankedListElement>(rankComp);
		errorExpl = new ArrayList<OneErrorExplSet>();
		errors = MarkerFactory.newMarkerSet();
		errorList = new ArrayList<ISingleMarker>();
	}
	
	private void initializeConfirmations(){
		confirmedExplanations = new HashSet<IBasicExplanation>();
		confirmedMarkers = new HashSet<ISingleMarker>();
	}

	//TODO change explanationCollection
	
	/**
	 * Generate OneErrorExplSets for each error and initialize the sortedSets
	 */
	@Override
	public void initializeCollection(ExplanationCollection collection) {
		int j, numberOfExplanations;

		numberOfSets = 1;
		this.explCollection = collection;

		numberOfExplanations = 0;
		for (int numExpls : collection.getNumExpls())
			numberOfExplanations += numExpls;

		sameExplanations = new BitMatrix(numberOfExplanations, numberOfExplanations);

		explainsMatrix = new int[explCollection.getErrorExplMap().keySet().size()][][];

		// Create set of errors
		for (ISingleMarker marker : explCollection.getErrorExplMap().keySet()) {
			errors.add(marker);
		}

		collection.computeRealSEAndExplains();

		// Create data structures for the explanation sets for each error
		for (ISingleMarker m : explCollection.getErrorExplMap().keySet()) {
			IExplanationSet e = explCollection.getErrorExplMap().get(m);
			OneErrorExplSet newOne = new OneErrorExplSet(e, m);
			errorExpl.add(newOne);
			numberOfSets *= newOne.size();
		}

		// Sort on min-max span to improve pruning
		Collections.sort(errorExpl, oneElemComp);
		for (OneErrorExplSet newOne : errorExpl)
			errorList.add(newOne.error);

		// Remove errors from side-effect to guarantee correct ranking
		j = 0;
		for (OneErrorExplSet newOne : errorExpl) {
			generateExplainsMatrix(newOne, j);
			j++;
		}

		// Find out which explanations are the same
		List<IBasicExplanation> allExpl = new ArrayList<IBasicExplanation>();

		for (OneErrorExplSet newOne : errorExpl) {
			for (int k = 0; k < newOne.size(); k++)
				allExpl.add(newOne.get(k));
		}

		for (int i = 0; i < allExpl.size(); i++) {
			for (int k = 0; k < allExpl.size(); k++) {
				if (i != k && allExpl.get(i).equals(allExpl.get(k)))
					sameExplanations.setSym(i, k);
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("set same explanations: " + sameExplanations.toString());
		}

		numberOfErrors = errors.size();

		// Initialize min/max sums
		combinedMin = new int[numberOfErrors];
		combinedMax = new int[numberOfErrors];

		for (int i = 0; i < combinedMin.length; i++) {
			OneErrorExplSet oneError = errorExpl.get(numberOfErrors - i - 1);
			combinedMin[numberOfErrors - i - 1] = oneError.minSE;
			combinedMax[numberOfErrors - i - 1] = oneError.maxSE;
		}

		if (log.isDebugEnabled()) {
			for (int i = 0; i < numberOfErrors; i++) {
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
			sortedSets.add(new RankedListElement(numberOfErrors, i));
		}

		init = true;
	}

	public TreeSet<RankedListElement> getRanking() {
		return sortedSets;
	}

	private void generateExplainsMatrix(OneErrorExplSet explanation, int position) {
		List<Integer> overlaps;
		IBasicExplanation e;
		IMarkerSet overlap;

		explainsMatrix[position] = new int[explanation.size()][];

		for (int row = 0; row < explanation.size(); row++) {
			overlaps = new ArrayList<Integer>();
			e = explanation.get(row);

			// Get other errors that are covered
			overlap = errors.cloneSet().intersect(e.getRealExplains());
			for (ISingleMarker error : overlap) {
				int errorPosition = getPositionForError(error);
				if (errorPosition != position)
					overlaps.add(errorPosition);
			}
			Collections.sort(overlaps);
			explainsMatrix[position][row] = new int[overlaps.size()];
			for (int column = 0; column < overlaps.size(); column++)
				explainsMatrix[position][row][column] = overlaps.get(column);

			if (log.isDebugEnabled())
				LoggerUtil.logArray(log, explainsMatrix[position][row], "[" + position
						+ "," + row + "] is ");
		}
	}

	private int getPositionForError(ISingleMarker error) {
		return errorList.indexOf(error);
	}

	private void generateUpTo(int upTo) {

		while (iterationDone <= upTo) {
			RankedListElement currentCandidate;

			// Skip ranked, complete elements
			if (iterationDone == -1)
				currentCandidate = sortedSets.first();
			else
				currentCandidate = sortedSets.higher(lastDoneElem);

			if (currentCandidate == null)
				throw new NoSuchElementException(
						"trying to access beyond last " + "element of ranking");

			// Current best candidate is not complete, expand it
			if (!currentCandidate.isDone())
				expandAndInsert(currentCandidate);
			
			// Current best candidate is complete, check if best incomplete candidate
			// cannot be better than this one (best.max <= incomplete.min).
			// If so, increase iterDone until this condition does not hold anymore
			else {
				int currentPosition = iterationDone + 1;
				RankedListElement includeCandidate = currentCandidate;

				// find first incomplete set if exists
				while (currentPosition != sortedSets.size() && includeCandidate.isDone()) {
					includeCandidate = sortedSets.higher(includeCandidate);
					currentPosition++;
				}

				// everything complete -> we are done with ranking
				if (includeCandidate == null) {
					rankingIsDone = true;
					numberOfSets = sortedSets.size();
					iterationDone = sortedSets.size() - 1;
					lastDoneElem = sortedSets.last();
					// requested non existing set?
					if (iterationDone < upTo)
						throw new NoSuchElementException(
								"trying to access beyond last "
										+ "element of ranking");
					return;
				}

				lastDoneElem = sortedSets.lower(includeCandidate);
				iterationDone = currentPosition - 1;

				// expand the best incomplete element
				expandAndInsert(includeCandidate);
			}
		}

	}

	/*
	 * Add all possible sets from one error to a set, remove the original set
	 * from the list and add the extended sets to the list.
	 */
	private void expandAndInsert(RankedListElement currentExplanation) {
		boolean disOverlap = currentExplanation.extensionWithoutOverlap();
		sortedSets.remove(currentExplanation);

		for (int i = 0; i < errorExpl.get(currentExplanation.firstUnset).size(); i++) {
			RankedListElement newOne = new RankedListElement(currentExplanation, i);
			if (log.isDebugEnabled()) {
				log.debug("Was included? : " + sortedSets.contains(newOne)
						+ "\n" + newOne);
			}
			if ((!disOverlap || !newOne.lastAdditionHasOverlap())
					&& !sortedSets.contains(newOne))
				sortedSets.add(newOne);
		}
	}

	@Override
	public IExplanationSet next() {
		advanceIteration();
		return getSetForRankedListElem(currentIteratorElement);
	}

	private void advanceIteration() {
		if (rankingIsDone && iteratorPosition + 1 >= numberOfSets)
			throw new NoSuchElementException("only " + numberOfSets + " elements");

		if (iteratorPosition + 1 > iterationDone)
			generateUpTo(iteratorPosition + 1);

		if (iteratorPosition + 1 > iterationDone)
			throw new NoSuchElementException("only " + numberOfSets + " elements");

		iteratorPosition++;
		if (currentIteratorElement == null)
			currentIteratorElement = sortedSets.first();
		else
			currentIteratorElement = sortedSets.higher(currentIteratorElement);
	}

	private IExplanationSet getSetForRankedListElem(RankedListElement element) {
		IExplanationSet result = ExplanationFactory
				.newExplanationSet(ExplanationComparators.sameElemComp);

		for (int i = 0; i < element.elem.length; i++)
			if (element.elem[i] > -1)
				result.addUnique(errorExpl.get(i).get(element.elem[i]));

		if (log.isDebugEnabled())
			if (log.isDebugEnabled()) {
				log.debug("set for iter is \n" + result.toString());
			}

		return result;
	}

	//TODO @BSMP: this is what you need to change
	//add the confirmed explanations here to compute real score involving confirmed explanations
	private void computeScore(RankedListElement rankedElem) {
		ArrayList<IBasicExplanation> sets = new ArrayList<IBasicExplanation>();

		rankedElem.min = 0;
		rankedElem.max = 0;
		
		for (int i = 0; i < rankedElem.elem.length; i++) {
			if (rankedElem.elem[i] > -1)
				sets.add(errorExpl.get(i).get(rankedElem.elem[i]));
			else if (rankedElem.elem[i] != -2) {
				rankedElem.min = Math.max(combinedMin[i], rankedElem.min);
				rankedElem.max += combinedMax[i];
			}
		}
		
		rankedElem.realScore = scoringFunction.getScore(sets);
		
		if (rankedElem.isDone()) {
			rankedElem.min = rankedElem.realScore;
			rankedElem.max = rankedElem.realScore;
		} else {
			rankedElem.min = Math.max(rankedElem.realScore, rankedElem.min);
			rankedElem.max = rankedElem.realScore + rankedElem.max;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		if (rankingIsDone && iteratorPosition < iterationDone)
			return true;
		if (!rankingIsDone && iteratorPosition == iterationDone)
			generateUpTo(iterationDone + 1);

		return iteratorPosition < iterationDone;
	}

	@Override
	public boolean ready() {
		return init;
	}

	@Override
	public void confirmExplanation(IBasicExplanation correctExpl) {
		
		//TODO 0) keep confirmed explanations in a field
		
		confirmedExplanations.add(correctExpl);
		
		//TODO 1) keep confirmed errors in another field
		
		IMarkerSet correctMarkers = correctExpl.getRealExplains();
		confirmedMarkers.addAll(correctMarkers);
		
		//TODO 2) find all
		
		//TODO 3) NOT THAT (store away current explanation set (at current position)) restart next
		
		//TODO 4) adapt ExplanationCollection
		
		//TODO 5) wipe internal data structures off the ranker 
		
		errors.removeAll(correctMarkers);
		errorList.removeAll(correctMarkers);

		//TODO 6) call initialize to restart the ranker
		
		//TODO 7) merge confirmed explanations for scoring
	}

	@Override
	public int getNumberOfExplSets() {
		return numberOfSets;
	}

	@Override
	public void resetIter() {
		iteratorPosition = -1;
		currentIteratorElement = null;
	}

	@Override
	public boolean isFullyRanked() {
		return rankingIsDone;
	}

	@Override
	public int getIteratorPosition() {
		return iteratorPosition + 1;
	}

	@Override
	public IExplanationSet previous() {
		if (--iteratorPosition < 0)
			throw new NoSuchElementException("try to get element before first");
		currentIteratorElement = sortedSets.lower(currentIteratorElement);
		return getSetForRankedListElem(currentIteratorElement);
	}

	@Override
	public boolean hasPrevious() {
		return iteratorPosition > 0;
	}

	@Override
	public int getNumberPrefetched() {
		if (log.isDebugEnabled()){
			if (log.isDebugEnabled()) {
				log.debug("ITER DONE " + (iterationDone + 1) + " incomplete "
						+ (sortedSets.size() - iterationDone - 1));
			}
		}
		return iterationDone + 1;
	}

	@Override
	public IExplanationSet getRankedExpl(int rank) {
		int oldIterPos = iteratorPosition;
		IExplanationSet result;

		assert (rank > 0 && (!rankingIsDone || iterationDone >= rank));
		if (!rankingIsDone)
			generateUpTo(rank);

		if (iteratorPosition > rank)
			resetIter();

		while (iteratorPosition < rank)
			advanceIteration();

		result = getSetForRankedListElem(currentIteratorElement);

		resetIter();
		while (iteratorPosition < oldIterPos)
			advanceIteration();

		return result;
	}

	@Override
	public boolean hasAtLeast(int numberOfElements) {
		if (rankingIsDone)
			return numberOfSets >= numberOfElements;
		try {
			generateUpTo(numberOfElements - 1);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	@Override
	public int getScore(int rank) {
		return scoringFunction.getScore(getRankedExpl(rank));
	}

	public void setScoringFunction(IScoringFunction scoringFunction) {
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
