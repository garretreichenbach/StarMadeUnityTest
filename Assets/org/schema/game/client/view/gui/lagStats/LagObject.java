package org.schema.game.client.view.gui.lagStats;

import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.objects.Sendable;

public class LagObject {

	public final Sendable s;
	private long lagTime;
	public LagObject(Sendable s) {
		this.s = s;

		this.lagTime = s.getCurrentLag();
	}
	@Override
	public int hashCode() {
		return s.hashCode()* (int)lagTime;
	}
	@Override
	public boolean equals(Object obj) {
		LagObject o = (LagObject)obj;
		return s == o.s && lagTime == o.lagTime;
	}
	public long getLagTime(){
		return lagTime;//s.getCurrentLag();
	}
	
	public String getType(){
		return  s instanceof RemoteSector ? "SECTOR" : 
			(s instanceof SimpleTransformableSendableObject ? ((SimpleTransformableSendableObject)s).getTypeString() : "OTHER");
	}
	public String getSector(){
		String sector;
		if(s instanceof RemoteSector){
			sector = ((RemoteSector)s).clientPos().toStringPure();
		}else if(s instanceof AbstractOwnerState){
			sector = ((PlayerState)s).getCurrentSector().toStringPure();
		}else if(s instanceof SimpleTransformableSendableObject){
			synchronized(s.getState()){
				s.getState().setSynched();
				Sendable sendable = s.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(((SimpleTransformableSendableObject) s).getSectorId());
				
				if(sendable != null && sendable instanceof RemoteSector){
					sector = ((RemoteSector)sendable).clientPos().toStringPure();
				}else{
					sector = "unloadedSector("+((SimpleTransformableSendableObject) s).getSectorId()+")";
				}
				s.getState().setUnsynched();
			}
			
		}else{
			sector = "unknown";
		}
		return sector;
	}
	public String getName(){
		return s instanceof RemoteSector ? "SectorTotal" : s.toString();
	}
	

}
