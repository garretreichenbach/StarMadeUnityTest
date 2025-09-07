package org.schema.common;

import java.util.HashSet;
import java.util.Set;

/**
 * Licence: Creative Commons Attribution 3.0 License.
 *
 * @author Riven (http://www.blogger.com/profile/12170038557837985530)
 */
public class SetUtil {
	public static <T> Set<T> and(Set<T> a, Set<T> b) {
		Set<T> c = new HashSet<T>();
		// c.addAll(SetUtil.or(a, b));
		// c.removeAll(SetUtil.xor(a, b));
		c.addAll(a);
		c.retainAll(b);
		return c;
	}

	public static <T> Set<T> or(Set<T> a, Set<T> b) {
		Set<T> c = new HashSet<T>();
		c.addAll(a);
		c.addAll(b);
		return c;
	}

	public static <T> Set<T> xor(Set<T> a, Set<T> b) {
		Set<T> a_minus_b = new HashSet<T>();
		a_minus_b.addAll(a);
		a_minus_b.removeAll(b);

		Set<T> b_minus_a = new HashSet<T>();
		b_minus_a.addAll(b);
		b_minus_a.removeAll(a);

		Set<T> c = new HashSet<T>();
		c.addAll(a_minus_b);
		c.addAll(b_minus_a);
		return c;
	}
}
