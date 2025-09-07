package org.schema.game.common.controller.database.tables;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.schema.game.common.controller.database.FogOfWarReceiver;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.server.data.Galaxy;

import java.sql.*;

public class VisibilityTable extends Table {

	public VisibilityTable(TableManager m, Connection c) {
		super("VISIBILITY", m, c);
	}

	@Override
	public void define() {
		addColumn("ID", "BIGINT");
		addColumn("X", "INT NOT NULL");
		addColumn("Y", "INT NOT NULL");
		addColumn("Z", "INT NOT NULL");
		addColumn("TIMESTAMP", "BIGINT");
		setPrimaryKey("ID", "X", "Y", "Z");

		addIndex("vissetfull", true, "ID", "X", "Y", "Z");
		addIndex("vists", "TIMESTAMP");
		addIndex("vispos", "X", "Y", "Z");
	}

	public void updateVisibileSystem(FogOfWarReceiver fogee, int x, int y, int z, long timestamp) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			PreparedStatement p = s.getConnection().prepareStatement(
					"MERGE INTO VISIBILITY AS t USING (VALUES(" +
							"CAST(? AS BIGINT)," +
							"CAST(? AS INT), " +
							"CAST(? AS INT), " +
							"CAST(? AS INT), " +
							"CAST(? AS BIGINT) " +
							")) "
							+ "AS vals(ID, X, Y, Z, TIMESTAMP) "
							+ "ON t.ID = vals.ID AND t.X = vals.X AND t.Y = vals.Y AND t.Z = vals.Z "
							+ "WHEN MATCHED THEN UPDATE SET t.TIMESTAMP = vals.TIMESTAMP "
							+ "WHEN NOT MATCHED THEN INSERT VALUES vals.ID, vals.X, vals.Y, vals.Z, vals.TIMESTAMP "
							+ ";");
			p.setLong(1, fogee.getFogOfWarId());
			p.setInt(2, x);
			p.setInt(3, y);
			p.setInt(4, z);
			p.setLong(5, timestamp);
			p.executeUpdate();
			p.close();

		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	public void fogOfWarCheck(String playerUID, int factionId, long permission) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			ResultSet q = s.executeQuery("SELECT ID, FACTION, PERMISSION FROM PLAYERS WHERE NAME = '" + playerUID + "';");

			if(q.next()) {
				long playerId = q.getInt(1);
				int oldFaction = q.getInt(2);
				long oldPermission = q.getLong(3);

				if(oldFaction != factionId ||
						((oldPermission & FactionPermission.PermType.FOG_OF_WAR_SHARE.value) != FactionPermission.PermType.FOG_OF_WAR_SHARE.value
								&&
								((permission & FactionPermission.PermType.FOG_OF_WAR_SHARE.value) == FactionPermission.PermType.FOG_OF_WAR_SHARE.value)
						)) {
					mergeVisibility(playerId, factionId);
				}
			}
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	public void clearVisibility(long id) throws SQLException {
		Statement s = null;
		try {
			System.err.println("[DATABASE] clearing visibility of ID: " + id);
			s = c.createStatement();
			PreparedStatement p = s.getConnection().prepareStatement(
					"DELETE FROM VISIBILITY WHERE ID = CAST(? AS BIGINT)" +
							";");
			p.setLong(1, id);
			p.executeUpdate();
			p.close();

		} finally {
			if(s != null) {
				try {
					s.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void mergeVisibility(FogOfWarReceiver from, FogOfWarReceiver to) throws SQLException {
		mergeVisibility(from.getFogOfWarId(), to.getFogOfWarId());
	}

	public void mergeVisibility(long from, long to) throws SQLException {
		System.err.println("[SERVER][FOW] MERGING VIS: " + from + " INTO " + to);
		Statement s = null;
		try {
			s = c.createStatement();

			PreparedStatement p = s.getConnection().prepareStatement(
					"MERGE INTO VISIBILITY AS t USING (SELECT ID, X, Y, Z, TIMESTAMP FROM VISIBILITY WHERE ID = CAST(? AS BIGINT)) " +
							"AS vals(ID, X, Y, Z, TIMESTAMP) " +
							"ON t.ID = CAST(? AS BIGINT) AND t.X = vals.X AND t.Y = vals.Y AND t.Z = vals.Z " +
							"WHEN MATCHED THEN UPDATE SET t.TIMESTAMP = vals.TIMESTAMP " +
							"WHEN NOT MATCHED THEN INSERT VALUES CAST(? AS BIGINT), vals.X, vals.Y, vals.Z, vals.TIMESTAMP " +
							";");
			p.setLong(1, from);
			p.setLong(2, to);
			p.setLong(3, to);
			p.executeUpdate();
			p.close();

		} finally {
			if(s != null) {
				try {
					s.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void isVisibileSystemsInGalaxy(FogOfWarReceiver fogee, Galaxy g, IntArrayList result) throws SQLException {
		boolean inTable = false;
		Statement s = null;

		int minX = g.galaxyPos.x * Galaxy.size;
		int minY = g.galaxyPos.y * Galaxy.size;
		int minZ = g.galaxyPos.z * Galaxy.size;

		int maxX = g.galaxyPos.x * Galaxy.size + Galaxy.size;
		int maxY = g.galaxyPos.y * Galaxy.size + Galaxy.size;
		int maxZ = g.galaxyPos.z * Galaxy.size + Galaxy.size;

		isVisibileSystemsInGalaxy(fogee, result, minX, minY, minZ, maxX, maxY, maxZ);
	}

	public void isVisibileSystemsInGalaxy(FogOfWarReceiver fogee, IntArrayList result, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) throws SQLException {
		Statement s = null;

		try {
			s = c.createStatement();
			ResultSet q = s.executeQuery("SELECT "
					+ "X, Y, Z "
					+ "FROM VISIBILITY WHERE "
					+ "ID = " + fogee.getFogOfWarId() + " AND "
					+ "X >= " + minX + " AND "
					+ "Y >= " + minY + " AND "
					+ "Z >= " + minZ + " AND "
					+ "X < " + maxX + " AND "
					+ "Y < " + maxY + " AND "
					+ "Z < " + maxZ
					+ ";");

			while(q.next()) {
				result.add(q.getInt(1));
				result.add(q.getInt(2));
				result.add(q.getInt(3));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	@Override
	public void afterCreation(Statement s) {

	}

	@Deprecated
	public void createVisibilityTable() throws SQLException {
		{
			Statement s = c.createStatement();

			s.execute("DROP TABLE VISIBILITY if exists;");
			s.execute("CREATE CACHED TABLE VISIBILITY(" +
					"BIGINT, " +
					"X INT not null, " +
					"Y INT not null, " +
					"Z INT not null, " +
					"TIMESTAMP BIGINT not null, " +
					"primary key (ID,X,Y,Z)" +
					");");
			s.execute("create index vispos on VISIBILITY(X,Y,Z);");
			s.execute("create index vists on VISIBILITY(TIMESTAMP);");
			s.execute("create unique index vissetfull on VISIBILITY(ID, X,Y,Z);");

			s.close();
		}

	}
}
