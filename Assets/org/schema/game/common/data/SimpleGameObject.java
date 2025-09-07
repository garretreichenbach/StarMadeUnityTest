package org.schema.game.common.data;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.StateInterface;

import com.bulletphysics.linearmath.Transform;

public interface SimpleGameObject extends Transformable {
	byte SIMPLE_TRANSFORMABLE_SENSABLE_OBJECT = 0;
	byte MISSILE = 1;
	byte MINABLE = 2;
	byte MINE = 3;

	public boolean existsInState();

	public StateInterface getState();

	public int getSectorId();

	public void calcWorldTransformRelative(int sectorId, Vector3i pos);

	public Transform getClientTransform();

	public Transform getClientTransformCenterOfMass(Transform out);

	public Vector3f getCenterOfMass(Vector3f out);

	public AbstractOwnerState getOwnerState();

	public Vector3f getLinearVelocity(Vector3f out);

	public boolean isInPhysics();

	public boolean isHidden();

	public int getAsTargetId();

	public byte getTargetType();

	public Transform getWorldTransformOnClient();

	public void transformAimingAt(Vector3f to,
	                              Damager from,
	                              SimpleGameObject target, Random random, float deviation);

	public int getFactionId();

	public long getOwnerId();
}
