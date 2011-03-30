package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.prov.SourceProvParser;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.MappingType;

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
		IMarkerSet sourceSE;
		
		prov = retrieveCopyProvenance();
		sourceSE = getRealCopyFromMappings(prov.getTuplesInProv()); 
		
		expl = new CopySourceError(error);
		expl.setSourceSE(sourceSE);
		expl.setTargetSE(computeTargetSideEffects(expl.getSourceSideEffects()));
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		result.addExplanation(expl);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		return result;
	}
	
	private IMarkerSet getRealCopyFromMappings(IMarkerSet tuplesInProv) {
		IMarkerSet result;
		ITupleMarker tup;
		String rel;
		String tid;
		int attr;
		Map<MappingType,Set<Integer>> maps;
		
		result = MarkerFactory.newMarkerSet();
		
		for(ISingleMarker marker: tuplesInProv) {
			tup = (ITupleMarker) marker;
			
			result.add(MarkerFactory.newAttrMarker(tup,attr));
		}
		
		return result;
	}
	
	private 

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
