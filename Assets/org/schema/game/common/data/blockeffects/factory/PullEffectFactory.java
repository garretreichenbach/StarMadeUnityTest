package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.PullEffect;

public class PullEffectFactory implements BlockEffectFactory<PullEffect> {
	public final Vector3f from = new Vector3f();
	public float force;
	public boolean applyTorque = false;
	private float time;

	@Override
	public void decode(DataInputStream stream) throws IOException {
		from.x = stream.readFloat();
		from.y = stream.readFloat();
		from.z = stream.readFloat();
		force = stream.readFloat();
		time = stream.readFloat();
		applyTorque = stream.readBoolean();
	}

	@Override
	public void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(from.x);
		buffer.writeFloat(from.y);
		buffer.writeFloat(from.z);
		buffer.writeFloat(force);
		buffer.writeFloat(time);
		buffer.writeBoolean(applyTorque);
	}

	@Override
	public PullEffect getInstanceFromNT(SendableSegmentController controller) {
		return new PullEffect(controller, from, force, applyTorque, time);
	}

	@Override
	public void setFrom(PullEffect to) {
		from.set(to.getFrom());
		applyTorque = to.isApplyTorque();
		force = to.getForce();
		time = to.getTime();
	}

}
