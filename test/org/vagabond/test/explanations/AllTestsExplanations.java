package org.vagabond.test.explanations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestCopyExplGen.class,
		TestCopyExplGenQueries.class,
		TestCorrExplGen.class,
		TestCorrExplGenQueries.class,
		TestExplanationCollection.class,
		TestExplSetGen.class,
		TestInflExplGen.class,
		TestMarkerParser.class,
		TestMarkers.class,
		TestQueryHolder.class,
		TestSrcSkeMapExplGen.class,
		TestSuperMapExplGen.class,
		TestTgtSkeMapExplGen.class
        })
public class AllTestsExplanations {

}
