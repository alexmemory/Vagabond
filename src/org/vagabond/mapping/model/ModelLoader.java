package org.vagabond.mapping.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.SchemasType;

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
	 * @throws XmlException
	 * @throws IOException
	 * @throws ValidationException
	 */

	public MapScenarioHolder load(File inFile) throws XmlException,
			IOException, ValidationException {
		return new MapScenarioHolder(getDoc(new FileInputStream(inFile)));
	}

	/**
	 * Load a mapping scenario from an Java <code>InputStream</code>.
	 * 
	 * @param inStream
	 *            The input stream to read from.
	 * @return The mapping scenario.
	 * @throws XmlException
	 * @throws IOException
	 * @throws ValidationException
	 */

	public MapScenarioHolder load(InputStream inStream) throws XmlException,
			IOException, ValidationException {
		return new MapScenarioHolder(getDoc(inStream));
	}

	private MappingScenarioDocument getDoc(InputStream in) throws XmlException,
			IOException, ValidationException {
		MappingScenarioDocument doc;

		doc = MappingScenarioDocument.Factory.parse(in);
		validate(doc);

		return doc;
	}

	/**
	 * Validate a mapping scenario XML document.
	 * 
	 * @param doc
	 *            The XMLBeans object for the document.
	 * @throws ValidationException
	 *             If the document does not conform to the XML schema for
	 *             mapping scenarios.
	 */

	public void validate(MappingScenarioDocument doc)
			throws ValidationException {
		List<?> errors = new ArrayList();
		XmlOptions options = new XmlOptions();
		boolean result;

		log.debug("validate mapping scenario");

		options.setErrorListener(errors);

		result = doc.validate(options);
		for (Object error : errors) {
			log.error("Validation Error: " + error);
		}

		if (!result)
			throw new ValidationException(errors);
		
		stricterValidation(doc);
	}

	public void stricterValidation (MappingScenarioDocument doc) throws ValidationException {
		SchemasType schemas = doc.getMappingScenario().getSchemas();
		int numSourceRels = schemas.getSourceSchema().getRelationArray().length;
		int numTargetRels = schemas.getTargetSchema().getRelationArray().length;
		int numTrans, numData;
		
		boolean hasTrans = doc.getMappingScenario().isSetTransformations();
		boolean hasCorrs = doc.getMappingScenario().isSetCorrespondences();
		boolean hasData = doc.getMappingScenario().isSetData();
		
		if (numTargetRels == 0)
			throw new ValidationException("no target relations");

		if (hasData) {
			numData = doc.getMappingScenario().getData().getInstanceArray().length + 
					doc.getMappingScenario().getData().getInstanceFileArray().length;
			if (numData != numSourceRels)
				throw new ValidationException("need the same amount of data elements as source relations");
		}
		
		if (hasTrans) {
			numTrans = doc.getMappingScenario().getTransformations().getTransformationArray().length;
			if (numTrans < numTargetRels)
				throw new ValidationException("have to have at least as many transformation than target relations");
		}
			
	}
	
	public void storeModel(OutputStream o, MappingScenarioDocument doc)
			throws IOException, ValidationException {
		validate(doc);
		doc.save(o);
	}

	public void storeModel(OutputStream o) throws IOException,
			ValidationException {
		storeModel(o, MapScenarioHolder.getInstance().getDocument());
	}

	public void storeModel(String fileName, MappingScenarioDocument doc)
			throws FileNotFoundException, IOException, ValidationException {
		storeModel(new FileOutputStream(fileName), doc);
	}

	public void storeModel(String fileName) throws FileNotFoundException,
			IOException, ValidationException {
		storeModel(fileName, MapScenarioHolder.getInstance().getDocument());
	}

}
