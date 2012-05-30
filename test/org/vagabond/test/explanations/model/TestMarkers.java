package org.vagabond.test.explanations.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.AttrMarker;
import org.vagabond.explanation.marker.AttrValueMarker;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISchemaMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerComparators;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.PartitionedMarkerSet;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.Pair;

public class TestMarkers extends AbstractVagabondTest {

	@Before
	public void setUp () throws XmlException, IOException, ValidationException {
		setSchemas("resource/exampleScenarios/homeless.xml");
	}
	
	@Test
	public void testSchemaResolver () throws Exception {
		testRelIdPair(0, "tramp");
		testRelIdPair(1, "socialworker");
		testRelIdPair(2, "soupkitchen");
		testRelIdPair(3, "person");
		
		testAttrIdPair(0,"tramp",0,"name");
		testAttrIdPair(0,"tramp",1,"nickname");
		testAttrIdPair(3,"person",1,"livesin");
		
		testTupSize(0,4);
		testTupSize(3,2);
	}
	
	@Test
	public void testAttrMarkerWithSchemaResolver () throws Exception {
		IAttributeValueMarker attr = MarkerFactory.
				newAttrMarker("tramp","1","name");
		IAttributeValueMarker attr2 = MarkerFactory.newAttrMarker(0, "1", 0);
		
		assertEquals(attr.getSize(), 1);
		assertEquals(attr, attr2);
		assertEquals(attr2.toString(), "('tramp'(0),1,'name'(0))");
		assertEquals(attr.hashCode(), attr2.hashCode());
	}
	
	@Test
	public void testTupleMarker () throws Exception {
		ITupleMarker m1 = MarkerFactory.newTupleMarker("tramp", "1");
		ITupleMarker m2 = MarkerFactory.newTupleMarker("tramp", "1");
		
		assertEquals(m1.getSize(), 4);
		assertEquals(m1, m2);
		assertEquals(m1.hashCode(), m2.hashCode());
	}
	
	@Test
	public void testMarkerSet () throws Exception {
		IMarkerSet set = MarkerFactory.newMarkerSet();
		IMarkerSet set2 = MarkerFactory.newMarkerSet();
		IMarkerSet set3 = MarkerFactory.newMarkerSet();
		IMarkerSet set4 = MarkerFactory.newMarkerSet();
		IMarkerSet set5 = MarkerFactory.newMarkerSet();
		IMarkerSet set6 = MarkerFactory.newMarkerSet();
		
		IAttributeValueMarker attr = MarkerFactory.
				newAttrMarker("tramp","1","name");
		IAttributeValueMarker attr2 = MarkerFactory.newAttrMarker(0, "1", 0);
		IAttributeValueMarker attr3 = MarkerFactory.newAttrMarker(1, "2", 0);
		IAttributeValueMarker attr4 = MarkerFactory.newAttrMarker(1, "2", 0);
		
		ITupleMarker m1 = MarkerFactory.newTupleMarker("tramp", "1");
		ITupleMarker m2 = MarkerFactory.newTupleMarker("tramp", "1");
		ITupleMarker m3 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		ITupleMarker m4 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		
		set.add(attr);
		set.add(attr2);
		assertEquals(set.getNumElem(),1);
		assertEquals(set.getSize(),1);

		assertFalse(set.equals(set2));
		
		set2.add(attr2);
		set2.add(attr);
		
		assertTrue(set.equals(set2));
		
		set2 = MarkerFactory.newMarkerSet();
		set2.add(attr);
		set3 = MarkerFactory.newMarkerSet();
		set3.add(attr2);
		set3.union(set2);
		
		assertEquals(set,set3);
		assertEquals(set.hashCode(), set3.hashCode());
		
		set6.add(attr);
		set6.add(attr4);
		set3.add(attr3);
		
		assertEquals(set3,set6);
		assertEquals(set3.hashCode(), set6.hashCode());
		
		set4.add(m1);
		set4.add(m3);
		set5.add(m4);
		set5.add(m2);
		
		assertEquals(set4, set5);
		assertEquals(set4.hashCode(), set5.hashCode());
		
		assertEquals(MarkerParser.getInstance().parseSet("{}"), 
				MarkerParser.getInstance().parseSet("{}"));
		
		assertFalse(MarkerParser.getInstance().parseSet("{A(tramp,1,name)}").equals( 
				MarkerParser.getInstance().parseSet("{}")));
		
		assertFalse(MarkerParser.getInstance().parseSet("{}").equals( 
				MarkerParser.getInstance().parseSet("{A(tramp,1,name)}")));
	}
	
	@Test
	public void testMarkerSetGetNumElem () throws Exception {
		IMarkerSet s = MarkerFactory.newMarkerSet();
		Random rand = new Random(0);
		int count = 0;
		List<ISingleMarker> l = new ArrayList<ISingleMarker> ();
		
		for(int i = 1; i < 1000; i++) {
			AttrValueMarker m = (AttrValueMarker) MarkerFactory.newAttrMarker(
					rand.nextInt(3), "" + rand.nextInt(10000), rand.nextInt(3));
			if (!l.contains(m)) {
				count++;
				l.add(m);
			}
			s.add(m);
			assertEquals(count, s.size());
			assertEquals(count, s.getNumElem());
		}
	}
	
