package org.vagabond.explanation.ranking;

import java.util.HashMap;
import java.util.Map;

import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.util.Pair;

public class RankerFactory {

	private static Map<String, Pair<Class, Class>> rankerSchemes;
	
	static {
		rankerSchemes = new HashMap<String, Pair<Class, Class>> ();
		rankerSchemes.put("Dummy", new Pair<Class,Class> (
				DummyRanker.class, 
				null));
		
		rankerSchemes.put("SideEffect", new Pair<Class,Class> (
				SideEffectExplanationRanker.class, 
				PartitionSideEffectRanker.class));
	}
	
	public static IExplanationRanker createRanker (String rankScheme) {
		return (IExplanationRanker) instantiate (rankerSchemes.get(rankScheme).getKey());
	}
	
	public static IExplanationRanker createInitializedRanker (String rankScheme, ExplanationCollection col) {
		IExplanationRanker result = createRanker (rankScheme);
		result.initialize(col);
		
		return result;
	}
	
	public static IPartitionRanker createPartRanker (String rankScheme) {
		return (IPartitionRanker) instantiate(rankerSchemes.get(rankScheme).getValue());
	}
	
	public static IPartitionRanker createPartRanker (String rankScheme, ExplPartition part) {
		IPartitionRanker result = createPartRanker(rankScheme);
		result.initialize(part);
		
		return result;
	}
	
	private static Object instantiate (Class c) {
		if(c.equals(DummyRanker.class))
			return new DummyRanker();
		if(c.equals(SideEffectExplanationRanker.class))
			return new SideEffectExplanationRanker();
		
		if(c.equals(PartitionSideEffectRanker.class))
			return new PartitionSideEffectRanker();
		
		return null;
	}
}
