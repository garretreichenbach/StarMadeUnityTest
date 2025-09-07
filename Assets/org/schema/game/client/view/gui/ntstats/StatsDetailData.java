package org.schema.game.client.view.gui.ntstats;

import it.unimi.dsi.fastutil.bytes.Byte2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

import org.schema.schine.network.DataStatsEntry;
import org.schema.schine.network.objects.NetworkObject;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

public class StatsDetailData {
	public final Class<? extends NetworkObject> ntObjClass;
	public final Int2LongOpenHashMap data;
	public final long volume;

	public StatsDetailData(Class<? extends NetworkObject> ntObjClass,
			Int2LongOpenHashMap data) {
		super();
		this.ntObjClass = ntObjClass;
		this.data = data;
		this.volume = DataStatsEntry.calcVolume(data);
	}

}
