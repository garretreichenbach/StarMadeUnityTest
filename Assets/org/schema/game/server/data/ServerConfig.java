package org.schema.game.server.data;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.io.FileUtils;
import org.schema.common.util.StringTools;
import org.schema.common.util.TranslatableEnum;
import org.schema.common.util.settings.*;
import org.schema.game.common.util.DebugUtil;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.ShadowQuality;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.SettingsInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.resource.FileExt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public enum ServerConfig implements SettingsInterface, TranslatableEnum {
	WORLD((Enum e) -> Lng.str("World"), (Enum e) -> Lng.str("set world to use (set 'old' for using the old world). if no world exists a new one will be set automatically"), () -> new SettingStateString("unset"), ServerConfigCategory.DATABASE_SETTING),
	PROTECT_STARTING_SECTOR(en -> Lng.str("Protect starting sector"), en -> Lng.str("Protects the starting sector"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	ENABLE_SIMULATION(en -> Lng.str("Enable simulation"), en -> Lng.str("Universe AI simulation"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	ALLOW_CUSTOM_IMAGES(en -> Lng.str("Allow custom images"), en -> Lng.str("Allow players to upload custom images"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	CONCURRENT_SIMULATION(en -> Lng.str("Concurrent simulations"), en -> Lng.str("How many simulation groups may be in the universe simultaneously (performance)"), () -> new SettingStateInt(256, 1, Integer.MAX_VALUE - 1), ServerConfigCategory.PERFORMANCE_SETTING),
	BLUEPRINTS_USE_COMPONENTS(en -> Lng.str("Blueprints use components"), en -> Lng.str("Blueprints use components instead of blocks"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	ENEMY_SPAWNING(en -> Lng.str("Enemy spawning"), en -> Lng.str("Enables enemy spawning"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SECTOR_SIZE(en -> Lng.str("Sector size"), en -> Lng.str("Sets the size of sectors in the universe **WARNING** scaling the size of an existing universe down may cause issues"), () -> new SettingStateInt(10000, 300, 1000000), ServerConfigCategory.GAME_SETTING),
	BLUEPRINT_DEFAULT_PRIVATE(en -> Lng.str("Blueprints automatically private"), en -> Lng.str("If true, set blueprints private as default (else they are public)"), () -> new SettingStateBoolean(true), ServerConfigCategory.DATABASE_SETTING),
	FLOATING_ITEM_LIFETIME_SECS(en -> Lng.str("Floating item lifetime"), en -> Lng.str("How much seconds items floating in space should be alive"), () -> new SettingStateInt(240, 1, 60 * 60 * 24 * 7), ServerConfigCategory.GAME_SETTING),
	SIMULATION_SPAWN_DELAY(en -> Lng.str("Simulation spawn delay"), en -> Lng.str("How much seconds between simulation spawn ticks"), () -> new SettingStateInt(420, 1, 60 * 60 * 24 * 7), ServerConfigCategory.GAME_SETTING),
	SIMULATION_TRADING_FILLS_SHOPS(en -> Lng.str("Simulation trading fills shops"), en -> Lng.str("Trading guild will deliver stock to shops"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SECTOR_INACTIVE_TIMEOUT(en -> Lng.str("Sector inactive timeout"), en -> Lng.str("Time in secs after which sectors go inactive (-1 = off)"), () -> new SettingStateInt(20, -1, 60 * 60 * 24 * 7), ServerConfigCategory.PERFORMANCE_SETTING),
	SECTOR_INACTIVE_CLEANUP_TIMEOUT(en -> Lng.str("Sector inactive cleanup timeout"), en -> Lng.str("Time in secs after which inactive sectors are completely removed from memory (-1 = off)"), () -> new SettingStateInt(10, -1, 60 * 60 * 24 * 7), ServerConfigCategory.NETWORK_SETTING),
	USE_STARMADE_AUTHENTICATION(en -> Lng.str("Use starmade authentication"), en -> Lng.str("allow star-made.org authentication"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	REQUIRE_STARMADE_AUTHENTICATION(en -> Lng.str("Require starmade authentication"), en -> Lng.str("require star-made.org authentication (USE_STARMADE_AUTHENTICATION must be true)"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	PROTECTED_NAMES_BY_ACCOUNT(en -> Lng.str("Protected names by account"), en -> Lng.str("How many player names a player may protect with his account (if exceeded, the player name, that was logged in the longest time ago gets unprotected)"), () -> new SettingStateInt(10, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	//	STARTING_CREDITS("How many credits a new player has", 25000, () -> new SettingStateInt(0, Integer.MAX_VALUE-1, 25000) ),
	DEFAULT_BLUEPRINT_ENEMY_USE(en -> Lng.str("Default blueprint enemy permission"), en -> Lng.str("Default option for blueprints not in catalog yet"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	DEFAULT_BLUEPRINT_FACTION_BUY(en -> Lng.str("Default blueprint faction permission"), en -> Lng.str("Default option for blueprints not in catalog yet"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	DEFAULT_BLUEPRINT_OTHERS_BUY(en -> Lng.str("Default blueprint others permission"), en -> Lng.str("Default option for blueprints not in catalog yet"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	DEFAULT_BLUEPRINT_HOME_BASE_BUY(en -> Lng.str("Default blueprint homebase permission"), en -> Lng.str("Default option for blueprints not in catalog yet"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	LOCK_FACTION_SHIPS(en -> Lng.str("Lock faction ships"), en -> Lng.str("If true, ships of other factions cant be edited, activated, or entered"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	CATALOG_SLOTS_PER_PLAYER(en -> Lng.str("Catalog slots per player"), en -> Lng.str("How many slots per player for saved ships (-1 for unlimited)"), () -> new SettingStateInt(-1, -1, Integer.MAX_VALUE - 1), ServerConfigCategory.GAME_SETTING),
	UNIVERSE_DAY_IN_MS(en -> Lng.str("Universe day in miliseconds"), en -> Lng.str("how long is a 'day' (stellar system rotation) in milliseconds (-1 to switch off system rotation)"), () -> new SettingStateInt(20 * 60 * 1000, -1, Integer.MAX_VALUE - 1), ServerConfigCategory.GAME_SETTING),
	ASTEROIDS_ENABLE_DYNAMIC_PHYSICS(en -> Lng.str("Asteroid dynamic physics"), en -> Lng.str("enables asteroids to be able to move in space"), () -> new SettingStateBoolean(true), ServerConfigCategory.PERFORMANCE_SETTING),
	ENABLE_BREAK_OFF(en -> Lng.str("Enable break off"), en -> Lng.str("debug (don't activate unless you know what you're doing)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	COLLISION_DAMAGE(en -> Lng.str("Collision damage"), en -> Lng.str("colliding into another object does damage"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	COLLISION_DAMAGE_THRESHOLD(en -> Lng.str("Collision damage threshold"), en -> Lng.str("Threshold of Impulse that does damage (the lower, the less force is needed for damage)"), () -> new SettingStateFloat(2.0f, 0.0f, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SKIN_ALLOW_UPLOAD(en -> Lng.str("Skin allow upload"), en -> Lng.str("if off, skin uploading to server is deactivated"), () -> new SettingStateBoolean(true), ServerConfigCategory.NETWORK_SETTING),
	CATALOG_NAME_COLLISION_HANDLING(en -> Lng.str("Catalog name collision handling"), en -> Lng.str("if off, saving with an existing entry is denied, if on the name is automatically changed by adding numbers on the end"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	SECTOR_AUTOSAVE_SEC(en -> Lng.str("Sector autosave in seconds"), en -> Lng.str("Time interval in secs the server will autosave (-1 for never)"), () -> new SettingStateInt(5 * 60, -1, 60 * 60 * 24 * 7), ServerConfigCategory.PERFORMANCE_SETTING),
	PHYSICS_SLOWDOWN_THRESHOLD(en -> Lng.str("Physics slowdown threshold"), en -> Lng.str("Milliseconds a collision test may take before anti-slowdown mode is activated"), () -> new SettingStateInt(40, -1, Integer.MAX_VALUE - 1), ServerConfigCategory.GAME_SETTING),
	THRUST_SPEED_LIMIT(en -> Lng.str("Thrust speed limit"), en -> Lng.str("How fast ships, etc. may go in m/s . Too high values may induce physics tunneling effects"), () -> new SettingStateInt(75, 1, 5000), ServerConfigCategory.NETWORK_SETTING),
	MAX_CLIENTS(en -> Lng.str("Max clients"), en -> Lng.str("Max number of clients allowed on this server"), () -> new SettingStateInt(32, 1, 1024), ServerConfigCategory.NETWORK_SETTING),
	SUPER_ADMIN_PASSWORD_USE(en -> Lng.str("Enable super admin password"), en -> Lng.str("Enable super admin for this server"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	SUPER_ADMIN_PASSWORD(en -> Lng.str("Super admin password"), en -> Lng.str("Super admin password for this server"), () -> new SettingStateString("mypassword"), ServerConfigCategory.NETWORK_SETTING),
	SERVER_LISTEN_IP(en -> Lng.str("Server listen IP"), en -> Lng.str("Enter specific ip for the server to listen to. use \"all\" to listen on every ip"), () -> new SettingStateString("all"), ServerConfigCategory.NETWORK_SETTING),
	SOCKET_BUFFER_SIZE(en -> Lng.str("Socket buffer size"), en -> Lng.str("buffer size of incoming and outgoing data per socket"), () -> new SettingStateInt(64 * 1024, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	PHYSICS_LINEAR_DAMPING(en -> Lng.str("Physics linear damping"), en -> Lng.str("how much object slow down naturally (must be between 0 and 1): 0 is no slowdown"), () -> new SettingStateFloat(0.05f, 0.0f, 1.0f), ServerConfigCategory.GAME_SETTING),
	PHYSICS_ROTATIONAL_DAMPING(en -> Lng.str("Physics rotational damping"), en -> Lng.str("how much object slow down naturally (must be between 0 and 1): 0 is no slowdown"), () -> new SettingStateFloat(0.05f, 0.0f, 1.0f), ServerConfigCategory.GAME_SETTING),
	AI_DESTRUCTION_LOOT_COUNT_MULTIPLIER(en -> Lng.str("AI Destruction loot count multiplier"), en -> Lng.str("multiply amount of items in a loot stack. use values smaller 1 for less and 0 for none"), () -> new SettingStateFloat(0.9f, 0.0f, 100.0f), ServerConfigCategory.GAME_SETTING),
	AI_DESTRUCTION_LOOT_STACK_MULTIPLIER(en -> Lng.str("AI Destruction loot stack multiplier"), en -> Lng.str("multiply amount of items spawned after AI destruction. use values smaller 1 for less and 0 for none"), () -> new SettingStateFloat(0.9f, 0.0f, 100.0f), ServerConfigCategory.GAME_SETTING),
	CHEST_LOOT_COUNT_MULTIPLIER(en -> Lng.str("Chest loot count multiplier"), en -> Lng.str("multiply amount of items in a loot stack. use values smaller 1 for less and 0 for none"), () -> new SettingStateFloat(0.9f, 0.0f, 100.0f), ServerConfigCategory.GAME_SETTING),
	CHEST_LOOT_STACK_MULTIPLIER(en -> Lng.str("Chest loot stack multiplier"), en -> Lng.str("multiply amount of items spawned in chests of generated chests. use values smaller 1 for less and 0 for none"), () -> new SettingStateFloat(0.9f, 0.0f, 100.0f), ServerConfigCategory.GAME_SETTING),
	USE_WHITELIST(en -> Lng.str("Use whitelist"), en -> Lng.str("only names/ips from whitelist.txt are allowed"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	FILTER_CONNECTION_MESSAGES(en -> Lng.str("Filter connection message"), en -> Lng.str("don't display join/disconnect messages"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	USE_UDP(en -> Lng.str("Use UDP"), en -> Lng.str("Use 'User Datagram Protocol' (UDP) instead of 'Transmission Control Protocol' (TCP) for connections"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	AUTO_KICK_MODIFIED_BLUEPRINT_USE(en -> Lng.str("Auto kick for modified blueprint usage"), en -> Lng.str("Kick players that spawn modified blueprints"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	AUTO_BAN_ID_MODIFIED_BLUEPRINT_USE(en -> Lng.str("Auto ban for modified blueprint usage"), en -> Lng.str("Ban player by name that spawn modified blueprints"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	AUTO_BAN_IP_MODIFIED_BLUEPRINT_USE(en -> Lng.str("Auto IP ban for modified blueprint usage"), en -> Lng.str("Ban player by IP that spawn modified blueprints"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	AUTO_BAN_TIME_IN_MINUTES(en -> Lng.str("Auto ban time in minutes"), en -> Lng.str("Time to ban in minutes (-1 for permanently)"), () -> new SettingStateInt(60, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	REMOVE_MODIFIED_BLUEPRINTS(en -> Lng.str("Remove modified blueprints"), en -> Lng.str("Auto-removes a modified blueprint"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	TCP_NODELAY(en -> Lng.str("TCP No delay"), en -> Lng.str("Naggles algorithm (WARNING: only change when you know what you're doing)"), () -> new SettingStateBoolean(true), ServerConfigCategory.NETWORK_SETTING),
	PING_FLUSH(en -> Lng.str("Ping flush"), en -> Lng.str("flushes ping/pong immediately (WARNING: only change when you know what you're doing)"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	SHOP_SPAWNING_PROBABILITY(en -> Lng.str("Shop spawning probability"), en -> Lng.str("(must be between 0 and 1): 0 is no shops spawned in asteroid sectors, 1 is shop spawned in everyone (default: 8% -> 0.08)"), () -> new SettingStateFloat(0.01f, 0.0f, 1.0f), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_SECTOR_X(en -> Lng.str("Default Spawn sector X"), en -> Lng.str("DEFAULT Spawn Sector X Coordinate"), () -> new SettingStateInt(2, Integer.MIN_VALUE, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_SECTOR_Y(en -> Lng.str("Default Spawn sector Y"), en -> Lng.str("DEFAULT Spawn Sector Y Coordinate"), () -> new SettingStateInt(2, Integer.MIN_VALUE, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_SECTOR_Z(en -> Lng.str("Default Spawn sector Z"), en -> Lng.str("DEFAULT Spawn Sector Z Coordinate"), () -> new SettingStateInt(2, Integer.MIN_VALUE, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MODIFIED_BLUEPRINT_TOLERANCE(en -> Lng.str("Modified blueprint tolerance"), en -> Lng.str("Tolerance of modified blueprint trigger (default = 10%)"), () -> new SettingStateFloat(0.1f, 0.0f, 100.0f), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_X_1(en -> Lng.str("Default Spawn local position nr1 X"), en -> Lng.str("First Rotating Spawn: Local Pos X Coordinate"), () -> new SettingStateFloat(-6.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Y_1(en -> Lng.str("Default Spawn local position nr1 Y"), en -> Lng.str("First Rotating Spawn: Local Pos Y Coordinate"), () -> new SettingStateFloat(251.5f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Z_1(en -> Lng.str("Default Spawn local position nr1 Z"), en -> Lng.str("First Rotating Spawn: Local Pos Z Coordinate"), () -> new SettingStateFloat(0.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_X_2(en -> Lng.str("Default Spawn local position nr2 X"), en -> Lng.str("Second Rotating Spawn: Local Pos X Coordinate"), () -> new SettingStateFloat(-6.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Y_2(en -> Lng.str("Default Spawn local position nr2 Y"), en -> Lng.str("Second Rotating Spawn: Local Pos Y Coordinate"), () -> new SettingStateFloat(251.5f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Z_2(en -> Lng.str("Default Spawn local position nr2 Z"), en -> Lng.str("Second Rotating Spawn: Local Pos Z Coordinate"), () -> new SettingStateFloat(0.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_X_3(en -> Lng.str("Default Spawn local position nr3 X"), en -> Lng.str("Third Rotating Spawn: Local Pos X Coordinate"), () -> new SettingStateFloat(-6.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Y_3(en -> Lng.str("Default Spawn local position nr3 Y"), en -> Lng.str("Third Rotating Spawn: Local Pos Y Coordinate"), () -> new SettingStateFloat(251.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Z_3(en -> Lng.str("Default Spawn local position nr3 Z"), en -> Lng.str("Third Rotating Spawn: Local Pos Z Coordinate"), () -> new SettingStateFloat(0.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_X_4(en -> Lng.str("Default Spawn local position nr4 X"), en -> Lng.str("Forth Rotating Spawn: Local Pos X Coordinate"), () -> new SettingStateFloat(-6.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Y_4(en -> Lng.str("Default Spawn local position nr4 Y"), en -> Lng.str("Forth Rotating Spawn: Local Pos Y Coordinate"), () -> new SettingStateFloat(251.5f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	DEFAULT_SPAWN_POINT_Z_4(en -> Lng.str("Default Spawn local position nr4 Z"), en -> Lng.str("Forth Rotating Spawn: Local Pos Z Coordinate"), () -> new SettingStateFloat(0.0f, -10000, 10000), ServerConfigCategory.GAME_SETTING),
	PLAYER_DEATH_CREDIT_PUNISHMENT(en -> Lng.str("Player credit loss on death punishment (ratio)"), en -> Lng.str("players credits lost of total on death (must be between 0 and 1): 1 = lose all, 0 = keep all"), () -> new SettingStateFloat(0.1f, 0.0f, 1.0f), ServerConfigCategory.GAME_SETTING),
	PLAYER_DEATH_CREDIT_DROP(en -> Lng.str("Player credit loss on death"), en -> Lng.str("drop credits lost on death into space instead"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	PLAYER_DEATH_BLOCK_PUNISHMENT(en -> Lng.str("Player block loss on death"), en -> Lng.str("player will drop all his blocks into space on death"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	PLAYER_DEATH_PUNISHMENT_TIME(en -> Lng.str("Player no punishment time after death"), en -> Lng.str("Time interval in seconds after death of a player in which the player is not punished"), () -> new SettingStateInt(5 * 60, 0, 60 * 60 * 24 * 7), ServerConfigCategory.GAME_SETTING),
	//	PLAYER_DEATH_INVULNERABILITY_TIME(new Translatable() {
//		@Override
//		public String getName(Enum en) {
//			return Lng.str("Player invulnerability time after death");
//		}
//	}, new Translatable() {
//		@Override
//		public String getName(Enum en) {
//			return Lng.str("Time the player is invulnerable after death in sec");
//		}
//	}, 5, () -> new SettingStateInt(0, 60 * 60 * 24 * 7, 5), ServerConfigCategory.GAME_SETTING),
	PLAYER_HISTORY_BACKLOG(en -> Lng.str("PLayer history backlog"), en -> Lng.str("How many login history objects (with name, IP, account-name, and time) should be saved by player state"), () -> new SettingStateInt(30, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	PROJECTILES_ADDITIVE_VELOCITY(en -> Lng.str("Projectiles additive velocity"), en -> Lng.str("initial projectile speed depend on relative linear velocity of object fired from"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	PROJECTILES_VELOCITY_MULTIPLIER(en -> Lng.str("Projectiles additive velocity multiplier"), en -> Lng.str("multiplicator for projectile velocity"), () -> new SettingStateFloat(1.0f, 0.0f, 100000.0f), ServerConfigCategory.GAME_SETTING),
	WEAPON_RANGE_REFERENCE(en -> Lng.str("Weapon range reference distance"), en -> Lng.str("Reference distance for weapon ranges. (what blockBehaviorConfig.xml weapon ranges are multiplied with (usually the sector size)). Set to 1 to interpret weapon ranges in the config in meters"), () -> new SettingStateFloat(1000.0f, 300.0f, 1000000.0f), ServerConfigCategory.GAME_SETTING),
	ALLOW_UPLOAD_FROM_LOCAL_BLUEPRINTS(en -> Lng.str("Allow upload from local blueprints"), en -> Lng.str("enables clients being able to upload their pre-build-blueprints to the server"), () -> new SettingStateBoolean(true), ServerConfigCategory.NETWORK_SETTING),
	SHOP_NPC_STARTING_CREDITS(en -> Lng.str("NPC Shop starting credits"), en -> Lng.str("how much credits do shops start with"), () -> new SettingStateInt(10000000, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SHOP_NPC_RECHARGE_CREDITS(en -> Lng.str("NPC Shop recharge credit amount / 10 minutes "), en -> Lng.str("how much credits do shops gain about every 10 min"), () -> new SettingStateInt(100000, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	AI_WEAPON_AIMING_ACCURACY(en -> Lng.str("AI Weapon accuracy"), en -> Lng.str("how accurate the AI aims (the higher the value the more accurate vs distance. 10 = about 99% accuracy at 10m)"), () -> new SettingStateInt(10, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	BROADCAST_SHIELD_PERCENTAGE(en -> Lng.str("Broadcast shield percentage"), en -> Lng.str("percent of shields changed for the server to broadcast a shield synch"), () -> new SettingStateInt(5, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	BROADCAST_POWER_PERCENTAGE(en -> Lng.str("Broadcast power percentage"), en -> Lng.str("percent of power changed for the server to broadcast a power synch (not that critical)"), () -> new SettingStateInt(20, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	ADMINS_CIRCUMVENT_STRUCTURE_CONTROL(en -> Lng.str("Admins circumvent structure control"), en -> Lng.str("admins can enter ships of any faction"), () -> new SettingStateBoolean(true), ServerConfigCategory.NETWORK_SETTING),
	STAR_DAMAGE(en -> Lng.str("Heat damage"), en -> Lng.str("suns dealing damage to entities"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SQL_NIO_FILE_SIZE(en -> Lng.str("SQL NIO File size"), en -> Lng.str("megabyte limit of .data file when to use NIO (faster) (must be power of 2)"), () -> new SettingStateInt(512, 256, Integer.MAX_VALUE), ServerConfigCategory.DATABASE_SETTING),
	GALAXY_DENSITY_TRANSITION_INNER_BOUND(en -> Lng.str("Galaxy density transition inner bound"), en -> Lng.str("Controls how much of the galaxy is considered to be within the core zone of the galaxy"), () -> new SettingStateFloat(0.1f, 0.1f, 1.0f), ServerConfigCategory.GAME_SETTING),
	GALAXY_DENSITY_TRANSITION_OUTER_BOUND(en -> Lng.str("Galaxy density transition outer bound"), en -> Lng.str("Controls how much of the galaxy is considered to be within the outer zone of the galaxy"), () -> new SettingStateFloat(0.2f, 0.1f, 1.0f), ServerConfigCategory.GAME_SETTING),
	GALAXY_DENSITY_RATE_INNER(en -> Lng.str("Galaxy inner density rate"), en -> Lng.str("How dense the galaxy is in the inner regions (must be between 0 and 1): 0 is no stars, 1 is full density"), () -> new SettingStateFloat(0.62f, 0.1f, 1.0f), ServerConfigCategory.GAME_SETTING),
	GALAXY_DENSITY_RATE_OUTER(en -> Lng.str("Galaxy outer density rate"), en -> Lng.str("How dense the galaxy is in the outer regions (must be between 0 and 1): 0 is no stars, 1 is full density"), () -> new SettingStateFloat(0.75f, 0.1f, 1.0f), ServerConfigCategory.GAME_SETTING),
	PLANET_CORE_RADIUS(en -> Lng.str("Planet Core Radius"), en -> Lng.str("How large the planet core is in comparison to the planet itself. The larger the core, the less chunks are generated inside planets. It's recommended to keep this between 0.5 and 0.8 for performance reasons."), () -> new SettingStateFloat(0.65f, 0.3f, 0.8f), ServerConfigCategory.GAME_SETTING),
	PLANET_SIZE_MEAN_VALUE(en -> Lng.str("Planet size mean"), en -> Lng.str("Planet size mean (normal gaussian distribution) (min 300)"), () -> new SettingStateFloat(350.0f, 300.0f, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	PLANET_SIZE_DEVIATION_VALUE(en -> Lng.str("Planet size deviation"), en -> Lng.str("Planet size standard deviation. Note: normal gaussian distribution graph scaled horizontally by 1/3 (min 0)"), () -> new SettingStateFloat(150.0f, 0, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	GAS_PLANET_SIZE_MEAN_VALUE(en -> Lng.str("Gas planet size mean"), en -> Lng.str("Gas planet size mean (normal gaussian distribution) (min 300)"), () -> new SettingStateFloat(500.0f, 300.0f, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	GAS_PLANET_SIZE_DEVIATION_VALUE(en -> Lng.str("Gas planet size deviation"), en -> Lng.str("Gas planet size standard deviation. Note: normal gaussian distribution graph scaled horizontally by 1/3 (min 0)"), () -> new SettingStateFloat(500.0f, 0, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	ASTEROID_RADIUS_MAX(en -> Lng.str("Asteroids max radius"), en -> Lng.str("Asteroid max radius in blocks (from -x to +x)"), () -> new SettingStateInt(64, 1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	ASTEROID_RESOURCE_SIZE(en -> Lng.str("Asteroids resource veins diameter"), en -> Lng.str("Average diameter of resource veins in asteroids"), () -> new SettingStateFloat(2.5f, 1.0f, 65504.0f), ServerConfigCategory.GAME_SETTING),
	ASTEROID_RESOURCE_CHANCE(en -> Lng.str("Asteroids resource chance"), en -> Lng.str("Chance per block to place a new resource vein (1.0 = 100%)"), () -> new SettingStateFloat(0.006f, 0.0f, 1.0f), ServerConfigCategory.GAME_SETTING),
	PLAYER_MAX_BUILD_AREA(en -> Lng.str("Player max build area"), en -> Lng.str("max area a player may add/remove in adv. build mode"), () -> new SettingStateInt(10, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	NT_SPAM_PROTECT_TIME_MS(en -> Lng.str("NT Spam protection time"), en -> Lng.str("period of spam protection"), () -> new SettingStateInt(30000, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	ASTEROID_SECTOR_REPLENISH_TIME_SEC(en -> Lng.str("Asteroid sector replenish time in seconds"), en -> Lng.str("seconds until a sector that is mined down to 0 asteroids is replenished (-1 = never)"), () -> new SettingStateInt(-1, -1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	NT_SPAM_PROTECT_MAX_ATTEMPTS(en -> Lng.str("NT Spam protection max attempts"), en -> Lng.str("max attempts before refusing connections in spam protect period (default is 1/sec for 30 sec)"), () -> new SettingStateInt(30, 0, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	NT_SPAM_PROTECT_EXCEPTIONS(en -> Lng.str("NT Spam protection IP exceptions"), en -> Lng.str("ips excepted from spam control (separate multiple with comma) (default is localhost)"), () -> new SettingStateString("127.0.0.1"), ServerConfigCategory.NETWORK_SETTING),
	ANNOUNCE_SERVER_TO_SERVERLIST(en -> Lng.str("Server announces to server list"), en -> Lng.str("announces the server to the starmade server list so clients can find it. Hostname must be provided for HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST!"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST(en -> Lng.str("Server host name to announce to server list"), en -> Lng.str("this must be a valid hostname (either ip or host, e.g. play.star-made.org)"), () -> new SettingStateString(""), ServerConfigCategory.NETWORK_SETTING),
	SERVER_LIST_NAME(en -> Lng.str("Server name in server list"), en -> Lng.str("max length 64 characters"), () -> new SettingStateString(""), ServerConfigCategory.NETWORK_SETTING),
	SERVER_LIST_DESCRIPTION(en -> Lng.str("Server description in server list"), en -> Lng.str("max length 128 characters"), () -> new SettingStateString(""), ServerConfigCategory.NETWORK_SETTING),
	MISSILE_DEFENSE_FRIENDLY_FIRE(en -> Lng.str("Anti missile friendly fire"), en -> Lng.str("can shoot down own or missiles from own faction"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	USE_DYNAMIC_RECIPE_PRICES(en -> Lng.str("Dynamic crafting recipe price"), en -> Lng.str("use recipe based prices (the price is the price of the parts it is made out of in crafting)"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	DYNAMIC_RECIPE_PRICE_MODIFIER(en -> Lng.str("Dynamic crafting recipe modifier"), en -> Lng.str("modifier to adjust dynamic price"), () -> new SettingStateFloat(1.05f, -1.0f, 1000.0f), ServerConfigCategory.GAME_SETTING),
	MAKE_HOMBASE_ATTACKABLE_ON_FP_DEFICIT(en -> Lng.str("Make homebase destructable on FP deficit"), en -> Lng.str("Home bases become attackable if a faction's Faction Points are in the minus and the faction doesn't own any territory"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	PLANET_SPECIAL_REGION_PROBABILITY(en -> Lng.str("Planet special region probability"), en -> Lng.str("one out of thisValue chance of a special region spawning per planet plate (cities, pyramids, etc) (changing this value migth change some plates, but won't change any plates that are already modified by a player)"), () -> new SettingStateInt(240, 1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	NT_BLOCK_QUEUE_SIZE(en -> Lng.str("NT Block queue size"), en -> Lng.str("How many blocks are sent per update. Huge placements will shot faster, but it will consume more bandwidth and is subject to spamming players"), () -> new SettingStateInt(1024, 1, Integer.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	CHUNK_REQUEST_THREAD_POOL_SIZE_TOTAL(en -> Lng.str("Chunk request thread pool size total"), en -> Lng.str("Thead pool size for chunk requests (from disk and generated)"), () -> new SettingStateInt(10, 1, Integer.MAX_VALUE), ServerConfigCategory.PERFORMANCE_SETTING),
	CHUNK_REQUEST_THREAD_POOL_SIZE_CPU(en -> Lng.str("Chunk request thread pool size CPU"), en -> Lng.str("Available threads of total for CPU generation. WARNING: too high can cause cpu spikes. About the amount of available cores minus one is best"), () -> new SettingStateInt(2, 1, Integer.MAX_VALUE), ServerConfigCategory.PERFORMANCE_SETTING),
	BUY_BLUEPRINTS_WITH_CREDITS(en -> Lng.str("Buy blueprints with credits"), en -> Lng.str("buy blueprints directly with credits"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	SHOP_USE_STATIC_SELL_BUY_PRICES(en -> Lng.str("Static shop prices"), en -> Lng.str("shop buy and sell price change depending on stock (shop prices will always stay the same if true)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	SHOP_SELL_BUY_PRICES_UPPER_LIMIT(en -> Lng.str("Shop sell/buy price upper limit"), en -> Lng.str("maximum of base price a shop will want depending on its stock (e.g. max 120 credits if the normal cost is 100)"), () -> new SettingStateFloat(1.2f, 0.001f, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SHOP_SELL_BUY_PRICES_LOWER_LIMIT(en -> Lng.str("Shop sell/buy price lower limit"), en -> Lng.str("minimum of base price a shop will want depending on its stock (e.g. max 80 credits if the normal cost is 100)"), () -> new SettingStateFloat(1.2f, 0.001f, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MINING_BONUS(en -> Lng.str("Mining bonus multiplier"), en -> Lng.str("general multiplier on all mining"), () -> new SettingStateInt(1, 1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MAX_LOGIC_SIGNAL_QUEUE_PER_OBJECT(en -> Lng.str("Max logic signal queue per object"), en -> Lng.str("max logic trace queue allowed"), () -> new SettingStateInt(250000, 1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MAX_LOGIC_ACTIVATIONS_AT_ONCE_PER_OBJECT_WARN(en -> Lng.str("Max logic activations at once per object warning"), en -> Lng.str("warn about objects that activate more than x blocks at once"), () -> new SettingStateInt(10000, 1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MAX_LOGIC_ACTIVATIONS_AT_ONCE_PER_OBJECT_STOP(en -> Lng.str("Max logic activations at once per object stop"), en -> Lng.str("stop logic of objects that activate more than x blocks at once. They will enter a logic cooldown of 10 seconds to prevent servers from overloading"), () -> new SettingStateInt(50000, 1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MAX_COORDINATE_BOOKMARKS(en -> Lng.str("Max sector bookmarks per player"), en -> Lng.str("coordinate bookmarks per player allowed"), () -> new SettingStateInt(20, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	ALLOWED_STATIONS_PER_SECTOR(en -> Lng.str("Max stations allowed per sector"), en -> Lng.str("How many stations are allowed per sector"), () -> new SettingStateInt(1, 1, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	STATION_CREDIT_COST(en -> Lng.str("Station credit cost"), en -> Lng.str("how much does a station or station blueprint cost"), () -> new SettingStateInt(1000000, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SKIN_SERVER_UPLOAD_BLOCK_SIZE(en -> Lng.str("Skin server upload block size"), en -> Lng.str("how fast should skins be transferred from server to clients (too high might cause lag) [default 256 ~ 16kb/s]"), () -> new SettingStateInt(256, 1, Short.MAX_VALUE), ServerConfigCategory.NETWORK_SETTING),
	WEIGHTED_CENTER_OF_MASS(en -> Lng.str("Weighted Center of Mass"), en -> Lng.str("if on, the center of mass for each structured will be calculated based on block mass. On 'false', the center of mass is always the core position"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SECURE_UPLINK_ENABLED(en -> Lng.str("Secure uplink enabled"), en -> Lng.str("dedicated servers can be registered on the StarMade registry"), () -> new SettingStateBoolean(false), ServerConfigCategory.NETWORK_SETTING),
	SECURE_UPLINK_TOKEN(en -> Lng.str("Secure uplink token"), en -> Lng.str("uplink token, provided when registering a dedicated server"), () -> new SettingStateString(""), ServerConfigCategory.NETWORK_SETTING),
	USE_STRUCTURE_HP(en -> Lng.str("Structure HP enabled"), en -> Lng.str("ships and other structures use the hitpoint system. if off, a ship will overheat when the core gets taken out (old)"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SHOP_REBOOT_COST_PER_SECOND(en -> Lng.str("Ship reboot cost per second at shop"), en -> Lng.str("Cost to reboot a ship at a shop (per second it would take to reboot in space)"), () -> new SettingStateFloat(1000.0f, 0, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SHOP_ARMOR_REPAIR_COST_PER_HITPOINT(en -> Lng.str("Ship armor repair cost per HP at shop"), en -> Lng.str("Cost to repair a ship's armor at a shop"), () -> new SettingStateFloat(1.0f, 0, Float.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	MAX_SIMULTANEOUS_EXPLOSIONS(en -> Lng.str("Max simultaneous explosions"), en -> Lng.str("the more the faster explosions at the same time are executed (costs in total about 20MB RAM each and of course CPU because it's all threaded) (10 is default for a medium powered singleplayer)"), () -> new SettingStateInt(10, 0, Integer.MAX_VALUE), ServerConfigCategory.PERFORMANCE_SETTING),
	REMOVE_ENTITIES_WITH_INCONSISTENT_BLOCKS(en -> Lng.str("Remove entities with inconsistent blocks"), en -> Lng.str("This will remove ships that have blocks that are normally disallowed (e.g. space station blocks on ships)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	OVERRIDE_INVALID_BLUEPRINT_TYPE(en -> Lng.str("Override invalid blueprint types"), en -> Lng.str("If a loaded blueprint is invalid, it's type will be overridden"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	FACTION_FOUNDER_KICKABLE_AFTER_DAYS_INACTIVITY(en -> Lng.str("Days of inactivity after a faction founder becomes kickable"), en -> Lng.str("Days of inactivity after which a founder may kick another founder"), () -> new SettingStateInt(30, 0, Integer.MAX_VALUE - 1), ServerConfigCategory.NETWORK_SETTING),

	BLUEPRINT_SPAWNABLE_SHIPS(en -> Lng.str("Spawnable ship blueprints"), en -> Lng.str("enables or disables blueprint spawning from item"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	BLUEPRINT_SPAWNABLE_STATIONS(en -> Lng.str("Spawnable station blueprints"), en -> Lng.str("enables or disables blueprint spawning from item"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	USE_OLD_GENERATED_PIRATE_STATIONS(en -> Lng.str("Use old pirate stations"), en -> Lng.str("enables spawning of old style pirate stations"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	CARGO_BLEED_AT_OVER_CAPACITY(en -> Lng.str("Cargo drops when over capacity"), en -> Lng.str("cargo is ejected every minute if storage is at over capacity"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	ALLOW_PERSONAL_INVENTORY_OVER_CAPACITY(en -> Lng.str("Allow over capacity for personal inventory"), en -> Lng.str("Personal Inventory can go over capacity"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	ONLY_ALLOW_FACTION_SHIPS_ADDED_TO_FLEET(en -> Lng.str("Allow only factions ships to be added to fleet"), en -> Lng.str("only allows faction ships to be added to fleet"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	MAX_CHAIN_DOCKING(en -> Lng.str("Max docking chain length"), en -> Lng.str("maximal deepness of docking chains (may cause glitches depending on OS (path and filename length) at high numbers)"), () -> new SettingStateInt(25, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	SHOP_RAILS_ON_ADV(en -> Lng.str("Rails on advanced shops"), en -> Lng.str("Advanced shops will have 4 rails dockers that can be used like a neutral homebase (anything docked is safe)"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	SHOP_RAILS_ON_NORMAL(en -> Lng.str("Rails on normal shops"), en -> Lng.str("Normal shops will have 4 rails dockers that can be used like a neutral homebase (anything docked is safe)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	ALLOW_FLEET_FORMATION(en -> Lng.str("Allow fleet formations"), en -> Lng.str("Allows fleet formation"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	BACKUP_WORLD_ON_MIGRATION(en -> Lng.str("Backup world on migration"), en -> Lng.str("Back up world when migrating to a new file format"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	BACKUP_BLUEPRINTS_ON_MIGRATION(en -> Lng.str("Backup blueprints on migration"), en -> Lng.str("Back up blueprints when migrating to a new file format"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),

	SECTORS_TO_EXPLORE_FOR_SYS(en -> Lng.str("Sectors to explore for system information"), en -> Lng.str("How many sectors of a system have to be explored"), () -> new SettingStateInt(15, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	NPC_FACTION_SPAWN_LIMIT(en -> Lng.str("Max NPC Factions per galaxy"), en -> Lng.str("Maximum npc factions per galaxy (-1 for unlimited (will still be around 2-10))"), () -> new SettingStateInt(-1, -1, Integer.MAX_VALUE), ServerConfigCategory.NPC_SETTING),
	NPC_DEBUG_MODE(en -> Lng.str("NPC Debug mode"), en -> Lng.str("Sends complete NPC faction package to clients (very bandwith intensive)"), () -> new SettingStateBoolean(false), ServerConfigCategory.GAME_SETTING),
	FLEET_OUT_OF_SECTOR_MOVEMENT(en -> Lng.str("Unloaded fleet sector movement speed in miliseconds"), en -> Lng.str("How long for an unloaded fleet to cross a sector in ms"), () -> new SettingStateInt(6000, 0, Integer.MAX_VALUE), ServerConfigCategory.GAME_SETTING),
	NPC_LOADED_SHIP_MAX_SPEED_MULT(en -> Lng.str("NPC max ship speed multiplier"), en -> Lng.str("How fast NPC fleet ships are compared to their max speed when loaded"), () -> new SettingStateFloat(0.7f, 0.0f, 100000.0f), ServerConfigCategory.GAME_SETTING),
	USE_FOW(en -> Lng.str("Fog of War"), en -> Lng.str("Use 'fog of war'. Turning this off will make everything visible to everyone"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),
	ALLOW_PASTE_AABB_OVERLAPPING(en -> Lng.str("Allow Paste bounding box overlapping"), en -> Lng.str("Allow Paste bounding box overlapping"), () -> new SettingStateBoolean(true), ServerConfigCategory.GAME_SETTING),

	// DEBUG | DEPRECATED
	DEBUG_FSM_STATE("transfer debug FSM state. Turning this on may slow down network", () -> new SettingStateBoolean(false)),
	PHYSICS_SHAPE_CASTING_TUNNELING_PREVENTION("Makes a convex cast for hight speed object to prevent clipping. High Cost. (Bugged right now, so dont turn it on)", () -> new SettingStateBoolean(false)),
	FORCE_DISK_WRITE_COMPLETION("forces writing operations of raw data to disk directly after operation. For some OS this prevents raw data corruption", () -> new SettingStateBoolean(false)),

	DEBUG_SEGMENT_WRITING("Debugs correctness of writing of segments (costs server performance)", () -> new SettingStateBoolean(false)),

	TURNING_DIMENSION_SCALE("Scaling of tuning speed VS ship dimension (default = 1.1)", () -> new SettingStateFloat(1.1f, 0.0f, 100.0f)),

	RECIPE_BLOCK_COST("How much blocks have to be invested to create a recipe (min 0)", () -> new SettingStateInt(5000, 0, Integer.MAX_VALUE - 1)),
	RECIPE_REFUND_MULT("how much blocks are refunded from selling a recipe (must be between 0 and 1): 0 no refund, 1 full refund", () -> new SettingStateFloat(0.5f, 0.0f, 1.0f)),
	RECIPE_LEVEL_AMOUNT("On how much created blocks will a recipe level up (base value) (min 0)", () -> new SettingStateInt(4000, 0, Integer.MAX_VALUE - 1)),

	NT_SPAM_PROTECT_ACTIVE("enables connection spawn protection (flooding servers with login attempts)", () -> new SettingStateBoolean(true)),
	USE_PERSONAL_SECTORS("will spawn a player in a locked sector sandbox (warning, don't use unless you know what you do)", () -> new SettingStateBoolean(false)),
	BATTLE_MODE("turn on battlemode (warning, don't use unless you know what you're doing)", () -> new SettingStateBoolean(false)),
	BATTLE_MODE_CONFIG("General config for battlemode", () -> new SettingStateString("battleSector=0,0,0,Physics.smsec;battleSector=15,15,15,Physics.smsec;countdownRound=300;countdownStart=30;maxMass=-1;maxDim=300;maxMassPerFaction=-1;")),
	BATTLE_MODE_FACTIONS("Faction config for battlemode", () -> new SettingStateString("[TeamA, fighters, 500,500,500, 0.5,0.1,0.9];[TeamB, fighters, -500,-500,-500, 0.5,0.9,0.2];[TeamFFA,ffa, 0,0,-500, 0.2,0.9,0.9];[Spectators,spectators, 0,500,0,0.8,0.4,0.8]")),
	LEADERBOARD_BACKLOG("time in hours to keep leaderboard backlog (the more time, the more data has to be sent to client)", () -> new SettingStateInt(24, 0, Integer.MAX_VALUE)),

	DEBUG_BEAM_POWER_CONSUMPTION("server will send notifications on power consumed (not counting power given from supply) on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),
	DEBUG_BEAM_TICKS_DONE("server will send notifications on ticks done on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),
	DEBUG_BEAM_POWER_PER_TICK("server will send notifications on beam power per tick on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),
	DEBUG_MISSILE_POWER_CONSUMPTION("server will send notifications on missiles on server (costs performance, so only use for debugging)", () -> new SettingStateBoolean(false)),

	NPC_LOG_MODE("use 0 for npc file logs [/logs/npc/] and 1 for normal log output", () -> new SettingStateInt(0, 0, 2)),
	NPC_DEBUG_SHOP_OWNERS("Additional shop owners for npc faction shops (case insensitive, seperate with comma)", () -> new SettingStateString("")),
	SQL_PERMISSION("user name allowed to sql query remotely (direct console always allowed /sql_query, /sql_update, /sql_insert_return_generated_keys) (case insensitive, seperate with comma)", () -> new SettingStateString("")),
	DEBUG_EMPTY_CHUNK_WRITES("Logs empty chunks (debug only)", () -> new SettingStateBoolean(false)),
	ALLOWED_UNPACKED_FILE_UPLOAD_IN_MB("how much mb is an uploaded blueprint/skin allowed to have (unzipped)", () -> new SettingStateInt(1024, 0, Integer.MAX_VALUE)),
	RESTRICT_BUILDING_SIZE("Restricts Building Size to X times Sector Size (-1 for off) Warning: values set in GameConfig.xml overwrite this", () -> new SettingStateFloat(2.0f, 0.0f, 32.0f)),

	DISPLAY_GROUPING_DEBUG_INFORMATION("Displays grouping calculation information", () -> new SettingStateBoolean(false)),
	MANAGER_CALC_CANCEL_ON("Enables performance increase by prematurely ending calculations when there is a refresh request", () -> new SettingStateBoolean(true)),
	JUMP_DRIVE_ENABLED_BY_DEFAULT("Weather all ships have jump capability or the basic jump chamber is required for jump capability", () -> new SettingStateBoolean(true)),
	SHORT_RANGE_SCAN_DRIVE_ENABLED_BY_DEFAULT("Weather all ships have scanner capability or the basic scan chamber is required for scan capability", () -> new SettingStateBoolean(true)),
	SPAWN_PROTECTION("Spawn protection in seconds (may not yet protect against everything)", () -> new SettingStateInt(10, 0, Integer.MAX_VALUE)),

	AI_ENGAGEMENT_RANGE_OF_MIN_WEAPON_RANGE("Percentage of minimum weapon range the AI prefers to engage from", () -> new SettingStateFloat(0.75f, 0.0f, 1.0f)),
	MISSILE_TARGET_PREDICTION_SEC("Since lockstep algorithm is based on recorded target positions, how much should a target chasing missiles predict a target's position based on its velocity (in ticks of 8ms). Change if missiles miss fast targets", () -> new SettingStateFloat(0.5f, 0.0f, 1000000.0f)),
	AI_WEAPON_SWITCH_DELAY("Delay inbetween an AI can switch weapon in ms", () -> new SettingStateInt(500, 0, Integer.MAX_VALUE)),
	ALLOW_FACTORY_ON_SHIPS("Factories work on ships", () -> new SettingStateBoolean(false)),
	SHIPYARD_IGNORE_STRUCTURE("Removes necessity to build shipyard structure (just computer and ancor is enough)", () -> new SettingStateBoolean(false)),
	IGNORE_DOCKING_AREA("Ignore size of structure when doing (might lead to overlapping)", () -> new SettingStateBoolean(false)),
	MISSILE_RADIUS_HP_BASE("Missile Damage to Radius relation: MissileRadius = ((3/4π) * (MissileTotalDamage/MISSILE_RADIUS_HP_BASE)) ^ (1/3)", () -> new SettingStateFloat(1.0f, 0.000001f, 9999999999.0f)),
	TEST_PLANET_TYPE("Select planet (0 to 6) ", () -> new SettingStateInt(1, 0, 6)),
	MAX_EXPLOSION_POOL("Maximum amount of explosions to keep in pool (to reuse in memory, -1 for unlimited)", () -> new SettingStateInt(-1, -1, 6)),
	;
	static boolean dirty = true;
	static List<ServerConfig> sortedSettings = new ObjectArrayList<>();
	/**
	 * The description.
	 */
	private final Translatable displayName;
	private final Translatable description;
	private final ServerConfigCategory category;
	private final SettingState s;

	//keeping description parameter so we still know what it does
	ServerConfig(String description, SettingState.SettingStateValueFac fac) {
		this(Translatable.DEFAULT, Translatable.DEFAULT, fac, ServerConfigCategory.DEBUG_SETTING);
	}

	ServerConfig(Translatable displayName, Translatable description, SettingState.SettingStateValueFac s, ServerConfigCategory category) {
		this.displayName = displayName;
		this.description = description;
		this.category = category;
		this.s = s.inst();
	}

	public String getDescription() {
		return description.getName(this);
	}

	public String getName() {
		return displayName.getName(this);
	}

	public static String autoCompleteString(String s) {
		s = s.trim();
		ArrayList<ServerConfig> list = list(s);
		boolean first = true;
		String a = "";
		for(ServerConfig e : list) {
			if(s.length() > a.length()) {
				a = StringTools.LongestCommonSubsequence(s, e.name().toLowerCase(Locale.ENGLISH));
				if(a.equals(s) && first) {
					s = e.name().toLowerCase(Locale.ENGLISH);
					first = false;
				} else {
					s = a;
				}
			}
		}

		return s;
	}

	public static void deserialize(NetworkGameState gameState) {
		for(int i = 0; i < gameState.serverConfig.getReceiveBuffer().size(); i++) {
			RemoteStringArray a = gameState.serverConfig.getReceiveBuffer().get(i);
			valueOf(a.get(0).get()).s.setFromString(a.get(1).get());
		}
	}

	public static ArrayList<ServerConfig> list(String autoComplete) {
		autoComplete = autoComplete.trim();
		if(sortedSettings.isEmpty()) {
			ServerConfig[] values = values();
			Collections.addAll(sortedSettings, values);
			Collections.sort(sortedSettings, new ServerConfigComperator());
		}
		ArrayList<ServerConfig> l = new ArrayList<ServerConfig>();

		for(int i = 0; i < sortedSettings.size(); i++) {
			if(sortedSettings.get(i).name().toLowerCase(Locale.ENGLISH).startsWith(autoComplete)) {
				l.add(sortedSettings.get(i));
			}
		}
		return l;
	}

	public static void print() {
		ServerConfig[] values = values();
		String spaces = "                                                             ";
		System.err.println("################### SERVER SETTINGS ##########################");
		for(int i = 0; i < values.length; i++) {
			int c = 50 - values[i].name().length();

			System.err.println(values[i].name() + spaces.substring(0, c) + values[i].s);
			if(i < values.length - 1) {
				System.err.println("----------------------------------------------------------------");
			}
		}
		System.err.println("################### /SERVER SETTINGS #########################");
	}

	public static void read() {

		System.err.println("################## READING SERVER SETTINGS");
		String curLine = "";
		try {
			ObjectArrayList<String> names = new ObjectArrayList<>(values().length);
			ObjectArrayList<String> values = new ObjectArrayList<>(values().length);
			File f = new FileExt("./server.cfg");

			if(!f.exists()) {
				File def = new File("./data/config/defaultSettings/server.cfg");
				FileUtils.copyFile(def, f);
				f = new FileExt("./server.cfg");
			}
			BufferedReader bf = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
			String line;
			int i = 0;
			while((line = bf.readLine()) != null) {
				curLine = line;
				if(line.trim().startsWith("//")) {
					continue;
				}
				if(line.contains("//")) {
					line = line.substring(0, line.indexOf("//"));
				}
				String[] split = line.split("=", 2);
				names.add(split[0].trim());
				values.add(split[1].trim());
				i++;
			}
			for(i = 0; i < names.size(); i++) {
				try {
					DebugUtil.logDebug("[SERVER][CONFIG] " + names.get(i) + " = " + values.get(i));
					valueOf(names.get(i)).s.setFromString(values.get(i));
				} catch(Exception e) {
					System.err.println("[SERVER][CONFIG] No value for " + names.get(i) + ". Creating default entry. " + values.get(i));
				}
			}

			bf.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("Could not read settings file: using defaults: " + curLine);
		}
	}

	public static void serialize(NetworkGameState gameState) {
		if(dirty) {
			for(ServerConfig s : values()) {
				assert (gameState != null);
				RemoteStringArray sArray = new RemoteStringArray(2, gameState);
				sArray.set(0, s.name());
				sArray.set(1, s.s.getAsString());
				gameState.serverConfig.add(sArray);
			}
			dirty = false;
			//dirty isonly used on server
		}
	}

	public static void writeDefault() {
		try {
			String path = "." + File.separator + "data" + File.separator + "config" + File.separator + "defaultSettings" + File.separator + "server.cfg";
			write(path);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void write() throws IOException {
		System.err.println("################## WRITING SERVER SETTINGS");
		write("./server.cfg");
	}

	public static void write(String path) throws IOException {
		File f = new FileExt(path);
		f.delete();
		f.createNewFile();
		BufferedWriter bf = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8));
		for(ServerConfig s : values()) {
			DebugUtil.logDebug("[SERVER][CONFIG] " + s.name() + " = " + s.s.getAsString());
			bf.write(s.name() + " = " + s.s.getAsString() + " //" + s.getDescription());
			bf.newLine();
		}
		bf.flush();
		bf.close();
	}

	public static String[] list() {
		ServerConfig[] values = values();
		String[] list = new String[values.length];
		for(int i = 0; i < values.length; i++) {
			list[i] = values[i].toString();
		}
		return list;
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH) + " (" + s + ") ";
	}

	public ServerConfigCategory getCategory() {
		return category;
	}

	@Override
	public void addChangeListener(EngineSettingsChangeListener c) {

	}

	@Override
	public void removeChangeListener(EngineSettingsChangeListener c) {

	}

	@Override
	public int getInt() {
		return s.getInt();
	}

	@Override
	public float getFloat() {
		return s.getFloat();
	}

	@Override
	public void setOn(boolean on) {
		s.setOn(on);
	}

	@Override
	public void setInt(int v) {
		s.setInt(v);
	}

	@Override
	public void setFloat(float v) {
		s.setFloat(v);
	}

	@Override
	public String getString() {
		return s.getString();
	}

	@Override
	public void setString(String v) {
		s.setString(v);
	}

	public Object getObject() {
		return s.getObject();
	}

	public void setObject(Object o) {
		s.setObject(o);
	}

	public static void setShadowOn(boolean b) {
		EngineSettings.G_SHADOW_QUALITY.setObject(ShadowQuality.OFF);
	}

	public void setValueByObject(Object value) {
		s.setValueByObject(value);
	}

	public void switchOn() {
		setOn(!isOn());
	}

	@Override
	public boolean isOn() {
		return s.isOn();
	}

	public enum ServerConfigCategory {
		DATABASE_SETTING,
		GAME_SETTING,
		NPC_SETTING,
		NETWORK_SETTING,
		PERFORMANCE_SETTING,
		DEBUG_SETTING
	}

	public String getAsString() {
		return s.getAsString();
	}

	public GUIElement getGUIElementTextBar(InputState state, GUIElement dependent, String deactText) {
		return s.getGUIElementTextBar(state, dependent, deactText);
	}

	public GUIElement getGUIElement(InputState state, GUIElement dependent) {
		return s.getGUIElement(state, dependent);
	}

	public GUIElement getGUIElement(InputState state, GUIElement dependent, String deactText) {
		return s.getGUIElement(state, dependent, deactText);
	}

	@Override
	public void next() {
		s.next();
	}

	@Override
	public void previous() {
		s.previous();
	}

	@Override
	public SettingsInterface getSettingsForGUI() {
		return this;
	}
}
