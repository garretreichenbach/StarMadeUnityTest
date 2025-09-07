package org.schema.game.common.data.player;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.LightTransformable;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Light;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.network.objects.remote.RemoteTransformation;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.IOException;

public class BuildModePosition implements LightTransformable{
	private Transform buildModePositionQueue;
	private PlayerState playerState;
	private final Transform lastPosition = new Transform();
	private final Transform currentPosition = new Transform();
	private final Transform currentServerTrans = new Transform();
	private final Transform clientTrans = new Transform();
	private boolean serverReceived = true;
	private boolean toSendLastPosition;
	private float accTime;
	private float lerpSpeed = 4;
	private boolean wasDisplayed;
	private Light light;
	public BuildModePosition(PlayerState playerState) {
		this.playerState = playerState;
		lastPosition.setIdentity();
		currentServerTrans.setIdentity();
		clientTrans.setIdentity();
		currentPosition.setIdentity();
	}


	
	public void update(Timer timer) {
		if(playerState.isClientOwnPlayer()) {
			//process on client for our own player
			
			playerState.getNetworkObject().isInBuildMode.forceClientUpdates();
			
			SimpleTransformableSendableObject<?> fsw = playerState.getFirstControlledTransformableWOExc();
			
			if(fsw instanceof SegmentController && ((GameClientState)getState()).getGlobalGameControlManager()
					.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isInAnyStructureBuildMode()) {
				SegmentController s = (SegmentController)fsw;
				playerState.getNetworkObject().isInBuildMode.set(true);
				TransformTimed camTrans = Controller.getCamera().getWorldTransform();
					
					
					//remove the entity transform from the camera, so the raw transform is sent. 
					//makes it smoother on moving entities if its added by the client. also possibly less data. 
					Transform t = new Transform(s.getWorldTransformInverse());
					t.mul(camTrans);
				
					
				if(!lastPosition.basis.epsilonEquals(t.basis, 0.001f) || !lastPosition.origin.epsilonEquals(t.origin, 0.001f)) {
					//send transfotm
					lastPosition.set(t);
					toSendLastPosition = true;
				}
			}else {
				playerState.getNetworkObject().isInBuildMode.set(false);
			}
		}else {
			if(isOnServer()) {
				//deligate on server. send the transform to other clients
				if(buildModePositionQueue != null) {
					Transform last = buildModePositionQueue;
					this.currentServerTrans.set(last);
					this.serverReceived = true;
					buildModePositionQueue = null;
				}
			}else {
				if(playerState.isClientOwnPlayer()) {
					//discard received since we are on our own client's player (program shouldnt be here anyways)
					buildModePositionQueue = null;
					
				}else {
					
					
					//process and update position of this player for other clients
					if(buildModePositionQueue != null) {
						Transform last = buildModePositionQueue;
						buildModePositionQueue = null;
						currentPosition.set(clientTrans);
						lastPosition.set(last);
						accTime = 0;
					}
					accTime += timer.getDelta()*lerpSpeed;
					
					if(!isDisplayed()) {
						wasDisplayed = false;
					}
					
					if(!wasDisplayed) {
						
						accTime = 1f;
						wasDisplayed = true;
					}
					if(accTime < 1f) {
							Vector3f resL = new Vector3f();
							Vector3fTools.lerp(currentPosition.origin, lastPosition.origin, accTime, resL);
							currentPosition.origin.set(resL);
						
							Quat4f resQ = new Quat4f();
							Quat4f a = Quat4fTools.set(currentPosition.basis, new Quat4f());
							Quat4f b = Quat4fTools.set(lastPosition.basis, new Quat4f());
							Quat4Util.slerp(a, b, accTime, resQ);
							currentPosition.basis.set(resQ);
					}else {
						currentPosition.set(lastPosition);
					}
					
					clientTrans.set(currentPosition);
					
					
				}
			}
		}
		if(playerState.isClientOwnPlayer() && EngineSettings.CAMERA_DRONE_FLASHLIGHT_ON.isOn() != playerState.getNetworkObject().isBuildModeSpotlight.getBoolean()) {
			playerState.getNetworkObject().isBuildModeSpotlight.set(EngineSettings.CAMERA_DRONE_FLASHLIGHT_ON.isOn(), true);
		}
	}
	
	
	public TransformTimed getWorldTransform() {
		
		if(isOnServer()) {
			SimpleTransformableSendableObject<?> sc = playerState.getFirstControlledTransformableWOExc();
			if(sc instanceof SegmentController) {
				SegmentController s = (SegmentController)sc;
				TransformTimed t;
				
				t = new TransformTimed(s.getWorldTransform());
				t.mul(currentServerTrans);
				return t;
			}else {
				System.err.println("[SERVER][ERROR][BUILDPOS] Error. not in entity: "+playerState);
				TransformTimed t = new TransformTimed();
				t.setIdentity();
				return t;
			}
			
			
		}else {
			if(playerState.isClientOwnPlayer()) {
				return Controller.getCamera().getWorldTransform();
			}else {
				SimpleTransformableSendableObject<?> sc = playerState.getFirstControlledTransformableWOExc();
				if(sc instanceof SegmentController) {
				
					SegmentController s = (SegmentController)sc;
					TransformTimed t;
					
						t = new TransformTimed(s.getWorldTransformOnClient());
					t.mul(clientTrans);
					return t;
				}else {
					System.err.println("[CLIENT][ERROR][BUILDPOS] Error. not in entity: "+playerState);
					TransformTimed t = new TransformTimed();
					t.setIdentity();
					return t;
				}
			}
		}
	}
	/**
	 * Only display when in build mode and its next to the client
	 * @return if the drone should be displayed
	 */
	public boolean isDisplayed() {
		GameClientState s = ((GameClientState)getState());
		
		return (!playerState.isClientOwnPlayer() || isOwnVisible()) && 
			
				playerState.getNetworkObject().isInBuildMode.getBoolean() && s.getController().isNeighborToClientSector(playerState.getSectorId());
	}
	
