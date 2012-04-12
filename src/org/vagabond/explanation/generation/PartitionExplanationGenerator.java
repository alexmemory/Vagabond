package org.vagabond.explanation.generation;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph;
import org.vagabond.explanation.generation.partition.ErrorPartitioner;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.PartitionedMarkerSet;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.Pair;

public class PartitionExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(PartitionExplanationGenerator.class);
	
	private ExplanationSetGenerator setGen;
	private ErrorPartitioner partioner;
	private ErrorPartitionGraph g;
	
	public PartitionExplanationGenerator () {
		partioner = new ErrorPartitioner();
		setGen = new ExplanationSetGenerator();
	}
	
	public void init () throws Exception {
		g = new ErrorPartitionGraph ();
	}
	
	public ExplPartition findExplanations (IMarkerSet errors) throws Exception {
		PartitionedMarkerSet part = partioner.partitionMarkers(g, errors); 
		ExplPartition result = new ExplPartition(part);
		
		for(Iterator<Pair<IMarkerSet,MarkerSummary>> i = part.pairIterator(); i.hasNext();) {
			Pair<IMarkerSet,MarkerSummary> p = i.next();
			ExplanationCollection col;
			
			col = setGen.findExplanations(p.getKey());
			result.add(col);
		}
		
		return result;
	}
}
