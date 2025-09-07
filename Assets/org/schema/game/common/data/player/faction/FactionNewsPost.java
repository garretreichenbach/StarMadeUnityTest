package org.schema.game.common.data.player.faction;

import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class FactionNewsPost implements TagSerializable, Comparable<FactionNewsPost> {

	private int factionId;
	private String op;
	private long date;
	private String message;
	private int permission;
	private String topic = "No Topic";
	private boolean delete;

	public FactionNewsPost() {

	}

	@Override
	public int compareTo(FactionNewsPost f) {
		return (int) (this.date - f.date);
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] sub = (Tag[]) tag.getValue();
		if ("fp-v0".equals(tag.getName())) {
			factionId = ((Integer) sub[0].getValue());
			op = (String) sub[1].getValue();
			date = (Long) sub[2].getValue();
			message = (String) sub[3].getValue();
			permission = (Integer) sub[4].getValue();
			if (sub[5].getType() != Type.FINISH) {
				this.topic = (String) sub[5].getValue();
			}
		} else {
			assert (false) : tag.getName();
		}
	}

	@Override
	public Tag toTagStructure() {

		Tag idTag = new Tag(Type.INT, "id", factionId);
		Tag opTag = new Tag(Type.STRING, "op", op);
		Tag dateTag = new Tag(Type.LONG, "dt", date);
		Tag messageTag = new Tag(Type.STRING, "msg", message);
		Tag permissionTag = new Tag(Type.INT, "perm", permission);
		Tag topicTag = new Tag(Type.STRING, "top", topic);
		return new Tag(Type.STRUCT, "fp-v0", new Tag[]{idTag, opTag, dateTag, messageTag, permissionTag, topicTag, FinishTag.INST});
	}

	/**
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * @return the factionId
	 */
	public int getFactionId() {
		return factionId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the op
	 */
	public String getOp() {
		return op;
	}

	/**
	 * @return the permission
	 */
	public int getPermission() {
		return permission;
	}


	@Override
	public int hashCode() {
		return (int) (this.factionId + this.date + this.op.hashCode() + this.message.hashCode());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		FactionNewsPost o = (FactionNewsPost) obj;
		return this.factionId == o.factionId &&
				this.date == o.date &&
				this.op.equals(o.op) &&
				this.message.equals(o.message);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FactionNewsPost [factionId=" + factionId + ", op=" + op
				+ ", date=" + date + ", message=" + message.replaceAll("\n", " ") + ", permission="
				+ permission + "]";
	}

	public void set(int id, String op, long date, String topic, String message, int permission, boolean delete) {
		this.factionId = id;
		this.op = op;
		this.date = date;
		this.message = message;
		this.permission = permission;
		this.topic = topic;
		this.delete = delete;
	}

	public void set(int id, String op, long date, String topic, String message, int permission) {
		set(id, op, date, topic, message, permission, false);
	}

	public String getTopic() {
		return topic;
	}

	/**
	 * @return the delete
	 */
	public boolean isDelete() {
		return delete;
	}

	/**
	 * @param delete the delete to set
	 */
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}
