package org.schema.schine.network.client;

import java.util.List;

import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallbackBlocking;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUICallbackController {

	private final List<GUICallback> guiCallbacks = new ObjectArrayList<GUICallback>();

	private final List<GUIElement> callingGUIElements = new ObjectArrayList<GUIElement>();

	private final List<GUICallbackBlocking> guiCallbacksBlocking = new ObjectArrayList<GUICallbackBlocking>();

	private final List<GUIElement> callingGUIElementsBlocking = new ObjectArrayList<GUIElement>();

	private final List<GUIElement> insideGUIs = new ObjectArrayList<GUIElement>();

	private final List<GUIToolTip> toolTips = new ObjectArrayList<GUIToolTip>();

	public GUICallbackController() {
	}

	public void addCallback(GUICallback callback, GUIElement guiElement) {
		int callBackIndex = guiCallbacks.indexOf(callback);
		if (callBackIndex < 0 || (callBackIndex != callingGUIElements.indexOf(guiElement))) {
			guiCallbacks.add(callback);
			callingGUIElements.add(guiElement);
		}
	}

	boolean mayExecute(GUICallback callback, GUIElement element, InputState state) {
		boolean wasBlocking = false;
		if ((state.getController().getInputController().getCurrentActiveDropdown() == null || state.getController().getInputController().getCurrentActiveDropdown() == callback) && (guiCallbacksBlocking.isEmpty() || (wasBlocking = guiCallbacksBlocking.remove(callback)))) {
			if (wasBlocking) {
				((GUICallbackBlocking) callback).onBlockedCallbackExecuted();
			}
			return true;
		}
		return false;
	}

	public void execute(MouseEvent e, InputState state) {
		assert (guiCallbacks.size() == callingGUIElements.size());
		int size = guiCallbacks.size();
		boolean executedDropClick = false;
		final int blockingSize = guiCallbacksBlocking.size();
		// System.err.println("CALLBACKS: "+size);
		for (int i = 0; i < size; i++) {
			GUICallback callback = guiCallbacks.get(i);
			GUIElement element = callingGUIElements.get(i);
			if (e.pressedLeftMouse() && state.getController().getInputController().getCurrentActiveDropdown() != callback) {
				executedDropClick = true;
			}
			if (mayExecute(callback, element, state)) {
				callback.callback(element, e);
			}
		}
		// System.err.println("BLOCKING "+blockingSize+"; "+guiCallbacksBlocking);
		if (blockingSize > 0 && !guiCallbacksBlocking.isEmpty()) {
			for (GUICallbackBlocking b : guiCallbacksBlocking) {
				b.onBlockedCallbackNotExecuted(blockingSize > guiCallbacksBlocking.size());
			}
		}
		// System.err.println("EXECUTE:::: "+state.currentContextPane+"; "+(state.currentContextPane != null ? state.currentContextPane.drawnOnce : "NULL"));
		if (state.getController().getInputController().getCurrentContextPane() != null && state.getController().getInputController().getCurrentContextPane().drawnOnce && !state.getController().getInputController().getCurrentContextPane().isInside() && e.isPressed()) {
			state.getController().getInputController().setCurrentContextPane(null);
		}
		if (state.getController().getInputController().getCurrentActiveDropdown() != null && ((size == 0 && e.pressedLeftMouse()) || (size > 0 && (state.getController().getInputController().getCurrentActiveDropdown() != null && executedDropClick)))) {
			if (!state.getController().getInputController().getCurrentActiveDropdown().isScrollBarInside()) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
				AudioController.fireAudioEventID(31);
				state.getController().getInputController().getCurrentActiveDropdown().setExpanded(false);
			}
		}
	}

	public void drawToolTips() {
		GUIElement.enableOrthogonal();
		for (GUIToolTip t : toolTips) {
			t.draw();
		}
		GUIElement.disableOrthogonal();
		toolTips.clear();
	}

	public void reset() {
		guiCallbacks.clear();
		callingGUIElements.clear();
		guiCallbacksBlocking.clear();
		callingGUIElementsBlocking.clear();
	}

	public List<GUICallback> getGuiCallbacks() {
		return guiCallbacks;
	}

	public void addBlocking(GUICallbackBlocking callback, GUIElement guiElement) {
		guiCallbacksBlocking.add(callback);
		callingGUIElementsBlocking.add(guiElement);
	}

	public int blockingCount() {
		return guiCallbacksBlocking.size();
	}

	public void addToolTip(GUIToolTip toolTip) {
		if (EngineSettings.DRAW_TOOL_TIPS.isOn()) {
			assert (toolTip != null);
			toolTips.add(toolTip);
		}
	}

	public void updateToolTips(Timer timer) {
		if (EngineSettings.DRAW_TOOL_TIPS.isOn()) {
			GUIToolTip.tooltipGraphics.update(timer);
		}
	}

	public List<GUIElement> getInsideGUIs() {
		return insideGUIs;
	}
	// public void addInsideGUIElement(GUIElement guiElement) {
	// insideGUIs.add(guiElement);
	// }
	// 
	// public void deactivateInsideGUIs() {
	// for(int i = 0; i < insideGUIs.size(); i++){
	// GUIElement element = insideGUIs.get(i);
	// element.resetMouseInside();
	// }
	// insideGUIs.clear();
	// }
}
