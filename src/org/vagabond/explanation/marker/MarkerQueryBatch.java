package org.vagabond.explanation.marker;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;

public class MarkerQueryBatch extends MarkerSet {

	static Logger log = LogProviderHolder.getInstance().getLogger(MarkerQueryBatch.class);
	
	private String query;
	
	public MarkerQueryBatch(String relName, String predicate) throws Exception {
		ResultSet rs;
		
		if (relName.toUpperCase().startsWith("SELECT")) {
			relName = "(" + relName + ")";
		}
		
		query = QueryHolder.getQuery("MarkerQueryBatch.GetQuery")
				.parameterize(relName, predicate);
		if (log.isDebugEnabled()) {log.debug("Compute markers for query:\n" + query);};

		rs = ConnectionManager.getInstance().execQuery(query);
		
		while(rs.next()) {
			String rel = rs.getString(1);
			String tid = rs.getString(2);
			String attBits = rs.getString(3);
			
			for (int i=0; i < attBits.length(); i++) {
				if (attBits.charAt(i) == '1') {
					String attName = ScenarioDictionary.getInstance().getAttrName(rel, i);
					ISingleMarker m = new AttrValueMarker(rel, tid, attName);
					markers.add(m);
				}
			}
			
		}
		
		ConnectionManager.getInstance().closeRs(rs);
		
	}
	
	@Override
	public IMarkerSet cloneSet() {
		return super.cloneSet();
	}
	
	public String getQuery() {
		return query;
	}

}
