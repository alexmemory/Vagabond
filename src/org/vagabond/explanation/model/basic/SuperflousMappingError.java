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

public class SuperflousMappingError extends AbstractBasicExplanation 
		implements IBasicExplanation {

	static Logger log = Logger.getLogger(SuperflousMappingError.class);
	
	private Set<MappingType> maps;
	
	public SuperflousMappingError () {
		super();
		maps = new HashSet<MappingType> ();
	}
	
	public SuperflousMappingError (IAttributeValueMarker marker) {
		super(marker);
		maps = new HashSet<MappingType> ();
	}
	
	public SuperflousMappingError (IAttributeValueMarker marker, 
			Set<MappingType> maps) {
		super(marker);
		this.maps = maps;
	}
	
	@Override
	public ExplanationType getType() {
		return ExplanationType.SuperflousMappingError;
	}

	@Override
	public Object getExplanation() {
		return maps;
	}

//	@Override
//	public String toString () {
//		try {
//			return "SuperflousMappingError for <" + error.toString() + ">\n\n" +
//					"with mapping side-effect:\n<" 
//					+ LoggerUtil.ObjectColToStringWithMethod(
//							maps, MappingType.class, "getId")  +
//					">\n\nand target side-effect:\n<" + targetSE.toString() + ">";
//		} catch (Exception e) {
//			LoggerUtil.logException(e, log);
//		}
//		return "";
//	}
//	
//	@Override
//	public boolean equals (Object other) {
//		SuperflousMappingError err;
//		
//		if (other == null)
//			return false;
//		
//		if (this == other)
//			return true;
//		
//		if (!(other instanceof SuperflousMappingError))
//			return false;
//		
//		err = (SuperflousMappingError) other;
//		
//		if (!this.error.equals(err.error))
//			return false;
//		
//		if (!this.maps.equals(err.maps))//TODO
//			return false;
//		
//		if (!this.targetSE.equals(err.targetSE))
//			return false;
//		
//		return true;
//	}
//	
//	@Override
//	public int hashCode () {
//		if (hash  == -1)
//		{
//			hash = error.hashCode();
//			hash = hash * 13 + targetSE.hashCode();
//		}
//		return hash;
//	}

	@Override
	public Set<MappingType> getMappingSideEffects() {
		return maps;
	}

	@Override
	public int getMappingSideEffectSize () {
		return maps.size();
	}

	public void addMapSE(MappingType map) {
		maps.add(map);
	}
	
	public void setMapSE(Set<MappingType> maps) {
		this.maps = maps;
	}

	public void setTargetSE(IMarkerSet targetSE) {
		this.targetSE = targetSE;
	}
	
}
