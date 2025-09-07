package org.schema.game.common.updater;

import org.schema.game.common.updater.Updater.VersionFile;

public class IndexFileEntry implements Comparable<IndexFileEntry> {

	public final String path;
	public final String version;
	public final String build;
	public final VersionFile v;

	public IndexFileEntry(String path, String version, String build,
	                      VersionFile v) {
		super();
		this.path = path;
		this.version = version;
		this.build = build;
		this.v = v;
	}

	@Override
	public int compareTo(IndexFileEntry arg0) {
		return build.compareToIgnoreCase(arg0.build);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return build + " v" + version + " (" + v.name() + ")";
	}

}