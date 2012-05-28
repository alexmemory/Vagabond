package org.vagabond.test.explanations.model;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.AbstractVagabondTest;

public class TestScenarioDictionary extends AbstractVagabondTest {

	@Before
	public void setUp () throws XmlException, IOException, ValidationException {
		setSchemas("resource/exampleScenarios/homeless.xml");
	}
	
	@Test
	public void testMappingIds () throws Exception {
		ScenarioDictionary dic = ScenarioDictionary.getInstance();
		
		assertEquals("M1 = 0", 0, dic.getMapId("M1"));
		assertEquals("M2 = 1", 1, dic.getMapId("M2"));
		
		assertEquals("0 = M1", "M1", dic.getMapName(0));
		assertEquals("1 = M2", "M2", dic.getMapName(1));
		
		assertEquals("M1.a = 0", 0, dic.getVarId("M1", "a"));
		assertEquals("M1.b = 1", 1, dic.getVarId("M1", "b"));
		assertEquals("M1.c = 2", 2, dic.getVarId("M1", "c"));
		assertEquals("0 = M1.a", "a", dic.getVarName(0, 0));
		assertEquals("1 = M1.b", "b", dic.getVarName(0, 1));
		assertEquals("2 = M1.c", "c", dic.getVarName(0, 2));
		
		assertEquals("M2.a = 0", 0, dic.getVarId("M2", "a"));
		assertEquals("M2.b = 1", 1, dic.getVarId("M2", "b"));
		assertEquals("M2.c = 2", 2, dic.getVarId("M2", "c"));
		assertEquals("0 = M2.a", "a", dic.getVarName(0, 0));
		assertEquals("1 = M2.b", "b", dic.getVarName(0, 1));
		assertEquals("2 = M2.c", "c", dic.getVarName(0, 2));
	}
	

	
}
