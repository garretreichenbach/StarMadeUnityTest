package org.schema.game.common.data.player;


import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.world.GravityState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.container.PhysicsDataContainer;

import com.bulletphysics.linearmath.Transform;

public interface AbstractCharacterInterface {
	public Vector4f getTint();
	public boolean isSitting();
	public boolean isHit();
	public boolean isHidden();
	public boolean isAnyMetaObjectSelected();
	/**
	 * 
	 * @param type
	 * @param subType (use -1 for any subtype)
	 * @return
	 */
	public boolean isMetaObjectSelected(MetaObjectType type, int subType);
	public boolean isAnyBlockSelected();
	public Transform getWorldTransform();
	public GravityState getGravity();
	public int getId();
	public void getClientAABB(Vector3f minAABB, Vector3f maxAABB);
	public float getCharacterHeightOffset();
	public Vector3f getOrientationUp();

	public Vector3f getOrientationRight();

	public Vector3f getOrientationForward();
	public boolean isInClientRange();
	
	public Vector3i getSittingPos();
	public Vector3i getSittingPosTo();
	public Vector3i getSittingPosLegs();
	public float getCharacterHeight();
	public StateInterface getState();
	public float getCharacterWidth();
	public boolean isOnServer();
	public PhysicsDataContainer getPhysicsDataContainer();
	public void setActionUpdate(boolean b);
	public int getSectorId();
	public boolean isVulnerable();
	public void handleCollision(SegmentPiece segmentPiece,
			Vector3f currentPosition);
	public int getActionUpdateNum();
	public void setActionUpdateNum(int updateNum);
	public void scheduleGravity(Vector3f vector3f, SimpleTransformableSendableObject object);
	public Vector3f getForward();
	public Vector3f getRight();
	public Vector3f getUp();
	public AnimationIndexElement getAnimationState();
	public void setAnimationState(AnimationIndexElement animationState);
	public boolean onGround();
	public PlayerState getConversationPartner();
}
