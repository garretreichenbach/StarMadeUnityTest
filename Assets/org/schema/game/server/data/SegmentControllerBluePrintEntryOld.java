package org.schema.game.server.data;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.EntityIndexScore;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.element.ControlElementMap;
import org.schema.game.common.data.element.ControlElementMapper;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.resource.tag.Tag;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntArrayMap;

public class SegmentControllerBluePrintEntryOld implements BlueprintInterface {

	public static final int PRIVACY_SHARED = 0;
	public static final int PRIVACY_PUBLIC = 0;
	public static final String MIGRATION_V0061_TO_V0062 = "v0.061 to v0.062";
	public static final String MIGRATION_V0078_TO_V0079 = "v0.078 to v0.079";
	public static final String MIGRATION_V00897_TO_V00898 = "v0.0897 to v0.0898";
	private static int currentVersion = 6;
	public BoundingBox bb;
	public Vector3i minPos;
	public Vector3i maxPos;
	public String name;
	public int entityType = -1;
	public ControlElementMapper controllingMap;
	public long price;
	public Short2IntArrayMap elementMap;
	public boolean buyable = true;
	public boolean useInWaves = ServerConfig.DEFAULT_BLUEPRINT_ENEMY_USE.isOn();
	public String ownerId = "";
	public int privacy = 0;
	public int version;
	public ArrayList<String> needsMigration = new ArrayList<String>();
	private ElementCountMap elementCountMap;

	public SegmentControllerBluePrintEntryOld(BoundingBox max,
	                                          String name) {
		super();
		this.bb = max;
		this.name = name;
	}

	public SegmentControllerBluePrintEntryOld(DataInputStream in) throws IOException {
		super();
		String n = in.readUTF();
		if (!n.contains(":")) {
			name = n;
			version = 0;
		} else {
			String p[] = n.split(":");
			version = Integer.parseInt(p[1]);
			name = p[0];
		}
		if (version < 3) {
			this.needsMigration.add(MIGRATION_V0061_TO_V0062);
		}
		if (version < 4) {
			this.needsMigration.add(MIGRATION_V0078_TO_V0079);
		}
		if (version < 6) {
			this.needsMigration.add(MIGRATION_V00897_TO_V00898);
		}
		if (version > 4) {
			buyable = in.readBoolean();
			useInWaves = in.readBoolean();
			ownerId = in.readUTF();
			privacy = in.readInt();
			this.entityType = in.readInt();
		} else if (version > 1) {
			buyable = in.readBoolean();
			useInWaves = in.readBoolean();
			ownerId = in.readUTF();
			privacy = in.readInt();
			this.entityType = BlueprintType.SHIP.ordinal();
		} else if (version > 0) {
			buyable = in.readBoolean();
			useInWaves = in.readBoolean();
			ownerId = "";
			this.entityType = BlueprintType.SHIP.ordinal();
		} else {
			buyable = true;
			useInWaves = true;
			ownerId = "";
			this.entityType = BlueprintType.SHIP.ordinal();
		}
		//		System.err.println("BLUPRINT VERSION: "+version+":  for "+name);

		bb = new BoundingBox(new Vector3f(in.readFloat(), in.readFloat(), in.readFloat()),
				new Vector3f(in.readFloat(), in.readFloat(), in.readFloat()));

		int size = in.readInt();
		price = 0;
		elementMap = new Short2IntArrayMap();

		for (int i = 0; i < size; i++) {
			short type = (short) Math.abs(in.readShort());
			int count = in.readInt();
			if (ElementKeyMap.exists(type)) {
				int integer = elementMap.get(type);
				if (integer == 0) {
					elementMap.put(type, 0);
				}else {
					elementMap.put(type, elementMap.get(type) + count);
					price += ElementKeyMap.getInfo(type).getPrice(false) * count;
				}
			}
		}
		elementCountMap = new ElementCountMap();
		elementCountMap.load(elementMap);
		//		System.err.println("[BLUEPRINT] Parsing: "+name+"; price: "+price);
		Tag controlTag = Tag.readFrom(in, false, false);
		this.controllingMap = new ControlElementMapper();
		this.controllingMap = ControlElementMap.mapFromTag(controlTag, this.controllingMap, true);
	}

	public SegmentControllerBluePrintEntryOld(String name) {
		super();
		this.name = name;
	}

	public void calculatePrice(ElementCountMap elementCountMap) {
		try {
			price = elementCountMap.getPrice();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ControlElementMapper getControllingMap() {
		return controllingMap;
	}

	@Override
	public ElementCountMap getElementMap() {
		//used for anti cheat (old blueprints do not support)
		return elementCountMap;
	}

	@Override
	public ElementCountMap getElementCountMapWithChilds() {
		return elementCountMap;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getPrice() {
		return price;
	}

	@Override
	public EntityIndexScore getScore() {
				return null;
	}

	@Override
	public BlueprintType getType() {
		return BlueprintType.SHIP;
	}

	@Override
	public Tag getAiTag() {
				return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (((SegmentControllerBluePrintEntryOld) obj).name).equals(this.name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	public void write(DataOutputStream outReal, SegmentController controller) throws IOException, CannotWriteExeption {

		ByteArrayOutputStream dd = new ByteArrayOutputStream(4096);
		DataOutputStream out = new DataOutputStream(dd);
		if (bb == null) {
			bb = new BoundingBox();
			bb.min.set(0, 0, 0);
			bb.max.set(0, 0, 0);
		}
		out.writeUTF(name + ":" + currentVersion);

		out.writeBoolean(buyable);
		out.writeBoolean(useInWaves);
		out.writeUTF(ownerId);
		out.writeInt(privacy);

		if (controller != null) {
			entityType = BlueprintType.getType(controller.getClass()).ordinal();
		}
		out.writeInt(entityType);

		out.writeFloat(bb.min.x);
		out.writeFloat(bb.min.y);
		out.writeFloat(bb.min.z);
		out.writeFloat(bb.max.x);
		out.writeFloat(bb.max.y);
		out.writeFloat(bb.max.z);

		if (controller != null) {
			out.writeInt(ElementKeyMap.typeList().length);
			for (Short i : ElementKeyMap.typeList()) {
				out.writeShort(i);
				out.writeInt(controller.getElementClassCountMap().get(i));
			}

			Tag tagStructure = controller.getControlElementMap().toTagStructure();
			tagStructure.writeTo(out, false);
		} else {
			if (elementMap == null) {
				throw new CannotWriteExeption();
			}
			assert (elementMap != null);

			out.writeInt(elementMap.size());
			for (Entry<Short, Integer> e : elementMap.entrySet()) {
				out.writeShort(e.getKey());
				out.writeInt(e.getValue());
			}

			assert (controllingMap != null);
			ControlElementMap.mapToTag(controllingMap).writeTo(out, true);
		}

		outReal.writeInt(dd.size());
		dd.writeTo(outReal);

		out.close();

	}

	@Override
	public boolean isChunk16() {
		return true;
	}

	@Override
	public double getCapacitySingle() {
		return 0;
	}

	@Override
	public BoundingBox getBb() {
		return null;
	}

	@Override
	public boolean isOldPowerFlag() {
		return true;
	}

	@Override
	public Long2ObjectOpenHashMap<VoidSegmentPiece> getDockerPoints() {
				return null;
	}

}
