package org.schema.game.common.controller.database.tables;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.data.GameServerState;

import java.sql.*;

public class MinesTable extends Table {
	public MinesTable(TableManager m, Connection c) {
		super("MINES", m, c);
	}

	@Override
	public void define() {

		addColumn("ID", "INT not null", true);
		addColumn("OWNER", "BIGINT not null");
		addColumn("FACTION", "INT DEFAULT 0 not null");
		addColumn("HP", "SMALLINT not null");
		addColumn("COMPOSITION", "SMALLINT ARRAY[6] not null");
		addColumn("SECTOR_X", "INT not null");
		addColumn("SECTOR_Y", "INT not null");
		addColumn("SECTOR_Z", "INT not null");
		addColumn("LOCAL_X", "FLOAT not null");
		addColumn("LOCAL_Y", "FLOAT not null");
		addColumn("LOCAL_Z", "FLOAT not null");
		addColumn("CREATION_DATE", "BIGINT not null");
		addColumn("ARMED", "BOOLEAN DEFAULT false not null");
		addColumn("ARMED_IN_SECS", "INT DEFAULT -1 not null");
		addColumn("AMMO", "SMALLINT DEFAULT -2 not null");

		addIndex("IN_OWNER", "OWNER");
		addIndex("IN_FACTION", "FACTION");
		addIndex("IN_SECTOR", "SECTOR_X", "SECTOR_Y", "SECTOR_Z");
		addIndex("IN_CREATION", "CREATION_DATE");
		addIndex("IN_ARMED", "ARMED");
		addIndex("IN_ARMED_OWNER", "OWNER", "ARMED");
		addIndex("IN_ARMED_OWNER_SEC", "OWNER", "SECTOR_X", "SECTOR_Y", "SECTOR_Z", "ARMED");
	}

	public void updateOrInsertMine(Mine mine) throws SQLException {
		if(mine.isInDatabase() && !mine.isChanged()) {
			return;
		}
		Statement s = null;
		try {
			s = c.createStatement();
			if(!mine.isInDatabase()) {
				System.err.println("ADDING MINE TO DATABASE: " + mine.getId() + ": " + mine);
				PreparedStatement p = s.getConnection().prepareStatement(
						"INSERT INTO " + table + "(ID, OWNER, FACTION, HP, COMPOSITION, SECTOR_X, SECTOR_Y, SECTOR_Z, LOCAL_X, LOCAL_Y, LOCAL_Z, CREATION_DATE, ARMED, ARMED_IN_SECS, AMMO) VALUES(" +
								"CAST(? AS INT)," +
								"CAST(? AS BIGINT)," +
								"CAST(? AS INT)," +
								"CAST(? AS SMALLINT)," +
								"CAST(? AS SMALLINT ARRAY[6])," +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS INT)," +
								"CAST(? AS FLOAT)," +
								"CAST(? AS FLOAT)," +
								"CAST(? AS FLOAT)," +
								"CAST(? AS BIGINT)," +
								"CAST(? AS BOOLEAN)," +
								"CAST(? AS INT)," +
								"CAST(? AS SMALLINT)" +
								");");
				p.setInt(1, mine.getId());
				p.setLong(2, mine.getOwnerId());
				p.setInt(3, mine.getFactionId());
				p.setShort(4, mine.getHp());
				Short[] a = new Short[mine.getComposition().length];
				for(int i = 0; i < a.length; i++) {
					a[i] = mine.getComposition()[i];
				}
				p.setArray(5, s.getConnection().createArrayOf("SMALLINT", a));
				p.setInt(6, mine.getSectorPos().x);
				p.setInt(7, mine.getSectorPos().y);
				p.setInt(8, mine.getSectorPos().z);
				p.setFloat(9, mine.getWorldTransform().origin.x);
				p.setFloat(10, mine.getWorldTransform().origin.y);
				p.setFloat(11, mine.getWorldTransform().origin.z);
				p.setLong(12, mine.getTimeCreated());
				p.setBoolean(13, mine.isArmed());
				p.setInt(14, mine.serverInfo.armInSecs);
				p.setInt(15, mine.getAmmo());
				p.executeUpdate();

				mine.setInDatabase(true);
				p.close();
			} else {

				PreparedStatement p = s.getConnection().prepareStatement(
						"UPDATE " + table + " SET(HP, ARMED, AMMO) = (CAST(? AS SMALLINT), CAST(? AS BOOLEAN), CAST(? AS SMALLINT) ) WHERE ID = CAST(? AS BIGINT);");

				p.setShort(1, mine.getHp());
				p.setBoolean(2, mine.isArmed());
				p.setInt(3, mine.getAmmo());
				p.setInt(4, mine.getId());

				p.executeUpdate();
				p.close();

			}
			mine.setChanged(false);
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	public void deleteMine(int mineId) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			s.executeUpdate("DELETE FROM " + table + " WHERE ID = " + mineId + ";");
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	public void loadMines(GameServerState state, Sector sector, Int2ObjectOpenHashMap<Mine> mines) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT ID, OWNER, FACTION, HP, COMPOSITION, SECTOR_X, SECTOR_Y, SECTOR_Z, LOCAL_X, LOCAL_Y, LOCAL_Z, CREATION_DATE, ARMED, ARMED_IN_SECS, AMMO FROM " + table + " "
					+ "WHERE SECTOR_X = " + sector.pos.x + " AND SECTOR_Y = " + sector.pos.y + " AND SECTOR_Z = " + sector.pos.z + ";");
			while(r.next()) {
				Mine m = new Mine(state);
				m.setFromDatabase(sector, r);
				mines.put(m.getId(), m);
			}
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	@Override
	public void afterCreation(Statement s) {

	}

	public void armMines(long ownerId, int sectorX, int sectorY, int sectorZ, boolean armed) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			PreparedStatement p = s.getConnection().prepareStatement(
					"UPDATE " + table + " SET(ARMED) = (CAST(? AS BOOLEAN)) "
							+ "WHERE OWNER = CAST(? AS BIGINT) AND SECTOR_X = CAST(? AS INT) AND SECTOR_Y = CAST(? AS INT) AND SECTOR_Z = CAST(? AS INT);");

			p.setBoolean(1, armed);
			p.setLong(2, ownerId);
			p.setInt(3, sectorX);
			p.setInt(4, sectorY);
			p.setInt(5, sectorZ);
			p.executeUpdate();
			p.close();
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	public void armMines(long ownerId, boolean armed) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			PreparedStatement p = s.getConnection().prepareStatement(
					"UPDATE " + table + " SET(ARMED) = (CAST(? AS BOOLEAN)) "
							+ "WHERE OWNER = CAST(? AS BIGINT);");

			p.setBoolean(1, armed);
			p.setLong(2, ownerId);
			p.executeUpdate();
			p.close();
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	public void clearSector(int x, int y, int z) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			s.executeUpdate("DELETE FROM " + table + " WHERE SECTOR_X = " + x + " AND SECTOR_Y = " + y + " AND SECTOR_Z = " + z + ";");
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

}
