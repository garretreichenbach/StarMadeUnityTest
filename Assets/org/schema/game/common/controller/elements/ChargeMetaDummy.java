package org.schema.game.common.controller.elements;

import org.schema.schine.resource.tag.Tag;

public class ChargeMetaDummy extends BlockMetaDataDummy{
	public float charge;
	private RecharchableSingleModule mod;
	public ChargeMetaDummy(RecharchableSingleModule mod) {
		this.mod = mod;
	}
	@Override
	protected void fromTagStructrePriv(Tag tag, int shift) {
		mod.fromTagStructrePriv(tag, shift);
	}
	
	
	@Override
	protected Tag toTagStructurePriv() {
		return mod.toTagStructurePriv();
	}
	@Override
	public String getTagName() {
		return mod.getTagId();
	}
}
