package org.schema.game.server.ai.program.fleetcontrollable.states;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.world.SectorTransformation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.TransformaleObjectTmpVars;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

public abstract class FleetFormationingAbstract extends ShipGameState {

	private Vector3f movingDir;
	private Quat4f orientationDir = new Quat4f();
	private float dist;
	private boolean evading;
	private boolean orientating;
	private static final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();

	public FleetFormationingAbstract(AiEntityStateInterface gObj) {
		super(gObj);
		movingDir = new Vector3f();
	}
	int evadingCounter = 0;
	long lastEvading;
	/**
	 * @return the movingDir
	 */
	public Vector3f getMovingDir() {
		return movingDir;
	}


	@Override
	public boolean onEnter() {

		movingDir.set(0, 0, 0);
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		if(getEntity().railController.isDockedAndExecuted()){
			return false;
		}
		
		orientating = false;
		Fleet fleet = getEntity().getFleet();
		if(fleet == null || fleet.isEmpty() || fleet.isFlagShip(getEntity())){
			stateTransition(Transition.RESTART);
			return false;
		}
		orientationDir.set(0,0,0,0);
		movingDir.set(0,0,0);
		if(!getEntity().getSegmentBuffer().isFullyLoaded()){
			movingDir.set(0,0,0.1f);
			return false;
		}
		if(getEntity().isCoreOverheating()){
			stateTransition(Transition.RESTART);
			return false;
		}
		FleetMember flagShipMember = fleet.getFlagShip();
		
		if(!flagShipMember.isLoaded()){
			//move to sector
			((TargetProgram<?>) getEntityState().getCurrentProgram()).setSectorTarget(new Vector3i(flagShipMember.getSector()));
			stateTransition(Transition.FLEET_MOVE_TO_FLAGSHIP_SECTOR);
			return true;
		}else{
			
			
			SegmentController flagShip = flagShipMember.getLoaded();
//			System.err.println(getEntity()+" ---> "+target);
			boolean obstructedByOthers = false;
			if (flagShip != null) {
				if(((ShipAIEntity)getEntityState()).fleetFormationPos.isEmpty()){
					return false;
				}

				
				
				Vector3f from = getEntity().getWorldTransform().origin;
				SectorTransformation nextPos = ((ShipAIEntity)getEntityState()).fleetFormationPos.get(0);
				float distToDest = 100;
				
				if(flagShip.getSectorId() == getEntity().getSectorId()){
					distToDest = Vector3fTools.diffLength(
						getEntity().getWorldTransform().origin, 
						((ShipAIEntity)getEntityState()).fleetFormationPos.get(((ShipAIEntity)getEntityState()).fleetFormationPos.size()-1).t.origin);
				}

				Transform toTrans = new Transform();
				toTrans.setIdentity();
				toTrans.origin.set(nextPos.t.origin);
				
				Transform toTransOut = new Transform();
				toTransOut.setIdentity();
				
				int ownSecId = getEntity().getSectorId();
				Vector3i ownSecPos = ((GameServerState) getEntity().getState()).getUniverse().getSector(ownSecId).pos;
				
				SimpleTransformableSendableObject.calcWorldTransformRelative
				(ownSecId, 
						ownSecPos, 
						nextPos.sectorId, toTrans, flagShip.getState(), 
						flagShip.isOnServer(), toTransOut, v);

				
				assert(!Float.isNaN(toTransOut.origin.x)):toTransOut.origin;
				
				movingDir.sub(toTransOut.origin, from);
				
//				System.err.println("NEXT: "+getEntity().getSectorId()+" -> "+nextPos.sectorId+"*: tNorm: "+nextPos.t.origin+" :: OPos: "+getEntity().getWorldTransform().origin+" -> Target "+toTransOut.origin+" DIR: "+getMovingDir()+"; "+((ShipAIEntity)getEntityState()).fleetFormationPos);
				
				
				assert(!Float.isNaN(movingDir.x)): movingDir;
				
				dist = movingDir.length();
				this.evading = false;
				if(!getEntity().getProximityObjects().isEmpty()){
//					System.err.println("AI EVADING: "+getEntity()+": ProxVec: "+getEntity().proximityVector+"; proxObjs "+getEntity().getProximityObjects()+"; ");
					Vector3f proxDir = new Vector3f(getEntity().proximityVector);
					boolean has = false;
					
					for(int sId : getEntity().getProximityObjects()){
						SegmentController c = (SegmentController) getEntity().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(sId);
						if(c.getSectorId() == getEntity().getSectorId()){
							has = true;
							Vector3f ft = new Vector3f();
							ft.sub(from, c.getWorldTransform().origin);
							
							//inverse so the seperation force is larger when closer together
							ft.normalize();
							proxDir.add(ft);
							
							if(!(c instanceof Ship) || ((Ship)c).getFleet() != getEntity().getFleet()){
								obstructedByOthers = true;
							}
						}
					}
					
					if(obstructedByOthers && dist < 10){
						//target is in other object
						movingDir.set(0,0,0.1f);
					}else{
						if(has){
							Vector3f f = new Vector3f(0, 1, 0);
							if(f.dot(proxDir)> 0){
								proxDir.y += 0.5f;
							}else{
								proxDir.y -= 0.5f;
							}
							proxDir.normalize();
							proxDir.scale(10);
							
							movingDir.normalize();
							movingDir.scale(0.9f);
							movingDir.add(proxDir);
							
							assert(!Float.isNaN(movingDir.x)): movingDir;
							this.evading = true;
						}
					}
					
				}
				
				else if (dist < 3 && ((ShipAIEntity)getEntityState()).fleetFormationPos.size() > 1) {
					((ShipAIEntity)getEntityState()).fleetFormationPos.remove(0);
					Quat4fTools.set(flagShip.getWorldTransform().basis, orientationDir);
					assert(!Float.isNaN(orientationDir.x)):orientationDir;
					this.orientating = true;
					return onUpdate();
				}else if (((ShipAIEntity)getEntityState()).fleetFormationPos.size() == 1 && dist < 0.3f) {
					
					movingDir.set(0,0,0.01f); //breaking
					Quat4fTools.set(flagShip.getWorldTransform().basis, orientationDir);
					assert(!Float.isNaN(orientationDir.x)):orientationDir;
					this.orientating = true;
//					System.err.println(getEntity()+" --REACHED-> "+target);
				}else if (distToDest < 15 && flagShip.getSectorId() == getEntity().getSectorId()) {
					Quat4fTools.set(flagShip.getWorldTransform().basis, orientationDir);
					assert(!Float.isNaN(orientationDir.x)):orientationDir;
					this.orientating = true;
				}
				assert(!Float.isNaN(orientationDir.x)):orientationDir;
				if(System.currentTimeMillis() - lastEvading > 1000){
					evadingCounter = 0;
				}
				if(evading){
					
					if(!((ShipAIEntity)getEntityState()).isBigEvading()){
						evadingCounter++;
						lastEvading = System.currentTimeMillis();
					}
					
					
					if(evadingCounter > 130){
						((ShipAIEntity)getEntityState()).setEvadingTime(3000);
						evadingCounter = 0;
					}
				}
				
				onDistance(dist);
				
			} else {
				stateTransition(Transition.RESTART);
			}
		}
		
		

		return false;
	}

	protected abstract void onDistance(float dist) throws FSMException;


	@Override
	public String getDescString() {
		return "(evade: "+evading+", Orient: "+orientating+"; Dist: "+dist+"; moveDir: "+getEntity().getNetworkObject().moveDir+", orient: "+orientationDir+")";
	}
	
	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().orientationDir.set(orientationDir);
		getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));

		getEntity().getNetworkObject().moveDir.set(movingDir);
		assert(!Float.isNaN(orientationDir.x)):orientationDir;
		boolean doOrientationInMoveDir = Quat4fTools.isZero(orientationDir);
		if(movingDir.lengthSquared() > 0){
			s.moveTo(timer, movingDir, doOrientationInMoveDir);
		}
		if(!doOrientationInMoveDir){
			assert(!Float.isNaN(orientationDir.x)):orientationDir;
			s.orientate(timer, orientationDir); 
		}
	}
}
