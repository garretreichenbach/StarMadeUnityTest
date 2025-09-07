package org.schema.game.client.view.shards;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.RigidDebrisBody;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.TransformaleObjectTmpVars;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Mesh;

import com.bulletphysics.linearmath.Transform;

public class Shard {

	private static final float MAX_LIFE_TIME_SECS_SLOW = 3;
	private static TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
	public final RigidDebrisBody body;
	public final Mesh hull;
	private float lifeTimeSecs;
	private short type = 1;
	private int sectorId;
	private Transform tmpLocal = new Transform();
	private Transform t = new Transform();
	private boolean killFlag;
	private float tl;
	private float slowLifeTimeSecs;
	private Vector3f gravity;

	public Shard(RigidDebrisBody bodyFromShape, Mesh convexHC, int sectorId, Vector3f gravity) {
		this.body = bodyFromShape;
		this.hull = convexHC;
		this.sectorId = sectorId;
		this.gravity = gravity;

		bodyFromShape.shard = this;

		int ltf = EngineSettings.D_LIFETIME_NORM.getInt();

		lifeTimeSecs = (float) (ltf + Math.random() * (ltf * 0.5f));

		slowLifeTimeSecs = (float) (MAX_LIFE_TIME_SECS_SLOW + Math.random() * MAX_LIFE_TIME_SECS_SLOW);
	}

	public void draw() {

		body.getMotionState().getWorldTransform(t);

		GlUtil.glPushMatrix();

		GlUtil.glMultMatrix(t);

		hull.draw();

		GlUtil.glPopMatrix();
	}

	public void drawBulk() {

		if (Controller.getCamera() != null && !Controller.getCamera().isBoundingSphereInFrustrum(t.origin, 1.42f)) {
			return;
		}

		GlUtil.glPushMatrix();

		GlUtil.glMultMatrix(t);

		float scale = 1f - (0.25f * (Math.min(1f, tl * 3f)));
		GlUtil.scaleModelview(scale, scale, scale);
		ElementInformation info = ElementKeyMap.getInfo(type);
		GlUtil.glColor4f(info.getTextureId(0), Math.min(1, Math.min(lifeTimeSecs, slowLifeTimeSecs)), 0, 1);

		hull.drawVBO();

		GlUtil.glPopMatrix();
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(short type) {
		this.type = type;
	}

	public boolean update(Timer timer, GameClientState state, int slow) {

		body.setGravity(gravity);

		tl += timer.getDelta();
		body.getMotionState().getWorldTransform(tmpLocal);

		RemoteSector to = (RemoteSector) state.getLocalAndRemoteObjectContainer()
				.getLocalObjects().get(sectorId);

		if (to == null) {
			slowLifeTimeSecs -= timer.getDelta() * 5f;
		} else {
			SimpleTransformableSendableObject.calcWorldTransformRelative(state.getCurrentSectorId(), state.getPlayer().getCurrentSector(), sectorId, tmpLocal, state, false, t, v);

			lifeTimeSecs -= timer.getDelta();

			if (killFlag) {
				slowLifeTimeSecs -= timer.getDelta() * 2f;
			}

			if (slow > 0 || slowLifeTimeSecs < 1f) {
				//either start with slow or finish of if already half transparent
				slowLifeTimeSecs -= slow * timer.getDelta();
			}
		}
		return slowLifeTimeSecs > 0 && lifeTimeSecs > 0;
	}

	public void kill() {
		if (!killFlag) {
			if (slowLifeTimeSecs > 1.0f) {
				slowLifeTimeSecs = 1.0f;
			}
			killFlag = true;
		}
	}

	public boolean isKilled() {
		return killFlag;
	}
}
