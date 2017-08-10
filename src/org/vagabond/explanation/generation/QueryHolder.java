package org.vagabond.explanation.generation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.PropertyWrapper;
import org.vagabond.util.QueryTemplate;

public class QueryHolder {

	static Logger log = LogProviderHolder.getInstance().getLogger(QueryHolder.class);
	
	public static final String DEFAULT_QUERY_LIST_FILE = "VagabondQueryList.txt";
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
	
	public void loadFromDirFallbackResource (File dir, String queryListFile) throws IOException {
		boolean useClassloader = false;
		String line = null;
		
		if (dir.exists() && dir.isDirectory()) {
			try {
				loadFromDir(dir);
			}
			catch (Exception e) {
				log.info("do not find " + dir);
				useClassloader = true;
			}
		}
		else	
			useClassloader = true;
		
		if (useClassloader) {
			queries = new PropertyWrapper();
			BufferedReader fileList = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(queryListFile)));
			while((line = fileList.readLine()) != null) {
				String l = line.trim();
				if (!line.trim().equals("")) {
					InputStream qFile = ClassLoader.getSystemResourceAsStream(l);
					queries.addFromXMLStream(qFile, l.replace(".xml", ""));
				}
			}
		}
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

	public void loadFromURLs (Map<String,URL> urlMap) throws InvalidPropertiesFormatException, IOException {
		queries = new PropertyWrapper();
		
		for(String key: urlMap.keySet()) {
			queries.addFromXMLStream(urlMap.get(key).openStream(), key);
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
