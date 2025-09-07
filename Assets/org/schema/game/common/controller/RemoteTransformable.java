package org.schema.game.common.controller;

import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.common.controller.ntsmothers.TransformationSmoother;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.NetworkEntityProvider;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.objects.LocalSectorTransition;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.NetworkTransformation;
import org.schema.schine.network.objects.WarpTransformation;
import org.schema.schine.network.objects.container.PhysicsDataContainer;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.network.objects.remote.RemoteFloatPrimitiveArray;
import org.schema.schine.network.objects.remote.RemoteWarpTransformation;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.physics.Physical;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class RemoteTransformable implements Transformable, Physical {
	private static final int EQUAL_TIMES = 32;
	private static final int LIN_ROT_UPDATES = 8;
	public final List<WarpTransformation> warpedTransformations = new ObjectArrayList<WarpTransformation>();
	final ReceivedTransformation receivedTransformation = new ReceivedTransformation();
	private final Transform initialTransform;
	private final boolean onServer;
	private final ReceivedTransformation receivedTransformationLocal = new ReceivedTransformation();
	private final Transform nextTransform = new Transform();
	private final Vector3f tmpDist = new Vector3f();
	private final Transform tmpT = new Transform();
	public boolean useSmoother = true;
	public int equalCounter;
	int uCounter = 0;
	private TransformationSmoother transformationSmoother;
	private PhysicsDataContainer physicsDataContainer;
	private float mass = 0.1f;
	private Identifiable obj;
	private StateInterface state;
	private Transform lastTransform = new Transform();
	private boolean sendFromClient;
	private boolean snapped;
	private Vector3f tmp = new Vector3f();
	private long lastSent;
	private boolean sendLinearAndAngularFromServer;
	private long lastMassUpdate;
	private boolean waitingForApprovalWarp;
	private long lastAll;
	private long timeOfPhysicsUpdate;

	public RemoteTransformable(Identifiable obj, StateInterface state) {
		this.obj = obj;
		this.state = state;
		onServer = state instanceof ServerStateInterface;
		transformationSmoother = new TransformationSmoother(this);
		//		if(obj instanceof Missile){
		//			transformationSmoother.setActive(false);
		//		}
		initialTransform = new Transform();
		initialTransform.setIdentity();
		physicsDataContainer = new PhysicsDataContainer();

		//		remotePhysicsTransform = new RemotePhysicsTransform(ntt, isOnServer());
	}

	public void broadcastTransform(Transform t, Vector3f linVelo, Vector3f angVelo, LocalSectorTransition trn, NetworkEntity e) {
		assert (getState() instanceof ServerStateInterface);
		RemoteFloatPrimitiveArray remoteFloatArray = new RemoteFloatPrimitiveArray(22, e);
		t.getOpenGLMatrix(remoteFloatArray.getFloatArray());
		//		remoteFloatArray.setArray(remoteFloatArray.getFloatArray());
		remoteFloatArray.getFloatArray()[16] = linVelo.x;
		remoteFloatArray.getFloatArray()[17] = linVelo.y;
		remoteFloatArray.getFloatArray()[18] = linVelo.z;
		remoteFloatArray.getFloatArray()[19] = angVelo.x;
		remoteFloatArray.getFloatArray()[20] = angVelo.y;
		remoteFloatArray.getFloatArray()[21] = angVelo.z;
		
		WarpTransformation wt = new WarpTransformation();
		wt.t = new Transform(t);
		wt.lin = new Vector3f(linVelo);
		wt.ang = new Vector3f(angVelo);
		wt.local = trn;
		
		e.warpingTransformation.add(new RemoteWarpTransformation(wt, e));
		//		System.err.println("[TRANSFORMATION] broadCast: "+t.origin);
		if(obj instanceof PlayerControllable){
			PlayerControllable c = (PlayerControllable) obj;
			if(!c.getAttachedPlayers().isEmpty()){
				System.err.println("################# SERVER WAITING FOR PLAYER WARP");
				waitingForApprovalWarp = true;
			}
		}
	}

	private void checkReceivedTransformation() {

		if (receivedTransformation.changed) {
			synchronized (receivedTransformation) {
				receivedTransformationLocal.timestamp = receivedTransformation.timestamp;
				receivedTransformationLocal.transform.set(receivedTransformation.transform);
				receivedTransformationLocal.linearVelocity.set(receivedTransformation.linearVelocity);
				receivedTransformationLocal.angularVelocity.set(receivedTransformation.angularVelocity);
				receivedTransformationLocal.receivedVelocities = receivedTransformation.receivedVelocities;
				receivedTransformationLocal.playerAttached = receivedTransformation.playerAttached;

				receivedTransformationLocal.changed = true;
				receivedTransformationLocal.changedOnce = true;
				receivedTransformation.changed = false;
			}
		}

		if (receivedTransformationLocal.changed) {
			receivedTransformationLocal.changed = false;
			if (physicsDataContainer.getObject() instanceof PairCachingGhostObjectAlignable) {
				PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) physicsDataContainer.getObject());
				if (receivedTransformationLocal.playerAttached != (o.getAttached() != null)) {
					//					System.err.println("NOT RECEIVED: playerattached update while nothing attached "+receivedTransformationLocal.playerAttached+" / "+o.getAttached());
					receivedTransformationLocal.changedOnce = false;
					return;
				} else {
					//					System.err.println("RECEIVED: playerattached update while nothing attached "+receivedTransformationLocal.playerAttached+" / "+o.getAttached());
				}
			}
			//			if(isOnServer() && getIdentifiable() instanceof AbstractCharacter<?>){
			//				System.err.println("RECE "+receivedTransformationLocal.transform.origin);
			//			}
			//			if(isOnServer() && getIdentifiable() instanceof AbstractCharacter<?>){
			//				if(getPhysicsDataContainer().getObject() instanceof PairCachingGhostObjectAlignable){
			//					PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable)getPhysicsDataContainer().getObject());
			//					System.err.println("RECEIVED: playerattached update while nothing attached "+receivedTransformationLocal.playerAttached+" / "+o.getAttached());
			//				}
			//			}

			CollisionObject object = physicsDataContainer.getObject();
			if (object instanceof RigidBody) {
				RigidBody rb = (RigidBody) object;
				if (!onServer && ((GameClientState) getState()).currentEnterTry == obj && (System.currentTimeMillis() - ((GameClientState) getState()).currentEnterTryTime < 1000)) {
					//smoother can sometimes be still applied in teh short instance after getting into a ship/etc
					System.err.println("NOT SMOOTHING VELO SHIP");
				} else {
					if (receivedTransformationLocal.receivedVelocities && !rb.isStaticObject()) {
						rb.setLinearVelocity(receivedTransformationLocal.linearVelocity);
						rb.setAngularVelocity(receivedTransformationLocal.angularVelocity);
						if (onServer) {
							sendLinearAndAngularFromServer = true;
						}
					}
				}

			}

			boolean smooth = isSmoothingGeneral();
			
			
			if (smooth) {
				
				//				if(!isOnServer() && obj.toString().contains("schema")){
				//					System.err.println("SMOOTHY: "+((GameClientState)getState()).currentEnterTry+" / "+obj);
				//				}
				if (!onServer && ((GameClientState) getState()).currentEnterTry == obj && (getState().getUpdateTime() - ((GameClientState) getState()).currentEnterTryTime < 1000)) {
					//smoother can sometimes be still applied in teh short instance after getting into a ship/etc
					System.err.println("[CLIENT] NOT SMOOTHING SHIP");
				} else if (!onServer) {
					if (isSmoothingEntity()) {
//						System.err.println(" "+getState()+"   "+getIdentifiable()+" smooth do step: ");
						// check if current transform has to be corrected
						transformationSmoother.recordStep(new Transform(receivedTransformationLocal.transform), receivedTransformationLocal.timestamp);
//						if(transformationSmoother.isSelectedOnClient()) {
//							System.err.println("SMOOTHING");
//						}
					}else {
						transformationSmoother.onNoSmooth();
					}
				}
			} else {
				transformationSmoother.onNoSmooth();
				if(onServer && waitingForApprovalWarp){
//					System.err.println("[SERVER] "+getIdentifiable()+" NOT APPLYING RECEIVED TRANS ON SERVER "+receivedTransformationLocal.transform.origin);
					//don't apply any remote transforms on server if they were sent from a client
					//that has the objects in an old sector still (sector switch sent but not applied on client)
					
				}else{
//					if(isOnServer()){
//						System.err.println("SERVER INSTANT STEP:::: "+receivedTransformationLocal.transform.origin+"; ");
//					}
					transformationSmoother.doInstantStep(receivedTransformationLocal.transform);
				}
			}
		}

	}
	public boolean isSmoothingEntity() {
		boolean docked = obj instanceof SegmentController && ((SegmentController) obj).getDockingController().isDocked();
		return !docked && useSmoother && !((SimpleTransformableSendableObject) obj).isHidden();
	}
	public boolean isSmoothingGeneral() {
		if (onServer) {
			return false;//this.getIdentifiable() instanceof SimpleTransformableSendableObject;
		} else {
			return this.obj instanceof SimpleTransformableSendableObject &&
					((SimpleTransformableSendableObject) this.obj).getSectorId() ==
							((GameClientState) getState()).getCurrentSectorId() && !GameClientState.smoothDisableDebug;
		}
	}
	public long getCurrentLatency() {
		if (state instanceof ServerStateInterface) {
			return 0; //no latency on server
		}

		return ((ClientState) state).getPing();
	}

	/**
	 * @return the equalCounter
	 */
	public int getEqualCounter() {
		return equalCounter;
	}

	/**
	 * @param equalCounter the equalCounter to set
	 */
	public void setEqualCounter(int equalCounter) {
		this.equalCounter = equalCounter;
	}

	public Identifiable getIdentifiable() {
		return obj;
	}

	@Override
	public Transform getInitialTransform() {
		return initialTransform;
	}

	@Override
	public float getMass() {
		return mass;
	}

	@Override
	public PhysicsDataContainer getPhysicsDataContainer() {
		return physicsDataContainer;
	}

	@Override
	public void setPhysicsDataContainer(
			PhysicsDataContainer physicsDataContainer) {
		this.physicsDataContainer = physicsDataContainer;
	}
	//	public boolean isControlelledByClient(){
	//		if(getState() instanceof ClientState && ((GameClientState)getState()).getShip() == getIdentifiable()){
	//			if(!((Ship)getIdentifiable()).getAttachedPlayers().isEmpty() && ((Ship)getIdentifiable()).getAttachedPlayers().get(0) == ((GameClientState)getState()).getPlayer()){
	//				//send from client if the ship is currently controlled by this client
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	@Override
	public TransformTimed getWorldTransform() {

		return physicsDataContainer.getCurrentPhysicsTransform();
	}

	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	/**
	 * @return the sendFromClient
	 */
	public boolean isSendFromClient() {
		boolean s = sendFromClient || (!onServer && obj instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) obj).isChainedToSendFromClient());
