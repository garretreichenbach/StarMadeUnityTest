package org.schema.game.common.data.element;

public class SendableControlMod {
	public long from;
	public long to;
	public short controlledType;
	public boolean add;

	public SendableControlMod() {

	}

	public SendableControlMod(long from, long to, short controlledType,
	                          boolean add) {
		super();
		this.from = from;
		this.to = to;
		this.controlledType = controlledType;
		this.add = add;
	}

}
