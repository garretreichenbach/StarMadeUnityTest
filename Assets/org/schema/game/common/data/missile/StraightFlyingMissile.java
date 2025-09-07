package org.schema.game.common.data.missile;


import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.MissileUpdateListener;
import com.bulletphysics.linearmath.Transform;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;

import javax.vecmath.Vector3f;

public abstract class StraightFlyingMissile extends Missile{
	private static Vector3f clientTmp = new Vector3f();
	private static Vector3f serverTmp = new Vector3f();
	private static Transform t = new Transform();
	
	public StraightFlyingMissile(StateInterface state) {
		super(state);
	}
	@Override
	public float updateTransform(Timer frameTime, Transform worldTransform, Vector3f direction, Transform out, boolean prediction) {
		if(direction.lengthSquared() == 0){
			return frameTime.getDelta();
		}
		assert(direction.lengthSquared() != 0);
		assert(!Float.isNaN(direction.x));
		Vector3f dirScaled = new Vector3f(direction);
		dirScaled.normalize();
		assert(!Float.isNaN(dirScaled.x)):direction;
		dirScaled.scale(frameTime.getDelta() * getSpeed());

		out.set(worldTransform);
		out.origin.add(dirScaled);
		assert(!Float.isNaN(dirScaled.x));
		return dirScaled.length();
	}
	@Override
	public void updateClient(Timer timer) {
		//INSERTED CODE
		if(!FastListenerCommon.missileUpdateListeners.isEmpty()) {
			for (MissileUpdateListener listener : FastListenerCommon.missileUpdateListeners) {
				listener.updateClient(this, timer);
			}
		}
		///
		distanceMade += updateTransform(timer, getWorldTransform(), getDirection(clientTmp), t, false);
		setTransformMissile(t);
		//INSERTED CODE
		if(!FastListenerCommon.missileUpdateListeners.isEmpty()) {
			for (MissileUpdateListener listener : FastListenerCommon.missileUpdateListeners) {
				listener.updateClientPost(this, timer);
			}
		}
		///
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateServer(Timer timer) {
		super.updateServer(timer);
		distanceMade += updateTransform(timer, getWorldTransform(), getDirection(serverTmp), t, false);
		setTransformMissile(t);
	}
	
}
