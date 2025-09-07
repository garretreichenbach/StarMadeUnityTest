package org.schema.common.util;

import javax.vecmath.Vector3f;

/**
 * a circular FIFO buffer that is created with all
 * objects already instantiated.
 * add() returns the next object to be overwritten or
 * created.
 * 
 * Good runtime performance O(1) for adding and getting since no shifting 
 * or instantiation is done after creation.
 * 
 * Capacity cannot be changed. Size can only be reduced by removing the last element.
 * 
 * 
 * @author schema
 *
 * @param <E>
 */
public abstract class PersistentRingBuffer<E> {

	private int size;
	private final int capacity;
	private final E[] e;
	private int start = 0;
	public PersistentRingBuffer(int capacity) {
		super();
		this.capacity = capacity;
		e = create(capacity);
	}

	public abstract E[] create(int capacity);
	
	public int size(){
		return size;
	}
	
	public E add(){
		if(size < capacity){
			size++;
		}else{
			start = (start+1) % capacity;
		}
		return get(size-1);
	}
	
	public E get(int i){
		if(i < 0 || i >= size){
			throw new ArrayIndexOutOfBoundsException(i);
		}
		int index = (start+i)%capacity;
		return e[index];
	}
	
	public E pop(){
		E r = get(size-1);
		size--;
		return r;
	}
	
	public static void main(String[] asd){
		PersistentRingBuffer<Vector3f> test = new PersistentRingBuffer<Vector3f>(4) {

			@Override
			public Vector3f[] create(int capacity) {
				Vector3f s[] = new Vector3f[capacity];
				for(int i = 0; i < capacity; i++){
					s[i] = new Vector3f();
				}
				return s;
			}
			
		};
		
		
		test.add().set(1,1,1);
		
		System.err.println(":::: "+test);
		
		test.add().set(2,2,2);
		System.err.println(":::: "+test);
		
		test.add().set(3,3,3);
		System.err.println(":::: "+test);
		
		test.add().set(4,4,4);
		System.err.println(":::: "+test);
		
		test.add().set(5,5,5);
		System.err.println(":::: "+test);
		
		test.add().set(6,6,6);
		System.err.println(":::: "+test);
		
		test.add().set(7,7,7);
		System.err.println(":::: "+test);
		
		test.add().set(8,8,8);
		System.err.println(":::: "+test);
		
		test.add().set(9,9,9);
			
	}
	
	@Override
	public String toString(){
		StringBuffer d = new StringBuffer();
		d.append("{");
		for(int i = 0; i < size; i++){
			d.append(get(i)+(i < (size -1 ) ? ", " : ""));
		}
		d.append("}");
		return d.toString();
	}
}
