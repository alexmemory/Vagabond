package org.vagabond.explanation.marker.query;

import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;

public class QueryMarkerSetGenerator {

	static Logger log = Logger.getLogger(QueryMarkerSetGenerator.class);
	
	private static QueryMarkerSetGenerator instance;
	
	static {
		instance = new QueryMarkerSetGenerator();
	}
	
	public static QueryMarkerSetGenerator getInstance () {
		return instance;
	}
	
	public IMarkerSet genMSetFromQuery (String query) {
		return null;
	}
	
}
