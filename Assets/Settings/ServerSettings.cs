using System;
using System.IO;
using Unity.VisualScripting;
using UnityEngine;

namespace Settings {
	public class ServerSettings : MonoBehaviour {

		string _settingsFilePath;

		[InspectorLabel("Sector Size")]
		[Tooltip("Size of each sector in the world.")]
		public IntSettingsValue SectorSize = new IntSettingsValue("Sector Size", "Size of each sector in the world.", 50000, 1000, 100000);

		public static ServerSettings Instance { get; private set; }

		void Start() {
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
					JsonUtility.FromJsonOverwrite(json, this);
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
			string json = JsonUtility.ToJson(this, true);
			File.WriteAllText(_settingsFilePath, json);
			Debug.Log("Settings saved to " + _settingsFilePath);
		}

		/**
		* Resets all settings to their default values and saves them.
		*/
		public void SetDefaults() {

			SaveSettings();
		}
	}
}