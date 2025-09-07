package org.schema.game.common.data.player.simplified;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.physics.CapsuleShapeExt;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.physics.KinematicCharacterControllerExt;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.AbstractCharacterInterface;
import org.schema.game.common.data.player.ForcedAnimation;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.common.data.world.GravityState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorNotFoundRuntimeException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.container.PhysicsDataContainer;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.physics.Physical;
import org.schema.schine.physics.PhysicsState;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SolverConstraint;
import com.bulletphysics.linearmath.Transform;

public class SimplifiedCharacter implements AbstractCharacterInterface, GameTransformable{

	private final Vector4f tint = new Vector4f();
	private boolean sitting;
	private PairCachingGhostObjectAlignable ghostObject;
	public PlayerState conversationPartner;
	private final Transform initialTransform = new Transform();
	private final TransformTimed t = new TransformTimed();
	{
		t.setIdentity();
		initialTransform.setIdentity();
	}
	private int id;
	private GravityState gravityState;
	private final Vector3i sittingPos = new Vector3i();
	private final Vector3i sittingPosTo = new Vector3i();
	private final Vector3i sittingPosLegs = new Vector3i();
	private final Vector3f up = new Vector3f();
	private final Vector3f forward = new Vector3f();
	private final Vector3f right = new Vector3f();
	protected KinematicCharacterControllerExt characterController;
	private int actionUpdateNum;
	private int sectorId;
	private final PhysicsDataContainer physicsDataContainer = new PhysicsDataContainer();
	private GravityState scheduledGravity;
	private CapsuleShapeExt capsule;
	private float stepHeight = 0.388f;
	private AnimationIndexElement animationState;
	private PlayerSkin playerSkin;
	private Vector3f movingDir = new Vector3f();
	public ForcedAnimation forcedAnimation;
	
	
	
	
	@Override
	public Vector4f getTint() {
		return tint;
	}

	@Override
	public boolean isSitting() {
		return sitting;
	}

	@Override
	public boolean isHit() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isAnyMetaObjectSelected() {
		return false;
	}

	@Override
	public boolean isMetaObjectSelected(MetaObjectType type, int subType) {
		return false;
	}

	@Override
	public boolean isAnyBlockSelected() {
		return false;
	}

	@Override
	public TransformTimed getWorldTransform() {
		return t;
	}

	@Override
	public GravityState getGravity() {
		return gravityState;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void getClientAABB(Vector3f minAABB, Vector3f maxAABB) {
		
	}

	@Override
	public float getCharacterHeightOffset() {
		return 0;
	}
	@Override
	public Vector3f getForward() {
		GlUtil.getUpVector(forward, t);
		return forward;
	}

	@Override
	public Vector3f getRight() {
		GlUtil.getRightVector(right, t);
		return right;
	}

	@Override
	public Vector3f getUp() {
		GlUtil.getUpVector(up, t);
		return up;
	}
	@Override
	public Vector3f getOrientationUp() {
		return characterController.upAxisDirection[1];
	}

	@Override
	public Vector3f getOrientationRight() {
		return characterController.upAxisDirection[0];
	}

	@Override
	public Vector3f getOrientationForward() {
		return characterController.upAxisDirection[2];
	}

	@Override
	public Vector3i getSittingPos() {
		return sittingPos;
	}

	@Override
	public Vector3i getSittingPosTo() {
		return sittingPosTo;
	}

	@Override
	public Vector3i getSittingPosLegs() {
		return sittingPosLegs;
	}
	@Override
	public void initPhysics(){
		ghostObject = new PairCachingGhostObjectAlignable(CollisionType.CHARACTER_SIMPLE, this.physicsDataContainer, this);

		ghostObject.setWorldTransform(initialTransform);
		assert(false):"TODO";
//		capsule = new CapsuleShapeExt(this, getCharacterWidth(), getCharacterHeight());
		
		capsule.setMargin(getCharacterMargin());
		ghostObject.setCollisionShape(capsule);

		ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

		ghostObject.setUserPointer(this.id);

		characterController = new KinematicCharacterControllerExt(this, ghostObject, capsule, stepHeight);

		physicsDataContainer.setObject(ghostObject);
		physicsDataContainer.setShape(capsule);

		physicsDataContainer.updatePhysical(getState().getUpdateTime());

		characterController.setGravity(0f);
	}

	private float getCharacterMargin() {
		return 0.1f;
	}

	@Override
	public float getCharacterHeight() {
		return 0;
	}

	@Override
	public float getCharacterWidth() {
		return 0;
	}

	@Override
	public StateInterface getState() {
				return null;
	}

	@Override
	public boolean isOnServer() {
				return false;
	}

	@Override
	public PhysicsDataContainer getPhysicsDataContainer() {
		return physicsDataContainer;
	}

	@Override
	public void setActionUpdate(boolean b) {
	}

	@Override
	public int getSectorId() {
		return sectorId;
	}

	@Override
	public boolean isVulnerable() {
		return true;
	}

	@Override
	public void handleCollision(SegmentPiece segmentPiece,
			Vector3f currentPosition) {
		
	}

	@Override
	public int getActionUpdateNum() {
		return actionUpdateNum;
	}

	@Override
	public void setActionUpdateNum(int updateNum) {
		this.actionUpdateNum = updateNum;
	}

	@Override
	public void scheduleGravity(Vector3f acc,
			SimpleTransformableSendableObject o) {
		scheduledGravity = new GravityState();
		scheduledGravity.setAcceleration(acc);
		scheduledGravity.source = o;
		scheduledGravity.forcedFromServer = false;
	}

	public void scheduleGravityWithBlockBelow(Vector3f acc,
			SimpleTransformableSendableObject o) {
		scheduledGravity = new GravityState();
		scheduledGravity.setAcceleration(acc);
		scheduledGravity.source = o;
		scheduledGravity.forcedFromServer = false;
		scheduledGravity.withBlockBelow = true;

	}

	public void scheduleGravityServerForced(Vector3f acc,
			SimpleTransformableSendableObject source) {
		scheduledGravity = new GravityState();
		scheduledGravity.setAcceleration(acc);
		scheduledGravity.source = source;
		scheduledGravity.forcedFromServer = true;
	}

	@Override
	public void createConstraint(Physical a, Physical b, Object userData) {
				
	}

	@Override
	public Transform getInitialTransform() {
		return initialTransform;
	}

	@Override
	public float getMass() {
		return 0;
	}

	@Override
	public void setPhysicsDataContainer(
			PhysicsDataContainer physicsDataContainer) {
		
	}

	@Override
	public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin,
			Vector3f tmpMin, Vector3f tmpMax, Transform instead) {
		
	}

