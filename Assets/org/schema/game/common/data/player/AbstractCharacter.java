package org.schema.game.common.data.player;

import api.element.block.Blocks;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.Hittable;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerCharacter;
import org.schema.game.common.controller.damage.beam.DamageBeamHittable;
import org.schema.game.common.controller.damage.effects.InterEffectContainer;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.ParticleHandler;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.element.meta.BuildProhibiter;
import org.schema.game.common.data.element.meta.FlashLight;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.physics.*;
import org.schema.game.common.data.world.*;
import org.schema.game.common.util.Collisionable;
import org.schema.game.network.CharacterBlockActivation;
import org.schema.game.network.objects.NetworkPlayerCharacter;
import org.schema.game.network.objects.remote.RemoteCharacterBlockActivation;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.*;

//#RM1958 removed import sun.security.pkcs.ParsingException;

public abstract class AbstractCharacter<E extends AbstractOwnerState> extends SimpleTransformableSendableObject<CharacterProvider> implements LightTransformable, Identifiable, AbstractCharacterInterface, ParticleHandler, TagSerializable, ShopperInterface, Collisionable, Hittable, Destroyable, Salvager, BeamHandlerContainer, DamageBeamHittable {

	public static final long MEDICAL_REUSE_MS = 20000;
	public static final long MEDICAL_HP = 10;
	private final PersonalBeamHandler<E> beamHandler;
	private final Set<ShopInterface> shopsInDistance = new HashSet<ShopInterface>();
	private final ObjectArrayFIFOQueue<CharacterBlockActivation> blockActivationReactions = new ObjectArrayFIFOQueue<CharacterBlockActivation>();
	public boolean actionUpdate;
	protected float stepHeight = 0.388f;
	protected KinematicCharacterControllerExt characterController;
	protected long lastAlignRequest;
	protected long lastAttach;
	private boolean hasSpawnedOnServer = false;
	private String uniqueIdentifier;
	private AnimationIndexElement animationState = AnimationIndex.IDLING_FLOATING;
	private ConvexShape capsule;
	private long lastCollisionHurt;
	private long warpOutOfCollisionRequest = -1;
	private PairCachingGhostObjectAlignable ghostObject;
	private long activatedStuckProtection;
	private long lastHit;
	private Vector3b tmpLocalPos = new Vector3b();
	private int actionUpdateNum = -1;
	private FlashLight flashLightActive;
	public boolean neverSpawnedBefore = true;

	public CollisionType getCollisionType() {
		return CollisionType.CHARACTER;
	}

	private static class CharacterEffectSet extends InterEffectContainer {

		@Override
		public InterEffectSet[] setupEffectSets() {
			InterEffectSet[] s = new InterEffectSet[1];
			for(int i = 0; i < s.length; i++) {
				s[i] = new InterEffectSet();
			}
			return s;
		}

		@Override
		public InterEffectSet get(HitReceiverType type) {
			if(type != HitReceiverType.CHARACTER) {
				throw new RuntimeException("illegal hit received " + type.name());
			}
			return sets[0];

		}

		@Override
		public void update(ConfigEntityManager c) {
		}

	}

	@Override
	protected InterEffectContainer setupEffectContainer() {
		return new CharacterEffectSet();
	}

	private final ObjectArrayList<CharacterProvider> listeners = new ObjectArrayList<CharacterProvider>();
	private CharacterProvider sendableSegmentProvider;

	@Override
	public void addListener(CharacterProvider s) {
		listeners.add(s);
	}

	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return this;
	}

	@Override
	public CharacterProvider createNetworkListenEntity() {
		sendableSegmentProvider = new CharacterProvider(getState());
		sendableSegmentProvider.initialize();
		return sendableSegmentProvider;
	}

	@Override
	public List<CharacterProvider> getListeners() {
		return listeners;
	}

	public AbstractCharacter(StateInterface state) {
		super(state);
		beamHandler = new PersonalBeamHandler<E>(this, this);
	}

	@Override
	public byte getFactionRights() {
		return getOwnerState().getFactionRights();
	}

	@Override
	public byte getOwnerFactionRights() {
		return getOwnerState().getFactionRights();
	}

	@Override
	public Vector4f getTint() {
		return getOwnerState().getTint();
	}

	@Override
	public void setActionUpdate(boolean b) {
		actionUpdate = b;
	}

	@Override
	public boolean isHit() {
		return getNetworkObject() != null && getNetworkObject().hit.get();
	}

	@Override
	public boolean isSitting() {
		return getOwnerState().isSitting();
	}

	@Override
	public Vector3i getSittingPos() {
		return getOwnerState().sittingPos;
	}

	@Override
	public Vector3i getSittingPosTo() {
		return getOwnerState().sittingPosTo;
	}

	@Override
	public Vector3i getSittingPosLegs() {
		return getOwnerState().sittingPosLegs;
	}

	@Override
	public boolean isAnyBlockSelected() {
		short type = getOwnerState().getInventory().getType(getOwnerState().getSelectedBuildSlot());
		return type > 0;
	}

	@Override
	public boolean isAnyMetaObjectSelected() {
		short type = getOwnerState().getInventory().getType(getOwnerState().getSelectedBuildSlot());
		return type < 0;
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
	public boolean isMetaObjectSelected(MetaObjectType type, int subType) {

		int metaId = getOwnerState()
				.getInventory().getMeta(getOwnerState().getSelectedBuildSlot());
		MetaObject meta = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);

		return meta != null && (meta.getSubObjectId() == -1 || subType == -1 || (subType == meta.getSubObjectId()));
	}

	@Override
	public void getClientAABB(Vector3f minAABB, Vector3f maxAABB) {
		getPhysicsDataContainer().getShape().getAabb(getWorldTransformOnClient(), minAABB, maxAABB);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#newNetworkObject()
	 */
	@Override
	public Vector3f getLinearVelocity(Vector3f out) {
		return ((AbstractCharacter<?>) this).characterController.getLinearVelocity(out);
	}

	@Override
	public abstract boolean isClientOwnObject();

	@Override
	public abstract NetworkPlayerCharacter getNetworkObject();

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject o) {
		super.initFromNetworkObject(o);

		NetworkPlayerCharacter p = (NetworkPlayerCharacter) o;
		uniqueIdentifier = p.uniqueId.get();

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject, int)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);

		ObjectArrayList<RemoteCharacterBlockActivation> bact = getNetworkObject().blockActivationsWithReaction.getReceiveBuffer();

		if(!bact.isEmpty()) {
			synchronized(blockActivationReactions) {
				for(int i = 0; i < bact.size(); i++) {
					blockActivationReactions.enqueue(bact.get(i).get());
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);

		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			drawDebugTransform();
		}

		beamHandler.update(timer);

		actionUpdate = false; //if this doesnt get flagged client will do an inplacepyhsics update

		getPhysicsDataContainer().getObject().activate(true);

		getWorldTransformInverse().set(getWorldTransform());
		getWorldTransformInverse().inverse(); //reverse the transformation, the physics did into local space

		if(isOnServer() && timer.currentTime - lastHit > 400 && getNetworkObject().hit.get()) {
			getNetworkObject().hit.set(false);
		}
		this.setProhibitingBuildingAroundOrigin(0);

		this.flashLightActive = null;

		for(int i = 0; i < getOwnerState().getInventory().getActiveSlotsMax(); i++) {
			int metaId = getOwnerState().getInventory().getMeta(i);
			MetaObject object = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);
			if(object != null && object instanceof BuildProhibiter && ((BuildProhibiter) object).active) {
				this.setProhibitingBuildingAroundOrigin(((BuildProhibiter) object).rangeRadius);
			}

			if(object != null && object instanceof FlashLight && ((FlashLight) object).active) {
				flashLightActive = ((FlashLight) object);
			}
		}

