package org.schema.game.common.controller.rules.rules.actions.sector;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.admin.AdminCommandIllegalArgument;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.common.language.Lng;

import java.util.Locale;

public class SectorRunAdminCommandAction extends SectorAction {
	
	
	@RuleValue(tag = "AdminCommand")
	public String cmd = "";

	public SectorRunAdminCommandAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SECTOR_RUN_ADMIN_COMMAND;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Executes admin command (<pos> replaced with own sector pos)");
	}

	@Override
	public void onTrigger(RemoteSector s) {
		if(!s.isOnServer()) {
			return;
		}
		try {
			
			if(cmd.trim().length() > 0) {
				String command = cmd.replaceAll("<pos>", s.getServerSector().pos.x+", "+s.getServerSector().pos.y+", "+s.getServerSector().pos.z);
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
	public void onUntrigger(RemoteSector s) {
	}

	
	
}
