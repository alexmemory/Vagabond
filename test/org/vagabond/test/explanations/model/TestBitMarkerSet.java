package org.vagabond.test.explanations.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.BitMarkerSet;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.AbstractVagabondDBTest;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.ewah.BitsetFactory;
import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IBitSet.BitsetType;

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
	
	private IMarkerSet otherSet1;
	private IMarkerSet otherSet2;
	

	
	private IAttributeValueMarker attr;
	private IAttributeValueMarker attr2;
	private IAttributeValueMarker attr3;
	private IAttributeValueMarker attr4;
	private IAttributeValueMarker attr5;
	
	private ITupleMarker FourElementMarker1;
	private ITupleMarker FourElementMarker2;
	private ITupleMarker ThreeElementMarker1;
	private ITupleMarker ThreeElementMarker2;

	private Set<ISingleMarker> list;
	private Set<ISingleMarker> tuppleList;
	
	public void initialize () throws Exception {
		loadToDB("resource/exampleScenarios/homeless.xml");
		set1 = MarkerFactory.newBitMarkerSet();
		set2 = MarkerFactory.newBitMarkerSet();
		otherSet1 = MarkerFactory.newMarkerSet();
		otherSet2 = MarkerFactory.newMarkerSet();
		setNoElement = MarkerFactory.newBitMarkerSet();
		setFirstElement = MarkerFactory.newBitMarkerSet();
		setSecondElement = MarkerFactory.newBitMarkerSet();
		setTwoElement = MarkerFactory.newBitMarkerSet();
		
		attr = MarkerFactory.newAttrMarker("tramp","1","name");
		attr2 = MarkerFactory.newAttrMarker(0, "1", 0);
		attr3 = MarkerFactory.newAttrMarker(1, "2", 0);
		attr4 = MarkerFactory.newAttrMarker(1, "2", 0);
		attr5 = MarkerFactory.newAttrMarker(1, "3", 1);
		
		FourElementMarker1= MarkerFactory.newTupleMarker("tramp", "1");
		FourElementMarker2 = MarkerFactory.newTupleMarker("tramp", "1");
		ThreeElementMarker1 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		ThreeElementMarker2 = MarkerFactory.newTupleMarker("soupkitchen", "1");
		
		setFirstElement.add(attr);
		setSecondElement.add(attr3);
		setTwoElement.add(attr);
		setTwoElement.add(attr3);
		
		list = new HashSet<ISingleMarker>();
		list.add(attr);
		list.add(attr4);
		
		tuppleList = new HashSet<ISingleMarker>();
		tuppleList.add(FourElementMarker1);
		tuppleList.add(ThreeElementMarker1);
	}
	
	@Test
	public void testTidScenDicFunctions () throws Exception {
		initialize();
		ScenarioDictionary d = ScenarioDictionary.getInstance();
		int tidId = d.getTidInt("1", 0);
		assertEquals(tidId, d.getOffset(0, 0, "1"));
		assertTrue (tidId < 3);
		
		int tidId2 = d.getTidInt("2", 0);
		assertEquals(tidId2, d.getOffset(0, 0, "2"));
		assertTrue (tidId2 < 3);
		
		// next attr
		assertEquals(tidId + 2, d.getOffset(0, 1, "1"));
		assertEquals(tidId2 + 2, d.getOffset(0, 1, "2"));
		
		// convert between bitpos and Markers
		assertEquals(attr, d.getAttrValueMarkerByIBitSet( d.attrMarkerToBitPos(attr)));
	}
	
	//add, equals, remove, hashcode, getNumElems
	@Test
	public void testAddRemoveAndHashcode() throws Exception{
		initialize();
		assertEquals(setTwoElement.getNumElem(), 2);
		set1.add(attr);
		assertEquals(set1.hashCode(), setFirstElement.hashCode());
		assertEquals(setFirstElement, set1);
		assertFalse(set1.getNumElem() == setTwoElement.getNumElem());
		assertEquals(set1.getNumElem(), setFirstElement.getNumElem());
		set1.remove(attr);
		assertFalse(setFirstElement.equals(set1));
		assertTrue(setNoElement.equals(set1));
		
		set1.add(FourElementMarker1);
		set2.add(FourElementMarker2);
		assertEquals(set1, set2);
		
	}
	
	// Union(), intersect(), diff(), clone(), addAll()
	@Test
	public void testSetOperations () throws Exception{
		initialize();
		set1.add(attr);
		set2.add(attr3);
		set1.union(set2);
		assertEquals(set1.getNumElem(), setTwoElement.getNumElem());
		assertEquals(set1, setTwoElement);
		set1.intersect(set2);
		assertEquals(set1,set2);
		assertFalse(set1.addAll(list));
		set1.diff(set2);
		assertEquals(set1, setFirstElement);
		set1.addAll(list);
		assertEquals(set1, setTwoElement);
		set1.removeAll(list);
		set2 = set1.cloneSet();
		assertEquals(set2, set1);
		set1.add(FourElementMarker1);
		set1.add(ThreeElementMarker1);
		assertTrue(set2.addAll(tuppleList));
		
		assertTrue(set2.addAll(tuppleList));
//	***	I also try addAll(list), and it only adds one element even when both line set the element

		assertEquals(set1.getNumElem(), set2.getNumElem());
		assertEquals(set1, set2);
	}
	
	
	// getSize(), size(), contains(string string), .getSummary(), subset(), clear(), isEmpty()
	@Test
	public void testSummarySubsetClearAndIsEmpty() throws Exception{
		initialize();
		set1.add(attr);
		assertTrue(set1.contains("tramp", "1"));
		assertEquals(set1.subset(set1.getSummary()), set1);
		set1.clear();
		assertEquals(set1, set2);
		assertTrue(set1.isEmpty());
		set1.addAll(list);
		
		assertEquals(set1.getNumElem(), setTwoElement.getNumElem());
		assertTrue(set1.contains(attr));
		assertTrue(set1.contains(attr3));
		assertTrue(set1.containsAll(list));
		
		
	}
	// getElems(), iterator(), toString(), toArray()
	@Test
	public void testAddAndIterator() throws Exception{
		initialize();
		set1.add(attr);
		set1.add(attr3);
		otherSet1.add(attr);
		otherSet1.add(attr3);

		Iterator<ISingleMarker> iterator1 = set1.iterator();
		Iterator<ISingleMarker> iterator2 = otherSet1.iterator();
		while(iterator1.hasNext()) {
			Object next = iterator1.next();
			assertTrue("Set1 elem " + next.toString(), otherSet1.contains(next));
		}
		while(iterator2.hasNext()) {
			Object next = iterator2.next();
			assertTrue("OtherSet1 elem " + next.toString(), set1.contains(next));
		}
	}
	
	@Test
	public void testBitSet () throws Exception {
		initialize();
		BitMarkerSet b = (BitMarkerSet) set1;
		
		b.add(attr);
		int bitpos = ScenarioDictionary.getInstance().attrMarkerToBitPos(attr);
		String bitString = bitString(bitpos);
		assertEquals("bitpos " + bitpos, bitString, b.getIBitSetElems().toBitsString());
		log.debug(bitString);
		
		b.add(attr3);
		
		int bitpos2 = ScenarioDictionary.getInstance().attrMarkerToBitPos(attr3);
		String bitString2 = bitString(bitpos2, bitpos);
		assertEquals("bitpos " + bitpos2, bitString2, b.getIBitSetElems().toBitsString());
		log.debug(bitString2);
		
		
		b.add(attr5);
		
		int bitpos3 = ScenarioDictionary.getInstance().attrMarkerToBitPos(attr5);
		String bitString3 = bitString(bitpos3, bitpos2, bitpos);
		assertEquals("bitpos " + bitpos3, bitString3, b.getIBitSetElems().toBitsString());
		log.debug(bitString2);
	}
	
	private String bitString  (int ... bitset) {
		IBitSet bit = BitsetFactory.newBitset(BitsetType.EWAHBitSet);
		
		for(int i: bitset)
			bit.set(i);
		
		return bit.toBitsString();
	}
	
	@Test
	public void testToString () throws Exception {
		initialize();
		set1.add(attr2);
		set1.add(attr3);
		
		otherSet1.add(attr3);
		otherSet1.add(attr2);
		
		assertEquals(otherSet1.toUserString(), set1.toUserString());
	}
	
	@Test
	public void testToArray () throws Exception {
		initialize();
		ArrayList<Object> list1 = new ArrayList<Object>(Arrays.asList(set1.toArray()));
		ArrayList<Object> list2 = new ArrayList<Object>(Arrays.asList(otherSet1.toArray()));
		Iterator<Object> iterator = list1.iterator();
		Iterator<Object> otheriterator = list2.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			assertTrue("List1 elem " + next.toString(), list2.contains(next));
		}
		while (otheriterator.hasNext()) {
			Object next = otheriterator.next();
			assertTrue("List2 elem " + next.toString(), list1.contains(next));
		}
	}
	
	//TODO addAll still has problem when adding tupple list and list!!!!!!
	@Test
	public void testAddAll () throws Exception {
		initialize();		
		set1.add(attr5);
		
		set2.add(attr5);
		set2.add(attr4);
		set2.add(attr);
		
		assertFalse(set2.equals(set1));
		set1.addAll(list);
		assertEquals(3, set1.getSize());
		assertEquals(set2, set1);
		
		initialize();
		
		set1.addAll(CollectionUtils.makeSet(attr, attr4));
		
		set2.add(attr);
		set2.add(attr4);
		
		assertEquals(2, set1.getSize());
		assertEquals(set2, set1);
	}
	
}
