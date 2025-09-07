package org.schema.schine.network.client;

import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;

public class DelayedDropDownSelectedChanged {
	DropDownCallback dropDownCallback;
	GUIListElement guiListElement;

	public DelayedDropDownSelectedChanged(
			DropDownCallback dropDownCallback, GUIListElement guiListElement) {
		super();
		this.dropDownCallback = dropDownCallback;
		this.guiListElement = guiListElement;
	}

	public void execute() {
		dropDownCallback.onSelectionChanged(guiListElement);
	}

}
