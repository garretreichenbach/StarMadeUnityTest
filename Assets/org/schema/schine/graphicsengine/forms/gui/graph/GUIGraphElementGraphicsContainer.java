package org.schema.schine.graphicsengine.forms.gui.graph;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.input.InputState;

public abstract class GUIGraphElementGraphicsContainer {

	/**
	 * keeps track of graph for the graphics
	 */
	
	private final GUIGraphElementGraphicsGlobal global;
	
	
	
	
	public GUIGraphElementGraphicsContainer(GUIGraphElementGraphicsGlobal global) {
		super();
		this.global = global;
	}
	
	public InputState getState() {
		return getGlobal().getState();
	}
	public abstract Vector4f getConnectionColorTo();
	public abstract String getText();
	public abstract Vector4f getBackgroundColor();
	public abstract int getTextOffsetX();
	public abstract int getTextOffsetY();
	public abstract Vector4f getTextColor();
	public abstract FontInterface getFontSize();
	public abstract boolean isSelected();
	public int getBackgroundWidth(int maxLineWidth) {
		return maxLineWidth+getTextOffsetX()*2;
	}

	public int getBackgroundHeight(int textHeight) {
		return textHeight+getTextOffsetY()*2;
	}
	
	public abstract GUICallback getSelectionCallback();

	public GUIGraphElementGraphicsGlobal getGlobal() {
		return global;
	}
	
	public abstract String getToolTipText();

	public abstract GUIGraphElementBackground initiateBackground();

	public abstract void setBackgroundColor(GUIGraphElementBackground background);

	public abstract void setConnectionColor(GUIGraphConnection c);

	public void drawExtra(int width, int height) {
		
	}
		
}
