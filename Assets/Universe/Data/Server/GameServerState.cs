using System.IO;
using Element;
using Networking;
using Settings;
using UnityEngine;
using Universe.Data.Inventory;
using Universe.World;

namespace Universe.Data.Server {
	public class GameServerState : GameState {
		public DatabaseManager DatabaseManager { get; private set; }

		bool _initialized;

		public static GameServerState Instance { get; private set; }

		public override InventoryController InventoryController { get; protected set; }

		void Start() {
			//Todo: Rather than calling on Start(), we should have a proper GameStateManager that handles game states and initialization
			Initialize();
		}


		public void Initialize() {
			if(_initialized) return;
			Debug.Log("Initializing GameServerState");
			Instance = this;
			NetworkState = new ServerNetworkState();
			ElementConfig elementConfig = new ElementConfig();
			elementConfig.LoadElementConfig();
			ServerConfig config = new ServerConfig();
			config.LoadServerConfig();
			DatabaseManager = new DatabaseManager();
			DatabaseManager.Initialize(Path.Combine(Application.persistentDataPath, $"Database{Path.DirectorySeparatorChar}{config.WorldName.Value}"));

			GameStateData = new GameStateData {
				WorldName = config.ServerName.Value,
				WorldIP = "localhost",
				WorldPort = config.ServerPort.Value,
				ElementConfig = elementConfig,
				ServerConfig = config
			};
		}
	}
}