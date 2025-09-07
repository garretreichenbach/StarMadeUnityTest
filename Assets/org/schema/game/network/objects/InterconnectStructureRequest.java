package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidUniqueSegmentPiece;

public class InterconnectStructureRequest implements SerializationInterface {

	public VoidUniqueSegmentPiece fromPiece;
	public VoidUniqueSegmentPiece toPiece;
	public int playerId = -1;

	public InterconnectStructureRequest() {
		fromPiece = new VoidUniqueSegmentPiece();
		toPiece = new VoidUniqueSegmentPiece();
	}

	public InterconnectStructureRequest(SegmentPiece controllerPiece, SegmentPiece controlledPiece, int playerId) {
		fromPiece = new VoidUniqueSegmentPiece(controllerPiece);
		toPiece = new VoidUniqueSegmentPiece(controlledPiece);
		this.playerId = playerId;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		fromPiece.serialize(b);
		toPiece.serialize(b);
		b.writeInt(playerId);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		fromPiece.deserialize(b);
		toPiece.deserialize(b);
		playerId = b.readInt();
	}

}
