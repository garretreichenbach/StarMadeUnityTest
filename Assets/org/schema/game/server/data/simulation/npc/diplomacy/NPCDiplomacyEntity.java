package org.schema.game.server.data.simulation.npc.diplomacy;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.schema.common.util.LogInterface;
import org.schema.common.util.StringTools;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.faction.FactionRelationOffer;
import org.schema.game.common.data.player.faction.FactionRelationOfferAcceptOrDecline;
import org.schema.game.common.data.player.faction.PersonalEnemyMod;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFactionConfig;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

public class NPCDiplomacyEntity implements LogInterface{
	
	private int points;
	private int pointCached;
	
	private int fid;

	private long dbId;
	
	private final Byte2ObjectOpenHashMap<DiplomacyAction> actions = new Byte2ObjectOpenHashMap<DiplomacyAction>();

	private final FactionState state;
	
	public NPCDiplomacyEntity(FactionState state, int fid, long dbId) {
		this(state, fid);
		this.dbId = dbId;
		
		
	}
	private NPCDiplomacy dCache;
	public NPCDiplomacyEntity(FactionState state, int fid) {
		this.fid = fid;
		this.state = state;
	}
	public boolean isSinglePlayer(){
		return dbId >= Integer.MAX_VALUE;
	}
	public boolean isFaction(){
		return dbId < Integer.MAX_VALUE;
	}
	public NPCDiplomacy getDiplomacy(){
		if(dCache == null){
			dCache = ((NPCFaction)state.getFactionManager().getFaction(fid)).getDiplomacy();
		}
		return dCache;
	}
	public boolean isFactionLoaded() {
		return ((NPCFaction)state.getFactionManager().getFaction(fid)) != null;
	}
	private boolean changed;

	private final Object2ObjectOpenHashMap<DiplActionType, NPCDiplTurnModifier> dynamicMap = new Object2ObjectOpenHashMap<DiplActionType, NPCDiplTurnModifier>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public NPCDiplTurnModifier remove(Object k) {
			if(!(k == null || k instanceof DiplActionType)){
				throw new IllegalArgumentException();
			}
			
			return super.remove(k);
		}

		@Override
		public NPCDiplTurnModifier get(Object k) {
			if(!(k == null || k instanceof DiplActionType)){
				throw new IllegalArgumentException();
			}
			return super.get(k);
		}

