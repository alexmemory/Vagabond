package org.vagabond.test.explanation.ranking;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestDummyExplRanker.class,
	TestPartitionSideEffectRanker.class,
	TestSideEffectRanker.class
        })
public class AllTestRanking {
}
