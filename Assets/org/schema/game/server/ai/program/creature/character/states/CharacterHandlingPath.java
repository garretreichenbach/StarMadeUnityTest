package org.schema.game.server.ai.program.creature.character.states;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.ai.program.creature.character.AICreatureMachineInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class CharacterHandlingPath extends CharacterState {

	/**
	 *
	 */
	
	private Vector3i currentTarget;
	private Vector3i lastTarget;

	public CharacterHandlingPath(AiEntityStateInterface gObj, AICreatureMachineInterface mic) {
		super(gObj, mic);
	}

	@Override
	public boolean onEnter() {
		//		System.err.println("HANDLING PATH "+lastTarget+" -> "+currentTarget);
		currentTarget = null;
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	//	private void checkZigZagOptimization(){
	//		/*
	//		 *
	//		 */
	//		if(getEntity().getOwnerState().getCurrentPath().size() > 2){
	//			long oneAfterIndex = getEntity().getOwnerState().getCurrentPath().get(0);
	//			long twoAfterIndex = getEntity().getOwnerState().getCurrentPath().get(1);
	//			Vector3i oneAfter = ElementCollection.getPosFromIndex(oneAfterIndex, new Vector3i());
	//			Vector3i twoAfter = ElementCollection.getPosFromIndex(twoAfterIndex, new Vector3i());
	//			if(currentTarget.y == oneAfter.y && oneAfter.y == twoAfter.y){
	//
	//			int foundCommon = 0;
	//			long threeAfterIndex = 0;
	//			long fourAfterIndex = 0;
	//			for (int i = 0; i < 6; i++) {
	//				long indexA = oneAfterIndex-ElementCollection.vals[i];
	//				for (int ia = 0; ia < 6; ia++) {
	//					long indexB = oneAfterIndex-ElementCollection.vals[ia];
	//					if(indexA == indexB){
	//						if(foundCommon == 0){
	//							threeAfterIndex = indexA;
	//						}else{
	//							fourAfterIndex = indexA;
	//						}
	//						foundCommon++;
	//					}
	//				}
	//			}
	//				if(foundCommon == 2){
	//
	//					Vector3i threeAfter = ElementCollection.getPosFromIndex(twoAfterIndex, new Vector3i());
	//					Vector3i fourAfter = ElementCollection.getPosFromIndex(twoAfterIndex, new Vector3i());
	//					if(getEntity().getAffinity() instanceof SegmentController){
	//						SegmentController sc = (SegmentController)getEntity().getAffinity();
	//						if(isOkToWalk(threeAfter, sc) && isOkToWalk(fourAfter, sc)){
	//
	//						}
	//					}
	//
	//
	//
	//				}
	//			}
	//
	//
	//		}
	//	}

	@Override
	public boolean onUpdate() throws FSMException {
		if (getEntity().getOwnerState().getCurrentPath() == null) {
			stateTransition(Transition.PATH_FINISHED);
			return false;
		}
		if (getEntity().getOwnerState().getCurrentPath().isEmpty()) {
			stateTransition(Transition.PATH_FINISHED);
			return false;
		}
		if (getEntity().getAffinity() != null) {

			//			Transform affTrans = new Transform(getEntity().getAffinity().getWorldTransform());

			long next = getEntity().getOwnerState().getCurrentPath().remove(0);
			currentTarget = ElementCollection.getPosFromIndex(next, new Vector3i());

			//			checkZigZagOptimization();
			if (lastTarget != null && currentTarget != null) {
				boolean vertical;
				do {
					vertical = false;
					for (int i = 0; i < 5; i++) {
						if (currentTarget.equals(lastTarget.x, lastTarget.y + i, lastTarget.z)) {
							vertical = true;
							break;
						}
					}
					if (vertical && !getEntity().getOwnerState().getCurrentPath().isEmpty()) {
						//						System.err.println("TAKING NEXT: "+getEntity().getOwnerState().getCurrentPath().size());
						next = getEntity().getOwnerState().getCurrentPath().remove(0);
						currentTarget = ElementCollection.getPosFromIndex(next, new Vector3i());
					}
				} while (!getEntity().getOwnerState().getCurrentPath().isEmpty() && vertical);
			}

			lastTarget = currentTarget;

			Vector3f relPos = new Vector3f(currentTarget.x - SegmentData.SEG_HALF, currentTarget.y - SegmentData.SEG_HALF, currentTarget.z - SegmentData.SEG_HALF);
			relPos.y += ((getEntity().getCharacterHeight() * 0.5f) - 0.1f);
			//			affTrans.transform(relPos);
			getEntityState().getCurrentMoveTarget().set(relPos);
			stateTransition(Transition.MOVE);
		}
		return false;
	}

	/**
	 * check two fields above to be free to be able to fit the character
	 * check one below to be solid to see if object can walk there
	 *
	 * @param pos
	 * @param sc
	 * @return character is able to walk there
	 */
	public boolean isOkToWalk(Vector3i pos, SegmentController sc) {
		SegmentPiece curPiece = sc.getSegmentBuffer().getPointUnsave(pos);//autorequest true previously
		if(curPiece == null){
			return false;
		}
		if ((curPiece.getType() == 0 || !ElementKeyMap.getInfo(curPiece.getType()).isPhysical(curPiece.isActive()))
				) {
			Vector3i above = new Vector3i(pos);
			above.y++;
			SegmentPiece abovePiece = sc.getSegmentBuffer().getPointUnsave(above);//autorequest true previously

			if(abovePiece == null){
				return false;
			}
			if ((abovePiece.getType() == 0 || !ElementKeyMap.getInfo(abovePiece.getType()).isPhysical(abovePiece.isActive()))
					) {
				Vector3i below = new Vector3i(pos);
				below.y--;
				SegmentPiece belowPiece = sc.getSegmentBuffer().getPointUnsave(below); //autorequest true previously
				if(belowPiece == null){
					return false;
				}
				if ((belowPiece.getType() != 0 && ElementKeyMap.getInfo(belowPiece.getType()).isPhysical(belowPiece.isActive()))
						) {
					//walking zigsag
					return true;

				}

			}

		}
		return false;
	}
}
