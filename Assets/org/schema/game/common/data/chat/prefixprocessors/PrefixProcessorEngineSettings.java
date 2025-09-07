package org.schema.game.common.data.chat.prefixprocessors;

import java.util.Locale;

import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.network.objects.ChatMessage;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.SettingStateParseError;

public class PrefixProcessorEngineSettings extends AbstractPrefixProcessor {

	public PrefixProcessorEngineSettings() {
		super("//");
	}

	@Override
	protected void process(ChatMessage msg, String command, String parameters, ChatChannel channel,
	                       GameClientState state) {
		try {
			try {
				EngineSettings valueOf = Enum.valueOf(EngineSettings.class, command.toUpperCase(Locale.ENGLISH));

				try {
					valueOf.setFromString(parameters);

					localResponse("[COMMAND] \"" + command + "\" successful: " + valueOf.name() + " = " + valueOf.getAsString(), channel);
				} catch (SettingStateParseError e) {
					throw new RuntimeException("[ERROR] STATE NOT KNOWN: " + e.getMessage());
				}
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(GameClientController.findCorrectedCommand(command));
			}
		} catch (IllegalArgumentException e) {
			if (!e.getMessage().startsWith("[ERROR]")) {
				localResponse("[ERROR] UNKNOWN COMMAND: " + command, channel);
			} else {
				localResponse(e.getMessage(), channel);
			}
		} catch (IndexOutOfBoundsException e1) {
			localResponse(e1.getMessage(), channel);
		}
	}

	@Override
	public boolean sendChatMessageAfterProcessing() {
		return false;
	}

}
