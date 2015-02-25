/**
 * 
 */
package org.vagabond.explanation.metrics;

import java.util.Iterator;

import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.ranking.IExplanationRanker;
import org.vagabond.explanation.ranking.IPartitionRanker;

/**
 * @author lord_pretzel
 *
 */
public class RankingMetricPrecisionRecall {

	private IExplanationSet groundTruth;
	
	public RankingMetricPrecisionRecall (IExplanationSet groundTruth) {
		this.groundTruth = groundTruth;
	}

	/**
	 * 
	 * | U Lambda_i n Lambda | / | U Lambda_i |
	 * 
	 * @param r
	 * @return
	 */
	public double computePrecision (IExplanationRanker r, int rank) {
		double prec = 0.0;
		IExplanationSet all = union (r, rank);
		int unSize = all.size();
		int inSize;
		
		all.retainAll(groundTruth); // compute intersection
		inSize = all.size();
		
		prec = ((double) inSize) / ((double) unSize);
		
		return prec;
	}
	
	public double computePrecision (IPartitionRanker r, int rank) {
		double prec = 0.0;
		IExplanationSet all = union (r, rank);
		int unSize = all.size();
		int inSize;
		
		all.retainAll(groundTruth); // compute intersection
		inSize = all.size();
		
		prec = ((double) inSize) / ((double) unSize);
		
		return prec;
	}
	
	/**
	 *
	 * | U Lambda_i n Lambda | / | Lambda | 
	 *
	 * @param r
	 * @return
	 */
	public double computeRecall (IExplanationRanker r, int rank) {
		double recall = 0.0;
		IExplanationSet all = union (r, rank);
		int groundSize = groundTruth.size();
		int inSize;
		
		all.retainAll(groundTruth); // compute intersection
		inSize = all.size();
		
		recall = ((double) inSize) / ((double) groundSize);	
		
		return recall;
	}
	
	/**
	 *
	 * | U Lambda_i n Lambda | / | Lambda | 
	 *
	 * @param r
	 * @return
	 */
	public double computeRecall (IPartitionRanker r, int rank) {
		double recall = 0.0;
		IExplanationSet all = union (r, rank);
		int groundSize = groundTruth.size();
		int inSize;
		
		all.retainAll(groundTruth); // compute intersection
		inSize = all.size();
		
		recall = ((double) inSize) / ((double) groundSize);	
		
		return recall;
	}
	
	public IExplanationSet union (IPartitionRanker iter, int limit) {
		IExplanationSet result;
		int i = 0;
		iter.resetIter();
		result = iter.next();
		while(iter.hasNext() && i++ < limit)
			result = result.union(iter.next());
		return result;
	}
	
	public IExplanationSet union (IExplanationRanker iter, int limit) {
		IExplanationSet result;
		int i = 0;
		iter.resetIter();
		result = iter.next();
		while(iter.hasNext() && i++ < limit)
			result = result.union(iter.next());
		return result;
	}
	
	public IExplanationSet getGroundTruth() {
		return groundTruth;
	}

	public void setGroundTruth(IExplanationSet groundTruth) {
		this.groundTruth = groundTruth;
	}
	
	
}
