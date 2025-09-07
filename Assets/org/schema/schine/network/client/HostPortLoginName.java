package org.schema.schine.network.client;

public class HostPortLoginName {
	public static final byte STARMADE_CLIENT = 0;
	public static final byte STAR_MOTE = 1;
	public String host;
	public int port;
	public String loginName;
	public byte userAgent;

	public HostPortLoginName(String host, int port, byte userAgent, String loginName) {
		super();
		this.host = host;
		this.port = port;
		this.loginName = loginName;
		this.userAgent = userAgent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return host.hashCode() + port;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return host.equals(((HostPortLoginName) obj).host) && port == (((HostPortLoginName) obj).port);
	}

	@Override
	public String toString() {
		return host + ":" + port;
	}

}