	public void updateToNetworkObject() {
		if(isOnServer() && this.serverReceived) {
			//send initially always or when server got an update
			playerState.getNetworkObject().buildModePositionBuffer.add(new RemoteTransformation(currentServerTrans, playerState.getNetworkObject()));
			this.serverReceived = false;
		}else if(toSendLastPosition) {
			//send from client from our own player
			playerState.getNetworkObject().buildModePositionBuffer.add(new RemoteTransformation(lastPosition, playerState.getNetworkObject()));
			toSendLastPosition = false;
		}
	}


	public boolean isOnServer() {
		return playerState.isOnServer();
	}


	public StateInterface getState() {
		return playerState.getState();
	}


	public void updateToFullNetworkObject() {
		updateToNetworkObject();
	}


	public void initFromNetworkObject() {
		updateFromNetworkObject();
	}


	
	public void updateFromNetworkObject() {
		for (int i = 0; i < playerState.getNetworkObject().buildModePositionBuffer.getReceiveBuffer().size(); i++) {
			RemoteTransformation t = playerState.getNetworkObject().buildModePositionBuffer.getReceiveBuffer().get(i);
			buildModePositionQueue = t.get();
		}		
	}



	@Override
	public TransformTimed getWorldTransformOnClient() {
		return getWorldTransform();
	}





	@Override
	public Light getLight() {
		return light;
	}



	@Override
	public void setLight(Light light) {
		this.light = light;		
	}



	@Override
	public AbstractOwnerState getOwnerState() {
		return playerState;
	}



	public boolean isSpotLightOn() {
		if(playerState.isClientOwnPlayer()) {
			return (((GameClientState)getState()).getGlobalGameControlManager()
					.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isInAnyStructureBuildMode()) && playerState.getNetworkObject().isBuildModeSpotlight.getBoolean();
		}
		return isDisplayed() && playerState.getNetworkObject().isBuildModeSpotlight.getBoolean();
	}

	public void setOwnVisible(boolean b) {
		EngineSettings.CAMERA_DRONE_OWN_VISIBLE.setOn(b);
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isOwnVisible() {
		return EngineSettings.CAMERA_DRONE_OWN_VISIBLE.isOn();
	}

	public void setFlashlightOnClient(boolean b) {
		EngineSettings.CAMERA_DRONE_FLASHLIGHT_ON.setOn(b);
		playerState.getNetworkObject().isBuildModeSpotlight.set(b, true);
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public boolean isFlashlightOn() {
		return playerState.getNetworkObject().isBuildModeSpotlight.getBoolean();
	}



	public boolean isClientOwnPlayer() {
		return playerState.isClientOwnPlayer();
	}



	public boolean isDrawNames() {
		return ((GameClientState)getState()).getGlobalGameControlManager()
				.getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().isCameraDroneDisplayName();
	}
}
