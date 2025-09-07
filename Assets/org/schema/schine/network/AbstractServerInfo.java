package org.schema.schine.network;

import java.util.Date;

import org.schema.common.util.Version;

public abstract class AbstractServerInfo {

	private boolean favorite;
	private boolean custom;
	/**
	 * @return the infoVersion
	 */
	public abstract byte getInfoVersion();

	/**
	 * @return the version
	 */
	public abstract Version getVersion();

	/**
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * @return the desc
	 */
	public abstract String getDesc();

	/**
	 * @return the startTime
	 */
	public abstract long getStartTime();

	/**
	 * @return the playerCount
	 */
	public abstract int getPlayerCount();

	/**
	 * @return the maxPlayers
	 */
	public abstract int getMaxPlayers();

	/**
	 * @return the ping
	 */
	public abstract long getPing();

	/**
	 * @return the host
	 */
	public abstract String getHost();

	/**
	 * @return the port
	 */
	public abstract int getPort();
	
	
	public abstract String getConnType();

	@Override
	public int hashCode() {
		return getHost().hashCode()*getPort();
	}
	@Override
	public boolean equals(Object obj) {
		return getHost().equals(((AbstractServerInfo)obj).getHost()) && getPort() == ((AbstractServerInfo)obj).getPort();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SERVER INFO FOR " + getHost() + ":" + getPort() + "(INFO VERSION: " + getInfoVersion() + ")\n");
		sb.append("Version: " + getVersion() + "\n");
		sb.append("Name: " + getName() + "\n");
		sb.append("Description: " + getDesc() + "\n");
		sb.append("Started: " + (new Date(getStartTime())) + "\n");
		sb.append("Players: " + getPlayerCount() + "/" + getMaxPlayers() + "\n");
		sb.append("Ping: " + getPing() + "\n");

		return sb.toString();
	}
	public boolean isFavorite(){
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}
	public void setCustom(boolean custom) {
		this.custom = custom;
	}
	
	public boolean isCustom(){
		return this.custom;
	}
	public abstract boolean isResponsive();
	
}
