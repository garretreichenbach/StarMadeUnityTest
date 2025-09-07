package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.input.InputState;

public class GUIColoredUnderlayRectangle extends GUIColoredRectangle {

	private GUIElement underlay;

	public GUIColoredUnderlayRectangle(InputState state, int width, int height, Vector4f color, GUIElement underlay) {
		super(state, width, height, color);
		this.setColor(color);
		this.underlay = underlay;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {

		if (underlay != null) {
			underlay.draw();
			if (underlay.isMouseUpdateEnabled()) {
				underlay.checkMouseInside();
			}
		}

		super.draw();

	}

}
