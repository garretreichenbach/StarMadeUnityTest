package org.schema.game.client.view;

import java.util.Observable;

public class DrawerUpdate {
	public Object arg1;
	Observable arg0;

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return arg0 == ((DrawerUpdate) obj).arg0 && arg1 == ((DrawerUpdate) obj).arg1;
	}

	public void set(Observable arg0, Object arg1) {
		this.arg0 = arg0;
		this.arg1 = arg1;
	}
}
