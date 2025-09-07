package org.schema.common.util;

public interface LogInterface {
	public enum LogMode{
		FILE,
		STDERR
	}
	public enum LogLevel{
		FINE,
		NORMAL,
		ERROR, DEBUG,
	}
	public void log(String s, LogLevel lvl);
}
