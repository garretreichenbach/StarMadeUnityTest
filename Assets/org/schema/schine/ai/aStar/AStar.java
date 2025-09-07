/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>AStar</H2>
 * <H3>org.schema.schine.ai.aStar</H3>
 * AStar.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.ai.aStar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.schine.network.Identifiable;

// TODO: Auto-generated Javadoc

/**
 * Pathfinder Class. AStar is a common algorithm to find the best path with the
 * help of heuristics.
 * <p/>
 * A* (pronounced "A star") is a graph/tree search algorithm that finds a path
 * from a given initial node to a given goal node (or one passing a given goal
 * test). It employs a "heuristic estimate" h(x) that ranks each node x by an
 * estimate of the best route that goes through that node. It visits the nodes
 * in order of this heuristic estimate. The A* algorithm is therefore an example
 * of best-first search. The algorithm was first described in 1968 by Peter
 * Hart, Nils Nilsson, and Bertram Raphael. In their paper, it was called
 * algorithm A; using this algorithm with an appropriate heuristic yields
 * optimal behavior, hence A*. (Wikipedia description)
 * <p/>
 * Consider the problem of route finding, for which A* is commonly used. A*
 * incrementally builds all routes leading from the starting point until it
 * finds one that reaches the goal. But, like all informed search algorithms, it
 * only builds routes that appear to lead towards the goal. To know which routes
 * will likely lead to the goal, A* employs a heuristic estimation of the
 * distance from any given point to the goal. In the case of route finding, this
 * may be the straight-line distance, which is usually an approximation of road
 * distance. What sets A* apart from best-first search is that it also takes the
 * distance already travelled into account. This makes A* complete and optimal,
 * i.e., A* will always find the shortest route if any exists. It is not
 * guaranteed to perform better than simpler search algorithms. In a maze-like
 * environment, the only way to reach the goal might be to first travel one way
 * (away from the goal) and eventually turn around. In this case trying nodes
 * closer to your destination first may cost you time.
 *
 * @author schema
 */
public class AStar extends Thread {

	/**
	 * The Constant WITH_OCCUPATION.
	 */
	public static final int WITH_OCCUPATION = 0;
	/**
	 * The Constant NO_OCCUPATION.
	 */
	public static final int NO_OCCUPATION = 1;
	/**
	 * The Constant FLY_PATH.
	 */
	public static final int FLY_PATH = 2;
	/**
	 * The Constant serialVersionUID.
	 */
	
	/**
	 * The with occupied.
	 */
	public boolean withOccupied = false;
	/**
	 * The nodes.
	 */
	private ANode[][] nodes;
	/**
	 * The map.
	 */
	private Map map;
	/**
	 * The open list.
	 */
	private Heap openList;
	/**
	 * The closed list.
	 */
	private HashSet<ANode> closedList;
	/**
	 * The turns made.
	 */
	private int turnsMade;
	/**
	 * The entity.
	 */
	private Identifiable entity;
	/**
	 * The used.
	 */
	private HashSet<ANode> used;
	/**
	 * The current.
	 */
	private ANode current;

	/**
	 * Instantiates a new a star.
	 *
	 * @param map    the map
	 * @param entity the entity
	 */
	public AStar(Map map, Identifiable entity) {
		openList = new Heap(map.getWidth() * map.getHeight(), new ANodeComperator());
		closedList = new HashSet<ANode>();
		used = new HashSet<ANode>();
		this.map = map;
		this.entity = entity;
		this.nodes = map.getNodes();
	}

	/**
	 * Gets the direct path.
	 *
	 * @param startMapPos the start map pos
	 * @param endMapPos   the end map pos
	 * @return the direct path
	 */
	public static Path getDirectPath(Vector3f startMapPos, Vector3f endMapPos) {
		Path p = new Path();
		p.addSeg(new PathSegment(startMapPos, endMapPos, 1));
		return p;
	}

	/**
	 * Converts a Node and and recursivly its parents into a String.
	 *
	 * @param node the node
	 * @return String of the path
	 */
	public static LinkedList<int[]> pathToArray(ANode node) {
		ANode last = node;
		LinkedList<int[]> path = new LinkedList<int[]>();
		int i = 0;
		while (last.getParent() != null) {
			i++;
			path.addFirst(new int[]{last.getX(), last.getZ()});
			last = last.getParent();
		}
		path.addFirst(new int[]{last.getX(), last.getZ()});
		;
		return path;
	}

