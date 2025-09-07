package org.schema.game.common.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Statistics {

	private static final int MAX_BACKLOG = 1000;
	private final ObjectArrayList<Object2ObjectOpenHashMap<String, StaticEntry>> stats = new ObjectArrayList<Object2ObjectOpenHashMap<String, StaticEntry>>();
	private Object2ObjectOpenHashMap<String, StaticEntry> current;

	public void newFrame() {
		current = new Object2ObjectOpenHashMap<String, StaticEntry>();
		stats.add(current);
		while (stats.size() > MAX_BACKLOG) {
			stats.remove(0);
		}
	}

	public StaticEntry addNew(String name) {
		StaticEntry s = new StaticEntry(name, System.currentTimeMillis());
		current.put(name, s);
		return s;
	}

	private class StaticEntry {
		private final String id;
		private final long started;
		private final Object2ObjectOpenHashMap<String, Statistics> subMap = new Object2ObjectOpenHashMap<String, Statistics>();

		public StaticEntry(String id, long started) {
			super();
			this.id = id;
			this.started = started;
		}

		@Override
		public String toString() {
			return "[" + id + " = " + (System.currentTimeMillis() - started) + (subMap.size() > 0 ? ("; " + subMap) : "") + "]";
		}
	}
}
