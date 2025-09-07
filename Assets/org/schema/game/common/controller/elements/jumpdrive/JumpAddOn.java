package org.schema.game.common.controller.elements.jumpdrive;

import api.listener.events.systems.InterdictionCheckEvent;
import api.mod.StarLoader;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableSingleModule;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Sector;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate.Type;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

@Deprecated
public class JumpAddOn extends RecharchableSingleModule {
	private long lastSentJmpMsg;

	private static final int DEFAULT_MAX_CHARGES = 1;

	@Override
	public int getMaxCharges() {
		return getConfigManager().apply(StatusEffectType.JUMP_MULTI_CHARGE_COUNT, DEFAULT_MAX_CHARGES);
	}

	public JumpAddOn(ManagerContainer<?> man) {
		super(man);
	}

	public int getDistance() {
		return (int) (getConfigManager().apply(StatusEffectType.JUMP_DISTANCE, VoidElementManager.REACTOR_JUMP_DISTANCE_DEFAULT));
	}

	@Override
	public void sendChargeUpdate() {
		if(isOnServer()) {
			JumpAddOnChargeValueUpdate v = new JumpAddOnChargeValueUpdate();
			v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), USABLE_ID_JUMP);
			assert (v.getType() == ValTypes.JUMP_CHARGE_REACTOR);
			((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
		}
	}

	@Override
	public boolean isDischargedOnHit() {
		return true;
	}

	@Override
	public boolean executeModule() {
		if(getSegmentController().isOnServer()) {
			if(getCharges() > 0) {
				if(!isInterdicted()) {
					if(getSegmentController().engageJump(getDistance())) {
						removeCharge();
						setCharge(0);
						//send one when used
						sendChargeUpdate();
						return true;
					}
				} else {
					if(getState().getUpdateTime() - lastSentJmpMsg > 3000) {
						try {
							getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nSector is interdicted by %s.", segmentController.getInterdictingEntity().getRealName()), ServerMessage.MESSAGE_TYPE_ERROR);
						} catch(NullPointerException exception) {
							getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nSector is interdicted."), ServerMessage.MESSAGE_TYPE_ERROR);
						}
						lastSentJmpMsg = getState().getUpdateTime();
					}
				}
			} else {
				if(getState().getUpdateTime() - lastSentJmpMsg > 3000) {
					getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nDrive not charged.\n(%s%%)", StringTools.formatPointZero(getCharge() * 100f)), ServerMessage.MESSAGE_TYPE_ERROR);
					lastSentJmpMsg = getState().getUpdateTime();
				}
			}
		}
		return false;
	}

	//REPLACE METHOD
	private boolean isInterdicted() {
		assert this.isOnServer();
		GameServerState serverState;
		Sector sector;
		boolean returnVal = false;
		if((sector = (serverState = (GameServerState) this.getState()).getUniverse().getSector(this.getSegmentController().getSectorId())) == null) {
			System.err.println("[SERVER][JUMP] " + this.getSegmentController() + " IS NOT IN A SECTOR " + this.getSegmentController().getSectorId());
		} else {
			Vector3i secPos = new Vector3i();

			for(int x = -1; x <= 1; ++x) {
				for(int y = -1; y <= 1; ++y) {
					for(int z = -1; z <= 1; ++z) {
						secPos.set(sector.pos.x + z, sector.pos.y + y, sector.pos.z + x);
						Sector var7;
						if((var7 = serverState.getUniverse().getSectorWithoutLoading(secPos)) != null && var7.isInterdicting(this.getSegmentController(), sector)) {
							this.getSegmentController().sendControllingPlayersServerMessage(new Object[] {43, var7.pos.toStringPure()}, 3);
							returnVal = true;
							break;
						}
					}
				}
			}
		}
		InterdictionCheckEvent event = new InterdictionCheckEvent(this, this.segmentController, returnVal);
		StarLoader.fireEvent(InterdictionCheckEvent.class, event, this.isOnServer());

		if(!event.useDefault) {
			return event.isInterdicted();
		} else {
			return returnVal;
		}
	}
	//

	@Override
	public void onChargedFullyNotAutocharged() {
		getSegmentController().popupOwnClientMessage(Lng.str("Jump Drive Charged!\nRight click on icon to jump!"), ServerMessage.MESSAGE_TYPE_INFO);
	}

