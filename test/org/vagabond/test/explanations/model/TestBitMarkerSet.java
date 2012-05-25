package org.vagabond.test.explanations.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.AbstractVagabondDBTest;
import org.vagabond.test.AbstractVagabondTest;

public class TestBitMarkerSet extends AbstractVagabondDBTest {

	public TestBitMarkerSet(String name) {
		super(name);
	}
	
	private IMarkerSet setNoElement;
	private IMarkerSet setFirstElement;
	private IMarkerSet setSecondElement;
	private IMarkerSet setTwoElement;
	private IMarkerSet set1;
	private IMarkerSet set2;
	

	
	private IAttributeValueMarker attr;
	private IAttributeValueMarker attr2;
	private IAttributeValueMarker attr3;
	private IAttributeValueMarker attr4;
	
	private ITupleMarker m1;
	private ITupleMarker m2;
	private ITupleMarker m3;
	private ITupleMarker m4;

	public void initialize () throws Exception {
		loadToDB("resource/exampleScenarios/homeless.xml");
		set1 = MarkerFactory.newBitMarkerSet();
		set2 = MarkerFactory.newBitMarkerSet();
		setNoElement = MarkerFactory.newBitMarkerSet();
		setFirstElement = MarkerFactory.newBitMarkerSet();
		setSecondElement = MarkerFactory.newBitMarkerSet();
		setTwoElement = MarkerFactory.newBitMarkerSet();
		
		attr = MarkerFactory.newAttrMarker("tramp","1","name");
		attr2 = MarkerFactory.newAttrMarker(0, "1", 0);
		attr3 = MarkerFactory.newAttrMarker(1, "2", 0);
		attr4 = MarkerFactory.newAttrMarker(1, "2", 0);
		
		m1 = MarkerFactory.newTupleMarker("tramp", "1");
		m2 = MarkerFactory.newTupleMarker("tramp", "1");
		m3 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		m4 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		
		setFirstElement.add(attr);
		setSecondElement.add(attr3);
		setTwoElement.add(attr);
		setTwoElement.add(attr3);
	}
	

