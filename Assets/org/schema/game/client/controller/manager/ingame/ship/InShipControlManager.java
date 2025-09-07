package org.schema.game.client.controller.manager.ingame.ship;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentControllerHpControllerInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class InShipControlManager extends AbstractControlManager {

	long lastFreeCamKey;

	private ShipControllerManager shipControlManager;

	private Vector3i eneteredFromShipyard;

	public InShipControlManager(GameClientState state) {
		super(state);
		initialize();
	}

	/**
	 * @return the entered
	 */
	public SegmentPiece getEntered() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getEntered();
	}

	public void setEntered(SegmentPiece entered) {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().setEntered(entered);
	}

	public static void switchEntered(SegmentController switchTo) {
		GameClientState state = ((GameClientState) switchTo.getState());
		final PlayerInteractionControlManager iMan = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		SegmentPiece pointUnsave;
		SegmentPiece oldEntered = iMan.getEntered();
		SegmentController segmentController = oldEntered.getSegment().getSegmentController();
		Vector3i absolutePos = iMan.getEntered().getAbsolutePos(new Vector3i());
		pointUnsave = switchTo.getSegmentBuffer().getPointUnsave(Ship.core);
		if (pointUnsave != null) {
			if ((!ElementKeyMap.isValidType(pointUnsave.getType()) || !ElementKeyMap.getInfoFast(pointUnsave.getType()).isEnterable()) && !(switchTo instanceof Ship) && (switchTo instanceof ManagedSegmentController<?>)) {
				ManagerContainer<?> m = ((ManagedSegmentController<?>) switchTo).getManagerContainer();
				LongOpenHashSet buildBlocks = m.getBuildBlocks();
				if (buildBlocks.size() > 0) {
					pointUnsave = switchTo.getSegmentBuffer().getPointUnsave(buildBlocks.iterator().nextLong());
				}
			}
			if (pointUnsave != null) {
				assert (oldEntered.getSegmentController() != switchTo) : oldEntered.getSegmentController();
				iMan.getInShipControlManager().setEntered(pointUnsave);
				state.getController().requestControlChange((PlayerControllable) oldEntered.getSegmentController(), (PlayerControllable) iMan.getEntered().getSegmentController(), oldEntered.getAbsolutePos(new Vector3i()), iMan.getEntered().getAbsolutePos(new Vector3i()), true);
			}
		} else {
			state.getController().popupAlertTextMessage(Lng.str("Position to go\nisn't loaded yet."), 0);
		}
	}

	public static boolean checkEnter(SegmentPiece p) {
		if (p != null) {
			final PlayerInteractionControlManager iMan = ((GameClientState) p.getSegmentController().getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
			return iMan.getPlayerCharacterManager().checkEnter(p);
		} else {
			return false;
		}
	}

	public static boolean checkEnterDry(SegmentPiece p, boolean verbose) {
		if (p != null) {
			final PlayerInteractionControlManager iMan = ((GameClientState) p.getSegmentController().getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
			return iMan.getPlayerCharacterManager().checkEnterDry(p, verbose);
		} else {
			return false;
		}
	}

	public static boolean checkEnter(SegmentController switchTo) {
		final PlayerInteractionControlManager iMan = ((GameClientState) switchTo.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		SegmentPiece pointUnsave;
		SegmentPiece oldEntered = iMan.getEntered();
		SegmentController segmentController = oldEntered.getSegment().getSegmentController();
		Vector3i absolutePos = iMan.getEntered().getAbsolutePos(new Vector3i());
		pointUnsave = switchTo.getSegmentBuffer().getPointUnsave(Ship.core);
		if (pointUnsave != null) {
			if ((!ElementKeyMap.isValidType(pointUnsave.getType()) || !ElementKeyMap.getInfoFast(pointUnsave.getType()).isEnterable()) && !(switchTo instanceof Ship) && (switchTo instanceof ManagedSegmentController<?>)) {
				ManagerContainer<?> m = ((ManagedSegmentController<?>) switchTo).getManagerContainer();
				LongOpenHashSet buildBlocks = m.getBuildBlocks();
				if (buildBlocks.size() > 0) {
					pointUnsave = switchTo.getSegmentBuffer().getPointUnsave(buildBlocks.iterator().nextLong());
				// System.err.println("AA CHECK: "+pointUnsave+"; "+pointUnsave.getSegmentController());
				}
			}
			if (pointUnsave != null && ElementKeyMap.isValidType(pointUnsave.getType()) && ElementKeyMap.getInfoFast(pointUnsave.getType()).isEnterable()) {
				return checkEnterDry(pointUnsave, true);
			}
		}
		return false;
	}

	public void exitShip(boolean forced) {
		if (getEntered() != null) {
			PlayerInteractionControlManager i = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
			if (i.getBuildToolsManager().getBuildHelper() != null) {
				i.getBuildToolsManager().getBuildHelper().clean();
				i.getBuildToolsManager().setBuildHelper(null);
			}
			final SegmentController segmentController = getEntered().getSegment().getSegmentController();
			final PlayerControllable pc;
			final Vector3i toPos = new Vector3i();
			final Vector3i absolutePos = getEntered().getAbsolutePos(new Vector3i());
			if (eneteredFromShipyard != null) {
				// toPos.set(eneteredFromShipyard);
				eneteredFromShipyard = null;
			}
			pc = ((PlayerControllable) segmentController);
			if (!forced &&  EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() >= 0 && segmentController.getSpeedPercentServerLimitCurrent() >=  EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() * 0.01f) {
				PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM_Exit", getState(), "Exit", "Do you really want to do that.\nThe current object was flying at " + StringTools.formatPointZero(segmentController.getSpeedCurrent()) + " speed\n\n(this message can be customized or\nturned off in the game option\n'Popup Detach Warning' in the ingame options menu)") {

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void pressedOK() {
						if (getEntered() != null) {
							System.err.println("[CLIENT] EXIT SHIP FROM EXTRYPOINT " + absolutePos + " OF " + segmentController + "; segSec: " + segmentController.getSectorId() + "; ");
							getState().getController().requestControlChange(pc, getState().getCharacter(), absolutePos, toPos, true);
							setEntered(null);
							setActive(false);
						}
						deactivate();
					}
				};
				c.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(181);
			} else {
				System.err.println("[CLIENT] EXIT SHIP FROM EXTRYPOINT " + absolutePos + " OF " + segmentController + "; segSec: " + segmentController.getSectorId() + "; ");
				getState().getController().requestControlChange(pc, getState().getCharacter(), absolutePos, toPos, true);
				setEntered(null);
				setActive(false);
			}
		}
	}

	/**
	 * @return the shipControlManager
	 */
	public ShipControllerManager getShipControlManager() {
		return shipControlManager;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
		if (e.isTriggered(KeyboardMappings.REBOOT_SYSTEMS)) {
			if (getState().getShip() != null) {
				popupShipRebootDialog(getState().getShip());
			}
		}
		if (e.isTriggered(KeyboardMappings.FREE_CAM)) {
			if (!Controller.FREE_CAM_STICKY) {
				if (System.currentTimeMillis() - lastFreeCamKey < 300) {
					Controller.FREE_CAM_STICKY = true;
					getState().getController().popupInfoTextMessage(Lng.str("Sticky free cam active:\nhit %s again to deactivate.", KeyboardMappings.FREE_CAM.getKeyChar()), 0);
				} else {
					lastFreeCamKey = System.currentTimeMillis();
				}
			} else {
				Controller.FREE_CAM_STICKY = false;
			}
		}
		if (e.isTriggered(KeyboardMappings.ENTER_SHIP)) {
			if (e.isTriggered(KeyboardMappings.ENTER_SHIP) && e.isTriggered(KeyboardMappings.ACTIVATE) && shipControlManager.getSegmentBuildController().isActive()) {
				SegmentPiece cp = shipControlManager.getSegmentBuildController().getCurrentSegmentPiece();
				if (cp != null && ElementKeyMap.isValidType(cp.getType()) && (ElementKeyMap.getInfo(cp.getType()).canActivate() || cp.getType() == ElementKeyMap.POWER_BATTERY || cp.getType() == ElementKeyMap.POWER_ID_OLD || cp.getType() == ElementKeyMap.POWER_CAP_ID || cp.getType() == ElementKeyMap.SHIELD_CAP_ID || cp.getType() == ElementKeyMap.SHIELD_REGEN_ID) && !ElementKeyMap.getInfo(cp.getType()).isEnterable()) {
					System.err.println("[CLIENT] CHECKING ACTIVATE OF " + cp + " FROM INSIDE BUILD MODE");
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().checkActivate(cp);
				} else {
					exitShip(false);
				}
			} else {
				exitShip(false);
			}
		} else if (e.isTriggered(KeyboardMappings.ACTIVATE)) {
			if (shipControlManager.getSegmentBuildController().isActive()) {
				SegmentPiece cp = shipControlManager.getSegmentBuildController().getCurrentSegmentPiece();
				if (cp != null) {
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().checkActivate(cp);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch()
	 */
	@Override
	public void onSwitch(boolean active) {
		// 
		// try{
		// throw new NullPointerException("INSHIP: "+active+" / "+isActive());
		// }catch(NullPointerException e){
		// e.printStackTrace();
		// }
		if (active && getEntered() != null) {
			Ship s = (Ship) getEntered().getSegmentController();
			getState().setShip(s);
			assert (getEntered() != null);
			// PositionControl controlledElements = s.getControlElementMap().getControlledElements(Element.TYPE_ALL, getEntered().getAbsolutePos(new Vector3i()));
			// getState().getPlayer().getPlayerKeyConfigurationManager().addDefaultConfigIfNotExistsClient(s, getEntered());
			shipControlManager.setActive(true);
			SegmentController segmentController = getEntered().getSegmentController();
			Vector3i absolutePos = getEntered().getAbsolutePos(new Vector3i());
		// System.err.println("ENTER ON ENTRYPOINT: "+absolutePos);
		} else {
			// getState().getPlayer().requestControlRelease(getState().getShip(), getEntered().getAbsolutePos(new Vector3i()), false );
			// System.err.println("[INSHIP] EXIT setting ship to null. "+getState());
			if (getState().getShip() == getState().getCurrentPlayerObject()) {
				getState().setCurrentPlayerObject(null);
			}
			// if(getState().getShip() != null){
			// getState().getShip().getControlElementMap().deleteObserver(getShipControlManager().getSegmentBuildController());
			// }
			getState().setShip(null);
		}
		// /player_put_into_entity_uid schema ENTITY_SHIP_m
		assert (!active || getState().getShip() != null) : ": Entered: " + (getEntered() != null ? getEntered().getSegment().getSegmentController() : "null") + " -> " + (getEntered() != null ? getEntered().getAbsolutePos(new Vector3i()) : "null");
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		if (getEntered() == null) {
			System.err.println("[CLIENT] CANNOT UPDATE inShipControlManager: entered is null");
			return;
		}
		if (!getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(getEntered().getSegment().getSegmentController().getId())) {
			System.err.println("[CLIENT][InShipControlManager] Entered object no longer exists");
			exitShip(true);
			setActive(false);
			return;
		}
		if (getEntered() != null) {
			getEntered().refresh();
			if (getEntered().getSegment().getSegmentController().getSegmentBuffer().containsKey(getEntered().getSegment().pos)) {
				if (((RemoteSegment) getEntered().getSegment()).getLastChanged() > 0 && getEntered().getType() == Element.TYPE_NONE) {
					Vector3i absolutePos = getEntered().getAbsolutePos(new Vector3i());
					SegmentPiece pointUnsave = getEntered().getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(absolutePos);
					if (pointUnsave != null) {
						if (pointUnsave.getType() == Element.TYPE_NONE) {
							System.err.println("[CLIENT][InShipControlManager] POINT BECAME AIR -> exiting ship; lastChanged: " + ((RemoteSegment) getEntered().getSegment()).getLastChanged());
							exitShip(true);
							setActive(false);
							return;
						} else {
							setEntered(pointUnsave);
						}
					}
				}
			}
			if (!getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(getEntered().getSegment().getSegmentController().getId())) {
				exitShip(true);
				setActive(false);
				return;
			}
		}
	}

	public void initialize() {
		shipControlManager = (new ShipControllerManager(getState()));
		getControlManagers().add(shipControlManager);
	}

	public void popupShipRebootDialog(final SegmentController c) {
		final SegmentControllerHpControllerInterface hpController = c.getHpController();
		if (!hpController.isRebooting()) {
			hpController.setRequestedTimeClient(false);
			Object desc = new Object() {

				@Override
				public String toString() {
					String t = "calculating...";
					if (hpController.getRebootTimeMS() > 0) {
						t = StringTools.formatTimeFromMS(hpController.getRebootTimeMS());
					}
					if (c instanceof ManagedSegmentController<?>) {
						PowerInterface p = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
						if (p.isUsingPowerReactors()) {
							float rb = Math.max(p.getReactorRebootCooldown(), p.getReactorSwitchCooldown());
							if (rb > 0) {
								t = StringTools.formatTimeFromMS((long) (rb * 1000f));
							} else {
								t = Lng.str("instant");
							}
						}
					}
					String desc = Lng.str("Do you want to reboot your systems?\nThis will remove any system failures from low hp\n") + "and it will set the HP according to your current blocks.\n\n" + ((c instanceof SpaceStation) ? Lng.str("Rebooting will also repair your armor on stations.\n\n") : "") + Lng.str("The reboot time depends on size and damage.\nRebooting possible in: ") + t;
					return desc;
				}
			};
			PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Reboot"), desc) {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
					float time = 0;
					if (c instanceof ManagedSegmentController<?>) {
						PowerInterface p = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
						time = Math.max(p.getReactorRebootCooldown(), p.getReactorSwitchCooldown());
					}
					if (c instanceof SegmentController && time <= 0) {
						c.getHpController().reboot(false);
						deactivate();
					}
				}
			};
			check.getInputPanel().getButtonOK().setText(new Object() {

				@Override
				public String toString() {
					getState().getController();
					float time = 0;
					if (c instanceof ManagedSegmentController<?>) {
						PowerInterface p = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
						time = Math.max(p.getReactorRebootCooldown(), p.getReactorSwitchCooldown());
					}
					if (time <= 0) {
						return Lng.str("OK");
					} else {
						getState().getController();
						return Lng.str("WAIT ") + "(" + (int) Math.ceil(time) + ")";
					}
				}
			});
			check.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(182);
		}
	}

	public void popupShipRebootBuyDialog() {
		SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
		if (currentPlayerObject instanceof SegmentController && !((SegmentController) currentPlayerObject).getHpController().isRebooting()) {
			final SegmentControllerHpControllerInterface hpController = ((SegmentController) currentPlayerObject).getHpController();
			hpController.setRequestedTimeClient(false);
			Object desc = new Object() {

				@Override
				public String toString() {
					String t = Lng.str("calculating...");
					if (hpController.getRebootTimeMS() > 0) {
						t = StringTools.formatTimeFromMS(hpController.getRebootTimeMS());
					}
					SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
					if (currentPlayerObject instanceof ManagedSegmentController<?>) {
						PowerInterface p = ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer().getPowerInterface();
						if (p.isUsingPowerReactors()) {
							if (p.isUsingPowerReactors()) {
								float rb = Math.max(p.getReactorRebootCooldown(), p.getReactorSwitchCooldown());
								if (rb > 0) {
									t = StringTools.formatTimeFromMS((long) (rb * 1000f));
								} else {
									t = Lng.str("instant");
								}
							}
						}
					}
					String desc = Lng.str("Do you want to reboot your systems?\n" + "This will remove any system failures from low hp\n" + "and it will set the HP according to your current blocks.\n" + "This will NOT replenish or repair any blocks.\n" + "Rebooting will be instant, but it will cost %s Credits\n" + "You currently have %s Credits", hpController.getShopRebootCost(), getState().getPlayer().getCredits());
					return desc;
				}
			};
			PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Reboot"), desc) {

				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
					SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
					if (currentPlayerObject instanceof SegmentController && !((SegmentController) currentPlayerObject).getHpController().isRebooting()) {
						((SegmentController) currentPlayerObject).getHpController().reboot(true);
						deactivate();
					} else {
						System.err.println("REBOOTING ALREADY!");
					}
				}
			};
			check.getInputPanel().getButtonOK().setText(new Object() {

				@Override
				public String toString() {
					getState().getController();
					float time = 0;
					SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
					if (currentPlayerObject instanceof ManagedSegmentController<?>) {
						PowerInterface p = ((ManagedSegmentController<?>) currentPlayerObject).getManagerContainer().getPowerInterface();
						time = Math.max(p.getReactorRebootCooldown(), p.getReactorSwitchCooldown());
					}
					if (time <= 0) {
						return Lng.str("OK");
					} else {
						getState().getController();
						return Lng.str("WAIT ") + "(" + (int) Math.ceil(time) + ")";
					}
				}
			});
			check.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(183);
		}
	}

	// public void popupShipArmorRepairBuyDialog() {
	// SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
	// if (currentPlayerObject instanceof SegmentController && !((SegmentController) currentPlayerObject).getHpController().isRebooting()) {
	// 
	// final SegmentControllerHpControllerInterface hpController = ((SegmentController) currentPlayerObject).getHpController();
	// 
	// hpController.setRequestedTimeClient(false);
	// 
	// Object desc = new Object() {
	// @Override
	// public String toString() {
	// 
	// String t = Lng.str("calculating...");
	// 
	// if (hpController.getRebootTimeMS() > 0) {
	// t = StringTools.formatTimeFromMS(hpController.getRebootTimeMS());
	// }
	// 
	// String desc = Lng.str("Do you want to repair your Armor?\n" +
	// "This will reset all your armor HP to their max value!\n"
	// + "This will NOT replenish or repair any blocks.\n\n" +
	// "Repairing will be instant, but it will cost %s Credits\n" +
	// "You currently have %s Credits", hpController.getShopArmorRepairCost(), getState().getPlayer().getCredits());
	// return desc;
	// }
	// };
	// 
	// PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(),
	// Lng.str("Repair Armor"), desc) {
	// 
	// @Override
	// public boolean isOccluded() {
	// return false;
	// }
	// 
	// @Override
	// public void onDeactivate() {
	// }
	// 
	// @Override
	// public void pressedOK() {
	// 
	// 
	// SimpleTransformableSendableObject currentPlayerObject = getState().getCurrentPlayerObject();
	// if (currentPlayerObject instanceof SegmentController && !((SegmentController) currentPlayerObject).getHpController().isRebooting()) {
	// getState().getController()
	// .queueUIAudio("0022_action - buttons push medium");
	// ((SegmentController) currentPlayerObject).getHpController().repairArmor(true);
	// }
	// deactivate();
	// 
	// }
	// };
	// check.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	// }
	// }
	public void enteredFromShipyard(Vector3i sh) {
		eneteredFromShipyard = sh;
	}
}
