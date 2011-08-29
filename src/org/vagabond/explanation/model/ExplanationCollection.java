package org.vagabond.explanation.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.explanation.ranking.IExplanationRanker;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;

public class ExplanationCollection implements Iterator<IExplanationSet> {

	static Logger log = LogProviderHolder.getInstance().getLogger(ExplanationCollection.class);
	
	private Map<ISingleMarker, IExplanationSet> explMap;
	private IdMap<ISingleMarker> errorIds;
	private Vector<Integer> numExpls;
	private int totalExpls = 1;
	private IExplanationRanker ranker = null;

	private int hash = -1;
	
	public ExplanationCollection () {
		explMap = new HashMap<ISingleMarker, IExplanationSet>();
		errorIds = new IdMap<ISingleMarker>();
		numExpls = new Vector<Integer>();
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
			id = errorIds.getId(marker);
			numExpl = numExpls.get(id) + numExpl;
			numExpls.set(id, numExpl);
			totalExpls *= numExpl;
		}
		else {
			explMap.put(marker, expls);
			errorIds.put(marker);
			totalExpls *= numExpl;
			numExpls.add(numExpl);
		}
	}
	
	public void createRanker (IExplanationRanker ranker) {
		this.ranker = ranker;
		ranker.initialize(this);
	}
	
	public void confirmExplanation (IBasicExplanation expl) {
		ranker.confirmExplanation(expl);
	}
	
	public int getNumErrors () {
		return errorIds.getSize();
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
		return ranker.hasNext();
	}
	
	@Override
	public void remove() {	
		ranker.remove();
	}
	
	public void resetIter () {
		ranker.resetIter();
	}
	
	public int getIterPos () {
		return ranker.getIterPos();
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
		return numExpls.get(errorIds.getId(error));
	}
	
	public Vector<Integer> getNumExpls () {
		return numExpls;
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

	@Override
	public IExplanationSet next() {
		return ranker.next();
	}
}
