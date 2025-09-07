package api.utils.game.inventory;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class ItemStack implements TagSerializable {

	private short id;
	private int amount;

	public ItemStack(Tag tag) {
		fromTagStructure(tag);
	}

	public ItemStack(short id, int amount) {
		this.id = id;
		this.amount = amount;
	}

	public short getId() {
		return id;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void addAmount(int amount) {
		this.amount += amount;
	}

	public ElementInformation getElementInfo() {
		return ElementKeyMap.getInfo(id);
	}

	public String getName() {
		return getElementInfo().getName();
	}

	@Override
	public void fromTagStructure(Tag tag) {
		id = tag.getStruct()[0].getShort();
		amount = tag.getStruct()[1].getInt();
	}

	@Override
	public Tag toTagStructure() {
		Tag idTag = new Tag(Tag.Type.SHORT, null, id);
		Tag amountTag = new Tag(Tag.Type.INT, null, amount);
		Tag[] struct = {idTag, amountTag};
		return new Tag(Tag.Type.STRUCT, null, struct);
	}
}
