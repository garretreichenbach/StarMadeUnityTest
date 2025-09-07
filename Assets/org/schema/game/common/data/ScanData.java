package org.schema.game.common.data;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2FloatOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.simulation.resource.PassiveResourceProvider;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class ScanData implements TagSerializable {
	public Vector3i origin = new Vector3i();
	public long time;
	public Universe.SystemOwnershipType systemOwnerShipType = Universe.SystemOwnershipType.NONE;
	public float range;
	public List<DataSet> data;
	public List<ExtractorResourceScanDataSet> extractorResourceData;
	public List<SystemResourceScanDataSet> systemResourceData;
	public boolean extractorScan = false; //whether or not the scanner has extractor scan capabilities

	public void deserialize(DataInput b) throws IOException {
		origin = new Vector3i(b.readInt(), b.readInt(), b.readInt());
		time = b.readLong();
		range = b.readFloat();
		systemOwnerShipType = Universe.SystemOwnershipType.values()[b.readInt()];
		extractorScan = b.readBoolean();

		final int dataSize = b.readInt();
		data = new ObjectArrayList<>(dataSize);

		for(int i = 0; i < dataSize; i++) {
			ScanData.DataSet d = new DataSet();
			d.deserialize(b);
			data.add(d);
		}

		final int resourceDataSize = b.readInt();
		systemResourceData = new ObjectArrayList<>(resourceDataSize);

		for(int i = 0; i < resourceDataSize; i++) {
			SystemResourceScanDataSet d = new SystemResourceScanDataSet();
			d.deserialize(b);
			systemResourceData.add(d);
		}

		final int extractorResourceDataSize = b.readInt();
		extractorResourceData = new ObjectArrayList<>(extractorResourceDataSize);

		for(int i = 0; i < extractorResourceDataSize; i++) {
			ExtractorResourceScanDataSet d = new ExtractorResourceScanDataSet();
			d.deserialize(b);
			extractorResourceData.add(d);
		}
	}

	public void serialize(DataOutput b) throws IOException {
		b.writeInt(origin.x);
		b.writeInt(origin.y);
		b.writeInt(origin.z);
		b.writeLong(time);
		b.writeFloat(range);
		b.writeInt(systemOwnerShipType.ordinal());
		b.writeBoolean(extractorScan);
		if(data != null) {
			b.writeInt(data.size());
			for(DataSet d : data) {
				d.serialize(b);
			}
		} else {
			b.writeInt(0);
		}

		if(systemResourceData != null){
			b.writeInt(systemResourceData.size());
			for(SystemResourceScanDataSet d : systemResourceData){
				d.serialize(b);
			}
		} else {
			b.writeInt(0);
		}

		if(extractorResourceData != null){
			b.writeInt(extractorResourceData.size());
			for(ExtractorResourceScanDataSet d : extractorResourceData){
				d.serialize(b);
			}
		} else {
			b.writeInt(0);
		}
	}

	public void addEntity(PlayerState to, SimpleTransformableSendableObject<?> transformable) {
		if(data == null) {
			data = new ObjectArrayList<>();
		}
		DataSet s = new DataSet();
		s.name = to.getName();
		s.sector = new Vector3i(to.getCurrentSector());
		s.factionId = to.getFactionId();
		if(transformable != null) {
			if(transformable instanceof SegmentController) {
				s.controllerInfo = "in " + transformable.getRealName();
			} else {
				s.controllerInfo = "in Space";
			}
		} else {
			s.controllerInfo = "(not spawned)";
		}
		data.add(s);
	}

	public void addExtractorResourceEntry(PassiveResourceProvider provider, boolean canViewGenerationData) {
		if(extractorResourceData == null) extractorResourceData = new ObjectArrayList<>();
		ExtractorResourceScanDataSet scanDataSet = new ExtractorResourceScanDataSet();
		scanDataSet.canViewGenerationData = canViewGenerationData;
		scanDataSet.name = provider.getSourceTypeInfo().getName();
		scanDataSet.type = provider.getSourceTypeInfo().getName();
		scanDataSet.sector = new Vector3i(provider.getLocation());
		scanDataSet.resourceInfo = provider.getResources().toString();
		extractorResourceData.add(scanDataSet);
	}

	public void addSystemResourceEntry(short type, float amount){
		if(systemResourceData == null) systemResourceData = new ObjectArrayList<>();
		SystemResourceScanDataSet set = new SystemResourceScanDataSet();
		set.setType(type);
		set.resourceQuantity = amount;
		systemResourceData.add(set);
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		origin = (Vector3i) t[0].getValue();
		time = (Long) t[1].getValue();
		range = (Float) t[2].getValue();
		systemOwnerShipType = Universe.SystemOwnershipType.values()[(Integer) t[3].getValue()];
		if(data == null) {
			data = new ObjectArrayList<>();
		}
		Tag.listFromTagStructSP(data, t[4], fromValue -> {
			DataSet d = new DataSet();
			d.fromTagStructure((Tag) fromValue);
			return d;
		});

		if(extractorResourceData == null) extractorResourceData = new ObjectArrayList<>();
		if(t.length > 5) { //Account for old data
			Tag.listFromTagStruct(extractorResourceData, t[5], fromValue -> {
				ExtractorResourceScanDataSet d = new ExtractorResourceScanDataSet();
				d.fromTagStructure((Tag) fromValue);
				return d;
			});
		}
	}

	@Override
	public Tag toTagStructure() {
		if(data == null) {
			//save empty list (no scan activity in this scan)
			data = new ObjectArrayList<>();
		}
		if(extractorResourceData == null) extractorResourceData = new ObjectArrayList<>();
		return new Tag(Tag.Type.STRUCT, "ScanDataTag", new Tag[] {
			new Tag(Tag.Type.VECTOR3i, "Origin", origin),
			new Tag(Tag.Type.LONG, "Time", time),
			new Tag(Tag.Type.FLOAT, "Range", range),
			new Tag(Tag.Type.INT, "SystemOwnershipType", systemOwnerShipType.ordinal()),
			Tag.listToTagStruct(data, "EntityData"),
			Tag.listToTagStruct(extractorResourceData, "ResourceData"),
			FinishTag.INST});
	}

	/**
	 * DataSet for system resource scan data.
	 * <br/> Represents a harvestable resource node accessible via extractors.
	 * <br/><br/>
	 * Base data will only include the star system resource info and so will not include any of these datasets, but the data return can be enhanced through the use of the following chambers:
	 * <li>The <b>Deep Core Scanner Chamber</b> logs information on resources available in planets, gas giants, and asteroids.</li>
	 * <li>The <b>Prospector Scanner Chamber</b> gives information about resource generation rates within the sector.</li>
	 */
	public static class ExtractorResourceScanDataSet implements TagSerializable {
		public String name;
		public String type;
		public Vector3i sector;
		public boolean canViewGenerationData;
		public String resourceInfo;
		public String resourceCaps;
		public String resourceAmounts;

		public void serialize(DataOutput output) throws IOException {
			output.writeUTF(name);
			output.writeUTF(type);
			output.writeInt(sector.x);
			output.writeInt(sector.y);
			output.writeInt(sector.z);
			output.writeBoolean(canViewGenerationData);
			output.writeUTF(resourceInfo);
			output.writeUTF(resourceCaps);
			output.writeUTF(resourceAmounts);
		}

		public void deserialize(DataInput input) throws IOException {
			name = input.readUTF();
			type = input.readUTF();
			sector = new Vector3i(input.readInt(), input.readInt(), input.readInt());
			canViewGenerationData = input.readBoolean();
			resourceInfo = input.readUTF();
			resourceCaps = input.readUTF();
			resourceAmounts = input.readUTF();
		}

		@Override
		public void fromTagStructure(Tag tag) {
			Tag[] t = (Tag[]) tag.getValue();
			name = (String) t[0].getValue();
			type = (String) t[1].getValue();
			sector = (Vector3i) t[2].getValue();
			canViewGenerationData = (Boolean) t[3].getValue();
			resourceInfo = (String) t[4].getValue();
			resourceCaps = (String) t[5].getValue();
			resourceAmounts = (String) t[6].getValue();
		}

		@Override
		public Tag toTagStructure() {
			return new Tag(Tag.Type.STRUCT, "ResourceScanDataSet",
				new Tag[] {
					new Tag(Tag.Type.STRING, "Name", name),
					new Tag(Tag.Type.STRING, "Type", type),
					new Tag(Tag.Type.VECTOR3i, "Sector", sector),
					new Tag(Tag.Type.BYTE, "CanViewGenerationData", canViewGenerationData),
					new Tag(Tag.Type.STRING, "ResourceData", resourceInfo),
					new Tag(Tag.Type.STRING, "ResourceCaps", resourceCaps),
					new Tag(Tag.Type.STRING, "ResourceAmounts", resourceAmounts),
					FinishTag.INST});
		}

		public Short2IntOpenHashMap getResourceInfoMap() {
			Short2IntOpenHashMap map = new Short2IntOpenHashMap();
			String[] entries = resourceInfo.substring(1, resourceInfo.length() - 1).split(", ");
			for(String entry : entries) {
				String[] keyValue = entry.split("=>");
				map.put(Short.parseShort(keyValue[0]), Integer.parseInt(keyValue[1]));
			}
			return map;
		}

		public Short2IntOpenHashMap getResourceCapsMap() {
			Short2IntOpenHashMap map = new Short2IntOpenHashMap();
			String[] entries = resourceCaps.substring(1, resourceCaps.length() - 1).split(", ");
			for(String entry : entries) {
				String[] keyValue = entry.split("=>");
				map.put(Short.parseShort(keyValue[0]), Integer.parseInt(keyValue[1]));
			}
			return map;
		}

		public Short2FloatOpenHashMap getResourceAmountsMap() {
			Short2FloatOpenHashMap map = new Short2FloatOpenHashMap();
			String[] entries = resourceAmounts.substring(1, resourceAmounts.length() - 1).split(", ");
			for(String entry : entries) {
				String[] keyValue = entry.split("=>");
				map.put(Short.parseShort(keyValue[0]), Float.parseFloat(keyValue[1]));
			}
			return map;
		}

		@Override
		public String toString() {
			Short2IntOpenHashMap infoMap = getResourceInfoMap();
			Short2IntOpenHashMap resourceCaps = getResourceCapsMap();
			Short2FloatOpenHashMap resourceAmounts = getResourceAmountsMap();
			if(infoMap.isEmpty()) {
				return "Type: " + type + "\nSector: " + sector + "\nResources: None";
			} else {
				assert infoMap.size() == resourceCaps.size() && infoMap.size() == resourceAmounts.size() : "Resource data mismatch! These maps should all be the same size:\nResource Info: " + infoMap.size() + ", Resource caps: " + resourceCaps.size() + ", Resource amounts: " + resourceAmounts.size() + "";
				StringBuilder builder = new StringBuilder();
				infoMap.forEach((key, value) -> {
					if(ElementKeyMap.isValidType(key)) {
						builder.append("\t").append(ElementKeyMap.getInfo(key).getName());
						if(canViewGenerationData) builder.append(": ").append(value).append(" (").append(resourceAmounts.get(key)).append(" / ").append(resourceCaps.get(key)).append(")\n");
						else builder.append(": ").append(value).append("\n");
					}
				});
				return "Type: " + type + "\nSector: " + sector + "\nResources:\n" + builder.toString().trim();
			}
		}
	}

	/**
	 * DataSet for system resource scan data.
	 * <br/> Represents a type of resource which may be available in the system, and its quantity.
	 */

	public static class SystemResourceScanDataSet implements TagSerializable {
		public String name; //resource name
		public short type; //resource id
		public String resourceInfo; //resource description/info
		public float resourceQuantity; //resource concentration


		public void setType(short type) {
			this.type = type;
			this.name = ElementKeyMap.getNameSave(type);
			ElementInformation e = ElementKeyMap.getInfo(type);
			if(e != null){
                resourceInfo = switch (type) {
					case ElementKeyMap.RESS_GAS_BASTYN, ElementKeyMap.RESS_GAS_ZERCANER -> Lng.str("Collected from Planet atmospheres or Nebulae using a Gas Harvester.");
					case ElementKeyMap.RESS_RARE_EXOGEN, ElementKeyMap.RESS_RARE_METATE, ElementKeyMap.RESS_RARE_QUANTANIUM -> Lng.str("Collected with passive extractor systems.");
                    default -> Lng.str("Mined from asteroids using the Salvage Beam.");
					//TODO refine this once information on each resource is more solidified
                };
			} else resourceInfo = Lng.str("[INVALID RESOURCE %s]", type);
		}

		public void serialize(DataOutput output) throws IOException {
			output.writeShort(type);
			output.writeFloat(resourceQuantity);
		}

		public void deserialize(DataInput input) throws IOException {
			setType(input.readShort());
			resourceQuantity = input.readFloat();
		}

		@Override
		public void fromTagStructure(Tag tag) {
			Tag[] t = (Tag[]) tag.getValue();
			type = (Short) t[0].getValue();
			resourceQuantity = (Float) t[1].getValue();

			name = ElementKeyMap.getNameSave(type);
		}

		@Override
		public Tag toTagStructure() {
			return new Tag(Tag.Type.STRUCT, "ResourceScanDataSet",
					new Tag[] {
							new Tag(Tag.Type.SHORT, "Type", type),
							new Tag(Tag.Type.FLOAT, "ResourceAmounts", resourceQuantity),
							FinishTag.INST});
		}

		@Override
		public String toString() {
			return Lng.str("Resource: ") + name + " (" + type + ") - " + Lng.str("Concentration: ") + (Math.round(resourceQuantity*1000)/10) + "%"; //round quantity % to 1 decimal place
		}
	}

	public static class DataSet implements TagSerializable {
		public String name;
		public Vector3i sector;
		public int factionId;
		public String controllerInfo;

		public void serialize(DataOutput b) throws IOException {
			b.writeUTF(name);
			b.writeInt(sector.x);
			b.writeInt(sector.y);
			b.writeInt(sector.z);
			b.writeInt(factionId);
			b.writeUTF(controllerInfo);
		}

		public void deserialize(DataInput b) throws IOException {
			name = b.readUTF();
			sector = new Vector3i(b.readInt(), b.readInt(), b.readInt());
			factionId = b.readInt();
			controllerInfo = b.readUTF();
		}

		@Override
		public void fromTagStructure(Tag tag) {
			Tag[] t = (Tag[]) tag.getValue();
			name = (String) t[0].getValue();
			sector = (Vector3i) t[1].getValue();
			factionId = (Integer) t[2].getValue();
			controllerInfo = (String) t[3].getValue();
		}

		@Override
		public Tag toTagStructure() {
			return new Tag(Tag.Type.STRUCT, null,
				new Tag[] {
					new Tag(Tag.Type.STRING, null, name),
					new Tag(Tag.Type.VECTOR3i, null, sector),
					new Tag(Tag.Type.INT, null, factionId),
					new Tag(Tag.Type.STRING, null, controllerInfo),
					FinishTag.INST});
		}
	}
}
