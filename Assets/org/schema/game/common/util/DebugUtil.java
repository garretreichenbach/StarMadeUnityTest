package org.schema.game.common.util;

/**
 * Utils for debugging and logging.
 *
 * @author TheDerpGamer
 */
public class DebugUtil {

	public static boolean loggingEnabled = true;

	public static void printStackTrace() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for(StackTraceElement element : stackTrace) System.out.println(element.toString());
	}

	public static void logDebug(String message) {
		if(loggingEnabled) System.out.println("[DEBUG]" + message);
	}
}
