package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIResizableElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIExpandableButton extends GUIElement implements GUICallback, GUIActiveInterface {

	GUIHorizontalArea button;

	private GUIResizableElement expandedPanel;

	private boolean expanded = false;

	private GUIActivationCallback actCallback;

	private Object text;

	private Object expandedText;

	private GUIInnerTextbox p;

	public int buttonWidthAdd;

	private boolean init;

	public GUIExpandableButton(InputState state, GUIInnerTextbox p, Object text, Object expandedText, GUIActivationCallback actCallback, GUIResizableElement expanded, boolean initialExpanded) {
		super(state);
		this.p = p;
		this.actCallback = actCallback;
		this.text = text;
		this.expandedText = expandedText;
		this.expandedPanel = expanded;
		this.expanded = initialExpanded;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		button.draw();
		if (expanded) {
			GlUtil.glPushMatrix();
			transform();
			expandedPanel.setPos(0, button.getHeight(), 0);
			expandedPanel.setWidth(p.getWidth());
			expandedPanel.draw();
			GlUtil.glPopMatrix();
		}
		p.tbHeight = (int) getHeight() + (expanded ? 3 : 3);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane#onInit()
	 */
	@Override
	public void onInit() {
		button = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, new Object() {

			/* (non-Javadoc)
					 * @see java.lang.Object#toString()
					 */
			@Override
			public String toString() {
				if (expanded) {
					return expandedText.toString();
				}
				return text.toString();
			}
		}, this, this, actCallback) {

			@Override
			public void draw() {
				setWidth(p.getWidth() + buttonWidthAdd);
				super.draw();
			}
		};
		button.onInit();
		init = true;
	}

	@Override
	public float getHeight() {
		if (expanded) {
			return button.getHeight() + expandedPanel.getHeight();
		} else {
			return button.getHeight();
		}
	}

	@Override
	public void cleanUp() {
		button.cleanUp();
		expandedPanel.cleanUp();
	}

	@Override
	public float getWidth() {
		return p.getWidth();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			expanded = !expanded;
			if (expanded) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.EXPAND)*/
				AudioController.fireAudioEventID(19);
			} else {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
				AudioController.fireAudioEventID(18);
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return !isActive() || !actCallback.isActive(getState());
	}
}
