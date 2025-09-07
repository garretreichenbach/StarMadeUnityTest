package org.schema.game.common.controller.elements.rail.speed;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class RailSpeedCollectionManager extends
		ControlBlockElementCollectionManager<RailSpeedUnit, RailSpeedCollectionManager, RailSpeedElementManager> {

//	private String activatorName;

	public RailSpeedCollectionManager(SegmentPiece element,
	                                  SegmentController segController, RailSpeedElementManager em) {
		super(element, Element.TYPE_ALL, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<RailSpeedUnit> getType() {
		return RailSpeedUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public RailSpeedUnit getInstance() {
		return new RailSpeedUnit();
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState())
					.getWorldDrawer().getGuiDrawer().managerChanged(this);
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return Lng.str("Rail Speed System");
	}

	private final SegmentPiece p = new SegmentPiece();
	private boolean checkComplete;
	
	
	public boolean checkAllConnections() {
		if(checkComplete){
			return true;
		}
		long cPos = ElementCollection.getIndex(getControllerPos());
		FastCopyLongOpenHashSet mm = getSegmentController().getControlElementMap().getControllingMap().getAll().get(cPos);
		
//		if(mm == null || mm.size() == 0){
//			//don't check empty
//			checkComplete = true;
//		}
		int tracks = 0;
		LongArrayList irregular = new LongArrayList();
		if(mm != null){
			for(long l : mm){
				SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(l, p);
				if(pointUnsave == null){
					return false;
				}
				if(ElementKeyMap.isValidType(pointUnsave.getType()) && ElementKeyMap.getInfoFast(pointUnsave.getType()).isRailTrack()){
					tracks++;
				}else{
					if(!ElementKeyMap.isValidType(pointUnsave.getType()) || 
							(!ElementKeyMap.getInfoFast(pointUnsave.getType()).isSignal()
							&& pointUnsave.getType() != ElementKeyMap.STASH_ELEMENT)){
						irregular.add(l);
					}
				}
			}
			for(long l : irregular){
				SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(l, p);
				try {
					throw new Exception("WARNING: Irregular block connected to rail speed: "+pointUnsave+"; Removing Connection");
				} catch (Exception e) {
					e.printStackTrace();
				}
				getSegmentController().getControlElementMap().removeControllerForElement
				(cPos, ElementCollection.getPosIndexFrom4(l), (short) ElementCollection.getType(l));
			}
		}
		
		int compare = 0;
		for(RailSpeedUnit a : getElementCollections()){
			compare += a.size();
		}
		
		if(compare == tracks){
			checkComplete = true;
			return true;
		}else{
			
			if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
				System.err.println("RAILSPEED: COMP: "+compare+" / "+tracks);
				for(RailSpeedUnit a : getElementCollections()){
					for(long l : a.getNeighboringCollection()){
						SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(l, p);
						if(pointUnsave == null){
							System.err.println("RAILSPEED: POINT NOT LOADED: "+ElementCollection.getPosFromIndex(l, new Vector3i()));
						}
						System.err.println("RAILSPEED: "+getControllerPos()+" COLLECTION POINT "+pointUnsave);
						SegmentPiece.debugDrawPoint(getControllerPos(),
								getSegmentController().getWorldTransform(), 
								0.1f, 
								1.0f, 1.0f, 0.3f, 1.0f, 
								2000L);
						pointUnsave.debugDrawPoint(
								getSegmentController().getWorldTransform(), 
								0.1f, 
								1.0f, 0.1f, 0.3f, 1.0f, 
								2000L);
					}
				}
			}
		}
		
		
		return false;
	}

}
