package org.schema.game.common.data.world;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.SectorUpdateListener;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.rules.rules.SectorRuleEntityManager;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager.EffectEntityType;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.blockeffects.config.ConfigProviderSource;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.network.objects.NetworkSector;
import org.schema.game.network.objects.remote.RemoteItem;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class RemoteSector implements Sendable, ConfigManagerInterface, ConfigProviderSource, RuleEntityContainer{
	private static final Vector3i tmpSys = new Vector3i();
	private static final Transform tmpTrns = new Transform();
	private static Vector3f tmpA = new Vector3f();
	private static Vector3f tmpB = new Vector3f();
	private final StateInterface state;
	private final boolean onServer;
	private final List<FreeItem> itemsToAdd = new ObjectArrayList<FreeItem>();
	private final List<Integer> itemsToRemove = new ObjectArrayList<Integer>();
	private int id = -1;
	private boolean markedForDeleteVolatile;
	private boolean markedForDeleteVolatileSent;
	private NetworkSector networkGameState;
	private Int2ObjectMap<FreeItem> items = new Int2ObjectOpenHashMap<FreeItem>();
	private Iterator<FreeItem> iterator;
	private long lastFullUpdateTime;
	private Set<PlayerState> currentPlayers = new ObjectOpenHashSet<PlayerState>();
	private Sector serverSector;
	private SectorType type;
	private boolean writtenForUnload;
	private PlanetType planetType;
	private Vector3i clientPos = new Vector3i();
	private ConfigEntityManager configManager;
	private Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> changedSlots = new Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet>();
	public long lag;
	public long lagEntities;
	private long lastLagSent;
	private long currentLag;
	private long lastLagReceived;
	private final List<ConfigProviderSource> sectorProjectionSources = new ObjectArrayList<ConfigProviderSource>();
	private SectorRuleEntityManager ruleEntityManager;
	//INSERTED CODE
	public Set<PlayerState> _getCurrentPlayers() {
		return currentPlayers;
	}
	///

	public RemoteSector(StateInterface state) {
		this.state = state;
		onServer = state instanceof ServerStateInterface;
		ruleEntityManager = new SectorRuleEntityManager(this);
		if(!onServer){
			configManager = new ConfigEntityManager(0, EffectEntityType.SECTOR, ((GameClientState)this.state));
			configManager.entityName = "SECTOR";
		}

	}

	public static void getUpdateSet(GameServerState state,
	                                Collection<RemoteSector> col) {
		for (PlayerState s : state.getPlayerStatesByName().values()) {
			Sendable sendable = state.getLocalAndRemoteObjectContainer()
					.getLocalObjects().get(s.getCurrentSectorId());
			if (sendable != null) {
				assert (sendable instanceof RemoteSector) : s + " -> " + s.getCurrentSectorId();
				col.add(((RemoteSector) sendable));
			} else {
				System.err
						.println("[SERVER][REMOTESECTOR] WARNING: REMOTE SECTOR FOR "
								+ s + " NOT FOUND: " + s.getCurrentSectorId());
			}
		}
	}

	private void addItem(FreeItem freeItem) {
		synchronized (items) {
			// System.err.println("[REMOTESECTOR] "+state+": added free item: "+freeItem);
			itemsToAdd.add(freeItem);
		}

	}

	public void addItem(Vector3f pos, short type, int metaId, int count) {
		if (type != Element.TYPE_NONE) {
			//			assert(type > 0 || metaId != -1):type+"; "+metaId;
			addItem(new FreeItem(GameServerState.itemIds++, type, count, metaId, pos));
		}
	}

	@Override
	public void cleanUpOnEntityDelete() {

	}

	@Override
	public void destroyPersistent() {
		// persistency in database (Sector)
	}

	@Override
	public NetworkSector getNetworkObject() {
		return networkGameState;
	}
	@Override
	public boolean isPrivateNetworkObject(){
		return false;
	}

	@Override
	public SendableType getSendableType() {
		return SendableTypes.REMOTE_SECTOR;
	}

	@Override
	public StateInterface getState() {
		return state;
	}

	@Override
	public void initFromNetworkObject(NetworkObject o) {
		id = o.id.get();
		byte tByte = ((NetworkSector) o).type.get();

		this.type = SectorType.values()[(byte) (tByte & 0xF)];
		this.planetType = PlanetType.values()[(byte) ((tByte >> 4) & 0xF)];
		networkGameState.pos.getVector(clientPos);
		configManager.initFromNetworkObject(networkGameState);
	}

	@Override
	public void initialize() {

		if (!onServer) {
			items = new Int2ObjectOpenHashMap<FreeItem>();
		}
	}

	@Override
	public boolean isMarkedForDeleteVolatile() {
		return markedForDeleteVolatile;
	}

	@Override
	public void setMarkedForDeleteVolatile(boolean markedForDelete) {
		markedForDeleteVolatile = markedForDelete;

	}

	@Override
	public boolean isMarkedForDeleteVolatileSent() {
		return markedForDeleteVolatileSent;
	}

	@Override
	public void setMarkedForDeleteVolatileSent(boolean b) {
		markedForDeleteVolatileSent = b;

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
		return false;
	}

	@Override
	public void markForPermanentDelete(boolean mark) {

	}

	@Override
	public void newNetworkObject() {
		networkGameState = new NetworkSector(state);
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		handleReceivedNetworkItems((NetworkSector) o);
		ruleEntityManager.receive(networkGameState);
		for(long s : networkGameState.lagAnnouncement.getReceiveBuffer()){
			currentLag = s;
			lastLagReceived = System.currentTimeMillis();
		}
		configManager.updateFromNetworkObject(networkGameState);
	}

	@Override
	public void updateLocal(Timer timer) {
		//INSERTED CODE
		boolean shouldIterate = (!FastListenerCommon.sectorUpdateListeners.isEmpty());
		if(shouldIterate){
			for (SectorUpdateListener listener : FastListenerCommon.sectorUpdateListeners) {
				listener.remote_preUpdate(this, timer);
			}
		}
		///
		configManager.updateLocal(timer, this);
		
//		if(isOnServer() && getServerSector().pos.equals(16, 16, 16)){
//			for(EffectModule e : configManager.getModulesList()){
//				System.err.println("UPDATE PROJECTION "+getState()+"; "+e);
//			}
//			System.err.println("WARP INTERFICTION: "+getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_ACTIVE, false));
//			try {
//				throw new Exception("WUT "+getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_ACTIVE, false));
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
//		}
		ruleEntityManager.update(timer);
		sectorProjectionSources.clear();
		if (onServer) {

			if (!items.isEmpty() || !itemsToAdd.isEmpty() || !itemsToRemove.isEmpty()) {

				for (PlayerState s : ((GameServerState) state).getPlayerStatesByName().values()) {
					if (s instanceof PlayerState
							&& s.getCurrentSectorId() == this.id) {
						currentPlayers.add((s));
					}
				}

				try {
					updateItems(timer, true);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				currentPlayers.clear();
			}

		} else {
			// System.err.println("UPDATING REMOTE SECTOR ON CLIENT "+items);

			GameClientState state = ((GameClientState) this.state);
			Vector3i clientPos = clientPos();
			Vector3i containingSystem = VoidSystem.getContainingSystem(clientPos, tmpSys);
			Vector3i relPos = Galaxy.getLocalCoordinatesFromSystem(containingSystem, new Vector3i());
			int systemType = state.getCurrentGalaxy().getSystemType(relPos);

			if (systemType == Galaxy.TYPE_BLACK_HOLE && !state.getCurrentGalaxy().isVoid(relPos)) {

				Vector3i offset = state.getCurrentGalaxy().getSunPositionOffset(relPos, new Vector3i());
				Vector3i realPos = new Vector3i(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
				realPos.add(offset);

				if (!containingSystem.equals(0, 0, 0) && !Sector.isTutorialSector(clientPos) && !Sector.isPersonalOrTestSector(clientPos)) {
					Sector.applyBlackHoleGrav(state, containingSystem, realPos, clientPos, id, tmpTrns, tmpA, tmpB, offset, timer);
				}
			}

			try {
				updateItems(timer, false);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			if (isClientInSector()) {
				// long l = getNetworkObject().nextRoundStart.get();
				// if(l >= 0){
				// float diff = (l - System.currentTimeMillis())/1000f;
				//
				// if(diff > 0){
				// String timeString = null;
				// if(diff < 60){
				// timeString = StringTools.formatPointZero(diff)+ " secs";
				// }else{
				// int secs = (int)diff;
				// int mins = secs / 60;
				// secs -= mins * 60;
				// int hours = mins / 60;
				// mins -= hours * 60;
				// int days = hours / 24;
				// hours -= days * 24;
				//
				// String min = mins > 0 ? String.valueOf(mins)+" mins and " :
				// "";
				// String hour = hours > 0 ? String.valueOf(hours)+" hours and "
				// : "";
				// String day = days > 0 ? String.valueOf(days)+" days and " :
				// "";
				//
				// timeString = day+hour+min+String.valueOf(secs)+" secs";
				// }
				// //
				// ((GameClientState)getState()).getController().showTitleMessage("nextWave",
				// // "The next wave will arrive in "+timeString+". Get Ready!",
				// 0);
				// }
				// }
			}
		}
		//INSERTED CODE
		if(shouldIterate){
			for (SectorUpdateListener listener : FastListenerCommon.sectorUpdateListeners) {
				listener.remote_postUpdate(this, timer);
			}
		}
		///
		if(timer.currentTime - lastLagReceived > 7000){
			currentLag = 0;
		}
	}

	@Override
	public void updateToFullNetworkObject() {
		networkGameState.id.set(id);
		sendAllItems();
		networkGameState.pos.set(getServerSector().pos);
		networkGameState.mode.set(getServerSector().getProtectionMode());
		networkGameState.active.set(getServerSector().isActive());
		networkGameState.type.set((byte) (type.ordinal() | planetType.ordinal() << 4));

		StellarSystem.debug = true;

		boolean borderSystem = StellarSystem
				.isBorderSystem(getServerSector().pos);
		boolean starSystem = StellarSystem.isStarSystem(getServerSector().pos);
		StellarSystem.debug = false;
		configManager.updateToFullNetworkObject(networkGameState);
	}

	@Override
	public void updateToNetworkObject() {
		if (onServer) {
			networkGameState.id.set(id);
			networkGameState.active.set(getServerSector().isActive());
			networkGameState.mode.set(getServerSector().getProtectionMode());
		}
		configManager.updateToNetworkObject(networkGameState);
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

	public boolean clientActive() {
		return networkGameState.active.get();
	}

	public Vector3i clientPos() {
		assert(!onServer);
		return clientPos;

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
	 * @return the items
	 */
	public Int2ObjectMap<FreeItem> getItems() {
		return items;
	}

	public void setItems(Int2ObjectMap<FreeItem> items) {
		this.items = items;
		iterator = items.values().iterator();
	}

	public Sector getServerSector() {
		assert (state instanceof GameServerState);
		return serverSector;
	}

	private void handleReceivedNetworkItems(NetworkSector o) {
		for (int i = 0; i < o.itemBuffer.getReceiveBuffer().size(); i++) {
			RemoteItem remoteItem = o.itemBuffer.getReceiveBuffer().get(i);
			if (remoteItem.isAdd()) {

				addItem(remoteItem.get());
			} else {
				removeItem(remoteItem.get().getId());
			}
		}
	}

	public boolean isClientInSector() {
		assert (state instanceof GameClientState);
		return ((GameClientState) state).getCurrentSectorId() == id;
	}

	public void removeItem(int id) {
		synchronized (items) {
			FreeItem freeItem = items.get(id);
			if (freeItem != null) {
				itemsToRemove.add(id);
			} else {
				System.err
						.println((onServer ? "[SERVER]" : "[CLIENT]")
								+ "[RemoteSector] WARNING: trying to delete item id that doesn't exist: "
								+ id);
			}
		}
	}

	private void sendAllItems() {
		synchronized (items) {

			for (FreeItem item : items.values()) {
				networkGameState.itemBuffer.add(new RemoteItem(item, true, networkGameState));
			}
		}
	}

	/**
	 * @param sector the sector to set
	 */
	public void setSector(Sector sector) {
		
		this.id = sector.getId();
		this.serverSector = sector;
		try {
			this.type = sector.getSectorType();
			this.planetType = sector.getPlanetType();

		} catch (IOException e) {
			e.printStackTrace();
		}
		assert(sector.getDBId() > 0);
		configManager = new ConfigEntityManager(sector.getDBId(), EffectEntityType.SECTOR, ((GameServerState) this.state));
		configManager.entityName = "SEC("+sector.pos+")";
		configManager.loadFromDatabase((GameServerState) state);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (onServer) {
			Sector serverSector = getServerSector();
			if (serverSector != null) {
				return "[SERVER RemoteSector(" + id + ") "
						+ getServerSector().isActive() + "; "
						+ getServerSector().pos + "]";
			} else {
				return "[SERVER RemoteSector(" + id + ") sector removed]";
			}
		} else {
			return "[CLIENT ReSector(" + id + ") " + clientActive() + "]";
		}
	}

	private void updateItems(Timer timer, boolean send) throws IOException {
		if (onServer) {

			//if items are updated, there is always a change
			getServerSector().setChangedForDb(true);

			long time = System.currentTimeMillis();
			if (iterator != null && time - lastFullUpdateTime > 200) {

				if (!iterator.hasNext()) {
					iterator = items.values().iterator();
				}
				int i = 0;
				while (iterator.hasNext() && i < 100) {
					FreeItem next = iterator.next();
					if (!next.isAlive(time)) {
						removeItem(next.getId());
					} else {

						if (next.checkFlagPhysics()) {
							boolean changed = next
									.doPhysicsTest(getServerSector());
							if (changed && send) {
								// submit changes to clients
								networkGameState.itemBuffer
										.add(new RemoteItem(next, true, networkGameState));
							}
						}

						for (PlayerState s : currentPlayers) {

							boolean removedItem = s.checkItemInReach(next, changedSlots);
							if (removedItem) {
								if (onServer) {
									getServerSector().setChangedForDb(true);
								}
								//enshure that only one picks up item
								break;
							}
						}

					}

					i++;

				}
				if (!iterator.hasNext()) {
					lastFullUpdateTime = time;
				}

			}
			for (Entry<PlayerState, IntOpenHashSet> a : changedSlots.entrySet()) {
				a.getKey().sendInventoryModification(a.getValue(), Long.MIN_VALUE);
			}
			changedSlots.clear();
		}

		if (!itemsToAdd.isEmpty()) {
			synchronized (items) {
				while (!itemsToAdd.isEmpty()) {
					FreeItem item = itemsToAdd.remove(0);

					/*
					 * not necessary: will be requested when
					 * picked up into inventory
					 *
					 */

					FreeItem old = items.put(item.getId(), item);
					if (old != null) {
						item.setTimeSpawned(old.getTimeSpawned());
						System.err.println("[REMOTESECTOR] " + state
								+ " ITEM change: " + old + " -> " + item);
					}
					//					System.err.println("[REMOTESECTOR] ITEM ADDED: "+item+": Total: "+items.size());
					if (send) {

						networkGameState.itemBuffer.add(new RemoteItem(item,
								true, networkGameState));
					}
					iterator = items.values().iterator();
				}
			}
		}
		if (!itemsToRemove.isEmpty()) {
			synchronized (items) {
				while (!itemsToRemove.isEmpty()) {
					Integer itemId = itemsToRemove.remove(0);
					FreeItem removedItem = items.remove(itemId);
					// System.err.println("[REMOTESECTOR] ITEM REMOVED: "+removedItem+": "+items);
					if (send && removedItem != null) {
						RemoteItem rItem = new RemoteItem(removedItem, false, networkGameState);
						networkGameState.itemBuffer.add(rItem);
					} else if (removedItem == null) {
						System.err
								.println("[SERVER][REMOTESECTOR] deleted invalid id: "
										+ itemId);
					}
					iterator = items.values().iterator();
				}
			}
		}
		
	}

	public int getSectorModeClient() {
		return networkGameState.mode.getInt();
	}

	public boolean isNoIndicationsClient() {
		return Sector.isMode(getSectorModeClient() ,SectorMode.NO_INDICATIONS);
	}

	public boolean isPeaceClient() {
		return Sector.isMode(getSectorModeClient() ,SectorMode.PROT_NO_SPAWN);
	}

	public boolean isProtectedClient() {
		return Sector.isMode(getSectorModeClient() ,SectorMode.PROT_NO_ATTACK);
	}

	public boolean isNoEntryClient() {
		return Sector.isMode(getSectorModeClient() ,SectorMode.LOCK_NO_ENTER);
	}

	public boolean isNoExitClient() {
		return Sector.isMode(getSectorModeClient() ,SectorMode.LOCK_NO_EXIT);
	}

	/**
	 * @return the type
	 */
	public SectorType getType() {
		return type;
	}

	@Override
	public void announceLag(long timeTaken) {
		if(System.currentTimeMillis() - lastLagSent > 1000){
			assert(state.isSynched());
			networkGameState.getLagAnnouncement().add(timeTaken);
			assert(networkGameState.isChanged());
			assert(networkGameState.getLagAnnouncement().isChanged());
			lastLagSent = System.currentTimeMillis();
		}
	}

	@Override
	public long getCurrentLag() {
		return currentLag;
	}
	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.SECTOR;
	}

	@Override
	public ConfigEntityManager getConfigManager() {
		return configManager;
	}

	@Override
	public ShortList getAppliedConfigGroups(ShortList out) {
		boolean permanentEffects = true;
		boolean transientEffects = true;
		return configManager.applyMergeTo(permanentEffects, transientEffects, out);
	}

	@Override
	public void registerTransientEffects(List<ConfigProviderSource> transientEffectSources) {
		//apply possibly region effects here
		
		//add AoE effects from ships/stations to sector effects 
		transientEffectSources.addAll(sectorProjectionSources);
	}

	public void entityUpdateInSector(SendableSegmentController sendableSegmentController) {
		
		sendableSegmentController.addSectorConfigProjection(sectorProjectionSources);
		
	}

	public void onAddedEntityFromSector(SendableSegmentController sendableSegmentController) {
	}

	public void onRemovedEntityFromSector(SendableSegmentController sendableSegmentController) {
	}

	@Override
	public long getSourceId() {
		return -2;
	}

	@Override
	public SectorRuleEntityManager getRuleEntityManager() {
		return ruleEntityManager;
	}



}
