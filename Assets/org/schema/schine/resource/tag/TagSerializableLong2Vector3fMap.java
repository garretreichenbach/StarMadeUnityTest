package org.schema.schine.resource.tag;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3fTools;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public class TagSerializableLong2Vector3fMap extends Long2ObjectLinkedOpenHashMap<Vector3f> implements SerializableTagElement, SerializationInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1556336980069443312L;
	
	/**
	 * if true, causes entries with a zero length vector as a value not to be serialized
	 */
	public boolean ignoreZeroLenVectors = false;

	public TagSerializableLong2Vector3fMap() {
		super();
	}

	public TagSerializableLong2Vector3fMap(int expected, float f) {
		super(expected, f);
	}

	public TagSerializableLong2Vector3fMap(int expected) {
		super(expected);
	}

	public TagSerializableLong2Vector3fMap(long[] k, Vector3f[] v, float f) {
		super(k, v, f);
	}

	public TagSerializableLong2Vector3fMap(long[] k, Vector3f[] v) {
		super(k, v);
	}

	public TagSerializableLong2Vector3fMap(Long2ObjectMap<Vector3f> m, float f) {
		super(m, f);
	}

	public TagSerializableLong2Vector3fMap(Long2ObjectMap<Vector3f> m) {
		super(m);
	}

	public TagSerializableLong2Vector3fMap(Map<? extends Long, ? extends Vector3f> m, float f) {
		super(m, f);
	}

	public TagSerializableLong2Vector3fMap(Map<? extends Long, ? extends Vector3f> m) {
		super(m);
	}

	@Override
	public byte getFactoryId() {
		return SerializableTagElement.LONG_2_VECTOR3f_MAP;
	}

	@Override
	public void writeToTag(DataOutput b) throws IOException {
		serialize(b, this, ignoreZeroLenVectors);
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		serialize(b, this, ignoreZeroLenVectors);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		deserialize(b, this);
	}
	private static void serialize(DataOutput b, Long2ObjectLinkedOpenHashMap<Vector3f> m, boolean ignoreZeroLenVectors) throws IOException {
		FastSortedEntrySet<Vector3f> es = m.long2ObjectEntrySet();
		int s = 0;
		for(Long2ObjectMap.Entry<Vector3f> e : es) {
			if(!ignoreZeroLenVectors || (e.getValue().x != 0 || e.getValue().y != 0 || e.getValue().z != 0)) {
				s++;
			}
		}
		b.writeInt(s);

		if(s > 0) {
			for(Long2ObjectMap.Entry<Vector3f> e : es) {
				if(!ignoreZeroLenVectors || (e.getValue().x != 0 || e.getValue().y != 0 || e.getValue().z != 0)) {
					b.writeLong(e.getLongKey());
					Vector3fTools.serialize(e.getValue(), b);
				}
			}
		}
	}
	public static void deserialize(DataInput b, Long2ObjectLinkedOpenHashMap<Vector3f> m) throws IOException {
		final int size = b.readInt();
		deserializeBody(b, m, size);
	}

	public static void deserializeBody(DataInput dis, Long2ObjectLinkedOpenHashMap s, final int size) throws IOException {
		for(int i = 0; i < size; i++){
			s.put(dis.readLong(), Vector3fTools.deserialize(dis));
		}		
	}
}
