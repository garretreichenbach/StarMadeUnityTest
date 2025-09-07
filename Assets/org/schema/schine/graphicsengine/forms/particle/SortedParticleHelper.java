package org.schema.schine.graphicsengine.forms.particle;

import javax.vecmath.Vector3f;

public class SortedParticleHelper implements Comparable<SortedParticleHelper> {

	private final Vector3f from;
	private final Vector3f to;
	Vector3f fHelp = new Vector3f();
	Vector3f fHelpo = new Vector3f();

	public SortedParticleHelper(Vector3f from, Vector3f to) {
		super();
		this.from = from;
		this.to = to;
	}

	@Override
	public int compareTo(SortedParticleHelper o) {
		fHelp.sub(from, to);
		fHelpo.sub(o.from, o.to);
		return (int) (fHelp.length() * 10000000f - fHelpo.length() * 10000000f);
	}

	public Vector3f getFrom() {
		return from;
	}

	public Vector3f getTo() {
		return to;
	}
}
