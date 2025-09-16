using System;
using UnityEditor;
using UnityEngine;
using Universe.Data.Client;
using Universe.Data.Client.MainMenu;
using Universe.Data.Common;
using Universe.Data.Server;

namespace Universe.Data {

	[CustomEditor(typeof(GameStateManager))]
	public class GameStateEditor : Editor {

		internal enum AutoStartMode {
			None,
			MainMenu,
			SinglePlayer,
			Client,
			Server,
		}

		internal static AutoStartMode AutoStart = AutoStartMode.MainMenu;
		string _ip = "localhost";
		int _port = 4242;

		public override void OnInspectorGUI() {
			DrawDefaultInspector();

			GameStateManager gsm = (GameStateManager)target;
			GUILayout.Label("Starters");
			if(GUILayout.Button("Start Main Menu")) {
				gsm.MainMenuStart();
			}
			if(GUILayout.Button("Start Single Player")) {
				gsm.SinglePlayerStart("localhost", 4242);
			}
			if(GUILayout.Button("Start Client")) {
				gsm.ServerStart(_ip, _port);
			}
			if(GUILayout.Button("Start Server")) {
				gsm.ServerStart(_ip, _port);
			}
			if(GUILayout.Button("Shutdown")) {
				gsm.Shutdown();
			}
			GUILayout.Label("Settings");
			AutoStart = (AutoStartMode)EditorGUILayout.EnumFlagsField("Auto Start Mode", AutoStart);
			_ip = EditorGUILayout.TextField("IP Address", _ip);
			_port = EditorGUILayout.IntField("Port", _port);
		}
	}

	public class GameStateManager : MonoBehaviour {

		static GameStateData _stateData;
		static MainMenuState _mainMenuState;
		static GameClientState _clientState;
		static GameServerState _serverState;

		void Start() {
			//Get run args
			string[] args = Environment.GetCommandLineArgs();
			if((GameStateEditor.AutoStart & GameStateEditor.AutoStartMode.MainMenu) != 0) {
				MainMenuStart(args);
			} else if((GameStateEditor.AutoStart & GameStateEditor.AutoStartMode.SinglePlayer) != 0) {
				SinglePlayerStart("localhost", 4242, args);
			} else if((GameStateEditor.AutoStart & GameStateEditor.AutoStartMode.Client) != 0) {
				ClientStart("localhost", 4242, args);
			} else if((GameStateEditor.AutoStart & GameStateEditor.AutoStartMode.Server) != 0) {
				ServerStart("localhost", 4242, args);
			}
		}

		public void MainMenuStart(params string[] args) {
			if(_clientState != null || _serverState != null || _mainMenuState != null) {
				return;
			}
			_stateData = new GameStateData {
				CurrentGameMode = GameStateData.GameMode.MainMenu
			};
			GameObject mainMenuObj = new GameObject("MainMenuState");
			MainMenuState mainMenuState = mainMenuObj.AddComponent<MainMenuState>();
			mainMenuState.GameStateData = _stateData;
			_mainMenuState = mainMenuState;
		}

		public void SinglePlayerStart(string ip, int port, params string[] args) {
			if(_clientState != null || _serverState != null) {
				return;
			}
			_stateData = new GameStateData {
				ServerIP = ip,
				ServerPort = port,
				CurrentGameMode = GameStateData.GameMode.SinglePlayer
			};
			GameObject serverObj = new GameObject("GameServerState");
			GameServerState serverState = serverObj.AddComponent<GameServerState>();
			serverState.GameStateData = _stateData;
			_serverState = serverState;
			serverState.Initialize(args);
			GameObject clientObj = new GameObject("GameClientState");
			GameClientState clientState = clientObj.AddComponent<GameClientState>();
			clientState.GameStateData = _stateData;
			_clientState = clientState;
			clientState.Initialize(args);
		}

		public void ClientStart(string ip, int port, params string[] args) {
			if(_clientState != null) {
				return;
			}
			_stateData = new GameStateData {
				ServerIP = ip,
				ServerPort = port,
				CurrentGameMode = GameStateData.GameMode.MultiplayerClient
			};
			GameObject clientObj = new GameObject("GameClientState");
			GameClientState clientState = clientObj.AddComponent<GameClientState>();
			clientState.GameStateData = _stateData;
			_clientState = clientState;
			clientState.Initialize(args);
		}

		public void ServerStart(string ip, int port, params string[] args) {
			if(_serverState != null) {
				return;
			}
			_stateData = new GameStateData {
				ServerIP = ip,
				ServerPort = port,
				CurrentGameMode = GameStateData.GameMode.DedicatedServer,
			};
			GameObject serverObj = new GameObject("GameServerState");
			GameServerState serverState = serverObj.AddComponent<GameServerState>();
			serverState.GameStateData = _stateData;
			_serverState = serverState;
			serverState.Initialize(args);
		}

		public void Shutdown() {
			switch(_stateData.CurrentGameMode) {
				case GameStateData.GameMode.SinglePlayer:
					if(_clientState != null) {
						_clientState.Shutdown();
						_clientState = null;
					}
					if(_serverState != null) {
						_serverState.Shutdown();
						_serverState = null;
					}
					break;

				case GameStateData.GameMode.MultiplayerClient:
					if(_clientState != null) {
						_clientState.Shutdown();
						_clientState = null;
					}
					break;

				case GameStateData.GameMode.DedicatedServer:
					if(_serverState != null) {
						_serverState.Shutdown();
						_serverState = null;
					}
					break;

				default:
					throw new Exception($"Unknown game mode: {_stateData.CurrentGameMode}");

			}
		}

		public static GameState GetCurrentState() {
			return _stateData.CurrentGameMode switch {
				GameStateData.GameMode.SinglePlayer => _serverState, //Prefer server state for single player
				GameStateData.GameMode.MultiplayerClient => _clientState,
				GameStateData.GameMode.DedicatedServer => _serverState,
				GameStateData.GameMode.MainMenu => _mainMenuState,
			_ => throw new Exception($"Unknown game mode: {_stateData.CurrentGameMode}"),
			};
		}
	}
}