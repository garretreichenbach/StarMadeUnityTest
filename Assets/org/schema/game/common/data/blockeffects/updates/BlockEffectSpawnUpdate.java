package org.schema.game.common.data.blockeffects.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffect;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class BlockEffectSpawnUpdate<E extends BlockEffect> extends BlockEffectUpdate {
	public byte effectType;
	public E sendBlockEffect;
	private BlockEffectFactory<E> facReceived;

	public BlockEffectSpawnUpdate(byte type, short id, long blockId) {
		super(type, id, blockId);
//		System.err.println("[EFFECT] DECODING SPAWN EFFECT: eid: "+this.id+"; bid "+this.blockIdentifyer);
		assert (type == SPAWN);
	}

	public BlockEffectSpawnUpdate(short id, long blockId) {
		super(SPAWN, id, blockId);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		effectType = stream.readByte();

		facReceived = (BlockEffectFactory<E>) BlockEffectTypes.values()[effectType].effectFactory.getInstance();
		facReceived.decode(stream);

		//		fac.setFrom(sendBlockEffect);

	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeByte(effectType);

		BlockEffectFactory<E> fac = (BlockEffectFactory<E>) BlockEffectTypes.values()[effectType].effectFactory.getInstance();
		fac.setFrom(sendBlockEffect);
		fac.encode(buffer);
	}

	@Override
	public void handleClientUpdate(
			Short2ObjectOpenHashMap<BlockEffect> effects, SendableSegmentController channel) {
		if (!effects.containsKey(id)) {
			BlockEffect m = getClientEffect(channel);
//			System.err.println("[CLIENT] SPAWNING NEW EFFECT "+m+"; eid: "+m.getId()+"; bid: "+m.getBlockAndTypeId4());
			effects.put(m.getId(), m);
		} else {
			System.err.println("[CLIENT] not adding effect (already exists) ID " + id + " -> " + effects.get(id));
		}
	}

	private BlockEffect getClientEffect(SendableSegmentController channel) {

		E m = facReceived.getInstanceFromNT(channel);
		m.setId(this.id);
		m.setBlockId(blockIdentifyer);
		return m;
	}
}
