using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.Serialization;
using System.Xml;
using System.Linq;
using System.Text;
using UnityEditor;
using UnityEngine;

namespace Element {
	public class ElementMap : MonoBehaviour {
		static readonly string BlockTypesPath = Path.Combine(Application.persistentDataPath, "Config", "BlockTypes.properties");
		static readonly string BlockConfigPath = Path.Combine(Application.persistentDataPath, "Config", "BlockConfig.xml");

		public static ElementInfo[] AllElements { get; private set; }

		static readonly Dictionary<string, ElementInfo> ElementNameLookup = new Dictionary<string, ElementInfo>();
		static bool _loadedTypes;
		static bool _loadedConfig;
		static ElementCategory _configRoot;

		[MenuItem("Tools/Regenerate Elements Enum")]
		public static void RegenerateEnum() {
			LoadElementTypes();
			LoadElementConfig();
			if(AllElements == null || AllElements.Length == 0) {
				Debug.LogWarning("ElementMap.RegenerateEnum: AllElements is null or empty. Make sure BlockTypes.properties exists and is valid.");
				return;
			}
			var sb = new StringBuilder();
			sb.AppendLine("namespace Element {");
			sb.AppendLine("    public enum Elements {");
			sb.AppendLine("        NONE = 0,");
			for(int i = 0; i < AllElements.Length; i++) {
				var element = AllElements[i];
				if(element != null) {
					sb.AppendLine($"        {element.IdName} = {element.TypeId},");
				}
			}
			sb.AppendLine("    }");
			sb.AppendLine("}");
			File.WriteAllText("Assets/Element/Elements.cs", sb.ToString());
			AssetDatabase.Refresh();
		}

		public static ElementInfo GetInfo(short id) {
			return AllElements[id];
		}

		public static ElementInfo GetInfo(string name) {
			return ElementNameLookup[name.ToLower().Trim()];
		}

		/**
		* Writes the BlockTypes.properties file in Java .properties format (IDNAME=short_id).
		*/
		public static void WriteElementTypes() {
			if(AllElements == null || AllElements.Length == 0) return;
			var sb = new StringBuilder();
			var seen = new HashSet<string>();
			for(int i = 0; i < AllElements.Length; i++) {
				var element = AllElements[i];
				if(element == null || string.IsNullOrWhiteSpace(element.IdName)) continue;
				if(seen.Contains(element.IdName)) continue;
				seen.Add(element.IdName);
				sb.AppendLine($"{element.IdName}={element.TypeId}");
			}
			File.WriteAllText(BlockTypesPath, sb.ToString());
		}

		/**
		* Loads the ElementConfig.xml file from the default location, or the given byte array if one is provided.
		*/
		public static void LoadElementConfig(byte[] data = null) {
			if(!_loadedTypes) {
				LoadElementTypes();
			}
			var root = new XmlDocument();
			root.LoadXml(data == null ? File.ReadAllText(BlockConfigPath) : Encoding.UTF8.GetString(data));
			var configNode = root.SelectSingleNode("Config");
			var elements = new List<ElementInfo>();
			if(configNode != null && configNode.HasChildNodes) {
				foreach(XmlNode child in configNode.ChildNodes) {
					if(child is XmlElement element) {
						if(element.Name == "Block") {
							// Deserialize as ElementInfo
							var serializer = new XmlSerializer(typeof(ElementInfo), new XmlRootAttribute("Block"));
							var info = (ElementInfo)serializer.Deserialize(new XmlNodeReader(element));
							elements.Add(info);
						} else {
							// Deserialize as ElementCategory and process
							var serializer = new XmlSerializer(typeof(ElementCategory), new XmlRootAttribute(element.Name));
							ElementCategory category;
							try {
								category = (ElementCategory)serializer.Deserialize(new XmlNodeReader(element));
								category.Name = element.Name;
								ProcessCategoryTree(category);
								// Collect all ElementInfo from this category and its children
								CollectElementsFromCategory(category, elements);
							} catch(Exception ex) {
								Debug.LogWarning($"ElementMap.LoadElementConfig: Failed to deserialize category {element.Name}: {ex}");
							}
						}
					}
				}
			}
			AllElements = elements.ToArray();
			_loadedConfig = true;
		}

