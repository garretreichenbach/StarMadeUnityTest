package org.schema.schine.resource.tag;

import java.io.DataInput;
import java.io.IOException;

public class TagSerializableLongSetFactory implements SerializableTagFactory{

	@Override
	public Object create(DataInput dis) throws IOException {
		final int size = dis.readInt();
		TagSerializableLongSet s = new TagSerializableLongSet(size);
		for(int i = 0; i < size; i++){
			s.add(dis.readLong());
		}
		return s;
	}

}
