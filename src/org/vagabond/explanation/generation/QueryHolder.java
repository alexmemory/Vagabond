package org.vagabond.explanation.generation;

import org.apache.log4j.Logger;
import org.vagabond.util.PropertyWrapper;
import org.vagabond.util.QueryTemplate;

public class QueryHolder {

	static Logger log = Logger.getLogger(QueryHolder.class);
	
	private static QueryHolder instance = new QueryHolder();
	
	private PropertyWrapper queries;
	
	public static QueryHolder getInstance () {
		return instance;
	}
	
	public static QueryTemplate getQuery (String name) {
		return instance.queries.getQueryTemplate(name);
	}

	public PropertyWrapper getQueries() {
		return queries;
	}

	public void setQueries(PropertyWrapper queries) {
		this.queries = queries;
	}
	
	public void setPraefix (String prefix) {
		this.queries.setPrefix(prefix);
	}
}
