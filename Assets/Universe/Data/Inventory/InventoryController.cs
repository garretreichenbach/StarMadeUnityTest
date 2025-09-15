using System.Collections.Generic;
using System.Threading.Tasks;
using Universe.Data.Server;
using Universe.World;

namespace Universe.Data.Inventory {
	public class InventoryController {

		Dictionary<string, Inventory> _clientInventoryCache = new Dictionary<string, Inventory>();
		public GameState GameState { get; private set; }

		public InventoryController(GameState gameState) {
			GameState = gameState;
		}

		public Inventory GetInventory(string inventoryUid) {
			if(GameState.IsClient()) {
				if(_clientInventoryCache.TryGetValue(inventoryUid, out var cachedInventory)) {
					return cachedInventory;
				}
				return RequestInventoryFromServer(inventoryUid);
			}
			((GameServerState) GameState).DatabaseManager.Load(DataType.InventoryData, inventoryUid, out object inventory);
			return (Inventory) inventory;
		}

		Inventory RequestInventoryFromServer(string inventoryUid) {
			var tcs = new TaskCompletionSource<Inventory>();
			GameState.NetworkState.RequestInventory(inventoryUid, (inv) => {
				_clientInventoryCache[inventoryUid] = inv as Inventory;
				tcs.SetResult(inv as Inventory);
			});
			return tcs.Task.Result;
		}
	}
}