//		System.err.println("PH;;AM : "+getState()+" "+this+"::: "+getGravity().source);

		if(!blockActivationReactions.isEmpty()) {
			synchronized(blockActivationReactions) {
				while(!blockActivationReactions.isEmpty()) {
					CharacterBlockActivation dequeue = blockActivationReactions.dequeue();
					handleBlockActivationReaction(dequeue);
				}
			}

		}

		if(warpOutOfCollisionRequest > 0 && timer.currentTime > warpOutOfCollisionRequest) {
			if(getPhysics().containsObject(ghostObject)) {
				Transform old = ghostObject.getWorldTransform(new Transform());
				System.err.println("SCHEDULED WARP CHECK: OLD POSITION: " + old.origin);
				Vector3f up = new Vector3f(0, 1, 0);
				if(scheduledGravity != null && scheduledGravity.source != null) {

					GlUtil.getUpVector(up, scheduledGravity.source.getWorldTransform());
					System.err.println("[SHEDULEDWARPOUT] took up from sheduled grav up " + up);
				} else if(getGravity() != null && getGravity().source != null) {
					GlUtil.getUpVector(up, getGravity().source.getWorldTransform());
					System.err.println("[SHEDULEDWARPOUT] took up from current grav up " + up);
				} else {
					System.err.println("[SHEDULEDWARPOUT] default grav up " + up);
				}
				characterController.warpOutOfCollision(getState(), getPhysics().getDynamicsWorld(), old, up);
				Transform worldTransform = ghostObject.getWorldTransform(new Transform());
				if(!old.origin.equals(worldTransform.origin)) {
					warpTransformable(worldTransform, true, false, null);
					ghostObject.setWorldTransform(worldTransform);
					System.err.println(getState() + "[PLAYERCHARACTER] WARPING OUT OF COLLISION " + old.origin + " -> " + worldTransform.origin);
					if(isClientOwnObject()) {
						((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("SPAWN-STUCK-PROTECTION\nWarping out of\npossible collisions!"), 0);
					}
					//					if(isOnServer() && !getAttachedPlayers().isEmpty()){
					//						((GameServerState)getState()).getController().sendPlayerMessage(getAttachedPlayers().get(0).getName(), "SPAWN-STUCK-PROTECTION\nWarping out of\npossible collisions", ServerMessage.MESSAGE_TYPE_INFO);
					//					}
				} else {
					System.err.println("NO WARP NECESSARY");
				}
			}
			warpOutOfCollisionRequest = -1;

		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		getNetworkObject().uniqueId.set(uniqueIdentifier, true);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#setFactionId(int)
	 */
	@Override
	public void setFactionId(int factionId) {
		super.setFactionId(factionId);
	}

	@Override
	public void getGravityAABB(Vector3f minOut, Vector3f maxOut) {
		Transform worldTransform = getPhysicsDataContainer().getObject().getWorldTransform(new Transform());
		AabbUtil2.transformAabb(
				new Vector3f(-1, -1, -1),
				new Vector3f(1, 1, 1),
				SegmentData.SEGf,
				getWorldTransform(),
				minOut,
				maxOut);
	}

	@Override
	public void getGravityAABB(Transform t,
	                           Vector3f minOut, Vector3f maxOut) {
		AabbUtil2.transformAabb(
				new Vector3f(-1, -1, -1),
				new Vector3f(1, 1, 1),
				SegmentData.SEGf,
				t,
				minOut,
				maxOut);
	}

	@Override
	protected void handleGravity() {
		if(isHidden()) {
			if(getGravity().source == null && getPhysicsDataContainer().getObject() != null && ghostObject.getAttached() != null) {
				System.err.println("[PlayerCharacter] " + getState() + "; " + this + " Cleaned up physics grvity. attached to was not null but no gravity source");
				ghostObject.setAttached(null);
			}
			return;
		}

		//		if(this instanceof PlayerCharacter){
		//			System.err.println(this.getState()+" "+this+" GRA: "+getGravity().isGravityOn()+"; "+getGravity().isAligedOnly()+" "+getGravity().source+", "+getGravity().acceleration+"; cGrav "+getCharacterController().getGravity()+"; changed: "+getGravity().isChanged());
		//		}
		if(getGravity().isGravityOn() || getGravity().isAligedOnly()) {
			SegmentController c = ((SegmentController) getGravity().source);
			if(!c.getPhysicsDataContainer().isInitialized()) {
				getGravity().pendingUpdate = c;
			} else {
				if(getGravity().isChanged() || (getGravity().pendingUpdate == getGravity().source)) {
					getGravity().pendingUpdate = null;

//					System.err.println("[CHARACTER][GRAVITY] " + this + " " + getState() + " starting gravity change: " + getWorldTransform().origin);
					{
						//reset
						ghostObject.localWorldTransform = null;
						ghostObject.attachedOrientation = Element.TOP;
						ghostObject.setAttached(null);
						characterController.setGravity(0);
						//gravity deactivated
						characterController.upAxisDirection[0].set(KinematicCharacterControllerExt.upAxisDirectionDefault[0]);
						characterController.upAxisDirection[1].set(KinematicCharacterControllerExt.upAxisDirectionDefault[1]);
						characterController.upAxisDirection[2].set(KinematicCharacterControllerExt.upAxisDirectionDefault[2]);
					}

//					System.err.println(this.getState() + "[ABSTRACTCHARACTER] " + this
//							+ " INITIALIZING GRAVITY FOR "
//							+ (getGravity().source));

					characterController.upAxisDirection[0]
							.set(KinematicCharacterControllerExt.upAxisDirectionDefault[0]);
					characterController.upAxisDirection[1]
							.set(KinematicCharacterControllerExt.upAxisDirectionDefault[1]);
					characterController.upAxisDirection[2]
							.set(KinematicCharacterControllerExt.upAxisDirectionDefault[2]);

					GlUtil.getRightVector(
							characterController.upAxisDirection[0],
							c.getWorldTransform());
					GlUtil.getUpVector(
							characterController.upAxisDirection[1],
							c.getWorldTransform());
					GlUtil.getForwardVector(
							characterController.upAxisDirection[2],
							c.getWorldTransform());

					int orientation = -1;
					Vector3f gr = getGravity().copyAccelerationTo(new Vector3f());
					if(gr.y < 0) {
						orientation = Element.TOP;
					} else if(gr.y > 0) {
						orientation = Element.BOTTOM;
					} else if(gr.x < 0) {
						orientation = Element.RIGHT;
					} else if(gr.x > 0) {
						orientation = Element.LEFT;
					} else if(gr.z < 0) {
						orientation = Element.FRONT;
					} else if(gr.z > 0) {
						orientation = Element.BACK;
					}

//					System.err.println("[PLAYERCHARACTER] " + getState() + " of " + this + " gravity to: " + Element.getSideString(orientation));

					GlUtil.setRightVector(
							characterController.upAxisDirection[0],
							getWorldTransform());
					GlUtil.setUpVector(
							characterController.upAxisDirection[1],
							getWorldTransform());
					GlUtil.setForwardVector(
							characterController.upAxisDirection[2],
							getWorldTransform());

					ghostObject.attachedOrientation = orientation;

					ghostObject.setAttached(c);

					ghostObject.localWorldTransform = new Transform();

					Matrix3f addRoation = characterController.getAddRoation();
					//					System.err.println("ROT\n"+addRoation);
					Transform sourceWT = new Transform(c.getWorldTransform());
					sourceWT.basis.mul(addRoation);
					sourceWT.inverse();

					Transform now = new Transform(getWorldTransform());
					now.basis.mul(addRoation);

					//FIXME adapt rotation when entering grav

					if(isClientOwnObject()) {
						Matrix3f currentPlayerRot = new Matrix3f();
						currentPlayerRot.setIdentity();
						GlUtil.setRightVector(getOwnerState().getRight(new Vector3f()), currentPlayerRot);
						GlUtil.setUpVector(getOwnerState().getUp(new Vector3f()), currentPlayerRot);
						GlUtil.setForwardVector(getOwnerState().getForward(new Vector3f()), currentPlayerRot);
						Matrix3f bb = new Matrix3f(sourceWT.basis);

						//rotation without whip rotation
						bb.mul(currentPlayerRot);

//						currentPlayerRot.invert();
						Transform n = new Transform(getWorldTransform());
						n.basis.set(bb);
						n.origin.set(0, 0, 0);
						Controller.getCamera().getLookAlgorithm().lookTo(n);
					}

					ghostObject.localWorldTransform.set(sourceWT);
					ghostObject.localWorldTransform.mul(now);

					inPlaceAttachedUpdate(getState().getUpdateTime());

					//make sure to send transformations
					getRemoteTransformable().setEqualCounter(0);

//					System.err.println("[CHARACTER][GRAVITY] " + this + " " + getState() + " starting gravity change DONE: source: " + c + " origin: " + getWorldTransform().origin);
				}
			}
			Vector3f gr = getGravity().copyAccelerationTo(new Vector3f());
			if(gr.y < 0) {
				characterController.setGravity(-gr.y);
			} else if(gr.y > 0) {
				characterController.setGravity(gr.y);
			} else if(gr.x < 0) {
				characterController.setGravity(-gr.x);
			} else if(gr.x > 0) {
				characterController.setGravity(gr.x);
			} else if(gr.z < 0) {
				characterController.setGravity(-gr.z);
			} else if(gr.z > 0) {
				characterController.setGravity(gr.z);
			} else {
				characterController.setGravity(0);
			}

		} else {
			if(getGravity().isChanged()) {

				sendOwnerMessage(Lng.astr("Gravity neutralized..."), ServerMessage.MESSAGE_TYPE_INFO);
				System.err.println(getState() + " " + this + " (handleGravity()) Deactivating gravity ");

				//FIXME handle rotation when exiting grav
				Transform bef = new Transform();
				ghostObject.getWorldTransform(bef);
				if(ghostObject.localWorldTransform != null) {

//					Matrix3f m = new Matrix3f(ghostObject.localWorldTransform.basis);
//					bef.basis.mul(m);
				}
				if(isClientOwnObject()) {
					Matrix3f currentPlayerRot = new Matrix3f();
					currentPlayerRot.setIdentity();
					GlUtil.setRightVector(getOwnerState().getRight(new Vector3f()), currentPlayerRot);
					GlUtil.setUpVector(getOwnerState().getUp(new Vector3f()), currentPlayerRot);
					GlUtil.setForwardVector(getOwnerState().getForward(new Vector3f()), currentPlayerRot);
					bef.basis.set(currentPlayerRot);
					Controller.getCamera().getLookAlgorithm().lookTo(bef);
				}

//				if(isClientOwnObject()){
//					if(isClientOwnObject()){
//						Controller.getCamera().getLookAlgorithm().force(Controller.getCamera().getWorldTransform());
//					}
//				}

				ghostObject.localWorldTransform = null;
				ghostObject.attachedOrientation = Element.TOP;
				ghostObject.setAttached(null);
				characterController.setGravity(0);
				//gravity deactivated
				characterController.upAxisDirection[0].set(KinematicCharacterControllerExt.upAxisDirectionDefault[0]);
				characterController.upAxisDirection[1].set(KinematicCharacterControllerExt.upAxisDirectionDefault[1]);
				characterController.upAxisDirection[2].set(KinematicCharacterControllerExt.upAxisDirectionDefault[2]);

				characterController.resetVerticalVelocity();
			}
		}

	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */

	//	@Override
	//	protected boolean hasVirtual() {
	//		return false;
	//	}
	@Override
	protected boolean hasVirtual() {
		return false;
	}

	@Override
	protected boolean isCheckSectorActive() {
		return !isHidden() && System.currentTimeMillis() - lastAttach > 6000;
	}

	@Override
	public void onPhysicsAdd() {
		//		if(isOnServer()){
		//			return;
		//		}
		super.onPhysicsAdd();

		PhysicsExt p = getPhysics();

		if(isClientOwnObject()) {
			PhysicsExt ph = getPhysics();
			Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), getWorldTransform());
			Transform before = new Transform(getWorldTransform());
			if(this.neverSpawnedBefore) {
				this.neverSpawnedBefore = false;
				setWarpToken(false);
			}
			if(isWarpToken()) {
				System.err.println("NOT WARPING OUT OF COLLISION, BECAUSE WARP TOKEN WAS SET");
				setWarpToken(false);
			} else {
				Vector3f up = new Vector3f(0, 1, 0);
				if(scheduledGravity != null && scheduledGravity.source != null) {

					GlUtil.getUpVector(up, scheduledGravity.source.getWorldTransform());
					System.err.println("[WARPOUT] took up from sheduled grav up " + up);
				} else if(getGravity() != null && getGravity().source != null) {
					GlUtil.getUpVector(up, getGravity().source.getWorldTransform());
					System.err.println("[WARPOUT] took up from current grav up " + up);
				} else {
					System.err.println("[WARPOUT] default grav up " + up);
				}
				characterController.warpOutOfCollision(getState(), ph.getDynamicsWorld(), getWorldTransform(), up);

				System.err.println("[PLAYERCHARACTER][ONPHYSICSADD][" + getState() + "] WARPING OUT OF COLLISION: " + this + ": " + before.origin + " -> " + ghostObject.getWorldTransform(new Transform()).origin);
			}
			Transform worldTransformWarp = ghostObject.getWorldTransform(new Transform());

			Vector3f length = new Vector3f();
			length.sub(before.origin, worldTransformWarp.origin);

			if(length.length() > 50) {
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("SPAWNING POINT ERROR DETECTED\nPLEASE SEND IN A REPORT\nLENGTH: %s", length.length()), 0);
				System.err.println("[PLAYERCHARACTER][ONPHYSICSADD][" + getState() + "] Exception while WARPING OUT OF COLLISION: " + this + ": " + before.origin + " -> " + worldTransformWarp.origin);
			}

			getRemoteTransformable().warp(worldTransformWarp, false);
		}

		if(isOnServer() || getSectorId() == ((GameClientState) getState()).getCurrentSectorId()) {

			if(!p.containsAction(characterController)) {
				p.getDynamicsWorld().addAction(characterController);
			}
		}
		if(getGravity().source != null) {
			getGravity().setChanged(true);
			handleGravity();
		}
	}

	@Override
	public void onPhysicsRemove() {
		super.onPhysicsRemove();
		if(isOnServer() && ((GameServerState) getState()).getUniverse().getSector(getSectorId()) == null) {
			/*
			 * only remove on laoded sector. marked-for-remove sectors
			 * are already removed from the sector-map. This happens
			 * when SimpleTransformableSendableObject.cleanUpOnEntityDelete
			 * is called, triggered by a sector unload process
			 */
		} else {
			PhysicsExt p = getPhysics();
			p.getDynamicsWorld().removeAction(characterController);
		}
	}

	public void climb(CollisionWorld.ClosestRayResultCallback nearestIntersection, int dir) {
		if(nearestIntersection != null && nearestIntersection.hasHit() && nearestIntersection instanceof CubeRayCastResult result) {
			if(result.getSegment() != null && result.getSegment().getSegmentController() instanceof EditableSendableSegmentController) {
				Vector3i absOnOut = new Vector3i();
				absOnOut.set(result.getSegment().pos.x + result.getCubePos().x, result.getSegment().pos.y + result.getCubePos().y, result.getSegment().pos.z + result.getCubePos().z);
				SegmentPiece segmentPiece = result.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(absOnOut.x, absOnOut.y, absOnOut.z);
				if(segmentPiece.getType() == Blocks.LADDER.getId()) {
					if(getGravity().source == null || getGravity().source != result.getSegment().getSegmentController()) {
						SegmentController align = result.getSegment().getSegmentController();
						scheduleGravity(new Vector3f(0, 0, 0), align);
						if(!isOnServer()) ((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Aligned to\n%s", align.toNiceString()), 0);
					}
					getOwnerState().climbingId = result.getSegment().getSegmentController().getId();
					getOwnerState().climbingPos = segmentPiece.getAbsoluteIndex();
					getOwnerState().climbingDir = dir;
					return;
				}
			}
		}
		getOwnerState().climbingId = -1;
		getOwnerState().climbingPos = -1;
		getOwnerState().climbingDir = 0;
	}

	public void sitDown(CollisionWorld.ClosestRayResultCallback from, Vector3f pos, Vector3f forward, float rayLengthToTest) {
		CollisionWorld.ClosestRayResultCallback nearestIntersection = from;
		if(nearestIntersection != null && nearestIntersection.hasHit() && nearestIntersection instanceof CubeRayCastResult) {

			CubeRayCastResult cs = (CubeRayCastResult) nearestIntersection;
			if(cs.getSegment() != null && cs.getSegment().getSegmentController() instanceof EditableSendableSegmentController) {

				Vector3f dir = new Vector3f(forward);

				Vector3f to = new Vector3f();
				dir.normalize();
				dir.scale(rayLengthToTest);

				to.add(pos, dir);

				CubeRayCastResult rayCallback = new CubeRayCastResult(
						pos, to, false);
				rayCallback.setOnlyCubeMeshes(true);
				rayCallback.setIgnoereNotPhysical(true);
				CollisionWorld.ClosestRayResultCallback testRayCollisionPoint = (getPhysics()).testRayCollisionPoint(pos,
						to, rayCallback, false);

				if(getGravity().source == null || getGravity().source != cs.getSegment().getSegmentController()) {
					SegmentController align = cs.getSegment().getSegmentController();
					scheduleGravity(new Vector3f(0, 0, 0), align);
					if(!isOnServer()) {
						((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Aligned to\n%s", align.toNiceString()), 0);
					}
				}

				if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {

					CubeRayCastResult c = (CubeRayCastResult) testRayCollisionPoint;

					//			DebugBox bb = new DebugBox(
					//
					//			new Vector3f(c.segment.pos.x +c.cubePos.x-8-0.515f, c.segment.pos.y+c.cubePos.y-8-0.515f,c.segment.pos.z+c.cubePos.z-8-0.515f),
					//			new Vector3f(c.segment.pos.x+c.cubePos.x+1-8-0.485f, c.segment.pos.y+c.cubePos.y+1-8-0.485f, c.segment.pos.z+c.cubePos.z+1-8-0.485f),
					//			c.segment.getSegmentController().getWorldTransform(),
					//			0, 0, 1, 1);
					//			DebugDrawer.boxes.add(bb);

					CubeRayCastResult cubeResult = (CubeRayCastResult) testRayCollisionPoint;
					if(cubeResult.getSegment() == null) {
						System.err.println("CUBERESULT SEGMENT NULL");
						return;
					}
					//			cacheLastHitSegmentFromAddAndRemove = cubeResult.getSegment();
					Vector3i p = new Vector3i(cubeResult.getSegment().pos.x, cubeResult.getSegment().pos.y, cubeResult.getSegment().pos.z);
					Vector3i absOnOut = new Vector3i();
					absOnOut.set(cubeResult.getSegment().pos.x + cubeResult.getCubePos().x
							, cubeResult.getSegment().pos.y + cubeResult.getCubePos().y
							, cubeResult.getSegment().pos.z + cubeResult.getCubePos().z);

					p.x += (cubeResult.getCubePos().x - SegmentData.SEG_HALF);
					p.y += (cubeResult.getCubePos().y - SegmentData.SEG_HALF);
					p.z += (cubeResult.getCubePos().z - SegmentData.SEG_HALF);
					//			Transform t = new Transform();
					//			t.setIdentity();
					//			DebugBox bg1 = new DebugBox(
					//					new Vector3f(testRayCollisionPoint.hitPointWorld.x-0.7f, testRayCollisionPoint.hitPointWorld.y-0.7f, testRayCollisionPoint.hitPointWorld.z-0.7f),
					//					new Vector3f(testRayCollisionPoint.hitPointWorld.x+0.7f, testRayCollisionPoint.hitPointWorld.y+0.7f, testRayCollisionPoint.hitPointWorld.z+0.7f),
					//					t,
					//					1, 0, 1, 1);
					//					DebugDrawer.boxes.add(bg1);
					if(!isOnServer()) {
						if(((GameClientState) getState()).getCurrentSectorId() == cubeResult.getSegment().getSegmentController().getSectorId()) {
							cubeResult.getSegment().getSegmentController().getWorldTransformInverse().transform(testRayCollisionPoint.hitPointWorld);
						} else {
							Transform t = new Transform(cubeResult.getSegment().getSegmentController().getWorldTransformOnClient());
							t.inverse();
							t.transform(testRayCollisionPoint.hitPointWorld);
						}
					} else {
						cubeResult.getSegment().getSegmentController().getWorldTransformInverse().transform(testRayCollisionPoint.hitPointWorld);
					}

					//			DebugBox bg = new DebugBox(
					//					new Vector3f(testRayCollisionPoint.hitPointWorld.x-0.6f, testRayCollisionPoint.hitPointWorld.y-0.6f, testRayCollisionPoint.hitPointWorld.z-0.6f),
					//					new Vector3f(testRayCollisionPoint.hitPointWorld.x+0.6f, testRayCollisionPoint.hitPointWorld.y+0.6f, testRayCollisionPoint.hitPointWorld.z+0.6f),
					//					c.segment.getSegmentController().getWorldTransform(),
					//					1, 1, 1, 1);
					//					DebugDrawer.boxes.add(bg);

					int side = Element.getSide(cubeResult.hitPointWorld, null, p, cubeResult.getSegment().getSegmentData().getType(cubeResult.getCubePos()), cubeResult.getSegment().getSegmentData().getOrientation(cubeResult.getCubePos()));

					System.err.println("[SIT] SIDE: " + Element.getSideString(side) + ": " + cubeResult.hitPointWorld + "; " + p);

					Vector3f local = new Vector3f(cubeResult.hitPointWorld.x - p.x, cubeResult.hitPointWorld.y - p.y, cubeResult.hitPointWorld.z - p.z);

					Vector3i toSit = new Vector3i();
					Vector3i toLeg = new Vector3i(p);
					ElementInformation g = ElementKeyMap.getInfo(cubeResult.getSegment().getSegmentData().getType(cubeResult.getCubePos()));
					if(g.getBlockStyle() != BlockStyle.WEDGE && !g.getBlockStyle().cube) {
						if(!isOnServer()) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can only sit on blocks and wedges!"), 0);
						}
						return;
					}
					boolean notInGrav = false;
					if(g.isNormalBlockStyle()) {
						if((ghostObject.getAttached() == null || ghostObject.attachedOrientation < 0) && side != Element.TOP) {
							System.err.println("[SIT] Can only go to top when unattached(or not in gravity)");
							if(!isOnServer()) {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can only sit on top!"), 0);
							}
							return;
						}
						if(ghostObject.getAttached() != null && ghostObject.attachedOrientation >= 0 && ghostObject.attachedOrientation != Element.getOpposite(side)) {
							if(ghostObject.attachedOrientation == Element.TOP && side == Element.TOP) {
								//FIXME special case for top??
							} else if(ghostObject.attachedOrientation == Element.BOTTOM && side == Element.BOTTOM) {
								//FIXME special case for bottom??
							} else {
								System.err.println("[SIT] " + getState() + " opposite orientation doesnt meet relative top: AttOrient: " + Element.getSideString(ghostObject.attachedOrientation) + "; OppositeRaySide: " + Element.getSideString(Element.getOpposite(side)));

								if(!isOnServer()) {
									((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can only sit on top!"), 0);
								}
								return;
							}

						}
					} else {
						int infoIndex = SegmentData.getInfoIndex(cubeResult.getCubePos());
						BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(g.getBlockStyle(),
								cubeResult.getSegment().getSegmentData().getOrientation(infoIndex));

						// #RM1926 - this logic looks all f'ed up, but it actually fixes the issue of being able to sit on a wedge from below...
						if((ghostObject.getAttached() == null || ghostObject.attachedOrientation < 0)
								&& side != Element.TOP && side != Element.BACK && side != Element.RIGHT) {
							System.err.println("[SIT] Can only go to top when unattached(or not in gravity)");
							if(!isOnServer()) {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can only sit on top!"), 0);
							}
							return;
						}

						if((ghostObject.getAttached() == null || ghostObject.attachedOrientation < 0
								|| ghostObject.attachedOrientation == Element.TOP)) {
							side = Element.TOP;
							notInGrav = true;
						} else {
							if(ghostObject.attachedOrientation == Element.TOP
									|| ghostObject.attachedOrientation == Element.BOTTOM
							) {
								//FIXME bottom and top mixed up
								side = ghostObject.attachedOrientation;
							} else {
								side = Element.getOpposite(ghostObject.attachedOrientation);
							}

						}
						byte wedgeGravityValidDir;
						if((notInGrav || ghostObject.attachedOrientation == Element.TOP
								|| ghostObject.attachedOrientation == Element.BOTTOM
						)) {
							//FIXME bottom and top mixed up
							wedgeGravityValidDir = algo.getWedgeGravityValidDir((byte) Element.getOpposite(side));
						} else {
							wedgeGravityValidDir = algo.getWedgeGravityValidDir((byte) side);
						}
						if(wedgeGravityValidDir < 0) {

							if(ghostObject.getAttached() == null || ghostObject.attachedOrientation < 0) {
								if(!isOnServer()) {
									((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't sit on this wedge\nwithout gravity!"), 0);
								}
							} else {
								if(!isOnServer()) {
									((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't sit on this wedge\nin this gravity direction!"), 0);
								}
							}
							return;
						} else {
							local.set(Element.DIRECTIONSf[wedgeGravityValidDir]);
						}
					}
					switch(side) {
						case (Element.RIGHT) -> {
							p.x += 1f;
							toSit.set(p);
							if(Math.abs(local.z) > Math.abs(local.y)) {
								toSit.z += Math.signum(local.z);
								toLeg.z += Math.signum(local.z);
							} else {
								toSit.y += Math.signum(local.y);
								toLeg.y += Math.signum(local.y);
							}
						}
						case (Element.LEFT) -> {
							p.x -= 1f;
							toSit.set(p);
							if(Math.abs(local.z) > Math.abs(local.y)) {
								toSit.z += Math.signum(local.z);
								toLeg.z += Math.signum(local.z);
							} else {
								toSit.y += Math.signum(local.y);
								toLeg.y += Math.signum(local.y);
							}
						}
						case (Element.TOP) -> {
							p.y += 1f;
							toSit.set(p);
							if(Math.abs(local.z) > Math.abs(local.x)) {
								toSit.z += Math.signum(local.z);
								toLeg.z += Math.signum(local.z);
							} else {
								toSit.x += Math.signum(local.x);
								toLeg.x += Math.signum(local.x);
							}
						}
						case (Element.BOTTOM) -> {
							p.y -= 1f;
							toSit.set(p);
							if(Math.abs(local.z) > Math.abs(local.x)) {
								toSit.z += Math.signum(local.z);
								toLeg.z += Math.signum(local.z);
							} else {
								toSit.x += Math.signum(local.x);
								toLeg.x += Math.signum(local.x);
							}
						}
						case (Element.FRONT) -> {
							p.z += 1f;
							toSit.set(p);
							if(Math.abs(local.y) > Math.abs(local.x)) {
								toSit.y += Math.signum(local.y);
								toLeg.y += Math.signum(local.y);
							} else {
								toSit.x += Math.signum(local.x);
								toLeg.x += Math.signum(local.x);
							}
						}
						case (Element.BACK) -> {
							p.z -= 1f;
							toSit.set(p);
							if(Math.abs(local.y) > Math.abs(local.x)) {
								toSit.y += Math.signum(local.y);
								toLeg.y += Math.signum(local.y);
							} else {
								toSit.x += Math.signum(local.x);
								toLeg.x += Math.signum(local.x);
							}
						}
						//						default:	System.err.println("[BUILDMODEDRAWER] WARNING: NO SIDE recognized!!!"); break;
					}

					if(g.getBlockStyle() == BlockStyle.WEDGE) {
						Vector3i axis = new Vector3i();
						axis.sub(toLeg, toSit);
						p.add(axis);
						toSit.add(axis);
						toLeg.add(axis);

						int infoIndex = SegmentData.getInfoIndex(cubeResult.getCubePos());
						BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(g.getBlockStyle(),
								cubeResult.getSegment().getSegmentData().getOrientation(infoIndex));

						System.err.println("[SIT] Sitting on wedge " + axis + "; " + algo + " ");
					}

					p.add(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
					toSit.add(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
					toLeg.add(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);

					SegmentPiece sOn = cubeResult.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(p);
					SegmentPiece sDir = cubeResult.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(toSit);
					SegmentPiece sBelow = cubeResult.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(toLeg);
					if(sOn == null || sDir == null || sBelow == null) {
						System.err.println("[SIT] one or more blocks null");
						if(!isOnServer()) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't sit here!"), 0);
						}
						return;
					}

					// #RM1927 check the block above to ensure we aren't sitting inside a block
					Vector3i above = new Vector3i(p);
					above.y += 1;
					SegmentPiece abovePiece = cubeResult.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(above);
					if(abovePiece != null && abovePiece.getType() != Element.TYPE_NONE) {
						System.err.println("[SIT] one or more blocks filled. Block to sit on: " + p.toString() + "  Block above: " + above.toString());
						if(!isOnServer()) {
							((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't sit here!\nArea blocked from above!"), 0);
						}
						return;
					}

					if((sOn.getType() != Element.TYPE_NONE && (g.getBlockStyle().cube)) || sDir.getType() != Element.TYPE_NONE) {

						boolean wedgeCompatible = false;
						if(sOn.getType() != Element.TYPE_NONE && g.getBlockStyle() == BlockStyle.WEDGE
								&& sDir.getType() != Element.TYPE_NONE
								&& ElementKeyMap.getInfo(sDir.getType()).getBlockStyle() == BlockStyle.WEDGE) {

							System.err.println("[SIT] Sitting on wedge with wedge in front");
							int infoIndexA = SegmentData.getInfoIndex(cubeResult.getCubePos());
							BlockShapeAlgorithm algoA = BlockShapeAlgorithm.getAlgo(g.getBlockStyle(),
									cubeResult.getSegment().getSegmentData().getOrientation(infoIndexA));

							int infoIndexB = sDir.getInfoIndex();
							BlockShapeAlgorithm algoB = BlockShapeAlgorithm.getAlgo(ElementKeyMap.getInfo(sDir.getType()).getBlockStyle(),
									sDir.getSegment().getSegmentData().getOrientation(infoIndexB));

							byte wedgeGravityValidDirA;
							if((notInGrav || ghostObject.attachedOrientation == Element.TOP
									|| ghostObject.attachedOrientation == Element.BOTTOM
							)) {
								//FIXME bottom and top mixed up
								wedgeGravityValidDirA = algoA.getWedgeGravityValidDir((byte) Element.getOpposite(side));
							} else {
								wedgeGravityValidDirA = algoA.getWedgeGravityValidDir((byte) side);
							}

							if(wedgeGravityValidDirA == Element.getOpposite(algoB.getWedgeOrientation()[0]) ||
									wedgeGravityValidDirA == Element.getOpposite(algoB.getWedgeOrientation()[1])) {
								wedgeCompatible = true;
							}

						}
						if(!wedgeCompatible) {
							System.err.println("[SIT] one or more blocks filled " + g + ": style " + g.getBlockStyle());
							if(!isOnServer()) {
								((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Can't sit here!\nArea blocked!"), 0);
							}

							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
								DebugDrawer.debugDraw(p.x, p.y, p.z, SegmentData.SEG_HALF, cubeResult.getSegment().getSegmentController());

								DebugDrawer.debugDraw(toSit.x, toSit.y, toSit.z, SegmentData.SEG_HALF, cubeResult.getSegment().getSegmentController());
								DebugDrawer.debugDraw(toLeg.x, toLeg.y, toLeg.z, SegmentData.SEG_HALF, cubeResult.getSegment().getSegmentController());
							}

							return;
						}
					}

					System.err.println("[SIT] " + getState() + ": " + p + " -> " + toSit + "; " + sOn + ", " + sDir + ", " + sBelow);

					if(!isOnServer() && getOwnerState() instanceof PlayerState) {
						((PlayerState) getOwnerState()).sendSimpleCommand(SimplePlayerCommands.SIT_DOWN,
								cubeResult.getSegment().getSegmentController().getId(),
								ElementCollection.getIndex(p),
								ElementCollection.getIndex(toSit),
								ElementCollection.getIndex(toLeg)

						);
					} else {
						System.err.println("[SIT] " + getState() + " Creature sitting down!");

						getOwnerState().sitDown(cubeResult.getSegment().getSegmentController(), p, toSit, toLeg);
					}
				}
			}
		}
	}

	@Override
	public void createConstraint(Physical a, Physical b, Object userData) {

	}

	@Override
	public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin, Vector3f minTmp, Vector3f maxTmp, Transform instead) {
		float m = capsule.getMargin();

		capsule.setMargin(margin);
		if(instead != null) {
			capsule.getAabb(instead, oMin, oMax);
		} else {
			capsule.getAabb(getWorldTransform(), oMin, oMax);
		}
		capsule.setMargin(m);
	}

	@Override
	public void initPhysics() {

		ghostObject = new PairCachingGhostObjectAlignable(CollisionType.CHARACTER, this.getPhysicsDataContainer(), this);

		ghostObject.setWorldTransform(getInitialTransform());

		capsule = new CapsuleShapeExt(this, getCharacterWidth(), getCharacterHeight());

		capsule.setMargin(getCharacterMargin());
		ghostObject.setCollisionShape(capsule);

		ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

		ghostObject.setUserPointer(this.getId());

		characterController = new KinematicCharacterControllerExt(this, ghostObject, capsule, stepHeight);

		getPhysicsDataContainer().collisionGroup = CollisionFilterGroups.CHARACTER_FILTER;
		getPhysicsDataContainer().collisionMask = CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.DEBRIS_FILTER;

		getPhysicsDataContainer().setObject(ghostObject);
		getPhysicsDataContainer().setShape(capsule);

		getPhysicsDataContainer().updatePhysical(0);

		characterController.setGravity(0f);

		setFlagPhysicsInit(true);

		if(!isOnServer()) {
			flagActivatedStuckProtection();
		}
	}

	public void damage(float damage, Damager from) {
		if(!canAttack(from)) {
			return;
		}
		if(getOwnerState().isVulnerable()) {
			getNetworkObject().hit.set(true);
			lastHit = getState().getUpdateTime();

			AbstractOwnerState oState = getOwnerState();

			if(oState != null) {
				oState.damage(damage, this, from);
				//			System.err.println("[SERVER] DAMAGING "+oState+": "+damage+" -> "+oState.getHealth());
			} else {
				System.err.println("Exception: trying to Damage: Warning " + this + " has no owner state " + getState());
			}
		}
	}

	@Override
	public void destroy(Damager destroyer) {
		if(isOnServer() && !this.isMarkedForDeleteVolatile()) {
			System.err.println("[SERVER] character " + this + " has been deleted by " + destroyer);
			this.setMarkedForDeleteVolatile(true);
		}
	}

	@Override
	public abstract void destroyPersistent();

	private void flagActivatedStuckProtection() {
		activatedStuckProtection = System.currentTimeMillis();
	}

	public void flagWapOutOfClient(int scheduledTimeAdd) {
		this.warpOutOfCollisionRequest = System.currentTimeMillis() + scheduledTimeAdd;
	}

	/**
	 * @return the activatedStuckProtection
	 */
	public long getActivatedStuckProtection() {
		return activatedStuckProtection;
	}

	/**
	 * @param activatedStuckProtection the activatedStuckProtection to set
	 */
	public void setActivatedStuckProtection(long activatedStuckProtection) {
		this.activatedStuckProtection = activatedStuckProtection;
	}

	public KinematicCharacterControllerExt getCharacterController() {
		return characterController;
	}

	@Override
	public abstract float getCharacterHeightOffset();

	@Override
	public abstract float getCharacterHeight();

	@Override
	public abstract float getCharacterWidth();

	//	private void checkProximityAlignment() {
	//		boolean found = false;
	//		OverlappingPairCache overlappingPairCache = getPhysics().getDynamicsWorld().getBroadphase().getOverlappingPairCache();
	//		for(int i = 0; i < overlappingPairCache.getNumOverlappingPairs(); i++){
	//			BroadphasePair broadphasePair = overlappingPairCache.getOverlappingPairArray().get(i);
	//			Object other;
	//			if(broadphasePair.pProxy0.clientObject == ghostObject){
	//				other = broadphasePair.pProxy1.clientObject;
	//			}else{
	//				other = broadphasePair.pProxy0.clientObject;
	//			}
	//			if(other instanceof RigidBodyExt){
	//				SegmentController segmentController = ((RigidBodyExt)other).getSegmentController();
	//
	//				if(segmentController instanceof Ship){
	//					align = segmentController;
	////					System.err.println("Local alignment with "+segmentController+" on "+segmentController.getState());
	//					GlUtil.getRightVector(getCharacterController().upAxisDirection[0], segmentController.getWorldTransform());
	//					GlUtil.getUpVector(getCharacterController().upAxisDirection[1], segmentController.getWorldTransform());
	//					GlUtil.getForwardVector(getCharacterController().upAxisDirection[2], segmentController.getWorldTransform());
	//
	//					GlUtil.setRightVector(getCharacterController().upAxisDirection[0], getWorldTransform());
	//					GlUtil.setUpVector(getCharacterController().upAxisDirection[1], getWorldTransform());
	//					GlUtil.setForwardVector(getCharacterController().upAxisDirection[2], getWorldTransform());
	//
	//					ghostObject.setWorldTransform(getWorldTransform());
	//					getPhysicsDataContainer().updatePhysical();
	//
	//
	//					found = true;
	//				}
	////				System.err.println("Broadphase overlapping: "+segmentController);
	//
	//			}
	//		}
	//
	//		if(!found){
	//			align = null;
	//			getCharacterController().upAxisDirection[0].set(KinematicCharacterControllerExt.upAxisDirectionDefault[0]);
	//			getCharacterController().upAxisDirection[1].set(KinematicCharacterControllerExt.upAxisDirectionDefault[1]);
	//			getCharacterController().upAxisDirection[2].set(KinematicCharacterControllerExt.upAxisDirectionDefault[2]);
	//		}else{
	//
	//		}
	//	}
	//
	//	@Override
	//	public void removeGravity(){
	//		super.removeGravity();
	//
	//		getCharacterController().upAxisDirection[0].set(KinematicCharacterControllerExt.upAxisDirectionDefault[0]);
	//		getCharacterController().upAxisDirection[1].set(KinematicCharacterControllerExt.upAxisDirectionDefault[1]);
	//		getCharacterController().upAxisDirection[2].set(KinematicCharacterControllerExt.upAxisDirectionDefault[2]);
	//
	//		GlUtil.setRightVector(getCharacterController().upAxisDirection[0], getWorldTransform());
	//		GlUtil.setUpVector(getCharacterController().upAxisDirection[1], getWorldTransform());
	//		GlUtil.setForwardVector(getCharacterController().upAxisDirection[2], getWorldTransform());
	//		getCharacterController().stopJump();
	//		getCharacterController().setGravity(0);
	//
	//		ghostObject.localWorldTransform = null;
	//		ghostObject.setAttached(null);
	//
	//	}

	public abstract Vector3i getBlockDim();

	public abstract Transform getHeadWorldTransform();

	public SegmentPiece getNearestPiece(boolean ignoreNotPhysical) {
		try {
			SegmentPiece nearestPiece = getNearestPiece(5f, ignoreNotPhysical);
			return nearestPiece;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public abstract ArrayList<? extends AbstractOwnerState> getAttachedPlayers();

	public GameTransformable getNearestEntity(boolean ignoreNotPhysical) {
		try {
			GameTransformable nearestPiece = getNearestEntity(5f, ignoreNotPhysical);
			return nearestPiece;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public GameTransformable getNearestEntity(float distance, boolean ignoreNotPhysical) {
		if(getAttachedPlayers().isEmpty()) {
			return null;
		}

		AbstractOwnerState playerState = getAttachedPlayers().get(0);

		Vector3f pos = new Vector3f(getHeadWorldTransform().origin);

		Vector3f to = new Vector3f();

		Vector3f dir = playerState.getForward(new Vector3f());
		dir.scale(distance);

		to.add(pos, dir);

		CubeRayCastResult rayCallback = new CubeRayCastResult(
				pos, to, false);
		rayCallback.setOnlyCubeMeshes(false);
		rayCallback.setIgnoereNotPhysical(ignoreNotPhysical);
		CollisionWorld.ClosestRayResultCallback result = getPhysics().testRayCollisionPoint(
				pos, to, rayCallback, false);

		if(result.hasHit() && result.collisionObject != null) {
			if(result.collisionObject instanceof RigidBodySegmentController) {
				return ((RigidBodySegmentController) result.collisionObject).getSegmentController();
			}
			if(result.collisionObject instanceof PairCachingGhostObjectAlignable) {
				return ((PairCachingGhostObjectAlignable) result.collisionObject).getObj();
			}
		}

		return null;
	}

	public SegmentPiece getNearestPiece(float distance, boolean ignoreNotPhysical) {

		if(getAttachedPlayers().isEmpty()) {
			System.err.println("Exception: NO nearest piece (no player attached)");
			return null;
		}

		AbstractOwnerState playerState = getAttachedPlayers().get(0);

		Vector3f pos = new Vector3f(getHeadWorldTransform().origin);

		Vector3f to = new Vector3f();

		Vector3f dir = playerState.getForward(new Vector3f());
		dir.scale(distance);

		to.add(pos, dir);

		CubeRayCastResult rayCallback = new CubeRayCastResult(
				pos, to, false);
		rayCallback.setOnlyCubeMeshes(true);
		rayCallback.setIgnoereNotPhysical(ignoreNotPhysical);
		CollisionWorld.ClosestRayResultCallback result = getPhysics().testRayCollisionPoint(
				pos, to, rayCallback, false);

		//		ClosestRayResultCallback result = getPhysics().testRayCollisionPoint(pos,
		//				to,  false, null, null, false, cachedLastNearedPieceSegment, false);

		if(result.hasHit() && result.collisionObject != null) {
			if(result instanceof CubeRayCastResult && ((CubeRayCastResult) result).getSegment() != null) {

				CubeRayCastResult cubeResult = (CubeRayCastResult) result;
				SegmentController segmentController = cubeResult.getSegment().getSegmentData().getSegmentController();
				Segment seg = cubeResult.getSegment();

				SegmentPiece p = new SegmentPiece(seg, cubeResult.getCubePos());
				System.err.println("HIT RESULT near: " + p + ", on " + segmentController);

				return p;
			}
		}

		return null;
	}

	@Override
	public ProjectileController getParticleController() {
		if(!isOnServer()) {
			return ((GameClientState) getState()).getParticleController();
		} else {
			return ((GameServerState) getState()).getUniverse().getSector(getSectorId()).getParticleController();
		}
	}

	/**
	 * @return the shopsInDistance
	 */
	@Override
	public Set<ShopInterface> getShopsInDistance() {
		return shopsInDistance;
	}

	public abstract Transform getShoulderWorldTransform();

	@Override
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	@Override
	public void handleCollision(SegmentPiece segmentPiece, Vector3f currentPosition) {
		if(isOnServer()) {
			//			System.err.println("[CHARACTER] ################### checking colission damage: "+currentPosition);
			if(segmentPiece.getType() == ElementKeyMap.TERRAIN_LAVA_ID && System.currentTimeMillis() - lastCollisionHurt > 500) {
				for(AbstractOwnerState s : getAttachedPlayers()) {
					s.damage(10, null, segmentPiece.getSegment().getSegmentController());
				}
				lastCollisionHurt = System.currentTimeMillis();
			}
		} else {
			if(segmentPiece.getType() == ElementKeyMap.SIGNAL_TRIGGER_STEPON) {
				ActivationTrigger activationTrigger = new ActivationTrigger(segmentPiece.getAbsoluteIndex(), getPhysicsDataContainer().getObject(), segmentPiece.getType());
				ActivationTrigger ret = segmentPiece.getSegment().getSegmentController().getTriggers().get(activationTrigger);
				if(ret == null) {
					System.err.println("ADDING CHARACTER TRIGGER: " + activationTrigger + "; " + segmentPiece.getSegmentController().getTriggers().size());
					segmentPiece.getSegment().getSegmentController().getTriggers().add(activationTrigger);
				} else {
					ret.ping();
				}
			}
		}
	}

	public void sendOwnerMessage(Object[] msg, byte type) {
		if(isOnServer()) {
			if(getAttachedPlayers().size() > 0 && getAttachedPlayers().get(0) instanceof PlayerState) {
				PlayerState p = ((PlayerState) getAttachedPlayers().get(0));
				p.sendServerMessage(new ServerMessage(msg, type, p.getId()));
			}
		}
	}

	public boolean canAttack(Damager from) {
		if(isSpectator() || (from != null && from instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) from).isSpectator())) {
			if(from instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) from).isClientOwnObject()) {
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot attack\nspectators!"), 0);
			}
			return false;
		}
		if(isOnServer()) {
			Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
			if(sector != null && sector.isProtected()) {
				if(from != null && from instanceof PlayerControllable) {
					List<PlayerState> attachedPlayers = ((PlayerControllable) from).getAttachedPlayers();
					for(int i = 0; i < attachedPlayers.size(); i++) {
						PlayerState ps = attachedPlayers.get(i);
						if(getState().getUpdateTime() - ps.lastSectorProtectedMsgSent > 5000) {
							ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
							ps.sendServerMessage(new ServerMessage(Lng.astr("This Sector is Protected!"), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
						}
					}
				}
				return false;
			}
		}
		return true;
	}

//	@Override
//	public ParticleHitCallback handleHit(ParticleHitCallback c, Damager damager, float damage, float damageBeforeShield, Vector3f startPos, Vector3f endPos, boolean shieldAbsorbed, long weaponId) {
//		if (isOnServer()) {
//			if (!canAttack(damager)) {
//				c.hit = false;
//				return c;
//			}
//			float restDamage = 0;
//			float armorEfficiency = 0;
//			int explosiveRadius = 0;
//			float pushForce = 0;
//			float pullForce = 0;
//			float grabForce = 0;
//			float powerDamage = 0;
////			EffectElementManager<?, ?, ?> effect = null;
////			if (effectType != 0) {
////				effect = getEffect(damager, effectType);
////
////				if (effect != null) {
////
////					effect.onHit(this);
////
////					float damageBef = damage;
////					//				System.err.println("HIT EFFECT::: "+effect+": Pier "+effect.isPiercing()+"; punch "+effect.isPunchThrough()+"; expl "+effect.isExplosive());
////
////					armorEfficiency = effect.getCannonArmorEfficiency();
////					pushForce = effect.getCannonPush() * effectRatio * effectSize;
////					pullForce = effect.getCannonPull() * effectRatio * effectSize;
////					grabForce = effect.getCannonGrab() * effectRatio * effectSize;
////
////					if (effect.isExplosive()) {
////						explosiveRadius = (int) Math.max(0, Math.min(1, effectRatio * effect.getCannonExplosiveRadius()));
////					}
////
////					damage = effect.modifyBlockDamage(damage, DamageDealerType.PROJECTILE, effectRatio);
////					
////					//				System.err.println("DAMAGE MOD: "+damageBef+" -> "+damage+" -rest> "+restDamage+" ;hpBef: "+healthBefore );
////				}
////			} else {
////				
////			}
//			damage(damage, damager);
//		} else {
//			// prepare to add some explosions
//			GameClientState s = (GameClientState) getState();
//			s.getWorldDrawer().getExplosionDrawer().addExplosion(c.hitPointWorld);
//
//		}
//		return c;
//	}

	@Override
	public boolean isVulnerable() {
		return getOwnerState().isVulnerable();
	}

	@Override
	public boolean checkAttack(Damager from, boolean checkDocked,
	                           boolean notifyFaction) {
		return canAttack(from);
	}

	public void drawDebugTransform() {

		Transform t = new Transform();

		if(isOnServer() && GameClientState.staticSector == this.getSectorId()) {
			ghostObject.getWorldTransform(t);
		} else {
			ghostObject.getWorldTransform(t);
		}

		Vector3f up = GlUtil.getUpVector(new Vector3f(), t);
		Vector3f right = GlUtil.getRightVector(new Vector3f(), t);
		Vector3f forward = GlUtil.getForwardVector(new Vector3f(), t);

		if(isOnServer()) {
			Vector3f a = new Vector3f();
			a.add(up);
			a.add(right);
			a.add(forward);
			a.normalize();
			a.scale(4);
			a.add(t.origin);
			DebugLine l = new DebugLine(new Vector3f(t.origin), a, new Vector4f(1, 0, 1, 1));
			if(!DebugDrawer.lines.contains(l)) {
				DebugDrawer.lines.add(l);
			}
		}

		Vector3f up1 = new Vector3f(up);
		Vector3f right1 = new Vector3f(right);
		Vector3f forward1 = new Vector3f(forward);

		up.set(0, 0, 0);
		right.set(0, 0, 0);
		forward.set(0, 0, 0);

		up1.scale(3);
		right1.scale(3);
		forward1.scale(3);

		up.add(t.origin);
		right.add(t.origin);
		forward.add(t.origin);
		up1.add(t.origin);
		right1.add(t.origin);
		forward1.add(t.origin);

		Vector4f uC = new Vector4f(0, 1, 0, 1);
		Vector4f rC = new Vector4f(1, 0, 0, 1);
		Vector4f fC = new Vector4f(0, 0, 1, 1);

		if(isOnServer()) {
			uC.x += 0.8f;
			rC.z += 0.8f;
			fC.y += 0.8f;
		}

		DebugLine uLine = new DebugLine(up, up1, uC);
		DebugLine rLine = new DebugLine(right, right1, rC);
		DebugLine fLine = new DebugLine(forward, forward1, fC);
		if(!DebugDrawer.lines.contains(uLine)) {
			DebugDrawer.lines.add(uLine);
		}
		if(!DebugDrawer.lines.contains(rLine)) {
			DebugDrawer.lines.add(rLine);
		}
		if(!DebugDrawer.lines.contains(fLine)) {
			DebugDrawer.lines.add(fLine);
		}

	}

	protected abstract float getCharacterMargin();

	public void inPlaceAttachedUpdate(long time) {
		characterController.loadAttached(time);
		getPhysicsDataContainer().updatePhysical(time);
	}

	/**
	 * @return the hasSpawnedOnServer
	 */
	public boolean isHasSpawnedOnServer() {
		return hasSpawnedOnServer;
	}

	/**
	 * @param hasSpawnedOnServer the hasSpawnedOnServer to set
	 */
	public void setHasSpawnedOnServer(boolean hasSpawnedOnServer) {
		this.hasSpawnedOnServer = hasSpawnedOnServer;
	}

	@Override
	public boolean needsManifoldCollision() {
		return false;
	}

	@Override
	public void onCollision(ManifoldPoint pt, Sendable collider) {
		if(pt.combinedRestitution > 0 || pt.appliedImpulseLateral1 > 0 || pt.appliedImpulseLateral2 > 0) {
			System.err.println("Restitution " + pt.combinedRestitution + "; A " + pt.appliedImpulseLateral1 + "; B " + pt.appliedImpulseLateral2 + "; Com " + pt.appliedImpulse);
		}
		//			if(pt.appliedImpulse > 0.1f){
		//				if(isOnServer()){
		//					damage(pt.appliedImpulse *10f, collider);
		//				}
		//			}

	}

	@Override
	public String toString() {
		return "PlayerCharacter[(" + uniqueIdentifier + ")" + "(" + getId() + ")]";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#sendHitConfirm()
	 */
	@Override
	public void sendHitConfirm(byte damageType) {
		getOwnerState().sendHitConfirm(damageType);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getName()
	 */
	@Override
	public String getName() {
		return getOwnerState().getName();
	}

	@Override
	public Vector3f getForward() {
		return getOwnerState().getForward(new Vector3f());
	}

	@Override
	public Vector3f getRight() {
		return getOwnerState().getRight(new Vector3f());
	}

	@Override
	public Vector3f getUp() {
		return getOwnerState().getUp(new Vector3f());
	}

	@Override
	public abstract E getOwnerState();

	protected void handleBlockActivationReaction(CharacterBlockActivation dequeue) {
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(dequeue.objectId);
		if(sendable != null && sendable instanceof SendableSegmentController) {
			SendableSegmentController s = (SendableSegmentController) sendable;

			SegmentPiece segmentPiece = s.getSegmentBuffer().getPointUnsave(dequeue.location);//autorequest true previously

			if(segmentPiece != null && ElementInformation.isMedical(segmentPiece.getType())) {

				if(s.getCooldownBlocks().containsKey(dequeue.location)) {
					long timeUsed = s.getCooldownBlocks().get(dequeue.location);
					long timeSinceUsed = System.currentTimeMillis() - timeUsed;
					long timeTillNext = MEDICAL_REUSE_MS - timeSinceUsed;
					sendControllingPlayersServerMessage(Lng.astr("Medical supplies depleted\nResupply in %s sec", (timeTillNext / 1000)), ServerMessage.MESSAGE_TYPE_ERROR);
				} else {
					getOwnerState().heal(MEDICAL_HP, this, s);
					s.getCooldownBlocks().put(dequeue.location, System.currentTimeMillis());
					sendControllingPlayersServerMessage(Lng.astr("Healed for %s HP\nResupply in %s sec", MEDICAL_HP, (MEDICAL_REUSE_MS / 1000)), ServerMessage.MESSAGE_TYPE_INFO);
				}

			}

		}
	}

	/**
	 * @return the speed
	 */
	public abstract float getSpeed();

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

	@Override
	public boolean onGround() {
		return characterController.onGround();
	}

	@Override
	public PlayerState getConversationPartner() {
		return getOwnerState().getConversationPartner();
	}

	public void transformBeam(BeamState con) {
		//		Transform t = new Transform();
		//		t.setIdentity();
		//
		//		if(getOwnerState() instanceof PlayerState){
		//			((PlayerState) getOwnerState()).getWordTransform(t);
		//		}

		getWorldTransform().transform(con.from);
		//		t.transform(con.from);
	}

	public void popupOwnClientMessage(String message, int messageType) {
		if(isClientOwnObject()) {
			GameClientController controller = ((GameClientState) getState()).getController();

			switch(messageType) {
				case ServerMessage.MESSAGE_TYPE_ERROR:
					controller.popupAlertTextMessage(message, 0);
					break;
				case ServerMessage.MESSAGE_TYPE_INFO:
					controller.popupInfoTextMessage(message, 0);
					break;
				case ServerMessage.MESSAGE_TYPE_SIMPLE:
					controller.getState().chat(getOwnerState().getName(), message, "[MESSAGE]", false);
					break;
				case ServerMessage.MESSAGE_TYPE_WARNING:
					controller.popupGameTextMessage(message, 0);
					break;
				default:
					assert false;
					break;

			}
		}
	}

	@Override
	public int handleSalvage(BeamState hittingBeam, int beamHits,
	                         BeamHandlerContainer<?> container, Vector3f from,
	                         SegmentPiece hitPiece, Timer timer,
	                         Collection<Segment> updatedSegments) {

		if(hitPiece.getSegment() == null) {
			assert (false);
			return 0;
		}
		//		System.err.println("HIT: "+cubeResult.getSegment()+"; "+cubeResult.cubePos+"; "+cubeResult.collisionObject);

		if(container.getOwnerState() != null && container.getOwnerState() instanceof PlayerState && container.getOwnerState().getFactionId() != 0 && container.getOwnerState().getFactionId() == hitPiece.getSegment().getSegmentController().getFactionId()) {
			if(!hitPiece.getSegment().getSegmentController().isSufficientFactionRights(((PlayerState) container.getOwnerState()))) {

				popupOwnClientMessage("You don't have the rank\nin your faction to\nsalvage this", ServerMessage.MESSAGE_TYPE_ERROR);
				return 0;
			}
		}

		if(isOnServer() && beamHits > 0) {

			byte orientation = (hitPiece.getOrientation());
			short type = hitPiece.getType();

			if(ElementKeyMap.isValidType(type) && ElementKeyMap.isValidType(ElementKeyMap.getInfoFast(type).getSourceReference())) {
				type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
			}

			if(hitPiece.getSegmentController().isScrap()) {
				if(Universe.getRandom().nextFloat() > 0.5f) {
					type = ElementKeyMap.SCRAP_ALLOYS;
				} else {
					type = ElementKeyMap.SCRAP_COMPOSITE;
				}
			}

			AbstractOwnerState playerState = getAttachedPlayers().get(0);

			boolean canPickup = playerState.getInventory().canPutIn(type, 1);
			int injectedResourceQuantity = 0;
			boolean isLucky = false;

			if(canPickup && ElementKeyMap.hasResourceInjected(type, orientation)) {

				float bonus = VoidElementManager.PERSONAL_SALVAGE_BEAM_BONUS;

				int received = (int) bonus;

				float chance = bonus - received;

				if(Math.random() < chance) {
					received += 2;
					isLucky = true;
				}

				int miningBonus = getMiningBonus(hitPiece.getSegmentController());

				injectedResourceQuantity = received * miningBonus;
				canPickup = playerState.getInventory().canPutIn(ElementKeyMap.orientationToResIDMapping[orientation], injectedResourceQuantity);
			}

			if(canPickup) {
				boolean removeElement = hitPiece.getSegment().removeElement(hitPiece.getPos(tmpLocalPos), false);
				if(removeElement) {
					updatedSegments.add(hitPiece.getSegment());
					((RemoteSegment) hitPiece.getSegment()).setLastChanged(System.currentTimeMillis());
					hitPiece.refresh();
					assert (hitPiece.getType() == Element.TYPE_NONE);

					hitPiece.setHitpointsByte(1);

//					segmentPiece.getSegment().getSegmentController().sendBlockMod(new RemoteSegmentPiece(segmentPiece, getNetworkObject()));

					hitPiece.getSegmentController().sendBlockSalvage(hitPiece);

					if(ElementKeyMap.isValidType(type)) {
						hitPiece.getSegmentController().getHpController().onManualRemoveBlock(ElementKeyMap.getInfo(type));
					}

					if(getAttachedPlayers().size() > 0) {

						playerState.modDelayPersonalInventory(type, 1);

						if(ElementKeyMap.hasResourceInjected(type, orientation)) {

							if(isLucky) {
								sendOwnerMessage(Lng.astr("Lucky!\nThis block yielded\n%s raw resources!", injectedResourceQuantity), ServerMessage.MESSAGE_TYPE_INFO);
							}

							playerState.modDelayPersonalInventory(ElementKeyMap.orientationToResIDMapping[orientation], injectedResourceQuantity);
						}
					}
				}
			} else {
				((PlayerState) playerState).sendServerMessage(new ServerMessage(Lng.astr("No empty slots in inventory!"), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
			}
		}
		return beamHits;
	}

	/**
	 * @return the beamHandler
	 */
	@Override
	public PersonalBeamHandler<E> getHandler() {
		return beamHandler;
	}

	/**
	 * @return the actionUpdateNum
	 */
	@Override
	public int getActionUpdateNum() {
		return actionUpdateNum;
	}

	/**
	 * @param actionUpdateNum the actionUpdateNum to set
	 */
	@Override
	public void setActionUpdateNum(int actionUpdateNum) {
		this.actionUpdateNum = actionUpdateNum;
	}

	/**
	 * @return the ghostObject
	 */
	public PairCachingGhostObjectAlignable getGhostObject() {
		return ghostObject;
	}

	//	/* (non-Javadoc)
//	 * @see org.schema.game.common.controller.DamageBeamHittable#handleBeamDamage(org.schema.game.common.controller.elements.BeamState, org.schema.game.common.controller.BeamHandlerContainer, long, javax.vecmath.Vector3f, javax.vecmath.Vector3f, org.schema.game.common.data.physics.CubeRayCastResult, org.schema.schine.graphicsengine.core.Timer)
//	 */
//	@Override
//	public int handleBeamDamage(
//			BeamState beam, int beamHits,
//			BeamHandlerContainer<? extends SimpleTransformableSendableObject> owner,
//			Vector3f from, Vector3f to,
//			CubeRayCastResult cubeResult, boolean ignoreShields, Timer timer) {
//		if (isOnServer()) {
//
//			if (beamHits > 0) {
//				float damage = (int) (beamHits * beam.getPower());
//				if (!canAttack(owner)) {
//					return 0;
//				}
//				
//				float restDamage = 0;
//				float armorEfficiency = 0;
//				int explosiveRadius = 0;
//				float pushForce = 0;
//				float pullForce = 0;
//				float grabForce = 0;
//				float powerDamage = 0;
//				
//				EffectElementManager<?, ?, ?> effect = null;
//				if (beam.effectType != 0) {
//					effect = getEffect(owner, beam.effectType);
//
//					if (effect != null) {
//
//						effect.onHit(this);
//
//						float damageBef = damage;
//						//				System.err.println("HIT EFFECT::: "+effect+": Pier "+effect.isPiercing()+"; punch "+effect.isPunchThrough()+"; expl "+effect.isExplosive());
//
//						armorEfficiency = effect.getCannonArmorEfficiency();
//						pushForce = effect.getCannonPush() * beam.effectRatio * beam.effectSize;
//						pullForce = effect.getCannonPull() * beam.effectRatio * beam.effectSize;
//						grabForce = effect.getCannonGrab() * beam.effectRatio * beam.effectSize;
//
//						if (effect.isExplosive()) {
//							explosiveRadius = (int) Math.max(0, Math.min(1, beam.effectRatio * effect.getCannonExplosiveRadius()));
//						}
//
//						damage = effect.modifyBlockDamage(damage, DamageDealerType.PROJECTILE, beam.effectRatio);
//						
//						//				System.err.println("DAMAGE MOD: "+damageBef+" -> "+damage+" -rest> "+restDamage+" ;hpBef: "+healthBefore );
//					}
//				} else {
//					
//				}
//				
//				damage(damage, owner);
//			}
//
//		}
//		return beamHits;
//	}
	public void transformAimingAt(Vector3f to,
	                              Damager from, SimpleGameObject target, Random random) {
		to.set(0, 0, 0);
		to.add(target.getClientTransform().origin);
	}

	public FlashLight getFlashLightActive() {
		return flashLightActive;
	}

	public void setFlashLightActive(FlashLight flashLightActive) {
		this.flashLightActive = flashLightActive;
	}

	@Override
	public void sendClientMessage(String str, byte type) {
		if(isClientOwnObject()) {
			switch(type) {
				case (ServerMessage.MESSAGE_TYPE_INFO) -> ((GameClientState) getState()).getController().popupInfoTextMessage(str, 0);
				default -> ((GameClientState) getState()).getController().popupAlertTextMessage(str, 0);
			}
		}
	}

	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
		if(getOwnerState() != null) {
			getOwnerState().sendServerMessage(astr, msgType);
		}
	}

	@Override
	public float getDamageGivenMultiplier() {
		return 1;
	}

	@Override
	public TopLevelType getTopLevelType() {
		return TopLevelType.ASTRONAUT;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		InterEffectSet attackEffectSet = getOwnerState().getAttackEffectSet(weaponId, damageDealerType);
		assert (attackEffectSet != null);
		return attackEffectSet;
	}

	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return getOwnerState().getMetaWeaponEffect(weaponId, damageDealerType);
	}

	private DamageBeamHitHandler damageBeamHitHandler = new DamageBeamHitHandlerCharacter();

	public DamageBeamHitHandler getDamageBeamHitHandler() {
		return damageBeamHitHandler;
	}

	public boolean canBeDamagedBy(Damager from, DamageDealerType beam) {
		return true;
	}

}
