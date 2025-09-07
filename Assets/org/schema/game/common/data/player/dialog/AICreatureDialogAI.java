package org.schema.game.common.data.player.dialog;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.ai.UnloadedAiContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.weapon.Weapon.WeaponSubType;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.FactionState;
import org.schema.schine.ai.stateMachines.AIGameEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;

import javax.script.Bindings;
import javax.vecmath.Vector3f;

public class AICreatureDialogAI extends AIGameEntityState<PlayerState> {

	private final AbstractOwnerState converationPartner;
	private ObjectArrayList<DelayedFollowUpHook> delayedFollowUpHooks = new ObjectArrayList<DelayedFollowUpHook>();

	public AICreatureDialogAI(String name, PlayerState playerState, AbstractOwnerState converationPartner) {
		super(name, playerState);
		this.converationPartner = converationPartner;
	}

	@Override
	public void updateAIClient(Timer timer) {

	}

	@Override
	public void updateAIServer(Timer timer) throws FSMException {
//		System.err.println("DELAYED HOOKS: "+delayedFollowUpHooks);
		if (delayedFollowUpHooks.size() > 0) {

			DelayedFollowUpHook delayedFollowUpHook = delayedFollowUpHooks.get(0);
			if (delayedFollowUpHook.isSatisfied(this)) {
				delayedFollowUpHook.execute(this);
				delayedFollowUpHooks.remove(0);
			}
		}
	}

	public String getConverationPartnerAffinity() {
		if (converationPartner instanceof AIPlayer && ((AIPlayer) converationPartner).getCreature().getAffinity() != null) {
			return ((AIPlayer) converationPartner).getCreature().getAffinity().toNiceString();
		}
		return "none";

	}

	public String getConverationPartnerFactionName() {
		FactionState s = (FactionState) converationPartner.getState();

		if (converationPartner.getFactionId() == 0) {
			return "neutral";
		}
		Faction faction = s.getFactionManager().getFaction(converationPartner.getFactionId());
		if (faction != null) {
			return faction.getName();
		} else {
			return "unknown";
		}
	}

	public String getConverationPartnerOwnerName() {
		if (converationPartner instanceof AIPlayer) {
			return ((AIPlayer) converationPartner).getCreature().getAiConfiguration().get(Types.OWNER).getCurrentState().toString().replaceAll("ENTITY_PLAYERSTATE_", "");
		}
		return "none";
	}

	public String getOwnName() {
		return converationPartner.getName();
	}

	public String getConverationParterName() {
		return converationPartner.getName();
	}

	public String getScriptName() {
		return converationPartner.getConversationScript();
	}

	public int setConversationState(String converationState) {
		System.err.println("[SERVER][CONVERSATION] " + getEntity() + " -> " + converationPartner + " NEW CONVERSATION STATE SET: " + converationState);
		if (converationPartner instanceof AIPlayer) {
			((AIPlayer) converationPartner).setConversationState(getEntity(), converationState);
			return 0;
		}
		return -1;
	}

	public String getConversationState() {
		if (converationPartner instanceof AIPlayer) {
			return ((AIPlayer) converationPartner).getConversationState(getEntity());
		} else {
			return "none";
		}
	}

	/**
	 * @param type
	 * @param count
	 * @return 0 if success, -1 if invetory full
	 */
	public int giveType(short type, int count) {

		System.err.println("[DIALOG] " + getConverationParterName() + " give " + getEntity() + ": " + ElementKeyMap.toString(type) + "; count: " + count);
		if (getEntity().getInventory().canPutIn(type, count)) {

			int slot = getEntity().getInventory().incExistingOrNextFreeSlot(type, count);

			IntArrayList l = new IntArrayList();
			l.add(slot);
			getEntity().getInventory().sendInventoryModification(l);
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * @param grav
	 * @return 0 if success, 1 if already set, -1 if failed
	 */
	public int giveGravity(boolean grav) {

		if (grav) {
			if (getEntity().getAssingedPlayerCharacter().getGravity().magnitude() == 0) {
				if (converationPartner instanceof AIPlayer) {
					AICreature<? extends AIPlayer> creature = ((AIPlayer) converationPartner).getCreature();
					if (creature.getAffinity() != null && creature.getAffinity() instanceof SegmentController) {
						SegmentController s = ((SegmentController) creature.getAffinity());
						getEntity().getAssingedPlayerCharacter().scheduleGravityServerForced(new Vector3f(0, -9.81f, 0), s);
						return 0;
					}
				}
			} else {
				return 1;
			}
		} else {
			if (getEntity().getAssingedPlayerCharacter().getGravity().magnitude() > 0) {
				if (converationPartner instanceof AIPlayer) {
					AICreature<? extends AIPlayer> creature = ((AIPlayer) converationPartner).getCreature();
					if (creature.getAffinity() != null && creature.getAffinity() instanceof SegmentController) {
						SegmentController s = ((SegmentController) creature.getAffinity());
						getEntity().getAssingedPlayerCharacter().scheduleGravityServerForced(new Vector3f(0, 0, 0), s);
						return 0;
					}
				}
			} else {
				return 1;
			}
		}
		return -1;
	}

	public int destroyShip(String startsWithUID) {
		for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) s).getUniqueIdentifier() != null
					&& ((SimpleTransformableSendableObject) s).getUniqueIdentifier().startsWith(startsWithUID)) {
				SimpleTransformableSendableObject ss = (SimpleTransformableSendableObject) s;
				if (ss.getSectorId() == getEntity().getCurrentSectorId()) {
					ss.markForPermanentDelete(true);
					ss.setMarkedForDeleteVolatile(true);
					return 1;
				}
			}
		}
