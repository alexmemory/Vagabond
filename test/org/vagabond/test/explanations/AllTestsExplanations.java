package org.vagabond.test.explanations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestMarkers.class,
		TestQueryHolder.class,
		TestExplSetGen.class,
		TestCopyExplGen.class,
		TestCopyExplGenQueries.class,
		TestCorrExplGen.class,
		TestCorrExplGenQueries.class,
		TestSrcSkeMapExplGen.class
        })
public class AllTestsExplanations {

}
