package org.schema.game.server.controller.world.factory;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.RequestData;

public class WorldCreatorSimpleFactory extends WorldCreatorFactory {

	public WorldCreatorSimpleFactory() {
	}

	private void createFromCorner(Segment w, SegmentController world) {
		Vector3b pos = new Vector3b();
		for (byte z = 0; z < SegmentData.SEG; z++) {
			for (byte y = 0; y < SegmentData.SEG; y++) {
				for (byte x = 0; x < (SegmentData.SEG); x++) {
					if (Math.random() > 0.299) {
						continue;
					}
					pos.set(x, y, z);
					byte e = Element.TYPE_NONE;
					double v = Math.random();
					//					if(v < 0.2){
					//						e =(byte)Element.getType(ShipCoreElement.class);
					//					}else if(v < 0.4){
					//						e =(byte)Element.getType(ShipHullElement.class);
					//					}else if(v < 0.8){
					//						e = (byte)Element.getType(ShipWeaponControllerElement.class);
					//					}else{
					//						e = (byte)Element.getType(ShipShieldGeneratorElement.class);
					//					}
					//					w.getSegmentData().setInfoElement(pos, e, true);
				}
			}
		}
	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		createFromCorner(w, world);
	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

	//	public void getQuad(int minX, int minZ, int maxX, int maxZ, int y, ManagedStaticDrawableSegment world){
	//		setEarthElement(minX, y, minZ, world);
	//		setEarthElement(maxX, y, minZ, world);
	//		setEarthElement(maxX, y, maxZ, world);
	//		setEarthElement(minX, y, maxZ, world);
	//	}
	//
	//
	//	public void getMiddle(int minX, int minZ, int maxX, int maxZ, int y, PlanetSurface world){
	//		int dX = maxX - minX;
	//		int dZ = maxZ - minZ;
	//		setEarthElement(minX+dX/2, y, minZ+dZ/2, world);
	//
	//		getQuad(minX, minZ, minX+dX, minZ+dZ, y, world);
	//		getQuad(minX, minZ+dX, minX+dX, maxZ, y, world);
	//		getQuad(minX+dX, minZ+dX, maxX, maxZ, y, world);
	//		getQuad(minX+dX, minZ, maxX, minZ+dZ, y, world);
	//	}

	//	public Element setEarthElement(int x, int y, int z, PlanetSurface world){
	//		Element e = new EarthElement();
	//		world.setElement(x, y, z, e);
	//		return e;
	//	}

}
