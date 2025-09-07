package org.schema.game.common.data.player;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.WaypointIcons;
import org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.forms.PositionableSubSprite;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Vector4f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SavedCoordinate extends TransformableEntityMapEntry implements TagSerializable, SerializationInterface {

	private int icon;
	private Vector4f color;
	private boolean removeFlag;
	private Sprite sprite;

	public SavedCoordinate() {
		sector = new Vector3i();
		name = "";
		color = new Vector4f(1, 1, 1, 1);
	}

	public SavedCoordinate(Vector3i sector, String name) {
		this(sector, name, false);
	}

	public SavedCoordinate(Vector3i sector, String name, Vector4f color, int icon) {
		this(sector, name, false);
		this.color = color;
		this.icon = icon;
		pos.set(sector.x * (100.0f / VoidSystem.SYSTEM_SIZEf), sector.y * (100.0f / VoidSystem.SYSTEM_SIZEf), sector.z * (100.0f / VoidSystem.SYSTEM_SIZEf));
	}

	public SavedCoordinate(Vector3i sector, String name, boolean remove) {
		this.sector = sector;
		this.name = name;
		removeFlag = remove;
		color = new Vector4f(1, 1, 1, 1);
		pos.set(sector.x * (100.0f / VoidSystem.SYSTEM_SIZEf), sector.y * (100.0f / VoidSystem.SYSTEM_SIZEf), sector.z * (100.0f / VoidSystem.SYSTEM_SIZEf));
	}


	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		sector = (Vector3i) t[0].getValue();
		name = (String) t[1].getValue();
		if(t.length > 2) {
			color = (Vector4f) t[2].getValue();
			icon = (Integer) t[3].getValue();
		}
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[] {new Tag(Type.VECTOR3i, null, sector), new Tag(Type.STRING, null, name), new Tag(Type.VECTOR4f, null, color), new Tag(Type.INT, null, icon), FinishTag.INST});
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(sector.x);
		b.writeInt(sector.y);
		b.writeInt(sector.z);
		b.writeUTF(name);
		b.writeBoolean(removeFlag);
		b.writeFloat(color.x);
		b.writeFloat(color.y);
		b.writeFloat(color.z);
		b.writeFloat(color.w);
		b.writeInt(icon);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		sector = new Vector3i(b.readInt(), b.readInt(), b.readInt());
		name = b.readUTF();
		removeFlag = b.readBoolean();
		try {
			color = new Vector4f(b.readFloat(), b.readFloat(), b.readFloat(), b.readFloat());
			icon = b.readInt();
		} catch(Exception ignored) {
			color = new Vector4f(1, 1, 1, 1);
			icon = WaypointIcons.OUTPOST.index;
		}
		pos.set(sector.x * (100.0f / VoidSystem.SYSTEM_SIZEf), sector.y * (100.0f / VoidSystem.SYSTEM_SIZEf), sector.z * (100.0f / VoidSystem.SYSTEM_SIZEf));
	}

	public Vector3i getSector() {
		return sector;
	}

	public void setSector(Vector3i sector) {
		this.sector = sector;
	}

	public boolean isRemoveFlag() {
		return removeFlag;
	}

	public void setRemoveFlag(boolean b) {
		removeFlag = b;
	}

	@Override
	public Vector4f getColor() {
		return color;
	}

	public WaypointIcons getIcon() {
		return WaypointIcons.values()[icon];
	}

	@Override
	public int hashCode() {
		return name.hashCode() + sector.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((SavedCoordinate) obj).name) && sector.equals(((SavedCoordinate) obj).sector);
	}

	public SavedCoordinate copy() {
		SavedCoordinate c = new SavedCoordinate();
		c.sector = new Vector3i(sector);
		c.name = name;
		c.color = new Vector4f(color);
		c.icon = icon;
		return c;
	}

	@Override
	public boolean canDraw() {
		return true;
	}

	public void drawMapEntry(Camera camera) {
		if(sprite == null) sprite = WaypointIcons.values()[icon].sprite;
		sprite.setBillboard(true);
		sprite.setBlend(true);
		sprite.setFlip(true);
		Sprite.draw3D(sprite, new PositionableSubSprite[] {this}, camera);
		sprite.setBillboard(false);
		sprite.setFlip(false);
	}

	@Override
	public float getScale(long time) {
		return 0.08f;
	}

	@Override
	public int getSubSprite(Sprite sprite) {
		return icon;
	}
}

