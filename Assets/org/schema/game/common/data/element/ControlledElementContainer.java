package org.schema.game.common.data.element;

/*
 * used for delayed updates, because it's possible that the sendableSegmentController.onControllerAdded
 * could call get point unsave, which would result in a deadlock, if, add/remove control (original caller)
 * would be called itself by the handle received method (already being in the network lock)
 */
public class ControlledElementContainer {
	public long from;
	public long to;
	public short controlledType;
	public boolean add = false;
	public boolean send;

	public ControlledElementContainer(long from, long to,
	                                  short toType, boolean add, boolean send) {
		super();
		this.from = from;
		this.to = to;
		this.controlledType = toType;
		this.add = add;
		this.send = send;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (from + to * 10000000000L);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((ControlledElementContainer) obj).add == add && ((ControlledElementContainer) obj).from == (from) &&
				((ControlledElementContainer) obj).to == (to) && ((ControlledElementContainer) obj).controlledType == controlledType;
	}

	@Override
	public String toString() {
		return "ControlledElementContainer[" + from + " -> " + to + "; type: " + controlledType + "; ADD: " +add+"]";
	}
}
