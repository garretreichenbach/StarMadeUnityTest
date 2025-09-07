package org.schema.game.common.api;

public class FileInfo {

	public String uri;
	public String fid;

	public String name;
	public String uid;
	public int size;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileInfo [uri=" + uri + ", fid=" + fid + "]";
	}

}
