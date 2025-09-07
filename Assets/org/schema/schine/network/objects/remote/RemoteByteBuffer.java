package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.RemoteBufferInterface;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;

public class RemoteByteBuffer extends RemoteField<ByteArrayList> implements ByteList, RemoteBufferInterface {

	private final ByteArrayList receiveBuffer = new ByteArrayList();
	public int MAX_BATCH = RemoteBuffer.MAX_BATCH_SIZE;

	public RemoteByteBuffer(boolean onServer) {
		super(new ByteArrayList(), onServer);
	}

	public RemoteByteBuffer(boolean onServer, int batch) {
		super(new ByteArrayList(), onServer);
		this.MAX_BATCH = batch;
	}

	public RemoteByteBuffer(NetworkObject synchOn) {
		super(new ByteArrayList(), synchOn);
	}

	public RemoteByteBuffer(NetworkObject synchOn, int batch) {
		super(new ByteArrayList(), synchOn);
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
			receiveBuffer.add(buffer.readByte());
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
			byte remoteField = get().getByte(i);

			buffer.writeByte(remoteField);
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
	public ByteArrayList getReceiveBuffer() {
		return receiveBuffer;
	}

	@Override
	public ByteListIterator iterator() {
		return get().iterator();
	}

	@Override
	public ByteListIterator listIterator() {
		return get().listIterator();
	}

	@Override
	public ByteListIterator listIterator(int index) {
		return get().listIterator(index);
	}


	@Override
	public ByteList subList(int fromIndex, int toIndex) {
		return get().subList(fromIndex, toIndex);
	}

	@Override
	public void size(int arg0) {
		get().size();
	}

	@Override
	public void getElements(int arg0, byte[] arg1, int arg2, int arg3) {
		get().getElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public void removeElements(int arg0, int arg1) {
		get().removeElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, byte[] arg1) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1);
	}

	@Override
	public void addElements(int arg0, byte[] arg1, int arg2, int arg3) {
		setChanged(true);
		observer.update(this);
		get().addElements(arg0, arg1, arg2, arg3);
	}

	@Override
	public boolean add(byte e) {
		boolean add = get().add(e);
		setChanged(add);
		observer.update(this);
		return add;
	}

	@Override
	public void add(int index, byte element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public boolean addAll(int index, ByteCollection c) {

		boolean add = get().addAll(index, c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int arg0, ByteList arg1) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0, arg1);
	}

	@Override
	public boolean addAll(ByteList arg0) {
		setChanged(true);
		observer.update(this);
		return get().addAll(arg0);
	}

	@Override
	public byte getByte(int index) {
		return get().getByte(index);
	}

	@Override
	public int indexOf(byte arg0) {
		return get().indexOf(arg0);
	}

	@Override
	public int lastIndexOf(byte arg0) {
		return get().lastIndexOf(arg0);
	}

	@Override
	public byte removeByte(int arg0) {
		return get().removeByte(arg0);
	}

	@Override
	public byte set(int index, byte element) {
		byte e = get().set(index, element);
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
	public boolean add(Byte e) {
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
	public boolean addAll(Collection<? extends Byte> c) {
		boolean add = get().addAll(c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Byte> c) {

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
	public Byte get(int index) {
		return get().get(index);
	}

	@Override
	public Byte set(int index, Byte element) {
		byte e = get().set(index, element);
		setChanged(true);
		observer.update(this);
		return e;
	}

	@Override
	public void add(int index, Byte element) {
		get().add(index, element);
		setChanged(true);
		observer.update(this);
	}

	@Override
	public Byte remove(int index) {
		byte e = get().remove(index);
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
	public int compareTo(List<? extends Byte> o) {
		return get().compareTo(o);
	}

	

	@Override
	public boolean contains(byte arg0) {
		return get().contains(arg0);
	}

	@Override
	public byte[] toByteArray() {
		return get().toByteArray();
	}

	@Override
	public byte[] toByteArray(byte[] arg0) {
		return get().toByteArray(arg0);
	}

	@Override
	public byte[] toArray(byte[] arg0) {
		return get().toArray(arg0);
	}

	@Override
	public boolean rem(byte arg0) {
		return get().rem(arg0);
	}

	@Override
	public boolean addAll(ByteCollection c) {
		boolean add = get().addAll(c);

		if (add) {
			setChanged(add);
			observer.update(this);
		}
		return add;
	}

	@Override
	public boolean containsAll(ByteCollection arg0) {
		return get().containsAll(arg0);
	}

	@Override
	public boolean removeAll(ByteCollection arg0) {
		return get().removeAll(arg0);
	}

	@Override
	public boolean retainAll(ByteCollection arg0) {
		return get().retainAll(arg0);
	}

}
