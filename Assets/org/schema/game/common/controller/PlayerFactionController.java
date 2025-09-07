package org.schema.game.common.controller;

import api.listener.events.faction.FactionCreateEvent;
import api.listener.events.player.PlayerJoinFactionEvent;
import api.listener.events.player.PlayerLeaveFactionEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.FactionChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.FogOfWarController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.faction.*;
import org.schema.game.common.data.player.faction.FactionPermission.PermType;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.faction.config.FactionActivityConfig;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.game.network.objects.NetworkPlayer;
import org.schema.game.network.objects.remote.RemoteFaction;
import org.schema.game.network.objects.remote.RemoteFactionNewsPost;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ProtectedUplinkName;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteIntegerArray;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import java.util.*;
import java.util.Map.Entry;

public class PlayerFactionController extends GUIObservable implements TagSerializable, FactionChangeListener {

	public static final int MAX_NEWS_REQUEST_BATCH = 5;
	public static final String factionTagVersion0 = "pFac-v0";
	private static final int ENCO = -9278389;
	private final PlayerState playerState;
	private final List<String> descriptionEdits = new ObjectArrayList<String>();
	private final List<String> chatMsgsRequests = new ObjectArrayList<String>();
	private final List<Faction> factionsToAdd = new ObjectArrayList<Faction>();
	private final List<int[]> factionChanges = new ObjectArrayList<int[]>();
	private final Set<FactionInvite> invitesIncomingDisplayed = new HashSet<FactionInvite>();
	private final List<FactionInvite> invitesIncoming = new ObjectArrayList<FactionInvite>();
	private final List<FactionInvite> invitesOutgoing = new ObjectArrayList<FactionInvite>();
	private final List<FactionRelationOffer> relationshipInOffers = new ObjectArrayList<FactionRelationOffer>();
	private final List<FactionRelationOffer> relationshipOutOffers = new ObjectArrayList<FactionRelationOffer>();
	private final List<ServerNewsRequest> serverNewsRequests = new ObjectArrayList<ServerNewsRequest>();
	private final List<FactionChange> changedFactionFrom = new ObjectArrayList<FactionChange>();
	
	private int factionId;
	private int openToJoinRequests = -1;
	private int attackNeutralRequests = -1;
	private int autoDeclareWarRequest = -1;
	private int flagLeave;
	private long lastFactionCreationTime;
	private boolean needsInviteReorganization = true;
	private FactionManager factionManager;
	private int flagJoin;
	private boolean needsRelationshipOfferReorganization = true;
	private String factionString = "";
	private boolean factionChanged;
	private boolean forcedJoin;
	private long lastFacActiveUpdate;
	private byte rank = 0;
	private boolean factionNewsReorganization;
	private long lastPerm;
	private int factionShareFowBuffer;
	private boolean checkedFleets;
	private int currentFactionEnc;
	private int suspended = 0;

	
	
	
	
	public PlayerFactionController(PlayerState playerState) {
		super();
		this.playerState = playerState;

	}

	public boolean chatClient(String entry) {
		playerState.getNetworkObject().factionChatRequests.add(new RemoteString(entry, playerState.getNetworkObject()));
		return true;
	}

	public void cleanUp() {
		((FactionState) playerState.getState()).getFactionManager().listeners.remove(this);
	}

	public void clientCreateFaction(String name, String description) {
		assert (!isOnServer());

		if (System.currentTimeMillis() - lastFactionCreationTime > 10000) {
			Faction f = new Faction(getState(), 0,  name, description);
			playerState.getNetworkObject().factionCreateBuffer.add(new RemoteFaction(f, playerState.getNetworkObject()));
			lastFactionCreationTime = System.currentTimeMillis();
		} else {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Error:\nSpam protection:\nplease wait 10 seconds\nuntil you can create\nanother faction."), 0);
		}

	}

	public boolean editDescriptionClient(String entry) {
		playerState.getNetworkObject().factionDescriptionEditRequest.add(new RemoteString(entry, playerState.getNetworkObject()));
		return true;
	}
	public void shareFow(int to) {
		playerState.getNetworkObject().factionShareFowBuffer.add(to);
	}
	/**
	 * @param flagJoin the flagJoin to set
	 */
	public void forceJoinOnServer(int flagJoin) {
		this.flagJoin = flagJoin;
		this.forcedJoin = true;
	}
	public void unsuspendFaction(GameServerState state, PlayerState p) {
		if(suspended != 0 && state.getFactionManager().existsFaction(factionId)) {
			
			
			String fromName = p.getName()+"|"+suspended;
			String toName = p.getName();
			Faction faction = state.getFactionManager().getFaction(suspended);
			if(faction == null) {
				System.err.println("[SERVER][UNSUSPENDFACTION] WARNING: faction the player was suspended from does no longer exist");
				suspended = 0;
				return;
			}
			FactionPermission factionPermission = faction.getMembersUID().get(fromName);
			
			if(factionPermission == null) {
				System.err.println("[SERVER][UNSUSPENDFACTION] WARNING: faction the player was suspended from removed the suspended membership. player needs to rejoin manually");
				suspended = 0;
				return;
			}
			
			Faction currentFac = state.getFactionManager().getFaction(factionId);
			if(currentFac != null) {
				System.err.println("[SERVER][UNSUSPENDFACTION] Player was still in another faction. Player will be removed from "+currentFac+" to rejoin "+faction);
				currentFac.removeMember(p.getName(), state.getGameState());
			}
			
			factionPermission.playerUID = toName;
			
			faction.sendMemberNameChangeMod(fromName, toName, state.getGameState());
			
			setFactionId(0);
			needsInviteReorganization = true;
			needsRelationshipOfferReorganization = true;
			
			suspended = faction.getIdFaction();
		}else {
			System.err.println("[SERVER][UNSUSPENDFACTION] ERROR: player "+p+" is not suspended from any faction");
		}
	}

