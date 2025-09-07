package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.PowerRegenDownEffect;

public class PowerRegenDownEffectFactory implements BlockEffectFactory<PowerRegenDownEffect> {
	private float force;

	@Override
	public void decode(DataInputStream stream) throws IOException {
		force = stream.readFloat();
	}

	@Override
	public void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(force);
	}

	@Override
	public PowerRegenDownEffect getInstanceFromNT(SendableSegmentController controller) {
		return new PowerRegenDownEffect(controller, force);
	}

	@Override
	public void setFrom(PowerRegenDownEffect to) {
		force = to.getForce();
	}

}
