package org.vagabond.explanation.model.basic;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.util.LoggerUtil;
import org.vagabond.xmlmodel.MappingType;

public class SourceSkeletonMappingError implements IBasicExplanation {

	static Logger log = Logger.getLogger(SourceSkeletonMappingError.class);
	
	private Set<MappingType> maps;
	private IMarkerSet targetSE;
	private ISingleMarker explains;
	private int hash = -1;
	
	public SourceSkeletonMappingError () {
		targetSE = MarkerFactory.newMarkerSet();
		maps = new HashSet<MappingType> ();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker) {
		this.explains = marker;
		targetSE = MarkerFactory.newMarkerSet();
		maps = new HashSet<MappingType> ();
	}
	
	public SourceSkeletonMappingError (IAttributeValueMarker marker, 
			Set<MappingType> maps) {
		this.explains = marker;
		targetSE = MarkerFactory.newMarkerSet();
		this.maps = maps;
	}
	
	@Override
	public int getSideEffectSize() {
		return targetSE.getSize();
	}

	@Override
	public IMarkerSet getSideEffects() {
		return targetSE;
	}

	@Override
	public ExplanationType getType() {
		return ExplanationType.SourceSkeletonMappingError;
	}

	@Override
	public ISingleMarker explains() {
		return explains;
	}

	@Override
	public Object getExplanation() {
		return maps;
	}

	@Override
	public String toString () {
		try {
			return "SourceSkeletonMappingError for <" + explains.toString() + ">\n\n" +
					"with mapping side-effect:\n<" 
					+ LoggerUtil.ObjectColToStringWithMethod(
							maps, MappingType.class, "getId")  +
					">\n\nand target side-effect:\n<" + targetSE.toString() + ">";
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		return "";
	}
	
	@Override
	public boolean equals (Object other) {
		SourceSkeletonMappingError err;
		
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (!(other instanceof SuperflousMappingError))
			return false;
		
		err = (SourceSkeletonMappingError) other;
		
		if (!this.explains.equals(err.explains))
			return false;
		
		if (!this.maps.equals(err.maps))//TODO
			return false;
		
		if (!this.targetSE.equals(err.targetSE))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode () {
		if (hash  == -1)
		{
			hash = explains.hashCode();
			hash = hash * 13 + targetSE.hashCode();
		}
		return hash;
	}

	public Set<MappingType> getMap() {
		return maps;
	}

	public void setMap(Set<MappingType> maps) {
		this.maps = maps;
	}

	public IMarkerSet getTargetSE() {
		return targetSE;
	}

	public void setTargetSE(IMarkerSet targetSE) {
		this.targetSE = targetSE;
	}

	public ISingleMarker getExplains() {
		return explains;
	}

	public void setExplains(ISingleMarker explains) {
		this.explains = explains;
	}

	public void addMap(MappingType map) {
		maps.add(map);
	}
	
}
