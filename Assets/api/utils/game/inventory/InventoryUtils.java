package api.utils.game.inventory;

import api.DebugFile;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;

/**
 * Created by Jake on 9/30/2020.
 * <insert description here>
 */
public class InventoryUtils {

    public static void addItem(Inventory inventory, short id, int amount) {
        int slot = inventory.incExistingOrNextFreeSlot(id, amount);
        if(inventory.getInventoryHolder() != null) inventory.sendInventoryModification(slot);
    }

    public static boolean addElementById(Inventory inventory, short id, int amount) {
        if(ElementKeyMap.isValidType(id)) {
            int i = inventory.incExistingOrNextFreeSlot(id, amount);
            inventory.sendInventoryModification(i);
            return true;
        } else return false;
    }

    public static int getItemAmount(Inventory inventory, short id) {
        if(!inventory.containsAny(id)) return 0;
        else {
            int count = 0;
            for(int slotNum : inventory.getSlots()) {
                InventorySlot slot = inventory.getSlot(slotNum);
                if(!slot.isMetaItem()) {
                    if(slot.isMultiSlot()) {
                        for(InventorySlot subSlot : slot.getSubSlots()) {
                            if(subSlot.getType() == id) count += subSlot.count();
                        }
                    } else if(slot.getType() == id) count += slot.count();
                }
            }
            return count;
        }
    }

    public static void consumeItems(Inventory inventory, short id, int toConsume) {
        toConsume = Math.abs(toConsume); //Idiot-proof
        if(!inventory.containsAny(id)) DebugFile.err("Tried to consume x" + toConsume + " " + id + "in an inventory, however the inventory did not contain any!");
        else {
            for(int slotNum : inventory.getSlots()) {
                if(toConsume <= 0) break;
                InventorySlot slot = inventory.getSlot(slotNum);
                if(!slot.isMetaItem()) {
                    if(slot.isMultiSlot()) {
                        for(InventorySlot subSlot : slot.getSubSlots()) {
                            if(subSlot.getType() == id) {
                                if(subSlot.count() >= toConsume) {
                                    subSlot.setCount(subSlot.count() - toConsume);
                                    toConsume = 0;
                                    break;
                                } else {
                                    subSlot.clear();
                                    toConsume -= subSlot.count();
                                }
                            }
                        }
                    } else {
                        if(slot.getType() == id) {
                            if(slot.count() >= toConsume) {
                                slot.setCount(slot.count() - toConsume);
                                toConsume = 0;
                            } else {
                                slot.clear();
                                toConsume -= slot.count();
                            }
                        }
                    }
                }
            }
            inventory.sendAll();
        }
    }
}
