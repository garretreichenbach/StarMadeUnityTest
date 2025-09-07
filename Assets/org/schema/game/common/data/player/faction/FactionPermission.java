package org.schema.game.common.data.player.faction;

import java.util.Locale;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.config.FactionActivityConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class FactionPermission implements TagSerializable {

//	public static final long PM_PERMISSION_EDIT = 1;
//	public static final long PM_KICK_PERMISSION = 2;
//	public static final long PM_INVITE_PERMISSION = 4;
//	public static final long PM_FACTION_EDIT_PERMISSION = 8;
//	public static final long PM_KICK_ON_FRIENDLY_FIRE = 16;
//	public static final long PM_UNDEFINED_PERMISSION_1 = 32;
//	public static final long PM_UNDEFINED_PERMISSION_2 = 64;
//	public static final long PM_UNDEFINED_PERMISSION_3 = 128;
//	public static final long PM_UNDEFINED_PERMISSION_4 = 256;
//	public static final long PM_UNDEFINED_PERMISSION_5 = 512;
//	public static final long PM_UNDEFINED_PERMISSION_6 = 1024;

	public static final long ADMIN_PERMISSIONS =
			PermType.RELATIONSHIPS_EDIT.value |
					PermType.KICK_PERMISSION.value |
					PermType.INVITE_PERMISSION.value |
					PermType.FACTION_EDIT_PERMISSION.value |
					PermType.MAY_CLAIM_SYSTEM.value |
					PermType.HOMEBASE_PERMISSION.value |
					PermType.NEWS_POST_PERMISSION.value |
					PermType.FOG_OF_WAR_SHARE.value |
					PermType.UNDEF5.value |
					PermType.UNDEF6.value;
	public static final long DEFAULT_PERMISSION = 0;
	public String playerUID;
	public byte role;
	public long activeMemberTime;
	public long lastSeenTime;
	public Vector3i lastSeenPosition = new Vector3i(0, 0, 0);
	public FactionPermission() {
	}

	public FactionPermission(FactionPermission p) {
		this(p.playerUID, p.role, p.activeMemberTime);
	}

	/**
	 * used for new factions
	 *
	 * @param playerState
	 * @param role
	 * @param memberTime
	 */
	public FactionPermission(PlayerState playerState, byte role, long memberTime) {
		playerUID = playerState.getName();
		this.role = role;
		this.activeMemberTime = memberTime;

	}

	/**
	 * default
	 *
	 * @param playerState
	 * @param role
	 * @param memberTime
	 */
	public FactionPermission(String playerState, byte role, long memberTime) {
		playerUID = playerState;
		this.role = role;
		this.activeMemberTime = memberTime;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] d = (Tag[]) tag.getValue();

		playerUID = (String) d[0].getValue();
		role = (Byte) d[1].getValue();
		if (d[2].getType() != Type.FINISH) {
			activeMemberTime = (Long) d[2].getValue();
		}
		if (d.length > 3 && d[3].getType() != Type.FINISH) {
			lastSeenPosition.set((Vector3i) d[3].getValue());
		}
		if (d.length > 4 && d[4].getType() != Type.FINISH) {
			lastSeenTime = (Long) d[4].getValue();
		}
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.STRING, null, playerUID), new Tag(Type.BYTE, null, role), new Tag(Type.LONG, null, activeMemberTime), new Tag(Type.VECTOR3i, null, lastSeenPosition), new Tag(Type.LONG, null, lastSeenTime), FinishTag.INST});
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return playerUID.toLowerCase(Locale.ENGLISH).hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return playerUID.toLowerCase(Locale.ENGLISH).equals(((FactionPermission) obj).playerUID.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public String toString() {
		return "FactionPermission [playerUID=" + playerUID + ", roleID=" + role + "]";
	}

	public boolean hasRelationshipPermission(Faction faction) {
		return faction.getRoles().hasRelationshipPermission(role);
	}

	public boolean hasInvitePermission(Faction faction) {
		return faction.getRoles().hasInvitePermission(role);
	}

	public boolean hasClaimSystemPermission(Faction faction) {
		return faction.getRoles().hasClaimSystemPermission(role);
	}

	public boolean hasHomebasePermission(Faction faction) {
		return faction.getRoles().hasHomebasePermission(role);
	}

	public boolean hasKickPermission(Faction faction) {
		return faction.getRoles().hasKickPermission(role);
	}

	public boolean hasDescriptionAndNewsPostPermission(Faction faction) {
		return faction.getRoles().hasDescriptionAndNewsPostPermission(role);
	}

	public boolean hasPermissionEditPermission(Faction faction) {
		return faction.getRoles().hasPermissionEditPermission(role);
	}
	public boolean hasFogOfWarPermission(Faction faction) {
		return faction.getRoles().hasFogOfWarPermission(role);
	}

	public void setRole(byte p,
	                    boolean add) {
		this.role = p;
	}

	public boolean isActiveMember() {
		return System.currentTimeMillis() - activeMemberTime < FactionActivityConfig.SET_INACTIVE_AFTER_HOURS * 60 * 60 * 1000;
	}

	public String toString(Faction faction) {
		return "FactionPermission for " + faction + " [playerUID=" + playerUID + ", roleID=" + role + " claim " + hasClaimSystemPermission(faction) + ", kick " + hasKickPermission(faction) + ", descNews " + hasDescriptionAndNewsPostPermission(faction) + ", invite " + hasInvitePermission(faction) + ", editPerms " + hasPermissionEditPermission(faction) + ", relationship " + hasRelationshipPermission(faction) + ", homebase " + hasHomebasePermission(faction) + "]";
	}

	public boolean isFounder(Faction faction) {
		return role >= faction.getRoles().getRoles().length - 1;
	}

	public boolean isOverInactiveLimit(GameStateInterface state) {
		return (System.currentTimeMillis() - lastSeenTime) > state.getGameState().getFactionKickInactiveTimeLimitMs();
	}
	
	public enum PermType {
		RELATIONSHIPS_EDIT(en -> {
			return Lng.str("Relationships Permission");
		}, 1, true),
		KICK_PERMISSION(en -> {
			return Lng.str("Kick Permission");
		}, 2, true),
		INVITE_PERMISSION(en -> {
			return Lng.str("Invite Permission");
		}, 4, true),
		FACTION_EDIT_PERMISSION(en -> {
			return Lng.str("'Permission Edit' Permission");
		}, 8, true),
		KICK_ON_FRIENDLY_FIRE(en -> {
			return Lng.str("Kick On Friendly Fire");
		}, 16, true),
		MAY_CLAIM_SYSTEM(en -> {
			return Lng.str("Claim System Permission");
		}, 32, true),
		HOMEBASE_PERMISSION(en -> {
			return Lng.str("Homebase set/clear");
		}, 64, true),
		NEWS_POST_PERMISSION(en -> {
			return Lng.str("Description and News Post");
		}, 128, true),
		FOG_OF_WAR_SHARE(en -> {
			return Lng.str("Share Fog of War");
		}, 256, true),
		UNDEF5(Translatable.DEFAULT, 512, false),
		UNDEF6(Translatable.DEFAULT, 1024, false),;

		private final Translatable name;
		public final long value;
		public final boolean active;

		private PermType(Translatable name, long value, boolean active) {
			this.name = name;
			this.value = value;
			this.active = active;
		}
		
		public String getName(){
			return name.getName(this);
		}
	}
}
