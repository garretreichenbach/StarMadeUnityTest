package org.schema.common;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class TimeStatistics {
	public static Object2LongOpenHashMap<String> timer = new Object2LongOpenHashMap<String>();

	public static long get(String string) {
		long l = timer.getLong(string);
		return l;
	}

	public static void reset(String name) {
		timer.put(name, System.currentTimeMillis());
	}

	public static void set(String name) {
		timer.put(name, (System.currentTimeMillis() - timer.getLong(name)));
	}
}