		static void CollectElementsFromCategory(ElementCategory category, List<ElementInfo> elements) {
			if(category.Blocks != null) {
				elements.AddRange(category.Blocks);
			}
			if(category.ChildCategories != null) {
				foreach(var child in category.ChildCategories) {
					CollectElementsFromCategory(child, elements);
				}
			}
		}

		static void ProcessCategoryTree(ElementCategory category) {
			if(category.ChildCategoriesRaw != null) {
				foreach(XmlElement elem in category.ChildCategoriesRaw) {
					if(elem.Name == "Block") continue; // Already handled
					if(string.Equals(elem.Name, "recipes", StringComparison.OrdinalIgnoreCase) || string.Equals(elem.Name, "recipe", StringComparison.OrdinalIgnoreCase)) continue; // Ignore recipes category and its children
					XmlSerializer serializer = new XmlSerializer(typeof(ElementCategory), new XmlRootAttribute(elem.Name));
					using XmlNodeReader reader = new XmlNodeReader(elem);
					try {
						ElementCategory child = (ElementCategory)serializer.Deserialize(reader);
						child.Name = elem.Name;
						ProcessCategoryTree(child);
						category.ChildCategories.Add(child);
					} catch(Exception exception) {
						Debug.LogWarning($"ElementMap.ProcessCategoryTree: Failed to deserialize category {elem.Name}: {exception}");
						//BlockConfig.xml has a bunch of old recipe stuff in it at the end,we can just ignore it
					}
				}
			}
		}

		/**
		* Writes the ElementConfig.xml file to the default location, or the given byte array if one is provided.
		*/
		public static void WriteElementConfig(byte[] data = null) {
			if(!_loadedConfig) {
				return;
			}
			XmlSerializer serializer = new XmlSerializer(typeof(ElementCategory), new XmlRootAttribute("Config"));
			serializer.Serialize(new MemoryStream(data ?? File.ReadAllBytes(BlockConfigPath)), _configRoot);
		}

		/**
		* Loads the BlockTypes.properties file and populates AllElements and ElementNameLookup.
		*/
		public static void LoadElementTypes() {
			if(!File.Exists(BlockTypesPath)) {
				Debug.LogWarning($"ElementMap.LoadElementTypes: {BlockTypesPath} does not exist. AllElements will be empty.");
				AllElements = Array.Empty<ElementInfo>();
				ElementNameLookup.Clear();
				_loadedTypes = false;
				return;
			}
			var lines = File.ReadAllLines(BlockTypesPath);
			if(lines.Length == 0) {
				Debug.LogWarning($"ElementMap.LoadElementTypes: {BlockTypesPath} is empty. AllElements will be empty.");
				AllElements = Array.Empty<ElementInfo>();
				ElementNameLookup.Clear();
				_loadedTypes = false;
				return;
			}
			var tempList = new List<(string idName, short typeId)>();
			short maxId = 0;
			foreach(var line in lines) {
				var trimmed = line.Trim();
				if(string.IsNullOrEmpty(trimmed) || trimmed.StartsWith("#") || trimmed.StartsWith(";")) continue;
				var idx = trimmed.IndexOf('=');
				if(idx <= 0 || idx == trimmed.Length - 1) continue;
				var idName = trimmed.Substring(0, idx).Trim();
				if(!short.TryParse(trimmed.Substring(idx + 1).Trim(), out var typeId)) continue;
				if(typeId > maxId) maxId = typeId;
				tempList.Add((idName, typeId));
			}
			if(tempList.Count == 0) {
				Debug.LogWarning($"ElementMap.LoadElementTypes: No valid entries found in {BlockTypesPath}. AllElements will be empty.");
				AllElements = Array.Empty<ElementInfo>();
				ElementNameLookup.Clear();
				_loadedTypes = false;
				return;
			}
			AllElements = new ElementInfo[maxId + 1];
			ElementNameLookup.Clear();
			foreach(var (idName, typeId) in tempList) {
				var info = new ElementInfo();
				var typeIdField = typeof(ElementInfo).GetField("TypeId", System.Reflection.BindingFlags.Instance | System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Public);
				var idNameField = typeof(ElementInfo).GetField("IdName", System.Reflection.BindingFlags.Instance | System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Public);
				if(typeIdField != null) typeIdField.SetValue(info, typeId);
				if(idNameField != null) idNameField.SetValue(info, idName);
				AllElements[typeId] = info;
				ElementNameLookup[idName.ToLower().Trim()] = info;
			}
			_loadedTypes = true;
		}
	}

