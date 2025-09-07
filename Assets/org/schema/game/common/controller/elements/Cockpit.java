package org.schema.game.common.controller.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.camera.Camera;

import com.bulletphysics.linearmath.Transform;

public class Cockpit implements SerializationInterface{
	public long block = ElementCollection.getIndex(Ship.core);
	public final Transform worldTransform = new Transform(TransformTools.ident);
	private final PlayerState player;
	
	public Cockpit(PlayerState player) {
		this.player = player;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(block);
		TransformTools.serializeFully(b, worldTransform);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		block = b.readLong();
		TransformTools.deserializeFully(b, worldTransform);
	}
	
	public void reset() {
		block = ElementCollection.getIndex(Ship.core);
		worldTransform.setIdentity();
	}
	public boolean equalsBlockPos(Vector3i v) {
		return ElementCollection.getIndex(v) == block;
	}
	
	public void changeBlockClient(Vector3i v, Transform t, Camera cam) {
		block = ElementCollection.getIndex(v);
		worldTransform.set(t);
		player.getNetworkObject().cockpit.setChanged(true);		
		if(cam instanceof InShipCamera) {
			InShipCamera shipCamera = (InShipCamera)cam;
			shipCamera.setAdjustMatrix(t);
		}
	}
	public Vector3i getBlock(Vector3i v) {
		return ElementCollection.getPosFromIndex(block, v);
	}
	public void addCockpoitOffset(Vector3f camPos) {
		worldTransform.transform(camPos);
	}
	public boolean isCore() {
		return block == ElementCollection.getIndex(Ship.core);
	}
	public boolean isInCockpitAdjustment() {
		ShipExternalFlightController c = ((GameClientState)player.getState()).getGlobalGameControlManager().getIngameControlManager()
		.getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController();
		return c.isTreeActive() && !c.isTreeActiveInFlight();
	}
}
