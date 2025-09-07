package org.schema.game.server.data;

import java.util.Set;

import org.schema.game.server.data.admin.AdminCommands;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class Admin {

	public final String name;

	public final Set<AdminCommands> deniedCommands = new ObjectOpenHashSet<AdminCommands>();

	public Admin(String name) {
		super();
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return name.equals(obj.toString());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

}
