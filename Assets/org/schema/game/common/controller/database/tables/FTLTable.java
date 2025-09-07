package org.schema.game.common.controller.database.tables;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.PercentCallbackInterface;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.elements.InterControllerCollectionManager;
import org.schema.game.common.controller.elements.InterControllerConnectionManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateElementManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.FTLConnection;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import java.io.IOException;
import java.sql.*;

public class FTLTable extends Table {
	public static String DIRECT_PREFIX = "DIRECT_";

	public FTLTable(TableManager m, Connection c) {
		super("FTL", m, c);
	}

	public static void deleteFTLEntry(Connection c, WarpgateElementManager warpGates, Vector3i fromCanBeNull) throws SQLException {
		Statement s = c.createStatement();
		String fromUID = DatabaseEntry.removePrefix(warpGates.getSegmentController().getUniqueIdentifier());
		if(fromCanBeNull == null) {
			s.executeUpdate("DELETE FROM FTL WHERE FROM_UID = '" + fromUID + "';");
		} else {
			s.executeUpdate("DELETE FROM FTL WHERE FROM_UID = '" + fromUID + "' AND FROM_X = " + fromCanBeNull.x + " AND FROM_Y = " + fromCanBeNull.y + " AND FROM_Z = " + fromCanBeNull.z + ";");
		}
		s.close();
	}

	public static boolean insertFTLEntry(Connection c, String fromUID, Vector3i from, Vector3i fromLocal, String toUID, Vector3i to, Vector3i toLoc, int type, int permission) throws SQLException {

		Statement s = c.createStatement();

		ResultSet fromThis = s.executeQuery("SELECT ID FROM FTL WHERE FROM_UID = '" + fromUID + "' AND FROM_X_LOC = " + fromLocal.x + " AND FROM_Y_LOC = " + fromLocal.y + " AND FROM_Z_LOC = " + fromLocal.z + ";");

		if(!fromThis.next()) {
			PreparedStatement p = c.prepareStatement(
					"INSERT INTO FTL(FROM_X,FROM_Y,FROM_Z,FROM_X_LOC,FROM_Y_LOC,FROM_Z_LOC, TO_X,TO_Y,TO_Z, TO_X_LOC,TO_Y_LOC,TO_Z_LOC, FROM_UID, TO_UID, TYPE, PERMISSION) VALUES(" +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS VARCHAR(128))," +
							"CAST(? AS VARCHAR(128))," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)" +
							");", Statement.RETURN_GENERATED_KEYS);

			p.setInt(1, from.x);
			p.setInt(2, from.y);
			p.setInt(3, from.z);
			p.setInt(4, fromLocal.x);
			p.setInt(5, fromLocal.y);
			p.setInt(6, fromLocal.z);
			p.setInt(7, to.x);
			p.setInt(8, to.y);
			p.setInt(9, to.z);
			p.setInt(10, toLoc.x);
			p.setInt(11, toLoc.y);
			p.setInt(12, toLoc.z);
			p.setString(13, fromUID);
			p.setString(14, toUID);
			p.setInt(15, type);
			p.setInt(16, permission);
			p.execute();

