package org.vagabond.explanation.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.basic.IBasicExplanation;

public class SimpleExplanationSet implements IExplanationSet {

	static Logger log = Logger.getLogger(SimpleExplanationSet.class);
	
	private IMarkerSet sideEffects;
	private Set<IBasicExplanation> expls;
	
	public SimpleExplanationSet () {
		expls = new HashSet<IBasicExplanation> ();
		sideEffects = MarkerFactory.newMarkerSet();
	}
	
	@Override
	public List<IBasicExplanation> getExplanations() {
		return new ArrayList<IBasicExplanation> (expls);
	}
	
	public Set<IBasicExplanation> getExplanationsSet () {
		return this.expls;
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
		
		result.append("ExplanationSet:\n\nStats:\n" + getStats());
		result.append("\nExpls:\n\n--");
		for (IBasicExplanation expl: expls) {
			result.append(expl.toString());
			result.append("\n\n--");
		}
		
		return result.toString();
	}
	
	public String getStats () {
		return "NumberOfExplanations: " + expls.size() + "\n" +
				"TotalSideEffectSize: " + sideEffects.getSize() + "\n";
	}


	@Override
	public IExplanationSet union(IExplanationSet other) {
		if (other == null)
			return this;
		this.expls.addAll(other.getExplanationsSet());
		this.sideEffects.union(other.getSideEffects());
		return this;
	}
	
	@Override
	public boolean equals (Object other) {
		SimpleExplanationSet otherSet;
		
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (!(other instanceof SimpleExplanationSet))
			return false;
		
		otherSet = (SimpleExplanationSet) other;
		
		if (expls.size() != otherSet.expls.size())
			return false;
		
		for(IBasicExplanation expl: expls) {
			if(!otherSet.getExplanations().contains(expl))
				return false;
		}
		
		return true;
	}
	
}
