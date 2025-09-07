package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.io.SegmentDataIONew;
import org.schema.game.common.data.SegmentSignature;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class RemoteSegmentRemoteObj extends RemoteField<SegmentSignature> {

	private static FastByteArrayOutputStream tmpArrayBuffer = new FastByteArrayOutputStream(1024 * 10);
	private static DataOutputStream tmpBuffer = new DataOutputStream(tmpArrayBuffer);
	private static final int bufferSize = 1024 * 100;
	private static ThreadLocal<byte[]> threadLocal = new ThreadLocal<byte[]>() {
		@Override
		protected byte[] initialValue() {
			return new byte[bufferSize];
		}
	};
	private RemoteSegment segmentFromCache;

	public RemoteSegmentRemoteObj(boolean synchOn) {
		super(new SegmentSignature(), synchOn);
	}

	public RemoteSegmentRemoteObj(NetworkObject synchOn) {
		super(new SegmentSignature(), synchOn);
	}

	public RemoteSegmentRemoteObj(RemoteSegment segmentFromCache,
	                              SegmentSignature sig,
	                              NetworkObject synchOn) {
		super(sig, synchOn);
		this.segmentFromCache = segmentFromCache;
	}

	//	public RemoteSegmentRemoteObj(SegmentSignature sig, boolean synchOn) {
	//		super(sig, synchOn);
	//	}
	//
	//	public RemoteSegmentRemoteObj(SegmentSignature sig, NetworkObject synchOn) {
	//		super(sig, synchOn);
	//	}

	public static void decode(DataInputStream buffer, int updateSenderStateId, boolean onServer, SegmentController segmentController) throws IOException {
		int x = buffer.readShort();
		int y = buffer.readShort();
		int z = buffer.readShort();
		int len = buffer.readShort();
		assert(len >= 0 && len <= bufferSize):len+" / "+bufferSize;
		byte[] bs = threadLocal.get();
		buffer.readFully(bs, 0, len);

		FastByteArrayInputStream tmpClientArrayBuffer = new FastByteArrayInputStream(bs);
		DataInputStream tmpClientBuffer = new DataInputStream(tmpClientArrayBuffer);

		ClientSegmentProvider pro = (ClientSegmentProvider) segmentController.getSegmentProvider();

		long index = ElementCollection.getIndex(x, y, z);
		//		get().setPos(new Vector3i(x,y,z));

		pro.decode(x, y, z, index, len, tmpClientBuffer);

	}

	@Override
	public int byteLength() {
		return 0;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		assert (false);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		//		GameServerState state = (GameServerState) segmentController.getState();
		SegmentController segmentController = get().context;

		int len = 0;
		assert (tmpArrayBuffer.length == 0);

		boolean available = false;
		try {
			Segment segmentFromBuffer = segmentFromCache;
			assert (segmentFromCache != null);
			if (segmentFromBuffer != null) {
				RemoteSegment seg = (RemoteSegment) segmentFromBuffer;
				assert (seg.pos.equals(get().getPos())) : " serializing " + get().getPos() + "; toSerialize " + seg.pos;
				len = seg.serialize(tmpBuffer, true, seg.getLastChanged(), SegmentDataIONew.VERSION);
				available = true;
			} else {
				System.err.println("Exception: Serialization failed: segment from cache is null");
				throw new RuntimeException("Exception: Serialization failed: segment from cache is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			assert (false);
		}

		//		System.err.println("ENCODING SEGMENT "+get().context+" "+get().getPos()+" LEN "+len);
		assert (len == tmpArrayBuffer.length) : "LEN " + len + "; written: " + tmpBuffer.size();
		assert (len > 0);
		buffer.writeShort(get().getPos().x);
		buffer.writeShort(get().getPos().y);
		buffer.writeShort(get().getPos().z);
		//12288 uncompressed. so no more then that -> short can be used
		buffer.writeShort(len);
		buffer.write(tmpArrayBuffer.array, 0, len);
		tmpArrayBuffer.reset();

		//		System.err.println("SERIALIZED: "+segmentController+" "+get().getPos());

		return 5 * ByteUtil.SIZEOF_INT + len;

	}
}
