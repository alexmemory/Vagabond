package org.vagabond.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.vagabond.test.explanations.AllTestsExplanations;
import org.vagabond.test.mapping.model.AllTestMappingModel;
import org.vagabond.test.util.AllTestsUtil;
import org.vagabond.test.xmlbeans.AllTestsXML;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		AllTestsExplanations.class,
		AllTestsUtil.class,
        AllTestsXML.class,
        AllTestMappingModel.class
        })
public class AllTests {
}
