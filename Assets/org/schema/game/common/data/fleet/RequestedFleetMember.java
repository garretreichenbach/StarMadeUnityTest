package org.schema.game.common.data.fleet;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.network.StateInterface;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/04/2022]
 */
public class RequestedFleetMember extends FleetMember {

	private int id;
	private int factionId;
	private CatalogPermission permission;

	public RequestedFleetMember(StateInterface state) {
		super(state);
	}

	public RequestedFleetMember(StateInterface state, int factionId, String name, CatalogPermission permission) {
		super(state);
		this.name = name;
		this.factionId = factionId;
		this.permission = permission;
		this.id = ID ++;
	}

	@Override
	public boolean isLoaded() {
		return false;
	}

	@Override
	public Vector3i getSector(){
		return new Vector3i();
	}

	@Override
	public int getFactionId() {
		return factionId;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeBoolean(false);
		b.writeUTF(name);
		b.writeInt(factionId);
		permission.serialize(b, isOnServer);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		if(!b.readBoolean()) {
			name = b.readUTF();
			factionId = b.readInt();
			permission = new CatalogPermission();
			permission.deserialize(b, updateSenderStateId, isOnServer);
		}
	}

	public void fromMember(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		name = b.readUTF();
		factionId = b.readInt();
		permission = new CatalogPermission();
		permission.deserialize(b, updateSenderStateId, isOnServer);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof RequestedFleetMember && obj.hashCode() == id);
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public void apply(FleetMember m) {

	}

	@Override
	public SegmentController getLoaded() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void moveUnloadedTowardsGoal(Fleet f, Vector3i to){
	}

	@Override
	public void moveRequestUnloaded(Fleet fleet, Vector3i to) {

	}

	public long getDockedToRootDbId() {
		return -1;
	}

	public String getPickupPoint() {
		return "N/A (doesn't exist)";
	}

	public CatalogPermission getPermission() {
		return permission;
	}

	private static int ID = 0;
}
