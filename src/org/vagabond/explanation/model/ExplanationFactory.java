package org.vagabond.explanation.model;

import org.apache.log4j.Logger;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import static org.vagabond.explanation.model.basic.IBasicExplanation.*;

public class ExplanationFactory {

	static Logger log = Logger.getLogger(ExplanationFactory.class);
	
	private static ExplanationFactory instance = new ExplanationFactory();
	
	private ExplanationFactory () {
		
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
		default:
			throw new Exception("now explanation class for type: <" 
					+ type + ">");
		}
	}
}
