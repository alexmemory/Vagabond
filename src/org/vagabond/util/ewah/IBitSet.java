package org.vagabond.util.ewah;

import java.util.Iterator;

public interface IBitSet extends Bitmap, Cloneable {

	public enum BitsetType {
		JavaBitSet,
		EWAHBitSet
	}
	
	public boolean get (int position);
	public void set (int position);
	
	public void clear();
	
	public boolean intersects (IBitSet other);
	public IBitSet and (IBitSet other);
	public IBitSet or (IBitSet other);
	public void not();
	public Object clone();
	
	public int getByteSize();
	
	public String toBitsString ();
	public void readFromBitsString (String values);
	
	// iterator methods
	public IntIterator intIterator ();
	public IntIterator intIterator (int start, int end);
	public Iterator<Integer> iterator ();
	public Iterator<Integer> iterator (int start, int end);
	
}
