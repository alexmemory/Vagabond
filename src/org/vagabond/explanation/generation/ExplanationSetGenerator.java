package org.vagabond.explanation.generation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType;
import org.vagabond.explanation.ranking.DummyRanker;
import org.vagabond.util.LogProviderHolder;

public class ExplanationSetGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(ExplanationSetGenerator.class);
	
	private List<ISingleExplanationGenerator> generators;
	
	public ExplanationSetGenerator () {
		generators = new ArrayList<ISingleExplanationGenerator> ();
		generators.add(new CopySourceExplanationGenerator());
		generators.add(new CorrespondencExplanationGenerator());
		generators.add(new InfluenceSourceExplanationGenerator());
		generators.add(new SuperfluousMappingExplanationGenerator());
		generators.add(new SourceSkeletonMappingExplanationGenerator());
//		generators.add(new TargetSkeletonMappingExplanationGenerator());
	}
	
	public ExplanationCollection findExplanations (IMarkerSet errors) throws Exception {
		ExplanationCollection result = new ExplanationCollection ();
		IExplanationSet explsForOne;
		
		for(ISingleMarker error: errors.getElems()) {
			explsForOne = findExplanations(error);
			result.addExplSet(error, explsForOne);
		}
		
		result.createRanker (new DummyRanker());
		
		if (log.isInfoEnabled()) {
			boolean error = false;
			for(IExplanationSet outSet: result.getExplSets()) {
				for(IBasicExplanation outE: outSet) {
					if (outE.getType() == ExplanationType.SuperflousMappingError 
							|| outE.getType() == ExplanationType.SourceSkeletonMappingError
							|| outE.getType() == ExplanationType.CorrespondenceError) {
						for(IExplanationSet inSet : result.getExplSets()) {
							for(IBasicExplanation inE: inSet) {
								if (inE.getType() == ExplanationType.SuperflousMappingError 
										|| inE.getType() == ExplanationType.SourceSkeletonMappingError
										|| inE.getType() == ExplanationType.CorrespondenceError) {
									if (inE.equals(outE) && inE != outE) {
										System.out.println("same explanation, but not same object");
										System.out.println(inE.toString());
										error = true;
									}
								}
							}
						}
					}
				}
			}
			if (error)
				System.exit(1);
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
