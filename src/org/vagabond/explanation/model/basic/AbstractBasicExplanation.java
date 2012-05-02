package org.vagabond.explanation.model.basic;

import static org.vagabond.util.LoggerUtil.ObjectColToStringWithMethod;
import static org.vagabond.util.LoggerUtil.logException;
import static org.vagabond.util.HashFNV.*;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;

public abstract class AbstractBasicExplanation implements IBasicExplanation {

	static Logger log = LogProviderHolder.getInstance().getLogger(AbstractBasicExplanation.class);
	
	private static IMarkerSet sourceSEDummy;
	private static  Collection<MappingType> mapSEDummy;
	private static  Collection<CorrespondenceType> corrSEDummy;
	private static  Collection<TransformationType> transSEDummy;
	
	protected IAttributeValueMarker error;
	protected IMarkerSet targetSE;
	protected IMarkerSet realTargetSE;
	protected IMarkerSet realExplains;
	protected int hash = -1;
	protected boolean hashed = false; 
	
	static {
		sourceSEDummy = MarkerFactory.newMarkerSet();
		mapSEDummy = new HashSet<MappingType> ();
		corrSEDummy = new HashSet<CorrespondenceType> ();
		transSEDummy = new HashSet<TransformationType> ();
	}
	
	public AbstractBasicExplanation () {
		targetSE = MarkerFactory.newMarkerSet();
		realTargetSE = MarkerFactory.newMarkerSet();
		realExplains = MarkerFactory.newMarkerSet();
	}
	
	public AbstractBasicExplanation (ISingleMarker error) {
		targetSE = MarkerFactory.newMarkerSet();
		realTargetSE = MarkerFactory.newMarkerSet();
		this.error = (IAttributeValueMarker) error;
		realExplains = MarkerFactory.newMarkerSet(error);
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
		updateHash();
	}
	
	public void addToTargetSE (ISingleMarker marker) {
		this.targetSE.add(marker);
		updateHash();
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
		
		if (!this.getSourceSideEffects().equals(err.getSourceSideEffects()))
			return false;
		
		// real side effects have been set?
		if (!this.getRealExplains().isEmpty()) {
			if (!this.getRealTargetSideEffects().equals(err.getRealTargetSideEffects()))
				return false;
			if (!this.getRealExplains().equals(err.getRealExplains()))
				return false;
		} 
		else {
			if (!this.getTargetSideEffects().equals(err.getTargetSideEffects()))
				return false;

			if (!this.explains().equals(err.explains()))
				return false;
		}
		
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
		if (!hashed)
			computeHash();
		
		return hash;
	}
	
	
	protected void updateHash() {
		if (hashed)
			computeHash();
	}
	
	protected void computeHash () {
		hash = fnv(error.hashCode());
		hash = fnv(getType(), hash);
		hash = fnv(realTargetSE, hash);
	}
	
	@Override
	public String toString () {
		StringBuffer result;
		
		result = new StringBuffer();
		
		result.append(getType().toString());
		result.append("<" + error.toString() + ">");

		if (getTargetSideEffectSize() > 0)
			result.append("\n\nwith target side-effect:\n<" 
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

	public int getRealTargetSideEffectSize() {
		return realTargetSE.size();
	}
	
	public IMarkerSet getRealTargetSideEffects() {
		return realTargetSE;
	}
	
	public void setRealTargetSideEffects(IMarkerSet set) {
		this.realTargetSE = set;
		updateHash();
	}
	
	@Override
	public void computeRealTargetSEAndExplains (IMarkerSet errors) {
		realTargetSE = targetSE.cloneSet().diff(errors);
		realExplains = MarkerFactory.newMarkerSet(error);
		realExplains.union(targetSE.cloneSet().intersect(errors));
		updateHash();
	}
	
	public IMarkerSet getRealExplains() {
		return this.realExplains;
	}
	
	public void setRealExplains(IMarkerSet set) {
		this.realExplains = set;
	}
	
	@Override
	public int compareTo (IBasicExplanation o) {
		if (this == o)
			return 0;
		
		int comp = this.getType().compareTo(o.getType());
		if (comp != 0)
			return comp;
		
		return this.hashCode() - o.hashCode();
	}
}
