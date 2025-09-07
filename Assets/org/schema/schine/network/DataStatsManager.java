package org.schema.schine.network;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.objects.NetworkObject;

import javax.vecmath.Vector4f;
import java.util.Map.Entry;

public class DataStatsManager extends GUIObservable {

	private static final int BACK_LOG_ENTRIES = 300;
	private final DataStatsList sentData = new DataStatsList(new Vector4f(0.4f, 1.0f, 0.4f, 0.7f), this);
	private final ObjectArrayFIFOQueue<DataStatsEntry> sentDataToAdd = new ObjectArrayFIFOQueue<DataStatsEntry>();

	private final DataStatsList receivedData = new DataStatsList(new Vector4f(1, 0.4f, 0.4f, 0.7f), this);
	private final ObjectArrayFIFOQueue<DataStatsEntry> receivedDataToAdd = new ObjectArrayFIFOQueue<DataStatsEntry>();
	private long lastUpdate;

	public void snapshotUpload(
			Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> sentData) {
		sentDataToAdd.enqueue(new DataStatsEntry(System.currentTimeMillis(), getClone(sentData)));
	}

	private Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> getClone(Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> data) {
		Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> clone = new Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap>();

		for (Entry<Class<? extends NetworkObject>, Int2LongOpenHashMap> e : data.entrySet()) {
			clone.put(e.getKey(), new Int2LongOpenHashMap(e.getValue()));
			e.getValue().clear();
		}

		return clone;
	}

	public void snapshotDownload(
			Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> sentData) {
		receivedDataToAdd.enqueue(new DataStatsEntry(System.currentTimeMillis(), getClone(sentData)));
	}

	public void update() {
		if (System.currentTimeMillis() - lastUpdate > 1000) {
			while (!sentDataToAdd.isEmpty()) {
				sentData.add(0, sentDataToAdd.dequeue());
			}
			while (!receivedDataToAdd.isEmpty()) {
				receivedData.add(0, receivedDataToAdd.dequeue());
			}

			archiveOld();

			notifyObservers();
			lastUpdate = System.currentTimeMillis();
		}

	}

	private void archiveOld() {
		while (sentData.size() > BACK_LOG_ENTRIES) {
			sentData.remove(sentData.size() - 1);
		}
		while (receivedData.size() > BACK_LOG_ENTRIES) {
			receivedData.remove(receivedData.size() - 1);
		}
	}

	/**
	 * @return the sentData
	 */
	public DataStatsList getSentData() {
		return sentData;
	}

	/**
	 * @return the receivedData
	 */
	public DataStatsList getReceivedData() {
		return receivedData;
	}

	public void notifyGUI() {
		notifyObservers();
	}

}
