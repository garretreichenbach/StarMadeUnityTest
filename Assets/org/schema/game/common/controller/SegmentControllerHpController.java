package org.schema.game.common.controller;

import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.listener.events.systems.SystemsRebootingEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.HpTrigger.HpTriggerType;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.*;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.game.network.objects.remote.RemoteSegmentPiece;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.util.Arrays;

public class SegmentControllerHpController implements SegmentControllerHpControllerInterface {

	private static final byte HP_CONTROLLER_CLASS_ID = 1;
	private final SendableSegmentController segmentController;
	private final boolean[] used = new boolean[HpTriggerType.values().length];
	boolean useHPLong = false;
	
	private ElementCountMap currentHPMatch = new ElementCountMap();
	private int hpInt;
	private int maxHpInt;
	
	
//	private int armorHPInt;
//	private long armorHPLong;
//	private boolean armorHpDirty;
//	private boolean maxArmorHPDirty;
//	private int maxArmorHpInt;
//	private long maxArmorHpLong;
//	boolean useArmorLong = false;
	
	private long hpLong;
	private long maxHpLong;
	
	
	private long rebootStarted;
	private boolean maxHPDirty;
	private boolean hpDirty;
	
	
	private long rebootTime;
	private boolean requestedTimeClient;
	private long lastHp;
	private boolean loadedFromTag;
	private boolean rebootRecover;
	private int lastDamager = -1;
	private float accumul;
	private boolean filledUpFromOld;

	public SegmentControllerHpController(SendableSegmentController segmentController) {
		this.segmentController = segmentController;
	}
//
//	public static int getArmorHpDamage(Int2LongOpenHashMap armorHpMap, int segControllerId, int hullDamage, short type, float effectRatio, DamageDealerType weaponType, float armorHPDeductionBonus, float armorHPAbsorbtionBonus, EffectElementManager<?, ?, ?> effect) {
//
//		long armorHP = armorHpMap.get(segControllerId);
//
//		if (armorHP > 0 && type > 0 && (ElementKeyMap.getInfoFast(type)).armorHP > 0) {
//
//			float ab = VoidElementManager.ARMOR_HP_ABSORBTION + armorHPAbsorbtionBonus;
//			float absorbtion = Math.min(armorHP, Math.min(1f, ab) * hullDamage);
//
//			float armorAbs = absorbtion;
//
//			float n = armorAbs * VoidElementManager.ARMOR_HP_ABSORBED_DAMAGE_MULTIPLIER;
//
//			armorHP -= (int) (n - (n * armorHPDeductionBonus));
//
//			hullDamage -= absorbtion;
//
//			armorHpMap.put(segControllerId, armorHP);
//		}
//
//		return hullDamage;
//	}

	private long getHpInternal() {
		if(isUsingNewHp()){
			return segmentController.getReactorHp();
		}else{
			return useHPLong ? hpLong : hpInt;
		}
	}

	private void setHpInternal(long v) {
//		if(segmentController.isUsingPowerReactors()){
//			//not set here
//		}else{
			boolean bef = useHPLong;
			useHPLong = v >= Integer.MAX_VALUE;
			assert(useHPLong == bef):bef+" -> "+useHPLong;
			if (useHPLong) {
				hpLong = v;
			} else {
				assert (v <= Integer.MAX_VALUE) : v;
				hpInt = (int) v;
			}
//		}
	}

	private long getMaxHpInternalOld() {
		return useHPLong ? maxHpLong : maxHpInt;
	}
	private boolean isUsingNewHp(){
		return !hadOldPowerBlocks() && segmentController.isUsingPowerReactors();
	}
	private long getMaxHpInternal() {
		if(isUsingNewHp()){
			return segmentController.getReactorHpMax();
		}else{
			return getMaxHpInternalOld();
		}
	}

	private void setMaxHpInternal(long v) {
//		if(segmentController.isUsingPowerReactors()){
//			//not set here
//		}else{
			useHPLong = v >= Integer.MAX_VALUE;
	
			if (useHPLong) {
				maxHpLong = v;
			} else {
				assert (v <= Integer.MAX_VALUE) : v;
				maxHpInt = (int) v;
			}
//		}
	}

//	private long getArmorInternal() {
//		return useArmorLong ? armorHPLong : armorHPInt;
//	}
//
//	private void setArmorInternal(long v) {
//		if (useArmorLong) {
//			armorHPLong = v;
//		} else {
//			assert (v <= Integer.MAX_VALUE) : v;
//			armorHPInt = (int) v;
//		}
//	}
//
//	private long getMaxArmorInternal() {
//		return useArmorLong ? maxArmorHpLong : maxArmorHpInt;
//	}
//
//	private void setMaxArmorInternal(long v) {
//		useArmorLong = v >= Integer.MAX_VALUE;
//		if (useArmorLong) {
//			maxArmorHpLong = v;
//		} else {
//			assert (v <= Integer.MAX_VALUE) : v;
//			maxArmorHpInt = (int) v;
//		}
//	}

