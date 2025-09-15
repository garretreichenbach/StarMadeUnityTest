using Element;
using UnityEngine;

namespace Universe.Data.Inventory {
	public class PlayerInventory : Inventory {

		int _selectedHotbarSlot;

		public void SelectNext() {
			_selectedHotbarSlot = (_selectedHotbarSlot + 1) % 10;
		}

		public void SelectPrevious() {
			_selectedHotbarSlot = (_selectedHotbarSlot + 9) % 10;
		}

		public InventorySlot GetSelectedSelectedSlot() {
			return slots[_selectedHotbarSlot];
		}

		public int GetSelectedSelectedSlotIndex() {
			return _selectedHotbarSlot;
		}

		/**
		* Attempts to add the given amount of the given type to the inventory, starting from the first slot and iterating through all slots until it finds a
		* matching type, or if none are found will add to an empty slot.
		* Will check the selected slot first, then the first slot, then the second slot, etc.
		*/
		public bool AddToAnySlot(short type, int count) {
			int added = 0;
			// First try to add to the selected slot
			// Then try to add to the first, and so on until we find a matching slot or run out of slots
			// If we run out of slots, add to an empty slot
			// Return true if we added to at least one slot
			for(int i = 0; i < slots.Count; i++) {
				int slotIndex = (i + _selectedHotbarSlot) % slots.Count;
				InventorySlot slot = slots[slotIndex];
				if(slot.ID == type) {
					int spaceInSlot = slot.Count;
					if(spaceInSlot > 0) {
						int toAdd = Mathf.Min(spaceInSlot, count - added);
						slot.Count += toAdd;
						added += toAdd;
						slots[slotIndex] = slot; // Update the slot in the inventory
						if(added == count) {
							return true; // Added all we needed
						}
					}
				}
			}
			// If we reach here, we didn't add all we needed, try to add to empty slots
			for(int i = 0; i < slots.Count; i++) {
				int slotIndex = (i + _selectedHotbarSlot) % slots.Count;
				InventorySlot slot = slots[slotIndex];
				if(slot.ID == 0) { // Empty slot
					int toAdd = count - added;
					slot.ID = type;
					slot.Count = toAdd;
					added += toAdd;
					slots[slotIndex] = slot; // Update the slot in the inventory
					if(added == count) {
						return true; // Added all we needed
					}
				}
			}
			return added > 0; // Return true if we added at least something
		}

		/**
		* Removes the given amount of the given type from the inventory, starting from the first slot and iterating through all slots.
		* Returns true if the operation was successful, false if there was not enough of the given type in the inventory.
		* Will check the selected slot first, then the first slot, then the second slot, etc.
		*/
		public bool RemoveFromAnySlot(short type, int count) {
			int removed = 0;
			for(int i = 0; i < slots.Count; i++) {
				int slotIndex = (i + _selectedHotbarSlot) % slots.Count;
				InventorySlot slot = slots[slotIndex];
				if(slot.ID == type) {
					int toRemove = Mathf.Min(slot.Count, count - removed);
					slot.Count -= toRemove;
					removed += toRemove;
					if(slot.Count == 0) {
						slot.ID = 0; // Clear the slot if empty
					}
					slots[slotIndex] = slot; // Update the slot in the inventory
					if(removed == count) {
						return true; // Removed all we needed
					}
				}
			}
			return removed == count; // Return true if we removed all we needed
		}
	}
}