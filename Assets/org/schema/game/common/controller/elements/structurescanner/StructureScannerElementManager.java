package org.schema.game.common.controller.elements.structurescanner;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.spacescanner.ScannerMetaDataDummy;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.playermessage.ServerPlayerMessager;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.io.IOException;

public class StructureScannerElementManager extends UsableControllableFiringElementManager<StructureScannerUnit, StructureScannerCollectionManager, StructureScannerElementManager> implements
		NTSenderInterface, TagModuleUsableInterface, NTReceiveInterface, BlockActivationListenerInterface, ManagerReloadInterface, HittableInterface, SupportElementManagerInterface, SingleElementCollectionContainerInterface<StructureScannerCollectionManager> {

	public final static String TAG_ID = "SSC";
	@ConfigurationElement(name = "ScanDurationBasic")
	public static long SCAN_DURATION_BASE_MS = 1000;
	@ConfigurationElement(name = "ScanStrengthBasic")
	public static long SCAN_STRENGTH_BASE = 0;
	@ConfigurationElement(name = "ScanStrengthPerBlock")
	public static float SCAN_STRENGTH_PER_BLOCK = 1;
	@ConfigurationElement(name = "ScanStrengthFalloffMultiplier")
	public static float SCAN_FALLOFF_MULTIPLIER = 1;
	@ConfigurationElement(name = "ScanStrengthFalloffExponent")
	public static float SCAN_FALLOFF_EXPONENT = 2;
	@ConfigurationElement(name = "TotalSizeDistanceModifier")
	public static float TOTAL_SIZE_DISTANCE_MODIFIER = 0.01f;
	@ConfigurationElement(name = "RatioNeededToReactorLevel")
	public static float RATIO_NEEDED_TO_REACTOR_LVL_FOR_MAX = 0.1f;
	@ConfigurationElement(name = "ReloadMsAfterUseMs")
	public static long RELOAD_AFTER_USE_MS = 1000;
	@ConfigurationElement(name = "BaseChargeTimeMs")
	public static float CHARGE_TIME_MS = 5000;
	@ConfigurationElement(name = "ChargeMsAddedPerBlock")
	public static float CHARGE_MS_ADDED_PER_BLOCK = 10;

	@ConfigurationElement(name = "ReactorPowerNeededForScanBase")
	public static float CHARGE_NEEDED_FOR_SCAN_BASE = 1000;
	@ConfigurationElement(name = "ReactorPowerNeededForScanPerBlock")
	public static float CHARGE_NEEDED_FOR_SCAN_PER_BLOCK = 1000;
	@ConfigurationElement(name = "ReactorPowerNeededForScanPerBlockAfterRatioMet")
	public static float CHARGE_NEEDED_FOR_SCAN_PER_BLOCK_AFTER_RATIO = 1000;
	@ConfigurationElement(name="MaxPossibleScanStrength")
	public static float SCAN_STRENGTH_CAP = 100f; //TODO Not sure we want to do this like this, but this value will allow testing the system at least...? needs UI documentation.

	public static boolean debug = false;
	private Vector3f shootingDirTemp = new Vector3f();
	private Vector3f shootingUpTemp = new Vector3f();
	private Vector3f shootingRightTemp = new Vector3f();
	private Vector3f shootingForwardTemp = new Vector3f();
	private Vector3i controlledFromOrig = new Vector3i();
	private Vector3i controlledFrom = new Vector3i();

	public static final long CARGO_SCAN_TIME = 15000;
	private int currentCargoScanning;
	private long currentCargoScanningStart;
	private int lastScanned;

	public StructureScannerElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.INTELL_COMPUTER, ElementKeyMap.INTELL_ANTENNA, segmentController);
	}

	@Override
	public String getTagId() {
		return TAG_ID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(StructureScannerUnit firingUnit,
												 StructureScannerCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Scanner Unit"), firingUnit);
	}


	@Override
	protected String getTag() {
		return "structurescanner";
	}

	@Override
	public StructureScannerCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<StructureScannerCollectionManager> clazz) {

		return new StructureScannerCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Structure Scanner System Collective");
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
				System.err.println("NO SCANNER MODULES");
			}
			//nothing to scan with
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
		//getPowerManager().sendNoPowerHitEffectIfNeeded(); //old power stuff
		if (debug) {
			System.err.println("FIREING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + controlledFrom);
		}
		if (!getCollectionManagers().isEmpty()) {
			StructureScannerCollectionManager m = getCollection();
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

					if (unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) || unit.isDown(KeyboardMappings.SHIP_ZOOM)) {
						if(isOnServer()) {
							if (!m.isActive() && m.isCharged()) {
								tryScan(unit.getPlayerState());
							} else if (m.isActive()) {
								m.resetCharge();
							}
						}
					} else if(!isOnServer()){
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

			if(m.isActive()){
				if(getConfigManager().apply(StatusEffectType.CARGO_SCANNER, false)){
					doCargoScan(timer);
				}
				getSegmentController().popupOwnClientMessage("SCNID", Lng.str("Scanning with strength: %s", getCollection().getTotalActiveScanStrength()), ServerMessage.MESSAGE_TYPE_INFO);
			}
		} else {

		}
		if (unpowered > 0 && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nScanner unpowered:",  unpowered), 0);
		}
		if (getCollectionManagers().isEmpty() && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Scanner!"), 0);
		}

	}

	private void doCargoScan(Timer timer) {
		AbstractOwnerState ownerState = getSegmentController().getOwnerState();
		if(ownerState != null && ownerState instanceof PlayerState p){
			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(p.getSelectedEntityId());
			if(sendable != null && sendable instanceof ManagedSegmentController<?>){
				ManagerContainer<?> man = ((ManagedSegmentController<?>)sendable).getManagerContainer();
				long diff = timer.currentTime - currentCargoScanningStart;
				if(currentCargoScanning != man.getSegmentController().getId()){
					currentCargoScanning = man.getSegmentController().getId();
					currentCargoScanningStart = timer.currentTime;
					lastScanned = 0;
				}else if(diff > CARGO_SCAN_TIME){
					if(lastScanned != man.getSegmentController().getId()){
						getSegmentController().popupOwnClientMessage("SCNIDCRGO", Lng.str("Scanning cargo of %s completed. Check your mail",man.getSegmentController()), ServerMessage.MESSAGE_TYPE_INFO);
						if(isOnServer()){
							ServerPlayerMessager msg = ((GameServerState)getState()).getServerPlayerMessager();
							StringBuilder content = new StringBuilder();
							ElementCountMap cm = new ElementCountMap();
							for(Inventory inv : man.getInventories().values()){
								inv.addToCountMap(cm);
							}

							for(short type : ElementKeyMap.typeList()){
								if(ElementKeyMap.isValidType(cm.get(type))){
									content.append(String.format("%s: %d",ElementKeyMap.getInfo(type).getName() , cm.get(type)));
									content.append("\n");
								}
							}
							msg.send(Lng.str("<system>"), p.getName(), Lng.str("Scan of %s", man.getSegmentController()), content.toString());
						}
						lastScanned = man.getSegmentController().getId();
					}
				}else{
					long left = CARGO_SCAN_TIME - diff;
					getSegmentController().popupOwnClientMessage("SCNIDCRGO", Lng.str("Scanning cargo of %s (%s left)",man.getSegmentController(), StringTools.formatTimeFromMS(left)), ServerMessage.MESSAGE_TYPE_INFO);
				}
			}
		}
	}

	public void tryScan(@Nullable PlayerState player) {
		if (!getCollectionManagers().isEmpty()) {
			StructureScannerCollectionManager m = getCollection();

			if (m.isCharged()) {
				m.setActive(true);
			} else {
				if (player != null && player.isClientOwnPlayer()) {
					((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Not charged\n%s/%s",  m.getChargeManager().getCharge(),  m.getMaxCharge()), 0);
				}
			}

		} else {
			if (player != null && player.isClientOwnPlayer()) {
				((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Scanner Modules\nconnected to entry point"), 0);
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
		StructureScannerCollectionManager ec = getCollectionManagersMap().get(controllerPos);
		if (ec != null) {
			long currentTime = System.currentTimeMillis();
			InputState state = (InputState) getState();

			 if(ec.isActive()) {
				ec.getActivationManager().drawReloads(iconPos, iconSize, state, currentTime);
			} else if(ec.getCooldownManager().isCoolingDown(currentTime)){
				 ec.getCooldownManager().drawReloads(iconPos, iconSize, state, currentTime);
		 	} else{
				ec.getChargeManager().drawReloads(iconPos, iconSize, state);
			}
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
			p += getCollectionManagers().get(i).getPowerConsumedPerSecondCharging();
		}
		return p;
	}

	@Override
	public BlockMetaDataDummy getDummyInstance() {
		return new ScannerMetaDataDummy();
	}

	@Override
	public boolean hasCollection(){
		return !getCollectionManagers().isEmpty();
	}

	@Override
	public StructureScannerCollectionManager getCollection() {
		if(!hasCollection()) return null;
		return getCollectionManagers().get(0);
	}
}
