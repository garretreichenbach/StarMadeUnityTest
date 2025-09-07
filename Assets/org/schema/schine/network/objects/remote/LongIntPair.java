package org.schema.schine.network.objects.remote;

import org.schema.common.util.CompareTools;

public class LongIntPair implements Comparable<LongIntPair>{
	public long l;
	public int i;
	public LongIntPair() {}
	public LongIntPair(long l, int i) {
		this.l = l;
		this.i = i;
	}
	@Override
	public int compareTo(LongIntPair o) {
		return CompareTools.compare(l, o.l);
	}
}
