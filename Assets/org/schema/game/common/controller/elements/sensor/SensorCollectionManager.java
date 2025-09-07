package org.schema.game.common.controller.elements.sensor;

import api.element.block.Blocks;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerModule;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

public class SensorCollectionManager extends ControlBlockElementCollectionManager<SensorUnit, SensorCollectionManager, SensorElementManager> {

	public SensorCollectionManager(SegmentPiece element,
	                                SegmentController segController, SensorElementManager em) {
		super(element, ElementKeyMap.ACTIVAION_BLOCK_ID, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<SensorUnit> getType() {
		return SensorUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	public void update(Timer timer) {
		super.update(timer);
	}
	private final SegmentPiece tmp0 = new SegmentPiece();
	private final SegmentPiece tmp1 = new SegmentPiece();
	public void check(){
		if(!getSegmentController().isOnServer()){
			return;
		}
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> con = getSegmentController().getControlElementMap().getControllingMap().get(ElementCollection.getIndex(getControllerPos()));
		
		if(con != null){
			long probed = Long.MIN_VALUE;
			int active = 0;
			int total = 0;
			
			long probedDisplayA = Long.MIN_VALUE;
			long probedDisplayB = Long.MIN_VALUE;
			for(Entry<Short, FastCopyLongOpenHashSet> e : con.entrySet()){
				if(e.getKey() == ElementKeyMap.ACTIVAION_BLOCK_ID){
					if(!e.getValue().isEmpty()){
						total = e.getValue().size();
						
						for(long l : e.getValue()){
							
							SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer()
									.getPointUnsave(ElementCollection.getPosIndexFrom4(l), tmp0);
							if(pointUnsave == null){
								return;
							}else{
								if(pointUnsave.isActive()){
									active++;
								}
							}
						}
					}
				}else if(ElementKeyMap.isTextBox(e.getKey())){
					for(long l : e.getValue()){
						if(probedDisplayA == Long.MIN_VALUE){
							probedDisplayA = l;
						}else if(probedDisplayB == Long.MIN_VALUE){
							probedDisplayB = l;
						}
					}
					
				}else if(probed == Long.MIN_VALUE && e.getValue().size() > 0){
					probed = e.getValue().iterator().nextLong();
				}
			}
			float fill = 1;
			
			if(total > 0){
				fill = (float)active / (float)total;
			}
			if(probedDisplayA != Long.MIN_VALUE && probedDisplayB != Long.MIN_VALUE){
				SegmentPiece disA = getSegmentController().getSegmentBuffer().getPointUnsave(probedDisplayA, tmp0);
				SegmentPiece disB = getSegmentController().getSegmentBuffer().getPointUnsave(probedDisplayB, tmp1);
				if(disA != null && disB != null){
					String texA = getSegmentController().getTextMap().get(disA.getTextBlockIndex());
					String texB = getSegmentController().getTextMap().get(disB.getTextBlockIndex());
					float sensorValue;
					if(texA == null && texB == null){
						sensorValue = 1;
					}else if(texA == null || texB == null){
						sensorValue = 0;
					}else{
						
						List<String> tokens = StringTools.tokenize(texA, "[", "]");
						for (String s : tokens) {
							if(s.toLowerCase(Locale.ENGLISH).equals("password")){
								texA = texA.replaceAll("\\["+s+"\\]", "");
								break;
							}
						}
						tokens = StringTools.tokenize(texB, "[", "]");
						for (String s : tokens) {
							if(s.toLowerCase(Locale.ENGLISH).equals("password")){
								texB = texB.replaceAll("\\["+s+"\\]", "");
								break;
							}
						}
						sensorValue = texA.equals(texB) ? 1 : 0;
					}
					if(sensorValue >= fill){
						sendSignal(true);
					}else{
						sendSignal(false);
					}
				}
				
			}else if(probed != Long.MIN_VALUE ){
				
				SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(probed, tmp0);
				if(pointUnsave != null && ElementKeyMap.isValidType(pointUnsave.getType())){
					
					
					short type = pointUnsave.getType();
					
					if(ElementKeyMap.getInfoFast(type).isDoor()){
						type = ElementKeyMap.DOOR_ELEMENT;
					}
					ManagerModule<?, ?, ?> module = null;
					
					if(ElementKeyMap.getInfoFast(type).isInventory()){
						if(type == ElementKeyMap.STASH_ELEMENT) module = getElementManager().getManagerContainer().getCargo();
						else if(type == Blocks.LOCK_BOX.getId()) module = getElementManager().getManagerContainer().getLockBox();
					}else{
						module = getContainer().getModulesMap().get(type);
					}
					if(module != null && module instanceof ManagerModuleSingle<?,?,?>){
						float sensorValue = ((ManagerModuleSingle<?,?,?>)module).getCollectionManager().getSensorValue(pointUnsave);
						
						
						if(sensorValue >= fill){
							sendSignal(true);
						}else{
							sendSignal(false);
						}
return;
					}else if(module != null){
						
//						while (module.getNext() != null) {
							assert (module instanceof ManagerModuleCollection<?, ?, ?>);
							ManagerModuleCollection<?, ?, ?> g = (ManagerModuleCollection<?, ?, ?>) module;
							
							if (g.getControllerID() == pointUnsave.getType() || ElementKeyMap.getInfoFast(type).isInventory()) {
								
								ControlBlockElementCollectionManager<?,?,?> cc = g.getCollectionManagersMap().get(pointUnsave.getAbsoluteIndex());
								if(cc != null){
									float sensorValue = cc.getSensorValue(pointUnsave);
									
									if(sensorValue >= fill){
										sendSignal(true);
//										System.err.println("[SensorCollectionManager] SENSOR C TRUE: "+getControllerPos()+"; "+lastSensorValue+" -> "+sensorValue+"; (sensed: "+cc+")");
									}else{
										
										sendSignal(false);
									}
								}
								return;
							}
//						}
					}
					
				}
			}
			
		}
	}
	
	private void sendSignal(boolean act) {
		assert(getSegmentController().isOnServer());
		((SendableSegmentController)getSegmentController()).activateSurroundServer(act, getControllerPos(), ElementKeyMap.getSignalTypesActivatedOnSurround());
	}

	@Override
	public CollectionShape requiredNeigborsPerBlock() {
		return CollectionShape.ALL_IN_ONE;
	}
	@Override
	public SensorUnit getInstance() {
		return new SensorUnit();
	}

	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return Lng.str("Sensor System");
	}

}
