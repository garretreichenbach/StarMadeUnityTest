package org.schema.game.common.data.player.faction;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.config.ConfigParserException;
import org.schema.common.util.ColorTools;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.FogOfWarReceiver;
import org.schema.game.common.controller.rules.rules.FactionRuleEntityManager;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.player.FogOfWarController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.faction.config.FactionPointGalaxyConfig;
import org.schema.game.common.data.player.faction.config.FactionPointIncomeConfig;
import org.schema.game.common.data.player.faction.config.FactionPointSpendingConfig;
import org.schema.game.common.data.player.faction.config.FactionPointsGeneralConfig;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.network.objects.NTRuleInterface;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.objects.remote.RemoteVector3i;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.UniqueInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.ListSpawnObjectCallback;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class Faction implements TagSerializable, FogOfWarReceiver, UniqueInterface, RuleEntityContainer {
	public static final int MODE_FIGHTERS_TEAM = 1;
	public static final int MODE_FIGHTERS_FFA = 2;
	public static final int MODE_SPECTATORS = 4;
	//	public static final int REL_NEUTRAL = 0;
	//	public static final int REL_ENEMY = 1;
	//	public static final int REL_FRIEND = 2;
	public static final String NT_CODE_MEMBER_NAME_CHANGE = "MCN";
	public static final String NT_CODE_OPEN_TO_JOIN = "OTJ";
	public static final String NT_CODE_DESCRIPTION = "DES";
	public static final String NT_CODE_NAME = "NAM";
	public static final String NT_CODE_ATTACK_NEUTRAL = "ATN";
	public static final String NT_CODE_AUTO_DECLARE_WAR = "ADW";
	private static int colorGen = 16;
	public final Int2LongOpenHashMap sentWarDeclaration = new Int2LongOpenHashMap();
	private final Map<String, FactionPermission> membersUID = new Object2ObjectAVLTreeMap<String, FactionPermission>(String.CASE_INSENSITIVE_ORDER);//new Object2ObjectOpenHashMap<String, FactionPermission>();
//	private final Long2ObjectOpenHashMap<FactionPermission> membersDBID = new Long2ObjectOpenHashMap<FactionPermission>();//new Object2ObjectOpenHashMap<String, FactionPermission>();
	private final FactionRoles roles;
	private final Vector3i homeSector = new Vector3i();
	private final ObjectOpenHashSet<String> personalEnemies = new ObjectOpenHashSet<String>();
	public float factionPoints;
	public float lastPointsFromOnline;
	public float lastPointsFromOffline;
	public int lastinactivePlayer;
	public float lastPointsSpendOnCenterDistance;
	public float lastPointsSpendOnDistanceToHome;
	public float lastPointsSpendOnBaseRate;
	public float lastGalaxyRadius;
	public FactionAddCallback addHook;
	public int lastCountDeaths;
	public float lastLostPointAtDeaths;
	public int serverDeaths;
	public List<Vector3i> lastSystemSectors = new ObjectArrayList<Vector3i>();
	public final Set<Vector3i> differenceSystemSectorsAdd = new ObjectOpenHashSet<Vector3i>();
	public final Set<Vector3i> differenceSystemSectorsRemove = new ObjectOpenHashSet<Vector3i>();
	public int clientLastTurnSytemsCount = -1;
	private String name;
	private String description;
	private int factionId;
	private long dateCreated;
	private String password = "";
	private boolean attackNeutral;
	private boolean allyNeutral;
	private boolean openToJoin;
	private boolean showInHub = true;
	private boolean autoDeclareWar;
	private String homebaseUID = "";
	private int factionMode;
	private Vector3f color = new Vector3f(0, 0, 0);
	private String homebaseRealName = "";
	private float serverLostDeathPoints;
	private List<String> lastSystemUIDs = new ObjectArrayList<String>();
	private List<Vector3i> lastSystems = new ObjectArrayList<Vector3i>();
	private FogOfWarController fow;
	private StateInterface state;
	private final ArrayList<Long> leasedFleets = new ArrayList<Long>();

	
	public final LongOpenHashSet attackedBy = new LongOpenHashSet();
	
	public Faction(StateInterface state) {
		super();
		roles = new FactionRoles();
		fow = new FogOfWarController(this);
		ruleEntityManager = new FactionRuleEntityManager(this);
		this.state = state;
	}
	public void checkActions() {
		for(long l : attackedBy){
			((FactionState) state).getFactionManager().diplomacyAction(DiplActionType.ATTACK, this.factionId, l);
			List<Faction> enemies = getEnemies();
			for(Faction f : enemies){
				((FactionState) state).getFactionManager().diplomacyAction(DiplActionType.ATTACK_ENEMY, f.factionId, l);
			}
		}
		attackedBy.clear();
	}
	
	private List<Faction> enemiesCache = new ObjectArrayList<Faction>();
	public List<Faction> getEnemies() {
		enemiesCache.clear();
		for(Faction f : ((FactionState) state).getFactionManager().getFactionMap().values()){
			if(((FactionState) state).getFactionManager().isEnemy(this.factionId, f.factionId)){
				enemiesCache.add(f);
			}
		}
		return enemiesCache;
	}
	private List<Faction> friendsCache = new ObjectArrayList<Faction>();
	private FactionRuleEntityManager ruleEntityManager;
	public List<Faction> getFriends() {
		friendsCache.clear();
		for(Faction f : ((FactionState) state).getFactionManager().getFactionMap().values()){
			if(((FactionState) state).getFactionManager().isFriend(this.factionId, f.factionId)){
				friendsCache.add(f);
			}
		}
		return friendsCache;
	}
	public Faction(StateInterface state, int factionId, String name, String description) {
		this(state);
		this.factionId = factionId;
		roles.factionId = factionId;
		this.name = name;
		this.description = description;
		dateCreated = System.currentTimeMillis();
		factionPoints = FactionPointsGeneralConfig.INITIAL_FACTION_POINTS;
		color = getNewColor();
	}

	public static synchronized Vector3f getNewColor() {
		Color c = ColorTools.getColor(colorGen++);

		Vector3f col = new Vector3f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);

		return col;
	}

	@Override
	public int hashCode() {
		return factionId;
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		return o instanceof Faction && factionId == ((Faction) o).factionId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Faction [id=" + factionId + ", name=" + name + ", description="
				+ description + ", size: " + membersUID.size() + "; FP: " + (int) factionPoints + "]";
	}

	//	/**
	//	 * @return the enemies
	//	 */
	//	public ArrayList<Integer> getEnemies() {
	//		return enemies;
	//	}
	//
	//	/**
	//	 * @return the friends
	//	 */
	//	public ArrayList<Integer> getFriends() {
	//		return friends;
	//	}

	public void addOrModifyMember(String initiator, String name, byte role, long activeTime, SendableGameState gameState, boolean send) {
		synchronized (membersUID) {
			FactionPermission factionPermission = membersUID.get(name);
			if (factionPermission == null) {
				membersUID.put(name, new FactionPermission(name, role, activeTime));
				if (gameState.isOnServer()) {
					System.err.println("[FACTION] Added to members " + name + " perm(" + role + ") of " + this + " on " + gameState.getState());
				}
			} else {
				boolean beforeShareFow = factionPermission.hasFogOfWarPermission(this);
				factionPermission.role = role;
				boolean afterShareFow = factionPermission.hasFogOfWarPermission(this);
				//dont set active time on server. but on clients
				if (!gameState.isOnServer()) {
					factionPermission.activeMemberTime = activeTime;
				}else{
					if(beforeShareFow && !afterShareFow){
						fogOfWarUpdateServer(factionPermission);
					}
				}

			}
		}
		if (send && gameState.isOnServer()) {
			RemoteStringArray rs = new RemoteStringArray(5, gameState.getNetworkObject());
			rs.set(0, name);
			rs.set(1, String.valueOf(factionId));
			rs.set(2, String.valueOf(role));
			rs.set(3, initiator);
			//send the current active time and not what we get from the client
			rs.set(4, String.valueOf(membersUID.get(name).activeMemberTime));
			gameState.getNetworkObject().factionMemberMod.add(rs);
		}
	}
	public RType getRelationshipWithFactionOrPlayer(long dbId) {
		assert(isOnServer());
		if(dbId >= Integer.MAX_VALUE){
			if(isOnServer()) {
				//player
				String playerName = ((GameServerState) state).getPlayerNameFromDbIdLowerCase(dbId);
				if(personalEnemies.contains(playerName)){
					return RType.ENEMY;
				}
			}else {
				Int2ObjectOpenHashMap<Sendable> pl = ((GameClientState) state).getLocalAndRemoteObjectContainer().getLocalObjectsByTopLvlType(TopLevelType.PLAYER);
				for(Sendable s : pl.values()) {
					if(s instanceof PlayerState) {
						if(((PlayerState) s).getDbId() == dbId && personalEnemies.contains(((PlayerState) s).getName().toLowerCase(Locale.ENGLISH))) {
							return RType.ENEMY;
						}
					}
				}
			}
		}else{
			
			Faction other = ((FactionState) state).getFactionManager().getFaction((int)dbId);
			if(other != null){
				return ((FactionState) state).getFactionManager().getRelation(factionId, other.factionId);
			}
		}
		return RType.NEUTRAL;
	}
	public boolean isFactionMode(int mode) {
		return (this.factionMode & mode) == mode;
	}

	public void setFactionMode(int mode, boolean on) {
		if (on) {
			this.factionMode |= mode;
		} else {
			this.factionMode &= ~mode;
		}
	}

	public void addOrModifyMemberClientRequest(String name, byte role, SendableGameState gameState) {
		RemoteStringArray rs = new RemoteStringArray(5, gameState.getNetworkObject());
		rs.set(0, name);
		rs.set(1, String.valueOf(factionId));
		rs.set(2, String.valueOf(role));
		rs.set(3, name); //initiator
		rs.set(4, "0");
		gameState.getNetworkObject().factionMemberMod.add(rs);
	}

	public void addOrModifyMemberClientRequest(String initiator, String name, byte role, SendableGameState gameState) {
		System.err.println("[CLIENT][Faction] Sending modding of member " + name + " from " + this);
		RemoteStringArray rs = new RemoteStringArray(5, gameState.getNetworkObject());
		rs.set(0, name);
		rs.set(1, String.valueOf(factionId));
		rs.set(2, String.valueOf(role));
		rs.set(3, initiator);
		rs.set(4, "0");
		gameState.getNetworkObject().factionMemberMod.add(rs);
	}

	public void clientRequestAttackNeutral(PlayerState player, boolean b) {
		player.getNetworkObject().requestAttackNeutral.add(new RemoteBoolean(b, player.getNetworkObject()));
	}

	public void clientRequestAutoDeclareWar(PlayerState player, boolean b) {
		player.getNetworkObject().requestAutoDeclareWar.add(new RemoteBoolean(b, player.getNetworkObject()));
	}

	public void clientRequestOpenFaction(PlayerState player, boolean b) {
		player.getNetworkObject().requestFactionOpenToJoin.add(new RemoteBoolean(b, player.getNetworkObject()));
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] sub = (Tag[]) tag.getValue();

		String code = sub[0].getString(); //UNUSED
		name = sub[1].getString();
		description = sub[2].getString();
		dateCreated = sub[3].getLong();

		Tag[] members = sub[4].getStruct();
		for (int i = 0; i < members.length - 1; i++) {
			FactionPermission p = new FactionPermission();
			p.fromTagStructure(members[i]);
			membersUID.put(p.playerUID, p);
		}
		openToJoin = ((Byte) sub[5].getValue() == 1);

		roles.fromTagStructure(sub[6]);
		setHomebaseUID((String) sub[7].getValue());
		if(FactionManager.isNPCFaction(factionId) && (String) sub[7].getValue() != null){
			setHomebaseUID(EntityType.SPACE_STATION.dbPrefix+DatabaseEntry.removePrefixWOException((String) sub[7].getValue()));
		}
		password = (String) sub[8].getValue();
		factionId = (Integer) sub[9].getValue();

		allyNeutral = sub[10].getBoolean();
		attackNeutral = sub[11].getBoolean();
		homeSector.set(sub[12].getVector3i());
		if (sub.length > 13 && sub[13].getType() == Type.BYTE) {
			autoDeclareWar = sub[13].getBoolean();
		}

		if (sub.length > 14 && sub[14].getType() != Type.FINISH) {
			Tag.listFromTagStruct(personalEnemies, sub[14].getStruct(), fromValue -> ((String) fromValue).toLowerCase(Locale.ENGLISH));
		}
		if (sub.length > 15 && sub[15].getType() != Type.FINISH) {
			factionMode = sub[15].getInt();
		}
		if (sub.length > 16 && sub[16].getType() != Type.FINISH) {
			color = sub[16].getVector3f();

			if (color.length() == 0 || (color.x == 0.5019608f && color.y == 1.0 && color.z == 1.0)) {
				color = getNewColor();
			}
		}
		if (sub.length > 17 && sub[17].getType() != Type.FINISH) {
			showInHub = sub[17].getBoolean();
		}
		if (sub.length > 25 && sub[25].getType() != Type.FINISH) {

			factionPoints = sub[18].getFloat();
			lastinactivePlayer = sub[19].getInt();
			lastGalaxyRadius = sub[20].getFloat();
			lastPointsFromOffline = sub[21].getFloat();
			lastPointsFromOnline = sub[22].getFloat();
			lastPointsSpendOnBaseRate = sub[23].getFloat();
			lastPointsSpendOnCenterDistance = sub[24].getFloat();
			lastPointsSpendOnDistanceToHome = sub[25].getFloat();

		}
		if (sub.length > 27 && sub[27].getType() != Type.FINISH) {
			lastCountDeaths = sub[26].getInt();
			lastLostPointAtDeaths = sub[27].getFloat();
		}
		if (sub.length > 28 && sub[28].getType() != Type.FINISH) {
			Tag.listFromTagStruct(lastSystems, sub[28]);
		}
		if (sub.length > 29 && sub[29].getType() != Type.FINISH) {
			try {
				fromTagAdditionalInfo(sub[29]);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag[] d = new Tag[31];

		d[0] = (new Tag(Type.STRING, null, "")); //UNUSED
		d[1] = (new Tag(Type.STRING, null, name));
		d[2] = (new Tag(Type.STRING, null, description));
		d[3] = (new Tag(Type.LONG, null, dateCreated));
		List<FactionPermission> UID = new ObjectArrayList<FactionPermission>(membersUID.values());
		d[4] = Tag.listToTagStruct(UID, "mem");
		d[5] = new Tag(Type.BYTE, null, openToJoin ? (byte) 1 : (byte) 0);

		d[6] = roles.toTagStructure();

		d[7] = new Tag(Type.STRING, "home", homebaseUID);
		d[8] = (new Tag(Type.STRING, "pw", password));
		d[9] = (new Tag(Type.INT, "id", factionId));

		d[10] = (new Tag(Type.BYTE, "fn", allyNeutral ? (byte) 1 : (byte) 0));
		d[11] = (new Tag(Type.BYTE, "en", attackNeutral ? (byte) 1 : (byte) 0));
		d[12] = (new Tag(Type.VECTOR3i, null, homeSector));

		d[13] = (new Tag(Type.BYTE, "aw", autoDeclareWar ? (byte) 1 : (byte) 0));

		List<String> enem = new ObjectArrayList<String>(personalEnemies);
		d[14] = Tag.listToTagStruct(enem, Type.STRING, "mem");

		d[15] = new Tag(Type.INT, null, factionMode);

		d[16] = new Tag(Type.VECTOR3f, null, color);

		d[17] = new Tag(Type.BYTE, null, showInHub ? (byte) 1 : (byte) 0);

		d[18] = new Tag(Type.FLOAT, null, factionPoints);
		d[19] = new Tag(Type.INT, null, lastinactivePlayer);
		d[20] = new Tag(Type.FLOAT, null, lastGalaxyRadius);
		d[21] = new Tag(Type.FLOAT, null, lastPointsFromOffline);
		d[22] = new Tag(Type.FLOAT, null, lastPointsFromOnline);
		d[23] = new Tag(Type.FLOAT, null, lastPointsSpendOnBaseRate);
		d[24] = new Tag(Type.FLOAT, null, lastPointsSpendOnCenterDistance);
		d[25] = new Tag(Type.FLOAT, null, lastPointsSpendOnDistanceToHome);
		d[26] = new Tag(Type.INT, null, lastCountDeaths);
		d[27] = new Tag(Type.FLOAT, null, lastLostPointAtDeaths);

		List<Vector3i> syss = new ObjectArrayList<Vector3i>(lastSystems);
		d[28] = Tag.listToTagStruct(syss, Type.VECTOR3i, null);

		d[29] = toTagAdditionalInfo();
		d[30] = FinishTag.INST;

		Tag root = new Tag(Type.STRUCT, "f0", d);
		return root;
	}

	protected void fromTagAdditionalInfo(Tag t) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
	}
	protected Tag toTagAdditionalInfo() {
		return new Tag(Type.BYTE, null, (byte)0);
	}
	public void initializeWithState(GameServerState state) throws IllegalArgumentException, IllegalAccessException, ConfigParserException {
	}
	public void setHomeParamsFromUID(GameServerState state) {
		if (homebaseUID.length() > 0) {
			try {
				List<DatabaseEntry> byUIDExact;
				try {
					byUIDExact = state.getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefixWOException(homebaseUID), 1);

					if (byUIDExact.size() == 1) {
						DatabaseEntry databaseEntry = byUIDExact.get(0);

						homeSector.set(new Vector3i(databaseEntry.sectorPos));
						homebaseRealName = new String(databaseEntry.realName);

					} else {
						throw new RuntimeException("Cannot set homebase on server: " + homebaseUID + " was not found in sql database");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException("Cannot set homebase on server: " + homebaseUID + " was not found in sql database");
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
				setHomebaseUID("");
				homeSector.set(0, 0, 0);
				homebaseRealName = "";
			}
		}
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the homebaseUID
	 */
	public String getHomebaseUID() {
		return homebaseUID;
	}

	/**
	 * @param homebaseUID the homebaseUID to set
	 */
	public void setHomebaseUID(String homebaseUID) {
		this.homebaseUID = homebaseUID;
		if(state instanceof FactionState && ((FactionState) state).getFactionManager() != null) {
			((FactionState) state).getFactionManager().flagHomeBaseChanged(this);
		}
	}

	/**
	 * @return the homeSector
	 */
	public Vector3i getHomeSector() {
		return homeSector;
	}

	public int getIdFaction() {
		return factionId;
	}

	public void setIdFaction(int factionId) {
		this.factionId = factionId;
		roles.factionId = factionId;

	}

	/**
	 * @return the members
	 */
	public Map<String, FactionPermission> getMembersUID() {
		return membersUID;
	}

	//	public boolean isEnemy(int factionId) {
	//		return factionId != this.factionId && (enemies.contains(factionId) || (factionId == 0 && isAttackNeutral()));
	//	}
	//
	//	public boolean isFriend(int factionId) {
	//		return factionId == this.factionId || (friends.contains(factionId)  || (factionId == 0 && isAllyNeutral()));
	//	}
	//
	//	public void setEnemyRelationship(int toId, SendableGameState gameState){
	//		setRelationship(toId, REL_ENEMY, gameState);
	//	}
	//	public void setFriendRelationship(int toId, SendableGameState gameState){
	//		setRelationship(toId, REL_FRIEND, gameState);
	//	}
	//	public void setNeutralRelationship(int toId, SendableGameState gameState){
	//		setRelationship(toId, REL_NEUTRAL, gameState);
	//	}

	//	public void setRelationship(int toId, int relationship, SendableGameState gameState){
	//		synchronized(this){
	//			if(relationship == REL_ENEMY){
	//				friends.remove(toId);
	//				if(!enemies.contains(toId)){
	//					enemies.add(toId);
	//				}
	////				Faction faction = gameState.getFactionManager().getFactionMap().get(toId);
	////				if(faction != null){
	////					faction.setEnemyRelationship(toId, gameState);
	////				}else{
	////					System.err.println("Exception: could not assign faction relationship versa: "+toId);
	////				}
	//			}else if(relationship == REL_FRIEND){
	//				enemies.remove(toId);
	//				if(!friends.contains(toId)){
	//					friends.add(toId);
	//				}
	//			}else if(relationship == REL_NEUTRAL){
	//				enemies.remove(toId);
	//				friends.remove(toId);
	//			}else{
	//				throw new IllegalArgumentException("Invalid Faction Relationship: "+relationship);
	//			}
	//		}
	//
	//	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the roles
	 */
	public FactionRoles getRoles() {
		return roles;
	}

	@Override
	public String getUniqueIdentifier() {
		return "FACTION_" + factionId;
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	public boolean isAllyNeutral() {
		return allyNeutral;
	}

	public void setAllyNeutral(boolean allyNeutral) {
		this.allyNeutral = allyNeutral;
	}

	public boolean isAttackNeutral() {
		return attackNeutral;
	}

	public void setAttackNeutral(boolean attackNeutral) {
		this.attackNeutral = attackNeutral;
	}

	/**
	 * @return the autoDeclareWar
	 */
	public boolean isAutoDeclareWar() {
		return autoDeclareWar;
	}

	/**
	 * @param autoDeclareWar the autoDeclareWar to set
	 */
	public void setAutoDeclareWar(boolean autoDeclareWar) {
		this.autoDeclareWar = autoDeclareWar;
	}

	public boolean isOpenToJoin() {
		return openToJoin;
	}

	public void setOpenToJoin(boolean openToJoin) {
		this.openToJoin = openToJoin;
	}

	public void kickMemberClientRequest(String initiator, String name, SendableGameState gameState) {
		System.err.println("[CLIENT][Faction] Sending removal of member " + name + " from " + this);
		RemoteStringArray rs = new RemoteStringArray(3, gameState.getNetworkObject());
		rs.set(0, name);
		rs.set(1, String.valueOf(factionId));
		rs.set(2, initiator);
		gameState.getNetworkObject().factionkickMemberRequests.add(rs);
	}
	public int getFactionIdOfAttacker(Damager attacker){
		int attFID = ((SimpleTransformableSendableObject<?>) attacker).getFactionId();

		

		if (attFID == 0 && attacker.getOwnerState() != null) {
			attFID = attacker.getOwnerState().getFactionId();
		}
		return attFID;
	}
	public void onAttackOnServer(Damager attacker) {
		assert (isOnServer());
		if (attacker instanceof SimpleTransformableSendableObject) {
				
			kickMemberOnFriendlyFire(attacker);

			if (autoDeclareWar) {
				declareWarOrPersonalEnemyOnHostileAction(attacker);
			}
			
			if(attacker != null){
				long id;
				if(attacker.getOwnerState() != null && attacker.getOwnerState() instanceof PlayerState){
					id = ((PlayerState)attacker.getOwnerState()).getDbId();
				}else{
					id = attacker.getFactionId();
				}
				attackedBy.add(id);
				((FactionState) state).getFactionManager().scheduleActionCheck(factionId);
			}
		}
	}
	public void kickMemberOnFriendlyFire(Damager attacker){
		assert (isOnServer());
		if (attacker.getOwnerState() != null && attacker.getOwnerState() instanceof PlayerState && attacker.getOwnerState().getFactionId() == factionId) {
			//Friendly Fire!
			PlayerState ps = ((PlayerState) attacker.getOwnerState());
			if (roles.hasKickOnFriendlyFire(((PlayerState) attacker.getOwnerState()).getFactionRights()) && System.currentTimeMillis() - ps.lastFriendlyFireStrike > 1000) {
				if (ps.friendlyFireStrikes >= 2) {
					ps.sendServerMessagePlayerError(Lng.astr("You have been kicked\nfrom your faction\nfor repeated friendly fire!"));
					Object[] msg = Lng.astr("Faction member\n%s\nwas kicked for\nfriendly fire!",  ps.getName());
					((GameServerState) attacker.getState()).getController().broadcastMessage(msg, ServerMessage.MESSAGE_TYPE_ERROR);
	
					((GameServerState) attacker.getState()).getFactionManager().removeMemberOfFaction(factionId, ps);
				} else {
					ps.friendlyFireStrikes++;
					int maxStrikes = 3;
					ps.sendServerMessagePlayerError(Lng.astr("WARNING!\nYour faction rank does not\nallow friendly fire!\nStrikes: %s/%s", ps.friendlyFireStrikes,  maxStrikes));
				}
				ps.lastFriendlyFireStrike = System.currentTimeMillis();
			}
		}
	}
	public void declareWarOrPersonalEnemyOnHostileAction(Damager attacker) {
		assert (isOnServer());
		FactionManager factionManager = ((FactionState) attacker.getState()).getFactionManager();
		int attFID = getFactionIdOfAttacker(attacker);
		
		if (attFID == FactionManager.TRAIDING_GUILD_ID) {
			//do not declare war against trading guild!
			return ;
		}
		
		if (factionId != attFID && factionManager.isNeutral(factionId, attFID)) {
			if (attFID == 0 || factionManager.getFaction(attFID) == null) {
				addPersonalElemy(attacker);
			} else {
				declareFactionWarOnHostileAction(attacker);
			}
		}
	}
	public void declareWarAgainstEntity(long entId){
		assert(isOnServer());
		if(entId >= Integer.MAX_VALUE){
			String playerName = ((GameServerState)state).getPlayerNameFromDbIdLowerCase(entId);
			personalEnemies.add(playerName.toLowerCase(Locale.ENGLISH));
			PlayerState playerState = ((GameServerState)state).getPlayerStatesByDbId().get(entId);
			if(playerState != null){
				playerState.sendServerMessagePlayerError(Lng.astr("Faction %s now considers you as a personal enemy!\nThis is not a war declaration against your faction!", name));
			}
		}else{
			Faction other = ((GameServerState)state).getFactionManager().getFaction((int)entId);
			if (other != null && getRelationshipWithFactionOrPlayer(entId) != RType.ENEMY) {
				sentWarDeclaration.put(other.factionId, System.currentTimeMillis());
				FactionRelationOffer war = new FactionRelationOffer();
				String news = Lng.str("WAR DECLARATION:\nPrepare for war!");

				Object[] msg = Lng.astr("WAR: \n%s declared war on\n%s!", name, other.name);

				other.broadcastMessage(msg, ServerMessage.MESSAGE_TYPE_ERROR, (GameServerState)state);

				war.set("ADMIN", factionId, other.factionId, RType.ENEMY.code, news, false);
				
				((GameServerState)state).getFactionManager().relationShipOfferServer(war);
			}
		}
	}
	public void declareFactionWarOnHostileAction(Damager attacker) {
		assert (isOnServer());
		FactionManager factionManager = ((FactionState) attacker.getState()).getFactionManager();
		int attFID = getFactionIdOfAttacker(attacker);
		
		if (attFID == FactionManager.TRAIDING_GUILD_ID) {
			//do not declare war against trading guild!
			return ;
		}
		Faction faction = factionManager.getFaction(attFID);
		if(faction == null){
			System.err.println("ERROR: Attacker had no faction. should be personal enemy: "+attacker);
			assert(false);
			return;
		}
		long cat = sentWarDeclaration.get(attFID);
		if (cat > 0 && System.currentTimeMillis() - cat < 5000) {
			//a war declation to this faction was sent less than 5 sec ago already
		} else {
			if (!factionManager.isEnemy(this.factionId, faction.factionId) && !factionManager.isFriend(this.factionId, faction.factionId)) {
				sentWarDeclaration.put(attFID, System.currentTimeMillis());
				FactionRelationOffer war = new FactionRelationOffer();
				String news = Lng.str("AUTO-DECLARED-WAR:\nHostile action detected from\n%s!",  attacker);
				if (attacker instanceof PlayerControllable) {
					if (((PlayerControllable) attacker).getAttachedPlayers().size() > 0) {
						news += Lng.str("Pilot:",  ((PlayerControllable) attacker).getAttachedPlayers().get(0).getName());
					}
				}

				Object[] msg = Lng.astr("WAR: \n%s declared war on\n%s!", name, faction.name);

				((GameServerState) attacker.getState()).getController().broadcastMessage(msg, ServerMessage.MESSAGE_TYPE_ERROR);

				war.set("ADMIN", factionId, attFID, RType.ENEMY.code, news, false);
				System.err.println("[SERVER][FACTION] Hostlie action detected from " + attacker + " of Faction " + faction + ": DECLARING WAR");

				factionManager.relationShipOfferServer(war);
			}
		}		
	}

	public void addPersonalElemy(Damager attacker) {
		assert(isOnServer());
		FactionManager factionManager = ((FactionState) attacker.getState()).getFactionManager();

		if (attacker.getOwnerState() != null && attacker.getOwnerState() instanceof PlayerState) {
			if (attacker.getOwnerState().getFactionId() != factionId && !personalEnemies.contains(attacker.getOwnerState().getName().toLowerCase(Locale.ENGLISH))) {
				personalEnemies.add(attacker.getOwnerState().getName().toLowerCase(Locale.ENGLISH));
				factionManager.sendPersonalEnemyAdd("SERVER", this, attacker.getOwnerState().getName().toLowerCase(Locale.ENGLISH));

				Object[] msg = Lng.astr("Faction Attack Response: \n%s now considers\n%s\nas an enemy!", name,  attacker.getOwnerState().getName());
				((GameServerState) attacker.getState()).getController().broadcastMessage(msg, ServerMessage.MESSAGE_TYPE_ERROR);
			}
		}		
	}

	public boolean isOnServer() {
		return state instanceof ServerStateInterface;
	}

	public void removeMember(String name, SendableGameState gameState) {
		synchronized (membersUID) {
			FactionPermission toDel = membersUID.remove(name);
			if (toDel != null) {
				//				System.err.println("[Faction] "+gameState.getState()+" Found entry to delete: "+toDel);

				if (gameState.isOnServer()) {
					System.err.println("[SERVER][Faction] Sending removal of member " + name + " from " + this);
					RemoteStringArray rs = new RemoteStringArray(5, gameState.getNetworkObject());
					rs.set(0, name);
					rs.set(1, String.valueOf(factionId));
					rs.set(2, "r");
					rs.set(3, name);
					rs.set(4, "0");
					gameState.getNetworkObject().factionMemberMod.add(rs);
				}
			} else {
				System.err.println("[Faction] WARNING: could not remove " + name + " from " + membersUID + " on " + gameState.getState());
			}

		}
	}

	public void removeMemberClientRequest(String initiator, String name, SendableGameState gameState) {
		System.err.println("[CLIENT][Faction] Sending removal of member " + name + " from " + this);
		RemoteStringArray rs = new RemoteStringArray(5, gameState.getNetworkObject());
		rs.set(0, name);
		rs.set(1, String.valueOf(factionId));
		rs.set(2, "r");
		rs.set(3, initiator);
		rs.set(4, "0");
		gameState.getNetworkObject().factionMemberMod.add(rs);
	}

	public void sendAttackNeutralMod(String initiator, boolean open, SendableGameState state) {
		sendMod(initiator, NT_CODE_ATTACK_NEUTRAL, String.valueOf(open), state);
	}

	public void sendAutoDeclareWar(String initiator, boolean open, SendableGameState state) {
		sendMod(initiator, NT_CODE_AUTO_DECLARE_WAR, String.valueOf(open), state);
	}

	public void sendDescriptionMod(String initiator, String desc, SendableGameState state) {
		sendMod(initiator, NT_CODE_DESCRIPTION, desc, state);
	}

	public void sendMod(String initiator, String option, String setting, SendableGameState state) {

		RemoteStringArray s = new RemoteStringArray(4, state.getNetworkObject());
		s.set(0, initiator);
		s.set(1, String.valueOf(factionId));
		s.set(2, option);
		s.set(3, setting);
		state.getNetworkObject().factionMod.add(s);
	}

	public void sendFactionPointUpdate(SendableGameState state) {
		FactionPointMod.send(this, state);

	}

	public void sendNameMod(String initiator, String desc, SendableGameState state) {
		sendMod(initiator, NT_CODE_NAME, desc, state);
	}

	public void sendOpenToJoinMod(String initiator, boolean open, SendableGameState state) {
		sendMod(initiator, NT_CODE_OPEN_TO_JOIN, String.valueOf(open), state);
	}
	public void sendMemberNameChangeMod(String from, String to, SendableGameState state) {
		sendMod(from, NT_CODE_MEMBER_NAME_CHANGE, to, state);
	}

	public void deserializeMembers(DataInputStream buffer) throws IOException {
		int size = buffer.readInt();

		for (int i = 0; i < size; i++) {
			String name = buffer.readUTF();
			byte role = buffer.readByte();
			long memberTime = buffer.readLong();
			FactionPermission factionPermission = new FactionPermission(name, role, memberTime);

			membersUID.put(factionPermission.playerUID, factionPermission);

		}
	}

	public void serializeMembers(DataOutputStream buffer) throws IOException {
		buffer.writeInt(membersUID.size());
		for (FactionPermission m : membersUID.values()) {
			buffer.writeUTF(m.playerUID);
			buffer.writeByte(m.role);
			buffer.writeLong(m.activeMemberTime);
		}
	}

	public void deserializePersonalEnemies(DataInputStream buffer) throws IOException {
		int size = buffer.readInt();

		for (int i = 0; i < size; i++) {
			String name = buffer.readUTF();
			personalEnemies.add(name.toLowerCase(Locale.ENGLISH));
		}
	}

	public void serializePersonalEmenies(DataOutputStream buffer) throws IOException {
		buffer.writeInt(personalEnemies.size());
		for (String m : personalEnemies) {
			buffer.writeUTF(m.toLowerCase(Locale.ENGLISH));
		}
	}

	/**
	 * @return the homebaseRealName
	 */
	public String getHomebaseRealName() {
		return homebaseRealName;
	}

	public void setHomebaseRealName(String realName) {
		this.homebaseRealName = realName;
	}

	/**
	 * @return the personalEnemies
	 */
	public ObjectOpenHashSet<String> getPersonalEnemies() {
		return personalEnemies;
	}

	/**
	 * @return the factionMode
	 */
	public int getFactionMode() {
		return factionMode;
	}

	/**
	 * @param factionMode the factionMode to set
	 */
	public void setFactionMode(int factionMode) {
		this.factionMode = factionMode;
	}

	public void setServerOpenForJoin(GameServerState state, boolean b) {
		openToJoin = b;
		RemoteStringArray s = new RemoteStringArray(4, state.getGameState().getNetworkObject());
		s.set(0, "ADMIN");
		s.set(1, String.valueOf(factionId));
		s.set(2, Faction.NT_CODE_OPEN_TO_JOIN);
		s.set(3, String.valueOf(b));

		System.err.println("SERVER SENDING FACTION CHANGE ###### OPEN TO JOIN " + b);
		state.getGameState().getNetworkObject().factionMod.add(s);
	}

	/**
	 * @return the color
	 */
	public Vector3f getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Vector3f color) {
		this.color = color;
	}

	public void broadcastMessage(Object[] msg, byte type, GameServerState state) {
		for (PlayerState p : state.getPlayerStatesByName().values()) {
			if (p.getFactionId() == factionId) {
				p.sendServerMessage(new ServerMessage(msg, type, p.getId()));
			}
		}
	}

	/**
	 * @return the showInHub
	 */
	public boolean isShowInHub() {
		return showInHub;
	}

	/**
	 * @param showInHub the showInHub to set
	 */
	public void setShowInHub(boolean showInHub) {
		this.showInHub = showInHub;
	}

	public void handleActivityServer(FactionManager man) {
		GameServerState state = (GameServerState) man.getGameState().getState();

		lastCountDeaths = serverDeaths;
		lastLostPointAtDeaths = serverLostDeathPoints;

		serverLostDeathPoints = 0;
		serverDeaths = 0;
	}

	public void handleFactionPointGainServer(FactionManager man) {
		GameServerState state = (GameServerState) man.getGameState().getState();

		float pointsFromOnline = 0;
		float pointsFromOffline = 0;
		int inactivePlayers = 0;
		for (String name : membersUID.keySet()) {
			PlayerState playerState = state.getPlayerStatesByNameLowerCase().get(name.toLowerCase(Locale.ENGLISH));

			if (playerState != null) {
				pointsFromOnline += FactionPointIncomeConfig.FACTION_POINTS_PER_ONLINE_MEMBER;
			} else if (membersUID.get(name).isActiveMember()) {
				pointsFromOffline += FactionPointIncomeConfig.FACTION_POINTS_PER_MEMBER;
			} else {
				inactivePlayers++;
			}
		}

		lastPointsFromOnline = pointsFromOnline;
		lastPointsFromOffline = pointsFromOffline;
		lastinactivePlayer = inactivePlayers;

		float gained = (pointsFromOffline + pointsFromOnline);
		this.factionPoints += gained;

//		System.err.println("[FACTIONS][POINTS] Faction: "+getName()+" gained: "+gained+" points (now: "+factionPoints+"); (online: "+pointsFromOnline+", offline: "+pointsFromOffline+", inactive: "+inactivePlayers+")");
	}

	public void handleFactionPointExpensesServer(FactionManager man, Object2IntOpenHashMap<Vector3i> galaxyMapCounts) {
		float totalSpendDistanceToCenter = 0;
		float totalSpendDistanceToHome = 0;
		float totalSpendBaseRate = 0;

		Vector3i homebase = StellarSystem.getPosFromSector(homeSector, new Vector3i());

		GameServerState state = (GameServerState) man.getGameState().getState();
		differenceSystemSectorsAdd.clear();
		differenceSystemSectorsRemove.clear();
		differenceSystemSectorsRemove.addAll(lastSystemSectors);

		this.lastSystemSectors.clear();
		this.lastSystems.clear();
		this.lastSystemUIDs.clear();

		List<Vector3i> systemsByFaction = state.getDatabaseIndex().getTableManager().getSystemTable().getSystemsByFaction(factionId, this.lastSystems, this.lastSystemSectors, this.lastSystemUIDs);
		boolean hasHomeBase = homebaseUID.length() > 0;
		for (int i = 0; i < systemsByFaction.size(); i++) {

			Vector3i sysPos = systemsByFaction.get(i);
			Vector3i secPos = this.lastSystemSectors.get(i);

			String ownerStationUid = this.lastSystemUIDs.get(i);

			if (ownerStationUid.startsWith(EntityType.SPACE_STATION.dbPrefix)) {
				boolean remove = true;

				Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(ownerStationUid);

				if(sendable != null && sendable instanceof SpaceStation){
					SpaceStation sp = (SpaceStation)sendable;
					Sector sector = state.getUniverse().getSector(sp.getSectorId());
					if(sector != null && sector.pos.equals(secPos)){
						remove = false;
					}
				}
				
				if(remove){
					List<DatabaseEntry> byUIDExact;
					try {
						byUIDExact = state.getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefixWOException(ownerStationUid), -1);
						if (byUIDExact.size() > 0 && byUIDExact.get(0).sectorPos.equals(secPos)) {
							remove = false;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				if (remove) {
					System.err.println("[SERVER][FACTION] Station for system " + sysPos + " was no longer found. removing ownership from faction " + this);
					broadcastMessage(Lng.astr("The station of your claimed system\n%s has been moved or destroyed.\nYou no longer control that territory.", sysPos), ServerMessage.MESSAGE_TYPE_ERROR, state);
					VoidSystem s = new VoidSystem();
					state.getDatabaseIndex().getTableManager().getSystemTable().loadSystem(state, new Vector3i(sysPos), s);
					FactionSystemOwnerChange fk = new FactionSystemOwnerChange("ADMIN(Auto Revoke on moved or destroyed Station)", 0, "", s.getOwnerPos(), s.getPos(), s.getName());

					state.getFactionManager().addFactionSystemOwnerChangeServer(fk);

					//faction doesn't already get any gains in this round from this system
					lastSystemSectors.remove(i);
					lastSystems.remove(i);
					lastSystemUIDs.remove(i);
					i--;
					continue;
				}

			}

			if (!hasHomeBase) {
				homebase.set(sysPos);
				hasHomeBase = true;
			}

			//only the sectors that are no longer in the list are not removed 
			//-> removeSet will contain of sectors no more in the list
			if (!differenceSystemSectorsRemove.remove(secPos)) {
				//only sectors that werent in the set before get added
				//-> addedSet will contain of sectors that werent in the list before
				differenceSystemSectorsAdd.add(secPos);
			}

			Vector3i galax = Galaxy.getContainingGalaxyFromSystemPos(sysPos, new Vector3i());
			int ownedSectorCount;

			Vector3i from = new Vector3i(galax.x * Galaxy.size - (Galaxy.halfSize - 1),
					galax.y * Galaxy.size - (Galaxy.halfSize - 1),
					galax.z * Galaxy.size - (Galaxy.halfSize - 1));
			Vector3i to = new Vector3i(galax.x * Galaxy.size + (Galaxy.halfSize),
					galax.y * Galaxy.size + (Galaxy.halfSize),
					galax.z * Galaxy.size + (Galaxy.halfSize));

			if (!galaxyMapCounts.containsKey(galax)) {

				ownedSectorCount = state.getDatabaseIndex().getTableManager().getSystemTable().getOwnedSystemCount(from.x, from.y, from.z, to.x, to.y, to.z);
				galaxyMapCounts.put(galax, ownedSectorCount);
			} else {
				ownedSectorCount = galaxyMapCounts.getInt(galax);
			}
//			System.err.println("COUNT FOR SYS: "+sysPos+" in galaxy: "+galax+" -> "+ownedSectorCount+"; SYSTEMRANGE["+from+" -> "+to+"]; current homebase in sys: "+homebase+", is sys current home: "+homebase.equals(sysPos)+": Free: "+(FactionPointGalaxyConfig.FREE_HOMEBASE && homebase.equals(sysPos)));

			if (!(FactionPointGalaxyConfig.FREE_HOMEBASE && homebase.equals(sysPos))) {
				//home base or first system is free
				totalSpendDistanceToCenter += getSpendingDisToCenter(sysPos, ownedSectorCount);
				totalSpendDistanceToHome += getSpendingDisToHome(homebase, sysPos);
				totalSpendBaseRate += FactionPointSpendingConfig.FACTION_POINTS_PER_CONTROLLED_SYSTEM;
			}
		}
		lastPointsSpendOnCenterDistance = totalSpendDistanceToCenter;

		lastPointsSpendOnDistanceToHome = totalSpendDistanceToHome;

		lastPointsSpendOnBaseRate = totalSpendBaseRate;

		float spent = (totalSpendDistanceToCenter + totalSpendDistanceToHome + totalSpendBaseRate + FactionPointSpendingConfig.BASIC_FLAT_COST);

		factionPoints -= spent;

//		System.err.println("[FACTIONS][POINTS] Faction: "+getName()+" spent: "+spent+" points (now: "+factionPoints+")(controlled: "+systemsByFaction.size()+", hb: "+totalSpendDistanceToHome+", cent: "+totalSpendDistanceToCenter+", base: "+totalSpendBaseRate+") galaxyRadius: "+lastGalaxyRadius);
	}

	private float getSpendingDisToHome(Vector3i homebase, Vector3i sysPos) {
		return Vector3fTools.length(homebase, sysPos) * FactionPointGalaxyConfig.PENALTY_PER_DISTANCE_UNIT_FROM_HOMEBASE;
	}

	private float getSpendingDisToCenter(Vector3i sysPos, int totalTakenInGalaxy) {
		Vector3i localCoordinatesFromSystem = Galaxy.getLocalCoordinatesFromSystem(sysPos, new Vector3i());

		localCoordinatesFromSystem.sub(Galaxy.halfSize, Galaxy.halfSize, Galaxy.halfSize);

		float radius = FastMath.carmackSqrt((FastMath.pow(totalTakenInGalaxy, 2) / FastMath.PI));

		lastGalaxyRadius = radius;

		float systemFromCenter = localCoordinatesFromSystem.length();

//		System.err.println("SYSTEM "+sysPos+" FROM CENTER: "+systemFromCenter+"; radius: "+radius);

		if (systemFromCenter < radius) {
			return 0;
		}
		float total = (systemFromCenter - radius) * FactionPointGalaxyConfig.PENALTY_FROM_CENTER_MULT;
		assert (total >= 0) : total + "; radius: " + radius + "";
		return total;
	}

	public void onPlayerDied(PlayerState playerState, Damager from) {
		if (playerState == from) {
			System.err.println("[DEATH][FACTION] " + playerState + " Suicide is free");
			playerState.sendServerMessage(new ServerMessage(Lng.astr("Respawn does not cost faction points!"), ServerMessage.MESSAGE_TYPE_SIMPLE, playerState.getId()));
			return;
		}

		if (System.currentTimeMillis() - playerState.getLastDeathNotSuicideFactionProt() < FactionPointsGeneralConfig.FACTION_POINT_DEATH_PROTECTION_MIN * 60 * 1000) {
			float proMin = ((FactionPointsGeneralConfig.FACTION_POINT_DEATH_PROTECTION_MIN * 60 * 1000 - (System.currentTimeMillis() - playerState.getLastDeathNotSuicideFactionProt())) / 1000f) / 60f;
			playerState.sendServerMessage(new ServerMessage(Lng.astr("Because of your previous death, you won't lose points for another %s minutes!", StringTools.formatPointZeroZero(proMin)), ServerMessage.MESSAGE_TYPE_SIMPLE, playerState.getId()));
			return;
		}

		float abs = FactionPointSpendingConfig.FACTION_POINT_ABS_LOSS_PER_DEATH;
		float member = FactionPointSpendingConfig.FACTION_POINT_MULT_BY_MEMBERS_LOSS_PER_DEATH * membersUID.size();

		serverLostDeathPoints += (abs + member);

		factionPoints -= (abs + member);

		FactionNewsPost n = new FactionNewsPost();
		n.set(this.factionId, "Faction Point Reporter", System.currentTimeMillis(), Lng.str("Faction Points Lost"), Lng.str("Faction member %s lost %d Faction Points for dying. Cause of death: %s", playerState.getName(), FastMath.round(abs + member), (from == null ? "unknown" : from.getName())), 0);
		((GameServerState) playerState.getState()).getFactionManager().addNewsPostServer(n);

		serverDeaths++;

		sendFactionPointUpdate(((GameServerState) playerState.getState()).getGameState());

		playerState.sendServerMessage(new ServerMessage(Lng.astr("You lost faction points for dying: %d", FastMath.round(abs + member)), ServerMessage.MESSAGE_TYPE_SIMPLE, playerState.getId()));
	}

	public void hanldeDeficit(FactionManager factionManager,
	                          Object2IntOpenHashMap<Vector3i> galaxyMapCounts) {
		GameServerState state = (GameServerState) factionManager.getGameState().getState();
		if (factionPoints < 0) {

			Vector3i farthest = null;
			float farest = -1;
			Vector3i homebase = StellarSystem.getPosFromSector(homeSector, new Vector3i());

			boolean hasHomeBase = homebaseUID.length() > 0;
			for (Vector3i sysPos : lastSystems) {
				if (!hasHomeBase) {
					homebase.set(sysPos);
					hasHomeBase = true;
				}
				float dist = Vector3fTools.length(sysPos, homebase);

				if (farthest == null || (dist > farest)) {
					farest = dist;
					farthest = sysPos;
				}
			}

			if (farthest != null) {
				VoidSystem s = new VoidSystem();
				state.getDatabaseIndex().getTableManager().getSystemTable().loadSystem(state, farthest, s);
				FactionSystemOwnerChange fk = new FactionSystemOwnerChange("ADMIN", 0, "", s.getOwnerPos(), s.getPos(), s.getName());

				FactionNewsPost n = new FactionNewsPost();
				n.set(this.factionId, "Faction Point Reporter", System.currentTimeMillis(), Lng.str("Territory Lost"), Lng.str("Because of lack of faction points, you lost control over galaxy system %s", farthest.toString()), 0);
				factionManager.addNewsPostServer(n);

				factionManager.addFactionSystemOwnerChangeServer(fk);
			} else {
				FactionNewsPost n = new FactionNewsPost();
				n.set(this.factionId, "Faction Point Reporter", System.currentTimeMillis(), Lng.str("CRITICAL: Home Base vulnerable"), Lng.str("CRITICAL WARNING!\nWithout Faction Points and territory, your homebase has become destroyable!"), 0);
			}
		}
	}

	@Override
	public FogOfWarController getFogOfWar() {
		return fow;
	}

	@Override
	public long getFogOfWarId() {
		return factionId;
	}


	@Override
	public StateInterface getState() {
		return state;
	}
	
	public void shareFogOfWar(Faction f){
		f.fow.merge(this);
	}

	private void fogOfWarUpdateServer(FactionPermission p) {
		if(state instanceof GameServerState){
			long playerId;
			try {
				playerId = ((GameServerState)state).getDatabaseIndex().getTableManager().getPlayerTable().getPlayerId(p.playerUID);
				if(playerId >= 0){
					((GameServerState)state).getDatabaseIndex().getTableManager().getVisibilityTable().mergeVisibility(this.factionId, playerId);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			((GameClientState)state).getController().getClientChannel().getGalaxyManagerClient().resetClientVisibility();
		}
	}
	public void fogOfWarCheckServer(FactionPermission p, FactionRoles newRoles) {
		
		
		if(roles.hasFogOfWarPermission(p.role) && newRoles.hasFogOfWarPermission(p.role)){
			System.err.println("[SERVER][FACTION] fog of war check. Role "+p.playerUID+" lost fog of war of faction. sharing current data");
			fogOfWarUpdateServer(p);
		}
		
	}

	public void clearFogOfWar() {
		if(state instanceof GameServerState){
			try {
				((GameServerState)state).getDatabaseIndex().getTableManager().getVisibilityTable().clearVisibility(getFogOfWarId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void sendFowResetToClient(Vector3i sysTo) {
		for(FactionPermission f : membersUID.values()){
			PlayerState pl;
			try {
				pl = ((GameServerState)state).getPlayerFromNameIgnoreCase(f.playerUID);
				pl.getNetworkObject().resetFowBuffer.add(new RemoteVector3i(sysTo, true));
			} catch (PlayerNotFountException e) {
				System.err.println("NOT ONLINE: "+f.playerUID);
			}
		}
	}

	public void initialize() {
	}

	public void onEntityDestroyedServer(SegmentController segmentController) {
		
	}
	public void onEntityOverheatingServer(SegmentController segmentController) {
		
	}

	public void onAddedSectorSynched(Sector sec) {
	}

	public void onRemovedSectorSynched(Sector sec) {
	}

	public boolean isNPC() {
		return false;
	}
	public void serializeExtra(DataOutputStream buffer) throws IOException{
	}
	public void deserializeExtra(DataInputStream stream)  throws IOException{
	}
	public boolean isPlayerFaction() {
		return factionId > 0;
	}
	public List<PlayerState> getOnlinePlayers() {
		assert(isOnServer());
		if(isNPC()){
			return new ObjectArrayList(0);
		}
		List<PlayerState> pp = new ObjectArrayList<PlayerState>();
		for(FactionPermission s : membersUID.values()){
			PlayerState pl = ((GameServerState) state).getPlayerFromNameIgnoreCaseWOException(s.playerUID.toLowerCase(Locale.ENGLISH));
			if(pl != null){
				pp.add(pl);
			}
		}
		return pp;
	}
	@Override
	public FactionRuleEntityManager getRuleEntityManager() {
		return ruleEntityManager;
	}
	@Override
	public TopLevelType getTopLevelType() {
		return TopLevelType.FACTION;
	}
	@Override
	public NTRuleInterface getNetworkObject() {
		return ((GameStateInterface) state).getGameState().getNetworkObject();
	}
	public void updateLocal(Timer timer) {
		ruleEntityManager.update(timer);
	}
}
