package org.schema.game.common.data.creature;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.PersonalFactoryInventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.Universe;
import org.schema.game.network.objects.TargetableAICreatureNetworkObject;
import org.schema.game.server.ai.program.creature.character.AICreatureProgramInterface;
import org.schema.game.server.controller.SegmentPathCallback;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.UniqueInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.List;

public abstract class AIPlayer extends AbstractOwnerState implements SegmentPathCallback, InteractionInterface, UniqueInterface {

	private static final int PROXIMITY_CHECKPERIOD = 5000;
	private static final float DEFAULT_HEALTH = 500;
	private final AICreature<? extends AIPlayer> creature;
	private final ObjectArrayFIFOQueue<AICreatureMessage> aiMessages = new ObjectArrayFIFOQueue<AICreatureMessage>();
	private final Transform orientation = new Transform();
	private final Transform orientationTo = new Transform();
	private final Quat4f targetQuat = new Quat4f();
	private final ObjectArrayList<SimpleTransformableSendableObject> proximityEntities = new ObjectArrayList<SimpleTransformableSendableObject>();
	private final Object2ObjectOpenHashMap<PlayerState, String> conversationStates = new Object2ObjectOpenHashMap<PlayerState, String>();
	protected Vector3f dir = new Vector3f();
	protected long lastTargetSet;
	private float health;
	private Transform target;
	private List<Long> currentPath;
	private float rotationSpeed = 10;
	private long lastCheck;
	private long currentLag;
	private long lastLagReceived;

	public AIPlayer(AICreature<? extends AIPlayer> creature) {
		super();
		health = getMaxHealth();
		this.creature = creature;
		orientation.setIdentity();
		orientationTo.setIdentity();

		setInventory(initInventory());

		setPersonalFactoryInventoryCapsule(new PersonalFactoryInventory(this, CAPSULE_INV, ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID));
		setPersonalFactoryInventoryMicro(new PersonalFactoryInventory(this, COMPONENT_INV, ElementKeyMap.FACTORY_COMPONENT_FAB_ID));
		setPersonalFactoryInventoryMacroBlock(new PersonalFactoryInventory(this, MACRO_BLOCK_INV, ElementKeyMap.FACTORY_BLOCK_ASSEMBLER_ID));
	}

	protected abstract boolean isFlying();

