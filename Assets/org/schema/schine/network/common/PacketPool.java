package org.schema.schine.network.common;

import org.schema.common.util.settings.SettingStateInt;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class PacketPool {
	private static final int MAX_PACKET_POOL_SIZE = 1000;
	public static final int MAX_PACKET_SIZE = 1024;
	
	private final ObjectArrayFIFOQueue<OutputPacket> outputPacketPool = new ObjectArrayFIFOQueue<OutputPacket>();
	private final ObjectArrayFIFOQueue<InputPacket> inputPacketPool = new ObjectArrayFIFOQueue<InputPacket>();
	private SettingStateInt delaySetting;
	
	
	public PacketPool(SettingStateInt delaySetting) {
		this.delaySetting = delaySetting;
	}
	
	public OutputPacket getNewOutputPacket() {
		if (!outputPacketPool.isEmpty()) {
			synchronized (outputPacketPool) {
				if (!outputPacketPool.isEmpty()) {
					return outputPacketPool.dequeue();
				}
			}
		}
		return new OutputPacket(new FastByteArrayOutputStream(MAX_PACKET_SIZE), delaySetting.getInt());
	}

	public void freeOutputPacket(final OutputPacket b) {
		b.reset();
		synchronized (outputPacketPool) {
			if (outputPacketPool.size() < MAX_PACKET_POOL_SIZE) {
				outputPacketPool.enqueue(b);
			}
		}
	}
	public InputPacket getNewInputPacket() {
		if (!inputPacketPool.isEmpty()) {
			synchronized (inputPacketPool) {
				if (!inputPacketPool.isEmpty()) {
					return inputPacketPool.dequeue();
				}
			}
		}
		return new InputPacket(MAX_PACKET_SIZE);
	}
	
	public void freeInputPacket(final InputPacket b) {
		b.reset();
		synchronized (inputPacketPool) {
			if (inputPacketPool.size() < MAX_PACKET_POOL_SIZE) {
				inputPacketPool.enqueue(b);
			}
		}
	}
}