	public void suspendFaction(GameServerState state, PlayerState p) {
		if(suspended == 0 && state.getFactionManager().existsFaction(factionId)) {
			
			
			String fromName = p.getName();
			String toName = p.getName()+"|"+suspended;
			Faction faction = state.getFactionManager().getFaction(factionId);
			FactionPermission factionPermission = faction.getMembersUID().get(fromName);
			
			factionPermission.playerUID = toName;
			
			faction.sendMemberNameChangeMod(fromName, toName, state.getGameState());
			
			setFactionId(0);
			needsInviteReorganization = true;
			needsRelationshipOfferReorganization = true;
			
			suspended = faction.getIdFaction();
		}else {
			System.err.println("[SERVER][SUSPENDFACTION] ERROR: player "+p+" already suspended for faction id "+suspended);
		}
	}
	private int getSuspended() {
		return suspended;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		if (factionTagVersion0.equals(tag.getName())) {
				Tag[] d = tag.getStruct();
				factionId = d[0].getInt();
			if(d[1].getType() == Type.INT) {
				suspended = d[1].getInt();
			}
			System.err.println("[SERVER] loaded faction for " + playerState + " -> " + factionId);
		} else {
			System.err.println("[Player Tag Controller][ERROR] Unknown tag version " + tag.getName());
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag[] d = new Tag[3];

		d[0] = new Tag(Type.INT, null, factionId);
		d[1] = new Tag(Type.INT, null, suspended);
		d[2] = FinishTag.INST;

		Tag root = new Tag(Type.STRUCT, factionTagVersion0, d);
		return root;
	}

	public int getFactionId() {
		return factionId;
	}

	public void setFactionId(int factionCode) {
		int old = this.factionId;
		this.factionId = factionCode;
		this.currentFactionEnc = factionCode + ENCO;
		if (!isOnServer() && old != this.factionId) {
			GameClientState s = ((GameClientState) getState());
			if (this.playerState != s.getPlayer() && this.factionId == s.getPlayer().getFactionId() && this.factionId != 0) {
				s.getController().popupGameTextMessage(Lng.str("%s\njoined your faction.",  playerState.getName()), 0);
			}
			if (this.playerState != s.getPlayer() && old == s.getPlayer().getFactionId() && old != 0) {
				s.getController().popupGameTextMessage(Lng.str("%s\nleft your faction.",  playerState.getName()), 0);
			}
			this.factionChanged = true;
		} else if (isOnServer() && old != this.factionId) {
			this.factionChanged = true;
		}
		if(playerState.isClientOwnPlayer()){
			System.err.println("VERIFY FACTION FOR "+playerState+": "+old+" -> "+this.factionId);
			playerState.sendSimpleCommand(SimplePlayerCommands.VERIFY_FACTION_ID, this.factionId);
		}
		needsInviteReorganization = true;
		needsRelationshipOfferReorganization = true;
	}

	public String getFactionName() {
		if (factionId == 0) {
			return Lng.str("NO FACTION");
		}
		if (!((FactionState) getState()).getFactionManager().existsFaction(factionId)) {
			return Lng.str("...CALCULATING(%s)",  factionId);
		}
		return ((FactionState) getState()).getFactionManager().getFaction(factionId).getName();
	}

	public String getFactionString() {
		return factionString;
	}

	/**
	 * @return the invitesIncoming
	 */
	public List<FactionInvite> getInvitesIncoming() {
		return invitesIncoming;
	}

	/**
	 * @return the invitesOutgoing
	 */
	public List<FactionInvite> getInvitesOutgoing() {
		return invitesOutgoing;
	}

	public FactionPermission getLoadedPermission() throws FactionNotFoundException {
		GameServerState serverState = ((GameServerState) getState());
		Faction faction = serverState.getFactionManager().getFaction(factionId);

		if (faction != null) {
			FactionPermission factionPermission = faction.getMembersUID().get(playerState.getName());
			if (factionPermission != null) {
				return factionPermission;
			} else {
				playerState.sendServerMessage(new ServerMessage(Lng.astr("ERROR\nYou can't change this setting!\n-> not a member!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
				throw new FactionNotFoundException(factionId);
			}
		} else {
			playerState.sendServerMessage(new ServerMessage(Lng.astr("ERROR\nYou can't change this setting!\n-> no faction!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
			throw new FactionNotFoundException(factionId);
		}
	}

	public RType getRelation(int otherFactionId) {
		if (factionManager != null) {

			return factionManager.getRelation(playerState.getName().toLowerCase(Locale.ENGLISH), factionId, otherFactionId);
		} else {
			return RType.NEUTRAL;
		}

	}

	public RType getRelation(PlayerState otherState) {
		if (factionManager != null) {
			return getRelation(otherState.getFactionId());
		} else {
			return RType.NEUTRAL;
		}
	}

	/**
	 * @return the relationshipInOffers
	 */
	public List<FactionRelationOffer> getRelationshipInOffers() {
		return relationshipInOffers;
	}

	/**
	 * @return the relationshipOutOffers
	 */
	public List<FactionRelationOffer> getRelationshipOutOffers() {
		return relationshipOutOffers;
	}

	private StateInterface getState() {
		return playerState.getState();
	}


	public void initFromNetworkObject(NetworkPlayer p) {
		factionId = p.factionId.get();
	}

	public boolean isEnemy(PlayerState otherState) {
		if (factionManager != null) {
			return factionManager.isEnemy(factionId, otherState);
		} else {
			return false;
		}
	}

	public boolean isFriend(PlayerState otherState) {
		if (factionManager != null) {
			return factionManager.isFriend(factionId, otherState.getFactionId());
		} else {
			return false;
		}
	}

	public boolean isNeutral(PlayerState otherState) {
		if (factionManager != null) {
			return factionManager.isNeutral(factionId, otherState.getFactionId());
		} else {
			return false;
		}
	}

	private boolean isOnServer() {
		return playerState.isOnServer();
	}

	public void joinFaction(int factionIdToJoin) {
		playerState.getNetworkObject().factionJoinBuffer.add(factionIdToJoin);
	}

	public void leaveFaction() {
		if (factionId != 0) {
			playerState.getNetworkObject().factionLeaveBuffer.add(factionId);
		}
	}

	public boolean postNewsClient(String topic, String message) {
		if (topic.length() > 0 && message.length() > 0) {
			FactionNewsPost fp = new FactionNewsPost();

			fp.set(factionId, playerState.getName(), System.currentTimeMillis(), topic, message, 0);
			((GameClientState) playerState.getState()).getGameState().getNetworkObject().factionNewsPosts.add(new RemoteFactionNewsPost(fp, ((GameClientState) playerState.getState()).getGameState().getNetworkObject()));
			return true;
		}

		if (topic.length() == 0) {
			((GameClientState) playerState.getState()).getController().popupAlertTextMessage(Lng.str("Can't post news!\nNo topic!"), 0);
		}
		if (message.length() == 0) {
			((GameClientState) playerState.getState()).getController().popupAlertTextMessage(Lng.str("Can't post news!\nNo post body!"), 0);
		}

		return false;
	}

	public void removeNewsClient(FactionNewsPost f) {
		f.setDelete(true);
		((GameClientState) playerState.getState()).getGameState().getNetworkObject().factionNewsPosts.add(new RemoteFactionNewsPost(f, ((GameClientState) playerState.getState()).getGameState().getNetworkObject()));
	}

	public void queueNewsRequestOnServer(NetworkClientChannel networkClientChannel,
	                                     long req) {
		ServerNewsRequest m = new ServerNewsRequest(networkClientChannel, req);
		synchronized (serverNewsRequests) {
			serverNewsRequests.add(m);
		}
	}

	public void reorganizeFactionInvites() {

		invitesIncoming.clear();
		invitesOutgoing.clear();
		for (FactionInvite fi : factionManager.getFactionInvitations()) {
			if (fi.getToPlayerName().equals(playerState.getName())) {
				invitesIncoming.add(fi);
				if (playerState.isClientOwnPlayer()) {
					if (!invitesIncomingDisplayed.contains(fi)) {
						Faction f = factionManager.getFaction(fi.getFactionUID());

						((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("You have been invited\nby %s to join\nthe faction\n%s.",  fi.getFromPlayerName(),  (f != null ? f.getName() : "ERROR(unknownFaction: " + fi.getFactionUID() + ")\n") ), 0);
						invitesIncomingDisplayed.add(fi);
					}
				}
			}
			if (fi.getFromPlayerName().equals(playerState.getName())) {
				if (fi.getFactionUID() == factionId) {
					invitesOutgoing.add(fi);
				} else {
					//the inviter is no longer member of
					//the faction the invitation is for
					factionManager.removeFactionInvitation(fi);
				}
			}
		}
		//		System.err.println("[PlayerFactionManager] reorganized in "+invitesIncoming.size()+", out "+invitesOutgoing.size()+" on "+playerState.getState());
		notifyObservers();
	}

	private void reorganizeFactionRelationShipOffers() {
		relationshipInOffers.clear();
		relationshipOutOffers.clear();
		for (FactionRelationOffer fi : factionManager.getRelationShipOffers().values()) {
			//			System.err.println("[PLAYERFACTIONCONTROLLER] ADD FACTION OFFER "+fi+" / "+getFactionId());
			if (fi.b == factionId) {
				relationshipInOffers.add(fi);
			}
			if (fi.a == factionId) {
				relationshipOutOffers.add(fi);
			}
		}
		//		System.err.println("[PlayerFactionManager] reorganized offers in "+getRelationshipInOffers().size()+", out "+getRelationshipOutOffers().size()+" on "+playerState.getState());
		notifyObservers();
	}

	public void sendEntityFactionIdChangeRequest(int factionId,
	                                             SimpleTransformableSendableObject obj) {
		assert (!playerState.isOnServer());
		RemoteIntegerArray a = new RemoteIntegerArray(2, playerState.getNetworkObject());
		a.set(0, factionId);
		a.set(1, obj.getId());
		playerState.getNetworkObject().factionEntityIdChangeBuffer.add(a);

	}
	public void sendEntityOwnerChangeRequest(String newName, SegmentController segCon) {
		assert(newName != null):"may not be null. to clear owner, submit empty string";
		segCon.getNetworkObject().currentOwnerChangeRequest.add(new RemoteString(newName, isOnServer()));
	}

	public void update(long time) {
		if (factionManager == null && ((FactionState) playerState.getState()).getFactionManager() != null) {
			factionManager = ((FactionState) playerState.getState()).getFactionManager();
			factionManager.listeners.add(this);
		}
//		System.err.println("FactionID: "+factionId+"; "+(this.currentFactionEnc - ENCO));
		if(playerState.isClientOwnPlayer() && factionId != 0 && (this.currentFactionEnc - ENCO) != factionId){
			setFactionId((this.currentFactionEnc - ENCO));
		}
		if (factionManager != null && needsInviteReorganization) {
			reorganizeFactionInvites();
			needsInviteReorganization = false;
		}
		if (factionManager != null && needsRelationshipOfferReorganization) {
			reorganizeFactionRelationShipOffers();
			needsRelationshipOfferReorganization = false;
		}
		if (factionChanged && factionManager != null) {
			if (!isOnServer()) {
				factionChanged = false;
				Faction f = factionManager.getFaction(factionId);
				if (f == null) {
					this.rank = (byte) 0;
					factionString = "";
				} else {
					factionString = "[" + f.getName() + "]";
					if (f.getMembersUID().containsKey(playerState.getName())) {
						this.rank = f.getMembersUID().get(playerState.getName()).role;
					} else {
						factionChanged = true;
					}
				}

			} else {
				//changed on server
			}

		}
		Faction f = factionManager.getFaction(factionId);
		if (f != null && f.getMembersUID().containsKey(playerState.getName())) {
			FactionPermission factionPermission = f.getMembersUID().get(playerState.getName());
			factionPermission.lastSeenPosition.set(playerState.getCurrentSector());
			factionPermission.lastSeenTime = time;
			this.rank = factionPermission.role;
		}

		if (isOnServer()) {
			serverUpdate();

		} else {
			boolean hadShare = (lastPerm & FactionPermission.PermType.FOG_OF_WAR_SHARE.value) == FactionPermission.PermType.FOG_OF_WAR_SHARE.value;
			boolean hasShare = (getFactionPermission() & FactionPermission.PermType.FOG_OF_WAR_SHARE.value) == FactionPermission.PermType.FOG_OF_WAR_SHARE.value;
			
			
			if (factionNewsReorganization) {
				notifyObservers();
				factionNewsReorganization = false;
			}
			
			if(!isOnServer() && factionId != 0 && lastPerm != getFactionPermission() && ((!hadShare && hasShare) || (hadShare && !hasShare))){
				//reset fog of war when own faction permission changes share flag
				((GameClientState)getState()).getController().getClientChannel().getGalaxyManagerClient().resetClientVisibility();
			}
			
			
			if(factionShareFowBuffer != 0){
				//someone shared intel with us
				((GameClientState)getState()).getController().getClientChannel().getGalaxyManagerClient().resetClientVisibility();
				factionShareFowBuffer = 0;
			}
			
			lastPerm = getFactionPermission();
		}
	}

	private void serverUpdate() {

		lastPerm = getFactionPermission();
		if (playerState.getClientChannel() != null && playerState.getClientChannel().isConnectionReady()) {
			//do triggers at the start from last frame
			//to make sure, the faction manager has at least
			//one update
			while (!changedFactionFrom.isEmpty()) {
				onChangedFactionServer(changedFactionFrom.remove(0));
			}
		}

		if(factionShareFowBuffer != 0 ){
			if(hasPermissionEditPermission()){
				Faction f = ((FactionState)getState()).getFactionManager().getFaction(factionShareFowBuffer);
				if(f != null && getFactionFow() != null){
					f.getFogOfWar().merge(factionManager.getFaction(factionId));
					for(FactionPermission pl : f.getMembersUID().values()){
						try {
							PlayerState player = ((GameServerState)getState()).getPlayerFromNameIgnoreCase(pl.playerUID);
							player.getNetworkObject().factionShareFowBuffer.add(factionShareFowBuffer);
						} catch (PlayerNotFountException e) {
						}
					}
					{
						FactionNewsPost n = new FactionNewsPost();
						n.set(factionId, "Fog of war shared", System.currentTimeMillis(), "Fog of war shared", "Faction member " + playerState.getName() + " shared fog of war with faction "+f.getName(), 0);
						((GameServerState) playerState.getState()).getFactionManager().addNewsPostServer(n);
					}
					{
						FactionNewsPost n = new FactionNewsPost();
						n.set(f.getIdFaction(), "Fog of war information received", System.currentTimeMillis(), "Fog of war information received", "Your faction received fog of war information from faction "+getFactionName(), 0);
						((GameServerState) playerState.getState()).getFactionManager().addNewsPostServer(n);
					}

				}
			}else{
				playerState.sendServerMessagePlayerError(Lng.astr("Cannot share Fog of War. Permission denied!\n(Need 'Permission Edit' permission)"));
			}
			factionShareFowBuffer = 0;
		}

		assert (isOnServer());
		if (playerState.getAssingedPlayerCharacter() != null) {
			if (playerState.getAssingedPlayerCharacter().getFactionId() != factionId) {
				playerState.getAssingedPlayerCharacter().setFactionId(factionId);
			}
		}
		if (!factionChanges.isEmpty()) {
			synchronized (factionChanges) {
				while (!factionChanges.isEmpty()) {
					int[] r = factionChanges.remove(0);

					int targetFactionId = r[0];
					int entityId = r[1];

					if (targetFactionId == factionId || targetFactionId == 0) {
						Sendable sendable = playerState.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(entityId);
						System.err.println("[SERVER] received object faction change request " + targetFactionId + " for object " + sendable);
						String targetFactionString = "Neutral";
						Faction targetFaction = factionManager.getFaction(targetFactionId);
						if (targetFactionId != 0 && targetFaction == null) {
							System.err.println("[PlayerFactionController][SERVER][ERROR] target factionId dos not exist " + sendable + "; target: " + targetFactionId);
							targetFactionId = 0;

						}
						if (targetFaction != null) {
							targetFactionString = targetFaction.getName();

						}

						if (sendable instanceof SimpleTransformableSendableObject) {

							SimpleTransformableSendableObject<?> o = (SimpleTransformableSendableObject<?>) sendable;
							boolean owner = (o instanceof SegmentController && ((SegmentController) o).isOwnerSpecific(playerState));
							if (!owner && o instanceof SegmentController && !(((SegmentController) o).isRankAllowedToChangeFaction(targetFactionId, playerState, rank))) {
								if ((o instanceof Planet || o instanceof PlanetIco) && targetFactionId == 0) {
									playerState.sendServerMessagePlayerError(Lng.astr("You don't have the\npermission to reset the faction \nsignature of this object!\nYou need homebase\npermission for this."));
								} else {
									playerState.sendServerMessagePlayerError(Lng.astr("You don't have permission \nto reset the faction signature\nof this object!"));
								}
								continue;
							}

							if (o.getFactionId() == 0 || factionId == o.getFactionId() || !factionManager.existsFaction(o.getFactionId()) || owner) {

								playerState.sendServerMessage(new ServerMessage(Lng.astr("Faction Module:\nchanged signature of %s\nto\n%s",
										o.toNiceString(),  targetFactionString), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
								if(o.getFactionId() != 0 && targetFactionId == 0 && o instanceof SegmentController){
									((SegmentController)o).railController.resetFactionForEntitiesWithoutFactionBlock(o.getFactionId());
								}else{
									o.setFactionId(targetFactionId);
								}
								if(o instanceof SegmentController && targetFactionId == 0){
									((SegmentController)o).currentOwnerLowerCase = "";
								}
							}

						} else {
							System.err.println("[PlayerFactionController][SERVER][ERROR] cant change faction id of etity " + sendable);
						}
					} else {
						System.err.println("[PlayerFactionController][SERVER][ERROR] cant change faction id of entity " + targetFactionId + "/" + factionId);
					}
				}
			}
		}

		if (!serverNewsRequests.isEmpty()) {
			synchronized (serverNewsRequests) {
				while (!serverNewsRequests.isEmpty()) {
					ServerNewsRequest r = serverNewsRequests.remove(0);

					TreeSet<FactionNewsPost> news = factionManager.getNews().get(factionId);
					int i = 0;
					if (news != null && news.size() > 0) {

						for (FactionNewsPost p : news.descendingSet()) {
							if (i >= MAX_NEWS_REQUEST_BATCH) {
								break;
							}
							if (r.req < 0) {
								r.networkClientChannel.factionNewsPosts.add(new RemoteFactionNewsPost(p, r.networkClientChannel));
							} else if (p.getDate() < r.req) {
								r.networkClientChannel.factionNewsPosts.add(new RemoteFactionNewsPost(p, r.networkClientChannel));
								i++;

							}
						}
					} else {
						System.err.println("[SERVER] " + playerState + " FactionNews for " + factionId + " not found!");
					}
				}
			}
		}

		GameServerState serverState = ((GameServerState) getState());

		if (factionId != 0) {
			int old = factionId;
			rank = (byte) 0;

			Faction f = ((GameServerState) getState()).getFactionManager().getFaction(factionId);
			if (f == null) {
				System.err.println("[SERVER][PlayerFactionController][ERROR] faction of player " + playerState + " not found. setting to 0 from " + old);
				playerState.sendServerMessage(new ServerMessage(Lng.astr("You have been removed from\na faction!\n\nFaction does not exist anymore."), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
				setFactionId(0);
			} else {
				if (!f.getMembersUID().containsKey(playerState.getName())) {
					System.err.println("[SERVER][PlayerFactionController][ERROR] faction " + f + " does not contain member " + playerState + ". setting to 0 from " + old);
					playerState.sendServerMessage(new ServerMessage(Lng.astr("You have been removed from\na faction!\n\nNot in member roster,\nyou may have been kicked."), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
					setFactionId(0);
				} else {
					this.rank = f.getMembersUID().get(playerState.getName()).role;
				}
			}
			if (old != factionId) {
				FactionChange c = new FactionChange();
				c.from = old;
				c.to = factionId;
				c.previousPermission = lastPerm;
				changedFactionFrom.add(c);
			}
		}

		if (!descriptionEdits.isEmpty()) {
			synchronized (descriptionEdits) {
				while (!descriptionEdits.isEmpty()) {
					String entry = descriptionEdits.remove(0);

					try {
						FactionPermission fp = getLoadedPermission();
						if (fp.hasDescriptionAndNewsPostPermission(serverState.getFactionManager().getFaction(factionId))) {
							Faction faction = serverState.getFactionManager().getFaction(factionId);
							faction.setDescription(entry);

							faction.sendDescriptionMod(playerState.getName(), entry, serverState.getGameState());
						}

					} catch (FactionNotFoundException e) {
						e.printStackTrace();
					}

				}
			}
		}
		if (!chatMsgsRequests.isEmpty()) {
			synchronized (chatMsgsRequests) {
				while (!chatMsgsRequests.isEmpty()) {
					String entry = chatMsgsRequests.remove(0);
					//TODO
				}
			}
		}

		if (!factionsToAdd.isEmpty()) {
			int old = factionId;
			synchronized (factionsToAdd) {
				while (!factionsToAdd.isEmpty()) {
					Faction f = factionsToAdd.remove(0);
					FactionCreateEvent event = new FactionCreateEvent(f, playerState);
					StarLoader.fireEvent(event, true);
					if(!event.isCanceled()) {
						f.setIdFaction(FactionManager.getNewId());
						serverState.getGameState().getFactionManager().addFaction(f);
						if(factionId != 0) {
							serverState.getGameState().getFactionManager().removeMemberOfFaction(factionId, playerState);
						}
						setFactionId(f.getIdFaction());
					}
				}
			}
			if (old != factionId) {
				FactionChange c = new FactionChange();
				c.from = old;
				c.to = factionId;
				c.previousPermission = lastPerm;

				changedFactionFrom.add(c);
			}
		}
		if (openToJoinRequests >= 0) {
			Faction faction = serverState.getGameState().getFactionManager().getFaction(factionId);

			if (faction != null) {

				FactionPermission factionPermission = faction.getMembersUID().get(playerState.getName());
				if (factionPermission != null) {
					if (factionPermission.hasInvitePermission(faction)) {
						faction.setOpenToJoin(openToJoinRequests == 1);
						faction.sendOpenToJoinMod(playerState.getName(), openToJoinRequests == 1, ((GameServerState) getState()).getGameState());
					} else {

						playerState.sendServerMessage(new ServerMessage(Lng.astr("ERROR\nYou can't change this setting!\n-> permission denied!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
						System.err.println(Lng.str("[SERVER][PlayerFaction] cannot change otj setting. no permission for %s", playerState));
					}
				} else {
					playerState.sendServerMessage(new ServerMessage(Lng.astr("You cannot change this setting\n-> not a member of this faction!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
					System.err.println("[SERVER][PlayerFaction] cannot change otj setting. not a member: " + playerState);
				}
			} else {
				playerState.sendServerMessage(new ServerMessage(Lng.astr("You cannot change this setting\n-> not in any faction!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
				System.err.println("[SERVER][PlayerFaction] cannot change otj setting. no faction for " + playerState);
			}
			openToJoinRequests = -1;
		}

		if (attackNeutralRequests >= 0) {
			Faction faction = serverState.getGameState().getFactionManager().getFaction(factionId);

			if (faction != null) {

				FactionPermission factionPermission = faction.getMembersUID().get(playerState.getName());
				if (factionPermission != null) {
					if (factionPermission.hasRelationshipPermission(faction)) {
						faction.setAttackNeutral(attackNeutralRequests == 1);
						faction.sendAttackNeutralMod(playerState.getName(), attackNeutralRequests == 1, ((GameServerState) getState()).getGameState());
					} else {

						playerState.sendServerMessage(new ServerMessage(Lng.astr("ERROR\nYou can't change this setting!\n-> permission denied!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
						System.err.println("[SERVER][PlayerFaction] cannot change otj setting. no permission for " + playerState);
					}
				} else {
					playerState.sendServerMessage(new ServerMessage(Lng.astr("You cannot change this setting\n-> not a member of this faction!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
					System.err.println("[SERVER][PlayerFaction] cannot change otj setting. not a member: " + playerState);
				}
			} else {
				playerState.sendServerMessage(new ServerMessage(Lng.astr("You cannot change this setting\n-> not in any faction!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
				System.err.println("[SERVER][PlayerFaction] cannot change otj setting. no faction for " + playerState);
			}
			attackNeutralRequests = -1;
		}
		if (autoDeclareWarRequest >= 0) {
			Faction faction = serverState.getGameState().getFactionManager().getFaction(factionId);

			if (faction != null) {

				FactionPermission factionPermission = faction.getMembersUID().get(playerState.getName());
				if (factionPermission != null) {
					if (factionPermission.hasRelationshipPermission(faction)) {
						faction.setAutoDeclareWar(autoDeclareWarRequest == 1);
						faction.sendAutoDeclareWar(playerState.getName(), autoDeclareWarRequest == 1, ((GameServerState) getState()).getGameState());
					} else {

						playerState.sendServerMessage(new ServerMessage(Lng.astr("ERROR\nYou can't change this setting!\n-> permission denied!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
						System.err.println("[SERVER][PlayerFaction] cannot change otj setting. no permission for " + playerState);
					}
				} else {
					playerState.sendServerMessage(new ServerMessage(Lng.astr("You cannot change this setting\n-> not a member of this faction!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
					System.err.println("[SERVER][PlayerFaction] cannot change otj setting. not a member: " + playerState);
				}
			} else {
				playerState.sendServerMessage(new ServerMessage(Lng.astr("You cannot change this setting\n-> not in any faction!"), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
				System.err.println("[SERVER][PlayerFaction] cannot change otj setting. no faction for " + playerState);
			}
			autoDeclareWarRequest = -1;
		}

		if (flagLeave != 0) {
			int beforeId = factionId;
			PlayerLeaveFactionEvent event = new PlayerLeaveFactionEvent(factionManager.getFaction(beforeId), playerState);
			StarLoader.fireEvent(event, true);
			if(!event.isCanceled()) {
				System.err.println("[FACTION] Player " + playerState + " is changing faction (leave) to " + flagJoin + "; current faction: " + factionId);
				Faction faction = factionManager.getFaction(flagLeave);
				String name = faction != null ? faction.getName() : "UNKNOWN(" + flagLeave + ")";
				playerState.sendServerMessage(new ServerMessage(Lng.astr("Leaving faction\n%s.", name), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
				assert (flagLeave == factionId) : flagLeave + ", should be " + factionId;
				serverState.getFactionManager().removeMemberOfFaction(factionId, this.playerState);
				setFactionId(0);
				flagLeave = 0;
				FactionChange c = new FactionChange();
				c.from = beforeId;
				c.to = factionId;
				c.previousPermission = lastPerm;
				changedFactionFrom.add(c);
			}
		}

		if (flagJoin != 0) {
			int beforeId = factionId;
			PlayerJoinFactionEvent event = new PlayerJoinFactionEvent(factionManager.getFaction(beforeId), factionManager.getFaction(flagJoin), playerState);
			StarLoader.fireEvent(event, true);
			if(!event.isCanceled()) {
				System.err.println("[FACTION] Player " + playerState + " is changing faction (join) to " + flagJoin);
				Faction factionToJoin = factionManager.getFaction(flagJoin);
				FactionInvite invitationTaken = null;
				if(factionToJoin != null) {
					boolean permitted = true;
					if(!factionToJoin.isOpenToJoin()) {
						permitted = false;
					}
					for(FactionInvite f : invitesIncoming) {
						if(f.getFactionUID() == factionToJoin.getIdFaction()) {
							permitted = true;
							invitationTaken = f;
							System.err.println("[PlayerFactionController] FOUND INVITATION TO JOIN " + f);
							break;
						}
					}
					if(permitted || forcedJoin) {
						if(flagJoin != factionId) {
							int oldFaction = factionId;
							factionToJoin.addOrModifyMember(playerState.getName(), playerState.getName(), FactionRoles.INDEX_DEFAULT_ROLE, System.currentTimeMillis(), factionManager.getGameState(), true);
							setFactionId(flagJoin);
							if(invitationTaken != null) {
								System.err.println("[SERVER][PlayerFactionManager] removing taken invitation " + invitationTaken);
								factionManager.removeFactionInvitation(invitationTaken);
							}
							if(oldFaction != 0) {
								serverState.getGameState().getFactionManager().removeMemberOfFaction(oldFaction, playerState);
							}
						} else {
							playerState.sendServerMessage(new ServerMessage(Lng.astr("You already are in that faction!"), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
							if(invitationTaken != null) {
								factionManager.removeFactionInvitation(invitationTaken);
							}
						}
						playerState.sendServerMessage(new ServerMessage(Lng.astr("Joining faction:\n", factionToJoin.getName()), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
					} else {
						playerState.sendServerMessage(new ServerMessage(Lng.astr("Cannot join faction!\nFaction is not public\nor you had no invitation!"), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
					}
				} else {
					playerState.sendServerMessage(new ServerMessage(Lng.astr("Faction to join\ndoes not exist!"), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
				}
				flagJoin = 0;
				forcedJoin = false;
				FactionChange c = new FactionChange();
				c.from = beforeId;
				c.to = factionId;
				c.previousPermission = lastPerm;
				changedFactionFrom.add(c);
			}
		}

		if(serverState.getFactionManager().existsFaction((int)playerState.offlinePermssion[0]) && ((int)playerState.offlinePermssion[0] != factionId ||
				(((playerState.offlinePermssion[1] & PermType.FOG_OF_WAR_SHARE.value) == PermType.FOG_OF_WAR_SHARE.value) &&
				!((getFactionPermission() & PermType.FOG_OF_WAR_SHARE.value) == PermType.FOG_OF_WAR_SHARE.value)
				))){
			System.err.println("[SERVER] Merged Fog of War since rank/faction changed offline");
			playerState.getFogOfWar().merge(serverState.getFactionManager().getFaction((int)playerState.offlinePermssion[0]));
		}
		playerState.offlinePermssion[0] = 0;
		if (factionId != 0) {
			
			
			Faction faction = serverState.getFactionManager().getFaction(factionId);
			if (faction != null) {
				FactionPermission factionPermission = faction.getMembersUID().get(playerState.getName());

				if (factionPermission != null) {
					if (System.currentTimeMillis() - playerState.getThisLogin() > FactionActivityConfig.SET_ACTIVE_AFTER_ONLINE_FOR_MIN * 60 * 1000) {
						if (!factionPermission.isActiveMember() || System.currentTimeMillis() - lastFacActiveUpdate > 60 * 1000 * 5) {
							if (!factionPermission.isActiveMember()) {
								playerState.sendServerMessage(new ServerMessage(Lng.astr("You became active in your faction.\nYour faction will earn faction points\nfor %s hours after you logout\neven when you are offline",  FactionActivityConfig.SET_INACTIVE_AFTER_HOURS), ServerMessage.MESSAGE_TYPE_INFO, playerState.getId()));
							}
							//needs to be explicitely set to be sent
							factionPermission.activeMemberTime = System.currentTimeMillis();
							faction.addOrModifyMember("ADMIN", playerState.getName(), factionPermission.role, factionPermission.activeMemberTime, serverState.getGameState(), true);
							lastFacActiveUpdate = System.currentTimeMillis();

							GameServerState state = ((GameServerState) playerState.getState());
							if (playerState.getStarmadeName() != null && playerState.getStarmadeName().length() > 0) {
								for (Entry<String, ProtectedUplinkName> e : state.getProtectedUsers().entrySet()) {
									if (!e.getKey().toLowerCase(Locale.ENGLISH).equals(playerState.getName().toLowerCase(Locale.ENGLISH))) {
										if (e.getValue().uplinkname.toLowerCase(Locale.ENGLISH).equals(playerState.getName().toLowerCase(Locale.ENGLISH))) {
											//duplictae sm login detected. set that player inactive to deny point farming

											for (Faction fac : serverState.getFactionManager().getFactionCollection()) {
												FactionPermission facPerm = faction.getMembersUID().get(e.getKey());
												if (facPerm != null && facPerm.isActiveMember()) {
													facPerm.activeMemberTime = 0;
													faction.addOrModifyMember("ADMIN", facPerm.playerUID, facPerm.role, facPerm.activeMemberTime, serverState.getGameState(), true);
													playerState.sendServerMessage(new ServerMessage(Lng.astr("You own other accounts in the same\nfaction! Member %s has\nbeen set to inactive.",  e.getKey()), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
												}
											}

										}
									}

								}
							}
						}

					}
				}
			}
		}
		
		if(!checkedFleets){
			((GameServerState)playerState.getState()).getFleetManager().checkMemberFaction(playerState);
			checkedFleets = true;
		}
		
	}

	private void onChangedFactionServer(FactionChange factionChange) {
		((GameServerState) playerState.getState()).getGameState().onFactionChangedServer(playerState, factionChange);

		//if channel doesnt exist, any changes will be covered by the method called when it's created
		((GameServerState) playerState.getState()).getChannelRouter().onFactionChangedServer(playerState.getClientChannel());
		
		playerState.onChangedFactionServer(factionChange);
	}


	public void updateFromNetworkObject(NetworkPlayer p) {
		if (isOnServer()) {
			if (factionId != 0) {
				for (int i = 0; i < playerState.getNetworkObject().factionLeaveBuffer.getReceiveBuffer().size(); i++) {
					int leaveId = playerState.getNetworkObject().factionLeaveBuffer.getReceiveBuffer().get(i);
					flagLeave = leaveId;
				}
			}
			for (int i = 0; i < playerState.getNetworkObject().requestFactionOpenToJoin.getReceiveBuffer().size(); i++) {
				boolean openToJoinRequest = playerState.getNetworkObject().requestFactionOpenToJoin.getReceiveBuffer().get(i).get();
				openToJoinRequests = openToJoinRequest ? 1 : 0;
			}
			for (int i = 0; i < playerState.getNetworkObject().requestAttackNeutral.getReceiveBuffer().size(); i++) {
				boolean openToJoinRequest = playerState.getNetworkObject().requestAttackNeutral.getReceiveBuffer().get(i).get();
				attackNeutralRequests = openToJoinRequest ? 1 : 0;
			}
			for (int i = 0; i < playerState.getNetworkObject().requestAutoDeclareWar.getReceiveBuffer().size(); i++) {
				boolean openToJoinRequest = playerState.getNetworkObject().requestAutoDeclareWar.getReceiveBuffer().get(i).get();
				autoDeclareWarRequest = openToJoinRequest ? 1 : 0;
			}
			for (int i = 0; i < playerState.getNetworkObject().factionJoinBuffer.getReceiveBuffer().size(); i++) {
				int joinId = playerState.getNetworkObject().factionJoinBuffer.getReceiveBuffer().get(i);
				flagJoin = joinId;
			}
			for (int i = 0; i < playerState.getNetworkObject().factionChatRequests.getReceiveBuffer().size(); i++) {
				String entry = playerState.getNetworkObject().factionChatRequests.getReceiveBuffer().get(i).get();
				synchronized (chatMsgsRequests) {
					chatMsgsRequests.add(entry);
				}
			}
			for (int i = 0; i < playerState.getNetworkObject().factionDescriptionEditRequest.getReceiveBuffer().size(); i++) {
				String entry = playerState.getNetworkObject().factionDescriptionEditRequest.getReceiveBuffer().get(i).get();
				synchronized (descriptionEdits) {
					descriptionEdits.add(entry);
				}
			}
			for (int i = 0; i < playerState.getNetworkObject().factionShareFowBuffer.getReceiveBuffer().size(); i++) {
				int entry = playerState.getNetworkObject().factionShareFowBuffer.getReceiveBuffer().getInt(i);
				factionShareFowBuffer = entry;
			}
			for (int i = 0; i < playerState.getNetworkObject().factionCreateBuffer.getReceiveBuffer().size(); i++) {
				RemoteFaction remoteFaction = playerState.getNetworkObject().factionCreateBuffer.getReceiveBuffer().get(i);
				Faction faction = remoteFaction.get();

				faction.getMembersUID().put(playerState.getName(), new FactionPermission(playerState, FactionRoles.INDEX_ADMIN_ROLE, System.currentTimeMillis()));
				synchronized (factionsToAdd) {
					factionsToAdd.add(faction);
				}

			}
			for (int i = 0; i < playerState.getNetworkObject().factionEntityIdChangeBuffer.getReceiveBuffer().size(); i++) {
				RemoteIntegerArray request = playerState.getNetworkObject().factionEntityIdChangeBuffer.getReceiveBuffer().get(i);
				synchronized (factionChanges) {
					factionChanges.add(new int[]{request.get(0).get(), request.get(1).get()});
				}
			}

		} else {
			//CLIENT
			int id = p.factionId.get();
			if (factionId != id) {
				setFactionId(id);
			}
		}
	}

	public void updateToFullNetworkObject() {
		playerState.getNetworkObject().factionId.set(factionId);
	}

	public void updateToNetworkObject() {
		if (isOnServer() /*|| playerState.isClientOwnPlayer()*/) {
			//			if(playerState.isClientOwnPlayer()){
			//				playerState.getNetworkObject().factionCode.forceClientUpdates();
			//			}
			playerState.getNetworkObject().factionId.set(factionId);
		}
	}

	public byte getFactionRank() {
		return rank;
	}

	public String getFactionRankName(byte rank) {
		Faction f;
		if ((f = ((FactionState) getState()).getFactionManager().getFaction(factionId)) != null) {
			if(rank >= 0 && factionId > 0){
				return f.getRoles().getRoles()[rank].name;
			}else if(rank == -1){
				return Lng.str("<Owner>");
			}
		}
		return Lng.str("(invalid rank)");
	}
	public long getFactionPermission() {
		Faction f;
		if ((f = ((FactionState) getState()).getFactionManager().getFaction(factionId)) != null) {
			if(rank >= 0 && factionId > 0){
				return f.getRoles().getRoles()[rank].role;
			}else if(rank == -1){
				return FactionPermission.ADMIN_PERMISSIONS;
			}
		}
		return 0;
	}

	public String getFactionOwnRankName() {
		return getFactionRankName(rank);
	}

	public float getFactionPoints() {
		return factionManager != null && factionManager.existsFaction(factionId) ? factionManager.getFaction(factionId).factionPoints : 0;
	}

	public boolean hasRelationshipPermission() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			FactionPermission factionPermission = factionManager.getFaction(factionId).getMembersUID().get(playerState.getName());
			if (factionPermission != null) {
				return factionPermission.hasRelationshipPermission(factionManager.getFaction(factionId));
			}
		} else {

		}
		return false;
	}

	public boolean hasDescriptionAndNewsPostPermission() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			FactionPermission factionPermission = factionManager.getFaction(factionId).getMembersUID().get(playerState.getName());
			if (factionPermission != null) {
				return factionPermission.hasRelationshipPermission(factionManager.getFaction(factionId));
			}
		} else {

		}
		return false;
	}

	public boolean hasInvitePermission() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			FactionPermission factionPermission = factionManager.getFaction(factionId).getMembersUID().get(playerState.getName());
			if (factionPermission != null) {
				return factionPermission.hasInvitePermission(factionManager.getFaction(factionId));
			}
		}
		return false;
	}

	public boolean hasKickPermission() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			FactionPermission factionPermission = factionManager.getFaction(factionId).getMembersUID().get(playerState.getName());
			if (factionPermission != null) {
				return factionPermission.hasKickPermission(factionManager.getFaction(factionId));
			}
		}
		return false;
	}

	public boolean hasPermissionEditPermission() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			FactionPermission factionPermission = factionManager.getFaction(factionId).getMembersUID().get(playerState.getName());
			if (factionPermission != null) {
				return factionPermission.hasPermissionEditPermission(factionManager.getFaction(factionId));
			}
		}
		return false;
	}
	public boolean hasHomebaseEditPermission() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			FactionPermission factionPermission = factionManager.getFaction(factionId).getMembersUID().get(playerState.getName());
			if (factionPermission != null) {
				return factionPermission.hasHomebasePermission(factionManager.getFaction(factionId));
			}
		}
		return false;
	}

	private class ServerNewsRequest {
		NetworkClientChannel networkClientChannel;
		long req;

		public ServerNewsRequest(NetworkClientChannel networkClientChannel,
		                         long req) {
			super();
			this.networkClientChannel = networkClientChannel;
			this.req = req;
		}

	}

	public boolean hasHomebase() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			
			return factionManager.getFaction(factionId).getHomebaseUID().length() > 0;
		}
		return false;
	}

	public boolean isHomebase(SimpleTransformableSendableObject obj) {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			return factionManager.getFaction(factionId).getHomebaseUID().equals(obj.getUniqueIdentifier());
		}
		return false;
	}

	public FogOfWarController getFactionFow() {
		if (factionManager != null && factionManager.existsFaction(factionId)) {
			return factionManager.getFaction(factionId).getFogOfWar();
		}
		return null;
	}
	
	
	@Override
	public void onFactionChanged() {
		onRelationShipOfferChanged();
		onFactionNewsDeleted();
		onInvitationsChanged();
	}

	@Override
	public void onRelationShipOfferChanged() {
		needsRelationshipOfferReorganization = true;		
	}

	@Override
	public void onFactionNewsDeleted() {
		factionNewsReorganization = true;		
	}

	@Override
	public void onInvitationsChanged() {
		needsInviteReorganization = true;
		
	}

	

	
	

}
