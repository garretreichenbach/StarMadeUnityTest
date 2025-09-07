package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.namegen.NameGenerator;
import org.schema.common.util.linAlg.Vector3i;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GalaxyZoneRequestAndAwnser {

	public List<GalaxyRequestAndAwnser> buffer;

	public Vector3i startSystem;

	public int zoneSize;

	public NetworkClientChannel networkObjectOnServer;

	public GalaxyZoneRequestAndAwnser(GalaxyZoneRequestAndAwnser a) {
		zoneSize = a.zoneSize;
		buffer = new ObjectArrayList<GalaxyRequestAndAwnser>(a.buffer);
		startSystem = a.startSystem;
	}

	public GalaxyZoneRequestAndAwnser() {
	}

	public static void main(String[] a) {
		try {
			NameGenerator n = new NameGenerator("./data/config/systemNames.syl", (new Random()).nextLong());
			int letters = 0;
			for (int i = 0; i < (128 * 128 * 128); i++) {
				String compose = n.compose((int) (Math.random() * 6 + 1));
//				System.err.println("::: "+compose);
				letters += compose.length();
			}
			System.err.println("TOTAL DATA: " + ((4 + letters * 4) / 1024 / 1024) + " MB");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deserialize(DataInput b, boolean onServer) throws IOException {
		if (onServer) {
			startSystem = new Vector3i(b.readInt(), b.readInt(), b.readInt());
			zoneSize = b.readInt();
		} else {
			startSystem = new Vector3i(b.readInt(), b.readInt(), b.readInt());
			int size = b.readInt();
			buffer = new ObjectArrayList<GalaxyRequestAndAwnser>(size);
			for (int i = 0; i < size; i++) {
				GalaxyRequestAndAwnser r = new GalaxyRequestAndAwnser();
				r.deserialize(b, onServer);
				buffer.add(r);
			}
		}
	}

	public void serialize(DataOutput b, boolean onServer) throws IOException {
		if (onServer) {
			b.writeInt(startSystem.x);
			b.writeInt(startSystem.y);
			b.writeInt(startSystem.z);
			assert (buffer != null);
			int size = buffer.size();
			b.writeInt(size);
			for (int i = 0; i < size; i++) {
				buffer.get(i).serialize(b, onServer);
			}
		} else {
			b.writeInt(startSystem.x);
			b.writeInt(startSystem.y);
			b.writeInt(startSystem.z);
			b.writeInt(zoneSize);

		}
	}

}
