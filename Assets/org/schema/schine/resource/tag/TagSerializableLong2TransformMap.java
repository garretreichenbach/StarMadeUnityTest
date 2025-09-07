package org.schema.schine.resource.tag;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;

import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public class TagSerializableLong2TransformMap extends Long2ObjectLinkedOpenHashMap<Transform> implements SerializableTagElement, SerializationInterface{

	/**
	 * if true, causes entries with a idenity transform as a value not to be serialized
	 */
	public boolean ignoreIdentTransforms = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = -1556336980069443312L;

	public TagSerializableLong2TransformMap() {
		super();
	}

	public TagSerializableLong2TransformMap(int expected, float f) {
		super(expected, f);
	}

	public TagSerializableLong2TransformMap(int expected) {
		super(expected);
	}

	public TagSerializableLong2TransformMap(long[] k, Transform[] v, float f) {
		super(k, v, f);
	}

	public TagSerializableLong2TransformMap(long[] k, Transform[] v) {
		super(k, v);
	}

	public TagSerializableLong2TransformMap(Long2ObjectMap<Transform> m, float f) {
		super(m, f);
	}

	public TagSerializableLong2TransformMap(Long2ObjectMap<Transform> m) {
		super(m);
	}

	public TagSerializableLong2TransformMap(Map<? extends Long, ? extends Transform> m, float f) {
		super(m, f);
	}

	public TagSerializableLong2TransformMap(Map<? extends Long, ? extends Transform> m) {
		super(m);
	}

	@Override
	public byte getFactoryId() {
		return SerializableTagElement.LONG_2_TRANSFORM_MAP;
	}

	@Override
	public void writeToTag(DataOutput b) throws IOException {
		serialize(b, this, ignoreIdentTransforms);
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		serialize(b, this, ignoreIdentTransforms);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		deserialize(b, this);
	}
	private static void serialize(DataOutput b, Long2ObjectLinkedOpenHashMap<Transform> m, boolean ignoreIdentTransforms) throws IOException {
		FastSortedEntrySet<Transform> es = m.long2ObjectEntrySet();
		int s = 0;
		List<Long2ObjectMap.Entry<Transform>> l = new ObjectArrayList<Long2ObjectMap.Entry<Transform>>(m.size());
		for(Long2ObjectMap.Entry<Transform> e : es) {
			if(!ignoreIdentTransforms || !e.getValue().equals(TransformTools.ident)) {
				l.add(e);
			}
		}
		b.writeInt(l.size());

		for(int i = 0; i < l.size(); i++) {
			Long2ObjectMap.Entry<Transform> e = l.get(i);
			b.writeLong(e.getLongKey());
			TransformTools.serializeFully(b, e.getValue());
		}
	}
	public static void deserialize(DataInput b, Long2ObjectLinkedOpenHashMap<Transform> m) throws IOException {
		final int size = b.readInt();
		deserializeBody(b, m, size);
	}

	public static void deserializeBody(DataInput dis, Long2ObjectLinkedOpenHashMap<Transform> s, final int size) throws IOException {
		for(int i = 0; i < size; i++){
			long l = dis.readLong();
			Transform t = TransformTools.deserializeFully(dis, new Transform());
			s.put(l, t);
		}		
	}
}
