package org.vagabond.mapping.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.ResultSetUtil;
import org.vagabond.util.UniqueStack;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.RelationType;

public class StatsQueryExecutor {

	static Logger log = LogProviderHolder.getInstance().getLogger(StatsQueryExecutor.class);
	
	private static StatsQueryExecutor instance = new StatsQueryExecutor();
	
	private StatsQueryExecutor () {
		
	}
	
	public static StatsQueryExecutor getInstance () {
		return instance;
	}
	
	public Map<String,Float> getMapDistrForTarget (String target) throws SQLException, ClassNotFoundException {
		Map<String,Float> result;
		ResultSet rs;
		String query;

		result = new HashMap<String,Float> ();
		query = QueryHolder.getQuery("Stats.GetMapDistributionForTarget")
				.parameterize("target." + target);
		rs = ConnectionManager.getInstance().execQuery(query);
		
		return result;
	}
	
	public List<String> getRelsAccessedByTransformation (String target) throws Exception {
		List<String> rels;
		
		rels  = ResultSetUtil.getUniqueBaseRelsForProvSchema(ProvenanceGenerator
				.getInstance().getProvSchemaForTarget(target));
		 
		return rels;
	}
	
	
}
