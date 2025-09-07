package org.schema.game.server.ai.program.common.states;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import api.common.GameCommon;
import api.utils.game.SegmentControllerUtils;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.elements.beam.repair.RepairElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.SegmentControllerAIEntity;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public abstract class ShipGameState extends SegmentControllerGameState<Ship>{

	public ShipGameState(AiEntityStateInterface gObj) {
		super(gObj);
	}

	public void updateAI(AIShipControllerStateUnit unit, Timer timer, Ship entity,
	                     ShipAIEntity s) throws FSMException{
		getEntity().getNetworkObject().targetId.set(-1);
		getEntity().getNetworkObject().targetType.set((byte) -1);
		getEntity().getNetworkObject().moveDir.set(new Vector3f(0, 0, 0));
		getEntity().getNetworkObject().orientationDir.set(0, 0, 0, 0);
		getEntity().getNetworkObject().targetPosition.set(0, 0, 0);
	}

	public void findAstronautTarget(boolean attachedPlayers) throws FSMException {
		findTarget(attachedPlayers, false, EntityType.ASTRONAUT);
	}
	@Override
	public SegmentControllerAIEntity<Ship> getEntityState() {
		return (SegmentControllerAIEntity<Ship>) super.getEntityState();
	}


	public void findTarget(boolean attachedPlayers, EntityType... filter) {
		findTarget(attachedPlayers, false, filter);
	}
	private void findTarget(boolean attachedPlayers, boolean includeInactiveAi, EntityType... filter) {

		if (((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget() == null) {
			getEntityState().lastEngage = "";
			int specificTargetId = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getSpecificTargetId();

			if (specificTargetId > 0) {
				//				System.err.println("[AI] specfific tar checcking "+specificTargetId);
				Sendable specificTarget = getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(specificTargetId);
				if (specificTarget != null && specificTarget instanceof SimpleTransformableSendableObject) {
					SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) specificTarget;
					Sector sector = ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId());
					if (sector == null) {
						System.err.println("[AI] sector of entity: " + getEntity() + " is not loaded: " + getEntity().getSectorId());
						((TargetProgram<?>) getEntityState().getCurrentProgram()).setSpecificTargetId(-1);

						return;
					} else if (sector.isProtected()
							|| sector.isPeace()) {
						//do not target objects in protected sectors
						return;
					}

					s.calcWorldTransformRelative(getEntity().getSectorId(), sector.pos);

					Vector3f dist = new Vector3f();

					dist.sub(s.getClientTransform().origin, getEntity().getWorldTransform().origin);
					if (dist.length() > getEntityState().getShootingRange()) {
						//						System.err.println("[AI] specfific tar out of range "+specificTarget);
						return;
					}
					if (specificTarget instanceof PlayerControllable && !(s instanceof AbstractCharacter<?>)) {
						if (((PlayerControllable) specificTarget).getAttachedPlayers().isEmpty() &&
								(specificTarget instanceof AiInterface &&

										!((AiInterface) specificTarget).getAiConfiguration().isActiveAI())) {
							//							System.err.println("[AI] specfific tar invalid "+specificTarget);
							return;
						}
					}
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(s);
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setSpecificTargetId(-1);
					System.err.println("[AI] specfific tar SET " + specificTarget);
					return;
				} else {
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setSpecificTargetId(-1);
				}
			}

			ArrayList<SimpleTransformableSendableObject> enemies = new ArrayList<SimpleTransformableSendableObject>();
			SimpleTransformableSendableObject own = getEntity();
			getEntityState().lastEngage = "";
			int lastLen = 0;
			for (Sendable s : getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if(getEntityState().lastEngage.length() > lastLen){
					getEntityState().lastEngage += "\n";
					lastLen = getEntityState().lastEngage.length();
				}
				boolean onCheckPlayer = false;
				if (s instanceof SimpleTransformableSendableObject) {
					SimpleTransformableSendableObject st = (SimpleTransformableSendableObject) s;
					if (st.isHidden()) {
						continue;
					}

					Sector sector = ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId());
					Sector sectorTar = ((GameServerState) getEntity().getState()).getUniverse().getSector(st.getSectorId());
					if (sector == null || sectorTar == null) {
						System.err.println("[AI][SEARCHFORTARGET] either own or target sector not loaded: " + sector + "; " + sectorTar);
						continue;
					}
					if (Math.abs(sector.pos.x - sectorTar.pos.x) > 3 ||
							Math.abs(sector.pos.y - sectorTar.pos.y) > 3 ||
							Math.abs(sector.pos.z - sectorTar.pos.z) > 3) {
						continue;
					}
					if (((SimpleTransformableSendableObject) s).getOwnerState() != null && st.getOwnerState() instanceof PlayerState) {
						onCheckPlayer = getEntityState().lastEngage.isEmpty();
						if (onCheckPlayer) {
							getEntityState().lastEngage += "...." + s + " " + st.getOwnerState().getName();
						}
					}

					if (filter != null && filter.length > 0) {
						boolean isInFilter = false;
						for (EntityType t : filter) {
							if (((SimpleTransformableSendableObject) s).getType() == t) {
								isInFilter = true;
								getEntityState().lastEngage += " NOT_IN_FILTERED ";
								break;
							}
						}
						if (!isInFilter) {
							continue;
						}
					}


					if (st == own) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " SELF ";
						}
						continue;
					}
					if (st instanceof ShopSpaceStation) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " SHOP ";
						}
						continue;
					}
					if (st instanceof PlayerControllable && st instanceof ShopperInterface) {
						if (!((PlayerControllable) st).getAttachedPlayers().isEmpty() && !((ShopperInterface) st).getShopsInDistance().isEmpty()) {
							if (onCheckPlayer) {
								getEntityState().lastEngage += " IN_SAFE_SHOP_DIST ";
							}
							continue;
						}
					}
					if (st instanceof AbstractCharacter<?> && ((AbstractCharacter<?>) st).getFactionId() < 0) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " NPC ";
						}
						//do not attack NPC characters
						continue;
					}
					if (st instanceof SegmentController && ((SegmentController) st).isCoreOverheating()) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " OVERHEATING ";
						}
						continue;
					}

					if(!((FactionState) getEntityState().getState()).getFactionManager().isEnemy(own, st) && !canRepair(own, st)) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " NOT_ENEMY " + own.getFactionId() + "" + st.getFactionId();
						}
						continue;
					}


					if (sectorTar.isProtected()
							|| sectorTar.isPeace()) {
						//do not target objects in protected sectors
						if (onCheckPlayer) {
							getEntityState().lastEngage += " SECTOR_PROTECTED ";
						}
						continue;
					}

					if (st instanceof SegmentController && (getEntity().getDockingController().isDocked() || ((SegmentController) st).getDockingController().isDocked())) {
						SegmentController c = (SegmentController) s;
						if (isDockedOn(getEntity(), c) || isDockedOn(c, getEntity())) {
							if (onCheckPlayer) {
								getEntityState().lastEngage += " OLD_DOCKED_SELF ";
							}
							continue;
						}
					}

					if (getEntity().railController.isInAnyRailRelationWith(st)) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " RAIL_DOCKED_SELF ";
						}
						continue;
					}

					st.calcWorldTransformRelative(getEntity().getSectorId(), sector.pos);

					Vector3f dist = new Vector3f();
					dist.sub(st.getClientTransform().origin, getEntity().getWorldTransform().origin);

					//					if (dist.length() > getEntityState().getShootingRange()) {
					//						if (onCheckPlayer) {
					//							getEntityState().lastEngage += " NOT_IN_SHOOTING_RANGE ";
					//						}
					//						continue;
					//					}

					if (s instanceof Ship && ((Ship) s).isCloakedFor(getEntity())) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " CLOAKED ";
						}
						continue;
					}
					if (s instanceof Ship && ((Ship) s).isJammingFor(getEntity()) && dist.length() > getEntityState().getShootingRange()) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " NOT_IN_JAM_RANGE ";
						}
						continue;
					}

					boolean isActiveAI = st instanceof AiInterface && ((AiInterface) st).getAiConfiguration().getAiEntityState().isActive();
					if(!isActiveAI && includeInactiveAi && s instanceof Ship){
						isActiveAI = true;
					}
					boolean hasAttachedPlayers = (st instanceof PlayerControllable && !((PlayerControllable) st).getAttachedPlayers().isEmpty());
					if (((SimpleTransformableSendableObject) s).isInAdminInvisibility()) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " INVISIBILITY_MODE ";
						}
						continue;
					}
					if (!attachedPlayers ||
							(hasAttachedPlayers || isActiveAI)) {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " SUCCESSFULLY_FOUND";
						}

						enemies.add(st);
					} else {
						if (onCheckPlayer) {
							getEntityState().lastEngage += " ATT_PLS;hasAtt;ActAI " + attachedPlayers + "; " + hasAttachedPlayers + "; " + isActiveAI;
						}
					}
				}
			}
			if (!enemies.isEmpty()) {
				if(getAIConfig().get(Types.PRIORIZATION).getCurrentState().toString().toLowerCase(Locale.ENGLISH).equals("highest")){
					SegmentController largest = null;
					for (Iterator<SimpleTransformableSendableObject> iterator = enemies.iterator(); iterator.hasNext(); ) {
						if (iterator.next() instanceof SegmentController entity) {
							if(entity.hasActiveReactors() && (largest == null || entity.getMassWithDocks() > largest.getMassWithDocks())){
								largest = entity;
							} else iterator.remove();
						}
					}
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(largest);
				} else if(getAIConfig().get(Types.PRIORIZATION).getCurrentState().toString().toLowerCase(Locale.ENGLISH).equals("lowest")){
					SegmentController smallest = null;
					for (Iterator<SimpleTransformableSendableObject> iterator = enemies.iterator(); iterator.hasNext(); ) {
						if (iterator.next() instanceof SegmentController entity) {
							if(entity.hasActiveReactors() && (smallest == null || entity.getMassWithDocks() < smallest.getMassWithDocks())){
								smallest = entity;
							} else iterator.remove();
						}
					}
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(smallest);
				}
				else {
					// take a random enemy
					int index = FastMath.round(Math.random() * (enemies.size() - 1));
					((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(enemies.get(index));
				}
			} else {
				if(!includeInactiveAi){
					getEntityState().lastEngage = "";
					findTarget(attachedPlayers, true, filter);
				}
				//				System.err.println("[SEARCHTARGET] NO ENEMIES FOUND FOR "+getEntity());
			}
		}
	}

	private boolean canRepair(SimpleTransformableSendableObject own, SimpleTransformableSendableObject st) {
		if(GameCommon.getGameState().getFactionManager().isFriend(own.getFactionId(), st.getFactionId()) && own.getFactionId() != 0) {
			if(st instanceof ManagedSegmentController<?> && own instanceof ManagedSegmentController<?>) {
				ManagedSegmentController<?> stMSC = (ManagedSegmentController<?>) st;
				if(st instanceof Ship && ((Ship) st).getReactorHp() < ((Ship) st).getReactorHpMax()) {
					RepairElementManager elementManager = SegmentControllerUtils.getElementManager((ManagedUsableSegmentController<?>) stMSC, RepairElementManager.class);
					if(elementManager != null && elementManager.totalSize > 0) {
						Ship ship = (Ship) st;
						ship.getAiConfiguration().getAiEntityState().getCurrentProgram().suspend(true);
						((TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram()).setTarget(null);
						System.err.println("[AI] [" + own + "] REPAIRING " + ship + " " + ship.getReactorHp() + "/" + ship.getReactorHpMax());
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isTargetValid(SimpleGameObject target)  {
		if (target == null) {
			return false;
		}
		if (target.isHidden()) {
			System.err.println("[AI] Hidden Entity. Getting new Target");
			((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
			return false;
		}

		if (getEntity().railController.isInAnyRailRelationWith(target)) {
			return false;
		}
		if (target instanceof SegmentController) {
			if (isDockedOn(getEntity(), ((SegmentController) target))) {
				return false;
			}
			if (isDockedOn(((SegmentController) target), getEntity())) {
				return false;
			}
		}
		Sector sector = ((GameServerState) getEntity().getState()).getUniverse().getSector(target.getSectorId());
		if (sector != null && (sector.isProtected()
				|| sector.isPeace())) {
			//do not target objects in protected sectors
			return false;
		}
		if(target instanceof SegmentController && ((SegmentController)target).isCoreOverheating()){
			((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
			return false;
		}
		//		if (target instanceof PlayerControllable) {
		//			if (((PlayerControllable) target).getAttachedPlayers().isEmpty()
		//					&& (target instanceof AiInterface && !((AiInterface) target).getAiConfiguration().isActiveAI())
		//					) {
		//				System.err.println("[AI] Dead Entity. Getting new Target");
		//				return false;
		//			}
		//		}

		if (getEntity().getFactionId() == FactionManager.PIRATES_ID) {
			if (target instanceof PlayerControllable && target instanceof ShopperInterface) {
				if (!((PlayerControllable) target).getAttachedPlayers().isEmpty() && !((ShopperInterface) target).getShopsInDistance().isEmpty()) {
					if (((TargetProgram<?>) getEntityState().getCurrentProgram()).getSpecificTargetId() < 0) {
						System.err.println("[AI] Entity in shop distance. searching new entity");
						((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean checkTargetinRangeSalvage(SegmentController target, float margin) {
		if(!isRangeValidPreCheck(target, true)){
			System.err.println("NO PRECHECK "+getEntity()+" ---> "+target);
			return false;
		}
		if (target instanceof Ship && target.isJammingFor(getEntity()) && dist.length() > getEntityState().getSalvageRange() / ShipAIEntity.JAMMING_DIV) {
			System.err.println("JAMMING "+getEntity()+" ---> "+target);
			return false;
		}
		if (dist.length() > getEntityState().getSalvageRange() - margin) {
			System.err.println("NOT SALVAGE RANGE dist "+dist.length()+" / range "+ getEntityState().getSalvageRange()+": "+getEntity()+" ---> "+target);
			return false;
		}
		return true;
	}
	private boolean isRangeValidPreCheck(SimpleGameObject target, boolean doTransUpdate){
		if(!isTargetValid(target)){
			return false;
		}
		if(doTransUpdate) {
			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);
		}

		getEntityState().random.setSeed(getEntityState().seed);

		target.getClientTransformCenterOfMass(serverTmp);
		target.transformAimingAt(serverTmp.origin, getEntityState().getEntity(), target, getEntityState().random, 0);

		dist.sub(serverTmp.origin, (((SimpleTransformableSendableObject) getEntityState().getEntity()).getWorldTransform().origin));
		if (target instanceof Ship && ((Ship) target).isCloakedFor(getEntity())) {
			return false;
		}
		return true;
	}
	public boolean checkTargetinRange(SimpleGameObject target, float margin, boolean doTransUpdate) {
		if(!isRangeValidPreCheck(target, doTransUpdate)){
			return false;
		}

		//This breaks AI drones against jamming targets and makes them unresponsive against a jamming hostile until they actually take block damage
		if (target instanceof Ship && ((Ship) target).isJammingFor(getEntity()) && dist.length() > getEntityState().getShootingRange() / ShipAIEntity.JAMMING_DIV) {
			if(ShipAIEntity.NERF_AI_RANGE_VS_JAMMING_TARGETS) {	//...but I'll leave it as a configurable option for now
				return false;
			}
		}


		if (dist.length() > getEntityState().getShootingRange() - margin) {
			return false;
		}
		return true;
	}
}