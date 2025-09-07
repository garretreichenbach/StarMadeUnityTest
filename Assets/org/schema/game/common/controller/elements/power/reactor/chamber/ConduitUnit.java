package org.schema.game.common.controller.elements.power.reactor.chamber;

import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.MainReactorUnit;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ConduitUnit extends ElementCollection<ConduitUnit, ConduitCollectionManager, ConduitElementManager> {

	private static final int BULK = 32;
	private LongListIterator currentIterator;
	private SegmentPiece tmp = new SegmentPiece();
	private final ObjectOpenHashSet<ReactorChamberUnit> connectedTmp = new ObjectOpenHashSet<ReactorChamberUnit>();
	private final ObjectOpenHashSet<ReactorChamberUnit> connected = new ObjectOpenHashSet<ReactorChamberUnit>();
	private final ObjectOpenHashSet<MainReactorUnit> connectedMainTmp = new ObjectOpenHashSet<MainReactorUnit>();
	private final ObjectOpenHashSet<MainReactorUnit> connectedMain = new ObjectOpenHashSet<MainReactorUnit>();
	private Vector3i tmpPos = new Vector3i();
	private boolean dirty;
	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		Vector3i dim = new Vector3i();
		dim.sub(getMax(new Vector3i()), getMin(new Vector3i()));
		return ControllerManagerGUI.create(state, Lng.str("Conduit Module"), this,
				new ModuleValueEntry(Lng.str("Dimension"), dim),
				new ModuleValueEntry(Lng.str("Recharge"), "N/A"));
	}

	@Override
	public boolean onChangeFinished() {
		flagCalcConnections();
		return super.onChangeFinished();
	}
	
	public void flagCalcConnections(){
		connectedTmp.clear();
		connectedMainTmp.clear();
		currentIterator = getNeighboringCollection().iterator();
		elementCollectionManager.flagConduitsDirty();
	}
	public void updateCalcConnections(){
		int c = 0;
		while(c < BULK && !isFinishedCalcConnections()){
			long index = currentIterator.nextLong();
			dirty = true;
			for(int i = 0; i < 6; i++){
				ElementCollection.getPosFromIndex(index, tmpPos);
				tmpPos.add(Element.DIRECTIONSi[i]);
				long nextIndex = ElementCollection.getIndex(tmpPos);
				SegmentPiece p = getSegmentController().getSegmentBuffer().getPointUnsave(nextIndex, tmp);
				if(p != null && ElementKeyMap.isValidType(p.getType())){
					if(ElementKeyMap.getInfoFast(p.getType()).isReactorChamberAny()){
						//found reactor chamber next to conduit
						
						long reactorIndex = p.getAbsoluteIndex();
						ReactorChamberUnit chamber = getPowerInterface().getReactorChamber(reactorIndex);		
						if(chamber != null){
							boolean add = connectedTmp.add(chamber);
						}else{
							
						}
					}else if(p.getType() == ElementKeyMap.REACTOR_MAIN){
						long reactorIndex = p.getAbsoluteIndex();
						MainReactorUnit reactor = getPowerInterface().getReactor(reactorIndex);	
						if(reactor != null){
							boolean add = connectedMainTmp.add(reactor);
						}
					}
					
				}
			}
			
			c++;
		}
		if(isFinishedCalcConnections() && dirty){
			connected.clear();
			connected.addAll(connectedTmp);
			connectedTmp.clear();
			connectedMain.clear();
			connectedMain.addAll(connectedMainTmp);
			connectedMainTmp.clear();
//			System.err.println("FINISHED CONDUITS: Main: "+connectedMain.size()+"; Chambers: "+connected.size());
			dirty = false;
		}
	}
	
	public boolean isValidConduit(){
		boolean valid = (connectedMain.size() == 0 && connected.size() == 2) || (connectedMain.size() == 1 && connected.size() >= 1);
		
		return valid;
	}
	public String getInvalidConduit(){
		if(!isValidConduit()){
			return Lng.str("Conduit must connect 'one reactor <-> one chamber' or 'one chamber <-> one chamber'");
		}else{
			return "";
		}
	}
	public boolean isFinishedCalcConnections(){
		return !currentIterator.hasNext();
	}

	public Set<ReactorChamberUnit> getConnectedChambers() {
		return connected;
	}
	public Set<MainReactorUnit> getConnectedReactors() {
		return connectedMain;
	}
}
