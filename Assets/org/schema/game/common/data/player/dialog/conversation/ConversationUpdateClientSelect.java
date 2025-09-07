package org.schema.game.common.data.player.dialog.conversation;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.data.player.AbstractOwnerState;

public class ConversationUpdateClientSelect extends ConverationUpdate {
	private int selection;

	public ConversationUpdateClientSelect() {
	}

	public ConversationUpdateClientSelect(int id) {
		super(id);
	}

	public ConversationUpdateClientSelect(
			AbstractOwnerState converationPartner, int selection) {
		this(converationPartner.getId());
		this.selection = selection;
	}

	@Override
	protected void deserialize(DataInputStream buffer) throws IOException {
		super.deserialize(buffer);
		selection = buffer.readInt();
	}

	@Override
	public void serialize(DataOutput buffer) throws IOException {
		super.serialize(buffer);
		buffer.writeInt(selection);
	}

	@Override
	public byte getType() {
		return CLIENT_SELECT;
	}

	/**
	 * @return the selection
	 */
	public int getSelection() {
		return selection;
	}

	/**
	 * @param selection the selection to set
	 */
	public void setSelection(int selection) {
		this.selection = selection;
	}
}
