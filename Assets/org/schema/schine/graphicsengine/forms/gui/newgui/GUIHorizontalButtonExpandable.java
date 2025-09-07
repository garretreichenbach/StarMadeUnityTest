package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.util.List;

import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallbackBlocking;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIHorizontalButtonExpandable extends GUIHorizontalButton implements GUIActivationHighlightCallback, GUICallback {

	private final List<GUIHorizontalButton> children = new ObjectArrayList<GUIHorizontalButton>();

	private boolean expanded;

	public void addButton(GUIHorizontalButton b) {
		children.add(b);
	}

	private class CB implements GUICallbackBlocking {

		private final GUICallback callback;

		public CB(GUICallback callback) {
			this.callback = callback;
		}

		@Override
		public boolean isOccluded() {
			return callback.isOccluded();
		}

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			callback.callback(callingGuiElement, event);
		}

		@Override
		public boolean isBlocking() {
			// is checked when inside only
			return true;
		}

		@Override
		public void onBlockedCallbackExecuted() {
		}

		@Override
		public void onBlockedCallbackNotExecuted(boolean anyOtherBlockedCallbacksExecuted) {
		// if(!anyOtherBlockedCallbacksExecuted){
		// expanded = !expanded;
		// }
		}
	}

	public void addButton(Object text, final GUIHorizontalArea.HButtonType type, GUICallback callback, final GUIActivationCallback actCallback) {
		final CB b = new CB(callback);
		final GUIHorizontalButton guiHorizontalButton = new GUIHorizontalButton(getState(), type, text, b, activeInterface, actCallback);
		children.add(guiHorizontalButton);
	}

	public GUIHorizontalButtonExpandable(ClientState state, HButtonType type, Object text, GUIActiveInterface activeInterface) {
		super(state, type, text, null, activeInterface, null);
		callback = this;
		actCallback = this;
	}

	@Override
	public void draw() {
		if (isInside()) {
		}
		if (actCallback == null || actCallback.isVisible(getState())) {
			boolean active = actCallback == null || actCallback.isActive(getState());
			boolean highlight = actCallback != null && actCallback instanceof GUIActivationHighlightCallback && ((GUIActivationHighlightCallback) actCallback).isHighlighted(getState());
			super.draw();
			if (highlight) {
				for (int i = 0; i < children.size(); i++) {
					GUIHorizontalButton b = children.get(i);
					b.setPos(0, (getHeight() * (i + 1)), 0);
					b.setWidth(getWidth());
					b.draw();
				}
			}
		}
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			expanded = !expanded;
			if (expanded) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.EXPAND)*/
				AudioController.fireAudioEventID(23);
			} else {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
				AudioController.fireAudioEventID(22);
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return activeInterface != null && !activeInterface.isActive();
	}

	@Override
	public boolean isVisible(InputState state) {
		return true;
	}

	@Override
	public boolean isActive(InputState state) {
		return activeInterface == null || activeInterface.isActive();
	}

	@Override
	public boolean isHighlighted(InputState state) {
		return expanded;
	}
}
