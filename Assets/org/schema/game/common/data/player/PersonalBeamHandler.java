package org.schema.game.common.data.player;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.HealingBeamHitListener;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.beam.BeamColors;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.Salvage;
import org.schema.game.common.controller.Salvager;
import org.schema.game.common.controller.SegmentBufferInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.damage.beam.DamageBeamHittable;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseIndex;
import org.schema.game.common.controller.elements.ActivationManagerInterface;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.racegate.RacegateCollectionManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.AbstractBeamHandler;
import org.schema.game.common.data.element.beam.BeamLatchTransitionInterface;
import org.schema.game.common.data.element.beam.DefaultLatchTransitionInterface;
import org.schema.game.common.data.element.meta.weapon.MarkerBeam;
import org.schema.game.common.data.element.meta.weapon.TransporterBeaconBeam;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;

import com.bulletphysics.linearmath.Transform;

public class PersonalBeamHandler<E extends AbstractOwnerState> extends AbstractBeamHandler<AbstractCharacter<E>> {
	public static final int SALVAGE = 0;
	public static final int HEAL = 1;
	public static final int POWER_SUPPLY = 2;
	public static final int MARKER = 3;
	public static final int SNIPER = 4;
	public static final int GRAPPLE = 5;
	public static final int TORCH = 6;
	public static final int TRANSPORTER_MARKER = 7;
	private static final float BEAM_TIMEOUT_IN_SECS = 0.001f;
	public static Vector4f salvageColor = getColorRange(BeamColors.WHITE);
	public static Vector4f healColor = getColorRange(BeamColors.GREEN);
	public static Vector4f powerSupplyColor = getColorRange(BeamColors.YELLOW);
	public static Vector4f markerColor = getColorRange(BeamColors.WHITE);

	private final BeamLatchTransitionInterface beamLatchTransitionInterface = new DefaultLatchTransitionInterface();
	public PersonalBeamHandler(AbstractCharacter<E> fireree,
	                           BeamHandlerContainer<AbstractCharacter<E>> owner) {
		super(owner, fireree);

	}

