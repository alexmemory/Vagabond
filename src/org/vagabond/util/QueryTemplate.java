package org.vagabond.util;

import org.apache.log4j.Logger;

public class QueryTemplate {

	static Logger log = LogProviderHolder.getInstance().getLogger(QueryTemplate.class);
	
	private String queryText;
	
	public QueryTemplate (String queryText) {
		this.queryText = queryText;
	}
	
	public String parameterize (String ... params) {
		String result = queryText;
		
		if (params == null)
			return result;
		
		for(int i = 1; i <= params.length; i++) {
			result = result.replaceAll("\\$\\{" + i + "\\}", params[i-1]);
		}
		
		return result; 
	}
	
}
