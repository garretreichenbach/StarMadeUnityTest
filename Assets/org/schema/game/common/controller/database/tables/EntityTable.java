package org.schema.game.common.controller.database.tables;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.schema.common.util.LogInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.PercentCallbackInterface;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.SimDatabaseEntry;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.world.EntityUID;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SystemRange;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.geo.NPCSystem;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EntityTable extends Table {

	public EntityTable(TableManager m, Connection c) {
		super("ENTITIES", m, c);
	}

	private static List<String> getDataFilesListOld(String UID) {
		String fil = UID + ".";
		FilenameFilter filter = (dir, name) -> name.startsWith(fil);
		File dir = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
		String[] list = dir.list(filter);

		List<String> l = new ObjectArrayList<String>();

		Collections.addAll(l, list);
		return l;
	}

	private static List<String> getFileNames(List<String> la) {
		List<String> l = new ObjectArrayList<String>();
		for(String s : la) {
			l.add((new FileExt(s)).getName());
		}
		return l;
	}

	public static long updateOrInsertSegmentController(Statement s, String uid, Vector3i sectorPos, int type, long seed,
	                                                   String lastModifier, String spawner, String realName, boolean touched,
	                                                   int faction, Vector3f pos, Vector3i min, Vector3i max, int creatorId,
	                                                   long dockedTo, long dockedRoot, boolean spawnedOnlyInDb, boolean tracked) throws SQLException {
		long id = -1;
		try {
			if(Float.isNaN(pos.x) || Float.isNaN(pos.y) || Float.isNaN(pos.z)) {
				pos.set(400, 400, 400);
				System.err.println("Exception: POS NaN: ");
				System.err.println("UID: " + uid);
				System.err.println("SECPOS: " + sectorPos);
				System.err.println("TYPE: " + type);
				System.err.println("SEED: " + seed);
				System.err.println("lastModifier: " + lastModifier);
				System.err.println("spawner: " + spawner);
				System.err.println("realName: " + realName);
				System.err.println("touched: " + touched);
				System.err.println("faction: " + faction);
				System.err.println("pos: " + pos);
				System.err.println("min: " + min);
				System.err.println("max: " + max);
				System.err.println("creatorId: " + creatorId);
			}
			if(realName.length() > 64) {
				System.err.println("[DATABASE] WARNING. ENTITY NAME IS TOO LONG AND WILL BE SHORTENED: " + realName + " -> " + realName.substring(0, 63));
				realName = realName.substring(0, 63);
			}
			if(spawner.length() > 64) {
				System.err.println("[DATABASE] WARNING. ENTITY CREATOR NAME IS TOO LONG AND WILL BE SHORTENED: " + spawner + " -> " + spawner.substring(0, 63));
				spawner = spawner.substring(0, 63);

			}
			long t = System.currentTimeMillis();
			long t0 = System.currentTimeMillis();
			long insertOrUpdateTime = 0;
			long stringBuildTime = 0;

			ResultSet query = s.executeQuery("SELECT ID FROM ENTITIES WHERE UID = '" + uid + "' AND TYPE = " + type + ";");
			long queryTime = System.currentTimeMillis() - t0;

			if(query.next()) {
				id = query.getLong(1);
				t0 = System.currentTimeMillis();

				String b = "UPDATE ENTITIES SET (UID,X,Y,Z,TYPE,NAME,FACTION,CREATOR,LAST_MOD,SEED,TOUCHED,LOCAL_POS,DIM,GEN_ID,DOCKED_TO,DOCKED_ROOT,SPAWNED_ONLY_IN_DB, TRACKED) = (" +
						"'" + uid + "'" + "," +
						sectorPos.x + "," +
						sectorPos.y + "," +
						sectorPos.z + "," +
						type + "," +
						"'" + realName + "'" + "," +
						faction + "," +
						"'" + spawner + "'" + "," +
						"'" + lastModifier + "'" + "," +
						seed + "," +
						touched + "," +
						"ARRAY[" + pos.x + "," + pos.y + "," + pos.z + "]," +
						"ARRAY[" + min.x + "," + min.y + "," + min.z + "," +
						max.x + "," + max.y + "," + max.z + "]," +
						creatorId + "," +
						dockedTo + "," +
						dockedRoot + "," +
						spawnedOnlyInDb + "," +
						tracked +
						") WHERE ID = " + id + ";";
				stringBuildTime = System.currentTimeMillis() - t0;

				t0 = System.currentTimeMillis();
				//			updateStatement.executeUpdate();
				s.executeUpdate(b);
				insertOrUpdateTime = System.currentTimeMillis() - t0;

			} else {

				//		assert(!uid.startsWith("ENTITY_")):uid;
				//		s.executeUpdate("DELETE FROM ENTITIES WHERE UID = '"+uid+"' AND TYPE = "+type+";");

				t0 = System.currentTimeMillis();

				PreparedStatement p = s.getConnection().prepareStatement(
						"INSERT INTO ENTITIES(UID,X,Y,Z,TYPE,NAME,FACTION,CREATOR, LAST_MOD, SEED, TOUCHED, LOCAL_POS, DIM, GEN_ID, DOCKED_TO, DOCKED_ROOT, SPAWNED_ONLY_IN_DB, TRACKED) VALUES(" +
								"CAST(? AS VARCHAR(128))," + //uid
								"CAST(? AS INT)," + //x
								"CAST(? AS INT)," + //y
								"CAST(? AS INT)," + //z
								"CAST(? AS INT)," + //type
								"CAST(? AS VARCHAR(64))," + //name
								"CAST(? AS INT)," + //faction
								"CAST(? AS VARCHAR(64))," + //creator
								"CAST(? AS VARCHAR(64))," + //last mod
								"CAST(? AS BIGINT)," + //seed
								"CAST(? AS BOOLEAN)," + //touched
								"CAST(? AS FLOAT ARRAY[3])," + //local pos
								"CAST(? AS INT ARRAY[6])," + //dim
								"CAST(? AS INT)," + //creator id
								"CAST(? AS BIGINT)," + // docked to
								"CAST(? AS BIGINT)," + //docked root
								"CAST(? AS BOOLEAN)," + //docked root
								"CAST(? AS BOOLEAN)" + //tracked
								");", Statement.RETURN_GENERATED_KEYS);

				p.setString(1, uid);
				p.setInt(2, sectorPos.x);
				p.setInt(3, sectorPos.y);
				p.setInt(4, sectorPos.z);
				p.setInt(5, type);
				p.setString(6, realName);
				p.setInt(7, faction);
				p.setString(8, spawner);
				p.setString(9, lastModifier);
				p.setLong(10, seed);
				p.setBoolean(11, touched);

				p.setArray(12, s.getConnection().createArrayOf("FLOAT", new Double[]{(double) pos.x, (double) pos.y, (double) pos.z}));
				p.setArray(13, s.getConnection().createArrayOf("INT", new Integer[]{min.x, min.y, min.z, max.x, max.y, max.z}));
				p.setInt(14, creatorId);
				p.setLong(15, dockedTo);
				p.setLong(16, dockedRoot);
				p.setBoolean(17, spawnedOnlyInDb);
				p.setBoolean(18, tracked);
				p.executeUpdate();

//				StringBuilder b = new StringBuilder();
//				b.append("INSERT INTO ENTITIES VALUES(");
//				b.append("'").append(uid).append("'").append(",");
//				b.append(sectorPos.x).append(",");
//				b.append(sectorPos.y).append(",");
//				b.append(sectorPos.z).append(",");
//				b.append(type).append(",");
//				b.append("'").append(realName).append("'").append(",");
//				b.append(faction).append(",");
//				b.append("'").append(spawner).append("'").append(",");
//				b.append("'").append(lastModifier).append("'").append(",");
//				b.append(seed).append(",");
//				b.append(touched).append(",");
//				b.append("ARRAY[").append(pos.x).append(",").append(pos.y).append(",").append(pos.z).append("],");
//				b.append("ARRAY[").append(min.x).append(",").append(min.y).append(",").append(min.z).append(",")
//						.append(max.x).append(",").append(max.y).append(",").append(max.z).append("],");
//				b.append(creatorId).append(",");
//				b.append(dockedTo).append(",");
//				b.append(dockedRoot).append(",");
//				b.append("NULL");
//				b.append(");");
//				stringBuildTime = System.currentTimeMillis() - t0;
//
//				t0 = System.currentTimeMillis();
//				s.executeUpdate(b.toString(), Statement.RETURN_GENERATED_KEYS);
				insertOrUpdateTime = System.currentTimeMillis() - t0;

				ResultSet generatedKeys = p.getGeneratedKeys();
				generatedKeys.next();
				id = generatedKeys.getLong(1);
				p.close();
			}

			long totalTime = System.currentTimeMillis() - t;

			if(totalTime > 20) {
				System.err.println("[SQL] WARNING: ROW UPDATE TOOK " + totalTime + " on " + uid + ": query: " + queryTime + "; stringBuild: " + stringBuildTime + "; insertOrUpdate: " + insertOrUpdateTime);
			}

			//		ResultSet executeQuery = s.executeQuery("SELECT SEED FROM ENTITIES WHERE UID = "+uid+";");
			//		executeQuery.next();
			//		System.err.println("INSERTING "+uid+": "+seed+" == "+executeQuery.getLong(1));

			//		s.executeUpdate("REPLACE INTO ENTITIES VALUES("+
			//				"'"+uid+"'"+","+
			//				sectorPos.x+","+
			//				sectorPos.y+","+
			//				sectorPos.z+","+
			//				type+","+
			//				"'"+realName+"'"+","+
			//				faction+","+
			//				"'"+spawner+"'"+","+
			//				"'"+lastModifier+"'"+","+
			//				seed+","+
			//				touched+");");
		} catch(SQLSyntaxErrorException e) {
			System.err.println("Exception: FAILED TO HANDLE SQL: ");
			System.err.println("UID: " + uid);
			System.err.println("SECPOS: " + sectorPos);
			System.err.println("TYPE: " + type);
			System.err.println("SEED: " + seed);
			System.err.println("lastModifier: " + lastModifier);
			System.err.println("spawner: " + spawner);
			System.err.println("realName: " + realName);
			System.err.println("touched: " + touched);
			System.err.println("faction: " + faction);
			System.err.println("pos: " + pos);
			System.err.println("min: " + min);
			System.err.println("max: " + max);
			System.err.println("creatorId: " + creatorId);

			throw e;
		}
		return id;
	}

	private static int getType(SegmentController c) {
		return c.getType().dbTypeId;
	}

	public static long updateOrInsertSegmentController(Statement s, SegmentController c) throws SQLException {
		GameServerState state = (GameServerState) c.getState();

		Sector sector = state.getUniverse().getSector(c.getSectorId());
		boolean touched;
		if(c instanceof TransientSegmentController) {
			touched = ((TransientSegmentController) c).isTouched();
		} else {
			touched = true;
		}
		Vector3i secPos;
		boolean transientSec = false;
		if(sector == null) {
			//			System.err.println("[SQL][WANRING] Sector null for "+c+"; using transient sector: "+c.transientSector);
			//should have been set by sector itself at writing
			secPos = c.transientSectorPos;
			transientSec = c.transientSector;
		} else {
			secPos = sector.pos;
			transientSec = sector.isTransientSector();
		}
		long id = -1;
		if(c instanceof TransientSegmentController && !((TransientSegmentController) c).needsTagSave() && transientSec) {
			//no need to write this as it's all default values derived from the sector
			return id;
		}

		if(c.hasChangedForDb() || c.getTagSectorId() == null || !c.getTagSectorId().equals(secPos) || !c.getWorldTransform().equals(c.getInitialTransform())) {
			id = updateOrInsertSegmentController(s, c.getUniqueIdentifier().split("_", 3)[2], secPos, getType(c), c.getSeed(),
					c.getLastModifier(), c.getSpawner(), c.getRealName(),
					touched, c.getFactionId(), c.getWorldTransform().origin,
					c.getMinPos(), c.getMaxPos(), c.getCreatorId(),
					c.railController.isDockedAndExecuted() ? c.railController.previous.rail.getSegmentController().dbId : -1L,
					c.railController.isDockedAndExecuted() ? c.railController.getRoot().dbId : -1L, false, c.isTracked());
			c.setChangedForDb(false);
			c.getTagSectorId().set(secPos);
			c.getInitialTransform().set(c.getWorldTransform());

			if(id >= 0) {
				c.dbId = id;
			}

		} else {
			System.err.println("[SERVER] Database: Entry " + c + " skipped SQL update since it hasn't changed");
		}

		if(c instanceof Planet || c instanceof SpaceStation) {
			StationaryManagerContainer<?> man = (StationaryManagerContainer<?>) ((ManagedSegmentController<?>) c).getManagerContainer();
			String fromUID = DatabaseEntry.removePrefix(c.getUniqueIdentifier());

			/*
			 "+getFieldsString()+" Generate map of all connection for this object in database
			 */

			ResultSet fromThis = s.executeQuery("SELECT FROM_X, FROM_Y, FROM_Z FROM FTL WHERE FROM_UID = '" + fromUID + "';");

			boolean dirt = false;
			while(fromThis.next()) {
				Vector3i from = new Vector3i(fromThis.getInt(1), fromThis.getInt(2), fromThis.getInt(3));
				if(!from.equals(secPos)) {
					System.err.println("[DATABASE] FTL changed origin position. Removing and reentering all entries for " + c);
					FTLTable.deleteFTLEntry(s.getConnection(), man.getWarpgate().getElementManager(), null);
					dirt = true;
					break;
				}
			}
			if(man.getWarpgate().getElementManager().getCollectionManagers().size() > 0) {
				FTLTable.updateOrInsertFTLEntry(s.getConnection(), man.getWarpgate().getElementManager());
				dirt = true;
			}
			if(man.getRacegate().getElementManager().getCollectionManagers().size() > 0) {
				FTLTable.updateOrInsertFTLEntry(s.getConnection(), man.getRacegate().getElementManager());
				dirt = true;
			}
			if(dirt) {
				synchronized(state.getUniverse().ftlDirty) {
					state.getUniverse().ftlDirty.enqueue(secPos);
				}
			}

		}

		if(c instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShopInterface) {
			TradeNodeTable.writeTradeNode(s, state, secPos, ((ShopInterface) ((ManagedSegmentController<?>) c).getManagerContainer()));
		}

		return id;
	}

	public static boolean removeSegmentController(Statement s, String fullUID, GameServerState state) throws SQLException {
		String dbId = DatabaseEntry.removePrefixWOException(fullUID);

		ResultSet q = s.executeQuery("SELECT ID FROM ENTITIES WHERE UID = '" + dbId + "';");

		long id = -1;

		if(q.next()) {
			id = q.getLong(1);
			int a = s.executeUpdate("DELETE FROM FLEET_MEMBERS WHERE ENTITY_ID = " + id + ";");

			int rows = s.executeUpdate("DELETE FROM ENTITIES WHERE UID = '" + dbId + "';");

			state.getUniverse().writeMap.remove(fullUID);

			System.err.println("[DATABASE] REMOVING SEGCONTROLLER: " + dbId + "; returned: " + rows + "; fleet dbId: " + id);
			return rows > 0;
		} else {
			return false;
		}
	}

	public static void removeSegmentController(Statement s, long c) throws SQLException {
		s.executeUpdate("DELETE FROM FLEET_MEMBERS WHERE ENTITY_ID = " + c + ";");
		s.executeUpdate("DELETE FROM ENTITIES WHERE ID = " + c + ";");
	}

	public static void removeSegmentController(Statement s, SegmentController c) throws SQLException {
		s.executeUpdate("DELETE FROM FLEET_MEMBERS WHERE ENTITY_ID = " + c.dbId + ";");
		s.executeUpdate("DELETE FROM ENTITIES WHERE UID = '" + DatabaseEntry.removePrefixWOException(c.getUniqueIdentifier()) + "';");
		((GameServerState) c.getState()).getUniverse().writeMap.remove(c.getUniqueIdentifier());
	}

	@Override
	public void define() {
		addColumn("UID", "VARCHAR(128) not null");
		addColumn("X", "INT NOT NULL");
		addColumn("Y", "INT NOT NULL");
		addColumn("Z", "INT NOT NULL");
		addColumn("TYPE", "TINYINT NOT NULL");
		addColumn("NAME", "char(64)");
		addColumn("FACTION", "INT DEFAULT 0");
		addColumn("CREATOR", "VARCHAR(64)");
		addColumn("LAST_MOD", "VARCHAR(64)");
		addColumn("SEED", "BIGINT");
		addColumn("TOUCHED", "BOOLEAN");
		addColumn("LOCAL_POS", "FLOAT ARRAY[3]");
		addColumn("DIM", "INT ARRAY[6]");
		addColumn("GEN_ID", "INT");
		addColumn("DOCKED_TO", "BIGINT DEFAULT -1");
		addColumn("DOCKED_ROOT", "BIGINT DEFAULT -1");
		addColumn("SPAWNED_ONLY_IN_DB", "BOOLEAN DEFAULT FALSE");
		addColumn("TRACKED", "BOOLEAN DEFAULT FALSE");
		addColumn("ID", "BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1)", true);
		addIndex("uidType", true, "UID", "TYPE");
		addIndex("ENTITIES_PK", "UID");
		addIndex("coordX", "X");
		addIndex("coordY", "Y");
		addIndex("coordZ", "Z");
		addIndex("coordIndex", "X", "Y", "Z");
		addIndex("coordIndexDT", "X", "Y", "Z", "DOCKED_TO");
		addIndex("typeIndex", "TYPE");
		addIndex("dockedToIndex", "DOCKED_TO");
		addIndex("dockedRootIndex", "DOCKED_ROOT");

	}

	@Override
	public void afterCreation(Statement s) {

	}

	public void optimizeDatabase(PercentCallbackInterface percentCallbackInterface) throws SQLException {
		Statement s = c.createStatement();
		s.executeUpdate("UPDATE ENTITIES SET TOUCHED = FALSE WHERE TYPE = 3 AND LAST_MOD = '';");

		ResultSet rCount = s.executeQuery("SELECT COUNT(*) FROM ENTITIES WHERE TYPE = 3 AND LAST_MOD = '';");
		rCount.next();
		float total = rCount.getInt(1);

		ResultSet r = s.executeQuery("SELECT UID, SEED FROM ENTITIES WHERE TYPE = 3 AND LAST_MOD = '';");

		Random rand = new Random();
		File[] ents = (new FileExt(GameServerState.ENTITY_DATABASE_PATH)).listFiles();
		File[] data = (new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH)).listFiles();
		float cur = 0;
		while(r.next()) {

			String looseUID = r.getString(1).trim();
			long seed = r.getLong(2);
			String fileName = SimpleTransformableSendableObject.EntityType.ASTEROID.dbPrefix + looseUID;
			if(percentCallbackInterface != null) {
				percentCallbackInterface.update((int) ((cur / total) * 100) + "% (" + fileName + ")");
			}
			for(int i = 0; i < ents.length; i++) {
				if(ents[i].getName().startsWith(fileName)) {
					ents[i].delete();
					System.err.println("REMOVED FILE: " + fileName);
					break;
				}

			}

			for(int i = 0; i < data.length; i++) {
				if(data[i].getName().startsWith(fileName)) {
					data[i].delete();
					System.err.println("REMOVED DATA FILE: " + fileName);
				}
			}

			if(cur > 0 && ((int) cur) % 200 == 0) {
				ents = (new FileExt(GameServerState.ENTITY_DATABASE_PATH)).listFiles();
				data = (new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH)).listFiles();
			}
			if(seed == 0) {
				s.executeUpdate("UPDATE ENTITIES SET SEED = " + rand.nextLong() + " WHERE TYPE = 3 AND UID = '" + looseUID + "';");
			}
			cur++;

		}

		s.close();
	}

	public int despawn(String sqlregexp, Despawn mode, Vector3i sectorFilter, SimpleTransformableSendableObject.EntityType type) throws SQLException {
		Statement s = c.createStatement();
		String unusedCom = switch(mode) {
			case UNUSED -> "AND LAST_MOD = '' ";
			case USED -> "AND NOT LAST_MOD = '' ";
			case ALL -> "";
		};
		if(sectorFilter != null) {
			unusedCom += "AND X = " + sectorFilter.x + " AND Y = " + sectorFilter.y + " AND Z = " + sectorFilter.z + " ";
		}
		if(type != null) {
			unusedCom += " AND TYPE = " + type.dbTypeId + " ";
		}
		ResultSet r = s.executeQuery("SELECT UID, DIM, ID FROM ENTITIES WHERE UID LIKE '" + sqlregexp + "' " + unusedCom + ";");
		int count = 0;
		while(r.next()) {
			String looseUID = r.getString(1).trim();

			s.executeUpdate("DELETE FROM FLEET_MEMBERS WHERE ENTITY_ID = " + r.getLong(3) + ";");
			s.executeUpdate("DELETE FROM ENTITIES WHERE ID = " + r.getLong(3) + "; ");

			String fullUID = SimpleTransformableSendableObject.EntityType.SHIP.dbPrefix + looseUID;
			File entityFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH + fullUID + ".ent");
			boolean delete = entityFile.delete();

			Array posMinArray = r.getArray(2);
			Object[] dimension = (Object[]) posMinArray.getArray();
			Vector3i minPos = new Vector3i((Integer) dimension[0], (Integer) dimension[1], (Integer) dimension[2]);
			Vector3i maxPos = new Vector3i((Integer) dimension[3], (Integer) dimension[4], (Integer) dimension[5]);

			List<String> allFiles = SegmentDataFileUtils.getAllFiles(minPos, maxPos, fullUID, null);

			assert (getFileNames(allFiles).containsAll(getDataFilesListOld(fullUID))) : "\n" + getFileNames(allFiles) + ";\n\n" + getDataFilesListOld(fullUID);

			int found = 0;

			for(String sA : allFiles) {
				File f = new FileExt(sA);
				if(f.exists()) {
					found++;
					System.err.println("[DATABASE][REMOVE] removing raw block data file: " + f.getName() + " (exists: " + f.exists() + ")");
					f.delete();
				}
			}

			System.err.println("[SQL] DESPAWNING: " + fullUID + " DELETE .ENT SUC " + delete + "; RAW DATA FILES FOUND AND REMOVED: " + found);
			count++;
		}

		s.close();
		return count;
	}

	public void loadAndAddFleetMember(Fleet f, FleetMember m) {
		Statement s;
		try {
			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT UID, X, Y, Z, NAME, TYPE FROM ENTITIES WHERE ID = " + m.entityDbId + ";");

			if(q.next()) {
				String uidSimple = q.getString(1);
				m.getSector().set(q.getInt(2), q.getInt(3), q.getInt(4));
				String name = q.getString(5);
				byte type = q.getByte(6);

				String UID = DatabaseEntry.getWithFilePrefix(uidSimple, type);

				File sE = new FileExt(GameServerState.DATABASE_PATH + UID + ".ent");
				if(sE.exists()) {
					m.UID = UID;
					m.name = name.trim();

					f.getMembers().add(m);
				} else {
					System.err.println("[DATABASE][ERROR] Failed to load fleet memeber UID: " + UID + ": Missing .ent file");
				}

			} else {
				System.err.println("[ERROR] couldnt load fleet member info for " + m + ": Entity not in database!");
			}

			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void changeSectorForEntity(String string, Vector3i to) throws SQLException {
		Statement s = c.createStatement();
		s.executeUpdate("UPDATE ENTITIES SET (X,Y,Z) = (" + to.x + ", " + to.y + ", " + to.z + ") WHERE UID = '" + string + "';");
		s.close();
	}

	public void changeSectorForEntity(long dbId, Vector3i to) throws SQLException {
		Statement s = c.createStatement();
		s.executeUpdate("UPDATE ENTITIES SET (X,Y,Z) = (" + to.x + ", " + to.y + ", " + to.z + ") WHERE ID = " + dbId + ";");
		s.close();
	}

	public void changeSectorForEntity(long dbId, Vector3i to, Vector3f local, boolean avoid) throws SQLException {
		Statement s = c.createStatement();
		if(dbId < 0) {
			throw new IllegalArgumentException();
		}
		if(avoid) {
			ResultSet r = s.executeQuery("SELECT LOCAL_POS, DIM FROM ENTITIES WHERE X = " + to.x + " AND Y = " + to.y + " AND Z = " + to.z + " AND DOCKED_TO = -1;");

			List<BoundingBox> bbs = new ObjectArrayList<BoundingBox>();

			while(r.next()) {
				BoundingBox bb = new BoundingBox();

				Array posArray = r.getArray(1);
				Object[] pArray = (Object[]) posArray.getArray();
				Vector3f pos = new Vector3f(((Double) pArray[0]).floatValue(), ((Double) pArray[1]).floatValue(), ((Double) pArray[2]).floatValue());

				Array posMinArray = r.getArray(2);
				Object[] dimension = (Object[]) posMinArray.getArray();
				Vector3i minPos = new Vector3i((Integer) dimension[0], (Integer) dimension[1], (Integer) dimension[2]);
				Vector3i maxPos = new Vector3i((Integer) dimension[3], (Integer) dimension[4], (Integer) dimension[5]);

				bb.min.set(pos.x + (minPos.x * SegmentData.SEG), pos.y + (minPos.y * SegmentData.SEG), pos.z + (minPos.z * SegmentData.SEG));
				bb.max.set(pos.x + (maxPos.x * SegmentData.SEG), pos.y + (maxPos.y * SegmentData.SEG), pos.z + (maxPos.z * SegmentData.SEG));

				bbs.add(bb);
			}
//			System.err.println("[SERVER][DB] Sector change issued: DB_ID "+dbId+" -> "+to+"; fetched entities to avoid overlapping: "+bbs.size());
			ResultSet rSelf = s.executeQuery("SELECT DIM FROM ENTITIES WHERE ID = " + dbId + ";");

			if(rSelf.next()) {
				Array posMinArray = rSelf.getArray(1);
				Object[] dimension = (Object[]) posMinArray.getArray();
				Vector3i minPos = new Vector3i((Integer) dimension[0], (Integer) dimension[1], (Integer) dimension[2]);
				Vector3i maxPos = new Vector3i((Integer) dimension[3], (Integer) dimension[4], (Integer) dimension[5]);

				BoundingBox ownBB = new BoundingBox();
				ownBB.min.set(local.x + (minPos.x * SegmentData.SEG), local.y + (minPos.y * SegmentData.SEG), local.z + (minPos.z * SegmentData.SEG));
				ownBB.max.set(local.x + (maxPos.x * SegmentData.SEG), local.y + (maxPos.y * SegmentData.SEG), local.z + (maxPos.z * SegmentData.SEG));

				Vector3f pp = new Vector3f(local);

				avoid(pp, local, ownBB, bbs, minPos, maxPos);

				local.set(pp);
			}

		}
		StringBuilder b = new StringBuilder();
		b.append("ARRAY[").append(local.x).append(",").append(local.y).append(",").append(local.z).append("]");
		s.executeUpdate("UPDATE ENTITIES SET (X,Y,Z, LOCAL_POS) = (" + to.x + ", " + to.y + ", " + to.z + ", " + b + ") WHERE ID = " + dbId + ";");

		//move all docked entities too
		s.executeUpdate("UPDATE ENTITIES SET (X,Y,Z, LOCAL_POS) = (" + to.x + ", " + to.y + ", " + to.z + ", " + b + ") WHERE DOCKED_ROOT = " + dbId + ";");

		s.close();

	}

	private void avoid(Vector3f pp, Vector3f local, BoundingBox ownBB, List<BoundingBox> bbs, Vector3i minPos, Vector3i maxPos) {
		int c = 0;
		while(ownBB.overlapsAny(bbs)) {
			pp.set(local);
			float size = (c + 1) * 100;
			for(int z = -1; z <= 1; z++) {
				for(int y = -1; y <= 1; y++) {
					for(int x = -1; x <= 1; x++) {
						Vector3f dir = Element.DIRECTIONSf[(c / 2) % 6];

						pp.x += dir.x * size * x;
						pp.y += dir.y * size * y;
						pp.z += dir.z * size * z;

						ownBB.min.set(pp.x + (minPos.x * SegmentData.SEG), pp.y + (minPos.y * SegmentData.SEG), pp.z + (minPos.z * SegmentData.SEG));
						ownBB.max.set(pp.x + (maxPos.x * SegmentData.SEG), pp.y + (maxPos.y * SegmentData.SEG), pp.z + (maxPos.z * SegmentData.SEG));

						if(!ownBB.overlapsAny(bbs)) {
							return;
						}
					}
				}
			}

			c++;

			if(c > 1000) {
				System.err.println("[SERVER][DB] Exception: Change Sector avoiding: Not found suitable position. Stoppping at: " + pp);
				return;
			}
		}
	}

	public List<DatabaseEntry> getBySector(Vector3i sector, int max) throws SQLException {
		Statement s = c.createStatement();
		if(max >= 0) {
			s.setMaxRows(Math.max(0, max));
		}
		long t = System.currentTimeMillis();
		ResultSet r = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE X = " + sector.x + " AND Y = " + sector.y + " AND Z = " + sector.z + ";");
		System.err.println("[SQL] SECTOR QUERY TOOK " + (System.currentTimeMillis() - t) + "ms");
		s.close();
		return resultToList(r);
	}

	public DatabaseEntry getById(long dbId) throws SQLException {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE ID = " + dbId + ";");
		s.close();

		if(r.next()) {
			return new DatabaseEntry(r);
		}
		return null;
	}

	public ShortSet getPlayerTakenSectorsLocalCoords(Vector3i start, Vector3i end) throws SQLException {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery("SELECT X, Y, Z FROM ENTITIES WHERE NOT creator = '<system>' AND "
				+ "((X >= " + start.x + " AND Y >= " + start.y + " AND Z >= " + start.z + ") "
				+ "AND (X < " + end.x + " AND Y < " + end.y + " AND Z < " + end.z + "));");

		s.close();
		ShortSet coors = new ShortOpenHashSet();

		while(r.next()) {
			coors.add(NPCSystem.getLocalIndexFromSector(r.getInt(1), r.getInt(2), r.getInt(3)));
		}

		return coors;

	}

	public List<DatabaseEntry> getBySectorRange(Vector3i start, Vector3i end, int[] type) throws SQLException {
		Statement s = c.createStatement();

		//		s.setMaxRows(Math.max(0, max));
		long t = System.currentTimeMillis();
		StringBuilder types = new StringBuilder();
		String typesString = "";
		if(type != null && type.length > 0) {
			types.append("(");
			for(int i = 0; i < type.length; i++) {
				types.append(type[i]);
				if(i < type.length - 1) {
					types.append(",");
				}
			}

			types.append(")");

			typesString = " TYPE IN " + types + " AND ";
		}
		ResultSet r = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE " + typesString + "((X >= " + start.x + " AND Y >= " + start.y + " AND Z >= " + start.z + ") AND (X < " + end.x + " AND Y < " + end.y + " AND Z < " + end.z + "));");
		if((System.currentTimeMillis() - t) > 50) {
			System.err.println("[SQL] SECTOR QUERY TOOK " + (System.currentTimeMillis() - t) + "ms types: " + types + "; from " + start + " to " + end);
		}
		s.close();
		return resultToList(r);
	}

	public List<SimDatabaseEntry> getBySectorRangeSim(Vector3i start, Vector3i end, int[] type, int max) throws SQLException {
		Statement s = c.createStatement();

		//		s.setMaxRows(Math.max(0, max));
		long t = System.currentTimeMillis();
		StringBuilder types = new StringBuilder();
		String typesString = "";
		if(type != null && type.length > 0) {
			types.append("(");
			for(int i = 0; i < type.length; i++) {
				types.append(type[i]);
				if(i < type.length - 1) {
					types.append(",");
				}
			}

			types.append(")");

			typesString = " TYPE IN " + types + " AND ";
		}

		ResultSet r = s.executeQuery("SELECT X, Y, Z, TYPE, FACTION, GEN_ID FROM ENTITIES WHERE " + typesString + "((X >= " + start.x + " AND Y >= " + start.y + " AND Z >= " + start.z + ") AND (X < " + end.x + " AND Y < " + end.y + " AND Z < " + end.z + "));");
		if((System.currentTimeMillis() - t) > 50) {
			System.err.println("[SQL] SECTOR QUERY TOOK " + (System.currentTimeMillis() - t) + "ms types: " + types + "; from " + start + " to " + end);
		}
		s.close();
		return resultToListSim(r);
	}

	public long updateOrInsertSegmentController(SegmentController c) throws SQLException {
		Statement s = this.c.createStatement();
		if(c == null || s == null) {
			throw new NullPointerException(c + ", " + s);
		}
		long id = updateOrInsertSegmentController(s, c);
		s.close();
		if(id >= 0) {
			c.dbId = id;
		}
		return id;
	}

	public long updateOrInsertSegmentController(String uidWithoutPrefix, Vector3i sectorPos,
	                                            int type, long seed, String lastModifier, String spawner,
	                                            String realName, boolean touched, int faction,
	                                            Vector3f pos, Vector3i min, Vector3i max,
	                                            int creatorId, boolean spawnedOnlyInDB, long dockedTo, long dockedRoot, boolean tracked) throws SQLException {
		Statement s = c.createStatement();
		assert (!uidWithoutPrefix.startsWith("ENTITY_")) : uidWithoutPrefix;
		long id = updateOrInsertSegmentController(s, uidWithoutPrefix, sectorPos, type, seed,
				lastModifier, spawner, realName, touched, faction, pos, min, max, creatorId, dockedTo, dockedRoot, spawnedOnlyInDB, tracked);
		s.close();

		return id;
	}

	public long updateOrInsertSegmentController(String uidWithoutPrefix, Vector3i sectorPos,
	                                            int type, long seed, String lastModifier, String spawner,
	                                            String realName, boolean touched, int faction,
	                                            Vector3f pos, Vector3i min, Vector3i max,
	                                            int creatorId, boolean spawnedOnlyInDB, boolean tracked) throws SQLException {
		Statement s = c.createStatement();
		assert (!uidWithoutPrefix.startsWith("ENTITY_")) : uidWithoutPrefix;
		long id = updateOrInsertSegmentController(s, uidWithoutPrefix, sectorPos, type, seed,
				lastModifier, spawner, realName, touched, faction, pos, min, max, creatorId, -1, -1, spawnedOnlyInDB, tracked);
		s.close();

		return id;
	}

	public Vector3i getSectorOfEntityByDBId(long entityDBId) throws SQLException {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery("SELECT X, Y, Z FROM ENTITIES WHERE ID = " + entityDBId + ";");
		s.close();

		if(r.next()) {
			return new Vector3i(r.getInt(1), r.getInt(2), r.getInt(3));
		}
		return null;
	}

	public List<DatabaseEntry> getByUID(String sqlRegExp, int max) throws SQLException {

		//		System.err.println("[SQL] query by uid: "+sqlRegExp);
		Statement s = c.createStatement();
		String limit = "";
		if(max > 0) {
			s.setMaxRows(max);
			limit = "LIMIT " + max;
		}
		ResultSet r = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE UPPER(UID) LIKE UPPER('" + sqlRegExp + "') " + limit + ";");
		s.close();
		return resultToList(r);
	}

	public List<DatabaseEntry> getByName(String sqlRegExp, int max) throws SQLException {
		Statement s = c.createStatement();
		String limit = "";
		if(max > 0) {
			s.setMaxRows(max);
			limit = "LIMIT " + max;
		}
		ResultSet r = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE UPPER(NAME) LIKE UPPER('" + sqlRegExp + "') " + limit + ";");
		s.close();
		return resultToList(r);
	}

	public DatabaseEntry getEntryForFullUID(String uidWithPrefix) {
		Statement s = null;
		try {
			long t = System.currentTimeMillis();
			s = c.createStatement();

			int type = DatabaseEntry.getType(uidWithPrefix);
			String uidWithoutPrefix = DatabaseEntry.removePrefix(uidWithPrefix);

			ResultSet query = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE TYPE = " + type + " AND UID = '" + uidWithoutPrefix + "';");
			List<DatabaseEntry> resultToList = resultToList(query);
			return resultToList.size() > 0 ? resultToList.get(0) : null;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<DatabaseEntry> getByUIDExact(String sqlRegExp, int max) throws SQLException {
		String q = "no query";
		try {
			//		System.err.println("[SQL] query by uid: "+sqlRegExp);
			Statement s = c.createStatement();
			String limit = "";
			if(max > 0) {
				s.setMaxRows(max);
				limit = "LIMIT " + max;
			}
			q = "SELECT " + getFieldsString() + " FROM ENTITIES WHERE UID = '" + sqlRegExp + "' " + limit + ";";
			ResultSet r = s.executeQuery(q);
			s.close();
			return resultToList(r);
		} catch(RuntimeException e) {
			throw new RuntimeException(q, e);
		}
	}

	public long getIdForFullUID(String uidWithPrefix) {
		Statement s = null;
		try {
			long t = System.currentTimeMillis();
			s = c.createStatement();

			int type = DatabaseEntry.getType(uidWithPrefix);
			String uidWithoutPrefix = DatabaseEntry.removePrefix(uidWithPrefix);

			ResultSet query = s.executeQuery("SELECT ID FROM ENTITIES WHERE TYPE = " + type + " AND UID = '" + uidWithoutPrefix + "';");

			if(query.next()) {
				long id = query.getLong(1);
				return id;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public List<EntityUID> loadByDockedEntity(long dockedRoot) {
		Statement s = null;
		try {
			long t = System.currentTimeMillis();
			s = c.createStatement();

			ResultSet query = s.executeQuery("SELECT ID, UID, TYPE, SPAWNED_ONLY_IN_DB, TRACKED FROM ENTITIES WHERE DOCKED_ROOT = " + dockedRoot + ";");
			List<EntityUID> uids = new ObjectArrayList<EntityUID>();

			while(query.next()) {
				long id = query.getLong(1);
				String ent = query.getString(2).trim();
				byte type = query.getByte(3);
				ent = DatabaseEntry.getWithFilePrefix(ent, type);
//				System.err.println("[DATABASE][DOCKLOAD] ADDING DOCKED ENTITY LOADING TO root("+dockedRoot+"): "+ent);
				EntityUID entityUID = new EntityUID(ent, SimpleTransformableSendableObject.EntityType.getByDatabaseId(type), id);
				entityUID.spawnedOnlyInDb = query.getBoolean(4);
				entityUID.tracked = query.getBoolean(5);
				uids.add(entityUID);

			}

			long total = System.currentTimeMillis() - t;

			if(total > 40) {
				System.err.println("[DATABASE] WARNING: Loading sector UIDs: " + total + "ms; entities: " + uids.size());
			}
			Collections.sort(uids);
			return uids;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return new ObjectArrayList();
	}

	public Set<EntityUID> loadSectorEntities(Vector3i sectorPos) {
		Statement s = null;
		try {
			long t = System.currentTimeMillis();
			s = c.createStatement();

			ResultSet query = s.executeQuery("SELECT ID, UID, TYPE, SPAWNED_ONLY_IN_DB, TRACKED, DOCKED_TO, DOCKED_ROOT FROM ENTITIES WHERE X = " + sectorPos.x + " AND Y = " + sectorPos.y + " AND Z = " + sectorPos.z + ";");
			ObjectOpenHashSet<EntityUID> uids = new ObjectOpenHashSet<EntityUID>();
			LongOpenHashSet ids = new LongOpenHashSet();
			while(query.next()) {
				long id = query.getLong(1);
				String ent = query.getString(2).trim();
				byte type = query.getByte(3);
				ent = DatabaseEntry.getWithFilePrefix(ent, type);
				EntityUID entityUID = new EntityUID(ent, SimpleTransformableSendableObject.EntityType.getByDatabaseId(type), id);
				entityUID.spawnedOnlyInDb = query.getBoolean(4);
				entityUID.tracked = query.getBoolean(5);
				entityUID.dockedTo = query.getLong(6);
				entityUID.dockedRoot = query.getLong(7);
				uids.add(entityUID);

				ids.add(id);

			}
			if(uids.size() > 0) {
				System.err.println("[DATABASE] LOADED ENTITIES: " + uids);

				ObjectIterator<EntityUID> it = uids.iterator();
				while(it.hasNext()) {
					EntityUID e = it.next();
					if(e.spawnedOnlyInDb && e.dockedTo > 0) {
						if(!ids.contains(e.dockedTo) || !ids.contains(e.dockedRoot)) {
//							System.err.println("[DATABASE] The parent of the entitiy is not in this sector. Dont load: DockedTo: "+e.dockedTo+"; DockedRoot: "+e.dockedRoot+"; "+e);
//							System.err.println("[DATABASE] LOADED KEYS: "+ids);
							it.remove();
						}
					}
				}

			}
			long total = System.currentTimeMillis() - t;

			if(total > 40) {
				System.err.println("[DATABASE] WARNING: Loading sector UIDs: " + total + "ms; entities: " + uids.size());
			}
			return uids;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return new ObjectOpenHashSet(0);
	}

	public List<DatabaseEntry> getByNameExact(String sqlRegExp, int max) throws SQLException {

		//		System.err.println("[SQL] query by uid: "+sqlRegExp);
		Statement s = c.createStatement();
		String limit = "";
		if(max > 0) {
			s.setMaxRows(max);
			limit = "LIMIT " + max;
		}
		ResultSet r = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE NAME = '" + sqlRegExp + "' " + limit + ";");
		s.close();
		return resultToList(r);
	}

	public int removeAll(String sqlregexp, Despawn mode, Vector3i sectorFilter, boolean shipOnly) throws SQLException {
		Statement s = c.createStatement();
		String unusedCom = switch(mode) {
			case UNUSED -> "AND LAST_MOD = '' ";
			case USED -> "AND NOT LAST_MOD = '' ";
			case ALL -> "";
		};
		if(sectorFilter != null) {
			unusedCom += "AND X = " + sectorFilter.x + " AND Y = " + sectorFilter.y + " AND Z = " + sectorFilter.z + " ";
		}
		if(shipOnly) {
			unusedCom += " AND TYPE = 5 ";
		}
		return s.executeUpdate("DELETE FROM ENTITIES WHERE UID LIKE '" + sqlregexp + "' " + unusedCom + ";");

	}

	public void resetSectorsAndDeleteStations(SystemRange r, int factionId, LogInterface logI, long... except) {
		Statement s = null;
		try {
			s = c.createStatement();
			//			"UPDATE SECTORS SET (X,Y,Z,TYPE,NAME,ITEMS,PROTECTION,STELLAR) = (" +

//			s.executeUpdate("DELETE FROM SECTORS WHERE X >= " + r.start.x + " AND Y >= " + r.start.y + " AND Z >= " + r.start.z + " AND "
//					+ "X < " + r.end.x + " AND Y < " + r.end.y + " AND Z < " + r.end.z + ";");

			List<DatabaseEntry> dbEnt = getBySectorRange(r.start, r.end, new int[]{SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbTypeId});

			for(DatabaseEntry e : dbEnt) {
				boolean exception = false;
				for(long exp : except) {
					if(e.dbId == exp) {
						exception = true;
						break;
					}
				}
				if(!exception) {
					if(logI != null) {
						logI.log("Removing entity and its sector from database: ID " + e.dbId + "; UID: " + e.uid + "; realName: " + e.realName, LogInterface.LogLevel.DEBUG);
					}
					e.destroyAssociatedDatabaseFiles();
					s.executeUpdate("DELETE FROM ENTITIES WHERE ID = " + e.dbId + "; ");
					s.executeUpdate("DELETE FROM SECTORS WHERE X = " + e.sectorPos.x + " AND Y = " + e.sectorPos.y + " AND Z = " + e.sectorPos.z + ";");
				}
			}

			return;

		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void removeSegmentController(long c) throws SQLException {
		Statement s = this.c.createStatement();
		removeSegmentController(s, c);
		TradeNodeTable.removeTradeNode(s, c);
		s.close();
	}

	public void removeSegmentController(SegmentController c) throws SQLException {
		Statement s = this.c.createStatement();
		removeSegmentController(s, c);
		TradeNodeTable.removeTradeNode(s, c.getDbId());
		s.close();
	}

	public boolean removeSegmentController(String fullUID, GameServerState state) throws SQLException {
		Statement s = c.createStatement();
		boolean removed = removeSegmentController(s, fullUID, state);
		s.close();

		return removed;
	}

	public void removeFactionCompletely(NPCFaction npcFaction) {

		Statement s = null;
		try {
			s = c.createStatement();
			//			"UPDATE SECTORS SET (X,Y,Z,TYPE,NAME,ITEMS,PROTECTION,STELLAR) = (" +

			ResultSet res = s.executeQuery("SELECT " + getFieldsString() + " FROM ENTITIES WHERE FACTION = " + npcFaction.getIdFaction() + ";");

			DatabaseEntry e = new DatabaseEntry();
			while(res.next()) {
				e.setFrom(res);
				e.destroyAssociatedDatabaseFiles();
			}

			s.executeUpdate("DELETE FROM ENTITIES WHERE FACTION = " + npcFaction.getIdFaction() + ";");

			s.executeUpdate("UPDATE SYSTEMS SET(OWNER_FACTION, OWNER_UID) = (0, NULL) WHERE OWNER_FACTION = " + npcFaction.getIdFaction() + ";");

		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Deprecated
	public void createEntitiesTable() throws SQLException {
		Statement s = c.createStatement();

		s.execute("DROP TABLE ENTITIES if exists;");
		s.execute("CREATE CACHED TABLE ENTITIES(" +
				"UID VARCHAR(128) not null, " +
				"X INT not null, " +
				"Y INT not null, " +
				"Z INT not null, " +
				"TYPE TINYINT not null, " +
				"NAME char(64), " +
				"FACTION INT default 0, " +
				"CREATOR VARCHAR(64), " +
				"LAST_MOD VARCHAR(64), " +
				"SEED BIGINT, " +
				"TOUCHED BOOLEAN, " +
				"LOCAL_POS FLOAT ARRAY[3], " +
				"DIM INT ARRAY[6], " +
				"GEN_ID INT, " +
				"DOCKED_TO BIGINT DEFAULT -1, " +
				"DOCKED_ROOT BIGINT DEFAULT -1, " +
				"SPAWNED_ONLY_IN_DB BOOLEAN DEFAULT FALSE, " +
				"BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1), " +
				"primary key (ID)" +
				");");

		s.execute("create index ENTITIES_PK on ENTITIES (UID);");
		s.execute("create unique index uidType on ENTITIES(UID,TYPE);");
		s.execute("create index coordIndex on ENTITIES(X,Y,Z);");
		s.execute("create index typeIndex on ENTITIES(TYPE);");
		s.execute("create index dockedToIndex on ENTITIES(DOCKED_TO);");
		s.execute("create index dockedRootIndex on ENTITIES(DOCKED_ROOT);");
		s.close();
	}

	public enum Despawn {
		UNUSED,
		USED,
		ALL
	}
}
