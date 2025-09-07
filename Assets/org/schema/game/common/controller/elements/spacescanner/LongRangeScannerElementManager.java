package org.schema.game.common.controller.elements.spacescanner;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.io.IOException;

public class LongRangeScannerElementManager extends UsableControllableFiringElementManager<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> implements
		NTSenderInterface, TagModuleUsableInterface, NTReceiveInterface, BlockActivationListenerInterface, ManagerReloadInterface, HittableInterface, SupportElementManagerInterface, SingleElementCollectionContainerInterface<LongRangeScannerCollectionManager> {

	public final static String TAG_ID = "LSC";

	@ConfigurationElement(name = "DefaultScanDistance")
	public static float DEFAULT_SCAN_DISTANCE = 4;
	@ConfigurationElement(name = "EnemySystemDistanceMult")
	public static float ENEMY_SYSTEM_DISTANCE_MULT = 4;
	@ConfigurationElement(name = "AllySystemDistanceMult")
	public static float ALLY_SYSTEM_DISTANCE_MULT = 4;

	@ConfigurationElement(name = "RatioNeededToReactorLevel")
	public static float RATIO_NEEDED_TO_REACTOR_LEVEL = 0.1f;
	@ConfigurationElement(name = "ReloadMsAfterUseMs")
	public static long RELOAD_AFTER_USE_MS = 1000;
	@ConfigurationElement(name = "BaseChargeTimeMs")
	public static float BASE_CHARGE_TIME_MS = 5000;
	@ConfigurationElement(name = "ChargeMsAddedPerBlock")
	public static float CHARGE_TIME_ADDED_PER_SECOND_PER_BLOCK = 10;
	@ConfigurationElement(name = "ReactorPowerNeededForScanBase")
	public static float SCAN_POWER_CONSUMPTION_FIXED = 1000;
	@ConfigurationElement(name = "ReactorPowerConsumptionRestingPerBlock")
	public static float SCAN_IDLE_POWER_CONSUMED_PER_BLOCK = 1000;
	@ConfigurationElement(name = "ReactorPowerConsumptionChargingPerBlock")
	public static float SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK = 1000;
	@ConfigurationElement(name = "ReactorPowerNeededForScanPerBlockAfterRatioMet")
	public static float SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK_AFTER_RATIO = 1000;
	@ConfigurationElement(name = "ECMStrengthMod")
	public static float ECM_STRENGTH_MOD = 10;

	public static boolean debug = false;
	private Vector3f shootingDirTemp = new Vector3f();
	private Vector3f shootingUpTemp = new Vector3f();
	private Vector3f shootingRightTemp = new Vector3f();
	private Vector3f shootingForwardTemp = new Vector3f();
	private Vector3i controlledFromOrig = new Vector3i();
	private Vector3i controlledFrom = new Vector3i();
	private long lastScan;
	public LongRangeScannerElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.SCANNER_COMPUTER, ElementKeyMap.SCANNER_MODULE, segmentController);
	}

	@Override
	public String getTagId() {
		return TAG_ID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(LongRangeScannerUnit firingUnit,
												 LongRangeScannerCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Scanner Unit"), firingUnit);
	}


	@Override
	protected String getTag() {
		return "longrangescanner";
	}

	@Override
	public LongRangeScannerCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<LongRangeScannerCollectionManager> clazz) {

		return new LongRangeScannerCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Scanner System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		long curTime = System.currentTimeMillis();
		if (!unit.isFlightControllerActive()) {
			if (debug) {
				System.err.println("NOT ACTIVE");
			}
			return;
		}
		if (getCollectionManagers().isEmpty()) {
			if (debug) {
				System.err.println("NO WEAPONS");
			}
			//nothing to shoot with
			return;
		}
		try {
			if (!convertDeligateControls(unit, controlledFromOrig, controlledFrom)) {
				if (debug) {
					System.err.println("NO SLOT");
				}
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int unpowered = 0;

		if (debug) {
			System.err.println("FIREING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + controlledFrom);
		}
		if (!getCollectionManagers().isEmpty()) {
			LongRangeScannerCollectionManager m = getCollectionManagers().get(0);
			if (unit.isSelected(m.getControllerElement(), controlledFrom)) {
				boolean controlling = controlledFromOrig.equals(controlledFrom);
				controlling |= getControlElementMap().isControlling(controlledFromOrig, m.getControllerPos(), controllerId);
				if (debug) {
					System.err.println("Controlling " + controlling + " " + getState());
				}

				if (controlling) {

					if (controlledFromOrig.equals(Ship.core)) {
						unit.getControlledFrom(controlledFromOrig);
					}

					if (unit.getPlayerState() != null && !m.getElementCollections().isEmpty() && clientIsOwnShip() && (unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) || unit.isDown(KeyboardMappings.SHIP_ZOOM))) {
						if(m.getPowered() > 0.999){
							if(!isOnServer() && unit.getPlayerState().equals(((GameClientState)getState()).getPlayer())){
								checkScan(unit.getPlayerState());
							}
							m.getCooldownManager().startCooldown(timer);
						}
						else unpowered++;
					} else {
						if (clientIsOwnShip() && unit.getPlayerState() == null) {
							System.err.println("[CLIENT] Exception: player state null: " + unit.getPlayerState());
						}
						if (clientIsOwnShip() && unit.getPlayerState() != ((GameClientState) getState()).getPlayer()) {
							System.err.println("[CLIENT] Exception: player state is different: " + unit.getPlayerState() + " vs: " + ((GameClientState) getState()).getPlayer());
						}
					}
				}
			} else {
			}
		} else {

		}
		if (unpowered > 0 && clientIsOwnShip()) {
			getPowerManager().sendNoPowerHitEffectIfNeeded();
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nScanner unpowered:",  unpowered), 0);
		}
		if (getCollectionManagers().isEmpty() && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Scanner!"), 0);
		}

	}

	public void executeScanOnServer(PlayerState player) {
		if (!getCollectionManagers().isEmpty()) {
			LongRangeScannerCollectionManager m = getCollectionManagers().get(0);
			if (m.isCharged()) {
				m.scanOnServer(player);
			} else {
				player.sendServerMessage(new ServerMessage(Lng.astr("Not fully charged (server)\n%s/%s", m.getChargeManager().getCharge(),  m.getChargeNeededForScan()), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
			}
		} else {
			player.sendServerMessage(new ServerMessage(Lng.astr("No Scan Computer!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
		}
	}

	public void checkScan(PlayerState player) {
		if (System.currentTimeMillis() - lastScan > 500) {
			if (!getCollectionManagers().isEmpty()) {
				LongRangeScannerCollectionManager m = getCollectionManagers().get(0);

				if (m.isCharged()) {
					System.err.println("[CLIENT][SCAN] " + getSegmentController() + " SENDING SIMPLE COMMAND");
					player.sendSimpleCommand(SimplePlayerCommands.SCAN, getSegmentController().getId());
					((GameClientState) getState()).getController().getClientChannel().getGalaxyManagerClient().resetClientVisibilitySystem(player.getCurrentSystem());
					lastScan = System.currentTimeMillis() + 2000;
				} else {
					if (player.isClientOwnPlayer()) {
						((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Not charged\n%s/%s",  m.getChargeManager().getCharge(),  m.getChargeNeededForScan()), 0);
					}
					lastScan = System.currentTimeMillis();
				}

			} else {
				if (player.isClientOwnPlayer()) {
					((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Scanner Modules\nconnected to entry point"), 0);
				}
				lastScan = System.currentTimeMillis();
			}

		}
	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
//		typesThatNeedActivation.add(ElementKeyMap.WEAPON_ID);
	}

	@Override
	public void updateFromNT(NetworkObject o) {
	}

	@Override
	public void updateToFullNT(NetworkObject networkObject) {
		if (getSegmentController().isOnServer()) {

		}
	}
	//	@Override
	//	public ControllerManagerGUI getGUIUnitValues(WeaponUnit firingUnit,
	//			WeaponCollectionManager col, ControlBlockElementCollectionManager<?,?,?> supportCol, ControlBlockElementCollectionManager<?,?,?> effectCol) {
	//
	//
	//		Modifier<WeaponUnit> gui = getAddOn().getGUI(col, firingUnit, supportCol, effectCol);
	//
	//		return ControllerManagerGUI.create((GameClientState)getState(), "Weapon Module (w/e support&effect)", firingUnit,
	//				new ModuleValueEntry(Lng.str("Damage/shot", StringTools.formatPointZero(firingUnit.getDamage())),
	//				new ModuleValueEntry(Lng.str("Damage/shot", StringTools.formatPointZero(firingUnit.getPowerConsumption())),
	//				new ModuleValueEntry(Lng.str("AdditionalPowerCon/#groups", StringTools.formatPointZero(firingUnit.getExtraConsume())),
	//				new ModuleValueEntry(Lng.str("Range", StringTools.formatPointZero(firingUnit.getDistance())),
	//				new ModuleValueEntry(Lng.str("Reload(ms)", StringTools.formatPointZero(firingUnit.getReloadTime())),
	//				new ModuleValueEntry(Lng.str("ProjectileSpeed", firingUnit.getSpeed())
	//		);
	//
	//	}
	@Override
	public String getReloadStatus(long id) {
		return Lng.str("STANDBY");
	}
	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		long time = System.currentTimeMillis();
		LongRangeScannerCollectionManager ec = getCollectionManagersMap().get(controllerPos);

		if (ec != null) {
			if(ec.getCooldownManager().isCoolingDown(time)) ec.getCooldownManager().drawReloads(iconPos,iconSize,(InputState)getState(),time);
			else ec.getChargeManager().drawReloads(iconPos,iconSize,(InputState)getState());
		}
	}

	@Override
	public void onHit(long pos, short type, double damage, DamageDealerType damageType) {
		if (getSegmentController().isOnServer()) {
			for (int i = 0; i < getCollectionManagers().size(); i++) {
				getCollectionManagers().get(i).hasHit(damage, damageType);
			}
		}
	}

	/**
	 * @return the lastScan
	 */
	public long getLastScan() {
		return lastScan;
	}

	/**
	 * @param lastScan the lastScan to set
	 */
	public void setLastScan(long lastScan) {
		this.lastScan = lastScan;
	}

	@Override
	public double calculateSupportIndex() {
		double p = 0;
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			p += getCollectionManagers().get(i).getTotalSize();
		}
		return p;
	}

	@Override
	public double calculateSupportPowerConsumptionPerSecondIndex() {
		double p = 0;
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			p += getCollectionManagers().get(i).getChargeNeededForScan();
		}
		return p;
	}

	@Override
	public BlockMetaDataDummy getDummyInstance() {
		return new ScannerMetaDataDummy();
	}

	@Override
	public boolean hasCollection() {
		return !getCollectionManagers().isEmpty();
	}

	@Override
	public LongRangeScannerCollectionManager getCollection() {
		if(!hasCollection()) return null;
		return getCollectionManagers().get(0);
	}
}
