package org.schema.game.client.data.gamemap;

import api.listener.events.draw.GameMapClientUpdateEntriesEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.gamemap.entry.MapEntryInterface;

public abstract class GameMap {
	public static final int STATUS_UNLOADED = 0;
	public static final int STATUS_LOADING = 1;
	public static final int STATUS_LOADED = 2;
	public static final byte TYPE_REGION = 1;
	public static final byte TYPE_SYSTEM = 2;
	public static final byte TYPE_SECTOR = 3;
	public static final byte TYPE_SYSTEM_SIMPLE = 4;
	protected MapEntryInterface parent;
	protected int loadStatus;
	private Vector3i pos;
	private MapEntryInterface[] entries;

	/**
	 * @return the entries
	 */
	public MapEntryInterface[] getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(MapEntryInterface[] entries) {
		this.entries = entries;
	}

	/**
	 * @return the pos
	 */
	public Vector3i getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(Vector3i pos) {
		this.pos = pos;
	}

	public void update(MapEntryInterface[] data) {
		entries = data;
		//INSERTED CODE
		GameMapClientUpdateEntriesEvent event = new GameMapClientUpdateEntriesEvent(this, data);
		StarLoader.fireEvent(event, false);
		//Update entry list
		this.entries = event.getEntryArray().toArray(new MapEntryInterface[0]);
		///
	}

}
