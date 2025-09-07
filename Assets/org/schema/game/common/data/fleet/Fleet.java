package org.schema.game.common.data.fleet;

import api.common.GameCommon;
import api.listener.events.fleet.FleetAttackedEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.controller.elements.stealth.StealthAddOn;
import org.schema.game.common.controller.trade.TradeActive;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.world.Sector;
import org.schema.game.network.objects.remote.FleetCommand;
import org.schema.game.network.objects.remote.RemoteFleet;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFleetManager;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemFleetManager;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemFleetManager.FleetType;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class Fleet extends AiEntityState implements SerializationInterface {

	private final List<FleetMember> members = new ObjectArrayList<FleetMember>();
	private final ArrayList<Vector3i> patrolTargets = new ArrayList<>();
	private final HashMap<String, Boolean> savedRemotes = new HashMap<>();
	private final long COMBINED_TARGETING_UPDATE_INTERVAL = 1000L;
	public long dbid;
	public long parentFleet;
	public String missionString = "idle";
	public int patrolIndex;
	public TradeActive activeTradeRoute;
	FleetCommand loadedCommand;
	private String name;
	private String owner;
	private boolean markForCacheCheck;
	private boolean flagRemove;
	private Vector3i moveTarget;
	private byte factionAccessible = 5;
	private boolean npcFleet;
	private boolean npcFleetGeneral;
	private int npcFaction;
	private Vector3i npcSystem;
	private NPCSystemFleetManager.FleetType npcType;
	private DebugMove debug;
	private long lastSec;
	private FleetCommand currentCommand;
	private String combatSetting = "SOMETIMES ENGAGE";
	private boolean combinedTargeting;
	private long lastCombinedTargetUpdate = -1L;

	public Fleet(StateInterface state) {
		super("flt", state);
	}

	public boolean canStealth() {
		for(FleetMember m : members) {
			if(m.getLoaded() instanceof ManagedSegmentController<?> c) {
				if(c.getSegmentController().isUsingPowerReactors()) {
					PlayerUsableInterface playerUsable = c.getManagerContainer().getPlayerUsable(PlayerUsableInterface.USABLE_ID_STEALTH_REACTOR);
					if(playerUsable instanceof StealthAddOn) {
						if(((StealthAddOn) playerUsable).canExecute()) return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isStealth() {
		for(FleetMember m : members) {
			if(m.getLoaded() instanceof ManagedSegmentController<?> c) {
				if(c.getSegmentController().isUsingPowerReactors()) {
					PlayerUsableInterface playerUsable = c.getManagerContainer().getPlayerUsable(PlayerUsableInterface.USABLE_ID_STEALTH_REACTOR);
					if(playerUsable instanceof StealthAddOn) {
						if(((StealthAddOn) playerUsable).isActive()) return true;
					}
				}
			}
		}
		return false;
	}

	public boolean canInterdict() {
		for(FleetMember m : members) {
			if(m.getLoaded() instanceof ManagedSegmentController<?> c) {
				if(c.getSegmentController().isUsingPowerReactors()) {
					PlayerUsableInterface playerUsable = c.getManagerContainer().getPlayerUsable(PlayerUsableInterface.USABLE_ID_INTERDICTION);
					if(playerUsable instanceof InterdictionAddOn) {
						if(((InterdictionAddOn) playerUsable).canExecute()) return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isInterdict() {
		for(FleetMember m : members) {
			if(m.getLoaded() instanceof ManagedSegmentController<?> c) {
				if(c.getSegmentController().isUsingPowerReactors()) {
					PlayerUsableInterface playerUsable = c.getManagerContainer().getPlayerUsable(PlayerUsableInterface.USABLE_ID_INTERDICTION);
					if(playerUsable instanceof InterdictionAddOn) {
						if(((InterdictionAddOn) playerUsable).isActive()) return true;
					}
				}
			}
		}
		return false;
	}

	public void sendOwnerMessageServer(ServerMessage msg) {
		if(isOnServer()) {
			PlayerState pl;
			try {
				pl = ((GameServerState) state).getPlayerFromNameIgnoreCase(owner);
				pl.sendServerMessage(msg);
			} catch(PlayerNotFountException e) {
			}
		} else {
			assert (false);
		}
	}

	@Override
	public void updateOnActive(Timer timer) throws FSMException {
		super.updateOnActive(timer);
		if(isOnServer()) {
			boolean attackMsg = false;
			for(int i = 0; i < members.size(); i++) {
				if(members.get(i).checkAttacked()) {
					attackMsg = true;
					//INSERTED CODE
					FleetAttackedEvent e = new FleetAttackedEvent(this, members.get(i));
					StarLoader.fireEvent(e, true);
					///
				}
			}
			if(attackMsg) {
				for(int i = 0; i < members.size(); i++) {
					if(members.get(i).isLoaded()) {
						members.get(i).setAttackedTriggered(true);
					}
				}
				PlayerState p;
				p = ((GameServerState) state).getPlayerFromNameIgnoreCaseWOException(owner);
				if(p != null) {
					p.sendServerMessagePlayerError(Lng.astr("WARNING!\nYour fleet '%s' is taking damage!", name));
				}
			}

			if(combinedTargeting && System.currentTimeMillis() - lastCombinedTargetUpdate > COMBINED_TARGETING_UPDATE_INTERVAL) {
				lastCombinedTargetUpdate = System.currentTimeMillis();
				if(getFlagShip() != null && getFlagShip().isLoaded() && getFlagShip().isOnServer()) {
					SegmentController flagShip = getFlagShip().getLoaded();
					if(flagShip instanceof ManagedUsableSegmentController<?>) {
						ManagedUsableSegmentController<?> musc = (ManagedUsableSegmentController<?>) flagShip;
						if(musc.isAIControlled() && musc.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof TargetProgram<?>) {
							TargetProgram<?> targetProgram = (TargetProgram<?>) musc.getAiConfiguration().getAiEntityState().getCurrentProgram();
							if(targetProgram.getSpecificTargetId() != -1) setCombinedTargetId(targetProgram.getSpecificTargetId());
						}
					}
				}
			}
		}
	}

	private void setCombinedTargetId(int targetId) {
		System.out.println("[SERVER][FLEET] SETTING COMBINED TARGET ID: " + targetId);
		if(isOnServer()) {
			for(FleetMember member : members) {
				if(member.isLoaded()) {
					SegmentController loaded = member.getLoaded();
					if(loaded instanceof ManagedUsableSegmentController<?>) member.setCombinedTargetId(targetId);
				}
			}
			sendFleet();
		}
	}

	public List<FleetMember> getMembers() {
		return members;
	}

	public FleetManager getFleetManager() {
		return ((FleetStateInterface) state).getFleetManager();
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeBoolean(flagRemove);

		b.writeLong(dbid);
		b.writeUTF(missionString);
		b.writeUTF(name);
		b.writeUTF(owner);


		b.writeBoolean(moveTarget != null);
		if(moveTarget != null) moveTarget.serialize(b);
		b.writeShort(members.size());
		for(int i = 0; i < members.size(); i++) members.get(i).serialize(b, isOnServer);
		b.writeByte(factionAccessible);
		b.writeShort(patrolTargets.size());
		for(Vector3i vector3i : patrolTargets) vector3i.serialize(b);
		b.writeInt(patrolIndex);
		if(savedRemotes.isEmpty()) b.writeBoolean(false);
		else {
			b.writeBoolean(true);
			b.writeShort(savedRemotes.size());
			for(Map.Entry<String, Boolean> entry : savedRemotes.entrySet()) {
				b.writeUTF(entry.getKey());
				b.writeBoolean(entry.getValue());
			}
		}
		if(combatSetting == null) combatSetting = "SOMETIMES ENGAGE";
		b.writeUTF(combatSetting);
		b.writeBoolean(combinedTargeting);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		flagRemove = b.readBoolean();

		dbid = b.readLong();
		missionString = b.readUTF();
		setName(b.readUTF());
		owner = b.readUTF();

		boolean hasMoveTarget = b.readBoolean();
		if(hasMoveTarget) {
			setCurrentMoveTarget(Vector3i.deserializeStatic(b));
		}


		short size = b.readShort();
		members.clear();
		for(int i = 0; i < size; i++) {
			FleetMember fleetMember = new FleetMember(state);
			fleetMember.deserialize(b, updateSenderStateId, isOnServer);
			members.add(fleetMember);
		}
		try {
			factionAccessible = b.readByte();
		} catch(Exception e) {
			factionAccessible = 0;
		}

		try {
			patrolTargets.clear();
			int length = b.readShort();
			for(int i = 0; i < length; i++) patrolTargets.add(Vector3i.deserializeStatic(b));
			patrolIndex = b.readInt();
			//if(b.readBoolean()) {
			//leaseData = new FleetLeaseData();
			//leaseData.deserialize(b, updateSenderStateId, isOnServer);
			//}
			if(b.readBoolean()) {
				savedRemotes.clear();
				int size2 = b.readShort();
				for(int i = 0; i < size2; i++) {
					savedRemotes.put(b.readUTF(), b.readBoolean());
				}
			}
			combatSetting = b.readUTF();
			combinedTargeting = b.readBoolean();
		} catch(EOFException ignored) {
			//Old data format, no patrol targets or saved remotes
			patrolTargets.clear();
			patrolIndex = 0;
			combatSetting = "SOMETIMES ENGAGE";
			combinedTargeting = false;
		}
	}

	public boolean isCombinedTargeting() {
		return combinedTargeting;
	}

	public void setCombinedTargeting(boolean combinedTargeting) {
		this.combinedTargeting = combinedTargeting;
		sendFleet();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		npcFleet = name.startsWith(NPCSystemFleetManager.IDPREFIX_FLEET);
		npcFleetGeneral = name.startsWith(NPCFleetManager.IDPREFIX_FLEET);
		if(npcFleet) {
			npcFaction = NPCSystemFleetManager.getFactionIdFromFleetName(name);
			npcSystem = NPCSystemFleetManager.getSystemFromFleetName(name);
			npcType = NPCSystemFleetManager.getTypeFromFleetName(name);
		} else if(npcFleetGeneral) {
			npcFaction = NPCFleetManager.getFactionIdFromFleetName(name);
			npcType = NPCFleetManager.getTypeFromFleetName(name);
		}
		this.name = name;
	}

	public void addMemberFromEntity(SegmentController c) {
		assert (isOnServer());
		for(FleetMember m : members) {

			if(m.entityDbId == c.getDbId()) {
				System.err.println("[SERVER][FLEET][ERROR] entity " + c + " already belongs to " + this);
				return;
			}
		}
		FleetMember fleetMember = new FleetMember(c);
		members.add(fleetMember);
		sendFleet();
	}

	public FleetMember addMemberFromDBID(long dbId) {
		assert (isOnServer());
		for(FleetMember m : members) {
			if(m.entityDbId == dbId) {
				System.err.println("[SERVER][FLEET][ERROR] entity dbId: " + dbId + " already belongs to " + this);
				return null;
			}
		}
		DatabaseEntry byId = null;
		try {
			byId = ((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().getById(dbId);
		} catch(SQLException e) {
			e.printStackTrace();
		}

		if(byId != null) {
			FleetMember fleetMember = new FleetMember(((GameServerState) state), dbId, byId.uid, byId.realName, byId.sectorPos, byId.dockedTo, byId.dockedRoot);
			members.add(fleetMember);
			sendFleet();
			return fleetMember;
		} else {
			System.err.println("[SERVER][FLEET][ERROR] entity dbId: " + dbId + " not in database");
			return null;
		}


	}

	public void removeMemberByEntity(SegmentController c) {
		assert (isOnServer());
		for(FleetMember m : members) {
			if(m.entityDbId == c.getDbId()) {
				boolean remove = members.remove(m);

				save();
				sendFleet();
				if(remove) {
					m.onRemovedFromFleet();
				}
				return;
			}
		}
	}

	public void removeFleet(boolean permanent) {
		assert (isOnServer());
		for(FleetMember m : members) {
			m.onRemovedFromFleet();
		}
		if(permanent) {

			((GameServerState) state).getDatabaseIndex().getTableManager().getFleetTable().removeFleetCompletely(this);
		}
		flagRemove = true;
		((GameStateInterface) state).getGameState().getNetworkObject().fleetBuffer.add(new RemoteFleet(this, isOnServer()));
	}

	public void sendFleet() {

		((GameStateInterface) state).getGameState().getNetworkObject().fleetBuffer.add(new RemoteFleet(this, isOnServer()));
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public StateInterface getState() {
		return state;
	}

	public void onUnloadedEntity(SegmentController c) {
		markForCacheCheck = true;

		if(!c.isMarkedForPermanentDelete()) {
			for(int i = 0; i < members.size(); i++) {
				FleetMember m = members.get(i);
				if(m.entityDbId == c.getDbId()) {
					m.setAttackedTriggered(false);
					((GameServerState) state).getDatabaseIndex().getTableManager().getFleetMemberTable().updateFleetMemberOnUnload(m);
					break;
				}
			}
		}
	}

	public String getFlagShipName() {
		if(members.isEmpty()) {
			return Lng.str("N/A");
		} else {
			return members.get(0).name;
		}
	}

	public String getFlagShipSector() {
		if(members.isEmpty()) {
			return Lng.str("N/A");
		} else {
			return members.get(0).getSector().toStringPure();
		}
	}

	public String getMissionName() {
		return missionString;
	}

	@Override
	public int hashCode() {
		return (int) (dbid ^ (dbid >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		return dbid == ((Fleet) obj).dbid && members.equals(((Fleet) obj).members);
	}

	public void save() {
		assert (isOnServer());
		try {
			((GameServerState) state).getDatabaseIndex().getTableManager().getFleetTable().updateOrInsertFleet(this);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Fleet [dbid=" + dbid + ", name=" + name + ", members=" + members + ", owner=" + owner + ", parentFleet=" + parentFleet + ", missionString=" + missionString + ", state=" + state + ", markForCacheCheck=" + markForCacheCheck + ", flagRemove=" + flagRemove + "]";
	}

	public void apply(Fleet fleet) {
		setName(fleet.name);
		owner = fleet.owner;
		parentFleet = fleet.parentFleet;
		missionString = fleet.missionString;

		//do a soft replacement so values are updated and not whole members replace
		//in case those are cached anywhere else (e.g. GUI)
		for(int i = 0; i < fleet.members.size(); i++) {
			FleetMember remoteFleetMember = fleet.members.get(i);
			if(members.contains(remoteFleetMember)) {
				FleetMember thisFleetMember = members.get(members.indexOf(remoteFleetMember));
				thisFleetMember.apply(remoteFleetMember);
				fleet.members.set(fleet.members.indexOf(remoteFleetMember), thisFleetMember);
			}
		}
		members.clear();

		members.addAll(fleet.members);
//		System.err.println("### APPLY FLEET :: :: REMOVE "+members+" ("+fleet.members+")");<<<<<< release
		factionAccessible = fleet.factionAccessible;
		patrolTargets.clear();
		patrolTargets.addAll(fleet.patrolTargets);
		patrolIndex = fleet.patrolIndex;
		savedRemotes.clear();
		savedRemotes.putAll(fleet.savedRemotes);
		combatSetting = fleet.combatSetting;
	}


	public FleetMember getFlagShip() {
		return members.isEmpty() ? null : members.get(0);
	}


	public boolean isEmpty() {
		return members.isEmpty();
	}


	public boolean isFlagShip(SegmentController c) {
		return !isEmpty() && members.get(0).UID.equals(c.getUniqueIdentifier());
	}


	public boolean isFlagShip(FleetMember f) {
		return !isEmpty() && members.get(0).UID.equals(f.UID);
	}


	public FleetMember removeMemberByDbIdUID(long entityDbId, boolean purge) {
		System.err.println("[SERVER][FLEET] NOW REMOVING FLEET MEMBER DBID: " + entityDbId + " from " + this);
		assert (isOnServer());
		for(int i = 0; i < members.size(); i++) {
			FleetMember m = members.get(i);
			if(m.entityDbId == entityDbId) {
				boolean remove = members.remove(m);
				((GameServerState) state).getDatabaseIndex().getTableManager().getFleetMemberTable().removeFleetMember(m);
				assert (remove);
				if(i == 0) save();
				sendFleet();
				System.err.println("[SERVER][FLEET] REMOVED FLEET MEMBER (by DBID): " + m + " from " + this);
				if(purge) {
					try {
						List<DatabaseEntry> bySector = ((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().getBySector(m.getSector(), -1);
						for(DatabaseEntry entry : bySector) {
							if(entry.dockedRoot == m.entityDbId)
								((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(entry.dbId);
						}
					} catch(Exception exception) {
						exception.printStackTrace();
					}
					((GameServerState) state).destroyEntity(m.entityDbId);
				}
				m.onRemovedFromFleet();
				return m;
			}
		}
		return null;
	}

	public FleetMember removeMemberByUID(String entityUID) {
		System.err.println("[SERVER][FLEET] NOW REMOVING FLEET MEMBER: " + entityUID + " from " + this);
		assert (isOnServer());
		for(int i = 0; i < members.size(); i++) {
			FleetMember m = members.get(i);
			if(m.UID.equals(entityUID)) {
				boolean remove = members.remove(m);

				((GameServerState) state).getDatabaseIndex().getTableManager().getFleetMemberTable().removeFleetMember(m);
				assert (remove);
				if(i == 0) {
					//update flagship
					save();
				}
				sendFleet();
				System.err.println("[SERVER][FLEET] REMOVED FLEET MEMBER: " + m + " from " + this);
				if(remove) {
					m.onRemovedFromFleet();
				}
				return m;
			}
		}
		assert (false);
		return null;
	}


	public void onSectorChangedLoaded(Ship ship, Sector newSector) {
		for(int i = 0; i < members.size(); i++) {
			FleetMember m = members.get(i);
			if(m.entityDbId == ship.getDbId()) {
				if(!newSector.pos.equals(m.getSector())) {
					m.onSectorChange(m.getSector(), newSector.pos);

					m.getSector().set(newSector.pos);
					((FleetStateInterface) state).getFleetManager().submitSectorChangeToClients(m);
				}
				break;
			}
		}
	}


	public Vector3i getCurrentMoveTarget() {
		return moveTarget;
	}

	public void setCurrentMoveTarget(Vector3i... m) {
		if(m == null) removeCurrentMoveTarget();
		else {
			Vector3i bef = moveTarget;
			if(m.length == 1) {
				moveTarget = m[0];
				//this.patrolTargets.clear();
				//if(!this.patrolTargets.contains(m[0])) this.patrolTargets.add(m[0]);
				if(bef == null || !bef.equals(m[0])) sendCurrentTargetToClients();
			} else {
				moveTarget = m[0];
				patrolTargets.clear();
				patrolTargets.addAll(Arrays.asList(m));
				if(bef == null || !bef.equals(m[0])) sendCurrentTargetToClients();
			}
		}
	}

	public void removeCurrentMoveTarget() {
		Vector3i bef = moveTarget;
		moveTarget = null;
		if(bef != null) sendCurrentTargetToClients();
	}

	public void sendCurrentTargetToClients() {
		if(isOnServer()) getFleetManager().submitTargetPositionToClients(this);
	}

	public void queuePatrolTargets(Vector3i... patrolTargets) {
		if(patrolTargets == null) removeCurrentMoveTarget();
		else {
			this.patrolTargets.clear();
			this.patrolTargets.addAll(Arrays.asList(patrolTargets));
			patrolIndex = 0;
			setCurrentMoveTarget(patrolTargets[0]);
			sendFleet();
		}
	}

	public ArrayList<Vector3i> getPatrolTargets() {
		return patrolTargets;
	}

	public boolean isCommandUsable(FleetCommandTypes c) {
		return true;
	}


	public void sendFleetCommand(FleetCommandTypes t, Object... args) {

		FleetCommand c = new FleetCommand(t, this, args);

		getFleetManager().sendFleetCommand(c);
	}


	public boolean isNPCFleet() {
		return npcFleet;
	}

	public boolean isNPCFleetGeneral() {
		return npcFleetGeneral;
	}


	public Vector3i getNpcSystem() {
		if(!npcFleet) {
			throw new IllegalArgumentException("NO NPC FLEET: " + name);
		}
		return npcSystem;
	}


	public int getNpcFaction() {
		if(!npcFleet && !npcFleetGeneral) {
			throw new IllegalArgumentException("NO NPC FLEET: " + name);
		}
		return npcFaction;
	}


	public NPCSystemFleetManager.FleetType getNpcType() {
		if(!npcFleet && !npcFleetGeneral) {
			throw new IllegalArgumentException("NO NPC FLEET: " + name);
		}
		return npcType;
	}


	public void onCommandPartFinished(FleetState currentState) {
		if(npcFleet) {
			Faction faction = ((FactionState) state).getFactionManager().getFaction(getNpcFaction());
			if(faction != null && faction instanceof NPCFaction) {
				((NPCFaction) faction).onCommandPartFinished(this, currentState);
			}
		}
	}

	public void onHitFleetMember(Damager damager, EditableSendableSegmentController seg) {
		if(isOnServer() && isActive()) {
			State currentState = getMachine().getFsm().getCurrentState();
			if(currentState != null && currentState instanceof FleetState) {
				((FleetState) currentState).onHitBy(damager);
			}

			if(npcFleet) {
				Faction faction = ((GameServerState) state).getFactionManager().getFaction(getNpcFaction());
				if(faction != null && faction.isNPC()) {
					((NPCFaction) faction).onAttackedFaction(this, seg, damager);
				}
			}
		}
	}


	public boolean isMember(long entDbId) {
		for(FleetMember m : members) {
			if(m.entityDbId == entDbId) {
				return true;
			}
		}
		return false;
	}


	public boolean isCached() {
		return ((FleetStateInterface) state).getFleetManager().isCached(this);
	}


	public void debugMoveBetween(Vector3i from, Vector3i to) {
		for(FleetMember s : members) {
			if(!s.isLoaded()) {
				System.err.println("[FLEET][DEBUG] Debug cannot start: not all members loaded: " + members);
				return;
			}
		}
		for(FleetMember s : members) {
			SegmentController c = s.getLoaded();
			if(c.railController.isRoot()) {
				c.saveDebugRail();
				System.err.println("[FLEET][DEBUG] Debug: Saved rail structure of " + c);
			}
		}
		DebugMove d = new DebugMove();
		d.from.set(from);
		d.to.set(to);
		debug = d;
	}

	/**
	 * Moves the fleet to a nearby unloaded sector
	 */
	public void moveToUnload() {
		Vector3i newSector = Vector3i.parseVector3i(getFlagShipSector());
		newSector.x += (new Random()).nextInt(16);
		newSector.y += (new Random()).nextInt(16);
		newSector.z += (new Random()).nextInt(16);
		findUnloadedOnWay(getFlagShip().getSector(), newSector);
		setCurrentMoveTarget(newSector);
	}

	private Vector3i findUnloadedOnWay(Vector3i from, Vector3i to) {
		if(from.equals(to)) {
			Vector3i pos = new Vector3i(from);
			while(((GameServerState) state).getUniverse().isSectorLoaded(pos)) pos.x++;
		}
		Vector3f f = new Vector3f(from.x, from.y, from.z);
		Vector3f t = new Vector3f(to.x, to.y, to.z);
		t.sub(f);
		t.normalize();
		int i = 0;
		Vector3i pos = new Vector3i(from);
		while(((GameServerState) state).getUniverse().isSectorLoaded(pos)) {
			f.add(t);
			pos.set(Math.round(f.x), Math.round(f.y), Math.round(f.z));
			i++;
			if(i > 1000)
				throw new IllegalArgumentException("NO UNLOADED SECTOR WITHIN 1000 tries: " + from + " -> " + to + "; f: " + f + "; t: " + t);
		}
		return pos;
	}

	public void orderShipyardRepair() {
		//sendFleetCommand(FleetCommandTypes.REPAIR_FLEET);
	}

	public void orderShipyardReinforce(CatalogPermission permission) {
		if(isEmpty()) members.add(new RequestedFleetMember(state, 0, permission.getUid(), permission));
		else
			members.add(new RequestedFleetMember(state, getFlagShip().getFactionId(), permission.getUid(), permission));
		sendFleet();
	}

	public void addReinforcement(SegmentController entity) {
		RequestedFleetMember toRemove = null;
		for(FleetMember member : members) {
			if(member instanceof RequestedFleetMember && member.getName().contains(entity.getName())) {
				toRemove = (RequestedFleetMember) member;
			}
		}
		if(toRemove != null) members.remove(toRemove);
		addMemberFromEntity(entity);
		sendFleet();
	}

	public Vector3i getCurrentPatrolTarget() {
		if(isPatrolling()) moveTarget = patrolTargets.get(patrolIndex);
		return moveTarget;
	}

	public Vector3i goToNextPatrolTarget() {
		if(isPatrolling()) {
			patrolIndex++;
			if(patrolIndex >= patrolTargets.size()) patrolIndex = 0;
			moveTarget = patrolTargets.get(patrolIndex);
		}
		return getCurrentPatrolTarget();
	}

	public boolean isEngaging() {
		for(FleetMember member : members) {
			if(member.getLoaded() != null) {
				Ship ship = (Ship) member.getLoaded();
				if(ship.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof TargetProgram) {
					if(((TargetProgram<?>) ship.getAiConfiguration().getAiEntityState().getCurrentProgram()).getTarget() != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String getStatus() {
		StringBuilder builder = new StringBuilder();
		builder.append(missionString).append(":\n");
		for(FleetMember member : members) {
			builder.append(member.getName()).append(" - ");
			if(member.getShipPercent() == 0) builder.append("DESTROYED [");
			else if(member.getShipPercent() < 1) builder.append("DAMAGED [");
			else builder.append("OK [");
			builder.append(member.getShipPercent() * 100).append("%]\n");
		}
		return builder.toString().trim();
	}

	public HashMap<String, Boolean> getSavedRemotes() {
		return savedRemotes;
	}

	public byte[] serializeRemotes() {
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(1024)) {
			ObjectOutput out;
			out = new ObjectOutputStream(bos);
			out.writeObject(savedRemotes);
			out.flush();
			return bos.toByteArray();
		} catch(IOException e) {
			e.printStackTrace();
			return new byte[1024];
		}
	}

	public void deserializeRemotes(byte[] data) {
		savedRemotes.clear();
		if(data == null) return;
		try(ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
			ObjectInput in;
			in = new ObjectInputStream(bis);
			savedRemotes.putAll((HashMap<String, Boolean>) in.readObject());
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void toggleRemote(String remote) {
		if(savedRemotes.containsKey(remote)) {
			boolean toggle = savedRemotes.get(remote);
			savedRemotes.remove(remote);
			savedRemotes.put(remote, !toggle);
			sendFleetCommand(FleetCommandTypes.ACTIVATE_REMOTE, remote, !toggle);
		} else {
			savedRemotes.put(remote, true);
			sendFleetCommand(FleetCommandTypes.ACTIVATE_REMOTE, remote, true);
		}
	}

	public void removeRemote(String s) {
		savedRemotes.remove(s);
	}

	public boolean isPatrolling() {
		return "PATROLLING".equals(missionString);
	}

	public byte getFactionAccess() {
		return factionAccessible;
	}

	public String getFactionAccessString() {
		return switch(factionAccessible) {
			case 0 -> Lng.str("RANK 4");
			case 1 -> Lng.str("RANK 3");
			case 2 -> Lng.str("RANK 2");
			case 3 -> Lng.str("RANK 1");
			case 4 -> Lng.str("FOUNDER");
			default -> Lng.str("PERSONAL");
		};
	}

	public void setFactionAccessible(byte factionAccessible) {
		this.factionAccessible = factionAccessible;
	}

	public int getFactionId() {
		try {
			return getFlagShip().getFactionId();
		} catch(Exception e) {
			return 0;
		}
	}

	private Vector3i getHomeSector() {
		try {
			return GameCommon.getGameState().getFactionManager().getFaction(getFactionId()).getHomeSector();
		} catch(Exception ignored) {
			return Vector3i.parseVector3i(getFlagShipSector());
		}
	}

	public void activateRemote(String name, Boolean toggle) {
		for(FleetMember member : members) {
			if(member.getLoaded() != null) {
				Ship ship = (Ship) member.getLoaded();
				for(Map.Entry<Long, String> entry : ship.getTextMap().entrySet()) {
					if(entry.getValue().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH))) {
						if(ship.getSegmentBuffer().existsPointUnsave(entry.getKey())) {
							SegmentPiece piece = ship.getSegmentBuffer().getPointUnsave(ElementCollection.getPosIndexFrom4(entry.getKey()));
							if(piece.getType() == ElementKeyMap.LOGIC_REMOTE_INNER)
								ship.getBlockActivationBuffer().enqueue(ElementCollection.getEncodeActivation(piece, true, toggle, false));
						}
					}
				}
			}
		}
	}

	public String getCombatSetting() {
		return combatSetting;
	}

	public void setCombatSetting(String combatSetting) {
		this.combatSetting = combatSetting;
	}

	public double getMass() {
		double mass = 0;
		for(FleetMember member : members) mass += member.getMass();
		return mass;
	}

	public boolean canAccess(String player) {
		if(player.toLowerCase(Locale.ROOT).equals(owner.toLowerCase(Locale.ROOT))) return true;
		Faction faction = GameCommon.getGameState().getFactionManager().getFaction(getFactionId());
		if(faction == null) return true;
		FactionPermission permission = faction.getMembersUID().get(player.toLowerCase(Locale.ROOT));
		if(permission == null || factionAccessible == 5) return false;
		return switch(permission.role) {
			case 0 -> factionAccessible == 4;
			case 1 -> factionAccessible >= 3;
			case 2 -> factionAccessible >= 2;
			case 3 -> factionAccessible >= 1;
			case 4 -> factionAccessible >= 0;
			default -> false;
		};
	}

	public void updateDebug(FleetManager m) {
		if(debug != null) {
			if(moveTarget == null || !moveTarget.equals(debug.getCurTar())) {
				FleetCommand com = new FleetCommand(FleetCommandTypes.MOVE_FLEET, this, new Vector3i(debug.getCurTar()));
				m.executeCommand(com);

			} else if(getFlagShip() != null && getFlagShip().getSector().equals(debug.getCurTar())) {
				if(debug.timeArrived <= 0) {
					System.err.println("[FLEET][DEBUG] DebugMove for: " + getFlagShip() + " Time Arrived Set " + debug.getCurTar());
					debug.timeArrived = System.currentTimeMillis();
				}

			}
			if(debug.timeArrived > 0) {
				long sec = (System.currentTimeMillis() - debug.timeArrived) / 1000;
				if(sec != lastSec) {
					System.err.println("[FLEET][DEBUG] DebugMove for: " + getFlagShip() + " WAITING: " + sec + " / " + (DebugMove.TIMEDELAY / 1000) + " SEC");
					lastSec = sec;
				}
				if(System.currentTimeMillis() - debug.timeArrived > DebugMove.TIMEDELAY) {
					debug.switchTar();
				}
			}
		}
	}

	public void debugStop() {
		for(FleetMember m : members) {
			((GameServerState) state).debugController.removeRailSave(m.entityDbId);
		}
		debug = null;
	}

	public byte[] getCurrentCommandBytes() throws IOException {
		if(currentCommand == null) {
			return null;
		} else {
			return currentCommand.serializeBytes();
		}
	}

	public void setCurrentCommand(FleetCommand fleetCommand) {
		currentCommand = fleetCommand;
	}

	public void setCurrentCommand(byte[] bytes) {
		if(bytes == null) {
			currentCommand = null;
		} else {
			FleetCommand f = new FleetCommand();
			DataInputStream in = new DataInputStream(new FastByteArrayInputStream(bytes));
			try {
				f.deserialize(in, 0);
				loadedCommand = f;
				System.err.println("[SERVER][FLEET] " + this + " loaded command " + loadedCommand);
				in.close();
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void checkLoadedCommand(FleetManager m) {
		if(loadedCommand != null) {
			System.err.println("[SERVER][FLEET] " + this + " executed loaded command " + loadedCommand);
			m.executeCommand(loadedCommand);
			loadedCommand = null;
		}
	}

	private class DebugMove {
		public static final long TIMEDELAY = 60000;
		public long timeArrived;
		boolean movingTo = true;
		private final Vector3i from = new Vector3i();
		private final Vector3i to = new Vector3i();

		public Vector3i getCurTar() {
			return movingTo ? to : from;
		}

		public void switchTar() {
			movingTo = !movingTo;
			timeArrived = 0;
		}
	}


}
