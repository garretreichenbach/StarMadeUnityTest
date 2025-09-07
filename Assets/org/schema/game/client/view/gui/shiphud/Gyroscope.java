package org.schema.game.client.view.gui.shiphud;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

import com.bulletphysics.linearmath.Transform;

public class Gyroscope extends GUIElement {

	Transform t = new Transform();
	private GameClientState state;
	private Transform inv = new Transform();

	public Gyroscope(InputState state) {
		super(state);
		this.state = (GameClientState) state;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		Ship ship = state.getShip();
		if (ship != null) {

			Vector3f rightNorm = new Vector3f(1, 0, 0);
			Vector3f upNorm = new Vector3f(0, 1, 0);
			Vector3f botNorm = new Vector3f(0, -1, 0);

			Vector3f rightCur = GlUtil.getRightVector(new Vector3f(), ship.getWorldTransform());
			Vector3f upCur = GlUtil.getUpVector(new Vector3f(), ship.getWorldTransform());
			Vector3f botCur = GlUtil.getBottomVector(new Vector3f(), ship.getWorldTransform());

			float pUp = (upNorm.dot(upCur));
			float pB = (botNorm.dot(upCur));

			float abs = (pUp + 1f) / 2;
			float p = abs;
			//			System.err.println("pppp "+p+";   "+upNorm.angle(upCur)+" / "+botNorm.angle(upCur));

			float heigth = p * getHeight();

			float tilt = rightNorm.dot(rightCur);

			GL11.glBegin(GL11.GL_LINES);
			GlUtil.glColor4f(1, 1, 1, 0.4f);
			GL11.glVertex2f(0, heigth);
			GL11.glVertex2f(getWidth(), heigth);
			GL11.glEnd();
		}
//		drawCircleHUD();

	}

	@Override
	public void onInit() {

	}

	@Override
	public float getHeight() {
		return 128;
	}

	@Override
	public float getWidth() {
		return 128;
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

}