//		if(!isOnServer() && obj instanceof SegmentController && ((SegmentController)obj).railController.isDockedAndExecuted()){
//			System.err.println("SEND FROM CLIENT:::: "+obj+" :: "+s);
//		}
		return s;
	}

	/**
	 * @param sendFromClient the sendFromClient to set
	 */
	public void setSendFromClient(boolean sendFromClient) {
		this.sendFromClient = sendFromClient;
	}

	/**
	 * @return the snapped
	 */
	public boolean isSnapped() {
		return snapped;
	}

	/**
	 * @param snapped the snapped to set
	 */
	public void setSnapped(boolean snapped) {
		this.snapped = snapped;
	}

	public void receiveTransformation(NetworkEntityProvider e) {
		
		if (getState() instanceof ClientState && isSendFromClient()) {
			//transformation is sent from this
			return;
		}
//		if(isOnServer() && !isSendFromClient()){
//			//don't let the client tell the server any positions
//			//unless the object is client controlled
//			System.err.println("RECEIVED TRANSFORM FROM "+getIdentifiable());
//			return;
//		}
		//		RemoteVector3f linearVelocity = e.linearVelocityBuffer;
		//		RemoteVector3f angularVelocity = e.angularVelocityBuffer;

		
		
		boolean received = false;
		NetworkTransformation networkTransformation = e.transformationBuffer.get();
		Transform receivedTrans = e.transformationBuffer.get().getTransformReceive();

		Vector3f vLin = new Vector3f(e.transformationBuffer.get().getLinReceive());
		Vector3f vAn = new Vector3f(e.transformationBuffer.get().getAngReceive());

		long curTS = networkTransformation.getTimeStampReceive();

		if (networkTransformation.received) {
			networkTransformation.received = false;
			//			System.err.println("UUUUU "+getIdentifiable()+" RECEIVED: "+t.origin);
			received = true;
//			if(isOnServer()){
//				System.err.println("REC "+getIdentifiable());
//			}
		}
		//		System.err.println("CURRENTLY: "+receivedTrans.origin+"; "+received);
		//		if(!received){
		//			System.err.println("NOT RECEIVED: c "+curTS+" / l "+lastTransformTimestamp+"           cur: "+System.currentTimeMillis());
		//		}
		
		
		
		if (received) {
			synchronized (receivedTransformation) {
				receivedTransformation.timestamp = curTS;
				receivedTransformation.transform.set(receivedTrans);
				receivedTransformation.linearVelocity.set(vLin);
				receivedTransformation.angularVelocity.set(vAn);
				receivedTransformation.receivedVelocities = networkTransformation.receivedVil;
				receivedTransformation.playerAttached = networkTransformation.isPlayerAttachedReceive();
				assert (receivedTransformation.transform.getMatrix(new Matrix4f()).determinant() != 0) : obj + "\n" + receivedTransformation.transform.getMatrix(new Matrix4f());
				networkTransformation.receivedVil = false;
				receivedTransformation.changed = true;
					
				
//				System.err.println("REMOTE TRANSFORMATION RECEIVED: "+receivedTransformation.transform.origin+" on "+getState()+" of "+getIdentifiable());
				boolean okCheck = !(obj instanceof FloatingRock) || receivedTransformation.transform.origin.lengthSquared() != 0;
				if(!okCheck){
					try {
						throw new Exception("WARNING: received strange transformation: "+ obj +"; "+getState()+"; "+receivedTransformation.transform.origin);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
			}
		}

	}

	private void receiveWarpTransformation(NetworkEntity e) {
		if (getState() instanceof ClientState && physicsDataContainer.isInitialized()) {

			for (int i = 0; i < e.warpingTransformation.getReceiveBuffer().size(); i++) {
				RemoteWarpTransformation floatArray = e.warpingTransformation.getReceiveBuffer().get(i);
				this.warpedTransformations.add(floatArray.get());

			}
		}
	}
	private void sendTransform() {
		if (obj instanceof SegmentController &&
				(((SegmentController) obj).getDockingController().isDocked() ||
						((SegmentController) obj).railController.isDockedOrDirty())) {
			//not sending docked (is sent seperately, since a lot less updates are necessary)
			return;
		}
		if (!physicsDataContainer.isInitialized()) {
			return;
		}
		if (obj instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) obj).isHidden()) {
			return;
		}
		
		boolean playerAttached = false;
		nextTransform.set(physicsDataContainer.getOriginalTransform());
		if (physicsDataContainer.getObject() instanceof PairCachingGhostObjectAlignable) {
			PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) physicsDataContainer.getObject());
			if (o.getAttached() != null) {
				playerAttached = true;
				nextTransform.set(o.localWorldTransform);
			}
		}
		
		SimpleTransformableSendableObject<? extends NetworkListenerEntity> se = (SimpleTransformableSendableObject<? extends NetworkListenerEntity>) obj;
		
		boolean send = false;
		boolean sendVeloRot = false;
		if (!physicsDataContainer.getObject().isStaticOrKinematicObject() &&
				timeOfPhysicsUpdate - lastSent > 20000 || sendLinearAndAngularFromServer) {
			sendLinearAndAngularFromServer = false;
			equalCounter = EQUAL_TIMES;
			uCounter = LIN_ROT_UPDATES + 1;
		}

		boolean equalsTrans = lastTransform.equals(nextTransform);
		boolean movable = (!physicsDataContainer.getObject().isStaticOrKinematicObject()
				|| physicsDataContainer.getObject() instanceof PairCachingGhostObjectAlignable);
		if(obj instanceof SegmentController && ((SegmentController) obj).getMass() <= 0){
			movable = false;
		}
		
		if (!equalsTrans || (movable && equalCounter >= EQUAL_TIMES)) {
			equalCounter = 0;
			send = true;
			
			CollisionObject object = physicsDataContainer.getObject();
			if (object instanceof RigidBody) {
				if (uCounter > LIN_ROT_UPDATES) {
					sendVeloRot = true;
					uCounter = 0;
				} else {
					
				}
			}

			lastTransform.set(nextTransform);
			uCounter++;
			lastSent = (System.currentTimeMillis());
		} else {
			if (equalsTrans) {
				equalCounter++;
			}
		}
		boolean forceSendAll = false;
		
