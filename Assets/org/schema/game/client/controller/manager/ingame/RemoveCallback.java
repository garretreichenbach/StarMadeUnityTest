package org.schema.game.client.controller.manager.ingame;

public interface RemoveCallback extends BuildSelectionCallback {

	public abstract void onRemove(long pos, short type);
}
