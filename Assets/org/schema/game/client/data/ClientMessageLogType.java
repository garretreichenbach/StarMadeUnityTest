package org.schema.game.client.data;

import javax.vecmath.Vector4f;

import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;

public enum ClientMessageLogType {

	CHAT_FACTION(ClientMessageLog.chatColor, new Vector4f(0.7f, 1, 0.7f, 1), en -> Lng.str("Faction chat")),
	CHAT_PRIVATE(ClientMessageLog.chatColor, new Vector4f(1, 1, 0.7f, 1), en -> Lng.str("Private chat")),
	CHAT_PRIVATE_SEND(ClientMessageLog.chatColor, new Vector4f(0.9f, 0.8f, 0.4f, 1), en -> Lng.str("Private chat sent")),
	CHAT_PUBLIC(ClientMessageLog.chatColor, new Vector4f(1, 1, 1, 1), en -> Lng.str("Public chat")),
	GAME(ClientMessageLog.gameColor, new Vector4f(0.74f, 0.74f, 1, 1), en -> Lng.str("Game")),
	INFO(ClientMessageLog.infoColor, new Vector4f(0.7f, 1, 1, 1), en -> Lng.str("Info")),
	ERROR(ClientMessageLog.errorColor, new Vector4f(1, 0.7f, 0.7f, 1), en -> Lng.str("Error")),
	TIP(ClientMessageLog.tipColor, new Vector4f(1, 0.7f, 1, 1), en -> Lng.str("Tip")),
	FLASHING(ClientMessageLog.flashingColor, new Vector4f(0.7f, 0.6f, 8f, 1), en -> Lng.str("Flashing")),;

	private final Translatable name;
	public final Color color;
	public final Vector4f textColor;

	private ClientMessageLogType(Color color, Vector4f textColor, Translatable name) {
		this.name = name;
		this.color = color;
		this.textColor = textColor;
	}

	public String getName() {
		return name.getName(this);
	}
}
