package org.vagabond.explanation.marker;

import java.util.HashMap;
import java.util.Map;

public class MarkerSetUtil {

	public static Map<String, IMarkerSet> partitionOnRelation (IMarkerSet set) {
		Map<String, IMarkerSet> result;
		
		result = new HashMap<String, IMarkerSet> ();
		for(ISingleMarker marker : set) {
			String rel = marker.getRel();
			
			if (!result.containsKey(rel)) {
				result.put(rel, MarkerFactory.newMarkerSet());
			}
			result.get(rel).add(marker);
		}
		
		return result;
 	}
	
}
