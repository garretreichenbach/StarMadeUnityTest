package org.schema.game.common.data.element;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.resource.tag.SerializableTagElement;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class ControlElementMapper implements SerializableTagElement, Long2ObjectMap<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> {

	/**
	 *
	 */
	

	private Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> backing = new Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>>();
	private Long2ObjectOpenHashMap<FastCopyLongOpenHashSet> all = new Long2ObjectOpenHashMap<FastCopyLongOpenHashSet>();
	//	private Long2ObjectOpenHashMap<LongOpenHashSet> controllers = new Long2ObjectOpenHashMap<LongOpenHashSet>();
	private int deserializeShift;

	public void clearAndTrim() {
		clear();
		all.trim();
		//		controllers.trim();
		backing.trim();
	}

	/**
	 * @return the all
	 */
	public Long2ObjectOpenHashMap<FastCopyLongOpenHashSet> getAll() {
		return all;
	}

	public int getAllElementsSize() {
		int size = 0;
		for (LongOpenHashSet a : all.values()) {
			size += a.size();
		}
		return size;
	}

	@Override
	public byte getFactoryId() {
		return SerializableTagElement.CONTROL_ELEMENT_MAPPER;
	}

	@Override
	public void writeToTag(DataOutput dos) throws IOException {
		ControlElementMap.serializeForDisk(dos, this);
	}

	public boolean put(long index, long to, short controlledType) {
		assert (controlledType != Element.TYPE_NONE);
		assert (controlledType != Element.TYPE_ALL);
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> map;

		if (!containsKey(index)) {
			map = new Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>();
			put(index, map);
		} else {
			map = get(index);
		}

		FastCopyLongOpenHashSet sMap;
		if (!map.containsKey(controlledType)) {
			sMap = new FastCopyLongOpenHashSet();
			map.put(controlledType, sMap);
		} else {
			sMap = map.get(controlledType);
		}

		long elementPosition = ElementCollection.getIndex4(to, controlledType);

		FastCopyLongOpenHashSet allHash;
		if (!all.containsKey(index)) {
			allHash = new FastCopyLongOpenHashSet();
			all.put(index, allHash);
		} else {
			allHash = all.get(index);
		}
		allHash.add(elementPosition);

		//		ElementInformation info = ElementKeyMap.getInfo(controlledType);
		//		if(info.isController()){
		//			LongOpenHashSet ctrlHash;
		//			if(!getControllers().containsKey(index)){
		//				ctrlHash = new LongOpenHashSet();
		//				getControllers().put(index, ctrlHash);
		//			}else{
		//				ctrlHash = getControllers().get(index);
		//			}
		//			ctrlHash.add(elementPosition);
		//
		//		}

		return sMap.add(elementPosition);
	}

	public boolean put(Vector3i fromV, long to, short controlledType) {

		long index = ElementCollection.getIndex(fromV);

		return put(index, to, controlledType);

	}

	public void putAll(long index, short[] elements, short controlledType) {
		if (!ElementKeyMap.exists(controlledType)) {
			return;
		}
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> map;

		if (!containsKey(index)) {
			map = new Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>(elements.length / 3);
			put(index, map);
		} else {
			map = get(index);
		}
		FastCopyLongOpenHashSet sMap;
		if (!map.containsKey(controlledType)) {
			sMap = new FastCopyLongOpenHashSet(elements.length / 3);
			map.put(controlledType, sMap);
		} else {
			sMap = map.get(controlledType);
		}

		FastCopyLongOpenHashSet allHash;
		if (!all.containsKey(index)) {
			allHash = new FastCopyLongOpenHashSet(elements.length / 3);
			all.put(index, allHash);
		} else {
			allHash = all.get(index);
		}

		final int size = elements.length;
		for (int i = 0; i < size; i += 3) {
			long elementPosition = ElementCollection.getIndex4(elements[i], elements[i + 1], elements[i + 2], controlledType);

			allHash.add(elementPosition);
			sMap.add(elementPosition);
		}

	}

	public boolean remove(long index, long controlled,
	                      short controlledType) {

		if (containsKey(index) && get(index).containsKey(controlledType)) {

			long fullTargetIndex = ElementCollection.getIndex4(controlled, controlledType);

			all.get(index).remove(fullTargetIndex);
			//			if(getControllers().containsKey(index)){
			//				getControllers().get(index).remove(fullTargetIndex);
			//			}

			return get(index).get(controlledType).remove(fullTargetIndex);
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap#remove(java.lang.Object)
	 */
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> remove(Vector3i controller) {
		long index = ElementCollection.getIndex(controller);
		all.remove(index);
		//		getControllers().remove(index);
		return backing.remove(index);

	}

	public boolean remove(Vector3i controller, Vector3i controlled,
	                      short controlledType) {

		long index = ElementCollection.getIndex(controller);
		long controlledIndex = ElementCollection.getIndex(controlled);
		return remove(index, controlledIndex, controlledType);

	}

	public void setFromMap(ControlElementMapper controllingMap) {
		backing = new Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>>(controllingMap.backing.size());
		all = new Long2ObjectOpenHashMap<FastCopyLongOpenHashSet>(controllingMap.all.size());
		//		controllers = new Long2ObjectOpenHashMap<LongOpenHashSet>(controllingMap.controllers.size());

		for (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> e : controllingMap.long2ObjectEntrySet()) {
			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> short2ObjectOpenHashMap = new Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>(e.getValue().size());
			put(e.getLongKey(), short2ObjectOpenHashMap);

			FastCopyLongOpenHashSet allHashSet = new FastCopyLongOpenHashSet(controllingMap.all.get(e.getLongKey()).size());

			for (it.unimi.dsi.fastutil.shorts.Short2ObjectMap.Entry<FastCopyLongOpenHashSet> v : e.getValue().short2ObjectEntrySet()) {

				FastCopyLongOpenHashSet objectOpenHashSet = new FastCopyLongOpenHashSet(1);

				short2ObjectOpenHashMap.put(v.getShortKey(), objectOpenHashSet);

				objectOpenHashSet.deepApplianceCopy(v.getValue());
//				objectOpenHashSet.addAll(v.getValue());
				allHashSet.addAll(v.getValue());

			}

			all.put(e.getLongKey(), allHashSet);
		}

		//		for(it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<LongOpenHashSet> e : controllingMap.controllers.long2ObjectEntrySet()){
		//			LongOpenHashSet cHashSet = new LongOpenHashSet(e.getValue().size());
		//						System.err.println("Transferring conrollers: "+ElementCollection.getPosFromIndex(e.getLongKey(), new Vector3i())+": "+e.getValue());
		//			controllers.put(e.getLongKey(), cHashSet);
		//			cHashSet.addAll(e.getValue());
		//		}

		//		putAll(controllingMap);
		//		all.putAll(controllingMap.all);
		//		controllers.putAll(controllingMap.controllers);
	}

	/**
	 * @param k
	 * @param v
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#put(long, java.lang.Object)
	 */
	@Override
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> put(
			long k,
			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> v) {
		return backing.put(k, v);
	}

	/**
	 * @param k
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#get(long)
	 */
	@Override
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> get(
			long k) {
		return backing.get(k);
	}

	@Override
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> remove(long index) {
		all.remove(index);
		//		getControllers().remove(index);
		return backing.remove(index);

	}

	/**
	 * @param k
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#containsKey(long)
	 */
	@Override
	public boolean containsKey(long k) {
		return backing.containsKey(k);
	}

	/**
	 * @param rv
	 * @see it.unimi.dsi.fastutil.longs.AbstractLong2ObjectFunction#defaultReturnValue(java.lang.Object)
	 */
	@Override
	public void defaultReturnValue(
			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> rv) {
		backing.defaultReturnValue(rv);
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.AbstractLong2ObjectFunction#defaultReturnValue()
	 */
	@Override
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> defaultReturnValue() {
		return backing.defaultReturnValue();
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap#entrySet()
	 */
	@Override
	public ObjectSet<java.util.Map.Entry<Long, Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>>> entrySet() {
		return backing.entrySet();
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#long2ObjectEntrySet()
	 */
	@Override
	public it.unimi.dsi.fastutil.longs.Long2ObjectMap.FastEntrySet<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> long2ObjectEntrySet() {
		return backing.long2ObjectEntrySet();
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#keySet()
	 */
	@Override
	public LongSet keySet() {
		return backing.keySet();
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#values()
	 */
	@Override
	public ObjectCollection<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> values() {
		return backing.values();
	}

	/**
	 * @param ok
	 * @param ov
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#put(java.lang.Long, java.lang.Object)
	 */
	@Override
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> put(
			Long ok,
			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> ov) {
		return backing.put(ok, ov);
	}

	/**
	 * @param ok
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.AbstractLong2ObjectFunction#get(java.lang.Object)
	 */
	@Override
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> get(
			Object ok) {
		return backing.get(ok);
	}

	/**
	 * @param ok
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.AbstractLong2ObjectFunction#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object ok) {
		return backing.containsKey(ok);
	}

	/**
	 * @param ok
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#remove(java.lang.Object)
	 */
	@Override
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> remove(
			Object ok) {
		return backing.remove(ok);
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#size()
	 */
	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public void clear() {
		all.clear();
		//		controllers.clear();
		backing.clear();
	}

	/**
	 * @param ok
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#get(java.lang.Long)
	 */
	public Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> get(
			Long ok) {
		return backing.get(ok);
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	/**
	 * @param v
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object v) {
		return backing.containsValue(v);
	}

	/**
	 * @param m
	 * @see it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll(
			Map<? extends Long, ? extends Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> m) {
		backing.putAll(m);
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#trim()
	 */
	public boolean trim() {
		return backing.trim();
	}

	/**
	 * @param n
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#trim(int)
	 */
	public boolean trim(int n) {
		return backing.trim(n);
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#hashCode()
	 */
	@Override
	public int hashCode() {
		return backing.hashCode();
	}

	/**
	 * @param o
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return backing.equals(o);
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap#clone()
	 */
	@Override
	public Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> clone() {
		return backing.clone();
	}

	public void deserialize(DataInput dataInputStream) throws IOException {
		
		int keySize = dataInputStream.readInt();
		if (keySize >= 0) {
			//old and versionless, so we need to shift
			deserializeShift = 8;
			deserializeFromDisk(dataInputStream, keySize);
			deserializeShift = 0;
			return;
		}
		int version = -keySize;
		if(version > 1024){
			version -= 1024;
			keySize = dataInputStream.readInt();
			deserializeFromDisk(dataInputStream, keySize);
			return;
		}
		if(version < 2){
			deserializeShift = 8;
		}
		keySize = dataInputStream.readInt();
		//		System.err.println("[CM] keys: "+keySize);
		if (backing.isEmpty()) {
			if (keySize < 0 || keySize > 100000000) {
				throw new IOException("KEYSIZE INVALID: Control element keySize negative or too big (more than 100 million): " + keySize);
			}
			backing = new Long2ObjectOpenHashMap(keySize);
		}

		for (int i = 0; i < keySize; i++) {

			short xKey = (short) (dataInputStream.readShort()+deserializeShift);
			short yKey = (short) (dataInputStream.readShort()+deserializeShift);
			short zKey = (short) (dataInputStream.readShort()+deserializeShift);

			long key = ElementCollection.getIndex(xKey, yKey, zKey);

			int valueSize = dataInputStream.readInt();

			for (int v = 0; v < valueSize; v++) {

				short type = dataInputStream.readShort();

				int elementSize = dataInputStream.readInt();

				final boolean bigX = dataInputStream.readBoolean();
				final boolean bigY = dataInputStream.readBoolean();
				final boolean bigZ = dataInputStream.readBoolean();

				final short medianX = dataInputStream.readShort();
				final short medianY = dataInputStream.readShort();
				final short medianZ = dataInputStream.readShort();

				if (elementSize < 0 || elementSize > 100000000) {
					throw new IOException("Control element size negative or too big (more than 100 million): " + elementSize);
				}
				short elements[] = new short[elementSize * 3];
				int eindex = 0;
				for (int e = 0; e < elementSize; e++) {

					elements[eindex] = (short) ((short) ((bigX ? dataInputStream.readShort() : dataInputStream.readByte()) + medianX)+deserializeShift);
					elements[eindex + 1] = (short) ((short) ((bigY ? dataInputStream.readShort() : dataInputStream.readByte()) + medianY)+deserializeShift);
					elements[eindex + 2] = (short) ((short) ((bigZ ? dataInputStream.readShort() : dataInputStream.readByte()) + medianZ)+deserializeShift);

					eindex += 3;
				}
				putAll(key, elements, type);
			}

		}
		
		deserializeShift = 0;
	}

	public void deserializeFromDisk(DataInput dataInputStream, int keySize) throws IOException {
		if (backing.isEmpty()) {
			backing = new Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>>(keySize);
		}
		for (int i = 0; i < keySize; i++) {
			short xKey = (short) (dataInputStream.readShort()+deserializeShift);
			short yKey = (short) (dataInputStream.readShort()+deserializeShift);
			short zKey = (short) (dataInputStream.readShort()+deserializeShift);
			long key = ElementCollection.getIndex(xKey, yKey, zKey);
			int valueSize = dataInputStream.readInt();
			//			System.err.println("[CM] values: "+valueSize);
			for (int v = 0; v < valueSize; v++) {

				short type = dataInputStream.readShort();

				int elementSize = dataInputStream.readInt();
//								System.err.println("[CM] elements: "+elementSize+" - "+(elementSize*3));
				if (elementSize < 0 || elementSize > 100000000) {
					throw new IOException("Control element size negative or too big (more than 100 million): " + elementSize);
				}
				short elements[] = new short[elementSize * 3];
				int eindex = 0;
				for (int e = 0; e < elementSize; e++) {

					elements[eindex] = (short) (dataInputStream.readShort()+deserializeShift);
					elements[eindex + 1] = (short) (dataInputStream.readShort()+deserializeShift);
					elements[eindex + 2] = (short) (dataInputStream.readShort()+deserializeShift);

					eindex += 3;
				}
				putAll(key, elements, type);
			}

		}
		deserializeShift = 0;
	}

}
