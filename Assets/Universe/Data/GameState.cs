using System;
using Element;
using Networking;
using Settings;
using UnityEngine;
using Universe.Data.Client;
using Universe.Data.Inventory;
using Universe.Data.Server;

namespace Universe.Data {

	public abstract class GameState : MonoBehaviour {
		public GameStateData GameStateData {
			get;
			protected set;
		}

		public abstract InventoryController InventoryController {
			get;
			protected set;
		}

		public NetworkState NetworkState {
			get;
			protected set;
		}

		public bool IsClient() => this is GameClientState;

		public bool IsServer() => this is GameServerState;
	}

	public struct GameStateData {
		public string WorldName;
		public string WorldIP;
		public int WorldPort;
		public ElementConfig ElementConfig;
		public ServerConfig ServerConfig;

		public bool IsConnectedToServer => WorldIP != "localhost";

		/**
		* Deserializes GameStateData from a byte array.
		*/
		public void Load(byte[] data) {
			using(var stream = new System.IO.MemoryStream(data)) {
				using(var reader = new System.IO.BinaryReader(stream)) {
					WorldName = reader.ReadString();
					WorldIP = reader.ReadString();
					WorldPort = reader.ReadInt32();
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
					writer.Write(WorldIP);
					writer.Write(WorldPort);
				}
				data = stream.ToArray();
			}
		}
	}
}