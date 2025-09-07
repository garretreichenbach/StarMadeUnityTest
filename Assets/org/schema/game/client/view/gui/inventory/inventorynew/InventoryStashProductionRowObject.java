package org.schema.game.client.view.gui.inventory.inventorynew;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;

public class InventoryStashProductionRowObject {
	public short type;
	public int amount;
	public ElementInformation info;
	private short metaType;
	private short subId;
	private MetaObject meta;
	public int fillUpTo;

	public InventoryStashProductionRowObject(short type, int amount, int fillUpTo) {
		super();
		this.type = type;
		this.amount = amount;
		this.fillUpTo = fillUpTo;
		if(type > 0){
			this.info = ElementKeyMap.getInfo(type);
		}else{
			int id = type;
			if(type > -256){
				this.metaType = type;
				meta = MetaObjectManager.instantiate(metaType, (short)-1, false);
			}else{
				this.metaType = (short) -(Math.abs(type)/256);
				this.subId = (short) ((Math.abs(type)%256)+this.metaType);
				meta = MetaObjectManager.instantiate(metaType, subId, false);
			}
		}
	}

	@Override
	public int hashCode() {
		return type;
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		return o instanceof InventoryStashProductionRowObject
				&& type == ((InventoryStashProductionRowObject) o).type && 
				amount == ((InventoryStashProductionRowObject) o).amount &&
				fillUpTo == ((InventoryStashProductionRowObject) o).fillUpTo;
	}

	public String getName() {
		if(meta != null){
			return meta.getName();
		}else{
			return info.getName();
		}
	}
}
