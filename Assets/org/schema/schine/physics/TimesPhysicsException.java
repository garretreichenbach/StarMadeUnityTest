package org.schema.schine.physics;

import com.bulletphysics.collision.dispatch.CollisionObject;

public class TimesPhysicsException {
	public long started, duration;
	public CollisionObject a, b;

	public TimesPhysicsException(CollisionObject a, CollisionObject b,
	                             long started, long duration) {
		super();
		this.a = a;
		this.b = b;
		assert (a != b);
		this.started = started;
		this.duration = duration;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		TimesPhysicsException o = (TimesPhysicsException) arg0;
		return (o.a.equals(a) && o.b.equals(b)) || (o.b.equals(a) && o.a.equals(b));
	}

}
