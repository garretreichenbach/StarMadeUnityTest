package org.schema.game.client.controller;


import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.gamemap.requests.GameMapRequestManager;
import org.schema.game.common.controller.ParticleEntry;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.controller.trade.TradeOrder;
import org.schema.game.common.controller.trade.TradeTypeRequestAwnser;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.ScanData;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.mines.updates.MineUpdate;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.faction.FactionNewsPost;
import org.schema.game.common.data.player.playermessage.PlayerMessageController;
import org.schema.game.common.data.world.GalaxyManager;
import org.schema.game.network.objects.*;
import org.schema.game.network.objects.remote.RemoteFactionNewsPost;
import org.schema.game.network.objects.remote.RemoteSavedCoordinate;
import org.schema.game.network.objects.remote.RemoteStringLongPair;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.FileExt;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ClientChannel implements Sendable {

	private final boolean onServer;
	private final TreeSet<FactionNewsPost> factionNews = new TreeSet<FactionNewsPost>();
	private final ArrayList<FactionNewsPost> toAddFactionNewsPosts = new ArrayList<FactionNewsPost>();
	private final ClientFileDownloadController clientFileDownloadController;
	private final GameMapRequestManager clientMapRequestManager;
	private final ObjectArrayFIFOQueue<ScanData> toAddScanData = new ObjectArrayFIFOQueue<ScanData>();
	int id = -12312323;
	private StateInterface state;
	private int playerId = -121212;
	private NetworkClientChannel networkClientChannel;
	private String clientBlockBehaviorToUpload = null;
	private PlayerMessageController playerMessageController;
	private boolean markedForDeleteVolatile;
	private boolean markedForDeleteSent;
	private int factionId;
	private boolean writtenForUnload;
	private PlayerState player;
	private GalaxyManager galaxyManagerClient;
	private final ObjectArrayFIFOQueue<TradePrices> receivedTradePrices = new ObjectArrayFIFOQueue<TradePrices>();
	private ReceivedTradeSearchResult receivedTradeSearchResults = new ReceivedTradeSearchResult();
	private ObjectArrayFIFOQueue<TradeTypeRequestAwnser> tradeSearchAwnseresToAdd = new ObjectArrayFIFOQueue<TradeTypeRequestAwnser>();
	private ObjectArrayList<NPCDiplomacyEntity> diplomacyEntitiesToAddClient = new ObjectArrayList<NPCDiplomacyEntity>();
	
	public interface TradeResultListener{
		public void onTradeResultsReceived();
	}
	public interface FactionNewsPostListener{
		public void onNewsReceived();
	}
	
	 
	public final List<FactionNewsPostListener> factionNewsListeners = new ObjectArrayList<>(); 
	@Override
	public SendableType getSendableType() {
		return SendableTypes.CLIENT_CHANNEL;
	}
	
	public ClientChannel(StateInterface state) {
		this.state = state;
		onServer = state instanceof ServerStateInterface;
		if (!onServer) {
			galaxyManagerClient = new GalaxyManager(state);
		}
		clientMapRequestManager = new GameMapRequestManager(this);
		clientFileDownloadController = new ClientFileDownloadController(this);
		
		
		
		playerMessageController = new PlayerMessageController(this);

		/*
		 * ClientChannel is initialized as soon as
		 * the client received the player object
		 * this means, the player now exists on
		 * both client and server
		 */
		if (!onServer) {
			this.player = ((GameClientState) state).getPlayer();
			player.setClientChannel(this);
		}

	}

	@Override
	public void cleanUpOnEntityDelete() {

	}
	public class ReceivedTradeSearchResult extends GUIObservable{
		public final List<TradeResultListener> tradeListeners = new ObjectArrayList<>();
		public ObjectArrayList<TradeTypeRequestAwnser> o = new ObjectArrayList<TradeTypeRequestAwnser>();
		public void changed(){
			for(TradeResultListener c : tradeListeners) {
				c.onTradeResultsReceived();
			}
			notifyObservers();
		}
		
	}
	public ReceivedTradeSearchResult getReceivedTradeSearchResults() {
		return receivedTradeSearchResults;
	}
	
	public void requestTradeNodesFor(short type){
		receivedTradeSearchResults.o.clear();
		networkClientChannel.tradeTypeRequestBuffer.add(type);
		receivedTradeSearchResults.changed();
	}
	@Override
	public void destroyPersistent() {
		//not saved persistent
	}

	@Override
	public NetworkClientChannel getNetworkObject() {
		return networkClientChannel;
	}

	@Override
	public StateInterface getState() {
		return state;
	}

	@Override
	public void initFromNetworkObject(NetworkObject o) {
		id = networkClientChannel.id.get();
		playerId = networkClientChannel.playerId.get();

		/*
		 * ClientChannel is initialized as soon as
		 * the client received the player object
		 * this means, the player now exists on
		 * both client and server
		 */
		if (onServer) {
			this.player = (PlayerState) ((GameServerState) state).getLocalAndRemoteObjectContainer().getLocalObjects().get(playerId);
			player.setClientChannel(this);
		}
	}

	@Override
	public void initialize() {

	}

	@Override
	public boolean isMarkedForDeleteVolatile() {
		return markedForDeleteVolatile;
	}

	@Override
	public void setMarkedForDeleteVolatile(boolean markedForDelete) {
		this.markedForDeleteVolatile = markedForDelete;

	}

	@Override
	public boolean isMarkedForDeleteVolatileSent() {
		return markedForDeleteSent;
	}

	@Override
	public void setMarkedForDeleteVolatileSent(boolean b) {
		markedForDeleteSent = b;

	}

	@Override
	public boolean isMarkedForPermanentDelete() {
		return false;
	}

	@Override
	public boolean isOkToAdd() {
		return true;
	}

	@Override
	public boolean isOnServer() {
		return onServer;
	}

	@Override
	public boolean isUpdatable() {
		return true;
	}

	@Override
	public void markForPermanentDelete(boolean mark) {
	}

	@Override
	public void newNetworkObject() {
		networkClientChannel = new NetworkClientChannel(state, ((MetaObjectState) state).getMetaObjectManager());
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		id = networkClientChannel.id.get();

		clientFileDownloadController.handleUploadNT(senderId);

		playerMessageController.handleReceived(networkClientChannel.playerMessageBuffer, networkClientChannel.playerMessageRequests);

		for (int i = 0; i < networkClientChannel.savedCoordinates.getReceiveBuffer().size(); i++) {
			RemoteSavedCoordinate e = networkClientChannel.savedCoordinates.getReceiveBuffer().get(i);
			player.getSavedCoordinatesToAdd().enqueue(e.get());
		}

		((GameStateInterface) state).getGameState().getManualTradeManager().updateFromNetworkObject(networkClientChannel);
		for (int i = 0; i < networkClientChannel.mineUpdateBuffer.getReceiveBuffer().size(); i++) {
			MineUpdate m = networkClientChannel.mineUpdateBuffer.getReceiveBuffer().get(i).get();
			((MineInterface) state.getController()).getMineController().receivedUpdate(this, m);
		}
		if (!onServer) {
			((GameStateInterface) state).getGameState().getConfigPool().clientReceive(this);
			for (int i = 0; i < networkClientChannel.scanDataUpdates.getReceiveBuffer().size(); i++) {
				ScanData scanData = networkClientChannel.scanDataUpdates.getReceiveBuffer().get(i).get();
				synchronized (toAddScanData) {
					toAddScanData.enqueue(scanData);
				}

			}
			for (int i = 0; i < networkClientChannel.npcDiplomacyBuffer.getReceiveBuffer().size(); i++) {
				diplomacyEntitiesToAddClient.add(networkClientChannel.npcDiplomacyBuffer.getReceiveBuffer().get(i).get());
			}
			for (int i = 0; i < networkClientChannel.tradeTypeBuffer.getReceiveBuffer().size(); i++) {
				
				TradeTypeRequestAwnser r = networkClientChannel.tradeTypeBuffer.getReceiveBuffer().get(i).get();
				tradeSearchAwnseresToAdd.enqueue(r);
				
			}
			for (int i = 0; i < networkClientChannel.factionNewsPosts.getReceiveBuffer().size(); i++) {
				RemoteFactionNewsPost news = networkClientChannel.factionNewsPosts.getReceiveBuffer().get(i);
				synchronized (toAddFactionNewsPosts) {
					System.err.println("[FACTIONMANAGER] received news on vlient channel " + state + ": " + news.get());
					toAddFactionNewsPosts.add(news.get());
				}
			}
			clientMapRequestManager.updateFromNetworkObject(networkClientChannel);
//			System.err.println("[CLIENT] REC: "+getNetworkObject().galaxyRequests.getReceiveBuffer().size());
			galaxyManagerClient.updateFromNetwork(this);

			for (int i = 0; i < networkClientChannel.particles.getReceiveBuffer().size(); i++) {
				ParticleEntry entry = networkClientChannel.particles.getReceiveBuffer().get(i).get();
				entry.handleClient((GameClientState) state);
			}
			for (int i = 0; i < networkClientChannel.pricesOfShopAwnser.getReceiveBuffer().size(); i++) {
				TradePrices entry = networkClientChannel.pricesOfShopAwnser.getReceiveBuffer().get(i).get();
				synchronized(receivedTradePrices){
					receivedTradePrices.enqueue(entry);
				}
			}
		} else {
			((GameStateInterface) state).getGameState().getConfigPool().checkRequestReceived(this);
			
			((GameServerState) state).getUniverse().getGalaxyManager().updateFromNetwork(this);

			for (int i = 0; i < networkClientChannel.tradeTypeRequestBuffer.getReceiveBuffer().size(); i++) {
				short r = networkClientChannel.tradeTypeRequestBuffer.getReceiveBuffer().getShort(i);
				((GameServerState) state).getGameState().getTradeManager().requestTradeTypePrices(networkClientChannel, r);
			}
			
			for (int i = 0; i < networkClientChannel.tradeOrderRequests.getReceiveBuffer().size(); i++) {
				TradeOrder r = networkClientChannel.tradeOrderRequests.getReceiveBuffer().get(i).get();
				r.clientChannelReceivedOn = this;
				((GameServerState) state).getGameState().getTradeManager().receivedOrderOnServer(r);
			}
			for (int i = 0; i < networkClientChannel.requestPricesOfShop.getReceiveBuffer().size(); i++) {
				long r = networkClientChannel.requestPricesOfShop.getReceiveBuffer().get(i).longValue();
				((GameServerState) state).getUniverse().getGalaxyManager().enqueuePricesRequest(r, this);
			}
			
			for (int i = 0; i < networkClientChannel.createDockBuffer.getReceiveBuffer().size(); i++) {
				CreateDockRequest r = networkClientChannel.createDockBuffer.getReceiveBuffer().get(i).get();
				if(player != null){
					synchronized(player.getCreateDockRequests()){
						player.getCreateDockRequests().enqueue(r);
					}
				}
			}
			
			for (int i = 0; i < networkClientChannel.blockBehaviorUploads.getReceiveBuffer().size(); i++) {

				PlayerState p = this.player;
				if (p.getNetworkObject().isAdminClient.get()) {
					System.err.println("[SERVER] received block behavior");
					String bb = networkClientChannel.blockBehaviorUploads.getReceiveBuffer().get(i).get();
					try {
						((GameServerState) state).getController().setBlockBehaviorChanged(bb.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						((GameServerState) state).getController().broadcastMessage(Lng.astr("Block behavior config upload failed!\nUnsupported Encoding"), ServerMessage.MESSAGE_TYPE_ERROR);
					}
				} else {
					System.err.println("[SERVER] " + p + " tried to upload block behavior but is not admin (hacked client)");
				}
			}
			for (int i = 0; i < networkClientChannel.metaObjectRequests.getReceiveBuffer().size(); i++) {
				int reqId = networkClientChannel.metaObjectRequests.getReceiveBuffer().get(i);
				((MetaObjectState) state).getMetaObjectManager().awnserRequestTo(reqId, networkClientChannel);
			}
		}
		for (int i = 0; i < networkClientChannel.chatBuffer.getReceiveBuffer().size(); i++) {
			ChatMessage chatMessage = networkClientChannel.chatBuffer.getReceiveBuffer().get(i).get();
			if (onServer) {
//				System.err.println("SERVER RECEIVED CHAT; ENQUEUE: "+chatMessage.toDetailString());
				((GameServerState) state).getChannelRouter().receivedChat.enqueue(chatMessage);
			} else {
//				System.err.println("CLIENT RECEIVED CHAT; ENQUEUE: "+chatMessage.toDetailString());
				((GameClientState) state).getChannelRouter().receivedChat.enqueue(chatMessage);
			}
		}
		for (int i = 0; i < networkClientChannel.chatChannelBuffer.getReceiveBuffer().size(); i++) {
			ChatChannelModification chatMessage = networkClientChannel.chatChannelBuffer.getReceiveBuffer().get(i).get();
			player.getPlayerChannelManager().receivedChannelMods.enqueue(chatMessage);
		}
		
		/*
		 * TIMESTAMP REQUEST/RESPONSE
		 */
		if (onServer) {

			((GameServerState) state).getGameMapProvider().updateFromNetworkObject(this);

			for (int i = 0; i < networkClientChannel.factionNewsRequests.getReceiveBuffer().size(); i++) {
				long req = networkClientChannel.factionNewsRequests.getReceiveBuffer().get(i);

				player.getFactionController().queueNewsRequestOnServer(networkClientChannel, req);

			}
			for (int i = 0; i < networkClientChannel.timeStampRequests.getReceiveBuffer().size(); i++) {
				String req = networkClientChannel.timeStampRequests.getReceiveBuffer().get(i).get();

				File f = new FileExt(SkinManager.serverDB + req + PlayerSkin.EXTENSION);
				long val = 0;
				long size = 0;
				if (f.exists()) {
					val = f.lastModified();
					size = f.length();
				}
				networkClientChannel.timeStampResponses.add(new RemoteStringLongPair(new StringLongLongPair(req, val, size), onServer));

			}

			for (int i = 0; i < networkClientChannel.fileRequests.getReceiveBuffer().size(); i++) {
				String req = networkClientChannel.fileRequests.getReceiveBuffer().get(i).get();
				System.err.println("[SERVER] received File request for " + req);
				((GameServerState) state).addServerFileRequest(this, req);

			}

			((GameServerController) state.getController()).getMissileController().fromNetwork(networkClientChannel);
		} else {
			for (int i = 0; i < networkClientChannel.timeStampResponses.getReceiveBuffer().size(); i++) {
				StringLongLongPair rA = networkClientChannel.timeStampResponses.getReceiveBuffer().get(i).get();

				((GameClientState) state).getController().getTextureSynchronizer().handleTimeStampResponse(rA);
			}

			((GameClientController) state.getController()).getClientMissileManager().fromNetwork(networkClientChannel);
		}

	}

	@Override
	public void updateLocal(Timer timer) {

		if (!onServer) {
			GameClientState s = (GameClientState) state;
			if (s.getPlayer().getFactionId() != 0 && s.getPlayer().getFactionId() != factionId) {
				factionNews.clear();
				requestNextNews();
				factionId = s.getPlayer().getFactionId();
			}
			if (clientBlockBehaviorToUpload != null) {
				if (((GameClientState) state).getPlayer().getNetworkObject().isAdminClient.get()) {
					((GameClientState) state).getController().popupInfoTextMessage(Lng.str("Uploading block behavior config..."), 0);
					networkClientChannel.blockBehaviorUploads.add(new RemoteString(clientBlockBehaviorToUpload, onServer));
				} else {
					((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Cannot upload!\nADMIN ACCESS REQUIRED"), 0);
				}
				clientBlockBehaviorToUpload = null;
			}
			for(int i = 0; i < diplomacyEntitiesToAddClient.size(); i++){
				assert(!onServer);
				NPCDiplomacyEntity n = diplomacyEntitiesToAddClient.get(i);
				if(n.isFactionLoaded()){
					n.getDiplomacy().entities.put(n.getDbId(), n);
					diplomacyEntitiesToAddClient.remove(i);
					i--;
					n.getDiplomacy().onClientChanged();
				}else{
					
				}
			}
			((GameStateInterface) state).getGameState().getConfigPool().checkClientRequest(this);
			if (!playerMessageController.initialRequest) {
				networkClientChannel.playerMessageRequests.add(10);
				playerMessageController.initialRequest = true;

			}
			while(!tradeSearchAwnseresToAdd.isEmpty()){
				TradeTypeRequestAwnser r = tradeSearchAwnseresToAdd.dequeue();
				
				TradeNodeStub tradeNodeStub = galaxyManagerClient.getTradeNodeDataById().get(r.node);
				
				if(tradeNodeStub != null){
					r.nodeClient = (TradeNodeClient) tradeNodeStub;
					
					receivedTradeSearchResults.o.add(r);
					receivedTradeSearchResults.changed();
				}
			}
			clientMapRequestManager.update(timer);
			galaxyManagerClient.updateClient();
			if (!toAddScanData.isEmpty()) {
				synchronized (toAddScanData) {
					while (!toAddScanData.isEmpty()) {

						ScanData scanData = toAddScanData.dequeue();
						player.addScanHistory(scanData);
						if (System.currentTimeMillis() - scanData.time < 60000) {
							((GameClientState) state).getController().popupGameTextMessage(Lng.str("Scan data received,\ncheck your Navigation Panel!"), 0);
						}
					}
				}
			}
			if (!receivedTradePrices.isEmpty()) {
				synchronized (receivedTradePrices) {
					while (!receivedTradePrices.isEmpty()) {
						
						TradePrices t = receivedTradePrices.dequeue();
						galaxyManagerClient.updatedPricesOnClient(t);
					}
				}
			}
			if (!toAddFactionNewsPosts.isEmpty()) {
				synchronized (toAddFactionNewsPosts) {
					while (!toAddFactionNewsPosts.isEmpty()) {

						FactionNewsPost f = toAddFactionNewsPosts.remove(0);

						if (!factionNews.isEmpty()) {
							if (factionNews.iterator().next().getFactionId() != ((GameClientState) state).getPlayer().getFactionId()) {
								System.err.println("[CLIENT] RECEIVED NEWS OF OTHER FACTION -> cleaning news");
								factionNews.clear();
							}
						}
						factionNews.add(f);

						for(FactionNewsPostListener p : factionNewsListeners) {
							p.onNewsReceived();
						}
					}
				}
			}

		} else {

		}
		try {

			clientFileDownloadController.updateLocal();
		} catch (IOException e) {
			e.printStackTrace();
			clientFileDownloadController.setNeedsUpdate(false);
		}
		if (!onServer) {
			if (!state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(playerId)) {
				markedForDeleteVolatile = true;
				System.err.println("[SERVER][CLIENTCHANNEL] DELETING: no more player attached");
				clientFileDownloadController.setNeedsUpdate(false);
			}

		}
	}

	@Override
	public void updateToFullNetworkObject() {
		networkClientChannel.playerId.set(playerId);
		networkClientChannel.id.set(id);

		updateToNetworkObject();
	}

	@Override
	public void updateToNetworkObject() {
		if (onServer) {
			networkClientChannel.connectionReady.set(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#isWrittenForUnload()
	 */
	@Override
	public boolean isWrittenForUnload() {
		return writtenForUnload;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#setWrittenForUnload(boolean)
	 */
	@Override
	public void setWrittenForUnload(boolean b) {
		writtenForUnload = b;
	}

	/**
	 * @return the clientFileDownloadController
	 */
	public ClientFileDownloadController getClientFileDownloadController() {
		return clientFileDownloadController;
	}

	/**
	 * @return the clientMapRequestManager
	 */
	public GameMapRequestManager getClientMapRequestManager() {
		return clientMapRequestManager;
	}

	/**
	 * @return the factionNews
	 */
	public TreeSet<FactionNewsPost> getFactionNews() {
		return factionNews;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the playerId
	 */
	public int getPlayerId() {
		return playerId;
	}

	/**
	 * @param playerId the playerId to set
	 */
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public boolean isConnectionReady() {
		return networkClientChannel != null && networkClientChannel.connectionReady.get();
	}

	public void requestAllNews() {
		networkClientChannel.factionNewsRequests.add(-1);
	}

	public void requestFile(String name) {
		clientFileDownloadController.setCurrentDownloadName(name);
		networkClientChannel.fileRequests.add(new RemoteString(name, networkClientChannel));
	}

	public void requestFileTimestamp(String name) {
		networkClientChannel.timeStampRequests.add(new RemoteString(name, networkClientChannel));
	}

	public void requestMetaObject(int metaObjectId) {
		networkClientChannel.metaObjectRequests.add(metaObjectId);
	}

	public void requestNextNews() {
		if (factionNews.isEmpty()) {
			networkClientChannel.factionNewsRequests.add(System.currentTimeMillis());
		} else {
			//request next from newest date received
			networkClientChannel.factionNewsRequests.add(factionNews.iterator().next().getDate());
		}
	}

	public void setConnectionReady() {
		networkClientChannel.connectionReady.set(true);
	}

	public void uploadClientBlockBehavior(String path) throws IOException {
		assert (!onServer);
		if (!onServer) {
			File f = new FileExt(path);
			BufferedReader r = new BufferedReader(new FileReader(f));
			try {
				StringBuffer b = new StringBuffer();
				String line;
				while ((line = r.readLine()) != null) {
					b.append(line);
					b.append("\n");
				}
				clientBlockBehaviorToUpload = b.toString();
			} finally {
				r.close();
			}
		} else {
			throw new IllegalArgumentException("Client only");
		}

	}

	/**
	 * @return the player
	 */
	public PlayerState getPlayer() {
		return player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(PlayerState player) {
		this.player = player;
	}

	/**
	 * @return the playerMessageController
	 */
	public PlayerMessageController getPlayerMessageController() {
		return playerMessageController;
	}

	/**
	 * @param playerMessageController the playerMessageController to set
	 */
	public void setPlayerMessageController(PlayerMessageController playerMessageController) {
		this.playerMessageController = playerMessageController;
	}

	/**
	 * @return the galaxyManagerClient
	 */
	public GalaxyManager getGalaxyManagerClient() {
		return galaxyManagerClient;
	}

	public void sendSavedCoordinateToServer(String name, Vector3i currentSystem) {
		networkClientChannel.savedCoordinates.add(new RemoteSavedCoordinate(new SavedCoordinate(new Vector3i(currentSystem), name, false), false));
	}

	public void sendSavedCoordinateToServer(SavedCoordinate f) {
		networkClientChannel.savedCoordinates.add(new RemoteSavedCoordinate(f, false));
	}

	public void removeSavedCoordinateToServer(SavedCoordinate f) {
		SavedCoordinate copy = f.copy();
		copy.setRemoveFlag(true);
		networkClientChannel.savedCoordinates.add(new RemoteSavedCoordinate(copy, false));
	}

	@Override
	public void announceLag(long timeTaken) {
				
	}

	@Override
	public long getCurrentLag() {
				return 0;
	}
	public void onStopClient(){
		if(galaxyManagerClient != null){
			galaxyManagerClient.shutdown();
		}
	}

	public void requestTradePrices(long entityDBId) {
		networkClientChannel.requestPricesOfShop.add(entityDBId);
	}
	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.GENERAL;
	}
	@Override
	public boolean isPrivateNetworkObject(){
		return true;
	}
}
