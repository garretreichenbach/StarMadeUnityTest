package org.schema.schine.network.synchronization;

import java.io.IOException;
import java.util.Collection;

import org.schema.schine.network.DataInputStreamPositional;
import org.schema.schine.network.NetUtil;
import org.schema.schine.network.NetworkStateContainer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.exception.SynchronizationException;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.UnsaveNetworkOperationException;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.physics.Physical;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SynchronizationReceiver {

	public static boolean serverDebug;

	private static NetworkObject handleChanged(NetworkStateContainer container, int id, StateInterface state,
	                                           DataInputStreamPositional inputStream, short packetId, int updateSenderStateId) throws IOException  {
		Int2ObjectOpenHashMap<NetworkObject> remoteObjects = container.getRemoteObjects();
		Int2ObjectOpenHashMap<Sendable> localObjects = container.getLocalObjects();

		assert (state.isSynched());
		if (NetworkObject.CHECKUNSAVE && !state.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		NetworkObject no;

		// process changed entity
		no = remoteObjects.get(id);
		if (no == null) {
			//this can happen with this lock waits,
			//and something gets deleted meanwhile
			//in this case we can use the ghose object
			no = container.getGhostObjects().get(id).sendable.getNetworkObject();
		}
		// decode the change to the entity
		Sendable localObject = localObjects.get(id);
		if (localObject == null) {
			System.err.println("NTException: local object " + id + " does not exist!");
			if (container.getGhostObjects().get(id) != null) {
				localObject = container.getGhostObjects().get(id).sendable;
			} else {
				throw new NullPointerException("NTException: " + state + " local object " + id + " does not exist (as well as ghost object)!; " + container);
			}
		}
		no.id.set(localObject.getId());

//		if(localObject.toString().startsWith("Missile") && state instanceof ServerStateInterface){
//			System.err.println("SERVER MISSILE UPDATE "+localObject);
//			serverDebug = true;
//		}

		//			if(container != state.getLocalAndRemoteObjectContainer()){
		//				System.err.println(state+": PRIVATE:  ---> handle changed ----> "+no.getClass().getSimpleName()+" : "+localObject.getClass().getSimpleName());
		//			}
		// System.err.println("decoding change sent by "+updateSenderStateId+" "+no.getClass().getSimpleName());
		no.decodeChange(state, inputStream, packetId, container != state.getLocalAndRemoteObjectContainer(), updateSenderStateId);
		assert (localObject != null) : ("Object with id " + id
				+ " is NOT local yet " + container.getLocalObjects());
		localObject.updateFromNetworkObject(no, updateSenderStateId);
		no.clearReceiveBuffers();

		/*
	     * the following line caused a nasty bug. resetting changed after
		 * RECEIVING update to false caused that some updates were dropped
		 * due to racing conditions.
		 * 
		 * There is no need to reset change since, well, all changes must be
		 * sent. Originally the thought probably was to avoid endless loops
		 * of sending. But that has to be taken care of anyway...
		 * 
		 * if (state instanceof ClientStateInterface) {
		 * 		no.setChanged(false);
		 * }
		 */
		serverDebug = false;
		return no;
		// }
	}

	/**
	 * Update deleted events.
	 *
	 * @throws Exception
	 */
	public static void handleDeleted(NetworkStateContainer container, StateInterface state, Collection<Integer> del) {
		del.clear();

		for (Sendable en : container.getLocalObjects().values()) {

			NetworkObject networkObject = container.getRemoteObjects().get(en.getId());

			if (networkObject != null && networkObject.markedDeleted
					.get()) {
				if (state instanceof ServerStateInterface) {
					if (!en.isMarkedForDeleteVolatileSent()) {
						System.err.println("[SERVER] delete not yet sent: " + en);
						continue;
					}
				}
				del.add(en.getId());

				networkObject.onDelete(state);

				en.cleanUpOnEntityDelete();

				container.getGhostObjects().put(en.getId(), new GhostSendable(System.currentTimeMillis(), en));

			}
		}
		if (del.size() > 0) {
			// remove object completely
			for (Integer l : del) {

				//					System.err.println("[DELETE][" + state + "] Sendable " + l
				//							+ " Physically DELETING!");
				Sendable remove = container.removeLocal(l);
				container.getRemoteObjects().remove(l);

				state.getController().onRemoveEntity(remove);
				state.notifyOfRemovedObject(remove);
				System.err.println("[DELETE][" + state + "] Sendable " + l + "(" + remove + ")"
						+ " Physically DELETING DONE and Notified!");
			}
		}
		container.checkGhostObjects();
	}

	private static NetworkObject handleNewObject(NetworkStateContainer container, int id, int updateSenderStateId,
	                                             byte classId, StateInterface state, DataInputStreamPositional inputStream,
	                                             short packetId, boolean isSynchronized)
			throws IOException {

		Int2ObjectOpenHashMap<NetworkObject> remoteObjects = container.getRemoteObjects();
		Int2ObjectOpenHashMap<Sendable> localObjects = container.getLocalObjects();

		Sendable newSendableInstance = null;

		assert (state.isSynched());
		if (NetworkObject.CHECKUNSAVE && !state.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		if (localObjects.containsKey(id)) {
			throw new IOException("Object already existed "+id+"; "+localObjects.get(id)+"; received object would resolve to "+NetUtil.getInstanceName(classId));
			
		} else {
			newSendableInstance = NetUtil.getInstance(classId, state);
			
		}
		long t = System.currentTimeMillis();

		newSendableInstance.initialize();
		newSendableInstance.newNetworkObject();
		newSendableInstance.getNetworkObject().init();
		final NetworkObject no = newSendableInstance.getNetworkObject();
		
		NetworkObject.decode(state, inputStream, no,
				packetId, container != state.getLocalAndRemoteObjectContainer(),
				updateSenderStateId);

		no.onInit(state);

		if (id != no.id.get()) {
			if (state instanceof ClientStateInterface) {
				String s = "[ERROR] in "
						+ state
						+ " received changed object \n"
						+ "|stream for a new object. the id of the received object could \n"
						+ "|not be decoded because it wasnt sent (never ment to be sent). \n"
						+ "|the obj was probably create on the server without knowlegde of \n"
						+ "|this client and has therefore to be re-requested\n"
						+ "|[NTID(" + no.id.get() + "; class: " + no.getClass() + " decoded: " + no.lastDecoded
						+ ") != receivedId[" + id
						+ "] received]; (SenderID: " + updateSenderStateId + "), \n" + "|isSynched("
						+ isSynchronized
						+ ") that was not yet " + "created in "
						+ state + ", \n"
						+ "|SCHEDULING RESYNC. "
						+ "current remotes: " + remoteObjects
						+ ", local: " + localObjects + "; container: " + container.getClass();
				throw new SynchronizationException(s);
			} else {
				assert (false) : "NEW object not correctly en/decoded (probably en)\n on "
						+ state
						+ " received ident: "
						+ id
						+ ", \nencoded: "
						+ no.id.get()
						+ ", \nmarkedForDel: "
						+ no.markedDeleted.get()
						+ ", \nCLASS: "
						+ no.getClass() + " decoded: " + no.lastDecoded
						+ ", \nSENDER: "
						+ updateSenderStateId
						+ ", \nsynchronized = "
						+ isSynchronized
						+ ", \nremotes: "
						+ container;
			}
		}
		//
		// .getRemoteObjects();

		if (no.id.get() < 0) {
			for (int i = 0; i < container.debugReceivedClasses.size(); i++) {
				System.err.println("[ERROR][CRITICAL] something fucked up: received id for new object:  " + no.id.get() + "; last decoded: " + no.lastDecoded + "; SenderID: " + updateSenderStateId);
				NetworkObject networkObject = container.debugReceivedClasses.get(i);
				if (networkObject != null) {
					System.err.println("[DEBUGINFO] decoded class #" + i + ": " + networkObject.getClass().getSimpleName() + "; decoded: " + networkObject.lastDecoded);
				} else {
					System.err.println("debug NetworkObject is null");
				}

			}
			throw new NetworkIDExcpetion();
		}
		newSendableInstance.setId(no.id.get());

		//all synchronized to 'no'
		newSendableInstance.initFromNetworkObject(no);

		if (newSendableInstance instanceof Physical) {
			((Physical) newSendableInstance).initPhysics();
		}
		newSendableInstance.updateFromNetworkObject(no, updateSenderStateId);
		no.clearReceiveBuffers();
		//						System.err.println( state+" ADDING TO LOCAL OBJECTS: "+newSendableInstance);
		// put the local version of this object into
		// the state
		container.putLocal(newSendableInstance.getId(),
				newSendableInstance);

		if (state instanceof ClientStateInterface) {
			//							System.err
			//							.println("["
			//									+ state
			//									+ "] received object set to not-new on client "
			//									+ state);
			no.newObject = false;

		}
		no.addObserversForFields();
		//						System.err.println( state+" ADDING TO REMOTE OBJECTS: "+newSendableInstance);
		remoteObjects.put(no.id.get(), no);
		state.notifyOfAddedObject(newSendableInstance);
		long took = (System.currentTimeMillis() - t);
		if (took > 10) {
			System.err.println("[SYNC-RECEIVER] " + state + " DECODING OF NEW OBJECT " + newSendableInstance + " TOOK " + took);
		}

		return no;
	}

	public static void update(NetworkStateContainer container, int updateSenderStateId,
	                          DataInputStreamPositional inputStream, StateInterface state,
	                          boolean isSynchronized, boolean forced, short packetId, ObjectArrayList<NetworkObject> lastReceived) throws IOException 
		{

		lastReceived.clear();

		Int2ObjectOpenHashMap<NetworkObject> remoteObjects = container.getRemoteObjects();

		assert(state.isReady());
		/*
         * WARING: ALWAYS KEEP THE SAME ORDER OF SYNCHRONIZATION
		 * #1: localObjects
		 * #2: remoteObjects
		 */

		final int size = inputStream.readInt();
		if (forced) {
			// if all remote objects are removed
			// all local object will be sent completely
			System.err.println("[SYNCHRONIZE] FORCED UPDATE");
		}
		byte classId;
		for (int i = 0; i < size; i++) {
			int id = inputStream.readInt();
			if (forced) {
				assert (state.isSynched());
				remoteObjects.remove(id);
			}
			
			classId = inputStream.readByte();
			GhostSendable gs = null;
			NetworkObject next = null;
			
			if (isSynchronized && remoteObjects.containsKey(id)
					&& !forced) {
				next = handleChanged(container, id, state, inputStream, packetId, updateSenderStateId);
				container.debugReceivedClasses.add(next);
				lastReceived.add(next);
			} else if ((gs = container.getGhostObjects().get(id)) != null) {

				System.err.println(state + ": Exception: Received update for ghost object: " + id + "; ignoring update");
				gs.sendable.getNetworkObject().decodeChange(state, inputStream, packetId, container != state.getLocalAndRemoteObjectContainer(), updateSenderStateId);
				System.err.println(state + ": Exception: Received update for ghost object: " + id + "; ignoring update: DECODED CHANGE TO GHOST OBJECT " + container.getGhostObjects().get(id));
				System.err.println(state + ": Exception: Ghost object added to debug at index: " + container.debugReceivedClasses.size());
				container.debugReceivedClasses.add(gs.sendable.getNetworkObject());

				lastReceived.add(gs.sendable.getNetworkObject());
			} else {


				next = handleNewObject(container, id, updateSenderStateId, classId,
						state, inputStream, packetId,
						isSynchronized);

				container.debugReceivedClasses.add(next);
			}

		}

		if (forced) {
			Int2ObjectOpenHashMap<Sendable> localObjects = container.getLocalObjects();
			if (!localObjects.keySet().equals(remoteObjects.keySet())) {
				try {
					System.err.println("LOCAL : " + localObjects);
					System.err.println("REMOTE: " + remoteObjects);
					throw new SynchronizationException("[CLIENT] " + state + " invalid synchronization state: local and remote objects differ");
				} catch (SynchronizationException e) {
					e.printStackTrace();
					assert (false);
				}
			}

		}
		container.debugReceivedClasses.clear();

	}
}
