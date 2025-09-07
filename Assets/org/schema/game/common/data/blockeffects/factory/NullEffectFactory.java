package org.schema.game.common.data.blockeffects.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;
import org.schema.game.common.data.blockeffects.NullEffect;

public class NullEffectFactory implements BlockEffectFactory<NullEffect> {
	@Override
	public void decode(DataInputStream stream) throws IOException {
	}

	@Override
	public void encode(DataOutputStream buffer) throws IOException {
	}

	@Override
	public NullEffect getInstanceFromNT(SendableSegmentController controller) {
		return new NullEffect(controller);
	}

	@Override
	public void setFrom(NullEffect to) {
		
	}

}
