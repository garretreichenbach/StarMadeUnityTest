package org.schema.game.network.objects;

public class StringLongLongPair {
	public String playerName;
	public long timeStamp;
	public long size;

	public StringLongLongPair() {
	}

	public StringLongLongPair(String stringVal, long longVal, long longVal2) {
		super();
		this.playerName = stringVal;
		this.timeStamp = longVal;
		this.size = longVal2;
	}

	public StringLongLongPair(StringLongLongPair rA) {
		this.playerName = rA.playerName;
		this.timeStamp = rA.timeStamp;
		this.size = rA.size;
	}

	;

}
