package org.vagabond.explanation.generation;

import java.sql.ResultSet;

import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.generation.prov.SourceProvParser;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.InfluenceSourceError;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.util.ConnectionManager;

public class InfluenceSourceExplanationGenerator 
		extends SourceProvenanceSideEffectGenerator 
		implements ISingleExplanationGenerator {

	private InfluenceSourceError expl;
	private IExplanationSet result;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		ProvWLRepresentation prov;
		result = ExplanationFactory.newExplanationSet();
		
		this.error = (IAttributeValueMarker) errorMarker;
		prov = ProvenanceGenerator.getInstance().computePIProv(error);
		genExplsForProv(prov);
		
		return result;
	}



	private void genExplsForProv(ProvWLRepresentation prov) throws Exception {
		//TODO
		expl = new InfluenceSourceError(error);
		result.addExplanation(expl);
		
		
		computeTargetSideEffects(expl.getSourceSideEffects(), error);
	}
	
	

}
