package org.vagabond.explanation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.basic.ExplanationComparators;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.util.LogProviderHolder;

public class SimpleExplanationSet implements IExplanationSet {

	static Logger log = LogProviderHolder.getInstance().getLogger(SimpleExplanationSet.class);
	
	private IMarkerSet targetSideEffects;
	private Set<IBasicExplanation> expls;
	private ArrayList<IBasicExplanation> sorted = null;
	private Comparator<IBasicExplanation> comp = null;
	
	public SimpleExplanationSet (Comparator<IBasicExplanation> comp) {
		init();
		this.comp = comp;
	}

	public SimpleExplanationSet () {
		init();
	}
	
	private void init() {
		expls = new HashSet<IBasicExplanation> ();
		targetSideEffects = MarkerFactory.newMarkerSet();
	}
	
	
	
	@Override
	public List<IBasicExplanation> getExplanations() {
		if (sorted == null)
			sorted = new ArrayList<IBasicExplanation> (expls);
		Collections.sort(sorted, ExplanationComparators.fullSideEffComp);
		return sorted;
	}
	
	public Set<IBasicExplanation> getExplanationsSet () {
		return this.expls;
	}

	@Override
	public boolean addExplanation (IBasicExplanation expl) {
		if (!expl.getRealExplains().isEmpty())
			targetSideEffects.union(expl.getRealTargetSideEffects());
		else
			targetSideEffects.union(expl.getTargetSideEffects());
		return expls.add(expl);
	}

	@Override
	public int getSize() {
		return expls.size();
	}

	@Override
	public int getSideEffectSize() {
		return targetSideEffects.getSize();
	}

	@Override
	public IMarkerSet getSideEffects() {
		return targetSideEffects;
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
		
		result.append("ExplanationSet(" + System.identityHashCode(this) + "," +  hashCode() + "):\n\nStats:\n" + getStats());
		result.append("\nExpls:\n\n--");
		for (IBasicExplanation expl: expls) {
			result.append(expl.toString());
			result.append("\n\n--");
		}
		
		return result.toString();
	}
	
	public String toSummaryString() {
		StringBuffer result = new StringBuffer();

		result.append("<" + expls.size() + "|" + targetSideEffects.getSize() + "|");
		for (IBasicExplanation expl: expls) {
			char typeC;
			switch(expl.getType()) {
			case CopySourceError:
				typeC = 'C';
				break;
			case CorrespondenceError:
				typeC = 'c';
				break;
			case InfluenceSourceError:
				typeC = 'I';
				break;
			case SourceSkeletonMappingError:
				typeC = 'S';
				break;
			case SuperflousMappingError:
				typeC = 'M';
				break;
			case TargetSkeletonMappingError:
				typeC = 'T';
				break;
			default:
				throw new NoSuchElementException();
			}
			
			result.append(typeC);
		}
		result.append(">");
		
		return result.toString();
	}
	
	public String getStats () {
		return "NumberOfExplanations: " + expls.size() + "\n" +
				"TotalSideEffectSize: " + targetSideEffects.getSize() + "\n";
	}


	@Override
	public IExplanationSet union(IExplanationSet other) {
		if (other == null)
			return this;
		this.expls.addAll(other.getExplanationsSet());
		this.targetSideEffects.union(other.getSideEffects());
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

	@Override
	public boolean add(IBasicExplanation e) {
		return addExplanation(e);
	}
	
	public boolean addUnique(IBasicExplanation e) {
		for(IBasicExplanation expl: expls) {
			if (comp.compare(e, expl) == 0)
				return false;
		}
		
		return add(e);
	}

	@Override
	public boolean addAll(Collection<? extends IBasicExplanation> c) {
		return expls.addAll(c);
	}

	@Override
	public void clear() {
		expls.clear();
		targetSideEffects.clear();
	}

	@Override
	public boolean contains(Object o) {
		return expls.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return expls.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return expls.isEmpty();
	}

	@Override
	public Iterator<IBasicExplanation> iterator() {
		return expls.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return expls.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return expls.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return expls.retainAll(c);
	}

	@Override
	public int size() {
		return expls.size();
	}

	@Override
	public Object[] toArray() {
		return expls.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return expls.toArray(a);
	}

	@Override
	public int hashCode() {
		return expls.hashCode();
	}
	
	
}
