package org.vagabond.explanation.generation.partition;

import java.util.List;

import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.PartitionedMarkerSet;

public class ErrorPartitioner {

	public ErrorPartitioner () {
		
	}
	
	public PartitionedMarkerSet partitionMarkers (ErrorPartitionGraph mapGraph, IMarkerSet markers) throws Exception {
		PartitionedMarkerSet result = new PartitionedMarkerSet();
		
		// partition attributes
		List<MarkerSummary> attrParts = mapGraph.paritionAttrs(markers.getSummary());
		
		// partition errors
		for(MarkerSummary m: attrParts)
			result.addPartition(markers.subset(m), m);
		
		return result;
	}
	
	
}
