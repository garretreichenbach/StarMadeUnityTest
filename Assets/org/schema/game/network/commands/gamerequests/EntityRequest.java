package org.schema.game.network.commands.gamerequests;

import org.schema.game.common.controller.*;
import org.schema.game.common.data.world.*;
import org.schema.schine.resource.FileExt;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.schema.common.LogUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerProcessorInterface;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.network.server.ServerStateInterface;

public class EntityRequest implements GameRequestInterface{

	public EntityType type;
	public final float[] mat = new float[16];
	public int minX;
	public int minY;
	public int minZ;

	public int maxX;
	public int maxY;
	public int maxZ;

	public int ownerId;

	public String uniqueIdentifier;
	public String realName;

	public EntityRequest() {
		super();
	}

	public static String convertAsteroidEntityName(String realName, boolean managed) {
		return (managed ? EntityType.ASTEROID_MANAGED.dbPrefix : EntityType.ASTEROID.dbPrefix) + realName;
	}

	public static String convertPlanetEntityName(String realName) {
		return EntityType.PLANET_SEGMENT.dbPrefix + realName;
	}

	public static String convertShipEntityName(String realName) {
		return EntityType.SHIP.dbPrefix + realName;
	}

	public static String convertShopEntityName(String realName) {
		return EntityType.SHOP.dbPrefix + realName;
	}

	public static String convertStationEntityName(String realName) {
		return EntityType.SPACE_STATION.dbPrefix + realName;
	}

	public static String convertVehicleEntityName(String realName) {
		return "ENTITY_VEHICLE_" + realName;
	}

	public static boolean caseSensitiveFileExists(String pathInQuestion) throws IOException {
		File f = new FileExt(pathInQuestion);
		return f.exists() && f.getCanonicalPath().endsWith(f.getName());
	}

