package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.SendableSegmentProvider;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteControlStructureBuffer extends RemoteBuffer<RemoteControlStructure> {

	private static Constructor<RemoteControlStructure> staticConstructor;
	private SendableSegmentProvider segmentProvider;
	public RemoteControlStructureBuffer(SendableSegmentProvider controller, boolean synchOn) {
		super(RemoteControlStructure.class, synchOn);
		this.segmentProvider = controller;
	}

	public RemoteControlStructureBuffer(SendableSegmentProvider sendableSegmentProvider, NetworkObject synchOn) {
		super(RemoteControlStructure.class, synchOn);
		this.segmentProvider = sendableSegmentProvider;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();
		//		System.err.println("RECEIVED: "+collectionSize+" CONTROL MAPS FOR "+segmentController);
		for (int n = 0; n < collectionSize; n++) {
			RemoteControlStructure instance = new RemoteControlStructure(segmentProvider, onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		SendableSegmentController segmentController = segmentProvider.getSegmentController();


		/*
		 * make sure that we can serialize this segment controller
		 */
		if (segmentController != null) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;
			for (RemoteControlStructure remoteField : get()) {
				size += remoteField.toByteStream(buffer, segmentController);
			}
		} else {
			buffer.writeInt(0);
		}
		get().clear();

		return size;

	}

	@Override
	protected void cacheConstructor() {
		try {
			if (staticConstructor == null) {
				staticConstructor = RemoteControlStructure.class.getConstructor(SendableSegmentProvider.class, boolean.class);
			}
		} catch (SecurityException e) {
			System.err.println("CLASS " + clazz);
			e.printStackTrace();

			assert (false);
		} catch (NoSuchMethodException e) {
			System.err.println("CLASS " + clazz);
			e.printStackTrace();
			assert (false);
		}
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteBuffer#add(org.schema.schine.network.objects.remote.Streamable)
	 */
	@Override
	public boolean add(RemoteControlStructure e) {
		return super.add(e);
	}

}
