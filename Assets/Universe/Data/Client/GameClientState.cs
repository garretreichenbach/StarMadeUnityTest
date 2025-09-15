using Networking;
using UnityEngine;
using Universe.Data.Client.Player;
using Universe.Data.Inventory;

namespace Universe.Data.Client {
	public class GameClientState : GameState {

		public static GameClientState Instance { get; private set; }

		ClientNetworkState _networkState;
		bool _initialized;
		PlayerState _playerState;

		public override InventoryController InventoryController { get; protected set; }

		void Start() {
			//Todo: Rather than calling on Start(), we should have a proper GameStateManager that handles game states and initialization
			Initialize();
		}

		public void Initialize() {
			if(_initialized) return;
			Instance = this;
			Debug.Log("Initializing GameClientState");
			_networkState = new ClientNetworkState();
			_networkState.RequestFromServer(RequestType.GameStateData,
				data => {
					GameStateData = new GameStateData();
					GameStateData.Load(data);
					InventoryController = new InventoryController(this);
					InitializePlayerState();
				});
			_initialized = true;
		}

		public void InitializePlayerState() {
			_playerState = gameObject.AddComponent<PlayerState>();
			_playerState.Initialize(this);
		}
	}
}