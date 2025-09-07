package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.TakeOffEffect;

public class TakeOffEffectFactory implements BlockEffectFactory<TakeOffEffect> {
	private float force;
	private float x;
	private float y;
	private float z;

	@Override
	public void decode(DataInputStream stream) throws IOException {
		force = stream.readFloat();
		x = stream.readFloat();
		y = stream.readFloat();
		z = stream.readFloat();
	}

	@Override
	public void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(force);
		buffer.writeFloat(x);
		buffer.writeFloat(y);
		buffer.writeFloat(z);
	}

	@Override
	public TakeOffEffect getInstanceFromNT(SendableSegmentController controller) {
		return new TakeOffEffect(controller, force, x,y,z);
	}

	@Override
	public void setFrom(TakeOffEffect to) {
		force = to.getForce();
		x = to.getDirection().x;
		y = to.getDirection().y;
		z = to.getDirection().z;
	}

}
