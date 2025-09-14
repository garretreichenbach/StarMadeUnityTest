using System.Collections.Generic;
using System.Xml.Serialization;

namespace Element {
	public struct ElementInfo {
		//Todo: Port the rest of the fields from BlockConfig.xml
		public short TypeId { get; private set; }
		[XmlAttribute("icon")]
		public int IconId { get; private set; }
		[XmlAttribute("name")]
		public string Name { get; private set; }
		[XmlAttribute("textureId")]
		public short[] TextureIds { get; set; }
		[XmlAttribute("type")]
		public string IdName { get; private set; }
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

	[XmlRoot]
	public struct ElementCategory {
		public string name;
		public ElementCategory[] parents;
		public List<ElementCategory> childCategories;
		public List<ElementInfo> members;
	}

	[XmlRoot("Consistence")]
	public struct Recipe {
		[XmlElement("Item")]
		public List<ItemStack> Items { get; set; }
	}

	[XmlRoot("Item")]
	public struct ItemStack {
		[XmlAttribute("count")]
		public int Count { get; set; }
		[XmlText]
		public string Name { get; set; }

		public ElementInfo GetInfo() {
			return ElementMap.GetInfo(Name);
		}
	}

	[XmlRoot("EffectArmor")]
	public struct EffectArmor {
		[XmlText(Type = typeof(float))]
		public float Heat;
		[XmlText(Type = typeof(float))]
		public float Kinetic;
		[XmlText(Type = typeof(float))]
		public float EM;
	}
}