using Element;
using Networking;
using Settings;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.Client;
using Universe.Data.Common.Resource;
using Universe.Data.Inventory;
using Universe.Data.Server;

namespace Universe.Data.Common {

	public abstract class GameState : MonoBehaviour {
		public GameStateData GameStateData {
			get;
			set;
		}

		public abstract InventoryController InventoryController {
			get;
			protected set;
		}

		public NetworkState NetworkState {
			get;
			protected set;
		}

		public ChunkMemoryManager ChunkMemoryManager {
			get;
			protected set;
		}

		public bool IsClient() => this is GameClientState;

		public bool IsServer() => this is GameServerState;

		public abstract void Shutdown(bool restart = false);

		public abstract ResourceManager ResourceManager { get; set; }

		void Start() {
			ResourceManager = new ResourceManager(this);
		}
	}

	public struct GameStateData {
		public enum GameMode {
			MainMenu,
			SinglePlayer,
			MultiplayerClient,
			DedicatedServer,
		}

		public string WorldName;
		public string ServerIP;
		public int ServerPort;
		public ElementConfig ElementConfig;
		public ServerConfig ServerConfig;

		public GameMode CurrentGameMode;

		/**
		* Deserializes GameStateData from a byte array.
		*/
		public void Load(byte[] data) {
			using(var stream = new System.IO.MemoryStream(data)) {
				using(var reader = new System.IO.BinaryReader(stream)) {
					WorldName = reader.ReadString();
					ServerIP = reader.ReadString();
					ServerPort = reader.ReadInt32();
					ElementConfig = new ElementConfig();
					ElementConfig.Read(reader);
					ServerConfig = new ServerConfig();
					ServerConfig.Read(reader);
				}
			}
		}

		/**
		* Serializes GameStateData into a byte array.
		*/
		public void Write(out byte[] data) {
			using(var stream = new System.IO.MemoryStream()) {
				using(var writer = new System.IO.BinaryWriter(stream)) {
					writer.Write(WorldName);
					writer.Write(ServerIP);
					writer.Write(ServerPort);
				}
				data = stream.ToArray();
			}
		}
	}
}