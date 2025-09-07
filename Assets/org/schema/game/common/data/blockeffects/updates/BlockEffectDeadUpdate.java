package org.schema.game.common.data.blockeffects.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffect;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class BlockEffectDeadUpdate extends BlockEffectUpdate {

	public BlockEffectDeadUpdate(byte type, short id, long blockId) {
		super(type, id, blockId);
		assert (type == DEAD);
	}

	public BlockEffectDeadUpdate(short id, long blockId) {
		this(DEAD, id, blockId);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
	}

	@Override
	public void encodeEffect(DataOutputStream buffer) throws IOException {
		super.encodeEffect(buffer);
		assert (type == DEAD);
	}

	@Override
	public void handleClientUpdate(
			Short2ObjectOpenHashMap<BlockEffect> effects, SendableSegmentController controller) {
		BlockEffect remove = effects.remove(id);
		if (remove != null) {
		} else {
			System.err.println("EFFECT CANNOT BE FOUND: " + id);
		}
	}

}
