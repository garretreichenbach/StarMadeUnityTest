package org.schema.schine.network.common;

public class ServerAdress {
	public String host;
	public int port;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ServerAdress) && this.host.equals(((ServerAdress) obj).host) && this.port == ((ServerAdress) obj).port;
	}
	
	public String toString() {
		return "[ServerAdress: "+host+":"+port+"]";
	}
}