//		assert(false):"ship to destroy not found::: "+startsWithUID;
		return -1;
	}

	public boolean isAtBlock(int x, int y, int z) {
//		System.err.println("CHECKING AT BLOCK: "+x+", "+y+", "+z);
		if (converationPartner instanceof AIPlayer) {
			AICreature<? extends AIPlayer> creature = ((AIPlayer) converationPartner).getCreature();
			if (creature.getAffinity() != null && creature.getAffinity() instanceof SegmentController) {
				SegmentController sc = (SegmentController) creature.getAffinity();
				Vector3i pos = SimpleTransformableSendableObject.getBlockPositionRelativeTo(creature.getWorldTransform().origin, sc, new Vector3i());

//				System.err.println("AFFINITY: "+sc+" CONDITION: "+x+", "+y+", "+z+"; current pos "+pos+"; condition: "+pos.equals(x, y, z));

				return pos.equals(x, y, z);
			}
		}
		assert (false) : ("CONDITION TRIGGERED AUTOMATICALLY");
		return true;
	}

	public int callTutorial(String tutorial) {
		getEntity().callTutorialServer(tutorial);
		return 0;
	}

	public int moveTo(int x, int y, int z) {
		if (converationPartner instanceof AIPlayer) {
			AICreature<? extends AIPlayer> creature = ((AIPlayer) converationPartner).getCreature();
			if (creature.getAffinity() != null && creature.getAffinity() instanceof SegmentController) {
				((AIPlayer) converationPartner).plotPath(new Vector3i(x, y, z));
			}
		}
		return -1;
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveMetaItem(MetaObjectType type, short subType, int cost) {
		try {

			if (getEntity().getCredits() >= cost) {
				MetaObject obj = MetaObjectManager
						.instantiate(type, subType, true);

				int slot = getEntity().getInventory().getFreeSlot();
				getEntity().getInventory(null).put(slot, obj);

				getEntity().sendInventoryModification(slot, Long.MIN_VALUE);
				getEntity().setCredits(getEntity().getCredits() - cost);
				return 0;
			}
			return -1;

		} catch (NoSlotFreeException e) {
			e.printStackTrace();
			return -2;
		}
	}
	/**
	 * These functions need to be compatible with lua. So (Object ... obj) isnt used
	 */
	public Object[] format(Object obj) {
		return new Object[]{obj};
	}
	/**
	 * These functions need to be compatible with lua. So (Object ... obj) isnt used
	 */
	public Object[] format(Object obj0, Object obj1) {
		return new Object[]{obj0, obj1};
	}
	/**
	 * These functions need to be compatible with lua. So (Object ... obj) isnt used
	 */
	public Object[] format(Object obj0, Object obj1, Object obj2) {
		return new Object[]{obj0, obj1, obj2};
	}
	/**
	 * These functions need to be compatible with lua. So (Object ... obj) isnt used
	 */
	public Object[] format(Object obj0, Object obj1, Object obj2, Object obj3) {
		return new Object[]{obj0, obj1, obj2, obj3};
	}
	/**
	 * These functions need to be compatible with lua. So (Object ... obj) isnt used
	 */
	public Object[] format(Object obj0, Object obj1, Object obj2, Object obj3, Object obj4) {
		return new Object[]{obj0, obj1, obj2, obj3, obj4};
	}
	/**
	 * These functions need to be compatible with lua. So (Object ... obj) isnt used
	 */
	public Object[] format(Object obj0, Object obj1, Object obj2, Object obj3, Object obj4, Object obj5) {
		return new Object[]{obj0, obj1, obj2, obj3, obj4, obj5};
	}
	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveLaserWeapon(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.LASER.type, cost);
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveGrappleBeam(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.GRAPPLE.type, cost);
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveTorchBeam(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.TORCH.type, cost);
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveBuildProhibiter(int cost) {
		return giveMetaItem(MetaObjectType.BUILD_PROHIBITER, (short) -1, cost);
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveFlashLight(int cost) {
		return giveMetaItem(MetaObjectType.FLASH_LIGHT, (short) -1, cost);
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveSniperRifle(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.SNIPER_RIFLE.type, cost);
	}

	public int giveMarkerBeam(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.MARKER.type, cost);
	}
	public int giveTransporterMarkerBeam(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.TRANSPORTER_MARKER.type, cost);
	}

	public int givePowerSupplyBeam(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.POWER_SUPPLY.type, cost);
	}

	public int giveHealingBeam(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.HEAL.type, cost);
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveRocketLauncher(int cost) {
		return giveMetaItem(MetaObjectType.WEAPON, WeaponSubType.ROCKET_LAUNCHER.type, cost);
	}

	/**
	 * @param cost
	 * @return 0 if ok, -1 if not enough money, -2 if no slot free in inv
	 */
	public int giveHelmet(int cost) {
		return giveMetaItem(MetaObjectType.HELMET, (short) -1, cost);
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return 0 if ok, -1 if failed, 1 if the block is already in that activation state
	 */
	public int activateBlock(int x, int y, int z, boolean activate) {

		if (converationPartner instanceof AIPlayer) {
			AICreature<? extends AIPlayer> creature = ((AIPlayer) converationPartner).getCreature();
			if (creature.getAffinity() != null && creature.getAffinity() instanceof SegmentController) {
				SegmentController s = ((SegmentController) creature.getAffinity());

				SegmentPiece p = s.getSegmentBuffer().getPointUnsave(new Vector3i(x, y, z));//autorequest true previously

				if (p != null && p.getType() != Element.TYPE_NONE && ElementKeyMap.getInfo(p.getType()).canActivate()) {
					if (p.isActive() != activate) {
						long index = ElementCollection.getEncodeActivation(p, true, activate, false);
						((SendableSegmentController) p.getSegment().getSegmentController()).getBlockActivationBuffer().enqueue(index);
						return 0;
					} else {
						return 1;
					}
				}
			}
		}
		assert (false);
		return -1;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return 0 if ok, -1 if failed
	 */
	public int activateBlockSwitch(int x, int y, int z) {
		if (converationPartner instanceof AIPlayer) {
			AICreature<? extends AIPlayer> creature = ((AIPlayer) converationPartner).getCreature();
			if (creature.getAffinity() != null && creature.getAffinity() instanceof SegmentController) {
				SegmentController s = ((SegmentController) creature.getAffinity());

				SegmentPiece p = s.getSegmentBuffer().getPointUnsave(new Vector3i(x, y, z));//autorequest true previously

				if (p != null && p.getType() != Element.TYPE_NONE && ElementKeyMap.getInfo(p.getType()).canActivate()) {
					long index = ElementCollection.getEncodeActivation(p, true, !p.isActive(), false);
					((SendableSegmentController) s).getBlockActivationBuffer().enqueue(index);
//					p.getSegment().getSegmentController().getNetworkObject().blockActivationBuffer.add(index);

					return 0;
				}
			}
		}

		return -1;
	}

	public int spawnCrew(int cost) {
		if (getEntity().getPlayerAiManager().getCrew().size() >= 5) {
			return -2;
		}
		if (getEntity().getCredits() >= cost) {
			getEntity().spawnCrew();
			getEntity().setCredits(getEntity().getCredits() - cost);
			return 0;
		} else {
			return -1;
		}

	}

	public boolean isConverationPartnerInTeam() {
		return getEntity().getPlayerAiManager().contains(((AIPlayer) converationPartner).getCreature());
	}

	public int unhireConverationPartner() {
		if (!isConverationPartnerInTeam()) {
			return -1;
		}

		getEntity().getPlayerAiManager().removeAI(new UnloadedAiContainer(((AIPlayer) converationPartner).getCreature()));
		return 0;

		//		if(getEntity().getCredits() >= cost ){
		//			getEntity().setCredits(getEntity().getCredits()-cost);
		//			return 0;
		//		}else{
		//			return -1;
		//		}

	}

	public int hireConverationPartner() {
		if (getEntity().getPlayerAiManager().getCrew().size() >= 5 || isConverationPartnerInTeam()) {
			return -2;
		}
		if (converationPartner.getFactionId() != getEntity().getFactionId()) {
			return -3;
		}
		int cost = 0;
		getEntity().getPlayerAiManager().addAI(new UnloadedAiContainer(((AIPlayer) converationPartner).getCreature()));
		return 0;

		//		if(getEntity().getCredits() >= cost ){
		//			getEntity().setCredits(getEntity().getCredits()-cost);
		//			return 0;
		//		}else{
		//			return -1;
		//		}

	}

	/**
	 * @return the converationPartner
	 */
	public AbstractOwnerState getConverationPartner() {
		return converationPartner;
	}

	public void addDelayedConditionFollowUpHook(
			DialogTextEntryHook dialogTextEntryHook,
			DialogTextEntryHookLua condition, DialogTextEntryHookLua followUp, Bindings bindings) {
		DelayedFollowUpHook h = new DelayedFollowUpHook(dialogTextEntryHook, condition, followUp, bindings);
		this.delayedFollowUpHooks.add(h);
	}

}
