package org.schema.game.common.staremote.gui.connection;

public class StarmoteConnection {
	public String username;
	public String url;
	public int port;

	public StarmoteConnection(String host, int port, String loginName) {
		this.url = host;
		this.port = port;
		this.username = loginName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return username.hashCode() + url.hashCode() + port;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return username.equals(((StarmoteConnection) o).username) && url.equals(((StarmoteConnection) o).url) && port == (((StarmoteConnection) o).port);
	}

	@Override
	public String toString() {
		return username + "@" + url + ":" + port;
	}

}
