package org.schema.game.common.data.explosion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class ExplosionRayInfo extends ShortArrayList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean unique;

	public ExplosionRayInfo() {
		super();
	}

	public ExplosionRayInfo(short size) {
		super(size);
	}

	public static void writeNull(DataOutputStream sphereOPF) throws IOException {
		sphereOPF.writeShort(0);
	}
	public static ExplosionRayInfo read(DataInputStream s) throws IOException {

		short size = s.readShort();
		if (size > 0) {
			ExplosionRayInfo c = new ExplosionRayInfo(size);
			for (int i = 0; i < size; i++) {
				c.add(s.readShort());
			}
			//			System.err.println("HIGH: "+highest);
			return c;
		} else {
			return null;
		}

	}

	public void addRay(int currentRay) {
		assert (currentRay <= Short.MAX_VALUE && currentRay >= Short.MIN_VALUE);
		if(!contains((short)currentRay)){
			add((short) currentRay);
		}
	}

	public void write(DataOutputStream sphereOPF) throws IOException {
		sphereOPF.writeShort(size());
		for (short c : this) {
			sphereOPF.writeShort(c);
		}
	}

}
