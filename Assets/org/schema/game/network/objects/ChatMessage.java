package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Vector4f;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.schine.graphicsengine.core.ChatMessageInterface;
import org.schema.schine.graphicsengine.forms.gui.ColoredTimedText;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ChatColorPalette;

public class ChatMessage extends ColoredTimedText implements SerializationInterface, ChatMessageInterface{

	public String sender;
	public String receiver;
	public ChatMessageType receiverType;
	private ChatChannel channel;
	private String wrapped;
	public ChatMessage() {
		super();
	}

	public ChatMessage(ChatMessage msg) {
		super(msg);
		sender = msg.sender;
		receiver = msg.receiver;
		receiverType = msg.receiverType;
		reset();
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeUTF(text);
		b.writeUTF(sender);
		b.writeByte((byte) receiverType.ordinal());
		b.writeUTF(receiver);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		text = b.readUTF();
		sender = b.readUTF();
		receiverType = ChatMessageType.values()[b.readByte()];
		receiver = b.readUTF();
		if(!isOnServer){
			reset();
		}
	}

	public String toDetailString() {
		return "[CHAT][sender=" + sender + "]"
				+ "[receiverType=" + receiverType + "][receiver=" + receiver
				+ "][message=" + text + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (wrapped == null) {
			String msg = sender.length() > 0 ? ("["+sender+"] " + text) : text;
			if (sender.length() == 0 && channel != null) {
				msg = "[" + channel.getName() + "] " + msg;
			}
			wrapped = msg;//StringTools.wrap(msg, 56);//
		}
		return wrapped;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.ColoredTimedText#getColor()
	 */
	@Override
	public Vector4f getStartColor() {
		if(receiverType == ChatMessageType.DIRECT){
			return ChatColorPalette.whisper;
		}
		if (receiverType == ChatMessageType.SYSTEM || (sender != null && sender.length() == 0 && text != null && (text.startsWith("[SERVER]") || text.startsWith("[MESSAGE]")))) {
			return ChatColorPalette.system;
		}

		if (channel != null) {
			return channel.getColor();
		}
		return super.getStartColor();
	}

	public ChatChannel getChannel() {
		return channel;
	}

	public void setChannel(ChatChannel channel) {
		this.channel = channel;
		reset();
	}

	public enum ChatMessageType {
		DIRECT,
		CHANNEL,
		SYSTEM,
	}

}