	public static boolean existsIdentifier(StateInterface state, String uniqueIdentifier) throws EntityAlreadyExistsException {
		//		System.err.println("CHECKING IF EXISTS: "+uniqueIdentifier);
		File f = new FileExt(GameServerState.ENTITY_DATABASE_PATH + uniqueIdentifier + ".ent");
		if (state instanceof GameServerState) {
			if (((GameServerState) state).getSegmentControllersByNameLowerCase().containsKey(uniqueIdentifier.toLowerCase(Locale.ENGLISH))) {
				throw new EntityAlreadyExistsException(uniqueIdentifier);
			}
		}
		if (f.exists()) {
			throw new EntityAlreadyExistsException(uniqueIdentifier);
		} else {
			return false;
		}
	}
	public static boolean existsIdentifierWOExc(StateInterface state, String uniqueIdentifier){
		//		System.err.println("CHECKING IF EXISTS: "+uniqueIdentifier);
		File f = new FileExt(GameServerState.ENTITY_DATABASE_PATH + uniqueIdentifier + ".ent");
		if (state instanceof GameServerState) {
			if (((GameServerState) state).getSegmentControllersByNameLowerCase().containsKey(uniqueIdentifier.toLowerCase(Locale.ENGLISH))) {
				return true;
			}
		}
		if (f.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static FloatingRock getNewAsteroid(ServerStateInterface state, String uniqueIdentifier, int sectorId, String realName, float[] mat, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String playerUID, boolean chunk16) {
		FloatingRock ship = new FloatingRock(state);
		ship.setUniqueIdentifier(uniqueIdentifier);
		ship.getMinPos().set(new Vector3i(minX, minY, minZ));
		ship.getMaxPos().set(new Vector3i(maxX, maxY, maxZ));
		ship.setId(state.getNextFreeObjectId());
		ship.setSectorId(sectorId);
		ship.setRealName(realName);
		ship.setLoadedFromChunk16(chunk16);
		ship.initialize();
		ship.getInitialTransform().setFromOpenGLMatrix(mat);
		ship.setSpawner(playerUID);
		return ship;
	}

	public static Ship getNewShip(ServerStateInterface state, String uniqueIdentifier, int sectorId, String realName, float[] mat, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String playerUID, boolean chunk16) {
		Ship ship = new Ship(state);
		ship.setUniqueIdentifier(uniqueIdentifier);
		ship.getMinPos().set(new Vector3i(minX, minY, minZ));
		ship.getMaxPos().set(new Vector3i(maxX, maxY, maxZ));
		ship.setId(state.getNextFreeObjectId());
		ship.setSectorId(sectorId);
		ship.setRealName(realName);
		ship.setLoadedFromChunk16(chunk16);
		ship.initialize();
		ship.getInitialTransform().setFromOpenGLMatrix(mat);
		ship.setTouched(true, false);
		ship.setSpawner(playerUID);
		return ship;
	}

	public static ShopSpaceStation getNewShop(ServerStateInterface state, String uniqueIdentifier, int sectorId, String realName, float[] mat, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String playerUID, boolean chunk16) {
		ShopSpaceStation ship = new ManagedShop(state);
		ship.setUniqueIdentifier(uniqueIdentifier);
		ship.getMinPos().set(new Vector3i(minX, minY, minZ));
		ship.getMaxPos().set(new Vector3i(maxX, maxY, maxZ));
		ship.setId(state.getNextFreeObjectId());
		ship.setSectorId(sectorId);
		ship.setRealName(realName);
		ship.setLoadedFromChunk16(chunk16);
		ship.initialize();
		ship.getInitialTransform().setFromOpenGLMatrix(mat);
		ship.setSpawner(playerUID);
		return ship;
	}

	public static SpaceStation getNewSpaceStation(ServerStateInterface state, String uniqueIdentifier, int sectorId, String realName, float[] mat, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean chunk16) {
		SpaceStation s = new SpaceStation(state);
		s.setUniqueIdentifier(uniqueIdentifier);
		s.getMinPos().set(new Vector3i(minX, minY, minZ));
		s.getMaxPos().set(new Vector3i(maxX, maxY, maxZ));
		s.setCreatorId(SpaceStationType.EMPTY.ordinal());
		s.setId(state.getNextFreeObjectId());
		s.setSectorId(sectorId);
		s.setRealName(realName);
		s.setLoadedFromChunk16(chunk16);
		s.initialize();
		s.getInitialTransform().setFromOpenGLMatrix(mat);
		return s;
	}

	public static Vehicle getNewVehicle(ServerStateInterface state, String uniqueIdentifier, int sectorId, String realName, float[] mat, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String playerUID) {
		Vehicle vehicle = new Vehicle(state);
		vehicle.setUniqueIdentifier(uniqueIdentifier);
		vehicle.getMinPos().set(new Vector3i(minX, minY, minZ));
		vehicle.getMaxPos().set(new Vector3i(maxX, maxY, maxZ));
		vehicle.setId(state.getNextFreeObjectId());
		vehicle.setSectorId(sectorId);
		vehicle.setRealName(realName);
		vehicle.initialize();
		vehicle.getInitialTransform().setFromOpenGLMatrix(mat);
		vehicle.setSpawner(playerUID);
		return vehicle;
	}

	public static boolean isShipNameValid(String entry) {
		return (entry.length() > 0 && entry.matches("[a-zA-Z0-9_-]+[ a-zA-Z0-9_-]*") && !entry.endsWith(" "));
	}


	public Ship getShip(GameServerState state, boolean touched) throws EntityNotFountException, IOException, EntityAlreadyExistsException {

		PlayerState pl = (PlayerState) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(ownerId);

		existsIdentifier(state, uniqueIdentifier);

		Ship ship = getNewShip(state, uniqueIdentifier, pl.getCurrentSectorId(), realName, mat, minX, minY, minZ, maxX, maxY, maxZ, pl.getUniqueIdentifier(), false);
		if(touched){
			ship.setTouched(true, false);
		}
		//new ship stub
		RemoteSegment s = new RemoteSegment(ship);
		s.setSegmentData(new SegmentData4Byte(state instanceof ClientStateInterface));
		s.getSegmentData().setSegment(s);
		try{
			s.getSegmentData().setInfoElementUnsynched((byte) Ship.core.x, (byte) Ship.core.y, (byte) Ship.core.z, ElementKeyMap.CORE_ID, true, 
				s.getAbsoluteIndex((byte) Ship.core.x, (byte) Ship.core.y, (byte) Ship.core.z), state.getUpdateTime());
		} catch (SegmentDataWriteException e) {
			throw new RuntimeException("Should be a normal data chunk", e);
		}
		s.setLastChanged(System.currentTimeMillis());
		ship.getSegmentBuffer().addImmediate(s);
		ship.getSegmentBuffer().updateBB(s);
		return ship;
	}

	public SpaceStation getSpaceStation(GameServerState state, boolean touched) throws EntityAlreadyExistsException {

		existsIdentifier(state, uniqueIdentifier);
		PlayerState pl = (PlayerState) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(ownerId);

		SpaceStation station = getNewSpaceStation(state, uniqueIdentifier, pl.getCurrentSectorId(), realName, mat, minX, minY, minZ, maxX, maxY, maxZ, false);
		if(touched){
			station.setTouched(true, false);
		}
		//new ship stub
	
		SegmentData d = new SegmentData4Byte(state instanceof ClientStateInterface);
		RemoteSegment s = new RemoteSegment(station);
		d.assignData(s);
		try{
			d.setInfoElementUnsynched((byte) SegmentData.SEG_HALF, (byte) SegmentData.SEG_HALF, (byte) SegmentData.SEG_HALF, ElementKeyMap.HULL_ID, true, s.getAbsoluteIndex((byte) SegmentData.SEG_HALF, (byte) SegmentData.SEG_HALF, (byte) SegmentData.SEG_HALF), state.getUpdateTime());
		} catch (SegmentDataWriteException e) {
			throw new RuntimeException("Should be a normal data chunk", e);
		}
		
		
		s.lastLocalTimeStamp = System.currentTimeMillis();
		station.getSegmentBuffer().addImmediate(s);
		
		s.setLastChanged(System.currentTimeMillis());
		station.getSegmentBuffer().setLastChanged(s.pos, s.lastLocalTimeStamp);
		

		assert(station.getTotalElements() == 1):station.getTotalElements();
		
		return station;
	}

	public Vehicle getVehicle(GameServerState state) throws EntityAlreadyExistsException {

		existsIdentifier(state, uniqueIdentifier);
		PlayerState pl = (PlayerState) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(ownerId);

		Vehicle vehicle = getNewVehicle(state, uniqueIdentifier, pl.getCurrentSectorId(), realName, mat, minX, minY, minZ, maxX, maxY, maxZ, pl.getUniqueIdentifier());

		//new ship stub
		RemoteSegment s = new RemoteSegment(vehicle);
		s.setSegmentData(new SegmentData4Byte(state instanceof ClientStateInterface));
		s.getSegmentData().setSegment(s);
		try{
			s.getSegmentData().setInfoElementUnsynched((byte) SegmentData.SEG_HALF, (byte) SegmentData.SEG_HALF, (byte)SegmentData.SEG_HALF, (byte) 1, true, s.getAbsoluteIndex((byte) SegmentData.SEG_HALF, (byte) SegmentData.SEG_HALF, (byte) SegmentData.SEG_HALF), state.getUpdateTime());
		} catch (SegmentDataWriteException e) {
			throw new RuntimeException("Should be a normal data chunk", e);
		}
		s.setLastChanged(System.currentTimeMillis());
		vehicle.getSegmentBuffer().addImmediate(s);

		return vehicle;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(type.ordinal());


		for(int i = 0; i < 16; i++) {
			b.writeFloat(mat[i]);
		}
		b.writeInt(minX);
		b.writeInt(minY);
		b.writeInt(minZ);

		b.writeInt(maxX);
		b.writeInt(maxY);
		b.writeInt(maxZ);


		b.writeInt(ownerId);

		b.writeUTF(uniqueIdentifier);
		b.writeUTF(realName);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		type = EntityType.values()[b.readByte()];

		for(int i = 0; i < 16; i++) {
			mat[i] = b.readFloat();
		}
		minX = b.readInt();
		minY = b.readInt();
		minZ = b.readInt();

		maxX = b.readInt();
		maxY = b.readInt();
		maxZ = b.readInt();

		ownerId = b.readInt();

		uniqueIdentifier = b.readUTF();
		realName = b.readUTF();
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.ENTITY;
	}

	@Override
	public void free() {

	}

	@Override
	public void handleAnswer(NetworkProcessor p, ServerState sd) throws IOException {
		GameServerState state = (GameServerState)sd;
		ServerProcessorInterface ss = (ServerProcessorInterface)p;

		System.err.println("[SERVER] HANDLING Entity REQUEST: " + type.name()+" by client "+ss.getClient());


		try {
			switch(type) {
				case SHIP -> {
					PlayerState player = state.getPlayerFromStateId(ss.getClient().getId());
					try {
						Ship ship = getShip(state, true);
						if(player.getInventory(null).existsInInventory(ElementKeyMap.CORE_ID)) {
							state.getController().getSynchController().addNewSynchronizedObjectQueued(ship);
							player.getInventory(null).incExistingAndSend(ElementKeyMap.CORE_ID, -1, player.getNetworkObject());
							LogUtil.log().fine("[SPAWN] " + player.getName() + " spawned new ship: \"" + ship.getRealName() + "\"");
						}
					} catch(EntityAlreadyExistsException e) {
						e.printStackTrace();
						state.getClients().get(player.getClientId()).serverMessage("[ERROR] An Entity with that name already exists");
					} catch(EntityNotFountException e) {
						e.printStackTrace();
						state.getClients().get(player.getClientId()).serverMessage("[ERROR] Entity not found");
					} catch(NoSlotFreeException e) {
						e.printStackTrace();
					}
					break;
				}
				case VEHICLE -> {
					PlayerState player = state.getPlayerFromStateId(ss.getClient().getId());
					try {
						Vehicle ship = getVehicle(state);
						state.getController().getSynchController().addNewSynchronizedObjectQueued(ship);
					} catch(EntityAlreadyExistsException e) {
						e.printStackTrace();
						state.getClients().get(player.getClientId()).serverMessage("[ERROR] An Entity with that name already exists");
					}
					break;
				}
				case SPACE_STATION -> {
					PlayerState player = state.getPlayerFromStateId(ss.getClient().getId());
					try {
						if(player.getCredits() < state.getGameState().getStationCost()) {
							player.sendServerMessage(new ServerMessage(Lng.astr("You don't have enough credits!\n%s of %s", StringTools.formatSeperated(player.getCredits()), StringTools.formatSeperated(state.getGameState().getStationCost())), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
						} else {
							boolean sectorOK = true;
							int stationsAllowed = ServerConfig.ALLOWED_STATIONS_PER_SECTOR.getInt();
							int stations = 0;
							for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
								if(s instanceof SpaceStation && ((SpaceStation) s).getSectorId() == player.getCurrentSectorId()) {
									stations++;
									break;
								}
							}
							sectorOK = stations < stationsAllowed && !player.isInPersonalSector() && !player.isInTestSector() && !player.isInTutorial();
							if(sectorOK) {
								SpaceStation station = getSpaceStation(state, true);
								player.modCreditsServer(-state.getGameState().getStationCost());
								state.getController().getSynchController().addNewSynchronizedObjectQueued(station);
								LogUtil.log().fine("[SPAWN] " + player.getName() + " spawned new station: \"" + station.getRealName() + "\"");
							} else {
								if(stations >= stationsAllowed) {
									player.sendServerMessagePlayerError(Lng.astr("Cannot spawn here!\nOnly %s station(s) per sector\nallowed!", stationsAllowed));
								} else {
									player.sendServerMessagePlayerError(Lng.astr("Cannot spawn here!\nNo station spawning allowed in this sector!"));
								}
							}
						}
					} catch(EntityAlreadyExistsException e) {
						e.printStackTrace();
						state.getClients().get(player.getClientId()).serverMessage("[ERROR] An Entity with that name already exists");
					}
					break;
				}
				default -> {
					throw new IllegalArgumentException("Unknown class to spawn: " + type.name());
				}
			}
		}catch(PlayerNotFountException e) {
			e.printStackTrace();
		}
	}

}