//		if (!isOnServer() && getIdentifiable() instanceof PlayerCharacter) {
//			PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) getPhysicsDataContainer().getObject());
//			System.err.println("CLIENT PLAYER OBJECT: "+getIdentifiable()+": send "+send+"; movable "+movable+"; equal "+equalsTrans+"; "+o.getAttached());
//		}
		
		if(send && timeOfPhysicsUpdate - lastAll > 3000){
			//do lazy updating of objects out of range
			lastAll = timeOfPhysicsUpdate;
			forceSendAll = true;
		}
//		System.err.println("3SEND TRANFRM "+nextTransform.origin+" on "+getState()+" of "+getIdentifiable());
		
		for(NetworkListenerEntity listener : se.getListeners()){
			if((listener.isSendTo() || forceSendAll) && send){
				NetworkEntityProvider e = listener.getNetworkObject();
				e.transformationBuffer.get().setTimeStamp(timeOfPhysicsUpdate);
				e.transformationBuffer.get().getTransform().set(nextTransform);
				e.transformationBuffer.get().setPlayerAttached(playerAttached);
				CollisionObject object = physicsDataContainer.getObject();
				if (object instanceof RigidBody) {
					RigidBody rb = (RigidBody) object;
					e.transformationBuffer.get().setLin(rb.getLinearVelocity(tmp));
					e.transformationBuffer.get().setAng(rb.getAngularVelocity(tmp));
					
					rb.getLinearVelocity(tmp);
					if (sendVeloRot) {
						e.transformationBuffer.get().sendVil = true;
					} else {
						e.transformationBuffer.get().sendVil = false;
					}
				}
				assert (e.transformationBuffer.get().getTransform().getMatrix(new Matrix4f()).determinant() != 0) : "\n" + receivedTransformation.transform.getMatrix(new Matrix4f());
				e.transformationBuffer.setChanged(true);
				e.setChanged(true);
				
				e.transformationBuffer.get().prime = true;
				boolean okCheck = !(obj instanceof FloatingRock) || nextTransform.origin.lengthSquared() != 0;
				if(!okCheck){
					try {
						throw new Exception("WARNING: SENDING strange transformation: "+ obj +"; "+getState()+"; "+nextTransform.origin);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public void setLagCompensation(boolean b) {

	}

	public void update(Timer timer) {

		//		if(getIdentifiable() instanceof Ship){
		//			if(isOnServer()){
		//				System.err.println("SERVER POS "+getIdentifiable()+" "+getWorldTransform().origin);
		//			}else{
		//				System.err.println("CLIENT POS "+getIdentifiable()+" "+getWorldTransform().origin);
		//			}
		//		}

		if (physicsDataContainer.isInitialized() && physicsDataContainer.getObject() == null) {
			receivedTransformation.changed = false;
			receivedTransformation.transform.set(getWorldTransform());
		}
		
//		if (isOnServer() && getIdentifiable() instanceof PlayerCharacter) {
//			PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) getPhysicsDataContainer().getObject());
//			System.err.println("SSER PLAYER OBJECT: "+getIdentifiable()+"; "+o.getAttached());
//		}
		
		if (physicsDataContainer.isInitialized() && physicsDataContainer.getObject() != null) {

			//			if(obj instanceof Ship && Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_COMMA) && Keyboard.isKeyDown(GLFW.GLFW_KEY_COLON)){
			//				getPhysicsDataContainer().updateMass(getMass());
			//			}

			checkReceivedTransformation();

			if (physicsDataContainer.getObject() instanceof RigidBody) {
				Vector3f linearVelocity = ((RigidBody) physicsDataContainer.getObject()).getLinearVelocity(new Vector3f());
				//				if(linearVelocity.length() > 0){
				//					System.err.println("CURRENT LINVEL: "+obj+": "+linearVelocity);
				//				}
			}
			boolean warped = false;
			while (!warpedTransformations.isEmpty()) {
				warped = true;
				if(obj instanceof SimpleTransformableSendableObject){
					if(!((SimpleTransformableSendableObject)obj).isHidden()){
						((SimpleTransformableSendableObject)obj).setWarpToken(true);
					}
				}
				WarpTransformation wp = warpedTransformations.remove(0);
				
				
				Transform t = new Transform(wp.t);
				if(wp.local != null){
					assert(!onServer);
					//local transition to avoid small jump on client on sector change
					if (physicsDataContainer.getObject() instanceof RigidBody &&
							!wp.local.oldPosPlanet && !wp.local.newPosPlanet) {
//						System.err.println("[CLIENT][REMOTETRANSFORMABLE] sector change local for client directly");
						t = wp.local.getTransitionTransform(getWorldTransform());
					
						//we are using our own velocities
						((RigidBody) physicsDataContainer.getObject()).getLinearVelocity(wp.lin);
						((RigidBody) physicsDataContainer.getObject()).getAngularVelocity(wp.ang);
					}
				}
//				System.err.println("[REMOTE] Received warp: " + getIdentifiable() + ": " + t.origin);
				CollisionObject object = physicsDataContainer.getObject();
				object.setWorldTransform(t);
				object.setInterpolationWorldTransform(t);
				
				if (physicsDataContainer.getObject() instanceof PairCachingGhostObjectAlignable) {
					PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) physicsDataContainer.getObject());
					if (o != null && o.localWorldTransform != null && o.getAttached() != null && o.getAttached().getWorldTransform() != null) {
						Transform f = new Transform(o.getAttached().getWorldTransform());
						f.inverse();
						Transform rel = new Transform(t);
						f.mul(rel);
						o.localWorldTransform.set(f);
					}
				}
				if (physicsDataContainer.getObject() instanceof RigidBody) {

					((RigidBody) physicsDataContainer.getObject()).setCenterOfMassTransform(t);

					MotionState myMotionState = (((RigidBody) physicsDataContainer.getObject()))
							.getMotionState();
					myMotionState.setWorldTransform(t);

//					System.err.println("RECEIVED BREADCAST VELOS: " + obj + " : " + wp.linTrans + "; " + wp.angTrans);

					((RigidBody) physicsDataContainer.getObject()).setLinearVelocity(wp.lin);
					((RigidBody) physicsDataContainer.getObject()).setAngularVelocity(wp.ang);
				}
				getWorldTransform().set(t);
				//update the cache
				physicsDataContainer.updatePhysical(getState().getUpdateTime());
				transformationSmoother.reset();
				if (!onServer) {
					if (obj == ((GameClientState) state).getShip()) {
						ShipExternalFlightController shipExternalFlightController = ((GameClientState) state).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
								.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController();

						if (shipExternalFlightController.isActive() && Controller.getCamera() instanceof InShipCamera) {

							InShipCamera cam = (InShipCamera) Controller.getCamera();
							cam.forceOrientation(t, timer);
							//							shipExternalFlightController.resetShipCamera();
							//							shipExternalFlightController.onSwitch(true);
						}
					}
					if (obj == ((GameClientState) getState()).getCurrentPlayerObject()) {
						//own object
						System.err.println("[REMOTE] FLAGGIN OWN CLIENT warp: " + obj + ": " + t.origin);
						((GameClientState) getState()).flagWarped();
					}
				}
				
			}
			if(warped && obj instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) obj).isClientOwnObject()){
				System.err.println("[CLIENT] WARP RECEIVED "+getWorldTransform().origin+" ACC SENT");
				((SimpleTransformableSendableObject) obj).getNetworkObject().receivedWarpACC.add((byte)1);
			}
			//			boolean valid;
			//			if(isOnServer()){
			//				valid = this.getIdentifiable() instanceof SimpleTransformableSendableObject;
			//			}else{
			//				valid = this.getIdentifiable() instanceof SimpleTransformableSendableObject &&
			//						((SimpleTransformableSendableObject)this.getIdentifiable()).getSectorId() !=
			//						((GameClientState)getState()).getCurrentSectorId();
			//			}

			if (onServer) {
				if (obj instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) obj).isConrolledByActivePlayer()) {
					boolean attached;
					
					if(!waitingForApprovalWarp){
						if (obj instanceof SegmentController &&
								(((SegmentController) obj).getDockingController().isDocked() || ((SegmentController) obj).railController.isDockedOrDirty())) {
							//do not do snaps for docked objects
							//they are updated by look-dir, and by mothership remote transforms
						} else {
							if (physicsDataContainer.getObject() != null && physicsDataContainer.getObject() instanceof PairCachingGhostObjectAlignable && ((PairCachingGhostObjectAlignable) physicsDataContainer.getObject()).getAttached() != null) {
								attached = true;
								Transform localWorldTransform = ((PairCachingGhostObjectAlignable) physicsDataContainer.getObject()).localWorldTransform;
														
								tmpDist.sub(receivedTransformationLocal.transform.origin, localWorldTransform.origin);
//								System.err.println("SERVER TRACING: "+localWorldTransform.origin+" TO "+receivedTransformationLocal.transform.origin+" ::: DIST "+tmpDist.length());
							} else {
								attached = false;
								tmpDist.sub(receivedTransformationLocal.transform.origin,
										physicsDataContainer.getObject() != null
												? physicsDataContainer.getObject().getWorldTransform(tmpT).origin
												: physicsDataContainer.getOriginalTransform().origin);
							}
							if (tmpDist.length() > 15) {
								if (receivedTransformationLocal.changedOnce) {
									System.err.println("[SERVER] snap plCntrldObj " + obj + " from " + getWorldTransform().origin + " (original " + physicsDataContainer.getOriginalTransform().origin + " [" + physicsDataContainer.lastCenter + "] PhysObjNotNull? " + (physicsDataContainer.getObject() != null) + ") to " + receivedTransformationLocal.transform.origin + "; ATTACHED: " + attached);
									transformationSmoother.doInstantStep(new Transform(receivedTransformationLocal.transform));
								}
							}
						}
					}
				}
			}
		} else {
		}
		
		transformationSmoother.update(timer);
		timeOfPhysicsUpdate = state.getUpdateTime();//System.currentTimeMillis();
	}

	public void updateFromRemoteInitialTransform(NetworkEntity e) {
		if (state instanceof ClientStateInterface && physicsDataContainer.isInitialized()) {
			mass = e.mass.get();
			physicsDataContainer.updateMass(mass, true);
		}
		//		System.err.println("initial transform update form remote: "+Arrays.toString(e.initialTransform.getTransientArray()));
		this.initialTransform.setFromOpenGLMatrix(e.initialTransform.getFloatArray());
	}

	public void updateFromRemoteTransform(NetworkEntity e) {
		if (state instanceof ClientStateInterface) {
			if (mass != e.mass.get() && physicsDataContainer.isInitialized()) {
				mass = e.mass.get();

				physicsDataContainer.updateMass(mass, true);
			}
		}
		if(obj instanceof PlayerControllable){
			PlayerControllable c = (PlayerControllable) obj;
			if(c.getAttachedPlayers().isEmpty()){
				if(waitingForApprovalWarp){
					System.err.println("################# SERVER NO MORE WAITING FOR PLAYER WARP (NO PLAYERS ATTACHED)");
				}
				//dont wait if there is no player attacged
				waitingForApprovalWarp = false;
			}
		}
		if(onServer && obj instanceof SimpleTransformableSendableObject){
			ByteArrayList receiveBuffer = ((SimpleTransformableSendableObject<?>) obj).getNetworkObject().receivedWarpACC.getReceiveBuffer();
			if(!receiveBuffer.isEmpty()){
				if(waitingForApprovalWarp){
					System.err.println("################# SERVER NO MORE WAITING FOR PLAYER WARP (ACC RECEIVED)");
				}
				waitingForApprovalWarp = false;
				
			}
		}
		receiveWarpTransformation(e);
		
	}

	public void updateToRemoteInitialTransform(NetworkEntity e) {
		if (state instanceof ServerStateInterface) {
			e.mass.set(mass);
			if (physicsDataContainer.isInitialized()) {
				/*
				 * use current server world transform if object is already
				 * loaded and initialized so object wont spawn somewhere else
				 * for the client (the original initial transform), when player
				 * leaves and joins a server again
				 */
				initialTransform.set(getWorldTransform());

			}
			initialTransform.getOpenGLMatrix(e.initialTransform.getFloatArray());
			e.initialTransform.setChanged(true);
			//			e.initialTransform.setArray(e.initialTransform.getFloatArray());

		} else {
			//force initial on client
			e.initialTransform.forceClientUpdates();
			initialTransform.getOpenGLMatrix(e.initialTransform.getFloatArray());
			e.initialTransform.setChanged(true);
			//			e.initialTransform.setArray(e.initialTransform.getFloatArray());
		}
	}

	public void updateToRemoteTransform(NetworkEntity e, StateInterface state) {
		if (onServer) {
			//			System.err.println("SENDING MASS FOR "+obj+": "+getMass());
			if (e.mass.getFloat() != mass && System.currentTimeMillis() - lastMassUpdate > 300) {
				e.mass.set(mass);
				lastMassUpdate = System.currentTimeMillis();
			}
		}
		if (physicsDataContainer == null || !physicsDataContainer.isInitialized()) {
			return;
		}
		if (onServer || isSendFromClient()) {
			sendTransform();
		}
	}

	public void warp(Transform worldTransform, boolean withRot) {

		if (physicsDataContainer.getObject() != null) {
			/*
			 * could be null if:
			 * recovery on docked ships one frames before the docking happens
			 */

			Transform t = physicsDataContainer.getObject().getWorldTransform(new Transform());
			if (!withRot) {
				t.origin.set(worldTransform.origin);
			} else {
				t.set(worldTransform);
			}

			CollisionObject object = physicsDataContainer.getObject();
//			System.err.println("[REMOTETRANFORM][WARP] " + getState() + " SETTING WARP POSITION from " + getWorldTransform().origin + " TO " + t.origin + " for " + this.obj);
			object.setWorldTransform(t);
			object.setInterpolationWorldTransform(t);
			if (physicsDataContainer.getObject() instanceof RigidBody) {

				((RigidBody) physicsDataContainer.getObject()).setCenterOfMassTransform(t);

				MotionState myMotionState = (((RigidBody) physicsDataContainer.getObject()))
						.getMotionState();
				myMotionState.setWorldTransform(t);
			}
			getWorldTransform().set(t);
			//update the cache
			physicsDataContainer.updatePhysical(getState().getUpdateTime());
			transformationSmoother.reset();
		}

	}

	public void onSmootherSet(Transform t) {
		if (obj instanceof SimpleTransformableSendableObject) {
			((SimpleTransformableSendableObject) obj).onSmootherSet(t);
		}
	}

	class ReceivedTransformation {
		public final Transform transform = new Transform();
		public final Vector3f linearVelocity = new Vector3f();
		public final Vector3f angularVelocity = new Vector3f();
		public long timestamp;
		public boolean receivedVelocities;
		public boolean playerAttached;
		/**
		 * changedOnce: server can snap if
		 * this has been updated at least once
		 */
		public boolean changedOnce;
		boolean changed;
	}

}
