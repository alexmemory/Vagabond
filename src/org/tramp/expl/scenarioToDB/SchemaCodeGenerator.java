package org.tramp.expl.scenarioToDB;

import org.tramp.xmlmodel.AttrDefType;
import org.tramp.xmlmodel.AttrListType;
import org.tramp.xmlmodel.ForeignKeyType;
import org.tramp.xmlmodel.MappingScenarioDocument.MappingScenario;
import org.tramp.xmlmodel.RelInstanceFileType;
import org.tramp.xmlmodel.RelInstanceType;
import org.tramp.xmlmodel.RelInstanceType.Row;
import org.tramp.xmlmodel.RelationType;
import org.tramp.xmlmodel.SchemaType;
import org.tramp.xmlmodel.SchemasType;
import org.tramp.xmlmodel.TransformationType;
import org.tramp.xmlmodel.TransformationsType;

public class SchemaCodeGenerator {

	private static SchemaCodeGenerator instance;
	
	private SchemaCodeGenerator () {
		
	}
	
	public static SchemaCodeGenerator getInstance () {
		if (instance == null) 
			instance = new SchemaCodeGenerator();
		return instance;
	}
	
	public String getSchemaPlusInstanceCode (MappingScenario map) {
		StringBuffer result = new StringBuffer ();
		
		getSchemasCode(map, result, false);
		getInstanceCode(map, "source", result);
		getAllSourceForeignKeysCode(map.getSchemas().getSourceSchema(), 
				"source", result);
		
		return result.toString();
	}
	
	public String getSchemasCode (MappingScenario map) {
		StringBuffer result = new StringBuffer ();
		
		getSchemasCode (map, result, true);
		
		return result.toString();
	}
	
	private void getSchemasCode (MappingScenario map, StringBuffer result, 
			boolean addFKeys) {
		getSchemaCode(map.getSchemas().getSourceSchema(), "source", result,
				addFKeys);
		result.append('\n');
		getTargetSchemaCode(map, "target", result);
	}
	
	
	public String getSchemaCode (SchemaType schema) {
		return getSchemaCode (schema, null);
	}
	
	public String getSchemaCode (SchemaType schema, String schemaName) {
		StringBuffer result = new StringBuffer();
		
		getSchemaCode(schema, schemaName, result, true);
		
		return result.toString();
	}
	
	private void getSchemaCode (SchemaType schema, String schemaName, 
			StringBuffer result, boolean addForeignKeys) {
		if (schemaName == null) {
			for(RelationType rel: schema.getRelationArray()) {
				result.append("DROP TABLE IF EXISTS " + rel.getName() +" CASCADE;\n");
			}
			result.append('\n');
		}
		else {
			result.append(getCreateSchemaCode(schemaName));
		}
		
		for(RelationType rel: schema.getRelationArray()) {
			getRelationCode(rel, schemaName, result);
			result.append("\n");
		}
		
		if (addForeignKeys)
			getAllSourceForeignKeysCode(schema, schemaName, result);
	}
	
	private String getCreateSchemaCode (String schemaName) {
		return "DROP SCHEMA IF EXISTS " + schemaName + " CASCADE;\n" +
				"CREATE SCHEMA " + schemaName + ";\n\n";
	}
	
	private void getRelationCode (RelationType rel, String schemaName, 
			StringBuffer buf) {
		schemaName = getSchemaString (schemaName);
		
		buf.append("CREATE TABLE " + schemaName + rel.getName() + "(\n");
		
		for(AttrDefType attr : rel.getAttrArray()) {
			buf.append(attr.getName() + " " + attr.getDataType());
			buf.append(attr.getNotNull() == null ? ",\n" : " NOT NULL,\n");
		}
		
		getPrimKey(rel.getPrimaryKey(), buf);
		
		buf.append(") WITH OIDS;\n");
	}
	
