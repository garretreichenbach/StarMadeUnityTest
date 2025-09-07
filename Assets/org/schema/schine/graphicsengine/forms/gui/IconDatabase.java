package org.schema.schine.graphicsengine.forms.gui;

import org.schema.common.util.StringTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class IconDatabase {
	private static final String corners32 = "UI 32px Corners-8x8-gui-";
	private static final String horizontal32 = "UI 32px-horizontals-1x32-gui-";

	private static final String icons16 = "UI 16px-8x8-gui-";

	public static GUIOverlay getBuildIcons(GUIOverlay d, int buildIconNum) {
		int bb = buildIconNum % 256;
		d.setSprite(getBuildIconsSprite(buildIconNum));
		d.setSpriteSubIndex(bb);
		return d;
	}

	public static GUIOverlay getBuildIconsInstance(InputState state, int buildIconNum) {
		GUIOverlay d = new GUIOverlay(getBuildIconsSprite(buildIconNum), state);
		getBuildIcons(d, buildIconNum);
		return d;
	}

	public static Sprite getBuildIconsSprite(int buildIconNum) {
		return Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "build-icons-" + StringTools.formatTwoZero(buildIconNum / 256) + "-16x16-gui-");
	}

	public static Sprite getIcons16(InputState state) {
		return Controller.getResLoader().getSprite(state.getGUIPath() + icons16);
	}

	public static Sprite getCorners32(InputState state) {
		return Controller.getResLoader().getSprite(state.getGUIPath() + corners32);
	}

	public static Sprite getHorizontal32(InputState state) {
		return Controller.getResLoader().getSprite(state.getGUIPath() + horizontal32);
	}

	public static GUIOverlay getSearchIconInstance16(InputState state) {
		GUIOverlay button = new GUIOverlay(getIcons16(state), state);
		button.setSpriteSubIndex(1);
		return button;
	}

	public static GUIOverlay getDownArrowInstance16(InputState state) {
		GUIOverlay button = new GUIOverlay(getIcons16(state), state);
		button.setSpriteSubIndex(5);
		return button;
	}

	public static GUIOverlay getUpArrowInstance16(InputState state) {
		GUIOverlay button = new GUIOverlay(getIcons16(state), state);
		button.setSpriteSubIndex(4);
		return button;
	}

	public static GUIOverlay getRightArrowInstance16(InputState state) {
		GUIOverlay button = new GUIOverlay(getIcons16(state), state);
		button.setSpriteSubIndex(6);
		return button;
	}

	public static GUIOverlay getLeftArrowInstance16(InputState state) {
		GUIOverlay button = new GUIOverlay(getIcons16(state), state);
		button.setSpriteSubIndex(7);
		return button;
	}

	public static GUIOverlay getLoadingIcon(InputState state) {
		GUIOverlay button = new GUIOverlay(getIcons16(state), state);
		button.setSpriteSubIndex(30);
		return button;
	}

	public static void updateLoadingIcon(GUIOverlay loadingIcon) {
		int spriteSubIndex = loadingIcon.getSpriteSubIndex();
		if(spriteSubIndex < 30 || spriteSubIndex > 37) {
			loadingIcon.setSpriteSubIndex(30);
			return;
		}
		int p = spriteSubIndex - 30;
		loadingIcon.setSpriteSubIndex(30 + ((p + 1) % 8));
	}

}
