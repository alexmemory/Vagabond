package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.AbstractVagabondTest;

public class TestMarkers extends AbstractVagabondTest {

	@Before
	public void setUp () throws XmlException, IOException, ValidationException {
		setUpSchemaResolver ("resource/exampleScenarios/homeless.xml");
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
		assertEquals(attr2.toString(), "(tramp(0),1,name(0))");
	}
	
	@Test
	public void testTupleMarker () throws Exception {
		ITupleMarker m1 = MarkerFactory.newTupleMarker("tramp", "1");
		ITupleMarker m2 = MarkerFactory.newTupleMarker("tramp", "1");
		
		assertEquals(m1.getSize(), 4);
		assertEquals(m1, m2);
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
		
		set6.add(attr);
		set6.add(attr4);
		set3.add(attr3);
		
		assertEquals(set3,set6);
		
		set4.add(m1);
		set4.add(m3);
		set5.add(m4);
		set5.add(m2);
		
		assertEquals(set4, set5);
	}
	
	private void testTupSize(int relId, int size) {
		assertEquals(SchemaResolver.getInstance().getTupleSize(relId), size);
	}
	
	private void testAttrIdPair (int relId, String relName, int attrId, 
			String attrName) throws Exception {
		assertEquals(SchemaResolver.getInstance().getRelName(relId), relName);
		assertEquals(SchemaResolver.getInstance().getAttrId(relId, attrName), attrId);
		assertEquals(SchemaResolver.getInstance().getAttrName(relId, attrId), attrName);
	}
	
	private void testRelIdPair (int id, String name) throws Exception {
		assertEquals(SchemaResolver.getInstance().getRelName(id), name);
		assertEquals(SchemaResolver.getInstance().getRelId(name), id);
	}
	
	private static void setUpSchemaResolver (String fileName) throws XmlException, IOException, ValidationException {
		MapScenarioHolder map = ModelLoader.getInstance().load(new 
				File(fileName));
		setSchemas(fileName);
	}
}
