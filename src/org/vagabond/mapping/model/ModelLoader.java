package org.vagabond.mapping.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelInstanceFileType;
import org.vagabond.xmlmodel.RelInstanceType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SchemaType;
import org.vagabond.xmlmodel.SchemasType;
import org.vagabond.xmlmodel.StringRefType;
import org.vagabond.xmlmodel.TransformationType;

/**
 * Singleton class for loading mapping scenarios from XML documents. Internally
 * XMLBeans is used to parse the XML document and create a Java model of the
 * mapping scenario.
 * 
 * @author Boris Glavic
 */
public class ModelLoader {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			ModelLoader.class.getName());

	private static ModelLoader instance = new ModelLoader();
	private boolean validation = true;
	
	private ModelLoader() {

	}

	/**
	 * @return The singleton instance of this class.
	 */

	public static ModelLoader getInstance() {
		return instance;
	}

	public void loadToInst(String fileName) throws Exception {
		MapScenarioHolder.getInstance().setDocument(
				getDoc(new FileInputStream(fileName)));
	}

	public void loadToInst(InputStream in) throws Exception {
		MapScenarioHolder.getInstance().setDocument(getDoc(in));
	}

	public void loadToInst(File inFile) throws Exception {
		MapScenarioHolder.getInstance().setDocument(
				getDoc(new FileInputStream(inFile)));
	}

	public MapScenarioHolder load(String fileName) throws Exception {
		return new MapScenarioHolder(getDoc(new FileInputStream(fileName)));
	}

	/**
	 * Load a mapping scenario from a file.
	 * 
	 * @param inFile
	 *            The XML file to load from.
	 * @return The mapping scenario.
	 * @throws Exception 
	 */

	public MapScenarioHolder load(File inFile) throws Exception {
		return new MapScenarioHolder(getDoc(new FileInputStream(inFile)));
	}

	/**
	 * Load a mapping scenario from an Java <code>InputStream</code>.
	 * 
	 * @param inStream
	 *            The input stream to read from.
	 * @return The mapping scenario.
	 * @throws Exception 
	 */

	public MapScenarioHolder load(InputStream inStream) throws Exception {
		return new MapScenarioHolder(getDoc(inStream));
	}

	private MappingScenarioDocument getDoc(InputStream in) throws Exception {
		MappingScenarioDocument doc;

		doc = MappingScenarioDocument.Factory.parse(in);
		if (isValidation())
			validate(doc);

		return doc;
	}

	/**
	 * Validate a mapping scenario XML document.
	 * 
	 * @param doc
	 *            The XMLBeans object for the document.
	 * @throws Exception If validation fails
	 */

	public void validate(MappingScenarioDocument doc)
			throws Exception {
		List<?> errors = new ArrayList();
		XmlOptions options = new XmlOptions();
		boolean result;

		if (log.isDebugEnabled()) {log.debug("validate mapping scenario");};

		options.setErrorListener(errors);

		result = doc.validate(options);
		for (Object error : errors) {
			log.error("Validation Error: " + error);
		}

		if (!result)
			throw new ValidationException(errors);
		
		stricterValidation(doc);
	}

	public void stricterValidation (MappingScenarioDocument doc) throws Exception {
		SchemasType schemas = doc.getMappingScenario().getSchemas();
		int numSourceRels = schemas.getSourceSchema().getRelationArray().length;
		int numTargetRels = schemas.getTargetSchema().getRelationArray().length;
		MapScenarioHolder scen = new MapScenarioHolder(doc);
		
		boolean hasTrans = doc.getMappingScenario().isSetTransformations();
		boolean hasCorrs = doc.getMappingScenario().isSetCorrespondences();
		boolean hasData = doc.getMappingScenario().isSetData();
		
		SchemaType src = doc.getMappingScenario().getSchemas().getSourceSchema();
		SchemaType trg = doc.getMappingScenario().getSchemas().getTargetSchema();
		
		if (numTargetRels == 0)
			throw new ValidationException("no target relations");

		// check that each source relation has an attached data element
		if (hasData)
			validateData(scen, numSourceRels);
		
		if (hasCorrs)
			validateCorr(scen);
		
		validateMaps(scen);
		
		if (hasTrans)
			validateTrans(scen, numTargetRels);
	}

	private void validateMaps(MapScenarioHolder scen) throws Exception {
		MappingScenarioDocument doc = scen.getDocument(); 
		
		// test that atoms in mappings have right arity
		for(MappingType m: doc.getMappingScenario().getMappings()
				.getMappingArray()) {
			Set<String> sourceRels = new HashSet<String> ();
			Set<String> trgRels = new HashSet<String> ();
			
			// check that atoms have the right number of arguments
			if (m.isSetForeach())
				for(RelAtomType a: m.getForeach().getAtomArray()) {
					checkAtomNumArgs(scen, a, false, m);
					sourceRels.add(a.getTableref());
				}
					
			for(RelAtomType a: m.getExists().getAtomArray()) {
				checkAtomNumArgs(scen, a, true, m);
				trgRels.add(a.getTableref());
			}
			
			// check that correspondences are over relations used in atoms
			if (scen.getDocument().getMappingScenario().isSetCorrespondences()
					&& m.isSetUses()) {	
				for(StringRefType c: m.getUses().getCorrespondenceArray()) {
					String corrName = c.getRef();
					if (!scen.hasCorr(corrName))
						throw new ValidationException("Mapping <" + m.getId() + 
								"> mentions non existing correspondence <" 
								+ corrName + ">");
					
					CorrespondenceType corr = scen.getCorr(corrName);
					
					if (!sourceRels.contains(corr.getFrom().getTableref()))
						throw new ValidationException ("Mapping <" + m.getId() 
								+ "> uses correspondence <" + corrName + 
								"> that mentions a source relation that is not " +
								"used in any LHS atom\n " + m.toString() + "\n\n" 
								+ corr.toString());
					
					if (!trgRels.contains(corr.getTo().getTableref()))
						throw new ValidationException ("Mapping <" + m.getId() 
								+ "> uses correspondence <" + corrName + 
								"> that mentions a target relation that is not " +
								"used in any RHS atom\n " + m.toString() + "\n\n" 
								+ corr.toString());
				}
			}
		}
	}

	private void validateTrans(MapScenarioHolder scen, int numTargetRels)
			throws Exception {
		int numTrans;
		MappingScenarioDocument doc = scen.getDocument();
		numTrans = doc.getMappingScenario().getTransformations()
				.getTransformationArray().length;
		if (numTrans < numTargetRels)
			throw new ValidationException("have to have at least as many " +
					"transformation than target relations");
		
		// check that transformation generates an existing target relation and that each 
		// target relation is generated by at least on transformation
		Set<String> targetRels = new HashSet<String> ();
		for(RelationType r: doc.getMappingScenario().getSchemas()
				.getTargetSchema().getRelationArray()) {
			targetRels.add(r.getName());
		}
		Set<String> expected = new HashSet<String> ();
		expected.addAll(targetRels);
		
		for(TransformationType t: doc.getMappingScenario().getTransformations()
				.getTransformationArray()) {
			// target relation exists
			String creates = t.getCreates();
			if (!targetRels.contains(creates))
				throw new ValidationException ("Transformation <" + t.getId() 
						+ "> creates" +" non-existing target relation <" 
						+ creates + ">\n" + t.toString());
			expected.remove(creates);
			// check that mappings exists and has a target atom for the relation
			// created by the transformation
			for(StringRefType mRef: t.getImplements().getMappingArray()) {
				String mName = mRef.getRef();
				boolean found = false;
				
				if(!scen.hasMapping(mName))
					throw new ValidationException("Transformation <" 
							+ t.getId() + "> implements non-existing mapping <" 
							+ mName +">\n" +t.toString());
				
				MappingType m = scen.getMapping(mName);
				
				for(RelAtomType a: m.getExists().getAtomArray()) {
					if (a.getTableref().equals(creates))
						found = true;
				}
				
				if (!found)
					throw new ValidationException("Transformation <" + t.getId()
							+ "> implements a mapping <" + mName + "> without " +
									"the right RHS atom <" + creates + 
									">\n " + t.toString());
			}
		}
		
		// check if at least on transformation implements each mapping
		if (!expected.isEmpty()) {
			throw new ValidationException ("Some target relations are not " +
					"implemented by any mappings <" + expected.toString() + ">");
		}
	}

	private void validateCorr(MapScenarioHolder scen) throws Exception {
		for(CorrespondenceType corr: scen.getDocument().getMappingScenario()
				.getCorrespondences().getCorrespondenceArray()) {
			// check that from and two relations exist
			String fromRelName = corr.getFrom().getTableref();
			if (!scen.hasRelForName(fromRelName, false))
				throw new ValidationException("Relation <" + fromRelName 
						+ "> in \"from\" part of correspondence <" + corr.getId() 
						+ "> does not exist \n<"+ corr.toString() + ">");
			
			String toRelName = corr.getTo().getTableref();
			if (!scen.hasRelForName(toRelName, true))
				throw new ValidationException("Relation <" + toRelName 
						+ "> in \"to\" part of correspondence <" + corr.getId() 
						+ "> does not exist \n<"+ corr.toString() + ">");
			
			RelationType fromRel = scen.getRelForName(fromRelName, false);
			RelationType toRel = scen.getRelForName(toRelName, true);
			
			// check that attributes exist
			for(String a: corr.getFrom().getAttrArray()) {
				boolean found = false;
				
				for (AttrDefType att: fromRel.getAttrArray()) {
					if (a.equals(att.getName()))
						found = true;
				}
				if (!found)
					throw new ValidationException("Did not find attr <" + a + 
							"> mentioned in relation <" + fromRel.getName() + ">");
			}
			
			for(String a: corr.getTo().getAttrArray()) {
				boolean found = false;
				
				for (AttrDefType att: toRel.getAttrArray()) {
					if (a.equals(att.getName()))
						found = true;
				}
				if (!found)
					throw new ValidationException("Did not find attr <" + a + 
							"> mentioned in relation <" + toRel.getName() + ">");
			}			
			
		}
	}
	
	private void validateData(MapScenarioHolder scen, int numSourceRels)
			throws ValidationException {
		int numData;
		MappingScenarioDocument doc = scen.getDocument();
		
		// build set of source relation names
		Set<String> relsExpected = new HashSet<String> ();
		for(RelationType rel: doc.getMappingScenario()
				.getSchemas().getSourceSchema().getRelationArray()) {
			relsExpected.add(rel.getName());
		}
		
		// check number of data element = number of source relations
		numData = doc.getMappingScenario().getData().getInstanceArray().length + 
				doc.getMappingScenario().getData().getInstanceFileArray().length;
		if (numData != numSourceRels)
			throw new ValidationException("need the same amount of data elements as source relations");
		
		for(RelInstanceType i: doc.getMappingScenario().getData().getInstanceArray()) {
			String rel = i.getName();
			if (!relsExpected.contains(rel)) {
				throw new ValidationException("Instance Data Element for Unkown" +
						" relation: <" + rel + "> found <" + i.toString() + ">");
			}
			relsExpected.remove(rel);
		}
		for(RelInstanceFileType i: doc.getMappingScenario().getData().getInstanceFileArray()) {
			String rel = i.getName();
			if (!relsExpected.contains(rel)) {
				throw new ValidationException("Instance Data File Element for Unkown" +
						" relation: <" + rel + "> found <" + i.toString() + ">");
			}
			relsExpected.remove(rel);
		}
		
		// some relations do not have associated data elements?		
		if (!relsExpected.isEmpty())
			throw new ValidationException("Some relations are missing instance " +
					"elements: <" + relsExpected.toString() + ">");
	}

	private void checkAtomNumArgs(MapScenarioHolder scen, RelAtomType a, 
			boolean trg, MappingType m)
			throws Exception {
		String relName = a.getTableref();
		if (!scen.hasRelForName(relName, trg))
			throw new ValidationException ("Relation <" + relName + "> does not" +
					" exist: mentioned in atom <" + a.toString() + "> of mapping <"
					+ m.toString() + ">");
		
		RelationType r = scen.getRelForName(relName, trg);
		int numAttr = r.sizeOfAttrArray();
		if (numAttr != a.sizeOfConstantArray() + a.sizeOfFunctionArray() 
				+ a.sizeOfSKFunctionArray() + a.sizeOfVarArray())
			throw new ValidationException ("Atom " + a.toString() + " has not the" +
					" same number of arguments as the relation it refers " +
					"to " + r.toString() + "\n mapping is <" + m.toString() + ">");
	}
	
	public void storeModel(OutputStream o, MappingScenarioDocument doc)
			throws Exception {
		validate(doc);
		doc.save(o);
	}

	public void storeModel(OutputStream o) throws Exception {
		storeModel(o, MapScenarioHolder.getInstance().getDocument());
	}

	public void storeModel(String fileName, MappingScenarioDocument doc)
			throws Exception {
		storeModel(new FileOutputStream(fileName), doc);
	}

	public void storeModel(String fileName) throws Exception {
		storeModel(fileName, MapScenarioHolder.getInstance().getDocument());
	}

	public boolean isValidation() {
		return validation;
	}

	public void setValidation(boolean validation) {
		this.validation = validation;
	}

}
