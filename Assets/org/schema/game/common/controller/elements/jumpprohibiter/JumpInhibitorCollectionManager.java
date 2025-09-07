package org.schema.game.common.controller.elements.jumpprohibiter;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedCooldownInterface;
import org.schema.game.common.controller.elements.behavior.managers.charging.ChargeManager;
import org.schema.game.common.controller.elements.behavior.managers.reload.CooldownManager;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveCollectionManager;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveElementManager;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.JumpInhibitorValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.structurepersistence.PersistentStructureDataManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.sound.controller.AudioController;

public class JumpInhibitorCollectionManager extends ControlBlockElementCollectionManager<JumpInhibitorUnit, JumpInhibitorCollectionManager, JumpInhibitorElementManager> implements PlayerUsableInterface, ManagerActivityInterface, ManagedCooldownInterface {
	private final CooldownManager cooldown;
	private boolean activeInhibitor; //not using full activation manager; this is a simple toggle
	private double accum;

	public JumpInhibitorCollectionManager(SegmentPiece element, SegmentController segController, JumpInhibitorElementManager em) {
		super(element, ElementKeyMap.JUMP_INHIBITOR_MODULE, segController, em);
		cooldown = new CooldownManager(this, false); //it'll just run in parallel. no need for updates
	}

	@Override
	protected void applyMetaData(BlockMetaDataDummy dummy) {
		activeInhibitor = ((JumpInhibitorMetaDataDummy) dummy).active;
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);

