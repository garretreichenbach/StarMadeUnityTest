package org.schema.game.network.objects.remote;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class RemoteSegmentPieceBuffer extends RemoteBuffer<RemoteSegmentPiece> {
	private static final int BYTE = 0;
	private static final int SHORT = 1;
	private static final int INT = 2;
	private final int queueSize;
	private int UPDATE_TYPE = BYTE; // byte
	private static ThreadLocal<RemoteSegmentPiecePool> poolGen = new ThreadLocal<RemoteSegmentPiecePool>() {
		@Override
		public RemoteSegmentPiecePool initialValue() {
			return new RemoteSegmentPiecePool();
		}
	};
	private final RemoteSegmentPiecePool pool = poolGen.get();

	public static class RemoteSegmentPiecePool {
		private final List<RemoteSegmentPiece> pool = new ObjectArrayList<RemoteSegmentPiece>();

		public void free(RemoteSegmentPiece p) {
			p.get().reset();
			pool.add(p);
		}

		public RemoteSegmentPiece get(boolean synchOn) {
			if(pool.isEmpty()) {
				return new RemoteSegmentPiece(new VoidSegmentPiece(), synchOn);
			} else {
				return pool.remove(pool.size() - 1);
			}
		}
	}

	public RemoteSegmentPieceBuffer(boolean synchOn, int queueSize) {
		super(RemoteSegmentPiece.class, synchOn);
		this.queueSize = queueSize;
		if(queueSize <= 0) {
			throw new IllegalArgumentException("QUEUESIZE INVALID: " + queueSize);
		}
		if(queueSize > Short.MAX_VALUE) {
			UPDATE_TYPE = INT;
		} else if(queueSize > Byte.MAX_VALUE) {
			UPDATE_TYPE = SHORT;
		} else {
			UPDATE_TYPE = BYTE;
		}
	}

	public RemoteSegmentPieceBuffer(NetworkObject synchOn, int queueSize) {
		super(RemoteSegmentPiece.class, synchOn);
		this.queueSize = queueSize;
		//		System.err.println("HHHHH "+synchOn.isOnServer()+" ---> "+queueSize);
		if(queueSize <= 0) {
			throw new IllegalArgumentException("QUEUESIZE INVALID: " + queueSize);
		}
		if(queueSize > Short.MAX_VALUE) {
			UPDATE_TYPE = INT;
		} else if(queueSize > Byte.MAX_VALUE) {
			UPDATE_TYPE = SHORT;
		} else {
			UPDATE_TYPE = BYTE;
		}
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		int collectionSize = switch(UPDATE_TYPE) {
			case (BYTE) -> buffer.readByte();
			case (SHORT) -> buffer.readShort();
			default -> buffer.readInt();
		};
		for(int n = 0; n < collectionSize; n++) {
			RemoteSegmentPiece instance = pool.get(onServer);//new RemoteSegmentPiece(null, onServer);
			instance.fromByteStream((VoidSegmentPiece) instance.get(), buffer, updateSenderStateId);
			instance.senderId = updateSenderStateId;
			getReceiveBuffer().add(instance);
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = ByteUtil.SIZEOF_INT;
		int batchSize = Math.min(queueSize, get().size());
		//add size of collection
		switch(UPDATE_TYPE) {
			case (BYTE) -> buffer.writeByte(batchSize);
			case (SHORT) -> buffer.writeShort((short) batchSize);
			default -> buffer.writeInt(batchSize);
		}
		//		System.err.println("SENDING: "+batchSize+" segs "+onServer);
		for(int i = 0; i < batchSize; i++) {
			RemoteSegmentPiece remoteField = get().remove(0);
			remoteField.setChanged(false);
			size += remoteField.toByteStream(buffer);
		}
		keepChanged = !get().isEmpty();
		//		try{
		//			throw new NullPointerException("WRITING CONTROL FOR "+segmentController);
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		return size;
	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		ObjectArrayList<RemoteSegmentPiece> rb = getReceiveBuffer();
		final int size = rb.size();
		for(int i = 0; i < size; i++) {
			pool.free(rb.get(i));
		}
		rb.clear();
	}
}
