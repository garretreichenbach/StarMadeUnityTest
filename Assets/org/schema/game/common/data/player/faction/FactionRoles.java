package org.schema.game.common.data.player.faction;

import java.util.Arrays;

import org.schema.game.common.data.player.faction.FactionPermission.PermType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class FactionRoles implements TagSerializable {
	public static final int ROLE_COUNT = 5;

	public static final byte INDEX_DEFAULT_ROLE = 0;
	public static final byte INDEX_ADMIN_ROLE = 4;

	public static final int PERSONAL_RANK = -1;
	public static final int NOT_SET_RANK = -2;

	public static final byte LOWEST_RANK = 0;
	
//	private final long[] roles = new long[]{0,0,0,0,FactionPermission.ADMIN_PERMISSIONS};
//	private final String[] roleNames = new String[]{"Member 4th Rank","Member 3rd Rank","Member 2rd Rank","Member 1st Rank","Founder"};
	public int factionId;
	public int senderId = -1;
	// #RM1876 fixed typos in default faction role names
	private FactionRole[] roles = new FactionRole[]{
			new FactionRole(FactionPermission.DEFAULT_PERMISSION, Lng.str("Member 4th Rank"), 0),
			new FactionRole(FactionPermission.DEFAULT_PERMISSION, Lng.str("Member 3rd Rank"), 1),
			new FactionRole(FactionPermission.DEFAULT_PERMISSION, Lng.str("Member 2nd Rank"), 2),
			new FactionRole(FactionPermission.DEFAULT_PERMISSION, Lng.str("Member 1st Rank"), 3),
			new FactionRole(FactionPermission.ADMIN_PERMISSIONS, Lng.str("Founder"), 4)
	};
	public static String getRoleName(byte b) {
		return switch(b) {
			case NOT_SET_RANK -> Lng.str("UNSET");
			case PERSONAL_RANK -> Lng.str("PERSONAL");
			case 4 -> Lng.str("FOUNDER");
			default -> Lng.str("RANK_%s", (ROLE_COUNT - 1) - b);
		};
	}
	public void apply(FactionRoles a) {
		for (int i = 0; i < ROLE_COUNT; i++) {
			assert (factionId == a.factionId);
			roles[i].name = a.roles[i].name;
			roles[i].role = a.roles[i].role;
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		if (tag.getName().equals("0")) {
			Tag[] subs = (Tag[]) tag.getValue();
			factionId = (Integer) subs[0].getValue();
			Tag[] rolesTag = (Tag[]) subs[1].getValue();
			Tag[] namesTag = (Tag[]) subs[2].getValue();
			for (int i = 0; i < ROLE_COUNT; i++) {
				getRoles()[i].role = (Long) rolesTag[i].getValue();
				getRoles()[i].name = (String) namesTag[i].getValue();

			}
			//load as non friendly fire. can be removed after a while
			getRoles()[4].role = getRoles()[4].role & ~FactionPermission.PermType.KICK_ON_FRIENDLY_FIRE.value;
		} else {
			assert (false);
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag rolesTag = new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.LONG, null, getRoles()[0].role),
				new Tag(Type.LONG, null, getRoles()[1].role),
				new Tag(Type.LONG, null, getRoles()[2].role),
				new Tag(Type.LONG, null, getRoles()[3].role),
				new Tag(Type.LONG, null, getRoles()[4].role),
				FinishTag.INST,

		});
		Tag namesTag = new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.STRING, null, getRoles()[0].name),
				new Tag(Type.STRING, null, getRoles()[1].name),
				new Tag(Type.STRING, null, getRoles()[2].name),
				new Tag(Type.STRING, null, getRoles()[3].name),
				new Tag(Type.STRING, null, getRoles()[4].name),
				FinishTag.INST,

		});

		return new Tag(Type.STRUCT, "0", new Tag[]{new Tag(Type.INT, null, factionId), rolesTag, namesTag, FinishTag.INST});
	}

	/**
	 * @return the roles
	 */
	public FactionRole[] getRoles() {
		return roles;
	}


	public boolean hasRelationshipPermission(int index) {
		return (roles[index].role & FactionPermission.PermType.RELATIONSHIPS_EDIT.value) == FactionPermission.PermType.RELATIONSHIPS_EDIT.value;
	}

	public boolean hasInvitePermission(int index) {
		return (roles[index].role & FactionPermission.PermType.INVITE_PERMISSION.value) == FactionPermission.PermType.INVITE_PERMISSION.value;
	}

	public boolean hasClaimSystemPermission(int index) {
		return (roles[index].role & FactionPermission.PermType.MAY_CLAIM_SYSTEM.value) == FactionPermission.PermType.MAY_CLAIM_SYSTEM.value;
	}

	public boolean hasKickPermission(int index) {
		return (roles[index].role & FactionPermission.PermType.KICK_PERMISSION.value) == FactionPermission.PermType.KICK_PERMISSION.value;
	}

	public boolean hasDescriptionAndNewsPostPermission(int index) {
		return (roles[index].role & FactionPermission.PermType.NEWS_POST_PERMISSION.value) == FactionPermission.PermType.NEWS_POST_PERMISSION.value;
	}

	public boolean hasHomebasePermission(int index) {
		return (roles[index].role & FactionPermission.PermType.HOMEBASE_PERMISSION.value) == FactionPermission.PermType.HOMEBASE_PERMISSION.value;
	}

	public boolean hasKickOnFriendlyFire(int index) {
		return (roles[index].role & FactionPermission.PermType.KICK_ON_FRIENDLY_FIRE.value) == FactionPermission.PermType.KICK_ON_FRIENDLY_FIRE.value;
	}

	public boolean hasPermissionEditPermission(int index) {
		return (roles[index].role & FactionPermission.PermType.FACTION_EDIT_PERMISSION.value) == FactionPermission.PermType.FACTION_EDIT_PERMISSION.value;
	}
	public boolean hasFogOfWarPermission(int index) {
		return (roles[index].role & FactionPermission.PermType.FOG_OF_WAR_SHARE.value) == FactionPermission.PermType.FOG_OF_WAR_SHARE.value;
	}

	public boolean hasPermission(int roleIndex, PermType permType) {
		return (roles[roleIndex].role & permType.value) == permType.value;
	}

	public void setPermission(int roleIndex, PermType permType, boolean active) {
		if (active) {
			roles[roleIndex].role = (roles[roleIndex].role | permType.value);
		} else {
			roles[roleIndex].role = (roles[roleIndex].role & ~permType.value);
		}
	}
	@Override
	public String toString() {
		return "FactionRoles(Faction "+factionId+")(sendId "+senderId+")["+Arrays.toString(roles)+"]";
	}
	

}
