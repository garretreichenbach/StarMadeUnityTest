package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.IconDatabase;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIExpandableWindow extends GUIPlainWindow {

	boolean init = false;

	private GUITextButton title;

	public boolean expanded;

	public int unexpendedHeight = 34;

	public int clickableTopPadding = -2;

	public int clickableBottomPadding = 2;

	public int clickableTopPaddingUnexp = 0;

	public int clickableBottomPaddingUnexp = 0;

	private GUIOverlay rightArrow;

	private GUIOverlay downArrow;

	public GUIExpandableWindow(final InputState state, int initialWidth, int initialHeight, String windowId, boolean expanded) {
		super(state, initialWidth, initialHeight, windowId);
		if (windowId == null || !windowMap.containsKey(windowId)) {
			this.expanded = expanded;
		}
	}

	public GUIExpandableWindow(InputState state, int initialWidth, int initialHeight, int initialPosX, int initalPosY, String windowId, boolean expanded) {
		super(state, initialWidth, initialHeight, initialPosX, initalPosY, windowId);
		if (savedSizeAndPosition != null && savedSizeAndPosition.newPanel) {
			this.expanded = expanded;
		}
	// System.err.println("PP EXP:::: "+windowId+": "+expanded+"; "+windowMap.containsKey(windowId));
	}

	@Override
	protected void drawContent(GUIContentPane mainContentPane) {
		if (expanded) {
			// no need to transform, as we already should be at this point
			super.drawContent(mainContentPane);
		}
	}

	@Override
	public float getHeight() {
		return (int) (expanded ? super.getHeight() : unexpendedHeight);
	}

	@Override
	protected void setSavedSizeAndPosFrom() {
		savedSizeAndPosition.setFrom(width, height, getPos(), expanded);
	}

	public void setTitle(final String titleStr, final ExpandableCallback serv) {
		if (this.title != null) {
			detach(this.title);
		}
		this.title = new GUITextButton(getState(), 10, 10, FontSize.BIG_20, new Object() {

			@Override
			public String toString() {
				return "  " + titleStr;
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIExpandableWindow.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					expand(serv, !expanded);
				}
			}
		}) {

			@Override
			public void draw() {
				if (!expanded) {
					setPos(0, clickableTopPaddingUnexp, 0);
					setHeight((unexpendedHeight - UIScale.getUIScale().scale(1)) - clickableBottomPaddingUnexp);
				} else {
					setPos(0, clickableTopPadding, 0);
					setHeight((unexpendedHeight - UIScale.getUIScale().scale(1)) - clickableBottomPadding);
				}
				if (expanded) {
					setTextPos(UIScale.getUIScale().inset, UIScale.getUIScale().scale(6) - clickableTopPadding);
				} else {
					setTextPos(UIScale.getUIScale().inset, UIScale.getUIScale().scale(6) - clickableTopPaddingUnexp);
				}
				setWidth(GUIExpandableWindow.this.getWidth());
				super.draw();
			}
		};
		this.title.getBackgroundColorMouseOverlayPressed().w = 0.35f;
		this.title.getBackgroundColorMouseOverlay().w = 0.2f;
		this.title.getBackgroundColor().w = 0;
		attachSuper(this.title);
		expand(serv, expanded);
	}

	public void expand(ExpandableCallback serv, boolean exp) {
		if (exp) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.EXPAND)*/
			AudioController.fireAudioEventID(21);
		} else {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.UNEXPAND)*/
			AudioController.fireAudioEventID(20);
		}
		expanded = exp;
		serv.onExpandedChanged();
		if (expanded) {
			title.removeBeforeIcon(rightArrow);
			title.addBeforeIcon(downArrow);
		} else {
			title.removeBeforeIcon(downArrow);
			title.addBeforeIcon(rightArrow);
		}
		title.setBeforOrAfterIconPos(rightArrow, 2, 3);
		title.setBeforOrAfterIconPos(downArrow, 1, 2);
	}

	@Override
	public void onInit() {
		if (init) {
			return;
		}
		super.onInit();
		rightArrow = IconDatabase.getRightArrowInstance16(getState());
		downArrow = IconDatabase.getDownArrowInstance16(getState());
		this.init = true;
	}

	@Override
	public int getInnerHeigth() {
		if (!expanded) {
			return 0;
		}
		return super.getInnerHeigth();
	}
}
