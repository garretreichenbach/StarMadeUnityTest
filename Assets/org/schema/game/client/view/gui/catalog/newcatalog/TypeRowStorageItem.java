package org.schema.game.client.view.gui.catalog.newcatalog;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.meta.BlockStorageMetaItem;

public class TypeRowStorageItem {
	public final ElementInformation info;
	public final int itemId;
	private GameClientState state;

	public TypeRowStorageItem(ElementInformation info, int itemId, GameClientState state) {
		super();
		this.info = info;
		this.itemId = itemId;
		this.state = state;
	}

	public BlockStorageMetaItem getItem() {
		return (BlockStorageMetaItem) state.getMetaObjectManager().getObject(itemId);
	}

	public int getCount() {
		return getItem().storage.get(info.getId());
	}


	@Override
	public int hashCode() {
		return info.hashCode();
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		return o instanceof TypeRowStorageItem && info.equals(((TypeRowStorageItem) o).info);
	}

}
