package org.schema.common;

import java.util.ArrayList;
import java.util.List;

/**
 * * Licence: Creative Commons Attribution 3.0 License.
 *
 * @author Riven (http://www.blogger.com/profile/12170038557837985530)
 */

public class ArraySorter {
	/**
	 * CHUNKED
	 */

	private static List<Sortable> less = new ArrayList<Sortable>();

	@SuppressWarnings("unchecked")
	private static List<Sortable>[] parts = new List[0];

	private static List<Sortable> more = new ArrayList<Sortable>();

	private static Sortable[] tmp = new Sortable[64];

	public static final synchronized void chunkedSort(Sortable[] p, int off,
	                                                  int len, int min, int max, int chunks) {
		for (int i = 0; i < len; i++)
			p[off + i].calcSortIndex();

		ArraySorter.doChunkedSortImpl(p, off, len, min, max, chunks);
	}

	@SuppressWarnings("unchecked")
	private static final void doChunkedSortImpl(Sortable[] p, int off, int len,
	                                            int min, int max, int chunks) {
		if (parts.length < chunks) {
			parts = new List[chunks];

			for (int i = 0; i < parts.length; i++)
				parts[i] = new ArrayList();
		}

		// sort-index is already calculated here

		// distribute sortables over lists
		for (int i = 0; i < len; i++) {
			Sortable s = p[off + i];
			int index = s.getSortIndex();

			if (index < min)
				less.add(s);
			else if (index >= max)
				more.add(s);
			else {
				float percent = (float) (s.getSortIndex() - min) / (max - min);
				int arrayIndex = (int) (percent * chunks);
				parts[arrayIndex].add(s);
			}
		}

		// sort lists, overwrite P
		tmp = less.toArray(tmp);
		ArraySorter.doFineSortImpl(tmp, 0, less.size());
		System.arraycopy(tmp, 0, p, off, less.size());
		off += less.size();

		for (int i = 0; i < chunks; i++) {
			if (parts[i].isEmpty())
				continue;

			tmp = parts[i].toArray(tmp);
			ArraySorter.doFineSortImpl(tmp, 0, parts[i].size());
			System.arraycopy(tmp, 0, p, off, parts[i].size());
			off += parts[i].size();
		}

		tmp = more.toArray(tmp);
		ArraySorter.doFineSortImpl(tmp, 0, more.size());
		System.arraycopy(tmp, 0, p, off, more.size());
		off += more.size();

		// clear up all references
		less.clear();
		for (int i = 0; i < chunks; i++)
			parts[i].clear();
		more.clear();
	}

	/**
	 * FINE
	 */

	private static final void doFineSortImpl(Sortable[] p, int off, int len) {
		if (len < 7) {
			for (int i = off; i < len + off; i++)
				for (int j = i; j > off
						&& p[j - 1].getSortIndex() > p[j].getSortIndex(); j--)
					swap(p, j, j - 1);

			return;
		}

		int m = off + (len >> 1);

		if (len > 7) {
			int l = off;
			int n = off + len - 1;

			if (len > 40) {
				int s = len >>> 3;
				l = med3(p, l, l + s, l + 2 * s);
				m = med3(p, m - s, m, m + s);
				n = med3(p, n - 2 * s, n - s, n);
			}

			m = med3(p, l, m, n);
		}

		int v = p[m].getSortIndex();

		int a = off;
		int b = a;
		int c = off + len - 1;
		int d = c;

		while (true) {
			while (b <= c && p[b].getSortIndex() <= v) {
				if (p[b].getSortIndex() == v)
					swap(p, a++, b);
				b++;
			}

			while (c >= b && p[c].getSortIndex() >= v) {
				if (p[c].getSortIndex() == v)
					swap(p, c, d--);
				c--;
			}

			if (b > c)
				break;

			swap(p, b++, c--);
		}

		int s, n = off + len;
		s = Math.min(a - off, b - a);
		swapRange(p, off, b - s, s);
		s = Math.min(d - c, n - d - 1);
		swapRange(p, b, n - s, s);

		if ((s = b - a) > 1)
			doFineSortImpl(p, off, s);

		if ((s = d - c) > 1)
			doFineSortImpl(p, n - s, s);
	}

	public static final synchronized void fineSort(Sortable[] p, int off,
	                                               int len) {
		for (int i = 0; i < len; i++)
			p[off + i].calcSortIndex();

		ArraySorter.doFineSortImpl(p, off, len);
	}

	private static final int med3(Sortable[] p, int a, int b, int c) {
		int a0 = p[a].getSortIndex();
		int b0 = p[b].getSortIndex();
		int c0 = p[c].getSortIndex();
		return (a0 < b0 ? (b0 < c0 ? b : (a0 < c0 ? c : a)) : (b0 > c0 ? b
				: (a0 > c0 ? c : a)));
	}

	private static final void swap(Sortable[] p, int a, int b) {
		Sortable q = p[a];
		p[a] = p[b];
		p[b] = q;
	}

	private static final void swapRange(Sortable[] p, int a, int b, int n) {
		Sortable q;

		for (int i = 0; i < n; i++, a++, b++) {
			q = p[a];
			p[a] = p[b];
			p[b] = q;
		}
	}
}
