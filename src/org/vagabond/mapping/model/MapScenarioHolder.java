package org.vagabond.mapping.model;

import java.util.logging.Logger;

import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;

/**
 * Class that functions as a wrapper around  a mapping scenario class object. 
 * 
 * @author Boris Glavic
 *
 */
public class MapScenarioHolder {

	static Logger log = Logger.getLogger(MapScenarioHolder.class.getName());
	
	private MappingScenarioDocument doc;
	
	
	public MapScenarioHolder () {
		doc = null;
	}
	
	/**
	 * Create this object as a wrapper around <code>doc</code>.
	 * 
	 * @param doc
	 */
	
	public MapScenarioHolder (MappingScenarioDocument doc) {
		setDocument (doc);
	}
	
	/**
	 * Set the document this object is wrapping.
	 * 
	 * @param doc
	 */
	
	public void setDocument (MappingScenarioDocument doc) {
		this.doc = doc;
	}

	/**
	 * 
	 * @return The wrapped mapping scenario.
	 */
	
	public MappingScenario getScenario () {
		return doc.getMappingScenario();
	}
	
	/**
	 * Check if the wrapped scenarion has instance data.
	 * 
	 * @return True, if the wrapped scenario has instance data. 
	 */
	
	public boolean hasData () {
		return doc.getMappingScenario().getData() != null;
	}
}
