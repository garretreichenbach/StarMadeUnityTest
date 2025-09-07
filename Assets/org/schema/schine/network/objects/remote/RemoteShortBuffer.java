package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.RemoteBufferInterface;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;

public class RemoteShortBuffer extends RemoteField<ShortArrayList> implements ShortList, RemoteBufferInterface {

	private final ShortArrayList receiveBuffer = new ShortArrayList();
	public int MAX_BATCH = RemoteBuffer.MAX_BATCH_SIZE;


	public RemoteShortBuffer(boolean onServer, int batch) {
		super(new ShortArrayList(), onServer);
		this.MAX_BATCH = batch;
	}

	public RemoteShortBuffer(NetworkObject synchOn, int batch) {
		super(new ShortArrayList(), synchOn);
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
			receiveBuffer.add(buffer.readShort());
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
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

		for (int i = 0; i < batchSize; i++) {
			short remoteField = get().getShort(i);

			buffer.writeShort(remoteField);
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
	public ShortArrayList getReceiveBuffer() {
		return receiveBuffer;
	}

	@Override
	public ShortListIterator iterator() {
		return get().iterator();
	}

	@Override
	public ShortListIterator listIterator() {
		return get().listIterator();
	}

	@Override
	public ShortListIterator listIterator(int index) {
		return get().listIterator(index);
	}

	@Override
	public ShortList subList(int fromIndex, int toIndex) {
		return get().subList(fromIndex, toIndex);
	}

	@Override
	public void size(int arg0) {
		get().size();
	}

	@Override
	public void getElements(int arg0, short[] arg1, int arg2, int arg3) {
		get().getElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public void removeElements(int arg0, int arg1) {
		get().removeElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, short[] arg1) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, short[] arg1, int arg2, int arg3) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1, arg2, arg3);
	}
	public boolean addCoord(short x, short y, short z) {
		boolean add = get().add(x) | get().add(y) | get().add(z);
		setChanged(add);
		observer.update(this);
		return add;
	}
	public boolean addCoord(short x, short y, short z, short w) {
		boolean add = get().add(x) | get().add(y) | get().add(z) | get().add(w);
		setChanged(add);
		observer.update(this);
		return add;
	}
	@Override
	public boolean add(short e) {
		boolean add = get().add(e);
		setChanged(add);
		observer.update(this);
		return add;
	}

	@Override
	public void add(int index, short element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public boolean addAll(int index, ShortCollection c) {

		boolean add = get().addAll(index, c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int arg0, ShortList arg1) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0, arg1);
	}

	@Override
	public boolean addAll(ShortList arg0) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0);
	}

	@Override
	public short getShort(int index) {
		return get().getShort(index);
	}

	@Override
	public int indexOf(short arg0) {
		return get().indexOf(arg0);
	}

	@Override
	public int lastIndexOf(short arg0) {
		return get().lastIndexOf(arg0);
	}

	@Override
	public short removeShort(int arg0) {
		return get().removeShort(arg0);
	}

	@Override
	public short set(int index, short element) {
		short e = get().set(index, element);
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
	public boolean add(Short e) {
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
	public boolean addAll(Collection<? extends Short> c) {
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
	public boolean addAll(int index, Collection<? extends Short> c) {

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
	public Short get(int index) {
		return get().get(index);
	}

	@Override
	public Short set(int index, Short element) {
		short e = get().set(index, element);
		setChanged(true);
		observer.update(this);
		return e;
	}

	@Override
	public void add(int index, Short element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public Short remove(int index) {
		short e = get().remove(index);
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
	public int compareTo(List<? extends Short> o) {
		return get().compareTo(o);
	}


	@Override
	public boolean contains(short arg0) {
		return get().contains(arg0);
	}

	@Override
	public short[] toShortArray() {
		return get().toShortArray();
	}

	@Override
	public short[] toShortArray(short[] arg0) {
		return get().toShortArray(arg0);
	}

	@Override
	public short[] toArray(short[] arg0) {
		return get().toArray(arg0);
	}

	@Override
	public boolean rem(short arg0) {
		return get().rem(arg0);
	}

	@Override
	public boolean addAll(ShortCollection c) {
		boolean add = get().addAll(c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean containsAll(ShortCollection arg0) {
		return get().containsAll(arg0);
	}

	@Override
	public boolean removeAll(ShortCollection arg0) {
		return get().removeAll(arg0);
	}

	@Override
	public boolean retainAll(ShortCollection arg0) {
		return get().retainAll(arg0);
	}

}
