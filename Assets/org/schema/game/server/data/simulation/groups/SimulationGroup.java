package org.schema.game.server.data.simulation.groups;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.EntityUID;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.NoSimstateFountException;
import org.schema.game.server.data.simulation.SimPrograms;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class SimulationGroup extends AiEntityState implements TagSerializable {
	public static final int SIMULATION_TICK = 30;
	public static final int STATE_MACHINE_TICKS = 50;
	public static final long SECTOR_SPEED_MS = 1000;
	/**
	 *
	 */
	
	private final ObjectArrayList<String> members = new ObjectArrayList<String>();

	private final Object2ObjectOpenHashMap<String, Vector3i> sectorCache = new Object2ObjectOpenHashMap<String, Vector3i>();
	private final GameServerState state;
	private Vector3i startSector;
	private long startTime;
	private long ticks;

	public SimulationGroup(GameServerState state) {
		super("SimulationGroup", state);
		this.startTime = System.currentTimeMillis();
		this.state = state;
	}

	public void aggro(SimpleTransformableSendableObject from, float actualDamage) {

		System.err.println("[SIM] AGRRRO from " + from);

		if (getCurrentProgram() != null && getCurrentProgram() instanceof TargetProgram<?>) {
			((TargetProgram<?>) getCurrentProgram()).setSpecificTargetId(from.getId());
		}

		for (int i = 0; i < members.size(); i++) {
			String uid = members.get(i);
			if (isLoaded(uid)) {
				SegmentController segmentController = state.getSegmentControllersByName().get(uid);
				if (segmentController instanceof AiInterface) {
					MachineProgram<? extends AiEntityState> currentLocalProgram = ((AiInterface) segmentController).getAiConfiguration().getAiEntityState().getCurrentProgram();
					if (currentLocalProgram instanceof TargetProgram<?> && getCurrentProgram() instanceof TargetProgram<?>) {
						if (state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(((TargetProgram<?>) getCurrentProgram()).getSpecificTargetId())) {
							//							System.err.println("[SIM] tarsferred target id: "+((TargetProgram<?>)getCurrentProgram()).getSpecificTargetId());
							//transfer target id from missionProgram to localProgram
							((TargetProgram<?>) currentLocalProgram).setSpecificTargetId(((TargetProgram<?>) getCurrentProgram()).getSpecificTargetId());
						} else {
							//target has unloaded
							((TargetProgram<?>) getCurrentProgram()).setSpecificTargetId(-1);
						}
					}
				}
			}
		}
	}

	public void deleteMembers() {
		for (int i = 0; i < members.size(); i++) {
			if (isLoaded(i)) {
				if(state.getLocalAndRemoteObjectContainer().getLocalObjects().get(i) instanceof SegmentController) {
					SegmentController segmentController = (SegmentController) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(i);
					segmentController.railController.destroyDockedRecursive();
					for(ElementDocking dock : segmentController.getDockingController().getDockedOnThis()) {
						dock.from.getSegment().getSegmentController().markForPermanentDelete(true);
						dock.from.getSegment().getSegmentController().setMarkedForDeleteVolatile(true);
					}
				}
				state.getLocalAndRemoteObjectContainer().getLocalObjects().get(i).markForPermanentDelete(true);
				state.getLocalAndRemoteObjectContainer().getLocalObjects().get(i).setMarkedForDeleteVolatile(true);
			} else {
				destroyPersistent(members.get(i));
			}
		}
	}

	private void destroyPersistent(final String uid) {
		assert (isOnServer());
		GameServerState state = this.state;
		final String path = GameServerState.ENTITY_DATABASE_PATH + uid;
		try {
			state.getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(uid, state);

			File entity = new FileExt(GameServerState.ENTITY_DATABASE_PATH + uid + ".ent");
			System.err.println("[SERVER][SEGMENTCONTROLLER][SIMGROUP] PERMANENTLY DELETING ENTITY: " + entity.getName());
			entity.delete();

			state.getThreadQueue().enqueue(() -> {
				FilenameFilter filter = (dir, name) -> name.startsWith(uid);
				File dir = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
				File[] list = dir.listFiles(filter);
				for (File f : list) {
					System.err.println("[SERVER][SEGMENTCONTROLLER] PERMANENTLY DELETING ENTITY DATA: " + f.getName());
					f.delete();
				}
			});

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] subs = (Tag[]) tag.getValue();

		byte version = (Byte) subs[0].getValue();
		int type = (Integer) subs[1].getValue();

		assert (type == getType().ordinal()) : type + " / " + getType().ordinal();

		Tag[] membersTags = (Tag[]) subs[2].getValue();

		for (int i = 0; i < membersTags.length && membersTags[i].getType() != Type.FINISH; i++) {
			members.add((String) membersTags[i].getValue());
		}

		this.startTime = (Long) subs[3].getValue();

		this.startSector = (Vector3i) subs[4].getValue();

		int programId = (Integer) subs[5].getValue();
		if (programId >= 0) {
			SimPrograms simPrograms = SimPrograms.values()[programId];
			MachineProgram<SimulationGroup> prog = SimPrograms.getProgram(simPrograms, this, false);
			setCurrentProgram(prog);
		}

		handleMetaData(subs[6]);
	}

	@Override
	public Tag toTagStructure() {

		System.err.println("[SIM][TAG] WRITING GROUP " + this + "; " + members);

		Tag version = new Tag(Type.BYTE, null, (byte) 1);

		assert (this.getClass().isInstance(getType().clazz.instantiate(state)));

		Tag type = new Tag(Type.INT, null, getType().ordinal());

		Tag[] memberArray = new Tag[members.size() + 1];
		for (int i = 0; i < members.size(); i++) {
			memberArray[i] = new Tag(Type.STRING, null, members.get(i));
		}
		memberArray[members.size()] = FinishTag.INST;

		Tag members = new Tag(Type.STRUCT, null, memberArray);

		Tag startTime = new Tag(Type.LONG, null, this.startTime);

		Tag startSector = new Tag(Type.VECTOR3i, null, this.startSector);

		Tag program;
		try {
			program = new Tag(Type.INT, null, getCurrentProgram() != null ? SimPrograms.getFromClass(this).ordinal() : -1);
		} catch (NoSimstateFountException e) {
			e.printStackTrace();
			program = new Tag(Type.INT, null, -1);
		}

		Tag metaData = getMetaData();

		return new Tag(Type.STRUCT, null, new Tag[]{version, type, members, startTime, startSector, program, metaData, FinishTag.INST});
	}

	public int getCountMembersLoaded() {
		int count = 0;
		for (int i = 0; i < members.size(); i++) {
			if (state.getSegmentControllersByName().containsKey(members.get(i))) {
				count++;
			}
		}
		return count;
	}

	public String getDebugString() {
		return this.toString();
	}

	/**
	 * @return the members
	 */
	public ObjectArrayList<String> getMembers() {
		return members;
	}

	/**
	 * Metadata like target, attack entity etc
	 *
	 * @return a tag of metadata
	 */
	protected Tag getMetaData() {
		/*
		 * this is empty metadata.
		 * can be overwritten if needed
		 */
		return new Tag(Type.BYTE, null, (byte) 0);
	}

	public Vector3i getSector(String uid, Vector3i pos) throws EntityNotFountException, SQLException {
		if (isLoaded(uid)) {
			SegmentController segmentController = state.getSegmentControllersByName().get(uid);
			Sector sector = state.getUniverse().getSector(segmentController.getSectorId());
			if (sector != null) {
				pos.set(sector.pos);
				return pos;
			}
		}
		if (sectorCache.containsKey(uid)) {
			pos.set(sectorCache.get(uid));
			return pos;
		}
		return state.getDatabaseIndex().getTableManager().getSectorTable().getSector(uid.split("_", 3)[2], pos);
	}

	/**
	 * @return the startSector
	 */
	public Vector3i getStartSector() {
		return startSector;
	}

	/**
	 * @param startSector the startSector to set
	 */
	public void setStartSector(Vector3i startSector) {
		this.startSector = startSector;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the state
	 */
	@Override
	public GameServerState getState() {
		return state;
	}

	@Override
	public void updateOnActive(Timer timer) throws FSMException {
		if (getCurrentProgram() != null && !getCurrentProgram().isSuspended()) {
			//			System.err.println("UPDATING SIM PROGRAM "+getCurrentProgram().getMachine().getFsm().getCurrentState()+
			//					"; SecTar: "+((TargetProgram<?>)getCurrentProgram()).getSectorTarget()+"; size: "+getMembers().size()+": "+sectorCache);
			getCurrentProgram().getMachine().update();
		} else {
			//			System.err.println("NOT UPDATING SIM PROGRAM "+getCurrentProgram());
		}
	}

	public abstract GroupType getType();


	protected void handleMetaData(Tag metadata) {
		/*
		 * this is for empty metadata.
		 * overwrite if metadata is present
		 */
	}

	public boolean isLoaded(int memberIndex) {
		return isLoaded(members.get(memberIndex));
	}

	public boolean isLoaded(String uid) {
		return state.getSegmentControllersByName().containsKey(uid);
	}

	public boolean isLoadedInSector(String uid, Vector3i what) {
		if (isLoaded(uid)) {
			SegmentController segmentController = state.getSegmentControllersByName().get(uid);
			Sector sector = state.getUniverse().getSector(segmentController.getSectorId());
			if (sector != null) {
				return sector.pos.equals(what);
			}
		}
		return false;
	}

	public boolean moveToTarget(String uid, Vector3i to) {

		if (isLoaded(uid)) {
			SegmentController segmentController = state.getSegmentControllersByName().get(uid);
			Sector sector = state.getUniverse().getSector(segmentController.getSectorId());

			Vector3i toDest = new Vector3i(to);

			if (sector != null) {

				try {
					if (sector.getSectorType() == SectorType.PLANET) {
						//avoid planets
						if (to.y > sector.pos.y) {
							//ship under the planet
							toDest.set(sector.pos.x - 3, sector.pos.y - 1, sector.pos.z);
						} else {
							toDest.set(sector.pos.x - 3, sector.pos.y + 1, sector.pos.z);
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			if (segmentController instanceof AiInterface) {
				MachineProgram<? extends AiEntityState> currentLocalProgram = ((AiInterface) segmentController).getAiConfiguration().getAiEntityState().getCurrentProgram();
				if (currentLocalProgram instanceof TargetProgram<?>) {
					((TargetProgram<?>) currentLocalProgram).setSectorTarget(toDest);

					if (getCurrentProgram() instanceof TargetProgram<?>) {
						if (state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(((TargetProgram<?>) getCurrentProgram()).getSpecificTargetId())) {
							//transfer target id from missionProgram to localProgram
							((TargetProgram<?>) currentLocalProgram).setSpecificTargetId(((TargetProgram<?>) getCurrentProgram()).getSpecificTargetId());
						} else {
							//target has unloaded
							((TargetProgram<?>) getCurrentProgram()).setSpecificTargetId(-1);
						}
					}

				}

			}
		} else {
			Vector3i from = new Vector3i();
			Vector3i toTmp = new Vector3i();
			Vector3i toDestTmp = new Vector3i();

			try {
				from = getSector(uid, from);
				float longest = -1;
				for (int i = 0; i < 6; i++) {
					toTmp.set(from);
					toTmp.add(Element.DIRECTIONSi[i]);
					toTmp.sub(to);
					toTmp.negate();
					if (longest < 0 || toTmp.length() < longest) {
						toDestTmp.set(from);
						toDestTmp.add(Element.DIRECTIONSi[i]);
						longest = toTmp.length();
					}
				}
				if (longest >= 0) {

					//					System.err.println("SIMULATION: Moving "+uid+" from "+from+" to "+toDestTmp+" for target "+to+" ###############");

					setSectorForUnloaded(uid, toDestTmp, false);
				}

			} catch (EntityNotFountException e) {
				e.printStackTrace();
				return false;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public void onWait() {
		for (int i = 0; i < members.size(); i++) {
			String uid = members.get(i);
			if (isLoaded(uid)) {
				SegmentController segmentController = state.getSegmentControllersByName().get(uid);
				if (segmentController instanceof AiInterface) {
					MachineProgram<? extends AiEntityState> currentLocalProgram = ((AiInterface) segmentController).getAiConfiguration().getAiEntityState().getCurrentProgram();
					if (currentLocalProgram instanceof TargetProgram<?> && getCurrentProgram() instanceof TargetProgram<?>) {
						if (state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(((TargetProgram<?>) getCurrentProgram()).getSpecificTargetId())) {
							//							System.err.println("[SIM] tarsferred target id: "+((TargetProgram<?>)getCurrentProgram()).getSpecificTargetId());
							//transfer target id from missionProgram to localProgram
							((TargetProgram<?>) currentLocalProgram).setSpecificTargetId(((TargetProgram<?>) getCurrentProgram()).getSpecificTargetId());
						} else {
							//target has unloaded
							((TargetProgram<?>) getCurrentProgram()).setSpecificTargetId(-1);
						}
					}
				}
			}
		}
	}

	public void returnHomeMessage(PlayerState s) {

	}

	public void sendInvestigationMessage(PlayerState s) {

	}

	public void setSectorForUnloaded(String uid, Vector3i to, boolean db) throws Exception {
		if (!isLoaded(uid)) {
			if (!sectorCache.containsKey(uid)) {
				sectorCache.put(uid, new Vector3i());
			}
			sectorCache.get(uid).set(to);

			if (db) {
				try {
					state.getDatabaseIndex().getTableManager().getEntityTable().changeSectorForEntity(uid.split("_", 3)[2], to);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (state.getUniverse().isSectorLoaded(to)) {
				sectorCache.remove(uid);
				System.err.println("[SIMULATION] SECTOR " + to + " IS LOADED: now LOADING " + uid + "; " + state.getSegmentControllersByName().size());
				state.getUniverse().getSector(to).loadEntitiy(state, new EntityUID(uid, DatabaseEntry.getEntityType(uid), -1));
			}
		}
	}

	public void setSectorForUnloaded(Vector3i to, boolean db) throws Exception {
		for (int i = 0; i < members.size(); i++) {
			setSectorForUnloaded(members.get(i), to, db);
		}
	}

	public void updateTicks(long ticksAdded) {
		this.ticks += ticksAdded;
		if (this.ticks > STATE_MACHINE_TICKS) {
			try {
				updateOnActive(null);
			} catch (FSMException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.ticks -= STATE_MACHINE_TICKS;
		}

	}

	public void writeToDatabase() {
		for (int i = 0; i < members.size(); i++) {
			try {
				setSectorForUnloaded(members.get(i), getSector(members.get(i), new Vector3i()), true);
			} catch (EntityNotFountException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public enum GroupType {
		TARGET_SECTOR((SimGroupFactory<TargetSectorSimulationGroup>) TargetSectorSimulationGroup::new),
		RAVEGING((SimGroupFactory<RavegingSimulationGroup>) RavegingSimulationGroup::new),
		ATTACK_SINGLE((SimGroupFactory<AttackSingleEntitySimulationGroup>) AttackSingleEntitySimulationGroup::new);
		;
		public final SimGroupFactory<? extends SimulationGroup> clazz;

		private GroupType(SimGroupFactory<? extends SimulationGroup> c) {
			this.clazz = c;
		}
	}

	public void print(RegisteredClientInterface client) throws IOException {
		client.serverMessage("-----------");
		client.serverMessage("GROUP: "+getClass().getSimpleName());
		client.serverMessage("TOTAL MEMBERS: "+ members.size());
		client.serverMessage("LOADED MEMBERS: "+getCountMembersLoaded());
		for(int i = 0; i < members.size(); i++){
			client.serverMessage("MEMBER INDEX "+i+": "+members.get(i));
		}
		client.serverMessage("PROGRAM: "+getCurrentProgram().getClass().getSimpleName());
		client.serverMessage("MACHINE: "+getCurrentProgram().getMachine().getClass().getSimpleName());
		client.serverMessage("STATE: "+getCurrentProgram().getMachine().getFsm().getCurrentState().getClass().getSimpleName());
	}

	public void despawn() {
		for(int i = 0; i < members.size(); i++){
			try {
				state.getController().despawn(DatabaseEntry.removePrefixWOException(members.get(i)));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		members.clear();
	}

}
