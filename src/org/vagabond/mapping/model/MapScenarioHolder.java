package org.vagabond.mapping.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.TransformationType;

/**
 * Class that functions as a wrapper around  a mapping scenario class object. 
 * 
 * @author Boris Glavic
 *
 */
public class MapScenarioHolder {

	static Logger log = Logger.getLogger(MapScenarioHolder.class.getName());
	
	private static MapScenarioHolder instance = new MapScenarioHolder();
	
	private MappingScenarioDocument doc; 
	
	
	public static MapScenarioHolder getInstance() {
		return instance;
	}
	
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
	
	public List<RelationType> getRelsAffectedByRels 
			(Collection<String> relnames) throws Exception {
		Set<RelationType> result;
		
		result = new HashSet<RelationType> ();
		
		for(MappingType map: doc.getMappingScenario().getMappings()
				.getMappingArray()) {
			for(RelAtomType atom: map.getForeach().getAtomArray()) {
				for(String relname: relnames) {
					if (atom.getTableref().equals(relname)) {
						for(RelAtomType affRel: map.getExists().getAtomArray()) {
							result.add(getRelForName(affRel.getTableref(), true));
						}
					}
				}
			}
		}
		
		return new ArrayList<RelationType> (result);
	}
	
	public List<TransformationType> getTransCreatingRel (String relname) throws Exception {
		RelationType rel;
		List<TransformationType> result;
		
		result = new ArrayList<TransformationType> ();
		
		rel = getRelForName (relname, true);
		for(TransformationType trans: doc.getMappingScenario().
				getTransformations().getTransformationArray()) {
			if (trans.getCreates().equals(relname))
				result.add(trans);
		}
		
		return result;
	}
	
	public RelationType getRelForName (String relname, boolean target) throws Exception {
		RelationType[] rels;
		
		if (target)
			rels = doc.getMappingScenario().getSchemas().
					getSourceSchema().getRelationArray();
		else
			rels = doc.getMappingScenario().getSchemas().
					getTargetSchema().getRelationArray();
		
		for (RelationType rel: rels) {
			if (rel.getName().equals(relname))
				return rel;
		}
		
		throw new Exception("Did not find " + (target ? "target" : "source") 
				+ " relation with name <" + relname + ">");
	}

	public MappingScenarioDocument getDocument() {
		return doc;
	}
	
	public MappingType getMapping (String name) throws Exception {
		for(MappingType map: doc.getMappingScenario().getMappings()
				.getMappingArray()) {
			if (map.getId().equals(name))
				return map;
		}
		
		throw new Exception ("Did not find mapping with name <" + name + ">");
	}
	
	public CorrespondenceType getCorr (String name) throws Exception {
		for (CorrespondenceType corr: doc.getMappingScenario()
				.getCorrespondences().getCorrespondenceArray()) {
			if (corr.getId().equals(name))
				return corr;
		}
		
		throw new Exception("Did not find correspondence with name <" 
				+ name + ">");
	}
	
	public Collection<CorrespondenceType> getCorrespondences 
			(MappingType map) throws Exception {
		Set<CorrespondenceType> result;
		CorrespondenceType corr;
		
		result = new HashSet<CorrespondenceType> ();
		
		for(String corrName: map.getUses().getCorrespondenceArray()) {
			corr = getCorr(corrName);
			result.add(corr);
		}
		
		return result;
	}
	
	public Collection<MappingType> getMapsForCorr (CorrespondenceType corr) {
		Set<MappingType> maps;
		
		maps = new HashSet<MappingType> ();
		
		for(MappingType map: doc.getMappingScenario().getMappings()
				.getMappingArray()) {
			for (String use: map.getUses().getCorrespondenceArray()) {
				if (use.equals(corr.getId()))
					maps.add(map);
			}
		}
		
		return maps;
	}
}
