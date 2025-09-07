package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TabCallback;
import org.schema.schine.common.TextAreaInput;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;

public class GUIActivatableTextBar extends GUIElement implements GUICallback, TabCallback {

	public boolean leftDependentHalf;

	public boolean rightDependentHalf;

	public int offsetX;

	protected GUIOverlay icon;

	private GUIElement dependend;

	private final TextAreaInput area;

	private GUITextInput guiTextInput;

	private boolean active;

	public GUITextOverlay guiSearchInd;

	private GUITextOverlay guiTextStats;

	private GUIScrollablePanel background;

	private boolean init;

	private GUITextOverlay areaStats;

	private GUIInnerTextbox wrapAround;

	private GUIResizableElement hz;

	private GUIActivatableTextBar toSwitch;

	private boolean clearButtonEnabled;

	private GUIOverlay cross;

	public int dependendDistanceFromRight;

	private boolean selectAllOnClick;

	public GUIActiveInterface activeInterface;

	public FontInterface fontSize = FontSize.MEDIUM_15;

	private final OnInputChangedCallback onInputChangedCallback;

	public boolean drawStats = true;

	public GUIActivatableTextBar(InputState state, FontInterface fontSize, int characterCount, int lineCount, String inactiveText, GUIElement dependent, TextCallback textCallback, OnInputChangedCallback onInputChangedCallback) {
		this(state, fontSize, characterCount, lineCount, inactiveText, dependent, textCallback, null, onInputChangedCallback);
	}

	public void setDeleteOnEnter(boolean e) {
		area.deleteEntryOnEnter = e;
		assert (area.getOnInputChangedCallback() == onInputChangedCallback);
	}

