package org.vagabond.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.vagabond.test.explanations.AllTestsExplanations;
import org.vagabond.test.util.AllTestsUtil;
import org.vagabond.test.xmlbeans.AllTestsXML;
import org.vagabond.test.xmlbeans.TestLoadXML;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		AllTestsExplanations.class,
		AllTestsUtil.class,
        AllTestsXML.class
        })
public class AllTests {
}
