using System;
using Universe.Data;

namespace Networking {
	public class ServerNetworkState : NetworkState {

		void RequestFromClient(RequestType requestType, System.Action<byte[]> callback = null, params object[] args) {

		}

		void HandleClientRequest(RequestType requestType, System.Action<byte[]> callback = null, params object[] args) {

		}

		public override void RequestInventory(string inventoryUid, Action<object> action) {

		}
	}
}