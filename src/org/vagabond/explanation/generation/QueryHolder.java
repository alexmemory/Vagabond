package org.vagabond.explanation.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.vagabond.util.PropertyWrapper;
import org.vagabond.util.QueryTemplate;

public class QueryHolder {

	static Logger log = Logger.getLogger(QueryHolder.class);
	
	private static QueryHolder instance = new QueryHolder();
	
	private PropertyWrapper queries;
	
	private QueryHolder () {
		
	}
	
	public static QueryHolder getInstance () {
		return instance;
	}
	
	public static QueryTemplate getQuery (String name) {
		return instance.queries.getQueryTemplate(name);
	}
	
	public static boolean hasQuery (String name) {
		return instance.queries.containsKey(name);
	}
	
	public void loadFromDir (File dir) throws FileNotFoundException, IOException {
		File[] files;
		
		queries = new PropertyWrapper ();
		
		files = dir.listFiles(new FilenameFilter () {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
			
		});
		
		for (File file: files) {
			queries.addFromXMLFile(file, file.getName().replace(".xml", ""));
		}
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