	private void getPrimKey (AttrListType primKey, StringBuffer buf) {
		char delim = ',';
		
		buf.append("PRIMARY KEY (");
		for(String attr : primKey.getAttrArray()) {
			buf.append(attr + delim);
		}
		buf.deleteCharAt(buf.length() - 1);
		buf.append(")\n");
	}
	
	private void getAllSourceForeignKeysCode (SchemaType schema, 
			String schemaName, StringBuffer buf) {
		for(ForeignKeyType fkey: schema.getForeignKeyArray()) {
			buf.append("\n");
			getForeignKeyCode(fkey, schemaName, buf);
		}
	}
	
	private void getForeignKeyCode (ForeignKeyType fkey, String schemaName, 
			StringBuffer buf) {
		char delim = ',';
		
		schemaName = getSchemaString (schemaName);
		
		buf.append("ALTER TABLE " + schemaName + fkey.getFrom().getTableref() + 
				" ADD FOREIGN KEY (");
		for(String attr: fkey.getFrom().getAttrArray()) {
			buf.append(attr + delim);		
		}
		buf.deleteCharAt(buf.length() - 1);
		
		buf.append(") REFERENCES " + schemaName + fkey.getTo().getTableref() + " (");
		
		for(String attr: fkey.getTo().getAttrArray()) {
			buf.append(attr + delim);
		}
		buf.deleteCharAt(buf.length() - 1);
		buf.append(");\n");
	}
	
	public String getTargetSchemaCode (MappingScenario scenario, String schemaName) {
		TransformationsType transes;
		StringBuffer result = new StringBuffer();
		
		getTargetSchemaCode(scenario, schemaName, result);
		
		return result.toString();
	}
	
	private void getTargetSchemaCode (MappingScenario scenario, String schemaName, 
			StringBuffer result) {
		TransformationsType transes;
		
		result.append(getCreateSchemaCode(schemaName));
		schemaName = getSchemaString(schemaName);
		transes = scenario.getTransformations();
		
		for(TransformationType trans: transes.getTransformationArray()) {
			getTransViewCode(trans, result, schemaName);
		}
	}
	
	private void getTransViewCode (TransformationType trans, StringBuffer buf, 
			String schemaName) {
		buf.append("CREATE VIEW " + schemaName + trans.getCreates() + " AS (\n");
		buf.append(trans.getCode().trim());
		buf.append("\n);\n\n");
	}
	
	private String getSchemaString (String schemaName) {
		if (schemaName == null)
			return "";
		return schemaName + ".";
	}

	public String getInstanceCode (MappingScenario map) {
		return getInstanceCode(map, null);
	}
	
	public String getInstanceCode (MappingScenario map, String schemaName) {
		StringBuffer buf = new StringBuffer();
		
		schemaName = getSchemaString(schemaName);
		getInstanceCode(map, schemaName, buf);
		
		return buf.toString();
	}
	
	private void getInstanceCode (MappingScenario map, String schemaName, StringBuffer buf) {
		schemaName = getSchemaString(schemaName);
	
		for (RelInstanceType inst: map.getData().getInstanceArray()) {
			getInserts(schemaName, buf, inst);
		}
		
		for (RelInstanceFileType inst: map.getData().getInstanceFileArray()) {
			getCopy(schemaName, buf, inst);
		}
	}
	
	private void getInserts (String schemaName, StringBuffer buf, 
			RelInstanceType inst) {
		for(Row row: inst.getRowArray()) {
			buf.append("INSERT INTO " + schemaName + inst.getName() + " VALUES (");
		
			for(String val : row.getValueArray()) {
				if(val.equals("NULL"))
					buf.append("NULL,");
				else 
					buf.append("'" + val + "',");
			}
			buf.deleteCharAt(buf.length() - 1);
			buf.append(");\n");
		}
		buf.append("\n");
	}
	
	private void getCopy (String schemaName, StringBuffer buf, 
			RelInstanceFileType inst) {
		
	}
}

