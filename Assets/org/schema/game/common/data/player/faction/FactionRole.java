package org.schema.game.common.data.player.faction;

import org.schema.game.common.data.player.faction.FactionPermission.PermType;

public class FactionRole {
	public final int index;
	public long role;
	public String name;

	public FactionRole(long role, String name, int index) {
		super();
		this.role = role;
		this.name = name;
		this.index = index;
	}

	public boolean hasPermission(PermType permType) {
		return (role & permType.value) == permType.value;
	}

	public void setPermission(PermType permType, boolean active) {
		if (active) {
			role = (role | permType.value);
		} else {
			role = (role & ~permType.value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	// #RM1863 altered to avoid potential exception if obj is not a FactionRole object
	@Override
	public boolean equals(Object obj) {
		return obj instanceof FactionRole && index == ((FactionRole) obj).index;
	}
	public String getRoleString() {
		StringBuffer b = new StringBuffer();
		PermType[] values = PermType.values();
		for(int i = 0; i < values.length; i++){
			PermType t = values[i];
			if(hasPermission(t)) {
				b.append("+"+t.name());
			}else {
				b.append("-"+t.name());
			}
			if(i < values.length-1) {
				b.append(", ");
			}
		}
		return b.toString();
	}
	@Override
	public String toString() {
		return "FactionRole [index=" + index + ", role=" + getRoleString() + ", name=" + name + "]";
	}

}
