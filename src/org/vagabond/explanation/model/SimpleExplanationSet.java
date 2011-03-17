package org.vagabond.explanation.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.basic.IBasicExplanation;

public class SimpleExplanationSet implements IExplanationSet {

	static Logger log = Logger.getLogger(SimpleExplanationSet.class);
	
	private IMarkerSet sideEffects;
	private List<IBasicExplanation> expls;
	
	public SimpleExplanationSet () {
		expls = new ArrayList<IBasicExplanation> ();
	}
	
	@Override
	public List<IBasicExplanation> getExplanations() {
		return expls;
	}

	@Override
	public void addExplanation (IBasicExplanation expl) {
		expls.add(expl);
		sideEffects.union(expl.getSideEffects());
	}

	@Override
	public int getSize() {
		return expls.size();
	}

	@Override
	public int getSideEffectSize() {
		return sideEffects.getSize();
	}

	@Override
	public IMarkerSet getSideEffects() {
		return sideEffects;
	}

	@Override
	public IMarkerSet getExplains() {
		IMarkerSet result;
		
		result = MarkerFactory.newMarkerSet();
		for(IBasicExplanation expl: expls) {
			result.add(expl.explains());
		}
		
		return result;
	}
	
	@Override
	public String toString () {
		StringBuffer result = new StringBuffer();
		
		result.append("ExplanationSet:\n\nStats:" + getStats());
		result.append("\nExpls:\n\n");
		for (IBasicExplanation expl: expls) {
			result.append(expl.toString());
		}
		
		return result.toString();
	}
	
	public String getStats () {
		return "NumberOfExplanations: " + expls.size() + "\n" +
				"TotalSideEffectSize: " + sideEffects.getSize() + "\n";
	}


	@Override
	public IExplanationSet union(IExplanationSet other) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
