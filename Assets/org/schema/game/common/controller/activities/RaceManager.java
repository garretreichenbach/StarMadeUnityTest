package org.schema.game.common.controller.activities;

import java.sql.SQLException;
import java.util.List;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.activities.Race.RaceState;
import org.schema.game.common.controller.activities.RaceModification.RacemodType;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.elements.racegate.RacegateCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.RemoteRace;
import org.schema.game.network.objects.remote.RemoteRaceMod;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RaceManager extends GUIObservable {

	private List<Race> archivedRaces = new ObjectArrayList<Race>();
	private List<Race> activeRaces = new ObjectArrayList<Race>();
	private Int2ObjectOpenHashMap<Race> activeRacesMap = new Int2ObjectOpenHashMap<Race>();
	private int idGen = 1;
	private final StateInterface state;
	private final SendableGameState gameState;
	private final boolean onServer;
	private ObjectArrayFIFOQueue<RaceModification> receivedMods = new ObjectArrayFIFOQueue<RaceModification>();
	private ObjectArrayFIFOQueue<Race> receivedRaces = new ObjectArrayFIFOQueue<Race>();
	private Race selectedRaceClient;

	public RaceManager(SendableGameState gameState) {
		this.gameState = gameState;
		this.state = gameState.getState();
		onServer = gameState.isOnServer();
	}

	public void endRaceOnClient(SegmentPiece block) {
		if (block.getSegmentController() instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) block
				.getSegmentController()).getManagerContainer();
			if (managerContainer instanceof StationaryManagerContainer<?>) {
				StationaryManagerContainer<?> sta = (StationaryManagerContainer<?>) managerContainer;
	
				RacegateCollectionManager racegateCollectionManager = sta
					.getRacegate().getCollectionManagersMap()
					.get(block.getAbsoluteIndex());
				if(isRaceActive(racegateCollectionManager)){
					Race race = getRace(racegateCollectionManager);
					if(!race.isFinished()){
						RaceModification m = new RaceModification();
						m.raceId = getRace(racegateCollectionManager).id;
						m.type = RacemodType.TYPE_FINISHED;
						sendMod(m);
					}
				}else{
					((GameClientState)state).getController().popupAlertTextMessage(Lng.str("Cannot end Race!\nNo race was created here!"), 0);
				}
			}
		}
	}
	public void startNewRaceOnClient(SegmentPiece block, int laps, int buyIn, String name) {
		if (block.getSegmentController() instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) block
				.getSegmentController()).getManagerContainer();
			if (managerContainer instanceof StationaryManagerContainer<?>) {
				StationaryManagerContainer<?> sta = (StationaryManagerContainer<?>) managerContainer;
	
				RacegateCollectionManager racegateCollectionManager = sta
					.getRacegate().getCollectionManagersMap()
					.get(block.getAbsoluteIndex());
				if(!isRaceActive(racegateCollectionManager)){
					RaceModification m = new RaceModification();
					m.raceId = -1;
					m.type = RacemodType.TYPE_CREATE_RACE;
					m.createRaceSegContId = block.getSegmentController().getId();
					m.createRaceBlockIndex = block.getAbsoluteIndex();
					m.createRaceName = name;
					m.laps = laps;
					System.err.println("CREATING RACE: LAPS: "+m.laps);
					m.buyIn = buyIn;
					m.creatorName = ((GameClientState)block.getSegmentController().getState()).getPlayerName();
					sendMod(m);
				}else{
					((GameClientState)state).getController().popupAlertTextMessage(Lng.str("Cannot create Race!\nA race at this gate already exists."), 0);
				}
			}
		}
		

	}

	public void startNewRaceOnServer(SegmentPiece block, String name, String creatorName, int laps, int buyIn) {

		if (block.getSegmentController() instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) block
					.getSegmentController()).getManagerContainer();
			if (managerContainer instanceof StationaryManagerContainer<?>) {
				StationaryManagerContainer<?> sta = (StationaryManagerContainer<?>) managerContainer;

				RacegateCollectionManager racegateCollectionManager = sta
						.getRacegate().getCollectionManagersMap()
						.get(block.getAbsoluteIndex());

				if (racegateCollectionManager != null) {
					if(!isRaceActive(racegateCollectionManager)){
						Race r = new Race();
						r.id = idGen++;
						try {
							r.create(racegateCollectionManager, name, creatorName, laps, buyIn);
							activeRaces.add(r);
							activeRacesMap.put(r.id, r);
							gameState.getNetworkObject().raceBuffer
									.add(new RemoteRace(r, onServer));
						} catch (SQLException e) {
							e.printStackTrace();
						} catch (EntityNotFountException e) {
							e.printStackTrace();
						}
					}else{
						System.err
						.println("[RACEMANAGER] cannot create race! Race already exists"
								+ block);
					}
				} else {
					System.err
							.println("[RACEMANAGER] cannot create race! No race controller found at index for "
									+ block);
				}
			}
		}

	}

	private void handleModOnClient(RaceModification m) {
		Race r = activeRacesMap.get(m.raceId);
		if (r != null) {
			switch (m.type) {
			case TYPE_ENTER: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					r.enter(p);
				}
				break;
			}
			case TYPE_ENTRANT_FOREFEIT: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					r.forefeit(p);
				}
				break;
			}
			case TYPE_ENTRANT_GATE: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					r.changeGate(p, m.gate, m.timeAtGate);
				}
				break;
			}
			case TYPE_FINISHED:
				r.setFinished(true);
				break;
			case TYPE_LEFT: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					r.leave(p);
				}
				break;
			}
			case TYPE_RACE_START:
				r.raceStart = m.raceStart - state.getServerTimeDifference();
				break;

			case TYPE_CREATE_RACE:
				// nothing to do (its a client request to server)
				break;
			default:
				assert (false);
				break;

			}
		} else {
			System.err
					.println("[CLIENT][RACE][ERROR] Could not apply mod. id not found: "
							+ m.raceId + "; " + activeRacesMap);
		}
	}

	private void handleModOnServer(RaceModification m) {
		if (m.type == RacemodType.TYPE_CREATE_RACE) {
			Sendable sendable = state.getLocalAndRemoteObjectContainer()
					.getLocalObjects().get(m.createRaceSegContId);
			if (sendable != null && sendable instanceof SegmentController) {
				
				SegmentPiece pp = ((SegmentController) sendable)
						.getSegmentBuffer().getPointUnsave(
								m.createRaceBlockIndex); //autorequest true previously
				if(pp != null){
					startNewRaceOnServer(pp, m.createRaceName, m.creatorName, m.laps, m.buyIn);
				}
			}
			return;
		}
		Race r = activeRacesMap.get(m.raceId);
		if (r != null) {
			switch (m.type) {
			case TYPE_ENTER: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					r.enter(p);
					r.broadcastAll(Lng.astr("%s joined the race.",  p.getName()),
							state);
				}
				break;
			}
			case TYPE_ENTRANT_FOREFEIT: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					RaceState raceState = r.getRaceState(p);
					if(raceState != null){
						if(r.isStarted() && !raceState.isFinished()){
							r.forefeit(p);
							r.broadcastAll(
									Lng.astr("%s forfeited the race.",  p.getName()), state);
						}else{
							
						}
					}
				}
				break;
			}
			case TYPE_ENTRANT_GATE: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					r.changeGate(p, m.gate, m.timeAtGate);

				}
				break;
			}
			case TYPE_FINISHED:
				r.setFinished(true);
				break;
			case TYPE_LEFT: {
				Sendable sendable = state.getLocalAndRemoteObjectContainer()
						.getLocalObjects().get(m.entrantId);
				if (sendable != null) {
					AbstractOwnerState p = (AbstractOwnerState) sendable;
					r.broadcastAll(Lng.astr("%s left the race.",  p.getName()),
							state);
					r.leave(p);
				}
				break;
			}
			case TYPE_RACE_START:
				r.start();
				r.broadcastAll(Lng.astr("Race started!"), state);
				break;

			default:
				assert (false);
				break;
			}

			// transport to clients
			sendMod(m);
		} else {
			System.err
					.println("[SERVER][RACE][ERROR] Could not apply mod. id not found: "
							+ m.raceId + "; " + activeRacesMap);
		}
	}

	public void requestJoinOnServer(AbstractOwnerState player,
			SegmentPiece raceControllerBlock) {
		for (Race c : activeRaces) {
			if (c.controlBlockUID.equals(raceControllerBlock
					.getSegmentController().getUniqueIdentifier())
					&& c.getStartRaceController() == raceControllerBlock
							.getAbsoluteIndex()) {
				requestJoinOnServer(player, c);
				return;
			}
		}
		((GameClientState) state).getController().popupAlertTextMessage(
				Lng.str("Can't find race to join!"), 0);
	}

	public void requestLeaveOnServer(AbstractOwnerState player,
			SegmentPiece raceControllerBlock) {
		RaceModification m = new RaceModification();
		for (Race c : activeRaces) {
			if (c.controlBlockUID.equals(raceControllerBlock
					.getSegmentController().getUniqueIdentifier())
					&& c.getStartRaceController() == raceControllerBlock
							.getAbsoluteIndex()) {
				requestLeave(player, c);
				return;
			}
		}
		((GameClientState) state).getController().popupAlertTextMessage(
				Lng.str("Can't find race to leave!"), 0);
	}

	public void requestJoinOnServer(AbstractOwnerState player, Race c) {
		if(isInRunningRace(player) && !getRace(player).isFinished() ){
			((GameClientState) state).getController().popupAlertTextMessage(
					Lng.str("Can't join Race! You are still in an active race!"), 0);
		}else if(!c.isStarted()){
			RaceModification m = new RaceModification();
			m.raceId = c.id;
			m.entrantId = player.getId();
			m.type = RacemodType.TYPE_ENTER;
			sendMod(m);
		}else{
			((GameClientState) state).getController().popupAlertTextMessage(
					Lng.str("Can't join Race! It already started!"), 0);
		}
	}

	public void requestLeave(AbstractOwnerState player, Race c) {
		RaceModification m = new RaceModification();
		m.raceId = c.id;
		m.entrantId = player.getId();
		m.type = RacemodType.TYPE_LEFT;
		sendMod(m);
	}

	public void requestForefit(AbstractOwnerState player, Race c) {
		requestForefitOnServer(player.getId(), c);
	}
	public void requestForefitOnServer(int player, Race c) {
		RaceModification m = new RaceModification();
		m.raceId = c.id;
		m.entrantId = player;
		m.type = RacemodType.TYPE_ENTRANT_FOREFEIT;
		sendMod(m);
	}
	public boolean isRaceActive(RacegateCollectionManager cm) {
		
		for (Race c : activeRaces) {
			if (c.controlBlockUID.equals(cm
					.getSegmentController().getUniqueIdentifier())
					&& c.getStartRaceController() == cm.getControllerElement()
							.getAbsoluteIndex()) {
				return true;
			}
		}
		return false;
				
	}
	public Race getRace(RacegateCollectionManager cm) {
		
		for (Race c : activeRaces) {
			if (c.controlBlockUID.equals(cm
					.getSegmentController().getUniqueIdentifier())
					&& c.getStartRaceController() == cm.getControllerElement()
					.getAbsoluteIndex()) {
				return c;
			}
		}
		return null;
		
	}
	public void onActivateRaceController(SegmentPiece block) {
		if (block.getSegmentController() instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) block
					.getSegmentController()).getManagerContainer();
			if (managerContainer instanceof StationaryManagerContainer<?>) {
				StationaryManagerContainer<?> sta = (StationaryManagerContainer<?>) managerContainer;

				RacegateCollectionManager racegateCollectionManager = sta
						.getRacegate().getCollectionManagersMap()
						.get(block.getAbsoluteIndex());

				if (racegateCollectionManager != null) {
					for (Race c : activeRaces) {
						if (c.controlBlockUID.equals(block
								.getSegmentController().getUniqueIdentifier())
								&& c.getStartRaceController() == block
										.getAbsoluteIndex() && !c.isStarted()) {
							if(c.getRacerCount() > 0){
								racegateCollectionManager.getSegmentController().sendSectorBroadcast(Lng.astr("A race started in this sector!"), ServerMessage.MESSAGE_TYPE_INFO);
								c.start();
								RaceModification m = new RaceModification();
								m.raceId = c.id;
								m.raceStart = c.raceStart;
								m.type = RacemodType.TYPE_RACE_START;
								sendMod(m);
							}else{
								racegateCollectionManager.getSegmentController().sendSectorBroadcast(Lng.astr("A Race in this sector cannot be started!\nNo Racers!"), ServerMessage.MESSAGE_TYPE_ERROR);
							}
						}
					}
				}
			}
		}
	}

	private void onFinished(Race race) {
		RaceModification m = new RaceModification();
		m.raceId = race.id;
		m.type = RacemodType.TYPE_FINISHED;
		sendMod(m);
	}

	public void requestFinishedOnClient(Race c) {
		onFinished(c);
	}

	void sendMod(RaceModification m) {
		assert (m.raceId != 0);
		gameState.getNetworkObject().raceModBuffer.add(new RemoteRaceMod(m, onServer));
	}

	public void updateServer(Timer timer) {
		while (!receivedMods.isEmpty()) {
			handleModOnServer(receivedMods.dequeue());

		}
		for (int i = 0; i < activeRaces.size(); i++) {
			activeRaces.get(i).updateServer(timer, state);
			if (activeRaces.get(i).isFinished()) {
				onFinished(activeRaces.get(i));
				Race r = activeRaces.remove(i);
				activeRacesMap.remove(r.id);
				archivedRaces.add(r);
				i--;
			}
			
			
		}
	}

	public void updateClient(Timer timer) {
		boolean changed = false;
		while (!receivedMods.isEmpty()) {
			RaceModification m = receivedMods.dequeue();
			handleModOnClient(m);
			changed = true;
		}
		while (!receivedRaces.isEmpty()) {
			Race r = receivedRaces.dequeue();
			activeRaces.add(r);
			activeRacesMap.put(r.id, r);
			changed = true;
		}
	
		for (int i = 0; i < activeRaces.size(); i++) {
			activeRaces.get(i).updateClient(timer, state);
			if (activeRaces.get(i).isFinished()) {
				
				Race r = activeRaces.remove(i);
				
				activeRacesMap.remove(r.id);
				if(selectedRaceClient == r){
					setSelectedRaceClient(null);
				}
				archivedRaces.add(r);
				i--;
				changed = true;
			}
		}
		if (changed) {
			
			notifyObservers();
		}
	}

	public void updateToFullNetworkObject(NetworkGameState o) {
		for (int i = 0; i < activeRaces.size(); i++) {
			o.raceBuffer.add(new RemoteRace(activeRaces.get(i), o));
		}
	}

	public void updateToNetworkObject(NetworkGameState o) {

	}

	public void updateFromNetworkObject(NetworkGameState o) {
		for (int i = 0; i < o.raceBuffer.getReceiveBuffer().size(); i++) {
			Race r = o.raceBuffer.getReceiveBuffer().get(i).get();
			receivedRaces.enqueue(r);
			
		}
		for (int i = 0; i < o.raceModBuffer.getReceiveBuffer().size(); i++) {
			RaceModification r = o.raceModBuffer.getReceiveBuffer().get(i)
					.get();
			receivedMods.enqueue(r);
		}
		
	}

	public void initFromNetworkObject(NetworkGameState o) {

		updateFromNetworkObject(o);
	}

	public boolean isOnServer() {
		return onServer;
	}

	public List<Race> getActiveRaces() {
		return activeRaces;
	}

	public Race getSelectedRaceClient() {
		return selectedRaceClient;
	}

	public void setSelectedRaceClient(Race selectedRaceClient) {
		this.selectedRaceClient = selectedRaceClient;
		notifyObservers();
	}

	public void onPassGate(AbstractOwnerState ownerState,
			RacegateCollectionManager elementCollectionManager) {
		for (Race r : activeRaces) {
			if (r.isParticipant(ownerState)) {
				r.onPassGate(ownerState, elementCollectionManager, this);
				return;
			}
		}
	}

	public boolean isInRunningRace(AbstractOwnerState player) {
		for (Race r : activeRaces) {
			if (r.isStarted() && r.isParticipantActive(player) && !r.isFinished()) {
				return true;
			}
		}
		return false;
	}

	public Race getRace(AbstractOwnerState player) {
		for (Race r : activeRaces) {
			if (r.isParticipant(player)) {
				return r;
			}
		}
		return null;
	}
	public RaceState getRaceState(AbstractOwnerState player) {
		Race race = getRace(player);
		if(race != null){
			return race.getRaceState(player);
		}
		return null;
	}

}
