package org.vagabond.explanation.generation;

import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.IExplanationSet;

public interface ISingleExplanationGenerator {

	public IExplanationSet findExplanations (ISingleMarker errorMarker)
			throws Exception; 
	
}
