package org.schema.game.common.data.player.playermessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Locale;

import org.schema.game.common.controller.database.DatabaseInsertable;
import org.schema.game.server.data.GameServerState;

public class PlayerMessage implements Comparator<PlayerMessage>, Comparable<PlayerMessage>, DatabaseInsertable {
	private String from;
	private String to;
	private long sent;
	private boolean read;
	private String message;
	private String topic;
	private boolean deleted;
	private boolean changed = true;

	public static PlayerMessage decode(DataInputStream stream) throws IOException {
		PlayerMessage p = new PlayerMessage();
		p.setFrom(stream.readUTF());
		p.to = stream.readUTF();
		p.setTopic(stream.readUTF());
		p.setMessage(stream.readUTF());
		p.sent = stream.readLong();
		p.read = stream.readBoolean();
		p.setDeleted(stream.readBoolean());
		return p;
	}

	@Override
	public int compareTo(PlayerMessage o) {
		return compare(this, o);
	}

	@Override
	public int compare(PlayerMessage o1, PlayerMessage o2) {
		return (int) (o2.sent - o1.sent);
	}

	public void dbUpdate(GameServerState state) {
		try {
			state.getDatabaseIndex().getTableManager().getPlayerMessagesTable().updateOrInsertMessage(this);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void encode(DataOutputStream buffer) throws IOException {
		buffer.writeUTF(from);
		buffer.writeUTF(to);
		buffer.writeUTF(topic);
		buffer.writeUTF(message);
		buffer.writeLong(sent);
		buffer.writeBoolean(read);
		buffer.writeBoolean(deleted);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (sent + from.toLowerCase(Locale.ENGLISH).hashCode() + to.toLowerCase(Locale.ENGLISH).hashCode());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PlayerMessage) {
			PlayerMessage o = (PlayerMessage) obj;
			return sent == o.sent && from.toLowerCase(Locale.ENGLISH).equals(o.from.toLowerCase(Locale.ENGLISH)) && to.toLowerCase(Locale.ENGLISH).equals(o.to.toLowerCase(Locale.ENGLISH));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PlayerMessage [from=" + from + ", to=" + to + ", sent=" + sent
				+ ", read=" + read + ", message=" + message + ", topic="
				+ topic + "]";
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		if (!from.equals(this.from)) {
			changed = true;
		}
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	/**
	 * @return the sent
	 */
	public long getSent() {
		return sent;
	}

	/**
	 * @param sent the sent to set
	 */
	public void setSent(long sent) {
		this.sent = sent;
	}

	/**
	 * @return the read
	 */
	public boolean isRead() {
		return read;
	}

	/**
	 * @param read the read to set
	 */
	public void setRead(boolean read) {
		this.read = read;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		if (!message.equals(this.message)) {
			changed = true;
		}
		this.message = message;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic the topic to set
	 */
	public void setTopic(String topic) {
		if (!topic.equals(this.topic)) {
			changed = true;
		}
		this.topic = topic;

	}

	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		if (deleted != this.deleted) {
			changed = true;
		}
		this.deleted = deleted;
	}

	@Override
	public boolean hasChangedForDb() {
		return changed;
	}

	@Override
	public void setChangedForDb(boolean changed) {
		this.changed = changed;
	}

}
