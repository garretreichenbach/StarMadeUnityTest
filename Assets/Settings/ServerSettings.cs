using System;
using System.IO;
using Unity.VisualScripting;
using UnityEngine;

namespace Settings {
	[Serializable]
	public class ServerSettings : MonoBehaviour {

		string _settingsFilePath;

		#region World Settings

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

		public static ServerSettings Instance { get; private set; }

		void Awake() {
			Instance = this;
			if(!Directory.Exists(Path.Combine(Application.persistentDataPath, "Config"))) {
				Directory.CreateDirectory(Path.Combine(Application.persistentDataPath, "Config"));
			}
			_settingsFilePath = Path.Combine(Application.persistentDataPath, "Config/server.json");
			LoadSettings();
		}

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
	}
}