using System;
using System.IO;
using Unity.VisualScripting;
using UnityEngine;

namespace Settings {
	public class ServerSettings : MonoBehaviour {

		const string SettingsFilePath = "Config/server.json";

		[InspectorLabel("Sector Size")]
		[Tooltip("Size of each sector in the world.")]
		public IntSettingsValue SectorSize = new IntSettingsValue("Sector Size", "Size of each sector in the world.", 50000, 1000, 100000);

		public static ServerSettings Instance { get; private set; }

		void Start() {
			Instance = this;
			LoadSettings();
		}

		/**
		* Loads the settings from the config file.
		*/
		public void LoadSettings() {
			if(File.Exists(SettingsFilePath)) {
				try {
					string json = File.ReadAllText(SettingsFilePath);
					JsonUtility.FromJsonOverwrite(json, this);
					Debug.Log("Settings loaded from " + SettingsFilePath);
				} catch(Exception e) {
					Debug.LogWarning("Failed to load settings from " + SettingsFilePath + ": " + e.Message);
					SetDefaults();
				}
			} else {
				Debug.LogWarning("Settings file not found at " + SettingsFilePath + ". Using default settings.");
				SetDefaults();
			}
		}

		/**
		* Saves the current settings to the config file.
		*/
		public void SaveSettings() {
			string json = JsonUtility.ToJson(this, true);
			File.WriteAllText(SettingsFilePath, json);
			Debug.Log("Settings saved to " + SettingsFilePath);
		}

		/**
		* Resets all settings to their default values and saves them.
		*/
		public void SetDefaults() {

			SaveSettings();
		}
	}
}