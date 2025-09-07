package org.schema.game.common.data.player.inventory;

import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class InventoryFilter {
	private static final byte VERSION = 0;
	public TypeAmountFastMap filter = new TypeAmountFastMap();
	public TypeAmountFastMap fillUpTo = new TypeAmountFastMap();
	public long inventoryId;
	
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, VERSION),
				filter.toTagStructure(),
				fillUpTo.toTagStructure(),
			FinishTag.INST
		});
	}
	public void fromTagStructure(Tag tag) {
		Tag[] s = tag.getStruct();
		
		if(s.length > 0){
			if(s[0].getType() == Type.BYTE){
				byte version = s[0].getByte();
				filter.fromTagStructure(s[1]);
				fillUpTo.fromTagStructure(s[2]);
			}else{
				//old
				filter.fromTagStructure(tag);
			}
		}
		
	}
	public void received(InventoryFilter inventoryFilter) {
		filter = inventoryFilter.filter;
		fillUpTo = inventoryFilter.fillUpTo;
	}
}
