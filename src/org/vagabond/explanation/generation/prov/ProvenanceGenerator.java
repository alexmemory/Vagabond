package org.vagabond.explanation.generation.prov;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.MappingType;

public class ProvenanceGenerator {

	static Logger log = Logger.getLogger(ProvenanceGenerator.class);
	
	private static ProvenanceGenerator instance;
	
	static {
		instance = new ProvenanceGenerator();
	}
	
	private ProvenanceGenerator () {
		
	}
	
	public static ProvenanceGenerator getInstance() {
		return instance;
	}
	
	public Vector<String> getMapProvStrings (IAttributeValueMarker error) 
			throws Exception {
		String query;
		ResultSet rs;
		Vector<String> maps;
		
		maps = new Vector<String>();
		
		query = QueryHolder.getQuery("MapAndTransProv.GetMapProv")
				.parameterize("target." + error.getRelName(), error.getTid());
		log.debug("Compute MapProv for <" + error + "> with query:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		while(rs.next()) {
			maps.add(rs.getString(1));
		}
		
		ConnectionManager.getInstance().closeRs(rs);
		
		log.debug("Get map strings provenance for <" + error 
				+ "> returned <" + maps + ">");
		
		return maps;
	}
	
	public Set<MappingType> getMapProv(IAttributeValueMarker error) 
			throws Exception {
		Set<MappingType> maps;
		Vector<String> mapStrings;
		
		mapStrings = getMapProvStrings(error);
		maps = new HashSet<MappingType> ();
		
		for (String map: mapStrings)
			maps.add(MapScenarioHolder.getInstance().getMapping(map));
		
		log.debug("Get mapping provenance for <" + error 
				+ "> returned <" + maps + ">");
		
		return maps;
	}
	
	public ProvWLRepresentation getCopyProvenance (IAttributeValueMarker error) 
			throws Exception {
		ResultSet rs;
		SourceProvParser parser;
		String query;
		
		query = getCopyCSQuery(error);
		log.debug("Parameterized copy source explanation query for <" 
				+ error + ">:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		parser = new SourceProvParser(rs);
		ConnectionManager.getInstance().closeRs(rs);
		
		return parser.getAllProv();
	}
		
		
	private String getCopyCSQuery (IAttributeValueMarker error) {
		String table = error.getRelName();
		String tid = error.getTid();
		String attr = error.getAttrName();
		
		return QueryHolder.getQuery("CopyCS.GetProv").
				parameterize("target." + table, tid, attr);
	}
	
	public ProvWLRepresentation computePIProv (IAttributeValueMarker error)
			throws Exception {
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
}