	private void setLastDamager(Damager from) {
		if (from != null) {
			if (from.getOwnerState() != null) {
				lastDamager = from.getOwnerState().getId();
			} else if (from instanceof SimpleTransformableSendableObject) {
				lastDamager = ((SimpleTransformableSendableObject) from).getId();
			}
		} else {
			lastDamager = -1;
		}
	}
	public void triggerOverheating(){
		assert(isOnServer());
		
		if (!segmentController.isCoreOverheating() && !isRebooting()) {


			System.err.println("[SERVER] Overheating triggered for "+segmentController);
			Sendable sendable = segmentController.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(lastDamager);
			Damager d = null;
			if (sendable != null && sendable instanceof Damager) {
				d = (Damager) sendable;
			}
			//INSERTED CODE @147
			SegmentControllerOverheatEvent event = new SegmentControllerOverheatEvent(this.segmentController, d);
			StarLoader.fireEvent(event, this.isOnServer());
			if(event.isCanceled()){
				return;
			}
			///
			segmentController.startCoreOverheating(d);
		}
	}
	private void handleEffectsServer() {
		if(segmentController.getSegmentBuffer().isFullyLoaded() && segmentController instanceof Ship && segmentController.getTotalElements() == 1) {
			SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(Ship.core);
			if(!segmentController.isCoreOverheating() && pointUnsave != null && pointUnsave.getType() == ElementKeyMap.CORE_ID && pointUnsave.getHitpointsByte() == 0) {
				System.err.println("[SERVER] Core AT 0 HP destroyed for "+segmentController+", which is in new power system, is not docked, and has no active reactor (-> death on core destruction)");
				triggerOverheating();
			}
			return;
		}
		if(segmentController.isNewPowerSystemNoReactor()){
			if(!segmentController.isCoreOverheating() && segmentController.isNewPowerSystemNoReactorOverheatingCondition()){
				System.err.println("[SERVER] Core destroyed for "+segmentController+", which is in new power system, is not docked, and has no active reactor (-> death on core destruction)");
				triggerOverheating();
			}
			return;
		}
		if(segmentController.railController.isDockedAndExecuted() && segmentController.railController.getRoot().hasActiveReactors()){
			return;
		}
		long failSaveDamageTakenForOldPowerMS = 5000; //only check for hp conditions for this time after taking damage
		if(!isUsingNewHp() && segmentController instanceof EditableSendableSegmentController && 
				segmentController.getState().getUpdateTime() > ((EditableSendableSegmentController)segmentController).lastDamageTaken+failSaveDamageTakenForOldPowerMS){
			//don't check hp condition on old power if ship wasnt damage (fail safe)
			return;
		}
		try{
			ObjectArrayList<HpCondition> hpCon = VoidElementManager.HP_CONDITION_TRIGGER_LIST.get(segmentController.isUsingPowerReactors());
			if (hpCon == null || hpCon.size() == 0) {
				if(hpCon == null){
					System.err.println("ERROR: NO HP CONDITIONS: "+segmentController+"; "+segmentController.getTypeString()+"; "+segmentController.isUsingPowerReactors()+": Check: "+VoidElementManager.HP_CONDITION_TRIGGER_LIST.checkString() );
				}
				return;
			}
		}catch(Exception e){
			throw new RuntimeException("Happened on entity: "+segmentController, e);
		}
		
		
		double percent = getHpPercent();

		Arrays.fill(used, false);
		int full = 0;
		ObjectArrayList<HpCondition> conditionList = VoidElementManager.HP_CONDITION_TRIGGER_LIST.get(segmentController.isUsingPowerReactors());
		for (int i = 0; i < conditionList.size(); i++) {
			HpCondition hpCondition = conditionList.get(i);
			if (!segmentController.isHandleHpCondition(hpCondition.trigger.type)) {
				continue;
			}
			if (percent <= hpCondition.hpPercent) {
				//System.err.println("[SERVER][HP][TRIGGER] "+getSegmentController()+"; Event "+hpCondition.trigger.type.name()+" ENTITY HP % "+percent+"; "+getHp()+"/"+getMaxHp()+"; Using Reactor: "+getSegmentController().isUsingPowerReactors());
				if (!used[hpCondition.trigger.type.ordinal()]) {
					//only apply the trigger from the codition with the lowest percent
					used[hpCondition.trigger.type.ordinal()] = true;

//					System.err.println("APPLYING HP NERF::: "+hpCondition.trigger.type);

					switch (hpCondition.trigger.type) {
						case CONTROL_LOSS:
							if(segmentController.railController.isRoot()){
								applyLostControl(true);
							}
							break;
						case OVERHEATING:
							
							triggerOverheating();
							break;
						case POWER:
							applyPowerdown(hpCondition.trigger.amount);
							break;
						case SHIELD:
							applyShielddown(hpCondition.trigger.amount);
							break;
						case THRUST:
							if(segmentController.railController.isRoot()){
								applySlowdown(hpCondition.trigger.amount);
							}
							break;
						default:
							break;
					}

					full++;
					if (full == used.length) {
						break;
					}
				}

			}
		}
		//make sure that all effects are removed if the hp condition is no longer satisfied
		for (int i = 0; i < used.length; i++) {
			if (!used[i]) {
				HpTriggerType hpTriggerType = HpTriggerType.values()[i];
				switch (hpTriggerType) {
					case CONTROL_LOSS:
						applyLostControl(false);
						break;
					case OVERHEATING:
						if (segmentController.isCoreOverheating()) {
							segmentController.stopCoreOverheating();
						}
						break;
					case POWER:
						applyPowerdown(1);
						break;
					case SHIELD:
						applyShielddown(1);
						break;
					case THRUST:
						applySlowdown(1);
						break;
					default:
						break;
				}
			}
		}
	}

