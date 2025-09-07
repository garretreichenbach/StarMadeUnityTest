package org.schema.game.client.view.gui.faction.newfaction;

public abstract class FactionPointStat {

	public String name;
	public String description;
	public FactionPointStat(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public abstract String getValue();

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	// #RM1863 fixed flawed implementation of .equals()
	@Override
	public boolean equals(Object obj) {
		return obj instanceof FactionPointStat && name.equals(((FactionPointStat) obj).name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getValue();
	}

}
