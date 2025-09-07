package org.schema.game.client.view.gui.advanced;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.List;

public abstract class AdvancedGUIMinimizeCallback {

	private boolean canMinimize;

	public GUITextButton closeLashButton;

	public boolean minimized;

	float minimizeStatus = 0;

	public final List<GUITextButton> additionalButtons = new ObjectArrayList<GUITextButton>();

	public abstract String getMinimizedText();

	public abstract String getMaximizedText();
	public AdvancedGUIMinimizeCallback(final InputState state, boolean canMinimize) {
		this.canMinimize = canMinimize;
		initialMinimized();
		final Object txtMin = new Object() {

			@Override
			public String toString() {
				return getMinimizedText();
			}
		};
		final Object txtMax = new Object() {

			@Override
			public String toString() {
				return getMaximizedText();
			}
		};
		closeLashButton = new GUITextButton(state, 170, 20, minimized ? txtMin : txtMax, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (isMinimized()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.MAXIMIZE)*/
						AudioController.fireAudioEventID(304);
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.MINIMIZE)*/
						AudioController.fireAudioEventID(303);
					}
					setMinimized(!isMinimized());
				}
			}
		}) {

			@Override
			public void draw() {
				if (isCloseLash()) {
					if (!isMinimized() && minimizeStatus < 0.5f) {
						if (getWidth() != 30) {
							setWidth(30);
							setText(txtMax);
						}
					} else if (isMinimized() && minimizeStatus > 0.5f) {
						if (getWidth() != 170) {
							setWidth(170);
							setText(txtMin);
						}
					}
					super.draw();
				}
			}
		};
	}

	public abstract void initialMinimized();

	public abstract void onMinimized(boolean minimized);

	public abstract boolean isActive();

	public boolean isInside() {
		return (canMinimize && (isAnyButtonInside() || (minimizeStatus > 0.0000001f && minimizeStatus < 0.999999f)));
	}

	private boolean isAnyButtonInside() {
		if (closeLashButton.isInside()) {
			return true;
		}
		for (GUITextButton e : additionalButtons) {
			if (e.isInside()) {
				return true;
			}
		}
		return false;
	}

	public boolean isCloseLash() {
		return canMinimize;
	}

	public void setButtonPosition(GUIElement e, AdvancedGUIElement ae) {
		if (isCloseLashOnRight()) {
			e.setPos(ae.getWidth() + closeLashButtonOffsetX(), (int) (ae.main.getHeight() / 2 - closeLashButton.getWidth() / 2), 0);
		} else {
			e.setPos(-(closeLashButton.getHeight() + closeLashButtonOffsetX()), (int) (ae.main.getHeight() / 2 - closeLashButton.getWidth() / 2), 0);
		}
		e.setRot(0, 0, 90);
	}

	public void setButtonPosition(AdvancedGUIElement ae) {
		if (canMinimize) {
			setButtonPosition(closeLashButton, ae);
		}
	}

	public float getMinimizeSpeed() {
		return 1.666f;
	}

	protected boolean isCloseLashOnRight() {
		return true;
	}

	protected int closeLashButtonOffsetX() {
		return 20;
	}

	public void onInit() {
		closeLashButton.onInit();
	}

	public void setMinimized(boolean b) {
		this.minimized = b;
		onMinimized(this.minimized);
	}

	public boolean isMinimized() {
		return minimized;
	}

	public void setMinimizedInitial(boolean b) {
		this.minimized = b;
		this.minimizeStatus = this.minimized ? 1f : 0f;
		onMinimized(this.minimized);
	}

	public void update(Timer timer) {
		// if(isCloseLashOnRight()) {
		if (!minimized && minimizeStatus > 0) {
			minimizeStatus = Math.max(0f, minimizeStatus - timer.getDelta() * getMinimizeSpeed());
		} else if (minimized && minimizeStatus < 1) {
			minimizeStatus = Math.min(1f, minimizeStatus + timer.getDelta() * getMinimizeSpeed());
		}
	// }else {
	// if(!minimized && minimizeStatus < 1){
	// minimizeStatus = Math.min(1f, minimizeStatus+timer.getDelta()*getMinimizeSpeed());
	// }else if(minimized && minimizeStatus > 0){
	// minimizeStatus = Math.max(0f, minimizeStatus-timer.getDelta()*getMinimizeSpeed());
	// }
	// }
	}

	public void draw() {
		closeLashButton.draw();
		for (GUITextButton e : additionalButtons) {
			e.draw();
		}
	}

	public int getButtonWidth() {
		return (int) closeLashButton.getWidth();
	}
}
