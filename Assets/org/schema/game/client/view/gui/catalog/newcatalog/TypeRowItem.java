package org.schema.game.client.view.gui.catalog.newcatalog;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.meta.BlueprintMetaItem;

public class TypeRowItem {
	public final ElementInformation info;
	public final int itemId;
	private GameClientState state;

	public TypeRowItem(ElementInformation info, int itemId, GameClientState state) {
		super();
		this.info = info;
		this.itemId = itemId;
		this.state = state;
	}

	public BlueprintMetaItem getItem() {
		return (BlueprintMetaItem) state.getMetaObjectManager().getObject(itemId);
	}

	public int getProgress() {
		return getItem().progress.get(info.getId());
	}

	public int getGoal() {
		return getItem().goal.get(info.getId());
	}

	public float getPercent() {
		return (float) getProgress() / (float) getGoal();
	}

	@Override
	public int hashCode() {
		return info.hashCode();
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		return o instanceof TypeRowItem && info.equals(((TypeRowItem) o).info);
	}

}
