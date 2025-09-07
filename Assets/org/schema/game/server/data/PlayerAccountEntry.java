package org.schema.game.server.data;

import java.util.Locale;

public class PlayerAccountEntry {
	
	public final long validUntil;
	public final String value;
	
	
	public PlayerAccountEntry(String value) {
		this(-1, value);
	}
	public PlayerAccountEntry(long validUntil, String value) {
		super();
		this.validUntil = validUntil;
		this.value = value;
	}
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof String){
			return value.equals(obj);
		}
		return value.equals(((PlayerAccountEntry)obj).value);
	}
	
	public boolean isValid(long currentTime){
		return validUntil <= 0 || currentTime < validUntil; 
	}
	public String fileLineName() {
		return "nmt:" +validUntil+":"+value.trim().toLowerCase(Locale.ENGLISH);
	}
	public String fileLineIP() {
		return "ipt:" +validUntil+":"+ value.trim();
	}
	public String fileLineAccount() {
		return "act:" +validUntil+":"+ value.trim();
	}
	@Override
	public String toString() {
		return value;
	}
	
	
	
}
