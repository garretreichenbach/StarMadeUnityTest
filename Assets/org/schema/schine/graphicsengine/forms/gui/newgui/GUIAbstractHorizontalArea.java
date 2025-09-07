package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIResizableElement;
import org.schema.schine.input.InputState;

public abstract class GUIAbstractHorizontalArea extends GUIResizableElement {
	public GUIAbstractHorizontalArea(InputState state) {
		super(state);
	}
	protected GUIActivationCallback actCallback;
	public GUIActiveInterface activeInterface;
	public static final int smallButtonHeight = UIScale.getUIScale().smallButtonHeight;
	public static float HORIZONTALS_TILING = 32;
	public int spacingButtonIndexX = 1;
	protected boolean changedSize;
	protected float width;
	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		if (this.width != width) {
			changedSize = true;
		}
		
		this.width = width;
		super.setWidth(width);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
	}

	@Override
	public void setWidth(float width) {
		if (this.width != width) {
			changedSize = true;
		}
		
		this.width = width;

	}
	protected abstract void adjustWidth();
}
