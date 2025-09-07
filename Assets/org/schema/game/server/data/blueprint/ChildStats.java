package org.schema.game.server.data.blueprint;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ChildStats {
	/**
	 * used to make sure, no equal UIDs are in the chilren of that blueprint
	 */
	public final ObjectOpenHashSet<String> usedNamed = new ObjectOpenHashSet<String>();
	public final ObjectOpenHashSet<String> railUIDs = new ObjectOpenHashSet<String>();
	public int childCounter;
	public String rootName;
	
	public final boolean transientSpawn;
	public int addedNum;

	public ChildStats(boolean transientSpawn) {
		super();
		this.transientSpawn = transientSpawn;
	}
	
	
}
