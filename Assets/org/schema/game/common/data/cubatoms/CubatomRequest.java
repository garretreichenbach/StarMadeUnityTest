package org.schema.game.common.data.cubatoms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CubatomRequest {

	public static final int REFINE = 1;
	public static final int RECIPE = 2;
	public static final int SPLIT = 3;
	public int type = 0;
	public short selectedRecipe;
	public int amount;
	public byte[] componentSlots;

	public CubatomRequest() {
	}

	public CubatomRequest(int type, short selectedRecipe, int amount,
	                      byte[] componentSlots) {

		this.type = type;
		this.amount = amount;
		this.selectedRecipe = selectedRecipe;
		this.componentSlots = componentSlots;
	}

	public void deserialize(DataInputStream stream) throws IOException {
		type = stream.readByte();

		if (type == RECIPE || type == SPLIT) {
			selectedRecipe = stream.readShort();
		}
		if (type == RECIPE || type == REFINE) {
			int size = stream.readBoolean() ? 2 : 1;
			componentSlots = new byte[size];
			for (int i = 0; i < size; i++) {
				componentSlots[i] = stream.readByte();
			}
		}
		if (type == RECIPE || type == SPLIT) {
			amount = stream.readInt();
		}
	}

	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeByte((byte) type);
		if (type == RECIPE || type == SPLIT) {
			stream.writeShort(selectedRecipe);
		}
		if (type == RECIPE || type == REFINE) {
			stream.writeBoolean(componentSlots.length == 2);
			for (int i = 0; i < componentSlots.length; i++) {
				stream.writeByte(componentSlots[i]);
			}
		}
		if (type == RECIPE || type == SPLIT) {
			stream.writeInt(amount);
		}

	}

}
