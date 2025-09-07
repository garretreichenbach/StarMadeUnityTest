package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.RemoteBufferInterface;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

public class RemoteIntBuffer extends RemoteField<IntArrayList> implements IntList, RemoteBufferInterface {

	private final IntArrayList receiveBuffer = new IntArrayList();
	public int MAX_BATCH = RemoteBuffer.MAX_BATCH_SIZE;

	public RemoteIntBuffer(boolean onServer) {
		super(new IntArrayList(), onServer);
	}

	public RemoteIntBuffer(boolean onServer, int batch) {
		super(new IntArrayList(), onServer);
		this.MAX_BATCH = batch;
	}

	public RemoteIntBuffer(NetworkObject synchOn) {
		super(new IntArrayList(), synchOn);
	}

	public RemoteIntBuffer(NetworkObject synchOn, int batch) {
		super(new IntArrayList(), synchOn);
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
			receiveBuffer.add(buffer.readInt());
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
			int remoteField = get().getInt(i);

			buffer.writeInt(remoteField);
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
	public IntArrayList getReceiveBuffer() {
		return receiveBuffer;
	}

	@Override
	public IntListIterator iterator() {
		return get().iterator();
	}



	@Override
	public IntListIterator listIterator() {
		return get().listIterator();
	}

	@Override
	public IntListIterator listIterator(int index) {
		return get().listIterator(index);
	}

	@Override
	public IntList subList(int fromIndex, int toIndex) {
		return get().subList(fromIndex, toIndex);
	}

	@Override
	public void size(int arg0) {
		get().size();
	}

	@Override
	public void getElements(int arg0, int[] arg1, int arg2, int arg3) {
		get().getElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public void removeElements(int arg0, int arg1) {
		get().removeElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, int[] arg1) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, int[] arg1, int arg2, int arg3) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public boolean add(int e) {
		boolean add = get().add(e);
		setChanged(add);
		observer.update(this);
		return add;
	}

	@Override
	public void add(int index, int element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public boolean addAll(int index, IntCollection c) {

		boolean add = get().addAll(index, c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int arg0, IntList arg1) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0, arg1);
	}

	@Override
	public boolean addAll(IntList arg0) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0);
	}

	@Override
	public int getInt(int index) {
		return get().getInt(index);
	}

	@Override
	public int indexOf(int arg0) {
		return get().indexOf(arg0);
	}

	@Override
	public int lastIndexOf(int arg0) {
		return get().lastIndexOf(arg0);
	}

	@Override
	public int removeInt(int arg0) {
		return get().removeInt(arg0);
	}

	@Override
	public int set(int index, int element) {
		int e = get().set(index, element);
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
	public boolean add(Integer e) {
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
	public boolean addAll(Collection<? extends Integer> c) {
		boolean add = get().addAll(c);
		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Integer> c) {
		boolean add = get().addAll(index, c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
		//			}
		//		}
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
	public Integer get(int index) {
		return get().get(index);
	}

	@Override
	public Integer set(int index, Integer element) {
		int e = get().set(index, element);
		setChanged(true);
		observer.update(this);
		return e;
	}

	@Override
	public void add(int index, Integer element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public Integer remove(int index) {
		int e = get().remove(index);
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
	public int compareTo(List<? extends Integer> o) {
		return get().compareTo(o);
	}


	@Override
	public boolean contains(int arg0) {
		return get().contains(arg0);
	}

	@Override
	public int[] toIntArray() {
		return get().toIntArray();
	}

	@Override
	public int[] toIntArray(int[] arg0) {
		return get().toIntArray(arg0);
	}

	@Override
	public int[] toArray(int[] arg0) {
		return get().toArray(arg0);
	}

	@Override
	public boolean rem(int arg0) {
		return get().rem(arg0);
	}

	@Override
	public boolean addAll(IntCollection c) {
		boolean add = get().addAll(c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean containsAll(IntCollection arg0) {
		return get().containsAll(arg0);
	}

	@Override
	public boolean removeAll(IntCollection arg0) {
		return get().removeAll(arg0);
	}

	@Override
	public boolean retainAll(IntCollection arg0) {
		return get().retainAll(arg0);
	}

}