	@Test
	public void testSchemaMarker () throws Exception {
		AttrMarker a1 = (AttrMarker) MarkerFactory.newSchemaMarker(0, 0);
		AttrMarker a2 = (AttrMarker) MarkerFactory.newSchemaMarker(MarkerFactory.newAttrMarker(0, "1", 0));
		Vector<ISchemaMarker> as = (Vector<ISchemaMarker>) MarkerFactory.newSchemaMarker(MarkerFactory.newTupleMarker(0, "1"));
		Vector<ISchemaMarker> as2 = CollectionUtils.makeVec(a1, 
				MarkerFactory.newSchemaMarker(0,1),
				MarkerFactory.newSchemaMarker(0,2),
				MarkerFactory.newSchemaMarker(0,3)
				); 
		
		assertEquals(a1,a2);
		assertTrue(a1 == a2);
		
		assertTrue(as.contains(a1));
		assertEquals(as2, as);
	}
	
	@Test
	public void testMarkerSummary () throws Exception {
		IAttributeValueMarker attr = MarkerFactory.newAttrMarker(0,"1",0);
		IAttributeValueMarker attr2 = MarkerFactory.newAttrMarker(0, "1", 1);
		IAttributeValueMarker attr3 = MarkerFactory.newAttrMarker(1, "2", 0);
		IAttributeValueMarker attr4 = MarkerFactory.newAttrMarker(1, "3", 0);
		IMarkerSet set = MarkerFactory.newMarkerSet(attr,attr2,attr3,attr4);
		
		MarkerSummary sum = MarkerFactory.newMarkerSummary(set);
		MarkerSummary exSum = MarkerFactory.newMarkerSummary(
				MarkerFactory.newSchemaMarker(0,0),
				MarkerFactory.newSchemaMarker(0,1),
				MarkerFactory.newSchemaMarker(1,0)
				);
		
		assertEquals (sum, exSum);
		assertEquals (sum, set.getSummary());
	}
	
	@Test
	public void testMarkerSetSubset () throws Exception {
		IAttributeValueMarker attr = MarkerFactory.newAttrMarker(0,"1",0);
		IAttributeValueMarker attr2 = MarkerFactory.newAttrMarker(0, "1", 1);
		IAttributeValueMarker attr3 = MarkerFactory.newAttrMarker(1, "2", 0);
		IAttributeValueMarker attr4 = MarkerFactory.newAttrMarker(1, "3", 0);
		IMarkerSet set = MarkerFactory.newMarkerSet(attr,attr2,attr3,attr4); 
				
		MarkerSummary sum = MarkerFactory.newMarkerSummary(
				MarkerFactory.newSchemaMarker(0,0),
				MarkerFactory.newSchemaMarker(1,0)
				);
		
		IMarkerSet subset = set.subset(sum);
		IMarkerSet exSubset = MarkerFactory.newMarkerSet(attr,attr3,attr4);
		
		assertEquals(subset, exSubset);
	}

	@Test
	public void testPartitionedMarkerSet () throws Exception {
		IAttributeValueMarker attr = MarkerFactory.newAttrMarker(0,"1",0);
		IAttributeValueMarker attr2 = MarkerFactory.newAttrMarker(0, "1", 1);
		IAttributeValueMarker attr3 = MarkerFactory.newAttrMarker(1, "2", 0);
		IAttributeValueMarker attr4 = MarkerFactory.newAttrMarker(1, "3", 0);
		IMarkerSet set = MarkerFactory.newMarkerSet(attr,attr2,attr3,attr4); 
				
		MarkerSummary sum1 = MarkerFactory.newMarkerSummary(
				MarkerFactory.newSchemaMarker(0,0),
				MarkerFactory.newSchemaMarker(1,0)
				);
		
		MarkerSummary sum2 = MarkerFactory.newMarkerSummary(
				MarkerFactory.newSchemaMarker(0,1)
				);
		
		IMarkerSet sub1 = set.subset(sum1);
		IMarkerSet sub2 = set.subset(sum2);
		
		PartitionedMarkerSet m = new PartitionedMarkerSet();
		
		m.addPartition(sub1, sum1);
		m.addPartition(sub2, sum2);
		
		assertEquals(2, m.getNumParts());
		
		assertEquals(sum1, m.getAttrPartition(0));
		assertEquals(sum2, m.getAttrPartition(1));
		
		assertEquals(sub1, m.getPartition(0));
		assertEquals(sub2, m.getPartition(1));
		
		assertEquals(sum1, m.getAttrPartition(MarkerFactory.newSchemaMarker(0, 0)));
		assertEquals(sum1, m.getAttrPartition(MarkerFactory.newSchemaMarker(1, 0)));
		assertEquals(sum2, m.getAttrPartition(MarkerFactory.newSchemaMarker(0, 1)));

		assertEquals(sub1, m.getMarkerPartition(MarkerFactory.newSchemaMarker(0, 0)));
		assertEquals(sub1, m.getMarkerPartition(MarkerFactory.newSchemaMarker(1, 0)));
		assertEquals(sub2, m.getMarkerPartition(MarkerFactory.newSchemaMarker(0, 1)));
		
		assertEquals(new Pair<IMarkerSet, MarkerSummary> (sub1, sum1), m.getPartAndSum(0));
		assertEquals(new Pair<IMarkerSet, MarkerSummary> (sub2, sum2), m.getPartAndSum(1));
	}
	
