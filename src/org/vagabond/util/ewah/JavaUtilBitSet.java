package org.vagabond.util.ewah;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.util.LoggerUtil;

public class JavaUtilBitSet extends BitSet implements IBitSet {

	static Logger log = Logger.getLogger(JavaUtilBitSet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 7635135378292182022L;

	/**
	 * 
	 */

	public JavaUtilBitSet () {
		super();
	}
	
	public JavaUtilBitSet (int size) {
		super(size);
	}
	
	
	@Override
	public boolean intersects(IBitSet other) {
		if (!(other instanceof BitSet))
			throw new ClassCastException();
		return this.intersects((BitSet) other);
	}

	@Override
	public IBitSet and(IBitSet other) {
		BitSet newSet;
		if (!(other instanceof BitSet))
			throw new ClassCastException();
		
		newSet = (BitSet) this.clone();
		newSet.and((BitSet) other);
		
		return (JavaUtilBitSet) newSet;
	}

	@Override
	public IBitSet or(IBitSet other) {
		BitSet newSet;
		if (!(other instanceof BitSet))
			throw new ClassCastException();
		
		newSet = (BitSet) this.clone();
		newSet.or((BitSet) other);
		
		return (JavaUtilBitSet) newSet;
	}

	@Override
	public void not() {
		for(int i = 0; i < this.size(); i++)
			this.flip(i);
	}
	
	@Override
	public IBitSet andNot(IBitSet other) {
		BitSet newSet;
		if (!(other instanceof BitSet))
			throw new ClassCastException();
		
		newSet = (BitSet) this.clone();
		newSet.andNot((BitSet) other);
		
		return (JavaUtilBitSet) newSet;
	}


	@Override
	public String toBitsString () {
		StringBuffer result = new StringBuffer();
		
		for(int i = 0; i < size(); i++) {
			if (get(i))
				result.append('1');
			else
				result.append('0');
			if (i !=0 && i % 8 == 0)
				result.append(' ');
		}
		return result.toString();
	}

	@Override
	public int getByteSize() {
		Field wordsField;
		try {
			wordsField = BitSet.class.getDeclaredField("words");
			wordsField.setAccessible(true);
			long[] myWords = (long[]) wordsField.get(this);
			return myWords.length * Long.SIZE / 8 ;
		} catch (SecurityException e) {
			LoggerUtil.logException(e, log);
		} catch (NoSuchFieldException e) {
			LoggerUtil.logException(e, log);
		} catch (IllegalArgumentException e) {
			LoggerUtil.logException(e, log);
		} catch (IllegalAccessException e) {
			LoggerUtil.logException(e, log);
		}
		
		return 0;
	}

	@Override
	public void readFromBitsString(String values) {
		int pos = 0;
		
		this.clear();
		for(char c: values.toCharArray()) {
			switch(c) {
				case '0':
					pos++;
					break;
				case '1':
					set(pos++);
					break;
				default:
					break;
			}
		}		
	}

	@Override
	public IntIterator intIterator() {
		return new IntIterator () {

			int pos = -1;
			
			@Override
			public boolean hasNext() {
				return nextSetBit(pos + 1) != -1;
			}

			@Override
			public int next() {
				pos = nextSetBit(pos + 1);
				return pos;
			}
			
		};
	}

	@Override
	public IntIterator intIterator(final int start, final int end) {
		return new IntIterator () {

			int pos = start - 1;
			
			@Override
			public boolean hasNext() {
				int next = nextSetBit(pos + 1); 
				return next != -1 && next < end;
			}

			@Override
			public int next() {
				pos = nextSetBit(pos + 1);
				return pos;
			}
			
		};
	}

	@Override
	public Iterator<Integer> iterator() {
		return new WrappedIntIterator(intIterator());
	}

	@Override
	public Iterator<Integer> iterator(final int start, final int end) {
		return new WrappedIntIterator(intIterator(start, end));
	}

	@Override
	public List<Integer> getPositions() {
		ArrayList<Integer> result = new ArrayList<Integer> ();
		IntIterator iter = intIterator();
		
		while(iter.hasNext())
			result.add(iter.next());
		
		return result;
	}

	@Override
	public int sizeInBits() {
		return super.size();
	}
	
	@Override
	public Object clone() {
		return super.clone();
	}


}
