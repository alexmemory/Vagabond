package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.util.ConnectionManager;

public class CopySourceExplanationGenerator implements
		ISingleExplanationGenerator {

	static Logger log = Logger.getLogger(CopySourceExplanationGenerator.class);
	
	private CopyProvExpl prov;
	private IAttributeValueMarker error;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker) throws Exception {
		this.error = (IAttributeValueMarker) errorMarker;
		return getExplanationSets();
	}
	
	private IExplanationSet getExplanationSets() throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		CopySourceError expl;
		
		retrieveCopyProvenance();
		
		expl = new CopySourceError(error);
		expl.setSourceSE(prov.getTuplesInProv());
		expl.setTargetSE(computeTargetSideEffects(expl));
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		result.addExplanation(expl);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		return result;
	}
	
	private IMarkerSet computeTargetSideEffects(CopySourceError expl) {
		IMarkerSet sourceError = expl.getSourceSE();
		
		
		return MarkerFactory.newMarkerSet();
	}

	private void retrieveCopyProvenance () 
			throws Exception {
		ResultSet rs;
		CopyCSParser parser;
		String query;
		
		query = getQuery();
		log.debug("Parameterized query for <" + error + ">:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		parser = new CopyCSParser(rs);
		ConnectionManager.getInstance().closeRs(rs);
		
		this.prov = parser.getAllProv();
	}

	private String getQuery () {
		String table = error.getRelName();
		String tid = error.getTid();
		String attr = error.getAttrName();
		
		return QueryHolder.getQuery("CopyCS.GetProv").
				parameterize("target." + table, tid, attr);
	}
	
	
}
