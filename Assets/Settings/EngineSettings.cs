using Dev;
using Unity.VisualScripting;
using UnityEngine;

namespace Settings {
	/**
	 * Serializable settings for the game engine.
	 */
	public class EngineSettings : MonoBehaviour {
		const string SettingsFilePath = "Assets/Config/settings.json";

		public static EngineSettings Instance { get; private set; }

		[InspectorLabel("FPS Limit")] [Tooltip("Limit the game's frame rate to this value. Set to -1 for] unlimited.")]
		public IntOptionsSettingsValue FPSLimit = new(
			name: "FPS Limit",
			description: "Limit the game's frame rate to this value. Set to -1 for unlimited.",
			defaultValue: 60,
			allowedValues: new[] { -1, 30, 60, 120 },
			false,
			new ISettingsChangeListener[] {
				new SettingsChangeListener<int>(value => {
					Application.targetFrameRate = value;
					Debug.Log($"FPS Limit set to {value}");
				})
			});

		[InspectorLabel("VSync Mode")]
		[Tooltip("Set the VSync mode. 0 = Don't Sync, 1 = Every V Blank, 2 = Every Second V Blank.")]
		public IntOptionsSettingsValue VSyncMode = new(
			name: "VSync Mode",
			description: "Set the VSync mode. 0 = Don't Sync, 1 = Every V Blank, 2 = Every Second V Blank.",
			defaultValue: 1,
			allowedValues: new[] { 0, 1, 2 },
			false,
			new ISettingsChangeListener[] {
				new SettingsChangeListener<int>(value => {
					QualitySettings.vSyncCount = value;
					Debug.Log($"VSync Mode set to {value}");
				})
			}
		);
		[InspectorLabel("Stats Overlay Mode")]
		[Tooltip("Set the stats overlay mode.")]
		public EnumSettingsValue<StatsDisplay.DisplayMode> StatsOverlayMode = new(
			name: "Stats Overlay Mode",
			description: "Set the stats overlay mode.",
			defaultValue: StatsDisplay.DisplayMode.FPS | StatsDisplay.DisplayMode.Ping,
			debugOnly: false,
			listeners: new ISettingsChangeListener[] {
				new SettingsChangeListener<StatsDisplay.DisplayMode>(value => {
					var statsDisplay = FindObjectOfType<StatsDisplay>();
					if(statsDisplay != null) {
						statsDisplay.enabled = value != StatsDisplay.DisplayMode.None;
						Debug.Log($"Stats Overlay Mode set to {value}");
					}
				})
			}
		);

		void Start() {
			Instance = this;
			LoadSettings();
		}
		
		/**
		 * Loads the settings from the config file.
		 */
		public void LoadSettings() {
			if(System.IO.File.Exists(SettingsFilePath)) {
				try {
					string json = System.IO.File.ReadAllText(SettingsFilePath);
					JsonUtility.FromJsonOverwrite(json, this);
					Debug.Log("Settings loaded from " + SettingsFilePath);
				} catch(System.Exception e) {
					Debug.LogWarning("Failed to load settings from " + SettingsFilePath + ": " + e.Message);
					SetDefaults();
				}
			} else {
				Debug.LogWarning("Settings file not found at " + SettingsFilePath + ". Using default settings.");
				SetDefaults();
			}

			// Apply settings
			FPSLimit.SetValue(FPSLimit.Value);
			VSyncMode.SetValue(VSyncMode.Value);
		}

		/**
		 * Saves the current settings to the config file.
		 */
		public void SaveSettings() {
			string json = JsonUtility.ToJson(this, true);
			System.IO.File.WriteAllText(SettingsFilePath, json);
			Debug.Log("Settings saved to " + SettingsFilePath);
		}

		/**
		 * Resets all settings to their default values and saves them.
		 */
		public void SetDefaults() {
			FPSLimit.SetValue(FPSLimit.DefaultValue);
			VSyncMode.SetValue(VSyncMode.DefaultValue);

			SaveSettings();
		}
	}
}