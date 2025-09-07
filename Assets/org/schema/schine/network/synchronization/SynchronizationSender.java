package org.schema.schine.network.synchronization;

import java.io.IOException;
import java.util.List;

import org.schema.schine.network.DataOutputStreamPositional;
import org.schema.schine.network.NetworkStateContainer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.commands.SynchronizeAllCommandPackage;
import org.schema.schine.network.exception.SynchronizationException;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.UnsaveNetworkOperationException;
import org.schema.schine.network.server.ServerProcessorInterface;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SynchronizationSender {
	public static final int RETURN_CODE_CHANGED_OBJECT = 1;
	public static final int RETURN_CODE_NOTHING_CHANGED = 0;
	public static boolean clientDebug;
	private static void determineNeedsUpdate(Sendable g, NetworkObject nwobj, List<Sendable> toUpdateSet) {
	    /*
         * check only if object was not yet marked for deleted.
		 * if markedDeleted is already true. don't reset it to
		 * false
		 * 
		 * if server marks -> when client received obj ->
		 * isMarkedForDeleteVolatile() == false &&
		 * nwobj.markedDeleted.get() == true
		 */
		if (g.isMarkedForDeleteVolatile()) {
			nwobj.markedDeleted.set(g.isMarkedForDeleteVolatile(), true);
			nwobj.setChanged(true);
			toUpdateSet.add(g);
		} else if (nwobj.newObject || nwobj.isChanged()) {
			if (nwobj.markedDeleted.get() && !g.isMarkedForDeleteVolatile()) {
				// assure object isn't sent
				nwobj.newObject = false;
				nwobj.setChanged(false);
			} else {
				toUpdateSet.add(g);
			}
		}
	}

	private static void encodeFullObject(Sendable sendableToSend, NetworkObject networkObjectToSend, DataOutputStreamPositional outputStream, boolean leaveObjectInChangedState, boolean privateChannel, boolean forceAllNew) throws IOException {
		// encode full object in output stream (even if it hasnt changed at all)
		// update with everything from this object
		networkObjectToSend = sendableToSend.getNetworkObject();
		if (sendableToSend.getId() < 0) {
			throw new IllegalArgumentException("[SENDER] Exception writing negative id for " + sendableToSend + ": " + sendableToSend.getId() + " " + sendableToSend.getState());
		}

		sendableToSend.updateToFullNetworkObject();
		// encode ALL fields (changed or not)
		// always send encode object when encoding new
		// object
		int sizeBefore = outputStream.size();
		boolean keepChanged = NetworkObject.encode(sendableToSend, networkObjectToSend, true,
				outputStream, leaveObjectInChangedState, privateChannel, forceAllNew);

		int sizeAfter = outputStream.size();

		int updateSize = sizeAfter - sizeBefore;
		if (updateSize > 5 * 1024) {
			System.err.println("[SERVER][SYNCH][NT] Big FullUpdate: " + updateSize + " bytes: " + sendableToSend);
		}

		if (!leaveObjectInChangedState) {

			networkObjectToSend.setChanged(keepChanged);
			networkObjectToSend.newObject = false;
		}
	}

	/**
	 * Send network objects.
	 *
	 * @param i
	 * @param localObjects       the from
	 * @param remoteObjects      the to
	 * @param updateCommandClass the update command
	 * @param clients            the clients
	 * @throws IOException
	 */
	public static int encodeNetworkObjects(NetworkStateContainer container, StateInterface sendingState,
	                                       DataOutputStreamPositional outputStream, final boolean forceAllNew) throws IOException {

		assert (sendingState.isSynched());
		if (NetworkObject.CHECKUNSAVE && !sendingState.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}

		Int2ObjectOpenHashMap<Sendable> localObjects = container.getLocalObjects();
		Int2ObjectOpenHashMap<NetworkObject> remoteObjects = container.getRemoteObjects();

		ObjectArrayList<Sendable> updateList = container.updateSet;
		Int2BooleanOpenHashMap newStatesBeforeForce = container.newStatesBeforeForce;
		updateList.clear();
		newStatesBeforeForce.clear();

		boolean existsChangedObject = false;
		int forceAllCount = 0;
		boolean debugDelete = false;
		if (forceAllNew) {
			//save states before full update so it can be put into incement mode afterwards
			for (NetworkObject no : remoteObjects.values()) {
				newStatesBeforeForce.put(no.id.get().intValue(), no.newObject);
				no.newObject = true;
				forceAllCount++;
			}
		}
		//removed objects only need to be checked when incremental
		for (Sendable g : localObjects.values()) {
			if (g.getId() < 0) {
				throw new IllegalArgumentException("[SENDER] Exception writing negative id for " + g + ": " + g.getId() + " " + g.getState());
			}

			if (g.isMarkedForDeleteVolatile()) {
				debugDelete = true;
			}
			NetworkObject objectToSend = remoteObjects.get(g.getId());

			if (objectToSend == null) {
				try {
					throw new SynchronizationException("!!!!!!!!!! sendingState(" + sendingState + ")FATAL-ERROR: " + g.getId()
							+ " does not exist: " + remoteObjects
							+ ", LOCAL: " + localObjects);
				} catch (SynchronizationException e) {
					e.printStackTrace();
					assert (false);
				}
			}

			assert (!forceAllNew || objectToSend.newObject) : " failed: forceAll -> objbectNew: "
					+ objectToSend + ": " + objectToSend.newObject;

			//for forced update all objects are being added
			determineNeedsUpdate(g, objectToSend, updateList);
		}
	

		if (updateList.isEmpty()) {
			return RETURN_CODE_NOTHING_CHANGED;
		}

		assert (!forceAllNew || forceAllCount == updateList.size()) : " force all "
				+ forceAllCount + ": " + updateList.size();
//		System.err.println("SENDING OBJECT: "+updateList.size()+" ------> "+updateList);
		if (forceAllNew) {
			System.err.println("[SERVER][SYNCH][FULL] SENDING ALL OBJECTS: " + updateList.size());
		}
		final int updateListSize = updateList.size();
		outputStream.writeInt(updateList.size());

		int verifySize = 0;
		for (int i = 0; i < updateListSize; i++) {
			Sendable sendableToSend = updateList.get(i);
			
			NetworkObject nwobj = remoteObjects.get(sendableToSend.getId());
			
			if (!nwobj.newObject) {
				//do incremental update (for forced all objects are in newObject state)
				assert(!forceAllNew);
				boolean objectChanged = encodePartialObjectIfChanged(sendableToSend, nwobj, outputStream, container != sendingState.getLocalAndRemoteObjectContainer() || forceAllNew);

				if (objectChanged) {
					verifySize++;
				}

				existsChangedObject = existsChangedObject || objectChanged;
			} else {

				
				assert (!(nwobj.markedDeleted.get() && !sendableToSend.isMarkedForDeleteVolatile()));
				existsChangedObject = true;
				boolean leaveObjectInChanged = false;
				encodeFullObject(sendableToSend, nwobj, outputStream, leaveObjectInChanged, container != sendingState.getLocalAndRemoteObjectContainer(), forceAllNew);
				verifySize++;
				
			}
			if (sendableToSend.isMarkedForDeleteVolatile()) {
				//						System.err.println("[SERVER] delete notify has been sent for "+g+"!");
				sendableToSend.setMarkedForDeleteVolatileSent(true);
			}
			SynchronizationSender.clientDebug = false;
		}

		assert (updateList.size() == verifySize) : " WRONG NUMBER OF OBJECTS WRITTEN: "
				+ updateList.size() + " / " + verifySize;

		if (forceAllNew) {
			for (NetworkObject no : remoteObjects.values()) {
				no.newObject = newStatesBeforeForce.get(no.id.get());
			}
		}

		if (existsChangedObject) {
			return RETURN_CODE_CHANGED_OBJECT;
		} else {
			return RETURN_CODE_NOTHING_CHANGED;
		}

	}

	private static boolean encodePartialObjectIfChanged(Sendable g, NetworkObject nwobj, DataOutputStreamPositional outputStream, boolean keepAllChanges) throws IOException {
		boolean changed = false;
		assert (nwobj.getState().isSynched());
		if (NetworkObject.CHECKUNSAVE && !nwobj.getState().isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		if (nwobj.isChanged()) {

			assert (!(nwobj.markedDeleted.get() && !g
					.isMarkedForDeleteVolatile()));
			// object changed
			changed = true;

			boolean keepChanged = nwobj.encodeChange(g, outputStream, keepAllChanges);
			// NEVER put this above the encode change
			// function :/

			nwobj.setChanged(keepChanged);
			nwobj.newObject = false;
		}
		return changed;
	}

	
	
	public static void sendSynchAll(StateInterface state, ServerProcessorInterface serverProcessor) throws IOException {

		
		SynchronizeAllCommandPackage pack = new SynchronizeAllCommandPackage();
		pack.prepareSending();
		
		
		
		System.err.println("[SERVER] sending ALL-SYNCHRONIZING update to " + serverProcessor.getClient().getClientName() + " " + state.getLocalAndRemoteObjectContainer().getLocalObjectsSize() + ", " + state.getLocalAndRemoteObjectContainer().getRemoteObjects().size());

		final int returnCode;
		boolean forceAll = true;
		// WRITING TO OUTPUT STREAMS
		returnCode = SynchronizationSender.encodeNetworkObjects(state.getLocalAndRemoteObjectContainer(), state,
				pack.out, forceAll);
		
		pack.send(serverProcessor);
			
	}
	

}
