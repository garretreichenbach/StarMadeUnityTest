package org.schema.game.common.controller.elements.mines;


import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;


public class MineLayerUnit extends CustomOutputUnit<MineLayerUnit, MineLayerCollectionManager, MineLayerElementManager> {



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MineLayerUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return 0;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.MINES;
	}
	private final Vector3i pTmp = new Vector3i();
	private final SegmentPiece pieceTmp = new SegmentPiece();
	private final SegmentPiece pieceTmpD = new SegmentPiece();
	
	public short[] calcComposition() {
		short[] composition = new short[6];
		final long mineCorePos = getNeighboringCollection().get(0);
		SegmentPiece mineCore = getSegmentController().getSegmentBuffer().getPointUnsave(mineCorePos, pieceTmp);
		if(mineCore != null) {
			assert(mineCore.getType() == ElementKeyMap.MINE_CORE):mineCore;
			
			
			for(int i = 0; i < 6; i++) {
				mineCore.getAbsolutePos(pTmp);
				pTmp.add(Element.DIRECTIONSi[i]);
				SegmentPiece n = getSegmentController().getSegmentBuffer().getPointUnsave(pTmp, pieceTmpD);
				if(n != null && n.getType() != 0) {
					composition[i] = n.getType();
				}
			}
			
		}
		return composition;
	}
	
	
	
	
	@Override
	public void doShot(ControllerStateInterface unit, Timer timer, ShootContainer shootContainer) {
		
		MineLayerElementManager em = elementCollectionManager.getElementManager();
		
		
//		SegmentPiece p = getSegmentController().
		
		final int armInSecs;
		if(unit.getPlayerState() != null) {
			
			armInSecs = unit.getPlayerState().getMineAutoArmSeconds();
		}else {
			if(getSegmentController().isInFleet()) {
				Fleet f = getSegmentController().getFleet();
				
				PlayerState p = ((GameServerState)getSegmentController().getState()).getPlayerFromNameIgnoreCaseWOException(f.getOwner());
				if(p != null) {
					armInSecs = p.getMineAutoArmSeconds(); 
				}else {
					//default for AI etc
					armInSecs = 60;
				}
			}else {
				//default for AI etc
				armInSecs = 60;				
			}
		}
		
//		System.err.println(getSegmentController().getState()+" "+getSegmentController()+" SHOT MINE (ARM: "+armInSecs+")");
		em.layMine(this, elementCollectionManager, armInSecs, shootContainer);
//		em.doShot(this, elementCollectionManager, weapontOutputWorldPos, shootingDirTemp, shootingUpTemp, shootingRightTemp, unit.getPlayerState(), timer);				
	}

	@Override
	public float getBasePowerConsumption() {
		return 0;
	}

	@Override
	public float getPowerConsumption() {
		return 0;
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return 0;
	}

	@Override
	public float getReloadTimeMs() {
		return 500;
	}

	@Override
	public float getInitializationTime() {
		return 1;
	}

	@Override
	public float getDistanceRaw() {
		return 0;
	}

	@Override
	public float getFiringPower() {
		return 0;
	}

	@Override
	public float getDamage() {
		return 0;
	}
	

}
