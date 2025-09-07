package org.schema.game.common.data.element;

import java.io.DataInput;
import java.io.IOException;

import org.schema.game.common.controller.ElementCountMap;
import org.schema.schine.resource.tag.SerializableTagFactory;

public class ElementCountMapFactory implements SerializableTagFactory {

	/**
	 *
	 */
	

	@Override
	public Object create(DataInput dis) throws IOException {

		ElementCountMap m = new ElementCountMap();
		m.deserialize(dis);

		return m;
	}

}
