package org.schema.game.client.view.gui.lagStats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.schema.game.common.data.world.RemoteSector;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.bytes.Byte2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LagDataStatsEntry {
	public final long time;
	public final ObjectArrayList<LagObject> entry;
	public final long volume;
	public boolean selected;

	public LagDataStatsEntry(
			long time,
			ObjectArrayList<LagObject> entry) {
		super();
		this.time = time;
		this.entry = entry;
		this.volume = getVolume(entry);
	}

	public static long calcVolume(Byte2LongOpenHashMap d) {
		long v = 0;

		for (long b : d.values()) {
			v += b;
		}

		return v;
	}

	private long getVolume(
			ObjectArrayList<LagObject> ents) {

		long v = 0;
		for (LagObject e : ents) {
			if(e.s instanceof RemoteSector){
				v += e.getLagTime();
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
		return ((LagDataStatsEntry) obj).time == time;
	}

	public String save(boolean sent) {
		File dir = new FileExt("./savedNetworkStatistics");
		dir.mkdir();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		File f = new FileExt("./savedNetworkStatistics/statistics_" + time + "_" + dateFormat.format(time) + (sent ? "_SENT" : "_RECEIVED") + ".txt");
		BufferedWriter b = null;
		try {
			b = new BufferedWriter(new FileWriter(f));

			for (LagObject e : entry) {
				String name = e.getName();
				b.write(name);
				int i = name.length();
				while (i < 95) {
					b.write("-");
					i++;
				}
				b.write(" ");
				b.write(e.getSector());
				b.write(" ");
				b.write(e.getType());
				b.write(" ");
				b.write(String.valueOf(e.getLagTime()));

				b.newLine();
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
