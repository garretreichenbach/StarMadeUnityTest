using System;
using System.IO;
using Dev;
using Unity.VisualScripting;
using UnityEngine;

namespace Settings {
	/**
	* Serializable settings for the game engine.
	*/
	[Serializable]
	public class EngineSettings : MonoBehaviour {
		string _settingsFilePath;

		#region Performance Settings

		[Header("Performance Settings")]
		[InspectorLabel("Max Block Modifications Per Frame")]
		[Tooltip("Maximum number of block modifications to process per frame from the BlockModificationQueue.")]
		public IntSettingsValue MaxBlockModificationsPerFrame = new IntSettingsValue("Max Block Modifications Per Frame",
			"Maximum number of block modifications to process per frame from the BlockModificationQueue.", 128, 1, 1000, true);

		[InspectorLabel("GPU Readback Timeout")]
		[Tooltip("Maximum time in seconds to wait for GPU readback to complete before timing out.")]
		public FloatSettingsValue MaxGPUReadbackTimeout = new FloatSettingsValue("GPU Readback Timeout",
			"Maximum time in seconds to wait for GPU readback to complete before timing out.", 5.0f, 0.1f, 30.0f, true);

		[InspectorLabel("Max Chunk Operation Wait Time")]
		[Tooltip("Maximum time in seconds to wait for chunk operations to complete before timing out.")]
		public FloatSettingsValue MaxChunkOperationWaitTime = new FloatSettingsValue("Max ChunkOperation Wait Time",
			"Maximum time in seconds to wait for chunk operations to complete before timing out.", 5.0f, 0.1f, 30.0f, true);

		[InspectorLabel("Max Entity Rebuilds Per Frame")]
		[Tooltip("Maximum number of entity mesh rebuilds to perform per frame.")]
		public IntSettingsValue MaxEntityRebuildsPerFrame = new IntSettingsValue("Max Entity Rebuilds Per Frame",
			"Maximum number of entity mesh rebuilds to perform per frame.", 5, 1, 10, true);

		[InspectorLabel("GPU Compression Buffer Pool Size")]
		[Tooltip("Number of buffers to allocate for GPU compression tasks.")]
		public IntOptionsSettingsValue GPUCompressionBufferPoolSize = new IntOptionsSettingsValue("GPU Compression Buffer Pool Size",
			"Number of buffers to allocate for GPU compression tasks.", 4, new[] { 1, 2, 4, 8, 16 }, true);

		[InspectorLabel("GPU Compression Batch Size")]
		[Tooltip("Number of chunks to process per GPU compression/decompression batch.")]
		public IntOptionsSettingsValue GPUCompressionBatchSize = new IntOptionsSettingsValue("GPU Compression Batch Size",
			"Number of chunks to process per GPU compression/decompression batch.",
			4, // default
			new[] { 1, 2, 4, 8, 16 }, true);

		#endregion

		#region Graphics Settings

		[Header("Graphics Settings")]
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

		#region Dev Settings

		[Header("Dev Settings")]
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

		public static EngineSettings Instance { get; private set; }

		void Awake() {
			//Todo: Dont call this in awake, we need a proper initialization order
			Instance = this;
			if(!Directory.Exists(Path.Combine(Application.persistentDataPath, "Config"))) {
				Directory.CreateDirectory(Path.Combine(Application.persistentDataPath, "Config"));
			}
			_settingsFilePath = Path.Combine(Application.persistentDataPath, "Config/Settings.json");
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

			// Apply settings
			FPSLimit.SetValue(FPSLimit.Value);
			VSyncMode.SetValue(VSyncMode.Value);
		}

		/**
		* Saves the current settings to the config file.
		*/
		public void SaveSettings() {
			string json = JsonUtility.ToJson(ToJson(), true);
			File.WriteAllText(_settingsFilePath, json);
			Debug.Log("Settings saved to " + _settingsFilePath);
		}

		object ToJson() {
			return new {
				MaxGPUReadbackTimeout = MaxGPUReadbackTimeout.Value,
				MaxChunkOperationWaitTime = MaxChunkOperationWaitTime.Value,
				MaxEntityRebuildsPerFrame = MaxEntityRebuildsPerFrame.Value,
				FPSLimit = FPSLimit.Value,
				VSyncMode = VSyncMode.Value,
				StatsOverlayMode = StatsOverlayMode.Value,
				GPUCompressionBufferPoolSize = GPUCompressionBufferPoolSize.Value,
				GPUCompressionBatchSize = GPUCompressionBatchSize.Value
			};
		}

		void FromJson(object json) {
			var dict = json as System.Collections.Generic.Dictionary<string, object>;
			if(dict == null) return;
			if(dict.ContainsKey("MaxGPUReadbackTimeout")) MaxGPUReadbackTimeout.SetValue(Convert.ToSingle(dict["MaxGPUReadbackTimeout"]));
			if(dict.ContainsKey("MaxChunkOperationWaitTime")) MaxChunkOperationWaitTime.SetValue(Convert.ToSingle(dict["MaxChunkOperationWaitTime"]));
			if(dict.ContainsKey("MaxEntityRebuildsPerFrame")) MaxEntityRebuildsPerFrame.SetValue(Convert.ToInt32(dict["MaxEntityRebuildsPerFrame"]));
			if(dict.ContainsKey("FPSLimit")) FPSLimit.SetValue(Convert.ToInt32(dict["FPSLimit"]));
			if(dict.ContainsKey("VSyncMode")) VSyncMode.SetValue(Convert.ToInt32(dict["VSyncMode"]));
			if(dict.ContainsKey("StatsOverlayMode")) StatsOverlayMode.SetValue((StatsDisplay.DisplayMode)Enum.Parse(typeof(StatsDisplay.DisplayMode), dict["StatsOverlayMode"].ToString()));
			if(dict.ContainsKey("GPUCompressionBufferPoolSize")) GPUCompressionBufferPoolSize.SetValue(Convert.ToInt32(dict["GPUCompressionBufferPoolSize"]));
			if(dict.ContainsKey("GPUCompressionBatchSize")) GPUCompressionBatchSize.SetValue(Convert.ToInt32(dict["GPUCompressionBatchSize"]));
		}

		/**
		* Resets all settings to their default values and saves them.
		*/
		public void SetDefaults() {
			FPSLimit.SetValue(FPSLimit.DefaultValue);
			VSyncMode.SetValue(VSyncMode.DefaultValue);
			StatsOverlayMode.SetValue(StatsOverlayMode.DefaultValue);
			GPUCompressionBufferPoolSize.SetValue(GPUCompressionBufferPoolSize.DefaultValue);
			GPUCompressionBatchSize.SetValue(GPUCompressionBatchSize.DefaultValue);
			MaxGPUReadbackTimeout.SetValue(MaxGPUReadbackTimeout.DefaultValue);
			MaxChunkOperationWaitTime.SetValue(MaxChunkOperationWaitTime.DefaultValue);
			MaxEntityRebuildsPerFrame.SetValue(MaxEntityRebuildsPerFrame.DefaultValue);
			SaveSettings();
		}
	}
}