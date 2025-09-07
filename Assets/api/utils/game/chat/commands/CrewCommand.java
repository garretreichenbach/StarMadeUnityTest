package api.utils.game.chat.commands;

import api.ModPlayground;
import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.element.quarters.crew.CrewUtils;
import org.schema.game.common.data.player.PlayerState;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

/**
 * Debugging command for crew until proper gui-based systems are in place.
 * <p>TODO: Remove this before release!
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class CrewCommand implements CommandInterface {
	@Override
	public String getCommand() {
		return "crew";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"crew"};
	}

	@Override
	public String getDescription() {
		return "Debug command for crew management. TODO: Remove this before release!\n" +
				"- /%COMMAND% add <name> : Spawns a randomized crew member with the specified name.\n" +
				"- /%COMMAND% remove <name> : Removes a crew member with the specified name.\n" +
				"- /%COMMAND% recall <name|*/all> : Recalls a crew member to their station.\n" +
				"- /%COMMAND% get_quarter <name> : Gets the info for a crew member's assigned quarter.\n" +
				"- /%COMMAND% set_area <id> <p1|p2> : Sets a crew quarter's area.\n" +
				"- /%COMMAND% force_update <id> : Forces a crew quarter to update.";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public boolean onCommand(PlayerState sender, String[] args) {
		if(args.length == 0) return false;
		else {
			switch(args[0].toLowerCase(Locale.ENGLISH)) {
				case "add" -> {
					if(args.length == 2) {
						CrewUtils.addCrew(sender, args[1]);
						return true;
					} else return false;
				}
				case "remove" -> {
					if(args.length == 2) {
						CrewUtils.removeCrew(sender, args[1]);
						return true;
					} else return false;
				}
				case "get_quarter" -> {
					if(args.length == 2) {
						PlayerUtils.sendMessage(sender, Objects.requireNonNull(CrewUtils.getQuarterInfo(sender, args[1])).toString());
						return true;
					} else return false;
				}
				case "recall" -> {
					if(args.length == 2) {
						if(args[1].toLowerCase(Locale.ENGLISH).equals("*") || args[1].toLowerCase(Locale.ENGLISH).equals("all")) CrewUtils.recallAllCrew(sender);
						else CrewUtils.recallCrew(sender, args[1]);
						return true;
					} else return false;
				}
				case "set_area" -> {
					if(args.length == 3) {
						Quarter quarter = CrewUtils.getById(sender, Integer.parseInt(args[1]));
						if(quarter != null) CrewUtils.setArea(sender, quarter, args[2]);
					} else return false;
				}
				case "force_update" -> {
					if(args.length == 2) {
						Quarter quarter = CrewUtils.getById(sender, Integer.parseInt(args[1]));
						if(quarter != null) CrewUtils.forceUpdate(quarter);
					} else return false;
				}
				default -> {
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public void serverAction(@Nullable PlayerState sender, String[] args) {

	}

	@Override
	public StarMod getMod() {
		return ModPlayground.inst;
	}
}
