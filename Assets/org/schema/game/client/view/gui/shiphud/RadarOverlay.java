package org.schema.game.client.view.gui.shiphud;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;

import com.bulletphysics.linearmath.Transform;

public class RadarOverlay extends GUIElement {

	Vector4f tint = new Vector4f();
	private GameClientState state;
	private float size;
	private GUITextOverlay mapText;
	private Transform t = new Transform();
	private Vector3f c = new Vector3f();

	public RadarOverlay(InputState state, float size) {
		super(state);
		this.state = (GameClientState) state;
		this.size = size;
		mapText = new GUITextOverlay(state);
		mapText.setTextSimple("Open Galaxy Map (" + KeyboardMappings.MAP_PANEL.getKeyChar() + ")");
		mapText.setPos(0, 124, 0);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		SimpleTransformableSendableObject currentPlayerObject = state.getCurrentPlayerObject();
		if (currentPlayerObject != null) {
			if (currentPlayerObject instanceof SegmentController) {
				t.set(((SegmentController) currentPlayerObject).getWorldTransformInverse());

			} else if (currentPlayerObject instanceof AbstractCharacter<?>) {
				t.set(Controller.getCamera().getWorldTransform());
				t.inverse();
			} else {
				t.set(currentPlayerObject.getWorldTransform());
				t.inverse();
			}

			PlayerInteractionControlManager playerIntercationManager = ((GameClientState) getState()).
					getGlobalGameControlManager().
					getIngameControlManager().
					getPlayerGameControlManager().
					getPlayerIntercationManager();

			SimpleTransformableSendableObject selectedEntity = playerIntercationManager.getSelectedEntity();

			GL11.glPointSize(2);
			GL11.glBegin(GL11.GL_POINTS);
			for (SimpleTransformableSendableObject o : state.getCurrentSectorEntities().values()) {

				c.set(o.getWorldTransformOnClient().origin);
				t.transform(c);
				c.scale(0.07f);
				if (c.length() < size / 2) {
					HudIndicatorOverlay.getColor(o, tint, selectedEntity == o, state);
					c.x = (size / 2 - c.x);
					c.z = (size / 2 - c.z);
					GlUtil.glColor4f(tint.x, tint.y, tint.z, tint.w);
					GL11.glVertex2f(c.x, c.z);
				}
			}
			GlUtil.glColor4f(1, 1, 1, 1);
			GL11.glEnd();

		}

		mapText.draw();
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
