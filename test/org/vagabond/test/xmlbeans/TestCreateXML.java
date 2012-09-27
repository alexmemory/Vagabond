package org.vagabond.test.xmlbeans;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlCursor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.xmlmodel.RelAtomType;

public class TestCreateXML {

	static Logger log = Logger.getLogger(TestCreateXML.class);
	
	@BeforeClass
	public static void setUp () {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
	}
	
	@Test
	public void testAddChoiceElementsToSeq () {
		RelAtomType a = RelAtomType.Factory.newInstance();
		a.addVar("a");
		a.addNewSKFunction().setSkname("SK1");
		a.addNewSKFunction().setSkname("SK2");
		a.addVar("c");
		XmlCursor x = a.newCursor();
		x.toFirstChild();
		if (log.isDebugEnabled()) {log.debug(x.getObject());};
		if (log.isDebugEnabled()) {log.debug(a);};
	}
}
