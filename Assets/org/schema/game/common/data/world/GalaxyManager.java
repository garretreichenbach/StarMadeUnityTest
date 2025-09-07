package org.schema.game.common.data.world;

import api.common.GameClient;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.network.objects.*;
import org.schema.game.network.objects.remote.*;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GalaxyManager {
	public static final int ZONE_SIZE = 32;
	private final StateInterface state;
	private final boolean onServer;
	private final Object2ObjectOpenHashMap<Vector3i, FTLConnection> ftlData = new Object2ObjectOpenHashMap<Vector3i, FTLConnection>();
	private final Object2ObjectOpenHashMap<Vector3i, List<TradeNodeStub>> tradeData = new Object2ObjectOpenHashMap<Vector3i, List<TradeNodeStub>>();
	private final Map<Vector3i, GalaxyZoneRequestAndAwnser> zoneMap = new Object2ObjectOpenHashMap<Vector3i, GalaxyZoneRequestAndAwnser>();
	private final Long2ObjectOpenHashMap<TradeNodeStub> tradeDataById = new Long2ObjectOpenHashMap<TradeNodeStub>();
	private final Vector3i zTmp = new Vector3i();
	private final Vector3i tmpPosServerAnswer = new Vector3i();
	Vector3i reqPos = new Vector3i();
	VoidSystem tmpVoidSystemServer = new VoidSystem();
	private ObjectArrayFIFOQueue<Request> serverRequestsAndClientAwnsers = new ObjectArrayFIFOQueue<Request>();
	private ObjectArrayFIFOQueue<ZoneRequest> serverZoneRequestsAndClientAwnsers = new ObjectArrayFIFOQueue<ZoneRequest>();
	private ObjectArrayFIFOQueue<FowRequestAndAwnser> serverFowRequests = new ObjectArrayFIFOQueue<FowRequestAndAwnser>();
	private ObjectArrayFIFOQueue<VoidSystem> clientSystemsToAdd = new ObjectArrayFIFOQueue<VoidSystem>();
	private Object2LongOpenHashMap<Vector3i> zoneAwnsersOnClient = new Object2LongOpenHashMap<Vector3i>();
	private Object2LongOpenHashMap<Vector3i> requested = new Object2LongOpenHashMap<Vector3i>();
	private ObjectOpenHashSet<Vector3i> requestedZones = new ObjectOpenHashSet<Vector3i>();
	private ObjectArrayFIFOQueue<GalaxyRequestAndAwnser> serverAwnsers = new ObjectArrayFIFOQueue<GalaxyRequestAndAwnser>();
	private ObjectArrayFIFOQueue<FowRequestAndAwnser> serverFowAwnsers = new ObjectArrayFIFOQueue<FowRequestAndAwnser>();
	private ObjectArrayFIFOQueue<FTLConnection> toAddFTLDataOnClient = new ObjectArrayFIFOQueue<FTLConnection>();
	private ObjectArrayFIFOQueue<TradeNodeStub> toAddTradeDataOnClient = new ObjectArrayFIFOQueue<TradeNodeStub>();
	private ObjectArrayFIFOQueue<FowRequestAndAwnser> fowAwnsersOnClient = new ObjectArrayFIFOQueue<FowRequestAndAwnser>();
	private ObjectArrayFIFOQueue<GalaxyZoneRequestAndAwnser> serverZonesAwnsers = new ObjectArrayFIFOQueue<GalaxyZoneRequestAndAwnser>();
	private Object2ObjectOpenHashMap<Vector3i, VoidSystem> clientData = new Object2ObjectOpenHashMap<Vector3i, VoidSystem>();
	private Vector3i cTmp = new Vector3i();
	public final TradeDataListener tradeDataListener = new TradeDataListener();

	public GalaxyManager(StateInterface state) {
		this.state = state;
		onServer = state instanceof ServerStateInterface;
		startThreads();
	}

	public static Vector3i getContainingZoneFromSector(Vector3i sector, int size, Vector3i out) {
		Vector3i systemPos = StellarSystem.getPosFromSector(sector, new Vector3i());
		getContainingZoneFromSystem(systemPos, size, out);
		return out;
	}

	public static Vector3i getContainingZoneFromSystem(Vector3i systemPos, int size, Vector3i out) {
		out.x = ByteUtil.divU32(systemPos.x) * size;
		out.y = ByteUtil.divU32(systemPos.y) * size;
		out.z = ByteUtil.divU32(systemPos.z) * size;
		//		System.err.println("SYSPOS: "+systemPos+": "+ByteUtil.divU32(systemPos.x)+", "+ByteUtil.divU32(systemPos.y)+", "+ByteUtil.divU32(systemPos.z)+" -> "+out);
		return out;
	}

	public boolean isOnServer() {
		return onServer;
	}

	public VoidSystem getSystemOnClient(Vector3i sectorPos) {
		assert (!onServer);
		synchronized(state) {
			boolean needsSynch = !state.isSynched();
			try {
				if(needsSynch) {
					state.setSynched();
				}
				Vector3i sysPos = StellarSystem.getPosFromSector(sectorPos, cTmp);
				VoidSystem voidSystem = clientData.get(sysPos);
				//			System.err.println("SYSPOS: "+sysPos);
				if(voidSystem == null) {
					if(!requested.containsKey(sysPos)) {
						clientRequest(((GameClientState) state).getController().getClientChannel().getNetworkObject(), sectorPos);
					} else {
						assert (requested.getLong(sysPos) > 0) : requested.getLong(sysPos) + "; " + requested.get(sysPos);
						if(System.currentTimeMillis() - requested.getLong(sysPos) > 16000) {
							assert (false) : "NO AWNSER FOR " + sysPos + "; " + requested.getLong(sysPos);
						}
					}
				} else {
					//				System.err.println("GOTTEN FRIN "+sectorPos+": -> "+sysPos+" -> "+voidSystem.getPos()+"; "+voidSystem.getOwnerUID());
				}
				if(!requestedZones.contains(getContainingZoneFromSystem(sysPos, ZONE_SIZE, zTmp))) {
					//requested zones will not clean up
					clientRequestZone(((GameClientState) state).getController().getClientChannel().getNetworkObject(), sectorPos);
				}
				return voidSystem;
			} finally {
				if(needsSynch) {
					state.setUnsynched();
				}
			}
		}
	}

	public void checkSystemBySectorPos(Vector3i sectorPos) {
		assert (!onServer);
		for(int z = -1; z < 2; z++) {
			for(int y = -1; y < 2; y++) {
				for(int x = -1; x < 2; x++) {
					reqPos.set(sectorPos);
					reqPos.add(x * (256), y * (256), z * (256));
					Vector3i sysPos = StellarSystem.getPosFromSector(reqPos, cTmp);
					Vector3i containingZoneFromSystem = getContainingZoneFromSystem(sysPos, ZONE_SIZE, zTmp);
					//						System.err.println("ZONES: "+reqPos+" -> "+sysPos+" -> "+containingZoneFromSystem);
					Vector3i galaxyPos = ((GameClientState) state).getCurrentGalaxy().galaxyPos;
					boolean inX = sysPos.x < galaxyPos.x + Galaxy.halfSize && sysPos.x > galaxyPos.x - Galaxy.halfSize;
					boolean inY = sysPos.y < galaxyPos.y + Galaxy.halfSize && sysPos.y > galaxyPos.y - Galaxy.halfSize;
					boolean inZ = sysPos.z < galaxyPos.z + Galaxy.halfSize && sysPos.z > galaxyPos.z - Galaxy.halfSize;
					if(inX && inY && inZ && !requestedZones.contains(containingZoneFromSystem)) {
						//requested zones will not clean up
						clientRequestZone(((GameClientState) state).getController().getClientChannel().getNetworkObject(), new Vector3i(reqPos));
					}
				}
			}
		}
	}

	public void clientRequest(NetworkClientChannel c, Vector3i sectorPos) {
		synchronized(state) {
			boolean needsSynch = !state.isSynched();
			if(needsSynch) {
				state.setSynched();
			}
			GalaxyRequestAndAwnser g = new GalaxyRequestAndAwnser();
			g.secX = sectorPos.x;
			g.secY = sectorPos.y;
			g.secZ = sectorPos.z;
			Vector3i sysPos = StellarSystem.getPosFromSector(sectorPos, new Vector3i()); //dont use tmp here
			requested.put(sysPos, System.currentTimeMillis());
			synchronized(c.galaxyRequests) {
				System.err.println("[CLIENT] REQUESTING SYSTEM: " + sysPos);
				c.galaxyRequests.add(new RemoteGalaxyRequest(g, c));
			}
			if(needsSynch) {
				state.setUnsynched();
			}
		}
	}

	public void clientRequestZone(NetworkClientChannel c, Vector3i sectorPos) {
		synchronized(state) {
			boolean needsSynch = !state.isSynched();
			if(needsSynch) {
				state.setSynched();
			}
			Vector3i zoneStart = getContainingZoneFromSector(sectorPos, ZONE_SIZE, new Vector3i());
			requestedZones.add(zoneStart);
			GalaxyZoneRequestAndAwnser g = new GalaxyZoneRequestAndAwnser();
			g.zoneSize = ZONE_SIZE;
			g.startSystem = zoneStart;
			synchronized(c.galaxyZoneRequests) {
				c.galaxyZoneRequests.add(new RemoteGalaxyZoneRequest(g, c));
			}
			if(needsSynch) {
				state.setUnsynched();
			}
		}
	}

	public void updateFromNetwork(ClientChannel chan) {
		NetworkClientChannel c = chan.getNetworkObject();
		synchronized(c.galaxyRequests) {
			ObjectArrayList<RemoteGalaxyRequest> receiveBuffer = c.galaxyRequests.getReceiveBuffer();
			for(RemoteGalaxyRequest g : receiveBuffer) {
				enqueueRequestOrAwnser(g.get(), c);
			}
		}
		synchronized(c.galaxyZoneRequests) {
			ObjectArrayList<RemoteGalaxyZoneRequest> receiveBuffer = c.galaxyZoneRequests.getReceiveBuffer();
			for(RemoteGalaxyZoneRequest g : receiveBuffer) {
				enqueueZoneRequestOrAwnser(g.get(), c);
			}
		}
		synchronized(c.galaxyServerMods) {
			ObjectArrayList<RemoteGalaxyRequest> receiveBuffer = c.galaxyServerMods.getReceiveBuffer();
			for(RemoteGalaxyRequest g : receiveBuffer) {
				assert (!onServer);
				enqueueRequestOrAwnser(g.get(), c);
			}
		}
		synchronized(c.ftlUpdatesAndRequests) {
			ObjectArrayList<RemoteFTLConnection> receiveBuffer = c.ftlUpdatesAndRequests.getReceiveBuffer();
			for(RemoteFTLConnection g : receiveBuffer) {
				assert (!onServer);
				synchronized(toAddFTLDataOnClient) {
					toAddFTLDataOnClient.enqueue(g.get());
				}
			}
		}
		synchronized(c.tradeNodeUpdatesAndRequests) {
			ObjectArrayList<RemoteTradeNode> receiveBuffer = c.tradeNodeUpdatesAndRequests.getReceiveBuffer();
			for(RemoteTradeNode g : receiveBuffer) {
				assert (!onServer);
				synchronized(toAddTradeDataOnClient) {
					toAddTradeDataOnClient.enqueue(g.get());
				}
			}
		}
		synchronized(c.fogOfWarRequestsAndAwnsers) {
			ObjectArrayList<RemoteFowRequestAndAwnser> receiveBuffer = c.fogOfWarRequestsAndAwnsers.getReceiveBuffer();
			for(RemoteFowRequestAndAwnser g : receiveBuffer) {
				g.get().receivedClientChannel = chan;
				if(onServer) {
					enqueueFowRequestOrAwnser(g.get(), c);
					//					System.err.println("SERVER RECEIVED VIS REQUEST " + g.get().sysX + ", " + g.get().sysY + "; " + g.get().sysZ + ": " + g.get().visible);
				} else {
					synchronized(fowAwnsersOnClient) {
						fowAwnsersOnClient.enqueue(g.get());
						//						System.err.println("CLIENT RECEIVED VIS AWNSER");
					}
				}
			}
		}
	}

	private void enqueueRequestOrAwnser(GalaxyRequestAndAwnser g, NetworkClientChannel c) {
		synchronized(serverRequestsAndClientAwnsers) {
			serverRequestsAndClientAwnsers.enqueue(new Request(c, g));
			serverRequestsAndClientAwnsers.notify();
		}
	}

	private void enqueueFowRequestOrAwnser(FowRequestAndAwnser g, NetworkClientChannel c) {
		assert (g.receivedClientChannel != null);
		synchronized(serverFowRequests) {
			serverFowRequests.enqueue(g);
			serverFowRequests.notify();
		}
	}

	private void enqueueZoneRequestOrAwnser(GalaxyZoneRequestAndAwnser g, NetworkClientChannel c) {
		synchronized(serverZoneRequestsAndClientAwnsers) {
			serverZoneRequestsAndClientAwnsers.enqueue(new ZoneRequest(c, g));
			serverZoneRequestsAndClientAwnsers.notify();
		}
	}

	public void shutdown() {
		shutdown = true;
		synchronized(serverRequestsAndClientAwnsers) {
			serverRequestsAndClientAwnsers.notify();
		}
		synchronized(serverZoneRequestsAndClientAwnsers) {
			serverZoneRequestsAndClientAwnsers.notify();
		}
	}

	public void resetClientVisibility() {
		visibility.clear();
		requestedVisibility.clear();
	}

	public void resetClientVisibilitySystem(Vector3i sysTo) {
		visibility.remove(sysTo);
		requestedVisibility.remove(sysTo);
	}

	private boolean shutdown;
	private final ObjectArrayFIFOQueue<PriceRequest> priceRequests = new ObjectArrayFIFOQueue<PriceRequest>();
	private final ObjectArrayFIFOQueue<PriceRequest> priceRequestsAwnsered = new ObjectArrayFIFOQueue<PriceRequest>();
	private final Object2BooleanOpenHashMap<Vector3i> visibility = new Object2BooleanOpenHashMap<Vector3i>();
	private final Set<Vector3i> requestedVisibility = new ObjectOpenHashSet<Vector3i>();

	public boolean isSystemVisiblyClient(Vector3i sys) {
		if(!requestedVisibility.contains(sys)) {
			ClientChannel clientChannel = ((GameClientState) state).getController().getClientChannel();
			synchronized(clientChannel.getNetworkObject().fogOfWarRequestsAndAwnsers) {
				FowRequestAndAwnser entry = new FowRequestAndAwnser();
				entry.sysX = sys.x;
				entry.sysY = sys.y;
				entry.sysZ = sys.z;
				clientChannel.getNetworkObject().fogOfWarRequestsAndAwnsers.add(new RemoteFowRequestAndAwnser(entry, false));
				//System.err.println("[CLIENT] REQUESTED FOW FOR "+sys);
			}
			requestedVisibility.add(new Vector3i(sys));
		}
		return visibility.getBoolean(sys);
	}

	Vector3i sysTmp = new Vector3i();

	public boolean isSectorVisiblyClientIncludingLastVisited(Vector3i sectorPos) {
		return visibility.getBoolean(VoidSystem.getContainingSystem(sectorPos, sysTmp)) || ((GameClientState) state).getPlayer().getLastVisitedSectors().contains(sectorPos);
	}

	public void startThreads() {
		if(onServer) {
			(new Thread((() -> {
				assert (onServer);
				while(!shutdown) {
					FowRequestAndAwnser r;
					synchronized(serverFowRequests) {
						while(serverFowRequests.isEmpty()) {
							try {
								serverFowRequests.wait();
								if(shutdown) {
									return;
								}
							} catch(InterruptedException e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
						r = serverFowRequests.dequeue();
					}
					r.visible = r.receivedClientChannel.getPlayer().getFogOfWar().isVisibleSystemServer(new Vector3i(r.sysX, r.sysY, r.sysZ));
					synchronized(serverFowAwnsers) {
						serverFowAwnsers.enqueue(r);
					}
				}
			}), "FogOfWarRequestThread" + (onServer ? "Server" : "Client"))).start();
		}
		(new Thread((() -> {
			while(!shutdown) {
				Request r;
				synchronized(serverRequestsAndClientAwnsers) {
					while(serverRequestsAndClientAwnsers.isEmpty()) {
						try {
							serverRequestsAndClientAwnsers.wait();
							if(shutdown) {
								return;
							}
						} catch(InterruptedException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
					r = serverRequestsAndClientAwnsers.dequeue();
				}
				if(onServer) {
					awnserRequestOnServer(r);
				} else {
					processAwnserOnClient(r);
				}
			}
		}), "GalaxyRequestThread" + (onServer ? "Server" : "Client"))).start();
		(new Thread((() -> {
			while(!shutdown) {
				ZoneRequest r;
				synchronized(serverZoneRequestsAndClientAwnsers) {
					while(serverZoneRequestsAndClientAwnsers.isEmpty()) {
						try {
							serverZoneRequestsAndClientAwnsers.wait();
							if(shutdown) {
								return;
							}
						} catch(InterruptedException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
					r = serverZoneRequestsAndClientAwnsers.dequeue();
				}
				if(onServer) {
					awnserZoneRequestOnServer(r);
				} else {
					processZoneAwnserOnClient(r);
				}
			}
		}), "GalaxyZoneRequestThread" + (onServer ? "Server" : "Client"))).start();
		if(onServer) {
			(new Thread((() -> {
				if(onServer) while(!shutdown) {
					PriceRequest r;
					synchronized(priceRequests) {
						while(priceRequests.isEmpty()) {
							try {
								priceRequests.wait();
								if(shutdown) {
									return;
								}
							} catch(InterruptedException e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
						r = priceRequests.dequeue();
					}
					if(onServer) {
						processPriceRequestThreadedDB(r);
					}
				}
			}), "PricesRequestThread" + (onServer ? "Server" : "Client"))).start();
		}
	}

	/**
	 * sends awnsers for requests. is called from main controller
	 */
	public void updateServer() {
		assert (onServer);
		if(!serverAwnsers.isEmpty()) {
			synchronized(serverAwnsers) {
				while(!serverAwnsers.isEmpty()) {
					GalaxyRequestAndAwnser dequeue = serverAwnsers.dequeue();
					System.err.println("[SERVER][GALAXY] SENDING SYSTEM BACK TO CLIENT: " + dequeue.secX + ", " + dequeue.secY + ", " + dequeue.secZ);
					dequeue.networkObjectOnServer.galaxyRequests.add(new RemoteGalaxyRequest(dequeue, onServer));
					assert (dequeue.networkObjectOnServer.galaxyRequests.hasChanged());
					assert (dequeue.networkObjectOnServer.isChanged());
				}
			}
		}
		if(!serverFowAwnsers.isEmpty()) {
			synchronized(serverFowAwnsers) {
				while(!serverFowAwnsers.isEmpty()) {
					FowRequestAndAwnser dequeue = serverFowAwnsers.dequeue();
					dequeue.receivedClientChannel.getNetworkObject().fogOfWarRequestsAndAwnsers.add(new RemoteFowRequestAndAwnser(dequeue, onServer));
				}
			}
		}
		if(!serverZonesAwnsers.isEmpty()) {
			synchronized(serverZonesAwnsers) {
				while(!serverZonesAwnsers.isEmpty()) {
					GalaxyZoneRequestAndAwnser dequeue = serverZonesAwnsers.dequeue();
					assert (dequeue != null);
					assert (dequeue.networkObjectOnServer != null);
					dequeue.networkObjectOnServer.galaxyZoneRequests.add(new RemoteGalaxyZoneRequest(dequeue, onServer));
				}
			}
		}
		if(!priceRequestsAwnsered.isEmpty()) {
			synchronized(priceRequestsAwnsered) {
				while(!priceRequestsAwnsered.isEmpty()) {
					PriceRequest dequeue = priceRequestsAwnsered.dequeue();
					assert (dequeue != null);
					processPriceRequestAwnser(dequeue);
				}
			}
		}
	}

	private void processPriceRequestAwnser(PriceRequest dequeue) {
		assert (onServer);
		dequeue.chan.getNetworkObject().pricesOfShopAwnser.add(new RemoteTradePrice(dequeue.response, onServer));
	}

	public void broadcastPrices(TradePrices p) {
		assert (onServer);
		for(RegisteredClientOnServer c : ((GameServerState) state).getClients().values()) {
			//the client's own client channel
			Sendable sendable = c.getLocalAndRemoteObjectContainer().getLocalObjects().get(0);
			//assert (sendable != null);
			if(sendable != null) {
				System.err.println("[SERVER] SENDING DIRTY DB PRICES ");
				((ClientChannel) sendable).getNetworkObject().pricesOfShopAwnser.add(new RemoteTradePrice(p, onServer));
			} else {
				System.err.println("[SENDABLEGAMESTATE] WARNING: Cannot send prices mod to " + c + ": no client channel!");
			}
		}
	}

	public class TradeDataListener extends GUIObservable {
		public void onChanged() {
			notifyObservers();
		}
	}

	public void updateClient() {
		assert (!onServer);
		if(!toAddFTLDataOnClient.isEmpty()) {
			synchronized(state) {
				synchronized(toAddFTLDataOnClient) {
					while(!toAddFTLDataOnClient.isEmpty()) {
						FTLConnection dequeue = toAddFTLDataOnClient.dequeue();
						if(dequeue.to == null) {
							//empty push. remove data
							ftlData.remove(dequeue.from);
						} else {
							ftlData.put(dequeue.from, dequeue);
						}
					}
				}
			}
		}
		if(!fowAwnsersOnClient.isEmpty()) {
			synchronized(fowAwnsersOnClient) {
				while(!fowAwnsersOnClient.isEmpty()) {
					FowRequestAndAwnser receivedTN = fowAwnsersOnClient.dequeue();
					this.visibility.put(new Vector3i(receivedTN.sysX, receivedTN.sysY, receivedTN.sysZ), receivedTN.visible);
				}
			}
		}
		if(!toAddTradeDataOnClient.isEmpty()) {
			synchronized(state) {
				synchronized(toAddTradeDataOnClient) {
					System.err.println("[CLIENT] RECEIVED TRADE NODES: " + toAddTradeDataOnClient.size());
					while(!toAddTradeDataOnClient.isEmpty()) {
						TradeNodeStub receivedTN = toAddTradeDataOnClient.dequeue();
						assert (receivedTN.getEntityDBId() > 0) : receivedTN.getEntityDBId();
						if(receivedTN.remove) {
							//remove specific from that system
							TradeNodeStub remove = tradeDataById.remove(receivedTN.getEntityDBId());
							if(remove != null) {
								List<TradeNodeStub> c = tradeData.get(remove.getSystem());
								if(c != null) {
									for(int i = 0; i < c.size(); i++) {
										if(c.get(i).getEntityDBId() == receivedTN.getEntityDBId()) {
											c.remove(i);
											break;
										}
									}
								}
							}
						} else {
							TradeNodeStub prev = tradeDataById.get(receivedTN.getEntityDBId());
							if(prev != null) {
								Vector3i system = new Vector3i(prev.getSystem());
								prev.updateWith(receivedTN);
								if(!system.equals(prev.getSystem())) {
									//system changed
									List<TradeNodeStub> c = tradeData.get(system);
									if(c != null) {
										for(int i = 0; i < c.size(); i++) {
											if(c.get(i).getEntityDBId() == prev.getEntityDBId()) {
												c.remove(i);
												break;
											}
										}
									}
									List<TradeNodeStub> e = tradeData.get(prev.getSystem());
									if(e == null) {
										e = new ObjectArrayList<TradeNodeStub>();
										tradeData.put(prev.getSystem(), e);
									}
									e.add(prev);
								}
							} else {
								((TradeNodeClient) receivedTN).spawnPriceListener();
								tradeDataById.put(receivedTN.getEntityDBId(), receivedTN);
								List<TradeNodeStub> e = tradeData.get(receivedTN.getSystem());
								if(e == null) {
									e = new ObjectArrayList<TradeNodeStub>();
									tradeData.put(receivedTN.getSystem(), e);
								}
								e.add(receivedTN);
							}
						}
						System.err.println("[CLIENT] Total nodes: " + tradeDataById.size());
					}
					tradeDataListener.onChanged();
				}
			}
		}
		if(!zoneAwnsersOnClient.isEmpty()) {
			synchronized(zoneAwnsersOnClient) {
				ObjectIterator<Entry<Vector3i, Long>> it = zoneAwnsersOnClient.entrySet().iterator();
				long t = System.currentTimeMillis();
				while(it.hasNext()) {
					Entry<Vector3i, Long> next = it.next();
					if(next.getValue() < t) {
						checkSystemBySectorPos(new Vector3i(next.getKey().x * VoidSystem.SYSTEM_SIZE, next.getKey().y * VoidSystem.SYSTEM_SIZE, next.getKey().z * VoidSystem.SYSTEM_SIZE));
						it.remove();
					}
				}
			}
		}
		if(!clientSystemsToAdd.isEmpty()) {
			synchronized(state) {
				synchronized(clientSystemsToAdd) {
					while(!clientSystemsToAdd.isEmpty()) {
						VoidSystem dequeue = clientSystemsToAdd.dequeue();
						VoidSystem put = clientData.put(dequeue.getPos(), dequeue);
						Long remove = requested.remove(dequeue.getPos());
						assert remove == null || (put == null || put.getPos().equals(dequeue.getPos())) : put.getPos() + "; " + dequeue.getPos();
					}
				}
			}
		}
	}

	private void processAwnserOnClient(Request r) {
		VoidSystem s = new VoidSystem();
		StellarSystem.getPosFromSector(new Vector3i(r.g.secX, r.g.secY, r.g.secZ), s.getPos());
		s.setOwnerFaction(r.g.factionUID);
		s.setOwnerUID(r.g.ownerUID);
		s.setOwnerPos(new Vector3i(r.g.secX, r.g.secY, r.g.secZ));
		//		System.err.println("[CLIENT][GALAXY] PROCESS AWNSER: SYSTEM: "+s.getPos()+": Sec: "+(r.g.secX+", "+ r.g.secY+", "+ r.g.secZ)+" FAC "+s.getOwnerFaction()+", "+s);
		synchronized(clientSystemsToAdd) {
			clientSystemsToAdd.enqueue(s);
		}
	}

	private void processZoneAwnserOnClient(ZoneRequest r) {
		GalaxyZoneRequestAndAwnser g = r.g;
		//		System.err.println("[CLIENT][GALAXY] ZONE PROCESS AWNSER: "+g.buffer.size()+" awnsers; request start system: "+r.g.startSystem);
		for(GalaxyRequestAndAwnser rg : g.buffer) {
			VoidSystem s = new VoidSystem();
			StellarSystem.getPosFromSector(new Vector3i(rg.secX, rg.secY, rg.secZ), s.getPos());
			s.setOwnerFaction(rg.factionUID);
			s.setOwnerUID(rg.ownerUID);
			s.setOwnerPos(new Vector3i(rg.secX, rg.secY, rg.secZ));
			//			System.err.println("[CLIENT][GALAXY] ZONE PROCESS AWNSER: SYSTEM: "+s.getPos()+": Sec: "+(rg.secX+", "+ rg.secY+", "+ rg.secZ)+" FAC "+s.getOwnerFaction()+", "+s);
			synchronized(clientSystemsToAdd) {
				clientSystemsToAdd.enqueue(s);
			}
		}
		synchronized(zoneAwnsersOnClient) {
			zoneAwnsersOnClient.put(new Vector3i(g.startSystem), System.currentTimeMillis() + 5000 + (long) (Math.random() * 2000d));
		}
	}

	private void awnserRequestOnServer(Request r) {
		long t = System.currentTimeMillis();
		r.getSystemPos(tmpPosServerAnswer);
		GameServerState serverState = (GameServerState) state;
		boolean loadSystem = serverState.getDatabaseIndex().getTableManager().getSystemTable().loadSystem((GameServerState) state, tmpPosServerAnswer, tmpVoidSystemServer);
		GalaxyRequestAndAwnser an = new GalaxyRequestAndAwnser();
		an.secX = r.g.secX;
		an.secY = r.g.secY;
		an.secZ = r.g.secZ;

		an.networkObjectOnServer = r.c;
		if(loadSystem) {
			an.factionUID = tmpVoidSystemServer.getOwnerFaction();
			an.ownerUID = tmpVoidSystemServer.getOwnerUID();
		}
		System.err.println("[SERVER][GALAXY] REQUESTING SYSTEM " + tmpPosServerAnswer + "; exists in DB: " + loadSystem + "; TIME: " + (System.currentTimeMillis() - t) + "ms: " + an);
		synchronized(serverAwnsers) {
			serverAwnsers.enqueue(an);
		}
	}

	private void awnserZoneRequestOnServer(ZoneRequest r) {
		long t = System.currentTimeMillis();
		GalaxyZoneRequestAndAwnser an;
		synchronized(zoneMap) {
			an = zoneMap.get(r.g.startSystem);
			if(an != null) {
				GalaxyZoneRequestAndAwnser b = new GalaxyZoneRequestAndAwnser(an);
				an = b;
			}
		}
		if(an == null) {
			an = new GalaxyZoneRequestAndAwnser();
			an.buffer = new ObjectArrayList();
			an.startSystem = new Vector3i(r.g.startSystem);
			an.zoneSize = r.g.zoneSize;
			boolean loadSystem = ((GameServerState) state).getDatabaseIndex().getTableManager().getSystemTable().loadSystemZone(r.g.startSystem, r.g.zoneSize, an.buffer);
			synchronized(zoneMap) {
				zoneMap.put(an.startSystem, an);
			}
		} else {
			//			System.err.println("[SERVER][GALAXY] REQUESTING ZONE FROM CACHE: "+r.g.startSystem+"; TIME: "+(System.currentTimeMillis()-t)+"ms "+a.buffer);
		}
		an.networkObjectOnServer = r.c;
		//		System.err.println("[SERVER][GALAXY] REQUESTING ZONE "+r.g.startSystem+"; TIME: "+(System.currentTimeMillis()-t)+"ms");
		synchronized(serverZonesAwnsers) {
			serverZonesAwnsers.enqueue(an);
		}
	}

	public void markZoneDirty(Vector3i system) {
		synchronized(zoneMap) {
			Vector3i zoneStart = getContainingZoneFromSystem(system, ZONE_SIZE, new Vector3i());
			//will be requested again
			//TODO can maybe update the buffer entry directly
			GalaxyZoneRequestAndAwnser remove = zoneMap.remove(zoneStart);
			if(remove == null) {
				System.err.println("[SERVER][GALAXYMANAGER] dirty galaxy zone could not be removed from cache. Changed " + system + "; zoneStart: " + zoneStart + "; current zones cached: " + zoneMap.keySet());
			} else {
				System.err.println("[SERVER][GALAXYMANAGER] galaxy zone has been marked dirty and has been removed from cache. Changed " + system + ";");
			}
		}
	}

	public void markZoneDirty(StellarSystem system) {
		markZoneDirty(system.getPos());
	}

	/**
	 * @return the clientData
	 */
	public Map<Vector3i, VoidSystem> getClientData() {
		return clientData;
	}

	/**
	 * used when a single sector changes
	 * <p/>
	 * (threadsafe. used in Universe.update())
	 *
	 * @param id
	 */
	public void sendDirectTradeUpdateOnServer(long id) {
		assert (onServer);
		try {
			TradeNodeStub con = ((GameServerState) state).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradeNode(id);
			boolean remove;
			if(con == null) {
				TradeNodeStub rem = tradeDataById.remove(id);
				if(rem != null) {
					if(FactionManager.isNPCFaction(rem.getFactionId())) {
						try {
							((GameServerState) state).getController().broadcastMessageAdmin(Lng.astr("WARNING: NPC TRADE NDOE REMOVED"), ServerMessage.MESSAGE_TYPE_ERROR);
							throw new Exception("REMOVED NPC TRADE NODE: " + id + ": " + rem);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
					System.err.println("[SERVER][GALAXYMANAGER] removed trade node: " + id + ": " + rem);
					Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(id);
					if(sendable != null && sendable instanceof SpaceStation) {
						if(((SpaceStation) sendable).isNPCHomeBase()) {
							assert (false) : sendable;
						}
					}
					List<TradeNodeStub> c = tradeData.get(rem.getSystem());
					if(c != null) {
						for(int i = 0; i < c.size(); i++) {
							if(c.get(i).getEntityDBId() == id) {
								c.remove(i);
								break;
							}
						}
					}
				}
				remove = true;
			} else {
				tradeDataById.put(con.getEntityDBId(), con);
				List<TradeNodeStub> c = tradeData.get(con.getSystem());
				if(c != null) {
					for(int i = 0; i < c.size(); i++) {
						if(c.get(i).getEntityDBId() == id) {
							c.remove(i);
							break;
						}
					}
					c.add(con);
				}
				remove = false;
			}
			for(RegisteredClientOnServer c : ((GameServerState) state).getClients().values()) {
				//the client's own client channel
				Sendable sendable = c.getLocalAndRemoteObjectContainer().getLocalObjects().get(0);
				//assert (sendable != null);
				if(sendable != null) {
					System.err.println("[SERVER] SENDING DIRTY TRADE NODES " + con + "; null is remove");
					if(remove) {
						assert (con == null);
						//send cleaning stub
						TradeNodeStub stub = new TradeNodeStub();
						assert (id > 0) : id;
						stub.setEntityDBId(id);
						stub.remove = true;
						((ClientChannel) sendable).getNetworkObject().tradeNodeUpdatesAndRequests.add(new RemoteTradeNode(stub, ((ClientChannel) sendable).getNetworkObject()));
					} else {
						assert (con != null);
						((ClientChannel) sendable).getNetworkObject().tradeNodeUpdatesAndRequests.add(new RemoteTradeNode(con, ((ClientChannel) sendable).getNetworkObject()));
					}
				} else {
					System.err.println("[SENDABLEGAMESTATE] WARNING: Cannot send trade node mod to " + c + ": no client channel!");
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * used when a single sector changes
	 * <p/>
	 * (threadsafe. used in Universe.update())
	 *
	 * @param sec
	 */
	public void sendDirectFtlUpdateOnServer(Vector3i sec) {
		assert (onServer);
		FTLConnection con = ((GameServerState) state).getDatabaseIndex().getTableManager().getFTLTable().getFtl(sec);
		if(con == null) {
			//make empty update to signal clients to remove the entry
			con = new FTLConnection();
			con.from = new Vector3i(sec);
			//also remove from ftl data so its no longer sent on full updates
			ftlData.remove(con.from);
		} else {
			ftlData.put(con.from, con);
		}
		for(RegisteredClientOnServer c : ((GameServerState) state).getClients().values()) {
			//the client's own client channel
			Sendable sendable = c.getLocalAndRemoteObjectContainer().getLocalObjects().get(0);
			//assert (sendable != null);
			if(sendable != null) {
				System.err.println("[SERVER] SENDING DIRTY FTL " + sec);
				((ClientChannel) sendable).getNetworkObject().ftlUpdatesAndRequests.add(new RemoteFTLConnection(con, ((ClientChannel) sendable).getNetworkObject()));
			} else {
				System.err.println("[SENDABLEGAMESTATE] WARNING: Cannot send ftl mod to " + c + ": no client channel!");
			}
		}
	}

	public void sendAllFtlDataTo(NetworkClientChannel networkObject) {
		for(FTLConnection c : ftlData.values()) {
			networkObject.ftlUpdatesAndRequests.add(new RemoteFTLConnection(c, networkObject));
		}
	}

	public void sendAllTradeStubsTo(NetworkClientChannel networkObject) {
		for(TradeNodeStub n : tradeDataById.values()) {
			networkObject.tradeNodeUpdatesAndRequests.add(new RemoteTradeNode(n, networkObject));
		}
	}

	/**
	 * @return the ftlData
	 */
	public Object2ObjectOpenHashMap<Vector3i, FTLConnection> getFtlData() {
		return ftlData;
	}

	public void initializeOnServer() {
		assert (onServer);
		((GameServerState) state).getDatabaseIndex().getTableManager().getFTLTable().fillFTLData(ftlData);
		((GameServerState) state).getDatabaseIndex().getTableManager().getTradeNodeTable().fillTradeNodeStubData(tradeData, tradeDataById);
	}

	private class Request {
		final NetworkClientChannel c;
		final GalaxyRequestAndAwnser g;

		public Request(NetworkClientChannel c, GalaxyRequestAndAwnser g) {
			super();
			this.c = c;
			this.g = g;
		}

		public void getSystemPos(Vector3i out) {
			StellarSystem.getPosFromSector(new Vector3i(g.secX, g.secY, g.secZ), out);
		}
	}

	private class ZoneRequest {
		final NetworkClientChannel c;
		final GalaxyZoneRequestAndAwnser g;

		public ZoneRequest(NetworkClientChannel c, GalaxyZoneRequestAndAwnser g) {
			super();
			this.c = c;
			this.g = g;
		}
		//		public void getSystemPos(Vector3i out) {
		//			StellarSystem.getPosFromSector(new Vector3i(g.secX,  g.secY, g.secZ), out);
		//		}
	}

	private class PriceRequest {
		long dbEnt;
		ClientChannel chan;
		public TradePrices response;

		public PriceRequest(long dbEnt, ClientChannel chan) {
			super();
			this.dbEnt = dbEnt;
			this.chan = chan;
		}
	}

	public void enqueuePricesRequest(long r, ClientChannel clientChannel) {
		synchronized(priceRequests) {
			priceRequests.enqueue(new PriceRequest(r, clientChannel));
			priceRequests.notify();
		}
	}

	private void processPriceRequestThreadedDB(PriceRequest p) {
		assert (onServer);
		DataInputStream tradePrices = ((GameServerState) state).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePricesAsStream(p.dbEnt);
		if(tradePrices != null) {
			try {
				TradePrices pc = ShoppingAddOn.deserializeTradePrices(tradePrices, true);
				tradePrices.close();
				p.response = pc;
				synchronized(priceRequestsAwnsered) {
					priceRequestsAwnsered.enqueue(p);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("[SERVER][TRADE] trade prices request failed for entDbID: " + p.dbEnt);
		}
	}

	public Object2ObjectOpenHashMap<Vector3i, List<TradeNodeStub>> getTradeNodes() {
		return tradeData;
	}

	public void updatedPricesOnClient(TradePrices t) {
		TradeNodeClient tradeNodeStub = (TradeNodeClient) tradeDataById.get(t.entDbId);
		if(tradeNodeStub != null) {
			tradeNodeStub.receiveTradePrices(t.getPrices());
		}
	}

	public Long2ObjectOpenHashMap<TradeNodeStub> getTradeNodeDataById() {
		return tradeDataById;
	}
}
