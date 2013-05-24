package org.vagabond.explanation.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.PartitionedMarkerSet;
import org.vagabond.explanation.ranking.IPartitionRanker;
import org.vagabond.util.Pair;

public class ExplPartition implements Iterable<ExplanationCollection> {

	private ArrayList<ExplanationCollection> cols;
	private PartitionedMarkerSet mPart;
	private IPartitionRanker ranker;
	
	
	public ExplPartition (PartitionedMarkerSet mPart) {
		cols = new ArrayList<ExplanationCollection> ();
		this.mPart = mPart;
	}

	@Override
	public Iterator<ExplanationCollection> iterator() {
		return cols.iterator();
	}
	
	public void add (ExplanationCollection col) {
		cols.add(col);
	}
	
	public ExplanationCollection get (int pos) {
		return cols.get(pos);
	}
	
	public int size () {
		return cols.size();
	}

	protected IPartitionRanker getRanker() {
		return ranker;
	}

	protected void setRanker(IPartitionRanker ranker) {
		this.ranker = ranker;
		ranker.initialize(this);
	}
	
	public ExplanationCollection getCol (int part) {
		return cols.get(part);
	}
	
	public Pair<ExplanationCollection,MarkerSummary> getExplAndPart (int part) {
		return new Pair<ExplanationCollection, MarkerSummary> (cols.get(part), mPart.getAttrPartition(part));
	}

	protected ArrayList<ExplanationCollection> getCols() {
		return cols;
	}

	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		
		if (other instanceof ExplPartition) {
			ExplPartition o = (ExplPartition) other;
			
			return this.mPart.equals(o.mPart) && this.cols.equals(o.cols);
		}
			
		return false;
	}

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer ();
		
		buf.append("---- EXPL PARTITION -----\nfor Partitions:" + mPart.toString());
		buf.append("\n-----------------------\n\n");
		
		buf.append(cols.toString());
		
		return buf.toString();
	}
	
}
