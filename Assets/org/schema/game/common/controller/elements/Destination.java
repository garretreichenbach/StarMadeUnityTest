package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;

public class Destination {
	public String uid = "none";
	public Vector3i local = new Vector3i();
	
	
	@Override
	public int hashCode() {
		return uid.hashCode() + local.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		Destination o = (Destination)obj;
		return o.uid.equals(uid) && o.local.equals(local);
	}
}