	[CustomEditor(typeof(ElementMap), false)]
	public class ElementsGUI : Editor {
		public override void OnInspectorGUI() {
			DrawDefaultInspector();
			GUILayout.Label("Element Types", EditorStyles.boldLabel);
			GUILayout.BeginHorizontal();
			if(GUILayout.Button("Load Element Types")) {
				ElementMap.LoadElementTypes();
			}
			if(GUILayout.Button("Save Element Types")) {
				ElementMap.WriteElementConfig();
			}
			GUILayout.EndHorizontal();
			GUILayout.Label("Element Config", EditorStyles.boldLabel);
			GUILayout.BeginHorizontal();
			if(GUILayout.Button("Load Element Config")) {
				ElementMap.LoadElementConfig();
			}
			if(GUILayout.Button("Save Element Config")) {
				ElementMap.WriteElementConfig();
			}
			GUILayout.EndHorizontal();
		}
	}

	[XmlRoot("Block")]
	public class ElementInfo {
		[XmlIgnore]
		public short TypeId { get; set; }
		[XmlAttribute("icon")]
		public int IconId { get; set; }
		[XmlAttribute("name")]
		public string Name { get; set; }

		[XmlAttribute("textureId")]
		string TextureIdString {
			get => string.Join(", ", TextureIds ?? Array.Empty<short>());
			set => TextureIds = value?.Split(',').Select(s => short.Parse(s.Trim())).ToArray();
		}

		[XmlIgnore]
		public short[] TextureIds {
			get => TextureIdString?.Split(',').Select(s => short.Parse(s.Trim())).ToArray() ?? Array.Empty<short>();
			set => TextureIdString = string.Join(", ", value);
		}

