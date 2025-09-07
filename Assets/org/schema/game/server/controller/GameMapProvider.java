package org.schema.game.server.controller;

import api.listener.events.network.GameMapServerSendEntriesEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.gamemap.GameMap;
import org.schema.game.client.data.gamemap.entry.GasPlanetEntityMapEntry;
import org.schema.game.client.data.gamemap.entry.MapEntryInterface;
import org.schema.game.client.data.gamemap.entry.PlanetEntityMapEntry;
import org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry;
import org.schema.game.client.data.gamemap.requests.GameMapAnswer;
import org.schema.game.client.data.gamemap.requests.GameMapRequest;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.remote.RemoteMapEntryAnswer;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerGameMapRequest;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class GameMapProvider extends Thread {

	private final GameServerState state;
	private final ObjectArrayFIFOQueue<ServerGameMapRequest> requests = new ObjectArrayFIFOQueue<ServerGameMapRequest>();
	private final ObjectArrayFIFOQueue<Answer> answers = new ObjectArrayFIFOQueue<Answer>();
	private Object2ObjectOpenHashMap<Vector3i, CacheEntry> cache = new Object2ObjectOpenHashMap<Vector3i, CacheEntry>();
	private boolean shutdown;

	public GameMapProvider(GameServerState state) {
		super("GameMapProvider");
		setDaemon(true);
		this.state = state;

		start();
	}

	public static Vector3f getRelativePos(Vector3f entityPos, Vector3i sectorPos, float sectorSize) {
		float m = sectorSize;
		float fac = 100f / VoidSystem.SYSTEM_SIZEf;
		return new Vector3f(
				((entityPos.x + m / 2f) / m) * fac + (ByteUtil.modU16(sectorPos.x) / VoidSystem.SYSTEM_SIZEf) * 100,
				((entityPos.y + m / 2f) / m) * fac + (ByteUtil.modU16(sectorPos.y) / VoidSystem.SYSTEM_SIZEf) * 100,
				((entityPos.z + m / 2f) / m) * fac + (ByteUtil.modU16(sectorPos.z) / VoidSystem.SYSTEM_SIZEf) * 100);
	}

	public void shutdown() {
		shutdown = true;
		synchronized(requests) {
			requests.notifyAll();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			while(!shutdown) {
				ServerGameMapRequest req;
				synchronized(requests) {

					while(requests.isEmpty()) {
						try {
							requests.wait();
							if(shutdown) {
								return;
							}
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					req = requests.dequeue();
				}

				serverHandle(req);
			}
		} catch(Exception e) {
			System.err.println("Exception CRITICAL: GameMapProvider Failed!");
			e.printStackTrace();
			synchronized(state) {
				state.setSynched();
				state.getController().broadcastMessageAdmin(Lng.astr("Game Map Provider failed\n%s\nPLEASE send in server logs immediately\n(else it might get washed out).", e.getClass().getSimpleName()), ServerMessage.MESSAGE_TYPE_ERROR);
				state.setUnsynched();
			}
		}
	}

	private void serverHandle(ServerGameMapRequest req) {
		GameMapAnswer answer = new GameMapAnswer(req.type, req.pos);

		if(req.type == GameMap.TYPE_SYSTEM) {
			try {
				Vector3i start = new Vector3i(req.pos.x * VoidSystem.SYSTEM_SIZEf, req.pos.y * VoidSystem.SYSTEM_SIZEf, req.pos.z * VoidSystem.SYSTEM_SIZEf);
				Vector3i end = new Vector3i(
						req.pos.x * VoidSystem.SYSTEM_SIZEf + VoidSystem.SYSTEM_SIZEf,
						req.pos.y * VoidSystem.SYSTEM_SIZEf + VoidSystem.SYSTEM_SIZEf,
						req.pos.z * VoidSystem.SYSTEM_SIZEf + VoidSystem.SYSTEM_SIZEf);
				int[] types = new int[]{
						EntityType.SHOP.dbTypeId,
						EntityType.SPACE_STATION.dbTypeId,
						EntityType.PLANET_SEGMENT.dbTypeId
				};

				Iterator<CacheEntry> it = cache.values().iterator();
				long time = System.currentTimeMillis();
				while(it.hasNext()) {
					CacheEntry next = it.next();
					if(time - next.time > CacheEntry.CACHE_TIMEOUT) {
						it.remove();
					}
				}

				List<DatabaseEntry> bySectorRange;
				CacheEntry cacheEntry = cache.get(start);
				if(cacheEntry != null) {
					bySectorRange = cacheEntry.entries;
				} else {
					bySectorRange = state.getDatabaseIndex().getTableManager().getEntityTable().getBySectorRange(start, end, types);
					CacheEntry e = new CacheEntry();
					e.entries = bySectorRange;
					e.time = System.currentTimeMillis();
					cache.put(start, e);
				}

				ObjectOpenHashSet<TransformableEntityMapEntry> entries = new ObjectOpenHashSet<TransformableEntityMapEntry>(bySectorRange.size());

				ObjectOpenHashSet<Vector3i> poses = new ObjectOpenHashSet<Vector3i>(bySectorRange.size());
				synchronized(state) {
					state.setSynched();
					synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
						for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
							if(s instanceof SimpleTransformableSendableObject<?> obj) {
								Sector sector = state.getUniverse().getSector(obj.getSectorId());
								if(sector != null && sector.pos.betweenIncExcl(start, end)) {
									if(obj instanceof ShopSpaceStation || obj instanceof SpaceStation || obj instanceof PlanetIco || obj instanceof GasPlanet) {
										TransformableEntityMapEntry t = null;
										switch(obj) {
											case ShopSpaceStation shopSpaceStation -> {
												t = new TransformableEntityMapEntry();
												t.type = (byte) obj.getType().ordinal();
												t.pos = getRelativePos(obj.getWorldTransform().origin, sector.pos, state.getSectorSize());
												t.name = obj.getRealName();
												t.sector = obj.getSector(new Vector3i());
											}
											case PlanetIco planetIco -> {
												t = new PlanetEntityMapEntry();
												t.type = (byte) obj.getType().ordinal();
												((PlanetEntityMapEntry) t).planetType = planetIco.getPlanetType();
												t.pos = getRelativePos(planetIco.getCore().getWorldTransform().origin, sector.pos, state.getSectorSize());
												t.name = planetIco.getCore().getRealName();
												t.sector = obj.getSector(new Vector3i());
											}
											case GasPlanet gasPlanet -> {
												t = new GasPlanetEntityMapEntry();
												t.type = (byte) obj.getType().ordinal();
												((GasPlanetEntityMapEntry) t).planetType = gasPlanet.getPlanetInfo().getType();
												t.pos = getRelativePos(obj.getWorldTransform().origin, sector.pos, state.getSectorSize());
												t.name = obj.getRealName();
												t.sector = obj.getSector(new Vector3i());
											}
											case SpaceStation spaceStation -> {
												t = new TransformableEntityMapEntry();
												t.type = (byte) obj.getType().ordinal();
												t.pos = getRelativePos(obj.getWorldTransform().origin, sector.pos, state.getSectorSize());
												t.name = obj.getRealName();
												t.sector = obj.getSector(new Vector3i());
											}
											default -> throw new IllegalArgumentException("Unknown Type: " + obj);
										}

										poses.add(sector.pos);
										entries.add(t);
									}
								} else {
//									System.err.println("[SERVER][MAPPROVIDER] WARNING: requested unloaded sector: " + obj + ": " + " in " + sector);
								}
							}
						}
					}
					state.setUnsynched();
				}

				for(int i = 0; i < bySectorRange.size(); i++) {
					DatabaseEntry e = bySectorRange.get(i);

					TransformableEntityMapEntry t = null;

					EntityType entityType = e.getEntityType();
					if(entityType == EntityType.PLANET_SEGMENT || entityType == EntityType.PLANET_ICO) {
						t = new PlanetEntityMapEntry();
						((PlanetEntityMapEntry) t).planetType = PlanetType.values()[e.creatorID];
					} else {
						t = new TransformableEntityMapEntry();
					}

					if(!poses.contains(e.sectorPos)) {
						t.type = (byte) entityType.ordinal();
						t.pos = getRelativePos(e.pos, e.sectorPos, state.getSectorSize());
						t.name = e.realName;
						poses.add(e.sectorPos);
						entries.add(t);
					}
				}
				StellarSystem stellarSystemFromStellarPos;
				synchronized(state) {
					state.setSynched();
					stellarSystemFromStellarPos = state.getUniverse().getStellarSystemFromStellarPos(req.pos);
					state.setUnsynched();
				}
				Vector3i sectorPos = new Vector3i();
				for(int z = 0, index = 0; z < VoidSystem.SYSTEM_SIZE; z++) {
					for(int y = 0; y < VoidSystem.SYSTEM_SIZE; y++) {
						for(int x = 0; x < VoidSystem.SYSTEM_SIZE; x++) {
							SectorType sectorType = stellarSystemFromStellarPos.getSectorType(index);
							sectorPos.set(start);
							sectorPos.add(x, y, z);

							if(!poses.contains(sectorPos)) {
								//sector neither in db or loaded ingame
								if(sectorType == SectorType.PLANET) {

									PlanetEntityMapEntry t = new PlanetEntityMapEntry();
									t.type = (byte) EntityType.PLANET_SEGMENT.ordinal();
									t.pos = getRelativePos(new Vector3f(0, 0, 0), sectorPos, state.getSectorSize());
									t.name = Lng.str("Undiscovered Planet: %s", sectorPos.toStringPure());
									t.planetType = stellarSystemFromStellarPos.getPlanetType(index);
									entries.add(t);
								} else if(sectorType == SectorType.SPACE_STATION) {

									TransformableEntityMapEntry t = new TransformableEntityMapEntry();
									t.type = (byte) EntityType.SPACE_STATION.ordinal();
									t.pos = getRelativePos(new Vector3f(0, 0, 0), sectorPos, state.getSectorSize());
									t.name = Lng.str("Undiscovered Station: %s", sectorPos.toStringPure());

									SpaceStationType spaceStationTypeType = stellarSystemFromStellarPos.getSpaceStationTypeType(index);
									entries.add(t);
								} else if(sectorType == SectorType.GAS_PLANET) {
									GasPlanetEntityMapEntry t = new GasPlanetEntityMapEntry();
									t.type = (byte) EntityType.PLANET_SEGMENT.ordinal();
									t.pos = getRelativePos(new Vector3f(0, 0, 0), sectorPos, state.getSectorSize());
									t.name = Lng.str("Undiscovered Planet: %s", sectorPos.toStringPure());
									t.planetType = stellarSystemFromStellarPos.getGasPlanetType(index);
									entries.add(t);
									//Todo: Gas Planet
								}
							}
							index++;
						}
					}
				}

				answer.data = new TransformableEntityMapEntry[entries.size()];
				int i = 0;
				for(TransformableEntityMapEntry t : entries) {
					answer.data[i] = t;
					i++;
				}

			} catch(SQLException e) {
				e.printStackTrace();
				assert (false);
			} catch(IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("[SERVER][GAMEMAP] request type " + req.type + " is unknown");
			assert (false);
			return;

		}

		assert (answer.data != null);
		synchronized(answers) {
			answers.enqueue(new Answer(req, new RemoteMapEntryAnswer(answer, true)));
		}

	}

	public void updateFromNetworkObject(ClientChannel ntState) {
		for(int i = 0; i < ntState.getNetworkObject().mapRequests.getReceiveBuffer().size(); i++) {
			GameMapRequest gameMapRequest = ntState.getNetworkObject().mapRequests.getReceiveBuffer().get(i).get();
			addRequestServer(gameMapRequest, ntState);
		}
	}

	public void updateServer() {

		if(!answers.isEmpty()) {
			synchronized(answers) {
				while(!answers.isEmpty()) {
					Answer ans = answers.dequeue();
					if(ans.req == null) {
						try {
							throw new IllegalArgumentException("ans.req == null");
						} catch(Exception e) {
							e.printStackTrace();
						}
						continue;
					}
					if(ans.req.gameState == null) {
						try {
							throw new IllegalArgumentException("ans.req.gameState == null");
						} catch(Exception e) {
							e.printStackTrace();
						}
						continue;
					}
					if(ans.req.gameState.getNetworkObject() == null) {
						try {
							throw new IllegalArgumentException("ans.req.gameState.getNetworkObject() == null");
						} catch(Exception e) {
							e.printStackTrace();
						}
						continue;
					}
					if(ans.req.gameState.getNetworkObject().mapAnswers == null) {
						try {
							throw new IllegalArgumentException("ans.req.gameState.getNetworkObject().mapAnswers == null");
						} catch(Exception e) {
							e.printStackTrace();
						}
						continue;
					}
					if(ans.answer == null) {
						try {
							throw new IllegalArgumentException("ans.answer == null");
						} catch(Exception e) {
							e.printStackTrace();
						}
						continue;
					}
					//INSERTED CODE
					GameMapServerSendEntriesEvent event = new GameMapServerSendEntriesEvent(ans);
					StarLoader.fireEvent(event, true);
					ans.answer.get().data = event.getDataArray().toArray(new MapEntryInterface[0]);
					///
					ans.req.gameState.getNetworkObject().mapAnswers.add(ans.answer);
				}
			}
		}
	}

	public void addRequestServer(GameMapRequest request, ClientChannel channel) {
		synchronized(requests) {
			requests.enqueue(new ServerGameMapRequest(request, channel));
			requests.notify();
		}
	}

	public void updateMapForAllInSystem(Vector3i sysPos) {

		Vector3i playerPos = new Vector3i();
		for(PlayerState pState : state.getPlayerStatesByName().values()) {
			StellarSystem.getPosFromSector(new Vector3i(pState.getCurrentSector()), playerPos);
			if(playerPos.equals(sysPos)) {
				addRequestServer(new GameMapRequest(GameMap.TYPE_SYSTEM, sysPos), pState.getClientChannel());
			}
		}

	}

	public class Answer {
		public final ServerGameMapRequest req;
		public final RemoteMapEntryAnswer answer;

		public Answer(ServerGameMapRequest req, RemoteMapEntryAnswer answer) {
			super();
			this.req = req;
			this.answer = answer;
		}

	}

	private class CacheEntry {
		public static final long CACHE_TIMEOUT = 60000 * 10;
		private long time;
		private List<DatabaseEntry> entries;
	}

}
