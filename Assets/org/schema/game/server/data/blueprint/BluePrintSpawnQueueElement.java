package org.schema.game.server.data.blueprint;

import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.inventory.Inventory;

public class BluePrintSpawnQueueElement {
	public final String catalogName;
	public final String shipName;
	public boolean activeAI;
	public boolean infiniteShop;
	public boolean directBuy;
	public int factionId;
	public int metaItem = -1;
	public Inventory inv;
	public SegmentPiece toDockOn;
	public BluePrintSpawnQueueElement(String name, String shipName, int factionId, boolean infiniteShop, boolean activeAI, boolean directBuy, SegmentPiece toDockOn) {
		super();
		this.catalogName = name;
		this.shipName = shipName;
		this.activeAI = activeAI;
		this.infiniteShop = infiniteShop;
		this.directBuy = directBuy;
		this.factionId = factionId;
		this.toDockOn = toDockOn;
	}

}
