package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Mouse;

public class GUIContextPane extends GUIAnchor {

	public boolean drawnOnce;

	public GUIContextPane(InputState state, float width, float height) {
		super(state, width, height);

		this.getPos().set(Mouse.getX(), Mouse.getY(), 0);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		drawnOnce = true;
		Vector3f posBef = new Vector3f(getPos());
		
		int fHeight = (GLFrame.getHeight() - 10);
		int fWidth = (GLFrame.getWidth() - 10);
		int diffY = (int) (posBef.y + getHeight() - fHeight);
		if(diffY > 0){
			getPos().y -= diffY;
		}
		int diffX = (int) (posBef.x + getWidth() - fWidth);
		if(diffX > 0){
			getPos().x -= diffX;
		}
		
		super.draw();
		
		
		getPos().set(posBef);
	}

}
