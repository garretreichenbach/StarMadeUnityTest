package org.schema.game.client.view.cubes.cubedyn;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class DrawMarker {
	public static final int VIRTUAL = 1;
	
	int id;
	Transform t = new Transform();
	IntArrayList start = new IntArrayList();
	IntArrayList count = new IntArrayList();
	public int optionBits;

	public void reset() {
		start.clear();
		count.clear();
		id = -1;
		optionBits = 0;
	}
}
