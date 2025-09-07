package org.schema.game.common.controller.elements.shipyard.orders.states;

import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.linearmath.Transform;

public class MovingToTestSite extends ShipyardState{

	private SegmentController currentDocked;
	private PlayerState playerState;
	private Sector sec;
	private long secLoaded;
	private boolean moved;

	public MovingToTestSite(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		currentDocked = getEntityState().getCurrentDocked();
		playerState = null;
		sec = null;
		moved = false;
		Sendable sendable = ((GameServerState)getEntityState().getState()).getLocalAndRemoteObjectContainer().getLocalObjects().get(getEntityState().playerStateSent);
		if(sendable != null && sendable instanceof PlayerState){
			playerState = (PlayerState)sendable;
		}
		return false;
	}

	@Override
	public boolean onExit() {
				return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		
		if(playerState == null){
			getEntityState().sendShipyardErrorToClient(Lng.str("Move to Test Sector Failed!\nNo player!"));
			stateTransition(Transition.SY_ERROR);
			if(currentDocked != null && !currentDocked.isVirtualBlueprint()){
				System.err.println("[SERVER][SHIPYARD] no player: Removing real spawn");
				currentDocked.setMarkedForDeletePermanentIncludingDocks(true);
			}
		}else if(currentDocked != null){
			
			if(currentDocked.isVirtualBlueprint()){
				getEntityState().sendShipyardErrorToClient(Lng.str("Move to Test Sector Failed!\nReal ship didn't spawn from design!"));
				stateTransition(Transition.SY_ERROR);
			}else if(currentDocked.isFullyLoadedWithDock()){
				if(currentDocked.railController.isDockedAndExecuted()){
					currentDocked.railController.disconnect();
				
				}else{
					
					if(currentDocked.getOwnerState() != null){
						System.err.println("[SERVER][SHIPYARD] someone is in ship: Removing real spawn");
						currentDocked.setMarkedForDeletePermanentIncludingDocks(true);
						//TODO check docks
						getEntityState().sendShipyardErrorToClient(Lng.str("Move to Test Sector Failed!\nCannot be in ship!"));
						stateTransition(Transition.SY_ERROR);
					}else if(playerState.getFirstControlledTransformableWOExc() != null && playerState.getFirstControlledTransformableWOExc() instanceof PlayerCharacter){
						try {
							
							if(sec == null){
								sec = ((GameServerState)getEntityState().getState()).getUniverse().getSector(playerState.testSector);
							
								sec.noEnter(true);
								sec.noExit(true);
								secLoaded = System.currentTimeMillis();
							}else if(System.currentTimeMillis() - secLoaded < 500){
								sec.setActive(true);
							}else{
								sec.setActive(true);
								
								assert(!moved);
								
								boolean contains = false;
								for(Sendable s : getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()){
									if(s instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject)s).getSectorId() == sec.getId()){
										((SimpleTransformableSendableObject)s).markForPermanentDelete(true);
										((SimpleTransformableSendableObject)s).setMarkedForDeleteVolatile(true);
										System.err.println("[SERVER][MOVINGTOTESTSITE] cleaning up test sector "+s);
										contains = true;
										break;
									}
								}
							
								if(!contains){
							
									moved = true;
									
									playerState.instantiateInventoryServer(false);
									playerState.spawnData.preSpecialSectorTransform = new Transform(playerState.getFirstControlledTransformableWOExc().getWorldTransform());
									playerState.spawnData.preSpecialSector = new Vector3i(playerState.getCurrentSector());
									boolean eliminateGravity = true;
									((GameServerState)getEntityState().getState()).getController()
									
									.queueSectorSwitch(currentDocked, sec.pos, SectorSwitch.TRANS_JUMP, false, true, eliminateGravity);
									
									((GameServerState)getEntityState().getState()).getController()
									.queueSectorSwitch(playerState.getFirstControlledTransformableWOExc(), sec.pos, SectorSwitch.TRANS_JUMP, false, true, eliminateGravity);
									sec = null;
									if(getEntityState().lastConstructedDesign != null){
										getEntityState().designToLoad = getEntityState().lastConstructedDesign.getId();
										getEntityState().lastConstructedDesign = null;
									}
									
									stateTransition(Transition.SY_MOVING_TO_TEST_SITE_DONE);
								}
							}
							
						} catch (IOException e) {
							e.printStackTrace();
							currentDocked.setMarkedForDeletePermanentIncludingDocks(true);
							getEntityState().sendShipyardErrorToClient(Lng.str("Move to Test Sector Failed!\nSector load failed!"));
							System.err.println("[SERVER][SHIPYARD] sector load failed: Removing real spawn");
							stateTransition(Transition.SY_ERROR);
						}
					}else{
						currentDocked.setMarkedForDeletePermanentIncludingDocks(true);
						getEntityState().sendShipyardErrorToClient(Lng.str("Move to Test Sector Failed!\nCannot be in ship!"));
						System.err.println("[SERVER][SHIPYARD] player is in any ship: Removing real spawn");
						stateTransition(Transition.SY_ERROR);
					}
				}
			}
			
		}else{
			getEntityState().sendShipyardErrorToClient(Lng.str("Move to Test Sector Failed!\nNothing Docked!"));
			stateTransition(Transition.SY_ERROR);
		}
		
		return false;
	}

}
