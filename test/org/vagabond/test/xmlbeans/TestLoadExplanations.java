package org.vagabond.test.xmlbeans;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.xmlbeans.ExplanationAndErrorXMLLoader;

public class TestLoadExplanations extends AbstractVagabondTest {

	@Before
	public void setUp() throws Exception {
		loadToDB("resource/test/simpleTest.xml");
	}

	@Test
	public void testExplForSimple() throws Exception {
		IExplanationSet s = ExplanationAndErrorXMLLoader.getInstance().loadExplanations(
				"resource/test/testExplForSimple.xml");
		
		CorrespondenceError c = new CorrespondenceError();
		c.setExplains(MarkerParser.getInstance().parseMarker("A(employee,1|1,name)"));
		c.addCorrespondence(MapScenarioHolder.getInstance().getCorr("c1"));
		c.setTargetSE(MarkerParser.getInstance().parseSet("{A(employee,2|2,name)," +
				"A(employee,3|,name),A(employee,4|2,name)}"));
		c.setMapSE(MapScenarioHolder.getInstance().getMappings("M1", "M2"));
		c.setTransSE(MapScenarioHolder.getInstance().getTransformations("T1"));
		
		CopySourceError cse = new CopySourceError();
		cse.setExplains(MarkerParser.getInstance().parseMarker("A(employee,4|2,city)"));
		cse.setSourceSE(MarkerParser.getInstance().parseSet("{A(address,2,city)}"));
		cse.setTargetSE(MarkerParser.getInstance().parseSet("{A(employee,2|2,city)}"));
		
		IExplanationSet ex = ExplanationFactory.newExplanationSet(c,cse);
		
		assertEquals(ex, s);
	}

}
