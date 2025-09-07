package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.RemoteBufferInterface;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;

public class RemoteLongBuffer extends RemoteField<LongArrayList> implements LongList, RemoteBufferInterface {

	private final LongArrayList receiveBuffer = new LongArrayList();
	public int MAX_BATCH = RemoteBuffer.MAX_BATCH_SIZE;

	public RemoteLongBuffer(boolean onServer) {
		super(new LongArrayList(), onServer);
	}

	public RemoteLongBuffer(boolean onServer, int batch) {
		super(new LongArrayList(), onServer);
		this.MAX_BATCH = batch;
	}

	public RemoteLongBuffer(NetworkObject synchOn) {
		super(new LongArrayList(), synchOn);
	}

	public RemoteLongBuffer(NetworkObject synchOn, int batch) {
		super(new LongArrayList(), synchOn);
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
			receiveBuffer.add(buffer.readLong());
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
			buffer.writeLong(get().getLong(i));
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
	public LongArrayList getReceiveBuffer() {
		return receiveBuffer;
	}

	@Override
	public LongListIterator iterator() {
		return get().iterator();
	}


	@Override
	public LongListIterator listIterator() {
		return get().listIterator();
	}

	@Override
	public LongListIterator listIterator(int index) {
		return get().listIterator(index);
	}


	@Override
	public LongList subList(int fromIndex, int toIndex) {
		return get().subList(fromIndex, toIndex);
	}

	@Override
	public void size(int arg0) {
		get().size();
	}

	@Override
	public void getElements(int arg0, long[] arg1, int arg2, int arg3) {
		get().getElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public void removeElements(int arg0, int arg1) {
		get().removeElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, long[] arg1) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, long[] arg1, int arg2, int arg3) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public boolean add(long e) {
		boolean add = get().add(e);
		setChanged(add);
		observer.update(this);
		return add;
	}

	@Override
	public void add(int index, long element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public boolean addAll(int index, LongCollection c) {

		boolean add = get().addAll(index, c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int arg0, LongList arg1) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0, arg1);
	}

	@Override
	public boolean addAll(LongList arg0) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0);
	}

	@Override
	public long getLong(int index) {
		return get().getLong(index);
	}

	@Override
	public int indexOf(long arg0) {
		return get().indexOf(arg0);
	}

	@Override
	public int lastIndexOf(long arg0) {
		return get().lastIndexOf(arg0);
	}

	@Override
	public long removeLong(int arg0) {
		return get().removeLong(arg0);
	}

	@Override
	public long set(int index, long element) {
		long e = get().set(index, element);
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
	public boolean add(Long e) {
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
	public boolean addAll(Collection<? extends Long> c) {
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
	public boolean addAll(int index, Collection<? extends Long> c) {

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
	public Long get(int index) {
		return get().get(index);
	}

	@Override
	public Long set(int index, Long element) {
		long e = get().set(index, element);
		setChanged(true);
		observer.update(this);
		return e;
	}

	@Override
	public void add(int index, Long element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public Long remove(int index) {
		long e = get().remove(index);
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
	public int compareTo(List<? extends Long> o) {
		return get().compareTo(o);
	}


	@Override
	public boolean contains(long arg0) {
		return get().contains(arg0);
	}

	@Override
	public long[] toLongArray() {
		return get().toLongArray();
	}

	@Override
	public long[] toLongArray(long[] arg0) {
		return get().toLongArray(arg0);
	}

	@Override
	public long[] toArray(long[] arg0) {
		return get().toArray(arg0);
	}

	@Override
	public boolean rem(long arg0) {
		return get().rem(arg0);
	}

	@Override
	public boolean addAll(LongCollection c) {
		boolean add = get().addAll(c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean containsAll(LongCollection arg0) {
		return get().containsAll(arg0);
	}

	@Override
	public boolean removeAll(LongCollection arg0) {
		return get().removeAll(arg0);
	}

	@Override
	public boolean retainAll(LongCollection arg0) {
		return get().retainAll(arg0);
	}

}
