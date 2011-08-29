package org.vagabond.explanation.ranking;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.util.LogProviderHolder;

public class ExplanationRanker {
	static Logger log = LogProviderHolder.getInstance().getLogger(ExplanationCollection.class);
	
	private ExplanationCollection explCollection;
	private List<IExplanationSet> sortedExpls;
	
	public ExplanationRanker(ExplanationCollection explCollection) {
		this.explCollection = explCollection;
		rankExplanations();
	}
	
	public List<IExplanationSet> getRankedExpls() {
		return sortedExpls;
	}
	
	private void rankExplanations() {
		Iterator<IExplanationSet> iterExplCollection = 
			((Collection<IExplanationSet>) explCollection).iterator();
		while (iterExplCollection.hasNext()) {
			sortedExpls.add(iterExplCollection.next());
		}
		Collections.sort(sortedExpls, new ExplanationSetComparator());
	}
	

}
