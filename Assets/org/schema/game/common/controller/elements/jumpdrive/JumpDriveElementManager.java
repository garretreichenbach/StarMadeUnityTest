package org.schema.game.common.controller.elements.jumpdrive;

import java.io.IOException;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.server.ServerMessage;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class JumpDriveElementManager extends UsableControllableFiringElementManager<JumpDriveUnit, JumpDriveCollectionManager, JumpDriveElementManager> implements
		NTSenderInterface, NTReceiveInterface, TagModuleUsableInterface, BlockActivationListenerInterface, ManagerReloadInterface, HittableInterface, SingleElementCollectionContainerInterface<JumpDriveCollectionManager> {

	public final static String TAG_ID = "J";
	@ConfigurationElement(name = "ChargeNeededForJumpAdditive")
	public static float CHARGE_NEEDED_FOR_JUMP_FIX = 1000;
	@ConfigurationElement(name = "ChargeNeededForJumpPerReactorLevel")
	public static float CHARGE_NEEDED_FOR_JUMP_PER_REACTOR_LEVEL = 100;
	@ConfigurationElement(name = "ChargeNeededForJumpPerMassLinear")
	public static float CHARGE_NEEDED_FOR_JUMP_PER_MASS_LINEAR = 100;

	@ConfigurationElement(name = "BaseChargeAddedPerSecond")
	public static float BASE_CHARGE_RATE_SEC = 10;
	@ConfigurationElement(name = "ChargeAddedPerBlockPerSecond")
	public static float CHARGE_ADDED_PER_SECOND_PER_BLOCK = 100;

	@ConfigurationElement(name = "ChargeUpkeepPowerConsumedBase", description = "Base power consumed per added jump drive system. If power supply falls below this rate, drive will discharge.")
	public static float RESTING_POWER_CONSUMPTION_BASE;
	@ConfigurationElement(name = "ChargeUpkeepPowerConsumedPerBlock", description = "Base power consumed per added jump drive block. If power supply falls below this rate, drive will discharge.")
	public static float RESTING_POWER_CONSUMPTION_PER_BLOCK;
	@ConfigurationElement(name = "ExtraPowerConsumedPerSecondChargingBase", description = "Base power required to charge a jump drive system.")
	public static float CHARGING_POWER_CONSUMPTION_BASE;
	@ConfigurationElement(name = "ExtraPowerConsumedPerSecondChargingPerBlock", description = "Charging power consumption added per block.")
	public static float CHARGING_POWER_CONSUMPTION_PER_BLOCK;

	@ConfigurationElement(name = "MinimumChargeTimeSeconds")
	public static float MINIMUM_CHARGE_TIME_SEC = 2;
	@ConfigurationElement(name = "ReloadMsAfterUseMs")
	public static long RELOAD_AFTER_USE_MS = 1000;
	@ConfigurationElement(name = "DistanceInSectors")
	public static int BASE_DISTANCE_SECTORS = 8;
	
	public static boolean debug = false;
	private Vector3i controlledFromOrig = new Vector3i();
	private Vector3i controlledFrom = new Vector3i();
	private long lastJump;
	public JumpDriveElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.JUMP_DRIVE_CONTROLLER, ElementKeyMap.JUMP_DRIVE_MODULE, segmentController);
	}

	@Override
	public String getTagId() {
		return TAG_ID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(JumpDriveUnit firingUnit,
	                                             JumpDriveCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Jump Drive Unit"), firingUnit);
	}


	@Override
	protected String getTag() {
		return "jumpdrive";
	}

	@Override
	public JumpDriveCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<JumpDriveCollectionManager> clazz) {

		return new JumpDriveCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Jump Drive System Collective");
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
		if (debug) {
			System.err.println("FIREING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + controlledFrom);
		}
		for (int i = 0; i < getCollectionManagers().size(); i++) {
			JumpDriveCollectionManager m = getCollectionManagers().get(i);
			if (unit.isSelected(m.getControllerElement(), controlledFrom)) {
				boolean controlling = controlledFromOrig.equals(controlledFrom);
				controlling |= getControlElementMap().isControlling(controlledFromOrig, m.getControllerPos(), controllerId);
				if (debug) {
					System.err.println("Controlling " + controlling + " " + getState());
				}

				if (controlling) {
					if(!m.allowedOnServerLimit()){
						continue;
					}
					if (controlledFromOrig.equals(Ship.core)) {
						unit.getControlledFrom(controlledFromOrig);
					}
					
					
					long timeSinceJump = System.currentTimeMillis() - lastJump;

					if (timeSinceJump > RELOAD_AFTER_USE_MS) {
						boolean chargeKeyDown = unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE);
						m.handleManualChargeStatus(chargeKeyDown); //can do this every update, no significant overhead
						if (chargeKeyDown) {
							if (m.getCooldownManager().isCoolingDown(timer)) {
								long diff = (RELOAD_AFTER_USE_MS - (timer.currentTime - m.getActivationManager().getLastActivation())) / 1000L;
								getSegmentController().popupOwnClientMessage("Cannot charge!\nJump Drive on Cooldown!\n("+diff+" secs)", ServerMessage.MESSAGE_TYPE_ERROR);
							} else if(m.hasChargeAvailable()){
								getSegmentController().popupOwnClientMessage(Lng.str("Jump Drive Charged!\nRight click to jump!"), ServerMessage.MESSAGE_TYPE_INFO);
							}
							//m.charge(timer); //Flagging manual charging already charges every update. Direct charging via the handling method allowed logic fast-charge exploits.
						}

						if (unit.isDown(KeyboardMappings.SHIP_ZOOM)) {
							
							if (m.hasChargeAvailable() && !m.isActive()) {
								if (getSegmentController().getDockingController().isDocked() || getSegmentController().railController.isDockedOrDirty()) {
									getSegmentController().popupOwnClientMessage(Lng.str("Cannot jump!\nShip is docked!"), ServerMessage.MESSAGE_TYPE_ERROR);
								} else {
									if (getSegmentController().getPhysicsDataContainer().getObject() != null) {
										System.err.println("[JUMPDRIVE] JUMPING " + getSegmentController().getState() + "; " + getSegmentController());
										//do jump
										m.getActivationManager().setActive(true,timer);
										lastJump = System.currentTimeMillis();
									} else {
										getSegmentController().sendControllingPlayersServerMessage(Lng.astr("ERROR\nPhysical Object not found!"), 0);
									}
								}
							}
							else if(m.isActive()){
								getSegmentController().popupOwnClientMessage(Lng.str("Jump Drive already engaged!"), ServerMessage.MESSAGE_TYPE_INFO);
							}
							else if (!m.hasChargeAvailable()) {
								getSegmentController().popupOwnClientMessage(Lng.str("Jump Drive not fully charged\nHold left mouse to charge!"), ServerMessage.MESSAGE_TYPE_INFO);
							}
						}
					} else {
						if (timeSinceJump > 500) {
							long diff = (RELOAD_AFTER_USE_MS - timeSinceJump) / 1000L;
							getSegmentController().popupOwnClientMessage(Lng.str("Cannot charge!\nJump Drive on Cooldown!\n(%d secs)", diff), ServerMessage.MESSAGE_TYPE_ERROR);
						}
					}

					if (m.getElementCollections().isEmpty() && clientIsOwnShip()) {

						((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Jump Drive Modules\nconnected to entry point."), 0);
					}
				}
			}
		}
		if (getCollectionManagers().isEmpty() && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo jump drive controllers."), 0);
		}

	}

	@Override
	public BlockMetaDataDummy getDummyInstance(){
		return new JumpDriveMetaDataDummy();
	}
	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.WEAPON_ID);
	}

	@Override
	public void updateFromNT(NetworkObject o) {
	}

	@Override
	public String getReloadStatus(long id) {
		return Lng.str("STANDBY");
	}
	@Override
	public void updateToFullNT(NetworkObject networkObject) {
		if (getSegmentController().isOnServer()) {

		}
	}

	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		JumpDriveCollectionManager ec = getCollectionManagersMap().get(controllerPos);
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
	public int getMaxCharges() {
		return getConfigManager().apply(StatusEffectType.JUMP_MULTI_CHARGE_COUNT,1);
	}

	@Override
	public void onHit(long pos, short type, double damage, DamageDealerType damageType) {
		if (getSegmentController().isOnServer()) {
			for (int i = 0; i < getCollectionManagers().size(); i++) {
				getCollectionManagers().get(i).onHit(damage, damageType);
			}
		}
	}

	@Override
	public boolean hasCollection() {
		return !getCollectionManagers().isEmpty();
	}

	@Override
	public JumpDriveCollectionManager getCollection() {
		if(hasCollection()){
			return getCollectionManagers().get(0);
		} else return null;
	}

	public long getLastActivation() {
		return lastJump;
	}

	public void onJumpComplete() {
		if(hasCollection()){
			getCollection().getActivationManager().setActive(false);
			getCollection().getChargeManager().consumeFullCharge(1); //this will prompt a charge update and set the drive inactive on client.
			//(a bit spaghetti, but avoids more packet traffic for synchronization)
		}
	}
}
