package org.tramp.expl.model;

import java.io.InputStream;
import java.util.logging.Logger;

import org.tramp.xmlmodel.MappingScenarioDocument;
import org.tramp.xmlmodel.MappingScenarioDocument.MappingScenario;

/*
 * 
 */
public class MapScenarioHolder {

	static Logger log = Logger.getLogger(MapScenarioHolder.class.getName());
	
	private MappingScenarioDocument doc;
	
	
	public MapScenarioHolder () {
		doc = null;
	}
	
	public MapScenarioHolder (MappingScenarioDocument doc) {
		setDocument (doc);
	}
	
	public void setDocument (MappingScenarioDocument doc) {
		this.doc = doc;
	}
	
	public MappingScenario getScenario () {
		return doc.getMappingScenario();
	}
	
	public boolean hasData () {
		return doc.getMappingScenario().getData() != null;
	}
}
