package org.vagabond.explanation.model;

import java.util.List;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;

public interface IExplanationSet {

	public List<IBasicExplanation> getExplanations();
	public int getSize();
	public int getSideEffectSize();
	public IMarkerSet getSideEffects();
	public IMarkerSet getExplains();
	public void addExplanation(IBasicExplanation expl);
	public IExplanationSet union (IExplanationSet other);
}
