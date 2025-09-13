using System;
using System.IO;
using Dev;
using Unity.VisualScripting;
using UnityEngine;

namespace Settings {
	/**
	* Serializable settings for the game engine.
	*/
	public class EngineSettings : MonoBehaviour {
		const string SettingsFilePath = "Config/settings.json";

		#region Performance Settings

		[InspectorLabel("GPU Readback Timeout")]
		[Tooltip("Maximum time in seconds to wait for GPU readback to complete before timing out.")]
		public FloatSettingsValue MaxGPUReadbackTimeout = new FloatSettingsValue("GPU Readback Timeout", "Maximum time in seconds to wait for GPU readback to complete before timing out.", 10.0f, 5.0f, 30.0f);

		[InspectorLabel("Max Chunk Operation Wait Time")]
		[Tooltip("Maximum time in seconds to wait for chunk operations to complete before timing out.")]
		public FloatSettingsValue MaxChunkOperationWaitTime = new FloatSettingsValue("Max Chunk Operation Wait Time", "Maximum time in seconds to wait for chunk operations to complete before timing out.", 30.0f, 5.0f, 30.0f);

		#endregion

		#region Dev Settings

		[InspectorLabel("Stats Overlay Mode")]
		[Tooltip("Set the stats overlay mode.")]
		public EnumSettingsValue<StatsDisplay.DisplayMode> StatsOverlayMode = new EnumSettingsValue<StatsDisplay.DisplayMode>("Stats Overlay Mode",
			"Set the stats overlay mode.",
			StatsDisplay.DisplayMode.FPS | StatsDisplay.DisplayMode.Ping,
			false,
			new ISettingsChangeListener[] {
				new SettingsChangeListener<StatsDisplay.DisplayMode>(value => {
					StatsDisplay statsDisplay = FindFirstObjectByType<StatsDisplay>();
					if(statsDisplay != null) {
						statsDisplay.enabled = value != StatsDisplay.DisplayMode.None;
						Debug.Log($"Stats Overlay Mode set to {value}");
					}
				}),
			});

		#endregion

		#region Graphics Settings

		[InspectorLabel("FPS Limit")] [Tooltip("Limit the game's frame rate to this value. Set to -1 for] unlimited.")]
		public IntOptionsSettingsValue FPSLimit = new IntOptionsSettingsValue("FPS Limit",
			"Limit the game's frame rate to this value. Set to -1 for unlimited.",
			-1,
			new[] { -1, 30, 60, 120, 240 },
			false,
			new ISettingsChangeListener[] {
				new SettingsChangeListener<int>(value => {
					Application.targetFrameRate = value;
					Debug.Log($"FPS Limit set to {value}");
				}),
			});

		[InspectorLabel("VSync Mode")]
		[Tooltip("Set the VSync mode. 0 = Don't Sync, 1 = Every V Blank, 2 = Every Second V Blank.")]
		public IntOptionsSettingsValue VSyncMode = new IntOptionsSettingsValue("VSync Mode",
			"Set the VSync mode. 0 = Don't Sync, 1 = Every V Blank, 2 = Every Second V Blank.",
			1,
			new[] { 0, 1, 2 },
			false,
			new ISettingsChangeListener[] {
				new SettingsChangeListener<int>(value => {
					QualitySettings.vSyncCount = value;
					Debug.Log($"VSync Mode set to {value}");
				}),
			});

		#endregion

		public static EngineSettings Instance { get; private set; }

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

			// Apply settings
			FPSLimit.SetValue(FPSLimit.Value);
			VSyncMode.SetValue(VSyncMode.Value);
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
			FPSLimit.SetValue(FPSLimit.DefaultValue);
			VSyncMode.SetValue(VSyncMode.DefaultValue);

			SaveSettings();
		}
	}
}