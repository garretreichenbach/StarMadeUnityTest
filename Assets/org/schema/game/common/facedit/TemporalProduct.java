package org.schema.game.common.facedit;

import java.util.ArrayList;

import org.schema.game.common.data.element.FactoryResource;

public class TemporalProduct implements Comparable<TemporalProduct> {
	public final ArrayList<FactoryResource> input = new ArrayList<FactoryResource>();
	public final ArrayList<FactoryResource> output = new ArrayList<FactoryResource>();
	public short factoryId;

	@Override
	public int compareTo(TemporalProduct o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public String toString() {
		return "" + input + "\n  -->  \n" + output;
	}
}
