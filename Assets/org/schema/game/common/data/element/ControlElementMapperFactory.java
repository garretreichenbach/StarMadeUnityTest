package org.schema.game.common.data.element;

import java.io.DataInput;
import java.io.IOException;

import org.schema.schine.resource.tag.SerializableTagFactory;

public class ControlElementMapperFactory implements SerializableTagFactory {

	/**
	 *
	 */
	

	@Override
	public Object create(DataInput dis) throws IOException {

		ControlElementMapper m = new ControlElementMapper();
		ControlElementMap.deserialize(dis, m);

		return m;
	}

}
