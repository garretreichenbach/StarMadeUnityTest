using System.Collections.Generic;
using Element;
using Unity.VisualScripting.Dependencies.Sqlite;

namespace Universe.Data.Inventory {
	public class Inventory {
		[PrimaryKey]
		public string Uid { get; set; } //Unique identifier for the inventory
		public List<InventorySlot> slots { get; set; } = new List<InventorySlot>();
	}

	public struct InventorySlot {
		public short ID { get; set; }
		public int Count { get; set; }
		public int MetaId { get; set; }

		public ElementInfo GetElementInfo() {
			return ElementConfig.GetInfo(ID);
		}
	}
}