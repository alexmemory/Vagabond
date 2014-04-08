package org.vagabond.explanation.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.util.IdMap;
import org.vagabond.util.LogProviderHolder;

public class BoundRanker implements IExplanationRanker {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			BoundRanker.class);
	
	private Boundary BoundaryInst;
    
	//priority queue of elements
    private PriorityQueue<PartialQNode> PartialRankedQueue;	    
    private PriorityQueue<FullQNode> FullRankedQueue;
    private List<ErrIDMapNode> MarkerExplMap;
    
	private ExplanationCollection col;
	private boolean INIT = false;
	private boolean RANKINGDONE = false;
	private IScoringFunction f;

	private int EXPL_NOT_SET = -1;   
	private int ResultSize;
	private int CurrPos;
	
	private int SizeOfMarkerSet;
	
	public BoundRanker (IScoringFunction f) {
		MarkerFactory.newMarkerSet();
		new ArrayList<ISingleMarker> ();
	    PartialRankedQueue = new PriorityQueue<PartialQNode>();	    
	    FullRankedQueue = new PriorityQueue<FullQNode>();
		this.f = f;
	}
	
	public class ErrIDMapNode
	{
		
		 /*      vector ele:
		 *      ID1 = {0: lambda[1,1], 1: lambda[1,2]}
		 *      ID2 = {0: lambda[2,1], 1: lambda[2,2]}
		 *      ID3 = {0: lambda[3,1]}
		 */
		
		ISingleMarker err;
		IdMap<IBasicExplanation> explIDMap; 
	}
	
    public class PartialQNode
    {
    	/*
		 * each PartialRankedQueue node: 
		 * Q1: <ExplIndexVec1:[0, -1, -1], high1, low1, score1, PartialExplSet1, ExpandStep1>
		 * Q2: <ExplIndexVec2:[1, -1, -1], high2, low2, score2, PartialExplSet2, ExpandStep2>
		 */
    	int explIdxVec[];
    	int highbound;
    	int lowbound;
    	int partialscore;
    	IExplanationSet partialExplSet;
    	int expandStep;    	
    	int actualExplCount;
    }
    
    public class FullQNode
    {
    	/*
		 * each FullRankedQueue node:
//		 * Q1: <FullExplSet1, ExplIndexVec1:[0, -1, -1], score1, VecSize1, actualSizeofExplSet>
		 */
    	IExplanationSet fullExplSet;
    	int explIdxVec[];
    	int fullScore;
    	int vecSize;
    	int actualExplCount;
    }
	 
	class partialnodecomp implements Comparator<PartialQNode> 
	{
	    public int compare(PartialQNode ele1, PartialQNode ele2) 
	    {			  
	   	 /* insert into PartialRankedQueue
		 * order by  
		 * 1. lower low bound
		 * 2. lower high bound
		 * 3. smaller expl index
		 */
	    	int comp;
            int up1  = ele1.highbound;
            int low1 = ele1.lowbound;
            int up2  = ele2.highbound;
            int low2 = ele2.lowbound;
            
            comp = low1 - low2;
            if (comp != 0)
            {
            	return comp;
            }
            
            comp = up1 - up2;
            if (up1 - up2 != 0)
            	return comp;
            
            comp = ele1.expandStep - ele2.expandStep;
            if (comp != 0)
            	return comp;
            
            int steps = ele1.expandStep;
            for (int i=0; i< steps; i++)
            {
            	comp = ele1.explIdxVec[i] - ele2.explIdxVec[i];
            	
            	if (comp != 0)
            		return comp;
            }
            
            return comp;
            
		}
	}
	
	class fullnodecomp implements Comparator<FullQNode> 
	{
	    public int compare(FullQNode ele1, FullQNode ele2) 
	    {			  
	    	int comp;

            comp = ele1.fullScore - ele2.fullScore;
            if (comp != 0)
            	return comp;
            
            comp = ele1.fullExplSet.size() - ele2.fullExplSet.size();
            if ( comp != 0 )
            	return comp;

            int steps = ele1.vecSize;
            for (int i=0; i< steps; i++)
            {
            	comp = ele1.explIdxVec[i] - ele2.explIdxVec[i];
            	
            	if (comp != 0)
            		return comp;
            }
            
            return comp;
            
		}
	}
    
	@Override
	public boolean hasNext() {
		if (RANKINGDONE && CurrPos < ResultSize)
			return true;
		if (!RANKINGDONE && CurrPos == ResultSize)
			generateUpTo(ResultSize + 1);
		
		return CurrPos < ResultSize;
	}

	@Override
	public IExplanationSet next() {
		if (RANKINGDONE && CurrPos + 1 >= ResultSize)
			throw new NoSuchElementException("only " + ResultSize + " elements");
		
		if (CurrPos + 1 > ResultSize)
			generateUpTo(CurrPos + 1);
		
		if (CurrPos + 1 > ResultSize)
			throw new NoSuchElementException("only " + ResultSize + " elements");
		
		CurrPos++;	
		
		return (IExplanationSet) FullRankedQueue.toArray()[CurrPos];
	}
	
	
	private void generateUpTo(int upTo) 
	{
		
		while(ResultSize <= upTo) 
		{
			PartialQNode topCandidate = PartialRankedQueue.poll();
			
			 while (topCandidate.explIdxVec[topCandidate.expandStep] != EXPL_NOT_SET && topCandidate.expandStep < SizeOfMarkerSet)
			 {
				 topCandidate.expandStep++;
			 }
			 
			 int SizeOfExplSet = MarkerExplMap.get(topCandidate.expandStep).explIDMap.getSize();
			 
			 for (int i=0; i<SizeOfExplSet; i++)
		     {
				 PartialQNode copyQnode = topCandidate;
				 IBasicExplanation singleExpl = MarkerExplMap.get(topCandidate.expandStep).explIDMap.get(i);
		         
				 //need to check intersection of other expl set to calculate actualExplCount
				 
				 IExplanationSet candidateExplSet = copyQnode.partialExplSet;
				 candidateExplSet.add(singleExpl);
						 
		         copyQnode.highbound = BoundaryInst.getUpBound(copyQnode, singleExpl, MarkerExplMap);
		         copyQnode.lowbound  = BoundaryInst.getLowBound(copyQnode, singleExpl, MarkerExplMap);
		                  
		         copyQnode.partialExplSet.add(singleExpl);
		                 
		         copyQnode.partialscore = f.getScore(copyQnode.partialExplSet);
		                  
		                  if  (copyQnode.expandStep < SizeOfMarkerSet)
		                  {
		                      PartialRankedQueue.add(copyQnode);
		                  }
		                  else
		                  {
		                	  FullQNode newFullRankNode = new FullQNode();
		                	  newFullRankNode.fullExplSet = copyQnode.partialExplSet;
		                	  newFullRankNode.explIdxVec  = copyQnode.explIdxVec;
		                	  newFullRankNode.fullScore   = copyQnode.partialscore;
		                	  newFullRankNode.vecSize     = copyQnode.expandStep;
		                	  newFullRankNode.actualExplCount = copyQnode.actualExplCount;
		                      FullRankedQueue.add(newFullRankNode);
		                      ResultSize ++;
		                      
		                  }
		                  
		              }
		         
				// everything complete -> we are done with ranking
				if (PartialRankedQueue == null) {
					RANKINGDONE = true;
					ResultSize = FullRankedQueue.size();			
					if (ResultSize < upTo)
						throw new NoSuchElementException("trying to access beyond last " +
								"element of ranking");
					return;
				}
		}

	}
	
	@Override
	public void initialize(ExplanationCollection coll) {
		// lower score is better
		this.col = coll;
		ResultSize = 0;
		CurrPos = 0;
		SizeOfMarkerSet = col.getNumErrors();
		int found = 0;
		
		//initialize mapping   err ---- IDMap
		for (IExplanationSet explSet:col.getExplSets())
		{
			found = 0;
			for (IBasicExplanation expl:explSet)
			{
				IMarkerSet thisErrSet = expl.getRealExplains();
				
				for (ISingleMarker err:thisErrSet)
				{
					int currMapSize = MarkerExplMap.size();
					
					for (int i=0; i<currMapSize; i++)
				    {
						if (MarkerExplMap.get(i).err == err)
						{
							found = 1;
							MarkerExplMap.get(i).explIDMap.put(expl);
						}
				    }
				    
                    if ( found == 0 )
				    {
				    	IdMap<IBasicExplanation> newIDMap = new IdMap<IBasicExplanation>();
				    	newIDMap.put(expl);
				    	
				    	ErrIDMapNode newErrIDMapNode = new ErrIDMapNode();
				    	newErrIDMapNode.err = err;
				    	newErrIDMapNode.explIDMap = newIDMap;
				    	
				    	MarkerExplMap.add(newErrIDMapNode);
				    }
				}
			}

		}
		
		BoundaryInst = new Boundary(this.col, f);	
		
		generateUpTo(1);

		INIT = true;

	}

	@Override
	public void rankFull() {
		try {
			generateUpTo (Integer.MAX_VALUE);
		} catch (NoSuchElementException e) {
			if (log.isDebugEnabled()) {log.debug("ranking done");};
		}
		resetIter();
	}

	
	@Override
	public boolean ready() {
		return INIT;
	}

	@Override
	public void confirmExplanation(IBasicExplanation correctExpl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IExplanationSet getRankedExpl(int rank) {
		IExplanationSet result;
		
		assert(rank > 0 && (!RANKINGDONE || ResultSize >= rank));
		if (!RANKINGDONE)
			generateUpTo(rank);
		
		FullQNode[] currentQ =(FullQNode[]) FullRankedQueue.toArray();
		
		result = currentQ[rank].fullExplSet;
		
		resetIter();

		return result;
	}

	@Override
	public int getScore(int rank) {
		int result;
		
		assert(rank > 0 && (!RANKINGDONE || ResultSize >= rank));
		if (!RANKINGDONE)
			generateUpTo(rank);
		
		FullQNode[] currentQ =(FullQNode[]) FullRankedQueue.toArray();
		
		result = currentQ[rank].fullScore;
		
		resetIter();

		return result;
	}

	@Override
	public int getIterPos() {
		return CurrPos;
	}

	@Override
	public IExplanationSet previous() {
		IExplanationSet result;
		
		assert(CurrPos > 1 );

		FullQNode[] currentQ =(FullQNode[]) FullRankedQueue.toArray();
		
		result = currentQ[CurrPos-1].fullExplSet;

		return result;
	}

	@Override
	public boolean hasPrevious() {
		return CurrPos > 1 ;
	}

	@Override
	public int getNumberOfExplSets() {
		return ResultSize;
	}

	@Override
	public int getNumberPrefetched() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetIter() {
		CurrPos = -1;
	}

	@Override
	public boolean hasAtLeast(int numElem) {
		if (!RANKINGDONE)
			generateUpTo(numElem);
		
		return numElem <= ResultSize;
	}

	@Override
	public boolean isFullyRanked() {
		return RANKINGDONE;
	}


	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
}
	