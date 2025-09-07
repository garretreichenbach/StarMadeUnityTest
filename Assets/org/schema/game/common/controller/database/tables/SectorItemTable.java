package org.schema.game.common.controller.database.tables;

import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.game.common.data.world.Sector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class SectorItemTable extends Table {

	public static final int itemArraySize = Sector.itemDataSize * 1024;

	public SectorItemTable(TableManager m, Connection c) {
		super("SECTORS_ITEMS", m, c);
	}

	public static byte[] getItemBinaryString(Map<Integer, FreeItem> items) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(Math.min(itemArraySize, items.size() * Sector.itemDataSize));
		DataOutputStream s = new DataOutputStream(byteArrayOutputStream);
		int i = 0;
		for(Map.Entry<Integer, FreeItem> e : items.entrySet()) {
			if(i >= itemArraySize) {
				break;
			}
			s.writeShort(e.getValue().getType());
			s.writeInt(e.getValue().getCount());
			s.writeFloat(e.getValue().getPos().x);
			s.writeFloat(e.getValue().getPos().y);
			s.writeFloat(e.getValue().getPos().z);
			s.writeInt(e.getValue().getMetaId());
			i += Sector.itemDataSize;

		}
		byteArrayOutputStream.flush();
		return byteArrayOutputStream.toByteArray();
	}

	public static void updateOrInsertItems(Statement s, long id,
	                                       Map<Integer, FreeItem> items) throws SQLException, IOException {

		ResultSet query = s.executeQuery("SELECT ID FROM SECTORS_ITEMS WHERE ID = " + id + ";");
		if(query.next()) {
			if(items.isEmpty()) {
				s.execute("DELETE FROM SECTORS_ITEMS WHERE ID = " + id + ";");
			} else {
				byte[] itemBinaryString = getItemBinaryString(items);
				PreparedStatement p = s.getConnection().prepareStatement(
						"UPDATE SECTORS_ITEMS SET (ITEMS) = (" +
								"CAST(? AS VARBINARY(" + itemArraySize + "))" +
								") WHERE ID = CAST(? AS BIGINT)" + ";");
				p.setBytes(1, itemBinaryString);
				p.setLong(2, id);
				p.execute();
				p.close();
			}
		} else {
			if(!items.isEmpty()) {
				PreparedStatement p = s.getConnection().prepareStatement(
						"INSERT INTO SECTORS_ITEMS(ID, ITEMS) VALUES(" +
								"CAST(? AS BIGINT)," +
								"CAST(? AS VARBINARY(" + itemArraySize + "))" +
								");");
				p.setLong(1, id);
				p.setBytes(2, getItemBinaryString(items));
				p.execute();
				p.close();
			}
		}
	}

	@Override
	public void define() {
		addColumn("ID", "BIGINT", true);
		addColumn("ITEMS", "VARBINARY(" + itemArraySize + ") not null");
	}

	@Deprecated
	public void createSectorItemTable() throws SQLException {
		{
			Statement s = c.createStatement();

			s.execute("DROP TABLE SECTORS_ITEMS if exists;");

			s.execute("CREATE CACHED TABLE SECTORS_ITEMS(" +
					"BIGINT, " +
					"ITEMS VARBINARY(" + itemArraySize + ") not null, " + //5kb
					"primary key (ID)" +
					");");

			s.close();
		}
	}

	@Override
	public void afterCreation(Statement s) {

	}

	public void loadItems(Statement s, Sector sector) throws SQLException, IOException {
		ResultSet query = s.executeQuery("SELECT ITEMS FROM SECTORS_ITEMS WHERE ID = " + sector.getDBId() + ";");
		if(query.next()) {
			sector.loadItems(query.getBytes(1));
		}
	}

}
