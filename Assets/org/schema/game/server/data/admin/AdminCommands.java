/**
 * @copyright Copyright � 2004-2014 Robin Promesberger (schema)
 */
package org.schema.game.server.data.admin;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.weapon.Weapon;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.StateChangeRequest;
import org.schema.schine.graphicsengine.core.settings.typegetter.TypeGetter;

import java.util.*;

/**
 * The Enum AdminCommands.
 */
public enum AdminCommands {

	@AdminCommandAnnotation(parameters = "true/false")
	LAST_CHANGED(
			"shows the unique id of the players that spawned and/or last modified the selected structure"), DEBUG_FSM_INFO(
			"shows FSM state of objects (Debug Command: slows down network)",
			new AdminParameter(Boolean.class, "show", "false")),

	TELEPORT_TO("teleports a player entity", new AdminParameter(
			String.class, "PlayerName", "schema"), new AdminParameter(
			Float.class, "X", "0.0"), new AdminParameter(Float.class, "Y",
			"1.0"), new AdminParameter(Float.class, "Z", "3.5")),
	TELEPORT_UID_TO("teleports an entity entity", new AdminParameter(
			String.class, "UID", "ENTITY_SHIP_myship"), new AdminParameter(
			Float.class, "X", "0.0"), new AdminParameter(Float.class, "Y",
			"1.0"), new AdminParameter(Float.class, "Z", "3.5")),
	TELEPORT_SELECTED_TO("teleports the currently selected entity", new AdminParameter(
			Float.class, "X", "0.0"), new AdminParameter(Float.class, "Y",
			"1.0"), new AdminParameter(Float.class, "Z", "3.5")),


	KILL_CHARACTER(
			"kills the entity with that name", new AdminParameter(String.class,
			"PlayerName", "schema")),

	TELEPORT_SELF_TO("teleports the current controlled entity",
			new AdminParameter(Float.class, "X", "0.0"), new AdminParameter(
			Float.class, "Y", "1.0"), new AdminParameter(Float.class,
			"Z", "3.5")),

