package org.schema.game.common.data.player.dialog;

import java.util.Arrays;

public class AwnserContainer {
	public Object[] awnser;

	public AwnserContainer(Object[] awnser) {
		super();
		this.awnser = awnser;
	}

	@Override
	public String toString() {
		return "AwnserContainer [awnser=" + Arrays.toString(awnser) + "]";
	}
	
}
