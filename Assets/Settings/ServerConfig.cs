using System;
using System.IO;
using Unity.VisualScripting;
using UnityEngine;
using Universe.Data;

namespace Settings {
	[Serializable]
	public class ServerConfig {

		string _settingsFilePath;

		public static ServerConfig Instance { get; private set; } = new ServerConfig();

		#region ServerInfo

		[Header("Server Info")]
		[InspectorLabel("Server Name")]
		[Tooltip("The name of the server as it will appear in server lists.")]
		public StringSettingsValue ServerName = new StringSettingsValue("Server Name", "The name of the server as it will appear in server lists.", "New Server");

		[InspectorLabel("Server IP")]
		[Tooltip("The IP address of the server. Leave empty for automatic detection.")]
		public StringSettingsValue ServerIP = new StringSettingsValue("Server IP", "The IP address of the server.", "");

		[InspectorLabel("Server Port")]
		[Tooltip("The port the server will listen on for incoming connections.")]
		public IntSettingsValue ServerPort = new IntSettingsValue("Server Port", "The port the server will listen on for incoming connections.", 4242, 1024, 65535);

		#endregion

		#region World Settings

		[Header("World Name")]
		[Tooltip("The name of the world to use.")]
		public StringSettingsValue WorldName = new StringSettingsValue("World Name", "The name of the world to use.", "world0");

		[Header("World Settings")]
		[InspectorLabel("Instant Commit")]
		[Tooltip("If true, changes to the database will be committed immediately. If false, changes will be batched and committed periodically.")]
		public BoolSettingsValue InstantCommit = new BoolSettingsValue("Instant Commit", "If true, changes to the database will be committed immediately. If false, changes will be batched and committed periodically.", false);

		[InspectorLabel("Database Auto Commit Interval (seconds)")]
		[Tooltip("If Instant Commit is false, this is the interval in seconds at which] changes will be committed to the database.")]
		public FloatSettingsValue DatabaseAutoCommitInterval = new FloatSettingsValue("Database Auto Commit Interval (seconds)", "If Instant Commit is false, this is the interval in seconds at which changes will be committed to the database.", 10.0f, 5.0f, 60.0f);

		[InspectorLabel("Sector Size")]
		[Tooltip("Size of each sector in the world.")]
		public IntSettingsValue SectorSize = new IntSettingsValue("Sector Size", "Size of each sector in the world.", 50000, 1000, 100000);

		[InspectorLabel("System Size")]
		[Tooltip("Size of each system in sectors.")]
		public IntSettingsValue SystemSize = new IntSettingsValue("System Size", "Size of each system in sectors.", 16, 4, 64);

		[InspectorLabel("Galaxy Radius")]
		[Tooltip("Radius of the Galaxy in systems.")]
		public IntSettingsValue GalaxyRadius = new IntSettingsValue("Galaxy Radius", "Radius of the Galaxy in systems.", 256, 100, 1000);

		#endregion

		/**
		* Loads the settings from the config file.
		*/
		public void LoadSettings() {
			if(File.Exists(_settingsFilePath)) {
				try {
					string json = File.ReadAllText(_settingsFilePath);
					FromJson(JsonUtility.FromJson<object>(json));
					Debug.Log("Settings loaded from " + _settingsFilePath);
				} catch(Exception e) {
					Debug.LogWarning("Failed to load settings from " + _settingsFilePath + ": " + e.Message);
					SetDefaults();
				}
			} else {
				Debug.LogWarning("Settings file not found at " + _settingsFilePath + ". Using default settings.");
				SetDefaults();
			}
		}

		/**
		* Saves the current settings to the config file.
		*/
		public void SaveSettings() {
			string json = JsonUtility.ToJson(ToJson(), true);
			File.WriteAllText(_settingsFilePath, json);
			Debug.Log("Settings saved to " + _settingsFilePath);
		}

		/**
		* Resets all settings to their default values and saves them.
		*/
		public void SetDefaults() {
			InstantCommit.SetValue(InstantCommit.DefaultValue);
			DatabaseAutoCommitInterval.SetValue(DatabaseAutoCommitInterval.DefaultValue);
			SectorSize.SetValue(SectorSize.DefaultValue);
			SystemSize.SetValue(SystemSize.DefaultValue);
			GalaxyRadius.SetValue(GalaxyRadius.DefaultValue);
			SaveSettings();
		}

		object ToJson() {
			return new {
				InstantCommit = InstantCommit.Value,
				DatabaseAutoCommitInterval = DatabaseAutoCommitInterval.Value,
				SectorSize = SectorSize.Value,
				SystemSize = SystemSize.Value,
				GalaxyRadius = GalaxyRadius.Value,
			};
		}

		void FromJson(object json) {
			var dict = json as System.Collections.Generic.Dictionary<string, object>;
			if(dict == null) return;
			if(dict.ContainsKey("InstantCommit")) InstantCommit.SetValue(Convert.ToBoolean(dict["InstantCommit"]));
			if(dict.ContainsKey("DatabaseAutoCommitInterval")) DatabaseAutoCommitInterval.SetValue(Convert.ToSingle(dict["DatabaseAutoCommitInterval"]));
			if(dict.ContainsKey("SectorSize")) SectorSize.SetValue(Convert.ToInt32(dict["SectorSize"]));
			if(dict.ContainsKey("SystemSize")) SystemSize.SetValue(Convert.ToInt32(dict["SystemSize"]));
			if(dict.ContainsKey("GalaxyRadius")) GalaxyRadius.SetValue(Convert.ToInt32(dict["GalaxyRadius"]));
		}

		public ServerConfig LoadServerConfig() {
			if(Directory.Exists(Path.Combine(Application.persistentDataPath, "Config"))) {
				Directory.CreateDirectory(Path.Combine(Application.persistentDataPath, "Config"));
			}
			_settingsFilePath = Path.Combine(Application.persistentDataPath, "Config/Server.json");
			LoadSettings();
			return this;
		}

		public void Read(BinaryReader reader) {
			InstantCommit.SetValue(reader.ReadBoolean());
			DatabaseAutoCommitInterval.SetValue(reader.ReadSingle());
			SectorSize.SetValue(reader.ReadInt32());
			SystemSize.SetValue(reader.ReadInt32());
			GalaxyRadius.SetValue(reader.ReadInt32());
		}
	}
}