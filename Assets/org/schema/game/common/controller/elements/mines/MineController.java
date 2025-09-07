package org.schema.game.common.controller.elements.mines;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.controller.ClientSectorChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.mines.Mine.MineDataException;
import org.schema.game.common.data.mines.MineActivityLevelContainer;
import org.schema.game.common.data.mines.MineActivityLevelContainer.ActiveLevel;
import org.schema.game.common.data.mines.ServerMineManager;
import org.schema.game.common.data.mines.updates.MineUpdate;
import org.schema.game.common.data.mines.updates.MineUpdate.MineUpdateType;
import org.schema.game.common.data.mines.updates.MineUpdateArmMinesRequest;
import org.schema.game.common.data.mines.updates.MineUpdateClearSector;
import org.schema.game.common.data.mines.updates.MineUpdateMineAdd;
import org.schema.game.common.data.mines.updates.MineUpdateMineAmmoChange;
import org.schema.game.common.data.mines.updates.MineUpdateMineArmedChange;
import org.schema.game.common.data.mines.updates.MineUpdateMineHit;
import org.schema.game.common.data.mines.updates.MineUpdateMineRemove;
import org.schema.game.common.data.mines.updates.MineUpdateSectorData;
import org.schema.game.common.data.mines.updates.MineUpdateSectorData.MineData;
import org.schema.game.common.data.mines.updates.MineUpdateSectorRequest;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.remote.RemoteMineUpdate;
import org.schema.game.server.controller.SectorListener;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MineController implements SectorListener, ClientSectorChangeListener{
	private final Int2ObjectOpenHashMap<Mine> clientMinesAll = new Int2ObjectOpenHashMap<Mine>();
	private final Int2ObjectOpenHashMap<ObjectArrayList<Mine>> clientMinesSector = new Int2ObjectOpenHashMap<ObjectArrayList<Mine>>();
	private final Int2ObjectOpenHashMap<ServerMineManager> serverMineManager;
	private final StateInterface state;
	private final Set<Sector> sectorsChanged = new ObjectOpenHashSet<Sector>(); 
	private final Set<Sector> sectorsAdded = new ObjectOpenHashSet<Sector>(); 
	private final Set<Sector> sectorsRemoved = new ObjectOpenHashSet<Sector>();
	private final List<ClientMineListener> mineListeners = new ObjectArrayList<ClientMineListener>();
	private final boolean onServer;
	private int lastSector;
	private final ObjectArrayFIFOQueue<MineControllerUpdate> receivedUpdates = new ObjectArrayFIFOQueue<MineControllerUpdate>();
	private int idGen;
	private boolean databaseUpdateIdGen;
	private final MineActivityLevelContainer clientActivityLvl = new MineActivityLevelContainer();
	
	private final static String DATABASE_ID_GEN = "MINE_GEN";
	public MineController(StateInterface state) {
		this.state = state;
		this.onServer = state instanceof GameServerState;
		if(onServer) {
			serverMineManager = new Int2ObjectOpenHashMap<ServerMineManager>();
			long l;
			try {
				l = ((GameServerState)state).getDatabaseIndex().getTableManager().getIdGenTable().getId(DATABASE_ID_GEN);
				this.idGen = (int)l;
				System.err.println("[MINE] IDGEN = "+this.idGen);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}else {
			serverMineManager = null;
			
			((GameClientState)state).getController().addSectorChangeListener(this);
		}
		
		assert(MineUpdateType.check()):"MINE CHECK";
	}
	public int getNewId() {
		int id = idGen++;
		databaseUpdateIdGen = true;
		try {
			((GameServerState)state).getDatabaseIndex().getTableManager().getIdGenTable().updateOrInsert(DATABASE_ID_GEN, idGen);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.err.println("[MINE] NEW MINE ID = "+id);
		return id;
	}
	private final Vector3i secTmp = new Vector3i();
	public List<Mine> getMinesInRange(SimpleTransformableSendableObject<?> from, float distance, List<Mine> out) {
		if(onServer) {
			Sector sector = ((GameServerState) state).getUniverse().getSector(from.getSectorId());
			if(sector == null) {
				return out;
			}
			for(int z = -1; z <= 1; z++) {
				for(int y = -1; y <= 1; y++) {
					for(int x = -1; x <= 1; x++) {
						secTmp.set(sector.pos.x+x, sector.pos.y+y, sector.pos.z+z);
						
						Sector sec = ((GameServerState) state).getUniverse().getSectorWithoutLoading(secTmp);
						if(sec != null) {
							ServerMineManager mm = serverMineManager.get(sec.getSectorId());
							if(mm != null) {
								mm.getMinesInRange(from, distance, out);
							}
						}
					}
				}
			}
			return out;
		}else {
			for(Mine m : clientMinesAll.values()) {
				if(m.getDistanceTo(from) <= distance) {
					out.add(m);
				}
			}
		}
		return out;
	}
	public List<Mine> getMinesInRange(int sectorFrom, Vector3f from, float distance, List<Mine> out) {
		if(onServer) {
			Sector sector = ((GameServerState) state).getUniverse().getSector(sectorFrom);
			if(sector == null) {
				return out;
			}
			for(int z = -1; z <= 1; z++) {
				for(int y = -1; y <= 1; y++) {
					for(int x = -1; x <= 1; x++) {
						secTmp.set(sector.pos.x+x, sector.pos.y+y, sector.pos.z+z);
						
						Sector sec = ((GameServerState) state).getUniverse().getSectorWithoutLoading(secTmp);
						if(sec != null) {
							ServerMineManager mm = serverMineManager.get(sec.getSectorId());
							if(mm != null) {
								mm.getMinesInRange(sectorFrom, from, distance, out);
							}
						}
					}
				}
			}
			return out;
		}else {
			for(Mine m : clientMinesAll.values()) {
				if(m.getDistanceTo(sectorFrom, from) <= distance) {
					out.add(m);
				}
			}
		}
		return out;
	}
	
	private void updateServer(Timer timer) {
		GameServerState state = (GameServerState)this.state;
		for(Sector s : sectorsAdded) {
			ServerMineManager mm = serverMineManager.get(s.getId());
			if(mm == null) {
				mm = new ServerMineManager( state, s);
				mm.loadServer();
				serverMineManager.put(s.getId(), mm);
			}
		}
		sectorsAdded.clear();
		
		
		for(Sector s : sectorsRemoved) {
			ServerMineManager mm = serverMineManager.remove(s.getId());
			if(mm != null) {
				mm.unloadServer();
			}
		}
		sectorsRemoved.clear();
		
		
		for(Sector s : sectorsChanged) {
			ServerMineManager mm = serverMineManager.get(s.getId());
			if(mm != null) {
				mm.onSectorEntitesChanged();
			}
		}
		sectorsChanged.clear();
		
		if(databaseUpdateIdGen) {
			databaseUpdateIdGen = false;
		}
		for(ServerMineManager m : serverMineManager.values()) {
			Sector sector = state.getUniverse().getSector(m.getSectorId());
			if(sector != null && sector.isActive()) {
				m.updateLocal(timer);
			}
		}
	}
	public void updateLocal(Timer timer) {
		
		while(!receivedUpdates.isEmpty()) {
			MineControllerUpdate d = receivedUpdates.dequeue();
			d.m.execute(d.clientChannel, this);
		}
		if(onServer) {
			updateServer(timer);
		}else {
			updateClient(timer);
		}
		
	}
	
	private void updateClient(Timer timer) {
		
		GameClientState state = (GameClientState)this.state;
		if(state.getCurrentRemoteSector() == null) {
			return;
		}
		if(state.getController().getClientChannel().isConnectionReady() && lastSector != state.getCurrentSectorId()) {
			removeMinesInObsoleteSectorClient(state);
			requestMinesInSectorsClient(state);
			lastSector = state.getCurrentSectorId();
		}
		clientActivityLvl.updateLocal(timer, this);
	}
	private void requestMinesInSectorsClient(GameClientState state) {
		
		Vector3i clientPos = state.getCurrentRemoteSector().clientPos();
		for(int z = -1; z <= 1; z++) {
			for(int y = -1; y <= 1; y++) {
				for(int x = -1; x <= 1; x++) {
					MineUpdateSectorRequest r = new MineUpdateSectorRequest();
					r.clientId = state.getId();
					r.s = new Vector3i(clientPos);
					r.s.add(x, y, z);
					state.getController().getClientChannel().getNetworkObject().mineUpdateBuffer.add(new RemoteMineUpdate(r, onServer));
				}
			}
		}
		
	}
	private void removeMinesInObsoleteSectorClient(GameClientState state) {
		ObjectIterator<Mine> iterator = clientMinesAll.values().iterator();
		while(iterator.hasNext()) {
			Mine m = iterator.next();
			if(!state.getController().isNeighborToClientSector(m.getSectorId())){
				iterator.remove();
				onRemovedMineClient(m);
			}
		}
	}
	private void onRemovedMineClient(Mine m) {
		ObjectArrayList<Mine> s = clientMinesSector.get(m.getSectorId());
		if(s != null) {
			s.remove(m);
		}
		
		clientActivityLvl.remove(m);
		
		for(int i = 0; i < mineListeners.size(); i++) {
			mineListeners.get(i).onRemovedMine(m);
		}
	}
	public boolean existsClient(Mine m) {
		return clientMinesAll.containsKey(m.getId());
	}
	public ServerMineManager getServerManager(Mine m) {
		return serverMineManager.get(m.getSectorId());
	}
	public boolean existsServer(Mine m) {
		ServerMineManager mm = getServerManager(m);
		return mm != null && mm.contains(m);
	}

	public void onSectorAdded(Sector sec) {
		sectorsAdded.add(sec);
	}
	public void onSectorRemoved(Sector sec) {
		sectorsRemoved.add(sec);
		
	}

	
	public void onSectorEntityAdded(SimpleTransformableSendableObject s, Sector sector) {
		sectorsChanged.add(sector);
	}
	public void onSectorEntityRemoved(SimpleTransformableSendableObject s, Sector sector) {
		sectorsChanged.add(sector);
	}
	public boolean isOnServer() {
		return onServer;
	}
	public void updateActiveLevel(Mine mine, ActiveLevel old, ActiveLevel a) {
		if(onServer) {
			ServerMineManager mm = getServerManager(mine);
			if(mm != null) {
				mm.updateActiveLevel(mine, old, a);
			}
		}else {
			clientActivityLvl.updateActiveLevel(mine, old, a);
		}
	}
	private static class MineControllerUpdate{
		private ClientChannel clientChannel;
		private MineUpdate m;
		
		public MineControllerUpdate(ClientChannel clientChannel, MineUpdate m) {
			this.clientChannel = clientChannel;
			this.m = m;
		}

	}
	public void receivedUpdate(ClientChannel clientChannel, MineUpdate m) {
		MineControllerUpdate c = new MineControllerUpdate(clientChannel, m);
		this.receivedUpdates.enqueue(c); 
	}
	
	public void removeMineServer(Mine mine) {
		deleteMineServer(mine);
	}
	public void deleteMineServer(Mine mine) {
		GameServerState state = (GameServerState)this.state;
		ServerMineManager mm = getServerManager(mine);
		if(mm != null) {
			mm.removeMine(mine);
			sendDeleteMine(mine);
			
		}else {
			assert(false):"Np sector for "+mine;
		}
	}
	private void sendDeleteMine(Mine mine) {
		assert(mine.getSectorPos() != null):mine;
		MineUpdateMineRemove up = new MineUpdateMineRemove();
		up.mineId = mine.getId();
		
		sendToSurroundingClient(mine.getSectorPos(), up);		
	}
	public void addMineServer(Mine mine) {
		GameServerState state = (GameServerState)this.state;
		ServerMineManager mm = getServerManager(mine);
		if(mm != null) {
			mm.addMine(mine);
			
			MineUpdateMineAdd up = new MineUpdateMineAdd();
			up.m = new MineData();
			up.m.setFrom(mine);
			up.sectorId = mine.getSectorId();
			sendToSurroundingClient(mine.getSectorPos(), up);
			
		}else {
			assert(false):"No sector for "+mine;
		}
	}
	
	public void sendToSurroundingClient(Vector3i sectorPos, MineUpdate up) {
		assert(sectorPos != null);
		GameServerState state = (GameServerState)this.state;
		for(RegisteredClientOnServer c : state.getClients().values()) {
			if(c.getPlayerObject() instanceof PlayerState) {
				PlayerState s = (PlayerState)c.getPlayerObject();
				if(s.getClientChannel() != null && s.getClientChannel().isConnectionReady() && Sector.isNeighbor(sectorPos, s.getCurrentSector())) {
					s.getClientChannel().getNetworkObject().mineUpdateBuffer.add(new RemoteMineUpdate(up, onServer));
				}
			}
		}
	}
	public Mine addMineClient(MineData data, int sectorId) throws MineDataException {
		Mine m = new Mine(state);
		m.setFrom(sectorId, data);
		clientMinesAll.put(m.getId(), m);
		
		onAddMineClient(m);
		
		
		return m;
	}
	private void onChangedMineClient(Mine m) {
		for(int i = 0; i < mineListeners.size(); i++) {
			mineListeners.get(i).onChangedMine(m);
		}
	}
	private void onAddMineClient(Mine m) {
		ObjectArrayList<Mine> s = clientMinesSector.get(m.getSectorId());
		if(s == null) {
			s = new ObjectArrayList<Mine>();
			clientMinesSector.put(m.getSectorId(), s);
		}
		s.add(m);
		clientActivityLvl.add(m);
		m.updateClientTransform();
		
		for(int i = 0; i < mineListeners.size(); i++) {
			mineListeners.get(i).onAddMine(m);
		}		
	}
	public void changeMineHpClient(int mineId, short hp) {
		Mine m = clientMinesAll.get(mineId);
		if(m != null) {
			m.setHp(hp);
			onChangedMineClient(m);
		}
	}
	public void changeMineAmmoClient(int mineId, short ammo) {
		Mine m = clientMinesAll.get(mineId);
		if(m != null) {
			m.setAmmo(ammo);
			onChangedMineClient(m);
		}
	}
	public void removeMineClient(int mineId) {
		Mine remove = clientMinesAll.remove(mineId);
		if(remove != null) {
			
			onRemovedMineClient(remove);
		}
	}
	public void handleClientRequest(ClientChannel clientChannel, Vector3i s) {
		GameServerState state = (GameServerState)this.state;
		try {
			Sector sector = state.getUniverse().getSector(s, false);
			if(sector != null) {
				ServerMineManager mm = serverMineManager.get(sector.getId());
				if(mm != null) {
					
					MineUpdateSectorData d = new MineUpdateSectorData();
					d.setFrom(mm);
					clientChannel.getNetworkObject().mineUpdateBuffer.add(new RemoteMineUpdate(d, onServer));
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void setArmedAndSendWithoutDbChange(ServerMineManager mm, long ownerId, boolean armed) {
		for(Mine mine : mm.getMines().values()) {
//			System.err.println("MINE: "+mine.getOwnerId()+"; "+ownerId+"; "+mine.isArmed()+"; "+(mine.getOwnerId() == ownerId && !mine.isArmed()));
			if(mine.getOwnerId() == ownerId && !mine.isArmed()) {
				mine.setArmed(true);
				sendArmed(mine);
			}
		}
	}
	public void handleClientArmRequest(ClientChannel clientChannel, boolean all, Vector3i s) {
		GameServerState state = (GameServerState)this.state;
		final boolean armed = true;
		final long ownerId = clientChannel.getPlayer().getDbId();
		try {
			if(all) {
				for(ServerMineManager mm : serverMineManager.values()) {
					setArmedAndSendWithoutDbChange(mm, ownerId, armed);
				}
				state.getDatabaseIndex().getTableManager().getMinesTable().armMines(ownerId, armed);
			}else {
				Sector sector = state.getUniverse().getSectorWithoutLoading(s);
				if(sector != null) {
					ServerMineManager mm = serverMineManager.get(sector.getId());
					if(mm != null) {
						setArmedAndSendWithoutDbChange(mm, ownerId, armed);
					}
				}
				state.getDatabaseIndex().getTableManager().getMinesTable().armMines(ownerId, s.x, s.y, s.z, armed);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public Mine createMineServer(GameServerState state, int sectorId,
			Vector3f localPos, AbstractOwnerState ownerState, int factionId, int mineArmedInSecs, short[] composition) throws MineDataException {
		Mine m = new Mine(state);
		m.setId(getNewId());
		m.initialize(state, sectorId, localPos, ownerState, factionId, mineArmedInSecs, composition);
		return m;
	}
	public void addClientMineListener(ClientMineListener l) {
		mineListeners.add(l);
	}
	public boolean exists(Mine mine) {
		if(onServer) {
			return existsServer(mine);
		}else {
			return existsClient(mine);
		}
	}
	@Override
	public void onSectorChangeSelf(int newSector, int oldSector) {
		List<Mine> toDelete = new ObjectArrayList<Mine>();
		for(Mine m : clientMinesAll.values()) {
			if(!((GameClientState)state).getController().isNeighborToClientSector(m.getSectorId())) {
				toDelete.add(m);
			}else {
				m.updateClientTransform();
			}
		}
		for(Mine m : toDelete) {
			removeMineClient(m.getId());
		}
		
		clientActivityLvl.onSectorChange(((GameClientState)state).getCurrentSectorEntities().values(), clientMinesAll.values());
	}
	public void clearMinesInSectorServer(int x, int y, int z) {
		try {
			((GameServerState)state).getDatabaseIndex().getTableManager().getMinesTable().clearSector(x,y,z);
			Sector sector = ((GameServerState)state).getUniverse().getSector(new Vector3i(x,y,z));
			ServerMineManager mm = serverMineManager.get(sector.getId());
			if(mm != null) {
				mm.clearMines();
				MineUpdateClearSector up = new MineUpdateClearSector();
				up.sectorId = sector.getId();
				sendToSurroundingClient(new Vector3i(x,y,z), up);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void clearClientMinesInSector(int sectorId) {
		
		List<Mine> toDelete = new ObjectArrayList<Mine>();
		for(Mine m : clientMinesAll.values()) {
			if(m.getSectorId() == sectorId) {
				toDelete.add(m);
			}
		}
		for(Mine m : toDelete) {
			removeMineClient(m.getId());
		}
	}
	public void handleHitClient(int sectorId, Vector3f posBeforeUpdate, Vector3f posAfterUpdate) {
		for(Mine mine : clientMinesAll.values()) {
			if(!mine.hit && mine.isHitClient(posBeforeUpdate, posAfterUpdate)) {
				mine.hit = true;
				MineUpdateMineHit up = new MineUpdateMineHit();
				up.mineId = mine.getId();
				up.sectorId = mine.getSectorId();
				((GameClientState)state).getController().getClientChannel().getNetworkObject().mineUpdateBuffer.add(new RemoteMineUpdate(up, onServer));
				System.err.println("[CLIENT] MINE HIT ON CLIENT "+mine);
				
			}
		}
	}
	public void handleHit(int sectorId, Vector3f posBeforeUpdate, Vector3f posAfterUpdate) {
		ServerMineManager mm = serverMineManager.get(sectorId);
		if(mm != null) {
			Int2ObjectOpenHashMap<Mine> mines = mm.getMines();
			for(Mine mine : mines.values()) {
				if(!mine.hit && mine.isHitServer(posBeforeUpdate, posAfterUpdate)) {
					mine.hit = true;
					mineHitServer(mine.getId(), sectorId);
					//call recursively to iterate again to hit more than one mine
					handleHit(sectorId, posBeforeUpdate, posAfterUpdate);
					return;
				}
			}
		}
	}
	public void mineHitServer(int mineId, int sectorId) {
		ServerMineManager mm = serverMineManager.get(sectorId);
		if(mm != null) {
			Mine mine = mm.removeMine(mineId);
			System.err.println("[CLIENT] MINE HIT ON SERVER "+mine);
			if(mine != null) {
				sendDeleteMine(mine);
			}
		}
	}
	
	/**
	 * used by both server and client to update mines
	 * @param timer
	 * @param tmpMineUpdate
	 */
	public void handleMineUpdate(Timer timer, List<Mine> tmpMineUpdate) {
		final int size = tmpMineUpdate.size();
		for(int c = 0; c < size; c++) {
			Mine mine = tmpMineUpdate.get(c);
			mine.checkArming(timer);
			
			if(mine.isArmed()) {
				if(mine.getAmmo() < 0) {
					mine.handleOutOfAmmo(timer);
				}
				mine.updateActivityLevelAndDetect();
				if(mine.isActive()) {
					mine.handleActive(timer);
				}else {
					mine.handleInactive(timer);
				}
			}
		}
	}
	public final StateInterface getState() {
		return state;
	}
	
	
	public Mine getMine(int id) {
		if(onServer) {
			for(ServerMineManager mm : serverMineManager.values()) {
				Mine mine = mm.getMines().get(id);
				if(mine != null) {
					return mine;
				}
			}
		}else {
			return clientMinesAll.get(id);
		}
		return null;
	}
	public void handleMineCollisions(Timer timer, List<Mine> mines) {
		if(onServer) {
			
			for(Mine m : mines) {
				m.handleCollision(timer);
			}
		}
	}
	public void requestArmedAllClient(long ownerId) {
		System.err.println("[CLIENT] Requesting all mines armed");
		assert(!onServer);
		MineUpdateArmMinesRequest up = new MineUpdateArmMinesRequest();
		up.all = true;
		up.clientId = ((GameClientState) state).getId();
		((GameClientState) state).getController().getClientChannel().getNetworkObject().mineUpdateBuffer.add(new RemoteMineUpdate(up, onServer));
	}
	public void requestArmedClient(long ownerId, Vector3i ... sectors) {
		System.err.println("[CLIENT] Requesting mines armed in sectors "+Arrays.toString(sectors));
		assert(!onServer);
		for(Vector3i sec : sectors) {
			MineUpdateArmMinesRequest up = new MineUpdateArmMinesRequest();
			up.all = false;
			up.s = new Vector3i(sec);
			up.clientId = ((GameClientState) state).getId();
			((GameClientState) state).getController().getClientChannel().getNetworkObject().mineUpdateBuffer.add(new RemoteMineUpdate(up, onServer));
		}
	}
	public void sendAmmo(Mine mine) {
		MineUpdateMineAmmoChange up = new MineUpdateMineAmmoChange();
		up.mineId = mine.getId();
		up.ammo = mine.getAmmo();
		sendToSurroundingClient(mine.getSectorPos(), up);
	}
	public void sendArmed(Mine mine) {
		MineUpdateMineArmedChange up = new MineUpdateMineArmedChange();
		up.mineId = mine.getId();
		up.armed = mine.isArmed();
		sendToSurroundingClient(mine.getSectorPos(), up);
	}
	public void changeMineArmedClient(int mineId, boolean armed) {
		Mine m = clientMinesAll.get(mineId);
		if(m != null) {
			m.setArmed(armed);
			onChangedMineClient(m);
		}
	}
	public void onBecomingInactive(Mine m) {
		for(int i = 0; i < mineListeners.size(); i++) {
			mineListeners.get(i).onBecomingInactive(m);
		}
	}
	public void onBecomingActive(Mine m) {
		for(int i = 0; i < mineListeners.size(); i++) {
			mineListeners.get(i).onBecomingActive(m);
		}		
	}

	
}