	/**
	 * Converts a Node and and recursivly its parents into an int[] ArrayList.
	 *
	 * @param node the node
	 * @return ArrayList of int[]
	 */
	public static Path pathToInt(ANode node) {
		ANode last = node;
		Path l = new Path();
		Vector3f end = new Vector3f(last.getX(), 0, last.getZ());
		while (last.getParent() != null) {
			end = new Vector3f(last.getX(), 0, last.getZ());
			last = last.getParent();
			Vector3f start = new Vector3f(last.getX(), 0, last.getZ());

			l.addSeg(new PathSegment(start, end, 1));
		}
		// l.add(new int[]{last.getX(),last.getZ()});
		return l;
	}

	/**
	 * Converts a Node and and recursivly its parents into a String.
	 *
	 * @param node the node
	 * @return String of the path
	 */
	public static String pathToString(ANode node) {
		String ret = "";
		ANode last = node;
		int i = 0;
		while (last.getParent() != null) {
			i++;
			ret += "[" + last.getX() + ", " + last.getZ() + "] <- ";
			last = last.getParent();
		}
		ret += "[" + last.getX() + ", " + last.getZ() + "] ";
		return "<" + i + "> " + ret;
	}

	/**
	 * Check direct path.
	 *
	 * @param startX the start x
	 * @param startY the start y
	 * @param desX   the des x
	 * @param desY   the des y
	 * @return the path
	 * @throws PathNotFoundException the path not found exception
	 */
	private Path checkDirectPath(int startX, int startY, int desX, int desY) throws PathNotFoundException {
		if (startX == desX && startY == desY) {
			throw new PathNotFoundException("path length zero");
		}
		System.err.println("[AStar] checking direct path from " + startX + ", " + startY + " to " + desX + ", " + desY);
		Vector3f start = new Vector3f(startX * Map.FS + Map.FS / 2, 0, startY * Map.FS + Map.FS / 2);
		Vector3f end = new Vector3f(desX * Map.FS + Map.FS / 2, 0, desY * Map.FS + Map.FS / 2);
		Vector3f dir = Vector3fTools.sub(end, start);
		float len = dir.length();
		dir.normalize();

		float c = 0;
		int size = Map.FS;//(int) Math.max(getEntity().getSize().x,getEntity().getSize().y);
		Field lastField = map.getField(startX, startY);
		while (c < len) {
			start.add(dir);
			int x = (int) Math.floor(start.x / Map.FS);
			int y = (int) Math.floor(start.z / Map.FS);
			if (!map.checkField(x, y)) {
				System.err.println("[AStar] no direct path. end of map " + x + ", " + y + " at len " + c + " of " + len);
				return null;
			}
			if (lastField != map.getField(x, y)) {
				boolean neighbor = false;
				for (int i = 0; i < map.getField(x, y).getNeighborCount(); i++) {
					if (map.getField(x, y).getNeighbors()[i] == lastField) {
						neighbor = true;
						break;
					}
				}
				if (!neighbor) {
					System.err.println("[AStar] no direct path. not a neightbor");
					return null;
				}
			}
			Field[] fs = map.getRectAroundField(x, y, size);
			for (Field f : fs) {
				if (!map.checkField(x, y) || f.getWeight() == 0 || f.isOccupied(entity)) {
					System.err.println("[AStar] no direct path. not walkable. size " + size);
					return null;
				}
			}
			lastField = map.getField(x, y);
			c++;
		}
		return getDirectPath(new Vector3f(startX, 0, startY), new Vector3f(desX, 0, desY));
	}

	/**
	 * Find common parent.
	 *
	 * @param start the start
	 * @param end   the end
	 * @return the a node
	 */
	private ANode findCommonParent(ANode start, ANode end) {
		ANode one = start.getParent();
		ANode two = end.getParent();
		ArrayList<ANode> loop = new ArrayList<ANode>();
		loop.add(start);
		loop.add(end);
		int j = 0;
		while (one != null) {
			while (two != null) {
				loop.add(two);
				for (int i = 0; i < two.getNeighborCount(); i++) {
					if (two.getNeighbors()[i] == one) {
						if (loop.size() > 10) {
							return one;
						}
						return null;
					}
				}
				if (one == two) {
					return null;
				}

				two = two.getParent();
			}
			j++;
			loop.add(one);
			one = one.getParent();
			two = end.getParent();
		}

		return null;
	}

