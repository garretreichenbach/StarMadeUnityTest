package org.schema.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * A <code>SimpleFormatter</code> formats log records into
 * short human-readable messages, typically one or two lines.
 *
 * @author Sascha Brawer (brawer@acm.org)
 */
public class CustomFormatter
		extends Formatter {

	/**
	 * The character sequence that is used to separate lines in the
	 * generated stream. Somewhat surprisingly, the Sun J2SE 1.4
	 * reference implementation always uses UNIX line endings, even on
	 * platforms that have different line ending conventions (i.e.,
	 * DOS). The GNU implementation does not replicate this bug.
	 *
	 * @see Sun bug parade, bug #4462871,
	 * "java.util.logging.SimpleFormatter uses hard-coded line separator".
	 */
	static final String lineSep = System.getProperty("line.separator");
	StringBuffer buf = new StringBuffer(180);
	/**
	 * An instance of a DateFormatter that is used for formatting
	 * the time of a log record into a human-readable string,
	 * according to the rules of the current locale.  The value
	 * is set after the first invocation of format, since it is
	 * common that a JVM will instantiate a SimpleFormatter without
	 * ever using it.
	 */
	private DateFormat dateFormat;
	//  StringBuffer buf = new StringBuffer(180);

	/**
	 * Formats a log record into a String.
	 *
	 * @param record the log record to be formatted.
	 * @return a short human-readable message, typically one or two
	 * lines.  Lines are separated using the default platform line
	 * separator.
	 * @throws NullPointerException if <code>record</code>
	 *                              is <code>null</code>.
	 */
	@Override
	public String format(LogRecord record) {
		StringBuffer buf = new StringBuffer(180);
		//		buf.delete(0, buf.length()-1);
		//	 buf.delete(0, buf.length()-1);

		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		}
		buf.append('[');
		buf.append(dateFormat.format(new Date(record.getMillis())));
		//		buf.append(record.getSourceClassName());
		//		buf.append(record.getSourceMethodName());
		buf.append(']');
		buf.append(' ');
		String msg = formatMessage(record);
		if (msg.startsWith("[ERR]") || msg.startsWith("[OUT]")) {
			msg = msg.substring(5);
		}
		buf.append(msg);
		//		buf.append(formatMessage(record).replaceFirst("\\]", "] "));

		buf.append(lineSep);

		Throwable throwable = record.getThrown();
		if (throwable != null) {
			StringWriter sink = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sink, true));
			buf.append(sink.toString());
		}

		return buf.toString();
	}

}