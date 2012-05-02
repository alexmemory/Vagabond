package org.vagabond.test.explanation.ranking;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestDummyExplRanker.class,
	TestPartitionRanker.class,
	TestAStarRanker.class,
	TestScoringFunctions.class,
	TestSkylineRanker.class
        })
public class AllTestRanking {
}
