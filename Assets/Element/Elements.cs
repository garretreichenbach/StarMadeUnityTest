using System.Collections.Generic;
using System.IO;
using System.Xml.Serialization;
using UnityEditor;
using UnityEngine;

namespace Element {
	public class ElementMap : MonoBehaviour {
		readonly string _blockTypesPath = Application.dataPath + "/Config/BlockTypes.json";
		readonly string _blockConfigPath = Application.dataPath + "/Config/BlockConfig.xml";

		public static ElementInfo[] AllElements;
		static readonly Dictionary<string, ElementInfo> ElementNameLookup = new Dictionary<string, ElementInfo>();
		bool _loadedTypes;
		bool _loadedConfig;
		ElementCategory _configRoot;

		public static ElementInfo GetInfo(short id) {
			return AllElements[id];
		}

		public static ElementInfo GetInfo(string name) {
			return ElementNameLookup[name.ToLower().Trim()];
		}

		/**
		* Loads the ElementTypes.json file from the default location, or the given byte array if one is provided.
		*/
		public void LoadElementTypes(byte[] data = null) {
			data ??= System.IO.File.ReadAllBytes(_blockTypesPath);
			string json = System.Text.Encoding.UTF8.GetString(data);
			AllElements = JsonUtility.FromJson<ElementInfo[]>(json);
			foreach(ElementInfo element in AllElements) {
				ElementNameLookup[element.Name.ToLower().Trim()] = element;
			}
			_loadedTypes = true;
		}

		/**
		* Writes the ElementTypes.json file to the default location, or the given byte array if one is provided.
		*/
		public void WriteElementTypes(byte[] data = null) {
			if(!_loadedTypes) {
				return;
			}
			string json = JsonUtility.ToJson(AllElements, true);
			if(data == null) {
				File.WriteAllText(_blockTypesPath, json);
			} else {
				byte[] bytes = System.Text.Encoding.UTF8.GetBytes(json);
				if(bytes.Length <= data.Length) {
					System.Buffer.BlockCopy(bytes, 0, data, 0, bytes.Length);
				}
			}
		}

		/**
		* Loads the ElementConfig.xml file from the default location, or the given byte array if one is provided.
		*/
		public void LoadElementConfig(byte[] data = null) {
			if(!_loadedTypes) {
				LoadElementTypes();
			}
			XmlSerializer serializer = new XmlSerializer(typeof(ElementCategory), new XmlRootAttribute("Config"));
			_configRoot = (ElementCategory) serializer.Deserialize(new MemoryStream(data ?? File.ReadAllBytes(_blockConfigPath)));
			_loadedConfig = true;
		}

		/**
		* Writes the ElementConfig.xml file to the default location, or the given byte array if one is provided.
		*/
		public void WriteElementConfig(byte[] data = null) {
			if(!_loadedConfig) {
				return;
			}
			XmlSerializer serializer = new XmlSerializer(typeof(ElementCategory), new XmlRootAttribute("Config"));
			serializer.Serialize(new MemoryStream(data ?? File.ReadAllBytes(_blockConfigPath)), _configRoot);
		}
	}

	public enum Elements {
		None = 0
	}

	[CustomEditor(typeof(ElementMap), false)]
	public class ElementsGUI : Editor {
		public override void OnInspectorGUI() {
			DrawDefaultInspector();
			GUILayout.Label("Element Types", EditorStyles.boldLabel);
			GUILayout.BeginHorizontal();
			if(GUILayout.Button("Load Element Types")) {
				(target as ElementMap)?.LoadElementConfig();
			}
			if(GUILayout.Button("Save Element Types")) {
				(target as ElementMap)?.WriteElementConfig();
			}
			GUILayout.EndHorizontal();
			GUILayout.Label("Element Config", EditorStyles.boldLabel);
			GUILayout.BeginHorizontal();
			if(GUILayout.Button("Load Element Config")) {
				(target as ElementMap)?.LoadElementTypes();
			}
			if(GUILayout.Button("Save Element Config")) {
				(target as ElementMap)?.WriteElementTypes();
			}
		}
	}
}