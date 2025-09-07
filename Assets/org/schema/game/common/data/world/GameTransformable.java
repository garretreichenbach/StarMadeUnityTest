package org.schema.game.common.data.world;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.physics.Physical;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SolverConstraint;
import com.bulletphysics.linearmath.Transform;

public interface GameTransformable extends SimpleTransformable, Physical{

	public void engageWarp(String warpToUID, boolean allowDirect, long delay, Vector3i local, int maxDist);

	public void sendControllingPlayersServerMessage(Object[] astr, int messageTypeError);

	public boolean handleCollision(int index, RigidBody originalBody,
            RigidBody originalBody2, SolverConstraint contactConstraint);

	public int getSectorId();

	public void getGravityAABB(Transform serverTransform, Vector3f min,
			Vector3f max);

	public int getId();

	

}
