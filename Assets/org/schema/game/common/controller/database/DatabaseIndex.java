package org.schema.game.common.controller.database;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.PercentCallbackInterface;
import org.schema.game.common.controller.database.tables.EntityTable;
import org.schema.game.common.controller.database.tables.SectorTable;
import org.schema.game.common.controller.database.tables.SystemTable;
import org.schema.game.common.controller.database.tables.TableManager;
import org.schema.game.common.controller.generator.AsteroidCreatorThread;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseIndex {

	private static final Object dbPath = new Object() {
		@Override
		public String toString() {
			return GameServerState.ENTITY_DATABASE_PATH + "index" + File.separator;
		}

	};
	private static final HashMap<String, String> sqlTokens;
	public static final int UID_LENGTH = 128;
	private static final Pattern sqlTokenPattern;

	static {
		//MySQL escape sequences: http://dev.mysql.com/doc/refman/5.1/en/string-syntax.html
		String[][] search_regex_replacement = {
				//search string     search regex        sql replacement regex
				{"\u0000", "\\x00", "\\\\0"},
				{"'", "'", "\\\\'"},
				{"\"", "\"", "\\\\\""},
				{"\b", "\\x08", "\\\\b"},
				{"\n", "\\n", "\\\\n"},
				{"\r", "\\r", "\\\\r"},
				{"\t", "\\t", "\\\\t"},
				{"\u001A", "\\x1A", "\\\\Z"},
				{"\\", "\\\\", "\\\\\\\\"}
		};

		sqlTokens = new HashMap<String, String>();
		String patternStr = "";
		for(String[] srr : search_regex_replacement) {
			sqlTokens.put(srr[0], srr[2]);
			patternStr += (patternStr.isEmpty() ? "" : "|") + srr[1];
		}
		sqlTokenPattern = Pattern.compile('(' + patternStr + ')');
	}

	final Connection c;
	private final HashMap<String, String> sqlTokensBack;
	private final Pattern sqlTokenPatternBack;
	private final TableManager tableManager;

	{
		//MySQL escape sequences: http://dev.mysql.com/doc/refman/5.1/en/string-syntax.html
		String[][] search_regex_replacement = {
				//search string     search regex        sql replacement regex
				{"\u0000", "\\x00", "\\\\0"},
				{"'", "'", "\\\\'"},
				{"\"", "\"", "\\\\\""},
				{"\b", "\\x08", "\\\\b"},
				{"\n", "\\n", "\\\\n"},
				{"\r", "\\r", "\\\\r"},
				{"\t", "\\t", "\\\\t"},
				{"\u001A", "\\x1A", "\\\\Z"},
				{"\\", "\\\\", "\\\\\\\\"}
		};

		sqlTokensBack = new HashMap<String, String>();
		String patternStr = "";
		for(String[] srr : search_regex_replacement) {
			sqlTokens.put(srr[0], srr[2]);
			patternStr += (patternStr.isEmpty() ? "" : "|") + srr[1];
		}
		sqlTokenPatternBack = Pattern.compile('(' + patternStr + ')');
	}

	public Connection _getConnection() {
		return c;
	}

	public DatabaseIndex() throws SQLException {

		System.err.println("[SQL] Fetching connection (may take some if game crashed on the run before)");

		String maxNIOSize = "";
		if(FastMath.isPowerOfTwo(ServerConfig.SQL_NIO_FILE_SIZE.getInt())) {
			maxNIOSize = ";hsqldb.nio_max_size=" + ServerConfig.SQL_NIO_FILE_SIZE.getInt() + ";";
		} else {
			throw new SQLException("server.cfg: SQL_NIO_FILE_SIZE must be power of two (256, 512, 1024,...), but is: " + ServerConfig.SQL_NIO_FILE_SIZE.getInt());
		}

		c = DriverManager.getConnection("jdbc:hsqldb:file:" + getDbPath() + ";shutdown=true" + maxNIOSize, "SA", "");
		System.err.println("[SQL] connection successfull");

		assert (!c.isClosed());
		assert (c.isValid(30));

		setMVCCMode();
		setDbNIOFileSizeControl(ServerConfig.SQL_NIO_FILE_SIZE.getInt());

		tableManager = new TableManager(c);
	}

	/**
	 * @return the dbPath
	 */
	public static String getDbPath() {
		return dbPath.toString();
	}

	public static boolean existsDB() {
		return (new FileExt(getDbPath())).exists();
	}

	public static void main(String[] asd) throws SQLException, IOException {
		GameServerState.readDatabasePosition(false);
		DatabaseIndex db = new DatabaseIndex();
		db.display("SELECT * FROM EFFECTS");
	}

	public static void registerDriver() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch(Exception e) {
			System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
		}

	}

	public static DefaultTableModel resultSetToTableModel(
			DefaultTableModel model, ResultSet row) throws SQLException {
		ResultSetMetaData meta = row.getMetaData();
		if(model == null)
			model = new DefaultTableModel();
		String[] cols = new String[meta.getColumnCount()];
		for(int i = 0; i < cols.length; ++i) {
			cols[i] = meta.getColumnLabel(i + 1);
		}

		model.setColumnIdentifiers(cols);

		while(row.next()) {
			Object[] data = new Object[cols.length];
			for(int i = 0; i < data.length; ++i) {
				data[i] = row.getObject(i + 1);
			}
			model.addRow(data);
		}
		return model;
	}

	public static void updateFromFile(File f, Statement s) throws IOException, SQLException {

		if(f.getName().startsWith(SimpleTransformableSendableObject.EntityType.SHOP.dbPrefix) || f.getName().startsWith(SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbPrefix)
				|| f.getName().startsWith(SimpleTransformableSendableObject.EntityType.ASTEROID_MANAGED.dbPrefix) || f.getName().startsWith(SimpleTransformableSendableObject.EntityType.ASTEROID.dbPrefix)
				|| f.getName().startsWith(SimpleTransformableSendableObject.EntityType.PLANET_SEGMENT.dbPrefix) || f.getName().startsWith(SimpleTransformableSendableObject.EntityType.SHIP.dbPrefix) || f.getName().startsWith(SimpleTransformableSendableObject.EntityType.GAS_PLANET.dbPrefix)) {
			DataInputStream is = new DataInputStream(new FileInputStream(f));
			Tag readFrom = Tag.readFrom(is, true, false);
			is.close();

			Tag tag;
			if(f.getName().startsWith(SimpleTransformableSendableObject.EntityType.SHOP.dbPrefix)) {
				tag = ((Tag[]) readFrom.getValue())[0];
			} else if(f.getName().startsWith(SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbPrefix) || f.getName().startsWith(SimpleTransformableSendableObject.EntityType.ASTEROID_MANAGED.dbPrefix) || f.getName().startsWith(SimpleTransformableSendableObject.EntityType.GAS_PLANET.dbPrefix)) {
				tag = ((Tag[]) readFrom.getValue())[1];
			} else if(f.getName().startsWith(SimpleTransformableSendableObject.EntityType.PLANET_SEGMENT.dbPrefix)) {
				System.err.println("PLANET TAGS: " + ((Tag[]) readFrom.getValue())[0].getValue() + "; " + ((Tag[]) readFrom.getValue())[1].getValue() + "; " + ((Tag[]) readFrom.getValue())[2].getValue());
				tag = ((Tag[]) readFrom.getValue())[2]; //planet core uid in the way
			} else {
				tag = readFrom;
			}
			int type = -1;

			for(SimpleTransformableSendableObject.EntityType t : SimpleTransformableSendableObject.EntityType.values()) {
				if(f.getName().startsWith(t.dbPrefix)) {
					type = t.dbTypeId;
					break;
				}
			}
			assert (type >= 0);

			System.err.println("PARSING: " + f.getName() + " -> " + type + "; tagVAL: " + tag.getValue());

			Tag[] segControllerValues = ((Tag[]) tag.getValue());

			Tag[] simpleTransformableValues = (Tag[]) segControllerValues[6].getValue();

			String uid = (String) segControllerValues[0].getValue();
			Vector3i sectorPos = (Vector3i) simpleTransformableValues[3].getValue();
			long seed;
			if(segControllerValues.length > 11 && segControllerValues[11].getType() == Tag.Type.LONG) {
				seed = (Long) segControllerValues[11].getValue();
			} else {
				seed = 0;
			}
			String lastModifier;
			if(segControllerValues.length > 10 && segControllerValues[10].getType() == Tag.Type.STRING) {
				lastModifier = (String) segControllerValues[10].getValue();
			} else {
				lastModifier = "";
			}
			if(lastModifier == null) {
				lastModifier = "";
			}
			if(lastModifier.startsWith("ENTITY_PLAYERSTATE_")) {
				lastModifier = lastModifier.substring("ENTITY_PLAYERSTATE_".length());
			}

			String spawner;
			if(segControllerValues.length > 9 && segControllerValues[9].getType() == Tag.Type.STRING) {
				spawner = (String) segControllerValues[9].getValue();
			} else {
				spawner = "";
			}
			if(spawner == null) {
				spawner = "";
			}
			if(spawner.startsWith("ENTITY_PLAYERSTATE_")) {
				spawner = spawner.substring("ENTITY_PLAYERSTATE_".length());
			}
			boolean touched = true;
			if(segControllerValues.length > 12 && segControllerValues[12].getValue() != null && segControllerValues[12].getType() == Tag.Type.BYTE) {
				touched = ((Byte) segControllerValues[12].getValue()) == 1;
			}
			String realName;
			if(segControllerValues[5].getValue() != null) {
				realName = (String) segControllerValues[5].getValue();
			} else {
				realName = "undef";
			}
			int faction = 0;

			if(simpleTransformableValues[4].getValue() != null && simpleTransformableValues[4].getType() == Tag.Type.INT) {
				faction = (Integer) simpleTransformableValues[4].getValue();
				System.err.println("PARSED FACTION " + faction);
			} else {
				System.err.println("COULD NOT PARSE FACTION " + simpleTransformableValues[4].getType().name());
			}

			Tag[] list = (Tag[]) simpleTransformableValues[1].getValue();
			float[] t = new float[list.length];
			for(int i = 0; i < list.length; i++) {
				t[i] = (Float) list[i].getValue();
			}

			Transform worldTransform = new Transform();
			worldTransform.setFromOpenGLMatrix(t);

			Vector3i min = ((Vector3i) segControllerValues[1].getValue());
			Vector3i max = ((Vector3i) segControllerValues[2].getValue());
			int creatorId = ((Integer) segControllerValues[8].getValue());

			EntityTable.updateOrInsertSegmentController(s, uid.split("_", 3)[2], sectorPos, type, seed,
					lastModifier, spawner, realName, touched, faction, worldTransform.origin, min, max,
					creatorId, -1, -1, false, false);

		}

	}

	public static void updateFromFileSector(File f, Statement s) throws IOException, SQLException {

		if(f.getName().startsWith("SECTOR")) {
			DataInputStream is = new DataInputStream(new FileInputStream(f));
			Tag tag = Tag.readFrom(is, true, false);
			is.close();

			Vector3i pos;
			int protectionMode;
			Map<Integer, FreeItem> items = new HashMap<Integer, FreeItem>();

			Tag[] top = (Tag[]) tag.getValue();
			pos = (Vector3i) top[0].getValue();
			assert (pos != null);

			protectionMode = (Integer) top[2].getValue();

			Tag[] t = (Tag[]) top[3].getValue();

			for(int i = 0; i < t.length - 1; i++) {
				FreeItem it = new FreeItem();
				it.fromTagStructure(t[i], null);
				it.setId(GameServerState.itemIds);
				GameServerState.itemIds++;
				if(it.getType() != Element.TYPE_NONE) {
					items.put(it.getId(), it);
				}
			}
			SectorTable.updateOrInsertSector(s.getConnection(), -1, pos, protectionMode, items, 0, -1L, false, 0L);
			System.err.println("[SQL] INSERTED SECTOR " + pos);
		}
	}

	public static String unscape(String s) {
		Matcher matcher = sqlTokenPattern.matcher(s);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, sqlTokens.get(matcher.group(1)));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static String escape(String s) {
		Matcher matcher = sqlTokenPattern.matcher(s);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, sqlTokens.get(matcher.group(1)));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static boolean isUsernameOk(String loginName) {
		if(loginName.length() <= 0) {
			return false;
		} else if(loginName.length() > 32) {
			return false;
		} else if(loginName.length() <= 2) {
			return false;
		} else if(loginName.matches("[_-]+")) {
			return false;
		} else {
			return loginName.matches("[a-zA-Z0-9_-]+");
		}
	}

	public void test(String query) throws SQLException {
		System.err.println("START Query: " + query);
		long t = System.currentTimeMillis();
		Statement s = c.createStatement();
		//		ResultSet r = s.executeQuery("SELECT * FROM ENTITIES");
		ResultSet r = s.executeQuery(query);
		s.close();

		long took = System.currentTimeMillis() - t;
		System.err.println("Query took " + took + ": " + query + "; ");
		int i = 0;
		while(r.next()) {
			i++;
			long id = r.getLong(1);
		}
		System.err.println("DONE Query took " + took + ": " + query + "; count#" + i);
	}

	public void checkDatabase() throws SQLException, IOException {
		if(!existsDB()) {
			createDatabase();
			fillCompleteDatabase();
		}

	}

	public void commit() throws SQLException {
		c.commit();
	}

	public void createDatabase() throws SQLException {

		tableManager.create();
//		createSystemsTable();
//		createSectorsTable();
//		createSectorItemTable();
//		createEntitiesTable();
//		createEntityEffectTable();
//		createPlayerMessageTable();
//		createPlayersTable();
//		createVisibilityTable();
//		createFTLTable();
//		createFleetsTable();
//		createTradeNodeTable();
//		createTradeTable();
//		createNPCStatsTable();

		File f = new FileExt(GameServerState.DATABASE_PATH + "version");
		BufferedWriter fw;
		try {
			fw = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8));
			fw.append(String.valueOf(GameServerState.DATABASE_VERSION));
			fw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void destroy() throws SQLException {
		Statement s = c.createStatement();
		s.execute("SHUTDOWN COMPACT;");
		s.close();
		c.close();
	}

	public void display(String query) throws SQLException {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery(query);
		s.close();

		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jFrame.setSize(1200, 700);
		jFrame.setContentPane(new JScrollPane(new JTable(resultSetToTableModel(new DefaultTableModel(), r))));
		SwingUtilities.invokeLater(() -> jFrame.setVisible(true));

		r.close();
	}

	public void fillCompleteDatabase() throws IOException, SQLException {
		fillCompleteDatabase(null);
	}

	public void fillCompleteDatabase(PercentCallbackInterface percentCallbackInterface) throws IOException, SQLException {
		File[] list = (new FileExt(GameServerState.DATABASE_PATH)).listFiles();
		Statement s = c.createStatement();
		int size = list.length;

		fillSystemsTable(list, s, percentCallbackInterface, size);
		fillSectorsTable(list, s, percentCallbackInterface, size);
		fillEntitiesTable(list, s, percentCallbackInterface, size);

		File[] delList = (new FileExt(GameServerState.DATABASE_PATH)).listFiles((dir, name) -> name.startsWith("SECTOR") || name.startsWith("VOIDSYSTEM"));
		for(int i = 0; i < delList.length; i++) {
			System.err.println("[MIGRATION] CLEANING UP DEPRECATED FILE: " + delList[i].getName());
			delList[i].delete();
		}
		s.close();
	}

	public void fillEntitiesTable(File[] list, Statement s, PercentCallbackInterface percentCallbackInterface, int size) throws IOException, SQLException {
		int i = 0;
		for(File f : list) {
			updateFromFile(f, s);
			if(percentCallbackInterface != null) {
				percentCallbackInterface.update((int) (((float) i / size) * 100) + "% " + " (" + f.getName() + ")");
			}
			i++;
		}
	}

	public void fillSectorsTable(File[] list, Statement s, PercentCallbackInterface percentCallbackInterface, int size) throws IOException, SQLException {
		int i = 0;
		for(File f : list) {
			updateFromFileSector(f, s);
			if(percentCallbackInterface != null) {
				percentCallbackInterface.update((int) (((float) i / size) * 100) + "% " + " (" + f.getName() + ")");
			}
			i++;
		}
	}

	public void fillSystemsTable(File[] list, Statement s, PercentCallbackInterface percentCallbackInterface, int size) throws IOException, SQLException {
		int i = 0;
		for(File f : list) {
			SystemTable.updateFromFileStarSystem(f, s);
			if(percentCallbackInterface != null) {
				percentCallbackInterface.update((int) (((float) i / size) * 100) + "% " + " (" + f.getName() + ")");
			}
			i++;
		}
	}

	public void migrateUIDFieldSize() throws SQLException {
		{
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT CHARACTER_MAXIMUM_LENGTH " + //character_maximum_length
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'ENTITIES' " +
					"AND COLUMN_NAME = 'UID'");
			if(r.next()) {
				int size = r.getInt(1);
//				System.err.println("[DATABASE] ENTITIES UID SIZE: " + size + "");
				if(size == 64) {
					s.executeUpdate("ALTER TABLE 'ENTITIES' ALTER COLUMN 'UID' SET DATA TYPE VARCHAR(128)");
				}
			} else {
				assert (false);
			}
			s.close();
		}

		{
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT CHARACTER_MAXIMUM_LENGTH " + //character_maximum_length
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'FTL' " +
					"AND COLUMN_NAME = 'FROM_UID'");
			if(r.next()) {
				int size = r.getInt(1);
//				System.err.println("[DATABASE] FTL UID SIZE: " + size + "");
				if(size == 64) {
					s.executeUpdate("ALTER TABLE 'FTL' ALTER COLUMN 'FROM_UID' SET DATA TYPE VARCHAR(128)");
					s.executeUpdate("ALTER TABLE 'FTL' ALTER COLUMN 'TO_UID' SET DATA TYPE VARCHAR(128)");
				}
			} else {
				assert (false);
			}
			s.close();
		}
		{
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT CHARACTER_MAXIMUM_LENGTH " + //character_maximum_length
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'SYSTEMS' " +
					"AND COLUMN_NAME = 'OWNER_UID'");
			if(r.next()) {
				int size = r.getInt(1);
//				System.err.println("[DATABASE] SYSTEMS UID SIZE: " + size + "");
				if(size == 64) {
					s.executeUpdate("ALTER TABLE 'SYSTEMS' ALTER COLUMN 'OWNER_UID' SET DATA TYPE VARCHAR(128)");
				}
			} else {
				assert (false);
			}
			s.close();
		}

	}

	public void migrateAddSectorAsteroidsTouched() throws SQLException {

		boolean neededMigration = false;
		{
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'SECTORS' " +
					"AND COLUMN_NAME = 'TRANSIENT'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: TRANSIENT (this can take a little) [DO NOT KILL THE PROCESS]");
				s.executeUpdate("ALTER TABLE SECTORS ADD COLUMN TRANSIENT BOOLEAN DEFAULT true not null");

				s.executeUpdate("DELETE FROM ENTITIES WHERE TYPE = 3 AND TOUCHED = FALSE;");
			}

			s.close();
		}
		{
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'SECTORS' " +
					"AND COLUMN_NAME = 'LAST_REPLENISHED'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: LAST_REPLENISHED (this can take a little) [DO NOT KILL THE PROCESS]");
				s.executeUpdate("ALTER TABLE SECTORS ADD COLUMN LAST_REPLENISHED BIGINT DEFAULT 0 not null");
			}

			s.close();
		}

	}

	public void migrateAddFields() throws SQLException {
		Statement s = c.createStatement();
		DatabaseMetaData metaData = c.getMetaData();

		{
			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'ENTITIES' " +
					"AND COLUMN_NAME = 'GEN_ID'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: GEN_ID");

				s.executeUpdate("ALTER TABLE ENTITIES ADD COLUMN GEN_ID INT");

				ResultSet res = s.executeQuery("SELECT UID FROM ENTITIES WHERE TYPE = 3 AND TOUCHED = FALSE;");
				Random rand = new Random();
				while(res.next()) {
					s.executeUpdate("UPDATE ENTITIES SET GEN_ID = " + rand.nextInt(AsteroidCreatorThread.AsteroidTypeOld.values().length) + " WHERE TYPE = 3 AND UID = '" + res.getString(1) + "';");
				}
			}
		}
		{

			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'SYSTEMS' " +
					"AND COLUMN_NAME = 'OWNER_UID'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: SYSTEMS Owner colums");

				s.executeUpdate("ALTER TABLE SYSTEMS ADD COLUMN OWNER_UID VARCHAR(128)");

				s.executeUpdate("ALTER TABLE SYSTEMS ADD COLUMN OWNER_FACTION INT DEFAULT 0 not null;");
				s.executeUpdate("ALTER TABLE SYSTEMS ADD COLUMN OWNER_X INT DEFAULT 0 not null;");
				s.executeUpdate("ALTER TABLE SYSTEMS ADD COLUMN OWNER_Y INT DEFAULT 0 not null;");
				s.executeUpdate("ALTER TABLE SYSTEMS ADD COLUMN OWNER_Z INT DEFAULT 0 not null;");

				s.execute("create index sysOwnFacIndex on SYSTEMS(OWNER_FACTION);");
				s.execute("create index sysOwnUIDIndex on SYSTEMS(OWNER_UID);");

			}
		}
		{

			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'ENTITIES' " +
					"AND COLUMN_NAME = 'DOCKED_ROOT'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: ENTITIES :: DOCKED_ROOT");

				s.executeUpdate("ALTER TABLE ENTITIES ADD COLUMN DOCKED_ROOT BIGINT DEFAULT -1;");

				s.execute("create index dockedRootIndex on ENTITIES(DOCKED_ROOT);");

			}
		}
		{

			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'ENTITIES' " +
					"AND COLUMN_NAME = 'DOCKED_TO'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: ENTITIES :: DOCKED_TO");

				s.executeUpdate("ALTER TABLE ENTITIES ADD COLUMN DOCKED_TO BIGINT DEFAULT -1;");

				s.execute("create index dockedToIndex on ENTITIES(DOCKED_TO);");

			}
		}
		{

			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'ENTITIES' " +
					"AND COLUMN_NAME = 'SPAWNED_ONLY_IN_DB'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: ENTITIES :: SPAWNED_ONLY_IN_DB");

				s.executeUpdate("ALTER TABLE ENTITIES ADD COLUMN SPAWNED_ONLY_IN_DB BOOLEAN DEFAULT FALSE;");
			}
		}
		{

			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'FLEET_MEMBERS' " +
					"AND COLUMN_NAME = 'FACTION'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: FLEET_MEMBERS :: FACTION");

				s.executeUpdate("ALTER TABLE FLEET_MEMBERS ADD COLUMN FACTION INT DEFAULT 0;");

				s.execute("create index ddEA on FLEET_MEMBERS(FACTION);");

			}
		}
		{
			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'ENTITIES' " +
					"AND COLUMN_NAME = 'ID'");

			if(!r.next()) {
				System.err.println("[SQL] Database migration needed: ID");

				s.executeUpdate("ALTER TABLE ENTITIES DROP PRIMARY KEY");
				s.executeUpdate("ALTER TABLE ENTITIES ADD COLUMN ID BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1)");

				ResultSet res = s.executeQuery("SELECT UID,TYPE FROM ENTITIES;");
				long id = 0;
				while(res.next()) {
					s.executeUpdate("UPDATE ENTITIES SET ID = " + id + " WHERE TYPE = " + res.getByte(2) + " AND UID = '" + res.getString(1) + "';");
					id++;
				}
				s.executeUpdate("ALTER TABLE ENTITIES ALTER COLUMN ID RESTART WITH " + id + ";");

				s.executeUpdate("ALTER TABLE ENTITIES ADD PRIMARY KEY(ID);");

				s.execute("create unique index uidType on ENTITIES(UID,TYPE);");

			}

		}

		s.close();

	}

	public void migrateSectorsAndSystems(PercentCallbackInterface percentCallbackInterface) throws SQLException, IOException {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'SYSTEMS';");
		if(!r.next()) {
			tableManager.getSystemTable().createTable();
			tableManager.getSectorTable().createTable(); //createSectorsTable();
			tableManager.getSectorItemTable().createTable(); //createSectorItemTable();
			File[] list = (new FileExt(GameServerState.DATABASE_PATH)).listFiles();

			int size = list.length;
			fillSystemsTable(list, s, percentCallbackInterface, size);
			fillSectorsTable(list, s, percentCallbackInterface, size);

			File[] delList = (new FileExt(GameServerState.DATABASE_PATH)).listFiles((dir, name) -> name.startsWith("SECTOR") || name.startsWith("VOIDSYSTEM"));
			for(int i = 0; i < delList.length; i++) {
				System.err.println("[MIGRATION] CLEANING UP DEPRECATED FILE: " + delList[i].getName());
				delList[i].delete();
			}
		}
	}

	public void migrateFleets(PercentCallbackInterface percentCallbackInterface) throws SQLException, IOException {
		{
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("SELECT * " +
					"FROM information_schema.COLUMNS " +
					"WHERE " +
					"TABLE_NAME = 'FLEETS';");
			if(!r.next()) {
				tableManager.getFleetTable().createTable();//FleetsTable();
				tableManager.getFleetMemberTable().createTable();//FleetsTable();

			} else {
				{
					ResultSet rr = s.executeQuery("SELECT * " +
							"FROM information_schema.COLUMNS " +
							"WHERE " +
							"TABLE_NAME = 'FLEET_MEMBERS' " +
							"AND COLUMN_NAME = 'DOCKED_TO'");

					if(!rr.next()) {
						s.executeUpdate("ALTER TABLE FLEET_MEMBERS ADD COLUMN DOCKED_TO BIGINT DEFAULT -1 not null");
					}

					rr = s.executeQuery("SELECT * " +
							"FROM information_schema.COLUMNS " +
							"WHERE " +
							"TABLE_NAME = 'FLEETS' " +
							"AND COLUMN_NAME = 'COMMAND'");

					if(!rr.next()) {
						s.executeUpdate("ALTER TABLE FLEETS ADD COLUMN COMMAND VARBINARY(1024)");
					}
				}
			}
			s.close();
		}

	}

	public void migrateTrade(PercentCallbackInterface percentCallbackInterface) throws SQLException, IOException {
		Statement s = c.createStatement();

		ResultSet rCheck = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'TRADE_NODES' AND COLUMN_NAME = 'SYS_X';");
		if(rCheck.next()) {
			s.execute("DROP TABLE TRADE_NODES if exists;");
			s.execute("DROP TABLE TRADE_HISTORY if exists");
		}
		ResultSet r = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'TRADE_HISTORY';");

		s.execute("DROP TABLE TRADES if exists;");

		if(!r.next()) {
			tableManager.getTradeNodeTable();//.createTradeNodeTable();
			tableManager.getTradeHistoryTable().createTable();//createTradeTable();
		}

		s.close();
	}

	public void migrateEffects(PercentCallbackInterface percentCallbackInterface) throws SQLException, IOException {
		Statement s = c.createStatement();

		ResultSet rCheck = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'EFFECTS';");
		if(!rCheck.next()) {
			tableManager.getEntityEffectTable().createTable();//createEntityEffectTable();
		}

		s.close();
	}

	public void migrateVisibilityAndPlayers(PercentCallbackInterface percentCallbackInterface) throws SQLException, IOException {
		Statement s = c.createStatement();

		ResultSet rCheck = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'VISIBILITY';");
		if(!rCheck.next()) {
			s.execute("DROP TABLE VISIBILITY if exists;");
			s.execute("DROP TABLE PLAYERS if exists");

			tableManager.getVisibilityTable().createTable();//createPlayersTable();
			tableManager.getVisibilityTable().createTable();//createVisibilityTable();
		}

		s.close();
	}

	public void migrateNPCFactionStats() throws SQLException, IOException {
		Statement s = c.createStatement();

		ResultSet rCheck = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'NPC_STATS';");
		if(!rCheck.next()) {

			//switch trade prices on existing worlds
			System.err.println("[DATABASE] switch trade prices");
			tableManager.getTradeNodeTable().switchTradePrices();

			tableManager.getNpcStatTable().createTable();
//			createNPCStatsTable();
		}

		s.close();
	}

	public void migrateMessageSystem(PercentCallbackInterface percentCallbackInterface) throws SQLException, IOException {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery("SELECT * " +
				"FROM information_schema.COLUMNS " +
				"WHERE " +
				"TABLE_NAME = 'PLAYER_MESSAGES';");
		if(!r.next()) {
			tableManager.getPlayerMessagesTable().createTable();//createPlayerMessageTable();

		}
		s.close();
	}

	public List<DatabaseEntry> resultToList(ResultSet r) throws SQLException {
		long t = System.currentTimeMillis();
		ObjectArrayList<DatabaseEntry> l = new ObjectArrayList<DatabaseEntry>();
		if(r.next()) {
			do {
				// Logic to retrieve the data from the resultset.
				l.add(new DatabaseEntry(r));
			} while(r.next());
		} else {

			// No data
		}

		if(!r.isClosed()) {
			r.close();
		}
		if((System.currentTimeMillis() - t) > 10) {
			System.err.println("[SQL] SECTOR QUERY LIST CONVERSION TOOK " + (System.currentTimeMillis() - t) + "ms; list size: " + l.size() + ";");
		}
		return l;
	}

	public void setAutoCommit(boolean b) throws SQLException {
		c.setAutoCommit(b);
	}

	private void setDbTransactionControl(String mode) {
		//	    Connection connection = null;
		Statement s = null;
		try {
			//	        connection = JdbcUtils.openConnection(ds);
			s = c.createStatement();
			s.execute("SET DATABASE TRANSACTION CONTROL " + mode);
		} catch(SQLException e) {
			//log it
			e.printStackTrace();
			//	        JdbcUtils.closeConnection(connection);
		} finally {
			try {
				if(s != null) {
					s.close();
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
			//	        JdbcUtils.closeConnection(connection);
		}
	}

	private void setDbNIOFileSizeControl(int megabytes) {

		if(!FastMath.isPowerOfTwo(megabytes)) {
			throw new IllegalArgumentException("DATABASE NIO FILE SIZE MUST BE POWER OF TWO, BUT WAS " + megabytes);
		}
		//	    Connection connection = null;
		Statement s = null;
		try {
			//	        connection = JdbcUtils.openConnection(ds);
			s = c.createStatement();
			s.execute("SET FILES NIO SIZE " + megabytes);
		} catch(SQLException e) {
			//log it
			e.printStackTrace();
			//	        JdbcUtils.closeConnection(connection);
		} finally {
			try {
				if(s != null) {
					s.close();
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
			//	        JdbcUtils.closeConnection(connection);
		}
	}

	public void setLockingMode() {
		setDbTransactionControl("LOCKS");

	}

	public void setMVCCMode() {
		setDbTransactionControl("MVCC");
	}

	public void updateFromFile(File f) throws IOException, SQLException {
		Statement s = c.createStatement();
		updateFromFile(f, s);
		s.close();
	}

	public static void resultSetToStringBuffer(
			StringBuffer model, ResultSet row) throws SQLException {
		ResultSetMetaData meta = row.getMetaData();
		String[] cols = new String[meta.getColumnCount()];
		for(int i = 0; i < cols.length; ++i) {
			cols[i] = meta.getColumnLabel(i + 1);
		}
		for(int i = 0; i < cols.length; i++) {
			model.append("\"" + cols[i] + "\"");
			if(i < cols.length - 1) {
				model.append(";");
			}
		}
		model.append("\n");
		while(row.next()) {
			Object[] data = new Object[cols.length];
			for(int i = 0; i < data.length; ++i) {
				data[i] = row.getObject(i + 1);
			}
			for(int i = 0; i < data.length; i++) {
				model.append("\"" + data[i] + "\"");
				if(i < data.length - 1) {
					model.append(";");
				}
			}
			model.append("\n");
		}
	}

	public void adminSql(String query, boolean update, boolean returnKeys,
	                     GameServerState state, RegisteredClientInterface client, StringBuffer out) {

		Statement s = null;
		try {
			s = c.createStatement();

			if(!update) {
				ResultSet executeQuery = s.executeQuery(query);

				resultSetToStringBuffer(out, executeQuery);
			} else {
				if(!returnKeys) {
					s.executeUpdate(query);
				} else {
					int executeUpdate = s.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

					ResultSet generatedKeys = s.getGeneratedKeys();

					resultSetToStringBuffer(out, generatedKeys);

				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch(Exception e) {
				e.printStackTrace();
				out.append(e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}

	public TableManager getTableManager() {
		return tableManager;
	}
}