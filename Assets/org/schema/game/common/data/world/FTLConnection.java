package org.schema.game.common.data.world;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.network.objects.NetworkClientChannel;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FTLConnection {
	public static final int TYPE_WARP_GATE = 0;
	public static final int TYPE_WORM_HOLE = 1;
	public static final int TYPE_RACE_WAY = 2;
	public Vector3i from;
	public ObjectArrayList<Vector3i> to;
	public ObjectArrayList<Vector3i> toLoc;
	public ObjectArrayList<Vector3i> param;
	public NetworkClientChannel channel;
	public String toUID;

	public void serialize(DataOutput b) throws IOException {
		b.writeInt(from.x);
		b.writeInt(from.y);
		b.writeInt(from.z);

		if (to == null) {
			b.writeInt(0);
		} else {
			b.writeInt(to.size());

			for (int i = 0; i < to.size(); i++) {
				b.writeInt(to.get(i).x);
				b.writeInt(to.get(i).y);
				b.writeInt(to.get(i).z);

				b.writeInt(param.get(i).x);
				b.writeInt(param.get(i).y);
				b.writeInt(param.get(i).z);
				
				b.writeShort(toLoc.get(i).x);
				b.writeShort(toLoc.get(i).y);
				b.writeShort(toLoc.get(i).z);
			}
		}
	}

	public void deserialize(DataInput b, int senderId) throws IOException {
		from = new Vector3i(b.readInt(), b.readInt(), b.readInt());

		int size = b.readInt();

		if (size > 0) {
			to = new ObjectArrayList<Vector3i>(size);
			param = new ObjectArrayList<Vector3i>(size);
			toLoc = new ObjectArrayList<Vector3i>(size);
			for (int i = 0; i < size; i++) {
				to.add(new Vector3i(b.readInt(), b.readInt(), b.readInt()));
				param.add(new Vector3i(b.readInt(), b.readInt(), b.readInt()));
				toLoc.add(new Vector3i(b.readShort(), b.readShort(), b.readShort()));
			}
		} else {
			//empty update (no FTL in this sector)
		}
	}
}