			s.close();
			return true;
		} else {
			System.err.println("[DB] WARNING: could not insert " + fromUID + " -> local " + fromLocal + "; already exists in db");
		}

		s.close();
		return false;

	}

	public static void updateOrInsertFTLEntry(Connection c, InterControllerConnectionManager connections) throws SQLException {

		Statement s = c.createStatement();

		String fromUID = DatabaseEntry.removePrefix(connections.getSegmentController().getUniqueIdentifier());

		/*
		 * Generate map of all connection for this object in database
		 */

		ResultSet fromThis = s.executeQuery("SELECT FROM_X_LOC, FROM_Y_LOC, FROM_Z_LOC, ID FROM FTL WHERE FROM_UID = '" + fromUID + "';");
		Long2LongOpenHashMap localMap2EntryID = new Long2LongOpenHashMap();
		while(fromThis.next()) {
			localMap2EntryID.put(ElementCollection.getIndex(fromThis.getInt(1), fromThis.getInt(2), fromThis.getInt(3)), fromThis.getLong(4));
		}

		/*
		 * Change all existing. Add non existing
		 */
		for(InterControllerCollectionManager col : connections.getCollectionManagers()) {
			if(localMap2EntryID.containsKey(col.getControllerElement().getAbsoluteIndex())) {
				if(!"none".equals(col.getWarpDestinationUID()) && !"unmarked".equals(col.getWarpDestinationUID()) && !connections.getSegmentController().isMarkedForPermanentDelete()) {
					//existing entry -> overwrite
					long dbid = localMap2EntryID.remove(col.getControllerElement().getAbsoluteIndex()); //remove so rest can be deleted from db afterwards

					try {

						Vector3i toLoc = col.getLocalDestination();
						Vector3i toSec;
						String toUID;
						if(col.getWarpDestinationUID().startsWith(DIRECT_PREFIX)) {
							toUID = col.getWarpDestinationUID();
							toSec = new Vector3i(toLoc);
						} else {
							toUID = DatabaseEntry.removePrefixWOException(col.getWarpDestinationUID());
							Sendable sendable = col.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(col.getWarpDestinationUID());
							Sector secTm;
							if(sendable != null && sendable instanceof SimpleTransformableSendableObject && (secTm = ((GameServerState) col.getState()).getUniverse().getSector(((SimpleTransformableSendableObject) sendable).getSectorId())) != null) {
								toSec = secTm.pos;
							} else {
								toSec = SectorTable.getSector(c, toUID, new Vector3i());
							}
						}
						PreparedStatement p = c.prepareStatement(
								"UPDATE FTL SET (TO_X,TO_Y,TO_Z,TO_X_LOC,TO_Y_LOC,TO_Z_LOC,TO_UID,TYPE,PERMISSION) = (" +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS VARCHAR(128))," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)" +
										") WHERE ID = " + "CAST(? AS BIGINT)" + ";");
						p.setInt(1, toSec.x);
						p.setInt(2, toSec.y);
						p.setInt(3, toSec.z);
						p.setInt(4, toLoc.x);
						p.setInt(5, toLoc.y);
						p.setInt(6, toLoc.z);
						p.setString(7, toUID);
						p.setInt(8, col.getWarpType());
						p.setInt(9, col.getWarpPermission());
						p.setLong(10, dbid);

						p.execute();

						p.close();

					} catch(EntityNotFountException e) {
						System.err.println("[DB][FTL] destination: " + col.getWarpDestinationUID() + " not found. removing connection from DB");
						//put back so it's removed later
						localMap2EntryID.put(col.getControllerElement().getAbsoluteIndex(), dbid);
					}
				}

			} else {
				//new entry -> insert
				if(!"none".equals(col.getWarpDestinationUID()) && !"unmarked".equals(col.getWarpDestinationUID()) && !connections.getSegmentController().isMarkedForPermanentDelete()) {
					try {
						String toUID;
						Vector3i toLoc = col.getLocalDestination();
						Vector3i toSec;

						if(col.getWarpDestinationUID().startsWith(DIRECT_PREFIX)) {
							toUID = col.getWarpDestinationUID();
							toSec = new Vector3i(toLoc);
							System.err.println("[DB][FTL] INSERTING DIRECT CONNECTION: " + fromUID + " -> " + toUID);
						} else {
							toUID = DatabaseEntry.removePrefix(col.getWarpDestinationUID());

							assert (!toUID.startsWith("ENTITY"));

							Sendable sendable = col.getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(col.getWarpDestinationUID());
							Sector secTm;
							if(sendable != null && sendable instanceof SimpleTransformableSendableObject && (secTm = ((GameServerState) col.getState()).getUniverse().getSector(((SimpleTransformableSendableObject) sendable).getSectorId())) != null) {
								toSec = secTm.pos;
							} else {
								System.err.println("[DB][FTL] SEARCHING FOR DESTINATION SECTOR: " + toUID);
								toSec = SectorTable.getSector(c, toUID, new Vector3i());
							}
						}

						Sector ownSec = ((GameServerState) col.getState()).getUniverse().getSector(connections.getSegmentController().getSectorId());
						Vector3i ownSecPos;
						if(ownSec != null) {
							ownSecPos = ownSec.pos;
						} else {
							ownSecPos = connections.getSegmentController().transientSectorPos;
						}
						String ownUID = DatabaseEntry.removePrefix(col.getSegmentController().getUniqueIdentifier());
						PreparedStatement p = c.prepareStatement(
								"INSERT INTO FTL(FROM_X,FROM_Y,FROM_Z,FROM_X_LOC,FROM_Y_LOC,FROM_Z_LOC, TO_X,TO_Y,TO_Z,TO_X_LOC,TO_Y_LOC,TO_Z_LOC, FROM_UID, TO_UID, TYPE, PERMISSION) VALUES(" +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)," +
										"CAST(? AS VARCHAR(128))," +
										"CAST(? AS VARCHAR(128))," +
										"CAST(? AS INT)," +
										"CAST(? AS INT)" +
										");", Statement.RETURN_GENERATED_KEYS);

						p.setInt(1, ownSecPos.x);
						p.setInt(2, ownSecPos.y);
						p.setInt(3, ownSecPos.z);
						p.setInt(4, col.getControllerPos().x);
						p.setInt(5, col.getControllerPos().y);
						p.setInt(6, col.getControllerPos().z);
						p.setInt(7, toSec.x);
						p.setInt(8, toSec.y);
						p.setInt(9, toSec.z);
						p.setInt(10, toLoc.x);
						p.setInt(11, toLoc.y);
						p.setInt(12, toLoc.z);
						p.setString(13, ownUID);
						p.setString(14, toUID);
						p.setInt(15, col.getWarpType());
						p.setInt(16, col.getWarpPermission());

						p.execute();

						p.close();

					} catch(EntityNotFountException e) {
						System.err.println("[DB][FTL] Exception: destination: " + col.getWarpDestinationUID() + " not found for " + connections.getSegmentController() + ". not inserting");
					}
//					assert(false):warpGates.getSegmentController()+"; "+fromUID;
				} else {
//					assert(false):warpGates.getSegmentController()+"; "+fromUID;
				}
			}
		}
		/*
		 * Remove all non existing.
		 */
		if(localMap2EntryID.size() > 0) {
			StringBuilder types = new StringBuilder();
			String idINString = "";
			types.append("(");
			boolean first = true;
			for(long id : localMap2EntryID.values()) {
				if(first) {
					types.append(id);
					first = false;
				} else {
					types.append(", ");
					types.append(id);
				}
			}
			types.append(")");
			idINString = " ID IN " + types;

			s.executeUpdate("DELETE FROM FTL WHERE" + idINString + ";");

			System.err.println("[DB][FTL] " + connections.getSegmentController() + " removing were " + idINString);
		}

		s.close();
	}

	@Override
	public void define() {
		addColumn("ID", "BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1)", true);
		addColumn("FROM_X", "INT NOT NULL");
		addColumn("FROM_Y", "INT NOT NULL");
		addColumn("FROM_Z", "INT NOT NULL");
		addColumn("FROM_X_LOC", "INT NOT NULL");
		addColumn("FROM_Y_LOC", "INT NOT NULL");
		addColumn("FROM_Z_LOC", "INT NOT NULL");
		addColumn("FROM_UID", "VARCHAR(128) NOT NULL");
		addColumn("TO_X", "INT NOT NULL");
		addColumn("TO_Y", "INT NOT NULL");
		addColumn("TO_Z", "INT NOT NULL");
		addColumn("TO_X_LOC", "INT DEFAULT 0 NOT NULL");
		addColumn("TO_Y_LOC", "INT DEFAULT 0 NOT NULL");
		addColumn("TO_Z_LOC", "INT DEFAULT 0 NOT NULL");
		addColumn("TO_UID", "VARCHAR(128) NOT NULL");
		addColumn("TYPE", "INT not null");
		addColumn("PERMISSION", "INT not null");

		addIndex("fromFTLIndLoc", true, "FROM_UID", "FROM_X_LOC", "FROM_Y_LOC", "FROM_Z_LOC");
		addIndex("fromFTLInd", "FROM_X", "FROM_Y", "FROM_Z");
		addIndex("fromUIDFTLInd", "FROM_UID", "FROM_X", "FROM_Y", "FROM_Z");
		addIndex("toFTLInd", "TO_X", "TO_Y", "TO_Z");
		addIndex("fromFTLUID", "FROM_UID");
		addIndex("toFTLUID", "TO_UID");
		addIndex("typeFTL", "TYPE");
		addIndex("permissionFTL", "PERMISSION");
	}

	@Override
	public void afterCreation(Statement s) {

	}

	public boolean insertFTLEntry(String fromUID, Vector3i from, Vector3i fromLocal, String toUID, Vector3i to, Vector3i toLoc, int type, int permission) throws SQLException {
		return insertFTLEntry(c, fromUID, from, fromLocal, toUID, to, toLoc, type, permission);
	}

	public void migrateFTL(PercentCallbackInterface percentCallbackInterface) throws SQLException, IOException {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'FTL';");
		if(!r.next()) {
//			createFTLTable();
			createTable();

		} else {
			ResultSet rs = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'FTL' AND "
					+
					"COLUMN_NAME = 'TO_X_LOC';");
			if(!rs.next()) {
				s.executeUpdate("ALTER TABLE FTL ADD COLUMN TO_X_LOC INT DEFAULT 0 not null;");
				s.executeUpdate("ALTER TABLE FTL ADD COLUMN TO_Y_LOC INT DEFAULT 0 not null;");
				s.executeUpdate("ALTER TABLE FTL ADD COLUMN TO_Z_LOC INT DEFAULT 0 not null;");
			}
		}
		s.close();
	}

	public FTLConnection getFtl(Vector3i from, Vector3i localFrom, String UID) {
		FTLConnection con = null;
		Statement s;
		try {
			s = c.createStatement();
			String query = "SELECT TO_X, TO_Y, TO_Z, TYPE, PERMISSION, TO_X_LOC, TO_Y_LOC, TO_Z_LOC, TO_UID FROM FTL WHERE FROM_UID = '" + UID + "' AND FROM_X = " + from.x + " AND FROM_Y = " + from.y + " AND FROM_Z = " + from.z + " AND FROM_X_LOC = " + localFrom.x + " AND FROM_Y_LOC = " + localFrom.y + " AND FROM_Z_LOC = " + localFrom.z;
			ResultSet q = s.executeQuery(query);
//			System.err.println("QUERY: "+query);
			while(q.next()) {
				if(con == null) {
					con = new FTLConnection();
					con.from = new Vector3i(from);
					con.to = new ObjectArrayList<Vector3i>();
					con.param = new ObjectArrayList<Vector3i>();
					con.toLoc = new ObjectArrayList<Vector3i>();
				}
				con.to.add(new Vector3i(q.getInt(1), q.getInt(2), q.getInt(3)));

				con.param.add(new Vector3i(q.getInt(4), q.getInt(5), 0));
				con.toLoc.add(new Vector3i(q.getInt(6), q.getInt(7), q.getInt(8)));
				con.toUID = q.getString(9);
			}
//			assert(con != null):query;
			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}

		return con;
	}

	public FTLConnection getFtl(Vector3i from, String UID) {
		FTLConnection con = null;
		Statement s;
		try {
			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT TO_X, TO_Y, TO_Z, TYPE, PERMISSION, TO_X_LOC, TO_Y_LOC, TO_Z_LOC FROM FTL WHERE FROM_UID = '" + UID + "' AND FROM_X = " + from.x + " AND FROM_Y = " + from.y + " AND FROM_Z = " + from.z);

			while(q.next()) {
				if(con == null) {
					con = new FTLConnection();
					con.from = new Vector3i(from);
					con.to = new ObjectArrayList<Vector3i>();
					con.param = new ObjectArrayList<Vector3i>();
					con.toLoc = new ObjectArrayList<Vector3i>();
				}
				con.to.add(new Vector3i(q.getInt(1), q.getInt(2), q.getInt(3)));

				con.param.add(new Vector3i(q.getInt(4), q.getInt(5), 0));
				con.toLoc.add(new Vector3i(q.getInt(6), q.getInt(7), q.getInt(8)));
			}

			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}

		return con;
	}

	public FTLConnection getFtl(Vector3i from) {
		FTLConnection con = null;
		Statement s;
		try {
			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT TO_X, TO_Y, TO_Z, TYPE, PERMISSION, TO_X_LOC, TO_Y_LOC, TO_Z_LOC FROM FTL WHERE FROM_X = " + from.x + " AND FROM_Y = " + from.y + " AND FROM_Z = " + from.z);

			while(q.next()) {
				if(con == null) {
					con = new FTLConnection();
					con.from = new Vector3i(from);
					con.to = new ObjectArrayList<Vector3i>();
					con.toLoc = new ObjectArrayList<Vector3i>();
					con.param = new ObjectArrayList<Vector3i>();
				}
				con.to.add(new Vector3i(q.getInt(1), q.getInt(2), q.getInt(3)));
				con.param.add(new Vector3i(q.getInt(4), q.getInt(5), 0));
				con.toLoc.add(new Vector3i(q.getInt(6), q.getInt(7), q.getInt(8)));
			}

			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}

		return con;
	}

	public void fillFTLData(
			Object2ObjectOpenHashMap<Vector3i, FTLConnection> ftlData) {
		Statement s;
		try {
			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT FROM_X, FROM_Y, FROM_Z, TO_X, TO_Y, TO_Z, TYPE, PERMISSION, TO_X_LOC, TO_Y_LOC, TO_Z_LOC FROM FTL;");

			while(q.next()) {
				Vector3i from = new Vector3i(q.getInt(1), q.getInt(2), q.getInt(3));
				FTLConnection con = ftlData.get(from);
				if(con == null) {
					con = new FTLConnection();
					con.from = from;
					con.to = new ObjectArrayList<Vector3i>();
					con.param = new ObjectArrayList<Vector3i>();
					con.toLoc = new ObjectArrayList<Vector3i>();
					ftlData.put(from, con);
				}
				con.to.add(new Vector3i(q.getInt(4), q.getInt(5), q.getInt(6)));
				con.param.add(new Vector3i(q.getInt(7), q.getInt(8), 0));
				con.toLoc.add(new Vector3i(q.getInt(9), q.getInt(10), q.getInt(11)));
			}

			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public void createFTLTable() throws SQLException {
		{
			Statement s = c.createStatement();

			s.execute("DROP TABLE FTL if exists;");
			s.execute("CREATE CACHED TABLE FTL(" +
					"BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1), " +
					"FROM_X INT not null, " +
					"FROM_Y INT not null, " +
					"FROM_Z INT not null, " +
					"FROM_X_LOC INT not null, " +
					"FROM_Y_LOC INT not null, " +
					"FROM_Z_LOC INT not null, " +
					"FROM_UID VARCHAR(128) not null, " +
					"TO_X INT not null, " +
					"TO_Y INT not null, " +
					"TO_Z INT not null, " +
					"TO_X_LOC INT DEFAULT 0 not null, " +
					"TO_Y_LOC INT DEFAULT 0 not null, " +
					"TO_Z_LOC INT DEFAULT 0 not null, " +
					"TO_UID VARCHAR(128) not null, " +
					"TYPE INT not null, " +
					"PERMISSION INT not null," +
					"primary key (ID)" +
					");");

			s.execute("create unique index fromFTLIndLoc on FTL(FROM_UID, FROM_X_LOC, FROM_Y_LOC, FROM_Z_LOC);");
			s.execute("create index fromFTLInd on FTL(FROM_X,FROM_Y,FROM_Z);");
			s.execute("create index fromUIDFTLInd on FTL(FROM_UID, FROM_X,FROM_Y,FROM_Z);");
			s.execute("create index toFTLInd on FTL(TO_X,TO_Y,TO_Z);");
			s.execute("create index fromFTLUID on FTL(FROM_UID);");
			s.execute("create index toFTLUID on FTL(TO_UID);");
			s.execute("create index typeFTL on FTL(TYPE);");
			s.execute("create index permissionFTL on FTL(PERMISSION);");
			s.close();
		}
	}
}