		if(activeInhibitor) {
			if(getTotalSize() == 0) {
				if(getSegmentController().isOnServer() && getSegmentController().isFullyLoadedWithDock()) {
					setActive(false);
					sendActiveUpdate();
				}
			} else {
				if(getSegmentController() instanceof SpaceStation) {
					long calculatedId = PersistentStructureDataManager.calculateId(getSectorId(), getSegmentController().dbId, getControllerIndex());
					if(!PersistentStructureDataManager.containerExists(calculatedId)) {
						PersistentStructureDataManager.addContainer(new JumpInhibitorPersistentDataContainer(this));
					}
				}
				if(getPowered() > 0.999) {
					if(getSegmentController().isOnServer()) {
						dischargeOthers(timer);
					}
				} else {
					if(getSegmentController().isOnServer() && getSegmentController().isFullyLoadedWithDock()) {
						setActive(false);
						sendActiveUpdate();
					}
				}
			}
		}
	}

	private double getPowered() {
		float result = 0;
		if(!getElementCollections().isEmpty()) {
			for(JumpInhibitorUnit j : getElementCollections()) result += j.getPowered();
			result /= getElementCollections().size();
		}
		return result;
	}

	private void dischargeOthers(Timer timer) {
		assert (getSegmentController().isOnServer());
		accum += timer.getDelta();

		if(accum >= 1.0) {
			long secs = (long) accum;

			float changedUsed = secs * getChargeRemovedPerSec();

			for(Sendable sss : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if(sss instanceof Ship s) {
					if(isDischarging(s)) {
						ManagerModuleCollection<JumpDriveUnit, JumpDriveCollectionManager, JumpDriveElementManager> jumpDrive = s.getManagerContainer().getJumpDrive();
						boolean discharged = false;
						for(JumpDriveCollectionManager c : jumpDrive.getCollectionManagers()) {
							ChargeManager cm = c.getChargeManager();
							if(cm.getCharge() > 0 || cm.getChargesCount() > 0) {
								cm.addCharge(-changedUsed * c.getMaxCharges()); //increase discharge rate to match multicharge size (balancing measure)
								c.sendChargeUpdate();
								discharged = true;
							}
						}
						if(discharged) {
							String source = getSegmentController().getRealName();
							s.sendControllingPlayersServerMessage(new String[]{Lng.str("Jump Inhibitor detected!\nSource: ") + source}, ServerMessage.MESSAGE_TYPE_ERROR);
							//not sure how this astr thing works, honestly. Just concat localization with real ship name for now
						}
					}
				}
			}

			accum -= secs;
		}
	}

	private boolean isDischarging(Ship s) {
		if(!JumpInhibitorElementManager.DISCHARGE_SELF && getSegmentController().railController.isInAnyRailRelationWith(s)) {
			return false;
		}
		if(!s.railController.isDockedAndExecuted() && s.isNeighbor(s.getSectorId(), getSegmentController().getSectorId())) {
			if(JumpInhibitorElementManager.DISCHARGE_FRIENDLY_SHIPS) {
				return true;
			}

			FactionRelation.RType relation = ((FactionState) getState()).getFactionManager().getRelation(getSegmentController(), s);

			return relation != FactionRelation.RType.FRIEND;

		}

		return false;
	}

	public void sendActiveUpdate() {
		JumpInhibitorValueUpdate chargeValueUpdate = new JumpInhibitorValueUpdate();
		chargeValueUpdate.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(chargeValueUpdate, getSegmentController().isOnServer()));
	}

	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Tag.Type.BYTE, null, activeInhibitor ? (byte) 1 : (byte) 0);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<JumpInhibitorUnit> getType() {
		return JumpInhibitorUnit.class;
	}

	@Override
	public boolean isControllerConnectedTo(long index, short type) {
		return true;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public JumpInhibitorUnit getInstance() {
		return new JumpInhibitorUnit();
	}

	@Override
	protected void onChangedCollection() {

		if(!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().managerChanged(this);
		}
		super.onChangedCollection();
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		float power = getPowerConsumptionPerSec();

		return new GUIKeyValueEntry[]{new ModuleValueEntry(Lng.str("Jumpdrive Discharge (/sec)"), getChargeRemovedPerSec()), new ModuleValueEntry(Lng.str("Power Consumption (/sec)"), power),

		};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Jump Inhibitor System");
	}

	public float getRange() {
		return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_RANGE, JumpInhibitorElementManager.INTERDICTION_SECTOR_RADIUS_BASE);
	}

	public float getChargeRemovedPerSec() {
		return getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_STRENGTH, JumpInhibitorElementManager.DISCHARGE_PER_SECOND + JumpInhibitorElementManager.DISCHARGE_PER_SECOND_PER_BLOCK * getTotalSize());
	}

	public float getPowerConsumptionPerSec() {
		return (float) getElementCollections().stream().mapToDouble(JumpInhibitorUnit::getPowerConsumedPerSecondCharging).sum();
		//TODO could also move power consumption to this class, as it is with scanners and jump drives
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		super.handleKeyEvent(unit, mapping, timer);
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE) {
			getElementManager().handle(unit, timer);
		}
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
		super.handleKeyPress(unit, timer);
	}

	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, HudContextHelperContainer.Hos hos) {
		String s = activeInhibitor ? Lng.str("Stop Inhibiting") : Lng.str("Inhibit");
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, s, hos, ContextFilter.IMPORTANT);
	}

	public void toggleActivation() {
		setActive(!activeInhibitor);
	}

	@Override
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
		super.onLogicActivate(selfBlock, oldActive, timer);
		boolean active = selfBlock.isActive();
		boolean update = active != activeInhibitor; //may not be synchronized, in which case just ignore this

		if(!active || !cooldown.isCoolingDown(timer)) { //no cooldown on deactivation
			setActive(active);
			if(isOnServer() && update) sendActiveUpdate();
		}
	}

	@Override
	public boolean isActive() {
		return activeInhibitor;
	}

	public void setActive(boolean v) {
		if(!isOnServer() && v != activeInhibitor) {
			if(v) {
				if(!isOnServer())
					AudioController.fireAudioEvent("0022_spaceship user - special synthetic weapon recharged 1", AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex(), 500)); //todo radius based on ship largest dimension
			} else {
				if(!isOnServer())
					AudioController.fireAudioEvent("0022_gameplay - power down big", AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex(), 500));
			}
		}
		activeInhibitor = v;
		if(!v) cooldown.startCooldown(System.currentTimeMillis());
	}

	@Override
	public CooldownManager getCooldownManager() {
		return cooldown;
	}

	@Override
	public long getCooldownDurationMs() {
		return 1000;
	}
}
