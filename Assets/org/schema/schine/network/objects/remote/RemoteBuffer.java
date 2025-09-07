package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.RemoteBufferInterface;
import org.schema.schine.network.objects.remote.pool.PrimitiveBufferPool;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoteBuffer<E extends Streamable<?>>
		extends RemoteField<ObjectArrayList<E>> implements List<E>, RemoteBufferInterface {

	public static final int MAX_BATCH_SIZE = 16;
	public Class<E> clazz;
	public int MAX_BATCH = MAX_BATCH_SIZE;
	protected ObjectArrayList<E> receiveBuffer;
	private PrimitiveBufferPool<E> pool;

	public RemoteBuffer(Class<E> clazz, boolean onServer) {
		super(new ObjectArrayList<E>(MAX_BATCH_SIZE), false, onServer);
		this.clazz = clazz;

		receiveBuffer = new ObjectArrayList<E>(MAX_BATCH);
		cacheConstructor();
	}

	public RemoteBuffer(Class<E> clazz, boolean onServer, int batch) {
		super(new ObjectArrayList<E>(batch), false, onServer);
		this.MAX_BATCH = batch;
		this.clazz = clazz;

		receiveBuffer = new ObjectArrayList<E>(batch);
		cacheConstructor();
	}

	public RemoteBuffer(Class<E> clazz, NetworkObject synchOn) {
		super(new ObjectArrayList<E>(MAX_BATCH_SIZE), false, synchOn);
		this.clazz = clazz;

		receiveBuffer = new ObjectArrayList<E>(MAX_BATCH);
		cacheConstructor();
	}

	public RemoteBuffer(Class<E> clazz, NetworkObject synchOn, int batch) {
		super(new ObjectArrayList<E>(batch), false, synchOn);
		this.MAX_BATCH = batch;
		this.clazz = clazz;

		receiveBuffer = new ObjectArrayList<E>(MAX_BATCH);
		cacheConstructor();
	}

	@Override
	public int byteLength() {
		int i = 0;
		//		for(E e : get()){
		//			i += e.byteLength();
		//		}
		return i + ByteUtil.SIZEOF_INT; // all fields plus size
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize;

		if (MAX_BATCH < Byte.MAX_VALUE) {
			collectionSize = buffer.readByte();
		} else if (MAX_BATCH < Short.MAX_VALUE) {
			collectionSize = buffer.readShort();
		} else {
			collectionSize = buffer.readInt();
		}

		for (int n = 0; n < collectionSize; n++) {
			E instance = pool.get(onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			receiveBuffer.add(instance);
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		int batchSize = Math.min(MAX_BATCH, get().size());

		if (MAX_BATCH < Byte.MAX_VALUE) {
			assert (batchSize < Byte.MAX_VALUE);
			buffer.writeByte(batchSize);
		} else if (MAX_BATCH < Short.MAX_VALUE) {
			assert (batchSize < Short.MAX_VALUE);
			buffer.writeShort(batchSize);
		} else {
			buffer.writeInt(batchSize);
		}

		int size = 0;
		for (int i = 0; i < batchSize; i++) {
			Streamable<?> remoteField = get().get(i);
			size += remoteField.toByteStream(buffer);
			remoteField.setChanged(false);
		}
		get().removeElements(0, batchSize);
		keepChanged = !get().isEmpty();

		return size + ByteUtil.SIZEOF_INT;

	}

	protected void cacheConstructor() {

		pool = PrimitiveBufferPool.get(clazz);
		assert (pool != null) : " pool is null for " + clazz;
	}

	@Override
	public void clearReceiveBuffer() {
		int size = getReceiveBuffer().size();
		for (int i = 0; i < size; i++) {
			assert (getReceiveBuffer() != null) : "ReceiveBuffer null";
			assert (pool != null) : "pool null for " + clazz;
			assert (getReceiveBuffer().get(i) != null) : "element null " + i;
			pool.release(getReceiveBuffer().get(i));
		}
		getReceiveBuffer().clear();
	}

	/**
	 * @return the receiveBuffer
	 */
	public ObjectArrayList<E> getReceiveBuffer() {
		return receiveBuffer;
	}

	/**
	 * @param receiveBuffer the receiveBuffer to set
	 */
	public void setReceiveBuffer(ObjectArrayList<E> receiveBuffer) {
		this.receiveBuffer = receiveBuffer;
	}

	@Override
	public int size() {
		return get().size();
	}

	@Override
	public boolean isEmpty() {
		return get().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return get().contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return get().iterator();
	}

	@Override
	public Object[] toArray() {
		return get().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return get().toArray(a);
	}

	@Override
	public boolean add(E e) {
		boolean add = get().add(e);
		setChanged(add);

		assert (observer != null) : "the ntField is probably not public!";
		observer.update(this);
		return add;
	}

	@Override
	public boolean remove(Object o) {
		boolean e = get().remove(o);
		return e;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return get().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean add = get().addAll(c);
		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean add = get().addAll(index, c);
		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean e = get().removeAll(c);
		return e;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean e = get().retainAll(c);
		return e;
	}

	@Override
	public void clear() {
		get().clear();
	}

	@Override
	public E get(int index) {
		return get().get(index);
	}

	@Override
	public E set(int index, E element) {
		E e = get().set(index, element);
		setChanged(true);
		observer.update(this);
		return e;
	}

	@Override
	public void add(int index, E element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public E remove(int index) {
		E e = get().remove(index);
		return e;
	}

	@Override
	public int indexOf(Object o) {
		return get().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return get().lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return get().listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return get().listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return get().subList(fromIndex, toIndex);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteField#toString()
	 */
	@Override
	public String toString() {
		return "(" + getClass().toString() + ": HOLD: " + get() + "; RECEIVED: " + getReceiveBuffer() + ")";
	}

}
