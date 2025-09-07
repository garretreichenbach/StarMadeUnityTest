package org.schema.schine.graphicsengine.forms.gui.newgui.config;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.gui.ColorPalletteInterface;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;

public class ButtonColorImpl implements ColorPalletteInterface {

	@Override
	public void setColorPallete(ColorPalette c, GUITextButton button) {
		assert (button != null);
		assert (c != null);
		switch(c) {
			case OK -> {
				button.getBackgroundColor().set(ButtonColorPalette.ok);
				button.getBackgroundColorMouseOverlay().set(ButtonColorPalette.okMouseOver);
				button.getBackgroundColorMouseOverlayPressed().set(ButtonColorPalette.okPressed);
				button.getColorText().set(ButtonColorPalette.okText);
				button.getColorTextPressed().set(ButtonColorPalette.okTextPressed);
				button.getColorTextMouseOver().set(ButtonColorPalette.okTextMouseOver);
			}
			case CANCEL -> {
				button.getBackgroundColor().set(ButtonColorPalette.cancel);
				button.getBackgroundColorMouseOverlay().set(ButtonColorPalette.cancelMouseOver);
				button.getBackgroundColorMouseOverlayPressed().set(ButtonColorPalette.cancelPressed);
				button.getColorText().set(ButtonColorPalette.cancelText);
				button.getColorTextPressed().set(ButtonColorPalette.cancelTextPressed);
				button.getColorTextMouseOver().set(ButtonColorPalette.cancelTextMouseOver);
			}
			case FRIENDLY -> {
				button.getBackgroundColor().set(ButtonColorPalette.friendly);
				button.getBackgroundColorMouseOverlay().set(ButtonColorPalette.friendlyMouseOver);
				button.getBackgroundColorMouseOverlayPressed().set(ButtonColorPalette.friendlyPressed);
				button.getColorText().set(ButtonColorPalette.friendlyText);
				button.getColorTextPressed().set(ButtonColorPalette.friendlyTextPressed);
				button.getColorTextMouseOver().set(ButtonColorPalette.friendlyTextMouseOver);
			}
			case HOSTILE -> {
				button.getBackgroundColor().set(ButtonColorPalette.hostile);
				button.getBackgroundColorMouseOverlay().set(ButtonColorPalette.hostileMouseOver);
				button.getBackgroundColorMouseOverlayPressed().set(ButtonColorPalette.hostilePressed);
				button.getColorText().set(ButtonColorPalette.hostileText);
				button.getColorTextPressed().set(ButtonColorPalette.hostileTextPressed);
				button.getColorTextMouseOver().set(ButtonColorPalette.hostileTextMouseOver);
			}
			case TRANSPARENT -> {
				button.getBackgroundColor().set(new Vector4f(0, 0, 0, 0));
				button.getBackgroundColorMouseOverlay().set(new Vector4f(0, 0, 0, 0));
				button.getBackgroundColorMouseOverlayPressed().set(new Vector4f(0, 0, 0, 0));
				button.getColorText().set(new Vector4f(0.6f, 0.6f, 0.6f, 1.0f));
				button.getColorTextPressed().set(new Vector4f(0.95f, 0.95f, 1.0f, 1.0f));
				button.getColorTextMouseOver().set(new Vector4f(0.9f, 0.9f, 0.9f, 1.0f));
			}
			case NEUTRAL -> {
				button.getBackgroundColor().set(ButtonColorPalette.neutral);
				button.getBackgroundColorMouseOverlay().set(ButtonColorPalette.neutralMouseOver);
				button.getBackgroundColorMouseOverlayPressed().set(ButtonColorPalette.neutralPressed);
				button.getColorText().set(ButtonColorPalette.neutralText);
				button.getColorTextPressed().set(ButtonColorPalette.neutralTextPressed);
				button.getColorTextMouseOver().set(ButtonColorPalette.neutralTextMouseOver);
			}
			case TUTORIAL -> {
				button.getBackgroundColor().set(ButtonColorPalette.tutorial);
				button.getBackgroundColorMouseOverlay().set(ButtonColorPalette.tutorialMouseOver);
				button.getBackgroundColorMouseOverlayPressed().set(ButtonColorPalette.tutorialPressed);
				button.getColorText().set(ButtonColorPalette.tutorialText);
				button.getColorTextPressed().set(ButtonColorPalette.tutorialTextPressed);
				button.getColorTextMouseOver().set(ButtonColorPalette.tutorialTextMouseOver);
			}
			default -> {
			}
		}
	}

}
