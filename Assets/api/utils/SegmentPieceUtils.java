package api.utils;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PositionControl;
import org.schema.game.common.controller.SegmentBufferInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

import java.util.ArrayList;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class SegmentPieceUtils {
	public static boolean isControlling(SegmentPiece controller, SegmentPiece controlled) {
		return getControlledPieces(controller).contains(controlled);
	}

	public static ArrayList<SegmentPiece> getControlledPieces(SegmentPiece segmentPiece) {
		ArrayList<SegmentPiece> controlledPieces = new ArrayList<>();
		for(ElementInformation info : ElementKeyMap.getInfoArray()) {
			try {
				if(info == null) continue;
				controlledPieces.addAll(getControlledPiecesMatching(segmentPiece, info.getId()));
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
		return controlledPieces;
	}

	public static ArrayList<SegmentPiece> getControlledPiecesMatching(SegmentPiece segmentPiece, short type) {
		ArrayList<SegmentPiece> controlledPieces = new ArrayList<>();
		PositionControl control = segmentPiece.getSegmentController().getControlElementMap().getControlledElements(type, new Vector3i(segmentPiece.x, segmentPiece.y, segmentPiece.z));
		if(control != null) {
			for(long l : control.getControlMap().toLongArray()) {
				SegmentPiece p = segmentPiece.getSegmentController().getSegmentBuffer().getPointUnsave(l);
				if(p != null && p.getType() == type) controlledPieces.add(p);
			}
		}
		return controlledPieces;
	}

	public static SegmentPiece getFirstMatchingAdjacent(SegmentPiece segmentPiece, short type) {
		ArrayList<SegmentPiece> matching = getMatchingAdjacent(segmentPiece, type);
		if(matching.isEmpty()) return null;
		else return matching.get(0);
	}

	public static ArrayList<SegmentPiece> getMatchingAdjacent(SegmentPiece segmentPiece, short type) {
		ArrayList<SegmentPiece> matchingAdjacent = new ArrayList<>();
		SegmentBufferInterface buffer = segmentPiece.getSegmentController().getSegmentBuffer();
		Vector3i pos = new Vector3i(segmentPiece.getAbsolutePos(new Vector3i()));
		Vector3i[] offsets = getAdjacencyOffsets(pos);
		for(Vector3i offset : offsets) {
			if(buffer.existsPointUnsave(offset)) {
				SegmentPiece piece = buffer.getPointUnsave(offset);
				if(piece.getType() == type) matchingAdjacent.add(piece);
			}
		}
		return matchingAdjacent;
	}

	private static Vector3i[] getAdjacencyOffsets(Vector3i absPos) {
		return new Vector3i[] {
			new Vector3i(absPos.x - 1, absPos.y, absPos.z),
			new Vector3i(absPos.x + 1, absPos.y, absPos.z),
			new Vector3i(absPos.x, absPos.y - 1, absPos.z),
			new Vector3i(absPos.x, absPos.y + 1, absPos.z),
			new Vector3i(absPos.x, absPos.y, absPos.z - 1),
			new Vector3i(absPos.x, absPos.y, absPos.z + 1)
		};
	}
}
