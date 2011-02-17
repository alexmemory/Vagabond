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

public class ModelLoader {

	static Logger log = Logger.getLogger(ModelLoader.class.getName());
	
	private static ModelLoader instance = new ModelLoader();
	
	private ModelLoader () {
		
	}
	
	public static ModelLoader getInstance() {
		return instance;
	}
	
	public MapScenarioHolder load (File inFile) throws XmlException, IOException, ValidationException {
		MappingScenarioDocument doc;
		MapScenarioHolder holder;
		
		doc = MappingScenarioDocument.Factory.parse(inFile);
		validate (doc);
		holder = new MapScenarioHolder (doc);
		
		return holder;
	}
	
	public MapScenarioHolder load (InputStream inStream) throws XmlException, IOException, ValidationException {
		MappingScenarioDocument doc;
		MapScenarioHolder holder;
		
		doc = MappingScenarioDocument.Factory.parse(inStream);
		validate (doc);
		holder = new MapScenarioHolder (doc);
		
		return holder;
	}
	
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
