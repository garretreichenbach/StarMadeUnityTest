//package org.schema.game.client.controller;
//
//import org.schema.schine.resource.FileExt; import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//import com.mysql.management.MysqldResource;
//import com.mysql.management.MysqldResourceI;
//
//public class SQLServer {
//	public static final String DRIVER = "com.mysql.jdbc.Driver";
//
//    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
//
//    public static void main(String[] args) throws Exception {
//        File ourAppDir = new FileExt(System.getProperty(JAVA_IO_TMPDIR)+"./schemadb");
//        File databaseDir = new FileExt(ourAppDir, "mysql-mxj");
//        int port = Integer.parseInt(System.getProperty("c-mxj_test_port",  "3336"));
//        String userName = "schema";
//        String password = "schema";
//        System.out.println("Database Dir: "+databaseDir.getAbsolutePath());
//
//        MysqldResource mysqldResource = startDatabase(databaseDir, port,
//                userName, password);
//
//    }
//
//    public static MysqldResource startDatabase(File databaseDir, int port,
//            String userName, String password) {
//        MysqldResource mysqldResource = new MysqldResource(databaseDir);
//
//        Map database_options = new HashMap();
//        database_options.put(MysqldResourceI.PORT, Integer.toString(port));
//        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
//        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, userName);
//        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, password);
//
//        mysqldResource.start("mysqld-thread", database_options);
//
//        if (!mysqldResource.isRunning()) {
//            throw new RuntimeException("MySQL did not start.");
//        }
//
//        System.err.println("MySQL is running.");
//
//        return mysqldResource;
//    }
//
//}
