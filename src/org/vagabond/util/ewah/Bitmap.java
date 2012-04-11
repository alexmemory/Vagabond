package org.vagabond.util.ewah;

import java.util.List;

public interface Bitmap extends Iterable<Integer> {

	public List<Integer> getPositions();
	public boolean get (int bitpos);
	public IntIterator intIterator();
	public int cardinality();
	public int sizeInBits();
	public String toBitsString ();
	
}
