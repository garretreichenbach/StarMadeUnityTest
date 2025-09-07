package org.schema.game.common.controller.pathfinding;

import javax.vecmath.Vector3f;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.generator.EmptyCreatorThread;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.pathfinding.BreakTestRequest;
import org.schema.schine.network.server.ServerStateInterface;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class IslandCalculator extends AbstractAStarCalculator<BreakTestRequest> {
	public static byte[] buffer = new byte[4096];
	public static FastByteArrayOutputStream sb = new FastByteArrayOutputStream(buffer);
	private ObjectOpenHashSet<Segment> reRequest = new ObjectOpenHashSet<Segment>();

	public IslandCalculator() {
		super(false);
	}

	@Override
	public void init(BreakTestRequest cr) {
		super.init(cr);
		reRequest.clear();
	}

	@Override
	public boolean canTravelPoint(Vector3i point, Vector3i from, SegmentController controller) {
		return controller == null || controller.getSegmentBuffer().existsPointUnsave(point);
	}

	@Override
	protected float getWeight(Vector3i before, Vector3i from, Vector3i to) {
		return getDistToSearchPos(from);
	}

	@Override
	protected float getWeightByBestDir(Vector3i before, Vector3i from, Vector3i to, Vector3i prefferedDir) {
				return 0;
	}

	public void send(SendableSegmentController c) {
	}

	//	public void send(SendableSegmentController c){
	//		for(Segment seg : reRequest){
	//			synchronized (c.getNetworkObject()) {
	//				((RemoteSegment)seg).setLastChanged(System.currentTimeMillis());
	//				//				System.err.println("Sending dirty segment: "+seg);
	//				c.getNetworkObject().dirtySegmentBuffer.add(new RemoteVector3i(seg.pos, c.getNetworkObject()));
	//			}
	//		}
	//		try {
	//			System.err.println("SEGMENT TO ADD: MINMAX: "+to.getMinPos()+", "+to.getMaxPos());
	//			((GameServerState)to.getState()).getController().getSynchController().addImmediateSynchronizedObject(to);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//	}
	public boolean breakUp(SegmentController from) {
		if (currentPart.isEmpty()) {
			System.err.println("CURRENT PART IS EMPTY");
			return false;
		}
		LongIterator iterator = currentPart.iterator();
		Vector3i posMod = new Vector3i();
		Vector3i posModMod = new Vector3i();
		Vector3i segPos = new Vector3i();
		to = new FloatingRock(from.getState());

		SegmentPiece p = new SegmentPiece();
		Object2ObjectOpenHashMap<Vector3i, RemoteSegment> newSegMap = new Object2ObjectOpenHashMap<Vector3i, RemoteSegment>();

		System.err.println("LOCAL MINMAX: " + min + "; " + max);
		center.set(min);
		center.add((max.x - min.x) / 2 - SegmentData.SEG_HALF, (max.y - min.y) / 2 - SegmentData.SEG_HALF, (max.z - min.z) / 2 - SegmentData.SEG_HALF);
		max.sub(center);
		min.sub(center);

		System.err.println("CENTERED MINMAX: " + min + "; " + max + " ;;; " + center);
		long time = System.currentTimeMillis();
		while (iterator.hasNext()) {
			long index = iterator.nextLong();
			ElementCollection.getPosFromIndex(index, posMod);
			SegmentPiece pointUnsave = from.getSegmentBuffer().getPointUnsave(posMod, p); //autorequest true previously

			posModMod.sub(posMod, center);

			byte elemIndexTmpX = (byte) ByteUtil.modUSeg(posModMod.x);
			byte elemIndexTmpY = (byte) ByteUtil.modUSeg(posModMod.y);
			byte elemIndexTmpZ = (byte) ByteUtil.modUSeg(posModMod.z);

			int segIndexTmpX = ByteUtil.divUSeg(posModMod.x);
			int segIndexTmpY = ByteUtil.divUSeg(posModMod.y);
			int segIndexTmpZ = ByteUtil.divUSeg(posModMod.z);

			segIndexTmpX *= SegmentData.SEG;
			segIndexTmpY *= SegmentData.SEG;
			segIndexTmpZ *= SegmentData.SEG;

			segPos.set(segIndexTmpX, segIndexTmpY, segIndexTmpZ);
			boolean empty = false;

			RemoteSegment s = newSegMap.get(segPos);
			if (s == null) {
				s = new RemoteSegment(to);
				s.pos.set(segPos);
				SegmentData freeSegmentData = to.getSegmentProvider().getFreeSegmentData();
				freeSegmentData.assignData(s);
				newSegMap.put(new Vector3i(segPos), s);

			}
			byte xP = (byte) ByteUtil.modUSeg(posModMod.x);
			byte yP = (byte) ByteUtil.modUSeg(posModMod.y);
			byte zP = (byte) ByteUtil.modUSeg(posModMod.z);

			//no need to sync since this is all on new segments
			try {
				s.getSegmentData().applySegmentData(xP, yP, zP, pointUnsave.getData(), 0, true, s.getAbsoluteIndex(xP, yP, zP), true, true, time);
			} catch (SegmentDataWriteException e) {
				e.printStackTrace();
			}

			//this MUST be synched (missile hits do stuff unsynched)
			try {
				pointUnsave.getSegment().getSegmentData().setType(SegmentData.getInfoIndex(pointUnsave.x, pointUnsave.y, pointUnsave.z), Element.TYPE_NONE);
			} catch (SegmentDataWriteException e) {
				e.printStackTrace();
			}
			reRequest.add(pointUnsave.getSegment());
		}
		to.setUniqueIdentifier("ENTITY_FLOATINGROCK_DEBRIS_" + System.currentTimeMillis());
		min.x = ByteUtil.divSeg(min.x);
		min.y = ByteUtil.divSeg(min.y);
		min.z = ByteUtil.divSeg(min.z);

		max.x = ByteUtil.divSeg(max.x) + 1;
		max.y = ByteUtil.divSeg(max.y) + 1;
		max.z = ByteUtil.divSeg(max.z) + 1;

		to.initialize();

		to.getMinPos().set(min);
		to.getMaxPos().set(max);

		to.setId(((ServerStateInterface)from.getState()).getNextFreeObjectId());
		to.setSectorId(from.getSectorId());

		Transform t = new Transform(from.getWorldTransform());

		Vector3f mod = new Vector3f(center.x, center.y, center.z);
		t.basis.transform(mod);
		t.origin.add(mod);

		to.getInitialTransform().set(t);

		to.setCreatorThread(new EmptyCreatorThread(to));

		to.setTouched(true, true);

		for (RemoteSegment r : newSegMap.values()) {
			System.err.println("ACT ADDING SEGMENT: " + r + ": " + r.getSize());
			to.getSegmentProvider().addSegmentToBuffer(r);
		}
		return true;

	}

}
