package org.schema.game.common.controller.elements.power.reactor;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.PolygonTools;
import org.schema.common.util.linAlg.PolygonToolsVars;
import org.schema.common.util.linAlg.Triangle;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.quickhull.Point3d;
import org.schema.common.util.linAlg.quickhull.QuickHull3D;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitUnit;
import org.schema.game.common.controller.elements.shield.CenterOfMassUnit;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.StructureAudioEmitter;
import org.schema.schine.common.language.Lng;

import javax.vecmath.Vector3f;

public class MainReactorUnit extends CenterOfMassUnit<MainReactorUnit, MainReactorCollectionManager, MainReactorElementManager> implements StructureAudioEmitter {

	private boolean single;

	public Triangle[] tris;

	private Vector3f tmpPoint = new Vector3f();

	private long[] singles;

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		Vector3i dim = new Vector3i();
		dim.sub(getMax(new Vector3i()), getMin(new Vector3i()));
		return ControllerManagerGUI.create(state, Lng.str("Main Reactor Module"), this, new ModuleValueEntry(Lng.str("Dimension"), dim), new ModuleValueEntry(Lng.str("Recharge"), "N/A"));
	}

	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet) {
		LongArrayList nc = getNeighboringCollectionUnsave();
		if (nc.size() > 3) {
			Point3d[] points = new Point3d[nc.size() + 2];
			int c = 0;
			for (int i = 0; i < nc.size(); i++) {
				long index = nc.getLong(i);
				points[c] = new Point3d(ElementCollection.getPosX(index), ElementCollection.getPosY(index), ElementCollection.getPosZ(index));
				c++;
				if (i == 0) {
					points[c] = new Point3d(ElementCollection.getPosX(index) + 0.5, ElementCollection.getPosY(index) + 0.5, ElementCollection.getPosZ(index) + 0.5);
					// add additional point within to guarantee that points arent colinear
					c++;
					points[c] = new Point3d(ElementCollection.getPosX(index) - 0.5, ElementCollection.getPosY(index) - 0.5, ElementCollection.getPosZ(index) - 0.5);
					// add additional point within to guarantee that points arent colinear
					c++;
				}
			}
			try {
				QuickHull3D hull = new QuickHull3D();
				if (nc.size() > 20000) {
					hull.setExplicitDistanceTolerance(0.34);
				} else if (nc.size() > 10000) {
					hull.setExplicitDistanceTolerance(0.32);
				} else if (nc.size() > 5000) {
					hull.setExplicitDistanceTolerance(0.3);
				} else if (nc.size() > 3000) {
					hull.setExplicitDistanceTolerance(0.2);
				} else if (nc.size() > 1000) {
					hull.setExplicitDistanceTolerance(0.1);
				}
				// System.err.println("TOLERANCE: "+nc.size()+": "+hull.getExplicitDistanceTolerance());
				hull.build(points, points.length);
				hull.triangulate();
				int numFaces = hull.getNumFaces();
				int[][] faces = hull.getFaces();
				Point3d[] vertices = hull.getVertices();
				tris = new Triangle[numFaces];
				for (int i = 0; i < numFaces; i++) {
					Point3d v1 = vertices[faces[i][0]];
					Point3d v2 = vertices[faces[i][1]];
					Point3d v3 = vertices[faces[i][2]];
					tris[i] = new Triangle(new Vector3f((float) v1.x, (float) v1.y, (float) v1.z), new Vector3f((float) v2.x, (float) v2.y, (float) v2.z), new Vector3f((float) v3.x, (float) v3.y, (float) v3.z));
				}
			} catch (Exception e) {
				// e.printStackTrace();
				// for(int i = 0; i < points.length; i++){
				// System.err.println("#"+i+": "+points[i]);
				// }
				// try {
				// throw new Exception("Calculating quick hull based on: "+points.length+" points", e);
				// } catch (Exception e1) {
				// e1.printStackTrace();
				// }
				System.err.println("[QUICKHULL]" + getSegmentController().getState() + " WARNING: failed calculation of quickhull for " + getSegmentController() + ". Using fallback. Used " + nc.size() + " blocks");
				single = true;
				singles = new long[nc.size()];
				for (int i = 0; i < singles.length; i++) {
					singles[i] = nc.getLong(i);
				}
			}
		} else {
			single = true;
			singles = new long[nc.size()];
			for (int i = 0; i < singles.length; i++) {
				singles[i] = nc.getLong(i);
			}
		}
		super.calculateExtraDataAfterCreationThreaded(updateSignture, totalCollectionSet);
	}

	@Override
	public boolean onChangeFinished() {
		ConduitCollectionManager conduits = getPowerInterface().getConduits();
		// flag all overlapping conduits to check their overlap
		for (ConduitUnit c : conduits.getElementCollections()) {
			if (ElementCollection.overlaps(c, this, 1, 0)) {
				c.flagCalcConnections();
			}
		}
		return super.onChangeFinished();
	}

	public float distanceToThis(long index, PolygonToolsVars v) {
		double x = ElementCollection.getPosX(index);
		double y = ElementCollection.getPosY(index);
		double z = ElementCollection.getPosZ(index);
		if (single) {
			float min = Float.POSITIVE_INFINITY;
			for (int i = 0; i < singles.length; i++) {
				// distance to single element of this group (because there is only one in this)
				long fst = singles[i];
				double xTo = ElementCollection.getPosX(fst) - x;
				double yTo = ElementCollection.getPosY(fst) - y;
				double zTo = ElementCollection.getPosZ(fst) - z;
				float d = FastMath.carmackSqrt((float) (xTo * xTo + yTo * yTo + zTo * zTo));
				if (d < min) {
					min = d;
					v.outFrom.set(ElementCollection.getPosX(fst), ElementCollection.getPosY(fst), ElementCollection.getPosZ(fst));
				}
			}
			return min;
		} else {
			if (tris == null) {
				// System.err.println(this.getSegmentController().getState()+": "+this+"; ERROR: NO QUICKHULL");
				return Float.POSITIVE_INFINITY;
			}
			this.tmpPoint.set((float) x, (float) y, (float) z);
			return PolygonTools.distance(tris, tris.length, this.tmpPoint, v);
		}
	}

	@Override
	public void startAudio() {
		/*AudioController.fireAudioEvent("MAIN_REACTOR", new AudioTag[] { AudioTags.GAME, AudioTags.AMBIENCE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.MAIN_REACTOR }, AudioParam.START, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this))*/
		//AudioController.fireAudioEventID(897, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this));
	}

	@Override
	public void stopAudio() {
		/*AudioController.fireAudioEvent("MAIN_REACTOR", new AudioTag[] { AudioTags.GAME, AudioTags.AMBIENCE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.MAIN_REACTOR }, AudioParam.STOP, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this))*/
		//AudioController.fireAudioEventID(898, AudioController.ent(getSegmentController(), getElementCollectionId(), getSignificator(), this));
	}
}