package org.vagabond.test.explanations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.vagabond.test.explanation.ranking.AllTestRanking;
import org.vagabond.test.explanations.model.AllTestExplanationsModel;


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
		TestProvParsers.class,
		AllTestExplanationsModel.class,
		AllTestRanking.class
        })
public class AllTestsExplanations {

}
