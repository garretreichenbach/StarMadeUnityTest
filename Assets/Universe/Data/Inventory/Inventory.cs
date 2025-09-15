using System.Collections.Generic;
using Element;

namespace Universe.Data.Inventory {
	public class Inventory {
		public List<InventorySlot> slots = new List<InventorySlot>(10);
	}

	public struct InventorySlot {
		public short id;
		public int count;
		public int metaId;

		public ElementInfo GetElementInfo() {
			return ElementMap.GetInfo(id);
		}
	}
}