package org.vagabond.explanation.ranking;

import java.util.List;

import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.explanation.ranking.BoundRanker.ErrIDMapNode;
import org.vagabond.explanation.ranking.BoundRanker.PartialQNode;
class Boundary
{	
    private IScoringFunction scoref;
    private ExplanationCollection col;
    private int FType;
    
    public Boundary(ExplanationCollection coll, IScoringFunction f) {
    	this.scoref = f;
    	this.col = coll;
    	this.FType = f.getFTypeCode();
	}

	public int getUpBound(PartialQNode Qnode, IBasicExplanation Expl, List<ErrIDMapNode> MarkerExplMap)
	//higher bound is worse
	{
			
		int retV = -1;
		switch (FType)
		{
		    case 1:
		        {
		        	//1. check expl in unset explset index
		        	
		        	//2. increase counter if not included
		        	retV = Qnode.actualExplCount + (Qnode.explIdxVec.length - Qnode.expandStep);
		        	
		        }
		    	break;
		    	
		    case 2:
		    {
		    	//SideEffectSizeScore
		    	//up: func(expl_set) + sum (max(side-effect size))
		    	//low: max(func(expl_set), max [ min (side-effect size (new expl set))]
		    	int tmpScore = scoref.getScore(Qnode.partialExplSet);
		    	int currStep = Qnode.expandStep;
		    	int maxStep = MarkerExplMap.size();
		    	
		    	int maxSideEffSize;
		    	//for each unset error, compute max side-effect size
		    	for (int i=currStep; i<maxStep; i++)
		    	{
		    		maxSideEffSize = 0;
		    		
		    		int ErrExplSetSize = MarkerExplMap.get(i).explIDMap.getSize();
		    		for (int j=0; j<ErrExplSetSize; j++)
		    		{
		    			maxSideEffSize = Math.max(maxSideEffSize, MarkerExplMap.get(i).explIDMap.get(j).getSourceSideEffectSize());
		    		}
		    		
		    		tmpScore = tmpScore + maxSideEffSize;
		    		
		    	}
		    	retV = tmpScore;
		    	
		    }
		    break;
		    	
		    case 3:
		    	//WeightedCombinedWMScoring
	        {
	    	
	        }
	    	break;
	    	
		    case 4:
		    	//EntropyScore
				 // 3. Source Error Type Entropy:
				 //    1) increase size of err_type to 5 (or the size of type set)
				 //    2) bin-pack, make the err_type bins equal
	        {

	        }
	    	break;
	    	
		    case 5://AvgErrTypeWeightScore
	        {
	    	
	        }
	    	break;
		    	
		    default:
		    	
		    	break;
		}				
			
		return retV;
	}      
	
    public int getLowBound(PartialQNode Qnode, IBasicExplanation Expl, List<ErrIDMapNode> MarkerExplMap)
    //lower bound is better
    {
		
		int retV = -1;		
		switch (FType)
		{
		    case 1:
		    	/*
		    	ExplanationSizeScore
		    	-Size of Explanation: 
		    		up: length(error_vector) + [ M -  length(error_vector)]
		    		low: length(error_vector) + 1
		    	*/
		    	retV = Qnode.lowbound + 1;
		    	break;
		    	
		    case 2:
		    {
		    	//SideEffectSizeScore
		    	//up: func(expl_set) + sum (max(side-effect size))
		    	//low: max(func(expl_set), max [ min (side-effect size (new expl set))]
		    	
		    	int tmpScore = scoref.getScore(Qnode.partialExplSet);
		    	
		    	IExplanationSet newExplSet = Qnode.partialExplSet;
		    	newExplSet.add(Expl);
		    	
		    	int currStep = Qnode.expandStep;
		    	int maxStep = MarkerExplMap.size();
		    	
		    	int minSideEffSize;
		    	//for each unset error, compute min side-effect size
		    	for (int i=currStep; i<maxStep; i++)
		    	{
		    		minSideEffSize = 0;
		    		
		    		int ErrExplSetSize = MarkerExplMap.get(i).explIDMap.getSize();
		    		for (int j=0; j<ErrExplSetSize; j++)
		    		{
		    			minSideEffSize = Math.min(minSideEffSize, MarkerExplMap.get(i).explIDMap.get(j).getSourceSideEffectSize());
		    		}
		    		
		    		tmpScore = tmpScore + minSideEffSize;
		    		
		    	}
		    	retV = tmpScore;
		    	
		    }
		    break;
		    	
		    case 3:
		    	//WeightedCombinedWMScoring

				
		    	break;
		    	
		    case 4:
		    	//EntropyScore
		    	//Source Error Type Entropy: all new errs add to max-weighted err-type
		    	// new data structure?
		    	// need to store size of err-types and info of current expl-set
				//	{
				//		double probability = (ErrTypeCounter.get(ErrType) + new_size) / 	
				//				            ( TotalSize + new_size );
				//		elem.max = elem.max - probability * Math.log(probability);
				//	}
			    //	break;
		    	// new data structure?
		    	// need to store size of err-types and info of current expl-set
/*		    	int size_diff = sizeof_typeset - sizeof_curr_typeset;
		    	
		    	int init_alloc = min(min(sizeof_curr_typeset), additional_size / size_diff);
		    	
		    	int remain_alloc = additional_size - init_alloc * size_diff;
		    	
		    	if ( remain_alloc >= (size_diff + 1) ){
		    		remain_alloc = remain_alloc - (size_diff + 1);
		    		//increase smallest k+1 bins by 1
		    		
		    	}
		    	else{
		    		//increase smallest bins by 1
		    	}
		    	
		    	//recalculate entropy score
				double retScore = 0.0;
				
				for (ExplanationType ErrType : ErrTypeCounter.keySet())
				{
					double probability = ErrTypeCounter.get(ErrType) / TotalSize;
					retScore = retScore - probability * Math.log(probability);
				}
	
				elem.min =  retScore;*/
		    	break;
		    	
		    case 5://AvgErrTypeWeightScore
		    	
		    	break;
		    	
		    default:
		    	
		    	break;
		    	

		}  
    
		return retV;
    }
}