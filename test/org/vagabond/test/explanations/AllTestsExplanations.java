package org.vagabond.test.explanations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestCopyExplGen.class,
		TestCopyExplGenQueries.class,
		TestCorrExplGen.class,
		TestCorrExplGenQueries.class,
		TestExplSetGen.class,
		TestInflExplGen.class,
		TestQueryHolder.class,
		TestSrcSkeMapExplGen.class,
		TestSuperMapExplGen.class,
		TestTgtSkeMapExplGen.class,
		TestProvAndSideEffect.class,
		TestProvAndSideEffectQueries.class,
		TestProvParsers.class
        })
public class AllTestsExplanations {

}
