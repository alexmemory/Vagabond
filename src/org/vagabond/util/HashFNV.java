package org.vagabond.util;

public class HashFNV {

	public static final int FNV1_32_INIT  = 0x811c9dc5;

	public static int fnv(final int[] values) {
		int seed = FNV1_32_INIT;
		for(int i = 0; i < values.length; i++)
			seed = fnv(values[i], seed);
		
		return seed;
	}
	
	public static int fnv(final Object o, int seed) {
		return fnv(o.hashCode(), seed);
	}
	
	public static int fnv(final Object o) {
		return fnv(o.hashCode());
	}
	
	public static int fnv(final String s) {
		return fnv(s.getBytes(), FNV1_32_INIT);
	}
	
	public static int fnv(final int value) {
		return fnv(value, FNV1_32_INIT);
	}
	
	public static int fnv(final int value, int seed) {
		byte[] bytes = new byte[] {
	                (byte)(value >>> 24),
	                (byte)(value >>> 16),
	                (byte)(value >>> 8),
	                (byte)value};
		return fnv(bytes, seed);
	}
	
	public static int fnv(final byte[] buf, int seed) {
		for (int i = 0; i < buf.length; i++) {
			seed ^= buf[i];
			seed += (seed << 1) + (seed << 4) + (seed << 7) + (seed << 8) + (seed << 24);
		}
		return seed;
	}
}


