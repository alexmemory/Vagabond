package org.vagabond.explanation.generation.prov;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.MappingType;

public class ProvenanceGenerator {

	static Logger log = Logger.getLogger(ProvenanceGenerator.class);
	
	public ProvenanceGenerator () {
		
	}
	
	public Set<MappingType> getMapProv(IAttributeValueMarker error) throws Exception {
		String query;
		ResultSet rs;
		Set<MappingType> maps;
		
		maps = new HashSet<MappingType> ();
		
		query = QueryHolder.getQuery("SuperMap.GetMapProv")
				.parameterize("target." + error.getRelName(), error.getTid());
		log.debug("Compute MapProv for <" + error + "> with query:\n" + query);
		
		rs = ConnectionManager.getInstance().execQuery(query);
		
		while(rs.next()) {
			maps.add(MapScenarioHolder.getInstance()
					.getMapping(rs.getString(1)));
		}
		
		ConnectionManager.getInstance().closeRs(rs);
		
		log.debug("Get mapping provenance for <" + error + "> returned <" + maps + ">");
		
		return maps;
	}
}
