package org.schema.game.common.data.physics;

public interface SegmentTraversalInterface<E> {

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param traverser
	 * @return continue true if the traversal shall continue. false, if we stop here
	 */
	public boolean handle(int x, int y, int z, RayTraceGridTraverser traverser);

	public E getContextObj();

}
