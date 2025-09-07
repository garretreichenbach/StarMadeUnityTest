package org.schema.game.common.controller.database.tables;

import java.sql.*;

public class IdGenTable extends Table {
	public IdGenTable(TableManager m, Connection c) {
		super("ID_GEN_TABLE", m, c);
	}

	@Override
	public void define() {

		addColumn("ID", "VARCHAR(128) not null", true);
		addColumn("ID_GEN", "BIGINT not null");
	}

	public void updateOrInsert(String idName, long gen) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT ID_GEN FROM " + table + " WHERE ID ='" + idName + "'");

			if(!r.next()) {
				PreparedStatement p = s.getConnection().prepareStatement(
						"INSERT INTO " + table + "(ID, ID_GEN) VALUES(" +
								"CAST(? AS VARCHAR(128))," +
								"CAST(? AS BIGINT)" +
								");");
				p.setString(1, idName);
				p.setLong(2, gen);
				p.executeUpdate();
				p.close();
			} else {

				PreparedStatement p = s.getConnection().prepareStatement(
						"UPDATE " + table + " SET(ID_GEN) = (" +
								"CAST(? AS BIGINT)" +
								") WHERE ID = CAST(? AS VARCHAR(128));");

				p.setLong(1, gen);
				p.setString(2, idName);
				int i = p.executeUpdate();
				assert (i > 0);
				p.close();

			}
		} finally {
			if(s != null) {
				s.close();
			}
		}
	}

	public long getId(String idName) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT ID_GEN FROM " + table + " WHERE ID ='" + idName + "'");
			if(r.next()) {
				return r.getLong(1);
			} else {
				return 0;
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
}