	private void applyLostControl(boolean apply) {
		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.CONTROLLESS)) {
			if (apply) {
				//nothing to do as the effect is already set
				return;
			}
			segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.CONTROLLESS).end();
		}
		if (apply) {
			segmentController.getBlockEffectManager().addEffect(new ControllessEffect(segmentController));
		}
	}

	private void applySlowdown(float amount) {
		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.THRUSTER_OUTAGE)) {
			if (((ThrusterOutageEffect) segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.THRUSTER_OUTAGE)).getForce() == amount) {
				//nothing to do as the effect is already set
				return;
			}
			segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.THRUSTER_OUTAGE).end();
		}
		if (amount < 1f) {
			segmentController.getBlockEffectManager().addEffect(new ThrusterOutageEffect(segmentController, amount));
		}
	}

	private void applyShielddown(float amount) {
		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.NO_SHIELD_RECHARGE)) {
			if (((ShieldRegenDownEffect) segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.NO_SHIELD_RECHARGE)).getForce() == amount) {
				//nothing to do as the effect is already set
				return;
			}
			segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.NO_SHIELD_RECHARGE).end();
		}
		if (amount < 1f) {
			segmentController.getBlockEffectManager().addEffect(new ShieldRegenDownEffect(segmentController, amount));
		}
	}

	private void applyPowerdown(float amount) {

		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.NO_POWER_RECHARGE)) {
			if (((PowerRegenDownEffect) segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.NO_POWER_RECHARGE)).getForce() == amount) {
				//nothing to do as the effect is already set
				return;
			}
			segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.NO_POWER_RECHARGE).end();
		}
		if (amount < 1f) {
			segmentController.getBlockEffectManager().addEffect(new PowerRegenDownEffect(segmentController, amount));
		}
	}

	@Override
	public float onHullDamage(Damager damager, float hullDamage, short hitBlockType, DamageDealerType weaponType) {

		ElementInformation info;

		if (isRebooting()) {
			segmentController.sendControllingPlayersServerMessage(Lng.astr("Reboot aborted\nby damage taken"), ServerMessage.MESSAGE_TYPE_ERROR);
			cancelReboot();
		}

//		if (!ignoreArmor && getArmorInternal() > 0) {
//			if (VoidElementManager.ARMOR_HP_ABSORBED_DAMAGE_MULTIPLIER != 0 && (hitBlockType < 0 || ((info = ElementKeyMap.getInfoFast(hitBlockType)).armorHP > 0))) {
//
//				float chamberAbsorbtion = (segmentController.getConfigManager().apply(StatusEffectType.ARMOR_HP_ABSORPTION, 1f))-1.0f;
//				float ab = VoidElementManager.ARMOR_HP_ABSORBTION +
//						segmentController.getBlockEffectManager().status.armorHPAbsorbtionBonus + chamberAbsorbtion;
//						
//				double absorbtion = Math.min((double) getArmorInternal(), Math.min(1f, ab) * hullDamage);
//
//				if (isOnServer()) {
//					float armorAbs;
//
//					armorAbs = (float) absorbtion;
//					double n = armorAbs * VoidElementManager.ARMOR_HP_ABSORBED_DAMAGE_MULTIPLIER;
//					int penetrationDepth = 1;
//					
//					double removed = Math.min(n - (n * segmentController.getBlockEffectManager().status.armorHPDeductionBonus),
//						ElementKeyMap.getInfoFast(hitBlockType).armorHP * penetrationDepth + ElementKeyMap.getInfoFast(hitBlockType).armorHP * penetrationDepth * getSystemStabilityPenalty());
//					
//					removed = segmentController.getConfigManager().apply(StatusEffectType.ARMOR_HP_EFFICIENCY, removed);
//					
//					//System.out.println("ARMOR HP: removed " + removed + " with original damage " + hullDamage);
//					setArmorInternal((long) (getArmorInternal() - removed));
//				}
//				hullDamage -= absorbtion;
//
//			} else if (ElementKeyMap.isValidType(hitBlockType) && (info = ElementKeyMap.getInfoFast(hitBlockType)).armorHP > 0) {
//				//absorbedDamageToArmorHP = (ArmorHpDamageThreshold * [armorhp of the block]) * [current ArmorHp Left On Ship];
//				//prevent possible overflow
//				long absorbed = ((long) (VoidElementManager.ARMOR_HP_DAMAGE_THRESHOLD * info.armorHP) * getArmorHp());
//
//				absorbed = (long) Math.min(hullDamage, absorbed);
//				if (isOnServer()) {
//					setArmorInternal(getArmorInternal() - absorbed);
//				}
//				hullDamage -= absorbed;
//			}
//		}

		return hullDamage;

	}

	@Override
	public void forceDamage(float hullDamage) {
		if (isOnServer()) {
//			long hpBef = getHpInternal();
			setHpInternal(Math.max(0, getHpInternal() - (long) hullDamage));
			setLastDamager(null);
//			System.err.println("[SHIPHP][FORCED] "+segmentController.getState()+" newHP "+hpBef+" -> "+hp +" of max "+maxHp+"; filled: "+getHpPercent()+"; missing: "+getHpMissingPercent());
		}
	}
	@Override
	public void onAddedElementsSynched(int[] map, int[] oreCounts) {
		if (!segmentController.hasStructureAndArmorHP()) {
			return;
		}
		/*
		 * only add HP for structures that didn't have any HP yet
		 */
		if (isOnServer() && !loadedFromTag) {
			if (getHpInternal() == getMaxHpInternal()) {
				/*
				 * when hp were on max, update the current hp to max too
				 */
				hpDirty = true;
			}
//			if (getArmorInternal() == getMaxArmorInternal()) {
//				/*
//				 * when hp were on max, update
//				 * the current hp to max too
//				 */
//				armorHpDirty = true;
//			}
			currentHPMatch.add(map, oreCounts);
			maxHPDirty = true;
//			maxArmorHPDirty = true;
		}
		
	}
	@Override
	public void onAddedElementSynched(short newType) {
		if (!segmentController.hasStructureAndArmorHP()) {
			return;
		}
		/*
		 * only add HP for structures that didn't have any HP yet
		 */
		if (isOnServer() && !loadedFromTag) {
			if (getHpInternal() == getMaxHpInternal()) {
				/*
				 * when hp were on max, update the current hp to max too
				 */
				hpDirty = true;
			}
//			if (getArmorInternal() == getMaxArmorInternal()) {
//				/*
//				 * when hp were on max, update
//				 * the current hp to max too
//				 */
//				armorHpDirty = true;
//			}
			currentHPMatch.inc(newType);
			maxHPDirty = true;
//			maxArmorHPDirty = true;
		}

	}

	@Override
	public void updateLocal(Timer timer) {
		if (!segmentController.hasStructureAndArmorHP()) {
			return;
		}
		
		if (isOnServer()) {
			
			if(segmentController.railController.isDockedAndExecuted() && segmentController.isNewPowerSystemNoReactorOverheatingCondition()) {
				triggerOverheating();
				return;
			}
			
			if (maxHPDirty) {
				setMaxHpInternal(currentHPMatch.getMaxHP());
				maxHPDirty = false;

			}

			if (hpDirty) {
				setHpInternal(getMaxHpInternal());
				hpDirty = false;
			}

			if(!getSegmentController().railController.hasActiveDockingRequest()){
				if(getSegmentController().railController.isDockedAndExecuted()){
					if(!filledUpFromOld && ((SegmentControllerHpController)getSegmentController().railController.getRoot().getHpController()).filledUpFromOld && getHpInternal() == 1){
						hpDirty = true;
						filledUpFromOld = true;
						return;
					}
				}
				handleEffectsServer();
			}
			
			
		}
		if (isRebooting()) {
			if (getRebootTimeLeftMS() <= 0) {
				forceReset();
			}

			if (segmentController.isClientOwnObject()) {
				((GameClientState) segmentController.getState()).getController().showBigTitleMessage("reboot",
						Lng.str("REBOOTING... %s until systems are back online",StringTools.formatTimeFromMS(getRebootTimeLeftMS())), 0);
			}
		}
		
		if (isOnServer() && (getHpInternal() != lastHp || 
				(segmentController.isNewPowerSystemNoReactor()
						&& segmentController.isNewPowerSystemNoReactorOverheatingCondition()))) {
			handleEffectsServer();
		}
		lastHp = getHpInternal();
	}
	@Override
	public boolean hadOldPowerBlocks(){
		return currentHPMatch.get(ElementKeyMap.POWER_ID_OLD) > 0  ;
	}
	@Override
	public void updateFromNetworkObject(NetworkSegmentController s) {
		if (!isOnServer()) {

			useHPLong = segmentController.getNetworkObject().useHpLong.getBoolean();
//			useArmorLong = segmentController.getNetworkObject().useArmorLong.getBoolean();

			if (useHPLong) {
				hpLong = segmentController.getNetworkObject().hpLong.getLong();
				maxHpLong = segmentController.getNetworkObject().hpMaxLong.getLong();
			} else {
				hpInt = segmentController.getNetworkObject().hpInt.getInt();
				maxHpInt = segmentController.getNetworkObject().hpMaxInt.getInt();
			}

//			if (useArmorLong) {
//				armorHPLong = segmentController.getNetworkObject().armorHpLong.getLong();
//				maxArmorHpLong = segmentController.getNetworkObject().armorHpMaxLong.getLong();
//			} else {
//				armorHPInt = segmentController.getNetworkObject().armorHpInt.getInt();
//				maxArmorHpInt = segmentController.getNetworkObject().armorHpMaxInt.getInt();
//			}

			if (segmentController.getNetworkObject().rebootStartTime.getLong() > 0) {
				rebootStarted = segmentController.getNetworkObject().rebootStartTime.getLong() - ((GameClientState) segmentController.getState()).getServerTimeDifference();
			} else {
				rebootStarted = segmentController.getNetworkObject().rebootStartTime.getLong();
			}
			rebootTime = segmentController.getNetworkObject().rebootDuration.getLong();

			rebootRecover = segmentController.getNetworkObject().rebootRecover.getBoolean();
		}
	}

	@Override
	public void initFromNetwork(NetworkSegmentController s) {
		updateFromNetworkObject(s);
	}

	@Override
	public void updateToNetworkObject() {
		if (isOnServer()) {
			segmentController.getNetworkObject().useHpLong.set(useHPLong);
//			segmentController.getNetworkObject().useArmorLong.set(useArmorLong);

			if (useHPLong) {
				segmentController.getNetworkObject().hpLong.set(hpLong);
				segmentController.getNetworkObject().hpMaxLong.set(maxHpLong);
			} else {
				segmentController.getNetworkObject().hpInt.set(hpInt);
				segmentController.getNetworkObject().hpMaxInt.set(maxHpInt);
			}

//			if (useArmorLong) {
//				segmentController.getNetworkObject().armorHpLong.set(armorHPLong);
//				segmentController.getNetworkObject().armorHpMaxLong.set(maxArmorHpLong);
//			} else {
//				segmentController.getNetworkObject().armorHpInt.set(armorHPInt);
//				segmentController.getNetworkObject().armorHpMaxInt.set(maxArmorHpInt);
//			}

			segmentController.getNetworkObject().rebootStartTime.set(rebootStarted);
			segmentController.getNetworkObject().rebootDuration.set(rebootTime);
			segmentController.getNetworkObject().rebootRecover.set(rebootRecover);

		}
	}

	@Override
	public void updateToFullNetworkObject() {
		updateToNetworkObject();
	}

	@Override
	public void onRemovedElementSynched(short oldType) {
	}

	@Override
	public void reboot(boolean fast) {
		//INSERTED CODE @???
		SystemsRebootingEvent event = new SystemsRebootingEvent(getSegmentController(), this, fast);
		StarLoader.fireEvent(event, isOnServer());
		///
		if (!isOnServer()) {
			if(!((ManagedSegmentController<?>) segmentController).getManagerContainer().getPowerInterface().isAnyRebooting()){
				((GameClientState) segmentController.getState()).getPlayer().sendSimpleCommand(SimplePlayerCommands.REBOOT_STRUCTURE, segmentController.getId(), fast);
				if (segmentController instanceof ManagedSegmentController<?>) {
					((ManagedSegmentController<?>) segmentController).getManagerContainer().getPowerInterface().requestRecalibrate();
				}
			}
		} else {
			rebootRecover = false;
			if (fast) {
				forceReset();
			} else {
				
				if (segmentController instanceof ManagedSegmentController<?>) {
					if (((ManagedSegmentController<?>) segmentController).getManagerContainer() instanceof ShieldContainerInterface) {
						ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer()).getShieldAddOn();
						shieldAddOn.onHit(0L, (short)0, (long) Math.ceil(shieldAddOn.getShields()), DamageDealerType.GENERAL);
						shieldAddOn.getShieldLocalAddOn().dischargeAllShields();
					}
					if (((ManagedSegmentController<?>) segmentController).getManagerContainer() instanceof PowerManagerInterface) {
						PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer()).getPowerAddOn();
						powerAddOn.consumePowerInstantly(powerAddOn.getPower());
					}
				}
				if(segmentController.isUsingOldPower()){
					this.rebootStarted = System.currentTimeMillis();
					this.rebootTime = getRebootTimeMS();
				}
			}
			if (segmentController.isCoreOverheating()) {
				rebootRecover = true;
				segmentController.stopCoreOverheating();
			}

		}
	}

	@Override
	public void forceReset() {

		if (isOnServer()) {
			
			if (segmentController instanceof Ship && segmentController.getSegmentBuffer().getPointUnsave(Ship.core) == null) {
				System.err.println("[ERROR] Core not loaded");
				return;
			}
			
			this.rebootStarted = 0;
			this.rebootTime = 0;
			currentHPMatch.resetAll();
			currentHPMatch.add(segmentController.getElementClassCountMap());
			maxHPDirty = true;
			hpDirty = true;

//			if (segmentController instanceof SpaceStation) {
//				repairArmor(true);
//			}

			if (segmentController instanceof Ship) {
				SegmentPiece block = segmentController.getSegmentBuffer().getPointUnsave(Ship.core);//autorequest true previously
				if (block.getType() == ElementKeyMap.CORE_ID && block.isDead()) {
					block.setHitpointsByte(1);
					try{
						block.getSegment().getSegmentData().applySegmentData(block, System.currentTimeMillis());
					}catch(SegmentDataWriteException e){
						SegmentDataWriteException.replaceData(block.getSegment());
						try {
							block.getSegment().getSegmentData().applySegmentData(block, System.currentTimeMillis());
						} catch (SegmentDataWriteException e1) {
							throw new RuntimeException(e1);
						}
					}
					((Ship) segmentController).sendBlockMod(new RemoteSegmentPiece(block, isOnServer()));
				}
			}
		}
	}

	@Override
	public boolean isRebooting() {
		return rebootStarted > 0;
	}

	@Override
	public long getRebootTimeLeftMS() {
		return (rebootStarted + rebootTime) - System.currentTimeMillis();
	}

	@Override
	public long getRebootTimeMS() {
		
		if (isOnServer()) {
			long rbTime;
			if(getSegmentController().isUsingOldPower()){
				rbTime = (long) (((getHpMissingPercent() * VoidElementManager.SHIP_REBOOT_TIME_IN_SEC_PER_MISSING_HP_PERCENT) * Math
						.max(1d,
								(currentHPMatch.getMass() * VoidElementManager.SHIP_REBOOT_TIME_MULTIPLYER_PER_MASS))) * 1000d);
				rbTime = Math
						.max(rbTime,
								(long) (VoidElementManager.SHIP_REBOOT_TIME_MIN_SEC * 1000d));
			}else{
				ManagerContainer<?> man = ((ManagedSegmentController<?>)segmentController).getManagerContainer();
				rbTime = (long)(man.getPowerInterface().getRebootTimeSec()*1000f);
			}
			return rbTime;
		} else {
			if (!requestedTimeClient) {
				((GameClientState) segmentController.getState()).getPlayer().sendSimpleCommand(SimplePlayerCommands.REBOOT_STRUCTURE_REQUEST_TIME, segmentController.getId());
				requestedTimeClient = true;
			}
			return rebootTime;
		}
	}

	@Override
	public void onElementDestroyed(Damager from, ElementInformation elementInformation, DamageDealerType weaponType, long weaponId) {
		if (!segmentController.hasStructureAndArmorHP()) {
			return;
		}
		if (isOnServer()) {
			//happens if block is hit by any damage

//			long hpBef = getHpInternal();

			int removed;

			double deducted = elementInformation.structureHP;

			deducted += getSystemStabilityPenalty() * deducted;

//			if (effect != null) {
//				removed = (int) effect.modifySystemHPDamage((int) deducted, weaponType, effectRatio);
//			} else {
				removed = (int) deducted;
//			}

			setHpInternal(Math.max(0, getHpInternal() - removed));
			setLastDamager(from);

//			System.err.println("[SHIPHP] "+segmentController.getState()+" newHP "+hpBef+" -> "+hp +" of max "+maxHp+"; filled: "+getHpPercent()+"; missing: "+getHpMissingPercent());
		}

	}

	@Override
	public void onManualRemoveBlock(ElementInformation elementInformation) {
		if (!segmentController.hasStructureAndArmorHP()) {
			return;
		}
		if (isOnServer()) {
			if (getHpInternal() == getMaxHpInternal()) {
				/*
				 * only change max hp
				 * if the ship was already at max hp
				 */
				currentHPMatch.dec(elementInformation.getId());
				maxHPDirty = true;
				hpDirty = true;
			} else {
				setHpInternal(Math.max(0, getHpInternal() - elementInformation.structureHP));
			}
//			if (getArmorInternal() == getMaxArmorInternal()) {
//				/*
//				 * only change max hp
//				 * if the ship was already at max hp
//				 */
//				armorHpDirty = true;
//				maxArmorHPDirty = true;
//			} else {
//				setArmorInternal(Math.max(0, getArmorInternal() - ((int) (elementInformation.armorHP * VoidElementManager.ARMOR_HP_BLOCK_MULTIPLIER))));
//			}
			setLastDamager(null);
		}
	}

	@Override
	public double getHpPercent() {
		return getMaxHpInternal() > 0 ? (double) getHpInternal() / (double) getMaxHpInternal() : 1; //return 100% if there are no max HP set yet
	}

	@Override
	public void setHpPercent(float v) {
		setHpInternal((long) ((double)v * getHpInternal()));
	}

	@Override
	public boolean isRebootingRecoverFromOverheating() {
		return isRebooting() && rebootRecover;
	}

	@Override
	public float getSystemStabilityPenalty() {
		return (VoidElementManager.HP_DEDUCTION_LOG_FACTOR * (Math.max(0, Math.max(0, (float) Math.log10(getMaxHpInternal())) + VoidElementManager.HP_DEDUCTION_LOG_OFFSET)));
	}

	/**
	 * @return the segmentController
	 */
	public SegmentController getSegmentController() {
		return segmentController;
	}

	/**
	 * @return
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#isOnServer()
	 */
	public boolean isOnServer() {
		return segmentController.isOnServer();
	}
	public void checkOneHp(){
		
//		System.err.println("LOADED :::: "+getSegmentController()+"; WITH OLD MAXHP :"+getMaxHpInternalOld()+"; oldpwtag "+getSegmentController().usedOldPowerFromTag);
		if (getSegmentController().usedOldPowerFromTag && getMaxHpInternalOld() == 0) {
			
			//illegal state: replace max with what was loaded
			setMaxHpInternal(currentHPMatch.getMaxHP());
			if (getMaxHpInternalOld() == 0) {
				//still illegal: recalculate HP upon loading (last resort)
				currentHPMatch.resetAll();
				setMaxHpInternal(0L);
				setHpInternal(0L);

//				setMaxArmorInternal(0);
//				setArmorInternal(0);
				loadedFromTag = false;
				return;
			}
		}else if(hadOldPowerBlocks() && getHpInternal() == 1){
			maxHPDirty = true;
			hpDirty = true;
			filledUpFromOld = true;
			System.err.println("[HP] reset hp of "+getSegmentController()+" with old power since it was at 1");
		}
	}
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] top = (Tag[]) tag.getValue();
		byte classId = (Byte) top[0].getValue();

		if (classId == HP_CONTROLLER_CLASS_ID) {

			loadedFromTag = true;

			Tag[] values = (Tag[]) top[1].getValue();

			if (values[0].getType() == Type.LONG) {
				useHPLong = true;
				hpLong = (Long) values[0].getValue();
				maxHpLong = (Long) values[1].getValue();
			} else {
				useHPLong = false;
				hpInt = (Integer) values[0].getValue();
				maxHpInt = (Integer) values[1].getValue();
			}

			if (values[2].getType() == Type.LONG) {
//				useArmorLong = true;
//				armorHPLong = (Long) values[2].getValue();
//				maxArmorHpLong = (Long) values[3].getValue();
			} else {
//				useArmorLong = false;
//				armorHPInt = (Integer) values[2].getValue();
//				maxArmorHpInt = (Integer) values[3].getValue();
			}

			rebootStarted = (Long) values[4].getValue();
			rebootTime = (Long) values[5].getValue();

			currentHPMatch = (ElementCountMap) values[6].getValue();

			rebootRecover = values.length > 7 && values[7].getType() == Type.BYTE && ((Byte) values[7].getValue() != (byte) 0);

//			if (getSegmentController().usedOldPowerFromTag && getMaxHpInternalOld() == 0) {
//				
//				//illegal state: replace max with what was loaded
//				setMaxHpInternal(currentHPMatch.getMaxHP());
//				if (getMaxHpInternalOld() == 0) {
//					//still illegal: recalculate HP upon loading (last resort)
//					currentHPMatch.resetAll();
//					setMaxHpInternal(0L);
//					setHpInternal(0L);
//
//					setMaxArmorInternal(0);
//					setArmorInternal(0);
//					loadedFromTag = false;
//					return;
//				}
//			}

			if (getHpInternal() > getMaxHpInternal()) {
				//inconsistence (can happen from dev versions)
				setMaxHpInternal(currentHPMatch.getMaxHP());
				setHpInternal(getMaxHpInternal());
				
				maxHPDirty = true;
				hpDirty = true;
			}
			
//			if (getArmorInternal() > getMaxArmorInternal()) {
//				//inconsistence (can happen from dev versions)
//				setMaxArmorInternal(currentHPMatch.getMaxArmorHP());
//				setArmorInternal(getMaxArmorInternal());
//				maxArmorHPDirty = true;
//				armorHpDirty = true;
//			}
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag classId = new Tag(Type.BYTE, null, HP_CONTROLLER_CLASS_ID);

		Tag hpTag;
		Tag maxHpTag;

		Tag armorHpTag;
		Tag maxArmorHpTag;

		if (useHPLong) {
			hpTag = new Tag(Type.LONG, null, hpLong);
			maxHpTag = new Tag(Type.LONG, null, maxHpLong);
		} else {
			hpTag = new Tag(Type.INT, null, hpInt);
			maxHpTag = new Tag(Type.INT, null, maxHpInt);
		}

//		if (useArmorLong) {
			armorHpTag = new Tag(Type.LONG, null, 0L /*armorHPLong*/);
			maxArmorHpTag = new Tag(Type.LONG, null, 0L /*maxArmorHpLong*/);
//		} else {
//			armorHpTag = new Tag(Type.INT, null, armorHPInt);
//			maxArmorHpTag = new Tag(Type.INT, null, maxArmorHpInt);
//		}

		System.err.println("[HP][SAVE] saved "+getSegmentController()+"; Reactors: "+getSegmentController().isUsingPowerReactors()+"; HP: long "+hpLong+"/"+maxHpLong+"; int "+hpInt+"/"+maxHpInt+"; using long "+useHPLong);
		
		Tag rebootStartedTag = new Tag(Type.LONG, null, rebootStarted);
		Tag rebootTimeTag = new Tag(Type.LONG, null, rebootTime);
		Tag currentHpMatchTag = new Tag(Type.SERIALIZABLE, null, new ElementCountMap(currentHPMatch));
		Tag recoverRebootTag = new Tag(Type.BYTE, null, rebootRecover ? (byte) 1 : (byte) 0);

		return new Tag(Type.STRUCT, null, new Tag[]{
				classId,
				new Tag(Type.STRUCT, null, new Tag[]{
						hpTag,
						maxHpTag,
						armorHpTag,
						maxArmorHpTag,
						rebootStartedTag,
						rebootTimeTag,
						currentHpMatchTag,
						recoverRebootTag,
						FinishTag.INST
				}),
				FinishTag.INST
		});
	}


	private void cancelReboot() {
		this.rebootStarted = 0;
		this.rebootTime = 0;
	}

	public double getHpMissingPercent() {
		return 1.0d - getHpPercent();
	}

	/**
	 * @return the requestedTimeClient
	 */
	public boolean isRequestedTimeClient() {
		return requestedTimeClient;
	}

	/**
	 * @param requestedTimeClient the requestedTimeClient to set
	 */
	@Override
	public void setRequestedTimeClient(boolean requestedTimeClient) {
		this.requestedTimeClient = requestedTimeClient;
	}

	/**
	 * @param rebootTime the rebootTime to set
	 */
	@Override
	public void setRebootTimeServerForced(long rebootTime) {
		this.rebootTime = rebootTime;
	}

	@Override
	public long getHp() {
		return getHpInternal();
	}

	@Override
	public long getMaxHp() {
		return getMaxHpInternal();
	}

