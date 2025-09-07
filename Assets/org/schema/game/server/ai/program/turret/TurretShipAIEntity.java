package org.schema.game.server.ai.program.turret;

import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.data.FactionState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

public class TurretShipAIEntity extends ShipAIEntity {

	/**
	 *
	 */
	
	/**
	 *
	 */
	private static final long TARGET_ATTACKED_CHANGE_DELAY = 5000;
	public Vector3f orientateDir = new Vector3f();
	//	private static final long CHECK_PROXIMITY_DELAY = 5000;
	private long lastTargetChangeDueToAttack;

	public TurretShipAIEntity(Ship s, boolean start) {
		super("Turret_Ent", s);
		if (s.isOnServer()) {
			setCurrentProgram(new TurretProgram(this, start));
		}

		//		lastProximityTest = (long) (System.currentTimeMillis()+ Math.random()*CHECK_PROXIMITY_DELAY);
	}
	@Override
	public float getShootingDifficulty(SimpleGameObject target) {
		AIConfiguationElements<String> a = ((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.AIM_AT));
		if (a.getCurrentState().equals("Missiles")){
			return getEntity().getConfigManager().apply(StatusEffectType.AI_ACCURACY_POINT_DEFENSE, getShootingDifficultyRaw(target));
		}else{
			return getEntity().getConfigManager().apply(StatusEffectType.AI_ACCURACY_TURRET, getShootingDifficultyRaw(target));
		}
	}
	/* (non-Javadoc)
	 * @see org.schema.game.server.ai.ShipAIEntity#updateAIServer(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateAIServer(Timer timer) throws FSMException {
		super.updateAIServer(timer);

		if(getStateCurrent() instanceof ShipGameState){
			((ShipGameState)getStateCurrent()).updateAI(unit, timer, getEntity(), this);
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.ai.SegmentControllerAIEntity#handleHitBy(int, org.schema.schine.network.objects.Sendable)
	 */
	@Override
	public void handleHitBy(float actualDamage, Damager from) {
		if(getState().getUpdateTime() - lastTargetChangeDueToAttack < TARGET_ATTACKED_CHANGE_DELAY) {
			return;
		}
		
		AIConfiguationElements<String> a = ((AIConfiguationElements<String>) (getEntity()).getAiConfiguration().get(Types.AIM_AT));

		
		
		
		if (!(a.getCurrentState().equals("Any") || 
				(a.getCurrentState().equals("Ships") && from instanceof Ship) ||
				(a.getCurrentState().equals("Stations") && (from instanceof SpaceStation || from instanceof PlanetIco || from instanceof Planet)))) {
			return;
		}

		if (from instanceof SimpleTransformableSendableObject) {

			SimpleTransformableSendableObject ship = (SimpleTransformableSendableObject) from;
			RType relation = ((FactionState) getState()).getFactionManager().getRelation(ship, getEntity());

			if (relation == RType.ENEMY || relation == RType.NEUTRAL) {
				//				System.err.println("[AI] Ship has been attacked and will now respond aggression");
				lastTargetChangeDueToAttack = System.currentTimeMillis();
				((TargetProgram<?>) getCurrentProgram()).setTarget(ship);
				//				this.aggroTarget = (ship);
			}
		}
	}

}