	//add, equals, remove
	@Test
	public void testSetOne() throws Exception{
		initialize();
		set1.add(attr);
		assertEquals(setFirstElement, set1);
		assertFalse(set1.getNumElem() == setTwoElement.getNumElem());
		assertEquals(set1.getNumElem(), setFirstElement.getNumElem());
		set1.remove(attr);
		assertFalse(setFirstElement.equals(set1));
		assertTrue(setNoElement.equals(set1));
		

		
		
		
		
		
	}
	@Test
	public void testTrial() throws Exception{
		initialize();
	}
	
//	
//	@Test
//	public void testMarkerSet () throws Exception {
//
//		
//		
//		
//		//getSize(), getNumElem(), add()
//		set.add(attr);
//		set.add(attr2);
//		assertEquals(set.getNumElem(),1);
//		assertEquals(set.getSize(),1);
//
//		// Equals()
//		assertFalse(set.equals(set2));
//		
//		set2.add(attr2);
//		set2.add(attr);
//		
//		assertTrue(set.equals(set2));
//		
//		
//		//Union(), HashCode()
//		set2 = MarkerFactory.newBitMarkerSet();
//		set2.add(attr);
//		set3 = MarkerFactory.newBitMarkerSet();
//		set3.add(attr2);
//		set3.union(set2);
//		
//		assertEquals(set,set3);
//		assertEquals(set.hashCode(), set3.hashCode());
//		
//		//intersect(includes contains)
//		set3.add(attr4);
//		set3.intersect(set2);
//		
//		assertEquals(set,set3);
//		assertEquals(set.hashCode(), set3.hashCode());
//		
//		// clone //TODO 
////		IMarkerSet set7 = set3.cloneSet();
//		
////		Test1: two empty set equals -> Equal is correct
////		Test2: add the remove still doesn't get rid of the thing
////		           So REMOVE is WRONG because of Contains
////		Test3: Adding attr1, and 3 still become 1 element
////					So add is wrong assuming get size is right
//		
////		Only Explaination for bad adding is the mapping always give same position
////		int bitPos = attrMarkerToBitPos ((IAttributeValueMarker) marker);
////		
////		Since the relation increase by one, the offset changes and Tid changes, so both
////		functions have problem
////		
////		BUT how's my offset mapping wrong?? or TID
//		
//		
//		
//		
//		IMarkerSet set7 = MarkerFactory.newBitMarkerSet(); //TODO
//		set7.add(attr);
//		set7.add(attr3);
//		IMarkerSet set100 = MarkerFactory.newBitMarkerSet();
//		
//
//		//assertEquals(set7.getNumElem()+1, set100.getNumElem());
//
//		set100.add(attr);
//		assertTrue(set100.remove(attr));
//	//	assertEquals(set7.getNumElem()+1, set100.getNumElem());
//		assertEquals(set100, set7);
//		
//		
//		set3.add(attr);
//		set3.add(attr4);
//		set3.add(m4);
////		assertEquals(set7.getNumElem(), set3.getNumElem() );//TODO
//		set7.add(attr4);
//		assertEquals(set3, set7);
//		set7.add(attr);
//		set3.remove(attr);
//		set3.remove(attr4);
//
////		assertFalse(set7.equals(set3));
//		
//		//getSummary
//		IMarkerSet set8 = set7.cloneSet();
//		assertTrue(set8.getSummary().equals(set7.getSummary()));
//		
//		//subset
//		IMarkerSet set9 = MarkerFactory.newBitMarkerSet();
//		set9.add(attr);
//		IMarkerSet set10 = MarkerFactory.newBitMarkerSet();
//		set10.add(attr);
//		assertTrue(set8.subset(set9.getSummary()).equals(set8.subset(set10.getSummary())));
//		
//		//addAll,Add
//		Set<ISingleMarker> list = new HashSet<ISingleMarker>();
//		list.add(attr);
//		list.add(attr4);
//		IMarkerSet set11 = MarkerFactory.newBitMarkerSet();
//		set11.addAll(list);
//		assertTrue(set11.equals(set8));
//		
//		//clear
//		set8.clear();
//		IMarkerSet set12 = MarkerFactory.newBitMarkerSet();
//		assertTrue(set12.equals(set8));
//		
//		//containAll //TODO
////		assertTrue(set11.containsAll(list));
//		
//		//isempty
//		IMarkerSet set13 = MarkerFactory.newBitMarkerSet();
//		assertTrue(set13.isEmpty());
//		
//		//remove
//		set13.add(attr);
//		set11.remove(attr3);
//		assertTrue(set13.equals(set11));
//		
//		//removeAll
//		set11.add(attr3);
//		set11.removeAll(list);
//		set13.remove(attr);
//		assertTrue(set11.equals(set13));
//		
//		//retainALL
//		set11.addAll(list);
//		set11.retainAll(list);
//		set13.addAll(list);
//		assertTrue(set11.equals(set13));
//		
//		
//		//iterator //TODO
////		Iterator<ISingleMarker> iterator = list.iterator();
////		assertTrue(set11.iterator().equals(iterator));
//	
//		//TODO
////		10.toString()
////		11.toUserString()
////		20.iterator()
////		25.size()
////		26.toArray()	
//		
//		IMarkerSet set16 = MarkerFactory.newBitMarkerSet();
//		IMarkerSet set17 = MarkerFactory.newMarkerSet();
//		set16.addAll(list);
//		set17.addAll(list);
//		
////		assertTrue(set16.toString().equals(set17.toString()));
////		assertTrue(set16.toUserString().equals(set17.toUserString()));
////		assertTrue(set16.iterator().equals(set17.iterator()));
////		assertTrue(set16.size() == set17.size());
////		assertTrue(set16.toArray().equals(set17.toArray()));
//		
//		set6.add(attr);
//		set6.add(attr4);
//		set3.add(attr3);
//		
//		assertEquals(set3,set6);
//		assertEquals(set3.hashCode(), set6.hashCode());
//		
//		set4.add(m1);
//		set4.add(m3);
//		set5.add(m4);
//		set5.add(m2);
//		
//		assertEquals(set4, set5);
//		assertEquals(set4.hashCode(), set5.hashCode());
//		
//		//getElems()
//		assertEquals(set4.getElems(), set5.getElems());
//		
//		
//		
//		assertEquals(MarkerParser.getInstance().parseSet("{}"), 
//				MarkerParser.getInstance().parseSet("{}"));
//		
//		assertFalse(MarkerParser.getInstance().parseSet("{A(tramp,1,name)}").equals( 
//				MarkerParser.getInstance().parseSet("{}")));
//		
//		assertFalse(MarkerParser.getInstance().parseSet("{}").equals( 
//				MarkerParser.getInstance().parseSet("{A(tramp,1,name)}")));
//	}
//	
	
	
	
}