	@Override
	public void engageWarp(String warpToUID, boolean allowDirect, long delay, Vector3i local, int maxDist) {
		
	}

	@Override
	public void sendControllingPlayersServerMessage(Object[] astr,
			int messageTypeError) {
		
	}

	@Override
	public boolean handleCollision(int index, RigidBody originalBody,
			RigidBody originalBody2, SolverConstraint contactConstraint) {
		return false;
	}

	@Override
	public void getGravityAABB(Transform serverTransform, Vector3f min,
			Vector3f max) {
		
	}
	public PhysicsExt getPhysics() throws SectorNotFoundRuntimeException {
		return (PhysicsExt) getPhysicsState().getPhysics();
	}

	public PhysicsState getPhysicsState() throws SectorNotFoundRuntimeException {
		if (isOnServer()) {
			Sector sector = ((GameServerState) getState()).getUniverse().getSector(sectorId);
			if (sector == null) {
				System.err.println("[ERROR][FATAL] Fatal Exception: SECTOR NULL FOR " + this + " " + sectorId);
				throw new SectorNotFoundRuntimeException(sectorId);
			}
			return sector;
		} else {
			return ((GameClientState) getState());
		}
	}
	@Override
	public TransformTimed getWorldTransformOnClient() {
		return t;
	}
	
	public void onPhysicsAdd() {
		//		if(isOnServer()){
		//			return;
		//		}
		physicsDataContainer.onPhysicsAdd();

		assert (!isOnServer() || ((Sector) getPhysics().getState()) == ((GameServerState) getState()).getUniverse().getSector(sectorId));
		PhysicsExt p = getPhysics();
		if (isOnServer() || isClientSectorIdValidForSpawning(sectorId)) {

			if (isOnServer()) {
				assert (((Sector) getPhysics().getState()) == ((GameServerState) getState()).getUniverse().getSector(sectorId));
				assert (getPhysics().getState() == getPhysicsState());
				getPhysics().addObject(physicsDataContainer.getObject(), physicsDataContainer.collisionGroup, physicsDataContainer.collisionMask);
			}
		}
				

		if (isOnServer() || sectorId == ((GameClientState) getState()).getCurrentSectorId()) {

			if (!p.containsAction(characterController)) {
				p.getDynamicsWorld().addAction(characterController);
			}
		}
		if (gravityState.source != null) {
			gravityState.setChanged(true);
//			handleGravity();
		}
	}
	public boolean isClientSectorIdValidForSpawning(int sectorId) {
		return sectorId == ((GameClientState) getState()).getCurrentSectorId()
				|| isPlayerNeighbor(sectorId);
	}
	public boolean isPlayerNeighbor(int sectorId) {

		RemoteSector objSector = (RemoteSector) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);

		return objSector != null && Sector.isNeighbor(objSector.clientPos(), ((GameClientState) getState()).getPlayer().getCurrentSector());
	}
	public void onPhysicsRemove() {
		physicsDataContainer.onPhysicsRemove();

		if (isOnServer()) {

			if (((GameServerState) getState()).getUniverse().getSector(sectorId) != null) {
				/*
				 * only remove on laoded sector. marked-for-remove sectors
				 * are already removed from the sector-map. This happens
				 * when SimpleTransformableSendableObject.cleanUpOnEntityDelete
				 * is called, triggered by a sector unload process
				 */
				getPhysics().removeObject(physicsDataContainer.getObject());
			}
		}else{
			getPhysics().removeObject(physicsDataContainer.getObject());
		}
	}

	/**
	 * @return the animationState
	 */
	@Override
	public AnimationIndexElement getAnimationState() {
		return animationState;
	}

	/**
	 * @param animationState the animationState to set
	 */
	@Override
	public void setAnimationState(AnimationIndexElement animationState) {
		this.animationState = animationState;
	}

	public PlayerSkin getSkin() {
		if(playerSkin == null){
			this.playerSkin =  GameResourceLoader.traidingSkin[0];
		}
		return playerSkin;
	}

	@Override
	public boolean onGround() {
		return characterController.onGround();
	}

	public Vector3f getMovingDir() {
		return movingDir;
	}

	@Override
	public PlayerState getConversationPartner() {
		return conversationPartner;
	}

	@Override
	public boolean isInClientRange() {
		return true;
	}

	

	
}
