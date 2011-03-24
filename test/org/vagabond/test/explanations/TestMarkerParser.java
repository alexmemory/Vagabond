package org.vagabond.test.explanations;

import static org.junit.Assert.*;

import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.test.AbstractVagabondTest;

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
		
		assertEquals(expec, MarkerParser.getInstance().parserSet(markSet));
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
	
}
