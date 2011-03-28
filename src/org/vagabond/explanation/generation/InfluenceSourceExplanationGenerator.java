package org.vagabond.explanation.generation;

import java.sql.ResultSet;

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
		prov = computePIProv();
		genExplsForProv(prov);
		
		return result;
	}

	private ProvWLRepresentation computePIProv() throws Exception {
		String query;
		ResultSet rs;
		SourceProvParser parser;
		ProvWLRepresentation prov;
		
		query = QueryHolder.getQuery("InfluenceCS.GetProv")
				.parameterize("target." + error.getRelName(), error.getTid(), 
						error.getAttrName());
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		parser = new SourceProvParser(rs);
		prov = parser.getAllProv();
		
		log.debug("compute prov for <" + error + ">:\n" + prov);
		
		ConnectionManager.getInstance().closeRs(rs);
		
		return prov;
	}

	private void genExplsForProv(ProvWLRepresentation prov) throws Exception {
		//TODO
		expl = new InfluenceSourceError(error);
		result.addExplanation(expl);
		
		
		computeTargetSideEffects(expl.getSourceSideEffects());
	}
	
	

}
