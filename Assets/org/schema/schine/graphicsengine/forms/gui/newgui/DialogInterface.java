package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;

public interface DialogInterface {
	public abstract GUIElement getInputPanel();

	public abstract boolean allowChat();

	public abstract void deactivate();

	public abstract boolean checkDeactivated();

	public abstract void handleKeyEvent(KeyEventInterface e);
	public abstract void handleCharEvent(KeyEventInterface e);

	public abstract void updateDeacivated();

	public abstract long getDeactivationTime();

	public abstract void update(Timer timer);
}
