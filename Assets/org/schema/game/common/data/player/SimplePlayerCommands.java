package org.schema.game.common.data.player;

import java.util.Arrays;

public enum SimplePlayerCommands {
	SEARCH_LAST_ENTERED_SHIP,
	PUT_ON_HELMET,
	HIRE_CREW,
	REPAIR_STATION(Integer.class, String.class), //id and name
	SIT_DOWN(Integer.class, Long.class, Long.class, Long.class), //id, block and block
	CLIMB(Integer.class, Long.class, Integer.class),
	SCAN(Integer.class), //id, block and block
	WARP_TO_TUTORIAL_SECTOR,
	DESTROY_TUTORIAL_ENTITY(String.class),
	BACKUP_INVENTORY(Boolean.class),
	RESTORE_INVENTORY,
	END_TUTORIAL,
	END_SHIPYARD_TEST,
	ADD_BLUEPRINT_META_SINGLE(Integer.class, Short.class, Integer.class), //metaid, type, count
	ADD_BLUEPRINT_META_ALL(Integer.class),  //metaid
	SPAWN_BLUEPRINT_META(Integer.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Boolean.class, Integer.class, Long.class),   //metaid, name, invHolderId, invX, invY, invZ 
	REQUEST_BLUEPRINT_ITEM_LIST(String.class),
	SET_FACTION_RANK_ON_OBJ(Integer.class, Byte.class),  //objID, rank
	FAILED_TO_JOIN_CHAT_INVALLID_PASSWD(String.class),
	REBOOT_STRUCTURE(Integer.class, Boolean.class),
	REPAIR_ARMOR(Integer.class, Boolean.class),
	REBOOT_STRUCTURE_REQUEST_TIME(Integer.class),
	SPAWN_SHOPKEEP(Integer.class),
	SEND_ALL_DESTINATIONS_OF_ENTITY(Integer.class),
	SET_SPAWN(Integer.class, Long.class), 
	GET_BLOCK_STORAGE_META_SINGLE(Integer.class, Short.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class), 
	ADD_BLOCK_STORAGE_META_SINGLE(Integer.class, Short.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class), 
	GET_BLOCK_STORAGE_META_ALL(Integer.class, Integer.class, Integer.class, Integer.class, Integer.class),
	ADD_BLOCK_STORAGE_META_ALL(Integer.class, Integer.class, Integer.class, Integer.class, Integer.class), 
	CLIENT_TO_SERVER_LOG(String.class), 
	VERIFY_FACTION_ID(Integer.class),
	SET_FREE_WARP_TARGET(Integer.class, Long.class, Integer.class, Integer.class, Integer.class),
	;

	public final Class<?>[] args;

	private SimplePlayerCommands(Class<?>... args) {
		this.args = args;
	}

	public void checkMatches(Object[] to) {
		if (args.length != to.length) {
			throw new IllegalArgumentException("Invalid argument count: Provided: " + Arrays.toString(to) + ", but needs: " + Arrays.toString(args));
		}
		for (int i = 0; i < args.length; i++) {
			if (!to[i].getClass().equals(args[i])) {
				System.err.println("Not Equal: " + to[i] + " and " + args[i]);
				throw new IllegalArgumentException("Invalid argument on index " + i + ": Provided: " + Arrays.toString(to) + "; cannot take " + to[i] + ":" + to[i].getClass() + ", it has to be type: " + args[i].getClass());
			}
		}
	}
}
