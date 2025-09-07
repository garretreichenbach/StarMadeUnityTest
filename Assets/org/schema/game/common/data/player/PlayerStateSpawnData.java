package org.schema.game.common.data.player;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedShop;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerStateSpawnData {
	private static final byte VERSION = 1;
	private static int spawnPointRotationId;
	//	public final Vector3f spawnPoint = new Vector3f();
//	public final Vector3i spawnSector = new Vector3i();
//	public final Vector3f logoutSpawnPoint = new Vector3f();
//	public final Vector3i logoutSpawnSector = new Vector3i();
	public final List<PlayerInfoHistory> hosts = new ArrayList<PlayerInfoHistory>();
	private final PlayerState playerState;
	public long thisLogin = 0;
	public long lastLogin = 0;
	public long lastLogout = 0;
	public Spawn deathSpawn = new Spawn();
	public Spawn logoutSpawn = new Spawn();

	
	public Vector3i preSpecialSector = null;
	public Transform preSpecialSectorTransform = null;
	
	public PlayerStateSpawnData(PlayerState playerState) {
		this.playerState = playerState;
		thisLogin = System.currentTimeMillis();
	}

	public void fromTagStructure(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();

		byte version = (Byte) v[0].getValue();

		deathSpawn.fromTagStructure(v[1]);
		logoutSpawn.fromTagStructure(v[2]);
		
		if (v.length > 3 && v[3].getType() == Type.VECTOR3f) {
			if (preSpecialSectorTransform == null) {
				preSpecialSectorTransform = new Transform();
			}
			preSpecialSectorTransform.origin.set((Vector3f) v[3].getValue());
		}

		if (v.length > 4 && v[4].getType() == Type.VECTOR3i) {
			if (preSpecialSector == null) {
				preSpecialSector = new Vector3i();
			}
			preSpecialSector.set((Vector3i) v[4].getValue());
		}
	}

	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, VERSION),
				deathSpawn.toTagStructure(),
				logoutSpawn.toTagStructure(),
				preSpecialSectorTransform == null ? new Tag(Type.BYTE, null, (byte) 0) : new Tag(Type.VECTOR3f, "pretutpoint", preSpecialSectorTransform.origin),
				preSpecialSector == null ? new Tag(Type.BYTE, null, (byte) 0) : new Tag(Type.VECTOR3i, "pretutsector", preSpecialSector),
				FinishTag.INST,
		});
	}

	public void fromNetworkObject() {

	}

	public void updateToNetworkObject() {

	}

	public void updateToFullNetworkObject() {

	}

	public void initFromNetworkObject() {

	}

	public void updateLocal(Timer timer) {
		if (playerState.isOnServer()) {
			setLogoutSpawnToPlayerPos();
		}
	}

	public Vector3f[] getDefaultSpawnPoints() {
		return new Vector3f[]{
				new Vector3f(
						ServerConfig.DEFAULT_SPAWN_POINT_X_1.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Y_1.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Z_1.getFloat()),
				new Vector3f(
						ServerConfig.DEFAULT_SPAWN_POINT_X_2.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Y_2.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Z_2.getFloat()),
				new Vector3f(
						ServerConfig.DEFAULT_SPAWN_POINT_X_3.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Y_3.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Z_3.getFloat()),
				new Vector3f(
						ServerConfig.DEFAULT_SPAWN_POINT_X_4.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Y_4.getFloat(),
						ServerConfig.DEFAULT_SPAWN_POINT_Z_4.getFloat()),
		};
	}

	public void setForNewPlayerOnLogin() throws IOException {

		System.out.println("[SERVER] Not Found Saved State: " + playerState + " : UID " + playerState.getUniqueIdentifier());
		Sector defaultSec = ((GameServerState) playerState.getState()).getUniverse().getSector(
				new Vector3i(
						ServerConfig.DEFAULT_SPAWN_SECTOR_X.getInt(),
						ServerConfig.DEFAULT_SPAWN_SECTOR_Y.getInt(),
						ServerConfig.DEFAULT_SPAWN_SECTOR_Z.getInt()));

//		player.setCredits(ServerConfig.STARTING_CREDITS.getInt());

		Vector3f[] defaultSpawingPoints = getDefaultSpawnPoints();
		Vector3f spawn = new Vector3f(defaultSpawingPoints[spawnPointRotationId]);
		spawnPointRotationId = (spawnPointRotationId + 1) % 4;

		if (ServerConfig.USE_PERSONAL_SECTORS.isOn()) {
			usePersonalSector(playerState, spawn);

		} else {
			deathSpawn.localPos.set(spawn);
			deathSpawn.absolutePosBackup.set(spawn);
			deathSpawn.absoluteSector.set(defaultSec.pos);

			logoutSpawn.localPos.set(spawn);
			logoutSpawn.absolutePosBackup.set(spawn);
			logoutSpawn.absoluteSector.set(defaultSec.pos);

			playerState.setCurrentSectorId(defaultSec.getId());
			playerState.getCurrentSector().set(defaultSec.pos);
		}
	}

	public void setFromLoadedSpawnPoint() throws IOException {

		if (ServerConfig.USE_PERSONAL_SECTORS.isOn()) {
			//since we are loading. the personal sector has already been determined on fist initialization
			int uniquePlayerId = 0;
			usePersonalSector(playerState, new Vector3f(0, 0, 0));

		} else {
			
			
		}
		
		Sector spawn = getSpawnSector(false);
		playerState.setCurrentSectorId(spawn.getId());
		playerState.setCurrentSector(spawn.pos);
	}

	private void usePersonalSector(PlayerState player, Vector3f spawn) throws IOException {

		

		System.err.println("[SERVER] using personal sector for player " + player + " -> " + player.personalSector);

		deathSpawn.localPos.set(spawn);
		deathSpawn.absolutePosBackup.set(spawn);

		Sector defaultSec = ((GameServerState) playerState.getState()).getUniverse().getSector(
				new Vector3i(
						player.personalSector.x,
						player.personalSector.y,
						player.personalSector.z));
		//set flags for personal sector
		defaultSec.noEnter(true);
		defaultSec.noExit(true);

		player.setCurrentSectorId(defaultSec.getId());
		player.getCurrentSector().set(defaultSec.pos);

		deathSpawn.absoluteSector.set(defaultSec.pos);
	}

	public void onLoggedOut() {
		setLogoutSpawnToPlayerPos();
	}

	public void setDeathSpawnToPlayerPos() {
		setSpawnToPlayerPos(deathSpawn);
	}

	public void setLogoutSpawnToPlayerPos() {
		setSpawnToPlayerPos(logoutSpawn);
	}

	public void setDeathSpawnTo(SegmentController s, Vector3i posOnEntity) {
		setSpawnTo(deathSpawn, s, posOnEntity);
	}

	public void setSpawnTo(Spawn spawn, SegmentController s, Vector3i posOnEntity) {
		spawn.localPos.set(posOnEntity.x - SegmentData.SEG_HALF, posOnEntity.y - SegmentData.SEG_HALF, posOnEntity.z - SegmentData.SEG_HALF);
		spawn.UID = s.getUniqueIdentifier();
		spawn.absolutePosBackup.set(s.getWorldTransform().origin);
		playerState.sendServerMessagePlayerInfo(Lng.astr("Spawn point set on\n%s",  s.toNiceString()));
	}

	public void setSpawnToPlayerPos(Spawn spawn) {
		SimpleTransformableSendableObject sc = playerState.getFirstControlledTransformableWOExc();
		if (sc != null) {
			if (sc instanceof SegmentController) {
				Object parameter = playerState.getControllerState().getUnits().iterator().next().parameter;
				if (parameter != null && parameter instanceof Vector3i) {
					Vector3i v = (Vector3i) parameter;
					spawn.localPos.set(v.x - SegmentData.SEG_HALF, v.y - SegmentData.SEG_HALF, v.z - SegmentData.SEG_HALF);

					spawn.UID = sc.getUniqueIdentifier();
				}
			} else if (sc instanceof PlayerCharacter) {
				if (((PlayerCharacter) sc).getGravity().source != null) {

					SimpleTransformableSendableObject source = ((PlayerCharacter) sc).getGravity().source;
					//we are in a gravity
					spawn.UID = source.getUniqueIdentifier();

					spawn.localPos.set(((PlayerCharacter) sc).getWorldTransform().origin);

					Transform t = new Transform(source.getWorldTransform());
					t.inverse();
					//relative position to entity to spawn on
					t.transform(spawn.localPos);

//					System.err.println("LOCAL::: "+spawn.localPos+" :: "+);

				} else {
					spawn.UID = "";
					//spawn position is the absolute position of where this character currrently is
					spawn.localPos.set(((PlayerCharacter) sc).getWorldTransform().origin);
				}
			}
			spawn.absoluteSector.set(playerState.getCurrentSector());
			spawn.absolutePosBackup.set(sc.getWorldTransform().origin);
		}
	}

	public void setSpawnGravity(PlayerCharacter c, boolean spawnedOnce) {
		getSpawn(spawnedOnce).setSpawnGravity(c);
	}

	public Spawn getSpawn(boolean spawnedOnce) {
		return spawnedOnce ? deathSpawn : logoutSpawn;
	}

	public void onSpawnPlayerCharacter(PlayerCharacter c, boolean spawnedOnce) throws IOException {
		GameServerState state = ((GameServerState) playerState.getState());

		Spawn spawn = getSpawn(spawnedOnce);

		Vector3i sectorPos = null;

		Sector spawnSector = state.getUniverse().getSector(spawn.absoluteSector);
		c.setSectorId(spawnSector.getId());
		if (!playerState.getCurrentSector().equals(spawnSector.pos)) {
			System.err.println("[SERVER] spawn player character: doing sector switch to " + spawnSector.pos);
			playerState.setCurrentSector(spawnSector.pos);
			playerState.setCurrentSectorId(spawnSector.getId());
		}

		c.getInitialTransform().setIdentity();

		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(spawn.UID);
		if (sendable == null || !(sendable instanceof SimpleTransformableSendableObject)) {
			//the spawn is not boud to another entity
			c.getInitialTransform().origin.set(spawn.localPos);

//			assert(false):"ABSOLUTE :::: "+spawn.localPos;
			System.err.println("[SERVER][SPAWN] " + playerState + "; ABSOLUTE :::: " + spawn.localPos);
		} else {
			Sector spawnSectorFromObject = state.getUniverse().getSector(sendable.getId());
			if(spawnSectorFromObject != null && spawnSectorFromObject != spawnSector){
				spawnSector = spawnSectorFromObject;
				playerState.setCurrentSector(spawnSector.pos);
				playerState.setCurrentSectorId(spawnSector.getId());
			}
			
			SimpleTransformableSendableObject<?> sc = (SimpleTransformableSendableObject<?>) sendable;
			Vector3f spawnPos = new Vector3f(spawn.localPos);

			c.spawnOnObjectId = sendable.getId();
			c.spawnOnObjectLocalPos = new Vector3f(spawn.localPos);
			
			sc.getWorldTransform().transform(spawnPos);
			c.getInitialTransform().origin.set(spawnPos);

			System.err.println("[SERVER][SPAWN] " + playerState + "; Relative: " + sc + " -> " + spawnPos + "; objOrigin: " + sc.getWorldTransform().origin);
		}

		playerState.spawnData.setSpawnGravity(c, playerState.spawnedOnce);
	}
	public Sector getSpawnSector(boolean spawnedOnce) throws IOException{
		GameServerState state = ((GameServerState) playerState.getState());
		Spawn spawn = getSpawn(spawnedOnce);

		
		if(Sector.isTutorialSector(spawn.absoluteSector)){
			spawn.UID = "";
			if(preSpecialSector != null && preSpecialSectorTransform != null){
				spawn.absoluteSector.set(preSpecialSector);
				spawn.localPos.set(preSpecialSectorTransform.origin);
				spawn.absolutePosBackup.set(spawn.localPos);
			}else{
				spawn.absoluteSector.set(ServerConfig.DEFAULT_SPAWN_SECTOR_X.getInt(),
				ServerConfig.DEFAULT_SPAWN_SECTOR_Y.getInt(),
				ServerConfig.DEFAULT_SPAWN_SECTOR_Z.getInt());
				Vector3f[] defaultSpawingPoints = getDefaultSpawnPoints();
				spawn.localPos.set(defaultSpawingPoints[spawnPointRotationId]);
				spawn.absolutePosBackup.set(spawn.localPos);
			}
		}
		
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(spawn.UID);
		Sector spawnSector;
		if (sendable == null || !(sendable instanceof SimpleTransformableSendableObject)) {

			Vector3i sectorPos = null;
			List<DatabaseEntry> byUIDExact;
			try {
				if (spawn.UID.length() > 0) {
					byUIDExact = state.getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefixWOException(spawn.UID), -1);
					if (byUIDExact.size() == 1) {
						sectorPos = new Vector3i(byUIDExact.get(0).sectorPos);
					} else {
//						assert(false):spawn.UID+"; "+byUIDExact;
						System.err.println("[SERVER][PLAYERSTATE] "+this+": The ship the player logged out on does no longer exist! Spawning at last known position...");
						playerState.sendServerMessagePlayerError(Lng.astr("The ship you logged out on\ndoes no longer exist!\nSpawning at last known position..."));
						spawn.UID = "";
						//used last know absolute position
						spawn.localPos.set(spawn.absolutePosBackup);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (sectorPos == null) {
				//entity to spawn on not found
				sectorPos = new Vector3i(spawn.absoluteSector);

			}
			//loads the sector and activates it
			spawnSector = state.getUniverse().getSector(sectorPos);
			spawn.absoluteSector.set(spawnSector.pos);
			
			System.err.println("[SERVER][PLAYERSTATE] Set spawn sector from unloaded object: "+spawnSector);
		} else {

			SimpleTransformableSendableObject<?> sc = (SimpleTransformableSendableObject<?>) sendable;

			spawnSector = state.getUniverse().getSector(sc.getSectorId());
			if (spawnSector != null) {
				spawn.absoluteSector.set(spawnSector.pos);
			}
			System.err.println("[SERVER][PLAYERSTATE] Set spawn sector from loaded object: "+spawnSector);
		}
		
		return spawnSector;
	}
	/**
	 * load relevant sector and entities so they are available for certain in the next update
	 *
	 * @param spawnedOnce
	 * @throws IOException
	 */
	public void onSpawnPreparePlayerCharacter(boolean spawnedOnce) throws IOException {
		GameServerState state = ((GameServerState) playerState.getState());
		Sector spawn = getSpawnSector(spawnedOnce);

		//so that onSpawnPlayerCharacter() is spawned next update (when all relevant entitis have been spanwed)
		state.getSpawnRequestsReady().add(playerState);
	}

	public enum SpawnType {
		ABSOLUTE,
		ON_ENTITY
	}

	public class Spawn {
		public String UID = "";

		public final Vector3i absoluteSector = new Vector3i();
		public final Vector3f localPos = new Vector3f();
		public final Vector3f absolutePosBackup = new Vector3f();

		public final Vector3f gravityAcceleration = new Vector3f();

		/**
		 * @param c to mod
		 */
		public void setSpawnGravity(PlayerCharacter c) {
			GameServerState state = ((GameServerState) playerState.getState());
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(UID);
			if (sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				System.err.println("[SERVER][SPAWN] Gravity object to use for " + playerState + " set to: " + UID);
				Vector3f grav = new Vector3f();
				if(state.getCurrentGravitySources().contains(sendable)) {
					grav.y = -9.81f;
				}
				c.scheduleGravityServerForced(grav, (SimpleTransformableSendableObject) sendable);
			} else {
				System.err.println("[SERVER][SPAWN] Gravity object to use for " + playerState + " not found. UID: " + UID);
			}
		}

		public Tag toTagStructure() {
			return new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.STRING, null, UID),
					new Tag(Type.VECTOR3i, null, absoluteSector),
					new Tag(Type.VECTOR3f, null, localPos),
					new Tag(Type.VECTOR3f, null, gravityAcceleration),
					new Tag(Type.VECTOR3f, null, absolutePosBackup),
					FinishTag.INST,
			});
		}

		public void fromTagStructure(Tag tag) {
			Tag[] v = (Tag[]) tag.getValue();
			UID = (String) v[0].getValue();
			absoluteSector.set((Vector3i) v[1].getValue());
			localPos.set((Vector3f) v[2].getValue());
			gravityAcceleration.set((Vector3f) v[3].getValue());
			if (v.length > 4 && v[4].getType() == Type.VECTOR3f) {
				absolutePosBackup.set((Vector3f) v[4].getValue());
			}
		}

		@Override
		public String toString() {
			return "Spawn [UID=" + UID + ", absoluteSector=" + absoluteSector
					+ ", localPos=" + localPos + ", absolutePosBackup="
					+ absolutePosBackup + ", gravityAcceleration="
					+ gravityAcceleration + "]";
		}
		
		
	}

	@Override
	public String toString() {
		return "PlayerStateSpawnData [thisLogin=" + thisLogin + ", lastLogin="
				+ lastLogin + ", lastLogout=" + lastLogout + ", deathSpawn="
				+ deathSpawn + ", logoutSpawn=" + logoutSpawn + "]";
	}

}
