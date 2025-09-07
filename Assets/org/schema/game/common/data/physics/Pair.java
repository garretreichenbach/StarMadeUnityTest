package org.schema.game.common.data.physics;

public class Pair<T> {
	public T a;
	public T b;

	public Pair(T a, T b) {
		super();
		this.a = a;
		this.b = b;
	}

	public boolean contains(T what) {
		return a.equals(what) || b.equals(what);
	}

	public void set(T a, T b) {
		this.a = a;
		this.b = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		Pair<T> oth = (Pair<T>) obj;
		return a.equals(oth.a) && b.equals(oth.b);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Pair(" + a + ", " + b + ")";
	}

}
