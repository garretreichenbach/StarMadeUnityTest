package org.schema.common;

/**
 * The Sortable interface is expected to calculate the sort-index in calcSortIndex() and return that value each and every time in getSortIndex()
 * <p/>
 * Licence: Creative Commons Attribution 3.0 License.
 *
 * @author Riven (http://www.blogger.com/profile/12170038557837985530)
 */
public interface Sortable {
	public void calcSortIndex();

	public int getSortIndex();
}
