package org.schema.game.client.view.gamemap;

import javax.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.objects.container.TransformTimed;

public class GameMapPosition implements Transformable {

	private final GameMapDrawer gameMapDrawer;
	private final TransformTimed transform;
	private final Vector3f oldPosition = new Vector3f();
	private final Vector3f newPosition = new Vector3f();
	private final Vector3f tmp = new Vector3f();
	private final Vector3i tmpSysPosA = new Vector3i();
	private Vector3i secPos = new Vector3i();
	private Vector3i currentSysPos;

	public GameMapPosition(GameClientState state, GameMapDrawer gameMapDrawer) {
		this.gameMapDrawer = gameMapDrawer;
		this.transform = new TransformTimed();
		transform.setIdentity();
	}

	public void add(int xSec, int ySec, int zSec, float offset, boolean immediate) {
		if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			secPos.add(xSec, ySec, zSec);
		} else {
			secPos.add(xSec * VoidSystem.SYSTEM_SIZE, ySec * VoidSystem.SYSTEM_SIZE, zSec * VoidSystem.SYSTEM_SIZE);
		}
		apply(immediate);
	}

	private void apply(boolean immediate) {

		float x = (secPos.x);
		float y = (secPos.y);
		float z = (secPos.z);

		//		oldPosition.set(getWorldTransform().origin);

		newPosition.set(
				(x - 7.5f) * (GameMapDrawer.size / VoidSystem.SYSTEM_SIZEf),
				(y - 7.5f) * (GameMapDrawer.size / VoidSystem.SYSTEM_SIZEf),
				(z - 7.5f) * (GameMapDrawer.size / VoidSystem.SYSTEM_SIZEf)
		);
		if (immediate) {
			transform.origin.set(newPosition);
			oldPosition.set(newPosition);
		}
		currentSysPos = VoidSystem.getPosFromSector(secPos, tmpSysPosA);
		gameMapDrawer.checkSystem(secPos);
	}

	public Vector3i get(Vector3i out) {
		out.set(secPos);
		return out;
	}

	/**
	 * @return the currentSysPos
	 */
	public Vector3i getCurrentSysPos() {
		return currentSysPos;
	}

	@Override
	public TransformTimed getWorldTransform() {
		return transform;
	}

	public boolean isActive(Vector3i sysPosToCheck) {
		return currentSysPos.equals(sysPosToCheck);
	}

	public void set(int xSec, int ySec, int zSec, boolean immediate) {
		secPos.set(xSec, ySec, zSec);

		System.err.println("[CLIENT][MAP][POS] SETTING TO " + secPos);
		apply(immediate);

	}

	public void set(Vector3i pos, boolean immediate) {
		secPos.set(pos);
		apply(immediate);
	}

	public void update(Timer timer) {

		if (!oldPosition.epsilonEquals(newPosition, 0.001f)) {
			tmp.sub(newPosition, oldPosition);
			float len = tmp.length();
			tmp.normalize();
			tmp.scale(timer.getDelta() * 10f);
			if (len > 1) {
				tmp.scale(len);
			}
			if (len < tmp.length()) {
				oldPosition.set(newPosition);
			} else {
				oldPosition.add(tmp);
			}

		} else {
			oldPosition.set(newPosition);
		}

		transform.origin.set(oldPosition);
	}

}
