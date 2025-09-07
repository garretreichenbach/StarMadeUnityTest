package org.schema.game.common.data.physics;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.schine.network.objects.container.PhysicsDataContainer;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.linearmath.Transform;

public class PairCachingGhostObjectAlignable extends PairCachingGhostObjectExt implements RelativeBody, GamePhysicsObject {

	private final GameTransformable obj;
	public Transform localWorldTransform;
	public int attachedOrientation = Element.TOP;

	public PairCachingGhostObjectAlignable(CollisionType type,
			PhysicsDataContainer physicsDataContainer, GameTransformable obj) {
		super(type, physicsDataContainer);
		this.obj = obj;
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.dispatch.CollisionObject#checkCollideWith(com.bulletphysics.collision.dispatch.CollisionObject)
	 */
	@Override
	public boolean checkCollideWith(CollisionObject co) {
		return !(co instanceof PairCachingGhostObjectAlignable);
	}

//	/* (non-Javadoc)
//	 * @see com.bulletphysics.collision.dispatch.CollisionObject#hasContactResponse()
//	 */
//	@Override
//	public boolean hasContactResponse() {
//		boolean response = !(obj instanceof AbstractCharacter) || !((AbstractCharacter<? extends AbstractOwnerState>)obj).getOwnerState().isSitting();
//		System.err.println("HAS RESPONSE "+response);
//		return response;
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PCGhostObjExt(" + getUserPointer() + "->Attached(" + getAttached() + "))@" + hashCode();
	}

	@Override
	public GameTransformable getSimpleTransformableSendableObject() {
		return obj;
	}

	/**
	 * @return the obj
	 */
	public GameTransformable getObj() {
		return obj;
	}
}
