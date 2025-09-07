package org.schema.game.server.data.simulation.groups;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShopperInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class TargetSectorSimulationGroup extends ShipSimulationGroup {

	
	public Vector3i targetSector;
	public boolean hasStockToDeliver;

	public TargetSectorSimulationGroup(GameServerState state) {
		super(state);
	}

	public TargetSectorSimulationGroup(GameServerState state, Vector3i targetSector) {
		super(state);
		this.targetSector = targetSector;
	}

	private void deliverStock() {
		for (String s : getMembers()) {
			if (isLoaded(s)) {
				SegmentController segmentController = getState().getSegmentControllersByName().get(s);
				if (segmentController != null && segmentController instanceof ShopperInterface) {
					ShopperInterface si = (ShopperInterface) segmentController;
					if (!si.getShopsInDistance().isEmpty()) {
						ShopInterface shop = si.getShopsInDistance().iterator().next();
						System.err.println("[SIMULATION] " + this + " filling stock of: " + shop);
						try {
							shop.fillInventory(true, false);
						} catch (NoSlotFreeException e) {
							e.printStackTrace();
						}
						hasStockToDeliver = false;
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#getMetaData()
	 */
	@Override
	protected Tag getMetaData() {
		return new Tag(Type.VECTOR3i, null, targetSector);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#getType()
	 */
	@Override
	public GroupType getType() {
		return GroupType.TARGET_SECTOR;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#handleMetaData(org.schema.schine.resource.Tag)
	 */
	@Override
	protected void handleMetaData(Tag metadata) {
		targetSector = (Vector3i) metadata.getValue();
		if (getCurrentProgram() != null) {
			((TargetProgram<?>) getCurrentProgram()).setSectorTarget(targetSector);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.groups.SimulationGroup#onWait()
	 */
	@Override
	public void onWait() {
		super.onWait();

		if (hasStockToDeliver) {

			deliverStock();

		}
	}

	@Override
	public void returnHomeMessage(PlayerState s) {
		s.sendServerMessage(new ServerMessage(
				Lng.astr("#### Transmission Start\nTarget has fled...\nReturn to base...\n#### Transmission End\n"), ServerMessage.MESSAGE_TYPE_WARNING, s.getId()));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#sendInvestigationMessage(org.schema.game.common.data.player.PlayerState)
	 */
	@Override
	public void sendInvestigationMessage(PlayerState s) {
		s.sendServerMessage(new ServerMessage(
				Lng.astr("#### Transmission Start\nHostile identified...\nExterminate...\n#### Transmission End\n"), ServerMessage.MESSAGE_TYPE_WARNING, s.getId()));
	}
}
