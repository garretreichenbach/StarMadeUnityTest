package org.schema.game.common.data.creature;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.LogUtil;
import org.schema.common.ParseException;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.physics.CubesCompoundShape;
import org.schema.game.common.data.physics.PairCachingGhostObjectExt;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.AIControllable;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ForcedAnimation;
import org.schema.game.common.data.player.InteractionInterface;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.TargetableAICreatureNetworkObject;
import org.schema.game.server.ai.CreatureAIEntity;
import org.schema.game.server.ai.program.creature.character.AICreatureProgramInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class AICreature<E extends AIPlayer> extends AbstractCharacter<E> implements AIControllable<AICreature<E>>, AiInterface, InteractionInterface {

	public static final float headUpScale = 0.485f;
	public static final float shoulderUpScale = 0.385f;
	private final static Int2ObjectOpenHashMap<Constructor<? extends AICreature<? extends AIPlayer>>> id2ClassMap = new Int2ObjectOpenHashMap<Constructor<? extends AICreature<? extends AIPlayer>>>();
	private final static Object2IntOpenHashMap<Class<? extends AICreature<? extends AIPlayer>>> class2IdMap = new Object2IntOpenHashMap<Class<? extends AICreature<? extends AIPlayer>>>();
	private static int AFFINITY_CHECK_PERIOD = 5000;

	static {
		try {
			int i = 0;
			class2IdMap.put(AICharacter.class, i++);
			class2IdMap.put(AIRandomCompositeCreature.class, i++);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		for (Entry<Class<? extends AICreature<? extends AIPlayer>>, Integer> a : class2IdMap.entrySet()) {
			try {
				id2ClassMap.put(a.getValue(), a.getKey().getConstructor(StateInterface.class));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	@Override
	public boolean isAIControlled() {
		return true;
	}
	@Override
	public long getOwnerId() {
		assert(false):"should be implemented";
		return Long.MIN_VALUE;
	}
	private final ArrayList<E> attachedPlayers = new ArrayList<E>();
	private final Vector3f lookDir = new Vector3f();
	public ForcedAnimation forcedAnimation;
	protected long lastAttack;
	private String realName = "";
	private SinusTimerUtil sinus = new SinusTimerUtil();
	private SimpleTransformableSendableObject affinity;
	private long lastAffinityCheck;
	private String nameTag = "NPC";
	private boolean initialGravity;

	public AICreature(StateInterface state) {
		super(state);
		attachedPlayers.add(instantiateOwnerState());

	}

	public static Tag toTagNPC(AICreature<? extends AIPlayer> creature) {
		Tag[] tags = new Tag[3];
		assert (creature != null);
		assert (class2IdMap.containsKey(creature.getClass())) : "NO KEY FOR " + creature.getClass().getSimpleName() + " -> " + class2IdMap;
		tags[0] = new Tag(Type.SHORT, null, class2IdMap.get(creature.getClass()).shortValue());
		tags[1] = creature.toTagStructure();
		tags[2] = FinishTag.INST;

		return new Tag(Type.STRUCT, null, tags);
	}

	public static AICreature<? extends AIPlayer> getNPCFromTag(Tag t,
	                                                           StateInterface state) throws CannotInstantiateAICreatureException {
		Tag[] tags = (Tag[]) t.getValue();

		int type = (Short) tags[0].getValue();
		assert (id2ClassMap.containsKey(type));
		Constructor<? extends AICreature<? extends AIPlayer>> constructor = id2ClassMap.get(type);
		try {
			AICreature<? extends AIPlayer> newInstance = constructor.newInstance(state);
			newInstance.initialize();
			//			System.err.println("INITIALIZING CREATURE: "+type);
			newInstance.fromTagStructure(tags[1]);

			return newInstance;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		throw new CannotInstantiateAICreatureException();
	}

	protected abstract E instantiateOwnerState();

	public abstract boolean isMeleeAttacker();

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.NPC;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#markedForPermanentDelete(boolean)
	 */
	@Override
	public void markForPermanentDelete(boolean mark) {
		if (mark) {
			setAffinity(null);
		}
		super.markForPermanentDelete(mark);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateToNetworkObject()
	 */
	@Override
	public void updateToNetworkObject() {

		super.updateToNetworkObject();
		getAiConfiguration().updateToNetworkObject(getNetworkObject());
		getOwnerState().updateToNetworkObject();

		if (isOnServer()) {
			getNetworkObject().affinity.set(affinity != null ? affinity.getUniqueIdentifier() : "none");
			if (!realName.equals(getNetworkObject().realName.get())) {
				getNetworkObject().realName.set(realName);
			}
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] subTags = (Tag[]) tag.getValue();
		setUniqueIdentifier((String) subTags[0].getValue());
		stepHeight = (Float) subTags[2].getValue();
		setRealName((String) subTags[3].getValue());
		getOwnerState().fromTagStructure(subTags[4]);
		super.fromTagStructure(subTags[5]);
		if (subTags[6].getType() != Type.FINISH) {
			if ((Byte) subTags[6].getValue() != 0) {
				initialGravity = true;
			}
		}
	}

	//	public CreatureAIEntity<E, ? extends AICreature<E>> getAIEntity(){
	//		return aiEntity;
	//	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		Tag idTag = new Tag(Type.STRING, null, this.getUniqueIdentifier());
		Tag speedTag = new Tag(Type.FLOAT, null, getSpeed());
		Tag stepHeightTag = new Tag(Type.FLOAT, null, stepHeight);
		Tag realName = new Tag(Type.STRING, null, this.realName);

		//!!! AI is saved in SimpleTransformable

		return new Tag(Type.STRUCT, null,
				new Tag[]{idTag, speedTag, stepHeightTag, realName, getOwnerState().toTagStructure(), super.toTagStructure(), new Tag(Type.BYTE, null, getGravity().source != null ? (byte) 1 : (byte) 0), FinishTag.INST});
	}

	@Override
	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyLifeform);
			case FRIEND -> out.set(ColorPalette.allyLifeform);
			case NEUTRAL -> out.set(ColorPalette.neutralLifeform);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionLifeform);
		}
		out.x += select;
		out.y += select;
		out.z += select;
	}
	@Override
	public String toNiceString() {
		if (getNetworkObject().getDebugState().get().length() > 0) {
			String s = nameTag + "(" + getId() + ")" + "\n" + getNetworkObject().getDebugState().get() + "\n[CLIENT " +
					(getAiConfiguration().isActiveAI() ? "ACTIVE" : "INACTIVE") + " " + getAiConfiguration().get(Types.ORDER) + "]\nProx: " + getAiConfiguration().isAttackOnProximity() + "; AttOnAtt: " + getAiConfiguration().isAttackOnAttacked() + "; AttStr: " + getAiConfiguration().isAttackStructures() + "; stpAtt: " + getAiConfiguration().isStopAttacking();

			if (isOnServer() || getAiConfiguration().getAiEntityState().getCurrentProgram() != null) {
				s += "\n[INMEM]" + getAiConfiguration().getAiEntityState().getCurrentProgram();
			}
			return s;
		} else {
			//			RemoteSector sectorThisObject = (RemoteSector) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(this.getSectorId());
			//			if(!isOnServer()){
			//				return nameTag;
			//			}else{
			//				return nameTag;
			//			}
			return nameTag + "[" + (int) Math.ceil(getOwnerState().getHealth()) + "hp] ";
		}
	}

	/**
	 * @return the realName
	 */
	@Override
	public String getRealName() {
		return realName;
	}

	@Override
	public float getIndicatorMaxDistance(RType relation) {

		if (((GameClientState) getState()).getPlayer() != null && ((GameClientState) getState()).getPlayer().isInTutorial()) {
			return 30;
		}

		if (relation == RType.ENEMY) {
			return 30;
		} else if (relation == RType.NEUTRAL) {
			return 150;
		} else {
			return super.getIndicatorMaxDistance(relation);
		}

	}

	/**
	 * @param realName the realName to set
	 */
	public void setRealName(String realName) {
		this.realName = realName;
		this.nameTag = new String(realName);
	}

	public abstract void initialFillInventory();

	public void enableGravityOnAI(SegmentController c, Vector3f acc) {

		if (c == null && getGravity().isGravityOn() || getGravity().isAligedOnly()) {
			System.err.println("[SERVER][AI][ACTIVATE] "+this+" "+getName()+" Exit gravity of " + getGravity().source);
			scheduleGravity(new Vector3f(0, 0, 0), null);
		} else {
			if (c != null) {
				if (!getGravity().isGravityOn() && !getGravity().isAligedOnly()) {
					assert (c != null);
					scheduleGravity(new Vector3f(acc), c);
					System.err.println("[SERVER][AI][ACTIVATE] "+this+" "+getName()+" Enter gravity of " + c.getId());
				} else {
					if (getGravity().source == c && getGravity().accelerationEquals(acc)) {
						scheduleGravity(new Vector3f(0, 0, 0), null);
						System.err.println("[SERVER][AI][ACTIVATE] "+this+" "+getName()+"  Exit gravity of " + c.getId());
					} else {
						scheduleGravity(new Vector3f(acc), c);
						System.err.println("[SERVER][AI][ACTIVATE] "+this+" "+getName()+" Change to gravity of " + c.getId());
					}
				}
			} else {
				System.err.println("[SERVER][AI][ACTIVATE] Exit gravity that wasn't on. ignoring...");
			}
		}
	}

	public Vector3i getPosInAffinity(Vector3i out) {
		if (affinity != null) {
			Transform other = new Transform(affinity.getWorldTransform());
			other.inverse();
			Transform self = new Transform(getWorldTransform());
			self.origin.y -= (getCharacterHeight() * 0.5f);
			other.mul(self);
			int x = FastMath.round(other.origin.x + SegmentData.SEG_HALF);
			int y = FastMath.round(other.origin.y + SegmentData.SEG_HALF);
			int z = FastMath.round(other.origin.z + SegmentData.SEG_HALF);

			out.set(x, y, z);
		}
		return out;
	}

	public abstract CreatureAIEntity<E, ? extends AICreature<E>> instantiateAIEntity();

	protected void findAffineObject() {
		if (isOnServer()) {
			if (affinity == null || System.currentTimeMillis() - lastAffinityCheck > AFFINITY_CHECK_PERIOD) {
				PairCachingGhostObjectExt gs = ((PairCachingGhostObjectExt) getPhysicsDataContainer().getObject());
				for (CollisionObject p : gs.getOverlappingPairs()) {
					if (p instanceof RigidBodySegmentController && p.getCollisionShape() instanceof CubesCompoundShape) {
						CubesCompoundShape s = (CubesCompoundShape) p.getCollisionShape();

						Vector3i posInAffinity = getPosInAffinity(new Vector3i());
						if (getAiConfiguration().get(Types.ORIGIN_X).getCurrentState().equals(Integer.MIN_VALUE) &&
								getAiConfiguration().get(Types.ORIGIN_Y).getCurrentState().equals(Integer.MIN_VALUE) &&
								getAiConfiguration().get(Types.ORIGIN_Z).getCurrentState().equals(Integer.MIN_VALUE)) {

							try {
								getAiConfiguration().get(Types.ORIGIN_X).switchSetting(String.valueOf(posInAffinity.x), false);
								getAiConfiguration().get(Types.ORIGIN_Y).switchSetting(String.valueOf(posInAffinity.y), false);
								getAiConfiguration().get(Types.ORIGIN_Z).switchSetting(String.valueOf(posInAffinity.z), false);
							} catch (StateParameterNotFoundException e) {
								e.printStackTrace();
							}
						}
						setAffinity(s.getSegmentController());
						//				System.err.println("Overlapping with "+affinity);
					}
				}
				lastAffinityCheck = System.currentTimeMillis();
			}
		} else {
			if (affinity == null || System.currentTimeMillis() - lastAffinityCheck > AFFINITY_CHECK_PERIOD || !affinity.getUniqueIdentifier().equals(getNetworkObject().affinity.get())) {
				String affinity = getNetworkObject().affinity.get();
				if (affinity.equals("none")) {
					if (this.affinity != null) {
						setAffinity(null);
					}
				} else {
					synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
						for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
							if (s instanceof SimpleTransformableSendableObject) {
								if (affinity.equals(((SimpleTransformableSendableObject) s).getUniqueIdentifier())) {
									setAffinity(((SimpleTransformableSendableObject) s));
									break;
								}
							}
						}
					}
				}
			}
		}

	}

	public void stopForcedAnimation() throws Exception {
		if (forcedAnimation != null) {
			forcedAnimation.time = 0;
		}
	}

	public void forceAnimation(String animation, String loopModeString, float speed, boolean fullBody) throws Exception {
		for (int i = 0; i < AnimationIndex.animations.length; i++) {
			if (AnimationIndex.animations[i].toString().toLowerCase(Locale.ENGLISH).equals(animation.toLowerCase(Locale.ENGLISH))) {
				LoopMode loopMode = LoopMode.valueOf(loopModeString.toUpperCase(Locale.ENGLISH));

				forcedAnimation = new ForcedAnimation(PartType.BOTTOM, AnimationIndex.animations[i], loopMode, speed, fullBody);
				forcedAnimation.time = System.currentTimeMillis();
				return;
			}
		}
		throw new ParseException("animation not found: " + animation + "; " + Arrays.toString(AnimationIndex.animations));
	}

	@Override
	public boolean isClientOwnObject() {
		return false;
	}

	@Override
	public abstract TargetableAICreatureNetworkObject getNetworkObject();

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractCharacter#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject o) {
		super.initFromNetworkObject(o);
		getOwnerState().initFromNetworkObject();
		getAiConfiguration().updateFromNetworkObject(getNetworkObject());
		if (!isOnServer()) {
			setRealName(getNetworkObject().realName.get());
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject, int)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		getAiConfiguration().updateFromNetworkObject(getNetworkObject());
		getOwnerState().updateFromNetworkObject();

		if (isOnServer() && !realName.equals(getNetworkObject().realName.get())) {
			System.err.println("[SERVER] received name change from client " + senderId + ": " + realName + " -> " + getNetworkObject().realName.get());
			try {
				LogUtil.log().fine("[RENAME] " + ((GameServerState) getState()).getPlayerFromStateId(senderId).getName() + " changed object name: \"" + realName + "\" to \"" + getNetworkObject().realName.get() + "\"");
			} catch (PlayerNotFountException e) {
				e.printStackTrace();
			}
			getNetworkObject().realName.setChanged(true);
		}
		setRealName(getNetworkObject().realName.get());
		//		if(!isOnServer()){
		//			System.err.println("[CLEINT]SDSD received rename "+this+": "+getRealName()+" -> "+getNetworkObject().realName.get());
		//		}
		//		if(!isOnServer() && !getRealName().equals(getNetworkObject().realName.get())){
		//
		//			System.err.println("[CLEINT] received rename "+this+": "+getRealName()+" -> "+getNetworkObject().realName.get());
		//
		//		}

	}

	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		if (affinity == null) {
			findAffineObject();
		}
		if (initialGravity && affinity != null) {

			scheduleGravityWithBlockBelow(new Vector3f(0, -9.81f, 0), affinity);
			initialGravity = false;
		}
		
