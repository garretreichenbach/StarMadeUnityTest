package org.schema.game.server.data;

public class ProtectedUplinkName implements Comparable<ProtectedUplinkName> {
	public final String uplinkname;
	public final String playername;
	public final long timeProtected;

	public ProtectedUplinkName(String uplink, String playername, long timeProtected) {
		super();
		this.uplinkname = uplink;
		this.timeProtected = timeProtected;
		this.playername = playername;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
				return uplinkname.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
				return uplinkname.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProtectedUplinkName [username=" + uplinkname + ", timeProtected="
				+ timeProtected + "]";
	}

	@Override
	public int compareTo(ProtectedUplinkName o) {
		return (int) (timeProtected - o.timeProtected);
	}

}
