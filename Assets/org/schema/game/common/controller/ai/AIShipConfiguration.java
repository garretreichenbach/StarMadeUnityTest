package org.schema.game.common.controller.ai;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableShipAIEntity;
import org.schema.game.server.ai.program.searchanddestroy.SimpleSearchAndDestroyShipAIEntity;
import org.schema.game.server.ai.program.turret.TurretShipAIEntity;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.StateInterface;

public class AIShipConfiguration extends AIGameSegmentControllerConfiguration<ShipAIEntity, Ship> {

	public AIShipConfiguration(StateInterface state, Ship owner) {
		super(state, owner);

	}

	@Override
	protected ShipAIEntity getIdleEntityState() {
		return new ShipAIEntity("shipAiEntity", getOwner());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.ai.AIGameConfiguration#onClientSettingChanged(org.schema.game.common.controller.ai.AIConfiguationElements)
	 */
	@Override
	protected void onClientSettingChanged(AIConfiguationElements<?> s) {
		if (s.getType() == Types.ACTIVE) {
			if (s.isOn()) {
				//				System.err.println("[CLIENT][AI] SENTINEL SET TO TRUE ON ");
			} else {
				//				System.err.println("[CLIENT][AI] SENTINEL SET TO FALSE ON ");
			}
			setAIEntity(new ShipAIEntity("shipAiEntity", getOwner()));
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.ai.AIGameConfiguration#onCoreDestroyed(org.schema.schine.network.objects.Sendable)
	 */
	@Override
	public void onCoreDestroyed(Damager from) {

		if (isActiveAI()) {
			if (from != null && getOwner().getFactionId() == FactionManager.PIRATES_ID) {
				((GameServerState) from.getState()).getController().spawnKillBonus(getOwner());
			}
			if (getOwner() != null && getOwner().getFactionId() != 0) {
				getOwner().railController.resetFactionForEntitiesWithoutFactionBlock(getOwner().getFactionId());
			}
		}

		super.onCoreDestroyed(from);
	}

	@Override
	public void onStartOverheating(Damager from) {
		if (isActiveAI()) {
			if (from != null && getOwner().getFactionId() == FactionManager.PIRATES_ID) {
				((GameServerState) from.getState()).getController().spawnKillBonus(getOwner());
			}
			if (getOwner() != null && getOwner().getFactionId() != 0) {
				getOwner().railController.resetFactionForEntitiesWithoutFactionBlock(getOwner().getFactionId());
			}
		}
		super.onStartOverheating(from);
	}

	@Override
	protected boolean isForcedHitReaction() {
				return false;
	}


	@Override
	protected void onServerSettingChanged(AIConfiguationElements s) {
		if (s.getType() == Types.TYPE) {
			if (s.getCurrentState().equals("Turret")) {
				setAIEntity(new TurretShipAIEntity(getOwner(), !get(Types.ACTIVE).isOn()));
			} else if (s.getCurrentState().equals("Ship")) {
				setAIEntity(new SimpleSearchAndDestroyShipAIEntity(getOwner(), !get(Types.ACTIVE).isOn()));
			} else if (s.getCurrentState().equals("Fleet")) {
				setAIEntity(new FleetControllableShipAIEntity(getOwner(), false));
				
			}
		}
		if(((AIConfiguationElements<String>)get(Types.TYPE)).getCurrentState().equals("Fleet") && !get(Types.ACTIVE).isOn()){
			((AIConfiguationElements<Boolean>)get(Types.ACTIVE)).setCurrentState(true, false);
		}
		super.onServerSettingChanged(s);

	}

	@Override
	protected void prepareActivation() {
		AIConfiguationElements s = get(Types.TYPE);

		if (s.getCurrentState().equals("Turret")) {
			setAIEntity(new TurretShipAIEntity(getOwner(), !get(Types.ACTIVE).isOn()));
		} else if (s.getCurrentState().equals("Ship")) {
			setAIEntity(new SimpleSearchAndDestroyShipAIEntity(getOwner(), !get(Types.ACTIVE).isOn()));
		} else if (s.getCurrentState().equals("Fleet")) {
			setAIEntity(new FleetControllableShipAIEntity(getOwner(), false));
			if(!get(Types.ACTIVE).isOn()){
				((AIConfiguationElements<Boolean>)get(Types.ACTIVE)).setCurrentState(true, true);
			}
		}

	}

}
