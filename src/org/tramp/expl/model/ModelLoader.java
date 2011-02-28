package org.tramp.expl.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.tramp.xmlmodel.MappingScenarioDocument;

/**
 * Singleton class for loading mapping scenarios from XML documents. Internally 
 * XMLBeans is used to parse the XML document and create a Java model of the
 * mapping scenario. 
 * 
 * @author Boris Glavic
 *
 */
public class ModelLoader {

	static Logger log = Logger.getLogger(ModelLoader.class.getName());
	
	private static ModelLoader instance = new ModelLoader();
	
	private ModelLoader () {
		
	}
	
	/**
	 * 
	 * @return The singleton instance of this class.
	 */
	
	public static ModelLoader getInstance() {
		return instance;
	}
	
	/**
	 * Load a mapping scenario from a file.
	 * 
	 * @param inFile The XML file to load from.
	 * @return The mapping scenario.
	 * @throws XmlException
	 * @throws IOException
	 * @throws ValidationException
	 */
	
	public MapScenarioHolder load (File inFile) throws XmlException, IOException, ValidationException {
		MappingScenarioDocument doc;
		MapScenarioHolder holder;
		
		doc = MappingScenarioDocument.Factory.parse(inFile);
		validate (doc);
		holder = new MapScenarioHolder (doc);
		
		return holder;
	}
	
	/**
	 * Load a mapping scenario from an Java <code>InputStream</code>.
	 * 
	 * @param inStream The input stream to read from.
	 * @return The mapping scenario.
	 * @throws XmlException
	 * @throws IOException
	 * @throws ValidationException
	 */
	
	public MapScenarioHolder load (InputStream inStream) throws XmlException, IOException, ValidationException {
		MappingScenarioDocument doc;
		MapScenarioHolder holder;
		
		doc = MappingScenarioDocument.Factory.parse(inStream);
		validate (doc);
		holder = new MapScenarioHolder (doc);
		
		return holder;
	}
	
	/**
	 * Validate a mapping scenario XML document.
	 * 
	 * @param doc The XMLBeans object for the document.
	 * @throws ValidationException If the document does not conform to the 
	 * 		XML schema for mapping scenarios. 
	 */
	
	public void validate (MappingScenarioDocument doc) throws ValidationException {
		List<?> errors = new ArrayList();
		XmlOptions options = new XmlOptions ();
		boolean result;
		
		log.debug("validate mapping scenario");
		
		options.setErrorListener(errors);
		
		result = doc.validate(options);
		for(Object error: errors) {
			log.error("Validation Error: " + error);
		}
		
		if (!result)
			throw new ValidationException (errors);
	}
}
