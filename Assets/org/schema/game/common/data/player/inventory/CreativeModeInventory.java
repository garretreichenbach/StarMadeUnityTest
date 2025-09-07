package org.schema.game.common.data.player.inventory;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.schine.resource.tag.Tag;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CreativeModeInventory extends Inventory {


	boolean fillMode = false;
	public CreativeModeInventory(InventoryHolder state, long parameter) {
		super(state, parameter);
		List<ElementInformation> infoElementsRecursive = ElementKeyMap.getCategoryHirarchy().getInfoElementsRecursive(new ObjectArrayList<ElementInformation>());
		for(ElementInformation k : infoElementsRecursive){
			//maybe dont add certain blocks
			if(k.isShoppable()){
			}
		}
		fillMode = true;
		for(ElementInformation k : infoElementsRecursive){
			try {
				if(k.isShoppable() && k.getSourceReference() == 0){
					super.incExistingOrNextFreeSlot(k.getId(), 1, -1, getActiveSlotsMax());
				}
			} catch (NoSlotFreeException e) {
				e.printStackTrace();
			}
		}
		fillMode = false;
	}

	@Override
	public void fromTagStructure(Tag tag) {
	}

	@Override
	public Tag toTagStructure() {
		return super.toTagStructure();
	}

	public static int getInventoryType() {
		return CREATIVE_MODE_INVENTORY;
	}

	@Override
	public int getActiveSlotsMax() {
		return 10;
	}

	

	@Override
	public int getLocalInventoryType() {
		return CREATIVE_MODE_INVENTORY;
	}


	@Override
	public String getCustomName() {
		return "";
	}

	@Override
	public void clear() {
	}

	@Override
	public void clear(IntOpenHashSet mod) {
	}

	@Override
	public void decreaseBatch(ElementCountMap elementMap, IntOpenHashSet mod)
			throws NotEnoughBlocksInInventoryException {
	}

	@Override
	public void decreaseBatch(short type, int amount, IntOpenHashSet mod) {
	}


//	@Override
//	public int getCount(int slot, short type) {
//		return getType(slot) == InventorySlot.MULTI_SLOT ? super.getCount(slot, type) :( !isSlotEmpty(slot) ? 99999 : 0);
//	}

	




	@Override
	public boolean isInfinite() {
		return true;
	}
	
	@Override
	public void handleReceived(InventoryMultMod a,
			NetworkInventoryInterface inventoryInterface) {
	}

	@Override
	public void inc(int slot, short type, int count) {
		if(fillMode){
			super.inc(slot, type, count);
		}
	}

	@Override
	public int incExisting(short type, int count) throws NoSlotFreeException {
		if(fillMode){
			return super.incExisting(type, count);
		}
		return -1;
	}

	@Override
	public int incExistingAndSend(short type, int count,
			NetworkInventoryInterface o) throws NoSlotFreeException {
		if(fillMode){
			return super.incExistingAndSend(type, count, o);
		}
		return -1;
	}

	@Override
	public int incExistingOrNextFreeSlot(short type, int count){
		if(fillMode){
			return super.incExistingOrNextFreeSlot(type, count);
		}
		return -1;
	}

	@Override
	public int incExistingOrNextFreeSlotWithoutException(short type, int count) {
		if(fillMode){
			return super.incExistingOrNextFreeSlotWithoutException(type, count);
		}
		return -1;
	}

	@Override
	public int incExistingOrNextFreeSlot(short type, int count, int metaId) {
		if(fillMode){
			return super.incExistingOrNextFreeSlot(type, count, metaId);
		}
		return -1;
	}

	@Override
	public int incExistingOrNextFreeSlotWithoutException(short type, int count,
			int metaId) {
		if(fillMode){
			return super.incExistingOrNextFreeSlotWithoutException(type, count, metaId);
		}
		return -1;
	}



	@Override
	public int putNextFreeSlot(short type, int count, int metaId, int startSlot) {
		if(fillMode){
			return super.putNextFreeSlot(type, count, metaId, startSlot);
		}
		return -1;
	}

	@Override
	public int putNextFreeSlotWithoutException(short type, int count, int metaId) {
		if(fillMode){
			return super.putNextFreeSlotWithoutException(type, count, metaId);
		}
		return -1;
	}

	@Override
	public int putNextFreeSlotWithoutException(short type, int count,
			int metaId, int startSlot) {
		if(fillMode){
			return super.putNextFreeSlotWithoutException(type, count, metaId, startSlot);
		}
		return -1;
	}

	@Override
	public int putNextFreeSlot(short type, int count, int metaId) {
		if(fillMode){
			return super.putNextFreeSlot(type, count, metaId);
		}
		return -1;
	}


	@Override
	public void sendAll() {
	}

//	@Override
//	public void sendInventoryModification(Collection<Integer> changedSlotsOthers) {
//	}
//
//	@Override
//	public void sendInventoryModification(int slot) {
//	}
//
	@Override
	public void serialize(DataOutput buffer) throws IOException {
	}
//
//	@Override
//	public void spawnInSpace(SimpleTransformableSendableObject owner) {
//	}

	@Override
	public void doSwitchSlotsOrCombine(int slot, int otherSlot,
			int subSlotFromOther, Inventory otherInventory, int count,
			Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> moddedSlots)
			throws InventoryExceededException {
		super.doSwitchSlotsOrCombine(slot, otherSlot, subSlotFromOther, otherInventory,
				count, moddedSlots);
	}



	@Override
	public void splitUpMulti(int slot) {
	}

	@Override
	public void removeMetaItem(MetaObject object) {
	}

	@Override
	public boolean conatainsMetaItem(MetaObject object) {
		return false;
	}
	
	

}
