using Element;
using Networking;
using Settings;
using UnityEngine;
using Universe.Data.Common;
using Universe.Data.Common.Resource;
using Universe.Data.Inventory;
using Universe.World;

namespace Universe.Data.Server {
	public class GameServerState : GameState {

		bool _initialized;

		public DatabaseManager DatabaseManager { get; private set; }

		public static GameServerState Instance { get; private set; }

		public override InventoryController InventoryController { get; protected set; }

		public void StartTimedShutdown(int seconds) {
			Debug.Log($"Server will shutdown in {seconds} seconds.");
			//Todo: Notify players of impending shutdown
			Invoke(nameof(Shutdown), seconds);
		}

		public override void Shutdown(bool restart = false) {
			Debug.Log("Shutting down server...");
			NetworkState?.Shutdown();
			DatabaseManager?.Shutdown();
			if(restart) {
				//Todo: Implement server restart
			}
		}

		public override ResourceManager ResourceManager {
			get => throw new System.NotImplementedException("GameServerState does not have a ResourceManager");
			set => throw new System.NotImplementedException("GameServerState does not have a ResourceManager");
		}

		public void Initialize(params string[] args) {
			if(_initialized) return;
			Debug.Log("Initializing GameServerState");
			Instance = this;
			NetworkState = new ServerNetworkState();
			ElementConfig elementConfig = new ElementConfig();
			elementConfig.LoadElementConfig();
			ServerConfig config = new ServerConfig();
			config.LoadServerConfig();
			DatabaseManager = new DatabaseManager(config.WorldName.Value);
			GameStateData = new GameStateData {
				WorldName = config.WorldName.Value,
				ServerIP = "localhost",
				ServerPort = config.ServerPort.Value,
				ElementConfig = elementConfig,
				ServerConfig = config,
			};
		}
	}
}