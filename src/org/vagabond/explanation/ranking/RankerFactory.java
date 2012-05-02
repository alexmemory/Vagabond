package org.vagabond.explanation.ranking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.scoring.ExplanationSizeScore;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.explanation.ranking.scoring.ScoreBasedTotalOrderComparator;
import org.vagabond.explanation.ranking.scoring.ScoreBasicComparator;
import org.vagabond.explanation.ranking.scoring.ScoreExplSetComparator;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.util.Pair;

public class RankerFactory {

	private Map<String, RankScheme> rankerSchemes;
	private static RankerFactory inst;
	
	private class RankScheme {
		public Class singleRanker;
		public Class partRanker;
		public IScoringFunction scoreFunction;
		public Comparator<IExplanationSet> comp;
		public Comparator<IBasicExplanation> bComp;
		
		public RankScheme (Class singleRanker, Class partRanker, IScoringFunction scoreFunction) {
			this.singleRanker = singleRanker;
			this.partRanker = partRanker;
			this.scoreFunction = scoreFunction;
			this.comp =  new ScoreExplSetComparator (this.scoreFunction);
			this.bComp = new ScoreBasicComparator(this.scoreFunction);
		}
	} 
	
	static {
		inst = new RankerFactory();
		
		inst.rankerSchemes = new HashMap<String, RankScheme> ();
		inst.rankerSchemes.put("Dummy", inst.new RankScheme (
				DummyRanker.class, 
				null,
				null));
		
		inst.rankerSchemes.put("SideEffect", inst.new RankScheme (
				AStarExplanationRanker.class, 
				PartitionRanker.class,
				SideEffectSizeScore.inst));
		inst.rankerSchemes.put("ExplSize", inst.new RankScheme (
				AStarExplanationRanker.class,
				PartitionRanker.class,
				ExplanationSizeScore.inst));
	}
	
	public static IExplanationRanker createRanker (String rankScheme) {
		return (IExplanationRanker) instantiate (inst.rankerSchemes.get(rankScheme).singleRanker, 
				inst.rankerSchemes.get(rankScheme).scoreFunction);
	}
	
	public static IExplanationRanker createInitializedRanker (String rankScheme, ExplanationCollection col) {
		IExplanationRanker result = createRanker (rankScheme);
		result.initialize(col);
		
		return result;
	}
	
	public static IPartitionRanker createPartRanker (String rankScheme) {
		return (IPartitionRanker) instantiatePart(inst.rankerSchemes.get(rankScheme).partRanker, 
				inst.rankerSchemes.get(rankScheme).scoreFunction);
	}
	
	public static IPartitionRanker createPartRanker (String rankScheme, ExplPartition part) {
		IPartitionRanker result = createPartRanker(rankScheme);
		result.initialize(part);
		
		return result;
	}
	
	public static IScoringFunction getScoreFunction (String rankerScheme) {
		return inst.rankerSchemes.get(rankerScheme).scoreFunction;
	}
	
	public static Comparator<IExplanationSet> getScoreExplSetComparator (String rankerScheme) {
		return inst.rankerSchemes.get(rankerScheme).comp;
	}
	
	public static Comparator<IBasicExplanation> getScoreBasicComparator (String rankerScheme) {
		return inst.rankerSchemes.get(rankerScheme).bComp;
	}
	
	public static Comparator<IBasicExplanation> getScoreBasicComparator (IScoringFunction f) {
		return new ScoreBasicComparator(f);
	}
	
	public static Comparator<IBasicExplanation> getScoreTotalOrderComparator (IScoringFunction f) {
		return new ScoreBasedTotalOrderComparator(f);
	}
	
	private static Object instantiatePart (Class c, IScoringFunction f) {
		if(c.equals(PartitionRanker.class))
			return new PartitionRanker(f);
		return null;
	}
	
	private static Object instantiate (Class c, IScoringFunction f) {
		if(c.equals(DummyRanker.class))
			return new DummyRanker();
		if(c.equals(AStarExplanationRanker.class))
			return new AStarExplanationRanker(f);
		
		return null;
	}
}
