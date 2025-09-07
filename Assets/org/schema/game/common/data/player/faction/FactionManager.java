package org.schema.game.common.data.player.faction;

import api.listener.events.faction.FactionRelationChangeEvent;
import api.listener.events.faction.SystemClaimEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import org.schema.common.LogUtil;
import org.schema.common.config.ConfigParserException;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.FactionChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.faction.config.FactionConfig;
import org.schema.game.common.data.player.faction.config.FactionPointsGeneralConfig;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.*;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.controller.SectorListener;
import org.schema.game.server.data.*;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFactionConfig;
import org.schema.game.server.data.simulation.npc.NPCFactionControlCommand;
import org.schema.game.server.data.simulation.npc.NPCFactionPresetManager;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacy;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteField;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class FactionManager implements DiskWritable, SectorListener {

	public static final int CODE_MIN_LENGTH = 6;
	public static final int CODE_MAX_LENGTH = 64;

	public static final int PIRATES_ID = -1;
	public static final int TRAIDING_GUILD_ID = -2;
	public static final int[] FAUNA_GROUP_ENEMY = new int[]{-1000, -1001, -1002, -1003, -1004, -1005, -1006, -1007};
	public static final int[] FAUNA_GROUP_NEUTRAL = new int[]{-2000, -2001, -2002, -2003, -2004, -2005, -2006, -2007};
	
	public static final int NPC_FACTION_START = -10000000;
	public static final int NPC_FACTION_END = -10000;
	public static final int OTHER_POWER0 = -3;
	public static final int OTHER_POWER1 = -4;
	public static final int OTHER_POWER2 = -5;
	public static final int OTHER_POWER3 = -6;
	public static final int OTHER_POWER4 = -7;
	public static final int OTHER_POWER5 = -8;
	public static final int OTHER_POWER6 = -9;
	public static final int OTHER_POWER7 = -10;
	public static final int OTHER_POWER8 = -11;
	public static final int OTHER_POWER9 = -12;
	public static final String[] RESERVED_CODES = new String[]{
			"pirates",
			"traiding guild",
			"NEUTRAL",
	};
	public static final int ID_NEUTRAL = 0;
	private static final String factionVersion0 = "factions-v0";
	private static final String factionVersion1 = "factions-v1";
	private static final String factionVersion2 = "factions-v2";
	public static Random random = new Random();
	private static int FACTION_ID_GEN = 10000;
	final List<FactionNewsPost> toAddFactionNewsPosts = new ArrayList<FactionNewsPost>();
	private final Set<FactionInvite> factionInvitations = new HashSet<FactionInvite>();
	private final Int2ObjectMap<TreeSet<FactionNewsPost>> news = new Int2ObjectOpenHashMap<TreeSet<FactionNewsPost>>();
	private final Long2ObjectOpenHashMap<FactionRelationOffer> relationShipOffers = new Long2ObjectOpenHashMap<FactionRelationOffer>();
	private final Long2ObjectOpenHashMap<FactionRelation> relations = new Long2ObjectOpenHashMap<FactionRelation>();
	private final ArrayList<FactionRelation> relationsToAdd = new ArrayList<FactionRelation>();
	private final ArrayList<FactionRelationOffer> relationOffersToAdd = new ArrayList<FactionRelationOffer>();
	private final ObjectArrayFIFOQueue<FactionRoles> relationRolesToMod = new ObjectArrayFIFOQueue<FactionRoles>();
	private final ArrayList<FactionHomebaseChange> factionHomebaseToMod = new ArrayList<FactionHomebaseChange>();
	private final ArrayList<FactionSystemOwnerChange> factionSystemOwnerToMod = new ArrayList<FactionSystemOwnerChange>();
	private final Int2ObjectOpenHashMap<Faction> factionMap = new Int2ObjectOpenHashMap<Faction>();
	private final SendableGameState gameState;
	private final ArrayList<Faction> toAddFactions = new ArrayList<Faction>();
	private final ArrayList<Faction> toDelFactions = new ArrayList<Faction>();
	private final ArrayList<FactionRelationOfferAcceptOrDecline> toAddFactionRelationOfferAccepts = new ArrayList<FactionRelationOfferAcceptOrDecline>();
	private final ArrayList<FactionInvite> toAddFactionInvites = new ArrayList<FactionInvite>();
	private final ArrayList<FactionInvite> toDelFactionInvites = new ArrayList<FactionInvite>();
	private final ObjectArrayList<PersonalEnemyMod> toModPersonalEnemies = new ObjectArrayList<PersonalEnemyMod>();
	private final ArrayList<FactionKick> toKickMember = new ArrayList<FactionKick>();
	private final ArrayList<FactionMod> changedFactions = new ArrayList<FactionMod>();
	private final ArrayList<FactionMemberMod> changedMembersFactions = new ArrayList<FactionMemberMod>();
	private final ArrayList<FactionMemberMod> failedChangedMembersFactions = new ArrayList<FactionMemberMod>();
	private final ObjectArrayFIFOQueue<NPCFactionControlCommand> simpleCommandQueue = new ObjectArrayFIFOQueue<NPCFactionControlCommand>();
	long lastupdate;
	private boolean flagCheckFactions;
	private boolean factionInvitationsChanged;

	private boolean changedFactionAspect;
	private boolean changedFactionNewsDeletedAspect;

	private boolean factionOffersChanged;
	private Object2IntOpenHashMap<Vector3i> galaxyMapCounts = new Object2IntOpenHashMap<Vector3i>();
	private ObjectArrayFIFOQueue<FactionPointMod> toAddFactionPointMods = new ObjectArrayFIFOQueue<FactionPointMod>();
	private boolean npcFactionChanged;
	private final ObjectArrayFIFOQueue<NPCFaction> turnSchedule = new ObjectArrayFIFOQueue<NPCFaction>();
	public NPCFactionPresetManager npcFactionPresetManager;
	private byte VERSION = 0;
	private final NPCFactionNews npcFactionNews;
	public int otherFaction;
	public String otherPlayer;
	public long otherDbId;
	private IntOpenHashSet actionCheck = new IntOpenHashSet();
	public final Set<NPCFaction> markedChangedContingentFactions = new ObjectOpenHashSet<NPCFaction>();
	public ObjectOpenHashSet<PlayerState> needsSendAll = new ObjectOpenHashSet<PlayerState>();
	public ObjectOpenHashSet<NPCDiplomacy> diplomacyChanged = new ObjectOpenHashSet<NPCDiplomacy>();
	private boolean wasNpcDebugMode;
	public int currentFactionIdCreator = NPC_FACTION_START;
	private NPCFaction currentTurn;
	private long lastNPCFactionTurnUpdate;
	private Faction flagHomebaseChanged;
	public final GUIObservable obs = new GUIObservable();
	public final List<FactionChangeListener> listeners = new ObjectArrayList<>();

	public StateInterface getState(){
		return gameState.getState();
	}
	public FactionManager(SendableGameState gameState) {
		super();
		this.gameState = gameState;

		
		this.npcFactionNews = new NPCFactionNews(this);
		try {
			FactionConfig.load(gameState.getState());
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}

		if (gameState.isOnServer()) {
			npcFactionPresetManager = new NPCFactionPresetManager();
			npcFactionPresetManager.readNpcPresets();
			try {
				Tag readEntity = ((GameServerState) gameState.getState()).getController().readEntity("FACTIONS", "fac");
				this.fromTagStructure(readEntity);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EntityNotFountException e) {
				System.err.println("[SERVER] NO FACTIONS FOUND ON DISK: " + e.getMessage());
				initializeNewDefautFactionManager();
			} catch (FactionFileOutdatedException e) {
				System.err.println("[SERVER] NO FACTIONS FOUND ON DISK (outdated): " + e.getMessage());
				initializeNewDefautFactionManager();
			}

			//mini-migration. can be deleted later.
			if (!relations.containsKey(FactionRelation.getCode(PIRATES_ID, TRAIDING_GUILD_ID))) {
				relationsToAdd.add(new FactionRelation(PIRATES_ID, TRAIDING_GUILD_ID, RType.ENEMY.code));
			}
			for (int i = 0; i < FAUNA_GROUP_NEUTRAL.length; i++) {

				if (!factionMap.containsKey(FAUNA_GROUP_NEUTRAL[i])) {
					Faction fauna = new Faction(getState(), FAUNA_GROUP_NEUTRAL[i], "Neutral Fauna Fac " + i, "A Neutral Fanua Faction");
					fauna.setShowInHub(false);
					factionMap.put(fauna.getIdFaction(), fauna);
				}
			}
			for (int i = 0; i < FAUNA_GROUP_ENEMY.length; i++) {

				if (!factionMap.containsKey(FAUNA_GROUP_ENEMY[i])) {
					Faction fauna = new Faction(getState(), FAUNA_GROUP_ENEMY[i], "Enemy Fauna Fac " + i, "An Enemy Fanua Faction");
					fauna.setAttackNeutral(true);
					fauna.setShowInHub(false);
					factionMap.put(fauna.getIdFaction(), fauna);
				}
				for (Faction f : factionMap.values()) {
					if (f.getIdFaction() > 0) {
						if (!relations.containsKey(FactionRelation.getCode(f.getIdFaction(), FAUNA_GROUP_ENEMY[i]))) {
							relationsToAdd.add(new FactionRelation(f.getIdFaction(), FAUNA_GROUP_ENEMY[i], RType.ENEMY.code));
						}
					}
				}
				if (!relations.containsKey(FactionRelation.getCode(PIRATES_ID, FAUNA_GROUP_ENEMY[i]))) {
					relationsToAdd.add(new FactionRelation(PIRATES_ID, FAUNA_GROUP_ENEMY[i], RType.ENEMY.code));
				}
				if (!relations.containsKey(FactionRelation.getCode(TRAIDING_GUILD_ID, FAUNA_GROUP_ENEMY[i]))) {
					relationsToAdd.add(new FactionRelation(TRAIDING_GUILD_ID, FAUNA_GROUP_ENEMY[i], RType.ENEMY.code));
				}
			}
		}
	}

	/**
	 * @return the factionManager
	 */

	public static synchronized int getNewId() {
		return FACTION_ID_GEN++;
	}


	public void updateFactionPoints() {

		GameServerState state = (GameServerState) gameState.getState();

		galaxyMapCounts.clear();

		if (System.currentTimeMillis() - lastupdate > FactionPointsGeneralConfig.INCOME_EXPENSE_PERIOD_MINUTES * 60 * 1000) {

			long t = System.currentTimeMillis();
			System.err.println("[FACTIONMANAGER] MAKING FACTION TURN: " + (new Date(lastupdate)) + "; " + factionMap.size() + "; Turn: " + FactionPointsGeneralConfig.INCOME_EXPENSE_PERIOD_MINUTES);
			for (Faction f : factionMap.values()) {
				if (f.getIdFaction() > 0) {
					f.handleActivityServer(this);
					f.handleFactionPointGainServer(this);
					f.handleFactionPointExpensesServer(this, galaxyMapCounts);
					f.hanldeDeficit(this, galaxyMapCounts);

					f.sendFactionPointUpdate(gameState);
				}
			}

			System.err.println("[FACTIONMANAGER] faction update took: " + (System.currentTimeMillis() - t) + "ms");
			lastupdate = System.currentTimeMillis();
		}

		state.getGameState().getNetworkObject().lastFactionPointTurn.set(lastupdate);

	}

	public void addFaction(Faction faction) {
		synchronized (toAddFactions) {
			toAddFactions.add(faction);
		}
	}

	public void addFactionInvitation(FactionInvite faction) {
		synchronized (toAddFactionInvites) {
			toAddFactionInvites.add(faction);
		}
	}

	private void checkFactions() {
		for (PlayerState ps : ((GameServerState) gameState.getState()).getPlayerStatesByName().values()) {
			for (Faction f : getFactionCollection()) {
				if (f.getIdFaction() != ps.getFactionId() && f.getMembersUID().containsKey(ps.getName())) {
					f.removeMember(ps.getName(), gameState);
					if (f.getMembersUID().size() == 0) {
						if (!f.isFactionMode(Faction.MODE_FIGHTERS_FFA) && !f.isFactionMode(Faction.MODE_FIGHTERS_TEAM) && !f.isFactionMode(Faction.MODE_SPECTATORS)) {
							removeFaction(f);
						}
					}
				}
			}
		}
	}

	public boolean existsFaction(int i) {
		return factionMap.containsKey(i);
	}

	public void flagCheckFactions() {
		this.flagCheckFactions = true;
	}
	private void fromRelationshipTag(Tag t){
		Tag[] relTags = (Tag[]) t.getValue();
		for (int i = 0; i < relTags.length - 1; i++) {
			FactionRelation relation = new FactionRelation();
			relation.fromTagStructure(relTags[i]);

			if (relation.a != relation.b) {
				if ((relation.a > 0 && !existsFaction(relation.a)) || (relation.b > 0 && !existsFaction(relation.b))) {
					System.err.println("[SERVER][FACTION][WARNING] not adding faction relation for non existent faction: " + relation);
				} else {
					relations.put(relation.getCode(), relation);
				}
			} else {
				System.err.println("[SERVER][FACTION][WARNING] not adding self-relation");
			}
		}
	}
	private void fromRelationshipOfferTag(Tag t){
		Tag[] relOfferTags = (Tag[]) t.getValue();
		for (int i = 0; i < relOfferTags.length - 1; i++) {
			FactionRelationOffer relationOffer = new FactionRelationOffer();
			relationOffer.fromTagStructure(relOfferTags[i]);
			if ((relationOffer.a > 0 && !existsFaction(relationOffer.a)) || (relationOffer.b > 0 && !existsFaction(relationOffer.b))) {
				System.err.println("[SERVER][FACTION][WARNING] not adding faction relation offer for non existent faction: " + relationOffer);
			} else {
				System.err.println("[SERVER][FACION][TAG] loaded relation offer " + relationOffer);
				relationShipOffers.put(relationOffer.getCode(), relationOffer);
			}
		}
	}
	private void fromInvitesTag(Tag t){
		Tag[] inviteTags = (Tag[]) t.getValue();
		for (int i = 0; i < inviteTags.length - 1; i++) {
			FactionInvite invite = new FactionInvite();
			invite.fromTagStructure(inviteTags[i]);
			Faction faction = factionMap.get(invite.getFactionUID());
			if (faction != null) {
				factionInvitations.add(invite);
			}
		}
	}
	private void fromNewsTag(Tag t){
		Tag[] newsTags = (Tag[]) t.getValue();
		for (int i = 0; i < newsTags.length - 1; i++) {
			if (newsTags[i].getType() == Type.STRUCT) {
				Tag[] posts = (Tag[]) newsTags[i].getValue();
				//					System.err.println("[SERVER][FACION][TAG] PARSING: "+newsTags[i].getName());
				for (int j = 0; j < posts.length - 1; j++) {
					FactionNewsPost nPost = new FactionNewsPost();
					nPost.fromTagStructure(posts[j]);
					Faction faction = factionMap.get(nPost.getFactionId());
					if (faction != null && !faction.isNPC()) {
						TreeSet<FactionNewsPost> treeSet = news.get(faction.getIdFaction());
						if (treeSet == null) {
							treeSet = new TreeSet();
							news.put(faction.getIdFaction(), treeSet);
						}
						//							System.err.println("[SERVER][FACION][TAG] adding news post "+nPost);
						treeSet.add(nPost);
					}
				}
			} else {
				//					System.err.println("[SERVER][FACION][TAG] NO NEWS POSTS");
			}
		}
		printNewsStats();
		
	}
	private void printNewsStats() {
		System.err.println("[SERVER][FACTION] News Statistics:");
		for(it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<TreeSet<FactionNewsPost>> e : news.int2ObjectEntrySet()){
			System.err.println("[SERVER][FACTION][NEWS] "+getFaction(e.getIntKey())+": Amount: "+e.getValue().size());
		}
	}

	public void diplomacyAction(DiplActionType type, int toFaction, long otherDbId){
		Faction faction = factionMap.get(toFaction);
		if(faction instanceof NPCFaction){
			((NPCFaction)faction).diplomacyAction(type, otherDbId);
		}
	}
	public static Tag getNPCFactionConfigTagTag(Collection<Faction> all){
		List<NPCFaction> map = new ObjectArrayList<NPCFaction>();
		
		for(Faction f : all){
			if(f instanceof NPCFaction){
				map.add(((NPCFaction)f));
			}
		}
		
		Tag[] t = new Tag[map.size()+1];
		t[t.length-1] = FinishTag.INST;
		
		
		for(int i = 0; i < map.size(); i++){
			NPCFaction e = map.get(i);
			
			Tag l = new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.INT, null, e.getIdFaction()),
				e.getConfig().toTagStructure(),
				FinishTag.INST
			});
			t[i] = l;
		}
		
		return new Tag(Type.STRUCT, null, t);
	}
	public static Int2ObjectOpenHashMap<NPCFactionConfig> fromTagConfigs(Tag t, NPCFactionPresetManager p) throws IllegalArgumentException, IllegalAccessException, ConfigParserException{
		Int2ObjectOpenHashMap<NPCFactionConfig> mm = new Int2ObjectOpenHashMap<NPCFactionConfig>();
		Tag[] struct = t.getStruct();
		
		for(int i = 0; i < struct.length-1; i++){
			Tag[] ent = struct[i].getStruct();
			int factionId = ent[0].getInt();
			NPCFactionConfig c = new NPCFactionConfig();
			c.fromTagStructure(ent[1], p);
			c.initialize();
			mm.put(factionId, c);
		}
		return mm;
	}
	@Override
	public void fromTagStructure(Tag tag) {
		
		if(factionVersion2.equals(tag.getName())){
			Int2ObjectOpenHashMap<NPCFactionConfig> parsedConfigs = new Int2ObjectOpenHashMap<NPCFactionConfig>();
			
			
			Tag[] subs = (Tag[]) tag.getValue();

			byte version = subs[0].getByte();
			
			
			
			
			npcFactionPresetManager.fromTagStructure(subs[1]);
			
			Int2ObjectOpenHashMap<NPCFactionConfig> fromTagConfigs = new Int2ObjectOpenHashMap<NPCFactionConfig>();
			try {
				fromTagConfigs = fromTagConfigs(subs[2], npcFactionPresetManager);
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (ConfigParserException e1) {
				e1.printStackTrace();
			}
					
			Tag[] facs = subs[3].getStruct();
			int highest = FACTION_ID_GEN;
			for (int i = 0; i < facs.length - 1; i++) {
				
				Faction f;
				Tag fifi = facs[i];
				Tag fac;
				int id = ((Tag[])fifi.getValue())[0].getInt();
				fac = fifi.getStruct()[1];
				String name = fac.getStruct()[1].getString();
				if(isNPCFaction(id)){
					f = new NPCFaction(getState(), id);
					f.setName(name);
					NPCFactionConfig npcFactionConfig = fromTagConfigs.get(id);
					assert(npcFactionConfig != null):id+"; ::: "+fromTagConfigs;
					assert(npcFactionConfig.getPreset() != null);
					npcFactionConfig.generate(((NPCFaction)f));
					((NPCFaction)f).setConfig(npcFactionConfig);
					
					
					assert(((NPCFaction)f).getConfig() != null);
					assert(((NPCFaction)f).getConfig() == npcFactionConfig);
					assert(((NPCFaction)f).getConfig().getPreset() != null);
				}else{
					f = new Faction(getState());
					f.setName(name);
				}
				try {
					f.initializeWithState((GameServerState) gameState.getState());
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
				f.fromTagStructure(fac);
				f.setHomeParamsFromUID((GameServerState) gameState.getState());
				assert (!factionMap.containsKey(f.getIdFaction()));
				if (!f.getMembersUID().isEmpty() || f.getIdFaction() < 0) {

					factionMap.put(f.getIdFaction(), f);
					if(f instanceof NPCFaction){
						Inventory put = gameState.getInventories().put(f.getIdFaction(), ((NPCFaction) f).getInventory());
						assert(put == null);
						System.err.println("[SERVER][FACTION] LOADED NPC INV: "+(f)+" "+((NPCFaction) f).getInventory());
					}
					highest = Math.max(highest, f.getIdFaction());
				} else {
					System.err.println("[SERVER][FACTION] not adding empty faction: " + f + ": " + f.getMembersUID());
				}
				assert(!(f instanceof NPCFaction) || ((NPCFaction)f).getConfig() != null):f;
				assert(!(f instanceof NPCFaction) || ((NPCFaction)f).getConfig().getPreset() != null):f;
				f.initialize();
			}

			FACTION_ID_GEN = highest + 1; //one over highest or the latest faction might get overwritten

			fromInvitesTag(subs[4]);
			fromNewsTag(subs[5]);
			fromRelationshipTag(subs[6]);
			fromRelationshipOfferTag(subs[7]);
			lastupdate = (Long) subs[8].getValue();
			npcFactionNews.fromTag(subs[9]);
			if(subs[10].getType() != Type.FINISH){//can be removed in any release
				currentFactionIdCreator = subs[10].getInt();
			}
		}else if (factionVersion0.equals(tag.getName()) || factionVersion1.equals(tag.getName())) {
			
			parseOldVersion(tag);

		} else {
			throw new FactionFileOutdatedException();
		}
	}

	@Override
	public Tag toTagStructure() {
	
		Tag[] factionTags = null;
		int i = 0;
	
		Tag[] newstags = null;
		
		List<Faction> f = new ObjectArrayList<Faction>();
		
		synchronized (factionMap) {
			f.addAll(factionMap.values());
		}
		factionTags = new Tag[factionMap.size() + 1];
		newstags = new Tag[factionMap.size() + 1];
		for (Faction faction : f) {
			
			factionTags[i] = new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.INT, null, faction.getIdFaction()),
					faction.toTagStructure(),
					FinishTag.INST})
					;
			
			
			TreeSet<FactionNewsPost> treeSet = news.get(faction.getIdFaction());
			if (treeSet == null || faction.isNPC()) {
				newstags[i] = new Tag(Type.BYTE, "0FN", (byte) 0);
			} else {
				newstags[i] = Tag.listToTagStruct(treeSet, "FN");
			}
			i++;
		}
		factionTags[factionMap.size()] = FinishTag.INST;
		newstags[factionMap.size()] = FinishTag.INST;
		
		Tag[] relationsTagArray = null;
		synchronized (relations) {
			i = 0;
			relationsTagArray = new Tag[relations.size() + 1];
			for (FactionRelation r : relations.values()) {
				relationsTagArray[i] = r.toTagStructure();
				i++;
			}
			relationsTagArray[relations.size()] = FinishTag.INST;
		}
	
		Tag[] relationsOfferTagArray = null;
		synchronized (relationShipOffers) {
			i = 0;
			relationsOfferTagArray = new Tag[relationShipOffers.size() + 1];
			for (FactionRelationOffer r : relationShipOffers.values()) {
				relationsOfferTagArray[i] = r.toTagStructure();
				i++;
			}
			relationsOfferTagArray[relationShipOffers.size()] = FinishTag.INST;
		}
	
		Tag[] inviteTags = null;
		synchronized (factionInvitations) {
			inviteTags = new Tag[factionInvitations.size() + 1];
			i = 0;
			for (FactionInvite faction : factionInvitations) {
				inviteTags[i] = faction.toTagStructure();
				i++;
			}
			inviteTags[factionInvitations.size()] = FinishTag.INST;
		}
	
		Tag newsTag = new Tag(Type.STRUCT, "NStruct", newstags);
		Tag factions = new Tag(Type.STRUCT, null, factionTags);
		Tag invites = new Tag(Type.STRUCT, null, inviteTags);
		Tag relationTag = new Tag(Type.STRUCT, null, relationsTagArray);
		Tag relationOfferTag = new Tag(Type.STRUCT, null, relationsOfferTagArray);
		Tag lastUpdate = new Tag(Type.LONG, null, lastupdate);
	
		
		return new Tag(Type.STRUCT, factionVersion2, new Tag[]{
				new Tag(Type.BYTE, null, VERSION), 
				npcFactionPresetManager.toTagStructure(), 
				getNPCFactionConfigTagTag(f),
				factions, 
				invites, 
				newsTag, 
				relationTag, 
				relationOfferTag, 
				lastUpdate, 
				npcFactionNews.toTag(),
				new Tag(Type.INT, null, currentFactionIdCreator),
				FinishTag.INST});
	}
	private void parseOldVersion(Tag tag){
		Tag[] subs = (Tag[]) tag.getValue();

		Tag[] facs = (Tag[]) subs[0].getValue();
		int highest = FACTION_ID_GEN;
		for (int i = 0; i < facs.length - 1; i++) {
			
			Faction f;
			Tag fifi = facs[i];
			Tag fac;
			if(factionVersion1.equals(tag.getName())){
				int id = (Integer)((Tag[])fifi.getValue())[0].getValue();
				fac = ((Tag[])fifi.getValue())[1];
				
				if(isNPCFaction(id)){
					//skip on old version
					continue;
				}else{
					f = new Faction(getState());
				}
			}else{
				fac = fifi;
				f = new Faction(getState());
			}
			try {
				f.initializeWithState((GameServerState) gameState.getState());
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
			f.fromTagStructure(fac);
			f.setHomeParamsFromUID((GameServerState) gameState.getState());
			assert (!factionMap.containsKey(f.getIdFaction()));
			if (!f.getMembersUID().isEmpty() || f.getIdFaction() < 0) {

				factionMap.put(f.getIdFaction(), f);
				if(f instanceof NPCFaction){
					((GameStateInterface)getState()).getGameState().getInventories().put(f.getIdFaction(), ((NPCFaction) f).getInventory());
				}
				highest = Math.max(highest, f.getIdFaction());
			} else {
				System.err.println("[SERVER][FACTION] not adding empty faction: " + f + ": " + f.getMembersUID());
			}
			f.initialize();
		}

		FACTION_ID_GEN = highest + 1; //one over highest or the latest faction might get overwritten

		fromInvitesTag(subs[1]);
		fromNewsTag(subs[2]);
		fromRelationshipTag(subs[3]);
		fromRelationshipOfferTag(subs[4]);

		if (subs[5].getType() != Type.FINISH) {
			lastupdate = (Long) subs[5].getValue();
		}
	}
	public Faction getFaction(int i) {
		return factionMap.get(i);
	}

	public Collection<Faction> getFactionCollection() {
		return factionMap.values();
	}

	public Set<FactionInvite> getFactionInvitations() {
		return factionInvitations;
	}

	/**
	 * @return the factionMap
	 */
	public Int2ObjectOpenHashMap<Faction> getFactionMap() {
		return factionMap;
	}

	public SendableGameState getGameState() {
		return gameState;
	}

	/**
	 * @return the news
	 */
	public Int2ObjectMap<TreeSet<FactionNewsPost>> getNews() {
		return news;
	}

	public RType getRelation(String playerNameInAnyCase, int from, int to) {
		Faction factionB = getFaction(to);
		if (from != to && factionB != null) {
			if (factionB.getPersonalEnemies().contains(playerNameInAnyCase.toLowerCase(Locale.ENGLISH))) {
				return FactionRelation.RType.ENEMY;
			}
		}
		return getRelation(from, to);
	}

	public RType getRelation(int a, int b) {
		if (a == 0 && b == 0) {
			return FactionRelation.RType.NEUTRAL;
		}
		if (a != 0 && b == 0) {
			Faction faction = getFaction(a);
			if (faction != null) {
				if (faction.isFactionMode(Faction.MODE_SPECTATORS)) {
					return FactionRelation.RType.NEUTRAL;
				}
				if (faction.isAttackNeutral()) {
					return FactionRelation.RType.ENEMY;
				} else if (faction.isAllyNeutral()) {
					return FactionRelation.RType.FRIEND;
				} else {
					return FactionRelation.RType.NEUTRAL;
				}
			} else {
				return FactionRelation.RType.NEUTRAL;
			}
		} else if (b != 0 && a == 0) {
			Faction faction = getFaction(b);
			if (faction != null) {
				if (faction.isFactionMode(Faction.MODE_SPECTATORS)) {
					return FactionRelation.RType.NEUTRAL;
				}
				if (faction.isAttackNeutral()) {
					return FactionRelation.RType.ENEMY;
				} else if (faction.isAllyNeutral()) {
					return FactionRelation.RType.FRIEND;
				} else {
					return FactionRelation.RType.NEUTRAL;
				}
			} else {
				return FactionRelation.RType.NEUTRAL;
			}
		} else if (a == b) {
			if ((getFaction(a) != null && getFaction(a).isFactionMode(Faction.MODE_FIGHTERS_FFA))) {
				return FactionRelation.RType.ENEMY;
			}
			return FactionRelation.RType.FRIEND;
		}
		if ((getFaction(a) != null && getFaction(a).isFactionMode(Faction.MODE_SPECTATORS)) || (getFaction(b) != null && getFaction(b).isFactionMode(Faction.MODE_SPECTATORS))) {
			return FactionRelation.RType.NEUTRAL;
		}
		long code = FactionRelation.getCode(a, b);
		if (!relations.containsKey(code)) {
			return FactionRelation.RType.NEUTRAL;
		} else {
			return relations.get(code).getRelation();
		}
	}

	public RType getRelation(SimpleTransformableSendableObject a, SimpleTransformableSendableObject b) {
		int aId = 0;
		int bId = 0;

		if (a instanceof PlayerControllable && !((PlayerControllable) a).getAttachedPlayers().isEmpty()) {
			aId = ((PlayerControllable) a).getAttachedPlayers().get(0).getFactionId();
		} else {
			aId = a.getFactionId();
		}

		if (b instanceof PlayerControllable && !((PlayerControllable) b).getAttachedPlayers().isEmpty()) {
			bId = ((PlayerControllable) b).getAttachedPlayers().get(0).getFactionId();

		} else {
			bId = b.getFactionId();
		}

		if (a.getOwnerState() != null && existsFaction(bId) && getFaction(bId).getPersonalEnemies().contains(a.getOwnerState().getName().toLowerCase(Locale.ENGLISH))) {
			return RType.ENEMY;
		}
		if (b.getOwnerState() != null && existsFaction(aId) && getFaction(aId).getPersonalEnemies().contains(b.getOwnerState().getName().toLowerCase(Locale.ENGLISH))) {
			return RType.ENEMY;
		}

		return getRelation(aId, bId);

	}

	/**
	 * @return the relationShipOffers
	 */
	public Long2ObjectOpenHashMap<FactionRelationOffer> getRelationShipOffers() {
		return relationShipOffers;
	}

	@Override
	public String getUniqueIdentifier() {
		return "FACTIONS";
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	public void initFromNetworkObject(NetworkGameState networkObject) {

		//		updateFromNetworkObject(networkObject);

		//normal update update is autoperformed after init
		
		npcFactionNews.initFromNetworkObject(networkObject);
	}

	public void initializeNewDefautFactionManager() {
		Faction pirates = new Faction(getState(), PIRATES_ID, Lng.str("Pirates"), Lng.str("Ravaging space pirates"));
		pirates.setAttackNeutral(true);

		Faction tradingGuild = new Faction(getState(), TRAIDING_GUILD_ID, Lng.str("Trading guild"), Lng.str("The intergalactic trading guild"));

		factionMap.put(tradingGuild.getIdFaction(), tradingGuild);
		factionMap.put(pirates.getIdFaction(), pirates);

		relationsToAdd.add(new FactionRelation(PIRATES_ID, TRAIDING_GUILD_ID, RType.ENEMY.code));
	}

	private void initializeRelationShips(Faction faction) {
		if (faction.getIdFaction() != PIRATES_ID) {
			relationsToAdd.add(new FactionRelation(PIRATES_ID, faction.getIdFaction(), RType.ENEMY.code));
		}
		for (int i = 0; i < FAUNA_GROUP_ENEMY.length; i++) {
			if (faction.getIdFaction() != FAUNA_GROUP_ENEMY[i]) {
				relationsToAdd.add(new FactionRelation(FAUNA_GROUP_ENEMY[i], faction.getIdFaction(), RType.ENEMY.code));
			}
		}
	}

	public boolean isEnemy(int a, AbstractOwnerState b) {
		long code = FactionRelation.getCode(a, b.getFactionId());
		if (a == 0) {
			Faction faction = getFaction(b.getFactionId());
			return faction != null && faction.isAttackNeutral();
		}
		if (b.getFactionId() == 0) {
			Faction faction = getFaction(a);
			return faction != null && (faction.isAttackNeutral() || faction.getPersonalEnemies().contains(b.getName().toLowerCase(Locale.ENGLISH)));
		} else if (a == b.getFactionId()) {
			return false;
		}

		return relations.containsKey(code) && relations.get(code).isEnemy();
	}

	public boolean isEnemy(int a, int b) {
		long code = FactionRelation.getCode(a, b);

		if (a == 0 || b == 0) {
			Faction faction = getFaction(a == 0 ? b : a);
			return faction != null && faction.isAttackNeutral();
		} else if (a == b) {
			return false;
		}

		return relations.containsKey(code) && relations.get(code).isEnemy();
	}

	//	public boolean isEnemy(int factionId, SimpleTransformableSendableObject o) {
	//		if(o instanceof PlayerControllable && !((PlayerControllable)o).getAttachedPlayers().isEmpty() ){
	//			return isEnemy(factionId, ((PlayerControllable)o).getAttachedPlayers().get(0).getFactionId());
	//		}else{
	//			return isEnemy(factionId, o.getFactionId());
	//		}
	//	}
	public boolean isEnemy(SimpleTransformableSendableObject a, SimpleTransformableSendableObject b) {
		PlayerState pA = null;
		PlayerState pB = null;
		Faction fA = null;
		Faction fB = null;
		if (a instanceof PlayerControllable && !((PlayerControllable) a).getAttachedPlayers().isEmpty()) {
			pA = ((PlayerControllable) a).getAttachedPlayers().get(0);
		}
		if (b instanceof PlayerControllable && !((PlayerControllable) b).getAttachedPlayers().isEmpty()) {
			pB = ((PlayerControllable) b).getAttachedPlayers().get(0);
		}

		if (pA != null) {
			fA = getFaction(pA.getFactionId());
		}
		if (pB != null) {
			fB = getFaction(pB.getFactionId());
		}
		if (fA == null) {
			fA = getFaction(a.getFactionId());
		}
		if (fB == null) {
			fB = getFaction(b.getFactionId());
		}
		//		if(pA != null || pB != null){
		//			System.err.println("CHECK: "+a+"; "+fA+"; "+pA+"; ---- "+b+"; "+fB+"; "+pB);
		//		}

		if (fB != null && fB != null && fA == fB) {
			return false;
		}
		if (fA != null && pB != null && fA.getPersonalEnemies().contains(pB.getName().toLowerCase(Locale.ENGLISH))) {
			return true;
		}
		if (fB != null && pA != null && fB.getPersonalEnemies().contains(pA.getName().toLowerCase(Locale.ENGLISH))) {
			return true;
		}
		return getRelation(a, b) == RType.ENEMY;

	}

	public boolean isFriend(int a, int b) {
		long code = FactionRelation.getCode(a, b);
		if (a == 0 || b == 0) {
			Faction faction = getFaction(a == 0 ? b : a);
			return faction != null && faction.isAllyNeutral();
		} else if (a == b) {
			return true;
		}
		return relations.containsKey(code) && relations.get(code).isFriend();
	}

	public boolean isFriend(SimpleTransformableSendableObject a, SimpleTransformableSendableObject b) {
		return getRelation(a, b) == RType.FRIEND;
	}

	public boolean isInSameFaction(FactionInterface a, FactionInterface b) {
		return a.getFactionId() == b.getFactionId();
	}

	public boolean isNeutral(int a, int b) {
		long code = FactionRelation.getCode(a, b);
		return !relations.containsKey(code) || relations.get(code).isNeutral();
	}

	public boolean isNeutral(SimpleTransformableSendableObject a, SimpleTransformableSendableObject b) {
		return getRelation(a, b) == RType.NEUTRAL;
	}

	/**
	 * @return the relationOffersToAdd
	 */
	public void relationShipOfferServer(FactionRelationOffer a) {
		synchronized (relationOffersToAdd) {
			relationOffersToAdd.add(a);
		}
	}

	public void removeFaction(Faction faction) {
		synchronized (toDelFactions) {
			toDelFactions.add(faction);
		}
	}

	public void removeFaction(int code) throws FactionNotFoundException {
		Faction faction = factionMap.get(code);
		
		
		if (faction != null) {
			if(isOnServer()){
				for(Galaxy g : ((GameServerState)getState()).getUniverse().getGalaxies()){
					g.getNpcFactionManager().removeFaction(faction);
				}
			}
			removeFaction(faction);
		} else {
			throw new FactionNotFoundException(code);
		}
	}

	public void removeFactionInvitation(FactionInvite faction) {
		synchronized (toDelFactionInvites) {
			toDelFactionInvites.add(faction);
		}
	}

	public void removeFactionInvitationClient(FactionInvite invite) {
		gameState.getNetworkObject().factionInviteDel.add(new RemoteFactionInvitation(invite, gameState.getNetworkObject()));
	}

	public void removeMemberOfFaction(int factionId, PlayerState playerState) {
		assert (gameState.isOnServer());
		synchronized (changedMembersFactions) {
			changedMembersFactions.add(new FactionMemberMod(factionId, playerState.getName(), false));
		}
	}

	public void sendFactionRoles(FactionRoles roles) {
		//		System.err.println("[FACTIONMANAGER] sending faction roles on "+getGameState().getState());
		gameState.getNetworkObject().factionRolesBuffer.add(new RemoteFactionRoles(roles, gameState.getNetworkObject()));
	}

	public void sendClientHomeBaseChange(String initiator, int faction, String entityUID) {
		RemoteStringArray a = new RemoteStringArray(7, gameState.getNetworkObject());

		a.set(0, initiator);
		a.set(1, String.valueOf(faction));
		a.set(2, entityUID);

		gameState.getNetworkObject().factionHomeBaseChangeBuffer.add(a);
	}

	public void sendHomeBaseChange(String initiator, int faction, String entityUID, Vector3i homeSector, String realName) {
		RemoteStringArray a = new RemoteStringArray(7, gameState.getNetworkObject());

		a.set(0, new String(initiator));
		a.set(1, String.valueOf(faction));
		a.set(2, new String(entityUID));
		a.set(3, String.valueOf(homeSector.x));
		a.set(4, String.valueOf(homeSector.y));
		a.set(5, String.valueOf(homeSector.z));
		a.set(6, new String(realName));

		gameState.getNetworkObject().factionHomeBaseChangeBuffer.add(a);
	}

	public void sendClientSystemOwnerChange(String initiator, int factionId, SimpleTransformableSendableObject<?> obj) {
		
		Vector3i clientSector = obj.getClientSector();
		
		if(clientSector != null){
			
			
			FactionSystemOwnerChange c = 
					new FactionSystemOwnerChange(initiator, 
							factionId, 
							obj.getUniqueIdentifier(), 
							clientSector, 
							VoidSystem.getContainingSystem(clientSector, new Vector3i()), 
							obj.getRealName());
			gameState.getNetworkObject().factionClientSystemOwnerChangeBuffer.add(new RemoteSystemOwnershipChange(c, gameState.getNetworkObject()));
		}

		
	}

	public void sendInviteClient(FactionInvite invite) {
		gameState.getNetworkObject().factionInviteAdd.add(new RemoteFactionInvitation(invite, gameState.getNetworkObject()));
	}

	public void sendPersonalEnemyAdd(String initiator, Faction f, String enemyPlayerName) {
		RemoteStringArray a = new RemoteStringArray(3, gameState.getNetworkObject());
		a.set(0, String.valueOf(f.getIdFaction()));
		a.set(1, String.valueOf(initiator));
		a.set(2, String.valueOf(enemyPlayerName));
		gameState.getNetworkObject().personalElemiesAdd.add(a);
	}

	public void sendPersonalEnemyRemove(String initiator, Faction f, String enemyPlayerName) {
		RemoteStringArray a = new RemoteStringArray(3, gameState.getNetworkObject());
		a.set(0, String.valueOf(f.getIdFaction()));
		a.set(1, String.valueOf(initiator));
		a.set(2, String.valueOf(enemyPlayerName));
		gameState.getNetworkObject().personalElemiesDel.add(a);
	}

	public void sendRelationshipAccept(String initiator, FactionRelationOffer offer, boolean accept) {
		RemoteStringArray s = new RemoteStringArray(3, gameState.getNetworkObject());
		s.set(0, initiator);
		s.set(1, String.valueOf(offer.getCode()));
		s.set(2, String.valueOf(accept));
		gameState.getNetworkObject().factionRelationshipAcceptBuffer.add(s);
	}

	public void sendRelationshipOffer(String initiator, int from, int to, byte relation, String message, boolean revoke) {
		FactionRelationOffer offer = new FactionRelationOffer();
		offer.set(initiator, from, to, relation, message, revoke);

		gameState.getNetworkObject().factionRelationshipOffer.add(offer.getRemoteArrayOffer(gameState.getNetworkObject()));
	}

	public void setAllRelations(byte rel) {
		for (int a : factionMap.keySet()) {
			for (int b : factionMap.keySet()) {
				if (a > 0 && b > 0 && a != b) {
					setRelationServer(a, b, rel);
				}
			}
		}
	}

	public void setEnemy(FactionInterface a, FactionInterface b) {
		long code = FactionRelation.getCode(a.getFactionId(), b.getFactionId());
		if (!relations.containsKey(code)) {
			FactionRelation r = new FactionRelation();
			r.set(a.getFactionId(), b.getFactionId());
			relations.put(code, r);
		}
		relations.get(code).setEnemy();
	}

	public void setFriend(FactionInterface a, FactionInterface b) {
		long code = FactionRelation.getCode(a.getFactionId(), b.getFactionId());
		if (!relations.containsKey(code)) {
			FactionRelation r = new FactionRelation();
			r.set(a.getFactionId(), b.getFactionId());
			relations.put(code, r);
		}
		relations.get(code).setFriend();
	}

	public void setNeutral(FactionInterface a, FactionInterface b) {
		long code = FactionRelation.getCode(a.getFactionId(), b.getFactionId());
		if (!relations.containsKey(code)) {
			FactionRelation r = new FactionRelation();
			r.set(a.getFactionId(), b.getFactionId());
			relations.put(code, r);
		}
		relations.get(code).setNeutral();
	}

	public void setRelationServer(int a, int b, byte rel) {
		synchronized (relationsToAdd) {
			relationsToAdd.add(new FactionRelation(a, b, rel));
		}
	}

	@Override
	public String toString() {
		return factionMap.values().toString();
	}

	public void updateFromNetworkObject(NetworkGameState networkObject) {

		npcFactionNews.updateFromNetworkObject(networkObject);
		
		for (int i = 0; i < networkObject.factionNewsPosts.getReceiveBuffer().size(); i++) {
			RemoteFactionNewsPost news = networkObject.factionNewsPosts.getReceiveBuffer().get(i);
			synchronized (toAddFactionNewsPosts) {
				//				System.err.println("[FACTIONMANAGER] received news on "+getGameState().getState()+": "+news.get());
				toAddFactionNewsPosts.add(news.get());
			}
		}
		
		for (int i = 0; i < networkObject.simpleCommandQueue.getReceiveBuffer().size(); i++) {
			SimpleCommand<?> r = networkObject.simpleCommandQueue.getReceiveBuffer().get(i).get();
			synchronized (simpleCommandQueue) {
				simpleCommandQueue.enqueue((NPCFactionControlCommand) r);
			}
		}
		
		for (int i = 0; i < networkObject.factionInviteAdd.getReceiveBuffer().size(); i++) {
			RemoteFactionInvitation remoteFaction = networkObject.factionInviteAdd.getReceiveBuffer().get(i);
			synchronized (toAddFactionInvites) {
				//				System.err.println("[FactionManager] Received Faction Invite on "+getGameState().getState());
				toAddFactionInvites.add(remoteFaction.get());
			}
		}
		for (int i = 0; i < networkObject.factionPointMod.getReceiveBuffer().size(); i++) {
			RemoteFactionPointUpdate r = networkObject.factionPointMod.getReceiveBuffer().get(i);
			synchronized (toAddFactionPointMods) {
				if (gameState.isOnServer()) {
					throw new RuntimeException();
				}
				//				System.err.println("[FactionManager] Received Faction Invite on "+getGameState().getState());
				toAddFactionPointMods.enqueue(r.get());
			}
		}
		for (int i = 0; i < networkObject.personalElemiesAdd.getReceiveBuffer().size(); i++) {
			RemoteStringArray r = networkObject.personalElemiesAdd.getReceiveBuffer().get(i);
			synchronized (toModPersonalEnemies) {
				toModPersonalEnemies.add(new PersonalEnemyMod(r.get(1).get(), r.get(2).get(), Integer.parseInt(r.get(0).get()), true));
			}
		}
		for (int i = 0; i < networkObject.personalElemiesDel.getReceiveBuffer().size(); i++) {
			RemoteStringArray r = networkObject.personalElemiesDel.getReceiveBuffer().get(i);
			synchronized (toModPersonalEnemies) {
				toModPersonalEnemies.add(new PersonalEnemyMod(r.get(1).get(), r.get(2).get(), Integer.parseInt(r.get(0).get()), false));
			}
		}

		for (int i = 0; i < networkObject.factionRelationshipAcceptBuffer.getReceiveBuffer().size(); i++) {
			RemoteStringArray remoteFaction = networkObject.factionRelationshipAcceptBuffer.getReceiveBuffer().get(i);
			synchronized (toAddFactionRelationOfferAccepts) {
				//				System.err.println("[FactionManager] Received Faction RelationShip offer accept on "+getGameState().getState());
				String initiator = remoteFaction.get(0).get();
				long code = Long.parseLong(remoteFaction.get(1).get());
				boolean accept = Boolean.parseBoolean(remoteFaction.get(2).get());
				synchronized (toAddFactionRelationOfferAccepts) {
					toAddFactionRelationOfferAccepts.add(new FactionRelationOfferAcceptOrDecline(initiator, code, accept));
				}

			}
		}

		for (int i = 0; i < networkObject.factionkickMemberRequests.getReceiveBuffer().size(); i++) {
			RemoteStringArray s = networkObject.factionkickMemberRequests.getReceiveBuffer().get(i);
			synchronized (toKickMember) {
				System.err.println("[FactionManager] Received Faction Kick on " + gameState.getState());
				toKickMember.add(new FactionKick(s.get(2).get(), Integer.parseInt(s.get(1).get()), s.get(0).get()));
			}
		}
		for (int i = 0; i < networkObject.factionInviteDel.getReceiveBuffer().size(); i++) {
			RemoteFactionInvitation remoteFaction = networkObject.factionInviteDel.getReceiveBuffer().get(i);
			synchronized (toDelFactionInvites) {
				System.err.println("[FactionManager] Received Faction Invite DELETE on " + gameState.getState());
				toDelFactionInvites.add(remoteFaction.get());
			}
		}
		for (int i = 0; i < networkObject.factionMod.getReceiveBuffer().size(); i++) {
			RemoteField<String>[] remoteFaction = networkObject.factionMod.getReceiveBuffer().get(i).get();
			String initiator = remoteFaction[0].get();
			int factionId = Integer.parseInt(remoteFaction[1].get());
			String option = remoteFaction[2].get();
			String setting = remoteFaction[3].get();

			FactionMod mod = new FactionMod();
			mod.initiator = initiator;
			mod.factionId = factionId;
			mod.option = option;
			mod.setting = setting;

			synchronized (changedFactions) {
				changedFactions.add(mod);
			}

		}

		for (int i = 0; i < networkObject.factionAdd.getReceiveBuffer().size(); i++) {
			RemoteFaction remoteFaction = networkObject.factionAdd.getReceiveBuffer().get(i);
			//			System.err.println("[FACTION] received faction: "+remoteFaction.get());
			addFaction(remoteFaction.get());
		}
		for (int i = 0; i < networkObject.factionDel.getReceiveBuffer().size(); i++) {
			RemoteFaction remoteFaction = networkObject.factionDel.getReceiveBuffer().get(i);
			removeFaction(remoteFaction.get());
		}

		for (int i = 0; i < networkObject.factionMemberMod.getReceiveBuffer().size(); i++) {
			RemoteField<String>[] remoteFields = networkObject.factionMemberMod.getReceiveBuffer().get(i).get();

			String playerName = remoteFields[0].get();
			int factionId = Integer.parseInt(remoteFields[1].get());
			String permissionOrRemove = remoteFields[2].get();
			String initiator = remoteFields[3].get();
			long lastActiveTime = Long.parseLong(remoteFields[4].get());

			FactionMemberMod mod = new FactionMemberMod();

			if (permissionOrRemove.equals("r")) {
				mod.addOrMod = false;
				mod.id = factionId;
				mod.playerState = playerName;
				mod.initiator = initiator;

			} else {
				byte permission = Byte.parseByte(permissionOrRemove);
				mod.addOrMod = true;
				mod.id = factionId;
				mod.playerState = playerName;
				mod.permissions = permission;
				mod.initiator = initiator;
				mod.lastActiveTime = lastActiveTime;
			}
			synchronized (changedMembersFactions) {
				changedMembersFactions.add(mod);
			}
		}
		for (int i = 0; i < networkObject.factionRelationships.getReceiveBuffer().size(); i++) {
			RemoteField<Integer>[] rf = networkObject.factionRelationships.getReceiveBuffer().get(i).get();
			int fromId = rf[0].get();
			int toId = rf[1].get();
			int relationship = rf[2].get();
			if (fromId != toId) {
				FactionRelation relation = new FactionRelation();
				relation.set(fromId, toId);
				relation.setRelation((byte) relationship);
				//			System.err.println("[FACTIONMANAGER] received faction relation on "+getGameState().getState()+": "+relation);

				synchronized (relationsToAdd) {
					relationsToAdd.add(relation);
				}
			} else {
				System.err.println("[FACTION] Exception: received relationship with self: " + fromId);
			}
		}

		for (int i = 0; i < networkObject.factionRelationshipOffer.getReceiveBuffer().size(); i++) {
			RemoteField<String>[] rf = networkObject.factionRelationshipOffer.getReceiveBuffer().get(i).get();
			int fromId = Integer.parseInt(rf[0].get());
			int toId = Integer.parseInt(rf[1].get());
			int relationship = Integer.parseInt(rf[2].get());
			String message = rf[3].get();
			String initiator = rf[4].get();
			boolean revoke = Boolean.parseBoolean(rf[5].get());

			FactionRelationOffer relation = new FactionRelationOffer();
			relation.set(initiator, fromId, toId, (byte) relationship, message, revoke);
			//			System.err.println("[FACTIONMANAGER] received faction relation offer on "+getGameState().getState()+" "+relation);
			synchronized (relationOffersToAdd) {
				relationOffersToAdd.add(relation);
			}
		}
		for (int i = 0; i < networkObject.factionHomeBaseChangeBuffer.getReceiveBuffer().size(); i++) {
			RemoteStringArray a = networkObject.factionHomeBaseChangeBuffer.getReceiveBuffer().get(i);

			String initiator = new String(a.get(0).get());
			int factionId = Integer.parseInt(a.get(1).get());
			String uid = new String(a.get(2).get());

			FactionHomebaseChange change;

			if (gameState.isOnServer()) {
				change = new FactionHomebaseChange(initiator, factionId, uid, null, null);
			} else {
				int x = Integer.parseInt(a.get(3).get());
				int y = Integer.parseInt(a.get(4).get());
				int z = Integer.parseInt(a.get(5).get());
				String realName = new String(a.get(6).get());
				change = new FactionHomebaseChange(initiator, factionId, uid, new Vector3i(x, y, z), realName);
			}

			synchronized (factionHomebaseToMod) {
				factionHomebaseToMod.add(change);
			}
		}
		for (int i = 0; i < networkObject.factionClientSystemOwnerChangeBuffer.getReceiveBuffer().size(); i++) {
			RemoteSystemOwnershipChange a = networkObject.factionClientSystemOwnerChangeBuffer.getReceiveBuffer().get(i);
			synchronized (factionSystemOwnerToMod) {
				factionSystemOwnerToMod.add(a.get());
			}

		}
		for (int i = 0; i < networkObject.factionRolesBuffer.getReceiveBuffer().size(); i++) {
			RemoteFactionRoles rRules = networkObject.factionRolesBuffer.getReceiveBuffer().get(i);
			synchronized (relationRolesToMod) {
				relationRolesToMod.enqueue(rRules.get());
			}
		}
	}

	public void serverRevokeFactionHome(int wasFaction) {
		if (gameState.isOnServer()) {
			String initiator = new String("");
			int factionId = wasFaction;
			String uid = "";

			FactionHomebaseChange change;

			change = new FactionHomebaseChange(initiator, factionId, uid, null, null);
			change.admin = true;
			synchronized (factionHomebaseToMod) {
				factionHomebaseToMod.add(change);
			}
		}

	}
	public void scheduleTurn(NPCFaction faction){
		assert(getState() instanceof GameServerState);
		synchronized(turnSchedule){
			turnSchedule.enqueue(faction);
		}
	}
	public void updateLocal(Timer timer) {
		if (flagCheckFactions) {
			checkFactions();
			flagCheckFactions = false;
		}

		if (gameState.isOnServer()) {
			updateFactionPoints();
		}

		if (!simpleCommandQueue.isEmpty()) {
			synchronized (simpleCommandQueue) {
				while (!simpleCommandQueue.isEmpty()) {
					NPCFactionControlCommand dequeue = simpleCommandQueue.dequeue();

					executeSimpleCommand(dequeue);
				}
			}
		}
		
		if (!toAddFactionNewsPosts.isEmpty()) {
			synchronized (toAddFactionNewsPosts) {
				while (!toAddFactionNewsPosts.isEmpty()) {

					//on server only
					FactionNewsPost f = toAddFactionNewsPosts.remove(0);
					if (f.isDelete()) {
						TreeSet<FactionNewsPost> treeSet = news.get(f.getFactionId());
						if (treeSet != null) {
							treeSet.remove(f);

							if (gameState.isOnServer()) {
								//deligate
								gameState.getNetworkObject().factionNewsPosts.add(new RemoteFactionNewsPost(f, gameState.getNetworkObject()));
							}
							changedFactionNewsDeletedAspect = true;
						}
						if (!gameState.isOnServer()) {
							if (((GameClientState) gameState.getState()).getController().getClientChannel() != null) {
								((GameClientState) gameState.getState()).getController().getClientChannel().getFactionNews().remove(f);
								changedFactionNewsDeletedAspect = true;
							}
						}

					} else {
//						System.err.println("[FACTIONMANAGER] trying to add news entry: " + f + " on " + getGameState().getState() + " -> fid(" + f.getFactionId() + ")");
						if (!news.containsKey(f.getFactionId())) {
							news.put(f.getFactionId(), new TreeSet<FactionNewsPost>());
						}
						if(existsFaction(f.getFactionId()) && !isNPCFaction(f.getFactionId())){
							news.get(f.getFactionId()).add(f);
							if (gameState.isOnServer()) {
								//deligate
								gameState.getNetworkObject().factionNewsPosts.add(new RemoteFactionNewsPost(f, gameState.getNetworkObject()));
							} else {
								PlayerState player = ((GameClientState) gameState.getState()).getPlayer();
								if (f.getDate() > player.getCreationTime() && f.getFactionId() == player.getFactionId()) {
									((GameClientState) gameState.getState()).getController().popupGameTextMessage(
											Lng.str("A news post has been posted\non your faction board\n%s...", f.getMessage().substring(0, Math.min(f.getMessage().length() - 1, 20))), 0);
								}
							}
							System.err.println("[FACTIONMANAGER] updated news on " + gameState.getState() + " for factionID " + f.getFactionId() + "; delete: " + f.isDelete());
							changedFactionAspect = true;
						}
					}

				}
			}
		}
		if(flagHomebaseChanged != null) {
			for(Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if(s instanceof SegmentController && ((SegmentController) s).getRuleEntityManager() != null) {
					((SegmentController) s).getRuleEntityManager().triggerHomebaseChanged();
				}
			}

			flagHomebaseChanged = null;
		}
		if (!toAddFactionInvites.isEmpty()) {
			synchronized (toAddFactionInvites) {
				while (!toAddFactionInvites.isEmpty()) {
					FactionInvite f = toAddFactionInvites.remove(0);
					synchronized (factionInvitations) {
						factionInvitations.remove(f);
						factionInvitations.add(f);
					}
					factionInvitationsChanged = true;
					if (gameState.isOnServer()) {
						gameState.getNetworkObject().factionInviteAdd.add(new RemoteFactionInvitation(f, gameState.getNetworkObject()));
						System.err.println("[FACTIONMANAGER] added invite: from " + f.getFromPlayerName() + " to " + f.getToPlayerName() + " to faction " + f.getFactionUID());
					}
				}
			}
		}
		if (!toDelFactionInvites.isEmpty()) {
			synchronized (toDelFactionInvites) {
				while (!toDelFactionInvites.isEmpty()) {

					FactionInvite f = toDelFactionInvites.remove(0);
					synchronized (factionInvitations) {
						boolean suc = factionInvitations.remove(f);
						System.err.println("[FactionManager] Removing faction invitation " + f + " on " + gameState.getState() + "; success: " + suc);
						if (!suc) {
							System.err.println("[FactionManager] Failed to delete invitation: " + f + ": " + factionInvitations);
						}
					}
					factionInvitationsChanged = true;
					if (gameState.isOnServer()) {
						gameState.getNetworkObject().factionInviteDel.add(new RemoteFactionInvitation(f, gameState.getNetworkObject()));
					}
				}
			}
		}

		if (factionInvitationsChanged) {
			System.err.println("[FACTIONMANAGER] Faction Invites Changed");
			for(FactionChangeListener s : listeners) {
				s.onInvitationsChanged();
			}
			obs.notify();
			factionInvitationsChanged = false;
		}
		for(Faction f : factionMap.values()) {
			f.updateLocal(timer);
		}
		if (!changedFactions.isEmpty()) {
			synchronized (changedFactions) {
				while (!changedFactions.isEmpty()) {
					System.err.println("########### APPLYING FACTION CHANGE ");
					FactionMod mod = changedFactions.remove(0);
					Faction f = factionMap.get(mod.factionId);
					if (f != null) {
						if (Faction.NT_CODE_MEMBER_NAME_CHANGE.equals(mod.option)) {
							FactionPermission factionPermission = f.getMembersUID().get(mod.initiator);
							System.err.println("[FACTIONMANAGER] Faction " + f + " set change member name: " + mod.initiator+" -> "+mod.setting);
							if(factionPermission != null) {
								factionPermission.playerUID = mod.setting;
							}else {
								System.err.println("[FACTIONMANAGER] member not found "+mod.initiator);
							}
							if(isOnServer()) {
								f.getRuleEntityManager().triggerFactionMemberMod();
							}
						}else if (Faction.NT_CODE_OPEN_TO_JOIN.equals(mod.option)) {
							f.setOpenToJoin(Boolean.parseBoolean(mod.setting));
							System.err.println("[FACTIONMANAGER] Faction " + f + " set open to join: " + mod.setting);
						} else if (Faction.NT_CODE_DESCRIPTION.equals(mod.option)) {
							f.setDescription(mod.setting);
							System.err.println("[FACTIONMANAGER] Faction " + f + " set description: " + mod.setting);
						} else if (Faction.NT_CODE_NAME.equals(mod.option)) {
							f.setName(mod.setting);
							System.err.println("[FACTIONMANAGER] Faction " + f + " set name: " + mod.setting);
						} else if (Faction.NT_CODE_ATTACK_NEUTRAL.equals(mod.option)) {
							f.setAttackNeutral(Boolean.parseBoolean(mod.setting));
							System.err.println("[FACTIONMANAGER] Faction " + f + " set attack neutral: " + mod.setting);
						} else if (Faction.NT_CODE_AUTO_DECLARE_WAR.equals(mod.option)) {
							f.setAutoDeclareWar(Boolean.parseBoolean(mod.setting));
							System.err.println("[FACTIONMANAGER] Faction " + f + " set auto attack: " + mod.setting);
						} else {
							System.err.println("[CLIENT] Exception: unknown faction mod command: " + mod.option + " -> " + mod.setting);
							assert (false);
						}

					}
					changedFactionAspect = true;
				}
			}
		}

		if (!toDelFactions.isEmpty()) {
			synchronized (toDelFactions) {
				while (!toDelFactions.isEmpty()) {
					Faction faction = toDelFactions.remove(0);
					synchronized (factionMap) {
						
						((GameStateInterface)getState()).getGameState().getInventories().remove(faction.getIdFaction());
						Faction remove = factionMap.remove(faction.getIdFaction());
						System.err.println("[FACTIONMANAGER] " + gameState.getState() + " Faction " + remove + " has been deleted");
						if (remove != null) {
							ArrayList<FactionRelation> toDel = new ArrayList<FactionRelation>();
							for (FactionRelation f : relations.values()) {
								if (f.contains(remove.getIdFaction())) {
									toDel.add(f);
								}
							}
							relations.values().removeAll(toDel);
						} else {
							System.err.println("[FACTION][WARNING] " + gameState.getState() + " could not delete " + faction + ". ID NOT FOUND");
						}
					}
					
					if (gameState.isOnServer()) {
						gameState.getNetworkObject().factionDel.add(new RemoteFaction(faction, gameState.getNetworkObject()));
						for(Faction f : factionMap.values()){
							if(f.isNPC() && f != faction){
								((NPCFaction)f).getDiplomacy().onDeletedFaction(faction);
							}
						}
					}
					changedFactionAspect = true;
				}
			}
		}
		if (!toAddFactions.isEmpty()) {
			synchronized (toAddFactions) {
				while (!toAddFactions.isEmpty()) {
					Faction faction = toAddFactions.remove(0);
					initializeRelationShips(faction);
					
					
					synchronized (factionMap) {
						//						System.err.println("[FACTIONMANAGER] ADDING FACTION ON "+gameState.getState()+": "+faction);
						//						System.err.println("[FACTIONMANAGER] INITIAL MEMBERS "+faction.getMembersUID().keySet());

						if (gameState.isOnServer()) {
							for (Entry<String, FactionPermission> k : faction.getMembersUID().entrySet()) {
								System.err.println("[FACTION] Added to members " + k.getKey() + " perm(" + k.getValue().role + ") of " + faction + " on " + gameState.getState());
							}
						}
						factionMap.put(faction.getIdFaction(), faction);
						if(faction instanceof NPCFaction){
							((GameStateInterface)getState()).getGameState().getInventories().put(faction.getIdFaction(), ((NPCFaction) faction).getInventory());
						}
					}
					if (faction.addHook != null) {
						faction.addHook.callback();
						faction.addHook = null;
					}

					if (isOnServer()) {
						gameState.getNetworkObject().factionAdd.add(new RemoteFaction(faction, gameState.getNetworkObject()));
						for (int i = 0; i < failedChangedMembersFactions.size(); i++) {
							FactionMemberMod m = failedChangedMembersFactions.get(i);
							if (m.id == faction.getIdFaction()) {
								changedMembersFactions.add(m);
								failedChangedMembersFactions.remove(i);
								i--;
							}
						}
					
						for(Faction f : factionMap.values()){
							if(f.isNPC() && f != faction){
								((NPCFaction)f).getDiplomacy().onAddedFaction(faction);
							}
							f.getRuleEntityManager().trigger(Condition.TRIGGER_ON_ALL);
						}

					}
					changedFactionAspect = true;
				}
			}

		}
		if (!toModPersonalEnemies.isEmpty()) {
			synchronized (toModPersonalEnemies) {
				while (!toModPersonalEnemies.isEmpty()) {

					PersonalEnemyMod m = toModPersonalEnemies.remove(0);
					Faction f = getFaction(m.fid);
					if (gameState.isOnServer()) {
						if (f != null) {
							FactionPermission factionPermission = f.getMembersUID().get(m.initiator);
							GameServerState s = ((GameServerState) gameState.getState());

							if (f.isNPC() || s.isAdmin(m.initiator) || (factionPermission != null && factionPermission.hasRelationshipPermission(f))) {
								if (m.add) {
									System.err.println("[FACTIONMANAGER] ADDING PERSONAL ENEMY OF FACTION: " + m.enemyPlayerName);
									f.getPersonalEnemies().add(m.enemyPlayerName.toLowerCase(Locale.ENGLISH));
									sendPersonalEnemyAdd(m.initiator, f, m.enemyPlayerName.toLowerCase(Locale.ENGLISH));
									npcFactionNews.war(f.getIdFaction(), m.enemyPlayerName);
								} else {
									System.err.println("[FACTIONMANAGER] REMOVING PERSONAL ENEMY OF FACTION: " + m.enemyPlayerName);
									f.getPersonalEnemies().remove(m.enemyPlayerName.toLowerCase(Locale.ENGLISH));
									sendPersonalEnemyRemove(m.initiator, f, m.enemyPlayerName.toLowerCase(Locale.ENGLISH));
									npcFactionNews.peace(f.getIdFaction(), m.enemyPlayerName);
								}

							} else {
								System.err.println("[SERVER] Exception: cannot add personal enemy: permission denied for " + m.initiator);
							}
						} else {
							System.err.println("[SERVER] Exception: cannot add personal enemy: faction does not exist");
						}

					} else {
						if (f != null) {
							if (m.add) {
								f.getPersonalEnemies().add(m.enemyPlayerName.toLowerCase(Locale.ENGLISH));
							} else {
								f.getPersonalEnemies().remove(m.enemyPlayerName.toLowerCase(Locale.ENGLISH));
							}
						} else {
							System.err.println("[CLIENT] Exception: cannot add personal enemy: faction does not exist");
						}
					}
				}
			}
		}
		if (!changedMembersFactions.isEmpty()) {
			synchronized (changedMembersFactions) {
				while (!changedMembersFactions.isEmpty()) {
					FactionMemberMod f = changedMembersFactions.remove(0);

					synchronized (factionMap) {

						Faction faction = factionMap.get(f.id);
						if (gameState.isOnServer()) {
							if (f.initiator != null) {
								faction.getRuleEntityManager().triggerFactionMemberMod();
								FactionPermission factionPermission = faction.getMembersUID().get(f.initiator);
								if (factionPermission != null) {
									FactionPermission targetPermission = faction.getMembersUID().get(f.playerState);

									if (f.playerState.toLowerCase(Locale.ENGLISH).equals(f.initiator.toLowerCase(Locale.ENGLISH)) && factionPermission.role == 4) {
										//faction admin is allowed to self demote
									} else {
										//check role rank
										if (targetPermission != null && factionPermission.role <= targetPermission.role) {
											System.err.println("[SERVER][ERROR] Failed to set permission (role too low: initiator: " + factionPermission.role + "; target " + targetPermission.role + ")! initiator: " + f.initiator);
											try {
												PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(f.initiator);
												playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Cannot set permission!\nYour rank is too low!"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
											} catch (PlayerNotFountException e) {
												e.printStackTrace();
											}
											continue;
										}
									}

									//check mod permission
									if (f.addOrMod && (factionPermission == null || !factionPermission.hasPermissionEditPermission(faction))) {
										System.err.println("[SERVER][ERROR] Failed to set permission! initiator: " + f.initiator);
										try {
											PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(f.initiator);
											playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Cannot set permission!\nAccess denied!"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
										} catch (PlayerNotFountException e) {
											e.printStackTrace();
										}
										continue;
									}

									//check kick permission
									if (!f.addOrMod && (factionPermission == null || !factionPermission.hasKickPermission(faction))) {
										System.err.println("[SERVER][ERROR] Failed to kick! initiator: " + f.initiator);
										try {
											PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(f.initiator);
											playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Cannot kick!\nAccess denied!"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
										} catch (PlayerNotFountException e) {
											e.printStackTrace();
										}
										continue;
									}
								} else {
									System.err.println("[SERVER][ERROR] Could not hande faction mod! unknown member: " + f.initiator);
									assert (false);
								}
							}
						}

						if (f.addOrMod) {
							if (faction != null) {
								faction.addOrModifyMember(f.playerState, f.playerState, f.permissions, f.lastActiveTime, this.gameState, true);
							} else {
								System.err.println("[FACTIONMANAGER][ERROR] adding member failed: " + f.playerState + " from " + f.id);

								if (!f.failed) {
									f.failTime = System.currentTimeMillis();
									failedChangedMembersFactions.add(f);
								}

							}

						} else {
							if (faction != null) {
								System.err.println("[FACTIONMANAGER] removing member: " + f.playerState + " from " + faction + "; on " + gameState.getState());
								faction.removeMember(f.playerState, this.gameState);
								if (gameState.isOnServer() && faction.getMembersUID().isEmpty()) {
									//only necessary on server
									System.err.println("[FACTIONMANAGER] Removed last member: Faction Empty -> Removing Faction " + faction);
									if (faction.getFactionMode() == 0) {
										removeFaction(faction);
									} else {
										System.err.println("[FACTIONMANAGER] faction was not removed because of 0 members because it has a mode flag (battle)");
									}
								}
							} else {
								System.err.println("[FactionManager][ERROR] removing member failed: " + f.playerState + " from " + f.id);

								if (!f.failed) {
									f.failTime = System.currentTimeMillis();
									failedChangedMembersFactions.add(f);
								}
								f.failed = true;
							}
						}
						changedFactionAspect = true;
					}

				}
			}
		}

		//only relevant on server
		if (!toKickMember.isEmpty()) {
			synchronized (toKickMember) {
				while (!toKickMember.isEmpty()) {
					FactionKick fk = toKickMember.remove(0);

					Faction f = getFaction(fk.faction);

					if (f != null) {
						if(isOnServer()) {
							f.getRuleEntityManager().triggerFactionMemberMod();
						}
						FactionPermission kickerPermission = f.getMembersUID().get(fk.initiator);
						FactionPermission targetPermission = f.getMembersUID().get(fk.player);
						if ((kickerPermission == null || targetPermission == null || !kickerPermission.hasKickPermission(f) ||
								targetPermission.role >= kickerPermission.role ||
								targetPermission.role >= f.getRoles().getRoles().length - 1)) {
							//can't kick higher ranks or founders
							try {
								PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(fk.initiator);
								playerFromName.getFactionController().setFactionId(0);
								playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Permission denied!"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
							} catch (PlayerNotFountException e) {
							}
						} else {
							FactionMemberMod m = new FactionMemberMod(f.getIdFaction(), fk.player, false);
							try {
								PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(fk.player);
								playerFromName.getFactionController().setFactionId(0);

								System.err.println("[FACTIONMANAGER] Member has been kicked from faction " + f + ": " + playerFromName);

								playerFromName.sendServerMessage(new ServerMessage(Lng.astr("You have been kicked from\n%s!",  f.getName()), ServerMessage.MESSAGE_TYPE_INFO, playerFromName.getId()));
							} catch (PlayerNotFountException e) {
								System.err.println("[FACTION][KICK] player not found (could be offline)");
							}
							changedMembersFactions.add(m);
						}

					}
				}
			}
		}

		if (!relationsToAdd.isEmpty()) {
			synchronized (relationsToAdd) {
				while (!relationsToAdd.isEmpty()) {
					FactionRelation fk = relationsToAdd.remove(0);
					assert (!(fk instanceof FactionRelationOffer)) : fk;
					FactionRelationChangeEvent event = new FactionRelationChangeEvent(getFaction(fk.a), getFaction(fk.b), getRelation(fk.a, fk.b), fk.getRelation());
					StarLoader.fireEvent(event, true);
					if(!event.isCanceled()) {
						relations.put(fk.getCode(), fk);
						Faction a = factionMap.get(fk.a);
						Faction b = factionMap.get(fk.b);
						if(isOnServer()) {
							if(a != null) {
								a.getRuleEntityManager().triggerFactionMemberMod();
							}
							if(b != null) {
								b.getRuleEntityManager().triggerFactionMemberMod();
							}
						}
						changedFactionAspect = true;
						if(gameState.isOnServer()) {
							gameState.getNetworkObject().factionRelationships.add(fk.getRemoteArray(gameState.getNetworkObject()));
						}
					}
				}
			}
		}
		for(int fid : actionCheck){
			Faction faction = factionMap.get(fid);
			if(faction != null){
				faction.checkActions();
			}
		}
		actionCheck.clear();
		
		
		if (!toAddFactionPointMods.isEmpty()) {
			synchronized (toAddFactionPointMods) {
				while (!toAddFactionPointMods.isEmpty()) {
					assert (!gameState.isOnServer());

					FactionPointMod pointMod = toAddFactionPointMods.dequeue();
					Faction faction = factionMap.get(pointMod.factionId);
					if (faction != null) {
						if(isOnServer()) {
							faction.getRuleEntityManager().triggerFactionPointsChange();
						}
						pointMod.apply(faction);
					} else {
						System.err.println("[FACTIONMANAGER] Exception: faction not found for point mod: " + pointMod.factionId);
					}
				}
			}
		}
		if (!relationRolesToMod.isEmpty()) {
			synchronized (relationRolesToMod) {
				while (!relationRolesToMod.isEmpty()) {

					FactionRoles roles = relationRolesToMod.dequeue();
					Faction f = getFaction(roles.factionId);


					if (f != null) {
						

						for(FactionPermission p : f.getMembersUID().values()){
							//send local map if needed
							f.fogOfWarCheckServer(p, f.getRoles());
						}
						f.getRoles().apply(roles);
						System.err.println("[SERVER][FACTIONMANAGER] Applied Faction Roles: Faction: "+f.getName()+"("+f.getIdFaction()+"); SenderID: "+roles.senderId+"; Roles: "+f.getRoles());


						if (isOnServer()) {
							//deligate faction roles to clients
							sendFactionRoles(f.getRoles());
						}
					} else {
						System.err.println("[FactionManager][ERROR] could not find factionto apply rule " + roles);
					}
				}
			}
		}
		handleFactionHomebaseMods();
		handleFactionSystemOwnerMods();
		handleRelationShipOffers();
		handleFactionOffersAccept();
		if (factionOffersChanged) {
//			System.err.println("[FACTIONMANAGER] Faction Offers Changed");

			for(FactionChangeListener s : listeners) {
				s.onRelationShipOfferChanged();
			}
			obs.notifyObservers();
			factionOffersChanged = false;
		}
		if (changedFactionNewsDeletedAspect) {
			for(FactionChangeListener s : listeners) {
				s.onFactionNewsDeleted();
			}
			obs.notifyObservers();
			changedFactionNewsDeletedAspect = false;
		}
		if (changedFactionAspect) {
			for(FactionChangeListener s : listeners) {
				s.onFactionChanged();
			}
			obs.notifyObservers();
			changedFactionAspect = false;
		}
		handleNPCFactionTurns(timer);
		
		
		if(markedChangedContingentFactions.size() > 0){
			Iterator<NPCFaction> iterator = markedChangedContingentFactions.iterator();
			while(iterator.hasNext()){
				NPCFaction fac = iterator.next();
				boolean hasStillChangedSystems = fac.structure.updateChangedSystems(timer.currentTime);
				if(!hasStillChangedSystems){
					iterator.remove();
				}
			}
		}
		npcFactionNews.updateLocal(timer);
		
		
		ObjectIterator<PlayerState> it = needsSendAll.iterator();
		while(it.hasNext()){
			PlayerState next = it.next();
			if(next.getClientChannel() != null){
				for(Faction f : factionMap.values()){
					if(f.isNPC()){
						((NPCFaction)f).getDiplomacy().checkNPCFactionSending(gameState, true);
					}
				}
				it.remove();
				
				
				
			}
		}
	
		for(NPCDiplomacy d : diplomacyChanged){
//			System.err.println("DIPLOMACY CHANGED: "+d);
			d.checkNPCFactionSending(gameState, false);
		}
		diplomacyChanged.clear();
	
		
		if(isOnServer() && ServerConfig.NPC_DEBUG_MODE.isOn()){
			if(!wasNpcDebugMode){
				checkNPCFactionSendingDebug(true);
			}else{
				checkNPCFactionSendingDebug(false);
			}
		}
		this.wasNpcDebugMode = ServerConfig.NPC_DEBUG_MODE.isOn();
		
	}
	private void handleNPCFactionTurns(Timer timer) {
		if(currentTurn != null && !existsFaction(currentTurn.getIdFaction())){
			currentTurn = null;
		}
		if(currentTurn != null){
			
			if(timer.currentTime - lastNPCFactionTurnUpdate > 100){
				boolean done = currentTurn.turn(timer.currentTime);
				
				if(done){
					currentTurn = null;
				}
				this.lastNPCFactionTurnUpdate = timer.currentTime;
			}
		}else if(!turnSchedule.isEmpty()){
			long time = System.currentTimeMillis();
			synchronized(turnSchedule){
				while(!turnSchedule.isEmpty()){
					NPCFaction c = turnSchedule.dequeue();
					currentTurn = c;
					break;
				}
			}
		}		
	}
	public boolean isOnServer(){
		return gameState.isOnServer();
	}
	private void executeSimpleCommand(NPCFactionControlCommand com) {
		Faction faction = getFaction(com.factionId);
		if(faction != null && faction.isNPC()){
			((NPCFaction)faction).executeFactionCommand(com);
		}else{
			assert(false):com+"; "+faction;
		}
	}
	private void handleFactionOffersAccept() {
		int accepted = 0;
		if (!toAddFactionRelationOfferAccepts.isEmpty()) {
			factionOffersChanged = true;
			synchronized (toAddFactionRelationOfferAccepts) {
				while (!toAddFactionRelationOfferAccepts.isEmpty()) {
					FactionRelationOfferAcceptOrDecline fk = toAddFactionRelationOfferAccepts.remove(0);
					FactionRelationOffer factionRelationOffer = relationShipOffers.remove(fk.code);
					if (factionRelationOffer != null) {
						if (gameState.isOnServer()) {
							if (fk.accept) {
								FactionRelation r = new FactionRelation();

								r.set(factionRelationOffer.a, factionRelationOffer.b);

								if (factionRelationOffer.isEnemy()) {
									npcFactionNews.war(factionRelationOffer.a, getFactionName(factionRelationOffer.b));
									r.setEnemy();
								} else if (factionRelationOffer.isFriend()) {
									npcFactionNews.ally(factionRelationOffer.a, getFactionName(factionRelationOffer.b));
									r.setFriend();
								} else if (factionRelationOffer.isNeutral()) {
									npcFactionNews.peace(factionRelationOffer.a, getFactionName(factionRelationOffer.b));
									r.setNeutral();
								} else {
									assert (false);
								}

								String msg = "[FACTION] Relationship Offer accepted: " + fk.code + " between " + r + "; " + factionMap.get(r.a).getName() + " AND " + factionMap.get(r.b).getName() +
										"; CODEA: " + FactionRelationOffer.getOfferCode(factionRelationOffer.a, factionRelationOffer.b) +
										"; CODEB: " + FactionRelationOffer.getOfferCode(factionRelationOffer.b, factionRelationOffer.a);
//								System.err.println(msg);
								LogUtil.log().fine(msg);
								synchronized (relationsToAdd) {
									relationsToAdd.add(r);
								}
							}
							//deligate to remove offer from clients
							sendRelationshipAccept(fk.initiator, factionRelationOffer, fk.accept);
							accepted++;
						}
					} else {
						//System.err.println("[FACTION][OFFERACCEPT] relation ship offer not found " + fk.code);
					}

				}
			}
		}		
		if(accepted > 10) {
			System.err.println("[FACTION] more than 10 relationship offers in this update: "+accepted);
		}
	}
	public void handleRelationShipOffers(){
		if (!relationOffersToAdd.isEmpty()) {
			synchronized (relationOffersToAdd) {
				while (!relationOffersToAdd.isEmpty()) {
					factionOffersChanged = true;
					FactionRelationOffer fk = relationOffersToAdd.remove(0);
					
					if (fk.a == fk.b) {
						try {
							throw new NullPointerException("tried to have relation with self " + fk.a + "; " + fk.b);
						} catch (Exception e) {
							e.printStackTrace();
						}
						continue;
					}
					Faction from = getFaction(fk.a);
					Faction to = getFaction(fk.b);
					
					if (from != null && to != null) {
						if (fk.revoke) {
							if (fk.rel == RType.FRIEND.code && getRelation(from.getIdFaction(), to.getIdFaction()) != null && getRelation(from.getIdFaction(), to.getIdFaction()) == RType.FRIEND) {
								System.err.println("[FACTIONMANAGER] revoked alliance " + fk + " on " + gameState.getState());
								FactionRelation r = new FactionRelation();
								r.set(from.getIdFaction(), to.getIdFaction());
								r.setNeutral();
								synchronized (relationsToAdd) {
									relationsToAdd.add(r);
								}
								
								if (gameState.isOnServer()) {
									diplomacyAction(DiplActionType.ALLIANCE_CANCEL, to.getIdFaction(), from.getIdFaction());
								}
							} else {
								System.err.println("[FACTIONMANAGER] remove relationship offer " + fk + " on " + gameState.getState());
								relationShipOffers.remove(fk.getCode());
							}

							if (gameState.isOnServer()) {
								gameState.getNetworkObject().factionRelationshipOffer.add(fk.getRemoteArrayOffer(gameState.getNetworkObject()));
							}
						} else {

							if (gameState.isOnServer()) {
								
								if (fk.isEnemy()) {

									FactionNewsPost warDeclaration = new FactionNewsPost();
									warDeclaration.set(to.getIdFaction(), from.getName(), System.currentTimeMillis(), "Declaration of War", from.getName() + " declared war!\n" + fk.getMessage(), 0);

									sendServerFactionMail(Lng.str("[FACTION] %s declared war!", from.getName()), Lng.str("%s declared war on your faction!\n%s", from.getName(), fk.getMessage()), to);
									sendServerFactionMail(Lng.str("[FACTION] Your faction declared war!"), Lng.str("Your faction %s\ndeclared war on %s!\n", from.getName(), to.getName()), from);

									diplomacyAction(DiplActionType.DECLARATION_OF_WAR, to.getIdFaction(), from.getIdFaction());
									npcFactionNews.war(from.getIdFaction(), to.getName());
									
									toAddFactionNewsPosts.add(warDeclaration);
								} else {
									if (fk.isFriend()) {

										FactionNewsPost allyDeclaration = new FactionNewsPost();
										allyDeclaration.set(to.getIdFaction(), from.getName(), System.currentTimeMillis(), "Alliance Offer", from.getName() + " offered an Alliance!\n" + fk.getMessage(), 0);

										diplomacyAction(DiplActionType.ALLIANCE_REQUEST, to.getIdFaction(), from.getIdFaction());
										
										toAddFactionNewsPosts.add(allyDeclaration);
									} else if (fk.isNeutral()) {

										FactionNewsPost peaceTreaty = new FactionNewsPost();
										peaceTreaty.set(to.getIdFaction(), from.getName(), System.currentTimeMillis(), "Peace Offer", from.getName() + " offered a peace treaty!\n" + fk.getMessage(), 0);

										diplomacyAction(DiplActionType.PEACE_OFFER, to.getIdFaction(), from.getIdFaction());
										
										toAddFactionNewsPosts.add(peaceTreaty);
									} else {
										assert (false);
									}
								}
							}
							if (fk.isEnemy()) {
								if (gameState.isOnServer()) {
									FactionRelation r = new FactionRelation();
									r.set(from.getIdFaction(), to.getIdFaction());
									r.setEnemy();
									synchronized (relationsToAdd) {
										relationsToAdd.add(r);
									}
								}

							} else {

								relationShipOffers.put(fk.getCode(), fk);
//								System.err.println("[FACTIONMANAGER] put relationship offer " + fk + " on " + getGameState().getState());
								if (gameState.isOnServer()) {
									gameState.getNetworkObject().factionRelationshipOffer.add(fk.getRemoteArrayOffer(gameState.getNetworkObject()));
								}
							}
						}
					}
				}

			}
		}
	}
	public void sendServerSystemFactionOwnerChange(String initiator, int factionId, String baseUID, Vector3i stationSector, Vector3i targetSystem, String realName) {
		assert (baseUID.length() > 0) : "only valid stations allowed since this is a server change";
		assert (gameState.isOnServer());
		FactionSystemOwnerChange c = new FactionSystemOwnerChange(initiator, factionId, baseUID, stationSector, targetSystem, realName);
		synchronized (factionSystemOwnerToMod) {
			factionSystemOwnerToMod.add(c);
		}
	}

	private void handleFactionSystemOwnerMods() {
		if (!factionSystemOwnerToMod.isEmpty()) {
			synchronized (factionSystemOwnerToMod) {
				while (!factionSystemOwnerToMod.isEmpty()) {
					FactionSystemOwnerChange fk = factionSystemOwnerToMod.remove(0);
					Faction fTar = getFaction(fk.factionId);
					if (gameState.isOnServer()) {
						if(fk.sender != 0){
							RegisteredClientOnServer registeredClientOnServer = ((GameServerState)getState()).getClients().get(fk.sender);
							
							if(registeredClientOnServer.getPlayerObject() == null || !(registeredClientOnServer.getPlayerObject() instanceof PlayerState)){
								System.err.println("[SERVER] SYSTEM OWNERSHIP CHANGE ERROR: no sender");
								continue;
							}
							PlayerState player = (PlayerState) registeredClientOnServer.getPlayerObject();
	
							if(getFaction(player.getFactionId()) == null){
								player.sendServerMessagePlayerError(Lng.astr("Error: You are not in any faction!"));
								continue;
							}
							
							Faction fown = getFaction(player.getFactionId());
							
							FactionPermission factionPermission = fown.getMembersUID().get(fk.initiator);
							
							System.err.println("[FACTIONMANAGER] System ownership called " + gameState.getState() + ": New Values: FactionId " + fk.factionId + "; System " + fk.targetSystem + " UID(" + fk.baseUID + ")");
							if (fk.baseUID.length() > 0 && fTar != null && fown.factionPoints < 0) {
								System.err.println("[FACTIONMANAGER] Cannot give ownership to " + gameState.getState() + ": " + fk.factionId + " -> " + fk.baseUID + "; faction points negative");
								fown.broadcastMessage(Lng.astr("Cannot claim sector with\nnegative faction points!"), ServerMessage.MESSAGE_TYPE_ERROR, (GameServerState) gameState.getState());
								continue;
							}
		
							if (!fk.forceOverwrite) {
								if (factionPermission == null || !factionPermission.hasClaimSystemPermission(fown)) {
									try {
										PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(fk.initiator);
										playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Cannot claim/unclaim:\nFaction permission denied!"), ServerMessage.MESSAGE_TYPE_INFO, playerFromName.getId()));
									} catch (PlayerNotFountException e) {
										System.err.println("[FACTION][HOMEBASE] player not found " + fk.initiator);
									}
									System.err.println("[FACTION][HOMEBASE][ERROR] no right to claim/unclaim system " + fk.initiator);
									continue;
								}
							}
		
							
							if (fk.baseUID.length() > 0 && !fk.baseUID.startsWith(EntityType.SPACE_STATION.dbPrefix) && 
									!fk.baseUID.startsWith(EntityType.PLANET_SEGMENT.dbPrefix) && !fk.baseUID.equals(VoidSystem.UNCLAIMABLE)) {
								System.err.println("[FACTION][SYSTEMOWNER][ERROR] cannot take ownership with base '" + fk.baseUID + "'");
								fk.baseUID = "";
								fown.broadcastMessage(Lng.astr("System can not be claimed!\n(invalid UID)"), ServerMessage.MESSAGE_TYPE_ERROR, (GameServerState) gameState.getState());
							}
						}
						int handlingFaction = 0;
					
						try {
							StellarSystem sys = ((GameServerState) gameState.getState()).getUniverse().getStellarSystemFromStellarPos(fk.targetSystem);
							Vector3i systemPos = (sys.getPos());
							String systemName = sys.getName();
							if (!fk.initiator.equals("ADMIN") && sys.getOwnerUID() != null && sys.getOwnerUID().equals(VoidSystem.UNCLAIMABLE) && sys.getOwnerFaction() == TRAIDING_GUILD_ID) {
								try {
									PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(fk.initiator);
									playerFromName.sendServerMessage(new ServerMessage(Lng.astr("System can not be claimed!\n(admin protected)"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
								} catch (PlayerNotFountException e) {
									e.printStackTrace();
								}

							} else {

								Sendable sendable = gameState.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(fk.baseUID);
								if (sendable != null && ((GameServerState) gameState.getState()).getUniverse().getSector(((SimpleTransformableSendableObject) sendable).getSectorId()) != null) {
									//by loaded object
									SimpleTransformableSendableObject obj = (SimpleTransformableSendableObject) sendable;

									//										(SimpleTransformableSendableObject)sendable
									Sector sector = ((GameServerState) gameState.getState()).getUniverse().getSector(obj.getSectorId());
									if (sector != null) {
										int ownerFactionBef = sys.getOwnerFaction();
										//INSERTED CODE
										SystemClaimEvent event = new SystemClaimEvent(sys, fk, sector, fTar);
										StarLoader.fireEvent(event, isOnServer());
										if(event.isCanceled()){
											return;
										}
										///
										if (fTar != null) {
											sys.setOwnerUID(new String(fk.baseUID));
											sys.getOwnerPos().set(sector.pos);
											sys.setOwnerFaction(fTar.getIdFaction());
											handlingFaction = fk.factionId;
										} else {
											handlingFaction = sys.getOwnerFaction();
											sys.setOwnerUID(null);
											sys.getOwnerPos().set(0, 0, 0);
											sys.setOwnerFaction(0);
											
										}

										((GameServerState) gameState.getState()).getDatabaseIndex().getTableManager().getSystemTable().updateOrInsertSystemIfChanged(sys, true);
										((GameServerState) gameState.getState()).getUniverse()
										.getGalaxyFromSystemPos(sys.getPos()).getNpcFactionManager()
										.onSystemOwnershipChanged(ownerFactionBef, sys.getOwnerFaction(), sys.getPos());
										
										gameState.sendGalaxyModToClients(sys, sector.pos);

									} else {
										throw new RuntimeException("Cannot set system ownership on server: sector for active object " + obj + " wasnt loaded");
									}
								} else {
									//by database
									try {
										Vector3i sectorPos = fk.stationSector;
										if (fk.baseUID.length() > 0) {
											String withoutPrefix = DatabaseEntry.removePrefix(fk.baseUID);

											List<DatabaseEntry> byUIDExact = ((GameServerState) gameState.getState()).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(withoutPrefix, 1);
											if (byUIDExact.size() == 1) {
												DatabaseEntry databaseEntry = byUIDExact.get(0);
												sys = ((GameServerState) gameState.getState()).getUniverse().getStellarSystemFromSecPos(databaseEntry.sectorPos);
												sectorPos = databaseEntry.sectorPos;
											} else {
												System.err
														.println("Cannot set system ownership on server: " + fk.baseUID + " was not found in sql database");
											}
										}
										//INSERTED CODE
										SystemClaimEvent event = new SystemClaimEvent(sys, fk, sectorPos, fTar);
										StarLoader.fireEvent(event, isOnServer());
										if(event.isCanceled()){
											return;
										}
										///

										if (fTar != null) {
											System.err.println("[SERVER][FACTION] Territory: SET SYSTEM " + sys + " to faction " + fTar.getName());
											sys.setOwnerUID(new String(fk.baseUID));
											sys.getOwnerPos().set(sectorPos);
											sys.setOwnerFaction(fTar.getIdFaction());
											handlingFaction = fk.factionId;
										} else {
											System.err.println("[SERVER][FACTION] Territory: REMOVE SYSTEM " + sys + " OWNERSHIP FROM FACTION " + getFaction(sys.getOwnerFaction()));
											handlingFaction = sys.getOwnerFaction();
											sys.setOwnerUID(null);
											sys.getOwnerPos().set(0, 0, 0);
											sys.setOwnerFaction(0);
										}

										((GameServerState) gameState.getState()).getDatabaseIndex().getTableManager().getSystemTable().updateOrInsertSystemIfChanged(sys, true);

										gameState.sendGalaxyModToClients(sys, sectorPos);

									} catch (SQLException e) {
										e.printStackTrace();
										((GameServerState) gameState.getState()).getController().broadcastMessage(Lng.astr("System ownership failed!\nPlease send in logs.\n%s\n%s", e.getClass().getSimpleName(),  e.getMessage()) , ServerMessage.MESSAGE_TYPE_ERROR);
										throw new RuntimeException("SYSTEM OWNERSHIP FAIL: Cannot set ownership  on server: " + fk.baseUID + " was not found in sql database");
									} catch (IOException e) {
										e.printStackTrace();
										((GameServerState) gameState.getState()).getController().broadcastMessage(Lng.astr("System ownership failed!\nPlease send in logs.\n%s\n%s", e.getClass().getSimpleName(), e.getMessage()) , ServerMessage.MESSAGE_TYPE_ERROR);
										throw new RuntimeException("SYSTEM OWNERSHIP FAIL: Cannot set ownership on server: " + fk.baseUID + " was not found in sql database");
									}
								}
							}
							if (fTar != null) {
								FactionNewsPost n = new FactionNewsPost();
								n.set(handlingFaction, fk.initiator, System.currentTimeMillis(),
										"System claimed", "System " + systemName + systemPos + " has been claimed by " + fk.initiator + ".", 0);
								toAddFactionNewsPosts.add(n);
							} else {
								FactionNewsPost n = new FactionNewsPost();

								String r = fk.initiator;
								if ("SYSTEM".equals(r)) {
									r = "reset of faction on that structure";
								}
								n.set(handlingFaction, r, System.currentTimeMillis(),
										"System Ownership Revoked", "Ownership of System " + systemName + systemPos + "\nhas been revoked by " + r + ".", 0);
								toAddFactionNewsPosts.add(n);
							}
						} catch (Exception e) {
							((GameServerState) gameState.getState()).getController().broadcastMessage(Lng.astr("System ownership failed!\nPlease send in logs.\n%s\n%s", e.getClass().getSimpleName(),  e.getMessage()), ServerMessage.MESSAGE_TYPE_ERROR);
							e.printStackTrace();

						}

					}

				}
			}
		}
	}

	private void handleFactionHomebaseMods() {
		if (!factionHomebaseToMod.isEmpty()) {
			synchronized (factionHomebaseToMod) {
				while (!factionHomebaseToMod.isEmpty()) {
					FactionHomebaseChange fk = factionHomebaseToMod.remove(0);

					Faction f = getFaction(fk.factionId);
					if (f != null) {

						if (gameState.isOnServer() && !fk.admin) {
							FactionPermission factionPermission = f.getMembersUID().get(fk.initiator);
							if (factionPermission == null || !factionPermission.hasHomebasePermission(f)) {
								try {
									PlayerState playerFromName = ((GameServerState) gameState.getState()).getPlayerFromName(fk.initiator);
									playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Cannot set homebase:\nFaction permission denied!\n"), ServerMessage.MESSAGE_TYPE_INFO, playerFromName.getId()));
								} catch (PlayerNotFountException e) {
									System.err.println("[FACTION][HOMEBASE] player not found " + fk.initiator);
								}
								System.err.println("[FACTION][HOMEBASE][ERROR] no right to set base " + fk.initiator);
								continue;
							}
						}

						if (!fk.baseUID.startsWith("ENTITY_SPACESTATION") && !fk.baseUID.startsWith("ENTITY_PLANET")) {
							System.err.println("[FACTION][HOMEBASE][ERROR] cannot make a home base " + fk.baseUID);
							fk.baseUID = "";
						}

						if (gameState.isOnServer()) {
							if (fk.baseUID.length() > 0) {
								try {

									Sendable sendable = gameState.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(fk.baseUID);
									if (sendable != null && ((GameServerState) gameState.getState()).getUniverse().getSector(((SimpleTransformableSendableObject) sendable).getSectorId()) != null) {
										SimpleTransformableSendableObject obj = (SimpleTransformableSendableObject) sendable;

										//										(SimpleTransformableSendableObject)sendable
										Sector sector = ((GameServerState) gameState.getState()).getUniverse().getSector(obj.getSectorId());
										f.setHomebaseUID(new String(fk.baseUID));

										f.getHomeSector().set(new Vector3i(sector.pos));
										f.setHomebaseRealName(new String(obj.getRealName()));

										sendHomeBaseChange(fk.initiator, fk.factionId, fk.baseUID, f.getHomeSector(), f.getHomebaseRealName());
									} else {

										List<DatabaseEntry> byUIDExact;
										try {
											byUIDExact = ((GameServerState) gameState.getState()).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefix(fk.baseUID), 1);

											if (byUIDExact.size() == 1) {
												DatabaseEntry databaseEntry = byUIDExact.get(0);
												f.setHomebaseUID(new String(fk.baseUID));

												f.getHomeSector().set(new Vector3i(databaseEntry.sectorPos));
												f.setHomebaseRealName(new String(databaseEntry.realName));

												sendHomeBaseChange(fk.initiator, fk.factionId, fk.baseUID, f.getHomeSector(), f.getHomebaseRealName());
											} else {
												throw new RuntimeException("Cannot set homebase on server: " + fk.baseUID + " was not found in sql database");
											}
										} catch (SQLException e) {
											e.printStackTrace();
											((GameServerState) gameState.getState()).getController().broadcastMessage(Lng.astr("Homebase creation failed!\nPlease send in logs."), ServerMessage.MESSAGE_TYPE_ERROR);
											throw new RuntimeException("HOMEBASE CREATION FAIL: Cannot set homebase on server: " + fk.baseUID + " was not found in sql database");
										}
									}
								} catch (RuntimeException e) {
									e.printStackTrace();
								}
								FactionNewsPost newHome = new FactionNewsPost();
								newHome.set(fk.factionId, fk.initiator, System.currentTimeMillis(),
										"New Faction Home", "Faction Home set to " + f.getHomebaseRealName() + " by " + fk.initiator + ".", 0);
								toAddFactionNewsPosts.add(newHome);
							} else {
								f.setHomebaseUID("");
								f.getHomeSector().set(0, 0, 0);
								f.setHomebaseRealName("");

								sendHomeBaseChange(fk.initiator, fk.factionId, "", new Vector3i(), "");

								FactionNewsPost revokation = new FactionNewsPost();
								if (fk.admin) {
									revokation.set(fk.factionId, "ADMIN", System.currentTimeMillis(),
											Lng.str("Faction Home abandoned"), 
											Lng.str("Faction Home has been reset, because faction block has been removed.\n\n "
													+ "If that was unintended, please ask an admin who last\nedited the Station"), 0);
								} else {
									revokation.set(fk.factionId, fk.initiator, System.currentTimeMillis(),
											Lng.str("Faction Home abandoned"), Lng.str("Faction Home has been unset by %s.", fk.initiator), 0);
								}
								toAddFactionNewsPosts.add(revokation);

							}
						} else {
							f.setHomebaseUID(new String(fk.baseUID));
							f.getHomeSector().set(new Vector3i(fk.homeVector));
							f.setHomebaseRealName(new String(fk.realName));

							if (((GameClientState) gameState.getState()).getPlayer().getFactionId() == fk.factionId) {
								if (fk.baseUID.length() > 0) {
									((GameClientState) gameState.getState()).getController().popupGameTextMessage(Lng.str("Your faction has a new home:\n%s", fk.baseUID) , 0);
								} else {
									((GameClientState) gameState.getState()).getController().popupGameTextMessage(Lng.str("Your faction has abandoned\nits home!"), 0);
								}
							}
						}
					} else {
						System.err.println("[FACTION][HOMEBASE][ERROR] faction not found " + fk.factionId);
					}
				}
			}
		}
	}

	private void sendServerFactionMail(String topic, String message, Faction to) {
		assert (gameState.getState() instanceof GameServerState);
		GameServerState state = ((GameServerState) gameState.getState());
		for (String name : to.getMembersUID().keySet()) {
			state.getServerPlayerMessager().send("[FACTION]", name, topic, message);
		}
	}

	public void updateToFullNetworkObject(NetworkGameState networkObject) {
		synchronized (factionMap) {
			//			System.err.println("[FULLUPDATE] SENDING ALL FACTIONS "+factionMap);
			for (Faction f : factionMap.values()) {
				//				System.err.println("[FULLUPDATE] SENDING FACTION "+f);
				networkObject.factionAdd.add(new RemoteFaction(f, networkObject));
				TreeSet<FactionNewsPost> treeSet = news.get(f.getIdFaction());
				if (treeSet != null) {
					//					for(FactionNewsPost fnp : treeSet){
					////						System.err.println("[SERVER][FACTIONMANAGER][FULLUPDATE] sending news "+fnp);
					//						networkObject.factionNewsPosts.add(new RemoteFactionNewsPost(fnp, networkObject));
					//					}
				} else {
					//					System.err.println("[SERVER][FACTIONMANAGER][FULLUPDATE] no news to send for "+f.getIdFaction());
				}

			}
		}
		synchronized (factionInvitations) {
			for (FactionInvite f : factionInvitations) {
				gameState.getNetworkObject().factionInviteAdd.add(new RemoteFactionInvitation(f, gameState.getNetworkObject()));
			}
		}
		synchronized (relationShipOffers) {
			for (FactionRelationOffer f : relationShipOffers.values()) {
				gameState.getNetworkObject().factionRelationshipOffer.add(f.getRemoteArrayOffer(gameState.getNetworkObject()));
			}
		}

		synchronized (relations) {
			for (FactionRelation f : relations.values()) {
				if(f.rel != RType.NEUTRAL.code) {
					//dont send neutral relations to save bandwidth
					gameState.getNetworkObject().factionRelationships.add(f.getRemoteArray(gameState.getNetworkObject()));
				}
			}
		}
		npcFactionNews.updateToFullNetworkObject(networkObject);
	}

	public void updateToNetworkObject(NetworkGameState networkObject) {
		npcFactionNews.updateFromNetworkObject(networkObject);
	}

	/**
	 * @return the changedFactions
	 */
	public ArrayList<FactionMod> getChangedFactions() {
		return changedFactions;
	}

	public void forceFactionPointTurn() {
		lastupdate = 0;
	}

	public void resetAllActivity(int id) throws FactionNotFoundException {
		if (!existsFaction(id)) {
			throw new FactionNotFoundException(id);
		} else {
			Faction faction = getFaction(id);
			for (FactionPermission m : faction.getMembersUID().values()) {
				m.activeMemberTime = 0;
				faction.addOrModifyMember("ADMIN", m.playerUID, m.role, m.activeMemberTime, gameState, true);
			}
		}

	}

	public void addNewsPostServer(FactionNewsPost n) {
		assert (gameState.isOnServer());
		synchronized (toAddFactionNewsPosts) {
			toAddFactionNewsPosts.add(n);
		}
	}

	public void addFactionSystemOwnerChangeServer(FactionSystemOwnerChange fk) {
		assert (gameState.isOnServer());
		synchronized (factionSystemOwnerToMod) {
			factionSystemOwnerToMod.add(fk);
		}
	}

	public boolean isInFaction(String name, int factionId) {
		Faction f = getFaction(factionId);
		return f != null && f.getMembersUID().containsKey(name);
	}

	public String getFactionName(int factionId) {
		return (existsFaction(factionId) ? getFaction(factionId).getName() : Lng.str("Neutral"));
	}
	public void setNPCFactionChanged(){
		this.npcFactionChanged = true;
	}
	
	
	public void checkNPCFactionSendingDebug(boolean force){
		if(npcFactionChanged || force){
			for(Faction f : factionMap.values()){
				if(f instanceof NPCFaction){
					NPCFaction nf = (NPCFaction)f;
					
					nf.checkNPCFactionSendingDebug(((GameStateInterface)getState()).getGameState(), force);
				}
			}
			if(!force){
				npcFactionChanged = false;
			}
		}
	}
	public static boolean isNPCFactionOrPirateOrTrader(int fid) {
		return fid < 0;
	}
	public static boolean isNPCFaction(int fid) {
		return fid >= FactionManager.NPC_FACTION_START && fid < FactionManager.NPC_FACTION_END;
	}
	public void scheduleActionCheck(int idFaction) {
		actionCheck.add(idFaction);
	}
	public ArrayList<FactionRelationOfferAcceptOrDecline> getToAddFactionRelationOfferAccepts() {
		return toAddFactionRelationOfferAccepts;
	}
	public ObjectArrayList<PersonalEnemyMod> getToModPersonalEnemies() {
		return toModPersonalEnemies;
	}
	public ArrayList<FactionRelationOffer> getRelationOffersToAdd() {
		return relationOffersToAdd;
	}
	public NPCFactionNews getNpcFactionNews() {
		return npcFactionNews;
	}
	@Override
	public void onSectorAdded(Sector sec) {
	}
	@Override
	public void onSectorRemoved(Sector sec) {
	}
	
	@Override
	public void onSectorEntityAdded(SimpleTransformableSendableObject s, Sector sector) {
		for(Faction fac : getFactionCollection()){
			fac.onAddedSectorSynched(sector);
		}
		
	}
	@Override
	public void onSectorEntityRemoved(SimpleTransformableSendableObject s, Sector sector) {
		for(Faction fac : getFactionCollection()){
			fac.onRemovedSectorSynched(sector);
		}		
	}
	public static String getFactionName(SimpleTransformableSendableObject<?> segCon) {
		FactionManager factionManager = ((FactionState)segCon.getState()).getFactionManager();
		Faction faction = factionManager.getFaction(segCon.getFactionId());
		return faction != null ? faction.getName() : Lng.str("None");
	}
	public void flagHomeBaseChanged(Faction faction) {
		flagHomebaseChanged = faction;
	}

}
