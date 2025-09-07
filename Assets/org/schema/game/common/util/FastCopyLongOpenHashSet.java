package org.schema.game.common.util;

import org.schema.common.FastMath;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * this class is used to have an extension to quicky take a copy of a set and
 * put it into another without having to rehash after each add (as
 * addAll(otherSet)) would do
 * <p/>
 * This also reuses arrays if possible
 *
 * @author schema
 */
public class FastCopyLongOpenHashSet extends LongOpenHashSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */

	public FastCopyLongOpenHashSet() {
		super();
	}

	public FastCopyLongOpenHashSet(int expected) {
		super(expected, DEFAULT_LOAD_FACTOR);
		//bigger load factor (we dont care that much about access speed as that is threaded)
	}

	public static int maxFillFast(final int n, final float f) {
		return FastMath.fastCeil(n * f);
	}

	/**
	 * Returns the least power of two smaller than or equal to 2<sup>30</sup>
	 * and larger than or equal to <code>Math.ceil( expected / f )</code>.
	 *
	 * @param expected the expected number of elements in a hash table.
	 * @param f        the load factor.
	 * @return the minimum possible size for a backing array.
	 * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
	 */
	public static int arraySize(final int expected, final float f) {
		final long s = nextPowerOfTwo(FastMath.fastCeil(expected / f));
		return (int) s;
	}

	public static int nextPowerOfTwo(int x) {
		if (x == 0)
			return 1;
		x--;
		x |= x >> 1;
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		return (x | x >> 16) + 1;
	}

	public void deepApplianceCopy(FastCopyLongOpenHashSet copyFrom) {
		assert (f == copyFrom.f);
		mask = copyFrom.mask;
		maxFill = copyFrom.maxFill;
		n = copyFrom.n;
		size = copyFrom.size;
//		if (used.length < copyFrom.used.length) {
//			used = new boolean[copyFrom.used.length];
//		}
		if (key.length < copyFrom.key.length) {
			key = new long[copyFrom.key.length];
		}
//		System.arraycopy(copyFrom.used, 0, used, 0, copyFrom.used.length);
		System.arraycopy(copyFrom.key, 0, key, 0, copyFrom.key.length);
//		for (int i = 0; i < set.used.length; i++) {
//			used[i] = set.used[i];
//		}
//		for (int i = 0; i < set.key.length; i++) {
//			key[i] = set.key[i];
//		}
		// used = set.used;
		// key = set.key;

	}

	
	
}
