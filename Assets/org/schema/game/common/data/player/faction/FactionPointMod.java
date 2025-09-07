package org.schema.game.common.data.player.faction;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.network.objects.remote.RemoteFactionPointUpdate;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FactionPointMod {

	public int factionId;
	private float factionPoints;
	private float lastPointsFromOnline;
	private float lastPointsFromOffline;
	private int lastinactivePlayer;
	private float lastPointsSpendOnCenterDistance;
	private float lastPointsSpendOnDistanceToHome;
	private float lastPointsSpendOnBaseRate;
	private float lastGalaxyRadius;
	private int lastCountDeaths;

	private float lastLostPointAtDeaths;

	private Set<Vector3i> differenceSystemSectorsAdd;
	private Set<Vector3i> differenceSystemSectorsRemove;

	public FactionPointMod() {
	}

	public static void send(Faction f, SendableGameState state) {
		if (!state.isOnServer()) {
			throw new RuntimeException();
		}
		FactionPointMod factionPointMod = new FactionPointMod();

		factionPointMod.get(f);

		state.getNetworkObject().factionPointMod.add(new RemoteFactionPointUpdate(factionPointMod, state.isOnServer()));
	}

	public void apply(Faction f) {
		assert (f.getIdFaction() == factionId);
		f.factionPoints = factionPoints;
		f.lastinactivePlayer = lastinactivePlayer;
		f.lastGalaxyRadius = lastGalaxyRadius;
		f.lastPointsFromOffline = lastPointsFromOffline;
		f.lastPointsFromOnline = lastPointsFromOnline;
		f.lastPointsSpendOnBaseRate = lastPointsSpendOnBaseRate;
		f.lastPointsSpendOnCenterDistance = lastPointsSpendOnCenterDistance;
		f.lastPointsSpendOnDistanceToHome = lastPointsSpendOnDistanceToHome;
		f.lastCountDeaths = lastCountDeaths;
		f.lastLostPointAtDeaths = lastLostPointAtDeaths;

		if (f.clientLastTurnSytemsCount > -1) {
			//updates after initial
			f.clientLastTurnSytemsCount = f.lastSystemSectors.size();
		}

		for (Vector3i r : differenceSystemSectorsRemove) {
			f.lastSystemSectors.remove(r);
		}
		for (Vector3i a : differenceSystemSectorsAdd) {
			if(!f.lastSystemSectors.contains(a)){
				f.lastSystemSectors.add(a);
			}
		}
		if (f.clientLastTurnSytemsCount < 0) {
			//initial update
			f.clientLastTurnSytemsCount = f.lastSystemSectors.size();
		}

	}

	private void get(Faction f) {
		this.factionId = f.getIdFaction();
		factionPoints = f.factionPoints;
		lastinactivePlayer = f.lastinactivePlayer;
		lastGalaxyRadius = f.lastGalaxyRadius;
		lastPointsFromOffline = f.lastPointsFromOffline;
		lastPointsFromOnline = f.lastPointsFromOnline;
		lastPointsSpendOnBaseRate = f.lastPointsSpendOnBaseRate;
		lastPointsSpendOnCenterDistance = f.lastPointsSpendOnCenterDistance;
		lastPointsSpendOnDistanceToHome = f.lastPointsSpendOnDistanceToHome;
		lastCountDeaths = f.lastCountDeaths;
		lastLostPointAtDeaths = f.lastLostPointAtDeaths;
		differenceSystemSectorsAdd = f.differenceSystemSectorsAdd;
		differenceSystemSectorsRemove = f.differenceSystemSectorsRemove;

//		System.err.println("DIFF: "+differenceSystemSectorsRemove);
//		System.err.println("LAST: "+f.lastSystemSectors);
//		System.err.println("DIFFADD: "+f.differenceSystemSectorsAdd);
	}

	public void deserialize(DataInput b, boolean onServer) throws IOException {
		factionId = b.readInt();
		factionPoints = b.readFloat();
		lastinactivePlayer = b.readInt();
		lastGalaxyRadius = b.readFloat();
		lastPointsFromOffline = b.readFloat();
		lastPointsFromOnline = b.readFloat();
		lastPointsSpendOnBaseRate = b.readFloat();
		lastPointsSpendOnCenterDistance = b.readFloat();
		lastPointsSpendOnDistanceToHome = b.readFloat();
		lastCountDeaths = b.readInt();
		lastLostPointAtDeaths = b.readFloat();

		int sizeAdd = b.readInt();
		differenceSystemSectorsAdd = new ObjectOpenHashSet<Vector3i>(sizeAdd);
		for (int i = 0; i < sizeAdd; i++) {
			differenceSystemSectorsAdd.add(new Vector3i(b.readInt(), b.readInt(), b.readInt()));
		}

		int sizeRemove = b.readInt();
		differenceSystemSectorsRemove = new ObjectOpenHashSet<Vector3i>(sizeRemove);
		for (int i = 0; i < sizeRemove; i++) {
			differenceSystemSectorsRemove.add(new Vector3i(b.readInt(), b.readInt(), b.readInt()));
		}
	}

	public void serialize(DataOutput b, boolean onServer) throws IOException {
		b.writeInt(factionId);
		b.writeFloat(factionPoints);
		b.writeInt(lastinactivePlayer);
		b.writeFloat(lastGalaxyRadius);
		b.writeFloat(lastPointsFromOffline);
		b.writeFloat(lastPointsFromOnline);
		b.writeFloat(lastPointsSpendOnBaseRate);
		b.writeFloat(lastPointsSpendOnCenterDistance);
		b.writeFloat(lastPointsSpendOnDistanceToHome);
		b.writeInt(lastCountDeaths);
		b.writeFloat(lastLostPointAtDeaths);

		b.writeInt(differenceSystemSectorsAdd.size());
		for (Vector3i v : differenceSystemSectorsAdd) {
			b.writeInt(v.x);
			b.writeInt(v.y);
			b.writeInt(v.z);
		}

		b.writeInt(differenceSystemSectorsRemove.size());
		for (Vector3i v : differenceSystemSectorsRemove) {
			b.writeInt(v.x);
			b.writeInt(v.y);
			b.writeInt(v.z);
		}

	}

}
