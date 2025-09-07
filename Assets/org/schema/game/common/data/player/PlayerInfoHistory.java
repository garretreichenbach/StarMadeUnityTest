package org.schema.game.common.data.player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.schema.common.SerializationInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class PlayerInfoHistory implements TagSerializable, Comparable<PlayerInfoHistory>, SerializationInterface {
	public long time;
	public String ip;
	public String starmadeName;

	public PlayerInfoHistory() {
	}

	;

	public PlayerInfoHistory(long time, String ip, String stamMadeName) {
		super();
		this.time = time;
		this.ip = ip;
		this.starmadeName = stamMadeName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[time=" + (new Date(time)) + ", ip=" + ip
				+ ", starmadeName=" + starmadeName + "]";
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		time = (Long) t[0].getValue();
		ip = (String) t[1].getValue();
		starmadeName = (String) t[2].getValue();
	}

	@Override
	public Tag toTagStructure() {

		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.LONG, null, time),
				new Tag(Type.STRING, null, ip),
				new Tag(Type.STRING, null, starmadeName != null ? starmadeName : ""),
				FinishTag.INST
		});
	}

	@Override
	public int compareTo(PlayerInfoHistory o) {
		return (int) (time - o.time);
	}

	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(time);
		b.writeUTF(ip);
		b.writeUTF(starmadeName);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		time = b.readLong();
		ip = b.readUTF();
		starmadeName = b.readUTF();
	}

}