	@Override
	public float getChargeRateFull() {
		float cNeeded = VoidElementManager.REACTOR_JUMP_CHARGE_NEEDED_IN_SEC + VoidElementManager.REACTOR_JUMP_CHARGE_NEEDED_IN_SEC_EXTRA_PER_MASS * getMassWithDocks() + Math.max(0, ((float) Math.log10(getMassWithDocks()) + VoidElementManager.REACTOR_JUMP_CHARGE_NEEDED_IN_SEC_LOG_OFFSET) * VoidElementManager.REACTOR_JUMP_CHARGE_NEEDED_IN_SEC_LOG_FACTOR) * VoidElementManager.REACTOR_JUMP_CHARGE_NEEDED_IN_SEC;

		float r = getConfigManager().apply(StatusEffectType.JUMP_CHARGE_TIME, cNeeded);
		return r;
	}

	@Override
	public boolean canExecute() {
		if(getSegmentController().getDockingController().isDocked() || getSegmentController().railController.isDockedOrDirty()) {
			getSegmentController().popupOwnClientMessage(Lng.str("Cannot jump!\nShip is docked!"), ServerMessage.MESSAGE_TYPE_ERROR);
			return false;
		} else {
			if(getSegmentController().getPhysicsDataContainer().getObject() != null) {
				return true;
			} else {
				getSegmentController().sendControllingPlayersServerMessage(Lng.astr("ERROR\nPhysical Object not found!"), 0);
				return false;
			}
		}
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		float r = getConfigManager().apply(StatusEffectType.JUMP_POWER_TOPOFF_RATE, VoidElementManager.REACTOR_JUMP_POWER_CONSUMPTION_RESTING_PER_MASS);
		double powCons = (double) r * (double) getMassWithDocks();

		return powCons;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		float r = getConfigManager().apply(StatusEffectType.JUMP_POWER_CHARGE_RATE, VoidElementManager.REACTOR_JUMP_POWER_CONSUMPTION_CHARGING_PER_MASS);
		double powCons = (double) r * (double) getMassWithDocks();

		return powCons;
	}

	@Override
	public boolean isAutoCharging() {
		return getConfigManager().apply(StatusEffectType.JUMP_AUTO_CHARGE, false);
	}

	@Override
	public boolean isAutoChargeToggable() {
		return getConfigManager().apply(StatusEffectType.JUMP_AUTO_CHARGE, false);
	}

	@Override
	public long getUsableId() {
		return PlayerUsableInterface.USABLE_ID_JUMP;
	}

	@Override
	public void chargingMessage() {
		getSegmentController().popupOwnClientMessage(Lng.str("Jump Drive not charged\nHold left mouse to charge!"), ServerMessage.MESSAGE_TYPE_INFO);
	}

	@Override
	public void onCooldown(long diff) {
		getSegmentController().popupOwnClientMessage(Lng.str("Cannot charge!\nJump Drive on Cooldown!\n(%d secs)", diff), ServerMessage.MESSAGE_TYPE_ERROR);
	}

	@Override
	public void onUnpowered() {
		getSegmentController().popupOwnClientMessage(Lng.str("WARNING!\n \nJump drive unpowered!"), ServerMessage.MESSAGE_TYPE_ERROR);
	}

	@Override
	public String getTagId() {
		return "JAO";
	}

	@Override
	public int updatePrio() {
		return 1;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.JUMP_DRIVE;
	}

	@Override
	public String getWeaponRowName() {
		return Lng.str("Jump Drive");
	}

	@Override
	public short getWeaponRowIcon() {
		return ElementKeyMap.JUMP_DRIVE_CONTROLLER;
	}

	@Override
	public boolean isPlayerUsable() {

		if(!((GameStateInterface) getSegmentController().getState()).getGameState().isModuleEnabledByDefault(USABLE_ID_JUMP) && !getConfigManager().apply(StatusEffectType.JUMP_DRIVE_ENABLE, false)) {
			return false;
		}

		return super.isPlayerUsable();
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public String getName() {
		return "JumpAddOn";
	}

	@Override
	protected Type getServerRequestType() {
		return Type.JUMP;
	}

	@Override
	protected void onNoLongerConsumerActiveOrUsable(Timer timer) {
	}

	@Override
	public String getExecuteVerb() {
		return Lng.str("Jump");
	}
}
