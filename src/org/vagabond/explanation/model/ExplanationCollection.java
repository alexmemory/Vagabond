package org.vagabond.explanation.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.util.IdMap;

public class ExplanationCollection implements Iterator<IExplanationSet> {

	static Logger log = LogProviderHolder.getInstance().getLogger(ExplanationCollection.class);
	
	private Map<ISingleMarker, IExplanationSet> explMap;
	private IdMap<ISingleMarker> errorIds;
	private Vector<Integer> numExpls;
	private Vector<Integer> iterPos;
	private int totalExpls = 1;

	private int hash = -1;
	
	public ExplanationCollection () {
		explMap = new HashMap<ISingleMarker, IExplanationSet>();
		errorIds = new IdMap<ISingleMarker>();
		numExpls = new Vector<Integer>();
		iterPos = new Vector<Integer>();
	}
	
	public Vector<Integer> getDimensions () {
		return numExpls;
	}
	
	public int getNumCombinations() {
		return totalExpls;
	}
	
	public void addExplSet (ISingleMarker marker, IExplanationSet expls) {
		int id;
		int numExpl = expls.getSize();
		
		if (explMap.containsKey(marker)) {
			totalExpls /= explMap.get(marker).getSize();
			id = errorIds.get(marker);
			numExpl = numExpls.get(id) + numExpl;
			numExpls.set(id, numExpl);
			totalExpls *= numExpl;
		}
		else {
			explMap.put(marker, expls);
			errorIds.put(marker);
			totalExpls *= numExpl;
			numExpls.add(numExpl);
			iterPos.add(0);
		}
	}
	
	public Collection<IExplanationSet> getExplSets () {
		return explMap.values();
	}
	
	public Map<ISingleMarker, IExplanationSet> getErrorExplMap () {
		return explMap;
	}
	
	public IdMap<ISingleMarker> getErrorIdMap () {
		return errorIds;
	}

	@Override
	public boolean hasNext() {
		return compareVec (iterPos, numExpls) < 0;
	}

	@Override
	public IExplanationSet next() {
		IExplanationSet set;
		
		assert(hasNext());
		
		set = generateExplForIter(iterPos);
		increaseIter ();
		
		log.debug("ExplSet for iter <" + iterPos + "> is \n" + set);
		
		return set; 
	}
	
	private IExplanationSet generateExplForIter(Vector<Integer> iterPos) {
		IExplanationSet result;
		ISingleMarker marker;
		IExplanationSet curSet;
		int iterVal;
		
		result = ExplanationFactory.newExplanationSet();
		for(int i = 0; i < iterPos.size(); i++) {
			iterVal = iterPos.get(i);
			marker = errorIds.get(i);
			curSet = explMap.get(marker);
			result.addExplanation(curSet.getExplanations().get(iterVal));
		}
		
		return result;
	}

	private void increaseIter () {
		int curPos;
		
		for(int i = 0; i < iterPos.size(); i++) {
			curPos = iterPos.get(i);
			if (curPos < numExpls.get(i) - 1)
			{
				iterPos.set(i, curPos + 1);
				log.debug("new iter pos is <" + iterPos + ">");
				return;
			}
			else {
				iterPos.set(i, 0);
			}
		}
		
		log.debug("iterator reached end: <" + iterPos + ">");
		for(int i = 0; i < iterPos.size(); i++)
			iterPos.set(i, numExpls.get(i));
	}
	
	@Override
	public void remove() {		
	}
	
	public void resetIter () {
		for(int i = 0; i < iterPos.size(); i++) {
			iterPos.set(i, 0);
		}
	}
	
	private int compareVec (Vector<Integer> left, Vector<Integer> right) {
		if (left.size() < right.size())
			return 1;
		if (right.size() < left.size())
			return -1;
		
		for(int i = 0; i < left.size(); i++) {
			if (left.get(i) < right.get(i))
				return -1;
			if (left.get(i) > right.get(i))
				return 1;
		}
		
		return 0;
	}
	
	@Override
	public String toString () {
		StringBuffer result;
		
		result = new StringBuffer();
		result.append("Explanation Collection:\n\n");
		result.append(getStats());
		result.append("\n\n------Sets:\n");
		
		for(IExplanationSet set: explMap.values()) {
			result.append(set);
			result.append("\n------------\n");
		}
		
		return result.toString();
	}
	
	public String getStats () {
		return "Stats:\n" + 
				"\tNumber Sets: <" + explMap.size() + ">\n" +
				"\tTotal explanations: <" + totalExpls + ">\n" +
				"\tNumber Expls per set: <" + numExpls + ">\n";
	}
	
	@Override
	public boolean equals (Object other) {
		ExplanationCollection oCol;
		
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (!(other instanceof ExplanationCollection))
			return false;
		
		oCol = (ExplanationCollection) other;
		
		if (this.totalExpls != oCol.totalExpls)
			return false;
				
		if (!this.explMap.equals(oCol.explMap))
			return false;
		
		// ids assigned to error markers are considered instable
		for(ISingleMarker error: explMap.keySet()) {
			if (this.getNumExpls(error) != oCol.getNumExpls(error))
				return false;
		}
		
//		if (!this.numExpls.equals(oCol.numExpls))
//			return false;
//		
//		if (!this.errorIds.equals(oCol.errorIds))
//			return false;
		
		return true;
	}
	
	private int getNumExpls (ISingleMarker error) {
		return numExpls.get(errorIds.get(error));
	}
	
	@Override
	public int hashCode () {
		if (hash == -1) {
			hash = totalExpls;
			hash = hash * 13 + numExpls.hashCode();
			hash = hash * 13 + explMap.hashCode();
		}
		
		return hash;
	}
}
