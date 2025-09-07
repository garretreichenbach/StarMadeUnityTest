package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteItem extends RemoteField<FreeItem> {

	private boolean add;

	public RemoteItem(FreeItem entry, Boolean add, boolean synchOn) {
		super(entry, synchOn);
		this.add = add;
	}

	public RemoteItem(FreeItem entry, Boolean add, NetworkObject synchOn) {
		super(entry, synchOn);
		this.add = add;
	}

	@Override
	public int byteLength() {
		return 1; //segPos
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		int id = stream.readInt();
		add = stream.readBoolean();

		if (add) {
			short type = 0;
			int count = 0;
			float x = 0, y = 0, z = 0;
			type = stream.readShort();
			count = stream.readInt();
			x = stream.readFloat();
			y = stream.readFloat();
			z = stream.readFloat();
			int meta = stream.readInt();
			get().set(id, type, count, meta, new Vector3f(x, y, z));
		} else {
			get().set(id, (short) -1, 0, -1, null);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		int size = 0;
		buffer.writeInt(get().getId());
		buffer.writeBoolean(add);
		size = 5;
		if (add) {
			buffer.writeShort(get().getType());
			buffer.writeInt(get().getCount());
			buffer.writeFloat(get().getPos().x);
			buffer.writeFloat(get().getPos().y);
			buffer.writeFloat(get().getPos().z);
			buffer.writeInt(get().getMetaId());
			size += 22;
		}

		return size;
	}

	/**
	 * @return the add
	 */
	public boolean isAdd() {
		return add;
	}

	/**
	 * @param add the add to set
	 */
	public void setAdd(boolean add) {
		this.add = add;
	}

}
