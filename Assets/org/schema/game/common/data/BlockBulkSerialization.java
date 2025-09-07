package org.schema.game.common.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

/**
 * Note that the received buffer is only freed on client
 * and the sender on server
 * <p/>
 * So to use this client -> server, the free methods will have to be added
 * to after processing/sending
 *
 * @author schema
 */
public class BlockBulkSerialization {

	private static ObjectArrayFIFOQueue<ByteArrayList> senderPoolReadyToSend = new ObjectArrayFIFOQueue<ByteArrayList>();
	private static ObjectArrayFIFOQueue<ByteArrayList> senderPool = new ObjectArrayFIFOQueue<ByteArrayList>();
	private static ObjectArrayFIFOQueue<ByteArrayList> receiverPool = new ObjectArrayFIFOQueue<ByteArrayList>();
	public Long2ObjectOpenHashMap<ByteArrayList> buffer;
	public int sentToCount;

	public static void freeBufferClient(ByteArrayList b) {
		b.clear();
		synchronized (receiverPool) {
			receiverPool.enqueue(b);
		}
	}

	public static ByteArrayList getBufferClient() {
		synchronized (receiverPool) {
			if (receiverPool.size() > 0) {
				return receiverPool.dequeue();
			}
		}
		return new ByteArrayList();
	}

	public static void freeBufferServer(ByteArrayList b) {
		b.clear();
		synchronized (senderPool) {
			senderPool.enqueue(b);
		}
	}

	public static ByteArrayList getBufferServer() {
		synchronized (senderPoolReadyToSend) {
			ByteArrayList bufferServer = getBufferServerP();
			senderPoolReadyToSend.enqueue(bufferServer);
			return bufferServer;
		}
	}

	private static ByteArrayList getBufferServerP() {
		synchronized (senderPool) {
			if (senderPool.size() > 0) {
				return senderPool.dequeue();
			}
		}
		return new ByteArrayList();
	}

	public static void freeUsedServerPool() {
		synchronized (senderPoolReadyToSend) {
			while (!senderPoolReadyToSend.isEmpty()) {
				freeBufferServer(senderPoolReadyToSend.dequeue());
			}
		}
	}

	public void deserialize(DataInputStream s) throws IOException {

		int bSize = s.readInt();

		buffer = new Long2ObjectOpenHashMap<ByteArrayList>(bSize);

		for (int i = 0; i < bSize; i++) {
			long seg = s.readLong();

			int size = s.readInt();

			ByteArrayList bufferClient = getBufferClient();
			bufferClient.ensureCapacity(size);

			for (int j = 0; j < size; j++) {
				bufferClient.add(s.readByte());
			}

			buffer.put(seg, bufferClient);
		}

	}

	public void serialize(DataOutputStream s) throws IOException {

		s.writeInt(buffer.size());
		for (Entry<Long, ByteArrayList> e : buffer.entrySet()) {
			s.writeLong(e.getKey());

			final int size = e.getValue().size();
			s.writeInt(size);

			for (int i = 0; i < size; i++) {
				s.writeByte(e.getValue().get(i));
			}
		}
	}

}
