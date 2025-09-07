package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.PushEffect;

public class PushEffectFactory implements BlockEffectFactory<PushEffect> {
	public final Vector3f relPos = new Vector3f();
	public final Vector3f from = new Vector3f();
	public float force;
	public boolean applyTorque = false;

	@Override
	public void decode(DataInputStream stream) throws IOException {
		from.x = stream.readFloat();
		from.y = stream.readFloat();
		from.z = stream.readFloat();
		relPos.x = stream.readFloat();
		relPos.y = stream.readFloat();
		relPos.z = stream.readFloat();
		force = stream.readFloat();
		applyTorque = stream.readBoolean();
	}

	@Override
	public void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(from.x);
		buffer.writeFloat(from.y);
		buffer.writeFloat(from.z);
		buffer.writeFloat(relPos.x);
		buffer.writeFloat(relPos.y);
		buffer.writeFloat(relPos.z);
		buffer.writeFloat(force);
		buffer.writeBoolean(applyTorque);
	}

	@Override
	public PushEffect getInstanceFromNT(SendableSegmentController controller) {
		return new PushEffect(controller, relPos, from, force, applyTorque);
	}

	@Override
	public void setFrom(PushEffect to) {
		from.set(to.getFrom());
		relPos.set(to.relPos);
		applyTorque = to.isApplyTorque();
		force = to.getForce();
	}

}
