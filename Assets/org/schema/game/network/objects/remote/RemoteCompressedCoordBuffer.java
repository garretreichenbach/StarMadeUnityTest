package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.RemoteBufferInterface;
import org.schema.schine.network.objects.remote.NetworkChangeObserver;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteField;
import org.schema.schine.network.objects.remote.Streamable;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;

public class RemoteCompressedCoordBuffer extends RemoteField<ShortArrayList> implements ShortList, RemoteBufferInterface {

	private final ShortArrayList receiveBuffer = new ShortArrayList();
	private static final Long2ObjectOpenHashMap<ByteArrayList> ordMap = new Long2ObjectOpenHashMap<ByteArrayList>();
	public int MAX_BATCH = RemoteBuffer.MAX_BATCH_SIZE;


	public RemoteCompressedCoordBuffer(boolean onServer, int batch) {
		super(new ShortArrayList(), onServer);
		this.MAX_BATCH = batch;
	}

	public RemoteCompressedCoordBuffer(NetworkObject synchOn, int batch) {
		super(new ShortArrayList(), synchOn);
		this.MAX_BATCH = batch;
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT; // all fields plus size
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			
			final int segX = buffer.readShort()*256;
			final int segY = buffer.readShort()*256;
			final int segZ = buffer.readShort()*256;
			
			final int colSize = buffer.readShort();
			
			for(int i = 0; i < colSize; i+=3){
				int x = buffer.readByte() & 0xFF;
				short absX = (short) (segX + x);
				receiveBuffer.add(absX);
				
				int y = buffer.readByte() & 0xFF;
				short absY = (short) (segY + y);
				receiveBuffer.add(absY);
				
				int z = buffer.readByte() & 0xFF;
				short absZ = (short) (segZ + z);
				receiveBuffer.add(absZ);
				
			}
			
			
		}
	}
	public static void main(String[] args){
		
		ShortArrayList s = new ShortArrayList();
		
		s.add((short)-15); s.add((short)-15); s.add((short)-15);
		s.add((short)0); s.add((short)0); s.add((short)0);
		s.add((short)55); s.add((short)55); s.add((short)55);
		s.add((short)256); s.add((short)256); s.add((short)256);
		s.add((short)-256); s.add((short)-256); s.add((short)-256);
		s.add((short)-1231); s.add((short)1231); s.add((short)-1231);
		
		RemoteCompressedCoordBuffer b = new RemoteCompressedCoordBuffer(true, 128);

		b.observer = new NetworkChangeObserver() {
			
			@Override
			public void update(Streamable<?> streamable) {
			}
			
			@Override
			public boolean isSynched() {
				return false;
			}
		};
		
		
		
		
		b.addAll(s);
		
		
		FastByteArrayOutputStream out = new FastByteArrayOutputStream(new byte[1000]);
		
		try {
			b.toByteStream(new DataOutputStream(out));
			
			FastByteArrayInputStream in = new FastByteArrayInputStream(out.array, 0, out.length);
			
			b.fromByteStream(new DataInputStream(in), 0);
			
			for(int i = 0; i < b.receiveBuffer.size(); i+= 3){
				int x = b.receiveBuffer.get(i);
				int y = b.receiveBuffer.get(i+1);
				int z = b.receiveBuffer.get(i+2);
				System.err.println("IN: "+s.getShort(i)+", "+s.getShort(i+1)+", "+s.getShort(i+2)+"; OUT: "+x+", "+y+", "+z+";");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int batchSize = Math.min(MAX_BATCH, get().size());
		
		
		
		synchronized(ordMap){
			for (int i = 0; i < batchSize; i+=3) {
				short x = get().removeShort(0);
				short y = get().removeShort(0);
				short z = get().removeShort(0);
				
				short mx = (short) ByteUtil.divU256(x);
				short my = (short) ByteUtil.divU256(y);
				short mz = (short) ByteUtil.divU256(z);
				
				
				long index = ElementCollection.getIndex(mx, my, mz);
				ByteArrayList byteArrayList = ordMap.get(index);
				if(byteArrayList == null){
					byteArrayList = getByteList();
					ordMap.put(index, byteArrayList);
				}
				byteArrayList.add((byte)ByteUtil.modU256(x));
				byteArrayList.add((byte)ByteUtil.modU256(y));
				byteArrayList.add((byte)ByteUtil.modU256(z));
				
//				System.err.println("INDEX ::: "+x+", "+y+", "+z+": "+mx+", "+my+" "+mz+" -> "+
//				ByteUtil.modU256(x)+", "+ByteUtil.modU256(y)+", "+ByteUtil.modU256(z)+" -> RE "+
//						(mx*256+ByteUtil.modU256(x))+", "+(my*256+ByteUtil.modU256(y))+", "+(mz*256+ByteUtil.modU256(z)));
				
			}
			buffer.writeInt(ordMap.size());
			for(Entry<Long, ByteArrayList> a : ordMap.entrySet()){
				long key = a.getKey();
				buffer.writeShort(ElementCollection.getPosX(key));
				buffer.writeShort(ElementCollection.getPosY(key));
				buffer.writeShort(ElementCollection.getPosZ(key));
				
				final short lSize = (short) a.getValue().size();
				buffer.writeShort(lSize);
				for(int i = 0; i < lSize; i++){
					byte b = a.getValue().getByte(i);
					buffer.writeByte(b);
				}
				a.getValue().clear();
				freeByteList(a.getValue());
			}
			ordMap.clear();
		}
		
		
//

//
//		int size = 0;
//		for (int i = 0; i < batchSize; i++) {
//			short remoteField = get().removeShort(0);
//
//			size += ByteUtil.SIZEOF_LONG;
//			buffer.writeShort(remoteField);
//		}
		keepChanged = !get().isEmpty();

		
		
		
		return ByteUtil.SIZEOF_INT;

	}
	private static ObjectArrayFIFOQueue<ByteArrayList> l = new ObjectArrayFIFOQueue<ByteArrayList>();
	private static ByteArrayList getByteList() {
		synchronized(l){
			if(l.isEmpty()){
				return new ByteArrayList();
			}else{
				return l.dequeue();
			}
		}
	}
	private static void freeByteList(ByteArrayList m) {
		synchronized(l){
			l.enqueue(m);
		}
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
