package org.vagabond.explanation.model.prov;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.vagabond.explanation.marker.ITupleMarker;

public class CopyProvExplIterator implements Iterator<Set<ITupleMarker>>{

	private Vector<Integer> iterTupFromWL;
	private Set<ITupleMarker> iterTupInCover;
	private Map<ITupleMarker,Integer> firstWL;
	private CopyProvExpl expl;
	
	public CopyProvExplIterator (CopyProvExpl expl) {
		this.expl = expl;
		
		iterTupFromWL = new Vector<Integer> ();
		iterTupInCover = new HashSet<ITupleMarker> ();
	}
	
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<ITupleMarker> next() {
		Set<ITupleMarker> newCover = new HashSet<ITupleMarker> ();

		
		return newCover;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}

}