		@Override
		public boolean containsKey(Object k) {
			if(!(k == null || k instanceof DiplActionType)){
				throw new IllegalArgumentException();
			}
			return super.containsKey(k);
		}
		
	};
	private final Object2ObjectOpenHashMap<DiplStatusType, NPCDiplStaticModifier> staticMap = new Object2ObjectOpenHashMap<DiplStatusType, NPCDiplStaticModifier>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public NPCDiplStaticModifier remove(Object k) {
			if(!(k == null || k instanceof DiplStatusType)){
				throw new IllegalArgumentException();
			}
			return super.remove(k);
		}

		@Override
		public NPCDiplStaticModifier get(Object k) {
			if(!(k == null || k instanceof DiplStatusType)){
				throw new IllegalArgumentException();
			}
			return super.get(k);
		}

		@Override
		public boolean containsKey(Object k) {
			if(!(k == null || k instanceof DiplStatusType)){
				throw new IllegalArgumentException();
			}
			return super.containsKey(k);
		}
		
	};

	public void setChanged(){
		this.changed = true;
	}
	
	public int getPoints(){
		if(!getFaction().isOnServer()){
			//from NT
			return pointCached;
		}
		if(changed){
			pointCached = recalcPoints();
			setNTChanged();
			changed = false;
		}
		return pointCached;
	}
	public int getRawPoints() {
		return points;
	}
	private void setNTChanged() {
		if(state instanceof ServerStateInterface){
			getDiplomacy().ntChanged(dbId);
			
		}
	}
	private int recalcPoints() {
		int p = points;
		for( NPCDiplStaticModifier e : staticMap.values()){
			p += e.value;
		}
		return  Math.max(getConfig().diplomacyMinPoints, Math.min(getConfig().diplomacyMaxPoints, p));
	}
	
	public enum DiplStatusType{
		IN_WAR(en -> {
			return Lng.str("At war with them");
		}),
		IN_WAR_WITH_ENEMY(en -> {
			return Lng.str("At war with their enemies");
		}),
		CLOSE_TERRITORY(en -> {
			return Lng.str("Your territories are close");
		}),
		POWER(en -> {
			return Lng.str("They see a difference in power");
		}),
		PIRATE(en -> {
			return Lng.str("They are pirates");
		}),
		ALLIANCE(en -> {
			return Lng.str("You are in an alliance");
		}),
		ALLIANCE_WITH_ENEMY(en -> {
			return Lng.str("You're in an alliance with their enemies");
		}),
		NON_AGGRESSION(en -> {
			return Lng.str("No agression");
		}),
		ALLIANCE_WITH_FRIENDS(en -> {
			return Lng.str("You're in an alliance with their friends");
		}),
		IN_WAR_WITH_FRIENDS(en -> {
			return Lng.str("You're in at war with their friends");
		}),
		FACTION_MEMBER_AT_WAR_WITH_US(en -> {
			return Lng.str("One or more of your faction members is at war with us");
		}),
		FACTION_MEMBER_WE_DONT_LIKE(en -> {
			return Lng.str("We don't like one or more of your faction members");
		}),;
		
		private Translatable description;
		
		private DiplStatusType(Translatable description){
			this.description = description;
		}
		public String getDescription() {
			return description.getName(this);
		}
		public static String list() {
			return StringTools.listEnum(DiplStatusType.values());
		}
	}
	
	public void calculateStaticModifiers(long timeElapsed){
			for(DiplStatusType s : DiplStatusType.values()){
				if(getConfig().existsStatus(s)){
					calculateStaticModifier(timeElapsed, staticMap, s);
				}
			}
		
		
		for(NPCDiplStaticModifier m : staticMap.values()){
			switch(m.type){
			case ALLIANCE:
				break;
			case ALLIANCE_WITH_ENEMY:
				break;
			case ALLIANCE_WITH_FRIENDS:
				break;
			case CLOSE_TERRITORY:
				break;
			case IN_WAR:
				break;
			case IN_WAR_WITH_ENEMY:
				break;
			case IN_WAR_WITH_FRIENDS:
				break;
			case NON_AGGRESSION:
				break;
			case PIRATE:
				break;
			case POWER:
				break;
			default:
				break;
			
			}
		}
		checkReactions();
		
		setChanged();
		setNTChanged();
	}
	private Int2BooleanOpenHashMap reactionsFired = new Int2BooleanOpenHashMap();   
	
	private void checkReactions() {
		List<DiplomacyReaction> reactions = getConfig().getDiplomacyReactions();
		for(DiplomacyReaction r : reactions){
			if(r.isSatisfied(this)){
				boolean executed = executeReaction(r);
				if(executed){
					reactionsFired.put(r.index, true);
				}else{
					reactionsFired.remove(r.index);	
				}
			}else{
				reactionsFired.remove(r.index);
			}
		}
	}
	private boolean executeReaction(DiplomacyReaction r) {
		switch(r.reaction) {
			case ACCEPT_ALLIANCE_OFFER -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.FRIEND.code && k.b == getFaction().getIdFaction()) {
							FactionRelationOfferAcceptOrDecline rl = new FactionRelationOfferAcceptOrDecline("ADMIN", k.getCode(), true);
							state.getFactionManager().getToAddFactionRelationOfferAccepts().add(rl);
							log("Faction accepted alliance offer", LogLevel.NORMAL);
							return true;
						}
					}
				}
				return false;
			}
			case REJECT_ALLIANCE_OFFER -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.FRIEND.code && k.b == getFaction().getIdFaction()) {
							FactionRelationOfferAcceptOrDecline rl = new FactionRelationOfferAcceptOrDecline("ADMIN", k.getCode(), false);
							state.getFactionManager().getToAddFactionRelationOfferAccepts().add(rl);
							log("Faction rejected alliance offer", LogLevel.NORMAL);
							return true;
						}
					}
				}
				return false;
			}
			case ACCEPT_PEACE_OFFER -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.NEUTRAL.code && k.b == getFaction().getIdFaction()) {
							FactionRelationOfferAcceptOrDecline rl = new FactionRelationOfferAcceptOrDecline("ADMIN", k.getCode(), true);
							state.getFactionManager().getToAddFactionRelationOfferAccepts().add(rl);
							log("Faction accepted peace offer", LogLevel.NORMAL);
							return true;
						}
					}
				}
				return true;
			}
			case REJECT_PEACE_OFFER -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.FRIEND.code && k.b == getFaction().getIdFaction()) {
							FactionRelationOfferAcceptOrDecline rl = new FactionRelationOfferAcceptOrDecline("ADMIN", k.getCode(), false);
							state.getFactionManager().getToAddFactionRelationOfferAccepts().add(rl);
							log("Faction rejected peace offer", LogLevel.NORMAL);
							return true;
						}
					}
				}
				return false;
			}
			case DECLARE_WAR -> {
				if(getFaction().getRelationshipWithFactionOrPlayer(dbId) != RType.ENEMY) {
					getFaction().declareWarAgainstEntity(dbId);
					return true;
				}
				return false;
			}
			case OFFER_ALLIANCE -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.FRIEND.code && k.b == getFaction().getIdFaction()) {
							//already exists
							return false;
						}
					}
					if(getFaction().getRelationshipWithFactionOrPlayer(dbId) != RType.ENEMY && getFaction().getRelationshipWithFactionOrPlayer(dbId) != RType.FRIEND) {
						for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
							if(k.rel == RType.FRIEND.code && k.b == getFaction().getIdFaction()) {
								return false;
							}
						}
						FactionRelationOffer offer = new FactionRelationOffer();
						offer.set("ADMIN", getFaction().getIdFaction(), (int) dbId, RType.NEUTRAL.code, "Since you haven't been attacking for some time, we wanted to make peace", false);
						FactionRelationOffer curOffer = state.getFactionManager().getRelationShipOffers().get(offer.getCode());
						if(curOffer == null || !curOffer.isNeutral()) {
							state.getFactionManager().getRelationOffersToAdd().add(offer);
						}
						return true;
					}
				}
				return false;
			}
			case OFFER_PEACE_DEAL -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.NEUTRAL.code && k.b == getFaction().getIdFaction()) {
							//already exists
							return false;
						}
					}
				}
				if(getFaction().getRelationshipWithFactionOrPlayer(dbId) == RType.ENEMY) {
					if(isSinglePlayer()) {
						String player = ((GameServerState) state).getPlayerNameFromDbIdLowerCase(dbId);
						PersonalEnemyMod mod = new PersonalEnemyMod("npc", player, getFaction().getIdFaction(), false);
						if(!state.getFactionManager().getToModPersonalEnemies().contains(mod)) {
							state.getFactionManager().getToModPersonalEnemies().add(mod);
							((GameServerState) state).getServerPlayerMessager().send(getFaction().getName(), player, Lng.str("Peace"), Lng.str("Since you haven't been attacking for some time, we will no longer consider you an enemy!"));
						}
					} else if(isFaction()) {
						FactionRelationOffer offer = new FactionRelationOffer();
						offer.set("ADMIN", getFaction().getIdFaction(), (int) dbId, RType.NEUTRAL.code, "Since you haven't been attacking for some time, we wanted to make peace", false);
						FactionRelationOffer curOffer = state.getFactionManager().getRelationShipOffers().get(offer.getCode());
						if(curOffer == null || !curOffer.isNeutral()) {
							if(!state.getFactionManager().getRelationOffersToAdd().contains(offer)) {
								state.getFactionManager().getRelationOffersToAdd().add(offer);
							}
						}
					}
				}
				return true;
			}
			case REMOVE_ALLIANCE_OFFER -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.FRIEND.code && k.b == getFaction().getIdFaction()) {
							FactionRelationOffer rej = new FactionRelationOffer();
							rej.set("ADMIN", k.a, k.b, k.rel, "We revoke our offer!", true);
							((GameServerState) getFaction().getState()).getFactionManager().relationShipOfferServer(rej);
							return true;
						}
					}
				}
				return false;
			}
			case REMOVE_PEACE_DEAL_OFFER -> {
				if(isFaction()) {
					for(FactionRelationOffer k : ((GameServerState) getFaction().getState()).getFactionManager().getRelationShipOffers().values()) {
						if(k.rel == RType.NEUTRAL.code && k.b == getFaction().getIdFaction()) {
							FactionRelationOffer rej = new FactionRelationOffer();
							rej.set("ADMIN", k.a, k.b, k.rel, "We revoke our offer!", true);
							((GameServerState) getFaction().getState()).getFactionManager().relationShipOfferServer(rej);
							return true;
						}
					}
				}
				return false;
			}
			case SEND_POPUP_MESSAGE -> {
				if(!reactionsFired.containsKey(r.index)) {
					if(isSinglePlayer()) {
						PlayerState playerState = ((GameServerState) state).getPlayerStatesByDbId().get(dbId);
						if(playerState != null) {
							playerState.sendServerMessagePlayerError(Lng.astr("Personal Diplomacy Message:\n%s", r.message));
						}
					} else {
						if((int) dbId > 0) {
							Faction f = state.getFactionManager().getFaction((int) dbId);
							if(f != null && r.message != null) {
								f.broadcastMessage(Lng.astr("Diplomacy Message to your faction:\n%s", r.message), ServerMessage.MESSAGE_TYPE_ERROR, (GameServerState) state);
							}
						}
					}
				}
				return true;
			}
			default -> {
			}
		}
		
		
		return false;
	}
	
	
	public void applyDynamicModifiers(long timeElapsed){
		int pointsBef = getPoints();
		for(NPCDiplTurnModifier e : dynamicMap.values()){
			modPoints(e.pointsPerTurn);
		}
		setChanged();
		if(pointsBef != getPoints()){
			onPointsChanged(pointsBef, getPoints());
		}
		
	}
	private void onPointsChanged(int pointsBef, int pointsNow) {
		//use for reaction based on points
		
		setNTChanged();
	}
	public boolean existsStatusModifier(DiplStatusType status){
		NPCDiplStaticModifier mod = staticMap.get(status);
		
		return mod != null && mod.value != 0;
	}
	public long persistedStatusModifier(DiplStatusType status){
		NPCDiplStaticModifier mod = staticMap.get(status);
		
		return mod == null ? -1 : mod.totalTimeApplied;
	}
	public long persistedActionModifier(DiplActionType action){
		NPCDiplTurnModifier mod = dynamicMap.get(action);
		
		return mod == null ? -1 : mod.totalElapsedTime;
	}
	private void calculateStaticModifier(long timeElapsed, Object2ObjectOpenHashMap<DiplStatusType, NPCDiplStaticModifier> map, DiplStatusType status){
		
		float intensity = calculateStatus(status);
		
//		if(dbId >= Integer.MAX_VALUE){
//			assert(status != DiplStatusType.IN_WAR || intensity == 0):status.name()+": "+intensity;
//		}
		
		int value = getConfig().getDiplomacyValue(status);
		
		
		NPCDiplStaticModifier mod = map.get(status);
		
		
		
		if(intensity > 0){
			if(mod == null){
				mod = new NPCDiplStaticModifier();
				mod.type = status;
				mod.value = (int) Math.ceil(value * intensity);
				mod.totalTimeApplied = 0;
				map.put(status, mod);
			}else{
				mod.elapsedTimeInactive = 0;
				mod.totalTimeApplied += timeElapsed;
			}
		}else{
			if(mod != null){
				mod.elapsedTimeInactive += timeElapsed;
				if(mod.type == DiplStatusType.NON_AGGRESSION || mod.elapsedTimeInactive > getConfig().getDiplomacyStaticTimpout(status)){
					map.remove(status);
				}
			}
		}
		if(mod != null){
			log("Faction static modifier: "+mod.type.name()+"; Value: "+value, LogLevel.DEBUG);
		}
		
	}
	public float getStatusPoints(DiplStatusType status) {
		NPCDiplStaticModifier mod = staticMap.get(status);
		return mod == null ? -1 : mod.value;
	}
	public float calculateStatus(DiplStatusType status) {
		RType rel = getDiplomacy().faction.getRelationshipWithFactionOrPlayer(dbId);
		
//		if(dbId >= Integer.MAX_VALUE){
//			System.err.println("CHECKING STATUS "+status.name()+": "+rel+" from "+getDiplomacy().faction.getIdFaction()+" --- "+dbId);
//		}
		switch(status){
		case ALLIANCE:
			
			if(rel == RType.FRIEND){
				return 1;
			}else{
				return 0;
			}
		
		case ALLIANCE_WITH_ENEMY:
			if(isFaction()){
				List<Faction> enemies = getFaction().getEnemies();
				int alliances = 0;
				for(Faction f : enemies){
					if(f.getIdFaction() != dbId){
						RType orel = f.getRelationshipWithFactionOrPlayer(dbId);
						if(orel == RType.FRIEND){
							alliances++;
						}
					}
				}
				
				return alliances;
			}else{
				return 0;
			}
		case CLOSE_TERRITORY:
			DiplomacyAction dd = actions.get((byte)DiplActionType.TERRITORY.ordinal());
			if(dd != null && dd.counter > 0){
				return 1;
			}else{
				return 0;
			}
		case IN_WAR:
			if(rel == RType.ENEMY){
//				System.err.println("RETURNING 1::::: "+rel);
				return 1;
			}else{
//				System.err.println("RETURNING 0::::: "+rel);
				return 0;
			}
		case IN_WAR_WITH_ENEMY:
			List<Faction> enemies = getFaction().getEnemies();
			int wars = 0;
			for(Faction f : enemies){
				if((f.isNPC() || f.isPlayerFaction()) && f.getIdFaction() != dbId){
					RType orel = f.getRelationshipWithFactionOrPlayer(dbId);
					if(orel == RType.ENEMY){
						wars++;
					}
				}
			}
			return wars;
		case ALLIANCE_WITH_FRIENDS:
			if(isFaction()){
				List<Faction> friends = getFaction().getFriends();
				int friendsWFriends = 0;
				for(Faction f : friends){
					if((f.isNPC() || f.isPlayerFaction()) && f.getIdFaction() != dbId){
						RType orel = f.getRelationshipWithFactionOrPlayer(dbId);
						if(orel == RType.FRIEND){
							friendsWFriends++;
						}
					}
				}
				return friendsWFriends;
			}else{
				return 0;
			}
		case IN_WAR_WITH_FRIENDS:
			List<Faction> friends = getFaction().getFriends();
			int warsWFriends = 0;
			for(Faction f : friends){
				if((f.isNPC() || f.isPlayerFaction()) && f.getIdFaction() != dbId){
					RType orel = f.getRelationshipWithFactionOrPlayer(dbId);
					if(orel == RType.ENEMY){
						warsWFriends++;
					}
				}
			}
			return warsWFriends;
		
		case NON_AGGRESSION:
			DiplomacyAction diplomacyAction = actions.get((byte)DiplActionType.ATTACK.ordinal());
			if(diplomacyAction == null || diplomacyAction.counter < 1){
				return 1;
			}else{
				return 0;
			}
		case PIRATE:
			return 1;
		case POWER:
			return 0;
		
		default:
			break;
		
		}
		return 0;
	}
	
	private NPCFaction getFaction() {
		return getDiplomacy().faction;
	}
	private void calculateDynamicModifier(long timeElapsed, Object2ObjectOpenHashMap<DiplActionType, NPCDiplTurnModifier> dynamicMap2, DiplActionType action){
		int upperOrig = getConfig().getDiplomacyUpperLimit(action);
		int lowerOrig = getConfig().getDiplomacyLowerLimit(action);
		
		if((upperOrig == lowerOrig) && lowerOrig == 0){
			//no modification for this action
			return;
		}
		int valueMod = upperOrig > lowerOrig ? 1 : -1;
		int upper = valueMod > 0 ? upperOrig : lowerOrig;
		int lower = valueMod > 0 ? lowerOrig : upperOrig;
		
		
		long timeOut = getConfig().getDiplomacyTurnTimeout(action);
		if(Math.abs(upper - lower) > 0){
			timeOut = 0;
		}
		int value = getConfig().getDiplomacyValue(action);
		int ac = getActionCount(action);
		
		NPCDiplTurnModifier mod = dynamicMap2.get(action);
		
		long delay = getConfig().diplomacyTurnEffectChangeDelay;
		if(ac > 0){
			if(mod == null){
				mod = new NPCDiplTurnModifier();
				mod.type = action;
				mod.pointsPerTurn = value;
				dynamicMap2.put(action, mod);
			}else{
				mod.elapsedTime += timeElapsed;
				mod.totalElapsedTime += timeElapsed;
				
				if(mod.elapsedTime > delay){
					int b = mod.pointsPerTurn;
					int md = valueMod*getConfig().getDiplomacyExistingActionModifier(action);
					mod.pointsPerTurn = Math.max(lower, Math.min(upper, mod.pointsPerTurn+md));
					log("PERSISTING ACTION "+b+" -> "+mod.pointsPerTurn+" ["+lower+", "+upper+"] mod: "+md, LogLevel.DEBUG);
					mod.elapsedTime -= delay;
				}else{
					log("PERSISTING ACTION not triggering yet "+mod.elapsedTime+"/"+delay+" -> "+mod.pointsPerTurn, LogLevel.DEBUG);
				}
			}
		}else{
			if(mod != null){
				mod.elapsedTime += timeElapsed;
				mod.totalElapsedTime += timeElapsed;
				if(mod.elapsedTime > delay){
					int b = mod.pointsPerTurn;
					int md = valueMod*getConfig().getDiplomacyNonExistingActionModifier(action);
					mod.pointsPerTurn = Math.max(lower, Math.min(upper, mod.pointsPerTurn-md));
					mod.elapsedTime -= delay;
					log("NON PERSISTING ACTION "+b+" -> "+mod.pointsPerTurn+" ["+lower+", "+upper+"] mod: "+md+";", LogLevel.DEBUG);
					/*
					 * if there is no upper/lower bound we use timeout
					 * else we remove if upper/lower bounds is reached for positive/negative effect
					 */
					if((timeOut <= 0 && ((valueMod > 0 && mod.pointsPerTurn == upperOrig) || 
							(valueMod < 0 && mod.pointsPerTurn == lowerOrig))) ||
							(timeOut> 0 && mod.totalElapsedTime > timeOut)){
						if(timeOut> 0 && mod.totalElapsedTime > timeOut){
							log("ACTION TIMEOUT! REMOVING; "+mod.totalElapsedTime+" / "+timeOut, LogLevel.DEBUG);
						}else{
							log("LIMIT REACHED! REMOVING; ppt "+mod.pointsPerTurn+" ["+lowerOrig+", "+upperOrig+"]", LogLevel.DEBUG);
						}
						//limit reached -> Remove
						dynamicMap2.remove(action);
					}
				}
			}
		}
		checkReactions();
	}
	public NPCFactionConfig getConfig(){
		return getDiplomacy().faction.getConfig();
	}
	void calculateDiplomacyModifiersFromActions(long timeElapsed){
		ObjectIterator<DiplomacyAction> iterator = actions.values().iterator();
		while(iterator.hasNext()){
			DiplomacyAction a = iterator.next(); 
			if(getConfig().existsAction(a.type)){
				DiplomacyReaction react = getConfig().getDiplomacyActionRequired(a.type);
				if(react != null){
					if(react.isSatisfied(this)){
						//dont enter this reaction into the executed map
						//as it is triggered on action
						executeReaction(react);
					}
				}else{
					calculateDynamicModifier(0, dynamicMap, a.type);	
				}
			}
			
			log("ACTION TIMEOUT: "+a.type.name()+" "+a.timeDuration+" - "+timeElapsed+" = "+(a.timeDuration-timeElapsed), LogLevel.DEBUG);
			a.timeDuration -= timeElapsed;
			if(a.timeDuration <= 0){
				iterator.remove();
			}
		}
		
		for(NPCDiplTurnModifier d : dynamicMap.values()){
			if(getConfig().existsAction(d.type)){
				calculateDynamicModifier(timeElapsed, dynamicMap, d.type);	
			}
		}
		
		setNTChanged();
	}
	
	
	
	public int getActionCount(DiplActionType type){
		DiplomacyAction action = actions.get((byte)type.ordinal());
		return action == null ? 0 : action.counter;
	}
	public void diplomacyAction(DiplActionType type) {
		DiplomacyAction action = actions.get((byte)type.ordinal());
		
		if(action == null){
			action = new DiplomacyAction();
			action.type = type;
			actions.put((byte)type.ordinal(), action);
		}
		
		log("Faction action modifier triggered: "+action.type.name(), LogLevel.DEBUG);
		if(isFaction() && (action.type == DiplActionType.ATTACK || action.type == DiplActionType.DECLARATION_OF_WAR)){
			FactionRelationOffer offer = state.getFactionManager().getRelationShipOffers().get(
					FactionRelationOffer.getCode(getFaction().getIdFaction(), (int)dbId));
			
			if(offer != null){
				//remove offer since there was aggression
				
				FactionRelationOffer e = new FactionRelationOffer();
				e.set("npc", getFaction().getIdFaction(), (int)dbId, 
						(byte)offer.getRelation().ordinal(), "msg", true);
				
				state.getFactionManager().getRelationOffersToAdd().add(e);
				log("Faction revoked "+offer.getRelation().name()+" offer because of aggresion", LogLevel.NORMAL);
			}
		}
		//reset action and increase counter
		action.timeDuration = getConfig().getDiplomacyActionTimeout(type);
		action.counter++;
		
		setChanged();
	}
	@Override
	public void log(String string, LogLevel l) {
		getDiplomacy().log("[TO_"+dbId+"]"+string, l);
	}
	public void setPoints(int points) {
		this.points = Math.max(getConfig().diplomacyMinPoints, Math.min(getConfig().diplomacyMaxPoints, points));
	}
	public long getDbId() {
		return dbId;
	}
	public void setDbId(long dbId) {
		this.dbId = dbId;
	}
	public Tag toTag() {
		
		getPoints();
		
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, (byte)0),
				new Tag(Type.LONG, null, dbId),
				new Tag(Type.INT, null, fid),
				new Tag(Type.INT, null, points),
				new Tag(Type.INT, null, pointCached),
				getActionsTag(),
				getDynamicMapTag(),
				getStaticMapTag(),
				getReactionsFiredMapTag(),
				FinishTag.INST
			});
	}
	public void fromTag(Tag tag) {
		Tag[] t = tag.getStruct();
		
		byte version = t[0].getByte();
		dbId = t[1].getLong();
		fid = t[2].getInt();
		points = t[3].getInt();
		pointCached = t[4].getInt();
		fromActionsTag(t[5]);
		fromDynamicMapTag(t[6]);
		fromStaticMapTag(t[7]);
		fromReactionsFiredMap(t[8]);
	}
	private void fromStaticMapTag(Tag tag) {
		Tag[] t = tag.getStruct();
		if((t.length-1) > 300){
			System.err.println("[SERVER][FACTION][NPC][DIPLOMACY] FID: "+fid+"; StaticMap: "+(t.length-1));
		}
		for(int i = 0; i < t.length-1; i++){
			Tag[] m = t[i].getStruct();
			byte key = m[0].getByte();
			NPCDiplStaticModifier s = new NPCDiplStaticModifier();
			s.fromTag(m[1]);
			staticMap.put(DiplStatusType.values()[key], s);
		}
	}
	private void fromReactionsFiredMap(Tag tag) {
		Tag[] t = tag.getStruct();
		if((t.length-1) > 300){
			System.err.println("[SERVER][FACTION][NPC][DIPLOMACY] FID: "+fid+"; ReActions: "+(t.length-1));
		}
		for(int i = 0; i < t.length-1; i++){
			Tag[] m = t[i].getStruct();
			short key = m[0].getShort();
			boolean val = m[1].getByte() != 0;
			reactionsFired.put(key, val);
		}
	}
	private void fromDynamicMapTag(Tag tag) {
		Tag[] t = tag.getStruct();
		if((t.length-1) > 300){
			System.err.println("[SERVER][FACTION][NPC][DIPLOMACY] FID: "+fid+"; DynMap: "+(t.length-1));
		}
		for(int i = 0; i < t.length-1; i++){
			Tag[] m = t[i].getStruct();
			byte key = m[0].getByte();
			NPCDiplTurnModifier s = new NPCDiplTurnModifier();
			s.fromTag(m[1]);
			dynamicMap.put(DiplActionType.values()[key], s);
		}
	}
	private void fromActionsTag(Tag tag) {
		Tag[] t = tag.getStruct();
		if((t.length-1) > 300){
			System.err.println("[SERVER][FACTION][NPC][DIPLOMACY] FID: "+fid+"; Actions: "+(t.length-1));
		}
		for(int i = 0; i < t.length-1; i++){
			Tag[] m = t[i].getStruct();
			byte key = m[0].getByte();
			DiplomacyAction s = new DiplomacyAction();
			s.fromTag(m[1]);
			actions.put(key, s);
		}
	}
	private Tag getActionsTag() {
		Tag[] t = new Tag[actions.size()+1];
		t[t.length-1] = FinishTag.INST;
		
		int i = 0;
		
		for(Entry<Byte, DiplomacyAction> e : actions.entrySet()){
			t[i++] = new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, e.getKey()),
				e.getValue().toTag(),
				FinishTag.INST
			});
		}
		
		return new Tag(Type.STRUCT, null, t);
	}
	private Tag getStaticMapTag() {
		Tag[] t = new Tag[staticMap.size()+1];
		t[t.length-1] = FinishTag.INST;
		
		int i = 0;
		
		for(Entry<DiplStatusType, NPCDiplStaticModifier> e : staticMap.entrySet()){
			t[i++] = new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, (byte)e.getKey().ordinal()),
				e.getValue().toTag(),
				FinishTag.INST
			});
		}
		
		return new Tag(Type.STRUCT, null, t);
	}
	private Tag getReactionsFiredMapTag() {
		Tag[] t = new Tag[reactionsFired.size()+1];
		t[t.length-1] = FinishTag.INST;
		
		int i = 0;
		
		for(Entry<Integer, Boolean> e : reactionsFired.entrySet()){
			t[i++] = new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.SHORT, null, e.getKey().shortValue()),
					new Tag(Type.BYTE, null, e.getValue().booleanValue() ? (byte)1 : (byte) 0),
					FinishTag.INST
			});
		}
		
		return new Tag(Type.STRUCT, null, t);
	}
	private Tag getDynamicMapTag() {
		Tag[] t = new Tag[dynamicMap.size()+1];
		t[t.length-1] = FinishTag.INST;
		
		int i = 0;
		
		for(Entry<DiplActionType, NPCDiplTurnModifier> e : dynamicMap.entrySet()){
			t[i++] = new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, (byte)e.getKey().ordinal()),
				e.getValue().toTag(),
				FinishTag.INST
			});
		}
		
		return new Tag(Type.STRUCT, null, t);
	}
	public void deserialize(DataInputStream stream, int updateSenderStateId,
			boolean onServer) throws IOException {
		actions.clear();
		dynamicMap.clear();
		staticMap.clear();
		Tag t = Tag.deserializeNT(stream);
		fromTag(t);
	}
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		Tag t = toTag();
		t.serializeNT(buffer);
	}
	@Override
	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append("ENTITY: "+dbId+"\n");
		b.append("Points: "+pointCached+" (Raw: "+points+")\n");
		b.append("Actions: \n");
		for(DiplomacyAction a : actions.values()){
			b.append("  "+a.type.name()+"x"+a.counter+"; Duration "+a.timeDuration+"\n");
		}
		b.append("Dynamic: \n");
		for(NPCDiplTurnModifier a : dynamicMap.values()){
			b.append("  "+a.type.name()+"; PPT: "+a.pointsPerTurn+";\n");
		}
		b.append("Static: \n");
		for(NPCDiplStaticModifier a : staticMap.values()){
			b.append("  "+a.type.name()+"; MOD: "+a.value+";\n");
		}
		
		return b.toString();
	}
	public Object2ObjectOpenHashMap<DiplActionType, NPCDiplTurnModifier> getDynamicMap() {
		return dynamicMap;
	}
	public Object2ObjectOpenHashMap<DiplStatusType, NPCDiplStaticModifier> getStaticMap() {
		return staticMap;
	}
	public void triggerReactionManually(DiplomacyReaction r) {
		log("Triggering reaction manually", LogLevel.NORMAL);
		executeReaction(r);
	}
	public void modPoints(int pnts) {
		int prevPoints = getPoints();
		setPoints(points +pnts);
		pointCached = recalcPoints();
		setNTChanged();
		
		//System.err.println("POINT MANUALLY CHANGED for " + state.getFactionManager().getFactionName(fid) + ": " + pnts+"; "+prevPoints+" -> "+getPoints()+"; RAW: "+getRawPoints());
	}
	
	
}
