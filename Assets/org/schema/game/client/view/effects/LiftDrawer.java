package org.schema.game.client.view.effects;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.lift.LiftUnit;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.texture.Material;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.Transform;

public class LiftDrawer implements Drawable {

	private final ArrayList<LiftUnit> activeUnits = new ArrayList<LiftUnit>();
	private boolean init;
	private Mesh box;
	private Transform wt = new Transform();
	private Vector3f half = new Vector3f();
	private Material material;

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}

		for (int i = 0; i < activeUnits.size(); i++) {
			SegmentController controller = activeUnits.get(i).getSegmentController();
			if (!activeUnits.get(i).isActive() || controller.getSectorId() != ((GameClientState) controller.getState()).getCurrentSectorId() ||
					!((GameClientState) controller.getState()).getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().containsKey(controller.getId())) {
				activeUnits.get(i).deactivate();
				activeUnits.remove(i);
				i--;
				continue;
			}

			LiftUnit liftUnit = activeUnits.get(i);
			ShaderLibrary.perpixelShader.loadWithoutUpdate();
			material.attach(0);
			if (liftUnit.getBody() != null) {
				Integer in = ((Integer) liftUnit.getBody().getUserPointer());
				Sendable sendable = controller.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(in);
				if (sendable == null || !(sendable instanceof SegmentController)) {
					System.err.println("[CLIENT][ERROR] lift cannot be drawn: objectID " + in + " -> " + sendable);
					activeUnits.get(i).deactivate();
					activeUnits.remove(i);
					continue;
				}

				if (((SegmentController) sendable).getSectorId() != ((GameClientState) controller.getState()).getCurrentSectorId()) {
					activeUnits.get(i).deactivate();
					activeUnits.remove(i);
					i--;
					continue;
				}

				liftUnit.getBody().getWorldTransform(wt);
				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(wt);

				BoxShape boxShape = (BoxShape) liftUnit.getBody().getCollisionShape();
				boxShape.getHalfExtentsWithoutMargin(half);
				GlUtil.scaleModelview(half.x * 2, half.y * 2, half.z * 2);

				box.draw();

				GlUtil.glPopMatrix();
			}
			material.detach();
			ShaderLibrary.perpixelShader.unloadWithoutExit();
		}
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		box = (Mesh) Controller.getResLoader().getMesh("Box").getChilds().get(0);

		material = new Material();
		material.setShininess(64);
		material.setSpecular(new float[]{1.3f, 1.3f, 1.3f, 1.0f});
		init = true;
	}

	public void updateActivate(LiftUnit u, boolean active) {

		if (active) {
			activeUnits.add(u);
		}
	}

}
