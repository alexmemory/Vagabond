package org.vagabond.test.explanations.model;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.CollectionUtils;

public class TestMarkerParser extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestMarkerParser.class);
	
	@BeforeClass
	public static void load () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
	}
	
	@Test
	public void testMarkerFromString () throws Exception {
		IAttributeValueMarker attr = MarkerFactory
				.newAttrMarker("employee", "2|2", "city");
		ITupleMarker tup = MarkerFactory
				.newTupleMarker("employee", "4|2");
		
		assertEquals(attr, MarkerParser.getInstance().parseMarker("A(employee,2|2,city)"));
		assertEquals(tup, MarkerParser.getInstance().parseMarker("T(employee,4|2)"));
	}
	
	@Test
	public void testMarkerSetFromString () throws Exception {
		IMarkerSet expec = MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "2|2", "city"),
				MarkerFactory.newTupleMarker("employee", "4|2")
				);
		String markSet = "{A(employee,2|2,city),T(employee,4|2)}";
		
		assertEquals(expec, MarkerParser.getInstance().parseSet(markSet));
	}
	
	@Test
	public void testEmptyMarkerSet () throws Exception {
		IMarkerSet expec = MarkerFactory.newMarkerSet();
		String markerSet = "{}";
		
		assertEquals(expec, MarkerParser.getInstance().parseSet(markerSet));
	}
	
	@Test
	public void testLoadMarkerFile () throws Exception {
		IMarkerSet expec = MarkerFactory.newMarkerSet(
			MarkerFactory.newAttrMarker("employee", "2|2", "city"),
			MarkerFactory.newTupleMarker("employee", "4|2")
			);
		
		assertEquals(expec,MarkerParser.getInstance().parseMarkers(
				new FileInputStream("resource/test/markers.txt")));
	}
	
	@Test
	public void testParseVector () throws Exception {
		Vector<ITupleMarker> result, expect;
		
		expect = CollectionUtils.makeVec(
				MarkerFactory.newTupleMarker("employee", "1"),
				MarkerFactory.newTupleMarker("employee", "2"));
		
		result = MarkerParser.getInstance().parseWL("  {T(employee,1)," +
				" T(employee,2) }");
		
		assertEquals(expect, result);
		
		expect = CollectionUtils.makeVec(
				MarkerFactory.newTupleMarker("employee", "1"),
				null);
		
		result = MarkerParser.getInstance().parseWL("  {T(employee,1)," +
				" null }");
		
		assertEquals(expect, result);
	}
	
}
