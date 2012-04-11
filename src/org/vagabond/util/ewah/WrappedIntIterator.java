package org.vagabond.util.ewah;

import java.util.Iterator;

public class WrappedIntIterator implements Iterator<Integer> {

	private final IntIterator iter;
	
	public WrappedIntIterator (final IntIterator iter) {
		this.iter = iter;
	}
	
	public Integer next() {
		return new Integer(this.iter.next());
	}

	public boolean hasNext() {
		return this.iter.hasNext();
	}

	public void remove() {
		throw new UnsupportedOperationException("bitsets do not support remove");
	}
}
