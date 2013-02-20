package org.vagabond.mapping.model.serialize.mapfile;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.xmlmodel.AttrDefType;
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
		
		result.append("# name of mapping\n");
		result.append("Name: \n");
		result.append("# source and target schema\n");
		
		// output schemas
		result.append("Source:\n");
		outputSchema(doc.getSchemas().getSourceSchema(), result);
		result.append("Target:\n");
		outputSchema(doc.getSchemas().getTargetSchema(), result);
		
		// output mappings
		result.append("\n# constraints\n");
		outputMappings(doc.getMappings(), result);
		
		return result.toString();
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
