package org.vagabond.explanation.ranking;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.vagabond.commandline.explgen.ExplGenOptions;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.scoring.AvgErrTypeWeightScore;
import org.vagabond.explanation.ranking.scoring.ErrorTypeScore;
import org.vagabond.explanation.ranking.scoring.ExplanationSizeScore;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.explanation.ranking.scoring.ScoreBasedTotalOrderComparator;
import org.vagabond.explanation.ranking.scoring.ScoreBasicComparator;
import org.vagabond.explanation.ranking.scoring.ScoreExplSetComparator;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.explanation.ranking.scoring.WeightedCombinedWMScoring;

public class RankerFactory {

	private Map<String, RankScheme> rankerSchemes;
	private static RankerFactory inst;
	
	private class RankScheme {
		public Class singleRanker;
		public Class partRanker;
		public IScoringFunction scoreFunction;
		public Comparator<IExplanationSet> comp;
		public Comparator<IBasicExplanation> bComp;
		
		public RankScheme (Class singleRanker, Class partRanker,IScoringFunction scoreFunction) {
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
	

	public static void putRankerScheme(String name, Class singleRanker, Class partRanker, IScoringFunction f) {
		inst.rankerSchemes.put(name, inst.new RankScheme(singleRanker, partRanker, f));
	}
	
	public static String RankerSchemeConstructor(int rankertype, String[] funcnames, double[] funcweights, double[] errweights){		
		String mNewRankerName = "";
		
		switch (rankertype){
			case 1:
				{
					if (errweights.length == 6)
					
					{
						mNewRankerName = "AvgTypeWeight[";
					    
					    for (int j = 0; j<errweights.length; j++){
						    mNewRankerName += errweights[j];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "]";
					    
					    IScoringFunction f = new AvgErrTypeWeightScore(errweights);
						RankerFactory.putRankerScheme(mNewRankerName,
			                                          AStarExplanationRanker.class,
			                                          PartitionRanker.class,
	                                                  f);
					}
				}
				break;
			case 2:
				{
					if (funcnames.length != 0 
							&& funcweights.length == funcnames.length)
					{
						mNewRankerName = "WeightedCombined[";
					    
					    for (int i = 0; i < funcnames.length; i++){
						    mNewRankerName += funcnames[i];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "][";
					    
					    for (int j = 0; j<funcweights.length; j++){
						    mNewRankerName += funcweights[j];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "]";
					    
					    IScoringFunction[] mScoreFuncs = new IScoringFunction[funcnames.length];
					    for (int k = 0; k<funcnames.length; k++){
					    	mScoreFuncs[k] = inst.getScoreFunction(funcnames[k]);
					    }
						RankerFactory.putRankerScheme(mNewRankerName,
								WeightedAStarExplanationRanker.class, 
								PartitionRanker.class,
								new WeightedCombinedWMScoring(mScoreFuncs, funcweights));
						//mNewRankerName = RankerFactory.createWeightedCombined(funcnames, funcweights);
					}
				}
				break;
			case 3:
				{
					if (errweights.length == 6)
					{
						mNewRankerName = "ErrorType[";
					    
					    for (int j = 0; j<errweights.length; j++){
						    mNewRankerName += errweights[j];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "]";
						IScoringFunction f = new ErrorTypeScore(errweights);
						RankerFactory.putRankerScheme(mNewRankerName,
								                      AStarExplanationRanker.class,
								                      PartitionRanker.class,
		                                              f);
					}
					}
				
				break;

			case 4: //boundary ranker
				{
					if (errweights.length == 6)
						
					{
						mNewRankerName = "AvgTypeWeight[";
					    
					    for (int j = 0; j<errweights.length; j++){
						    mNewRankerName += errweights[j];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "]";
					    
					    IScoringFunction f = new AvgErrTypeWeightScore(errweights);
						RankerFactory.putRankerScheme(mNewRankerName,
								BoundRanker.class,
			                                          PartitionRanker.class,
	                                                  f);
					}
					else if (funcnames.length != 0 
							&& funcweights.length == funcnames.length)
					{
						mNewRankerName = "WeightedCombined[";
					    
					    for (int i = 0; i < funcnames.length; i++){
						    mNewRankerName += funcnames[i];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "][";
					    
					    for (int j = 0; j<funcweights.length; j++){
						    mNewRankerName += funcweights[j];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "]";
					    
					    IScoringFunction[] mScoreFuncs = new IScoringFunction[funcnames.length];
					    for (int k = 0; k<funcnames.length; k++){
					    	mScoreFuncs[k] = inst.getScoreFunction(funcnames[k]);
					    }
						RankerFactory.putRankerScheme(mNewRankerName,
								BoundRanker.class, 
								PartitionRanker.class,
								new WeightedCombinedWMScoring(mScoreFuncs, funcweights));
						//mNewRankerName = RankerFactory.createWeightedCombined(funcnames, funcweights);
					}
					else if (errweights.length == 6)
					{
						mNewRankerName = "ErrorType[";
					    
					    for (int j = 0; j<errweights.length; j++){
						    mNewRankerName += errweights[j];
						    mNewRankerName += ",";
					    }
					    mNewRankerName += "]";
						IScoringFunction f = new ErrorTypeScore(errweights);
						RankerFactory.putRankerScheme(mNewRankerName,
								BoundRanker.class,
								                      PartitionRanker.class,
		                                              f);
					}
						
				}
				break;
			default:
				break;
				
		}
		return mNewRankerName;
		
	}
	
	public static String createAverageTypeWeight(double[] errweights){
	    double[] mErrWeights = errweights;
	    
	    String mNewRankerName = "AvgTypeWeight[";
	    
	    for (int j = 0; j<mErrWeights.length; j++){
		    mNewRankerName += mErrWeights[j];
		    mNewRankerName += ",";
	    }
	    mNewRankerName += "]";
	    
		inst.rankerSchemes.put(mNewRankerName, inst.new RankScheme (
				AStarExplanationRanker.class,
				PartitionRanker.class,
				new AvgErrTypeWeightScore(mErrWeights)));
		
		return mNewRankerName;
	}
	
	public static String createWeightedCombined(String[] funcnames, double[] funcweights){
	    String[] mFuncNames = funcnames;
	    double[] mFuncWeights = funcweights;
	    
	    String mNewRankerName = "WeightedCombined[";
	    
	    for (int i = 0; i < mFuncNames.length; i++){
		    mNewRankerName += mFuncNames[i];
		    mNewRankerName += ",";
	    }
	    mNewRankerName += "][";
	    
	    for (int j = 0; j<mFuncWeights.length; j++){
		    mNewRankerName += mFuncWeights[j];
		    mNewRankerName += ",";
	    }
	    mNewRankerName += "]";
	    
	    IScoringFunction[] mScoreFuncs = new IScoringFunction[mFuncNames.length];
	    for (int k = 0; k<mFuncNames.length; k++){
	    	mScoreFuncs[k] = inst.getScoreFunction(mFuncNames[k]);
	    }

	    
		inst.rankerSchemes.put(mNewRankerName, inst.new RankScheme (
				WeightedAStarExplanationRanker.class, 
				PartitionRanker.class,
				new WeightedCombinedWMScoring(mScoreFuncs, mFuncWeights)));
		
		return mNewRankerName;
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
	
	public static SkylineRanker createSkylineRanker (String[] rankSchemes, String finalScheme, 
			ExplPartition part) {
		SkylineRanker result = new SkylineRanker(rankSchemes, finalScheme);
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
	
	public static Collection<String> getRankerSchemes () {
		return inst.rankerSchemes.keySet();
	}
}
