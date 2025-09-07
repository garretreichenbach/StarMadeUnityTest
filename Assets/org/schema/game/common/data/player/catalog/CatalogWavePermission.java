package org.schema.game.common.data.player.catalog;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class CatalogWavePermission implements TagSerializable{
	public short difficulty;
	public int factionId;
	public int amount;
	@Override
	public void fromTagStructure(Tag t) {
		Tag[] w = (Tag[])t.getValue();
		
		difficulty = (Short) w[0].getValue();
		factionId = (Integer) w[1].getValue();
		amount = (Integer) w[2].getValue();
	}
	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.SHORT, null, difficulty),
			new Tag(Type.INT, null, factionId),
			new Tag(Type.INT, null, amount),
			FinishTag.INST,
		});
	}
	@Override
	public int hashCode() {
		return difficulty * factionId;
	}
	@Override
	public boolean equals(Object obj) {
		return factionId == ((CatalogWavePermission)obj).factionId && difficulty == ((CatalogWavePermission)obj).difficulty;
	}
	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeShort(difficulty);
		buffer.writeInt(factionId);
		buffer.writeInt(amount);
	}
	public void deserialize(DataInput stream) throws IOException {
		difficulty = stream.readShort();
		factionId = stream.readInt();
		amount = stream.readInt();
	}
	
	
}
