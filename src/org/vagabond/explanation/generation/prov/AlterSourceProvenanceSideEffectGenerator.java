package org.vagabond.explanation.generation.prov;

import java.util.Map;
import java.util.Set;

import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;

public class AlterSourceProvenanceSideEffectGenerator 
		extends SourceProvenanceSideEffectGenerator {

	@Override
	public String getSideEffectQuery (String relName, Set<String> sourceRels, 
			Map<String, IMarkerSet> sourceSE) {
		StringBuffer conditions;
		String query;
		String unnumSource;
		
		conditions = new StringBuffer();
		
		conditions.append("(");
		for(String source: sourceRels) {
			unnumSource = getUnNumRelName(source);
			if (sourceSE.get(unnumSource) != null) {
				for(ISingleMarker sourceErr: sourceSE.get(unnumSource).getElems()) {
					conditions.append(
							getSideEffectEqualityCond(source, 
									 sourceErr)
							+ " OR ");
				}
			}
		}
		conditions.delete(conditions.length() - 4, conditions.length() - 1);
		conditions.append(")");
		
		query = QueryHolder.getQuery("ProvSE.GetSideEffectUsingAgg")
				.parameterize("target." + relName, conditions.toString());
		
		log.debug("Compute side effect query for\nrelname <" + relName + 
				">\nconditions <" + conditions + ">\nwith query:\n" + query);
		
		return query;
	}

	@Override
	protected String getSideEffectEqualityCond (String source, ISingleMarker sourceErr) {
		return "prov_source_" + source + 
				"_tid = " + sourceErr.getTid();
	}
	
}
