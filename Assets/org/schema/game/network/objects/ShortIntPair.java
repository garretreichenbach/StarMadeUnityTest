package org.schema.game.network.objects;

public class ShortIntPair {
	public long pos;
	public short type;
	public int count;

	public ShortIntPair() {
	}

	public ShortIntPair(long pos, short type, int count) {
		super();
		this.pos = pos;
		this.type = type;
		this.count = count;
	}

	public ShortIntPair(ShortIntPair rA) {
		this.pos = rA.pos;
		this.type = rA.type;
		this.count = rA.count;
	}

	;

}
