/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ANode</H2>
 * <H3>org.schema.schine.ai.aStar</H3>
 * ANode.java
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


import org.schema.schine.network.Identifiable;

// TODO: Auto-generated Javadoc

/**
 * An Anode is a tile on the map with a cost and heuristics. it has also
 * neightbors and one Parent
 *
 * @author schema
 */
public abstract class ANode implements Node{

	/**
	 * The Constant serialVersionUID.
	 */
	

	/**
	 * The z.
	 */
	private int x, z;

	/**
	 * The cost to this.
	 */
	private int costToThis;

	/**
	 * The cost to goal.
	 */
	private int costToGoal;

	/**
	 * The weight.
	 */
	private int weight;

	/**
	 * The neighbors.
	 */
	private ANode[] neighbors = new ANode[8];

	/**
	 * The parent.
	 */
	private ANode parent;

	/**
	 * The close reason.
	 */
	private String closeReason; //debug

	/**
	 * The neighbor count.
	 */
	private int neighborCount = 0;

	/**
	 * Instantiates a new a node.
	 *
	 * @param x the x
	 * @param y the y
	 */
	public ANode(int x, int y) {
		setPos(x, y);
	}

	/**
	 * adds a neighbor to the map.
	 *
	 * @param node the node
	 */
	public void addNeighbor(ANode node) {
		getNeighbors()[neighborCount++] = node;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		ANode a = ((ANode) obj);
		return a.x == this.x && a.z == this.z;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + x + "," + z + " / " + (costToGoal + costToThis) + (closeReason != null ? "(" + closeReason + ")" : "") + "]";
	}

	/**
	 * Gets the close reason.
	 *
	 * @return the closeReason
	 */
	public String getCloseReason() {
		return closeReason;
	}

	/**
	 * Sets the close reason.
	 *
	 * @param closeReason the closeReason to set
	 */
	public void setCloseReason(String closeReason) {
		this.closeReason = closeReason;
	}

	/**
	 * the cost to move here.
	 *
	 * @return cost
	 */
	public int getCostToGoal() {
		return costToGoal;
	}

	/**
	 * sets the cost of this node + its weight.
	 *
	 * @param costToGoal the new cost to goal
	 */
	public void setCostToGoal(int costToGoal) {
		this.costToGoal = costToGoal;
	}

	/**
	 * Gets the cost to this.
	 *
	 * @return the costToThis
	 */
	public int getCostToThis() {
		return costToThis;
	}

	/**
	 * Sets the cost to this.
	 *
	 * @param costToThis the costToThis to set
	 */
	public void setCostToThis(int costToThis) {
		this.costToThis = costToThis;
	}

	/**
	 * Gets the neighbor count.
	 *
	 * @return the neighborCount
	 */
	public int getNeighborCount() {
		return neighborCount;
	}

	/**
	 * Sets the neighbor count.
	 *
	 * @param neighborCount the neighborCount to set
	 */
	public void setNeighborCount(int neighborCount) {
		this.neighborCount = neighborCount;
	}

	/**
	 * returns all neighbors of the Node on a rectangle shaped field this can be
	 * max 8.
	 *
	 * @return the neighbors
	 */
	@Override
	public ANode[] getNeighbors() {
		return neighbors;
	}

	/**
	 * calculates the heuristic from this node to dest node.
	 * <p/>
	 * this heuristics are calculateted with the skyscraper method
	 * <p/>
	 * Vertical Nodes to dest + Horizontal Nodes to dest multiplied with a
	 * faktor to attach the importance to the heuristics
	 *
	 * @param dest the dest
	 * @return the traverse cost
	 */
	@Override
	public int getTraverseCost(ANode dest) {
		int xDis = Math.abs(dest.x - x);
		int yDis = Math.abs(dest.z - z);
		return 10 * (xDis + yDis);
	}

	/**
	 * Sets the neighbors.
	 *
	 * @param neighbors the neighbors to set
	 */
	public void setNeighbors(ANode[] neighbors) {
		this.neighbors = neighbors;
	}

	/**
	 * parent node is the previous node of this node works like a linked List.
	 *
	 * @return the parent node
	 */
	public ANode getParent() {
		return parent;
	}

	/**
	 * sets the parent node e.g parent -> parent -> parent -> this
	 *
	 * @param parent the new parent
	 */
	public void setParent(ANode parent) {
		this.parent = parent;
	}

	/**
	 * the higher the weight the less this node will be chosen for next pathNode
	 * Weight less than 1 or bigger then 10 means its not walkable.
	 *
	 * @return weight of this node
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * sets the weight of this node. the higher the weight the less this node
	 * will be chosen for next pathNode Weight less than 1 or bigger then 10
	 * means its not walkable
	 *
	 * @param weight the new weight
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * x.
	 *
	 * @return the x coodinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * y.
	 *
	 * @return the y coodinate
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Checks if is occupied for.
	 *
	 * @param unit the unit
	 * @return true, if is occupied for
	 */
	public abstract boolean isOccupiedFor(Identifiable unit);

	/**
	 * removes a neighbor from the node.
	 *
	 * @param node the node
	 */
	public void removeNeighbor(ANode node) {
		//		neighbors.remove(node);
		for (int i = 0; i < neighborCount; i++) {
			if (getNeighbors()[i].equals(node)) {
				getNeighbors()[i] = getNeighbors()[neighborCount - 1];
				getNeighbors()[neighborCount - 1] = null;
				neighborCount = neighborCount - 1;
			}
		}
	}

	/**
	 * the nodes position on the map.
	 *
	 * @param x the x
	 * @param y the y
	 */
	public void setPos(int x, int y) {
		this.x = x;
		this.z = y;
	}

}
