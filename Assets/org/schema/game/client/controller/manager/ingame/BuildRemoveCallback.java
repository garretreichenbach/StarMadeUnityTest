package org.schema.game.client.controller.manager.ingame;

public interface BuildRemoveCallback extends BuildSelectionCallback {

	public void onRemove(long pos, short type);

	boolean canRemove(short type);

}
