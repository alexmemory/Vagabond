package org.vagabond.mapping.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph;
import org.vagabond.mapping.stats.StatsQueryExecutor;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.GlobalResetter;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.StringRefType;
import org.vagabond.xmlmodel.TransformationType;

/**
 * Class that functions as a wrapper around  a mapping scenario class object. 
 * 
 * @author Boris Glavic
 *
 */
public class MapScenarioHolder {

	static Logger log = LogProviderHolder.getInstance().getLogger(MapScenarioHolder.class.getName());
	
	private static MapScenarioHolder instance = new MapScenarioHolder();
	
	protected MappingScenarioDocument doc; 
	private Map<MappingType, MappingGraph> graphsForMaps;
	private Map<TransformationType, RelationType[]> transToSource;
	private Map<TransformationType, RelationType> transToTarget;
	private ErrorPartitionGraph scenGraph;
	
	public static MapScenarioHolder getInstance() {
		return instance;
	}
	
	public MapScenarioHolder () {
		doc = null;
		init();
	}
	
	/**
	 * Create this object as a wrapper around <code>doc</code>.
	 * 
	 * @param doc
	 */
	
	public MapScenarioHolder (MappingScenarioDocument doc) {
		setDocument (doc);
		init();
	}
	
	protected void init() {
		graphsForMaps = new HashMap<MappingType, MappingGraph> ();
		transToSource = new HashMap<TransformationType, RelationType[]> ();
		transToTarget = new HashMap<TransformationType, RelationType> ();
	}
	
	/**
	 * Set the document this object is wrapping.
	 * 
	 * @param doc
	 */
	
	public void setDocument (MappingScenarioDocument doc) {
		this.doc = doc;
		GlobalResetter.getInstance().reset();
	}

	/**
	 * 
	 * @return The wrapped mapping scenario.
	 */
	
	public MappingScenario getScenario () {
		return doc.getMappingScenario();
	}
	
	public void reset () {
		graphsForMaps = new HashMap<MappingType, MappingGraph> ();
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
	
	public List<TransformationType> getTransCreatingRel (String relname) 
			throws Exception {
		List<TransformationType> result;
		
		result = new ArrayList<TransformationType> ();
		
		for(TransformationType trans: doc.getMappingScenario().
				getTransformations().getTransformationArray()) {
			if (trans.getCreates().equals(relname))
				result.add(trans);
		}
		
		return result;
	}
	
	public TransformationType getTransformation (String id) throws Exception {
		for (TransformationType trans: 
				doc.getMappingScenario().getTransformations()
						.getTransformationArray()) {
			if (trans.getId().equals(id))
				return trans;
		}
		
		throw new Exception ("no transformation for id <" + id + ">");
	}
	
	public Set<TransformationType> getTransformations (String ... names) throws Exception {
		Set<TransformationType> result = new HashSet<TransformationType> ();
		
		for(String name: names)
			result.add(getTransformation(name));
		
		return result;
	}
	
	public List<TransformationType> getTransForRels (Collection<String> rels) 
			throws Exception {
		List<TransformationType> result;
		
		result = new ArrayList<TransformationType> ();
		for(String rel: rels) {
			result.addAll(getTransCreatingRel(rel));
		}
		
		return result;
	}
	
	public TransformationType[] getTransForMap (MappingType m) {
		String id = m.getId();
		List<TransformationType> result;
		
		result = new ArrayList<TransformationType> ();
		
		for(TransformationType t: doc.getMappingScenario().getTransformations()
				.getTransformationArray()) {
			if (search(t.getImplements().getMappingArray(), id))
				result.add(t);
		}
		
		return result.toArray(new TransformationType[] {});
	}
	
	private boolean search (StringRefType[] array, String id) {
		for(StringRefType r: array)
			if (r.getRef().equals(id))
				return true;
		return false;
	}
	
	public RelationType getRelCreateByTrans (TransformationType t) throws Exception {
		if (!transToTarget.containsKey(t)) {
			String relId = t.getCreates();
			transToTarget.put(t, getRelForName(relId, true));
		}
		return transToTarget.get(t);
	}
	
	public RelationType[] getRelsAccessedByTrans (TransformationType t) throws Exception {		
		if (!transToSource.containsKey(t)) {
			List<String> relNames;
			RelationType [] rels;
			int i = 0;
			
			relNames = StatsQueryExecutor.getInstance().
					getRelsAccessedByTransformation(t.getCreates());
			rels = new RelationType[relNames.size()];
			for(String relName: relNames) {
				rels[i++] = getRelForName(relName, false);
			}
			
			transToSource.put(t, rels);
		}
		
		return transToSource.get(t);
	}
	
	public RelationType getRelForName (String relname, boolean target) throws Exception {
		RelationType[] rels;
		
		if (target)
			rels = doc.getMappingScenario().getSchemas().
					getTargetSchema().getRelationArray();
		else
			rels = doc.getMappingScenario().getSchemas().
					getSourceSchema().getRelationArray();
		
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
	
	public Set<MappingType> getMappings (String ... names) throws Exception {
		Set<MappingType> result = new HashSet<MappingType> ();
		
		for(String name: names)
			result.add(getMapping(name));
		
		return result;
	}
	
	public MappingType getMapping (String name) throws Exception {
		for(MappingType map: doc.getMappingScenario().getMappings()
				.getMappingArray()) {
			if (map.getId().equals(name))
				return map;
		}
		
		throw new Exception ("Did not find mapping with name <" + name + ">");
	}
	
	public MappingGraph getGraphForMapping (MappingType map) throws Exception {
		if (!graphsForMaps.containsKey(map)) {
			graphsForMaps.put(map,  new MappingGraph(map));
		}
		
		return graphsForMaps.get(map);
	}
	
	public MappingGraph getGraphForMapping (String name) throws Exception {
		return getGraphForMapping(getMapping(name));
	}
	
	public CorrespondenceType getCorr (String name) throws Exception {
		for (CorrespondenceType corr: doc.getMappingScenario()
				.getCorrespondences().getCorrespondenceArray()) {
			if (corr.getId().toUpperCase().equals(name) 
					|| corr.getId().toLowerCase().equals(name))
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
		
		for(StringRefType corrName: map.getUses().getCorrespondenceArray()) {
			corr = getCorr(corrName.getRef());
			result.add(corr);
		}
		
		return result;
	}
	
	public Set<CorrespondenceType> getCorrespondences (String ... ids) throws Exception {
		Set<CorrespondenceType> result = new HashSet<CorrespondenceType> ();
		
		for(String id: ids)
			result.add(getCorr(id));
		
		return result;
	}
	
	public Collection<MappingType> getMapsForCorr (CorrespondenceType corr) {
		Set<MappingType> maps;
		
		maps = new HashSet<MappingType> ();
		
		for(MappingType map: doc.getMappingScenario().getMappings()
				.getMappingArray()) {
			for (StringRefType use: map.getUses().getCorrespondenceArray()) {
				if (use.getRef().equals(corr.getId()))
					maps.add(map);
			}
		}
		
		return maps;
	}

	public ErrorPartitionGraph getScenGraph() {
		return scenGraph;
	}

	public void setScenGraph(ErrorPartitionGraph scenGraph) {
		this.scenGraph = scenGraph;
	}
	
	
}
