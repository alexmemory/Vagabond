package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;

import org.junit.Test;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.generation.PartitionExplanationGenerator;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph;
import org.vagabond.explanation.generation.partition.ErrorPartitioner;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.PartitionedMarkerSet;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.test.AbstractVagabondTest;

public class TestPartExplGen extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestPartExplGen.class);
	
	private PartitionExplanationGenerator explGen = new PartitionExplanationGenerator();
	private ExplanationSetGenerator explSetGen = new ExplanationSetGenerator();
	private ErrorPartitioner parter = new ErrorPartitioner();
	private ErrorPartitionGraph g;
	
	private void setUp (String filename) throws Exception {
		loadToDB(filename);
		g = new ErrorPartitionGraph();
		explGen.init();
	}
	
	@Test
	public void testSimplExplGen () throws Exception {
		setUp("resource/test/severalComps.xml");
	
		IAttributeValueMarker a1 = MarkerFactory.newAttrMarker("u", "2", "u1"); 
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker("v", "1", "v1");
		
		IMarkerSet m = MarkerFactory.newMarkerSet(a1, a2);
		
		PartitionedMarkerSet mPart = parter.partitionMarkers(g, m); 
		
		ExplPartition ex = new ExplPartition(mPart);
		
		ExplanationCollection col1 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a1));
		ex.add(col1);
		ExplanationCollection col2 = explSetGen.findExplanations(MarkerFactory.newMarkerSet(a2));
		ex.add(col2);		
		
		
		ExplPartition e = explGen.findExplanations(m);
		
		log.debug(e);
		
		assertEquals(ex, e);
	}
	
}
