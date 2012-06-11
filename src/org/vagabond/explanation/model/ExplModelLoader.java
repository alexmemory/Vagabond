package org.vagabond.explanation.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

public class ExplModelLoader {

	static Logger log = Logger.getLogger(ExplModelLoader.class);

	private static ExplModelLoader inst = new ExplModelLoader();

	private ExplModelLoader() {

	}

	public static ExplModelLoader getInstance() {
		return inst;
	}


	public static ExplanationAndErrorsDocument loadExpls(String fileName) throws XmlException, IOException, ValidationException {
		return inst.getDoc(new FileInputStream(fileName));
	}

	
	public static ExplanationAndErrorsDocument loadExpls(InputStream in) throws XmlException, IOException, ValidationException {
		return inst.getDoc(in);
	}
	
	public static void storeModel (String fileName, ExplanationAndErrorsDocument doc) throws ValidationException, IOException {
		storeModel(new FileOutputStream(fileName), doc);
	}
	
	public static void storeModel (OutputStream out, ExplanationAndErrorsDocument doc) throws ValidationException, IOException {
		inst.validate(doc);
		doc.save(out);
	}

	private ExplanationAndErrorsDocument getDoc(InputStream in) throws XmlException,
			IOException, ValidationException {
		ExplanationAndErrorsDocument doc;

		doc = ExplanationAndErrorsDocument.Factory.parse(in);
		validate(doc);

		return doc;
	}

	public void validate(ExplanationAndErrorsDocument doc)
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
	}

}
