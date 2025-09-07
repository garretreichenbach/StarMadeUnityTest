package org.schema.game.common.data.fleet;

import api.common.GameClient;
import api.common.GameCommon;
import api.common.GameServer;
import api.listener.events.controller.fleet.FleetCacheEvent;
import api.listener.events.controller.fleet.FleetUnCacheEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.schema.common.util.LogInterface.LogLevel;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.fleet.FleetSelectionInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.data.fleet.FleetModification.FleetModType;
import org.schema.game.common.data.fleet.missions.MissionProgram;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.Sector;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.*;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Observable;


public class FleetManager extends Observable implements FleetSelectionInterface {

	public final Long2ObjectOpenHashMap<Fleet> fleetCache = new Long2ObjectOpenHashMap<Fleet>();
	public final Object2LongOpenHashMap<String> fleetsByName = new Object2LongOpenHashMap<String>();
	public final Long2LongOpenHashMap fleetsByEntityDbId = new Long2LongOpenHashMap();
	public final Object2LongOpenHashMap<String> fleetsByUID = new Object2LongOpenHashMap<String>();
	public final Object2ObjectOpenHashMap<String, LongOpenHashSet> fleetsByOwnerLowerCase = new Object2ObjectOpenHashMap<String, LongOpenHashSet>();

	public final StateInterface state;
	private final Long2ObjectOpenHashMap<FleetUnloadedAction> updateActionMap = new Long2ObjectOpenHashMap<FleetUnloadedAction>();
	private final Long2IntOpenHashMap updateActionMapCounter = new Long2IntOpenHashMap();
	private final LongOpenHashSet serverUncached = new LongOpenHashSet();
	private long selectedFleet = -1l;
	private long lastFleetChachingCheck;

	public final GUIObservable obs = new GUIObservable();



	public FleetManager(StateInterface state) {
		super();
		this.state = state;

		fleetsByEntityDbId.defaultReturnValue(-1L);
		updateActionMapCounter.defaultReturnValue(0);
	}

	public void update(Timer timer) {
		if(isOnServer()) {
			updateServer(timer);
		}
	}