	protected boolean isDamagingMines(BeamState hittingBeam) {
		return hittingBeam.beamType == SNIPER;
	}
	@Override
	public boolean canhit(BeamState con, SegmentController controller, String[] cannotHitReason, Vector3i position) {

		if (controller.equals(getBeamShooter())) {
			return false;
		}
		if (con.beamType == SALVAGE) {
			if (!(controller instanceof Salvage)) {
				cannotHitReason[0] = "Object cannot be hit by this";
				return false;
			}

			if ((!(controller instanceof Ship) || !((Ship) controller).isCoreOverheating()) && controller.getFactionId() != getBeamShooter().getFactionId() && controller.isInExitingFaction()) {
				cannotHitReason[0] = "Faction access denied (" + controller.getFactionId() + "/" + getBeamShooter().getFactionId() + ")";
				return false;
			}
			if (!((Salvage) controller).isSalvagableFor(getBeamShooter(), cannotHitReason, position)) {
				if (cannotHitReason[0] == null || cannotHitReason[0].length() == 0) {
					cannotHitReason[0] = "Object can't be hit\nby this player";
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public float getBeamTimeoutInSecs() {
		return BEAM_TIMEOUT_IN_SECS;
	}

	@Override
	public float getBeamToHitInSecs(BeamState beamState) {
		return beamState.getTickRate();
	}

	@Override
	public int onBeamHit(BeamState hittingBeam, int beamHits, BeamHandlerContainer<AbstractCharacter<E>> container, SegmentPiece hitPiece, Vector3f from,
	                     Vector3f to, Timer timer, Collection<Segment> updatedSegments) {

		if (hittingBeam.beamType == SALVAGE) {
			beamHits = ((Salvager) getBeamShooter()).handleSalvage(hittingBeam, beamHits, container, from, hitPiece, timer, updatedSegments);
			((Salvage) hitPiece.getSegmentController()).handleBeingSalvaged(hittingBeam, container, to, hitPiece, beamHits);
		} else if (hittingBeam.beamType == HEAL) {
//			((EditableSendableSegmentController) segment.getSegmentController()).handleRepair(hittingBeam, beamHits, container, from, cubeResult, timer);
//			beamHits = 1;
//			throw new RuntimeException("TODO");

			if (hitPiece.getSegmentController().isClientOwnObject() && ((GameClientState) hitPiece.getSegmentController().getState()).getWorldDrawer() != null) {
				((GameClientState) hitPiece.getSegmentController().getState()).getWorldDrawer().getGuiDrawer().notifyEffectHit(hitPiece.getSegmentController(), OffensiveEffects.REPAIR);
			}

			//INSERTED CODE
			boolean abort = false;
			for (HealingBeamHitListener listener : FastListenerCommon.healingBeamHitListeners)
				abort |= listener.handle(hittingBeam, beamHits, container, hitPiece, from, to, timer, updatedSegments);
			if (abort) return beamHits;
			///

			if(getBeamShooter().isOnServer()) {
				final int maxHP = hitPiece.getInfo().getMaxHitPointsByte();
				if(hitPiece.getHitpointsByte() < maxHP) {
					hitPiece.setHitpointsByte(maxHP);
					hitPiece.applyToSegment(getBeamShooter().isOnServer());
				}
			}
			return beamHits;
		} else if (hittingBeam.beamType == SNIPER) {
//			((EditableSendableSegmentController) segment.getSegmentController()).handleBeamDamage(hittingBeam, beamHits, getBeamShooter(), from, to, cubeResult, false, timer);
//			beamHits = 1;
//			throw new RuntimeException("TODO");
			DamageBeamHitHandler ddb = ((DamageBeamHittable) hitPiece.getSegmentController()).getDamageBeamHitHandler();
			if(ddb instanceof DamageBeamHitHandlerSegmentController) {
				beamHits = ((DamageBeamHitHandlerSegmentController)ddb).onBeamDamage(hittingBeam, beamHits, container, hitPiece, from,
		                to, timer, updatedSegments);
			}
			ddb.reset();
			hitPiece.refresh();
			return beamHits;
		} else if (hittingBeam.beamType == TORCH) {
//			((EditableSendableSegmentController) segment.getSegmentController()).handleBeamDamage(hittingBeam, beamHits, getBeamShooter(), from, to, cubeResult, true, timer);
//
//			if (segment.getSegmentController().isClientOwnObject()) {
//				if (segment.getSegmentController() instanceof Ship) {
//					((GameClientState) segment.getSegmentController().getState()).getController()
//							.popupAlertTextMessage(Lng.str("WARNING!\nIntruders on structure\ndetected!\nYou are being boarded!"), 0);
//				} else {
//					((GameClientState) segment.getSegmentController().getState()).getController()
//							.popupAlertTextMessage(Lng.str("WARNING!\nIntruders on structure\ndetected!"), 0);
//				}
//			}
			DamageBeamHitHandler ddb = ((DamageBeamHittable) hitPiece.getSegmentController()).getDamageBeamHitHandler();
			if(ddb instanceof DamageBeamHitHandlerSegmentController) {
				beamHits = ((DamageBeamHitHandlerSegmentController)ddb).onBeamDamage(hittingBeam, beamHits, container, hitPiece, from,
		                to, timer, updatedSegments);
			}
			/*
			 * throw player out of core if core was killed by torch
			 */
			if (hitPiece.getSegmentController().isOnServer() &&
					hitPiece.getSegmentController() instanceof Ship) {
				Vector3i absoluteElemPos = hitPiece.getAbsolutePos(new Vector3i());
				SegmentBufferInterface sb = ((EditableSendableSegmentController) hitPiece.getSegmentController()).getSegmentBuffer();
				SegmentPiece p;
				if (Ship.core.equals(absoluteElemPos) &&
						((p = sb.getPointUnsave(absoluteElemPos)) != null) && p.isDead()) {//autorequest true previously
					List<PlayerState> attachedPlayers = ((PlayerControllable) (hitPiece.getSegmentController())).getAttachedPlayers();
					for (PlayerState s : attachedPlayers) {
						for (ControllerStateUnit u : s.getControllerState().getUnits()) {
							if (u.parameter != null && u.parameter.equals(absoluteElemPos)) {
								s.getControllerState().forcePlayerOutOfSegmentControllers();
							}
						}
					}
				}
			}
			
			beamHits = 1;
			return beamHits;
		} else if (hittingBeam.beamType == GRAPPLE) {

			if (!getBeamShooter().isOnServer()) {
				SegmentController align = hitPiece.getSegmentController();
				getBeamShooter().scheduleGravity(new Vector3f(0, 0, 0), align);
				((GameClientState) getBeamShooter().getState()).getController().popupInfoTextMessage(Lng.str("Grappled to\n%s",  align.toNiceString()), 0);
			}
			getBeamShooter().getGravity().grappleStart = System.currentTimeMillis();
			beamHits = 1;
		} else if (hittingBeam.beamType == TRANSPORTER_MARKER) {

			if (beamHits > 0) {
				boolean ok = true;
				boolean marking = hitPiece.getType() == ElementKeyMap.TRANSPORTER_CONTROLLER;//hittingBeam.mouseButton == 0;
				TransporterBeaconBeam b = (TransporterBeaconBeam) hittingBeam.originMetaObject;
				if (marking) {
					boolean set = true;
					if (getBeamShooter().getOwnerState() instanceof PlayerState) {
						if (hitPiece.getSegmentController().getFactionId() != 0 &&
								(getBeamShooter().getOwnerState()).getFactionId() != hitPiece.getSegmentController().getFactionId() &&
								!SegmentController.isPublicException(hitPiece, getBeamShooter().getOwnerState().getFactionId())) {
							set = false;
							((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Wrong faction ID!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
						}
					}
					if (set) {
						b.markerLocation = hitPiece.getAbsoluteIndex();
						b.marking = hitPiece.getSegmentController().getUniqueIdentifier();
						b.realName = hitPiece.getSegmentController().getRealName();
						if(hitPiece.getSegmentController().isOnServer()){
							System.err.println("MARKER SET ON SERVER: "+b.marking+" : "+hitPiece);
						}
						if(!(hitPiece.getSegmentController()).isOnServer() && hitPiece.getSegmentController().getSectorId() == ((GameClientState) hitPiece.getSegmentController().getState()).getCurrentSectorId()){
							String m = Lng.str("Set to transporter '%s'",  b.realName);
							
							Transform t = new Transform();
							t.setIdentity();
							hitPiece.getWorldPos(t.origin, getBeamShooter().getSectorId());
							RaisingIndication raisingIndication = new RaisingIndication(t, m, 0, 1, 0, 1);
							raisingIndication.speed = 0.1f;
							raisingIndication.lifetime = 3.0f;
							HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
						}
					} else {
						ok = false;
					}
				}else{
					if(!(hitPiece.getSegmentController()).isOnServer() && hitPiece.getSegmentController().getSectorId() == ((GameClientState) hitPiece.getSegmentController().getState()).getCurrentSectorId()){
						String m = Lng.str("Cannot set! \nYou must hit a transporter computer!");
						
						Transform t = new Transform();
						t.setIdentity();
						hitPiece.getWorldPos(t.origin, getBeamShooter().getSectorId());
						RaisingIndication raisingIndication = new RaisingIndication(t, m, 1, 0, 0, 1);
						raisingIndication.speed = 0.1f;
						raisingIndication.lifetime = 3.0f;
						HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
					}
				}
			}
		} else if (hittingBeam.beamType == MARKER) {


			if (beamHits > 0) {
				boolean ok = true;
				boolean marking = hittingBeam.beamButton.contains(KeyboardMappings.SHIP_PRIMARY_FIRE);
				MarkerBeam b = (MarkerBeam) hittingBeam.originMetaObject;
				if (marking) {
					boolean set = true;
					if (getBeamShooter().getOwnerState() instanceof PlayerState) {
						if (hitPiece.getSegmentController().getFactionId() != 0 &&
								(getBeamShooter().getOwnerState()).getFactionId() != hitPiece.getSegmentController().getFactionId() &&
								!SegmentController.isPublicException(hitPiece, getBeamShooter().getOwnerState().getFactionId())) {
							set = false;
							((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Wrong faction ID!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
						}
					}
					if (set) {
						b.markerLocation = hitPiece.getAbsoluteIndex();
						b.marking = hitPiece.getSegmentController().getUniqueIdentifier();
						b.realName = hitPiece.getSegmentController().getRealName();
					} else {
						ok = false;
					}
				} else {

					if (hitPiece.getType() == ElementKeyMap.RACE_GATE_CONTROLLER) {
						if (hitPiece.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer() instanceof StationaryManagerContainer<?>) {
							StationaryManagerContainer<?> man = (StationaryManagerContainer<?>) ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer();
							RacegateCollectionManager racegateCollectionManager = man.getRacegate().getCollectionManagersMap().get(hitPiece.getAbsoluteIndex());
							if (racegateCollectionManager != null) {
								boolean set = true;
								if (getBeamShooter().getOwnerState() instanceof PlayerState) {
									if (racegateCollectionManager.getFactionId() != 0 &&
											(getBeamShooter().getOwnerState()).getFactionId() != racegateCollectionManager.getFactionId() &&
											!SegmentController.isPublicException(hitPiece, getBeamShooter().getOwnerState().getFactionId())) {
										set = false;
										((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Wrong faction ID!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
									}
								}
								if (set) {
									racegateCollectionManager.setDestination(b.marking, ElementCollection.getPosFromIndex(b.markerLocation, new Vector3i()));
									if(hitPiece.getSegmentController().isOnServer() ){
										GameServerState state = ((GameServerState)hitPiece.getSegmentController().getState());
										DatabaseIndex databaseIndex = state.getDatabaseIndex();
										
										racegateCollectionManager.getWarpDestinationUID();
										Vector3i toLocal = racegateCollectionManager.getLocalDestination();
										
										Vector3i fromSector = new Vector3i(state.getUniverse().getSector(racegateCollectionManager.getSegmentController().getSectorId()).pos);
										String fromUID = DatabaseEntry.removePrefixWOException(racegateCollectionManager.getSegmentController().getUniqueIdentifier());
										Vector3i fromLocal = racegateCollectionManager.getControllerPos();
										if(!b.marking.equals("unmarked")){
											String toUID = DatabaseEntry.removePrefixWOException(b.marking);
										
											List<DatabaseEntry> byUIDExact;
											try {
												byUIDExact = state.getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(toUID, 1);
											
												if(byUIDExact.size() > 0){
													Vector3i toSec = byUIDExact.get(0).sectorPos;
													databaseIndex.getTableManager().getFTLTable().insertFTLEntry(fromUID, fromSector, fromLocal, toUID, toSec, ElementCollection.getPosFromIndex(b.markerLocation, new Vector3i()), racegateCollectionManager.getWarpType(), racegateCollectionManager.getWarpPermission());
													((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("ADDED TO TRACK:\nfrom %s\nto %s", hitPiece.getSegmentController().getName(),  b.realName), ServerMessage.MESSAGE_TYPE_INFO, getBeamShooter().getOwnerState().getId()));
												}else{
													((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Entity not yet saved in database!\nPlease wait for auto save\nor get the sector unloaded by getting at least 3 sectors away \nfor more than 20 sec."), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
												}
											} catch (SQLException e) {
												e.printStackTrace();
											}
											
										}
									}
								} else {
									ok = false;
								}
							} else {
								ok = false;
								if (getBeamShooter().getOwnerState() instanceof PlayerState) {
									((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Input System\nnot found!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
								}
							}
						} else {
							ok = false;
							if (getBeamShooter().getOwnerState() instanceof PlayerState) {
								((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Object not compatible\nfor marking!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
							}
						}
					}else if (hitPiece.getType() == ElementKeyMap.WARP_GATE_CONTROLLER) {
						if (hitPiece.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer() instanceof StationaryManagerContainer<?>) {
							StationaryManagerContainer<?> man = (StationaryManagerContainer<?>) ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer();
							WarpgateCollectionManager warpgateCollectionManager = man.getWarpgate().getCollectionManagersMap().get(hitPiece.getAbsoluteIndex());
							if (warpgateCollectionManager != null) {
								boolean set = true;
								if (getBeamShooter().getOwnerState() instanceof PlayerState) {
									if (warpgateCollectionManager.getFactionId() != 0 &&
											(getBeamShooter().getOwnerState()).getFactionId() != warpgateCollectionManager.getFactionId() &&
											!SegmentController.isPublicException(hitPiece, getBeamShooter().getOwnerState().getFactionId())) {
										set = false;
										((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Wrong faction ID!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
									}
								}
								if (set) {
									warpgateCollectionManager.setDestination(b.marking, ElementCollection.getPosFromIndex(b.markerLocation, new Vector3i()));
								} else {
									ok = false;
								}
							} else {
								ok = false;
								if (getBeamShooter().getOwnerState() instanceof PlayerState) {
									((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Input System\nnot found!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
								}
							}
						} else {
							ok = false;
							if (getBeamShooter().getOwnerState() instanceof PlayerState) {
								((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Object not compatible\nfor marking!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
							}
						}
					} else if (hitPiece.getType() == ElementKeyMap.LOGIC_WIRELESS) {
						if (hitPiece.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer() instanceof ActivationManagerInterface) {
							ManagerContainer<?> m = ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer();
							ActivationManagerInterface man = (ActivationManagerInterface) m;

							ActivationCollectionManager activationCollectionManager = man.getActivation().getCollectionManagersMap().get(hitPiece.getAbsoluteIndex());
							if (activationCollectionManager != null) {
								activationCollectionManager.setDestination(b);
							} else {
								ok = false;
								if (getBeamShooter().getOwnerState() instanceof PlayerState) {
									((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Input System\nnot found!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
								}
							}
						} else {
							ok = false;
							if (getBeamShooter().getOwnerState() instanceof PlayerState) {
								((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Object not compatible\nfor marking!"), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
							}
						}

					} else {
						if (getBeamShooter().getOwnerState() instanceof PlayerState) {
							((PlayerState) getBeamShooter().getOwnerState()).sendServerMessage(new ServerMessage(Lng.astr("Cannot input on\nthis block type."), ServerMessage.MESSAGE_TYPE_ERROR, getBeamShooter().getOwnerState().getId()));
						}
						ok = false;
					}
				}

				if (ok && !(hitPiece.getSegmentController()).isOnServer() && hitPiece.getSegmentController().getSectorId() == ((GameClientState) hitPiece.getSegmentController().getState()).getCurrentSectorId()) {
					String m = Lng.str("Marked '%s'",  b.realName);
					if (!marking) {
						m = Lng.str("Entered '%s' as destination.",  b.realName );
					}

					Transform t = new Transform();
					t.setIdentity();
					hitPiece.getWorldPos(t.origin, getBeamShooter().getSectorId());
					RaisingIndication raisingIndication = new RaisingIndication(t, m, 0, 1, 0, 1);
					raisingIndication.speed = 0.2f;
					raisingIndication.lifetime = 1.0f;
					HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
				}
			}

		} else if (hittingBeam.beamType == POWER_SUPPLY) {
			if (hitPiece.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer() instanceof PowerManagerInterface) {
				PowerAddOn pOther = ((PowerManagerInterface) ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer()).getPowerAddOn();


				if (beamHits > 0) {

					double power = Math.max(0, beamHits * hittingBeam.getPower());
					if(pOther.getSegmentController().isUsingOldPower()) {
						pOther.incPower(power);
	
						if (!(hitPiece.getSegmentController()).isOnServer() && hitPiece.getSegmentController().getSectorId() == ((GameClientState) hitPiece.getSegmentController().getState()).getCurrentSectorId()) {
	
							if (container.getHandler().getBeamShooter().isClientOwnObject() && ElementKeyMap.getFactorykeyset().contains(hitPiece.getType())) {
								((GameClientState) hitPiece.getSegmentController().getState()).getController()
										.popupInfoTextMessage(Lng.str("Tip: power supply can only supply\none active factory. Please make\nsure other factories of the same\nstructure are disabled if there is no\nother power source."), 0);
							}
	
							Transform t = new Transform();
							t.setIdentity();
							hitPiece.getWorldPos(t.origin, getBeamShooter().getSectorId());
							RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("+%s Power",  ((int) power)), 0, 1, 0, 1);
							raisingIndication.speed = 0.2f;
							raisingIndication.lifetime = 1.0f;
							HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
						}
					}else {
						( ((ManagedSegmentController<?>) hitPiece.getSegmentController()).getManagerContainer()).getPowerInterface().injectPower(hittingBeam.getHandler().getBeamShooter(), power);
						if (!(hitPiece.getSegmentController()).isOnServer() && hitPiece.getSegmentController().getSectorId() == ((GameClientState) hitPiece.getSegmentController().getState()).getCurrentSectorId()) {
							
							if (container.getHandler().getBeamShooter().isClientOwnObject() && ElementKeyMap.getFactorykeyset().contains(hitPiece.getType())) {
								((GameClientState) hitPiece.getSegmentController().getState()).getController()
										.popupInfoTextMessage(Lng.str("Tip: power supply can only supply\none active factory. Please make\nsure other factories of the same\nstructure are disabled if there is no\nother power source."), 0);
							}
	
							Transform t = new Transform();
							t.setIdentity();
							hitPiece.getWorldPos(t.origin, getBeamShooter().getSectorId());
							RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("injected %s Power/Sec",  ((int) power)), 0, 1, 0, 1);
							raisingIndication.speed = 0.1f;
							raisingIndication.lifetime = 1.5f;
							HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
						}
					}
				}
			} else {
				beamHits = 0;
			}
		} else {
			beamHits = 0;
			assert (false);
		}
		return beamHits;
	}

	@Override
	protected boolean onBeamHitNonCube(BeamState con, int hits,
	                                BeamHandlerContainer<AbstractCharacter<E>> owner,
	                                Vector3f from, Vector3f to,
	                                CubeRayCastResult cubeResult, Timer timer,
	                                Collection<Segment> updatedSegments) {
		if (cubeResult.collisionObject != null && cubeResult.collisionObject instanceof PairCachingGhostObjectAlignable) {
			GameTransformable obj = ((PairCachingGhostObjectAlignable) cubeResult.collisionObject).getObj();
			if (obj.isOnServer()) {
				if (obj instanceof AbstractCharacter<?>) {
					if (con.beamType == HEAL) {
						((AbstractCharacter<?>) obj).getOwnerState().heal(con.getPower(), (AbstractCharacter<?>) obj, (Damager) owner);
					} else if (con.beamType == SNIPER) {
						((AbstractCharacter<?>) obj).damage(con.getPower(), owner);
						con.setPower(0); //deffuse it to not do more than one hit
					}
				}
			}
			
		}
		return true;
	}

	@Override
	public void transform(BeamState con) {
		getBeamShooter().transformBeam(con);
	}

	@Override
	public boolean drawBlockSalvage() {
		return true;
	}

	@Override
	public Vector4f getDefaultColor(BeamState beamState) {
		if (beamState.beamType == SALVAGE) {
			return salvageColor;
		} else if (beamState.beamType == HEAL) {
			return healColor;
		} else if (beamState.beamType == MARKER) {
			return markerColor;
		} else if (beamState.beamType == TRANSPORTER_MARKER) {
			return markerColor;
		}  else if (beamState.beamType == POWER_SUPPLY) {
			return powerSupplyColor;
		}
		return salvageColor;
	}


	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
				return null;
	}


	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
				return null;
	}


	@Override
	public boolean isUsingOldPower() {
				return false;
	}


	@Override
	public BeamLatchTransitionInterface getBeamLatchTransitionInterface() {
		return beamLatchTransitionInterface;
	}

}
