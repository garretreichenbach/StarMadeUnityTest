package org.schema.schine.graphicsengine.forms.gui.graph;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector2fTools;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class GUIGraphElement extends GUIElement {

	private GUIElement content;

	public GUIGraphElement(InputState state) {
		super(state);
	}
	public GUIGraphElement(InputState state, GUIElement content) {
		super(state);

		this.content = content;
	}

	public void setContent(GUIElement content){
		this.content = content;
	}
	@Override
	public void cleanUp() {
		if(this.content != null){
			this.content.cleanUp();
		}
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();

		transform();

		this.content.draw();
		for (AbstractSceneNode e : getChilds()) {
			e.draw();
		}

		if (isMouseUpdateEnabled()) {
			checkMouseInside();
		} else {

		}

		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
	}

	@Override
	public float getHeight() {
		return content.getHeight();
	}

	@Override
	public float getWidth() {
		return content.getWidth();
	}

	public Vector2f getCenter() {
		return new Vector2f((int)(getPos().x + getWidth() / 2), (int)(getPos().y + getHeight() / 2));
	}

	public Vector2f getBoxIntersectionTo(Vector2f center) {

		Vector2f c = getCenter();

		Vector2f intersectsFromInside = Vector2fTools.intersectsFromInside(
				getPos().x, getPos().y, getWidth(), getHeight(), c, center);
//		assert(intersectsFromInside != null):getPos()+"; "+content.getWidth()+", "+content.getHeight()+"; "+c+"; "+center;
		return intersectsFromInside;
	}
	public Vector4f getAsClipPaneScreen() {
		return new Vector4f(getPos().x, getPos().x+getWidth(), getPos().y, getPos().y+getHeight());
	}

}
