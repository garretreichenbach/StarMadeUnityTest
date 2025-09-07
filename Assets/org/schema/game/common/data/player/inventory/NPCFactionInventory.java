package org.schema.game.common.data.player.inventory;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.data.SegmentPiece;

public class NPCFactionInventory extends ShopInventory {

	private final GameStateInterface state;

	public NPCFactionInventory(GameStateInterface state, long parameter) {
		super(null, parameter);
		assert(state != null);
		this.state = state;
	}

	
	@Override
	public InventoryHolder getInventoryHolder() {
		setInventoryHolder(state.getGameState());
		return state.getGameState();
	}

	@Override
	public long getParameterIndex() {
		return getParameter();
	}
	@Override
	public double getCapacity() {
		return super.getCapacity();
	}


	public static int getInventoryType() {
		return NPC_FACTION_INVENTORY;
	}

	@Override
	public int getActiveSlotsMax() {
		return 0;
	}

	
	@Override
	public int getLocalInventoryType() {
		return NPC_FACTION_INVENTORY;
	}

	@Override
	public SegmentPiece getBlockIfPossible() {
		return null;
	}
	@Override
	public String getCustomName() {
		return "";
	}

	@Override
	public int getMaxStock() {
		return Integer.MAX_VALUE;
	}

}
