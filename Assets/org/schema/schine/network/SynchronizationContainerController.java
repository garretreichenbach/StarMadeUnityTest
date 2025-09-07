package org.schema.schine.network;

import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.UnsaveNetworkOperationException;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.physics.Physical;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SynchronizationContainerController {

	private final ObjectArrayList<Sendable> toAdd = new ObjectArrayList<Sendable>();

	private final NetworkStateContainer container;

	private final StateInterface state;

	private final boolean privateChannel;
	long delay = 0;

	public SynchronizationContainerController(NetworkStateContainer container, StateInterface state, boolean privateChannel) {
		this.container = container;
		this.state = state;
		this.privateChannel = privateChannel;
	}

	public void addImmediateSynchronizedObject(Sendable sendable) {
		assert (sendable != null);
		if (sendable.getId() < 0) {
			throw new IllegalArgumentException("[NT][CRITICAL] Tried to add " + sendable + " with illegal ID " + sendable.getId());
		}
		assert (state.isSynched());
		if (NetworkObject.CHECKUNSAVE && !state.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		if (!sendable.isOkToAdd()) {
			System.err.println("Exception: CRITICAL ERROR: could not add object " + sendable);
			return;
		}

		
		sendable.newNetworkObject();
		assert(sendable.getNetworkObject() != null):sendable.getClass();
		sendable.getNetworkObject().init();
		sendable.getNetworkObject().newObject = true;
		sendable.getNetworkObject().addObserversForFields();
		if (sendable instanceof Physical) {
			((Physical) sendable).initPhysics();
		}
		if(state instanceof ServerState){
			((ServerState) state).doDatabaseInsert(sendable);
		}
		container.putLocal(sendable.getId(), sendable);
		sendable.updateToFullNetworkObject();

		sendable.getNetworkObject().setAllFieldsChanged();
		sendable.getNetworkObject().setChanged(true);

		if (sendable.getNetworkObject().id.get() < 0) {
			throw new IllegalArgumentException("[NT][CRITICAL] Tried to add NetworkObject for " + sendable + ": " + sendable.getNetworkObject() + " with illegal ID " + sendable.getNetworkObject().id.get());
		}
		assert (sendable.getNetworkObject().id.get() >= 0);
		assert (sendable.getNetworkObject().newObject);
		//				sendable.getNetworkObject().id.setChanged(true);

		container.getRemoteObjects().put(sendable.getId(), sendable.getNetworkObject());
		state.notifyOfAddedObject(sendable);

	}

	public void addNewSynchronizedObjectQueued(Sendable sendable) {
		if (sendable.getId() < 0) {
			throw new IllegalArgumentException("[NT][CRITICAL] Tried to add " + sendable + " with illegal ID " + sendable.getId());
		}
//		assert(!(sendable instanceof SpaceStation) || ((SpaceStation)sendable).getDbId() > 0);
		assert (!toAdd.contains(sendable));
		synchronized (toAdd) {
			toAdd.add(sendable);
		}

	}

	/**
	 * @return the state
	 */
	public StateInterface getState() {
		return state;
	}

	public void handleQueuedSynchronizedObjects() {

		if (!toAdd.isEmpty()) {

//			System.err.println("TO ADD: "+toAdd);

			long t0 = System.currentTimeMillis();
			synchronized (toAdd) {
				for (int i = 0; i < toAdd.size(); i++) {
					addImmediateSynchronizedObject(toAdd.get(i));
				}
				toAdd.clear();
			}
			long took = System.currentTimeMillis() - t0;
			if (took > 10) {
				System.err.println("[SERVER][UPDATE] WARNING: handleQueuedSynchronizedObjects update took " + took + " on " + state);
			}
		}
	}

	/**
	 * @return the privateChannel
	 */
	public boolean isPrivateChannel() {
		return privateChannel;
	}
}
