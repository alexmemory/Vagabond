package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.MappingsType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SchemaType;

public class ScenarioDictionary {

	static Logger log = LogProviderHolder.getInstance().getLogger(ScenarioDictionary.class);
	
	private static ScenarioDictionary instance = new ScenarioDictionary();
	
	private List<MappingType> maps;
	private ArrayList<ArrayList<String>> varNames;
	private List<RelationType> rels;
	private SchemaType sourceSchema;
	private SchemaType targetSchema;
	private int totalAttrCount = -1;
	private int totalVarCount = -1;
	
	private ScenarioDictionary () {
		rels = new ArrayList<RelationType> ();
		maps = new ArrayList<MappingType> ();
		varNames = new ArrayList<ArrayList<String>> ();
	}
	
	public static ScenarioDictionary getInstance () {
		return instance;
	}
	
	public void initFromScenario () {
		setSchemas(MapScenarioHolder.getInstance().getScenario().getSchemas()
						.getSourceSchema(),
				MapScenarioHolder.getInstance().getScenario().getSchemas()
						.getTargetSchema());
		setMappings(MapScenarioHolder.getInstance().getScenario().getMappings());
	}
	
	public void setMappings (MappingsType mappings) {
		ArrayList<String> vars;
		maps = new ArrayList<MappingType> ();
		varNames = new ArrayList<ArrayList<String>> ();
		totalVarCount = -1;
		for(MappingType m: mappings.getMappingArray()) {
			maps.add(m);
			vars = new ArrayList<String> ();
			varNames.add(vars);
			
			for(RelAtomType a: m.getForeach().getAtomArray()) {
				for (String var: a.getVarArray()) {
					if (!vars.contains(var))
						vars.add(var);
				}
			}
			
			for(RelAtomType a: m.getExists().getAtomArray()) {
				for (String var: a.getVarArray()) {
					if (!vars.contains(var))
						vars.add(var);
				}
			}
			
		}
	}

	public void setSchemas (SchemaType source, SchemaType target) {
		this.sourceSchema = source;
		this.targetSchema = target;
		totalAttrCount = -1;
		rels = new ArrayList<RelationType> ();
		for(RelationType rel: sourceSchema.getRelationArray())
			rels.add(rel);
		for(RelationType rel: targetSchema.getRelationArray())
			rels.add(rel);
	}
	
	public String getRelName (int relId) {
		return rels.get(relId).getName();
	}
	
	public int getRelId (String relName) throws Exception {
		RelationType rel;
		
		for(int i=0; i < rels.size(); i++) {
			rel = rels.get(i);
			if (rel.getName().equals(relName))
				return i;
		}
		
		throw new Exception ("Did not find relation <" + relName + ">");
	}
	
	public boolean validateRelId (int relId) {
		return relId >= 0 && relId < rels.size();
	}
	
	public boolean validateAttrId (int relId, int attrId) {
		RelationType rel;
		
		if (!validateRelId(relId) || attrId < 0)
			return false;
		rel = rels.get(relId);
		
		return attrId < rel.getAttrArray().length;
	}
	
	public String getAttrName (int relId, int attrId) {
		assert(validateRelId(relId) && validateAttrId(relId, attrId));
		
		return rels.get(relId).getAttrArray()[attrId].getName();
	}
	
	public String getAttrName (String relName, int attrId) throws Exception {
		return getAttrName(getRelId(relName), attrId);
	}
	
	public int getAttrId (String relName, String attrName) throws Exception {
		return getAttrId (getRelId(relName), attrName);
	}
	
	public int getAttrId (int relId, String attrName) throws Exception {
		RelationType rel;
		AttrDefType attr;
		
		rel = rels.get(relId);
		
		for(int i = 0; i < rel.getAttrArray().length; i++) {
			attr = rel.getAttrArray(i);
			if (attr.getName().equals(attrName))
				return i;
		}
		
		throw new Exception ("Did not find attr <" + attrName + "> for " +
				"relation <" + rel.getName() + ">");
	}
	
	public int getTupleSize (int relId) {
		RelationType rel;
		
		rel = rels.get(relId);
		return rel.getAttrArray().length;
	}
	
	public SchemaType getTargetSchema() {
		return targetSchema;
	}
	
	public String getMapName (int mapId) {
		return maps.get(mapId).getId();
	}
	
	public String getVarName (int mapId, int varId) {
		return varNames.get(mapId).get(varId);
	}
	
	public int getMapId (String mapName) throws Exception {
		MappingType m;
		
		for(int i = 0; i < maps.size(); i++) {
			m = maps.get(i);
			if (m.getId().equals(mapName))
				return i;
		}
		
		throw new Exception ("Did not find mapping <" + mapName + ">");
	}
	
	public int getVarId (String mapName, String varName) throws Exception {
		int mapId = getMapId(mapName);
		ArrayList<String> vars = varNames.get(mapId);
		
		for(int i = 0; i < vars.size(); i++) {
			if (vars.get(i).equals(varName))
				return i;
		}
		
		throw new Exception ("Did not find var <" + varName + "> for mapping <" + mapName + ">");
	}

	public List<String> getVarNames (int mapId) {
		return varNames.get(mapId);
	}
	
	public int getNumVars (int mapId) {
		return varNames.get(mapId).size();
	}
	
	public int getMapCount () {
		return maps.size();
	}
	
	public int getRelCount () {
		return rels.size();
	}
	
	public int getSchemaRelCount (boolean source) {
		if (source)
			return sourceSchema.getRelationArray().length;
		else
			return targetSchema.getRelationArray().length;
	}
	
	public int getSourceAttrCount () {
		return getAttrCount(sourceSchema.getRelationArray());
	}
	
	public int getTargetAttrCount () {
		return getAttrCount(targetSchema.getRelationArray());
	}
	
	private int getAttrCount (List<RelationType> rels) {
		int attrCount = 0;
		
		for(RelationType r: rels)
			attrCount += r.getAttrArray().length;
		
		return attrCount;
	}
	
	private int getAttrCount (RelationType[] rels) {
		int attrCount = 0;
		
		for(RelationType r: rels)
			attrCount += r.getAttrArray().length;
		
		return attrCount;
	}
	
	public int getTotalAttrCount ()  {
		if (totalAttrCount == -1)
			totalAttrCount = getAttrCount(rels);
		
		return totalAttrCount;
	}
	
	public int getTotalVarCount () {
		if (totalVarCount == -1) {
			totalVarCount = 0;
			for(int i = 0; i < varNames.size(); i++)
				totalVarCount += varNames.get(i).size();
		}
		
		return totalVarCount;
	}
}

