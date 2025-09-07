package org.schema.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An OutputStream that writes contents to a Logger upon each call to flush()
 */
class LoggingOutputStream extends ByteArrayOutputStream {

	private String lineSeparator;

	private Logger logger;
	private Level level;
	private PrintStream stdout;

	private String name;

	public boolean toErr;
	/**
	 * Constructor
	 *
	 * @param stdout
	 * @param logger      Logger to write to
	 * @param level       Level at which to write the log message
	 * @param description
	 */
	public LoggingOutputStream(PrintStream stdout, Logger logger, Level level, String name) {
		super();
		this.name = name;
		this.logger = logger;
		this.level = level;
		lineSeparator = System.getProperty("line.separator");
		this.stdout = stdout;
	}

	/* (non-Javadoc)
	 * @see java.io.ByteArrayOutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		super.close();
		for (Handler h : logger.getHandlers()) {
			h.close();
		}
	}

	/**
	 * upon flush() write the existing contents of the OutputStream
	 * to the logger as a log record.
	 *
	 * @throws java.io.IOException in case of error
	 */
	@Override
	public void flush() throws IOException {

		String record;
		synchronized (this) {
			super.flush();
			record = this.toString();
			super.reset();

			if (record.length() == 0 || record.equals(lineSeparator)) {
				// avoid empty records
				return;
			}

			logger.logp(level, "", "", "[" + name + "]" + record);
			if(toErr) {
				System.err.println(record);
			}
			if (this.stdout != null) {
				stdout.append(record);
				//stdout.append("\n");
				stdout.flush();
			}
		}
	}

}