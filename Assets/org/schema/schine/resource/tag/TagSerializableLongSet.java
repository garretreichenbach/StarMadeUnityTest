package org.schema.schine.resource.tag;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schema.common.JsonSerializable;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class TagSerializableLongSet extends LongOpenHashSet implements SerializableTagElement, JsonSerializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public TagSerializableLongSet() {
		super();
	}
	
	public TagSerializableLongSet(JSONObject json) {
		fromJson(json);
	}

	public TagSerializableLongSet(Collection<? extends Long> c, float f) {
		super(c, f);
	}

	public TagSerializableLongSet(Collection<? extends Long> c) {
		super(c);
	}

	public TagSerializableLongSet(int expected, float f) {
		super(expected, f);
	}

	public TagSerializableLongSet(int expected) {
		super(expected);
	}

	public TagSerializableLongSet(Iterator<?> i, float f) {
		super(i, f);
	}

	public TagSerializableLongSet(Iterator<?> i) {
		super(i);
	}

	public TagSerializableLongSet(long[] a, float f) {
		super(a, f);
	}

	public TagSerializableLongSet(long[] a, int offset, int length, float f) {
		super(a, offset, length, f);
	}

	public TagSerializableLongSet(long[] a, int offset, int length) {
		super(a, offset, length);
	}

	public TagSerializableLongSet(long[] a) {
		super(a);
	}

	public TagSerializableLongSet(LongCollection c, float f) {
		super(c, f);
	}

	public TagSerializableLongSet(LongCollection c) {
		super(c);
	}

	public TagSerializableLongSet(LongIterator i, float f) {
		super(i, f);
	}

	public TagSerializableLongSet(LongIterator i) {
		super(i);
	}

	@Override
	public byte getFactoryId() {
		return SerializableTagElement.LONG_SET;
	}

	@Override
	public void writeToTag(DataOutput dos) throws IOException {
		dos.writeInt(size());
		for(long l : this) {
			dos.writeLong(l);
		}
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for(long l : this) array.put(l);
		json.put("array", array);
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		JSONArray array = json.getJSONArray("array");
		for(int i = 0; i < array.length(); i++) add(array.getLong(i));
	}
}
