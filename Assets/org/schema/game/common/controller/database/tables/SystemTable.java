package org.schema.game.common.controller.database.tables;

import api.common.GameCommon;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.database.SystemInDatabase;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.GalaxyRequestAndAwnser;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.geo.StarSystemResourceRequestContainer;
import org.schema.schine.resource.tag.Tag;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collection;
import java.util.List;

public class SystemTable extends Table {

	public SystemTable(TableManager m, Connection c) {
		super("SYSTEMS", m, c);
	}

	private static String getCelestialsPath(Vector3i pos) {
		String path = Paths.get("").toAbsolutePath() + "\\celestial-object-test-cache\\SERVER-" + GameCommon.getUniqueContextId().replace(':', '~') + "\\SYS_" + pos.x + '_' + pos.y + "_" + pos.z + ".tag";
		return path;
	}

	public static long updateOrInsertSystem(Connection c, long id, Vector3i pos, String name, int type, byte[] infos, long startStime, boolean forceUpdate, String ownerUID, int ownerFaction, Vector3i ownerPos, byte[] systemResources) throws SQLException, IOException {

		Statement s = c.createStatement();
		s.setFetchSize(1);
		ResultSet query = s.executeQuery("SELECT ID FROM SYSTEMS WHERE X = " + pos.x + " AND Y = " + pos.y + " AND Z = " + pos.z + ";");

		if(query.next()) {

			//no write needed
			if(forceUpdate) {
				System.err.println("[DB] FORCE UPDATING SYSTEM " + pos);
				PreparedStatement p = c.prepareStatement(
						"UPDATE SYSTEMS SET (X,Y,Z,TYPE,NAME,STARTTIME,INFOS, OWNER_UID, OWNER_FACTION, OWNER_X, OWNER_Y, OWNER_Z, RESOURCES) = (" +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS VARCHAR(64))," +
								"CAST(? AS BIGINT)," +
								"CAST(? AS VARBINARY(" + VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * StellarSystem.DATA_SIZE + "))," +
								"CAST(? AS VARCHAR(128))," +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS VARBINARY(" + VoidSystem.RESOURCES + "))" +
								") WHERE ID = " + "CAST(? AS BIGINT)" + ";");
				p.setInt(1, pos.x);
				p.setInt(2, pos.y);
				p.setInt(3, pos.z);
				p.setInt(4, type);
				p.setString(5, "default");
				p.setLong(6, startStime);
				p.setBytes(7, infos);
				if(ownerUID != null) {
					System.err.println("SYSTEM OWNERSHIP: " + ownerUID);
					p.setString(8, ownerUID);
				} else {
					p.setNull(8, Types.VARCHAR);
				}
				p.setInt(9, ownerFaction);
				p.setInt(10, ownerPos.x);
				p.setInt(11, ownerPos.y);
				p.setInt(12, ownerPos.z);
				p.setBytes(13, systemResources);

				//WHERE ID =
				p.setLong(14, id);

				p.execute();

				p.close();
			}

		} else {
			System.err.println("[DATABASE][SYSTEMS] NO SYSTEM ENTRY FOUND FOR " + id + " FOR " + pos);
			//			assert(!checkDuplicateSystem(s,pos,id));

			PreparedStatement p = c.prepareStatement(
					"INSERT INTO SYSTEMS(X,Y,Z,TYPE,NAME,STARTTIME,INFOS, OWNER_UID, OWNER_FACTION, OWNER_X, OWNER_Y, OWNER_Z, RESOURCES) VALUES(" +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS VARCHAR(64))," +
							"CAST(? AS BIGINT)," +
							"CAST(? AS VARBINARY(" + VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * StellarSystem.DATA_SIZE + "))," +
							"CAST(? AS VARCHAR(128))," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS VARBINARY(" + VoidSystem.RESOURCES + "))" +
							");", Statement.RETURN_GENERATED_KEYS);
			p.setInt(1, pos.x);
			p.setInt(2, pos.y);
			p.setInt(3, pos.z);
			p.setInt(4, type);
			p.setString(5, "default");
			p.setLong(6, startStime);
			p.setBytes(7, infos);
			p.setString(8, ownerUID);
			p.setInt(9, ownerFaction);
			p.setInt(10, ownerPos.x);
			p.setInt(11, ownerPos.y);
			p.setInt(12, ownerPos.z);
			p.setBytes(13, systemResources);

			try {
				p.executeUpdate();
				System.err.println("[DATABASE][SYSTEMS] CREATED NEW SYSTEM ENTRY " + id + " FOR " + pos);
			} catch(SQLIntegrityConstraintViolationException ex) {
				if(ex.getMessage().contains("SYSCOORDINDEX")) {
					ResultSet doublecheck = s.executeQuery("SELECT * FROM SYSTEMS WHERE X = " + pos.x + " AND Y = " + pos.y + " AND Z = " + pos.z + ";");
					if(doublecheck.next()) {
						System.err.println("SQL CONSTRAINT VIOLATION: Row already exists for unique index " + doublecheck.getInt(1) + ", " + doublecheck.getInt(2) + ", " + doublecheck.getInt(3) + "...");
						ex.printStackTrace();
					}
					System.err.println("Welp. The row wasn't there, and now it is after attempting to execute this query. Is this an obscure Java SQL bug, or...???? ~Ithirahad");
				} else throw ex;
			}

			ResultSet generatedKeys = p.getGeneratedKeys();
			generatedKeys.next();
			id = generatedKeys.getLong(1);

			p.close();
		}
		s.close();

		return id;
	}

	public static void updateFromFileStarSystem(File f, Statement s) throws IOException, SQLException {

		if(f.getName().startsWith("VOIDSYSTEM")) {
			DataInputStream is = new DataInputStream(new FileInputStream(f));
			Tag tag = Tag.readFrom(is, true, false);
			is.close();

			Vector3i pos;
			byte[] infos;
			long simStart;

			Tag[] root = (Tag[]) tag.getValue();

			pos = ((Vector3i) root[0].getValue());

			infos = (byte[]) (root[1].getValue());

			if(root[2].getType() == Tag.Type.LONG) {
				simStart = (Long) root[2].getValue();
			} else {
				simStart = 0;
			}

			int type = 0;
			String name = "default"; //TODO save names

			updateOrInsertSystem(s.getConnection(), 0, pos, name, type, infos, simStart, false, null, 0, new Vector3i(), new byte[VoidSystem.RESOURCES]);
			System.err.println("[SQL] INSERTED SYSTEM " + pos);
		}
	}

	@Override
	public void define() {
		addColumn("ID", "BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1)", true);
		addColumn("X", "INT NOT NULL");
		addColumn("Y", "INT NOT NULL");
		addColumn("Z", "INT NOT NULL");
		addColumn("TYPE", "INT NOT NULL");
		addColumn("STARTTIME", "BIGINT");
		addColumn("NAME", "VARCHAR(64)");
		addColumn("INFOS", "VARBINARY(" + VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * StellarSystem.DATA_SIZE + ") not null");
		addColumn("OWNER_UID", "VARCHAR(128)");
		addColumn("OWNER_FACTION", "INT DEFAULT 0 not null");
		addColumn("OWNER_X", "INT DEFAULT 0 not null");
		addColumn("OWNER_Y", "INT DEFAULT 0 not null");
		addColumn("OWNER_Z", "INT DEFAULT 0 not null");
		addColumn("RESOURCES", "VARBINARY(" + VoidSystem.RESOURCES + ") not null");

		addIndex("sysCoordIndex", true, "X", "Y", "Z");
		addIndex("sysOwnFacIndex", "OWNER_FACTION");
		addIndex("sysOwnUIDIndex", "OWNER_UID");
	}

	@Override
	public void afterCreation(Statement s) {

	}

	public long updateOrInsertSystemIfChanged(StellarSystem ss, boolean forceUpdate) throws SQLException, IOException {
		return updateOrInsertSystem(c, ss.getDBId(), ss.getPos(), ss.getName(), ss.getCenterSectorType().ordinal(), ss.getInfos(), ss.getSimulationStart(), forceUpdate, ss.getOwnerUID(), ss.getOwnerFaction(), ss.getOwnerPos(), ss.systemResources.res);
	}

	public boolean updateSystemResources(Vector3i systemPos, StarSystemResourceRequestContainer r) {
		Statement s;
		try {
			s = c.createStatement();

			ResultSet queryId = s.executeQuery("SELECT RESOURCES, OWNER_FACTION FROM SYSTEMS WHERE X = " + systemPos.x + " AND Y = " + systemPos.y + " AND Z = " + systemPos.z + ";");

			if(queryId.next()) {
				int factionId = queryId.getInt(2);
				r.factionId = factionId;

				if(queryId.getBytes(1) == null) {
					s.close();
					return false;
				}
				System.arraycopy(queryId.getBytes(1), 0, r.res, 0, r.res.length);
				s.close();
				return true;
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void setSystemOwnership(Vector3i system, int factionId,
	                               String ownerUID, int secX, int secY, int secZ) throws SQLException {
		PreparedStatement p = c.prepareStatement(
				"UPDATE SYSTEMS SET (OWNER_UID, OWNER_FACTION, OWNER_X, OWNER_Y, OWNER_Z) = (" +
						"CAST(? AS VARCHAR(128))," +
						"CAST(? AS INT)," +
						"CAST(? AS INT)," +
						"CAST(? AS INT)," +
						"CAST(? AS INT)" +
						") WHERE X = CAST(? AS INT) AND Y = CAST(? AS INT) AND Z = CAST(? AS INT);");
		if(ownerUID != null && ownerUID.length() > 0) {
			p.setString(1, ownerUID);
		} else {
			p.setNull(1, Types.VARCHAR);
		}
		p.setInt(2, factionId);
		p.setInt(3, secX);
		p.setInt(4, secY);
		p.setInt(5, secZ);
		p.setInt(6, system.x);
		p.setInt(7, system.y);
		p.setInt(8, system.z);

		p.execute();

		p.close();

	}

	public List<Vector3i> getSystemsByFaction(int factionId, List<Vector3i> systems, List<Vector3i> sectors, List<String> uids, List<String> sysnames) {
		Statement s;
		try {
			s = c.createStatement();

			ResultSet queryId = s.executeQuery("SELECT X,Y,Z, OWNER_X, OWNER_Y, OWNER_Z, OWNER_UID, NAME FROM SYSTEMS WHERE OWNER_FACTION = " + factionId + ";");

			while(queryId.next()) {
				systems.add(new Vector3i(queryId.getInt(1), queryId.getInt(2), queryId.getInt(3)));
				sectors.add(new Vector3i(queryId.getInt(4), queryId.getInt(5), queryId.getInt(6)));
				uids.add(queryId.getString(7));
				sysnames.add(queryId.getString(8));

			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return systems;
	}

	public boolean loadSystem(GameServerState state, Vector3i systemPos, VoidSystem ssys) {

		Statement s;
		try {
			s = c.createStatement();

			ResultSet queryId = s.executeQuery("SELECT ID FROM SYSTEMS WHERE X = " + systemPos.x + " AND Y = " + systemPos.y + " AND Z = " + systemPos.z + ";");

			if(queryId.next()) {
				int id = queryId.getInt(1);
				ResultSet query = s.executeQuery("SELECT TYPE, STARTTIME, NAME, INFOS, OWNER_UID, OWNER_FACTION, OWNER_X, OWNER_Y, OWNER_Z, RESOURCES FROM SYSTEMS WHERE ID = " + id + ";");
				query.next();

				ssys.setDBId(id);
				ssys.getPos().set(systemPos);
				ssys.setSimulationStart(query.getLong(2));
				String name = query.getString(3);
				ssys.loadInfos(state.getUniverse().getGalaxy(Galaxy.getContainingGalaxyFromSystemPos(ssys.getPos(), new Vector3i())), query.getBytes(4));

				ssys.setOwnerUID(query.getString(5));
				ssys.setOwnerFaction(query.getInt(6));
				ssys.getOwnerPos().set(query.getInt(7), query.getInt(8), query.getInt(9));

				ssys.setSystemResources(query.getBytes(10));
				s.close();
				return true;
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void migrateSystemsResourcesField() throws SQLException {
		{
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT * " + //character_maximum_length
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'SYSTEMS' " +
					"AND COLUMN_NAME = 'RESOURCES'");
			if(r.next()) {
				int len = r.getInt("CHARACTER_OCTET_LENGTH");
				if(len != VoidSystem.RESOURCES) { //probably wrong width
					System.err.println("[DB][WARNING] Starsystems Table Resource Length (" + len + ") in database did not match resource count (" + VoidSystem.RESOURCES + ").");
					if(len > VoidSystem.RESOURCES) {
						s.executeUpdate("UPDATE SYSTEMS SET RESOURCES = LEFT(RESOURCES, " + VoidSystem.RESOURCES + ")");
						System.err.println("[DB][WARNING] Existing resource info had to be truncated.");
					}
					s.executeUpdate("ALTER TABLE 'SYSTEMS' ALTER COLUMN RESOURCES VARBINARY(" + VoidSystem.RESOURCES + ");");
					System.err.println("[DB] Altered DB resource info size successfully.");
				}
			} else {
				s.executeUpdate("ALTER TABLE 'SYSTEMS' ADD COLUMN RESOURCES VARBINARY(" + VoidSystem.RESOURCES + ");");
				System.err.println("[DB][WARNING] Starsystems Table in database had no resource column. Table has been migrated.");
			}
			s.close();
		}

	}

	public List<Vector3i> getSystemsByFaction(int factionId, List<Vector3i> systems, List<Vector3i> sectors, List<String> UIDs) {
		Statement s;
		try {
			s = c.createStatement();

			ResultSet queryId = s.executeQuery("SELECT X,Y,Z, OWNER_X, OWNER_Y, OWNER_Z, OWNER_UID FROM SYSTEMS WHERE OWNER_FACTION = " + factionId + ";");

			while(queryId.next()) {
				systems.add(new Vector3i(queryId.getInt(1), queryId.getInt(2), queryId.getInt(3)));
				sectors.add(new Vector3i(queryId.getInt(4), queryId.getInt(5), queryId.getInt(6)));
				UIDs.add(queryId.getString(7));
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return systems;
	}

	public List<Vector3i> getSystemsByFaction(int factionId, List<Vector3i> systems) {
		Statement s;
		try {
			s = c.createStatement();

			ResultSet queryId = s.executeQuery("SELECT X,Y,Z FROM SYSTEMS WHERE OWNER_FACTION = " + factionId + ";");

			while(queryId.next()) {
				systems.add(new Vector3i(queryId.getInt(1), queryId.getInt(2), queryId.getInt(3)));
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return systems;
	}

	public int getOwnedSystemCount(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
		int total = 0;
		Statement s;
		try {
			s = c.createStatement();
			ResultSet rCount = s.executeQuery("SELECT COUNT(*) FROM SYSTEMS WHERE NOT OWNER_FACTION = 0 AND X >= " + fromX + " AND X <= " + toX + " AND Y >= " + fromY + " AND Y <= " + toY + " AND Z >= " + fromZ + " AND Z <= " + toZ);
			rCount.next();
			total = rCount.getInt(1);
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return total;
	}

	public boolean loadSystemZone(Vector3i startSystem, int zoneSize,
	                              List<GalaxyRequestAndAwnser> buffer) {

		int fromX = startSystem.x;
		int fromY = startSystem.y;
		int fromZ = startSystem.z;
		int toX = startSystem.x + zoneSize;
		int toY = startSystem.y + zoneSize;
		int toZ = startSystem.z + zoneSize;

//		System.err.println("ZONE REQUEST FROM ["+fromX+", "+(toX-1)+"], ["+fromY+", "+(toY-1)+"], ["+fromZ+", "+(toZ-1)+"]");

		Statement s;
		try {
			s = c.createStatement();
			ResultSet rCount = s.executeQuery("SELECT OWNER_UID,OWNER_FACTION,X,Y,Z,OWNER_X, OWNER_Y, OWNER_Z FROM SYSTEMS WHERE X >= " + fromX + " AND X < " + toX + " AND Y >= " + fromY + " AND Y < " + toY + " AND Z >= " + fromZ + " AND Z < " + toZ);

			while(rCount.next()) {
				GalaxyRequestAndAwnser c = new GalaxyRequestAndAwnser();

				c.ownerUID = rCount.getString(1);
				c.factionUID = rCount.getInt(2);

				//find a valid sector
				c.secX = rCount.getInt(3) * VoidSystem.SYSTEM_SIZE + 1;
				c.secY = rCount.getInt(4) * VoidSystem.SYSTEM_SIZE + 1;
				c.secZ = rCount.getInt(5) * VoidSystem.SYSTEM_SIZE + 1;

				if(c.ownerUID != null && c.factionUID != 0) {
					c.secX = rCount.getInt(6);
					c.secY = rCount.getInt(7);
					c.secZ = rCount.getInt(8);
				}

//				System.err.println("ZONE REQUEST:::: "+c);
				buffer.add(c);
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public SystemInDatabase getSystem(Vector3i sys) {
		Statement s;
		try {
			s = c.createStatement();
			ResultSet rCount = s.executeQuery("SELECT OWNER_UID,OWNER_FACTION,X,Y,Z,OWNER_X, OWNER_Y, OWNER_Z FROM SYSTEMS WHERE X = " + sys.x + " AND Y = " + sys.y + " AND Z = " + sys.z + ";");

			while(rCount.next()) {
				SystemInDatabase c = new SystemInDatabase();

				c.ownerUID = rCount.getString(1);
				c.factionUID = rCount.getInt(2);

				c.sysX = rCount.getInt(3);
				c.sysY = rCount.getInt(4);
				c.sysZ = rCount.getInt(5);

				if(c.ownerUID != null && c.factionUID != 0) {
					c.secX = rCount.getInt(6);
					c.secY = rCount.getInt(7);
					c.secZ = rCount.getInt(8);
				}

				return c;
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean getSystems(Vector3i startSystem, int zoneSize,
	                          Collection<SystemInDatabase> buffer) {

		int fromX = startSystem.x - zoneSize;
		int fromY = startSystem.y - zoneSize;
		int fromZ = startSystem.z - zoneSize;
		int toX = startSystem.x + zoneSize;
		int toY = startSystem.y + zoneSize;
		int toZ = startSystem.z + zoneSize;

//		System.err.println("ZONE REQUEST FROM ["+fromX+", "+(toX-1)+"], ["+fromY+", "+(toY-1)+"], ["+fromZ+", "+(toZ-1)+"]");

		Statement s;
		try {
			s = c.createStatement();
			ResultSet rCount = s.executeQuery("SELECT OWNER_UID,OWNER_FACTION,X,Y,Z,OWNER_X, OWNER_Y, OWNER_Z FROM SYSTEMS WHERE X >= " + fromX + " AND X < " + toX + " AND Y >= " + fromY + " AND Y < " + toY + " AND Z >= " + fromZ + " AND Z < " + toZ);

			while(rCount.next()) {
				SystemInDatabase c = new SystemInDatabase();

				c.ownerUID = rCount.getString(1);
				c.factionUID = rCount.getInt(2);

				c.sysX = rCount.getInt(3);
				c.sysY = rCount.getInt(4);
				c.sysZ = rCount.getInt(5);

				if(c.ownerUID != null && c.factionUID != 0) {
					c.secX = rCount.getInt(6);
					c.secY = rCount.getInt(7);
					c.secZ = rCount.getInt(8);
				}

//				System.err.println("ZONE REQUEST:::: "+c);
				buffer.add(c);
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Deprecated
	public void createSystemsTable() throws SQLException {
		{
			Statement s = c.createStatement();

			s.execute("DROP TABLE SYSTEMS if exists;");
			s.execute("CREATE CACHED TABLE SYSTEMS(" +
					"BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1), " +
					"X INT not null, " +
					"Y INT not null, " +
					"Z INT not null, " +
					"TYPE INT not null, " +
					"STARTTIME BIGINT not null, " +
					"NAME VARCHAR(64) not null, " +
					"INFOS VARBINARY(" + VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * VoidSystem.SYSTEM_SIZE * StellarSystem.DATA_SIZE + ") not null, " +
					"OWNER_UID VARCHAR(128)," +
					"OWNER_FACTION INT DEFAULT 0 not null," +
					"OWNER_X INT DEFAULT 0 not null," +
					"OWNER_Y INT DEFAULT 0 not null," +
					"OWNER_Z INT DEFAULT 0 not null," +
					"RESOURCES VARBINARY(" + VoidSystem.RESOURCES + ") not null," +

					"primary key (ID)" +
					");");

			s.execute("create unique index sysCoordIndex on SYSTEMS(X,Y,Z);");
			s.execute("create index sysOwnFacIndex on SYSTEMS(OWNER_FACTION);");
			s.execute("create index sysOwnUIDIndex on SYSTEMS(OWNER_UID);");
			s.close();
		}
	}
}
