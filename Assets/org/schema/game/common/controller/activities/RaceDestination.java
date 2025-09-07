package org.schema.game.common.controller.activities;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.Destination;

public class RaceDestination extends Destination{
	public Vector3i sector = new Vector3i();
	public String uid_full;

	@Override
	public int hashCode() {
		return super.hashCode()+sector.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		RaceDestination o = (RaceDestination)obj;
		return o.sector.equals(sector) && super.equals(o);
	}

	@Override
	public String toString() {
		return "RaceDestination [sector=" + sector + ", uid=" + uid
				+ ", local=" + local + "]";
	}
	
	
}
