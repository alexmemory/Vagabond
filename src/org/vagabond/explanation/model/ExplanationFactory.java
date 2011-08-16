package org.vagabond.explanation.model;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.model.basic.InfluenceSourceError;
import org.vagabond.explanation.model.basic.SuperflousMappingError;

import static org.vagabond.explanation.model.basic.IBasicExplanation.*;

public class ExplanationFactory {

	static Logger log = LogProviderHolder.getInstance().getLogger(ExplanationFactory.class);
	
	private static ExplanationFactory instance = new ExplanationFactory();
	
	private ExplanationFactory () {
		
	}
	
	public static ExplanationCollection newExplanationCollection () {
		return new ExplanationCollection();
	}
	
	public static ExplanationCollection newExplanationCollection 
			(IExplanationSet ... elems) {
		ExplanationCollection result =  new ExplanationCollection();
		
		for(IExplanationSet set: elems) {
			result.addExplSet(set.getExplains().iterator().next(), set);
		}
		
		return result;
	}
	
	public static IExplanationSet newExplanationSet () {
		return new SimpleExplanationSet ();
	}
	
	public static IExplanationSet newExplanationSet (IBasicExplanation ... elems) {
		SimpleExplanationSet result;
		
		result = new SimpleExplanationSet();
		
		for(IBasicExplanation expl: elems) {
			result.addExplanation(expl);
		}
		
		return result;
	}
	 
	public static IBasicExplanation newBasicExpl (ExplanationType type) throws Exception {
		switch(type) {
		case CopySourceError:
			return new CopySourceError();
		case InfluenceSourceError:
			return new InfluenceSourceError();
		case SuperflousMappingError:
			return new SuperflousMappingError();
		default:
			throw new Exception("now explanation class for type: <" 
					+ type + ">");
		}
	}
}
