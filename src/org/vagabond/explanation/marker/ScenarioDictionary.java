package org.vagabond.explanation.marker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
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
	private List<IdMap<String>> TidMapping;
	private int[][] offsets;
	
	private ScenarioDictionary () {
		rels = new ArrayList<RelationType> ();
		maps = new ArrayList<MappingType> ();
		varNames = new ArrayList<ArrayList<String>> ();
		TidMapping = new ArrayList<IdMap<String>>();
	}
	
	public static ScenarioDictionary getInstance () {
		return instance;
	}
	
	public void initFromScenario () throws Exception {
		setSchemas(MapScenarioHolder.getInstance().getScenario().getSchemas()
						.getSourceSchema(),
				MapScenarioHolder.getInstance().getScenario().getSchemas()
						.getTargetSchema());
		setMappings(MapScenarioHolder.getInstance().getScenario().getMappings());
		initTidMappingGenerating();
		createOffsetsMapping ();
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
	
	public String getSchemaPlusRelName (int relId) {
		RelationType rel;
		RelationType[] schemaRels;
		rel = rels.get(relId);
		
		schemaRels = sourceSchema.getRelationArray();
		for(int i = 0; i < schemaRels.length; i++) {
			if (schemaRels[i] == rel)
				return "source." + rel.getName();
		}
		return "target." + rel.getName();
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
	
	public ArrayList<String> getAttrNameList(String relName) throws Exception {
		ArrayList<String> attNameList = new ArrayList<String>();
		for (org.vagabond.xmlmodel.AttrDefType r : rels.get(getRelId(relName)).getAttrArray()) {
			attNameList.add(r.getName());
		}
		return attNameList;
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
	
	public int getAttrCount(int relId) {
		return rels.get(relId).sizeOfAttrArray();
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
	
	
	
	
	
	
	public void updateTidTable() throws Exception{
		TidMapping.clear();
		initTidMappingGenerating();
	}
	
	public void initTidMappingGenerating() throws Exception {
		TidMapping.clear();
		for (int i = 0; i < rels.size(); i++)
			singleTableTidGenerating(i);
	}
	
	public void singleTableTidGenerating(int relId) throws SQLException, ClassNotFoundException {
		ResultSet rs;
		String fullRelName = getSchemaPlusRelName(relId);
		String query = "SELECT tid FROM " + fullRelName;
		TidMapping.add(new IdMap<String>());
		
		if (log.isDebugEnabled()) {log.debug("get tids for <" + fullRelName + "> using query:\n" + query);};
		
		rs = ConnectionManager.getInstance().execQuery(query);
		while(rs.next()) {
			TidMapping.get(relId).put(rs.getString("tid"));
		}
		ConnectionManager.getInstance().closeRs(rs);
		
	}
	
	public void createOffsetsMapping () {
		int curOffset = 0;
		
		offsets = new int[rels.size()][];
		
		for(int i = 0; i < rels.size(); i++) {
			RelationType rel = rels.get(i);
			offsets[i] = new int[rel.getAttrArray().length];
			for(int j = 0; j < rel.getAttrArray().length; j++) {
				offsets[i][j] = curOffset;
				curOffset += TidMapping.get(i).size();
			}
		}
	}
	
	public AttrValueMarker getAttrValueMarkerByIBitSet(int bitpos) throws Exception{
		int relId= 0;
		int attrId = 0;
		int rowIndex = 0;
		int columnIndex = 0;
		for (rowIndex = 0; rowIndex < offsets.length; rowIndex++){
			int[] row  = offsets[rowIndex];
			if(row != null){
				for(columnIndex = 0; columnIndex < row.length; columnIndex++){
					if(offsets[rowIndex][columnIndex]<bitpos){
						relId = rowIndex;
						attrId = columnIndex;
					}
					
					else if (offsets[rowIndex][columnIndex] >= bitpos){
						int tidId = bitpos - offsets[relId][attrId];
						return new AttrValueMarker(relId, getTidString(tidId, getRelName(relId)), attrId);
						
					}
				}
				
			}
			
		}
		return null;
	}
	
	public int attrMarkerToBitPos (IAttributeValueMarker m) {
		int result = 0;
		try {
			result = ScenarioDictionary.getInstance().getOffset (m.getRelId(), m.getAttrId(), m.getTid());
			return result;
		} catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
		return result;
	}
	
	public int getOffset (int relId, int attrId, String tid) throws Exception {
		return getOffsetForRelAttr(relId, attrId) + getTidInt(tid, relId);
	}
	
	public int getOffset (int relId, int attrId, int tidId) {
		return getOffsetForRelAttr(relId, attrId) + tidId;
	}
	
	public int getOffsetForRelAttr (String relName, String attrName) throws Exception { 
		return getOffsetForRelAttr (getRelId(relName), getAttrId(relName, attrName));
	}
	
	public int getOffsetForRelAttr (int relId, int attrId) {
		return offsets[relId][attrId];
	}
	
	public int getTidInt(String tidString, String relation) throws Exception{
		return TidMapping.get(getRelId(relation)).getId(tidString);	
	}

	public int getTidInt(String tidString, int relId) throws Exception{
		return TidMapping.get(relId).getId(tidString);	
	}
	
	public String getTidString(int tidInt, String relation) throws Exception{
		return TidMapping.get(getRelId(relation)).get(tidInt);
		
	}
	
	public String getTidString(int tidInt, int relation) throws Exception{
		return TidMapping.get(relation).get(tidInt);	
	}
	
	public int getTotalTidCount(){
		int totaltid = 0;
		for (int i = 0; i < TidMapping.size(); i++ ){
			totaltid += TidMapping.get(i).size();
		}
		return totaltid;
	}
	
	
	public void addTid(String tid, String relation) throws Exception{
		TidMapping.get(getRelId(relation)).put(tid);
	}
	
	public boolean containTidString(String tid, String relation) throws Exception{
		return TidMapping.get(getRelId(relation)).containsVal(tid);
	}
	
	public boolean containTidInt(int id, String relation) throws Exception{
		return TidMapping.get(getRelId(relation)).containsKey(id);
		
	}
	public int maxTidInt(String relation) throws Exception{
		return TidMapping.get(getRelId(relation)).getMaxId();
	}
	
}