	/**
	 * This method finds a path for ground units.
	 *
	 * @param startX the start x
	 * @param startY the start y
	 * @param desX   the des x
	 * @param desY   the des y
	 * @return an Arraylist of int Arrays than contain the path int[0] =
	 * X_coordinate int[1] = Y_coordinate
	 * @throws PathNotFoundException the path not found exception
	 */
	public Path findPath(int startX, int startY, int desX, int desY) throws PathNotFoundException {
		Path path = null;

		if (!used.isEmpty() || !openList.isEmpty() || !closedList.isEmpty()) {
			System.err
					.println("!! PATH: WARNING: OPEN OR CLOSED LIST NOT EMPTY");
			reset();
		}

		path = checkDirectPath(startX, startY, desX, desY);
		if (path != null) {
			//found direct route

			System.err.println("[AStart] found direct path");
			return path;
		}
		path = new Path();

		openList.insert(nodes[startY][startX]);
		used.add(nodes[startY][startX]);
		current = null;
		boolean success = false;
		if (openList.isEmpty()) {
			throw new NullPointerException("The Openlist is unexpected Empty");
		}
		//System.err.println("-- PATH: Now calculating A* Path from "+startX+", "
		// +startY+ " to "+desX+","+desY);
		while (!openList.isEmpty() && !success) {
			current = getBestNode();
			ANode g = flood(current, desX, desY);
			//			if (withOccupied) {
			//				getBestNoOccupation(current, desX, desY, true);
			//			} else {
			getBest(current, desX, desY);
			//			}
			// System.err.println("Path: "+pathToString(current));
			if (closedList.contains(nodes[desY][desX])) {
				success = true;
			}
			if (current.getX() == desX && current.getZ() == desY) {
				success = true;
			}
			turnsMade++;
			if (turnsMade % 3000 == 0) {
				System.err.println("-- PATH: turns Made: " + turnsMade
						+ "; OpenList Size: " + openList.count_);
			}
			//			try {
			//				Thread.sleep(60);
			//			} catch (InterruptedException e) {
			//				e.printStackTrace();
			//			}
		}
		if (success) {
			System.err.println("-> Path [" + startX + "," + startY + "] to ["
					+ desX + "," + desY + "](SUCCESS): "
					+ pathToString(current));
			path = pathToInt(current);
			return path;
		} else {
			System.err.println("!! PATH: ERROR current: " + current.getX()
					+ ", " + current.getZ());
			// System.err.println("!! Dest: "+desX+", "+desY+"-> Path: "+
			// pathToString(current));
			System.err.println("-> Path (!ERROR!): " + pathToString(current));

			throw new PathNotFoundException(startX, startY, desX, desY, path, closedList);

			// throw new NullPointerException();
			// return path;

		}
	}

	/**
	 * Flood.
	 *
	 * @param start the start
	 * @param desX  the des x
	 * @param desY  the des y
	 * @return the a node
	 * @throws PathNotFoundException the path not found exception
	 */
	private ANode flood(ANode start, int desX, int desY) throws PathNotFoundException {
		//make a star formed ray test. if intersections are %2 == 0 then its not en island
		//OR check if there is a loop in the closed list graph
		//OR keep track of the two shortest (discontinuous. check the side of the target)
		// skyscraper lines to the taget. should the lines be
		//makin a rectangle. the space inside should be added to the closed list

		for (int i = 0; i < start.getNeighborCount(); i++) {
			ANode n = start.getNeighbors()[i];
			if (n.getParent() != null && n.getParent() != start) {
				//has different root
				ANode loopConnectNode = findCommonParent(start, n);
				if (loopConnectNode != null) {
					int maxY = loopConnectNode.getZ() > start.getZ() ? loopConnectNode.getZ() : start.getZ();
					int maxX = loopConnectNode.getX() > start.getX() ? loopConnectNode.getX() : start.getX();
					int minY = loopConnectNode.getZ() < start.getZ() ? loopConnectNode.getZ() : start.getZ();
					int minX = loopConnectNode.getX() < start.getX() ? loopConnectNode.getX() : start.getX();

					if (desX > minX && desX < maxX && desY > minY && desY < maxY) {
						throw new PathNotFoundException("Target on an island!");
					}
				}
			}
		}
		return null;
		//		flooded.add(start);
		//
		//		for(int i = 0; i < start.getNeigborCount(); i++){
		//			if(!flooded.contains((start.getNeighbors()[i]))){
		//				flood(start.getNeighbors()[i], flooded);
		//			}
		//			try {
		//				Thread.sleep(30);
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//		}
	}