	private void updateServer(Timer timer) {
		for(Fleet f : fleetCache.values()) {

			f.checkLoadedCommand(this);

			f.updateDebug(this);

			try {
				f.getCurrentProgram().update(timer);
			} catch(FSMException e) {
				e.printStackTrace();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		int actionLimit = 10000;
		ObjectIterator<Entry<Long, FleetUnloadedAction>> iterator = updateActionMap.entrySet().iterator();
		if(updateActionMap.size() > actionLimit) {
			System.err.println("[SERVER][FLEET] Exception: Fleet action count above limit: " + updateActionMap.size() + "; Emergency clear to avoid stall! Fleets: " + fleetCache.size());
			updateActionMap.clear();
		}
		int c = 0;
		while(iterator.hasNext()) {
			Entry<Long, FleetUnloadedAction> e = iterator.next();
			FleetUnloadedAction action = e.getValue();
			long fleet = fleetsByEntityDbId.get(e.getKey().longValue());
			if(fleet == action.fleet.dbid) {
				if(fleetCache.containsKey(action.fleet.dbid)) {
					boolean execute = action.execute(timer);
					c++;
					if(c > 0 && c % 200 == 0) {
						System.err.println("[SERVER][FLEET] Warning: Executed " + c + " fleet actions within one update");
					}
					if(execute) {
						iterator.remove();

						int from = updateActionMapCounter.get(action.fleet.dbid);

						modAction(action.fleet.dbid, -1);
					}
				} else {
					modAction(action.fleet.dbid, -1);
					iterator.remove();
				}
			} else {
				if(fleet > 0) {
					try {
						throw new Exception("Action doesn't correspond to a fleet " + fleet + "; " + action.fleet.dbid);
					} catch(Exception e1) {
						e1.printStackTrace();
					}
				} else {
					//fleet id returned -1 (no longer cached)
					System.err.println("[SERVER][FLEETMANAGER][WARNING] action for no longer cached fleet (discarding) " + action.fleet.dbid);
				}
				modAction(action.fleet.dbid, -1);
				iterator.remove();
			}

		}
		if(timer.currentTime - lastFleetChachingCheck > 60000) {

			checkChacheStatus();
			lastFleetChachingCheck = timer.currentTime;
		}
	}

	private void checkChacheStatus() {
		assert (isOnServer());
		LongArrayList toUncache = new LongArrayList();
		for(Fleet f : fleetCache.values()) {
			if(((GameServerState) state).getPlayerStatesByNameLowerCase().containsKey(f.getOwner().toLowerCase(Locale.ENGLISH))) {
				//player active. dont uncache
				continue;
			}
			if(f.isNPCFleet()) {
				Faction faction = ((FactionState) state).getFactionManager().getFaction(f.getNpcFaction());
				if(faction != null && faction instanceof NPCFaction) {
					if(((NPCFaction) faction).isSystemActive(f.getNpcSystem())) {
						continue;
					}
				}
			}
			if(updateActionMapCounter.get(f.dbid) > 0) {
				if(f.isNPCFleet()) {
					Faction faction = ((FactionState) state).getFactionManager().getFaction(f.getNpcFaction());
					if(faction != null && faction instanceof NPCFaction) {
						((NPCFaction) faction).log("Not uncaching Fleet " + f.getName() + " (existing mission) " + updateActionMapCounter.get(f.dbid), LogLevel.DEBUG);
					}
				}
				//there is unfinished business for this fleet
				continue;
			}
			boolean loaded = false;
			for(FleetMember m : f.getMembers()) {
				if(m.isLoaded()) {
					loaded = true;
					break;
				}
			}
			if(loaded) {
				continue;
			}

			toUncache.add(f.dbid);
		}

		for(long toDel : toUncache) {

			Fleet fleet = fleetCache.get(toDel);
			//			System.err.println("[SERVER][FLEETMANAGER] Saving and Uncaching Fleet "+fleet);
			//			fleet.save();
			//			uncacheFleet(fleet);

			FleetModification mod = new FleetModification();
			mod.type = FleetModType.UNCACHE;
			mod.fleetId = fleet.dbid;
			executeMod(mod);
		}
	}

	/**
	 * Will automatically attempt to load and cache the fleet on server
	 * <p>
	 * On client, will return null, if no fleet is cached
	 *
	 * @param c
	 *
	 * @return the fleet for a segmentController
	 */
	public Fleet getByEntity(SegmentController c) {
		Fleet f;
		if((f = getCachedByEntity(c)) != null || !isOnServer()) {
			//already loaded
			return f;
		} else {

			if(c.getDbId() > 0 && serverUncached.contains(c.getDbId())) {
				f = ((GameServerState) state).getDatabaseIndex().getTableManager().getFleetTable().loadFleetByAnyEntityId(state, c.dbId);

				if(f != null) {
					cacheFleet(f);
				} else {
					serverUncached.add(c.getDbId());
				}
				return f;
			}
		}
		return null;
	}

	public Fleet getByEntityDbId(long dbId) {
		Fleet f;
		if((f = getCachedByEntityDbId(dbId)) != null || !isOnServer()) {
			//already loaded
			return f;
		} else {

			if(dbId > 0 && serverUncached.contains(dbId)) {
				f = ((GameServerState) state).getDatabaseIndex().getTableManager().getFleetTable().loadFleetByAnyEntityId(state, dbId);

				if(f != null) {
					cacheFleet(f);
				} else {
					serverUncached.add(dbId);
				}
				return f;
			}
		}
		return null;
	}

	public Fleet getByFleetDbId(long fleetId) {
		Fleet f;
		if((f = fleetCache.get(fleetId)) != null || !isOnServer()) {
			//already loaded
			return f;
		} else {

			if(fleetId > 0) {
				f = ((GameServerState) state).getDatabaseIndex().getTableManager().getFleetTable().loadFleetById(state, fleetId);

				if(f != null) {
					cacheFleet(f);
				}
				return f;
			}
		}
		return null;
	}

	private void cacheFleetWithoutMembers(Fleet f) {

		fleetCache.put(f.dbid, f);

		fleetsByName.put(f.getName(), f.dbid);

		LongOpenHashSet fleetsByOwner = this.fleetsByOwnerLowerCase.get(f.getOwner().toLowerCase(Locale.ENGLISH));
		if(fleetsByOwner == null) {
			fleetsByOwner = new LongOpenHashSet();
			this.fleetsByOwnerLowerCase.put(f.getOwner().toLowerCase(Locale.ENGLISH), fleetsByOwner);
		}
		fleetsByOwner.add(f.dbid);
	}

	public void initializeFleet(Fleet f) {
		if(f.getCurrentProgram() == null) {
			f.setCurrentProgram(new MissionProgram(f, false));
		}
	}

	private void cacheFleet(Fleet f) {

		initializeFleet(f);

		cacheFleetWithoutMembers(f);

		for(FleetMember m : f.getMembers()) {

			cacheMember(f, m.UID, m.entityDbId);
		}

		obs.notifyObservers();
		//INSERTED CODE @318
		FleetCacheEvent event = new FleetCacheEvent(this, f);
		StarLoader.fireEvent(event, isOnServer());
		///
	}

	private void saveCommandInDb(Fleet fleet, FleetCommand fleetCommand) {
		assert (isOnServer());
		fleet.setCurrentCommand(fleetCommand);

		try {
			((GameServerState) fleet.getState()).getDatabaseIndex().getTableManager().getFleetTable().updateFleetCommand(fleet.dbid, fleetCommand);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	private void cacheMember(Fleet f, String UID, long entityDbId) {
		serverUncached.remove(entityDbId);

		fleetsByUID.put(UID, f.dbid);

		fleetsByEntityDbId.put(entityDbId, f.dbid);
	}

	private void uncacheFleet(Fleet f) {
		//INSERTED CODE @339
		FleetUnCacheEvent event = new FleetUnCacheEvent(this, f);
		StarLoader.fireEvent(event, isOnServer());
		if(event.isCanceled()) return;
		///
		uncacheFleetWithoutMembers(f);

		for(FleetMember m : f.getMembers()) {

			uncacheMember(m.UID, m.entityDbId);
		}
		if(f.dbid == selectedFleet) {
			selectedFleet = -1;
		}
		if(f.isNPCFleet()) {
			if(f.isNPCFleet()) {
				Faction faction = ((FactionState) state).getFactionManager().getFaction(f.getNpcFaction());
				if(faction != null && faction instanceof NPCFaction) {
					((NPCFaction) faction).onUncachedFleet(f);
				}
			}
		}
		obs.notifyObservers();
	}

	private void uncacheFleetWithoutMembers(Fleet f) {
		fleetCache.remove(f.dbid);

		fleetsByName.remove(f.getName());

		LongOpenHashSet fs = this.fleetsByOwnerLowerCase.get(f.getOwner().toLowerCase(Locale.ENGLISH));
		if(fs != null) {
			fs.remove(f.dbid);
			if(fs.isEmpty()) {
				this.fleetsByOwnerLowerCase.remove(f.getOwner().toLowerCase(Locale.ENGLISH));
			}
		}

	}

	private void uncacheMember(String UID, long entityDbId) {
		fleetsByUID.remove(UID);

		fleetsByEntityDbId.remove(entityDbId);
	}

	public boolean isOnServer() {
		return state instanceof ServerStateInterface;
	}

	public void updateFromNetworkObject(NetworkGameState o) {
		boolean needsUpdate = false;
		RemoteFleetBuffer fleetBuffer = o.fleetBuffer;

		for(int i = 0; i < fleetBuffer.getReceiveBuffer().size(); i++) {
			RemoteFleet remoteFleet = fleetBuffer.getReceiveBuffer().get(i);

			Fleet f = fleetCache.get(remoteFleet.get().dbid);

			//			System.err.println("[CLIENT] ##### RECEIVED REMOTE FLEET "+remoteFleet.get()+" -> "+f);
			if(f != null) {

				if(!f.equals(remoteFleet.get())) {
					//uncache all before recaching
					for(FleetMember m : f.getMembers()) {
						uncacheMember(m.UID, m.entityDbId);
					}
					f.apply(remoteFleet.get());

					cacheFleet(f);

					//System.err.println("[CLIENT] RECEIVED REMOTE FLEET (already existed): "+f);
				}
			} else {
				cacheFleet(remoteFleet.get());
				//System.err.println("[CLIENT] RECEIVED REMOTE FLEET "+remoteFleet.get());
			}
			needsUpdate = true;

		}

		RemoteFleetModBuffer modBuffer = o.fleetModBuffer;

		for(int i = 0; i < modBuffer.getReceiveBuffer().size(); i++) {
			RemoteFleetMod remoteFleet = modBuffer.getReceiveBuffer().get(i);
			executeMod(remoteFleet.get());
			needsUpdate = true;
		}

		RemoteFleetCommandBuffer cBuffer = o.fleetCommandBuffer;
		for(int i = 0; i < cBuffer.getReceiveBuffer().size(); i++) {
			RemoteFleetCommand remoteFleet = cBuffer.getReceiveBuffer().get(i);
			executeCommand(remoteFleet.get());
			needsUpdate = true;
		}
		if(needsUpdate) {
			obs.notifyObservers();
		}
	}

	public void sendFleetCommand(FleetCommand c) {
		((GameStateInterface) state).getGameState().getNetworkObject().fleetCommandBuffer.add(new RemoteFleetCommand(c, isOnServer()));
	}

	public void executeCommand(FleetCommand fleetCommand) {
		FleetCommandTypes t = FleetCommandTypes.values()[fleetCommand.getCommand()];

		assert (fleetCommand.fleetDbId > -1);

		Fleet fleet = fleetCache.get(fleetCommand.fleetDbId);

		//		System.err.println("[SERVER][FLEET] Executing command: "+t.name()+" -> "+t.transition.name()+"; on Fleet: "+fleet);

		if(fleet == null) {
			System.err.println("[SERVER][ERROR] Fleet not found: " + fleetCommand.fleetDbId + "; " + fleetCache);
			return;
		}
		try {
			switch(t) {
				case IDLE:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(FSMException e) {
						e.printStackTrace();
					}
					break;
				case MOVE_FLEET:
					Vector3i v = (Vector3i) fleetCommand.getArgs()[0];
					if(v.x == Integer.MIN_VALUE && v.y == Integer.MIN_VALUE && v.z == Integer.MIN_VALUE) {
						fleet.removeCurrentMoveTarget();
						try {
							fleet.getCurrentProgram().getMachine().getFsm().stateTransition(Transition.RESTART);
						} catch(FSMException e) {
							e.printStackTrace();
						}
					} else {
						fleet.setCurrentMoveTarget(v);
						try {
							fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
						} catch(FSMException e) {
							e.printStackTrace();
						}
					}
					break;
				case ESCORT:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case PATROL_FLEET:
					if(!fleet.isNPCFleet() && fleetCommand.getArgs() instanceof Vector3i[]) fleet.queuePatrolTargets((Vector3i[]) fleetCommand.getArgs());
					else if(fleetCommand.getArgs() != null) {
						Vector3i[] args = new Vector3i[fleetCommand.getArgs().length]; //For some unknown reason, the args are sometimes not Vector3i[] but Object[]
						for(int i = 0; i < args.length; i++) args[i] = (Vector3i) fleetCommand.getArgs()[i];
						fleet.queuePatrolTargets(args);
					} else fleet.setCurrentMoveTarget((Vector3i) fleetCommand.getArgs()[0]);
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case TRADE_FLEET:
					fleet.setCurrentMoveTarget((Vector3i) fleetCommand.getArgs()[0]);
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case CALL_TO_CARRIER:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
			/* TODO: Finish this later
		case REPAIR_FLEET:
			try {
				fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
			} catch(FSMException exception) {
				exception.printStackTrace();
			}
			break;
			 */
				case FLEET_ATTACK:
					fleet.setCurrentMoveTarget((Vector3i) fleetCommand.getArgs()[0]);
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case FLEET_DEFEND:
					fleet.setCurrentMoveTarget((Vector3i) fleetCommand.getArgs()[0]);
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case FLEET_IDLE_FORMATION:
					try {
						if(ServerConfig.ALLOW_FLEET_FORMATION.isOn()) {
							fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
						} else {
							try {
								PlayerState pl = ((GameServerState) state).getPlayerFromNameIgnoreCase(fleet.getOwner());
								pl.sendServerMessagePlayerError(Lng.astr("Formation not allowed on this server"));
							} catch(PlayerNotFountException e) {
								e.printStackTrace();
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case SENTRY:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case MINE_IN_SECTOR:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case CLOAK:
				case UNCLOAK:
				case JAM:
				case UNJAM:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case SENTRY_FORMATION:
					try {
						if(ServerConfig.ALLOW_FLEET_FORMATION.isOn()) {
							fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
						} else {
							try {
								PlayerState pl = ((GameServerState) state).getPlayerFromNameIgnoreCase(fleet.getOwner());
								pl.sendServerMessagePlayerError(Lng.astr("Formation not allowed on this server"));
							} catch(PlayerNotFountException e) {
								e.printStackTrace();
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case ACTIVATE_REMOTE:
					try {
						fleet.activateRemote((String) fleetCommand.getArgs()[0], (Boolean) fleetCommand.getArgs()[1]);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case REPAIR:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case ARTILLERY:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
				case INTERDICT:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case STOP_INTERDICT:
					try {
						fleet.getCurrentProgram().getMachine().getFsm().stateTransition(t.transition);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					throw new IllegalArgumentException("unkonwn command: " + t);
			}
		} catch(ClassCastException e) {
			e.printStackTrace();
		}

		if(isOnServer() && fleetCommand != fleet.loadedCommand) {
			saveCommandInDb(fleet, fleetCommand);
		}
	}

	private Fleet getCachedByEntityDbId(long dbId) {
		long id;
		return ((id = fleetsByEntityDbId.get(dbId)) > 0) ? fleetCache.get(id) : null;
	}

	private Fleet getCachedByEntity(SegmentController c) {
		long id;
		if(c.getDbId() < 0) {

			//on clients, there is no dbid known
			return ((id = fleetsByUID.getLong(c.getUniqueIdentifier())) > 0) ? fleetCache.get(id) : null;
		} else {
			return ((id = fleetsByEntityDbId.get(c.getDbId())) > 0) ? fleetCache.get(id) : null;
		}
	}

	public void onJoinedPlayer(PlayerState c) {
		assert (isOnServer());
		loadByOwner(c.getName().toLowerCase(Locale.ENGLISH));
		if(c.getFactionId() != 0) {
			//player is in a faction; load all shared fleets
			Faction fac = GameServerState.instance.getFactionManager().getFaction(c.getFactionId());
			for(String member : fac.getMembersUID().keySet()) {
				loadByOwner(member.toLowerCase(Locale.ENGLISH), true);
				//technically sends the fleet back to EVERYONE who can see it, which is inefficient, but this is not a common event
			}
		}
	}

	public void checkMemberFaction(PlayerState c) {
		ObjectArrayList<Fleet> availableFleets = getAvailableFleets(c.getName().toLowerCase(Locale.ENGLISH));
		for(Fleet f : availableFleets) {
			boolean changed = false;
			List<FleetMember> s = new ObjectArrayList<FleetMember>(f.getMembers());
			for(FleetMember m : s) {
				if(m.getFactionId() != 0 && m.getFactionId() != c.getFactionId()) {
					f.removeMemberByDbIdUID(m.entityDbId, false);
					changed = true;
				}
			}
			if(changed) {
				f.sendFleet();
			}
		}

	}

	public void loadByOwner(String owner) {
		loadByOwner(owner, false);
	}

	public void loadByOwner(String owner, boolean onlyShared) {
		assert (isOnServer());
		List<Fleet> loadFleetByOwner = ((GameServerState) state).getDatabaseIndex().getTableManager().getFleetTable().loadFleetByOwner(state, owner.toLowerCase(Locale.ENGLISH));

		for(Fleet f : loadFleetByOwner) {
			assert (f != null);
			if(!fleetCache.containsKey(f.dbid)) {
				if(!onlyShared || f.canAccess(owner)) {
					cacheFleet(f);
					f.sendFleet();
				}
			}
		}
	}

	public void unloadByOwner(String owner) {
		assert (isOnServer());
		LongOpenHashSet loadFleetByOwner = fleetsByOwnerLowerCase.get(owner.toLowerCase(Locale.ENGLISH));

		if(loadFleetByOwner != null) {
			LongOpenHashSet cpy = new LongOpenHashSet(loadFleetByOwner);
			for(long fid : cpy) {
				Fleet f = fleetCache.get(fid);
				f.save();
				uncacheFleet(f);
			}
		}
	}

	public void onLeftPlayer(PlayerState c) {
		assert (isOnServer());
		//TODO mark fleets to be checked for uncache
	}

	public void onAddedEntity(SegmentController c) {
		assert (isOnServer());
		assert (!(c instanceof Ship) || c.isVirtualBlueprint() || c.dbId > 0) : c;
		if(c.dbId < 0) {
			return;
		}
		//this will load and cache any fleet bound to this entity
		Fleet byEntity = getByEntity(c);

		if(byEntity != null) {
			for(int i = 0; i < byEntity.getMembers().size(); i++) {
				if(byEntity.getMembers().get(i).getLoaded() == c) {
					FleetMember fleetMember = byEntity.getMembers().get(i);
					Sector sector = ((GameServerState) state).getUniverse().getSector(c.getSectorId());
					if(sector != null) {
						if(!fleetMember.getSector().equals(sector.pos)) {
							fleetMember.getSector().set(sector.pos);
							submitSectorChangeToClients(fleetMember);
						}
					}
					break;
				}
			}
		}
		if(isOnServer() && c.getRuleEntityManager() != null) {
			c.getRuleEntityManager().triggerOnFleetChange();
		}
	}

	public void onRemovedEntity(long entityDbId) {

		Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(entityDbId);
		if(sendable != null && sendable instanceof SegmentController) {
			onRemovedEntity(((SegmentController) sendable));
		} else {
			long fleetId = fleetsByEntityDbId.get(entityDbId);
			Fleet f = getByEntityDbId(fleetId);
			if(f != null) {
				FleetMember removed = f.removeMemberByDbIdUID(entityDbId, false);
				if(removed != null){
					if(removed.getLoaded() != null) {
						if(isOnServer() && removed.getLoaded().getRuleEntityManager() != null) {
							removed.getLoaded().getRuleEntityManager().triggerOnFleetChange();
						}
					}

					uncacheMember(removed.UID, removed.entityDbId);
				}
				f.sendFleet();
			}
		}
	}

	public void onRemovedEntity(SegmentController c) {
		Fleet f;
		if((f = getCachedByEntity(c)) != null) {
			f.onUnloadedEntity(c);

			if(c.isMarkedForPermanentDelete()) {
				//re,pbe de�eted member from DB and cache and send it to clients

				FleetMember removed = null;
				if(f != null) {
					removed = f.removeMemberByUID(c.getUniqueIdentifier());
					if(removed != null) {
						uncacheMember(removed.UID, removed.entityDbId);
					}
					f.sendFleet();
				}
			}
			if(isOnServer() && c.getRuleEntityManager() != null) {
				c.getRuleEntityManager().triggerOnFleetChange();
			}
		}
	}

	public void updateToFullNetworkObject(NetworkGameState o) {
		for(Fleet f : fleetCache.values()) {

			//			try{
			//				throw new NullPointerException(f.toString());
			//				}catch(Exception e){
			//					e.printStackTrace();
			//				}
			o.fleetBuffer.add(new RemoteFleet(f, isOnServer()));
		}
	}

	private void sendMod(FleetModification mod) {
		((GameStateInterface) state).getGameState().getNetworkObject().fleetModBuffer.add(new RemoteFleetMod(mod, isOnServer()));
	}

	public Collection<Fleet> getAvailableFleetsClient() {

		String pName = ((GameClientState) state).getPlayer().getName().toLowerCase(Locale.ENGLISH);

		LongOpenHashSet longOpenHashSet = fleetsByOwnerLowerCase.get(pName);

		ObjectArrayList<Fleet> fleets = new ObjectArrayList<>();
		if(longOpenHashSet != null) {
			for(long l : longOpenHashSet) {
				Fleet fleet = fleetCache.get(l);
				if(fleet != null) fleets.add(fleet);
			}
		}
		fleets.addAll(getFactionAccessibleFleetsClient());
		return fleets;
	}

	public ObjectArrayList<Fleet> getAvailableFleets(String owner) {

		LongOpenHashSet longOpenHashSet = fleetsByOwnerLowerCase.get(owner.toLowerCase(Locale.ENGLISH));

		ObjectArrayList<Fleet> fleets = new ObjectArrayList<>();
		if(longOpenHashSet != null) {
			for(long l : longOpenHashSet) {
				Fleet fleet = fleetCache.get(l);
				if(fleet != null) fleets.add(fleet);
			}
		}
		if(GameServerState.instance != null){
			ObjectArrayList<Fleet> accFleets = getFactionAccessibleFleets(owner);
			if(accFleets != null) fleets.addAll(accFleets);
		}
		return fleets;
	}

	public ObjectArrayList<Fleet> getFactionAccessibleFleetsClient() {
		ObjectArrayList<Fleet> fleets = new ObjectArrayList<>();
		if(GameClient.getClientPlayerState().getFactionId() > 0) {
			Faction faction = GameClient.getClientState().getFaction();
			if(faction != null) {
				for(String s : faction.getMembersUID().keySet()) {
					LongOpenHashSet fleetsForMember = fleetsByOwnerLowerCase.get(s.toLowerCase(Locale.ENGLISH));
					if(fleetsForMember != null) for(Long l : fleetsForMember) {
						Fleet fleet = fleetCache.get(l);
						if(fleet != null && fleet.canAccess(GameClient.getClientPlayerState().getName()) && !fleet.getOwner().toLowerCase(Locale.ENGLISH).equals(GameClient.getClientPlayerState().getName().toLowerCase(Locale.ENGLISH))) fleets.add(fleet);
					}
				}
			}
		}
		return fleets;
	}

	public ObjectArrayList<Fleet> getFactionAccessibleFleets(String owner) {
		ObjectArrayList<Fleet> fleets = new ObjectArrayList<>();
		try {
			PlayerState player = GameServer.getServerState().getPlayerFromNameIgnoreCase(owner);
			if(player != null && player.getFactionId() > 0) {
				Faction faction = GameCommon.getGameState().getFactionManager().getFaction(GameServer.getServerState().getPlayerFromName(owner).getFactionId());
				if(faction != null) {
					for(String s : faction.getMembersUID().keySet()) {
						for(Fleet fleet : fleetCache.values()) {
							if(fleet != null && fleet.getOwner().toLowerCase(Locale.ENGLISH).equals(s.toLowerCase(Locale.ENGLISH)) && fleet.canAccess(owner) && !fleet.getOwner().toLowerCase(Locale.ENGLISH).equals(owner.toLowerCase(Locale.ENGLISH))) fleets.add(fleet);
						}
					}
				}
			}
		} catch(PlayerNotFountException ignored) {}
		return fleets;
	}

	private void executeMod(FleetModification mod) {
		//		System.err.println("[SERVER][FLEETMANAGER] received mod: "+mod);
		switch(mod.type) {
			case ADD_MEMBER:
				assert (isOnServer());

				Fleet fleet = fleetCache.get(mod.fleetId);
				assert (!fleetsByEntityDbId.containsKey(mod.entityDBId)) : "Ship already part of fleet: " + fleetsByEntityDbId.get(mod.entityDBId) + "; wanted to join: " + mod.fleetId;
				Sendable s = state.getLocalAndRemoteObjectContainer().getDbObjects().get(mod.entityDBId);

				if(fleet != null) {
					if(s != null && s instanceof SegmentController) {
						System.err.println("[SERVER][FLEETMANAGER] adding (loaded entity) fleet member: " + s + " to " + fleet);
						fleet.addMemberFromEntity((SegmentController) s);
						cacheMember(fleet, ((SegmentController) s).getUniqueIdentifier(), ((SegmentController) s).dbId);
					} else {
						FleetMember mem = fleet.addMemberFromDBID(mod.entityDBId);
						if(mem != null) {
							System.err.println("[SERVER][FLEETMANAGER] adding (from db entity) fleet member: " + mem + " to " + fleet);
							cacheMember(fleet, mem.UID, mod.entityDBId);
						}
					}
					fleet.save();
				}
				break;
			case COMMAND:
				break;
			case CREATE:

				Fleet f = new Fleet(state);
				f.setName(mod.name);
				f.setOwner(mod.owner);

				f.save(); //creates dbid
				cacheFleet(f);
				System.err.println("[FLEET][CREATE] " + state + " CREATED FLEET: " + mod.name + "; CURRENTLY CACHED FLEETS FOR " + mod.owner + ": " + fleetsByOwnerLowerCase.get(mod.owner.toLowerCase(Locale.ENGLISH)));
				f.sendFleet();

				break;
			case DELETE_FLEET:

				Fleet fltToDel = fleetCache.get(mod.fleetId);
				if(fltToDel != null) {
					for(FleetMember m : fltToDel.getMembers()) {
						m.onRemovedFromFleet();
					}

					uncacheFleet(fltToDel);
					if(isOnServer()) {
						sendMod(mod);
					}
				}
				if(isOnServer()) {
					deleteFleetFromDatabase(mod.fleetId);
				}

				break;
			case UNCACHE:

				Fleet fltToUncache = fleetCache.get(mod.fleetId);
				//			System.err.println("[FLEET] "+state+" UNCACHING FLEET "+fltToUncache);
				if(fltToUncache != null) {
					if(isOnServer()) {
						fltToUncache.save();
					}
					uncacheFleet(fltToUncache);
					if(isOnServer()) {
						sendMod(mod);
					}
				}

				break;
			case MISSION_UPDATE:

				Fleet fltUpdateMis = fleetCache.get(mod.fleetId);
				if(fltUpdateMis != null) {
					fltUpdateMis.missionString = mod.missionString;
					obs.notifyObservers();
			}


			break;
		case REMOVE_MEMBER:
			System.err.println("[FLEET] REMOVING MEMBER REQUEST (EXEC ON SERVER): " + mod);
				Fleet fltToDelMemFrom = fleetCache.get(mod.fleetId);
				FleetMember removed = null;
				if(fltToDelMemFrom != null) {
					removed = fltToDelMemFrom.removeMemberByDbIdUID(mod.entityDBId, false);
					if(removed != null) {
						uncacheMember(removed.UID, removed.entityDbId);
						if(removed.isLoaded()) {
							((AIConfiguationElements<Boolean>) ((Ship) removed.getLoaded()).getAiConfiguration().get(Types.ACTIVE)).setCurrentState(false, true);
						}
					}

				}
				break;
			case RENAME:
				break;
			case SECTOR_CHANGE:
				assert (!isOnServer());
				long l = fleetsByEntityDbId.get(mod.entityDBId);
				Fleet fl = fleetCache.get(l);
				boolean found = false;
				if(fl != null) {
					for(int i = 0; i < fl.getMembers().size(); i++) {
						if(fl.getMembers().get(i).entityDbId == mod.entityDBId) {
							fl.getMembers().get(i).getSector().set(mod.sector);
							found = true;
							break;
						}
					}
				}
				assert (!isOnServer() || found) : "Fleet not found by entityDBID: " + mod.entityDBId + "; " + fleetsByEntityDbId;
				break;
			case MOVE_MEMBER:
				Fleet fltToMoveIn = fleetCache.get(mod.fleetId);
				if(fltToMoveIn != null) {
					for(int i = 0; i < fltToMoveIn.getMembers().size(); i++) {
						FleetMember fleetMember = fltToMoveIn.getMembers().get(i);
						if(fleetMember.UID.equals(mod.entityUID)) {
							int newPos = i + mod.orderMove;
							if(newPos >= 0 && newPos < fltToMoveIn.getMembers().size()) {
								fltToMoveIn.getMembers().set(i, fltToMoveIn.getMembers().get(newPos));
								fltToMoveIn.getMembers().set(newPos, fleetMember);
								fltToMoveIn.save();
								fltToMoveIn.sendFleet();
							}
							break;
						}
					}
				}

				break;
			case TARGET_SEC_UDPATE:
				Fleet tarUpdate = fleetCache.get(mod.fleetId);
				if(tarUpdate != null) {
					if(mod.target != null) {
						tarUpdate.setCurrentMoveTarget(mod.target);
					} else {
						tarUpdate.removeCurrentMoveTarget();
					}
				}
				break;

			case SET_PERMISSION:
				Fleet toUpdate = fleetCache.get(mod.fleetId);
				if(toUpdate != null) {
					toUpdate.setFactionAccessible(mod.permission);
					toUpdate.save();
					toUpdate.sendFleet();
				}
				break;
			case CHANGE_SETTING:
				Fleet toUpdate2 = fleetCache.get(mod.fleetId);
				if(toUpdate2 != null) {
					toUpdate2.setCombatSetting(mod.combatSetting);
					toUpdate2.save();
					toUpdate2.sendFleet();
				}
				break;
			case CHANGE_TARGETING:
				Fleet toUpdate3 = fleetCache.get(mod.fleetId);
				if(toUpdate3 != null) {
					toUpdate3.setCombinedTargeting(mod.combinedTargeting);
					toUpdate3.save();
					toUpdate3.sendFleet();
				}
				break;
			default:
				break;
		}
	}

	private void deleteFleetFromDatabase(long fltToDel) {
		assert (isOnServer());

		((GameServerState) state).getDatabaseIndex().getTableManager().getFleetTable().removeFleetCompletely(fltToDel);
	}

	public void requestFleetTargetingChange(Fleet fleet, boolean value) {
		if(forbiddenAccessOnClient(fleet, true)) {
			((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Error! You don't own this fleet!"), 0);
			return;
		}

		FleetModification mod = new FleetModification();
		mod.type = FleetModType.CHANGE_TARGETING;
		mod.fleetId = fleet.dbid;
		mod.combinedTargeting = value;
		if(isOnServer()) executeMod(mod);
		else sendMod(mod);
	}

	public void requestCreateFleet(String name, String owner) {
		FleetModification mod = new FleetModification();
		mod.type = FleetModType.CREATE;
		mod.name = name;
		mod.owner = owner;

		if(isOnServer()) {
			executeMod(mod);
		} else {
			sendMod(mod);
		}

	}

	public void requestShipAdd(Fleet fleet, SegmentController c) {
		requestShipAdd(fleet, c.dbId);
	}

	public void requestShipAdd(Fleet fleet, long entityDbId) {
		if(forbiddenAccessOnClient(fleet, true)) {
			((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Error! You don't own this fleet!"), 0);
			return;
		}

		FleetModification mod = new FleetModification();
		mod.type = FleetModType.ADD_MEMBER;
		mod.entityDBId = entityDbId;
		mod.fleetId = fleet.dbid;

		if(isOnServer()) {
			executeMod(mod);
		} else {
			sendMod(mod);
		}
	}

	public void requestFleetRemove(Fleet f) {
		if(forbiddenAccessOnClient(f, false)) {
			((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Error! You don't own this fleet!"), 0);
			return;
		}

		FleetModification mod = new FleetModification();
		mod.type = FleetModType.DELETE_FLEET;
		mod.fleetId = f.dbid;

		if(isOnServer()) {
			executeMod(mod);
		} else {
			sendMod(mod);
		}
	}

	public boolean forbiddenAccessOnClient(Fleet f, boolean respectFactionShare) {
		if(isOnServer()) return false;
		if(f.canAccess(GameClient.getClientPlayerState().getName()) && respectFactionShare) return f.getFactionId() != ((GameClientState) state).getPlayer().getFactionId() && f.getFactionId() != 0;
		else return !f.getOwner().toLowerCase(Locale.ENGLISH).equals(((GameClientState) state).getPlayer().getName().toLowerCase(Locale.ENGLISH));
	}


	public void requestFleetMemberRemove(Fleet f, FleetMember m) {
		if(forbiddenAccessOnClient(f, true)) {
			((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Error! You don't own this fleet!"), 0);
			return;
		}

		FleetModification mod = new FleetModification();
		mod.type = FleetModType.REMOVE_MEMBER;
		mod.fleetId = f.dbid;
		mod.entityDBId = m.entityDbId;

		if(isOnServer()) {
			executeMod(mod);
		} else {
			sendMod(mod);
		}
	}

	public void requestFleetOrder(Fleet f, FleetMember m, int orderMod) {
		if(forbiddenAccessOnClient(f, true)) {
			((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Error! You don't own this fleet!"), 0);
			return;
		}

		FleetModification mod = new FleetModification();
		mod.type = FleetModType.MOVE_MEMBER;
		mod.fleetId = f.dbid;
		mod.entityUID = m.UID;
		mod.orderMove = (byte) orderMod;

		if(isOnServer()) {
			executeMod(mod);
		} else {
			sendMod(mod);
		}
	}

	public void requestFleetPermissionChange(Fleet f, byte newVal) {
		if(forbiddenAccessOnClient(f, false)) { //only the fleet owner should be able to toggle fleet permission
			((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Error! You don't own this fleet!"), 0);
			return;
		}

		FleetModification mod = new FleetModification();
		mod.type = FleetModType.SET_PERMISSION;
		mod.fleetId = f.dbid;
		mod.permission = newVal;

		if(isOnServer()) {
			executeMod(mod);
		} else {
			sendMod(mod);
		}
	}

	public void requestFleetSettingChange(Fleet fleet, String value) {
		if(forbiddenAccessOnClient(fleet, true)) {
			((GameClientState) state).getController().popupAlertTextMessage(Lng.str("Error! You don't own this fleet!"), 0);
			return;
		}

		FleetModification mod = new FleetModification();
		mod.type = FleetModType.CHANGE_SETTING;
		mod.fleetId = fleet.dbid;
		mod.combatSetting = value;
		if(isOnServer()) executeMod(mod);
		else sendMod(mod);
	}

	@Override
	public Fleet getSelected() {
		return fleetCache.get(selectedFleet);
	}

	public void setSelected(Fleet f) {
		if(f != null) {
			selectedFleet = f.dbid;
		} else {
			selectedFleet = -1l;
		}
		obs.notifyObservers();
	}

	public void submitSectorChangeToClients(FleetMember fleetMember) {
		FleetModification mod = new FleetModification();
		mod.type = FleetModType.SECTOR_CHANGE;
		mod.entityDBId = fleetMember.entityDbId;
		mod.sector = fleetMember.getSector();
		sendMod(mod);
	}

	public void addUpdateAction(FleetUnloadedAction a) {

		FleetUnloadedAction put = this.updateActionMap.put(a.member.entityDbId, a);

		if(put == null) {
			//			System.err.println("ADDING ACTION::: "+a.fleet.dbid+", MEM "+a.member.entityDbId);
			assert (a.fleet.getMembers().contains(a.member)) : a.member + "; " + a.fleet;

			modAction(a.fleet.dbid, 1);
		}
	}

	private void modAction(long fleetId, int mod) {
		int c = updateActionMapCounter.get(fleetId);
		int nw = Math.max(0, c + mod);

		//		System.err.println("UPDATING :::::::::: "+c+" -> "+nw );
		if(fleetCache.containsKey(fleetId)) {
			updateActionMapCounter.put(fleetId, nw);
			assert (c <= fleetCache.get(fleetId).getMembers().size()) : c + "; " + fleetCache.get(fleetId).getMembers().size();
		} else {
			assert (false) : fleetId + "; " + fleetCache + "; \n" + c + " -> " + nw;
		}

	}

	public void submitMissionChangeToClients(Fleet fleet) {
		FleetModification mod = new FleetModification();
		mod.type = FleetModType.MISSION_UPDATE;
		mod.fleetId = fleet.dbid;
		mod.missionString = fleet.missionString;
		sendMod(mod);

	}

	public void submitTargetPositionToClients(Fleet fleet) {
		FleetModification mod = new FleetModification();
		mod.type = FleetModType.TARGET_SEC_UDPATE;
		mod.fleetId = fleet.dbid;
		if(fleet.getCurrentMoveTarget() != null) {
			mod.target = new Vector3i[] {new Vector3i(fleet.getCurrentMoveTarget())};
		} else {
			mod.target = null;
		}
		sendMod(mod);

	}

	public void removeFleet(Fleet f) {
		assert (isOnServer());
		FleetModification m = new FleetModification();
		m.fleetId = f.dbid;
		m.type = FleetModType.DELETE_FLEET;
		executeMod(m);
	}

	public boolean isCached(Fleet fleet) {
		return fleetCache.containsKey(fleet.dbid);
	}

	public boolean isInFleet(long entityDbId) {
		return fleetsByEntityDbId.containsKey(entityDbId);
	}
}
