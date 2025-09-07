package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.SendableSegmentProvider;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteControlStructure extends RemoteField<Boolean> {

	private SendableSegmentProvider segmentController;

	public RemoteControlStructure(SendableSegmentProvider segmentController, boolean synchOn) {
		super(true, synchOn);
		this.segmentController = segmentController;
	}

	public RemoteControlStructure(SendableSegmentProvider segmentController, NetworkObject synchOn) {
		super(true, synchOn);
		this.segmentController = segmentController;
	}

	@Override
	public int byteLength() {
		return 0;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		segmentController.getSegmentController().getControlElementMap().deserializeZipped(stream);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		segmentController.getSegmentController().getControlElementMap().serializeZippedForNT(buffer);
		return 1;
	}

	@Override
	public boolean initialSynchUpdateOnly() {
		return true;
	}

	public int toByteStream(DataOutputStream buffer,
	                        SendableSegmentController segmentController) throws IOException {
		segmentController.getControlElementMap().serializeZippedForNT(buffer);
		return 1;
	}

}
