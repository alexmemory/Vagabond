package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.SimpleExplanationSet;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;

public class SideEffectExplanationRanker implements IExplanationRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			SideEffectExplanationRanker.class);
	
	// represents a set of explanations
	protected class RankedListElement {
		public int[] elem;
		public int min;
		public int max;
		public int realSE = 0;
		public int countSet;
		public int firstUnset;
		
		public RankedListElement (int maxSize, int ... elems) {
			elem = new int[maxSize];
			countSet = elems.length;
			
			// initialize unset elements with -1
			for(int i = elems.length; i < maxSize; i++)
				elem[i] = -1;
			
			// set elements and implied elements
			for(int i = 0; i < elems.length; i++) {
				int newElem =  elems[i];
				elem[i] = newElem;
				for(int j = 0; j < explainsMatrix[i][newElem].length; j++) {
					int pos = explainsMatrix[i][newElem][j];
					
					if (this.elem[pos] == -1) {
						this.elem[pos] = -2;
						countSet++;
					}
				}
			}
			
			updateFirstUnset();
							
			computeSideEffectSize(this);
		}
		
		public RankedListElement (RankedListElement prefix, int newElem) {
			this.elem = Arrays.copyOf(prefix.elem, prefix.elem.length);
			this.min = prefix.min;
			this.max = prefix.max;
			this.countSet = prefix.countSet;
			this.realSE = prefix.realSE;
			this.firstUnset = prefix.firstUnset;
			this.elem[prefix.firstUnset] = newElem;
			countSet++;
			
			// set implied explanations and new element
			for(int i = 0; i < explainsMatrix[prefix.firstUnset][newElem].length; i++) {
				int pos = explainsMatrix[prefix.firstUnset][newElem][i];
				if (this.elem[pos] == -1) {
					this.elem[pos] = -2;
					countSet++;
				}
			}
			
				
			
			// adapt the side effect size
			updateFirstUnset();
			computeSideEffectSize(this);
			
			if (log.isDebugEnabled()) {
				log.debug("extended set " + prefix.toString() + " to " 
						+ toString());
			}
		}
		
		private void updateFirstUnset () {
			for(int i = 0; i < elem.length; i++)
				if (elem[i] == -1) {
					firstUnset = i;
					return;
				}
		}
		
		public boolean isDone () {
			return (countSet == elem.length);
		}
		
		public String toString () {
			StringBuilder result = new StringBuilder();
			
			result.append("SORTED[");
			for(int i: elem) {
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
		public boolean equals (Object o) {
			if (o == null || !(o instanceof RankedListElement))
				return false;
			return RankedListComparator.comp.compare(this, (RankedListElement) o) == 0;
		}

		public boolean extensionWithoutOverlap() {
			for(int i = 0; i < explainsMatrix[firstUnset].length; i++) {
				boolean hasOverlap = false;
				int[] overlap = explainsMatrix[firstUnset][i];
				
				for(int j: overlap)
					if (j < firstUnset)
						hasOverlap = true;
				if (!hasOverlap)
					return true;
			}
			return false;
		}
		
		public boolean lastAdditionHasOverlap () {
			for(int i = elem.length - 1; i > 0; i--) {
				if (elem[i] > -1) {
					for(int j = 0; j < explainsMatrix[i][elem[i]].length;j++) {
						if (explainsMatrix[i][elem[i]][j] < i)
							return true;
					}
					return false;
				}
			}
			return false;
		}
	}
	
	public static class RankedListComparator implements Comparator<RankedListElement> {

		public static final RankedListComparator comp = new RankedListComparator();
		
		@Override
		public int compare(RankedListElement o1, RankedListElement o2) {
			if (o1.min != o2.min)
				return o1.min - o2.min; 
			if (o1.max != o2.max)
				return o1.max - o2.max;
			if (o1.countSet != o2.countSet)
				return o2.countSet - o1.countSet;
			for(int i = 0; i < o1.elem.length; i++)
				if (o1.elem[i] != o2.elem[i])
					return o1.elem[i] - o2.elem[i];
			
			return 0;
		}
	}
	
	// represents an ordering of the explanations for one error and min/max side-effect sizes
	protected class OneErrorExplSet extends IdMap<IBasicExplanation> {
		private int minSE;
		private int maxSE;
		private ISingleMarker error;
		
		public OneErrorExplSet (IExplanationSet explSet, ISingleMarker error) {
			super();
			this.error = error;
			List<IBasicExplanation> expList;
			
			// remove errors from side effects and add them to explains
			for(IBasicExplanation e: explSet) {
				e.setRealTargetSideEffects(e.getTargetSideEffects().cloneSet()
						.diff(errors));
				e.getRealExplains().union(
						e.getTargetSideEffects().cloneSet().intersect(errors));
			}
			
			expList = explSet.getExplanations();
			
			// sort on intersected side effects
			Collections.sort(expList, ExplanationComparators.realEffComp);
			for(IBasicExplanation e: expList)
				put(e);
			minSE = expList.get(0).getTargetSideEffectSize();
			maxSE = expList.get(expList.size() - 1).getTargetSideEffectSize();
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
		
		public String toString () {
			StringBuilder result = new StringBuilder();
			
			result.append("ONE ERROR EXPL for <");
			result.append(error);
			result.append(" subsuming expls \n");
			result.append(this.idToObj.values().toString());
			result.append("\nwith min " + minSE + " and max " + maxSE);
			
			return result.toString();
		}
			
	}
	
	public static class OneErrorExplSetComparator implements Comparator<OneErrorExplSet> {

		public static final OneErrorExplSetComparator comp = new OneErrorExplSetComparator();
		
		@Override
		public int compare(OneErrorExplSet arg0, OneErrorExplSet arg1) {
			int span1, span2;
			
			span1 = arg0.maxSE - arg0.minSE;
			span2 = arg1.maxSE - arg1.minSE;
			return span2 - span1;
		}
		
	}
	
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
	private ExplanationCollection col;
	private int[] combinedMin;
	private int[] combinedMax;
	private boolean init = false;
	private boolean rankingDone = false;
	
	public SideEffectExplanationRanker () {
		sortedSets = new TreeSet<RankedListElement> (RankedListComparator.comp);
		errorExpl = new ArrayList<OneErrorExplSet> ();
		errors = MarkerFactory.newMarkerSet();
		errorList = new ArrayList<ISingleMarker> ();
	}
	
	/**
	 * Generate OneErrorExplSets for each error and initialize the
	 * sortedSets
	 */
	
	@Override
	public void initialize(ExplanationCollection coll) {
		int j;
		
		numSets = 1;
		this.col = coll;
		
		explainsMatrix = new int[col.getErrorExplMap().keySet().size()][][];
		
		// create set of errors
		for(ISingleMarker m: col.getErrorExplMap().keySet()) {
			errors.add(m);
		}
		
		// create data structures for the expl sets for each error
		for(ISingleMarker m: col.getErrorExplMap().keySet()) {
			OneErrorExplSet newOne;
			newOne = new OneErrorExplSet(col.getErrorExplMap().get(m), m);
			errorExpl.add(newOne);
			numSets *= newOne.size();
		}
		
		// sort on min-max span to improve pruning
		Collections.sort(errorExpl, OneErrorExplSetComparator.comp);
		for(OneErrorExplSet newOne: errorExpl)
			errorList.add(newOne.error);
		
		// remove errors from side-effect to guarantee correct ranking
		j = 0;
		for(OneErrorExplSet newOne: errorExpl) {
			generateExplainsMatrix(newOne,j);
			j++;
		}
		
		numErrors = errors.size();
		
		// initialize min/max sums
		combinedMin = new int[numErrors];
		combinedMax = new int[numErrors];
		
		for(int i = 0; i < combinedMin.length; i++) {
			OneErrorExplSet one = errorExpl.get(numErrors - i - 1);
			combinedMin[numErrors - i - 1] = one.minSE;
			combinedMax[numErrors - i - 1] = one.maxSE; 
		}
		
		if (log.isDebugEnabled()) {
			for(int i = 0; i < numErrors; i++) {
				log.debug("min " + combinedMin[i] + " max " + combinedMax[i]);
			}
		}
		
		// initialize the sorted list with one expl, explanations
		for(int i = 0; i < errorExpl.get(0).size(); i++) {
			sortedSets.add(new RankedListElement(numErrors, i));
		}

		init = true;
	}

	private void generateExplainsMatrix (OneErrorExplSet expl, int pos) {
		List<Integer> overlaps;
		IBasicExplanation e;
		IMarkerSet overlap;
		
		explainsMatrix[pos] = new int[expl.size()][];
		
		for(int i = 0; i < expl.size(); i++) {
			overlaps = new ArrayList<Integer> ();
			e = expl.get(i);
	
			// get other errors that are covered
			overlap = errors.cloneSet().intersect(e.getTargetSideEffects());
			for(ISingleMarker error: overlap) {
				int errorPos = getPosForError(error);
				if (errorPos != pos)
					overlaps.add(errorPos);
			}
			Collections.sort(overlaps);
			explainsMatrix[pos][i] = new int [overlaps.size()];
			for(int j = 0; j < overlaps.size(); j++)
				explainsMatrix[pos][i][j] = overlaps.get(j);
			
			LoggerUtil.logArray(log, explainsMatrix[pos][i], 
					"[" + pos + "," + i + "] is ");
		}
	}
	
	private int getPosForError (ISingleMarker error) {
		return errorList.indexOf(error);
	}
	
	private void generateUpTo(int upTo) {
		
		while(iterDone <= upTo) {
			RankedListElement curCand;

			// skip ranked, complete elements
			if (iterDone == -1)
				curCand = sortedSets.first();
			else
				curCand = sortedSets.higher(lastDoneElem);
			
			// current best candidate is not complete, expand it
			if (!curCand.isDone())
				expandAndInsert(curCand);
			// current best candidate is complete, check if best incomplete candidate
			// cannot be better than this one (best.max <= incomplete.min), 
			// if so increase iterDone until this condition does not hold anymore
			else {
				int curPos = iterDone + 1;
				RankedListElement inclCand = curCand;
				
				// find first incomplete set if exists
				while(curPos != sortedSets.size() && inclCand.isDone()) {
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
						throw new NoSuchElementException("trying to access beyond last " +
								"element of ranking");
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
	 * Add all possible sets from one error to a set,
	 * remove the original set from the list and add the 
	 * extended sets to the list.
	 */
	
	private void expandAndInsert(RankedListElement curExp) {
		boolean disOverlap;
		
		sortedSets.remove(curExp);
		disOverlap = curExp.extensionWithoutOverlap();
		
		for(int i = 0; i < errorExpl.get(curExp.firstUnset).size(); i++) {
			RankedListElement newOne = new RankedListElement (curExp, i);
			if (!disOverlap || !newOne.lastAdditionHasOverlap())
				sortedSets.add(newOne);
		}
	}

	@Override
	public IExplanationSet next() {
		if (++iterPos >= numSets)
			throw new NoSuchElementException("only " + numSets + " elements");
		
		if (iterPos > iterDone)
			generateUpTo(iterPos);			

		if (curIterElem == null)
			curIterElem = sortedSets.first();
		else
			curIterElem = sortedSets.higher(curIterElem);
		
		return getSetForRankedListElem (curIterElem);
	}

	private IExplanationSet getSetForRankedListElem (RankedListElement elem) {
		IExplanationSet result = ExplanationFactory.newExplanationSet(
				ExplanationComparators.sameElemComp);		
		
		for(int i = 0; i < elem.elem.length; i++)
			if (elem.elem[i] > -1)
				result.addUnique(errorExpl.get(i).get(elem.elem[i]));
		
		if (log.isDebugEnabled())
			log.debug("set for iter is \n"  + result.toString());
		
		return result;
	}

	private void computeSideEffectSize (RankedListElement elem) {
		IMarkerSet sideEff = MarkerFactory.newMarkerSet();
		
		elem.min = 0;
		elem.max = 0;
		for(int i = 0; i <  elem.elem.length; i++) {
			if (elem.elem[i] > -1)
				sideEff.union(errorExpl.get(i).get(elem.elem[i]).getTargetSideEffects());
			else if (elem.elem[i] != -2){
				elem.min = Math.max(combinedMin[i], elem.min);
				elem.max += combinedMax[i];
			}
		}
			
		elem.realSE = sideEff.getSize();
		if (elem.isDone()) {
			elem.min = elem.realSE;
			elem.max = elem.realSE;
		}
		else {
			elem.min = Math.max(elem.realSE, elem.min);
			elem.max = elem.realSE + elem.max;
		}
	}
	
	@Override
	public void remove() {
		// TODO do nothing
	}

	@Override
	public boolean hasNext() {
		return iterPos < numSets - 1;
	}
	
	@Override
	public boolean ready() {
		return init;
	}

	@Override
	public void confirmExplanation(IBasicExplanation correctExpl) {
		// TODO Auto-generated method stub

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
	public int getIterPos() {
		return iterPos + 1;
	}

	@Override
	public IExplanationSet previous() {
		if (--iterPos < 0)
			throw new NoSuchElementException("try to get element before first");
		
		curIterElem = sortedSets.lower(curIterElem);
		
		return getSetForRankedListElem (curIterElem);
	}

	@Override
	public boolean hasPrevious() {
		return iterPos > 0;
	}

	@Override
	public int getNumberPrefetched() {
		if (log.isDebugEnabled())
			log.debug("ITER DONE " + (iterDone + 1) + " incomplete " 
					+ (sortedSets.size() - iterDone - 1));
		return iterDone + 1;
	}

}
