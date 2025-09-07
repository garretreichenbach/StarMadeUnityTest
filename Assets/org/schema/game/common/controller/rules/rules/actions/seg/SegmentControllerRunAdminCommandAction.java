package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.admin.AdminCommandIllegalArgument;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.common.language.Lng;

import java.util.Locale;

public class SegmentControllerRunAdminCommandAction extends SegmentControllerAction {
	
	
	@RuleValue(tag = "AdminCommand")
	public String cmd = "";

	public SegmentControllerRunAdminCommandAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_RUN_ADMIN_COMMAND;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Executes admin command (<uid> replaced with own entity uid)");
	}

	@Override
	public void onTrigger(SegmentController s) {
		if(!s.isOnServer()) {
			return;
		}
		try {
			
			if(cmd.trim().length() > 0) {
				String command = cmd.replaceAll("<uid>", s.getUniqueIdentifier());
				String[] parts = command.split("\\s+");
				AdminCommands valueOf = Enum.valueOf(AdminCommands.class, parts[0].toUpperCase(Locale.ENGLISH));
	
				String param = command.substring(command.indexOf(parts[0]) + parts[0].length()).trim();
				if (param.length() > 0) {
					String[] parameterArray = StringTools.splitParameters(param);
					Object[] packParameters = AdminCommands.packParameters(valueOf, parameterArray);
					((GameServerState) s.getState()).getController().enqueueAdminCommand(((GameServerState) s.getState()).getAdminLocalClient(), valueOf, packParameters);
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
					((GameServerState) s.getState()).getController().enqueueAdminCommand(((GameServerState) s.getState()).getAdminLocalClient(), valueOf, new Object[0]);
				}
				
				
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUntrigger(SegmentController s) {
	}

	
	
}
