package org.vagabond.explanation.generation;

import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.prov.SourceProvParser;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.util.ConnectionManager;

public class CopySourceExplanationGenerator 
		extends SourceProvenanceSideEffectGenerator 
		implements ISingleExplanationGenerator {

	static Logger log = Logger.getLogger(CopySourceExplanationGenerator.class);
	
	private ProvWLRepresentation prov;
	protected CopySourceError expl;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker) throws Exception {
		this.error = (IAttributeValueMarker) errorMarker;
		
		return getExplanationSets();
	}
	
	private IExplanationSet getExplanationSets() throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		
		this.prov = retrieveCopyProvenance();
		
		expl = new CopySourceError(error);
		expl.setSourceSE(prov.getTuplesInProv());
		expl.setTargetSE(computeTargetSideEffects(expl.getSourceSE()));
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		result.addExplanation(expl);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		return result;
	}
	
	private ProvWLRepresentation retrieveCopyProvenance () 
			throws Exception {
		ResultSet rs;
		SourceProvParser parser;
		String query;
		
		query = getCopyCSQuery();
		log.debug("Parameterized copy source explanation query for <" 
				+ error + ">:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		parser = new SourceProvParser(rs);
		ConnectionManager.getInstance().closeRs(rs);
		
		return parser.getAllProv();
	}

	
	private String getCopyCSQuery () {
		String table = error.getRelName();
		String tid = error.getTid();
		String attr = error.getAttrName();
		
		return QueryHolder.getQuery("CopyCS.GetProv").
				parameterize("target." + table, tid, attr);
	}
	
	
}