		[XmlAttribute("type")]
		public string IdName { get; set; }
		[XmlElement("Consistence")]
		public Recipe Consistence { get; set; }
		[XmlElement("ChamberPrerequisites")]
		public short[] ChamberPrerequisites { get; set; }
		[XmlElement("ChamberMutuallyExclusive")]
		public short[] ChamberMutuallyExclusive { get; set; }
		[XmlElement("ChamberChildren")]
		public short[] ChamberChildren { get; set; }
		[XmlElement("LightSourceColor")]
		public float[] LightSourceColor { get; set; }
		[XmlElement("ArmorValue")]
		public float ArmorValue { get; set; }
		[XmlElement("EffectArmor")]
		public EffectArmor EffectArmor { get; set; }
		[XmlElement("Price")]
		public long Price { get; set; }
		[XmlElement("Description")]
		public string Description { get; set; }
		[XmlElement("BlockResourceType")]
		public int BlockResourceType { get; set; }
		[XmlElement("ProducedInFactory")]
		public short ProducedInFactory { get; set; }
		[XmlElement("BasicResourceFactory")]
		public short BasicResourceFactory { get; set; }
		[XmlElement("FactoryBakeTime")]
		public float FactoryBakeTime { get; set; }
		[XmlElement("InventoryGroup")]
		public string InventoryGroup { get; set; }
		[XmlElement("Animated")]
		public bool Animated { get; set; }
		[XmlElement("Transparency")]
		public bool HasTransparency { get; set; }
		[XmlElement("InShop")]
		public bool InShop { get; set; }
		[XmlElement("Orientation")]
		public bool HasOrientation { get; set; }
		[XmlElement("BlockComputerReference")]
		public short BlockComputerReference { get; set; }
		[XmlElement("Slab")]
		public byte SlabType { get; set; }
		[XmlElement("SlabIds")]
		public short[] SlabIds { get; set; }
		[XmlElement("StyleIds")]
		public short[] StyleIds { get; set; }
		[XmlElement("SourceReference")]
		public short SourceReference { get; set; }
		[XmlElement("GeneralChamber")]
		public bool IsGeneralChamber { get; set; }
		[XmlElement("ChamberCapacity")]
		public float ChamberCapacity { get; set; }
		[XmlElement("ChamberRoot")]
		public short ChamberRoot { get; set; }
		[XmlElement("ChamberParent")]
		public short ChamberParent { get; set; }
		[XmlElement("ChamberUpgradesTo")]
		public short ChamberUpgradesTo { get; set; }
		[XmlElement("ChamberPermission")]
		public short ChamberPermission { get; set; }
		[XmlElement("ChamberAppliesTo")]
		public byte ChamberAppliesTo { get; set; }
		[XmlElement("ReactorHp")]
		public float ReactorHp { get; set; }
		[XmlElement("ReactorGeneralIconIndex")]
		public int ReactorIconIndex { get; set; }
		[XmlElement("Enterable")]
		public bool IsEnterable { get; set; }
		[XmlElement("Mass")]
		public float Mass { get; set; }
		[XmlElement("Volume")]
		public float Volume { get; set; }
		[XmlElement("Hitpoints")]
		public int HP { get; set; }
		[XmlElement("Placable")]
		public bool IsPlacable { get; set; }
		[XmlElement("InRecipe")]
		public bool IsInRecipe { get; set; }
		[XmlElement("CanActivate")]
		public bool CanActivate { get; set; }
		[XmlElement("IndividualSides")]
		public byte IndividualSides { get; set; }
		[XmlElement("SideTexturesPointToOrientation")]
		public bool SideTexturesPointToOrientation { get; set; }
		[XmlElement("HasActivationTexture")]
		public bool HasActivationTexture { get; set; }
		[XmlElement("MainCombinationController")]
		public bool IsMainCombinationController { get; set; }
		[XmlElement("SupportCombinationController")]
		public bool IsSupportCombinationController { get; set; }
		[XmlElement("EffectCombinationController")]
		public bool IsEffectCombinationController { get; set; }
		[XmlElement("Beacon")]
		public bool IsBeacon { get; set; }
		[XmlElement("Physical")]
		public bool IsPhysical { get; set; }
		[XmlElement("BlockStyle")]
		public byte BlockStyle { get; set; }
		[XmlElement("LightSource")]
		public bool IsLightSource { get; set; }
		[XmlElement("Door")]
		public bool IsDoor { get; set; }
		[XmlElement("SensorInput")]
		public bool IsSensorInput { get; set; }
		[XmlElement("DrawLogicConnection")]
		public bool DrawLogicConnection { get; set; }
		[XmlElement("Deprecated")]
		public bool IsDeprecated { get; set; }
		[XmlElement("ExplosionAbsorbtion")]
		public float ExplosionAbsorbtion { get; set; }
		[XmlElement("OnlyDrawnInBuildMode")]
		public bool OnlyDrawnInBuildMode { get; set; }
		[XmlElement("LodActivationAnimationStyle")]
		public byte LodActivationAnimationStyle { get; set; }
		[XmlElement("SystemBlock")]
		public bool IsSystemBlock { get; set; }
		[XmlElement("LogicBlock")]
		public bool IsLogicBlock { get; set; }
		[XmlElement("LogicSignaledByRail")]
		public bool IsLogicSignaledByRail { get; set; }
		[XmlElement("LogicBlockButton")]
		public bool IsLogicBlockButton { get; set; }
	}

	public class ElementCategory {
		[XmlIgnore]
		public string Name { get; set; }

		[XmlElement("Block")]
		public List<ElementInfo> Blocks { get; set; } = new List<ElementInfo>();

		[XmlAnyElement]
		public List<XmlElement> ChildCategoriesRaw { get; set; }

		[XmlIgnore]
		public List<ElementCategory> ChildCategories { get; set; } = new List<ElementCategory>();
	}

	[XmlRoot("Consistence")]
	public class Recipe {
		[XmlElement("Item")]
		public List<ItemStack> Items { get; set; }
	}

	[XmlRoot("Item")]
	public class ItemStack {
		[XmlAttribute("count")]
		public int Count { get; set; }
		[XmlText]
		public string Name { get; set; }

		public ElementInfo GetInfo() {
			return ElementMap.GetInfo(Name);
		}
	}

	public class EffectArmor {
		[XmlElement("Heat")]
		public float Heat { get; set; }
		[XmlElement("Kinetic")]
		public float Kinetic { get; set; }
		[XmlElement("EM")]
		public float EM { get; set; }
	}
}