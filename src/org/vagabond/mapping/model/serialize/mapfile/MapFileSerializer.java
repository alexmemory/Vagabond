package org.vagabond.mapping.model.serialize.mapfile;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.AttrRefType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.FunctionType;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.MappingsType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;
import org.vagabond.xmlmodel.SchemaType;

public class MapFileSerializer {

	static Logger log = Logger.getLogger(MapFileSerializer.class);
	
	private static MapFileSerializer instance = new MapFileSerializer();
	
	private MapFileSerializer () {

	}
	
	public static MapFileSerializer getInstance() {
		return instance;
	}
	
	public String transformToMap (MappingScenario doc) throws Exception {
		StringBuilder result = new StringBuilder();
		
		result.append("################# NAME OF MAPPING ##########################\n");
		result.append("Name: \n");
		result.append("################# SOURCE AND TARGET SCHEMA #################\n");
		
		// output schemas
		result.append("Source:\n");
		outputSchema(doc.getSchemas().getSourceSchema(), result);
		result.append("Target:\n");
		outputSchema(doc.getSchemas().getTargetSchema(), result);
		
		// output mappings
		result.append("\n############### MAPPINGS ################################\n");
		outputMappings(doc.getMappings(), result);
		
		// output PK and foreign key constraints
		result.append("\n############### PK CONSTRAINTS ##########################\n");
		outputPKConstraints(doc, result);
		result.append("\n############### FK CONSTRAINTS ##########################\n");
		outputFKConstraints(doc, result);
		
		return result.toString();
	}

	private void outputFKConstraints(MappingScenario doc, StringBuilder result) {
		result.append("\n## SOURCE SCHEMA\n");
		outputFKConstraints(doc.getSchemas().getSourceSchema(), result);
		result.append("\n## TARGET SCHEMA\n");
		outputFKConstraints(doc.getSchemas().getTargetSchema(), result);
	}

	private void outputFKConstraints(SchemaType schema,StringBuilder result) {
		for(ForeignKeyType fk: schema.getForeignKeyArray()) {
			result.append("# ");
			outputFKPart(fk.getFrom(),result);
			result.append(" -> ");
			outputFKPart(fk.getTo(),result);
			result.append("\n");
		}
		
	}

	private void outputFKPart(AttrRefType a, StringBuilder result) {
		result.append(a.getTableref() + "(");
		for(String at: a.getAttrArray()) {
			result.append(at);
			result.append(", ");
		}
		result.delete(result.length() - 2, result.length());
		result.append(")");
	}

	private void outputPKConstraints(MappingScenario doc, StringBuilder result) {
		result.append("\n## SOURCE SCHEMA\n");
		outputPKConstraints(doc.getSchemas().getSourceSchema(), result);
		result.append("\n## TARGET SCHEMA\n");
		outputPKConstraints(doc.getSchemas().getTargetSchema(), result);	
	}

	private void outputPKConstraints(SchemaType schema, StringBuilder result) {
		for(RelationType r: schema.getRelationArray()) {
			if (r.isSetPrimaryKey()) {
				result.append("# " + r.getName() + "(");
				
				for(String a: r.getPrimaryKey().getAttrArray()) {
					result.append(a);
					result.append(", ");
				}
				
				result.delete(result.length() - 2, result.length());
				result.append(")\n");
			}
		}		
	}

	private void outputMappings(MappingsType mappings, StringBuilder buf) throws Exception {
		XmlObject[] args;
		
		for(MappingType m: mappings.getMappingArray()) {
			buf.append("# --------- Mapping " + m.getId() + " -------\n");
			for(RelAtomType a: m.getForeach().getAtomArray()) {
				outputAtom(buf, m, a);
				buf.append(" & ");
			}

			buf.delete(buf.length() - 3, buf.length());
			buf.append(" -> ");
			
			for(RelAtomType a: m.getExists().getAtomArray()) {	
				outputAtom(buf, m, a);
				buf.append(" & ");
			}

			buf.delete(buf.length() - 3, buf.length());
			buf.append("\n");
		}
	}

	private void outputAtom(StringBuilder buf, MappingType m, RelAtomType a)
			throws Exception {
		XmlObject[] args;
		buf.append(a.getTableref() + "(");
		args = MapScenarioHolder.getInstance().getAtomArguments(a);
		for (int j = 0; j < args.length; j++) {
			buf.append(atomArgToString(args[j]));
			buf.append(", ");
		}
		buf.delete(buf.length() - 2, buf.length());
		buf.append(")");
	}
	
	private String atomArgToString (XmlObject arg) throws Exception {
		if (arg instanceof SKFunction)
			return skFunctionToString ((SKFunction) arg);
		if (arg instanceof XmlString)
			return ((XmlString) arg).getStringValue(); 
		if (arg instanceof FunctionType)
			return functionToString ((FunctionType) arg);
		
		throw new Exception ("unexpected object type: " + arg.getClass());
	}

	private String functionToString(FunctionType f) throws Exception {
		StringBuilder b = new StringBuilder();
		int numElements = f.sizeOfConstantArray() + f.sizeOfFunctionArray() + 
				f.sizeOfSKFunctionArray() + f.sizeOfVarArray();

		b.append(f.getFname());
		XmlCursor c = f.newCursor();
		argsToString(c, b, numElements);
		
		return b.toString();
	}

	private void argsToString(XmlCursor c, StringBuilder b, int numElements)
			throws Exception {
		b.append("(");

		c.toChild(0);
		for(int i = 0; i < numElements; i++) {
			XmlObject o = (XmlObject) c.getObject();
			b.append(atomArgToString(o) + ", ");
			c.toNextSibling();
		}
		
		b.delete(b.length() - 2, b.length());
		b.append(")");
	}

	private String skFunctionToString (SKFunction f) throws Exception {
		StringBuilder b = new StringBuilder();
		int numElements = f.sizeOfFunctionArray() + 
				f.sizeOfSKFunctionArray() + f.sizeOfVarArray();

		b.append(f.getSkname());
		XmlCursor c = f.newCursor();
		argsToString(c, b, numElements);
		
		return b.toString();
	}
	
	private void outputSchema(SchemaType schema, StringBuilder buf) {
		for(RelationType r: schema.getRelationArray()) {
			buf.append("\t" + r.getName() + "(");
			for(AttrDefType a: r.getAttrArray()) {
				buf.append(a.getName() + ", ");
			}
			buf.delete(buf.length() - 2, buf.length());
			buf.append(");\n");
		}
	}
	
	
}
