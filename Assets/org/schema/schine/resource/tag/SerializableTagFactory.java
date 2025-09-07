package org.schema.schine.resource.tag;

import java.io.DataInput;
import java.io.IOException;

public interface SerializableTagFactory {

	public Object create(DataInput dis) throws IOException;

}