//	@Override
//	public long getArmorHp() {
//		return getArmorInternal();
//	}
//
//	@Override
//	public long getMaxArmorHp() {
//		return getMaxArmorInternal();
//	}

	@Override
	public String getDebuffString() {
		if (getHpInternal() == getMaxHpInternal()) {
			return Lng.str("All Systems Fine");
		}
		StringBuffer debuff = new StringBuffer();

		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.CONTROLLESS)) {
			if (debuff.length() == 0) {
				debuff.append(Lng.str("Systems damaged: Controls"));
			} else {
				debuff.append(", Controls");
			}
		}

		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.NO_POWER_RECHARGE)) {
			String t = "Power Recharge (" +
					StringTools.formatPointZero(
							((PowerRegenDownEffect) segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.NO_POWER_RECHARGE)).getForce() * 100f)
					+ "%)";
			if (debuff.length() == 0) {
				debuff.append(Lng.str("Systems damaged: "));
			} else {
				debuff.append(", ");
			}
			debuff.append(t);
		}
		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.NO_SHIELD_RECHARGE)) {

			String t = "Shield Recharge (" +
					StringTools.formatPointZero(
							((ShieldRegenDownEffect) segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.NO_SHIELD_RECHARGE)).getForce() * 100f)
					+ "%)";

			if (debuff.length() == 0) {
				debuff.append(Lng.str("Systems damaged: "));
			} else {
				debuff.append(", ");
			}
			debuff.append(t);
		}
		if (segmentController.getBlockEffectManager().hasEffect(BlockEffectTypes.THRUSTER_OUTAGE)) {
			String t = "Thrust (" +
					StringTools.formatPointZero(
							((ThrusterOutageEffect) segmentController.getBlockEffectManager().getEffect(BlockEffectTypes.THRUSTER_OUTAGE)).getForce() * 100f)
					+ "%)";
			if (debuff.length() == 0) {
				debuff.append(Lng.str("Systems damaged: "));
			} else {
				debuff.append(", ");
			}
			debuff.append(t);
		}

		if (segmentController.isCoreOverheating()) {
			debuff.append("\n" + Lng.str("CRITICAL: CORE OVERHEATING ") + StringTools.formatTimeFromMS(segmentController.getCoreOverheatingTimeLeftMS(System.currentTimeMillis())) + "!");
		} else if (isRebooting()) {
			debuff.append("\n" + Lng.str("Systems rebooting: ") + StringTools.formatTimeFromMS(getRebootTimeLeftMS()) + "!");
		}

		if (debuff.length() == 0) {
			debuff.append(Lng.str("Hull Damage! Reboot Systems to reset HP ['%s']", KeyboardMappings.REBOOT_SYSTEMS.getKeyChar()));
		} else {
			debuff.append("\n" + Lng.str("Reboot to reset HP and restore Systems"));
		}
		return debuff.toString();
	}

	@Override
	public void onManualAddBlock(ElementInformation elementInformation) {
		if (!segmentController.hasStructureAndArmorHP()) {
			return;
		}
		if (!loadedFromTag) {
			/*
			 * was not loaded from tag. adding a block means that no longer HP
			 * will be added in the onAddedElementSynched function.
			 * We skip the adding here one time
			 * as HP was already added in onAddedElementSynched
			 * which was executed before this
			 */
			loadedFromTag = true;
		} else {
			if (isOnServer()) {
				if (getHpInternal() == getMaxHpInternal()) {
					/*
					 * when hp were on max, update
					 * the current hp to max too
					 */
					hpDirty = true;
				}
//				if (getArmorInternal() == getMaxArmorInternal()) {
//					/*
//					 * when hp were on max, update
//					 * the current hp to max too
//					 */
//					armorHpDirty = true;
//				}
				setLastDamager(null);
				currentHPMatch.inc(elementInformation.getId());
				maxHPDirty = true;
//				maxArmorHPDirty = true;

			}
		}
	}

