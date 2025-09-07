package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.ThrusterOutageEffect;

public class ThrusterOutageEffectFactory implements BlockEffectFactory<ThrusterOutageEffect> {
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
	public ThrusterOutageEffect getInstanceFromNT(SendableSegmentController controller) {
		return new ThrusterOutageEffect(controller, force);
	}

	@Override
	public void setFrom(ThrusterOutageEffect to) {
		force = to.getForce();
	}

}
