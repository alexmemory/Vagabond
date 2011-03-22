package org.vagabond.explanation.model.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;

import static org.vagabond.util.LoggerUtil.*;

public class CorrespondenceError implements IBasicExplanation {

	static Logger log = Logger.getLogger(CorrespondenceError.class);
	
	private IMarkerSet sideEffects;
	private Set<CorrespondenceType> correspondences;
	private Collection<MappingType> mapSE;
	private IAttributeValueMarker explains;

	private int hash = -1;

	public CorrespondenceError () {
		setUp();
	}

	public CorrespondenceError (ISingleMarker marker) {
		setUp();
		explains = (IAttributeValueMarker) marker;
	}
	
	private void setUp() {
		sideEffects = MarkerFactory.newMarkerSet();
		mapSE = new ArrayList<MappingType> ();
		correspondences = new HashSet<CorrespondenceType> ();
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
	public ExplanationType getType() {
		return ExplanationType.CorrespondenceError;
	}

	@Override
	public ISingleMarker explains() {
		return explains;
	}

	@Override
	public Object getExplanation() {
		return correspondences;
	}

	public Set<CorrespondenceType> getCorrespondences() {
		return correspondences;
	}

	public void setCorrespondences (Set<CorrespondenceType> correspondence) {
		this.correspondences = correspondence;
	}
	
	public void addCorrespondence (CorrespondenceType corr) {
		this.correspondences.add(corr);
	}

	public Collection<MappingType> getMapSE () {
		return mapSE;
	}

	public void setMapSE(Collection<MappingType> mapSE) {
		this.mapSE = mapSE;
	}

	public void setSideEffects(IMarkerSet sideEffects) {
		this.sideEffects = sideEffects;
	}

	public void setExplains(IAttributeValueMarker explains) {
		this.explains = explains;
	}

	@Override
	public String toString () {
		StringBuffer result;
		
		result = new StringBuffer();
		
		result.append("CorrspondenceExplanation for <");
		result.append(explains.toString());
		result.append(">:\nIncorrect Corrspondences: ");
		try {
			result.append(ObjectColToStringWithMethod(correspondences, 
					CorrespondenceType.class, "getId"));
		} catch (Exception e) {
			logException(e, log);
		}
		
		result.append("\n\nwith target side-effects:\n<");
		result.append(sideEffects.toString());
		result.append(">\n");
		
		result.append("\nwith map side-effects:\n");
		try {
			result.append(ObjectColToStringWithMethod(mapSE, 
					MappingType.class, "getId"));
		} catch (Exception e) {
			logException(e, log);
		}
		
		return result.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		CorrespondenceError cOther;
		
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (!(other instanceof CorrespondenceError))
			return false;
		
		cOther = (CorrespondenceError) other;
		
		if (!this.explains().equals(cOther.explains()))
			return false;
		
		if (!this.sideEffects.equals(cOther.getSideEffects()))
			return false;
		
		if (!this.correspondences.equals(cOther.getCorrespondences()))
			return false;
		
		if (!this.mapSE.equals(cOther.getMapSE()))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode () {
		if (hash == -1) {
			hash = explains.hashCode() * 13 
					+ mapSE.hashCode() 
					+ correspondences.hashCode()
					+ sideEffects.hashCode();
		}
		return hash;
	}
	
}
