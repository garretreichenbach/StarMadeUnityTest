package org.schema.game.client.data.gamemap.entry;

import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractMapEntry implements MapEntryInterface {

//	public static final byte ENTRY_SHIP = (byte) EntityType.SHIP.damabaseTypeId;
//	public static final byte ENTRY_STATION = (byte) EntityType.SPACE_STATION.damabaseTypeId;
//	public static final byte ENTRY_PLANET = (byte) EntityType.PLANET_SEGMENT.damabaseTypeId;
//	public static final byte ENTRY_SHOP = (byte) EntityType.SHOP.damabaseTypeId;
//	public static final byte ENTRY_ASTEROID = (byte) EntityType.ASTEROID.damabaseTypeId;
//	public static final byte ENTRY_ASTEROID_MANAGED = (byte) EntityType.ASTEROID_MANAGED.damabaseTypeId;

	public static MapEntryInterface[] decode(DataInputStream stream) throws IOException {
		int size = stream.readInt();

		MapEntryInterface[] e = new MapEntryInterface[size];

		for (int i = 0; i < size; i++) {
			e[i] = decodeEntry(stream);
		}

		return e;
	}

	private static MapEntryInterface decodeEntry(DataInputStream stream) throws IOException {
		AbstractMapEntry e = null;
		byte type = stream.readByte();

		EntityType entityType = EntityType.values()[type];
		if (entityType == EntityType.PLANET_CORE || entityType == EntityType.PLANET_SEGMENT || entityType == EntityType.PLANET_ICO) {
			e = new PlanetEntityMapEntry();
		} else if(entityType == EntityType.GAS_PLANET) {
			e = new GasPlanetEntityMapEntry();
		} else {
			e = new TransformableEntityMapEntry();
		}
		assert (e != null);
		e.setType(type);
		e.decodeEntryImpl(stream);
		return e;
	}

	public static void encode(DataOutputStream buffer, MapEntryInterface[] data) throws IOException {

		buffer.writeInt(data.length);
		for (int i = 0; i < data.length; i++) {
			buffer.writeByte(data[i].getType());

			data[i].encodeEntryImpl(buffer);
		}
	}

	protected abstract void decodeEntryImpl(DataInputStream stream) throws IOException;

}
