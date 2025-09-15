using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.Serialization;
using System.Xml;
using System.Linq;
using System.Reflection;
using System.Text;
using UnityEditor;
using UnityEngine;

namespace Element {
	public class ElementMap : MonoBehaviour {
		static string BlockTypesPath;
		static string BlockConfigPath;

		public static List<ElementInfo> AllElements { get; private set; }

		static readonly Dictionary<short, string> TypeIdByName = new Dictionary<short, string>();
		public static readonly Dictionary<string, ElementInfo> ElementNameLookup = new Dictionary<string, ElementInfo>();
		static bool _loadedTypes;
		static bool _loadedConfig;
		public static ElementCategory _configRoot;

		void Awake() {
			BlockTypesPath = Path.Combine(Application.persistentDataPath, "Config", "BlockTypes.properties");
			BlockConfigPath = Path.Combine(Application.persistentDataPath, "Config", "BlockConfig.xml");
		}

		[MenuItem("Tools/Regenerate Elements Enum")]
		public static void RegenerateEnum() {
			LoadElementTypes();
			if(TypeIdByName == null || TypeIdByName.Count == 0) {
				Debug.LogWarning("ElementMap.RegenerateEnum: LoadedElements is null or empty. Make sure BlockTypes.properties and BlockConfig.xml exist and are valid.");
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.AppendLine("namespace Element {");
			sb.AppendLine("    public enum Elements {");
			sb.AppendLine("        NONE = 0,");
			foreach(var element in TypeIdByName) {
				sb.AppendLine($"        {element.Value.ToUpperInvariant()} = {element.Key},");
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
			return ElementNameLookup[name.Trim()];
		}

		/**
		* Writes the BlockTypes.properties file in Java .properties format (IDNAME=short_id).
		*/
		public static void WriteElementTypes() {
			if(AllElements == null || !AllElements.Any()) return;
			var sb = new StringBuilder();
			var seen = new HashSet<string>();
			foreach(ElementInfo element in AllElements) {
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
			var elementNode = configNode?.SelectSingleNode("Element");
			var elements = new List<ElementInfo>();
			if(configNode != null && elementNode.HasChildNodes) {
				foreach(XmlNode child in elementNode.ChildNodes) {
					if(child is XmlElement element) {
						if(element.Name == "Block") {
							// Deserialize as ElementInfo
							var serializer = new XmlSerializer(typeof(ElementInfo), new XmlRootAttribute("Block"));
							var info = (ElementInfo)serializer.Deserialize(new XmlNodeReader(element));
							// Assign TypeId from TypeIdByName
							if(!string.IsNullOrWhiteSpace(info.IdName) && ElementNameLookup.TryGetValue(info.IdName.Trim(), out var tid)) {
								var typeIdField = typeof(ElementInfo).GetField("TypeId", BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public);
								if(typeIdField != null) typeIdField.SetValue(info, tid);
							}
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
			_loadedConfig = true;
			AllElements = elements;
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
			// Defensive parse of BlockElements into Blocks
			if(category.BlockElements != null) {
				var serializer = new XmlSerializer(typeof(ElementInfo), new XmlRootAttribute("Block"));
				foreach(XmlElement blockElem in category.BlockElements) {
					try {
						ElementInfo info = (ElementInfo)serializer.Deserialize(new XmlNodeReader(blockElem));
						category.Blocks.Add(info);
						AllElements.Add(info);
						if(info.IdName != null) {
							ElementNameLookup[info.IdName.Trim()] = info;
						}
					} catch(Exception ex) {
						Debug.LogWarning($"ElementMap.ProcessCategoryTree: Failed to parse Block: {blockElem.OuterXml}\n{ex}");
					}
				}
			}
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
				throw new FileNotFoundException($"ElementMap.LoadElementTypes: {BlockTypesPath} does not exist.");
			}
			var lines = File.ReadAllLines(BlockTypesPath);
			if(lines.Length == 0) {
				throw new Exception($"ElementMap.LoadElementTypes: {BlockTypesPath} is empty. AllElements will be empty.");
			}
			short maxId = 0;
			foreach(string line in lines) {
				string trimmed = line.Trim();
				if(string.IsNullOrEmpty(trimmed) || trimmed.StartsWith("#") || trimmed.StartsWith(";")) continue;
				int idx = trimmed.IndexOf('=');
				if(idx <= 0 || idx == trimmed.Length - 1) continue;
				string idName = trimmed[..idx].Trim();
				short typeId = (short)int.Parse(trimmed[(idx + 1)..].Trim());
				if(typeId > maxId) maxId = typeId;
				if(!TypeIdByName.ContainsKey(typeId)) {
					TypeIdByName[typeId] = idName;
				}
			}
			_loadedTypes = true;
		}
	}

	[CustomEditor(typeof(ElementMap), false)]
	public class ElementsGUI : Editor {
		Dictionary<ElementCategory, bool> foldoutStates = new Dictionary<ElementCategory, bool>();
		ElementInfo selectedElement = null;
		string searchString = "";

		public override void OnInspectorGUI() {
			DrawDefaultInspector();
			GUILayout.Label("Element Types", EditorStyles.boldLabel);
			GUILayout.BeginHorizontal();
			if(GUILayout.Button("Load Element Types")) {
				ElementMap.LoadElementTypes();
			}
			if(GUILayout.Button("Save Element Types")) {
				ElementMap.WriteElementTypes();
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

			GUILayout.Space(10);
			GUILayout.Label("Browse Elements (by Category)", EditorStyles.boldLabel);
			searchString = EditorGUILayout.TextField("Search", searchString);
			if (ElementMap._configRoot != null) {
				DrawCategory(ElementMap._configRoot, 0, searchString);
			} else {
				GUILayout.Label("No root category loaded.");
			}

			if (selectedElement != null) {
				GUILayout.Space(10);
				GUILayout.Label($"TypeId: {selectedElement.TypeId}");
				GUILayout.Label($"IdName: {selectedElement.IdName}");
				GUILayout.Label($"Name: {selectedElement.Name}");
				GUILayout.Label($"IconId: {selectedElement.IconId}");
				GUILayout.Label($"TextureIds: {string.Join(", ", selectedElement.TextureIds ?? Array.Empty<short>())}");
				GUILayout.Label($"Description: {selectedElement.Description}");
				// Add more fields as needed
			}
		}

		private bool DrawCategory(ElementCategory category, int indent, string search) {
			if (category == null) return false;
			bool anyBlockVisible = false;
			if (!foldoutStates.ContainsKey(category)) foldoutStates[category] = false;
			// Check if any block or subcategory matches the search
			bool hasMatch = CategoryHasMatch(category, search);
			if (!hasMatch && !string.IsNullOrEmpty(search)) return false;
			EditorGUI.indentLevel = indent;
			foldoutStates[category] = EditorGUILayout.Foldout(foldoutStates[category], category.Name ?? "<Unnamed Category>", true);
			if (foldoutStates[category]) {
				EditorGUI.indentLevel = indent + 1;
				// Draw blocks
				if (category.Blocks != null) {
					foreach (var block in category.Blocks) {
						if (block == null) continue;
						if (!BlockMatchesSearch(block, search)) continue;
						anyBlockVisible = true;
						if (GUILayout.Button(block.IdName ?? "<null>", EditorStyles.miniButton)) {
							selectedElement = block;
						}
					}
				}
				// Draw subcategories
				if (category.ChildCategories != null) {
					foreach (var subcat in category.ChildCategories) {
						if (DrawCategory(subcat, indent + 1, search)) {
							anyBlockVisible = true;
						}
					}
				}
			}
			EditorGUI.indentLevel = indent;
			return anyBlockVisible || string.IsNullOrEmpty(search);
		}

		private bool BlockMatchesSearch(ElementInfo block, string search) {
			if (string.IsNullOrEmpty(search)) return true;
			search = search.ToLowerInvariant();
			return (block.IdName != null && block.IdName.ToLowerInvariant().Contains(search))
				|| (block.Name != null && block.Name.ToLowerInvariant().Contains(search))
				|| (block.Description != null && block.Description.ToLowerInvariant().Contains(search));
		}

		private bool CategoryHasMatch(ElementCategory category, string search) {
			if (string.IsNullOrEmpty(search)) return true;
			if (category.Blocks != null && category.Blocks.Any(b => BlockMatchesSearch(b, search))) return true;
			if (category.ChildCategories != null && category.ChildCategories.Any(c => CategoryHasMatch(c, search))) return true;
			return false;
		}
	}

	[XmlRoot("Block")]
	public class ElementInfo {
		[XmlIgnore]
		public short? TypeId { get; set; }
		[XmlAttribute("icon")]
		string IconIdRaw { get; set; }
		[XmlIgnore]
		public int? IconId => int.TryParse(IconIdRaw, out var v) ? v : null;
		[XmlAttribute("name")]
		public string Name { get; set; }

		[XmlAttribute("textureId")]
		public string TextureIdString { get; set; }
		[XmlIgnore]
		public short[] TextureIds =>
			string.IsNullOrWhiteSpace(TextureIdString) ? Array.Empty<short>() : TextureIdString.Split(',').Select(s => short.TryParse(s.Trim(), out var v) ? v : (short)0).ToArray();

		[XmlAttribute("type")]
		public string IdName { get; set; }
		[XmlElement("Consistence")]
		public Recipe Consistence { get; set; }

		[XmlElement("ChamberPrerequisites")]
		string ChamberPrerequisitesRaw { get; set; }
		[XmlIgnore]
		public short[] ChamberPrerequisites => ParseShortArray(ChamberPrerequisitesRaw);

		[XmlElement("ChamberMutuallyExclusive")]
		string ChamberMutuallyExclusiveRaw { get; set; }
		[XmlIgnore]
		public short[] ChamberMutuallyExclusive => ParseShortArray(ChamberMutuallyExclusiveRaw);

		[XmlElement("ChamberChildren")]
		string ChamberChildrenRaw { get; set; }
		[XmlIgnore]
		public short[] ChamberChildren => ParseShortArray(ChamberChildrenRaw);

		[XmlElement("LightSourceColor")]
		string LightSourceColorRaw { get; set; }
		[XmlIgnore]
		public float[] LightSourceColor => ParseFloatArray(LightSourceColorRaw);

		[XmlElement("ArmorValue")]
		string ArmorValueRaw { get; set; }
		[XmlIgnore]
		public float? ArmorValue => float.TryParse(ArmorValueRaw, out var v) ? v : null;
		[XmlElement("EffectArmor")]
		public EffectArmor EffectArmor { get; set; }
		[XmlElement("Price")]
		public long? Price { get; set; }
		[XmlElement("Description")]
		public string Description { get; set; }
		[XmlElement("BlockResourceType")]
		public int? BlockResourceType { get; set; }
		[XmlElement("ProducedInFactory")]
		public short? ProducedInFactory { get; set; }
		[XmlElement("BasicResourceFactory")]
		public short? BasicResourceFactory { get; set; }
		[XmlElement("FactoryBakeTime")]
		public float? FactoryBakeTime { get; set; }
		[XmlElement("InventoryGroup")]
		public string InventoryGroup { get; set; }
		[XmlElement("Animated")]
		public bool? Animated { get; set; }
		[XmlElement("Transparency")]
		public bool? HasTransparency { get; set; }
		[XmlElement("InShop")]
		public bool? InShop { get; set; }
		[XmlElement("Orientation")]
		public bool? HasOrientation { get; set; }
		[XmlElement("BlockComputerReference")]
		public short? BlockComputerReference { get; set; }
		[XmlElement("Slab")]
		public byte? SlabType { get; set; }
		[XmlElement("SlabIds")]
		string SlabIdsRaw { get; set; }
		[XmlIgnore]
		public short[] SlabIds => ParseShortArray(SlabIdsRaw);
		[XmlElement("StyleIds")]
		string StyleIdsRaw { get; set; }
		[XmlIgnore]
		public short[] StyleIds => ParseShortArray(StyleIdsRaw);
		[XmlElement("SourceReference")]
		public short? SourceReference { get; set; }
		[XmlElement("GeneralChamber")]
		public bool? IsGeneralChamber { get; set; }
		[XmlElement("ChamberCapacity")]
		public float? ChamberCapacity { get; set; }
		[XmlElement("ChamberRoot")]
		public short? ChamberRoot { get; set; }
		[XmlElement("ChamberParent")]
		public short? ChamberParent { get; set; }
		[XmlElement("ChamberUpgradesTo")]
		public short? ChamberUpgradesTo { get; set; }
		[XmlElement("ChamberPermission")]
		public short? ChamberPermission { get; set; }
		[XmlElement("ChamberAppliesTo")]
		public byte? ChamberAppliesTo { get; set; }
		[XmlElement("ReactorHp")]
		public float? ReactorHp { get; set; }
		[XmlElement("ReactorGeneralIconIndex")]
		public int? ReactorIconIndex { get; set; }
		[XmlElement("Enterable")]
		public bool? IsEnterable { get; set; }
		[XmlElement("Mass")]
		public float? Mass { get; set; }
		[XmlElement("Volume")]
		public float? Volume { get; set; }
		[XmlElement("Hitpoints")]
		public int? HP { get; set; }
		[XmlElement("Placable")]
		public bool? IsPlacable { get; set; }
		[XmlElement("InRecipe")]
		public bool? IsInRecipe { get; set; }
		[XmlElement("CanActivate")]
		public bool? CanActivate { get; set; }
		[XmlElement("IndividualSides")]
		public byte? IndividualSides { get; set; }
		[XmlElement("SideTexturesPointToOrientation")]
		public bool? SideTexturesPointToOrientation { get; set; }
		[XmlElement("HasActivationTexture")]
		public bool? HasActivationTexture { get; set; }
		[XmlElement("MainCombinationController")]
		public bool? IsMainCombinationController { get; set; }
		[XmlElement("SupportCombinationController")]
		public bool? IsSupportCombinationController { get; set; }
		[XmlElement("EffectCombinationController")]
		public bool? IsEffectCombinationController { get; set; }
		[XmlElement("Beacon")]
		public bool? IsBeacon { get; set; }
		[XmlElement("Physical")]
		public bool? IsPhysical { get; set; }
		[XmlElement("BlockStyle")]
		string BlockStyleRaw { get; set; }
		[XmlIgnore]
		public byte? BlockStyle => ParseByte(BlockStyleRaw);
		[XmlElement("LightSource")]
		public bool? IsLightSource { get; set; }
		[XmlElement("Door")]
		public bool? IsDoor { get; set; }
		[XmlElement("SensorInput")]
		public bool? IsSensorInput { get; set; }
		[XmlElement("DrawLogicConnection")]
		public bool? DrawLogicConnection { get; set; }
		[XmlElement("Deprecated")]
		public bool? IsDeprecated { get; set; }
		[XmlElement("ExplosionAbsorbtion")]
		public float? ExplosionAbsorbtion { get; set; }
		[XmlElement("OnlyDrawnInBuildMode")]
		public bool? OnlyDrawnInBuildMode { get; set; }
		[XmlElement("LodActivationAnimationStyle")]
		public byte? LodActivationAnimationStyle { get; set; }
		[XmlElement("SystemBlock")]
		public bool? IsSystemBlock { get; set; }
		[XmlElement("LogicBlock")]
		public bool? IsLogicBlock { get; set; }
		[XmlElement("LogicSignaledByRail")]
		public bool? IsLogicSignaledByRail { get; set; }
		[XmlElement("LogicBlockButton")]
		public bool? IsLogicBlockButton { get; set; }

		static short[] ParseShortArray(string raw) {
			if(string.IsNullOrWhiteSpace(raw) || raw == "{}") return Array.Empty<short>();
			return raw.Split(',').Select(s => short.TryParse(s.Trim(), out var v) ? v : (short)0).ToArray();
		}
		static float[] ParseFloatArray(string raw) {
			if(string.IsNullOrWhiteSpace(raw) || raw == "{}") return Array.Empty<float>();
			return raw.Split(',').Select(s => float.TryParse(s.Trim(), out var v) ? v : 0f).ToArray();
		}
		static byte? ParseByte(string raw) {
			if(string.IsNullOrWhiteSpace(raw)) return null;
			if(byte.TryParse(raw.Trim(), out var v)) return v;
			return null;
		}
	}

	public class ElementCategory {
		[XmlIgnore]
		public string Name { get; set; }

		[XmlAnyElement("Block")]
		public List<XmlElement> BlockElements { get; set; }
		[XmlIgnore]
		public List<ElementInfo> Blocks { get; set; } = new List<ElementInfo>();

		[XmlAnyElement] internal List<XmlElement> ChildCategoriesRaw { get; set; }

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