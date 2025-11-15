using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using Unity.Serialization.Json;
using Unity.VisualScripting;
using UnityEngine;

namespace Settings {
	/**
	* Serializable settings for the game engine.
	*/
	[Serializable]
	public class EngineSettings {
		string _settingsFilePath;

		#region Dev Settings

		[Header("Dev Settings")]

		#endregion

		public static ServerConfig Instance { get; private set; } = new ServerConfig();

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

		string ToJson() {
			JsonObject obj = new JsonObject();
			foreach(FieldInfo setting in GetAllSettings()) {
				PropertyInfo nameProperty = setting.GetType().GetProperty("Name");
				PropertyInfo valueProperty = setting.GetType().GetProperty("Value");
				if(nameProperty != null && valueProperty != null) {
					string name = nameProperty.GetValue(setting) as string;
					object value = valueProperty.GetValue(setting);
					if(name != null) {
						obj[name] = value;
					}
				}
			}
			return obj.ToString();
		}

		void FromJson(object json) {
			var dict = json as Dictionary<string, object>;
			if(dict == null) {
				return;
			}

			foreach(FieldInfo field in GetAllSettings()) {
				Type fieldType = field.FieldType;
				// Check if the field type implements ISettingsValue<T> for any T
				if(fieldType.GetInterfaces().Any(i => i.IsGenericType && i.GetGenericTypeDefinition() == typeof(ISettingsValue<>))) {
					object setting = field.GetValue(this);
					if(setting != null) {
						PropertyInfo nameProperty = setting.GetType().GetProperty("Name");
						PropertyInfo valueProperty = setting.GetType().GetProperty("Value");
						if(nameProperty != null && valueProperty != null) {
							string name = nameProperty.GetValue(setting) as string;
							if(name != null && dict.ContainsKey(name)) {
								Type settingType = setting.GetType();
								object newValue = null;

								if(settingType == typeof(IntSettingsValue) || settingType == typeof(IntOptionsSettingsValue)) {
									newValue = Convert.ToInt32(dict[name]);
								} else if(settingType == typeof(FloatSettingsValue) || settingType == typeof(FloatOptionsSettingsValue)) {
									newValue = Convert.ToSingle(dict[name]);
								} else if(settingType == typeof(BoolSettingsValue)) {
									newValue = Convert.ToBoolean(dict[name]);
								}

								if(newValue != null) {
									// Set the value on the copy
									valueProperty.SetValue(setting, newValue);
									// Set the modified struct back to the field
									field.SetValue(this, setting);
								}
							}
						}
					}
				}
			}
		}

		/**
		* Resets all settings to their default values and saves them.
		*/
		public void SetDefaults() {
			foreach(FieldInfo field in GetAllSettings()) {
				Type fieldType = field.FieldType;
				// Check if the field type implements ISettingsValue<T> for any T
				if(fieldType.GetInterfaces().Any(i => i.IsGenericType && i.GetGenericTypeDefinition() == typeof(ISettingsValue<>))) {
					object setting = field.GetValue(this);
					if(setting != null) {
						PropertyInfo defaultValueProperty = setting.GetType().GetProperty("DefaultValue");
						PropertyInfo valueProperty = setting.GetType().GetProperty("Value");
						if(defaultValueProperty != null && valueProperty != null) {
							object defaultValue = defaultValueProperty.GetValue(setting);
							valueProperty.SetValue(setting, defaultValue);
							// Set the modified struct back to the field
							field.SetValue(this, setting);
						}
					}
				}
			}
			SaveSettings();
		}

		public void Read(BinaryReader reader) {
			string json = reader.ReadString();
			FromJson(JsonUtility.FromJson<object>(json));
			Debug.Log("Settings loaded from binary stream");
		}

		public void Write(BinaryWriter writer) {
			string json = ToJson();
			writer.Write(json);
			Debug.Log("Settings written to binary stream");
		}

		/**
		* Returns all Settings from this class using reflection.
		*/
		public FieldInfo[] GetAllSettings() {
			return GetType().GetFields(BindingFlags.Public | BindingFlags.Instance);
		}

		#region Performance Settings

		[Header("Performance Settings")]
		[InspectorLabel("Max Block Modifications Per Frame")]
		[Tooltip("Maximum number of block modifications to process per frame from the BlockModificationQueue.")]
		public IntSettingsValue MaxBlockModificationsPerFrame = new IntSettingsValue("Max Block Modifications Per Frame", "Maximum number of block modifications to process per frame from the BlockModificationQueue.", 128, 1, 1000, true);

		[InspectorLabel("GPU Readback Timeout")]
		[Tooltip("Maximum time in seconds to wait for GPU readback to complete before timing out.")]
		public FloatSettingsValue MaxGPUReadbackTimeout = new FloatSettingsValue("GPU Readback Timeout", "Maximum time in seconds to wait for GPU readback to complete before timing out.", 5.0f, 0.1f, 30.0f, true);

		[InspectorLabel("Max Chunk Operation Wait Time")]
		[Tooltip("Maximum time in seconds to wait for chunk operations to complete before timing out.")]
		public FloatSettingsValue MaxChunkOperationWaitTime = new FloatSettingsValue("Max ChunkOperation Wait Time", "Maximum time in seconds to wait for chunk operations to complete before timing out.", 5.0f, 0.1f, 30.0f, true);

		[InspectorLabel("Max Entity Rebuilds Per Frame")]
		[Tooltip("Maximum number of entity mesh rebuilds to perform per frame.")]
		public IntSettingsValue MaxEntityRebuildsPerFrame = new IntSettingsValue("Max Entity Rebuilds Per Frame", "Maximum number of entity mesh rebuilds to perform per frame.", 5, 1, 10, true);

		[InspectorLabel("GPU Compression Buffer Pool Size")]
		[Tooltip("Number of buffers to allocate for GPU compression tasks.")]
		public IntOptionsSettingsValue GPUCompressionBufferPoolSize = new IntOptionsSettingsValue("GPU Compression Buffer Pool Size", "Number of buffers to allocate for GPU compression tasks.", 4, new[] { 1, 2, 4, 8, 16 }, true);

		[InspectorLabel("GPU Compression Batch Size")]
		[Tooltip("Number of chunks to process per GPU compression/decompression batch.")]
		public IntOptionsSettingsValue GPUCompressionBatchSize = new IntOptionsSettingsValue("GPU Compression Batch Size",
			"Number of chunks to process per GPU compression/decompression batch.",
			4, // default
			new[] { 1, 2, 4, 8, 16 },
			true);

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

	}
}