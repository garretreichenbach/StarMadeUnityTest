package org.schema.game.common.data.player.faction;

import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class FactionInvite implements TagSerializable {
	private String fromPlayerName;
	private String toPlayerName;
	private long date;
	private int factionUID;

	public FactionInvite() {
	}

	public FactionInvite(String fromPlayerName, String toPlayerName, int factionUID) {
		this(fromPlayerName, toPlayerName, factionUID, System.currentTimeMillis());
	}

	public FactionInvite(String fromPlayerName, String toPlayerName, int factionUID, long date) {
		super();
		set(fromPlayerName, toPlayerName, factionUID, date);
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] d = (Tag[]) tag.getValue();
		fromPlayerName = (String) d[0].getValue();
		toPlayerName = (String) d[1].getValue();
		factionUID = (Integer) d[2].getValue();
		date = (Long) d[3].getValue();

	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.STRING, null, fromPlayerName),
				new Tag(Type.STRING, null, toPlayerName),
				new Tag(Type.INT, null, factionUID),
				new Tag(Type.LONG, null, date),

				FinishTag.INST})
				;
	}

	public long getDate() {
		return date;
	}

	public int getFactionUID() {
		return factionUID;
	}

	public String getFromPlayerName() {
		return fromPlayerName;
	}

	public String getToPlayerName() {
		return toPlayerName;
	}


	@Override
	public int hashCode() {
		return toPlayerName.hashCode() + fromPlayerName.hashCode() + factionUID;
	}

	@Override
	public boolean equals(Object o) {
		return ((FactionInvite) o).toPlayerName.equals(toPlayerName) &&
				((FactionInvite) o).fromPlayerName.equals(fromPlayerName)
				&& ((FactionInvite) o).factionUID == factionUID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FactionInvite [fromPlayerName=" + fromPlayerName
				+ ", toPlayerName=" + toPlayerName + ", date=" + date
				+ ", factionUID=" + factionUID + "]";
	}

	public void set(String from, String to, int facId, long date) {
		this.fromPlayerName = from;
		this.toPlayerName = to;
		this.factionUID = facId;
		this.date = date;
	}
}
