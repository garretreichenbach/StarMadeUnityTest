package org.schema.schine.network.objects.remote.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.schema.schine.network.objects.remote.Streamable;

import com.bulletphysics.util.ObjectArrayList;

/**
 * Object pool.
 *
 * @author jezek2
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PrimitiveBufferPool<T extends Streamable<?>> {

	private static ThreadLocal<Map> threadLocal = new ThreadLocal<Map>() {
		@Override
		protected Map initialValue() {
			return new HashMap();
		}
	};
	private ObjectArrayList<T> list = new ObjectArrayList<T>();
	private Constructor<T> constructor;

	public PrimitiveBufferPool(Class<T> cls) {
		try {
			constructor = cls.getConstructor(boolean.class);
		} catch (SecurityException e) {
			e.printStackTrace();
			assert (false);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			assert (false) : "Clazz " + cls;
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
	public static <T extends Streamable<?>> PrimitiveBufferPool<T> get(Class<T> cls) {
		Map map = threadLocal.get();

		PrimitiveBufferPool<T> pool = (PrimitiveBufferPool<T>) map.get(cls);
		if (pool == null) {
			pool = new PrimitiveBufferPool(cls);
			map.put(cls, pool);
		}

		return pool;
	}

	////////////////////////////////////////////////////////////////////////////

	private T create(boolean onServer) {
		try {
			return constructor.newInstance(onServer);
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
			return create(onServer);
		}
	}

	/**
	 * Release instance into pool.
	 *
	 * @param obj previously obtained instance from pool
	 */
	public void release(T obj) {
		obj.cleanAtRelease();
		list.add(obj);
	}

}
