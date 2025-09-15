using System;

namespace Networking {
	public class ClientNetworkState : NetworkState {

		public void RequestFromServer(RequestType requestType, System.Action<byte[]> callback = null, params object[] args) {

		}

		public void HandleServerRequest(RequestType requestType, System.Action<byte[]> callback = null, params object[] args) {

		}

		public override void RequestInventory(string inventoryUid, Action<object> action) {
			throw new NotImplementedException();
		}
	}
}