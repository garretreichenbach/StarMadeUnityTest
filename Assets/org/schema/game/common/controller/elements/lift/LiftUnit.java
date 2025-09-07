package org.schema.game.common.controller.elements.lift;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ActivateValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.physics.LiftBoxShape;
import org.schema.game.common.data.physics.RigidBodyExt;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.physics.Physics;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

public class LiftUnit extends ElementCollection<LiftUnit, LiftCollectionManager, VoidElementManager<LiftUnit, LiftCollectionManager>> {

	private float height = -1;

	private RigidBody body;

	private Transform t;

	private float maxHeight;

	private float timeSpendUp = 0;

	public void activate(boolean fromServer) {
		System.err.println("[LIFTUNIT] CHECK ACTIVATING LIFT: active status: " + isActive() + "; " + getSegmentController() + "; " + getSegmentController().getState() + "; " + this);
		if (!isActive()) {
			System.err.println("[LIFTUNIT] ACTIVATING LIFT " + getSegmentController() + "; " + getSegmentController().getState());
			Physics p = getSegmentController().getPhysics();
			LiftBoxShape bb = new LiftBoxShape(new Vector3f(Math.max(2, getMax(new Vector3i()).x - getMin(new Vector3i()).x), 0.2f, Math.max(2, getMax(new Vector3i()).z - getMin(new Vector3i()).z)));
			Vector3f pos = new Vector3f(getSignificator(new Vector3i()).x - SegmentData.SEG_HALF, getMin(new Vector3i()).y - SegmentData.SEG_HALF - 1f, getSignificator(new Vector3i()).z - SegmentData.SEG_HALF);
			getSegmentController().getWorldTransform().transform(pos);
			t = new Transform();
			t.setIdentity();
			t.origin.set(pos);
			t.basis.set(getSegmentController().getWorldTransform().basis);
			timeSpendUp = 0;
			height = 0;
			if (body != null) {
				p.removeObject(body);
			}
			bb.setUserPointer("lift");
			RigidBodyExt bodyFromShape = (RigidBodyExt) p.getBodyFromShape(bb, 0, t);
			bodyFromShape.setUserPointer(-424242);
			body = bodyFromShape;
			bodyFromShape.setUserPointer(getSegmentController().getId());
			maxHeight = getMax(new Vector3i()).y - getMin(new Vector3i()).y + 0.5f;
			p.addObject(body);
			if (!getSegmentController().isOnServer()) {
				((GameClientState) getSegmentController().getState()).getWorldDrawer().getLiftDrawer().updateActivate(this, true);
			}
		} else {
			if (!getSegmentController().isOnServer()) {
				((GameClientState) getSegmentController().getState()).getController().popupAlertTextMessage(Lng.str("Lift is busy\n(currently active)"), 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#cleanUp()
	 */
	@Override
	public void cleanUp() {
		deactivate();
		super.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#onChangeFinished()
	 */
	@Override
	public boolean onChangeFinished() {
		boolean onChangeFinished = super.onChangeFinished();
		deactivate();
		return onChangeFinished;
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create(state, Lng.str("Lift Module"), this, new ActivateValueEntry(new Object() {

			@Override
			public String toString() {
				return (isActive() ? Lng.str("Deactivate") : Lng.str("Activate"));
			}
		}) {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (isActive()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(894);
						deactivate();
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
						AudioController.fireAudioEventID(893);
						activate(false);
					}
				}
			}
		});
	}

	public void deactivate() {
		// System.err.println("[LIFT] DEACTIVATING LIFT");
		if (isActive()) {
			// System.err.println("[LIFT] DEACTIVATE ACTIVE LIFT");
			Physics p = getSegmentController().getPhysics();
			if (body != null) {
				p.removeObject(body);
			}
			height = -1;
			timeSpendUp = 0;
		}
	}

	/**
	 * @return the body
	 */
	public RigidBody getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(RigidBody body) {
		this.body = body;
	}

	public boolean isActive() {
		return height >= 0;
	}

	public void refreshLiftCapabilities() {
	}

	public void update(Timer timer) {
		if (isActive()) {
			// System.err.println("[LIFTUNIT] UPDATING : active status: "+isActive()+"; "+getSegmentController()+"; "+getSegmentController().getState()+" "+this);
			float d = timer.getDelta() * Math.max(1f, (getMax(new Vector3i()).y - getMin(new Vector3i()).y) / SegmentData.SEGf);
			Vector3f up = GlUtil.getUpVector(new Vector3f(), getSegmentController().getWorldTransform());
			if (height < maxHeight) {
				height += d;
				up.scale(d);
				t.origin.add(up);
				t.basis.set(getSegmentController().getWorldTransform().basis);
				body.setActivationState(CollisionObject.ACTIVE_TAG);
				// System.err.println("LIFT: "+t.origin+"  "+controller.getState()+" "+body.getActivationState());
				body.getMotionState().setWorldTransform(t);
				body.setWorldTransform(t);
			} else {
				timeSpendUp += d;
				// System.err.println("TIME:a "+timeSpendUp);
				if (timeSpendUp > 5) {
					deactivate();
				}
			}
		}
	}
}