	@Test
	public void testComparator () throws Exception {
		Comparator<ISingleMarker> c = MarkerComparators.singleMarkerComp;
		Comparator<IMarkerSet> mc = MarkerComparators.markerSetComp;
		
		IAttributeValueMarker a1 = MarkerFactory.
				newAttrMarker("tramp","1","name");
		IAttributeValueMarker a2 = MarkerFactory.newAttrMarker(0, "1", 0);
		IAttributeValueMarker a3 = MarkerFactory.newAttrMarker(1, "2", 0);
		IAttributeValueMarker a4 = MarkerFactory.newAttrMarker(1, "2", 0);
		
		ITupleMarker m1 = MarkerFactory.newTupleMarker("tramp", "1");
		ITupleMarker m2 = MarkerFactory.newTupleMarker("tramp", "1");
		ITupleMarker m3 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		ITupleMarker m4 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		
		IMarkerSet set1 = MarkerFactory.newMarkerSet(a1);
		IMarkerSet set2 = MarkerFactory.newMarkerSet(a1,a2);
		IMarkerSet set3 = MarkerFactory.newMarkerSet(a1,a2,a3);
		IMarkerSet set4 = MarkerFactory.newMarkerSet(a1,a3);
		IMarkerSet set5 = MarkerFactory.newMarkerSet(a2,a4);
		IMarkerSet set6 = MarkerFactory.newMarkerSet(a3,a3,a4);
		
		// attr markers
		assertEquals(0, c.compare(a1, a1));
		assertEquals(0, c.compare(a2, a2));
		assertEquals(0, c.compare(a3, a3));
		assertEquals(0, c.compare(a4, a4));
		
		assertEquals(0, c.compare(a1, a2));
		assertEquals(0, c.compare(a1, a2));
		assertEquals(0, c.compare(a3, a4));
		assertEquals(0, c.compare(a4, a3));
		
		assertEquals(-1, c.compare(a1, a3));
		assertEquals(1, c.compare(a3, a1));
		
		// tuple markers
		assertEquals(0, c.compare(m1, m1));
		assertEquals(0, c.compare(m2, m2));
		assertEquals(0, c.compare(m3, m3));
		assertEquals(0, c.compare(m4, m4));
		
		assertEquals(0, c.compare(m1, m2));
		assertEquals(0, c.compare(m1, m2));
		assertEquals(0, c.compare(m3, m4));
		assertEquals(0, c.compare(m4, m3));
		
		assertEquals(-2, c.compare(m1, m3));
		assertEquals(2, c.compare(m3, m1));
		
		// sets
		assertEquals(0, mc.compare(set1, set1));
		assertEquals(0, mc.compare(set2, set2));
		assertEquals(0, mc.compare(set3, set3));
		assertEquals(0, mc.compare(set4, set4));
		assertEquals(0, mc.compare(set5, set5));
		assertEquals(0, mc.compare(set6, set6));
		
		assertEquals(0, mc.compare(set1, set2));
		assertEquals(0, mc.compare(set2, set1));
		
		assertEquals(0, mc.compare(set3, set4));
		assertEquals(0, mc.compare(set4, set5));
		assertEquals(0, mc.compare(set5, set3));
		
		assertEquals(-1, mc.compare(set1, set3));
		assertEquals(1, mc.compare(set3, set1));
		
		assertEquals(-1, mc.compare(set1, set6));
		assertEquals(1, mc.compare(set6, set1));
		
		assertEquals(1, mc.compare(set3, set6));
		assertEquals(-1, mc.compare(set6, set3));
	}
	
	private void testTupSize(int relId, int size) {
		assertEquals(ScenarioDictionary.getInstance().getTupleSize(relId), size);
	}
	
	private void testAttrIdPair (int relId, String relName, int attrId, 
			String attrName) throws Exception {
		assertEquals(ScenarioDictionary.getInstance().getRelName(relId), relName);
		assertEquals(ScenarioDictionary.getInstance().getAttrId(relId, attrName), attrId);
		assertEquals(ScenarioDictionary.getInstance().getAttrName(relId, attrId), attrName);
	}
	
	private void testRelIdPair (int id, String name) throws Exception {
		assertEquals(ScenarioDictionary.getInstance().getRelName(id), name);
		assertEquals(ScenarioDictionary.getInstance().getRelId(name), id);
	}
	
}
