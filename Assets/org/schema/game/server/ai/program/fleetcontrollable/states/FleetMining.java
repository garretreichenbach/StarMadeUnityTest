package org.schema.game.server.ai.program.fleetcontrollable.states;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.game.common.controller.SegmentBufferIteratorInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;

public class FleetMining extends ShipGameState implements SegmentBufferIteratorInterface{

	private Vector3f rotDir = new Vector3f();
	private Vector3f targetVelocity = new Vector3f();
	private Vector3f targetPosition = new Vector3f();
	private Vector3f fromPos = new Vector3f();
	private int targetid;
	private byte targetType;
	private Vector3f moveDir = new Vector3f();

	public FleetMining(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {
		targetVelocity.set(0,0,0);
		targetPosition.set(0,0,0);
		targetid = -1;
		moveDir.set(0,0,0);
		rotDir.set(0,0,0);
		fromPos.set(getEntity().getWorldTransform().origin);
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		SimpleGameObject target = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget();
		if(target == null || !(target instanceof SegmentController) || ((SegmentController)target).getTotalElements() <= 0){
			System.err.println("[MINING] no target!");
			stateTransition(Transition.RESTART);
			return false;
		}
		if(!getEntityState().canSalvage()){
//			System.err.println("CANNOT SALV (no salvage)");
			stateTransition(Transition.RESTART);
			return false;
		}
		boolean foundBlock = findTargetBlock((SegmentController)target);
		
		targetVelocity.set(0,0,0);
		targetPosition.set(0,0,0);
		targetid = -1;
		moveDir.set(0,0,0);
		rotDir.set(0,0,0);
		
		Vector3f distToOrigPoint = new Vector3f();
		distToOrigPoint.sub(fromPos, getEntity().getWorldTransform().origin);
		if(distToOrigPoint.length() > getEntityState().getSalvageRange()-3){
//			System.err.println( getEntity()+" CANNOT SALV (DIST)");
			stateTransition(Transition.RESTART);
			return false;
		}
		if(foundBlock){
			if(this.currentTarget.getSegment() != null){
				this.currentTarget.refresh();
				
				
				
				//target and mine block
				boolean findRotDir = findRotDir((SegmentController)target, this.currentTarget);
				
				if(findRotDir){
					
					Vector3f forward = new Vector3f();
					GlUtil.getForwardVector(forward, getEntity().getWorldTransform());
					Vector3f normRotDir = new Vector3f(rotDir);
					normRotDir.normalize();
//					System.err.println("CURRENT TAREGT ::: "+this.currentTarget+" NORM ::: "+normRotDir.epsilonEquals(forward, ShipAIEntity.EPSILON_RANGE));
					if (normRotDir.epsilonEquals(forward, ShipAIEntity.EPSILON_RANGE)) {
						
						if(((SegmentController)target).getPhysicsObject() != null && !((SegmentController)target).getPhysicsObject().isStaticOrKinematicObject()){
							((SegmentController)target).getPhysicsObject().getLinearVelocity(targetVelocity);
						}else{
							targetVelocity.set(0,0,0);
						}
						//in range & orientatied. shoot now
//						System.err.println(getEntity()+" MINE NOW !!!!!!!!! ");
						targetPosition.set(currentTo);
						targetid = target.getAsTargetId();
						targetType = SimpleGameObject.MINABLE;
						
					}
				}else{
					if(rotDir.length() > 1200){
//						System.err.println("EEEE :RESET "+getEntity()+" MINE !!!!!!!!! ");
						//we migth have drifted off. Go back to formation
						stateTransition(Transition.RESTART);
						return false;
					}else{
//						System.err.println("NO ROT DIR");
					}
					moveDir.set(rotDir);
					rotDir.set(0,0,0);
				}
				
			}
		}else{
			//System.err.println("[MINING] no target block found!");
			if(!foundBlock){
				//System.err.println("[MINING] no nonempty segment found! -> restart");
				stateTransition(Transition.RESTART);
			}
		}
		return false;
	}
	
	Vector3f posTmp = new Vector3f();
	private Vector3f currentTo = new Vector3f();
	public boolean findRotDir(SegmentController target, SegmentPiece block){

		if (target != null) {
			
			
			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);
			

			block.getAbsolutePos(posTmp);
			posTmp.x -= SegmentData.SEG_HALF;
			posTmp.y -= SegmentData.SEG_HALF;
			posTmp.z -= SegmentData.SEG_HALF;
			
			target.getClientTransform().transform(posTmp);
			
			Vector3f from = getEntity().getWorldTransform().origin;

			currentTo.set(posTmp);
			
			rotDir.sub(currentTo, from);
			
			if (!checkTargetinRangeSalvage(target, 0)) {
				return false;
			}
			
			
			
			return true;
		}
		rotDir.set(0,0,0);
		return false;
	}


