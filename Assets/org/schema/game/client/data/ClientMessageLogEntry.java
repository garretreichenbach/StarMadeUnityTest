package org.schema.game.client.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.schema.common.util.StringTools;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;

public class ClientMessageLogEntry implements Comparable<ClientMessageLogEntry> {
	private static final SimpleDateFormat dateFormat = StringTools.getSimpleDateFormat(Lng.str("dd MMM yyyy HH:mm:ss"), "dd MMM yyyy HH:mm:ss");
	private final String from;
	private final String to;
	private final String message;
	private final long timestamp;
	private final ClientMessageLogType type;

	public ClientMessageLogEntry(String from, String to, String message, long timestamp, ClientMessageLogType type) {
		super();
		this.from = from;
		this.to = to;
		this.message = message.replaceAll("\n", " ");
		this.timestamp = timestamp;
		this.type = type;
	}

	@Override
	public int hashCode() {
		return type.ordinal() + from.hashCode() + to.hashCode() + message.hashCode() + Long.valueOf(timestamp).hashCode();
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ClientMessageLogEntry))
			return false;

		ClientMessageLogEntry m = (ClientMessageLogEntry) o;
		return timestamp == m.timestamp && from.equals(m.from) && to.equals(m.to)
				&& type.ordinal() == (m.type.ordinal()) && message.equals(m.message);
	}

	@Override
	public int compareTo(ClientMessageLogEntry o) {
		return (int) (timestamp - o.timestamp);
	}

	public GUIListElement getGUIListEntry(GameClientState state, boolean dateTag, boolean typeTag) {

		GUITextOverlay t = new GUITextOverlay(FontSize.TINY_11, state);

		String chatStringFull = StringTools.wrap("[" + dateFormat.format(new Date(timestamp)) + "][" + type.name()
				+ "]" + "[" + from + "]" + " " + message, 95);
		String chatStringWithoutDate = StringTools.wrap("[" + type.name() + "]" + "[" + from + "]" + " " + message, 95);
		String chatStringWithoutType = StringTools.wrap("[" + dateFormat.format(new Date(timestamp)) + "][" + from
				+ "]" + " " + message, 95);
		String chatStringWithoutDateWihoutType = StringTools.wrap("[" + from + "]" + " " + message, 95);

		t.setColor(type.textColor);

		if (dateTag && typeTag) {
			t.setTextSimple(chatStringFull);
		} else if (dateTag && !typeTag) {
			t.setTextSimple(chatStringWithoutType);
		} else if (typeTag && !dateTag) {
			t.setTextSimple(chatStringWithoutDate);
		} else {
			t.setTextSimple(chatStringWithoutDateWihoutType);
		}

		GUIListElement m = new GUIListElement(t, t, state) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#getHeight()
			 */
			@Override
			public float getHeight() {
				return super.getHeight() + 5;
			}

		};
		return m;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the type
	 */
	public ClientMessageLogType getType() {
		return type;
	}

}
