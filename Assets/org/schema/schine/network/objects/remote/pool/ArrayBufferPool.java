package org.schema.schine.network.objects.remote.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.schema.schine.network.objects.remote.StreamableArray;

import com.bulletphysics.util.ObjectArrayList;

/**
 * Object pool.
 *
 * @author jezek2
 */
@SuppressWarnings("unused")
public class ArrayBufferPool<T extends StreamableArray<?>> {

	@SuppressWarnings("rawtypes")
	private static ThreadLocal<Map> threadLocal = new ThreadLocal<Map>() {

		@Override
		protected Map initialValue() {
			return new HashMap();
		}
	};
	private ObjectArrayList<T> list = new ObjectArrayList<T>();
	private Constructor<T> constructor;
	private Integer size;

	public ArrayBufferPool(Class<T> cls) {
		try {
			constructor = cls.getConstructor(int.class, boolean.class);
		} catch (SecurityException e) {
			e.printStackTrace();
			assert (false);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			assert (false);
		}
	}

	public static void cleanCurrentThread() {
		threadLocal.remove();
	}

	/**
	 * Returns per-thread object pool for given type, or create one if it doesn't exist.
	 *
	 * @param cls type
	 * @return object pool
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T extends StreamableArray<?>> ArrayBufferPool<T> get(Class<T> cls, Integer size) {
		Map map = threadLocal.get();

		Map subMap = ((Map) map.get(cls));

		if (subMap == null) {
			subMap = new HashMap();
			map.put(cls, subMap);
		}

		ArrayBufferPool<T> pool = (ArrayBufferPool<T>) subMap.get(size);
		if (pool == null) {
			pool = new ArrayBufferPool(cls);
			pool.size = size;

			subMap.put(size, pool);
		}

		return pool;
	}

	////////////////////////////////////////////////////////////////////////////

	private T create(Integer i, boolean o) {
		try {
			return constructor.newInstance(i, o);
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns instance from pool, or create one if pool is empty.
	 *
	 * @return instance
	 */
	public T get(boolean onServer) {
		if (list.size() > 0) {
			T remove = list.remove(list.size() - 1);
			//			remove.synchObject = o;
			return remove;
		} else {

			return create(size, onServer);
		}
	}

	/**
	 * Release instance into pool.
	 *
	 * @param obj previously obtained instance from pool
	 */
	public void release(T obj) {
		assert (((StreamableArray<?>) obj).arrayLength() == size) : ((StreamableArray<?>) obj).arrayLength() + " / " + size;
		//		System.err.println("RELEASED: "+((RemoteArray)obj).arrayLength()+" // "+ obj.getClass().getSimpleName()+ " INTO: "+size+" // "+cls.getSimpleName());
		obj.cleanAtRelease();
		list.add(obj);
	}

}
