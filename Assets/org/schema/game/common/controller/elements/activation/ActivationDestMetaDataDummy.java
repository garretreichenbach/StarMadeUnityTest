package org.schema.game.common.controller.elements.activation;

import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.data.element.meta.weapon.MarkerBeam;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class ActivationDestMetaDataDummy extends BlockMetaDataDummy {

	public MarkerBeam dest;

	@Override
	protected void fromTagStructrePriv(Tag tag, int shift) {
		Tag[] v = ((Tag[]) tag.getValue());
		if (v[0].getType() == Type.STRUCT) {
			dest = MarkerBeam.getMarkingFromTag(v[0], shift);
		}
	}
	@Override
	public String getTagName() {
		return ActivationElementManager.TAG_ID;
	}
	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.STRUCT, null, new Tag[]{
			dest.toTag(),
			FinishTag.INST,
			
		});
	}	
}
