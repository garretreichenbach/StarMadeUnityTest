//package org.schema.game.client.controller;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.Properties;
//
//import org.apache.derby.impl.jdbc.EmbedResultSet40;
//import org.schema.common.util.data.DataUtil;
//import org.schema.schine.graphicsengine.core.ResourceException;
//import org.schema.schine.resource.ResourceLoader;
//
//import com.mysql.management.util.QueryUtil;
//
//public class SQLConnection {
//    private Connection connection = null;
//
//
//	public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
//
//	private String framework = "embedded";
//	public  static String DRIVER_SQLITE_EMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver";
//    public static final String PROTOCOL_DERBY = "jdbc:derby:";
//    public static final String PROTOCOL_MYSQL = "jdbc:mysql:";
//
//    public static final String PROTOCOL = PROTOCOL_DERBY;
//    public static final String DRIVER = DRIVER_SQLITE_EMBEDDED;
////	private QueryUtil queryUtil;
//
//    public void connect() throws SQLException{
//
////        String port = "3336";
//        String port = "";
//
//        try{
//        Class.forName(DRIVER);
//        System.err.println("for description");
//        System.setProperty("derby.system.durability", "test");
//        System.setProperty("derby.storage.pageSize", "65536");
//        System.setProperty("derby.storage.initialPages", "500");
//        Properties props = new Properties(); // connection properties
//        props.put("user", userName);
//        props.put("password", password);
//
//
//        String dbName = "schemadb";
////        String url = PROTOCOL+"//localhost:" + port + "/" + dbName //
////                + "?" + "createDatabaseIfNotExist=true"//
////                + "&server.initialize-user=true";
////        System.err.println("connecting to "+url);
////        setConnection(DriverManager.getConnection(url, userName, password));
//        System.err.println("connection successful");
//        setConnection(DriverManager.getConnection(PROTOCOL + dbName
//                + ";create=true", props));
//
//
//        }catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//     // We want to control transactions manually. Autocommit is on by
//        // default in JDBC.
//        getConnection().setAutoCommit(false);
////        queryUtil = new QueryUtil(getConnection());
//    }
//    public void close() throws SQLException{
//    	 if (getConnection() != null) {
//             getConnection().close();
//         }
//    }
//
//    public static void main(String[] args) throws Exception {
//
//        SQLConnection sqlConnection = new SQLConnection();
//        sqlConnection.connect();
//
//        Class.forName(DRIVER);
//        System.err.println("for description");
//        try {
//            String sql = "SELECT VERSION()";
//            String queryForString = new QueryUtil(sqlConnection.getConnection()).queryForString(sql);
//
//            System.out.println("------------------------");
//            System.out.println(sql);
//            System.out.println("------------------------");
//            System.out.println(queryForString);
//            System.out.println("------------------------");
//
//            System.out.println("dropping schema");
//            sqlConnection.executeSQLFile("schema-drop.sql");
//            System.out.println("creating schema");
//            sqlConnection.executeSQLFile("schema-creation.sql");
//            System.out.println("indicing schema");
//            sqlConnection.executeSQLFile("schema-index.sql");
//
//            System.out.println("------------------------");
//            System.out.flush();
//
//
//
//            Thread.sleep(100); // wait for System.out to finish flush
//        } finally {
//            try {
//               sqlConnection.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//    public void executeSQLFile(String path) throws ResourceException, IOException{
//    	System.err.println("EXECUTING SQL FILE "+path);
//    	BufferedReader br = new BufferedReader(new InputStreamReader(
//				ResourceLoader.resourceUtil
//				.getResourceAsInputStream(DataUtil.dataPath + "sql/"+path)));
//    	StringBuffer query = new StringBuffer();
//    	String line = null;
//    	while((line = br.readLine()) != null){
//    		if(line.startsWith("--")){
//    			//skip comments
//    		}else{
//    			query.append(line);
//    			query.append("\n");
//    		}
//
//
//    	}
//    	System.out.println("start execution of query: "+query.toString());
////		queryUtil.execute(query.toString());
//		try {
//			getConnection().createStatement().execute(query.toString());
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//    }
//
//    public void executeUpdate(String query){
//    	try {
//			PreparedStatement prepareStatement = getConnection().prepareStatement(query);
//			prepareStatement.executeUpdate();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//    }
//    /**
//	 * Facts I learned...
//	 * HashMap makes absolutely not guarantees about the iteration order. It can (and will) even change completely when new elements are added.
//     * TreeMap will iterate according to the "natural ordering" of the keys according to their compareTo() method (or an externally supplied Comparator). Additionally, it implements the SortedMap interface, which contains methods that depend on this sort order.
//     * LinkedHashMap will iterate in the order in which the entries were put into the map
//
//	 */
//    public EmbedResultSet40 executeQuery(String query){
//    	try {
//			return (EmbedResultSet40)getConnection().createStatement().executeQuery(query.toString());
//		} catch (SQLException e) {
//
//			e.printStackTrace();
//		}
//		return null;
//    }
//	/**
//	 * @param connection the connection to set
//	 */
//	public void setConnection(Connection connection) {
//		this.connection = connection;
//	}
//	/**
//	 * @return the connection
//	 */
//	public Connection getConnection() {
//		return connection;
//	}
//
//}
