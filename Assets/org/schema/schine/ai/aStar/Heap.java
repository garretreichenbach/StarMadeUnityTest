/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Heap</H2>
 * <H3>org.schema.schine.ai.aStar</H3>
 * Heap.java
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

// TODO: Auto-generated Javadoc

/**
 * The Class Heap.
 */
public class Heap {

	/**
	 * The cmp_.
	 */
	protected final Comparator<Object> cmp_; // for ordering
	/**
	 * The debug nodes.
	 */
	protected Vector<ANode> debugNodes;
	/**
	 * The nodes_.
	 */
	protected Object[] nodes_; // the tree nodes, packed into an array
	/**
	 * The count_.
	 */
	protected int count_ = 0; // number of used slots
	/**
	 * The debug.
	 */
	boolean debug = false;

	/**
	 * Create a Heap with the given capacity, and relying on natural ordering.
	 *
	 * @param capacity the capacity
	 */

	public Heap(int capacity) {
		this(capacity, null);
	}

	/**
	 * Create a Heap with the given initial capacity and comparator.
	 *
	 * @param capacity the capacity
	 * @param cmp      the cmp
	 * @throws IllegalArgumentException if capacity less or equal to zero
	 */

	@SuppressWarnings("unchecked")
	public Heap(int capacity, @SuppressWarnings("rawtypes") Comparator cmp) throws IllegalArgumentException {
		if (capacity <= 0) {
			throw new IllegalArgumentException();
		}
		debugNodes = new Vector();
		nodes_ = new Object[capacity];
		cmp_ = cmp;
	}

	/**
	 * remove all elements *.
	 */
	public synchronized void clear() {
		if (debug) {
			debugNodes.clear();
			return;
		}
		for (int i = 0; i < count_; ++i) {
			nodes_[i] = null;
		}
		count_ = 0;
	}

	/**
	 * perform element comaprisons using comparator or natural ordering *.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the int
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected int compare(Object a, Object b) {
		if (cmp_ == null) {
			return ((Comparable) a).compareTo(b);
		} else {
			return cmp_.compare(a, b);
		}
	}

	/**
	 * Contains.
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	public boolean contains(Object o) {
		if (debug) {
			return debugNodes.contains(o);
		}
		for (Object h : nodes_) {
			if (o == h) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return and remove least element, or null if empty.
	 *
	 * @return the object
	 */

	public synchronized Object extract() {
		if (debug) {
			count_--;
			return debugNodes.remove(0);
		}
		if (count_ < 1) {
			return null;
		}
		if (count_ == 1) {
			count_ = 0;
			Object s = nodes_[0];
			nodes_[0] = null;
			return s;
		}
		int k = 0; // take element at root;
		Object least = nodes_[k];
		--count_;
		Object x = nodes_[count_];
		nodes_[k] = x;
		nodes_[count_] = null;
		for (; ; ) {
			int l = leftChild(k);
			if (l >= count_) {
				break;
			} else {
				int r = rightChild(k);
				int child = (r >= count_ || compare(nodes_[l], nodes_[r]) < 0) ? l
						: r;
				if (compare(nodes_[k], nodes_[child]) > 0) {
					swap(child, k);
					k = child;
				} else {
					break;
				}
			}
		}

		//		isSmallest(least);

		return least;
	}

	/**
	 * Index of.
	 *
	 * @param b the b
	 * @return the int
	 */
	public synchronized int indexOf(Object b) {
		if (debug) {
			return debugNodes.indexOf(b);

		}
		int x = 0;
		for (Object h : nodes_) {
			if (b == h) {
				return x;
			}
			x++;
		}
		return -1;
	}

	/**
	 * insert an element, resize if necessary.
	 *
	 * @param x the x
	 */
	public synchronized void insert(Object x) {
		if (debug) {
			debugNodes.add((ANode) x);
			Collections.sort(debugNodes, cmp_);
			count_++;
			//			System.err.println("inserting "+x);
			return;
		}
		if (count_ >= nodes_.length) {

			int newcap = 3 * nodes_.length / 2 + 1; // find new index one after
			// last index
			Object[] newnodes = new Object[newcap]; // make new array with
			// bigger size
			System.arraycopy(nodes_, 0, newnodes, 0, nodes_.length); // add all
			// from
			// last
			// array
			nodes_ = newnodes;
		}

		int k = count_;
		nodes_[k] = x;
		++count_;
		while (k > 0) { // slip trough to root
			int par = parent(k);
			// if bigger: swap
			if (compare(nodes_[k], nodes_[par]) < 0) {
				swap(par, k);
				k = par;
			} else {
				break; // place found
			}
		}

	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		if (debug) {
			return debugNodes.isEmpty();
		}
		if (count_ <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is smallest.
	 *
	 * @param o the o
	 */
	public void isSmallest(Object o) {
		for (Object s : nodes_) {
			if (s != null && cmp_.compare(s, o) < 0) {
				// System.err.println(Arrays.toString(nodes_));
				throw new IllegalArgumentException("Element not smallest " + o
						+ " is bigger then " + s);
			}
		}
	}

	/**
	 * Left child.
	 *
	 * @param k the k
	 * @return the int
	 */
	protected final int leftChild(int k) {
		return 2 * k + 1;
	}

	// indexes of heap parents and children

	/**
	 * Parent.
	 *
	 * @param k the k
	 * @return the int
	 */
	protected final int parent(int k) {
		return (k - 1) / 2;
	}

	/**
	 * Return least element without removing it, or null if empty *.
	 *
	 * @return the object
	 */
	public synchronized Object peek() {
		if (debug) {
			return debugNodes.get(0);
		}
		if (count_ > 0) {
			return nodes_[0];
		} else {
			return null;
		}
	}

	/**
	 * Refactor.
	 *
	 * @param b the b
	 */
	public synchronized void refactor(Object b) {
		if (debug) {
			return;
		}
		int index = indexOf(b);
		if (index == -1) {
			throw new IllegalArgumentException("Object not in list");
		}
		int k = index;
		boolean shiftUp = false;
		if (compare(nodes_[parent(index)], nodes_[index]) > 0) { // parent is
			// bigger
			shiftUp = true;
		}

		if (shiftUp) {
			while (k > 0) {
				int par = parent(k);
				if (compare(nodes_[k], nodes_[par]) < 0) {
					swap(par, k);
					k = par;
				} else {
					break;
				}
			}

			// isSmallest(nodes_[0]);
		} else {
			for (; ; ) {
				int l = leftChild(k);
				if (l >= count_) {
					break;
				} else {
					int r = rightChild(k);
					int child = (r >= count_ || compare(nodes_[l], nodes_[r]) < 0) ? l
							: r;
					if (compare(nodes_[k], nodes_[child]) > 0) {
						swap(child, k);
						k = child;
					} else {
						break;
					}
				}
			}
		}
	}

	/**
	 * Right child.
	 *
	 * @param k the k
	 * @return the int
	 */
	protected final int rightChild(int k) {
		return 2 * (k + 1);
	}

	/**
	 * Return number of elements *.
	 *
	 * @return the int
	 */
	public synchronized int size() {
		if (debug) {
			return debugNodes.size();
		}
		return count_;
	}

	/**
	 * Swap.
	 *
	 * @param i the i
	 * @param j the j
	 */
	protected void swap(int i, int j) {
		Object t = nodes_[i];
		nodes_[i] = nodes_[j];
		nodes_[j] = t;
	}
}
