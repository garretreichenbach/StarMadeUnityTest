package org.schema.game.common.controller.elements.jumpprohibiter;

import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class JumpInhibitorMetaDataDummy extends BlockMetaDataDummy {

	public boolean active;

	@Override
	protected void fromTagStructrePriv(Tag tag, int shift) {
		active = (Byte) tag.getValue() != 0;
	}

	@Override
	public String getTagName() {
		return JumpInhibitorElementManager.TAG_ID;
	}
	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.BYTE, null, active ? (byte)1 : (byte)0);
	}
}
