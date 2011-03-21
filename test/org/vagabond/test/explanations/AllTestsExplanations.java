package org.vagabond.test.explanations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestMarkers.class,
		TestCopyExplGen.class,
		TestCopyExplGenQueries.class
        })
public class AllTestsExplanations {

}