	CHANGE_SECTOR("teleports the current player to another sector",
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4")),

	CHANGE_SECTOR_COPY("teleports the current player to another sector and leave a copy of the current controlled structure behind",
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4")),

	CHANGE_SECTOR_FOR("teleports any player to another sector",
			new AdminParameter(String.class, "player", "schema"),
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4")),
	CHANGE_SECTOR_SELECTED("teleports selected object to another sector",
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4")),
	CHANGE_SECTOR_FOR_UID("teleports any entity (by uid) to another sector",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship"),
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4")),
	CHANGE_SECTOR_FOR_COPY("teleports any player to another sector and leave a copy of the current controlled structure behind",
			new AdminParameter(String.class, "player", "schema"),
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4")),

	EXPORT_SECTOR(
			"exports the whole sector. be sure to use /force_save before",
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4"), new AdminParameter(String.class, "name",
			"mySavedSector")),

	EXPORT_SECTOR_BULK(
			"exports the whole sector from file. be sure to use /force_save before",
			new AdminParameter(String.class, "fileName",
					"sector-bulk-export-import-example.txt")),

	IMPORT_SECTOR_BULK(
			"make sure that the target sector is unloaded", new AdminParameter(
			String.class, "fileName",
			"sector-bulk-export-import-example.txt")),

	IMPORT_SECTOR(
			"make sure that the target sector is unloaded", new AdminParameter(
			Integer.class, "toX", "2"), new AdminParameter(
			Integer.class, "toY", "3"), new AdminParameter(
			Integer.class, "toZ", "4"), new AdminParameter(
			String.class, "name", "mySavedSector")),

	REPAIR_SECTOR(
			"attempts to correct the regitry of the sector",
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "3"), new AdminParameter(Integer.class,
			"Z", "4")),
	FLEET_DEBUG_MOVE(
			"Moves fleet of selected ship between you and sector (patrol). Waits 60 sec on destination",
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
					Integer.class, "Y", "3"), new AdminParameter(Integer.class,
							"Z", "4")),
	FLEET_DEBUG_STOP(
			"Stops any debugging of fleet"),

	LOAD_SYSTEM("loads the whole system (debug)", new AdminParameter(
			Integer.class, "X", "2"), new AdminParameter(Integer.class, "Y",
			"3"), new AdminParameter(Integer.class, "Z", "4")),

	LOAD_SECTOR_RANGE("loads sectors from pos (incl) to pos (incl) (debug)",
			new AdminParameter(Integer.class, "fromX", "2"),
			new AdminParameter(Integer.class, "fromY", "3"),
			new AdminParameter(Integer.class, "fromZ", "4"),
			new AdminParameter(Integer.class, "toX", "9"), new AdminParameter(
			Integer.class, "toY", "8"), new AdminParameter(
			Integer.class, "toZ", "7")),

	TELEPORT_SELF_HOME(
			"teleports the current controlled entity to the spawning point of the player controlling it"),

	DESTROY_ENTITY("Destroys the selected Entity"), DESTROY_ENTITY_DOCK(
			"Destroys the selected Entity and all docked ships"),

	DESTROY_ENTITY_ONLY_DOCK(
			"Destroys all docks of the selected Entity withotu destroying the mother structure"),

	GIVEID("Gives player elements by ID", new AdminParameter(String.class,
			"PlayerName", "schema"), new AdminParameter(Short.class,
			"ElementID", "2"), new AdminParameter(Integer.class, "Count", "10")),

	GIVE("Gives player elements by NAME", new AdminParameter(String.class,
			"PlayerName", "schema"), new AdminParameter(String.class,
			"ElementName", "Power"), new AdminParameter(Integer.class, "Count",
			"10")),

	GIVE_METAITEM("Gives player a meta item)", new AdminParameter(String.class,
			"PlayerName", "schema"),
			new AdminParameter(String.class,
					"Type", getMetaObjectTypesString())),

	GIVE_LASER_WEAPON(
			"Gives player laser weapon)", new AdminParameter(String.class,
			"PlayerName", "schema")),

	GIVE_LASER_WEAPON_OP(
			"Gives player overpowered laser weapon)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_ROCKET_LAUNCHER_OP(
			"Gives player overpowered missile weapon)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_ROCKET_LAUNCHER_TEST(
			"Gives player testing (around ship strength) missile weapon)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_HEAL_WEAPON(
			"Gives player heal beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_MARKER_WEAPON(
			"Gives player marker beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_TRANSPORTER_MARKER_WEAPON(
			"Gives player transporter marker beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_SNIPER_WEAPON(
			"Gives player sniper rifle beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_SNIPER_WEAPON_OP(
			"Gives player overpowered sniper rifle beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_TORCH_WEAPON(
			"Gives player sniper torch beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_TORCH_WEAPON_OP(
			"Gives player overpowered torch beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_GRAPPLE_ITEM(
			"Gives player grapple beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_GRAPPLE_ITEM_OP(
			"Gives player overpowered grapple beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),

	GIVE_ROCKET_LAUNCHER_WEAPON(
			"Gives player rocket launcher)", new AdminParameter(String.class,
			"PlayerName", "schema")),
	GIVE_POWER_SUPPLY_WEAPON(
			"Gives player power supply beam)", new AdminParameter(String.class,
			"PlayerName", "schema")),

	GIVE_CREDITS("Gives player credits)", new AdminParameter(String.class,
			"PlayerName", "schema"), new AdminParameter(Long.class, "Count",
			"1000")),
	SET_INFINITE_INVENTORY_VOLUME("Sets a players inventory to infinite volume)", new AdminParameter(String.class,
			"PlayerName", "schema"), new AdminParameter(Boolean.class, "true/false",
					"true")),
	GATE_DEST("Gives player credits)", new AdminParameter(String.class,
			"UID", "ENTITY_SPACESTATION_something")),

	START_COUNTDOWN("Starts a countdown visible for everyone)",
			new AdminParameter(Integer.class, "Seconds", "180"),
			new AdminParameter(String.class, "Message", "may contain spaces")),

	JUMP("Jump to an object in line of sight if possible"),

	SIMULATION_INFO("Prints info about macro AI Simulation"),
	SIMULATION_SEND_RESPONSE_FLEET("Sends a trading guild response fleet"),

	CREATE_SPAWNER_TEST("Debug Only"),

	REMOVE_SPAWNERS("Removes all spawners from selected entity"),

	RESTRUCT_AABB("Reconstructs the AABBs of all objects on the server"),

	EXECUTE_ENTITY_EFFECT("Debug Only", new AdminParameter(Integer.class,
			"ID", "1")),

//	EXPLODE_PLANET_SECTOR("Simulates the effect of core destruction for all planet segments in the current sector"),

	EXPLODE_PLANET_SECTOR_NOT_CORE("Simulates the effect of core destruction for all planet segments in the current sector (but keeps core)"),

	TP_TO("warp to player's position", new AdminParameter(String.class,
			"PlayerName", "schema")),

	TP("warp a player to your position", new AdminParameter(String.class,
			"PlayerName", "schema")),

	DAYTIME("sets the time of the day in hours", new AdminParameter(
			Integer.class, "TimeInHours", "12")), SIMULATION_INVOKE(
			"invokes the simulation thread directly"),

	SIMULATION_SPAWN_DELAY(
			"sets the time of the day in hours", new AdminParameter(
			Integer.class, "TimeInSecs", "300")),

	SIMULATION_AI_ENABLE(
			"enables/disables AI simulation", new AdminParameter(Boolean.class,
			"enable", "false")),

	MISSILE_DEFENSE_FRIENDLY_FIRE(
			"enables/disables point defense friendly fire", new AdminParameter(Boolean.class,
			"enabled", "false")),

	IGNORE_DOCKING_AREA(
			"enables/disables docking area validation (default off)",
			new AdminParameter(Boolean.class, "enable", "false")),
	PLAYER_PUT_INTO_ENTITY_UID("Puts a player into an entity",
			new AdminParameter(String.class, "Player", "schema"),
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship")
			),
	PLAYER_SET_SPAWN_TO("Sets the spawing point for a player to a sector and a local position within that sector",
			new AdminParameter(String.class, "Player", "schema"),
			new AdminParameter(Integer.class, "SectorX", "23"),
			new AdminParameter(Integer.class, "SectorY", "1"),
			new AdminParameter(Integer.class, "SectorZ", "3"),
			new AdminParameter(Float.class, "LocalX", "324"),
			new AdminParameter(Float.class, "LocalY", "231.2"),
			new AdminParameter(Float.class, "LocalZ", "1.2")
			),
	PLAYER_GET_SPAWN("returns the spawing point for a player",
			new AdminParameter(String.class, "Player", "schema")),

	SET_SPAWN("Sets the spawing point for this player to the current position"),
	FACTION_POINT_TURN("Forces the next faction point calculation turn"),

	FACTION_POINT_SET("Sets faction points of a faction to a value",
			new AdminParameter(Integer.class, "FactionId", "10001"),
			new AdminParameter(Integer.class, "points", "10")),
	FACTION_POINT_ADD("Adds faction points of a faction by a value (negative substracts)",
			new AdminParameter(Integer.class, "FactionId", "10001"),
			new AdminParameter(Integer.class, "points", "10")),
	FACTION_POINT_GET("Retrieves faction points of a faction",
			new AdminParameter(Integer.class, "FactionId", "10001")),

	FACTION_POINT_PROTECT_PLAYER("Protects player from faction point loss on death (persistent)",
			new AdminParameter(String.class, "Name", "schema"),
			new AdminParameter(Boolean.class, "Protect", "false")),
	RUN_SCRIPT("Runs a script in /data/scripts/", new AdminParameter(String.class, "Script Name", "MyScript.lua"), new AdminParameter(String.class, "Function Name", "main")),
	LIST_SCRIPTS("Lists all currently running scripts"),
	CREATURE_SCRIPT("sets the creature to a script in /data/scripts/", new AdminParameter(String.class, "Script", "MyScript.lua")),
	CREATURE_ANIMATION_START("forces an animation for a creature",
			new AdminParameter(String.class, "Animation", "TALK_SALUTE"),
			new AdminParameter(String.class, "LoopMode", "loop/dont_loop"),
			new AdminParameter(Float.class, "Speed", "1"),
			new AdminParameter(Boolean.class, "FullBody", "true")),
	CREATURE_ANIMATION_STOP("stops the forced animation"),
	CREATURE_RENAME("Renames the selected creature or AI character", new AdminParameter(String.class,
			"Name", "ACreature")),
	SET_SPAWN_PLAYER("Sets the spawing point for this player to the current position", new AdminParameter(String.class,
			"Name", "schema")),
	CREATURE_GOTO("order selected to go to"),
	CREATURE_IDLE("makes the select one idle"),
	CREATURE_ROAM("makes the select one roam in a small space"),
	CREATURE_STAND_UP("order selected to stand up from sitting"),
	CREATURE_SIT("order selected to sit down"),
	CREATURE_ENTER_GRAVITY("debug for gravity on AI creature"),

	MISSILE_TARGET_PREDICTION(
			"changes the missile target prediction (in ticks to target velocity, change if missiles are missing fast targets)",
			new AdminParameter(Float.class, "Ticks", "2.0")),
	SHUTDOWN(
			"shutsdown the server in specified seconds (neg values will stop any active countdown)",
			new AdminParameter(Integer.class, "TimeToShutdown", "120")),
	FACTION_RESET_ACTIVITY(
			"resets activity flags for all member of the faction (all to inactive)",
			new AdminParameter(Integer.class, "FactionId", "10001")),
	SHIELD_DAMAGE(
			"damages the shield for value provided",
			new AdminParameter(Integer.class, "Damage", "120")),
	DECAY(
			"sets a structure decayed or not decayed",
			new AdminParameter(Boolean.class, "Decayed", "true")),

	FORCE_SAVE("The server will save all data to disk"),

	STRUCTURE_SET_VULNERABLE("Sets vulnerability of selected structure",
			new AdminParameter(Boolean.class, "vulnerable", "false")
			),
	STRUCTURE_SET_MINABLE("Sets minability of selected structure",
			new AdminParameter(Boolean.class, "minable", "false")),
	STRUCTURE_SET_VULNERABLE_UID("Sets vulnerability of selected structure",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship"),
			new AdminParameter(Boolean.class, "vulnerable", "false")),
	STRUCTURE_SET_MINABLE_UID("Sets minability of selected structure",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship"),
			new AdminParameter(Boolean.class, "minable", "false")),

	ADD_ADMIN("Gives admin rights to (param0(String)))",
			new AdminParameter(String.class, "PlayerName", "schema")

	),
	LIST_ADMIN_DENIED_COMMANDS("Lists all forbidden commands for a player",
			new AdminParameter(String.class, "PlayerName", "schema")

	),
	ADD_ADMIN_DENIED_COMAND("Forbids admin the use of a command",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(String.class, "Command", "destroy_entity")
	),
	REMOVE_ADMIN_DENIED_COMAND("Removes a forbidden command for an admin",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(String.class, "Command", "destroy_entity")
	),

	LIST_ADMINS("Lists all admins"),

	STATUS("Displays server status"),

	LIST_BANNED_IP("Lists all banned IPs"),

	LIST_BANNED_NAME("Lists all banned names"),


	LIST_BANNED_ACCOUNTS("Lists all banned star-made.org accounts"),

	LIST_WHITELIST_ACCOUNTS("Lists all whitelisted star-made.org accounts"),

	LIST_WHITELIST_IP("Lists all whitelisted IPs"),

	LIST_WHITELIST_NAME("Lists all whitelisted names"),



	AI_WEAPON_SWITCH_DELAY("Ai weapon switch delay in ms", new AdminParameter(
			Integer.class, "Delay", "1000")),
	GIVE_LOOK("gives <count> of the block the player is looking at", new AdminParameter(
			Integer.class, "Count", "100")),

	GIVE_SLOT("gives <count> of the block the player has currently selected in the build action bar", new AdminParameter(
			Integer.class, "Count", "100")),

	BAN("bans (and kicks) a playername from this server",
			new AdminParameter(String.class, "Name", "schema"),
			new AdminParameter(Boolean.class, "Kick", "true"),
			new AdminParameterOptional(String.class, "Reason", "hacking"),
			new AdminParameterOptional(Integer.class, "Time (Minutes)", "90")),

	BAN_IP("bans a ip from this server", new AdminParameter(String.class,
			"PlayerIP", "192.0.0.1")),
	BAN_IP_BY_PLAYERNAME("bans a ip of a player from this server", new AdminParameter(String.class,
			"Name", "schema")),

	BAN_IP_TEMP("bans a ip from this server for x minutes", new AdminParameter(String.class,
			"PlayerIP", "192.0.0.1"),
			new AdminParameter(Integer.class, "Time in minutes", "1")),
	BAN_IP_BY_PLAYERNAME_TEMP("bans a ip of a player from this server for x minutes", new AdminParameter(String.class,
			"Name", "schema"),
			new AdminParameter(Integer.class, "Time in minutes", "1")),

	SHIP_INFO_UID("displays info about a ship (works with unloaded)", new AdminParameter(String.class,
			"UID", "SHIP_MYSHIP")),
	SHIP_INFO_NAME("displays info about a ship (works with unloaded)", new AdminParameter(String.class,
			"Name", "MyShip")),
	SHIP_INFO_SELECTED("displays info about a ship"),

	SECTOR_INFO("displays info about a sector (works with unloaded)",
			new AdminParameter(Integer.class, "SectorX", "10"),
			new AdminParameter(Integer.class, "SectorY", "12"),
			new AdminParameter(Integer.class, "SectorZ", "15")),

	PLAYER_GET_INVENTORY("Lists inventory of a player",
			new AdminParameter(String.class,
					"PlayerName", "myNameOnTheServer")),
	PLAYER_GET_BLOCK_AMOUNT("Returns amount of blocks of a certain type in a player's inventory",
			new AdminParameter(String.class, "PlayerName", "myNameOnTheServer"),
			new AdminParameter(Integer.class, "BlockId", "5")
		),
	PLAYER_PROTECT("protects a playername with a star-made.org account name",
			new AdminParameter(String.class,
					"PlayerName", "myNameOnTheServer"),
			new AdminParameter(String.class,
					"StarMadeAccountName", "myLoginName")),
	PLAYER_UNPROTECT("removes protection of a playername with its star-made.org account name", new AdminParameter(String.class,
			"PlayerName", "schema")),

	PLAYER_INFO("displays info about a player (even when player is not on)", new AdminParameter(String.class,
			"Name", "schema")),
	KICK_PLAYERS_OUT_OF_ENTITY("kicks out all players of a selected entity"),

	SHIELD_OUTAGE("brings down the shields of the selected object or the currently controlling"),
	POWER_OUTAGE("brings down the shields of the selected object or the currently controlling"),

	ENTITY_SET_STRUCTURE_HP_PERCENT("sets HP (0 to 1) percent of selected object", new AdminParameter(Float.class, "Percent", "0.5")),
	ENTITY_TRACK("tracks selected entity for admins", new AdminParameter(Boolean.class, "Tracking", "true")),
	ENTITY_TRACK_UID("tracks  entity by UID for admins", new AdminParameter(String.class,
			"UID", "ENTITY_SHIP_my_ship"),new AdminParameter(Boolean.class, "Tracking", "true")),

	ENTITY_REBOOT("resets maxHP of selected entity"),

	ENTITY_SET_CHECK_FLAG("Sets checked flag for this entity"),
	ENTITY_IS_CHECK_FLAG("Checks if selected entity has the check flag"),

	ENTITY_SET_CHECK_FLAG_UID("Sets checked flag for this entity", new AdminParameter(String.class,
			"UID", "ENTITY_SHIP_my_ship")),
	ENTITY_IS_CHECK_FLAG_UID("Checks if selected entity has the check flag", new AdminParameter(String.class,
			"UID", "ENTITY_SHIP_my_ship")),


//	ENTITY_REPAIR_ARMOR("resets max armor HP of selected entity"),
	TEST_STATISTICS_SCRIPT("debug"),

	BAN_ACCOUNT("bans a starMade account from this server (user must be uplinked)", new AdminParameter(String.class,
			"StarMadeUserName", "schema")),
	BAN_ACCOUNT_TEMP("bans a starMade account from this server (user must be uplinked) for x minutes", new AdminParameter(String.class,
			"StarMadeUserName", "schema"),
			new AdminParameter(Integer.class, "Time in minutes", "1")),

	UNBAN_ACCOUNT("unbans a StarMade account from this server", new AdminParameter(String.class,
			"StarMadeUserName", "schema")),

	BAN_ACCOUNT_BY_PLAYERNAME("bans a starMade account from this server by playername (user must be uplinked)", new AdminParameter(String.class,
			"Name", "schema")),
	BAN_ACCOUNT_BY_PLAYERNAME_TEMP("bans a starMade account from this server by playername (user must be uplinked) for x minutes", new AdminParameter(String.class,
			"Name", "schema"),
			new AdminParameter(Integer.class, "Time in minutes", "1")),

	WHITELIST_ACCOUNT("whitelists a starMade account for this server (needs require user auth in server.cfg)", new AdminParameter(String.class,
			"StarMadeUserName", "schema")),

	WHITELIST_NAME("add a playername to the white list", new AdminParameter(
			String.class, "PlayerName", "schema")),

	WHITELIST_IP("add an IP to the white list", new AdminParameter(
			String.class, "PlayerIP", "192.0.0.1")),


	WHITELIST_ACCOUNT_TEMP("whitelists a starMade account for this server (needs require user auth in server.cfg) for x minutes", new AdminParameter(String.class,
			"StarMadeUserName", "schema"),
			new AdminParameter(Integer.class, "Time in minutes", "1")),

	WHITELIST_NAME_TEMP("add a playername to the white list for x minutes", new AdminParameter(
			String.class, "PlayerName", "schema"),
			new AdminParameter(Integer.class, "Time in minutes", "1")),

	WHITELIST_IP_TEMP("add an IP to the white list for x minutes", new AdminParameter(
			String.class, "PlayerIP", "192.0.0.1"),
			new AdminParameter(Integer.class, "Time in minutes", "1")),

	WHITELIST_ACTIVATE("Turns white list on/off (will be saved in server.cfg)",
			new AdminParameter(Boolean.class, "enable", "false")),

	UNBAN_NAME("unbans a playername from this server", new AdminParameter(
			String.class, "PlayerName", "schema")),

	UNBAN_IP("unbans a ip from this server", new AdminParameter(String.class,
			"PlayerIP", "192.0.0.1")),

	PLAYER_SUSPEND_FACTION("suspends a player's membership in a faction and puts the player on neutral", new AdminParameter(String.class,
			"PlayerName", "schema")),
	PLAYER_UNSUSPEND_FACTION("if player was suspended, this will remove them from the current faction and put them back into the faction they were suspended from (with all parameters intact)", new AdminParameter(String.class,
			"PlayerName", "schema")),

	KICK("kicks a player from the server", new AdminParameter(String.class,
			"PlayerName", "schema")),
	KICK_REASON("kicks a player from the server with a reason message (use quotes in message)",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(String.class, "Reason", "\"I don't like you\"")),

	UPDATE_SHOP_PRICES("Updates the prices of all shops instantly"),

	REMOVE_ADMIN("Removes admin rights of player", new AdminParameter(
			String.class, "PlayerName", "schema")),

	REFRESH_SERVER_MSG("Refreshes the server welcome message"),
	PLAYER_LIST("Lists online players"),
	SERVER_MESSAGE_TO("Sends a custom message to a player",
			new AdminParameter(String.class, "Type", "plain/info/warning/error"),
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(String.class, "Message", "Hello!")),

	SERVER_MESSAGE_BROADCAST("Sends a custom message to all players",
			new AdminParameter(String.class, "Type", "plain/info/warning/error"),
			new AdminParameter(String.class, "Message", "Hello!")),

	SAVE("Saves the currently entered/selected Object in the Catalog",
			new AdminParameter(String.class, "NameInCatalog", "myNewShip")),
	SAVE_AS("Saves the currently entered/selected Object in the Catalog with a classification",
			new AdminParameter(String.class, "NameInCatalog", "myNewShip"), new AdminParameter(String.class, "Classification", "Attack")),
	SAVE_UID("Saves the currently entered/selected Object in the Catalog",
			new AdminParameter(String.class, "Uid", "ENTITY_SHIP_myship"),
			new AdminParameter(String.class, "NameInCatalog", "myNewShip")),

	LOAD("Loads the object and puts it in the nearest available spot",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "myShipInGame")),
	LOAD_DOCKED("Loads the object and docks it to the rail the player is looking at (only for ships, picks first available docker block on blueprint)",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "myShipInGame")),
	LOAD_AS_FACTION("Loads the object with a faction and puts it in the nearest available spot",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "myShipInGame"),
			new AdminParameter(Integer.class, "FactionID", "-1")
			),
	LOAD_AS_FACTION_DOCKED("Loads the object with a faction and puts it in the nearest available spot  (only for ships, picks first available docker block on blueprint)",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "myShipInGame"),
			new AdminParameter(Integer.class, "FactionID", "-1")
			),
	LOAD_STATION_NEUTRAL("Loads the object and puts it in the nearest available spot",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "myShipInGame")),
	LOAD_STATION_PIRATE("Loads the object and puts it in the nearest available spot",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "myShipInGame")),
	LOAD_STATION_TRADING_GUILD("Loads the object and puts it in the nearest available spot",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "myShipInGame")),

	LIST_BLUEPRINTS("Lists the current ship catalog"),
	LIST_BLUEPRINTS_BY_OWNER("Lists the current ship catalog by owner",
			new AdminParameter(String.class, "Name", "schema")),
	LIST_BLUEPRINTS_BY_OWNER_VERBOSE("Lists the current ship catalog by owner with extra info such as owner, type and mass",
			new AdminParameter(String.class, "Name", "schema")),
	LIST_BLUEPRINTS_VERBOSE("Lists the current ship catalog with extra info such as owner, type and mass"),
	BREAK_SHIP("tests breaking from this point (debug)"),

	SPAWN_CREATURE("(debug)"),
	SPAWN_CREATURE_MASS("(debug)", new AdminParameter(Integer.class, "amount", "2")),

	SEARCH("Returns the sector of a ship of station with that uid ",
			new AdminParameter(String.class, "ShipOrStationName", "myLostShip")),

	DECAY_UID("Removes an entity (warning: cannot be undone)",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship")),
	DESTROY_UID("Removes an entity (warning: cannot be undone)",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship")),
	DESTROY_UID_DOCKED("Removes an entity and all that is docked to it (warning: cannot be undone)",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship")),

	DESTROY_UID_ONLY_DOCKED("Doesn't remove entity but removes all that is docked to it (warning: cannot be undone)",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_myship")),

	SHOP_RESTOCK_UID("Restocks a shop with a uid. Works for unloaded shops as well",
			new AdminParameter(String.class, "ShopUID", "ENTITY_SHOP_-4198426460705605982_315")),
	SHOP_RESTOCK_FULL_UID("Restocks a shop to full with a uid. Works for unloaded shops as well",
			new AdminParameter(String.class, "ShopUID", "ENTITY_SHOP_-4198426460705605982_315")),

	SIMULATION_CLEAR_ALL("Clears all AI from simulation"),


	SOFT_DESPAWN("Softly unloads an entity (will not be removed from database and reload with the sector loading the next time)"
			),
	SOFT_DESPAWN_DOCK("Softly unloads an entity and everything docked to it (will not be removed from database and reload with the sector loading the next time)"
			),
	DESPAWN_ALL(
			"WARNING: this will delete the entites that start with the given pattern from the database!",
			new AdminParameter(String.class, "ShipNameStart", "MOB_"),
			new AdminParameter(String.class, "Mode(used/unused/all)", "unused"),
			new AdminParameter(Boolean.class, "ShipOnly", "true")),

	DESPAWN_SECTOR(
			"WARNING: this will delete the entites of a sector that start with the given pattern from the database!",
			new AdminParameter(String.class, "ShipNameStart", "MOB_"),
			new AdminParameter(String.class, "Mode(used/unused/all)", "unused"),
			new AdminParameter(Boolean.class, "ShipOnly", "true"),
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "2"), new AdminParameter(Integer.class,
			"Z", "2")),

	POPULATE_SECTOR(
			"WARNING: this will populate the sector. Use this as a reset after using /despawn_sector!",
			new AdminParameter(Integer.class, "X", "2"), new AdminParameter(
			Integer.class, "Y", "2"), new AdminParameter(Integer.class,
			"Z", "2")),

	SPAWN_MOBS(
			"Spawn AI from catalog with factionID at random places around you",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(Integer.class, "factionID", "-1"),
			new AdminParameter(Integer.class, "count", "20")),

	SPAWN_MOBS_LINE(
			"Spawn AI from catalog with factionID at the point you are looking at",
			new AdminParameter(String.class, "CatalogName", "mySavedShip"),
			new AdminParameter(Integer.class, "factionID", "-1"),
			new AdminParameter(Integer.class, "count", "20")),

	INITIATE_WAVE("Initiates an enemy wave", new AdminParameter(Integer.class,
			"Level", "5"), new AdminParameter(Integer.class, "Seconds", "20")
	, new AdminParameter(Integer.class, "FactionID", "-1")),

	START_SHIP_AI("Makes current ship into a AI for faction",
			new AdminParameter(Integer.class, "factionID", "-1")),

	STOP_SHIP_AI("Current ship will stop being AI"),

	GIVE_ALL_ITEMS("Adds every item for a player", new AdminParameter(
			String.class, "PlayerName", "schema"), new AdminParameter(
			Integer.class, "count", "20")),

	GIVE_CATEGORY_ITEMS("Adds for player items from category",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(Integer.class, "count", "20"),
			new AdminParameter(String.class, "terrain/ship/station", "20")),

	SECTOR_CHMOD(
			"Changes the sector mode: example '/sector_chmod 8 8 8 + peace', available modes are 'peace'(no enemy spawn), 'protected'(no attacking possible), 'noenter'(disables entering sector), " +
					"'noexit'(disables leaving sector), 'noindications'(disables hud indicators), 'nofploss'(disables faction point loss)",
			new AdminParameter(Integer.class, "SectorX", "10"),
			new AdminParameter(Integer.class, "SectorY", "12"),
			new AdminParameter(Integer.class, "SectorZ", "15"),
			new AdminParameter(String.class, "+/-", "+"), new AdminParameter(
			String.class, "peace/protected/noenter/noexit/noindications/nofploss", "noenter")),

	SHOP_RESTOCK("Restocks the selected shop with items"),
	SHOP_RESTOCK_FULL("Fully Restocks the selected shop with items"),
	SHOP_INFINITE("Turn infinite shop on/off (unlimited stock at 0 credits price)"),

	SPAWN_ITEM("spawn an item in front of you", new AdminParameter(
			String.class, "BlockName", "power"), new AdminParameter(
			Integer.class, "count", "20")),

	SET_DEBUG_MODE("set's mode to debug server remotely", new AdminParameter(
			Integer.class, "mode", "1")),

	LIST_CONTROL_UNITS("who is attached to what (debug)"),

	TINT("sets a tint (colors must be between 0 and 1) on a selected player (astronaut)",
			new AdminParameter(Float.class, "r", "1"),
			new AdminParameter(Float.class, "g", "0.3"),
			new AdminParameter(Float.class, "b", "0"),
			new AdminParameter(Float.class, "a", "1.0")
	),
	TINT_NAME("sets a tint (colors must be between 0 and 1) on a player by name (astronaut)",
			new AdminParameter(Float.class, "r", "1"),
			new AdminParameter(Float.class, "g", "0.3"),
			new AdminParameter(Float.class, "b", "0"),
			new AdminParameter(Float.class, "a", "1.0"),
			new AdminParameter(String.class, "playername", "schema")
	),

	FLEET_SPEED("How long for a fleet to cross a sector", new AdminParameter(Integer.class, "milliseconds", "6000")),
	FOG_OF_WAR("Turns fog of war on/off", new AdminParameter(Boolean.class, "true/false", "true")),
	MANAGER_CALCULATIONS("Turns manager cancel on/off", new AdminParameter(Boolean.class, "true/false", "true")),
	NPC_DEBUG_MODE("Turns npc debug mode on/off. Very bandwidth intesive!", new AdminParameter(Boolean.class, "true/false", "true")),
	NPC_FLEET_LOADED_SPEED("How fast npc faction ships go when loaded", new AdminParameter(Float.class, "value in percent 0 to 1", "0.5")),
	NPC_TURN_ALL("Turn for all NPC factions"),
	NPC_KILL_RANDOM_IN_SYSTEM("Kills random spanwed npc entity in current system"),
	NPC_BRING_DOWN_SYSTEM_STATUS("Brings down system status in current system", new AdminParameter(Float.class, "status%", "10")),

	NPC_SPAWN_FACTION("Spawns a faction on a random position",
			new AdminParameter(String.class, "name", "\"My NPC Faction\""),
			new AdminParameter(String.class, "description", "\"My Faction's description\""),
			new AdminParameter(String.class, "preset (npc faction config folder name)", "\"Outcasts\""),
			new AdminParameter(Integer.class, "Initial Growth", "10")
	),
	NPC_ADD_SHOP_OWNER("Adds an owner to all NPC faction shops (debug)",
			new AdminParameter(String.class, "name", "\"schema\"")
			),
	NPC_REMOVE_SHOP_OWNER("Removes an owner from all NPC faction shops (debug)",
			new AdminParameter(String.class, "name", "\"schema\"")
			),
	NPC_SPAWN_FACTION_POS_FIXED("Spawns a faction on a fixed position",
		new AdminParameter(String.class, "name", "\"My NPC Faction\""),
		new AdminParameter(String.class, "description", "\"My Faction's description\""),
		new AdminParameter(String.class, "preset (npc faction config folder name)", "\"Outcasts\""),
		new AdminParameter(Integer.class, "Initial Growth", "10"),
		new AdminParameter(Integer.class, "System X", "12"),
		new AdminParameter(Integer.class, "System Y", "3"),
		new AdminParameter(Integer.class, "System Z", "22")
	),
	NPC_REMOVE_FACTION("Removes a faction and all its stuff completely. WARNING: Cannot be undone!",
			new AdminParameter(Integer.class, "FactionID", "-1992232")
	),
	SQL_QUERY("Sends an SQL query! Only people listed in serverconfig SQL_PERMISSION are allowed to. WARNING: Using direct sql can destroy the database and or crash the game. Please only use if you know the consequences!",
			new AdminParameter(String.class, "hsqldb query", "SELECT * FROM ENTITIES WHERE X = 10 AND Y = 3 AND Z = 10")
	),
	SQL_UPDATE("Sends an SQL update! Only people listed in serverconfig SQL_PERMISSION are allowed to. WARNING: Using direct sql can destroy the database and or crash the game. Please only use if you know the consequences!",
	new AdminParameter(String.class, "hsqldb query", "UPDATE ENTITIES SET(REAL_NAME) = ('Shippy Mc shipface') WHERE ID = 234232")
	),
	SQL_INSERT_RETURN_GENERATED_KEYS("Sends an SQL update and returns the generated key (if exists)! Only people listed in serverconfig SQL_PERMISSION are allowed to. WARNING: Using direct sql can destroy the database and or crash the game. Please only use if you know the consequences!",
			new AdminParameter(String.class, "hsqldb query", "INSERT INTO .... ")
			),


	FACTION_LIST("lists all factions"),

	SCAN("Scans system for user (FoW)",
			new AdminParameter(Integer.class, "SystemX", "10"),
			new AdminParameter(Integer.class, "SystemY", "12"),
			new AdminParameter(Integer.class, "SystemZ", "15")),
	TERRITORY_MAKE_UNCLAIMABLE("makes a system unclaimable (use system coords, reset with /territory_reset)",
			new AdminParameter(Integer.class, "SystemX", "10"),
			new AdminParameter(Integer.class, "SystemY", "12"),
			new AdminParameter(Integer.class, "SystemZ", "15")),

	TERRITORY_RESET("takes away claim of a system (use system coords)",
			new AdminParameter(Integer.class, "SystemX", "10"),
			new AdminParameter(Integer.class, "SystemY", "12"),
			new AdminParameter(Integer.class, "SystemZ", "15")),

	FACTION_EDIT("edits a faction with name and description",
			new AdminParameter(Integer.class, "factionID", "1001"),
			new AdminParameter(String.class, "FactionName", "myNewFaction"),
			new AdminParameter(String.class, "Description", "mustbeoneword")),

	FACTION_CREATE("creates a faction with name and description",
			new AdminParameter(String.class, "FactionName", "myNewFaction"),
			new AdminParameter(String.class, "Leader", "playername")
	),
	FACTION_CREATE_AMOUNT("debug command to create an amount of factions",
			new AdminParameter(String.class, "FactionName", "myNewFaction"),
			new AdminParameter(Integer.class, "Amount", "10")
	),
	FACTION_CREATE_AS("creates a faction with name and description)",
			new AdminParameter(Integer.class, "FactionId", "1001"),
			new AdminParameter(String.class, "FactionName", "myNewFaction"),
			new AdminParameter(String.class, "Leader", "playername")
	),

	FACTION_DELETE("removes a faction", new AdminParameter(Integer.class,
			"factionID", "-1")),

	FACTION_CHECK(
			"checks sanity of factions (removes leftover/invalid factions)"),
	SIM_FACTION_SPAWN_TEST(
					"(dev only. do not use)"),

	FACTION_SET_ENTITY("Set faction ID for an entity", new AdminParameter(
			Integer.class, "factionID", "1001")),

	FACTION_SET_ENTITY_UID("Set faction ID for an entity",
			new AdminParameter(String.class, "UID", "SHIP_MYSHIP"),
			new AdminParameter(Integer.class, "factionID", "1001")
			),
	FACTION_SET_ENTITY_RANK("Set faction rank of for selected or entered entity (-2 = unset, -1 = personal, 0 = 4th Rank, 1 = 3rd Rank, ..., 4 = Founder Rank)", new AdminParameter(
			Integer.class, "Rank", "3")),

	FACTION_SET_ENTITY_RANK_UID("Set faction rank of for an entity (-2 = unset, -1 = personal, 0 = 4th Rank, 1 = 3rd Rank, ..., 4 = Founder Rank)",
			new AdminParameter(String.class, "UID", "SHIP_MYSHIP"),
			new AdminParameter(Integer.class, "Rank", "3")
			),

	FACTION_SET_ALL_RELATIONS(
			"Set relation for all factions", new AdminParameter(String.class,
			"Relation", "ally/neutral/enemy")),

	FACTION_LIST_MEMBERS("lists members of faction ", new AdminParameter(
			Integer.class, "factionID", "-1")),

	CLEAR_OVERHEATING("removes all active overheating objects in current sector"),
	CLEAR_OVERHEATING_SECTOR("removes all active overheating objects in specified sector",
			new AdminParameter(Integer.class, "SectorX", "10"),
			new AdminParameter(Integer.class, "SectorY", "12"),
			new AdminParameter(Integer.class, "SectorZ", "15")
			),
	CLEAR_OVERHEATING_ALL("removes all active overheating objects"),

	CLEAR_SYSTEM_SHIP_SPAWNS("removes ship entities in range that are not spawned by a player (admin loaded or mobs)"),
	CLEAR_SYSTEM_SHIP_SPAWNS_ALL("removes all ship entities that are not spawned by a player (admin loaded or mobs)"),

	FACTION_JOIN_ID("joins a player to a faction with given id",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(Integer.class, "factionID", "1001")),

	FACTION_SET_ID_MEMBER(
			"sets a player to a faction id (warning: debug! not a join)",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(Integer.class, "factionID", "1001")),

	FACTION_REINSTITUTE("adds ids to players from faction members"),

	FACTION_MOD_MEMBER("sets a player to role within the faction",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(Integer.class, "Role(1-5)", "1")),

	FACTION_MOD_RELATION("sets relation of two factions", new AdminParameter(
			Integer.class, "FactionID", "1001"), new AdminParameter(
			Integer.class, "FactionID", "1002"), new AdminParameter(
			String.class, "enemy/ally/neutral", "enemy")),

	FACTION_DEL_MEMBER("deletes a player from the faction", new AdminParameter(
			String.class, "PlayerName", "schema"), new AdminParameter(
			Integer.class, "FactionId", "1")),

	GOD_MODE("enables god mode for a player", new AdminParameter(String.class,
			"PlayerName", "schema"), new AdminParameter(Boolean.class,
			"active", "true")),

	CREATIVE_MODE("enables creative mode for a player", new AdminParameter(String.class,
			"PlayerName", "schema"), new AdminParameter(Boolean.class,
				"active", "true")),

	SECTOR_SIZE("sets sector dimension)", new AdminParameter(
			Integer.class, "Size [300-5000]", "1000")),

	INVISIBILITY_MODE("enables invisibility mode for a player",
			new AdminParameter(String.class, "PlayerName", "schema"),
			new AdminParameter(Boolean.class, "active", "true")),

	DEBUG_ID("sets the id of an object to debug", new AdminParameter(
			Integer.class, "ID", "1")),
	CREATE_TRADE_PARTY("creates a trading ship party to refill the selected shop"),

	SET_GLOBAL_SPAWN("sets default spawnpoint to where client is now"),

	DELAY_SAVE("delays autosave in secs",
			new AdminParameter(Integer.class, "Seconds", "60")),

	SPAWN_ENTITY("Spawns a ship in any sector with a faction tag and AI tag.",
			new AdminParameter(String.class, "BlueprintName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "shipName"),
			new AdminParameter(Integer.class, "X", "sectorX"),
			new AdminParameter(Integer.class, "Y", "sectorY"),
			new AdminParameter(Integer.class, "Z", "sectorZ"),
			new AdminParameter(Integer.class, "factionID", "-1"),
			new AdminParameter(Boolean.class, "ActiveAI", "true")),

	SPAWN_ENTITY_POS("Spawns a ship in any sector with a faction tag and AI tag.",
			new AdminParameter(String.class, "BlueprintName", "mySavedShip"),
			new AdminParameter(String.class, "ShipName", "shipName"),
			new AdminParameter(Integer.class, "SecX", "sectorX"),
			new AdminParameter(Integer.class, "SecY", "sectorY"),
			new AdminParameter(Integer.class, "SecZ", "sectorZ"),
			new AdminParameter(Float.class, "PosX", "local-X"),
			new AdminParameter(Float.class, "PosY", "local-Y"),
			new AdminParameter(Float.class, "PosZ", "local-Z"),
			new AdminParameter(Integer.class, "factionID", "-1"),
			new AdminParameter(Boolean.class, "ActiveAI", "true")),

	SHIELD_REGEN("Turn shield regen on/off for selected entity",
			new AdminParameter(Boolean.class, "RegenActive", "true")),

	POWER_REGEN("Turn power regen on/off for selected entity",
			new AdminParameter(Boolean.class, "RegenActive", "true")),

	POWER_DRAIN("Drains the specified amount from the entity's power",
			new AdminParameter(Integer.class, "Drain", "120")),

	SPAWN_PARTICLE("Spawns the provided particle for all players within range",
			new AdminParameter(String.class, "ParticleName", "name")),

	SERVER("Change server", true,
			new AdminParameter(String.class, "Host:port", "play.star-made.org:4242")),
	SERVER_AND_PLAYER("Change server", true,
			new AdminParameter(String.class, "Host:port", "play.star-made.org:4242"),
			new AdminParameter(String.class, "PlayerName", "schema")),

	FLEET_INFO("Debug info for fleets"),

	ENTITY_INFO("Prints info for selected object"),
	ENTITY_INFO_UID("Prints info for object by object uid",
			new AdminParameter(String.class,
					"UID", "SHIP_MYSHIP")
			),
	ENTITY_INFO_NAME("Prints info for object by object name",
			new AdminParameter(String.class,
					"EntityName", "MyShip")
			),
	ENTITY_INFO_BY_PLAYER_UID("Prints info for object the player is in",
			new AdminParameter(String.class,
					"PlayerName", "schema")),


	CLEAR_MINES_HERE("Clear mines in current sector"),
	CLEAR_MINES_SECTOR("Clear mines in target sector",
			new AdminParameter(Integer.class, "X", "sectorX"),
			new AdminParameter(Integer.class, "Y", "sectorY"),
			new AdminParameter(Integer.class, "Z", "sectorZ")),
	KICK_PLAYERS_OUT_OF_ENTITY_UID("Kicks players out of entity",
			new AdminParameter(Integer.class, "uid", "SHIP_SOMESHIP")),
	KICK_PLAYERS_OUT_OF_ENTITY_UID_DOCK("Kicks players out of entity and any of its docks",
			new AdminParameter(Integer.class, "uid", "SHIP_SOMESHIP")),
	KICK_PLAYER_NAME_OUT_OF_ENTITY("Kicks players out of entity by playername",
			new AdminParameter(String.class, "playername", "schema")),
	BLUEPRINT_INFO("blueprint information",
			new AdminParameter(String.class, "blueprintname", "my_ship")),
	BLUEPRINT_DELETE("removes blueprint permanently (warning: cannot be undone)",
			new AdminParameter(String.class, "blueprintname", "my_ship")),
	BLUEPRINT_SET_OWNER("sets owner for a blueprint",
			new AdminParameter(String.class, "blueprintname", "my_ship"),
			new AdminParameter(String.class, "playername", "schema")),
	RAIL_RESET_ALL("Resets rail (undock/redock) rail of all (sub)entities of selected or entered entity"),
	RAIL_RESET("Resets rail (undock/redock) rail of only the selected or entered entity"),

	SET_WEAPON_RANGE_REFERENCE("Sets the weapon reference range distance in meters, which config values are multiplied with (default is sector distance)",
			new AdminParameter(Float.class, "range", "2000")),
	RESET_REPRAIR_DELAY("Resets repair delay on selected/entered vessel"),
	RESET_INTEGRITY_DELAY("Resets integrity delay on selected/entered vessel"),
	GIVE_UID_STORAGE_ID("Puts items into a specific inventory on an entity",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_SOMETHING"),
			new AdminParameter(Integer.class, "inv_coord_x", "3"),
			new AdminParameter(Integer.class, "inv_coord_y", "-4"),
			new AdminParameter(Integer.class, "inv_coord_z", "11"),
			new AdminParameter(Integer.class, "BlockID", "1"),
			new AdminParameter(Integer.class, "count", "100")
			),
	ENTITY_GET_INVENTORY("Reads items from a specific inventory on an entity",
			new AdminParameter(String.class, "UID", "ENTITY_SHIP_SOMETHING"),
			new AdminParameter(Integer.class, "inv_coord_x", "3"),
			new AdminParameter(Integer.class, "inv_coord_y", "-4"),
			new AdminParameter(Integer.class, "inv_coord_z", "11")
			);

	static ArrayList<AdminCommands> sortedSettings = new ArrayList<AdminCommands>();
	/** The description. */
	private final String description;
	private final String parameterString;
	private final AdminParameter[] params;
	private final String exampleString;
	private final int parameterCount;
	private int requiredParameterCount;
	private final boolean localCommand;

	AdminCommands(String description, AdminParameter... params) {
		this(description, false, params);
	}

	/**
	 * Instantiates a new org.schema.schine.graphicsengine settings.
	 * @param description the description
	 */
	AdminCommands(String description, boolean localCommand, AdminParameter... params) {
		this.params = params;
		this.localCommand = localCommand;
		this.description = (localCommand ? "[LOCAL COMMAND] " : "") + description + (localCommand ? "(Can be executed as non admin)" : "");
		parameterCount = params.length;
		requiredParameterCount = parameterCount;
		StringBuffer parameterBuffer = new StringBuffer();
		StringBuffer exampleBuffer = new StringBuffer();

		for (int i = 0; i < params.length; i++) {
			AdminParameter param = params[i];
			boolean optional = param instanceof AdminParameterOptional;

			if (optional) {
				requiredParameterCount--;
			}

			parameterBuffer.append(param.name);
			parameterBuffer.append(optional ? "[" : "(");
			parameterBuffer.append(param.clazz.equals(Boolean.class) ? "True/False" : param.clazz.getSimpleName());
			parameterBuffer.append(optional ? "]" : ")");

			if (i < params.length - 1) {
				parameterBuffer.append(", ");
			}
		}

		parameterString = parameterBuffer.toString();

		exampleBuffer.append("/");
		exampleBuffer.append(name().toLowerCase(Locale.ENGLISH));
		exampleBuffer.append(" ");
		for (int i = 0; i < params.length; i++) {
			exampleBuffer.append(params[i].example);
			if (i < params.length - 1) {
				exampleBuffer.append(" ");
			}
		}
		exampleString = exampleBuffer.toString();

	}
	private static String getMetaObjectTypesString() {
		StringBuffer mString = new StringBuffer();

		for(MetaObjectManager.MetaObjectType t : MetaObjectManager.MetaObjectType.values()){
			if(t != MetaObjectManager.MetaObjectType.WEAPON){
				mString.append(t.name().toLowerCase(Locale.ENGLISH)+", ");
			}
		}
		for(Weapon.WeaponSubType t : Weapon.WeaponSubType.values()){
			mString.append(t.name().toLowerCase(Locale.ENGLISH)+", ");
		}
		mString.delete(mString.length()-2, mString.length());
		return mString.toString();
	}
	// private final Class<? extends Object>[] params;

	public static final String getBBCode() {
		StringBuffer code = new StringBuffer();

		code.append("[table]");
		code.append("\n");
		//		code.append("\t[thead]\n");
		code.append("\t\t[tr]\n");

		code.append("\t\t\t[th]\n");
		code.append("\t\t\t\tCommand[/th]\n");

		code.append("\t\t\t[th]\n");
		code.append("\t\t\t\tDescription[/th]\n");

		code.append("\t\t\t[th]\n");
		code.append("\t\t\t\tParameters[/th]\n");

		code.append("\t\t\t[th]\n");
		code.append("\t\t\t\tSample[/th]\n");
		code.append("\t\t[/tr]\n");
		code.append("\t[/thead]\n");
		//		code.append("\t[tbody]\n");
		for (AdminCommands c : values()) {
			code.append("\t\t[tr]\n");
			code.append("\t\t\t[td]\n");
			code.append("\t\t\t\t" + c.name().toLowerCase(Locale.ENGLISH)
					+ "[/td]\n");

			code.append("\t\t\t[td]\n");
			code.append("\t\t\t\t" + c.description + "[/td]\n");

			// code.append("\t\t\t[td style=\"white-space: nowrap;\"]\n");
			code.append("\t\t\t[td]\n");
			code.append("\t\t\t\t" + c.parameterString + "[/td]\n");

			code.append("\t\t\t[td]\n");
			code.append("\t\t\t\t" + c.exampleString
					+ "[/td]\n");

			code.append("\t\t[tr]\n");
		}
		//		code.append("\t[/tbody]\n");
		code.append("[/table]\n");

		return code.toString();
	}

	public static final String getBBListCode() {
		StringBuffer code = new StringBuffer();

		code.append("\n");
		//		code.append("\t[thead]\n");
		//		code.append("\t\t[tr]\n");
		//
		//		code.append("\t\t\t[th]\n");
		//		code.append("\t\t\t\tCommand[/th]\n");
		//
		//		code.append("\t\t\t[th]\n");
		//		code.append("\t\t\t\tDescription[/th]\n");
		//
		//		code.append("\t\t\t[th]\n");
		//		code.append("\t\t\t\tParameters[/th]\n");
		//
		//		code.append("\t\t\t[th]\n");
		//		code.append("\t\t\t\tSample[/th]\n");
		//		code.append("\t\t[/tr]\n");
		//		code.append("\t[/thead]\n");
		//		code.append("\t[tbody]\n");
		code.append("[FONT=Times New Roman][U][SIZE=7]Admin Command List[/SIZE][/U][/FONT][FONT=Courier New][FONT=Times New Roman][U][SIZE=7] (StarMade v" + VersionContainer.VERSION + ")[/SIZE][/U][/FONT][/FONT]");

		ArrayList<AdminCommands> sorted = new ArrayList<>();
		Collections.addAll(sorted, values());
		Collections.sort(sorted, new Comparator<AdminCommands>() {

			@Override
			public int compare(AdminCommands o1, AdminCommands o2) {
				return o1.name().compareTo(o2.name());
			}
		});

		for (AdminCommands c : sorted) {
			code.append("\t\t[LIST]\n");
			code.append("\t\t\t[*]");
			code.append("[B]" + c.name().toLowerCase(Locale.ENGLISH) + "[/B]");
			code.append("\t\t[LIST]\n");
			code.append("\t\t\t[*]");
			code.append("DESCRIPTION: " + c.description);

			// code.append("\t\t\t[td style=\"white-space: nowrap;\"]\n");
			code.append("\t\t\t[*]");
			code.append("PARAMETERS: " + c.parameterString);

			code.append("\t\t\t[*]");
			code.append("EXAMPLE: " + c.exampleString);

			code.append("\t\t[/LIST]\n");
			code.append("\t\t[/LIST]\n");
		}
		//		code.append("\t[/tbody]\n");

		return code.toString();
	}

	public static final String getCCode() {
		StringBuffer code = new StringBuffer();

		code.append("<table align=\"left\" border=\"2\" cellpadding=\"1\" cellspacing=\"1\" style=\"width: 666px; height: 240px;\">");
		code.append("\n");
		code.append("\t<thead>\n");
		code.append("\t\t<tr>\n");

		code.append("\t\t\t<th scope=\"col\">\n");
		code.append("\t\t\t\tCommand</th>\n");

		code.append("\t\t\t<th scope=\"col\">\n");
		code.append("\t\t\t\tDescription</th>\n");

		code.append("\t\t\t<th scope=\"col\">\n");
		code.append("\t\t\t\tParameters</th>\n");

		code.append("\t\t\t<th scope=\"col\">\n");
		code.append("\t\t\t\tSample</th>\n");
		code.append("\t\t</tr>\n");
		code.append("\t</thead>\n");
		code.append("\t<tbody>\n");
		for (AdminCommands c : values()) {
			code.append("\t\t<tr>\n");
			code.append("\t\t\t<td>\n");
			code.append("\t\t\t\t" + c.name().toLowerCase(Locale.ENGLISH)
					+ "</td>\n");

			code.append("\t\t\t<td>\n");
			code.append("\t\t\t\t" + c.description + "</td>\n");

			// code.append("\t\t\t<td style=\"white-space: nowrap;\">\n");
			code.append("\t\t\t<td>\n");
			code.append("\t\t\t\t" + c.parameterString + "</td>\n");

			code.append("\t\t\t<td style=\"white-space: nowrap;\">\n");
			code.append("\t\t\t\t" + "<pre><strong>" + c.exampleString
					+ "</strong></pre></td>\n");

			code.append("\t\t<tr>\n");
		}
		code.append("\t</tbody>\n");
		code.append("</table>\n");

		return code.toString();
	}

	public static void main(String[] args) {
		//		System.out.println(getCCode());
		//		System.out.println(getBBCode());
		System.out.println(getBBListCode());
	}

	public static Object[] packParameters(AdminCommands c, String... parameters)
			throws AdminCommandIllegalArgument {
		if (c.parameterCount <= 0) {
			return new Object[]{};
		}
		int current = 0;
		try {

			if (parameters.length < c.requiredParameterCount || parameters.length > c.parameterCount){
				throw new IndexOutOfBoundsException();
			}

			Object[] obj = new Object[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				current = i;
				obj[i] = TypeGetter.getTypeGetter(c.params[i].clazz).parseType(
						parameters[i]);
			}

			return obj;

		} catch (Exception e) {
			System.err.println("[ADMIN-COMMAND] Exception caught: "
					+ e.getClass().getSimpleName() + ": " + e.getMessage()
					+ "; " + Arrays.toString(parameters));
			if (e instanceof IndexOutOfBoundsException) {
				String needed = "but should be ";
				if (c.requiredParameterCount != c.parameterCount) {
					needed += "minimum of " + c.requiredParameterCount + ", maximum of " + c.parameterCount;
				} else {
					needed += c.parameterCount;
				}
				throw new AdminCommandIllegalArgument(c, parameters,
						"Invalid Parameter count: used " + parameters.length
								+ ", " + needed);
			}
			if (e instanceof NumberFormatException) {
				throw new AdminCommandIllegalArgument(
						c,
						parameters,
						"Invalid Parameter("
								+ current
								+ ") type: \""
								+ parameters[current]
								+ "\" is not of type ["
								+ c.params[current].clazz.getSimpleName()
								+ "] (if you dont know what that is, use google!)");
			}
			e.printStackTrace();
		}
		throw new AdminCommandIllegalArgument(c, parameters);
	}

	public static String autoCompleteString(String s) {
		s = s.trim();
		ArrayList<AdminCommands> list = list(s);
		// System.err.println("AUTOCOMPLETE: "+s+": "+list.size());
		return StringTools.autoComplete(s, list, true, Enum::name);
	}

	public static ArrayList<AdminCommands> list(String autoComplete) {
		autoComplete = autoComplete.trim().toLowerCase(Locale.ENGLISH);
		if (sortedSettings.isEmpty()) {
			AdminCommands[] values = values();
			Collections.addAll(sortedSettings, values);
			Collections
					.sort(sortedSettings, new AdminCommandLengthComparator());
		}
		ArrayList<AdminCommands> l = new ArrayList<AdminCommands>();

		for (int i = 0; i < sortedSettings.size(); i++) {
			if (sortedSettings.get(i).name().toLowerCase(Locale.ENGLISH)
					.startsWith(autoComplete)) {
				l.add(sortedSettings.get(i));
			}
		}
		return l;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the parameterCount
	 */
	public int getTotalParameterCount() {
		return parameterCount;
	}

	public int getRequiredParameterCount() {
		return requiredParameterCount;
	}

	public String[] list() {
		AdminCommands[] values = values();
		String[] list = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			list[i] = values[i].toString();
		}
		return list;
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH) + " " + parameterString
				+ "(e.g.: " + exampleString + ")";
	}
	public boolean isLocalCommand() {
		return localCommand;
	}

	public void processLocal(ChatChannel c, GameClientState state, Object ... packParameters) {
		String playerName = state.getPlayerName();
		switch (this) {
		case SERVER_AND_PLAYER:
			playerName = packParameters[1].toString().trim();
		case SERVER:
			if(GLFrame.stateChangeRequest == null){
				StateChangeRequest r = new StateChangeRequest();

				String[] ipAndPort = packParameters[0].toString().split(":");
				if(ipAndPort.length != 2){
					c.localChatOnClient("Malformed host:port . Example: "+params[0].example);
					return;
				}
				r.hostPortLogin.host = ipAndPort[0];
				try{
					r.hostPortLogin.port = Integer.parseInt(ipAndPort[1]);
				}catch(NumberFormatException e){
					e.printStackTrace();
					c.localChatOnClient("Malformed port (must be number). Example: "+params[0].example);
					return;
				}

				r.hostPortLogin.loginName = playerName;

				GLFrame.stateChangeRequest = r;
			}

			break;

		default:
			throw new IllegalArgumentException("Local Command '"+name()+"' not implemented");
		}

	}

}