//	@Override
//	public double getArmorHpPercent() {
//		return getMaxArmorInternal() > 0 ? (double) getArmorInternal() / (double) getMaxArmorInternal() : 0;
//	}

	@Override
	public long getShopRebootCost() {

		double v = (double) (getRebootTimeMS() / 1000L) * (double) ((GameStateInterface) segmentController.getState()).getGameState().getShopRebootCostPerSecond();

		return (long) v;
	}

//	@Override
//	public long getShopArmorRepairCost() {
//		double v = (double) (getMaxArmorInternal() - getArmorInternal()) * (double) ((GameStateInterface) segmentController.getState()).getGameState().getShopArmorRepairPerSecond();
//		return (long) v;
//	}
//
//	@Override
//	public void repairArmor(boolean fast) {
//		if (!isOnServer()) {
//			((GameClientState) segmentController.getState()).getPlayer().sendSimpleCommand(SimplePlayerCommands.REPAIR_ARMOR, segmentController.getId(), fast);
//
//		} else {
//			if (fast) {
//				currentHPMatch.resetAll();
//				currentHPMatch.add(segmentController.getElementClassCountMap());
//				armorHpDirty = true;
//				maxArmorHPDirty = true;
//			} else {
//				throw new IllegalArgumentException("For the moment, armor repair can only be done fast");
//			}
//		}
//	}
//
//	@Override
//	public void setArmorHpPercent(float v) {
//		setArmorHp((long) (v * getMaxArmorInternal()));
//	}
//
//	@Override
//	public void setArmorHp(long v) {
//		setArmorInternal(Math.max(0, Math.min(getMaxArmorInternal(), v)));
//	}

}
