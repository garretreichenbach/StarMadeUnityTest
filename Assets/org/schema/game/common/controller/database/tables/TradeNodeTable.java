package org.schema.game.common.controller.database.tables;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.controller.database.FogOfWarReceiver;
import org.schema.game.common.controller.trade.TradeManager;
import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class TradeNodeTable extends Table {
	/*
	 * type (2)
	 * count (4)
	 * price (4)
	 * --------
	 * 10
	 */
	public static final int itemSize = 2048 * (2 + 4 + 4 + 4 + 4) * 2 + 4;

	public TradeNodeTable(TableManager m, Connection c) {
		super("TRADE_NODES", m, c);
	}

	public static void insertOrUpdateTradeNode(Statement s, TradeNodeStub node) throws SQLException, IOException {
		ResultSet query = s.executeQuery("SELECT ID FROM TRADE_NODES WHERE ID = " + node.getEntityDBId() + ";");
		if(query.next()) {
			long dbId = query.getLong(1);

			PreparedStatement p = s.getConnection().prepareStatement(
					"UPDATE TRADE_NODES SET ("
							+ "SEC_X, "
							+ "SEC_Y, "
							+ "SEC_Z, "
							+ "PLAYER, "
							+ "STATION_NAME, "
							+ "FACTION, "
							+ "PERMISSION, "
							+ "ITEMS,"
							+ "VOLUME, "
							+ "CAPACITY, "
							+ "CREDITS "
							+ ") = (" +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS VARCHAR(128))," +
							"CAST(? AS VARCHAR(128))," +
							"CAST(? AS INT)," +
							"CAST(? AS BIGINT)," +
							"CAST(? AS VARBINARY(" + itemSize + "))," +
							"CAST(? AS DOUBLE)," +
							"CAST(? AS DOUBLE)," +
							"CAST(? AS BIGINT)" +
							") WHERE ID = CAST(? AS BIGINT)" + ";");
			p.setInt(1, node.getSector().x);
			p.setInt(2, node.getSector().y);
			p.setInt(3, node.getSector().z);
			p.setString(4, node.getOwnerString());
			p.setString(5, node.getStationName());
			p.setInt(6, node.getFactionId());
			p.setLong(7, node.getTradePermission());
			byte[] b = new byte[node.getPricesInputStream().available()];
			node.getPricesInputStream().read(b);
			p.setBytes(8, b);
			p.setDouble(9, node.getVolume());
			p.setDouble(10, node.getCapacity());
			p.setLong(11, node.getCredits());
			p.setLong(12, node.getEntityDBId());
			p.executeUpdate();
			p.close();

		} else {
			PreparedStatement p = s.getConnection().prepareStatement(
					"INSERT INTO TRADE_NODES("
							+ "SEC_X, "
							+ "SEC_Y, "
							+ "SEC_Z, "
							+ "PLAYER, "
							+ "STATION_NAME, "
							+ "FACTION, "
							+ "PERMISSION, "
							+ "ITEMS, "
							+ "VOLUME, "
							+ "CAPACITY, "
							+ "CREDITS, "
							+ "ID"
							+ ") VALUES(" +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS INT)," +
							"CAST(? AS VARCHAR(128))," +
							"CAST(? AS VARCHAR(128))," +
							"CAST(? AS INT)," +
							"CAST(? AS BIGINT)," +
							"CAST(? AS VARBINARY(" + itemSize + ")), " +
							"CAST(? AS DOUBLE)," +
							"CAST(? AS DOUBLE)," +
							"CAST(? AS BIGINT)," +
							"CAST(? AS BIGINT)" +
							");");
			p.setInt(1, node.getSector().x);
			p.setInt(2, node.getSector().y);
			p.setInt(3, node.getSector().z);
			p.setString(4, node.getOwnerString());
			p.setString(5, node.getStationName());
			p.setInt(6, node.getFactionId());
			p.setLong(7, node.getTradePermission());
			byte[] b = new byte[node.getPricesInputStream().available()];
			node.getPricesInputStream().read(b);
			p.setBytes(8, b);
			p.setDouble(9, node.getVolume());
			p.setDouble(10, node.getCapacity());
			p.setLong(11, node.getCredits());
			p.setLong(12, node.getEntityDBId());
			p.executeUpdate();
			p.close();
		}
	}

	public static void removeTradeNode(Statement s, TradeNodeStub m) throws SQLException {
		removeTradeNode(s, m.getEntityDBId());
	}

	public static void removeTradeNode(Statement s, long id) throws SQLException {
		ResultSet executeQuery = s.executeQuery("SELECT ID FROM TRADE_NODES WHERE ID = " + id + ";");
		if(executeQuery.next()) {
			try {
				throw new Exception("[SERVER][DATABASE] REMOVE TRADENODE: " + id + " (Not an error just a trace just in case this shouldn't have happened)");
			} catch(Exception e) {
				e.printStackTrace();
			}
			s.executeUpdate("DELETE FROM TRADE_NODES WHERE ID = " + id + ";");
		}
	}

	public static TradeNodeStub getTradeNode(Statement s, long id) throws SQLException {
		return getTradeNode(s, id, null);
	}

	public static TradeNodeStub getTradeNode(Statement s, long id, TradeNodeStub n) throws SQLException {
		ResultSet q = s.executeQuery("SELECT "
				+ "SEC_X, "
				+ "SEC_Y, "
				+ "SEC_Z, "
				+ "PLAYER, "
				+ "STATION_NAME, "
				+ "FACTION, "
				+ "PERMISSION, "
				+ "VOLUME, "
				+ "CAPACITY, "
				+ "CREDITS "
				+ "FROM TRADE_NODES WHERE ID = " + id + ";");

		if(q.next()) {
			return getTradeNodeStubFromResult(id, q, n);
		}
		return null;
	}

	static void writeTradeNode(Statement s, GameServerState state, Vector3i secPos,
	                           ShopInterface shopInterface) throws SQLException {

		long dbId = shopInterface.getSegmentBuffer().getSegmentController().dbId;

		if(dbId > 0) {
			TradeNodeStub shopNode = getTradeNode(s, dbId);
			boolean updatedOrRemoved = false;
			if(shopNode != null && !shopInterface.isTradeNode()) {
				System.err.println("[SERVER][DATABASE] REMOVING TRADENODE. ShopInterface " + shopInterface + " is not a tradenode: removing: " + dbId);
				removeTradeNode(s, dbId);
				updatedOrRemoved = true;
			} else if(shopInterface != null && shopInterface.isTradeNode()) {
				if(!shopInterface.isValidShop()) {
					if(FactionManager.isNPCFaction(shopInterface.getFactionId())) {

						Faction faction = state.getFactionManager().getFaction(shopInterface.getFactionId());
						System.err.println("FACT " + faction);
						if(faction != null) {
							String db = shopInterface.getSegmentController().getType().dbPrefix + faction.getHomebaseUID();
							System.err.println("FACT " + faction + ": " + shopInterface.getSegmentController().getUniqueIdentifier() + " ---> " + db);
						}
						try {
							throw new Exception("TRIED TO REMOVE NPC SHOP: " + shopInterface.getSegmentController() + "; ");
						} catch(Exception e) {
							e.printStackTrace();
						}
						assert (false);
					}
					if(shopInterface.wasValidTradeNode()) {
						removeTradeNode(s, shopInterface.getSegmentController().getDbId());
					}

				} else {
					try {
						insertOrUpdateTradeNode(s, shopInterface.getTradeNode());
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				updatedOrRemoved = true;
			}

			if(updatedOrRemoved) {
				Vector3i system = VoidSystem.getContainingSystem(secPos, new Vector3i());

				synchronized(state.getUniverse().tradeNodesDirty) {
					state.getUniverse().tradeNodesDirty.enqueue(dbId);
				}
			}

		}
	}

	private static TradeNodeStub getTradeNodeStubFromResult(long id, ResultSet q, TradeNodeStub n) throws SQLException {
		if(n == null) {
			n = new TradeNodeStub();
		}
		n.setEntityDBId(id);
		n.setSector(new Vector3i(q.getInt(1), q.getInt(2), q.getInt(3)));
		n.parseOwnerString(q.getString(4));
		n.setStationName(q.getString(5));
		n.setFactionId(q.getInt(6));
		n.setTradePermission(q.getLong(7));
		n.setVolume(q.getDouble(8));
		n.setCapacity(q.getDouble(9));
		n.setCredits(q.getLong(10));
		return n;
	}

	@Override
	public void define() {

		try {
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'TRADES';");
			if(r.next()) {
				s.execute("ALTER TABLE TRADES DROP CONSTRAINT ffATN;");
				s.execute("ALTER TABLE TRADES DROP CONSTRAINT ffBTN;");
			}
			s.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		addColumn("ID", "BIGINT", true);
		addColumn("SEC_X", "INT NOT NULL");
		addColumn("SEC_Y", "INT NOT NULL");
		addColumn("SEC_Z", "INT NOT NULL");
		addColumn("PLAYER", "VARCHAR(128) not null");
		addColumn("STATION_NAME", "VARCHAR(128) not null");
		addColumn("FACTION", "INT NOT NULL");
		addColumn("PERMISSION", "BIGINT DEFAULT " + TradeManager.PERM_ALL_BUT_ENEMY + " not null");
		addColumn("ITEMS", "VARBINARY(" + itemSize + ") not null");
		addColumn("VOLUME", "DOUBLE NOT NULL");
		addColumn("CAPACITY", "DOUBLE NOT NULL");
		addColumn("CREDITS", "BIGINT NOT NULL");

		addIndex("facIndex", "FACTION");
		addIndex("playerIndex", "PLAYER");
		addIndex("trSysCoordIndex", "SEC_X", "SEC_Y", "SEC_Z");
	}

	public void setTradeCredits(long entityDBId, long credits) throws SQLException {
		Statement s = c.createStatement();
		PreparedStatement p = s.getConnection().prepareStatement(
				"UPDATE TRADE_NODES SET ("
						+ "CREDITS"
						+ ") = (" +
						"CAST(? AS BIGINT)" +
						") WHERE ID = CAST(? AS BIGINT)" + ";");
		p.setDouble(1, credits);
		p.setLong(2, entityDBId);

		p.execute();

		s.close();
	}

	public void setTradePrices(long entityDBId, double volume, long credits, List<TradePriceInterface> prices) throws SQLException {
		Statement s = c.createStatement();
		PreparedStatement p = s.getConnection().prepareStatement(
				"UPDATE TRADE_NODES SET ("
						+ "VOLUME, "
						+ "CREDITS, "
						+ "ITEMS"
						+ ") = (" +
						"CAST(? AS DOUBLE)," +
						"CAST(? AS BIGINT)," +
						"CAST(? AS VARBINARY(" + itemSize + "))" +
						") WHERE ID = CAST(? AS BIGINT)" + ";");
		p.setDouble(1, volume);
		p.setDouble(2, credits);

		FastByteArrayOutputStream backing = new FastByteArrayOutputStream(10 * 1024);
		DataOutputStream b = new DataOutputStream(backing);
		try {
			ShoppingAddOn.serializeTradePrices(b, true, TradePrices.getFromPrices(prices, entityDBId), entityDBId);
		} catch(IOException e) {
			throw new SQLException(e);
		}

		byte[] byteArray = Arrays.copyOf(backing.array, (int) backing.position());
		p.setBytes(3, byteArray);

		p.setLong(4, entityDBId);

		p.execute();

		s.close();
	}

	public void setTradePrices(long dbId, long credits, double volume, double capacity, TradePrices prices) throws SQLException, IOException {
		Statement s;
		s = c.createStatement();
		PreparedStatement p = s.getConnection().prepareStatement(
				"UPDATE TRADE_NODES SET ("
						+ "ITEMS,"
						+ "VOLUME, "
						+ "CAPACITY, "
						+ "CREDITS "
						+ ") = (" +
						"CAST(? AS VARBINARY(" + itemSize + "))," +
						"CAST(? AS DOUBLE)," +
						"CAST(? AS DOUBLE)," +
						"CAST(? AS BIGINT)" +
						") WHERE ID = CAST(? AS BIGINT)" + ";");
		p.setBytes(1, prices.getPricesBytesCompressed(true));
		p.setDouble(2, volume);
		p.setDouble(3, capacity);
		p.setLong(4, credits);
		p.setLong(5, dbId);
		p.executeUpdate();
		p.close();
	}

	public void insertOrUpdateTradeNode(TradeNodeStub node) throws SQLException, IOException {
		Statement s = c.createStatement();
		insertOrUpdateTradeNode(s, node);
		s.close();
	}

	public void removeTradeNode(TradeNodeStub m) throws SQLException {
		removeTradeNode(m.getEntityDBId());
	}

	public List<TradeNodeStub> getTradeNodes(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) throws SQLException {
		List<TradeNodeStub> nodes = new ObjectArrayList<TradeNodeStub>();
		Statement s;
		try {
			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT "
					+ "SEC_X, "
					+ "SEC_Y, "
					+ "SEC_Z, "
					+ "PLAYER, "
					+ "STATION_NAME, "
					+ "FACTION, "
					+ "PERMISSION, "
					+ "VOLUME, "
					+ "CAPACITY, "
					+ "CREDITS, "
					+ "ID "
					+ "FROM TRADE_NODES WHERE "
					+ "SEC_X >= " + fromX + " AND " + "SEC_X <= " + toX + " AND "
					+ "SEC_Y >= " + fromY + " AND " + "SEC_Y <= " + toY + " AND "
					+ "SEC_Z >= " + fromZ + " AND " + "SEC_Z <= " + toZ
					+ ";");

			while(q.next()) {
				nodes.add(getTradeNodeStubFromResult(q.getLong(11), q, null));
			}

			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}
		return nodes;
	}

	public TradeNodeStub getTradeNode(long id) throws SQLException {
		Statement s = c.createStatement();
		TradeNodeStub updateTradeNode = getTradeNode(s, id, null);
		s.close();
		return updateTradeNode;

	}

	public void getTradeNode(long id, TradeNodeStub output) throws SQLException {
		Statement s = c.createStatement();
		TradeNodeStub updateTradeNode = getTradeNode(s, id, output);
		s.close();

	}

	public TradeNodeStub updateTradeNode(long id, TradeNode n) throws SQLException {
		Statement s = null;
		try {
			s = c.createStatement();

			return getTradeNode(s, id, n);

		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			if(s != null) {
				s.close();
			}
		}
		return null;
	}

	public void removeTradeNode(long id) {
		Statement s;
		try {
			s = c.createStatement();
			removeTradeNode(s, id);
			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void fillTradeNodeStubData(
			Object2ObjectOpenHashMap<Vector3i, List<TradeNodeStub>> nodes, Long2ObjectOpenHashMap<TradeNodeStub> tradeDataById) {
		Vector3i tmp = new Vector3i();
		Statement s;
		Statement s2;
		try {
			s = c.createStatement();
			s2 = c.createStatement();

			ResultSet q = s.executeQuery("SELECT "
					+ "SEC_X, "
					+ "SEC_Y, "
					+ "SEC_Z, "
					+ "PLAYER, "
					+ "STATION_NAME, "
					+ "FACTION, "
					+ "PERMISSION, "
					+ "VOLUME, "
					+ "CAPACITY, "
					+ "CREDITS, "
					+ "ID "
					+ "FROM TRADE_NODES;");

			while(q.next()) {
				TradeNodeStub n = getTradeNodeStubFromResult(q.getLong(11), q, null);

				ResultSet rem = s2.executeQuery("SELECT ID FROM ENTITIES WHERE ID = " + n.getEntityDBId() + ";");

				if(rem.next()) {
					tradeDataById.put(n.getEntityDBId(), n);
					List<TradeNodeStub> list = nodes.get(n.getSystem());
					if(list == null) {
						list = new ObjectArrayList();
						nodes.put(new Vector3i(n.getSystem()), list);
					}
					list.add(n);
				} else {
					System.err.println("[DATABADE] Exception; Trade Node had no attached entity: removing");
					removeTradeNode(n);
				}
			}

			s.close();
			s2.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public List<TradeNodeStub> getTradeNodesBySystem(Vector3i sys) throws SQLException {

		return getTradeNodes(
				sys.x * VoidSystem.SYSTEM_SIZE,
				sys.y * VoidSystem.SYSTEM_SIZE,
				sys.z * VoidSystem.SYSTEM_SIZE,

				sys.x * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE - 1,
				sys.y * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE - 1,
				sys.z * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE - 1);
	}

	public List<TradePriceInterface> getTradePricesAsList(long dbEnt) {
		TradePrices pc = getTradePrices(dbEnt);
		if(pc == null) {
			return null;
		}
		return pc.getPrices();
	}

	public TradePrices getTradePrices(long dbEnt) {
		try {
			DataInputStream tpStream = getTradePricesAsStream(dbEnt);
			if(tpStream != null) {
				TradePrices pc = ShoppingAddOn.deserializeTradePrices(tpStream, true);
				return pc;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DataInputStream getTradePricesAsStream(long dbEnt) {
		DataInputStream binaryStream = null;
		Statement s = null;
		try {
			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT "
					+ "ITEMS "
					+ "FROM TRADE_NODES WHERE "
					+ "ID = " + dbEnt
					+ ";");

			if(q.next()) {
				binaryStream = new DataInputStream(new FastByteArrayInputStream(q.getBytes(1)));
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
		return binaryStream;
	}

	public DataInputStream switchTradePrices() {
		DataInputStream binaryStream = null;
		Statement s;
		try {

			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT "
					+ "ID, "
					+ "ITEMS "
					+ "FROM TRADE_NODES"
					+ ";");

			while(q.next()) {

				long id = q.getLong(1);

				System.err.println("[MIGRATION] SWITCHING TRADE PRICES OF TRADE ID: " + id);
				binaryStream = new DataInputStream(new FastByteArrayInputStream(q.getBytes(2)));
				TradePrices pc = ShoppingAddOn.deserializeTradePrices(binaryStream, true);

				pc = pc.switchBuyAndSell();

				PreparedStatement p = s.getConnection().prepareStatement(
						"UPDATE TRADE_NODES SET(ITEMS) = (CAST(? AS VARBINARY(" + itemSize + "))) WHERE ID = CAST(? AS BIGINT);");

				FastByteArrayOutputStream backing = new FastByteArrayOutputStream(10 * 1024);
				DataOutputStream b = new DataOutputStream(backing);
				try {
					ShoppingAddOn.serializeTradePrices(b, true, pc, id);
				} catch(IOException e) {
					throw new SQLException(e);
				}

				byte[] byteArray = Arrays.copyOf(backing.array, (int) backing.position());
				p.setBytes(1, byteArray);
				p.setLong(2, id);
				p.executeUpdate();
			}

			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		return binaryStream;
	}

	public long getTradeNodeCredits(long dbEnt) {
		long cr = -1;
		Statement s;
		try {
			s = c.createStatement();

			ResultSet q = s.executeQuery("SELECT "
					+ "CREDITS "
					+ "FROM TRADE_NODES WHERE "
					+ "ID = " + dbEnt
					+ ";");

			if(q.next()) {
				cr = q.getLong(1);
			}

			s.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}
		return cr;
	}

	public boolean isVisibileSystem(FogOfWarReceiver fogee, int x, int y, int z) throws SQLException {
		long cr = -1;
		boolean inTable = false;
		Statement s = null;
		try {
			s = c.createStatement();
			ResultSet q = s.executeQuery("SELECT "
					+ "ID "
					+ "FROM VISIBILITY WHERE "
					+ "ID = " + fogee.getFogOfWarId() + " AND "
					+ "X = " + x + " AND "
					+ "Y = " + y + " AND "
					+ "Z = " + z
					+ ";");

			return q.next();

		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			if(s != null) {
				s.close();
			}
		}
		return false;
	}

	@Deprecated
	public void createTradeNodeTable() throws SQLException {
		Statement s = c.createStatement();

		ResultSet r = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'TRADES';");
		if(r.next()) {
			s.execute("ALTER TABLE TRADES DROP CONSTRAINT ffATN;");
			s.execute("ALTER TABLE TRADES DROP CONSTRAINT ffBTN;");
		}
		s.execute("DROP TABLE TRADE_NODES if exists;");

		s.execute("CREATE CACHED TABLE TRADE_NODES(" +
				"BIGINT, " + //same as entity databaseID (unique)
				"SEC_X INT not null, " +
				"SEC_Y INT not null, " +
				"SEC_Z INT not null, " +
				"PLAYER VARCHAR(128) not null, " +
				"STATION_NAME VARCHAR(128) not null, " +
				"FACTION INT not null, " +
				"PERMISSION BIGINT DEFAULT " + TradeManager.PERM_ALL_BUT_ENEMY + " not null, " +
				"ITEMS VARBINARY(" + itemSize + ") not null, " + //5kb
				"VOLUME DOUBLE not null, " +
				"CAPACITY DOUBLE not null, " +
				"CREDITS BIGINT not null, " +
				"primary key (ID)" +
				");");

		s.execute("create index facIndex on TRADE_NODES(FACTION);");
		s.execute("create index playerIndex on TRADE_NODES(PLAYER);");
		s.execute("create index trSysCoordIndex on TRADE_NODES(SEC_X,SEC_Y,SEC_Z);");

		s.close();
	}

	@Override
	public void afterCreation(Statement s) {

	}
}
