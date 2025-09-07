package org.schema.game.common.data.chat.prefixprocessors;

import api.mod.StarLoader;
import api.network.packets.PacketUtil;
import api.network.packets.client.PacketCSAdminCommand;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.server.data.admin.AdminCommandIllegalArgument;
import org.schema.game.server.data.admin.AdminCommands;

import java.util.Locale;

public class PrefixProcessorAdminCommand extends AbstractPrefixProcessor {

	public PrefixProcessorAdminCommand() {
		super("/");
	}

	@Override
	protected void process(ChatMessage msg, String command, String parameters, ChatChannel channel,
	                       GameClientState state) {
		//INSERTED CODE @22
		if(processCustomCommand(command, parameters)) return;
		//
		try {
			try {
				AdminCommands valueOf = Enum.valueOf(AdminCommands.class, command.toUpperCase(Locale.ENGLISH));

				String param = parameters.trim();
				if (param.length() > 0) {
					String[] parameterArray = null;
					parameterArray = StringTools.splitParameters(param);
					Object[] packParameters = AdminCommands.packParameters(valueOf, parameterArray);
					if(valueOf.isLocalCommand()){
						valueOf.processLocal(channel, state, packParameters);
					}else{
						state.getController().sendAdminCommand(valueOf, packParameters);
					}
				} else {
					if (valueOf.getTotalParameterCount() > 0) {
						String needed = "need ";
						if (valueOf.getRequiredParameterCount() != valueOf.getTotalParameterCount()) {
							needed += "minimum of " + valueOf.getRequiredParameterCount();
						} else {
							needed += valueOf.getTotalParameterCount();
						}
						throw new AdminCommandIllegalArgument(valueOf, null, "No parameters provided: " + needed);
					}
					//no parameters needed
					if(valueOf.isLocalCommand()){
						valueOf.processLocal(channel, state);
					}else{
						state.getController().sendAdminCommand(valueOf);
					}
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(GameClientController.findCorrectedCommand(command));
			}

		} catch (IllegalArgumentException e) {
			if (!e.getMessage().startsWith("[ERROR]")) {
				localResponse("[ERROR] UNKNOWN COMMAND: " + command, channel);
			} else {
				localResponse(e.getMessage(), channel);
			}
		} catch (IndexOutOfBoundsException e1) {
			localResponse(e1.getMessage(), channel);
		} catch (AdminCommandIllegalArgument e2) {
			if (e2.getMsg() != null) {
				localResponse("[ERROR] " + e2.getCommand() + ": " + e2.getMsg(), channel);
				localResponse("[ERROR] usage: " + e2.getCommand().getDescription(), channel);
			} else {
				localResponse(e2.getMessage(), channel);
			}
		}
	}
	//INSERTED CODE @74
	private boolean processCustomCommand(String commandUsed, String argsString) {
		if(StarLoader.getClientCommandList().contains(commandUsed)) {
			String[] args = StringTools.splitParameters(argsString);
			PacketCSAdminCommand cmd = new PacketCSAdminCommand(commandUsed, args);
			PacketUtil.sendPacketToServer(cmd);
			return true;
		} else return false;
	}
	//

	@Override
	public boolean sendChatMessageAfterProcessing() {
		return false;
	}

}
