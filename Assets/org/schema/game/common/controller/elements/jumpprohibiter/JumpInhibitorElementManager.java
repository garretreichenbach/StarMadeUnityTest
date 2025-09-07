package org.schema.game.common.controller.elements.jumpprohibiter;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.NetworkObject;

import java.io.IOException;

public class JumpInhibitorElementManager extends UsableControllableFiringElementManager<JumpInhibitorUnit, JumpInhibitorCollectionManager, JumpInhibitorElementManager> implements NTSenderInterface, NTReceiveInterface, TagModuleUsableInterface, BlockActivationListenerInterface {

	public static final String TAG_ID = "JP";

	@ConfigurationElement(name = "DischargePerSecond")
	public static float DISCHARGE_PER_SECOND = 10;
	@ConfigurationElement(name = "DischargePerSecondPerBlock")
	public static float DISCHARGE_PER_SECOND_PER_BLOCK = 10;

	@ConfigurationElement(name = "DischargeFriendlyShips")
	public static boolean DISCHARGE_FRIENDLY_SHIPS = true;
	@ConfigurationElement(name = "DischargeSelf")
	public static boolean DISCHARGE_SELF = true;

	@ConfigurationElement(name = "InterdictionSectorRadiusBase", description = "Base radius of the interdiction AOE in sectors, can be upgraded.")
	public static float INTERDICTION_SECTOR_RADIUS_BASE = 10.0f;

	@ConfigurationElement(name = "ReactorPowerConsumptionPerSecondBase")
	public static float POWER_CONSUMPTION_BASE = 10;
	@ConfigurationElement(name = "ReactorPowerConsumptionRestingPerBlock")
	public static float REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING = 10;
	@ConfigurationElement(name = "ReactorPowerConsumptionChargingPerBlock")
	public static float REACTOR_POWER_CONSUMPTION_PER_BLOCK_CHARGING = 10;

	public static boolean debug;
	private final Vector3i controlledFromOrig = new Vector3i();
	private final Vector3i controlledFrom = new Vector3i();

	private long lastAct;

	public JumpInhibitorElementManager(SegmentController segmentController) {
		super(ElementKeyMap.JUMP_INHIBITOR_COMPUTER, ElementKeyMap.JUMP_INHIBITOR_MODULE, segmentController);
	}

	@Override
	public String getTagId() {
		return TAG_ID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(JumpInhibitorUnit firingUnit, JumpInhibitorCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Jump Inhibitor Unit"), firingUnit);
	}


	@Override
	protected String getTag() {
		return "jumpinhibitor";
	}

	@Override
	public JumpInhibitorCollectionManager getNewCollectionManager(SegmentPiece position, Class<JumpInhibitorCollectionManager> clazz) {

		return new JumpInhibitorCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Jump Inhibitor System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		long curTime = System.currentTimeMillis();
		if(!unit.isFlightControllerActive()) {
			if(debug) {
				System.err.println("NOT ACTIVE");
			}
			return;
		}
		if(getCollectionManagers().isEmpty()) {
			if(debug) {
				System.err.println("NO WEAPONS");
			}
			//nothing to activate
			return;
		}
		try {
			if(!convertDeligateControls(unit, controlledFromOrig, controlledFrom)) {
				if(debug) {
					System.err.println("NO SLOT");
				}
				return;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}

		if(debug) {
			System.err.println("FIREING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + controlledFrom);
		}
		for(int i = 0; i < getCollectionManagers().size(); i++) {
			JumpInhibitorCollectionManager m = getCollectionManagers().get(i);
			if(unit.isSelected(m.getControllerElement(), controlledFrom) && unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE)) {
				boolean controlling = controlledFromOrig.equals(controlledFrom);
				controlling |= getControlElementMap().isControlling(controlledFromOrig, m.getControllerPos(), controllerId);
				if(debug) {
					System.err.println("Controlling " + controlling + " " + getState());
				}

				if(controlling) {
					if(!m.allowedOnServerLimit()) {
						continue;
					}
					if(getSegmentController().isOnServer()) {
						if(m.isActive() || !m.getCooldownManager().isCoolingDown(timer)) { //cooldown only applies for activation; system can be turned off whenever
							m.toggleActivation();
							m.sendActiveUpdate();
							lastAct = System.currentTimeMillis();
						}
					}

				}
			}
		}
		if(getCollectionManagers().isEmpty() && clientIsOwnShip()) {
			((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo controllers"), 0);
		}

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
		if(getSegmentController().isOnServer()) {

		}
	}


	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		for(JumpInhibitorCollectionManager e : getCollectionManagers()) {
			if(e.isActive()) {
				drawReload((InputState) getState(), iconPos, iconSize, reloadColor, false, 1);
			} else {
				long t = System.currentTimeMillis();
				if(e.getCooldownManager().isCoolingDown(t)) {
					e.getCooldownManager().drawReloads(iconPos, iconSize, (InputState) getState(), t);
				}
			}
		}
	}

	@Override
	public BlockMetaDataDummy getDummyInstance() {
		return new JumpInhibitorMetaDataDummy();
	}

}
