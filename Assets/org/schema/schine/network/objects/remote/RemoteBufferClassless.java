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

public class RemoteBufferClassless<E extends Streamable<?>>
		extends RemoteField<ObjectArrayList<E>> implements List<E>, RemoteBufferInterface {

	public int MAX_BATCH = RemoteBuffer.MAX_BATCH_SIZE;
	protected ObjectArrayList<E> receiveBuffer;
	private PrimitiveBufferPool<E> pool;

	public RemoteBufferClassless(boolean onServer) {
		super(new ObjectArrayList<E>(), false, onServer);

		receiveBuffer = new ObjectArrayList<E>();
	}

	public RemoteBufferClassless(boolean onServer, int batch) {
		super(new ObjectArrayList<E>(), false, onServer);
		this.MAX_BATCH = batch;

		receiveBuffer = new ObjectArrayList<E>();
	}

	public RemoteBufferClassless(NetworkObject synchOn) {
		super(new ObjectArrayList<E>(), false, synchOn);

		receiveBuffer = new ObjectArrayList<E>();
	}

	public RemoteBufferClassless(NetworkObject synchOn, int batch) {
		super(new ObjectArrayList<E>(), false, synchOn);
		this.MAX_BATCH = batch;

		receiveBuffer = new ObjectArrayList<E>();
	}

	@Override
	public int byteLength() {
		int i = 0;
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
			Streamable<?> remoteField = get().remove(0);

			size += remoteField.toByteStream(buffer);
			remoteField.setChanged(false);
		}
		keepChanged = !get().isEmpty();

		return size + ByteUtil.SIZEOF_INT;
		//		}

	}

	@Override
	public void clearReceiveBuffer() {
		for (int i = 0; i < receiveBuffer.size(); i++) {
			assert (receiveBuffer != null) : "ReceiveBuffer null";
			assert (receiveBuffer.get(i) != null) : "element null " + i;
			pool.release(receiveBuffer.get(i));
		}
		receiveBuffer.clear();
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
		//		}
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
		return "(" + getClass().toString() + ": HOLD: " + get() + "; RECEIVED: " + receiveBuffer + ")";
	}

}
