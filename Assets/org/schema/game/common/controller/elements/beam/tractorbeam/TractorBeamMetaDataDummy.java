package org.schema.game.common.controller.elements.beam.tractorbeam;

import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorBeamHandler.TractorMode;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class TractorBeamMetaDataDummy extends BlockMetaDataDummy {

	public TractorMode mode;

	@Override
	protected void fromTagStructrePriv(Tag tag, int shift) {
		mode = TractorMode.values()[tag.getByte()];
	}

	@Override
	public String getTagName() {
		return TractorElementManager.TAG_ID;
	}
	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.BYTE, null, (byte)mode.ordinal());
	}
}
