package org.schema.game.common.controller.database;

import org.schema.common.util.linAlg.Vector3i;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimDatabaseEntry {
	public Vector3i sectorPos;
	public int type;
	public int faction;
	public int creatorID;

	public SimDatabaseEntry(ResultSet res) throws SQLException {

		sectorPos = new Vector3i(res.getInt(1), res.getInt(2), res.getInt(3));
		type = res.getByte(4);
		faction = res.getInt(5);
		creatorID = res.getInt(6);

	}

}
