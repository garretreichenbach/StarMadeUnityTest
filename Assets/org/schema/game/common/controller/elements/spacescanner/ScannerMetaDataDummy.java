package org.schema.game.common.controller.elements.spacescanner;

import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class ScannerMetaDataDummy extends BlockMetaDataDummy {

	public float charge;

	@Override
	protected void fromTagStructrePriv(Tag tag, int shift) {
		charge = (Float) tag.getValue();
	}

	@Override
	public String getTagName() {
		return LongRangeScannerElementManager.TAG_ID;
	}
	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.FLOAT, null, charge);
	}
}