	public boolean findTargetBlock(SegmentController target){
		found = false;
		foundBlock = false;
		target.getSegmentBuffer().iterateOverNonEmptyElement(this, true);
		return foundBlock;
	}
	
	
	boolean found = false;
	long lastSegPos = -1;
	int lastBlockPos;
	private SegmentPiece currentTarget = new SegmentPiece();
	private boolean foundBlock;
	
	@Override
	public boolean handle(Segment s, long lastChanged) {
//		System.err.println("FOUND SEGMENT "+s+"; "+s.getSegmentData());
		SegmentData segmentData = s.getSegmentData();
		if(segmentData != null){
			
			long index = ElementCollection.getIndex(s.pos);
			if(index != lastSegPos){
				lastBlockPos = 0;
				lastSegPos = index;
			}
			for(int i = 0; i < 256 && !foundBlock; i++){
				if(lastBlockPos < SegmentData.BLOCK_COUNT){
					short type = segmentData.getType(lastBlockPos);
					if(ElementKeyMap.isValidType(type)){
						int bIndex = lastBlockPos;
						//FIXME use bit mask
						byte z = (byte) (bIndex / SegmentData.SEG_TIMES_SEG);
						bIndex -= z * SegmentData.SEG_TIMES_SEG;
						byte y = (byte) (bIndex / SegmentData.SEG);
						bIndex -= y * SegmentData.SEG;
						byte x = (byte) bIndex;
						
						this.currentTarget.setByReference(s, x, y, z);
						foundBlock = true;
					}else{
						lastBlockPos++;
					}
				}else{
					lastBlockPos = 0;
				}
			}
			found = true;
		}
		return false; //stop as long as first is not mined out
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer, Ship entity,
			ShipAIEntity s) throws FSMException {
		
		Vector3f targetPosition = new Vector3f();
		Vector3f targetVelocity = new Vector3f();
	
		targetPosition.set(this.targetPosition);
		targetVelocity.set(this.targetVelocity);
		int targetId = this.targetid;
		byte targetType = this.targetType;
	
		getEntity().getNetworkObject().moveDir.set(moveDir); //break
		
		if (targetPosition.length() > 0) {
			getEntity().getNetworkObject().targetPosition.set(targetPosition);
			getEntity().getNetworkObject().targetVelocity.set(targetVelocity);
			getEntity().getNetworkObject().targetId.set(targetId);
			getEntity().getNetworkObject().targetType.set(targetType);
			s.doShooting(unit, timer);
			this.targetPosition.set(0, 0, 0);
//			stateTransition(Transition.SHOOTING_COMPLETED);
//			System.err.println("[MINING] SHOOTING COMPLETED");
		}
		
		
		getEntity().getNetworkObject().orientationDir.set(rotDir, 0);
		
		if(rotDir.lengthSquared() > 0){
			s.orientate(timer, Quat4fTools.getNewQuat(rotDir.x, rotDir.y, rotDir.z, 0));
		}
	}
}
