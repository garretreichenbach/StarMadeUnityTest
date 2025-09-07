package org.schema.game.common.data.blockeffects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;

public interface BlockEffectFactory<E extends BlockEffect> {
	public void decode(DataInputStream stream) throws IOException;

	public void encode(DataOutputStream buffer) throws IOException;

	public E getInstanceFromNT(SendableSegmentController controller);

	public void setFrom(E to);
}
