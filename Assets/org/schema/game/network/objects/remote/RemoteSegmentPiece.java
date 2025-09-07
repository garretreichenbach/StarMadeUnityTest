package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteSegmentPiece extends RemoteField<SegmentPiece> {

	public static final int FULL = 0;
	public static final int ACTIVATE = 16;
	public static final int DEACTIVATE = 32;
	public static final int REMOVE = 64;
	public static final int REMOVE_SOFT = 128;
	private static final int addBits = 8;
	private static final int shortBits = 4;
	public int senderId;
	public long controllerPos = Long.MIN_VALUE;
	public int simpleMode = FULL;

	public RemoteSegmentPiece(SegmentPiece modification, boolean synchOn) {
		super(modification, synchOn);
	}

	public RemoteSegmentPiece(SegmentPiece modification, NetworkObject synchOn) {
		super(modification, synchOn);
	}

	@Override
	public int byteLength() {
		return 1; //segPos
	}

	@Override
	public int hashCode() {
		return get().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return get().equals(((RemoteSegmentPiece)obj).get());
	}
	public void fromByteStream(VoidSegmentPiece p, DataInputStream stream, int updateSenderStateId) throws IOException {
		int dataType = stream.readByte() & 255;

		boolean forceClientSegmentAdd;

		if (dataType >= addBits) {
			dataType -= addBits;
			forceClientSegmentAdd = true;
		} else {
			forceClientSegmentAdd = false;
		}
		int x;
		int y;
		int z;
		//check databit for controller info
		if (dataType >= shortBits) {
			dataType -= shortBits;
			controllerPos = ElementCollection.getIndex(stream.readShort(),stream.readShort(),stream.readShort());
		} else {
			controllerPos = Long.MIN_VALUE;
		}
		if (dataType == 0) {
			x = stream.readByte();
			y = stream.readByte();
			z = stream.readByte();
		} else if (dataType == 1) {
			x = stream.readShort();
			y = stream.readShort();
			z = stream.readShort();
		} else {
			x = stream.readInt();
			y = stream.readInt();
			z = stream.readInt();
		}

		int data = SegmentPiece.deserializeData(stream);

		
		
		p.forceClientSegmentAdd = forceClientSegmentAdd;
		p.senderId = updateSenderStateId;
		p.controllerPos = controllerPos;
		p.setDataByReference(data);
		p.voidPos.set(x, y, z);
		set(p);
	}
	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		VoidSegmentPiece p = new VoidSegmentPiece();
		fromByteStream(p, stream, updateSenderStateId);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		assert (get() != null);
		if(get().getSegment() != null) {
			writeDynamicPosition(
					get().x + get().getSegment().pos.x,
					get().y + get().getSegment().pos.y,
					get().z + get().getSegment().pos.z,
					get().forceClientSegmentAdd,
					buffer);
		}
		
		
		// write the payload (int) as 3 bytes
		SegmentPiece.serializeData(buffer, get().getData());
		return 1;
	}

	public void writeDynamicPosition(int x, int y, int z, boolean forceClientSegmentAdd, DataOutputStream buffer) throws IOException {

		int mode = 0;

		if (forceClientSegmentAdd) {
			mode += 8;
		}

		if (controllerPos != Long.MIN_VALUE) {
			mode += 4;
		}

		if (x >= Byte.MIN_VALUE && y >= Byte.MIN_VALUE && z >= Byte.MIN_VALUE &&
				x <= Byte.MIN_VALUE && y <= Byte.MIN_VALUE && z <= Byte.MAX_VALUE) {
			buffer.writeByte((mode + 0));
			if (controllerPos != Long.MIN_VALUE) {
				buffer.writeShort(ElementCollection.getPosX(controllerPos));
				buffer.writeShort(ElementCollection.getPosY(controllerPos));
				buffer.writeShort(ElementCollection.getPosZ(controllerPos));
			}

			buffer.writeByte(x);
			buffer.writeByte(y);
			buffer.writeByte(z);
		} else if (x >= Short.MIN_VALUE && y >= Short.MIN_VALUE && z >= Short.MIN_VALUE &&
				x <= Short.MIN_VALUE && y <= Short.MIN_VALUE && z <= Short.MAX_VALUE) {
			buffer.writeByte((mode + 1));
			if (controllerPos != Long.MIN_VALUE) {
				buffer.writeShort(ElementCollection.getPosX(controllerPos));
				buffer.writeShort(ElementCollection.getPosY(controllerPos));
				buffer.writeShort(ElementCollection.getPosZ(controllerPos));
			}
			buffer.writeShort(x);
			buffer.writeShort(y);
			buffer.writeShort(z);
		} else {
			buffer.writeByte((mode + 2));
			if (controllerPos != Long.MIN_VALUE) {
				buffer.writeShort(ElementCollection.getPosX(controllerPos));
				buffer.writeShort(ElementCollection.getPosY(controllerPos));
				buffer.writeShort(ElementCollection.getPosZ(controllerPos));
			}
			buffer.writeInt(x);
			buffer.writeInt(y);
			buffer.writeInt(z);
		}
	}

}
