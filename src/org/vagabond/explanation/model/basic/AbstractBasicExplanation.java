package org.vagabond.explanation.model.basic;

import static org.vagabond.util.LoggerUtil.ObjectColToStringWithMethod;
import static org.vagabond.util.LoggerUtil.logException;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public abstract class AbstractBasicExplanation implements IBasicExplanation {

	static Logger log = Logger.getLogger(AbstractBasicExplanation.class);
	
	private static IMarkerSet sourceSEDummy;
	private static  Collection<MappingType> mapSEDummy;
	private static  Collection<CorrespondenceType> corrSEDummy;
	private static  Collection<TransformationType> transSEDummy;
	
	protected IAttributeValueMarker error;
	protected IMarkerSet targetSE;
	private int hash = -1;
	
	static {
		sourceSEDummy = MarkerFactory.newMarkerSet();
		mapSEDummy = new HashSet<MappingType> ();
		corrSEDummy = new HashSet<CorrespondenceType> ();
		transSEDummy = new HashSet<TransformationType> ();
	}
	
	public AbstractBasicExplanation () {
		targetSE = MarkerFactory.newMarkerSet();
	}
	
	public AbstractBasicExplanation (ISingleMarker error) {
		targetSE = MarkerFactory.newMarkerSet();
		this.error = (IAttributeValueMarker) error;
	}
	
	
	@Override
	public ISingleMarker explains() {
		return error;
	}

	public void setExplains (ISingleMarker explains) {
		this.error = (IAttributeValueMarker) explains;
	}
	
	@Override
	public int getTargetSideEffectSize() {
		return targetSE.getSize();
	}

	@Override
	public IMarkerSet getTargetSideEffects() {
		return targetSE;
	}
	
	public void setTargetSE(IMarkerSet targetSE) {
		this.targetSE = targetSE;
	}
	
	public void addToTargetSE (ISingleMarker marker) {
		this.targetSE.add(marker);
	}

	@Override
	public int getSourceSideEffectSize () {
		return 0;
	}
	
	@Override
	public IMarkerSet getSourceSideEffects () {
		return sourceSEDummy;
	}
	
	@Override
	public int getMappingSideEffectSize() {
		return 0;
	}

	@Override
	public Collection<MappingType> getMappingSideEffects() {
		return mapSEDummy;
	}

	@Override
	public int getCorrSideEffectSize() {
		return 0;
	}

	@Override
	public Collection<CorrespondenceType> getCorrespondenceSideEffects() {
		return corrSEDummy;
	}

	@Override
	public int getTransformationSideEffectSize() {
		return 0;
	}

	@Override
	public Collection<TransformationType> getTransformationSideEffects() {
		return transSEDummy;
	}
	
	@Override
	public boolean equals (Object other) {
		IBasicExplanation err;
		
		if (other == null)
			return false;
		
		if (this == other)
			return true;
		
		if (!(other instanceof IBasicExplanation))
			return false;
		
		err = (IBasicExplanation) other;
		
		if (!this.getType().equals(err.getType()))
			return false;
		
		if (!this.getExplanation().equals(err.getExplanation()))
			return false;
		
		if (!this.explains().equals(err.explains()))
			return false;
		
		if (!this.getSourceSideEffects().equals(err.getSourceSideEffects()))
			return false;
		
		if (!this.getTargetSideEffects().equals(err.getTargetSideEffects()))
			return false;
		
		if (!this.getMappingSideEffects().equals(err.getMappingSideEffects()))
			return false;
		
		if (!this.getCorrespondenceSideEffects()
				.equals(err.getCorrespondenceSideEffects()))
			return false;
		
		if (!this.getTransformationSideEffects()
				.equals(err.getTransformationSideEffects()))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode () {
		if (hash == -1)
		{
			hash = error.hashCode();
			hash = hash * 13 + getType().hashCode();
			hash = hash * 13 + getSourceSideEffects().hashCode();
			hash = hash * 13 + getTargetSideEffects().hashCode();
		}
		return hash;
	}
	
	@Override
	public String toString () {
		StringBuffer result;
		
		result = new StringBuffer();
		
		result.append(getType().toString());
		result.append("<" + error.toString() + ">");

		if (getTargetSideEffectSize() > 0)
			result.append("\n\nwith source side-effect:\n<" 
					+ getTargetSideEffects().toString() + ">");
		
		if (getSourceSideEffectSize() > 0)
			result.append("\n\nwith source side-effect:\n<" 
					+ getSourceSideEffects().toString() + ">");
		
		if (getMappingSideEffectSize() > 0)
				getStringFromCol(result, "with mapping side-effect:\n<",
						getMappingSideEffects(), 
						MappingType.class, "getId");
		
		if (getCorrSideEffectSize() > 0)
			getStringFromCol(result, "with correspondence side-effect:\n<",
					getCorrespondenceSideEffects(), 
					CorrespondenceType.class, "getId");
				
		
		if (getTransformationSideEffectSize() > 0)
			getStringFromCol(result, "with transformation side-effect:\n<",
					getTransformationSideEffects(), 
					TransformationType.class, "getId");
		
		return result.toString();
	}
	
	private void getStringFromCol (StringBuffer buf, String message, 
			Collection<?> coll, Class<?> clas, String methodName) {
		buf.append("\n\n" + message);
		try {
			buf.append(ObjectColToStringWithMethod(coll, clas, methodName));
			buf.append('>');
		} catch (Exception e) {
			logException(e, log);
		}
		
	}

}
