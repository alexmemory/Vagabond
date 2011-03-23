package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.CopyProvExpl;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;

public class CopySourceExplanationGenerator 
		extends SourceProvenanceSideEffectGenerator 
		implements ISingleExplanationGenerator {

	static Logger log = Logger.getLogger(CopySourceExplanationGenerator.class);
	
	private CopyProvExpl prov;
	protected CopySourceError expl;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker) throws Exception {
		this.error = (IAttributeValueMarker) errorMarker;
		
		return getExplanationSets();
	}
	
	private IExplanationSet getExplanationSets() throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		
		retrieveCopyProvenance();
		
		expl = new CopySourceError(error);
		expl.setSourceSE(prov.getTuplesInProv());
		expl.setTargetSE(computeTargetSideEffects(expl.getSourceSE()));
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		result.addExplanation(expl);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		return result;
	}
	
	private void retrieveCopyProvenance () 
			throws Exception {
		ResultSet rs;
		CopyCSParser parser;
		String query;
		
		query = getCopyCSQuery();
		log.debug("Parameterized copy source explanation query for <" 
				+ error + ">:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		parser = new CopyCSParser(rs);
		ConnectionManager.getInstance().closeRs(rs);
		
		this.prov = parser.getAllProv();
	}

	
	private String getCopyCSQuery () {
		String table = error.getRelName();
		String tid = error.getTid();
		String attr = error.getAttrName();
		
		return QueryHolder.getQuery("CopyCS.GetProv").
				parameterize("target." + table, tid, attr);
	}
	
	
}
