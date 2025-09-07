package org.schema.game.client.controller.element.world;

import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Created by Jordan on 11/04/2017.
 */
public class SegmentSortArray {

	protected int elements;

	protected long[] longArray;
	protected int[] intArray;
	protected float[] floatArray;

	protected Int2ObjectOpenHashMap<LongOpenHashSet> longHashSet;

	public SegmentSortArray(){
		this(16);
	}

	public SegmentSortArray(int capacity){
		elements = 0;

		longArray = new long[capacity];
		intArray = new int[capacity];
		floatArray = new float[capacity];

		longHashSet = new Int2ObjectOpenHashMap<LongOpenHashSet>(capacity);
	}

	public void enqueue(long _long, int _int, float distance) {

		int newLength = elements+1;
		longArray = LongArrays.grow(longArray, newLength);
		intArray = IntArrays.grow(intArray, newLength);
		floatArray = FloatArrays.grow(floatArray, newLength);

		int i = elements;

		// Check for highest position that is closer than the new entry
		while (--i >= 0 && floatArray[i] > distance){}
		i++; // Go after that position

		// Copy existing elements from that position and move them up an index to make room for new entry
		int copyCount = elements - i;

		if (copyCount > 0) {
			System.arraycopy(longArray, i, longArray, i+1, copyCount);
			System.arraycopy(intArray, i, intArray, i+1, copyCount);
			System.arraycopy(floatArray, i, floatArray, i+1, copyCount);
		}

		// Place new entry
		longArray[i] = _long;
		intArray[i] = _int;
		floatArray[i] = distance;

		LongOpenHashSet hashSet = longHashSet.get(_int);
		if(hashSet == null){
			hashSet = new LongOpenHashSet();
			longHashSet.put(_int, hashSet);
		}
		hashSet.add(_long);

		elements++;
	}

	public long dequeue(){

		if (elements == 0)
			return 0;

		long l = longArray[0];
		int _int = peekInt();
		
		if (--elements > 0) {
			System.arraycopy(longArray, 1, longArray, 0, elements);
			System.arraycopy(intArray, 1, intArray, 0, elements);
			System.arraycopy(floatArray, 1, floatArray, 0, elements);
		}

		LongOpenHashSet longOpenHashSet = longHashSet.get(_int);
		if(longOpenHashSet != null){
			longOpenHashSet.remove(l);
			if(longOpenHashSet.isEmpty()){
				longHashSet.remove(_int);
			}
		}

		return l;
	}

	public int peekInt(){
		if (elements > 0)
			return intArray[0];

		return 0;
	}

	public int size(){
		return elements;
	}

	public boolean contains(int i, long l){
		LongOpenHashSet longOpenHashSet = longHashSet.get(i);
		return longOpenHashSet != null && longOpenHashSet.contains(l);
	}

}
