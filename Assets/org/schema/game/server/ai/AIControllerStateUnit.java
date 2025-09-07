package org.schema.game.server.ai;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.ai.stateMachines.AIGameEntityState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.network.StateInterface;

import com.bulletphysics.linearmath.Transform;

public abstract class AIControllerStateUnit<E extends SimpleTransformableSendableObject> implements ControllerStateInterface {
	
	@Override
	public Vector3f getForward(Vector3f out) {
		return GlUtil.getForwardVector(out, getEntity().getWorldTransform());
	}

	@Override
	public Vector3f getUp(Vector3f out) {
		return GlUtil.getUpVector(out, getEntity().getWorldTransform());
	}

	@Override
	public Vector3f getRight(Vector3f out) {
		return GlUtil.getRightVector(out, getEntity().getWorldTransform());
	}
	
	private AIGameEntityState<E> entState;
	public AIControllerStateUnit(AIGameEntityState<E> entState) {
		super();
		
		this.entState = entState;
	}
	
	public E getEntity(){
		return entState.getEntity();
	}
	public StateInterface getState(){
		return entState.getState();
	}
	
	public boolean isOnServer(){
		return entState.isOnServer();
	}
	protected final Transform localTransform = new Transform();
	protected final Transform outputTransform = new Transform();
	
}
