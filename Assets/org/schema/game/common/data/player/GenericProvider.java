package org.schema.game.common.data.player;

import org.schema.game.common.controller.NetworkListenerEntity;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.server.ServerStateInterface;

public abstract class GenericProvider<E extends SimpleTransformableSendableObject> implements NetworkListenerEntity{

	
	
	protected int id = -123123;
	protected final StateInterface state;
	protected final boolean onServer;
	private boolean markedForDeleteVolatile;
	private boolean markedForDeleteSent;
	private int clientId;
	private E providedObject;
	private boolean listen;
	
	public GenericProvider(StateInterface state) {
		this.state = state;
		this.onServer = state instanceof ServerStateInterface;
	}
	public boolean isConnectionReady() {
		return getSegmentController() != null && getNetworkObject() != null && getNetworkObject().connectionReady.get();
	}
	@Override
	public void cleanUpOnEntityDelete() {
	}

	@Override
	public void destroyPersistent() {
		//not persistant
	}


	@Override
	public StateInterface getState() {
		return state;
	}

	@Override
	public void initFromNetworkObject(NetworkObject o) {
		id = getNetworkObject().id.get();
		clientId = getNetworkObject().clientId.get();
		if(getSegmentController() != null){
			getSegmentController().addListener(this);
			listen = true;
		}
//		newObjectOnServer = 2;
	}
	public void setProvidedObject(E e){
		this.providedObject = e;
	}
	public E getSegmentController(){
		if (providedObject == null) {
			providedObject = (E) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
		}
		return providedObject;
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
		markedForDeleteVolatile = markedForDelete;		
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
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		if(getSegmentController() != null &&
				getSegmentController().getRemoteTransformable() != null &&
				getNetworkObject() != null){
			getSegmentController()
			.getRemoteTransformable()
			.receiveTransformation(
					getNetworkObject());		
		}else{
			try {
				if(getSegmentController() == null){
					throw new Exception("segmentController of this provider is null ("+id+") Maybe it was removed on server");
				}else if(getSegmentController().getRemoteTransformable() == null){
					throw new Exception("segmentController.remoteTransformable of this provider is null ("+id+")");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateLocal(Timer timer) {
		if(!listen && getSegmentController() != null){
			getSegmentController().addListener(this);
			listen = true;
		}
	}

	@Override
	public void updateToFullNetworkObject() {
		getNetworkObject().id.set(id);
		getNetworkObject().clientId.set(clientId);
		if (onServer) {
			getNetworkObject().connectionReady.set(true);
		}

	}

	@Override
	public void updateToNetworkObject() {
		getNetworkObject().id.set(id);
		if (onServer) {
			getNetworkObject().connectionReady.set(true);

		}
	}


	@Override
	public boolean isWrittenForUnload() {
		return true;
	}

	@Override
	public void setWrittenForUnload(boolean b) {
	}

	@Override
	public void announceLag(long timeTaken) {
	}

	@Override
	public long getCurrentLag() {
		return 0;
	}

	@Override
	public void setClientId(int id) {
		clientId = id;		
	}

	@Override
	public int getClientId() {
		return clientId;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}
	@Override
	public boolean isSendTo() {
		if (!onServer) {
			//a modification from client is always sent as the object is in range by default
			return true;
		}
		RegisteredClientOnServer registeredClientOnServer = ((GameServerState) state).getClients().get(clientId);
		if (registeredClientOnServer != null) {
			PlayerState p = (PlayerState) registeredClientOnServer.getPlayerObject();

			Sector sector = ((GameServerState) state).getUniverse().getSector(getSegmentController().getSectorId());

			return sector != null && Sector.isNeighbor(p.getCurrentSector(), sector.pos);
		}
		return false;
	}
	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.SEG_PROVIDER;
	}
}
