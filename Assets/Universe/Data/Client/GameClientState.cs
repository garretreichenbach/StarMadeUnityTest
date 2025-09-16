using Networking;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.Client.Player;
using Universe.Data.Common;
using Universe.Data.Inventory;
using ResourceManager = Universe.Data.Common.Resource.ResourceManager;

namespace Universe.Data.Client {
	public class GameClientState : GameState {
		bool _initialized;

		ClientNetworkState _networkState;
		PlayerState _playerState;

		public static GameClientState Instance { get; private set; }

		public override InventoryController InventoryController { get; protected set; }

		public bool IsConnected { get; set; }

		public override ResourceManager ResourceManager { get; set; }

		public override void Shutdown(bool restart = false) { }

		public void Initialize(params string[] args) {
			if(_initialized) return;
			Instance = this;
			Debug.Log("Initializing GameClientState");
			ResourceManager = new ResourceManager(this);
			_networkState = new ClientNetworkState();
			_networkState.RequestFromServer(RequestType.GameStateData,
				data => {
					GameStateData = new GameStateData();
					GameStateData.Load(data);
					InventoryController = new InventoryController(this);
					InitializePlayerState();
				});
			ChunkMemoryManager = new ChunkMemoryManager(this);
			_initialized = true;
		}

		public void InitializePlayerState() {
			_playerState = gameObject.AddComponent<PlayerState>();
			_playerState.Initialize(this);
		}
	}
}