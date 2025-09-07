package org.schema.game.client.view.gui;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIIconButton;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.Suspendable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUIBlockIconButton extends GUIIconButton {

	public final short type;

	public GUIBlockIconButton(InputState state, short type, GUICallback callback, Suspendable s) {
		super(state, 64, 64, new GUIOverlay(getLayer(type), state), callback, s);
		image.setSpriteSubIndex(getSubSprite(type));
		this.type = type;
	}

	public GUIBlockIconButton(InputState state,
	                          short type, GUICallback callback) {
		super(state, 64, 64, new GUIOverlay(getLayer(type), state), callback);
		image.setSpriteSubIndex(getSubSprite(type));
		this.type = type;
	}

	public GUIBlockIconButton(InputState state,
	                          Vector4f backgroundColor, Vector4f foregroundColor,
	                          short type, GUICallback callback, Suspendable s) {
		super(state, 64, 64, backgroundColor, foregroundColor, new GUIOverlay(getLayer(type), state), callback, s);
		image.setSpriteSubIndex(getSubSprite(type));
		this.type = type;
	}

	public GUIBlockIconButton(InputState state,
	                          Vector4f backgroundColor, Vector4f foregroundColor,
	                          short type, GUICallback callback) {
		super(state, 64, 64, backgroundColor, foregroundColor, new GUIOverlay(getLayer(type), state), callback);
		image.setSpriteSubIndex(getSubSprite(type));
		this.type = type;
	}

	public static int getSubSprite(short type) {
		if (ElementKeyMap.isValidType(type)) {
			return ElementKeyMap.getInfo(type).getBuildIconNum() % 256;
		} else if (type < 0) {
			return Math.abs(type);
		} else {
			//invisible
			return 511;
		}
	}

	public static Sprite getLayer(short type) {
		if (ElementKeyMap.isValidType(type)) {
			int layer = ElementKeyMap.getInfo(type).getBuildIconNum() / 256;
			return Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-" + StringTools.formatTwoZero(layer) + "-16x16-gui-");

		} else {
			return Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"meta-icons-00-16x16-gui-");
		}
	}

}
