package org.schema.game.common.controller.elements;

import org.schema.schine.resource.tag.Tag;

public interface TagModuleUsableInterface {
	public BlockMetaDataDummy getDummyInstance();
	public String getTagId();
	public Tag toTagStructure();
}
