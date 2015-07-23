package org.vagabond.explanation.ranking;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.SimpleExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;

public class DummyRanker implements IExplanationRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(DummyRanker.class);
	
	private ExplanationCollection coll = null;
	private Vector<Integer> iterPos;
	private boolean[] fixedPos;
	private boolean init = false;
	private int numExplSets = 1;
	private int curIterPos = -1;
	private IExplanationSet fixed = new SimpleExplanationSet();
	
	public DummyRanker () {
		iterPos = new Vector<Integer>();
	}
	
	@Override
	public IExplanationSet getRankedExpl (int pos) {
		setIter(pos);
		
		return generateExplForIter(iterPos);
	}
	
	@Override
	public boolean hasNext() {
		return curIterPos < numExplSets - 1;
	}

	@Override
	public IExplanationSet next() {
		IExplanationSet set;
		
		assert(hasNext());
		
		if (log.isDebugEnabled()) {log.debug("cur iter pos = " + iterPos.toString());};

		increaseIter ();
		set = generateExplForIter(iterPos);
		
		return set;
	}
	
	@Override
	public void remove() {
		log.fatal("not allowed to remove object");
	}

	@Override
	public void initializeCollection(ExplanationCollection coll) {
		
		this.coll = coll;
		int numErrors = coll.getNumErrors();
		
		iterPos = new Vector<Integer> ();
		for(int i = 0; i < numErrors; i++) {
			iterPos.add(0);
			numExplSets *= coll.getDimensions().get(i);
		}
		if (iterPos.size() != 0)
			iterPos.set(0, -1);
		fixedPos = new boolean[numErrors];
		Arrays.fill(fixedPos, false);
		
		coll.computeRealSEAndExplains();
//		errors = MarkerFactory.newMarkerSet(coll.getErrorExplMap().keySet());
//		for(ISingleMarker error: errors) {
//			IExplanationSet explSet = coll.getErrorExplMap().get(error);
//			// remove errors from side effects and add them to explains
//			for(IBasicExplanation e: explSet) {
//				e.setRealTargetSideEffects(e.getTargetSideEffects().cloneSet()
//						.diff(errors));
//				e.getRealExplains().union(
//						e.getTargetSideEffects().cloneSet().intersect(errors));
//			}
//		}
		
		init = true;
	}

	@Override
	public void confirmExplanation(IBasicExplanation correctExpl) {
		IdMap<ISingleMarker> ids = coll.getErrorIdMap();
		Map<ISingleMarker,IExplanationSet> sets = coll.getErrorExplMap();
		int index;
		
		for(int i = 0; i < ids.size(); i++) {
			List<IBasicExplanation> set = sets.get(ids.next())
					.getExplanations();
			index = set.indexOf(correctExpl);
			if (index != -1 && !fixedPos[i]) {
				fixedPos[i] = true;
				iterPos.set(i, index);
				fixed.add(correctExpl);
				numExplSets /= set.size();
			}
		}
		
	}

	@Override
	public int getNumberOfExplSets() {
		return numExplSets;
	}

	@Override
	public void resetIter() {
		for(int i = 0; i < iterPos.size(); i++) {
			if (!fixedPos[i])
				iterPos.set(i, 0);
		}
		iterPos.set(0, -1);
		curIterPos = -1; 
	}

	@Override
	public boolean ready() {
		return init;
	}
	
	public int getTotalNumExpls () {
		return numExplSets;
	}
	
	private void setIter (int pos) {
		assert(pos < 0 || pos >= numExplSets);
		
		while (curIterPos < pos)
			increaseIter();
			
		while (curIterPos > pos)
			decreaseIter();
	}

	private void decreaseIter () {
		int curPos;
		Vector<Integer> numExpls;
		
		numExpls = coll.getNumExpls();
		
		for(int i = 0; i < iterPos.size(); i++) {
			if (fixedPos[i])
				continue;
			
			curPos = iterPos.get(i);
			if (curPos > 0)
			{
				iterPos.set(i, curPos - 1);
				curIterPos--;
				if (log.isDebugEnabled()) {log.debug("new iter pos is <" + iterPos + "> : " + curIterPos);};
				return;
			}
			else {
				iterPos.set(i, numExpls.get(i) - 1);
			}
		}
		
		for(int i = 0; i < iterPos.size(); i++)
			if (!fixedPos[i])
				iterPos.set(i, 0);
		curIterPos--;
	}
	
	private void increaseIter () {
		int curPos;
		Vector<Integer> numExpls;
		
		numExpls = coll.getNumExpls();
		
		for(int i = 0; i < iterPos.size(); i++) {
			if (fixedPos[i])
				continue;
			
			curPos = iterPos.get(i);
			if (curPos < numExpls.get(i) - 1)
			{
				iterPos.set(i, curPos + 1);
				curIterPos++;
				if (log.isDebugEnabled()) {log.debug("new iter pos is <" + iterPos + "> : " + curIterPos);};
				return;
			}
			else {
				iterPos.set(i, 0);
			}
		}
		
		for(int i = 0; i < iterPos.size(); i++)
			if (!fixedPos[i])
				iterPos.set(i, numExpls.get(i) -1);
		
		curIterPos++;
		if (log.isDebugEnabled()) {log.debug("iterator reached end: <" + iterPos + "> : " + curIterPos);};
	}
		
	@SuppressWarnings("unused")
	private int compareVec (Vector<Integer> left, Vector<Integer> right) {
		if (left.size() < right.size())
			return 1;
		if (right.size() < left.size())
			return -1;
		
		for(int i = 0; i < left.size(); i++) {
			if (left.get(i) < right.get(i) - 1)
				return -1;
			if (left.get(i) > right.get(i) - 1)
				return 1;
		}
		
		return 0;
	}

	
	private IExplanationSet generateExplForIter(Vector<Integer> iterPos) {
		IExplanationSet result;
		ISingleMarker marker;
		IExplanationSet curSet;
		int iterVal;
		
		result = ExplanationFactory.newExplanationSet();
		for(int i = 0; i < iterPos.size(); i++) {
			iterVal = iterPos.get(i);
			marker = coll.getErrorIdMap().get(i);
			curSet = coll.getErrorExplMap().get(marker);
			result.addExplanation(curSet.getExplanations().get(iterVal));
		}
		
		return result;
	}

	@Override
	public int getIterPos() {
		return curIterPos + 1;
	}

	@Override
	public IExplanationSet previous() {
		IExplanationSet set;
		
		assert(hasPrevious());
		
		decreaseIter ();
		if (log.isDebugEnabled()) {log.debug("cur iter pos = " + iterPos.toString());};
		set = generateExplForIter(iterPos);
		
		if (log.isDebugEnabled()) {log.debug("ExplSet for iter <" + iterPos + "> is \n" + set);};
		
		return set;
	}

	@Override
	public boolean hasPrevious() {
		return curIterPos > 0;
	}

	@Override
	public int getNumberPrefetched() {
		return 0;
	}

	@Override
	public boolean hasAtLeast(int numElem) {
		return (getNumberOfExplSets() > numElem);
	}

	@Override
	public boolean isFullyRanked() {
		return true;
	}

	@Override
	public int getScore(int rank) {
		return getRankedExpl(rank).getSideEffectSize();
	}

	@Override
	public void rankFull() {
		if (log.isDebugEnabled()) {log.debug("Dummy ranker cannot materialize");};
	}

	/* (non-Javadoc)
	 * @see org.vagabond.explanation.ranking.IExplanationRanker#getScoreF()
	 */
	@Override
	public IScoringFunction getScoringFunction() {
		// TODO Auto-generated method stub
		return null;
	}

}
