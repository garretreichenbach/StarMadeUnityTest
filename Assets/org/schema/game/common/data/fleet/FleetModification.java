package org.schema.game.common.data.fleet;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.RegisteredClientOnServer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FleetModification implements SerializationInterface {

	public long fleetId = -1;
	public FleetModType type;
	public String command;
	public String name;
	public String owner;
	public int entityId;
	public long entityDBId;
	public String entityUID;
	public byte orderMove;
	public Vector3i sector;
	public String missionString;
	public byte permission;
	public Vector3i[] target;
	public String combatSetting;
	public boolean combinedTargeting;
	private int senderId;

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(type.ordinal());
		b.writeLong(fleetId);

		switch(type) {
			case TARGET_SEC_UDPATE:
				b.writeBoolean(target != null);
				if(target != null) {
					b.writeInt(target.length);
					for(Vector3i v : target) v.serialize(b);
				}
				break;
			case CREATE:
				b.writeUTF(name);
				b.writeUTF(owner);
				break;
			case RENAME:
				assert (name != null);
				assert (fleetId > 0);
				b.writeUTF(name);
				break;
			case ADD_MEMBER:
				assert (entityDBId != 0);
				assert (fleetId > 0);
				b.writeLong(entityDBId);
				break;
			case REMOVE_MEMBER:
				assert (fleetId > 0);
				b.writeLong(entityDBId);
				break;
			case MOVE_MEMBER:
				assert (fleetId > 0);
				b.writeInt(entityId);
				b.writeUTF(entityUID);
				b.writeByte(orderMove);
				break;
			case COMMAND:
				assert (fleetId > 0);
				b.writeUTF(command);
				break;
			case DELETE_FLEET:
				assert (fleetId > 0);
				break;
			case SECTOR_CHANGE:
				assert (entityDBId > 0);
				b.writeLong(entityDBId);
				b.writeInt(sector.x);
				b.writeInt(sector.y);
				b.writeInt(sector.z);
				break;
			case MISSION_UPDATE:
				assert (fleetId > 0);
				b.writeUTF(missionString);
				break;
			case SET_PERMISSION:
				assert (fleetId > 0);
				b.writeByte(permission);
				break;
			case CHANGE_SETTING:
				assert (fleetId > 0);
				b.writeUTF(combatSetting);
				break;
			case CHANGE_TARGETING:
				assert (fleetId > 0);
				b.writeBoolean(combinedTargeting);
			default:
				break;
		}
	}

	public PlayerState getSender(GameServerState serverState) {
		RegisteredClientOnServer cl = serverState.getClients().get(senderId);
		if(cl != null) {
			return (PlayerState) cl.getPlayerObject();
		}
		return null;
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		type = FleetModType.values()[b.readByte()];
		fleetId = b.readLong();
		senderId = updateSenderStateId;
		switch(type) {
			case TARGET_SEC_UDPATE:
				if(b.readBoolean()) {
					try {
						int i = b.readInt();
						target = new Vector3i[i];
						for(int j = 0; j < i; j++) {
							target[j] = new Vector3i();
							target[j].deserialize(b);
						}
					} catch(Exception e) {
						target = new Vector3i[1];
						target[0] = new Vector3i();
						target[0].deserialize(b);
					}
				}
				break;
			case CREATE:
				name = b.readUTF();
				owner = b.readUTF();
				break;
			case RENAME:
				name = b.readUTF();
				break;
			case ADD_MEMBER:
				entityDBId = b.readLong();
				break;
			case REMOVE_MEMBER:
				entityDBId = b.readLong();
				break;
			case MOVE_MEMBER:
				entityId = b.readInt();
				entityUID = b.readUTF();
				orderMove = b.readByte();
				break;
			case COMMAND:
				command = b.readUTF();
			case SECTOR_CHANGE:
				entityDBId = b.readLong();
				sector = new Vector3i(b.readInt(), b.readInt(), b.readInt());
				break;
			case MISSION_UPDATE:
				missionString = b.readUTF();
				break;
			case DELETE_FLEET:
				break;
			case SET_PERMISSION:
				permission = b.readByte();
				break;
			case CHANGE_SETTING:
				combatSetting = b.readUTF();
				break;
			case CHANGE_TARGETING:
				combinedTargeting = b.readBoolean();
				break;
			default:
				break;
		}
	}

	@Override
	public String toString() {
		return "FleetModification [fleetId=" + fleetId + ", type=" + type + ", command=" + command + ", name=" + name + ", owner=" + owner + ", entityId=" + entityId + ", factionPermission=" + permission + ", combatSetting=" + combatSetting + ", combinedTargeting=" + combinedTargeting + "]";
	}

	public enum FleetModType {
		CREATE,
		ADD_MEMBER,
		REMOVE_MEMBER,
		COMMAND,
		DELETE_FLEET,
		RENAME,
		MOVE_MEMBER,
		SECTOR_CHANGE,
		MISSION_UPDATE,
		UNCACHE,
		TARGET_SEC_UDPATE,
		SET_PERMISSION,
		CHANGE_SETTING,
		CHANGE_TARGETING
	}
}
