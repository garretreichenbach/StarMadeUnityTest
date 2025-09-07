package org.schema.game.common.data.element;

import org.schema.game.common.data.SegmentPiece;

public class ElementDocking {
	public final SegmentPiece from;
	public final SegmentPiece to;
	public boolean publicDockingException;

	public ElementDocking(SegmentPiece from, SegmentPiece to, boolean publicDockingException) {
		super();
		this.from = from;
		this.to = to;
		this.publicDockingException = publicDockingException;
	}

	public boolean equalsDock(ElementDocking o) {
		return from.equals(o.from) && to.equals(o.to);
	}

	@Override
	public int hashCode() {
		return from.hashCode() + to.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return equalsDock((ElementDocking) o);
	}
}
