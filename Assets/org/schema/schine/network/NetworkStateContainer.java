package org.schema.schine.network;

import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.synchronization.GhostSendable;
import org.schema.schine.resource.UniqueInterface;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class NetworkStateContainer {

	public final ObjectArrayList<Sendable> updateSet = new ObjectArrayList<Sendable>();
	public final Int2BooleanOpenHashMap newStatesBeforeForce = new Int2BooleanOpenHashMap();
	private final Int2ObjectOpenHashMap<Sendable> localObjects;
	private final Int2ObjectOpenHashMap<Sendable> localUpdatableObjects;
	private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Sendable>> topLevelObjects;
	private final Object2ObjectOpenHashMap<String, Sendable> uidObjects;
	private final Long2ObjectOpenHashMap<Sendable> dbObjects;
	private final Int2ObjectOpenHashMap<NetworkObject> remoteObjects;
	private final Object2ObjectOpenHashMap<String, Sendable> persistentObjects;
	private final Int2ObjectOpenHashMap<GhostSendable> ghostObjects = new Int2ObjectOpenHashMap<GhostSendable>();
	private final boolean privateChannel;
	private final StateInterface state;
	public ObjectArrayList<NetworkObject> debugReceivedClasses = new ObjectArrayList<NetworkObject>();

	public NetworkStateContainer(boolean privateChannel, StateInterface state) {
		localObjects = (new Int2ObjectOpenHashMap<Sendable>());
		remoteObjects = (new Int2ObjectOpenHashMap<NetworkObject>());
		uidObjects = new Object2ObjectOpenHashMap<String, Sendable>();
		persistentObjects = new Object2ObjectOpenHashMap<String, Sendable>();
		localUpdatableObjects = new Int2ObjectOpenHashMap<Sendable>();
		topLevelObjects = new Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Sendable>>();
		dbObjects = new Long2ObjectOpenHashMap<Sendable>();
		this.state = state;
		this.privateChannel = privateChannel;
	}

	public void checkGhostObjects() {
		if (!ghostObjects.isEmpty()) {
			long time = System.currentTimeMillis();

			synchronized (ghostObjects) {
				ObjectIterator<GhostSendable> iterator = ghostObjects.values().iterator();
				while (iterator.hasNext()) {
					GhostSendable gs = iterator.next();
					if (time - gs.timeDeleted > 20000) {
						iterator.remove();
					}
				}

			}
		}
	}

	public Int2ObjectOpenHashMap<GhostSendable> getGhostObjects() {
		return ghostObjects;
	}

	/**
	 * @return the localObjects
	 */
	public Int2ObjectOpenHashMap<Sendable> getLocalObjects() {
		return localObjects;
	}

	/**
	 * @return the localUpdatableObjects
	 */
	public Int2ObjectOpenHashMap<Sendable> getLocalUpdatableObjects() {
		return localUpdatableObjects;
	}

	/**
	 * @return the remoteObjects
	 */
	public Int2ObjectOpenHashMap<NetworkObject> getRemoteObjects() {
		return remoteObjects;
	}

	/**
	 * @return the privateChannel
	 */
	public boolean isPrivateChannel() {
		return privateChannel;
	}

	public void putLocal(int id, Sendable sendable) {
		assert (sendable != null);
		assert (state.isSynched());
		if(sendable == null){
			throw new NullPointerException();
		}
		localObjects.put(id, sendable);
		if (sendable.isUpdatable()) {
			localUpdatableObjects.put(id, sendable);
		}
		if (sendable instanceof TagSerializable && ((UniqueInterface) sendable).getUniqueIdentifier() != null) {
			uidObjects.put(((UniqueInterface) sendable).getUniqueIdentifier(), sendable);
		}
		if (sendable instanceof UniqueLongIDInterface) {
			
			if(((UniqueLongIDInterface) sendable).getDbId() > 0){
				dbObjects.put(((UniqueLongIDInterface) sendable).getDbId(), sendable);
			}
		}
		Int2ObjectOpenHashMap<Sendable> tmap = topLevelObjects.get(sendable.getTopLevelType().ordinal());
		if(tmap == null){
			tmap = new Int2ObjectOpenHashMap();
			topLevelObjects.put(sendable.getTopLevelType().ordinal(), tmap);
		}
		tmap.put(id, sendable);
	}

	public Sendable removeLocal(int id) {
		
		Sendable sendable = localObjects.remove(id);
		if(sendable != null){
			Int2ObjectOpenHashMap<Sendable> tmap = topLevelObjects.get(sendable.getTopLevelType().ordinal());
			if(tmap != null){
				tmap.remove(id);
			}
		}
		System.err.println("[ENTITIES] removed object from loaded state "+sendable+"; "+id);
		assert (state.isSynched());
		if (sendable.isUpdatable()) {
			Sendable remove = localUpdatableObjects.remove(id);
			
		}
		if (sendable instanceof UniqueInterface && ((UniqueInterface) sendable).getUniqueIdentifier() != null) {
			uidObjects.remove(((UniqueInterface) sendable).getUniqueIdentifier());
		}
		if (sendable instanceof UniqueLongIDInterface) {
			dbObjects.remove(((UniqueLongIDInterface) sendable).getDbId());
		}
		return sendable;
	}

	@Override
	public String toString() {
		return "(Local/Remote: " + localObjects + "/" + remoteObjects + ")";
	}

	/**
	 * @return the uidObjects
	 */
	public Object2ObjectOpenHashMap<String, Sendable> getUidObjectMap() {
		return uidObjects;
	}

	public int getLocalObjectsSize() {
		return localObjects.size();
	}

	public Long2ObjectOpenHashMap<Sendable> getDbObjects() {
		return dbObjects;
	}
	private final Int2ObjectOpenHashMap<Sendable> empty = new Int2ObjectOpenHashMap<Sendable>();
	
	public Int2ObjectOpenHashMap<Sendable> getLocalObjectsByTopLvlType(TopLevelType t) {
		Int2ObjectOpenHashMap<Sendable> tmap = topLevelObjects.get(t.ordinal());
		if(tmap != null){
			return tmap;
		}
		return empty;
	}

	public Object2ObjectOpenHashMap<String, Sendable> getPersistentObjects() {
		return persistentObjects;
	}

}
