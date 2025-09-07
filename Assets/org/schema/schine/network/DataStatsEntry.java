package org.schema.schine.network;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.common.util.StringTools;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.resource.FileExt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;

public class DataStatsEntry {
	public final long time;
	public final Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> entry;
	public final long volume;
	public boolean selected;

	public DataStatsEntry(
			long time,
			Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> entry) {
		super();
		this.time = time;
		this.entry = entry;
		this.volume = getVolume(entry);
	}

	public static long calcVolume(Int2LongOpenHashMap d) {
		long v = 0;

		for (long b : d.values()) {
			v += b;
		}

		return v;
	}

	private long getVolume(
			Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2LongOpenHashMap> ents) {

		long v = 0;
		for (Entry<Class<? extends NetworkObject>, Int2LongOpenHashMap> e : ents.entrySet()) {
			for (Long a : e.getValue().values()) {
				v += a;
			}
		}

		return v;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (time ^ (time >>> 32));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((DataStatsEntry) obj).time == time;
	}

	public String save(boolean sent) {
		File dir = new FileExt("./savedNetworkStatistics");
		dir.mkdir();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		File f = new FileExt("./savedNetworkStatistics/statistics_" + time + "_" + dateFormat.format(time) + (sent ? "_SENT" : "_RECEIVED") + ".txt");
		BufferedWriter b = null;
		try {
			b = new BufferedWriter(new FileWriter(f));

			for (Entry<Class<? extends NetworkObject>, Int2LongOpenHashMap> e : entry.entrySet()) {
				String name = e.getKey().toString();
				b.write(name);
				int i = name.length();
				while (i < 95) {
					b.write("-");
					i++;
				}
				b.write(" ");
				b.write(StringTools.readableFileSize(calcVolume(e.getValue())));

				String[] fieldNames = NetworkObject.getFieldNames(e.getKey());
				b.newLine();
				for (it.unimi.dsi.fastutil.ints.Int2LongMap.Entry w : e.getValue().int2LongEntrySet()) {
					String fieldName = "    " + fieldNames[w.getIntKey()];
					b.write(fieldName);
					int j = fieldName.length();
					while (j < 96) {
						b.write(" ");
						j++;
					}
					b.write(StringTools.readableFileSize(w.getLongValue()));
					b.newLine();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (b != null) {
				try {
					b.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return f.getAbsolutePath();

	}

}
