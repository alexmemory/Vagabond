package org.vagabond.test.explanations.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.vagabond.explanation.generation.partition.ErrorPartitionGraph;
import org.vagabond.explanation.generation.partition.ErrorPartitioner;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.PartitionedMarkerSet;
import org.vagabond.test.AbstractVagabondTest;

public class TestErrorPartitioning extends AbstractVagabondTest {

	private ErrorPartitionGraph g;
	private ErrorPartitioner partitioner;
	
	private void setUp (String filename) throws Exception {
		loadToDB(filename);
		g = new ErrorPartitionGraph();
		partitioner = new ErrorPartitioner();
	}
	
	
	
	@Test
	public void testPartitionMarkers () throws Exception {
		setUp("resource/test/severalComps.xml");
		
		IAttributeValueMarker attr = MarkerFactory.newAttrMarker(3,"1",0);
		IAttributeValueMarker attr2 = MarkerFactory.newAttrMarker(3, "2", 0);
		
		IAttributeValueMarker attr3 = MarkerFactory.newAttrMarker(4, "4", 0);
		
		IMarkerSet set = MarkerFactory.newMarkerSet(attr,attr2,attr3); 
				
		MarkerSummary sum1 = MarkerFactory.newMarkerSummary(
				MarkerFactory.newSchemaMarker(3,0)
				);
		
		MarkerSummary sum2 = MarkerFactory.newMarkerSummary(
				MarkerFactory.newSchemaMarker(4,0)
				);
		
		IMarkerSet sub1 = set.subset(sum1);
		IMarkerSet sub2 = set.subset(sum2);
		
		PartitionedMarkerSet ex = new PartitionedMarkerSet();
		ex.addPartition(sub1, sum1);
		ex.addPartition(sub2, sum2);
		
		PartitionedMarkerSet m = partitioner.partitionMarkers(g, set);
		
		assertEquals(ex, m);
	}
	
}
