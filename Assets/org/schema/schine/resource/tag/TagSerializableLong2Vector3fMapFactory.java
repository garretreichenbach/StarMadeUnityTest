package org.schema.schine.resource.tag;

import java.io.DataInput;
import java.io.IOException;

public class TagSerializableLong2Vector3fMapFactory implements SerializableTagFactory{

	@Override
	public Object create(DataInput dis) throws IOException {
		final int size = dis.readInt();
		TagSerializableLong2Vector3fMap s = new TagSerializableLong2Vector3fMap(size);
		TagSerializableLong2Vector3fMap.deserializeBody(dis, s, size);
		
		return s;
	}

}
