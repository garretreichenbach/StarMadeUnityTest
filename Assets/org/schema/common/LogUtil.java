package org.schema.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.schema.schine.resource.FileExt;

public class LogUtil {

	private static PrintStream debugLogger;
	private static LoggingOutputStream losErr;
	private static LoggingOutputStream losOut;
	private static FileHandler fileHandler;
	private static Logger logger;
	private static Logger serverLog;
	private static Logger shipyardLog;
	private static boolean serverSetup;
	private static boolean shipyardSetup;
	private static int logFileCount = 20;

	//	private static LoggingOutputStream losDebug;
	public static void closeAll() throws IOException {
		if(losOut != null || fileHandler != null){
			try {
				throw new Exception("[LOGUTIL] shutdown logs");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (losOut != null) {
			losOut.close();
		}
		if (losErr != null) {
			losErr.close();
		}

		if (fileHandler != null) {
			if (logger != null) {
				logger.removeHandler(fileHandler);

			}
			fileHandler.close();
		}
	}

	public static PrintStream debug() {
		return debugLogger;
	}

	public static Logger log() {
		return serverLog;
	}
	public static Logger sy() {
		return shipyardLog;
	}

	public static void setUp(int logFileCount, CallInterace c) throws SecurityException, IOException {
		LogUtil.logFileCount = logFileCount;
		closeAll();
		// initialize logging to go to rolling log file
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();
		if (!serverSetup) {
			setUpServerLog();
		}
		if (!shipyardSetup) {
			setUpShipyardLog();
		}
		File f = new FileExt("./logs/");
		if (!f.exists()) {
			f.mkdirs();
		}
		c.call();
		// log file max size 10K, 3 rolling files, append-on-open
		fileHandler = new FileHandler("./logs/logstarmade.%g.log", 1024 * 1024 * 4, logFileCount);
		
		fileHandler.setFormatter(new CustomFormatter());

		// preserve old stdout/stderr streams in case they might be useful
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;

		// now rebind stdout/stderr to logger

		logger = Logger.getLogger("stdout");
		logger.addHandler(fileHandler);
		losOut = new LoggingOutputStream(stdout, logger, StdOutErrLevel.STDOUT, "OUT");
		System.setOut(new PrintStream(losOut, true));
		logManager.addLogger(logger);

		logger = Logger.getLogger("stderr");
		logger.addHandler(fileHandler);
		losErr = new LoggingOutputStream(stderr, logger, StdOutErrLevel.STDERR, "ERR");
		System.setErr(new PrintStream(losErr, true));
		logManager.addLogger(logger);

		//		setUpDebug();
	}

	public static void setUpServerLog() throws SecurityException, IOException {
		serverSetup = true;

		// initialize logging to go to rolling log file
		LogManager logManager = LogManager.getLogManager();
		File f = new FileExt("./logs/");
		if (!f.exists()) {
			f.mkdirs();
		}
		FileHandler fileHandler = new FileHandler("./logs/serverlog.%g.log", 1024 * 1024 * 2, logFileCount);
		fileHandler.setLevel(Level.ALL);

		CustomFormatter customFormatter = new CustomFormatter();
		fileHandler.setFormatter(customFormatter);

		serverLog = Logger.getLogger("server");
		serverLog.addHandler(fileHandler);
		serverLog.addHandler(new Handler() {
			
			@Override
			public void publish(LogRecord record) {
				System.err.println("[SERVERLOG] "+record.getMessage());
			}
			
			@Override
			public void flush() {
				
			}
			
			@Override
			public void close() throws SecurityException {
				
			}
		});
		logManager.addLogger(serverLog);
		serverLog.setLevel(Level.ALL);
	}
	public static void setUpShipyardLog() throws SecurityException, IOException {
		shipyardSetup = true;
		
		// initialize logging to go to rolling log file
		LogManager logManager = LogManager.getLogManager();
		File f = new FileExt("./logs/");
		if (!f.exists()) {
			f.mkdirs();
		}
		FileHandler fileHandler = new FileHandler("./logs/shipyardlog.%g.log", 1024 * 1024 * 2, logFileCount);
		fileHandler.setLevel(Level.ALL);
		
		fileHandler.setFormatter(new CustomFormatter());
		
		shipyardLog = Logger.getLogger("shipyard");
		shipyardLog.addHandler(fileHandler);
		logManager.addLogger(shipyardLog);
		shipyardLog.setLevel(Level.ALL);
		
		shipyardLog.fine("------------------------------- STARTING NEW LOG ----------------------------");
	}

	

}
