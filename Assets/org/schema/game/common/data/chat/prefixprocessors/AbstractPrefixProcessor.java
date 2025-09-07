package org.schema.game.common.data.chat.prefixprocessors;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.network.objects.ChatMessage;

public abstract class AbstractPrefixProcessor implements Comparable<AbstractPrefixProcessor> {

	private final String prefixCommon;

	public AbstractPrefixProcessor(String prefixCommon) {
		super();
		this.prefixCommon = prefixCommon;
	}

	public void localResponse(String text, ChatChannel channel) {

		channel.localChatOnClient(text);

	}

	public String getPrefixCommon() {
		return prefixCommon;
	}

	public void process(ChatMessage msg, ChatChannel channel, GameClientState state) {
		StringBuffer m = new StringBuffer(msg.text);

		m.delete(0, prefixCommon.length());

		int first = m.indexOf(" ");

		String command = m.substring(0, first >= 0 ? (first + 1) : m.length()).trim();

		String parameters;
		if (first < 0) {
			parameters = "";
		} else {
			parameters = m.substring(first).trim();
		}

		process(msg, command, parameters, channel, state);
	}

	protected abstract void process(ChatMessage msg, String command, String parameters, ChatChannel channel, GameClientState state);

	public abstract boolean sendChatMessageAfterProcessing();

	public boolean fits(ChatMessage message) {
		return message.text.startsWith(prefixCommon);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AbstractPrefixProcessor o) {
				return prefixCommon.length() - o.prefixCommon.length();
	}

}
