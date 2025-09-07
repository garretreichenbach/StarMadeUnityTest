package org.schema.game.server.controller;

import org.schema.game.server.data.blueprintnw.BlueprintEntry;

public interface MayImportCallback {
	public void callbackOnImportDenied(BlueprintEntry e);

	public boolean mayImport(BlueprintEntry e);

	void onImport(BlueprintEntry e);
}
