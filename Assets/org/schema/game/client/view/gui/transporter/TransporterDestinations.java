package org.schema.game.client.view.gui.transporter;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

public class TransporterDestinations {
	public SegmentController target;
	public Vector3i pos;
	public String name;
	@Override
	public int hashCode() {
		return target.getId() * pos.hashCode() + name.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		TransporterDestinations o = (TransporterDestinations)obj;
		return o.target == target && pos.equals(o.pos) && name.equals(o.name);
	}
	
	
	
}
