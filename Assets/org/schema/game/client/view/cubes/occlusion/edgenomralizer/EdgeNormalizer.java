package org.schema.game.client.view.cubes.occlusion.edgenomralizer;

public class EdgeNormalizer {

	/**
	 * The idea here would be to calculate all the edges from each vertex of a block (possible including the diagonals)
	 * The for each edge that is part of of our own block:
	 *  * if there is a vertex of another edge from another block extending that vector -> share
	 *  * if there is a vertex of another block with less than 180° -> self-shadow
	 *  
	 *   
	 *   We can precalculate the edges of each block for each vertex including for special shapes, the steps would be:
	 *   
	 *   
	 *   * for each vertex, add the edges that have starting point on the same vertex
	 *   * for each of our own edges, check find edge on the same plane (possibly add normal to edges from side and algo), the we can just compare normals of edges
	 *   * if edge is extended by other edge -> share
	 *   * if angle is convex -> shadow
	 *   * else just normal light
	 * 
	 */
}
