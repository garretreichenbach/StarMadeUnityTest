package org.schema.game.client.view.gui;

import org.schema.schine.common.TextAreaInput;
import org.schema.schine.common.TextInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollableTextPanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextInput;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.network.client.ClientState;

public class GUITextAreaInputPanel extends GUIInputPanel {
	private final int areaWidth;
	private final int areaHeight;
	private GUITextInput guiTextInput;
	private TextAreaInput input;
	private GUIScrollableTextPanel scrollPane;
	private GUITextOverlay areaStats;
	private FontInterface font;

	public GUITextAreaInputPanel(String windowId, ClientState state, int areaWidth, int areaHeight, GUICallback guiCallback,
	                             Object info, Object description, TextAreaInput textInput, FontInterface font, boolean mouseActive) {
		this(windowId, state, 500, 240, areaWidth, areaHeight, guiCallback, info, description, textInput, font, mouseActive);
	}

	public GUITextAreaInputPanel(String windowId, ClientState state, int width, int height, int areaWidth, int areaHeight, GUICallback guiCallback,
	                             Object info, Object description, TextAreaInput textInput, FontInterface font, boolean mouseActive) {
		super(windowId, state, width, height, guiCallback, info, description);

		this.areaWidth = width;
		this.areaHeight = height;
		this.font = font;
		guiTextInput = new GUITextInput(256, 32, font, state, mouseActive);
		guiTextInput.setPreText("");
		guiTextInput.setTextInput(textInput);
		this.input = textInput;
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		guiTextInput.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#draw()
	 */
	@Override
	public void draw() {
		if (scrollPane != null) {
			guiTextInput.setHeight(guiTextInput.getTextHeight());
			scrollPane.setWidth(scrollableContent.getWidth());
			scrollPane.setHeight(scrollableContent.getHeight());
			areaStats.getPos().set(getButtonOK().getPos().x, getButtonOK().getPos().y - 19, 0);
		}
		super.draw();

	}

	@Override
	public void onInit() {
		super.onInit();
		guiTextInput.onInit();
		scrollPane = new GUIScrollableTextPanel(areaWidth, areaHeight, getState());
		scrollPane.setContent(guiTextInput);

		guiTextInput.getPos().x = 2;
		scrollPane.setPos(getContent().getPos());
		getBackground().attach(scrollPane);
		GUIDialogWindow c = ((GUIDialogWindow) getBackground());
		c.innerHeightSubstraction = UIScale.getUIScale().W_innerHeightSubstraction_textarea;

		areaStats = new GUITextOverlay(font, getState());

		areaStats.setTextSimple(new Object() {
			@Override
			public String toString() {
				return Lng.str("(Characters Left: %d/%d; New Lines Left: %d/%d)", 
					(input.getLimit() - input.getCache().length()), 
					input.getLimit(), 
					((input.getLineLimit() - input.getLineIndex()) - 1), 
					(input.getLineLimit() - 1));
			}

		});
		areaStats.getPos().set(getButtonOK().getPos().x, getButtonOK().getPos().y - 19, 0);

		getBackground().attach(areaStats);
	}

	@Override
	public void newLine() {
		scrollPane.scrollToCarrier(guiTextInput);
	}

	public void setInput(TextInput textInput) {
		if (guiTextInput == null) {
			guiTextInput = new GUITextInput(256, 32, getState());
			guiTextInput.setPreText("");
			guiTextInput.getPos().x = 2;
			scrollPane = new GUIScrollableTextPanel(areaWidth, areaHeight, getState());
			scrollPane.setContent(guiTextInput);
			scrollPane.setPos(getContent().getPos());
			if (getBackground() != null) {
				getBackground().attach(scrollPane);
			}

		}
		this.guiTextInput.setTextInput(textInput);

	}

	/**
	 * @return the guiTextInput
	 */
	public GUITextInput getGuiTextInput() {
		return guiTextInput;
	}

}
