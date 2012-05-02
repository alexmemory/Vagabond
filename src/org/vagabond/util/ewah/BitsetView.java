package org.vagabond.util.ewah;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A read-only bitmap without separate storage. 
 * This bitmap wraps a real bitmap and allows access to a subsequence of the wrapped bitmaps bits. 
 * 
 * @author lord_pretzel
 *
 */
public class BitsetView implements Bitmap {

	private int start;
	private int end;
	private final IBitSet map;
	
	public BitsetView (IBitSet map, int start, int end) {
		this.map = map;
		this.start = start;
		this.end = end;
	}
	
	public BitsetView (BitsetView v, int start, int end) {
		this.map = v.map;
		this.start = v.start + start;
		this.end = v.start + end;
	}
	
	public BitsetView getView (int start, int end) {
		return new BitsetView(this, start, end);
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new WrappedIntIterator(this.intIterator());
	}

	@Override
	public List<Integer> getPositions() { //TODO do on RLW to be faster
		List<Integer> result = new ArrayList<Integer> ();
		IntIterator iter = this.intIterator();
		
		while(iter.hasNext())
			result.add(iter.next());
		
		return result;
	}

	@Override
	public boolean get(int bitpos) {
		return map.get(start + bitpos);
	}

	@Override
	public IntIterator intIterator() {
		return new IntIterator () {

			private IntIterator supIter = map.intIterator(start, end); 
			
			@Override
			public int next() {
				return supIter.next() - start;
			}

			@Override
			public boolean hasNext() {
				return supIter.hasNext();
			}
			
		};
	}

	@Override
	public int cardinality() { //TODO do on RLW to be faster
		int count = 0;
		
		IntIterator iter = this.intIterator();
		
		while(iter.hasNext()) {
			iter.next();
			count++;
		}
		
		return count;
	}

	@Override
	public int sizeInBits() {
		return end - start;
	}
	
	@Override
	public String toBitsString () { //TODO use RLWs directly
		StringBuffer buf = new StringBuffer();
		IntIterator iter = this.intIterator();
		int setbit = iter.next();
		int i;
		
		for(i = 0; i < sizeInBits() && setbit != -1; i++) {
			if (i != 0 && i % 8 == 0)
				buf.append(' ');
			if (i == setbit) {
				buf.append('1');
				if (iter.hasNext())
					setbit = iter.next();
				else
					setbit = -1;
			} 
			else {
				buf.append('0');
			}
		}
		for(; i < end - start; i++) {
			if (i != 0 && i % 8 == 0)
				buf.append(' ');
			buf.append('0');
		}
		
		return buf.toString();
	}

	protected IBitSet getMap() {
		return map;
	}

}