	public GUIActivatableTextBar(InputState state, FontInterface fontSize, int characterCount, int lineCount, String inactiveText, GUIElement dependent, TextCallback textCallback, GUIInnerTextbox wrapAround, OnInputChangedCallback onInputChangedCallback) {
		super(state);
		this.onInputChangedCallback = onInputChangedCallback;
		this.dependend = dependent;
		this.wrapAround = wrapAround;
		area = new TextAreaInput(characterCount, lineCount, textCallback);
		area.setOnInputChangedCallback(onInputChangedCallback);
		area.onTabCallback = this;
		guiTextInput = new GUITextInput(10, 10, fontSize, state, true);
		guiTextInput.setTextInput(area);
		guiTextInput.onInit();
		cross = new GUIOverlay(IconDatabase.getIcons16(getState()), getState());
		cross.onInit();
		cross.setUserPointer("X");
		cross.getSprite().setTint(new Vector4f(1, 1, 1, 1));
		cross.setCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
					AudioController.fireAudioEventID(16);
					area.clear();
				}
			}
		});
		if (lineCount <= 1) {
			if (wrapAround == null) {
				hz = new GUIHorizontalArea(state, HButtonType.TEXT_FIELD, 10);
				hz.setMouseUpdateEnabled(true);
				hz.setCallback(this);
			} else {
				hz = new GUINormalBackground(state, (int) dependent.getWidth(), UIScale.getUIScale().h);
				hz.onInit();
				hz.setMouseUpdateEnabled(true);
				hz.setCallback(this);
			}
			background = new GUIScrollablePanel((int) dependent.getWidth(), hz.getHeight(), state);
			background.setScrollable(0);
			background.setLeftRightClipOnly = true;
			background.setContent(hz);
		} else {
			int lHeight = guiTextInput.getFont().getLineHeight() * lineCount;
			background = new GUIScrollablePanel((int) dependent.getWidth(), lHeight, state);
			background.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
			hz = new GUINormalForeground(state, (int) dependent.getWidth(), lHeight);
			hz.onInit();
			hz.setMouseUpdateEnabled(true);
			hz.setCallback(this);
			background.setContent(hz);
			areaStats = new GUITextOverlay(FontSize.SMALL_14, getState());
			areaStats.setTextSimple(new Object() {

				@Override
				public String toString() {
					return "(Characters Left: " + (area.getLimit() - area.getCache().length()) + "/" + area.getLimit() + "; Lines Left: " + ((area.getLineLimit() - area.getLineIndex()) - 2) + "/" + (area.getLineLimit() - 1) + ")";
				}
			});
			if(drawStats) attach(areaStats);
			else areaStats.setTextSimple("");
		}
		guiSearchInd = new GUITextOverlay(guiTextInput.getFontSize(), state) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#draw()
			 */
			@Override
			public void draw() {
				if (!active && area.getCache().length() == 0) {
					super.draw();
				}
			}
		};
		guiSearchInd.setTextSimple(inactiveText);
		guiSearchInd.onInit();
		guiSearchInd.setColor(0.6f, 0.6f, 0.6f, 1f);
		background.onInit();
	}

	public GUIActivatableTextBar(InputState state, FontInterface fontSize, String inactiveText, GUIElement dependent, TextCallback textCallback, OnInputChangedCallback onInputChangedCallback) {
		this(state, fontSize, 64, 1, inactiveText, dependent, textCallback, onInputChangedCallback);
	}

	public GUIActivatableTextBar(InputState state, FontInterface fontSize, GUIElement dependent, TextCallback textCallback, OnInputChangedCallback onInputChangedCallback) {
		this(state, fontSize, "SEARCH", dependent, textCallback, onInputChangedCallback);
	}

	public String getText() {
		return area.getCache();
	}

	public String getTT() {
		return area.toString();
	}

	public void appendText(String predefinedText) {
		area.append(predefinedText);
		area.update();
	}

	public void setText(final String predefinedText) {
		area.clear();
		area.append(predefinedText);
		assert (predefinedText.equals(area.getCache())) : predefinedText + "; " + area.getCache();
	}

	public void setTextWithoutCallback(String predefinedText) {
		OnInputChangedCallback onAppendCallback = area.getOnInputChangedCallback();
		area.setOnInputChangedCallback(null);
		area.clear();
		area.append(predefinedText);
		area.setOnInputChangedCallback(onAppendCallback);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			activateBar();
		}
	}

	@Override
	public boolean isOccluded() {
		return !dependend.isActive();
	}

	@Override
	public void cleanUp() {
		if (background != null) {
			background.cleanUp();
		}
		if (guiSearchInd != null) {
			guiSearchInd.cleanUp();
		}
		if (guiTextStats != null) {
			guiTextStats.cleanUp();
		}
		if (wrapAround != null) {
//			wrapAround.cleanUp();
		}
		if (guiTextInput != null) {
			guiTextInput.cleanUp();
		}
		if (icon != null) {
			icon.cleanUp();
		}
		if (cross != null) {
			cross.cleanUp();
		}
		if (hz != null) {
			hz.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (wrapAround != null) {
			wrapAround.tbHeight = Math.max(UIScale.getUIScale().h + UIScale.getUIScale().inset, guiTextInput.getInputBox().getTextHeight() + UIScale.getUIScale().inset * 2);
			hz.setHeight(wrapAround.tbHeight - UIScale.getUIScale().inset);
		// guiTextInput.setTextBox(wrapAround.tbHeight == 28);
		} else {
			if (area.getLineLimit() > 1) {
				hz.setHeight(Math.max(guiTextInput.getTextHeight() + 8, dependend.getHeight()));
			}
		}
		GlUtil.glPushMatrix();
		transform();
		int dWidth = (int) dependend.getWidth();
		int dPos = 0;
		if (leftDependentHalf) {
			dWidth = (int) dependend.getWidth() / 2;
		} else if (rightDependentHalf) {
			dWidth = (int) dependend.getWidth() / 2 - dependendDistanceFromRight;
			dPos = ((int) dependend.getWidth()) - dWidth - dependendDistanceFromRight;
		}
		dPos += offsetX;
		background.setPos(dPos, 0, 0);
		// GlUtil.translateModelview(dPos, 0, 0);
		background.setWidth(dWidth);
		if (area.getLineLimit() > 1) {
			background.setHeight(dependend.getHeight());
		} else {
			background.setHeight((int) Math.min(25, dependend.getHeight()));
		}
		guiTextInput.setWidth((int) background.getWidth());
		guiTextInput.setHeight((int) background.getHeight());
		if (areaStats != null) {
			if(drawStats) areaStats.setPos(4, getHeight() + UIScale.getUIScale().inset, 0);
			else areaStats.setTextSimple("");
		}
		if (background instanceof GUIScrollablePanel) {
			((GUIResizableElement) background.getContent()).setWidth(Math.max(guiTextInput.getMaxLineWidth() + 4, dWidth));
			guiTextInput.setWidth((int) ((GUIResizableElement) background.getContent()).getWidth());
			guiTextInput.setHeight((int) ((GUIResizableElement) background.getContent()).getHeight());
		}
		boolean nowActive = getState().getController().getInputController().getCurrentActiveField() == area;
		if (active && !nowActive) {
			onBecomingInactive();
		}
		active = nowActive;
		if (active) {
			guiTextInput.setColor(1f, 1f, 1f, 1f);
		} else {
			guiTextInput.setColor(0.6f, 0.6f, 0.6f, 1f);
		}
		if (icon != null && (!clearButtonEnabled || area.getCache().isEmpty())) {
			icon.setPos((int) (getWidth() - icon.getWidth() - 4), UIScale.getUIScale().inset, 0);
			icon.setInvisible(false);
		} else if (icon != null) {
			icon.setInvisible(true);
		}
		if (clearButtonEnabled && cross != null && !area.getCache().isEmpty()) {
			cross.setPos((int) (getWidth() - cross.getWidth() - 4), UIScale.getUIScale().inset, 0);
			cross.setInvisible(false);
			if (cross.isInside() && (cross.getCallback() == null || !cross.getCallback().isOccluded()) && isActive()) {
				cross.getSprite().getTint().set(1.0f, 1.0f, 1.0f, 1.0f);
			} else {
				cross.getSprite().getTint().set(0.5f, 0.5f, 0.5f, 1.0f);
			}
			cross.setSpriteSubIndex(0);
			cross.setMouseUpdateEnabled(true);
		} else if (cross != null) {
			cross.setInvisible(true);
			cross.setMouseUpdateEnabled(false);
		}
		background.draw();
		guiTextInput.setDrawCarrier(active);
		for (AbstractSceneNode e : getChilds()) {
			e.draw();
		}
		if (isRenderable() && isMouseUpdateEnabled()) {
			checkMouseInside();
		}
		GlUtil.glPopMatrix();
		cross.getSprite().getTint().set(1.0f, 1.0f, 1.0f, 1.0f);
	}

	protected void onBecomingInactive() {
	}

	@Override
	public void onInit() {
		if (!init) {
			background.onInit();
			if (background instanceof GUIScrollablePanel) {
				background.getContent().attach(guiTextInput);
				background.getContent().attach(guiSearchInd);
				if (icon != null) {
					background.getContent().attach(icon);
				}
				background.getContent().attach(cross);
			} else {
				background.attach(guiTextInput);
				background.attach(guiSearchInd);
				if (icon != null) {
					background.attach(icon);
				}
				background.attach(cross);
			}
			if (wrapAround != null) {
				guiTextInput.getInputBox().autoWrapOn = (GUIResizableElement) dependend;
				guiTextInput.onInit();
			}
			cross.setInvisible(true);
			guiTextInput.setPos(5, fontSize.getBarTopDist(), 0);
			guiSearchInd.setPos(guiTextInput.getPos());
		}
		init = true;
	}

	@Override
	public float getHeight() {
		return background.getHeight();
	}

	@Override
	public boolean isActive() {
		return super.isActive() && (dependend == null || dependend.isActive()) && (activeInterface == null || activeInterface.isActive());
	}

	@Override
	public float getWidth() {
		return background.getWidth();
	}

	public void setHeight(int height) {
		background.setHeight(height);
	}

	/**
	 * @return the active
	 */
	public boolean isBarActive() {
		return active;
	}

	public TextAreaInput getTextArea() {
		return area;
	}

	public void deactivateBar() {
		Keyboard.enableRepeatEvents(false);
		if (active) {
			onBecomingInactive();
			active = false;
		}
		getState().getController().getInputController().setCurrentActiveField(null);
		getState().getController().getInputController().setLastSelectedInput(null);
	}

	public void activateBar() {
		if (!isActive()) {
			deactivateBar();
			return;
		}
		Keyboard.enableRepeatEvents(true);
		active = true;
		getState().getController().getInputController().setCurrentActiveField(area);
		getState().getController().getInputController().setLastSelectedInput(area);
		if (selectAllOnClick) {
			area.selectAll();
		}
	}

	public void setMinimumLength(int i) {
		area.setMinimumLength(i);
	}

	/**
	 * add to be able to tab switch to
	 * @return
	 */
	public void addTextBarToSwitch(GUIActivatableTextBar messageTextBar) {
		toSwitch = (messageTextBar);
	}

	/**
	 * add to be able to tab switch to
	 * @return
	 */
	public GUIActivatableTextBar getToSwitch() {
		return toSwitch;
	}

	@Override
	public boolean catchTab(TextAreaInput textAreaInput) {
		if (toSwitch != null) {
			toSwitch.activateBar();
			return true;
		}
		return false;
	}

	public boolean isClearButtonEnabled() {
		return clearButtonEnabled;
	}

	public void setClearButtonEnabled(boolean clearButtonEnabled) {
		this.clearButtonEnabled = clearButtonEnabled;
	}

	public boolean isSelectAllOnClick() {
		return selectAllOnClick;
	}

	public void setSelectAllOnClick(boolean selectAllOnClick) {
		this.selectAllOnClick = selectAllOnClick;
	}

	public void reset() {
		area.clear();
	}

	@Override
	public void onEnter() {
		// deactivate bar on enter pressed
		deactivateBar();
	}
}
