package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementGraphicsContainer;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementGraphicsGlobal;

public abstract class ReactorGraphContainer extends GUIGraphElementGraphicsContainer{


	public ReactorGraphContainer(GUIGraphElementGraphicsGlobal global) {
		super(global);
	}

	public Vector4f customBackgroundColor;
	protected static Vector4f textColor0 = new Vector4f(1f,1f,1f,1f);
	protected static Vector4f neutral = new Vector4f(1,1,1,0.9f);
	protected static Vector4f backgroundColor0 = new Vector4f(0.3f,0.2f,0.2f,0.8f);

	protected static Vector4f backgroundColorGrey = new Vector4f(0.2f,0.2f,0.2f,0.8f);
	protected static Vector4f backgroundColorRed = new Vector4f(0.6f,0.2f,0.2f,0.8f);
	protected static Vector4f backgroundColorGreen = new Vector4f(0.2f,0.6f,0.2f,0.8f);
	protected static Vector4f backgroundColorBlue = new Vector4f(0.2f,0.2f,0.6f,0.8f);
	protected static Vector4f backgroundColorYellow = new Vector4f(0.2f,0.6f,0.6f,0.8f);

	protected static Vector4f connectionColorGrey = new Vector4f(0.8f,0.8f,0.8f,1.0f);
	protected static Vector4f connectionColorYellow = new Vector4f(0.5f,0.8f,0.8f,1.0f);
	protected static Vector4f connectionColorBlue = new Vector4f(0.5f,0.5f,0.8f,1.0f);
	protected static Vector4f connectionColorRed = new Vector4f(1.0f,0.4f,0.4f,1.0f);
	protected static Vector4f connectionColorGreen = new Vector4f(0.4f,1.0f,0.4f,1.0f);
	//0 = blue, 1 = orange, 2 = red, 3 = green
	public static final int CONNECTION_BLUE = 0;
	public static final int CONNECTION_ORANGE = 1;
	public static final int CONNECTION_RED = 2;
	public static final int CONNECTION_GREEN = 3;
	

	@Override
	public Vector4f getBackgroundColor() {
		return customBackgroundColor == null ? backgroundColor0 : customBackgroundColor;
	}

	@Override
	public Vector4f getTextColor() {
		return textColor0;
	}

	@Override
	public FontInterface getFontSize() {
		return FontSize.MEDIUM_15;
	}
	@Override
	public int getTextOffsetX() {
		return 24;
	}

	@Override
	public int getTextOffsetY() {
		return 24;
	}
}