//		if(getName().equals("NPC-00")){
//			System.err.println("[CGHAR] "+getState()+": "+getName()+" "+getWorldTransform().origin);
//		}
		if (isOnServer()) {
			
			if (forcedAnimation != null && forcedAnimation.time != getNetworkObject().forcedAnimation.get().time) {
				getRemoteTransformable().equalCounter = 0;

				System.err.println("[SERVER] " + this + " sending forced animation: " + forcedAnimation);
				getNetworkObject().forcedAnimation.get().set(forcedAnimation);
				getNetworkObject().forcedAnimation.setChanged(true);
				;
				getNetworkObject().setChanged(true);
			}
		} else {
			if (getNetworkObject().forcedAnimation.get().received) {
				if (getNetworkObject().forcedAnimation.get().time > 0) {
					forcedAnimation = getNetworkObject().forcedAnimation.get();
					forcedAnimation.resetApplied();
				} else {
					forcedAnimation = null;
				}
				getNetworkObject().forcedAnimation.get().received = false;
			}

		}
		//		if(getAffinity() != null){
		//			Vector3i posInAffinity = getPosInAffinity(new Vector3i());
		//			if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
		//				DebugDrawer.boxes.add(new DebugBox(
		//						new Vector3f(posInAffinity.x+8-0.5f, posInAffinity.y+8-0.5f, posInAffinity.z+8-0.5f),
		//						new Vector3f(posInAffinity.x+8+0.5f, posInAffinity.y+8+0.5f, posInAffinity.z+8+0.5f),
		//						getAffinity().getWorldTransform(), 1, 0.3f, 0.3f, 1.0f));
		//			}
		//		}
		getAiConfiguration().update(timer);

		if (getOwnerState().getConversationPartner() != null) {
			PlayerCharacter assingedPlayerCharacter = getOwnerState().getConversationPartner().getAssingedPlayerCharacter();
			Vector3f dir = new Vector3f();
			dir.sub(assingedPlayerCharacter.getWorldTransform().origin, getWorldTransform().origin);
			dir.normalize();

			lookDir.set(dir);
		} else if (getOwnerState().getTarget() == null && getAiConfiguration().get(Types.ORDER).getCurrentState().equals(AIGameCreatureConfiguration.BEHAVIOR_IDLING)) {

//			sinus.setSpeed(4);
//			sinus.update(timer);
			if (Math.random() < 0.0003) {
				lookDir.set(getOwnerState().getForward(new Vector3f()));
				Vector3f r = new Vector3f(getOwnerState().getRight(new Vector3f()));

				r.scale((((float) (Math.random() - 0.5f)) * 2.001f));

				lookDir.add(r);
				lookDir.normalize();
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractCharacter#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		getOwnerState().updateToFullNetworkObject();
		getAiConfiguration().updateToFullNetworkObject(getNetworkObject());
		getNetworkObject().realName.set(realName);
		if (isOnServer()) {
			getNetworkObject().affinity.set(affinity != null ? affinity.getUniqueIdentifier() : "none");
		}
	}

	@Override
	public void damage(float damage, final Damager from) {
		super.damage(damage, from);
		if (canAttack(from)) {
			if (from instanceof SimpleTransformableSendableObject && getOwnerState().isVulnerable()) {
				getOwnerState().sendAIMessage(new AICreatureMessage() {

					@Override
					public void handle(AICreatureProgramInterface p) {
						try {
							p.underFire((SimpleTransformableSendableObject) from);
						} catch (FSMException e) {
							e.printStackTrace();
						}
					}

					@Override
					public org.schema.game.common.data.creature.AICreatureMessage.MessageType getType() {
						return MessageType.UNDER_FIRE;
					}
				});
			}
		}
		if (!getOwnerState().isVulnerable()) {
			if (from != null && from instanceof PlayerCharacter) {
				((PlayerCharacter) from).sendOwnerMessage(Lng.astr("Cannot attack in tutorial!"), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			if (from != null && from instanceof SegmentController) {
				((SegmentController) from).sendControllingPlayersServerMessage(Lng.astr("Cannot attack in tutorial!"), ServerMessage.MESSAGE_TYPE_ERROR);
			}
		}
	}

	@Override
	public Transform getHeadWorldTransform() {
		Transform worldTransform = new Transform(super.getWorldTransform());
		Vector3f up = GlUtil.getUpVector(new Vector3f(), worldTransform);
		up.scale(headUpScale);
		worldTransform.origin.add(up);
		return worldTransform;
	}

	/**
	 * @return the attachedPlayers
	 */
	@Override
	public ArrayList<E> getAttachedPlayers() {
		return attachedPlayers;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractCharacter#isVolatile()
	 */
	@Override
	public boolean isVolatile() {
		//dont use normal save when affinity is null
		return affinity != null;
	}

	@Override
	public String toString() {
		return "PlayerCreature[(" + getUniqueIdentifier() + ")" + "("+getName()+")(" + getId() + ")]";
	}

	@Override
	public E getOwnerState() {
		return attachedPlayers.get(0);
	}

	/**
	 * @return the affinity
	 */
	public SimpleTransformableSendableObject getAffinity() {
		return affinity;
	}

	/**
	 * @param affinity the affinity to set
	 */
	public void setAffinity(SimpleTransformableSendableObject affinity) {
		if (isMarkedForPermanentDelete()) {
			return;
		}
		assert (this.affinity == null || (!(getAiConfiguration().get(Types.ORIGIN_X).getCurrentState().equals(Integer.MIN_VALUE) &&
				getAiConfiguration().get(Types.ORIGIN_Y).getCurrentState().equals(Integer.MIN_VALUE) &&
				getAiConfiguration().get(Types.ORIGIN_Z).getCurrentState().equals(Integer.MIN_VALUE))));

		System.err.println("[CREATURE] " + getState() + " SETTING AFFINITY OF " + this + " FROM " + this.affinity + " to " + affinity + " on " + getState());
		if (this.affinity != null) {

			this.affinity.getAttachedAffinity().remove(this);
		}
		if (affinity != null) {
			affinity.getAttachedAffinity().add(this);

		}
		if (isOnServer() && this.affinity != affinity) {

			if (affinity != null && affinity instanceof SegmentController) {
				System.err.println("[CREATURE] " + this + " align on " + affinity);
				enableGravityOnAI((SegmentController) affinity, new Vector3f(0, 0, 0));
			} else {
				System.err.println("[CREATURE] " + this + " exit align off " + this.affinity);
				enableGravityOnAI(null, new Vector3f(0, 0, 0));
			}
		}

		this.affinity = affinity;

	}

	public abstract CreaturePartNode getCreatureNode();

	public float getScale() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AiInterface#getAiConfiguration()
	 */
	@Override
	public abstract AIGameCreatureConfiguration<?, ?> getAiConfiguration();

	public boolean isInShootingRange(SimpleGameObject target) {
		if (target.getSectorId() != getSectorId()) {
			return false;
		}
		Vector3f dist = new Vector3f(getWorldTransform().origin);
		dist.sub(target.getWorldTransform().origin);

		return dist.length() < getAiConfiguration().getAiEntityState().getShootingRange();
	}

	/**
	 * @return the lookDir
	 */
	public Vector3f getLookDir() {
		return lookDir;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#newNetworkObject()
	 */
	@Override
	public void newNetworkObject() {
		
	}

	@Override
	public void interactClient(AbstractOwnerState from) {
		System.err.println("[Character] " + from + " Interacting with " + getOwnerState());
		getOwnerState().interactClient(from);
	}

	public abstract void setSpeed(float speed);

	public boolean isAttacking() {
		return System.currentTimeMillis() - lastAttack < 500;
	}

}
