package org.schema.game.common.controller.elements.dockingBlock;

import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class DockingMetaDataDummy extends BlockMetaDataDummy {

	public byte orientation;

	@Override
	protected void fromTagStructrePriv(Tag tag, int shift) {
		orientation = (Byte) tag.getValue();
	}
	@Override
	public String getTagName() {
		return DockingBlockElementManager.TAG_ID;
	}
	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.BYTE, null, orientation);
	}
}
