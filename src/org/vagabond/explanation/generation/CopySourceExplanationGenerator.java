package org.vagabond.explanation.generation;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.generation.prov.SideEffectGenerator;
import org.vagabond.explanation.generation.prov.SourceProvParser;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.MappingType;

public class CopySourceExplanationGenerator  
		implements ISingleExplanationGenerator {

	static Logger log = Logger.getLogger(CopySourceExplanationGenerator.class);
	
	private IAttributeValueMarker error;
	private ProvWLRepresentation prov;
	protected CopySourceError expl;
	
	@Override
	public IExplanationSet findExplanations(ISingleMarker errorMarker) throws Exception {
		this.error = (IAttributeValueMarker) errorMarker;
		
		return getExplanationSets();
	}
	
	private IExplanationSet getExplanationSets() throws Exception {
		IExplanationSet result = ExplanationFactory.newExplanationSet();
		IMarkerSet sourceSE;
		IMarkerSet targetSE;
		
		prov = ProvenanceGenerator.getInstance().getCopyProvenance(error);
//		sourceSE = getRealCopyFromMappings(prov.getTuplesInProv()); 
		sourceSE = prov.getTuplesInProv();
		targetSE = SideEffectGenerator.getInstance()
				.computeTargetSideEffects(sourceSE, error);
		
		expl = new CopySourceError(error);
		expl.setSourceSE(sourceSE);
		expl.setTargetSE(targetSE);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		result.addExplanation(expl);
		
		log.debug("Generated Explanation:\n" + expl.toString());
		
		return result;
	}
	
	private IMarkerSet getRealCopyFromMappings() {
		IMarkerSet result;
		ITupleMarker witness;
		Vector<Set<Integer>> attrForBaseRel;
		
		result = MarkerFactory.newMarkerSet();
		attrForBaseRel = getCopyCSAttrsForBaseRels();
		
		/* for each tid in each witness list: Determine from the
		 * mappings from which attributes values are copied to the error
		 * and add IAttributeValueMarkers for each of them.
		 */
		for(Vector<ITupleMarker> wl: prov.getWitnessLists()) {
			for(int i = 0; i < wl.size(); i++) {
				witness = wl.get(i);
				if (witness != null) {
					for (int attr: attrForBaseRel.get(i)) {
						result.add(MarkerFactory.newAttrMarker(witness, attr));
					}
				}
			}
		}
		
		return result;
	}
	
	private Vector<Set<Integer>> getCopyCSAttrsForBaseRels () {
		Map<MappingType,Set<Integer>> maps;
		Vector<Set<Integer>> result;
		result = new Vector<Set<Integer>>();
		
		
		
		return result;
	}
	

	
	
}
