package org.vagabond.explanation.generation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;

public class ExplanationSetGenerator {

	static Logger log = Logger.getLogger(ExplanationSetGenerator.class);
	
	private List<ISingleExplanationGenerator> generators;
	
	public ExplanationSetGenerator () {
		generators = new ArrayList<ISingleExplanationGenerator> ();
		generators.add(new CopySourceExplanationGenerator());
		generators.add(new CorrespondencExplanationGenerator());
	}
	
	public ExplanationCollection findExplanations (IMarkerSet errors) throws Exception {
		ExplanationCollection result = new ExplanationCollection ();
		IExplanationSet explsForOne;
		
		for(ISingleMarker error: errors.getElems()) {
			explsForOne = findExplanations(error);
			result.addExplSet(error, explsForOne);
		}
		
		return result;
	}
	
	private IExplanationSet findExplanations (ISingleMarker error) throws Exception {
		IExplanationSet result;
		
		result = ExplanationFactory.newExplanationSet();
		
		for (ISingleExplanationGenerator gen: generators) {
			result.union(gen.findExplanations(error));
		}
		
		return result;
	}
}
