package org.schema.schine.resource.tag;

import java.io.DataInput;
import java.io.IOException;

public class TagSerializableLong2TransformMapFactory implements SerializableTagFactory{

	@Override
	public Object create(DataInput dis) throws IOException {
		final int size = dis.readInt();
		TagSerializableLong2TransformMap s = new TagSerializableLong2TransformMap(size);
		TagSerializableLong2TransformMap.deserializeBody(dis, s, size);
		
		return s;
	}

}
