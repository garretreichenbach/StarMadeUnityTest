package org.schema.game.common.controller.elements.shipyard;

import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.schine.resource.tag.Tag;

public class ShipyardMetaDataDummy extends BlockMetaDataDummy {


	Tag tag;

	@Override
	protected void fromTagStructrePriv(Tag tag, int shift) {
		this.tag = tag;
	}

	@Override
	public String getTagName() {
		return ShipyardElementManager.TAG_ID;
	}
	@Override
	protected Tag toTagStructurePriv() {
		return tag;
	}
}
