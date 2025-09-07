package org.schema.game.common.data.element;

import com.bulletphysics.collision.dispatch.CollisionObject;

public class ActivationTrigger {
	private final short type;
	public long pos;
	public CollisionObject obj;
	public boolean fired;
	public long ping;

	public ActivationTrigger(long pos, CollisionObject obj, short type) {
		super();
		this.pos = pos;
		this.obj = obj;
		this.type = type;
		ping();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (obj.hashCode() * pos );
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		ActivationTrigger a = ((ActivationTrigger) other);
		//		System.err.println("WHAT: "+other+"; "+a.obj+"; "+(obj == a.obj));
		return obj == a.obj && pos == (a.pos);// && colUnitPos == (a.colUnitPos);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ActivationTrigger [pos=" + pos + ", obj=" + obj + ", fired="
				+ fired + ", ping=" + ping + "]";
	}

	public void ping() {
		ping = System.currentTimeMillis();
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

}
