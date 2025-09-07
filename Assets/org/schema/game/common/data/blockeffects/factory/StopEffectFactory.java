package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.StopEffect;

public class StopEffectFactory implements BlockEffectFactory<StopEffect> {
	public float stoppingForce;

	@Override
	public void decode(DataInputStream stream) throws IOException {
		stoppingForce = stream.readFloat();
	}

	@Override
	public void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(stoppingForce);
	}

	@Override
	public StopEffect getInstanceFromNT(SendableSegmentController controller) {
		return new StopEffect(controller, stoppingForce);
	}

	@Override
	public void setFrom(StopEffect to) {
		stoppingForce = to.getStoppingForce();
	}

}
