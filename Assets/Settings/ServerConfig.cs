using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using Unity.Serialization.Json;
using Unity.VisualScripting;
using UnityEngine;

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
							if(nameProperty.GetValue(setting) is string name && dict.ContainsKey(name)) {
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

		public ServerConfig LoadServerConfig() {
			if(Directory.Exists(Path.Combine(Application.persistentDataPath, "Config"))) {
				Directory.CreateDirectory(Path.Combine(Application.persistentDataPath, "Config"));
			}
			_settingsFilePath = Path.Combine(Application.persistentDataPath, "Config/Server.json");
			LoadSettings();
			return this;
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

		public FieldInfo[] GetAllSettings() {
			return GetType().GetFields(BindingFlags.Public | BindingFlags.Instance);
		}
	}
}