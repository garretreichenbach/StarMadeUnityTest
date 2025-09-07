package org.schema.game.common.controller.database.tables;

import java.sql.*;

public class NPCStatTable extends Table {
	public NPCStatTable(TableManager m, Connection c) {
		super("NPC_STATS", m, c);
	}

	@Override
	public void define() {

		addColumn("ID", "INT", true);
		addColumn("SYS_X", "INT not null");
		addColumn("SYS_Y", "INT not null");
		addColumn("SYS_Z", "INT not null");
		addColumn("FLEET_SPAWNS", "INT DEFAULT 0");
		addColumn("ENTITY_SPAWNS", "INT DEFAULT 0");

		setPrimaryKey("ID", "SYS_X", "SYS_Y", "SYS_Z");

	}

	public void updateNPCSpawns(int factionID, int XSys, int YSys,
	                            int ZSys, int shipCreatorNumber, int fleetCreatorNumber) throws SQLException {
		Statement s = c.createStatement();
		PreparedStatement p = c.prepareStatement(
				"UPDATE NPC_STATS SET(ENTITY_SPAWNS,FLEET_SPAWNS) = (" +
						"CAST(? AS INT)," +
						"CAST(? AS INT)" +
						") WHERE "
						+ "ID = CAST(? AS INT) AND "
						+ "SYS_X = CAST(? AS INT) AND "
						+ "SYS_Y = CAST(? AS INT) AND "
						+ "SYS_Z = CAST(? AS INT);");
		p.setInt(1, shipCreatorNumber);
		p.setInt(2, fleetCreatorNumber);

		p.setInt(3, factionID);
		p.setInt(4, XSys);
		p.setInt(5, YSys);
		p.setInt(6, ZSys);

		p.executeUpdate();

		p.close();

		s.close();
	}

	public int[] getNPCFleetAndEntitySpawns(int factionID, int XSys, int YSys, int ZSys) throws SQLException {
		Statement s = c.createStatement();
		try {
			ResultSet query = s.executeQuery("SELECT FLEET_SPAWNS,ENTITY_SPAWNS FROM NPC_STATS WHERE "
					+ "ID = " + factionID + " AND SYS_X = " + XSys + " AND SYS_Y = " + YSys + " AND SYS_Z = " + ZSys + ";");
			if(query.next()) {
				return new int[]{query.getInt(1), query.getInt(2)};
			}
		} finally {
			s.close();
		}
		return new int[]{0, 0};
	}

	public void insertNPCSpawns(int factionID, int XSys, int YSys,
	                            int ZSys, int shipCreatorNumber, int fleetCreatorNumber) throws SQLException {

		Statement s = c.createStatement();
		ResultSet query = s.executeQuery("SELECT FLEET_SPAWNS,ENTITY_SPAWNS FROM NPC_STATS WHERE "
				+ "ID = " + factionID + "AND SYS_X = " + XSys + " AND SYS_Y = " + YSys + " AND SYS_Z = " + ZSys + ";");
		if(!query.next()) {
			PreparedStatement p = c.prepareStatement(
					"INSERT INTO NPC_STATS(ID, SYS_X,SYS_Y,SYS_Z,FLEET_SPAWNS,ENTITY_SPAWNS) VALUES(" +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)" +
							");");
			p.setInt(1, factionID);
			p.setInt(2, XSys);
			p.setInt(3, YSys);
			p.setInt(4, ZSys);
			p.setInt(5, shipCreatorNumber);
			p.setInt(6, fleetCreatorNumber);

			p.executeUpdate();

			p.close();
		}

		s.close();
	}

	@Override
	public void afterCreation(Statement s) {

	}
}
