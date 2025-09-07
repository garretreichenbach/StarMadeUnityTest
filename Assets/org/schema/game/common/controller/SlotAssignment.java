package org.schema.game.common.controller;

import java.util.Map.Entry;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.game.network.objects.ShipKeyConfig;
import org.schema.game.network.objects.remote.RemoteShipKeyConfig;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.bytes.Byte2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class SlotAssignment implements TagSerializable {
	private final Byte2LongOpenHashMap slot2Pos = new Byte2LongOpenHashMap();
	private final Long2ByteOpenHashMap pos2Slot = new Long2ByteOpenHashMap();

	private final SendableSegmentController segmentController;
	private final ObjectArrayFIFOQueue<ShipKeyConfig> receivedChanged = new ObjectArrayFIFOQueue<ShipKeyConfig>();
	private boolean reassigned;

	public SlotAssignment(SendableSegmentController controller) {
		super();
		this.segmentController = controller;
	}

	public void modAndSend(byte slot, Vector3i pos) {
		modAndSend(slot, pos.x, pos.y, pos.z);
	}

	public void modAndSend(byte slot, int x, int y, int z) {
		modAndSend(slot, ElementCollection.getIndex(x, y, z));
	}

	@Override
	public String toString() {
		return "[SLOTS: "+segmentController+": "+slot2Pos+"]";
	}

	public void modAndSend(byte slot, long absPos) {
		byte mod = mod(slot, absPos);

		send(mod);
		send(slot);

	}

	private boolean checkConsistency() {
		if (slot2Pos.size() != pos2Slot.size()) {
			System.err.println("Exception: SIZES DIFFER: " + slot2Pos + " ;;;;; " + pos2Slot);
			return false;
		}
		for (byte b : slot2Pos.keySet()) {
			if (pos2Slot.get(slot2Pos.get(b)) != b) {
				return false;
			}
		}
		return true;
	}

	public void update() {
		if (!receivedChanged.isEmpty()) {
			synchronized (receivedChanged) {
				while (!receivedChanged.isEmpty()) {
					ShipKeyConfig s = receivedChanged.dequeue();

//					System.err.println("[KEY] "+segmentController+"; "+segmentController.getState()+" RECEIVED KEY ASSIGNMENT: "+s+"; "+slot2Pos+"; "+pos2Slot);

					assert (checkConsistency()) : s;
					if (s.remove) {
						removeBySlot(s.slot);
					} else {
						mod(s.slot, s.blockPos);
					}
					assert (checkConsistency()) : s;
					if (segmentController.isOnServer()) {
						segmentController.getNetworkObject().slotKeyBuffer.add(new RemoteShipKeyConfig(s, segmentController.isOnServer()));
					}
				}
			}
		}
//		if (!reassigned && slot2Pos.isEmpty() && segmentController.isOnServer()) {
//			reassignIfNotExists(new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
//		}
		if (!reassigned  && segmentController.isOnServer()) {
			long index = ElementCollection.getIndex(Ship.core);
			PlayerUsableInterface playerUsable = ((ManagedSegmentController<?>)segmentController).getManagerContainer().getPlayerUsable(index);
			if(playerUsable == null){
				//remove core if there is no usable for it (might be there in the future, in which case deleting would be bad)
				removeByPosAndSend(index);
			}
			reassigned = true;
		}
	}

	public void updateFromNetworkObject(NetworkSegmentController s) {
		for (int i = 0; i < s.slotKeyBuffer.getReceiveBuffer().size(); i++) {
			ShipKeyConfig shipKeyConfig = s.slotKeyBuffer.getReceiveBuffer().get(i).get();
			synchronized (receivedChanged) {
				receivedChanged.enqueue(shipKeyConfig);
			}
		}
	}

	public void sendAll() {
		for (byte a : slot2Pos.keySet()) {

			send(a);
		}
	}

	private void send(byte slot) {
		if (slot >= 0) {
			ShipKeyConfig shipKeyConfig = new ShipKeyConfig();
			shipKeyConfig.slot = slot;
			if (slot2Pos.containsKey(slot)) {
				//send put
				shipKeyConfig.blockPos = slot2Pos.get(slot);
			} else {
				shipKeyConfig.remove = true;
				//send remove
			}
//			try{
//				System.err.println("[KEY] "+segmentController+"; "+segmentController.getState()+" SENT KEY ASSIGNMENT: "+shipKeyConfig);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
			segmentController.getNetworkObject().slotKeyBuffer.add(new RemoteShipKeyConfig(shipKeyConfig, segmentController.isOnServer()));
			assert (checkConsistency());
		}
	}

	private byte mod(byte slot, long absPos) {
		assert (checkConsistency());
		byte rem = -1;
		slot2Pos.remove(slot);
		if (pos2Slot.containsKey(absPos)) {
			rem = pos2Slot.remove(absPos);
		}
		pos2Slot.values().remove(slot);

		slot2Pos.put(slot, absPos);
		pos2Slot.put(absPos, slot);

		assert (checkConsistency());

		return rem;
	}

	private void removeBySlot(byte slot) {
		assert (checkConsistency());

		long remove = slot2Pos.remove(slot);
		pos2Slot.remove(remove);

		assert (checkConsistency());
	}

	private byte removeByPos(Vector3i pos) {
		return removeByPos(pos.x, pos.y, pos.z);
	}

	private byte removeByPos(int x, int y, int z) {
		return removeByPos(ElementCollection.getIndex(x, y, z));
	}

	public byte removeByPos(long pos) {
		assert (checkConsistency());
		
		if (pos2Slot.containsKey(pos)) {
			byte remove = pos2Slot.remove(pos);
			slot2Pos.remove(remove);
			assert (checkConsistency());
			return remove;
		} else {
			assert (checkConsistency());
			return (byte) -1;
		}

	}

	public void removeBySlotAndSend(byte slot) {
		if (hasConfigForSlot(slot)) {
			removeBySlot(slot);
			send(slot);
		}
	}

	public byte removeByPosAndSend(Vector3i pos) {

		byte rem = removeByPos(pos);
		send(rem);
		return rem;
	}

	public byte removeByPosAndSend(int x, int y, int z) {
		byte rem = removeByPos(ElementCollection.getIndex(x, y, z));
		send(rem);
		return rem;
	}

	public byte removeByPosAndSend(long pos) {
		byte rem = removeByPos(pos);
		send(rem);
		return rem;
	}

	public boolean hasConfigForPos(Vector3i pos) {
		return hasConfigForPos(pos.x, pos.y, pos.z);
	}

	public boolean hasConfigForPos(int x, int y, int z) {
		return hasConfigForPos(ElementCollection.getIndex(x, y, z));
	}

	public boolean hasConfigForPos(long pos) {

		return pos2Slot.containsKey(pos);
	}

	public boolean hasConfigForSlot(int slot) {
		return slot2Pos.containsKey((byte) slot);
	}

	public Vector3i get(int slot) {
		if (hasConfigForSlot(slot)) {
			return ElementCollection.getPosFromIndex(slot2Pos.get((byte) slot), new Vector3i());
		} else {
			return null;
		}
	}
	public long getAsIndex(int slot) {
		if (hasConfigForSlot(slot)) {
			return slot2Pos.get((byte) slot);
		} else {
			return Long.MIN_VALUE;
		}
	}

//	public void reassignIfNotExists(Vector3i from) {
//		//if either completely empty, or only docking beam assigned
//		if (!reassigned && (slot2Pos.size() == 0 ||
//				(slot2Pos.size() == 1 && hasConfigForSlot(9) && get(9).equals(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF)))) {
//
//			if (!segmentController.isOnServer()) {
//				((GameClientState) segmentController.getState()).getController().popupInfoTextMessage(Lng.str("Automatically assigning\nweapon slots...\n(can be turned off in options)"), 0);
//			}
//			PositionControl controlledElements = segmentController.getControlElementMap().getControlledElements(Element.TYPE_ALL, from);
//			reassignClient(controlledElements);
//
//			if (from.equals(Ship.core)) {
//				mod((byte) 9, ElementCollection.getIndex(Ship.core.x, Ship.core.y, Ship.core.z));
//			}
//			sendAll();
//			reassigned = true;
//		}
//	}
//
//	private void reassignClient(PositionControl controlledElements) {
//		int i = 0;
//		long coreindex = ElementCollection.getIndex(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
//		for (long v : segmentController.getControlElementMap().getControllingMap().keySet()) {
//			if (v != coreindex) {
//				if (i != 9) {
//					if (controlledElements.getControlPosMap().contains(v)) {
//						mod((byte) i, v);
//					}
//				}
//				i++;
//			}
//		}
//
//	}

	public int getByIndex(long pos) {
		if (hasConfigForPos(pos)) {
			return pos2Slot.get(pos);
		}

		return -1;
	}
	public int getByPos(Vector3i pos) {
		return getByIndex(ElementCollection.getIndex(pos));
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] top = (Tag[]) tag.getValue();
		Tag[] entrySet = (Tag[]) top[1].getValue();

		for (int i = 0; i < entrySet.length - 1; i++) {
			Tag[] v = (Tag[]) entrySet[i].getValue();
			byte slot = (Byte) v[0].getValue();
			long pos = (Long) v[1].getValue();
			
			if(segmentController.isLoadedFromChunk16()){
				pos = ElementCollection.shiftIndex(pos, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_);
			}
			slot2Pos.put(slot, pos);
			pos2Slot.put(pos, slot);
		}

	}

	@Override
	public Tag toTagStructure() {
		Tag[] tags = new Tag[slot2Pos.size() + 1];
		tags[tags.length - 1] = FinishTag.INST;
		int i = 0;
		for (Entry<Byte, Long> e : slot2Pos.entrySet()) {
			tags[i] = new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.BYTE, null, e.getKey()),
					new Tag(Type.LONG, null, e.getValue()),
					FinishTag.INST});
			i++;
		}
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, (byte) 0), // placeholder if there is a need for any more data
				new Tag(Type.STRUCT, null, tags), // the actual maps
				FinishTag.INST
		});
	}

}
