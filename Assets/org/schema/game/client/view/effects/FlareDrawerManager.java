package org.schema.game.client.view.effects;

import java.util.Iterator;
import java.util.Map;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FlareDrawerManager implements BloomEffectInterface, Drawable{
	private static ObjectArrayList<FlareDrawer> flareDrawerPool = new ObjectArrayList<FlareDrawer>();
	private final Map<ManagedSegmentController<?>, FlareDrawer> flareDrawerMap = new Object2ObjectOpenHashMap<ManagedSegmentController<?>, FlareDrawer>();
	private final FlareDrawer[] toDraw = new FlareDrawer[64];
	private int flarePointer;
	private boolean drawNeeded;

	private static FlareDrawer getDrawer(ManagedSegmentController<?> s, FlareDrawerManager man) {
		if (flareDrawerPool.isEmpty()) {
			return new FlareDrawer(s, man);
		} else {
			FlareDrawer remove = flareDrawerPool.remove(0);
			remove.cleanUp();
			remove.set(s);
			return remove;
		}
	}

	private static void releaseDrawer(FlareDrawer sd) {
		sd.unset();
		flareDrawerPool.add(sd);
	}

	public void activate(FlareDrawer flareDrawer, boolean active) {
		if (active) {
			//			System.err.println("NOTIFIED OBSERVER OF HIT!");
			if (flarePointer < toDraw.length) {
				toDraw[flarePointer] = flareDrawer;
				flarePointer++;
			}
		} else {
			//			System.err.println("NOTIFIED OBSERVER OF HIT END!");
			//			if(toDraw.length == 1){
			//				flarePointer = 0;
			//			}else{
			if (flarePointer > 0 && flarePointer < toDraw.length) {
				for (int i = 0; i < toDraw.length; i++) {
					if (toDraw[i] == flareDrawer) {
						//							System.err.println("POINT: "+flarePointer+"; len "+toDraw.length+"; i "+i);
						toDraw[i] = toDraw[flarePointer - 1];

						flarePointer--;
						break;
					}
				}
			}
			//			}
		}

		drawNeeded = flarePointer > 0;
	}

	public void addController(ManagedSegmentController<?> s) {
		FlareDrawer drawer = getDrawer(s, this);
		flareDrawerMap.put(drawer.controller, drawer);
	}

	public void clearSegment(DrawableRemoteSegment segment) {
		if(segment.getSegmentController() instanceof ManagedSegmentController<?>) {
			FlareDrawer flareDrawer = flareDrawerMap.get((ManagedSegmentController<?>)segment.getSegmentController());
	
			if (flareDrawer != null) {
				flareDrawer.clear(segment);
				activate(flareDrawer, false);
			}
		}
	}

	//	public void clear(){
	//		for(FlareDrawer sd : flareDrawerMap.values()){
	//			releaseDrawer(sd);
	//		}
	//		flarePointer = 0;
	//		flareDrawerMap.clear();
	//	}
	@Override
	public void draw() {
		if(!MainGameGraphics.drawBloomedEffects()){
			return;
		}
		if (drawNeeded) {
			for (int i = 0; i < flarePointer; i++) {
				toDraw[i].draw();
			}
		}
	}
	@Override
	public void drawRaw() {
		if (drawNeeded) {
			for (int i = 0; i < flarePointer; i++) {
				toDraw[i].drawRaw();
			}
		}
	}
	/**
	 * @return the flareDrawerMap
	 */
	public Map<ManagedSegmentController<?>, FlareDrawer> getFlareDrawerMap() {
		return flareDrawerMap;
	}

	public void refresh(
			Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>> currentEntities) {
		Iterator<FlareDrawer> iterator = flareDrawerMap.values().iterator();
		while (iterator.hasNext()) {
			FlareDrawer next = iterator.next();
			if (!currentEntities.containsKey(next.controller.getSegmentController().getId())) {
				//				System.err.println("RELEASING FLARES: "+next.controller+": "+next.containsFlares());
				releaseDrawer(next);
				iterator.remove();
			}
		}

		for (SimpleTransformableSendableObject<?> s : currentEntities.values()) {
			if (s instanceof ManagedSegmentController<?>) {
				if (!flareDrawerMap.containsKey((ManagedSegmentController<?>)s)) {
					addController((ManagedSegmentController<?>) s);
				}
			}
		}
		for (int i = 0; i < flarePointer; i++) {
			toDraw[i] = null;
		}
		flarePointer = 0;
		for (FlareDrawer d : flareDrawerMap.values()) {
			//			System.err.println("CONTAINS FLARES: "+d.controller+": "+d.containsFlares());

			if (d.containsFlares()) {
				activate(d, true);
			}
		}

	}

	public void update(Timer timer) {

		//		while(!toAdd.isEmpty()){
		//
		//
		//			FlareDrawer n = toAdd.remove(0);
		//
		//
		//
		//		}
	}

	public void updateSegment(DrawableRemoteSegment segment) {
		IntArrayList beacons = segment.getCurrentBufferContainer().beacons;

		int size = beacons.size();

		//		System.err.println("updating FLARE: "+segment.getSegmentController()+": "+size);

		Vector3b posInSegment = new Vector3b();
		Vector3i pos = new Vector3i();
		SegmentPiece p = new SegmentPiece();
		@SuppressWarnings("unlikely-arg-type")
		FlareDrawer flareDrawer = flareDrawerMap.get(segment.getSegmentController());

		if (flareDrawer != null) {
			boolean activeBefore = flareDrawer.containsFlares();
			flareDrawer.clear(segment);

			for (int i = 0; i < size; i++) {
				SegmentData.getPositionFromIndex(beacons.get(i), posInSegment);
				//				System.err.println("ADDING FLARE AT "+posInSegment+" FOR "+segment.getPos());
				p.setByReference(segment, posInSegment);
				flareDrawer.addFlare(p);
			}
			boolean activeAfter = flareDrawer.containsFlares();
			if (activeBefore && !activeAfter) {
				activate(flareDrawer, false);
			} else if (!activeBefore && activeAfter) {
				activate(flareDrawer, true);
			}
		}
		segment.getCurrentBufferContainer().beacons.clear();
	}

	@Override
	public void cleanUp() {
		for(FlareDrawer e : flareDrawerMap.values()){
			e.cleanUp();
		}
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
				
	}

	

}
