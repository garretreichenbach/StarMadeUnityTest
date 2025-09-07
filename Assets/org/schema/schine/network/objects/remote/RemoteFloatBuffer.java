package org.schema.schine.network.objects.remote;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.RemoteBufferInterface;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;

public class RemoteFloatBuffer extends RemoteField<FloatArrayList> implements FloatList, RemoteBufferInterface {

	private final FloatArrayList receiveBuffer = new FloatArrayList();
	public int MAX_BATCH = RemoteBuffer.MAX_BATCH_SIZE;

	public RemoteFloatBuffer(boolean onServer) {
		super(new FloatArrayList(), onServer);
	}

	public RemoteFloatBuffer(boolean onServer, int batch) {
		super(new FloatArrayList(), onServer);
		this.MAX_BATCH = batch;
	}

	public RemoteFloatBuffer(NetworkObject synchOn) {
		super(new FloatArrayList(), synchOn);
	}

	public RemoteFloatBuffer(NetworkObject synchOn, int batch) {
		super(new FloatArrayList(), synchOn);
		this.MAX_BATCH = batch;
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT; // all fields plus size
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
			receiveBuffer.add(buffer.readFloat());
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		final int batchSize = Math.min(MAX_BATCH, get().size());

		if (MAX_BATCH < Byte.MAX_VALUE) {
			assert (batchSize < Byte.MAX_VALUE);
			buffer.writeByte(batchSize);
		} else if (MAX_BATCH < Short.MAX_VALUE) {
			assert (batchSize < Short.MAX_VALUE);
			buffer.writeShort(batchSize);
		} else {
			buffer.writeInt(batchSize);
		}

		for (int i = 0; i < batchSize; i++) {
			buffer.writeFloat(get().getFloat(i));
		}
		get().removeElements(0, batchSize);
		keepChanged = !get().isEmpty();

		return ByteUtil.SIZEOF_INT;

	}

	@Override
	public void clearReceiveBuffer() {
		receiveBuffer.clear();
	}

	/**
	 * @return the receiveBuffer
	 */
	public FloatArrayList getReceiveBuffer() {
		return receiveBuffer;
	}

	@Override
	public FloatListIterator iterator() {
		return get().iterator();
	}


	@Override
	public FloatListIterator listIterator() {
		return get().listIterator();
	}

	@Override
	public FloatListIterator listIterator(int index) {
		return get().listIterator(index);
	}

	@Override
	public FloatList subList(int fromIndex, int toIndex) {
		return get().subList(fromIndex, toIndex);
	}

	@Override
	public void size(int arg0) {
		get().size();
	}

	@Override
	public void getElements(int arg0, float[] arg1, int arg2, int arg3) {
		get().getElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public void removeElements(int arg0, int arg1) {
		get().removeElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, float[] arg1) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, float[] arg1, int arg2, int arg3) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public boolean add(float e) {
		boolean add = get().add(e);
		setChanged(add);
		observer.update(this);
		return add;
	}

	@Override
	public void add(int index, float element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public boolean addAll(int index, FloatCollection c) {

		boolean add = get().addAll(index, c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int arg0, FloatList arg1) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0, arg1);
	}

	@Override
	public boolean addAll(FloatList arg0) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0);
	}

	@Override
	public float getFloat(int index) {
		return get().getFloat(index);
	}

	@Override
	public int indexOf(float arg0) {
		return get().indexOf(arg0);
	}

	@Override
	public int lastIndexOf(float arg0) {
		return get().lastIndexOf(arg0);
	}

	@Override
	public float removeFloat(int arg0) {
		return get().removeFloat(arg0);
	}

	@Override
	public float set(int index, float element) {
		float e = get().set(index, element);
		setChanged(true);
		observer.update(this);
		return e;
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
	public Object[] toArray() {
		return get().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return get().toArray(a);
	}

	@Override
	public boolean add(Float e) {
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
	public boolean addAll(Collection<? extends Float> c) {
		boolean add = get().addAll(c);
		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
		//			}
		//		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends Float> c) {

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
	public Float get(int index) {
		return get().get(index);
	}

	@Override
	public Float set(int index, Float element) {
		float e = get().set(index, element);
		setChanged(true);
		observer.update(this);
		return e;
	}

	@Override
	public void add(int index, Float element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public Float remove(int index) {
		float e = get().remove(index);
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

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteField#toString()
	 */
	@Override
	public String toString() {
		return "(" + getClass().toString() + ": HOLD: " + get() + "; RECEIVED: " + receiveBuffer + ")";
	}

	@Override
	public int compareTo(List<? extends Float> o) {
		return get().compareTo(o);
	}


	@Override
	public boolean contains(float arg0) {
		return get().contains(arg0);
	}

	@Override
	public float[] toFloatArray() {
		return get().toFloatArray();
	}

	@Override
	public float[] toFloatArray(float[] arg0) {
		return get().toFloatArray(arg0);
	}

	@Override
	public float[] toArray(float[] arg0) {
		return get().toArray(arg0);
	}

	@Override
	public boolean rem(float arg0) {
		return get().rem(arg0);
	}

	@Override
	public boolean addAll(FloatCollection c) {
		boolean add = get().addAll(c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean containsAll(FloatCollection arg0) {
		return get().containsAll(arg0);
	}

	@Override
	public boolean removeAll(FloatCollection arg0) {
		return get().removeAll(arg0);
	}

	@Override
	public boolean retainAll(FloatCollection arg0) {
		return get().retainAll(arg0);
	}

}
