using System;

namespace Networking {

	public enum RequestType {
		ServerInfo = 0,
		GameStateData = 1,
		PlayerData = 2,
		EntityData = 3,
		//Todo: More request types
	}

	public abstract class NetworkState {

		public abstract void RequestInventory(string inventoryUid, Action<object> action);

		public abstract void Shutdown();
	}
}