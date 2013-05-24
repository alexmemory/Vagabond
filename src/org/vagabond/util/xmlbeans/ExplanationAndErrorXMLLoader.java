package org.vagabond.util.xmlbeans;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.AbstractBasicExplanation;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.xmlmodel.explanderror.AttributeMarkerType;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;
import org.vagabond.xmlmodel.explanderror.ExplanationType;
import org.vagabond.xmlmodel.explanderror.MapScenObjectSetType;
import org.vagabond.xmlmodel.explanderror.MarkerSetType;
import org.vagabond.xmlmodel.explanderror.TupleMarkerType;
import org.vagabond.xmlmodel.explanderror.TypeOfExplanationType;
import org.vagabond.xmlmodel.explanderror.TypeOfExplanationType.Enum;

public class ExplanationAndErrorXMLLoader {

	static Logger log = Logger.getLogger(ExplanationAndErrorXMLLoader.class);

	private static ExplanationAndErrorXMLLoader inst =
			new ExplanationAndErrorXMLLoader();

	private ExplanationAndErrorXMLLoader() {

	}

	public static ExplanationAndErrorXMLLoader getInstance() {
		return inst;
	}

	public IExplanationSet loadExplanations(File file) throws Exception {
		return loadExplanations(new FileInputStream(file));
	}

	public IExplanationSet loadExplanations(String fileName) throws Exception {
		return loadExplanations(new FileInputStream(fileName));
	}

	public IExplanationSet loadExplanations(InputStream in) throws Exception {
		ExplanationAndErrorsDocument doc = getDoc(in);
		return translate(doc);
	}

	public ExplanationAndErrorsDocument translateToXML(IExplanationSet e) {
		ExplanationAndErrorsDocument result;

		result = ExplanationAndErrorsDocument.Factory.newInstance();

		return result;
	}

	private IExplanationSet translate(ExplanationAndErrorsDocument doc)
			throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		ExplanationType[] expls;

		expls =
				doc.getExplanationAndErrors().getExplanations()
						.getExplanationArray();
		for (ExplanationType e : expls) {
			result.add(translateExpl(e));
		}

		return result;
	}

	private IBasicExplanation translateExpl(ExplanationType e) throws Exception {
		TypeOfExplanationType.Enum type = e.getType();
		AbstractBasicExplanation result;
		IBasicExplanation.ExplanationType javaType = getBasicExplType(type);
		
		if (log.isDebugEnabled()) {log.debug("Generate explanation of type <" + javaType.toString() + ">");};
		
		result = (AbstractBasicExplanation) ExplanationFactory
						.newBasicExpl(javaType);
		result.setExplains(translate(e.getExplains()));
		
		if (e.isSetCorrespondenceSE())
			result.setCorrSE(MapScenarioHolder.getInstance().getCorrespondences(
					getStringArray(e.getCorrespondenceSE())));
		
		if (e.isSetMappingSE())
			result.setMapSE(MapScenarioHolder.getInstance().getMappings(
					getStringArray(e.getMappingSE())));
		if (e.isSetSourceInstSE())
			result.setSourceSE(extractMarkerSet(e.getSourceInstSE()));

		IMarkerSet targetSe = extractMarkerSet(e.getCoverage());
		targetSe.remove(result.explains());
		result.setTargetSE(targetSe);

		if( e.isSetTransformationSE())
			result.setTransSE(MapScenarioHolder.getInstance().getTransformations(
					getStringArray(e.getTransformationSE())));

		if (log.isDebugEnabled()) {log.debug("Explanation is: " + result.toString());};
		
		return result;
	}

	private IMarkerSet extractMarkerSet(MarkerSetType set) throws Exception {
		IMarkerSet result = MarkerFactory.newMarkerSet();

		if (set == null)
			return MarkerFactory.newMarkerSet();

		for (AttributeMarkerType a : set.getAttrMarkerArray())
			result.add(translate(a));

		for (TupleMarkerType t : set.getTupleMarkerArray())
			result.add(translate(t));

		return result;
	}

	private ISingleMarker translate(TupleMarkerType t) throws Exception {
		// using int identifiers
		if (t.isSetRelId())
			return MarkerFactory.newTupleMarker(t.getRelId(), t.getTID());
		else
			return MarkerFactory
					.newTupleMarker(t.getRelation(), t.getTupleID());
	}

	private ISingleMarker translate(AttributeMarkerType a) throws Exception {
		if (a.isSetRelId())
			return MarkerFactory.newAttrMarker(a.getRelId(), a.getTID(),
					a.getAttrId());
		else
			return MarkerFactory.newAttrMarker(a.getRelation().trim(), a.getTupleID().trim(),
					a.getAttribute().trim());
	}

	private String[] getStringArray(MapScenObjectSetType set) {
		List<String> list = set.getListValue();
		String[] result = new String[list.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}

		return result;
	}

	private
			org.vagabond.explanation.model.basic.IBasicExplanation.ExplanationType
			getBasicExplType(Enum type) throws Exception {
		switch (type.intValue()) {
		case TypeOfExplanationType.INT_CORRESPONDENCE:
			return IBasicExplanation.ExplanationType.CorrespondenceError;
		case TypeOfExplanationType.INT_SOURCE_COPY:
			return IBasicExplanation.ExplanationType.CopySourceError;
		case TypeOfExplanationType.INT_SOURCE_JOIN_VALUE:
			return IBasicExplanation.ExplanationType.InfluenceSourceError;
		case TypeOfExplanationType.INT_SOURCE_SKELETON:
			return IBasicExplanation.ExplanationType.SourceSkeletonMappingError;
		case TypeOfExplanationType.INT_SUPERFLUOUS_MAPPING:
			return IBasicExplanation.ExplanationType.SuperflousMappingError;
		case TypeOfExplanationType.INT_TARGET_SKELETON:
			return IBasicExplanation.ExplanationType.TargetSkeletonMappingError;
		}
		throw new Exception("Cannot translate explanation type "
				+ type.toString());
	}

	private ExplanationAndErrorsDocument getDoc(InputStream in)
			throws XmlException, IOException, ValidationException {
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

		if (log.isDebugEnabled()) {log.debug("validate explanation XML");};

		options.setErrorListener(errors);

		result = doc.validate(options);
		for (Object error : errors) {
			log.error("Validation Error: " + error);
		}

		if (!result)
			throw new ValidationException(errors);
	}

}
