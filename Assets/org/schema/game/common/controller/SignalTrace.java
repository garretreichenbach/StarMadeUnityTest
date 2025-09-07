package org.schema.game.common.controller;

public class SignalTrace {
	public long pos;
	public long a;
	public SignalTrace parent;
	public long rootPos = Long.MIN_VALUE;
	
	public void set(long pos, long a, SignalTrace parent) {
		this.a = a;
		this.pos = pos;
		this.parent = parent;
		if (parent != null) {
			rootPos = parent.rootPos;
		}else {
			rootPos = pos; //initial
		}
	}
	public void reset() {
		pos = 0;
		a = 0;
		rootPos = Long.MIN_VALUE;
		parent = null;
	}

	public boolean checkLoop() {
		long p = pos;
		SignalTrace cp = this.parent;
		while (cp != null) {
			if (cp.pos == p) {
				return true;
			}
			cp = cp.parent;
		}
		return false;
	}
}