	/**
	 * extracts the best node.
	 *
	 * @param current the current
	 * @param desX    the des x
	 * @param desY    the des y
	 * @return the best
	 */
	private void getBest(ANode current, int desX, int desY) {

		for (int i = 0; i < current.getNeighborCount(); i++) {
			ANode neighbor = current.getNeighbors()[i];
			int moveCost = 10;
			if (neighbor.getX() != current.getX()
					&& neighbor.getZ() != current.getZ()) {
				moveCost = 15;
			}

			if (!closedList.contains(neighbor)) {

				//				System.err.println("finding best for "+unit);
				boolean occ = entity != null;

				occ = occ && neighbor.isOccupiedFor(entity);

				if (occ) {
					//					System.err.println("is occ "+neighbor);
					neighbor.setCloseReason(" occupied for " + entity + ": " + neighbor);
					//Field.isSizeOccupiedByRobotSize(neighbor.getX()*Map.FS+Map.FS/2, neighbor.getZ()*Map.FS+Map.FS/2, getRobot());
					closedList.add(neighbor);

				} else {
					if (!openList.contains(neighbor)) {
						// Search for the non-existent item
						neighbor.setParent(current); // make the current quare

						int costToGoal = neighbor.getTraverseCost(nodes[desY][desX]);
						int costToThis = current.getCostToThis() + moveCost + neighbor.getWeight();
						neighbor.setCostToThis(costToThis);
						neighbor.setCostToGoal(costToGoal);

						openList.insert(neighbor);
						if (neighbor.getZ() > 25 && neighbor.getZ() < 25) {
							System.err.println("added to openlist " + neighbor);
						}
						used.add(neighbor);
						//						System.err.println(used);
					} else {
						if (neighbor.getCostToGoal() + current.getCostToThis() < neighbor.getCostToGoal() + neighbor.getParent().getCostToThis()) {

							neighbor.setParent(current);

							int costToGoal = neighbor.getTraverseCost(nodes[desY][desX]);
							int costToThis = current.getCostToThis() + moveCost + neighbor.getWeight();
							neighbor.setCostToThis(costToThis);
							neighbor.setCostToGoal(costToGoal);

							//							neighbor.setCurrentPathCost(moveCost);
							//							neighbor.setCost(neighbor.getCurrentPathCost()
							//											+ neighbor.getTraverseCost(nodes[desY][desX]));
							openList.refactor(neighbor);
							used.add(neighbor);
							if (neighbor.getZ() > 25 && neighbor.getZ() < 28) {
								System.err.println("refactoring " + neighbor);
							}
						} else {
							if (neighbor.getZ() > 25 && neighbor.getZ() < 28) {
								System.err.println("neighbor was not best way " + neighbor);
							}
						}
					}
				}
			}

		}
	}

	/**
	 * one of the most important method of A* to extract the best Node from the
	 * Openlist. because the open list is organized in a priority Queue. the
	 * best Node is easily found.
	 *
	 * @return the node with the least (cost+heuristics)
	 */
	private ANode getBestNode() {
		if (openList.isEmpty()) {
			throw new NullPointerException(
					"Cannot extract from open list: already empty");
		}
		ANode n = (ANode) openList.extract();
		closedList.add(n);

		return n;
	}

	/**
	 * Gets the current.
	 *
	 * @return the current
	 */
	public ANode getCurrent() {
		return current;
	}

	/**
	 * Sets the current.
	 *
	 * @param current the current to set
	 */
	public void setCurrent(ANode current) {
		this.current = current;
	}

	/**
	 * Gets the entity.
	 *
	 * @return the entity
	 */
	public Identifiable getEntity() {
		return entity;
	}

	/**
	 * Sets the entity.
	 *
	 * @param entity the new entity
	 */
	public void setEntity(Identifiable entity) {
		this.entity = entity;
	}

	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public ANode[][] getNodes() {
		return nodes;
	}

	/**
	 * Sets the nodes.
	 *
	 * @param nodes the new nodes
	 */
	public void setNodes(ANode[][] nodes) {
		this.nodes = nodes;
	}

	/**
	 * resets the weight map to its state after initializing and increments the
	 * weights with path, that are currently on the map so a set of units dont
	 * use the same path.
	 */
	public void reset() {
		System.err.println("[AStar] last search took " + used.size() + " used nodes");
		for (ANode n : used) {
			n.setCostToGoal(map.getField(n.getX(), n.getZ()).getWeight());
			n.setParent(null);
		}
		resetLists();

	}

	/**
	 * clears the open- and closed Lists.
	 */
	private void resetLists() {
		openList.clear();
		closedList.clear();
		used.clear();
	}

}
