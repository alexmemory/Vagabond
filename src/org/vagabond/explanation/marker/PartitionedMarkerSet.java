package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.vagabond.util.Pair;

public class PartitionedMarkerSet implements Iterable<IMarkerSet> {

	public class FullPartIterator implements Iterator<Pair<IMarkerSet,MarkerSummary>> {

		private int pos = 0;
		
		@Override
		public boolean hasNext() {
			return pos < mParts.size();
		}
		
		@Override
		public Pair<IMarkerSet,MarkerSummary> next() {
			if (pos >= mParts.size())
				throw new NoSuchElementException();
			Pair<IMarkerSet, MarkerSummary> result =  new Pair<IMarkerSet, MarkerSummary> (mParts.get(pos), attrParts.get(pos));
			pos++;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removal is not supported");
		}		
	}
	
	public class MSetIterator implements Iterator<IMarkerSet> {

		private int pos = 0;
		
		public MSetIterator () {
			
		}
		
		@Override
		public boolean hasNext() {
			return pos < mParts.size();
		}

		@Override
		public IMarkerSet next() {
			if (pos >= mParts.size())
				throw new NoSuchElementException();
			return mParts.get(pos++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removal is not supported");
		}
		
	}
	
	ArrayList<IMarkerSet> mParts;
	ArrayList<MarkerSummary> attrParts;
	Map<ISchemaMarker, Integer> attrToMSet;
	
	
	public PartitionedMarkerSet () {
		mParts = new ArrayList<IMarkerSet> ();
		attrParts = new ArrayList<MarkerSummary> ();
		attrToMSet = new HashMap<ISchemaMarker, Integer> ();
	}
	
	public IMarkerSet getMarkerPartition (ISchemaMarker attr) {
		return mParts.get(attrToMSet.get(attr));
	}
	
	public MarkerSummary getAttrPartition (ISchemaMarker attr) {
		return attrParts.get(attrToMSet.get(attr));
	}
	
	public MarkerSummary getAttrPartition (int part) {
		return attrParts.get(part);
	}
	
	public void addPartition (IMarkerSet markers, MarkerSummary attrs) {
		int numPart;
		
		mParts.add(markers);
		attrParts.add(attrs);
		
		numPart = attrParts.size();
		for (ISchemaMarker m : attrs)
			attrToMSet.put(m, numPart - 1);
	}
	
	public int getNumParts () {
		return mParts.size();
	}
	
	public IMarkerSet getPartition (ISchemaMarker m) {
		return mParts.get(attrToMSet.get(m));
	}
	
	public IMarkerSet getPartition (int part) {
		if (part < 0 || part >= mParts.size())
			throw new NoSuchElementException();
		return mParts.get(part);
	}
	
	public Pair<IMarkerSet,MarkerSummary> getPartAndSum (int part) {
		if (part < 0 || part >= mParts.size())
			throw new NoSuchElementException();
		return new Pair<IMarkerSet,MarkerSummary> (mParts.get(part), attrParts.get(part));
	}

	@Override
	public Iterator<IMarkerSet> iterator() {
		return new MSetIterator();
	}
	
	public Iterator<Pair<IMarkerSet,MarkerSummary>> pairIterator() {
		return new FullPartIterator();
	}
	
	@Override
	public boolean equals (Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		
		if (other instanceof PartitionedMarkerSet) {
			PartitionedMarkerSet o = (PartitionedMarkerSet) other;
			
			return this.attrParts.equals(o.attrParts) 
					&& this.mParts.equals(o.mParts);
		}
		
		return false;
	}
	
	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer ();
		
		buf.append("PARITIONED MARKER SET <" + attrParts + ">\n\n");
		buf.append(mParts);
		
		return buf.toString();
	}
	
	
}