	protected abstract Inventory initInventory();
	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
	}
	public abstract void resetTargetToMove();
	public int getSectorId() {
		return getAbstractCharacterObject().getSectorId();
	}
	public abstract boolean isTargetReachTimeout();
	@Override
	public byte getFactionRights() {
		return 0;
	}
	public abstract boolean isTargetReachLocalTimeout();

	public abstract void setTargetToMove(Vector3f pos, long timeout);

	@Override
	public void damage(float damage, Destroyable destroyable,
	                   Damager from) {
		health -= damage;
		if (damage > 0) {
			from.sendHitConfirm(Damager.CHARACTER);
		}
		if (health <= 0) {
			dieOnServer(from);
		}
	}

	@Override
	public void heal(float heal, Destroyable destroyable, Damager from) {
		health = Math.min(getMaxHealth(), health + heal);
	}

	@Override
	public float getMaxHealth() {
		return DEFAULT_HEALTH;
	}

	@Override
	public float getHealth() {
		return health;
	}

	@Override
	public Vector3f getRight(Vector3f out) {
		return GlUtil.getRightVector(out, orientation);
	}

	@Override
	public Vector3f getUp(Vector3f out) {
		return GlUtil.getUpVector(out, orientation);
	}

	@Override
	public Vector3f getForward(Vector3f out) {
		return GlUtil.getForwardVector(out, orientation);
	}

	@Override
	public boolean isInvisibilityMode() {
		return false;
	}

	@Override
	public boolean isOnServer() {
		return creature.isOnServer();
	}

	@Override
	public TargetableAICreatureNetworkObject getNetworkObject() {
		return creature.getNetworkObject();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractOwnerState#isVulnerable()
	 */
	@Override
	public boolean isVulnerable() {
		return creature.getAffinity() == null || !(creature.getAffinity() instanceof SegmentController) || ((SegmentController) creature.getAffinity()).isVulnerable();
	}

	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		dir.set(0, 0, 0);
		updateInventory();

		if (isOnServer()) {
			checkProximityObjects();
			handleAIMessages();
			updateServer();
		} else {
			updateClient();
		}
		moveToTarget(timer);

		interpolateOrientation(timer);
		
		if(timer.currentTime - lastLagReceived > 7000){
			currentLag = 0;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AIPlayer(" + getName() + ", " + getId() + ")";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractOwnerState#getAbstractCharacterObject()
	 */
	@Override
	public AbstractCharacter<? extends AbstractOwnerState> getAbstractCharacterObject() {
		return creature;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractOwnerState#getId()
	 */
	@Override
	public int getId() {
		return creature.getId();
	}

	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		getNetworkObject().health.set(health);
		boolean changedSit = getNetworkObject().sittingState.getLongArray()[0] != sittingOnId;
		getNetworkObject().sittingState.set(0, sittingOnId);
		getNetworkObject().sittingState.set(1, ElementCollection.getIndex(sittingPos));
		getNetworkObject().sittingState.set(2, ElementCollection.getIndex(sittingPosTo));
		getNetworkObject().sittingState.set(3, ElementCollection.getIndex(sittingPosLegs));
		
		getNetworkObject().climbingState.set(0, climbingId);
		getNetworkObject().climbingState.set(1, climbingPos);
		getNetworkObject().climbingState.set(2, climbingDir);
	}

	private void destroy() {
		creature.markForPermanentDelete(true);
		creature.setMarkedForDeleteVolatile(true);
	}

	private void dieOnServer(Damager from) {
		System.err.println(from + " killed AI " + this);
		destroy();
	}

	private void adjustForward(Vector3f dir) {
		Vector3f forward = new Vector3f(dir);
		forward.normalize();
		Vector3f up = new Vector3f(0, 1, 0);
		Vector3f right = new Vector3f();

		right.cross(up, forward);
		right.normalize();
		up.cross(forward, right);
		up.normalize();

		forward.normalize();

		GlUtil.setForwardVector(forward, orientationTo);
		GlUtil.setUpVector(up, orientationTo);
		GlUtil.setRightVector(right, orientationTo);

//		if(isOnServer() && this instanceof AICompositeCreaturePlayer){
//			System.err.println(getState()+" ORIENTATION TO: \n"+orientationTo.basis);
//			System.err.println(getState()+" WT: \n"+getWorldTransform().basis);
//		}

		creature.getPhysicsDataContainer().getObject().setWorldTransform(getWorldTransform());

	}

	private void interpolateOrientation(Timer timer) {

		//		Vector3f toRight = new Vector3f();
		//		Vector3f toForward = new Vector3f();
		//		Vector3f toUp = new Vector3f();
		//
		//		GlUtil.getRightVector(toRight, orientationTo);
		//		GlUtil.getForwardVector(toForward, orientationTo);
		//		GlUtil.getUpVector(toUp, orientationTo);
		//
		//
		//		Transform toMatrix = new Transform();
		//		GlUtil.setForwardVector(toForward, toMatrix);
		//		GlUtil.setUpVector(toUp, toMatrix);
		//		GlUtil.setRightVector(toRight, toMatrix);

		Quat4f from = new Quat4f();
		Quat4fTools.set(orientation.basis, from);

		Quat4f to = new Quat4f();
		Quat4fTools.set(orientationTo.basis, to);

		Quat4f res = new Quat4f();

		Quat4Util.slerp(from, to, Math.min(1, timer.getDelta() * rotationSpeed), res);
		res.normalize();

		if (res.w != 0) {
			orientation.setRotation(res);
//			if(isOnServer() && this instanceof AICompositeCreaturePlayer){
//				System.err.println("INTERPOLATION: "+from+" -> "+to+" ----> "+res+"\nR"+rotationSpeed+"\n: "+orientation.basis+"\nSETWT:\n"+getWorldTransform().basis);
//			}
		}
	}

	public boolean isAtTarget() {
		if (target != null) {
			Transform target = new Transform(this.target);

			Transform inv;
			if (creature.getAffinity() != null) {
				inv = new Transform(creature.getAffinity().getWorldTransform());
			} else {
				inv = new Transform();
				inv.setIdentity();
			}

			Transform worldTransform = new Transform(getWorldTransform());
			inv.inverse();
			inv.mul(worldTransform);
			worldTransform.set(inv);
			Vector3f d = new Vector3f();
			d.sub(target.origin, worldTransform.origin);

			boolean there = d.length() < 0.5f;
			if (there) {
				//				System.err.println("TARGET REACHED: "+there);
			} else {
				//				System.err.println("TOO MUCH: "+d.length());
			}
			return there;
		}

		return true;

	}

	public void moveToTarget(Timer timer) {
//		System.err.println(getState()+" MOVE TO: "+(target != null ? target.origin : "null"));
		if (creature.getAffinity() == null) {
			if (creature.isOnServer()) {
				//				System.err.println("[SERVER][AICREATURE] AI has no affinity. resetting move target "+this+" on ");
				resetTargetToMove();
				return;
			} else {
				if (target != null) {
					System.err.println("[CLIENT][AICREATURE] AI " + this + " has no affinity, but target. waiting...");
				}
				return;
			}

		}

		if (isSitting()) {
			return;
		}
		boolean hasTargetQuat = false;
		Vector3f lookDir = creature.getLookDir();
		if (lookDir.length() > 0) {
			hasTargetQuat = true;
			Vector3f projectedForward = new Vector3f();
			GlUtil.project(getUp(new Vector3f()), lookDir, projectedForward);
			projectedForward.normalize();

			Vector3f right = new Vector3f();

			right.cross(projectedForward, getUp(new Vector3f()));
			right.normalize();
			Transform t = new Transform();
			t.setIdentity();
			GlUtil.setForwardVector(projectedForward, t);
			GlUtil.setUpVector(getUp(new Vector3f()), t);
			GlUtil.setRightVector(right, t);

			Quat4fTools.set(t.basis, targetQuat);
		}

		if (target != null) {
			Transform target = new Transform(this.target);
//			System.err.println("TARGET!!! "+getState()+" "+target.origin+"; "+getCreature().getAffinity());
			Transform inv = new Transform(creature.getAffinity().getWorldTransform());
			Transform worldTransform = new Transform(getWorldTransform());
			inv.inverse();
			inv.mul(worldTransform);
			worldTransform.set(inv);

			if (isFlying()) {
				dir.sub(target.origin, worldTransform.origin);

				dir.normalize();
				creature.getAffinity().getWorldTransform().basis.transform(dir);

				dir.scale(timer.getDelta() * creature.getSpeed());
				if (dir.length() > 0) {
					creature.getCharacterController().setWalkDirectionStacked(dir);
				}

			} else {

				dir.sub(target.origin, worldTransform.origin);
				dir.normalize();
				creature.getAffinity().getWorldTransform().basis.transform(dir);
				dir.y = 0;

				if (target.origin.y - worldTransform.origin.y > 0.5f && creature.getCharacterController().canJump()) {
					creature.getCharacterController().jump();
				}
				if (worldTransform.origin.y - target.origin.y > 0.5f) {
					if (creature.getCharacterController().isJumping()) {
						creature.getCharacterController().breakJump(timer);
					}
				}
				dir.scale(timer.getDelta() * creature.getSpeed());

				if (dir.length() > 0) {
					creature.getCharacterController().setWalkDirectionStacked(dir);
				}
				//hanle on terrain
			}
			if (hasTargetQuat) {
				adjustForward(lookDir);
			} else {
//				System.err.println("ADJUST FORWARD: "+dir);
				adjustForward(dir);
			}
		} else {
			if (hasTargetQuat) {
				adjustForward(lookDir);
			}
		}
	}

	public Vector3f getMovingDir() {
		return dir;
	}

	@Override
	public String getName() {
		return creature.getRealName();
	}

	@Override
	public StateInterface getState() {
		return creature.getState();
	}

	public void initFromNetworkObject() {
		this.health = getNetworkObject().health.getFloat();

		sittingOnId = (int) getNetworkObject().sittingState.getLongArray()[0];
		ElementCollection.getPosFromIndex(getNetworkObject().sittingState.getLongArray()[1], sittingPos);
		ElementCollection.getPosFromIndex(getNetworkObject().sittingState.getLongArray()[2], sittingPosTo);
		ElementCollection.getPosFromIndex(getNetworkObject().sittingState.getLongArray()[3], sittingPosLegs);
		
		climbingId = (int) getNetworkObject().climbingState.getLongArray()[0];
		climbingPos = getNetworkObject().climbingState.getLongArray()[1];
		climbingDir = (int) getNetworkObject().climbingState.getLongArray()[2];
	}

	public void plotInstantPath() {

		if (creature.getAffinity() != null) {
			Vector3i posInAffinity = creature.getPosInAffinity(new Vector3i());
			SegmentPiece from = ((SegmentController) creature.getAffinity()).getSegmentBuffer().getPointUnsave(posInAffinity);//autorequest true previously

			if(from != null){
			int oX = (Integer) creature.getAiConfiguration().get(Types.ORIGIN_X).getCurrentState();
			int oY = (Integer) creature.getAiConfiguration().get(Types.ORIGIN_Y).getCurrentState();
			int oZ = (Integer) creature.getAiConfiguration().get(Types.ORIGIN_Z).getCurrentState();

			int rX = (Integer) creature.getAiConfiguration().get(Types.ROAM_X).getCurrentState();
			int rY = (Integer) creature.getAiConfiguration().get(Types.ROAM_Y).getCurrentState();
			int rZ = (Integer) creature.getAiConfiguration().get(Types.ROAM_Z).getCurrentState();

			Vector3i origin = new Vector3i(oX, oY, oZ);

//			assert(oX > Integer.MIN_VALUE && oY > Integer.MIN_VALUE && oZ > Integer.MIN_VALUE);

			BoundingBox roaming;
			if (rX > 0 && rY > 0 && rZ > 0) {
				roaming = new BoundingBox(new Vector3f(-rX, -rY, -rZ), new Vector3f(rX, rY, rZ));
			} else {
				//					roaming = new BoundingBox(((SegmentController)getCreature().getAffinity()).getBoundingBox());
				roaming = new BoundingBox(new Vector3f(-20, -10, -20), new Vector3f(20, 10, 20));
			}

			if (creature.getGravity().isGravityOn()) {
				Vector3i dir = new Vector3i(Universe.getRandom().nextInt(3) - 1, 0, Universe.getRandom().nextInt(3) - 1);
//									System.err.println(this+" QUEUING RANDOM PATH "+from+" -> "+dir);
				((GameServerState) creature.getState()).getController().queueSegmentRandomGroundPath(from, origin, roaming, dir, this);
			} else {
				Vector3i dir = new Vector3i(Universe.getRandom().nextInt(3) - 1, Universe.getRandom().nextInt(3) - 1, Universe.getRandom().nextInt(3) - 1);
//									System.err.println(this+" QUEUING RANDOM PATH "+from+" -> "+dir);
				((GameServerState) creature.getState()).getController().queueSegmentRandomPath(from, origin, roaming, dir, this);
			}
			}else{
				System.err.println("[AIPlayer] cannot plot random path for " + creature + ": " + creature.getAffinity()+"; from segment piece is null");
			}
		} else {
			System.err.println("[AIPlayer] cannot plot random path for " + creature + ": " + creature.getAffinity());
		}
	}

	public void plotPath(SegmentPiece to, boolean forceAffinity) {
		if (forceAffinity && to.getSegment().getSegmentController() != creature.getAffinity()) {
			creature.setAffinity(to.getSegment().getSegmentController());
		}

		if (creature.getAffinity() != null && creature.getAffinity() == to.getSegment().getSegmentController()) {
			Vector3i posInAffinity = creature.getPosInAffinity(new Vector3i());
			SegmentPiece from = to.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(posInAffinity);//autorequest true previously
			if(from  != null){
				System.err.println(this + " QUEUING PATH " + from + " -> " + to);
				if (creature.getGravity().isGravityOn()) {
					((GameServerState) creature.getState()).getController().queueSegmentGroundPath(from, to, this);
				} else {
					((GameServerState) creature.getState()).getController().queueSegmentPath(from, to, this);
				}
			}else{
				System.err.println("[AIPlayer] FROM SEGMENT PIECE NULL");
			}
		} else {
			System.err.println("[AIPlayer] cannot plot path for " + creature + ": " + creature.getAffinity() + "; to " + to + " of " + to.getSegment().getSegmentController());
		}
	}

	public void plotPath(Vector3i to) {

		Vector3i from = creature.getPosInAffinity(new Vector3i());
		System.err.println(this + " QUEUING ABSOLUTE (UNSAVE) PATH " + from + " -> " + to + "; Affinity: " + creature.getAffinity());
		if (creature.getGravity().isGravityOn()) {
			((GameServerState) creature.getState()).getController().queueSegmentGroundPath(from, to, (SegmentController) creature.getAffinity(), this);
		} else {
			((GameServerState) creature.getState()).getController().queueSegmentPath(from, to, (SegmentController) creature.getAffinity(), this);
		}
	}

	public void updateFromNetworkObject() {
		super.handleInventoryNT();
		if (!creature.isOnServer()) {
			health = getNetworkObject().health.getFloat();

			sittingOnId = (int) getNetworkObject().sittingState.getLongArray()[0];
			ElementCollection.getPosFromIndex(getNetworkObject().sittingState.getLongArray()[1], sittingPos);
			ElementCollection.getPosFromIndex(getNetworkObject().sittingState.getLongArray()[2], sittingPosTo);
			ElementCollection.getPosFromIndex(getNetworkObject().sittingState.getLongArray()[3], sittingPosLegs);
//			System.err.println("UPDATE ON CLIENT: "+getNetworkObject().hasTarget.get()+"; "+getNetworkObject().target.getVector());
			if (getNetworkObject().hasTarget.get()) {
				if (target == null) {

					target = new Transform();
					target.setIdentity();
				}
				target.origin.set(getNetworkObject().target.getVector());
//				System.err.println("TARGET GOTTEN: "+target.origin);
			} else {
				target = null;
			}
		}
		
		for(long s : getNetworkObject().lagAnnouncement.getReceiveBuffer()){
			currentLag = s;
			lastLagReceived = System.currentTimeMillis();
		}
	}

	public void updateToNetworkObject() {
		if (isOnServer()) {
			getNetworkObject().health.set(health);
			boolean changedSit = getNetworkObject().sittingState.getLongArray()[0] != sittingOnId;
			getNetworkObject().sittingState.set(0, sittingOnId);
			getNetworkObject().sittingState.set(1, ElementCollection.getIndex(sittingPos));
			getNetworkObject().sittingState.set(2, ElementCollection.getIndex(sittingPosTo));
			getNetworkObject().sittingState.set(3, ElementCollection.getIndex(sittingPosLegs));
			
			getNetworkObject().climbingState.set(0, climbingId);
			getNetworkObject().climbingState.set(1, climbingPos);
			getNetworkObject().climbingState.set(2, climbingDir);
		}
	}

	/**
	 * @return the worldTransform
	 */
	public Transform getWorldTransform() {
		return creature.getWorldTransform();
	}

	/**
	 * @return the creature
	 */
	public AICreature<? extends AIPlayer> getCreature() {
		return creature;
	}

	private void handleAIMessages() {
		if (!aiMessages.isEmpty()) {
			synchronized (aiMessages) {
				MachineProgram<? extends AiEntityState> currentProgram = creature.getAiConfiguration().getAiEntityState().getCurrentProgram();
				if (currentProgram != null && currentProgram instanceof AICreatureProgramInterface) {
					AICreatureProgramInterface p = (AICreatureProgramInterface) currentProgram;
					while (!aiMessages.isEmpty()) {
						aiMessages.dequeue().handle(p);
					}
				}
			}
		}
	}

	public void sendAIMessage(AICreatureMessage m) {
		synchronized (aiMessages) {
			aiMessages.enqueue(m);
		}
	}

	private void checkProximityObjects() {
		if (System.currentTimeMillis() - lastCheck > PROXIMITY_CHECKPERIOD) {
			proximityEntities.clear();
			synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (final Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (s instanceof SimpleTransformableSendableObject && isEnemy(s) && creature.isInShootingRange((SimpleTransformableSendableObject) s)) {

						proximityEntities.add((SimpleTransformableSendableObject) s);

					}
				}
			}
			if (proximityEntities.size() > 0) {
				final SimpleTransformableSendableObject s = proximityEntities.get(Universe.getRandom().nextInt(proximityEntities.size()));
				sendAIMessage(new AICreatureMessage() {

					@Override
					public void handle(AICreatureProgramInterface p) {
						try {
							p.enemyProximity(s);
						} catch (FSMException e) {
							e.printStackTrace();
						}
					}

					@Override
					public MessageType getType() {
						return MessageType.PROXIMITY;
					}
				});
			}
			lastCheck = System.currentTimeMillis();
		}
	}

	protected boolean isEnemy(Sendable s) {
		if (s == this || s == creature) {
			return false;
		}

		if (s instanceof SimpleTransformableSendableObject) {
			if (s instanceof SegmentController && !creature.getAiConfiguration().isAttackStructures()) {
				return false;
			}
			return ((FactionState) getState()).getFactionManager().isEnemy(getFactionId(), ((SimpleTransformableSendableObject) s).getFactionId());
		}

		return false;
	}

	protected abstract void updateServer();

	protected abstract void updateClient();

	/**
	 * @return the target
	 */
	public Transform getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(Transform target) {
//		if(target != null){
//			System.err.println("SETTING TARGET "+getState()+": "+target.origin);
//		}else{
//			if(this.target != null){
//				try{
//				throw new NullPointerException();
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//				System.err.println("SETTING TARGET "+getState()+": "+null);
//			}
//		}
		this.target = target;
		lastTargetSet = System.currentTimeMillis();
	}

	public boolean hasNaturalWeapon() {
		//overwritten
		return false;
	}

	public void fireNaturalWeapon(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, Vector3f dir) {
		//overwritten
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.controller.SegmentPathCallback#pathFinished(boolean, java.util.List)
	 */
	@Override
	public void pathFinished(boolean success, List<Long> resultPathReference) {
		//		System.err.println("[AIPlayer] received finished path callback: suc: "+success+"; "+(success ? "(path: "+resultPathReference.size()+")" : ""));

		if (success) {
			this.currentPath = resultPathReference;
		} else {
			this.currentPath = null;
			sendAIMessage(new AICreatureMessage() {

				@Override
				public void handle(AICreatureProgramInterface p) {
					try {
						p.onNoPath();
					} catch (FSMException e) {
						e.printStackTrace();
					}
				}

				@Override
				public MessageType getType() {
					return MessageType.NO_PATH;
				}
			});
		}
	}

	@Override
	public Vector3i getObjectSize() {
		return creature.getBlockDim();
	}

	/**
	 * @return the currentPath
	 */
	public List<Long> getCurrentPath() {
		return currentPath;
	}

	/**
	 * @param currentPath the currentPath to set
	 */
	public void setCurrentPath(List<Long> currentPath) {
		this.currentPath = currentPath;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.faction.FactionInterface#getFactionId()
	 */
	@Override
	public int getFactionId() {
		return creature.getFactionId();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.faction.FactionInterface#getUniqueIdentifier()
	 */
	@Override
	public String getUniqueIdentifier() {
		return "AIPlayerOf" + creature.getUniqueIdentifier();
	}

	private void setFactionId(int factionId) {
		creature.setFactionId(factionId);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.resource.UniqueInterface#isVolatile()
	 */
	@Override
	public boolean isVolatile() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.resource.tag.TagSerializable#fromTagStructure(org.schema.schine.resource.tag.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		setFactionId((Integer) t[0].getValue());
		getInventory().fromTagStructure(t[1]);
		health = (Float) t[2].getValue();
		if (t[3].getType() != Type.FINISH) {
			String c = (String) t[3].getValue();
			if (!c.equals("none")) {
				conversationScript = c;
			}
		}

		if (t.length > 4 && t[4].getType() != Type.FINISH) {
			fromSittingTag(t[4]);

		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.resource.tag.TagSerializable#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		Tag factionTag = new Tag(Type.INT, null, getFactionId());
		Tag healthTag = new Tag(Type.FLOAT, null, health);
		Tag inventoryTag = getInventory().toTagStructure();
		Tag scriptTag = new Tag(Type.STRING, null, getConversationScript() == null ? "none" : getConversationScript());
		Tag sitting = toSittingTag();
		return new Tag(Type.STRUCT, null,
				new Tag[]{factionTag, inventoryTag, healthTag, scriptTag, sitting, FinishTag.INST});
	}

	/**
	 * @return the proximityEntities
	 */
	public ObjectArrayList<SimpleTransformableSendableObject> getProximityEntities() {
		return proximityEntities;
	}

	public void setConversationState(PlayerState entity, String converationState) {
		System.err.println("[SERVER] CONVERSATION STATE SET FOR " + entity + " -> " + converationState);
		conversationStates.put(entity, converationState);
	}

	public String getConversationState(PlayerState entity) {
		if (!conversationStates.containsKey(entity)) {
			return "none";
		} else {
			return conversationStates.get(entity);
		}
	}
	public long getCurrentLag() {
		return currentLag;
	}
}
