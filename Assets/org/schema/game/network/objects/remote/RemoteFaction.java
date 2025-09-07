package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoteFaction extends RemoteField<Faction> {

	public RemoteFaction(Faction entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteFaction(Faction entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		String name = stream.readUTF();
		String description = stream.readUTF();
		int id = stream.readInt();
		boolean allyNeutral = stream.readBoolean();
		boolean attackNeutral = stream.readBoolean();
		boolean openToJoin = stream.readBoolean();

		int factionMode = stream.readInt();

		Vector3f color = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());

		boolean autoDeclareWar = stream.readBoolean();
		String homebase = stream.readUTF();
		int x = stream.readInt();
		int y = stream.readInt();
		int z = stream.readInt();
		String homebaseRealName = stream.readUTF();

		get().factionPoints = stream.readFloat();
		get().lastinactivePlayer = stream.readInt();
		get().lastGalaxyRadius = stream.readFloat();
		get().lastPointsFromOffline = stream.readFloat();
		get().lastPointsFromOnline = stream.readFloat();
		get().lastPointsSpendOnBaseRate = stream.readFloat();
		get().lastPointsSpendOnCenterDistance = stream.readFloat();
		get().lastPointsSpendOnDistanceToHome = stream.readFloat();
		get().lastCountDeaths = stream.readInt();
		get().lastLostPointAtDeaths = stream.readFloat();

		int sizeAdd = stream.readInt();
		get().lastSystemSectors = new ObjectArrayList(sizeAdd);
		for (int i = 0; i < sizeAdd; i++) {
			get().lastSystemSectors.add(new Vector3i(stream.readInt(), stream.readInt(), stream.readInt()));
		}
		if (updateSenderStateId == 0) {
			get().clientLastTurnSytemsCount = get().lastSystemSectors.size();
		}
		get().getRoles().factionId = stream.readInt();
		get().getRoles().senderId = updateSenderStateId;
		for (int i = 0; i < FactionRoles.ROLE_COUNT; i++) {
			get().getRoles().getRoles()[i].role = stream.readLong();
			get().getRoles().getRoles()[i].name = stream.readUTF();
		}
		get().deserializeMembers(stream);
		get().deserializePersonalEnemies(stream);

		get().setShowInHub(stream.readBoolean());

		get().getHomeSector().set(x, y, z);
		get().setHomebaseRealName(homebaseRealName);
		get().setHomebaseUID(homebase);
		get().setName(name);
		get().setDescription(description);
		get().setIdFaction(id);
		get().setFactionMode(factionMode);
		get().setAttackNeutral(attackNeutral);
		get().setAllyNeutral(allyNeutral);
		get().setOpenToJoin(openToJoin);
		get().setAutoDeclareWar(autoDeclareWar);
		get().getColor().set(color);
		
		get().deserializeExtra(stream);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeUTF(get().getName());
		buffer.writeUTF(get().getDescription());
		buffer.writeInt(get().getIdFaction());

		buffer.writeBoolean(get().isAllyNeutral());
		buffer.writeBoolean(get().isAttackNeutral());
		//		System.err.println("SENDING OPEN TO JOIN "+get().isOpenToJoin()+" "+get());
		buffer.writeBoolean(get().isOpenToJoin());
		buffer.writeInt(get().getFactionMode());

		buffer.writeFloat(get().getColor().x);
		buffer.writeFloat(get().getColor().y);
		buffer.writeFloat(get().getColor().z);

		buffer.writeBoolean(get().isAutoDeclareWar());
		buffer.writeUTF(get().getHomebaseUID());
		buffer.writeInt(get().getHomeSector().x);
		buffer.writeInt(get().getHomeSector().y);
		buffer.writeInt(get().getHomeSector().z);
		buffer.writeUTF(get().getHomebaseRealName());

		buffer.writeFloat(get().factionPoints);
		buffer.writeInt(get().lastinactivePlayer);
		buffer.writeFloat(get().lastGalaxyRadius);
		buffer.writeFloat(get().lastPointsFromOffline);
		buffer.writeFloat(get().lastPointsFromOnline);
		buffer.writeFloat(get().lastPointsSpendOnBaseRate);
		buffer.writeFloat(get().lastPointsSpendOnCenterDistance);
		buffer.writeFloat(get().lastPointsSpendOnDistanceToHome);
		buffer.writeInt(get().lastCountDeaths);
		buffer.writeFloat(get().lastLostPointAtDeaths);

		buffer.writeInt(get().lastSystemSectors.size());
		for (Vector3i v : get().lastSystemSectors) {
			buffer.writeInt(v.x);
			buffer.writeInt(v.y);
			buffer.writeInt(v.z);
		}

		buffer.writeInt(get().getRoles().factionId);

		for (int i = 0; i < FactionRoles.ROLE_COUNT; i++) {
			buffer.writeLong(get().getRoles().getRoles()[i].role);
			buffer.writeUTF(get().getRoles().getRoles()[i].name);
		}
		get().serializeMembers(buffer);

		get().serializePersonalEmenies(buffer);

		buffer.writeBoolean(get().isShowInHub());

		get().serializeExtra(buffer);
		
		return byteLength();
	}

}
