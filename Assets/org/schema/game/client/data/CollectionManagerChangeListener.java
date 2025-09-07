package org.schema.game.client.data;

import org.schema.game.common.controller.elements.ElementCollectionManager;

public interface CollectionManagerChangeListener {
	public void onChange(ElementCollectionManager<?, ?, ?> col);
}
