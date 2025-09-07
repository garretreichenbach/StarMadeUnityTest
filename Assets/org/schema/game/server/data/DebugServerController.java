package org.schema.game.server.data;

import java.sql.SQLException;
import java.util.List;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class DebugServerController {

	private GameServerState state;

	private Long2ObjectOpenHashMap<RailSave> railSaves = new Long2ObjectOpenHashMap<RailSave>();
	private ObjectOpenHashSet<String> railSaveUID = new ObjectOpenHashSet<String>();
	
	private class RailSave{
		private List<RailSave> childs = new ObjectArrayList<RailSave>();
		public final String UID;
		public long dbId;
		
		public RailSave(String UID, long dbId){
			this.UID = UID;
			this.dbId = dbId;
		}

		@Override
		public String toString() {
			return "RailSave [UID=" + UID + ", dbId=" + dbId + ", "+getStatus()+"]";
		}
		
		public String getStatus(){
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(dbId);
			
			if(sendable != null){
				SegmentController c = (SegmentController)sendable;
				return "###LOADED SECID: "+c.getSectorId()+"; Sector: "+state.getUniverse().getSector(c.getSectorId());
			}else{
				DatabaseEntry byId = null;
				try {
					byId = state.getDatabaseIndex().getTableManager().getEntityTable().getById(dbId);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if(byId != null){
					return "UNLOADED ("+byId.sectorPos.toStringPure()+")";
				}else{
					return "!!!!NOT IN DATABASE!!!!";
				}
			}
		}
		
	}
	
	public DebugServerController(GameServerState state) {
		this.state = state;
	}

	private RailSave getRailSaveRec(SegmentController c) {
		RailSave s = new RailSave(c.getUniqueIdentifier(), c.dbId);
		
		for(RailRelation r : c.railController.next){
			s.childs.add(getRailSaveRec(r.docked.getSegmentController()));
		}
		return s;
	}
	
	public void check(SegmentController c){
		if(railSaves.containsKey(c.dbId)){
			
			if(!railSaveUID.contains(c.getUniqueIdentifier())){
				throw new RuntimeException("ENTITY "+c+" DIDN'T HAVE UID: "+c.dbId+"; "+c.getUniqueIdentifier()+"; "+railSaveUID);
			}
			
			RailSave railSave = railSaves.get(c.dbId);
			
			if(!railSave.UID.equals(c.getUniqueIdentifier())){
				throw new RuntimeException("ENTITY "+c+" DIDN'T HAS DIFFERENT UID: "+c.dbId+"; "+c.getUniqueIdentifier()+" != "+railSave.UID);
			}
			
			if(c.isFullyLoadedWithDock()){
				boolean ok = checkRailSaveWith(c, railSave);
				if(!ok){
					throw new RuntimeException("ENTITY "+c+" LOST DOCKS: NOW: "+c.railController.next+"; SHOULD BE: "+railSave.childs);
				}
			}
		}
	}
	
	private boolean checkRailSaveWith(SegmentController c, RailSave sa) {
		
		if(c.railController.next.size() != sa.childs.size()){
			throw new RuntimeException("ENTITY "+c+" LOST DOCKS (BY CONTROLLER): NOW: "+c.railController.next+"; SHOULD BE: "+sa.childs);
		}
		boolean ok = checkRailChildrenByController(c, sa);
		ok = checkRailChildrenBySave(c, sa) && ok;
		return ok;
		
	}

	private boolean checkRailChildrenBySave(SegmentController xc, RailSave sa) {
		if(sa.childs.isEmpty()){
			return true;
		}
		boolean ok = false;
		
		for(RailSave s : sa.childs){
			
			for(RailRelation r : xc.railController.next){
				SegmentController c = r.docked.getSegmentController();
				if(s.dbId == c.dbId || s.UID.equals(c.getUniqueIdentifier())){
					ok = checkRailChildrenBySave(c, s);
				}
			}
			if(ok){
				break;
			}else{
				throw new RuntimeException("ENTITY "+xc+" LOST DOCKS (BY RAILSAVE): SPECIFICALLY: "+s+"; NOT FOUND IN RAIL CHILDREN: "+xc.railController.next+"; ");
			}
		}
		return ok;
	}
	private boolean checkRailChildrenByController(SegmentController xc, RailSave sa) {
		if(xc.railController.next.isEmpty()){
			return true;
		}
		boolean ok = false;
		
		for(RailRelation r : xc.railController.next){
			SegmentController c = r.docked.getSegmentController();
			for(RailSave s : sa.childs){
				if(s.dbId == c.dbId || s.UID.equals(c.getUniqueIdentifier())){
					ok = checkRailChildrenByController(c, s);
				}
			}
			if(ok){
				break;
			}else{
				throw new RuntimeException("ENTITY "+xc+" LOST DOCKS: SPECIFICALLY: "+r.docked.getSegmentController()+"; NOT FOUND IN RAILSAVE CHILDS: "+sa.childs);
			}
		}
		return ok;
	}

	public void saveRail(SegmentController c) {
		
		RailSave s = getRailSaveRec(c);
		
		railSaves.put(c.dbId, s);
		railSaveUID.add(c.getUniqueIdentifier());
		
	}

	public void removeRailSave(long entityDbId) {
		if(railSaves.containsKey(entityDbId)){
			RailSave remove = railSaves.remove(entityDbId);
			boolean r = railSaveUID.remove(remove.UID);
			if(!r){
				throw new RuntimeException("Couldnt remove "+remove+"; not in UID");
			}
		}
	}

}
