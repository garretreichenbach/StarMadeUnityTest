package org.schema.game.common.data.physics.sweepandpruneaabb;


public class OverlappingSweepPair<E> {
	public SweepPoint<E> a;
	public SweepPoint<E> b;
	private int hash;

	public OverlappingSweepPair() {

	}

	public OverlappingSweepPair(SweepPoint<E> a, SweepPoint<E> b) {
		set(a, b);
	}

	public static int getHashCode(SweepPoint<?> a, SweepPoint<?> b) {
		//		if(a.hashCode() < b.hashCode()){
		return a.hashCode() + b.hashCode() * 100000;
		//		}else{
		//			return b.hashCode()+a.hashCode()*100000;
		//		}

	}

	public boolean contains(SweepPoint<E> what) {
		return a.equals(what) || b.equals(what);
	}

	public void set(SweepPoint<E> a, SweepPoint<E> b) {
		this.a = a;
		this.b = b;
		this.hash = getHashCode(a, b);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		OverlappingSweepPair<E> oth = (OverlappingSweepPair<E>) obj;
		boolean eq = hash == (oth.hash);
		//		assert(!eq || (((((OverlappingSweepPair)obj).a.equals(a) && ((OverlappingSweepPair)obj).b.equals(b))) || (((OverlappingSweepPair)obj).a.equals(b) && ((OverlappingSweepPair)obj).b.equals(a)))):"other: "+((OverlappingSweepPair)obj).a+", "+((OverlappingSweepPair)obj).b+"-> "+hash+" <-"+a+", "+b;
		return eq;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Pair(" + a + ", " + b + ")";
	}

	public String toFullString() {
		return "PairHash{[" + hash + "](" + a.toFullString() + ", " + b.toFullString() + ")}";
	}
}
