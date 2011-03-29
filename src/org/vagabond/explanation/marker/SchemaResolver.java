package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SchemaType;

public class SchemaResolver {

	static Logger log = Logger.getLogger(SchemaResolver.class);
	
	private static SchemaResolver instance = new SchemaResolver();
	
	private SchemaType sourceSchema;
	private SchemaType targetSchema;
	private List<RelationType> rels;
	
	private SchemaResolver () {
		rels = new ArrayList<RelationType> ();
	}
	
	public static SchemaResolver getInstance () {
		return instance;
	}
	
	public void setSchemas () {
		setSchemas(MapScenarioHolder.getInstance().getScenario().getSchemas()
						.getSourceSchema(),
				MapScenarioHolder.getInstance().getScenario().getSchemas()
						.getTargetSchema());
	}

	public void setSchemas (SchemaType source, SchemaType target) {
		this.sourceSchema = source;
		this.targetSchema = target;
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
	
	public String getAttrName (int relId, int attrId) {
		RelationType rel;
		
		rel = rels.get(relId);
		return rel.getAttrArray()[attrId].getName();
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
	
}
