package org.schema.game.client.view.cubes.cubedyn;

import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class VBOManagerBulkBase {

	static final boolean DEBUG = false;
	public static int MAX_BYTES = (EngineSettings.G_VBO_BULKMODE_SIZE.getInt()) * 1024 * 1024 ; //4MB
	private static final ObjectArrayList<DrawMarker> markerPool = new ObjectArrayList<DrawMarker>();
	static {
		for(int i = 0; i < 128; i++) {
			markerPool.add(new DrawMarker());
		}
	}
	public final ObjectArrayList<VBOSeg> vboSegs = new ObjectArrayList<VBOSeg>();
	public final Int2ObjectOpenHashMap<ObjectArrayList<VBOSeg>> reservedVBOSegs = new Int2ObjectOpenHashMap<ObjectArrayList<VBOSeg>>();

	public VBOManagerBulkBase() {
		super();
	}

	static DrawMarker getMarker() {
		if (!markerPool.isEmpty()) {
			return markerPool.remove(markerPool.size() - 1);
		}
		return new DrawMarker();
	}

	static void releaseMarker(DrawMarker m) {
		m.reset();
		markerPool.add(m);
	}

	private VBOCell getCell(int sizeNeeded, int reservedId) {
		VBOCell seg = null;
		for (int i = 0; i < vboSegs.size(); i++) {
			seg = vboSegs.get(i).getFree(sizeNeeded);
			if (seg != null) {
				return seg;
			}
		}

		if (seg == null) {
			//no free cells in any current vbo
			VBOSeg newSeg = new VBOSeg(reservedVBOSegs);
			newSeg.init(sizeNeeded);
			vboSegs.add(newSeg);
			newSeg.reserved = reservedId;
			if (reservedId > 0) {
				ObjectArrayList<VBOSeg> vboSegs = reservedVBOSegs.get(reservedId);
				if (vboSegs == null) {
					vboSegs = new ObjectArrayList<>();
					reservedVBOSegs.put(reservedId, vboSegs);
				}
			}

			seg = newSeg.getFree(sizeNeeded);
		}

		return seg;
	}

	public VBOCell getFreeSegment(int sizeNeeded, DrawableRemoteSegment segment) {
		if (DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		int receiveMethod;
		VBOCell seg;
		seg = getCell(sizeNeeded, 0);
		assert (seg != null):segment+"; "+segment.getSegmentController();

		if (DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		assert (seg != null);
		return seg;
	}


	

}
