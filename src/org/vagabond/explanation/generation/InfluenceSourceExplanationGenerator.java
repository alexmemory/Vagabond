package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.InfluenceSourceError;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.util.ConnectionManager;

public class InfluenceSourceExplanationGenerator 
		extends SourceProvenanceSideEffectGenerator 
		implements ISingleExplanationGenerator {

	private InfluenceSourceError expl;
	private IExplanationSet result;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker)
			throws Exception {
		result = ExplanationFactory.newExplanationSet();
		
		this.error = (IAttributeValueMarker) errorMarker;
		computePIProv();
		
		return result;
	}

	private void computePIProv() throws Exception {
		String query;
		ResultSet rs;
		CopyCSParser parser;
		CopyProvExpl prov;
		
		query = QueryHolder.getQuery("InfluenceCS.GetProv")
				.parameterize("target." + error.getRelName(), error.getTid(), 
						error.getAttrName());
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		parser = new CopyCSParser(rs);
		prov = parser.getAllProv();
		
		log.debug("compute prov for <" + error + ">:\n" + prov);
		
		ConnectionManager.getInstance().closeRs(rs);
		
		genExplsForProv (prov);
	}

	private void genExplsForProv(CopyProvExpl prov) throws Exception {
		//TODO
		expl = new InfluenceSourceError(error);
		result.addExplanation(expl);
		
		
		computeTargetSideEffects(expl.getSourceSE());
	}
	
	

}
