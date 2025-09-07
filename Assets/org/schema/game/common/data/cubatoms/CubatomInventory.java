package org.schema.game.common.data.cubatoms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class CubatomInventory implements TagSerializable {

	private final byte[] inventory = new byte[256];
	private final byte[] lastInventory = new byte[256];
	private boolean clientChanged;

	public CubatomInventory(PlayerState owner) {
		Arrays.fill(inventory, Byte.MIN_VALUE);
		Arrays.fill(lastInventory, Byte.MIN_VALUE);
	}

	public boolean changed(int id) {
		assert (id >= 0 && id < 256) : id;
		boolean changed = inventory[id] != lastInventory[id];
		lastInventory[id] = inventory[id];
		return changed;
	}

	public void deserialize(DataInputStream stream) throws IOException {

		stream.readFully(inventory);
		clientChanged = true;

	}

	@Override
	public void fromTagStructure(Tag tag) {
		byte[] inventory = (byte[]) tag.getValue();
		for (int i = 0; i < inventory.length; i++) {
			this.inventory[i] = inventory[i];
		}

	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.BYTE_ARRAY, "ci0", inventory);
	}

	public int getCount(int id) {
		assert (id >= 0 && id < 256) : id;
		return inventory[id] + 128;
	}


	public void inc(int id, int count) {
		assert (id >= 0 && id < 256);
		if (count == 0) {
			return;
		}
		if ((inventory[id] + 128) + count < 0) {
			//			System.err.println("[CUBATOMINV] error min amount: "+inventory[id]+": "+count);
			set(id, 0);
		} else if ((inventory[id] + 128) + count > 255) {
			//			System.err.println("[CUBATOMINV] error max amount: id "+inventory[id]+": count "+count);
			set(id, 255);
		} else {
			inventory[id] += (byte) (count);
			send();
		}
	}

	/**
	 * @return the clientChanged
	 */
	public boolean isClientChanged() {
		return clientChanged;
	}

	/**
	 * @param clientChanged the clientChanged to set
	 */
	public void setClientChanged(boolean clientChanged) {
		this.clientChanged = clientChanged;
	}

	private void send() {
//		if (owner.isOnServer()) {
//			owner.getNetworkObject().cubatomInventory.setChanged(true);
//			owner.getNetworkObject().setChanged(true);
//		} else {
//			setClientChanged(true);
//		}
	}

	public void serialize(DataOutputStream stream) throws IOException {
		stream.write(inventory);
	}

	public void set(int id, int count) {
		assert (id >= 0 && id < 256) : id;
		boolean changed = inventory[id] != (byte) (count - 128);
		inventory[id] = (byte) (count - 128);
		send();
	}

}
