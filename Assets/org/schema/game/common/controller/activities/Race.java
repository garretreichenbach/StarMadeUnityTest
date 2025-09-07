package org.schema.game.common.controller.activities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.RaceManagerState;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.activities.RaceModification.RacemodType;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.racegate.RacegateCollectionManager;
import org.schema.game.common.controller.elements.racegate.RacegateElementManager;
import org.schema.game.common.controller.elements.racegate.RacegateUnit;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.FTLConnection;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Race implements SerializationInterface{

	public long raceStart;
	private long startRaceController;
	private RaceDestination startGate;
	
	public class RaceState implements SerializationInterface, Comparable<RaceState>, Comparator<RaceState>{
		public int entrant;
		public long timeOfLastGate;
		public boolean forefeit = false;
		public int currentGate = -1;
		public int currentRank;
		public int lap;
		public String name;
		public Vector3f localDist = new Vector3f();
		public Transform totalDistance = new Transform();
		private Vector3f currentPos = new Vector3f();
		private Vector3f a = new Vector3f();
		private Vector3f b = new Vector3f();
		public boolean paid;
		@Override
		public void serialize(DataOutput b, boolean isOnServer)
				throws IOException {
			b.writeInt(entrant);
			b.writeLong(timeOfLastGate);
			b.writeBoolean(forefeit);
			b.writeInt(currentGate);
			b.writeUTF(name);
		}
		@Override
		public void deserialize(DataInput b, int updateSenderStateId,
				boolean isOnServer) throws IOException {
			entrant = b.readInt();
			timeOfLastGate = b.readLong();
			forefeit = b.readBoolean();
			currentGate = b.readInt();
			name = b.readUTF();
			
		}
		public long getFinishedTime() {
			return (currentGate == raceList.size() -1) ? timeOfLastGate : 0;
		}
		@Override
		public int compareTo(RaceState o) {
			int g = currentGate - o.currentGate;
			if(g == 0){
				if(currentGate == raceList.size()-1){
					return timeOfLastGate > o.timeOfLastGate ? 1 : (timeOfLastGate < o.timeOfLastGate ? -1 : 0);
				}else{
					
					a.set(totalDistance.origin);
					b.set(o.totalDistance.origin);
					a.sub(currentPos);
					b.sub(o.currentPos);
					
					return Float.compare(b.lengthSquared(), a.lengthSquared());
				}
			}else{
				return g;
			}
		}
		
		@Override
		public int compare(RaceState o1, RaceState o2) {
			
			return o1.compareTo(o2);
		}
		public boolean isFinished() {
			return getFinishedTime() > 0;
		}
		public boolean isActive() {
			return getFinishedTime() == 0 && !forefeit;
		}
	}
	
	
	final List<RaceState> states = new ObjectArrayList<RaceState>();
	
	
	
	public Race(){
	}
	
	private List<RaceDestination> raceList = new ObjectArrayList<RaceDestination>();

	private boolean finished;

	public boolean changed;

	
	private int placement = 0;
	public int id;
	public String name = "unfdefined";
	public Vector3i startSector = new Vector3i();
	private Vector3i tmpSysPos = new Vector3i();
	public String controlBlockUID;
	private int buyIn;
	private int pot;
	private String creatorName;
	
	
	public void createOnClient(RacegateCollectionManager start, String name) throws SQLException, EntityNotFountException{
		
	}
	
	public void create(RacegateCollectionManager start, String name, String creatorName, int laps, int buyIn) throws SQLException, EntityNotFountException{
		
		this.creatorName = creatorName;
		this.startGate = new RaceDestination();
		this.startRaceController = start.getControllerElement().getAbsoluteIndex();
		
		this.controlBlockUID = start.getSegmentController().getUniqueIdentifier();
		
		RaceDestination s = new RaceDestination();
		s.local.set(start.getControllerPos());
		s.uid = DatabaseEntry.removePrefixWOException(start.getSegmentController().getUniqueIdentifier());
		s.sector = new Vector3i(((GameServerState)start.getState()).getUniverse().getSector(start.getSegmentController().getSectorId()).pos);
		s.uid_full = start.getSegmentController().getUniqueIdentifier();
		startSector.set(s.sector);
		this.name = name;
		raceList.add(s);
		startGate = s;
		this.buyIn = buyIn;
		System.err.println("SERVER: CREATING RACE: LAPS: "+laps);
		int lastLap = 0;
		while(!s.uid.equals("none") && laps > 0){
			
			Vector3i sector;
			if(start.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(s.uid_full)){
				Sendable sendable = start.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(s.uid_full);
				sector = new Vector3i(((GameServerState)start.getState()).getUniverse().getSector(((SimpleTransformableSendableObject)sendable).getSectorId()).pos);
			}else{
				sector = ((GameServerState)start.getState()).getDatabaseIndex().getTableManager().getSectorTable().getSector(DatabaseEntry.removePrefixWOException(s.uid), new Vector3i());
			}
				
			FTLConnection ftl = ((GameServerState)start.getState()).getDatabaseIndex().getTableManager().getFTLTable().getFtl(sector, s.local, DatabaseEntry.removePrefixWOException(s.uid));
			if(ftl != null){
				
				String fullToUid = EntityType.SPACE_STATION.dbPrefix+ftl.toUID;
				if(!start.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(fullToUid)){
					fullToUid = EntityType.PLANET_SEGMENT.dbPrefix+ftl.toUID;
				}
				if(start.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().containsKey(fullToUid)){
					//found loaded
					SpaceStation sendable = (SpaceStation)start.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(fullToUid);
					s = new RaceDestination();
					s.sector = ftl.to.get(0);
					s.local.set(ftl.toLoc.get(0));
					s.uid = ftl.toUID;
					s.uid_full = fullToUid;
					System.err.println("[RACE] GOAL (loaded) ADDED TO RACE: "+s+"; start: "+startGate);
					raceList.add(s);
					for(int i = raceList.size()-2; i >= lastLap; i--){
						if(s.equals(raceList.get(i))){
							lastLap = raceList.size()-1;
							laps--;
							System.err.println("LAP: "+laps);
							break;
						}
					}
					
				}else{
					//check in db
					List<DatabaseEntry> byUIDExact = ((GameServerState)start.getState()).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(ftl.toUID, 1);
					
					if(byUIDExact.size() > 0){
						s = new RaceDestination();
						s.sector = ftl.to.get(0);
						s.local.set(ftl.toLoc.get(0));
						s.uid = ftl.toUID;
						s.uid_full = byUIDExact.get(0).getEntityType().dbPrefix+ftl.toUID;
						raceList.add(s);
						System.err.println("[RACE] GOAL (db) ADDED TO RACE: "+s+"; start: "+startGate);
						for(int i = raceList.size()-2; i >= lastLap; i--){
							if(s.equals(raceList.get(i))){
								lastLap = raceList.size()-1;
								laps--;
								System.err.println("LAP: "+laps);
								break;
							}
						}
						
					}else{
						break;
					}
				}
			}else{
				break;
			}
		}
		
		
		
		System.err.println("[RACE] created new race: "+raceList);
	}
	public void start(){
		this.raceStart = System.currentTimeMillis();
		
	}
	public void enter(AbstractOwnerState p){
		RaceState rs = new RaceState();
		
		rs.entrant = p.getId();
		rs.timeOfLastGate = System.currentTimeMillis();
		rs.currentGate = -1;
		rs.name = p.getName();
		this.pot += buyIn;
		if(p instanceof PlayerState){
			((PlayerState)p).modCreditsServer(-buyIn);
		}
		states.add(rs);
	}
	
	private void calculateDistances(StateInterface state) {
		for(RaceState r : states){
//			System.err.println(state+" Finished: "+r.getFinishedTime()+"; "+r.isActive());
			if(r.currentGate < raceList.size()-1){
				Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(r.entrant);
				if(s != null && s instanceof PlayerState){
					PlayerState p = (PlayerState)s;
					
					
					RaceDestination destination = raceList.get(r.currentGate+1);
					Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(destination.uid_full);
					if(sendable != null && sendable instanceof SpaceStation){
						ManagerModuleCollection<RacegateUnit, RacegateCollectionManager, RacegateElementManager> rg = ((SpaceStation)sendable).getManagerContainer().getRacegate();
						RacegateCollectionManager rm = rg.getCollectionManagersMap().get(ElementCollection.getIndex(destination.local));
						if(rm != null){
							if( !rm.getElementCollections().isEmpty() && rm.getElementCollections().get(0).size() > 0 && rm.getElementCollections().get(0).getSignificator() != Long.MIN_VALUE){
								RacegateUnit racegateUnit = rm.getElementCollections().get(0);
								
								Vector3i min = racegateUnit.getMin(new Vector3i());
								Vector3i max = racegateUnit.getMax(new Vector3i());
								
								min.add((max.x-min.x)/2 - SegmentData.SEG_HALF, (max.y-min.y)/2 - SegmentData.SEG_HALF, (max.z-min.z)/2 - SegmentData.SEG_HALF);
								
								rm.getSegmentController().getAbsoluteElementWorldPositionLocal(min, r.localDist);
//								System.err.println("USING WT+GATEUNIT: "+r.localDist+"; "+racegateUnit.getMin(new Vector3i())+"; "+racegateUnit.getMax(new Vector3i())+"; ");
							}else{
								((SpaceStation)sendable).getAbsoluteElementWorldPositionLocalShifted(rm.getControllerElement().getAbsolutePos(new Vector3i()), r.localDist);
//								System.err.println("USING WT+GATECOMP: "+r.localDist);
							}
						}else{
							r.localDist.set(((SpaceStation)sendable).getWorldTransform().origin);
//							System.err.println("USING WT: "+r.localDist);
						}
					}else{
						r.localDist.set(0,0,0);
					}
					
					r.totalDistance.setIdentity();
					
					
					calcWaypointSecPos(destination.sector, r.totalDistance, p.getCurrentSector(), p.getCurrentSectorId(), state, tmpSysPos);
					
					SimpleTransformableSendableObject fc = p.getFirstControlledTransformableWOExc();
					if(fc != null){
						r.currentPos.set(fc.getWorldTransform().origin);
					}else{
						r.currentPos.set(0,0,0);
					}
//					System.err.println("LOCAL::: "+r.localDist+"; "+" ::: "+r.totalDistance.origin.length()+"; ");
					r.totalDistance.origin.add(r.localDist);
				}else{
					r.currentPos.set(0,0,0);
				}
			}
		}
	}
	public static void calcWaypointSecPos(Vector3i absSec, Transform out, Vector3i secFrom, int secFromId, StateInterface ss, Vector3i tmpSysPos) {

		Vector3i sysPos = StellarSystem.getPosFromSector(absSec, tmpSysPos);

		Vector3i pPos = new Vector3i(absSec);
		GameStateInterface state = ((GameStateInterface) ss);
		pPos.sub(secFrom);
//		System.err.println("PPOS: "+secFrom+" -> "+absSec+" = "+pPos);
		out.setIdentity();
		float year = state.getGameState().getRotationProgession();

		Vector3f otherSecCenter = new Vector3f(
				pPos.x * state.getSectorSize(),
				pPos.y * state.getSectorSize(),
				pPos.z * state.getSectorSize());

		Matrix3f rot = new Matrix3f();
		rot.rotX((FastMath.PI * 2) * year);

		Sendable sendable = ss.getLocalAndRemoteObjectContainer().getLocalObjects().get(secFromId);
		if (sendable != null && sendable instanceof RemoteSector) {
			RemoteSector sec = (RemoteSector) sendable;

			if (sec.getType() == SectorType.PLANET) {
				//we are next to a planet sector
				//-> rotate planet sector
				rot.invert();
				Vector3f bb = new Vector3f();
				bb.add(otherSecCenter);
				TransformTools.rotateAroundPoint(bb, rot, out, new Transform());
				out.origin.add(otherSecCenter);

				return;
			}
		}
		out.origin.set(otherSecCenter);

	}
	private void calculateRanks(StateInterface state) {
//		Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(r.entrant);
//		if(s != null && s instanceof PlayerState){
//			
//		}
		int highestGate = 0;
		for(RaceState r : states){
			highestGate = Math.max(r.currentGate, highestGate);
		}
		
		Collections.sort(states);
		
		for(int i = 0; i < states.size(); i++){
			states.get(i).currentRank = states.size() - i;
		}
	}

	public void updateClient(Timer timer, StateInterface state) {
		calculateDistances(state);
		calculateRanks(state);
		
	}

	public void updateServer(Timer timer, StateInterface state) {
		
		if(isStarted()){
		
			for(RaceState r : states){
				if(!state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(r.entrant)){
					r.forefeit = true;
					((RaceManagerState)state).getRaceManager().requestForefitOnServer(r.entrant, this);
				}
			}
			if(getActiveAndFinishedRacerCount() > 0){
				calculateDistances(state);
				calculateRanks(state);
			}else{
				finished = true;
			}
		}
		
	}
	public int getActiveAndFinishedRacerCount(){
		int act = 0;
		for(int i = 0; i < states.size(); i++){
			if(!states.get(i).forefeit){
				act ++;
			}
		}
		return act;
	}
	public int getActiveRacerCount(){
		int act = 0;
		for(int i = 0; i < states.size(); i++){
			if(!states.get(i).forefeit && !states.get(i).isFinished()){
				act ++;
			}
		}
		return act;
	}
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeInt(id);
		b.writeLong(raceStart);
		b.writeUTF(controlBlockUID);
		b.writeLong(this.startRaceController);

		b.writeUTF(name);
		b.writeUTF(creatorName);
		b.writeInt(this.buyIn);
		b.writeInt(startSector.x);
		b.writeInt(startSector.y);
		b.writeInt(startSector.z);
		
		
		b.writeShort(raceList.size());
		for(int i = 0; i < raceList.size(); i++){
			RaceDestination rd = raceList.get(i);
			b.writeUTF(rd.uid);
			b.writeUTF(rd.uid_full);
			rd.local.serialize(b);
			rd.sector.serialize(b);
		}
		
		b.writeShort(states.size());
		
		for(int i = 0; i < states.size(); i++){
			states.get(i).serialize(b, isOnServer);
		}
		
		
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		id = b.readInt();
		raceStart = b.readLong();
		controlBlockUID = b.readUTF();
		
		this.startRaceController = b.readLong();
		
		name = b.readUTF();
		creatorName = b.readUTF();
		this.buyIn = b.readInt();
		
		startSector.set(b.readInt(), b.readInt(), b.readInt());
		
		int countRaceDest = b.readShort();
		for(int i = 0; i < countRaceDest; i++){
			RaceDestination r = new RaceDestination();
			
			r.uid = b.readUTF();
			r.uid_full = b.readUTF();
			r.local.deserialize(b);
			r.sector.deserialize(b);
			
			
			raceList.add(r);
		}
		
		int countStates = b.readShort();
		
		for(int i = 0; i < countStates; i++){
			RaceState r = new RaceState();
			r.deserialize(b, updateSenderStateId, isOnServer);
			states.add(r);
		}
	}
	public void forefeit(AbstractOwnerState p) {
		for(RaceState s : states){
			if(s.entrant == p.getId()){
				s.forefeit = true;
				return;
			}
		}
	}
	public void changeGate(AbstractOwnerState p, int gate, long timeAtGate) {
		for(RaceState s : states){
			if(s.entrant == p.getId()){
				s.currentGate = gate;
				s.timeOfLastGate = timeAtGate;
				return;
			}
		}
	}
	public void leave(AbstractOwnerState p) {
		for(int i = 0;i < states.size(); i++){
			RaceState s = states.get(i);
			if(s.entrant == p.getId()){
				states.remove(i);
				this.pot -= buyIn;
				if(p instanceof PlayerState){
					((PlayerState)p).modCreditsServer(buyIn);
				}
				return;
			}
		}
	}
	public long getStartRaceController() {
		return startRaceController;
	}
	public void setStartRaceController(long startRaceController) {
		this.startRaceController = startRaceController;
	}
	public void broadcastAll(Object[] astr, StateInterface state) {
		for(RaceState r : states){
			Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(r.entrant);
			if(s != null && s instanceof PlayerState){
				((PlayerState)s).sendServerMessagePlayerInfo(astr);
			}
		}
	}
	public boolean isParticipantActive(AbstractOwnerState player) {
		for(RaceState r : states){
			if(!r.isFinished() && !r.forefeit && r.entrant == player.getId()){
				return true;
			}
		}
		return false;
	}
	public boolean isParticipant(AbstractOwnerState player) {
		for(RaceState r : states){
			if(!r.isFinished() && r.entrant == player.getId()){
				return true;
			}
		}
		return false;
	}
	public int getRacerCount() {
		return states.size();
	}
	
	public boolean isStarted(){
		return raceStart > 0;
	}
	public Collection<RaceState> getEntrants() {
		return states;
	}
	public boolean canEdit(PlayerState player) {
		Sendable sendable = player.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(controlBlockUID);
		if(sendable != null && sendable instanceof SpaceStation){
			SpaceStation s = (SpaceStation)sendable;
			if(s.allowedToEdit(player)){
				return true;
			}
		}
		return player.getNetworkObject().isAdminClient.getBoolean() || (isStarted() && getActiveRacerCount() == 0) || player.getName().equals(creatorName);
	}

	public void onPassGate(AbstractOwnerState ap,
			RacegateCollectionManager rc,
			RaceManager raceManager) {
		assert(rc.getSegmentController().isOnServer());
		
		for(RaceState r : states){
			if(isStarted() && r.entrant == ap.getId() && !r.forefeit){
				
				System.err.println("RACEGATE PASSED: "+ap);
				if(r.currentGate < raceList.size()-2){
					
					RaceDestination destination = raceList.get(r.currentGate+1);
					if(destination.uid_full.equals(rc.getSegmentController().getUniqueIdentifier()) && rc.getControllerPos().equals(destination.local)){
						
						r.currentGate++;
						r.timeOfLastGate = System.currentTimeMillis();
						
						if(ap instanceof PlayerState ){
							if(r.currentGate > 0 && rc.getSegmentController().getUniqueIdentifier().equals(startGate.uid_full) &&
									rc.getControllerElement().getAbsoluteIndex() == startRaceController){
								r.lap++;
								((PlayerState)ap).sendServerMessagePlayerInfo(Lng.astr("Lap %s\nPassed Gate %s of %s",  (r.lap+1),  (r.currentGate),  (raceList.size()-1)));
							}else{
								((PlayerState)ap).sendServerMessagePlayerInfo(Lng.astr("Passed Gate %s of %s",  (r.currentGate),  (raceList.size()-1)));
							}
						}
					}
					
					
					
					RaceModification rm = new RaceModification();
					rm.raceId = id;
					rm.entrantId = ap.getId();
					rm.gate = r.currentGate;
					rm.timeAtGate = r.timeOfLastGate;
					rm.type = RacemodType.TYPE_ENTRANT_GATE;
					raceManager.sendMod(rm);
					
				}else if(r.currentGate == raceList.size() - 2){
					if(!r.paid){
						if(ap instanceof PlayerState){
							PlayerState player = (PlayerState)ap;
							int winnings = 0;
							if(placement == 0){
								if(states.size() >= 3){
									winnings = ((int)(pot * 0.6));
									
								}else if(states.size() >= 2){
									winnings = ((int)(pot * 0.8));
								}else{
									winnings = ((pot));
								}
							}else if(placement == 1){
								if(states.size() >= 3){
									winnings = ((int)(pot * 0.3));
								}else if(states.size() >= 2){
									winnings = ((int)(pot * 0.2));
								}
							}else if(placement == 2){
								winnings = ((int)(pot * 0.1));
								
							}
							player.modCreditsServer(winnings);
							if(winnings > 0){
								player.sendServerMessagePlayerInfo(Lng.astr("Congratulations!\nYou finished on rank %s!\nYou won %s credits!", r.currentRank,  winnings));
							}else{
								player.sendServerMessagePlayerInfo(Lng.astr("You finished the race on rank %s!", r.currentRank));
							}
						}
						r.paid = true;
					}
					placement++;
					
					r.currentGate++;
					RaceModification rm = new RaceModification();
					rm.raceId = id;
					rm.entrantId = ap.getId();
					rm.gate = r.currentGate;
					rm.timeAtGate = r.timeOfLastGate;
					rm.type = RacemodType.TYPE_ENTRANT_GATE;
					raceManager.sendMod(rm);
				}
				
			}
		}
	}

	public RaceState getRaceState(AbstractOwnerState player) {
		for(RaceState r : states){
			if(r.entrant == player.getId()){
				return r;
			}
		}
		return null;
	}

	public int getTotalGates() {
		return raceList.size();
	}

	public int getBuyIn() {
		return buyIn;
	}

	public void setBuyIn(int buyIn) {
		this.buyIn = buyIn;
	}
	
}
