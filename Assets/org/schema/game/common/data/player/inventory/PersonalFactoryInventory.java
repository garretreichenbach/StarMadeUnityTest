package org.schema.game.common.data.player.inventory;

import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class PersonalFactoryInventory extends Inventory {

	private short factoryType;

	public PersonalFactoryInventory(InventoryHolder state, long parameter, short factoryType) {
		super(state, parameter);
		this.factoryType = factoryType;
	}

	public static int getInventoryType() {
		return PLAYER_INVENTORY;
	}

	public Tag toMetaData() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.SHORT, null, factoryType), FinishTag.INST});
	}

	private void fromMetaData(Tag tag) {
		Tag[] value = (Tag[]) tag.getValue();

		factoryType = ((Short) value[0].getValue());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.inventory.Inventory#fromTagStructure(org.schema.schine.resource.tag.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] value = (Tag[]) tag.getValue();

		fromMetaData(value[0]);
		super.fromTagStructure(value[1]);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.inventory.Inventory#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{toMetaData(), super.toTagStructure(), FinishTag.INST});
	}

	@Override
	public int getActiveSlotsMax() {
		return 0;
	}


	@Override
	public int getLocalInventoryType() {
		return PLAYER_INVENTORY;
	}


	@Override
	public String getCustomName() {
		return "";
	}

	/**
	 * @return the factoryType
	 */
	public short getFactoryType() {
		return factoryType;
	}

	/**
	 * @param factoryType the factoryType to set
	 */
	public void setFactoryType(short factoryType) {
		this.factoryType = factoryType;
	}

}
