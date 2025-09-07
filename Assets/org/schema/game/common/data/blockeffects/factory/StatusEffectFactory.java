package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.StatusBlockEffect;

public abstract class StatusEffectFactory<E extends StatusBlockEffect> implements BlockEffectFactory<E> {
	protected int blockCount;
	protected long idServer;
	protected float effectCap;
	protected float powerConsumption;
	protected float multiplier;

	@Override
	public void decode(DataInputStream stream) throws IOException {
		blockCount = stream.readInt();
		effectCap = stream.readFloat();
		powerConsumption = stream.readFloat();
		multiplier = stream.readFloat();
	}

	@Override
	public void encode(DataOutputStream buffer) throws IOException {
		buffer.writeInt(blockCount);
		buffer.writeFloat(effectCap);
		buffer.writeFloat(powerConsumption);
		buffer.writeFloat(multiplier);
	}

	@Override
	public void setFrom(StatusBlockEffect to) {
		effectCap = to.getEffectCap();
		blockCount = to.getBlockCount();
		idServer = to.getPos();
		powerConsumption = to.getPowerConsumption();
		multiplier = to.getBaseMultiplier();
	}
}
