package org.schema.game.server.ai.program.simpirates.states;

import java.io.IOException;

import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.objects.Sendable;

public class RefillingShops extends SimulationGroupState<SimulationGroup> {

	/**
	 *
	 */
	
	private Sector s;

	public RefillingShops(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnter() {

		SimulationGroup simGroup = getSimGroup();
		try {
			s = simGroup.getState().getUniverse().getSector(((TargetProgram<?>) getEntityState().getCurrentProgram()).getSectorTarget());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		SimulationGroup simGroup = getSimGroup();
		if (s != null) {
			synchronized (simGroup.getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable g : simGroup.getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (g instanceof ShopSpaceStation && ((ShopSpaceStation) g).getSectorId() == s.getId()) {
						try {
							if (((ShopSpaceStation) g).getShoppingAddOn().isAIShop()) {
								((ShopSpaceStation) g).getShoppingAddOn().fillInventory(true, false);
							}
						} catch (NoSlotFreeException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		stateTransition(Transition.WAIT_COMPLETED);

		return false;
	}

}
