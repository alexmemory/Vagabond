package org.vagabond.util;

public class IntPair {

	public int left;
	public int right;
	
	public IntPair (int left, int right) {
		this.left = left;
		this.right = right;
	}
	
	@Override
	public int hashCode () {
		return left * 13 + right;
	}
	
	@Override
	public boolean equals (Object o) {
		if (o == null)
			return false;
		
		if (this == o)
			return true;
		
		if (o instanceof IntPair) {
			IntPair other = (IntPair) o;
			return this.left == other.left 
					&& this.right == other.right;
		}
		
		return false;
	}
}
