package org.schema.game.common.api;

// created on 2009-10-24

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A utility to convert Java objects into PHP serialized strings.
 * <p/>
 * <table border="1" style="border-collapse: collapse;">
 * <caption>PHP's Serialization Format</caption>
 * <thead>
 * <tr><th>Type</th><th>serialized</th><th>Example</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>NULL</td><td>N;</td><td>N;</td></tr>
 * <tr><td>Integer</td><td>i:$data;</td><td>i:123;</td></tr>
 * <tr><td>Double</td><td>d:$data;</td><td>d:1.23;</td></tr>
 * <tr><td>Float</td><td>d:$data;</td><td>d:1.23;</td></tr>
 * <tr><td>Boolean</td><td>b:$bool_value;</td><td>b:1;</td></tr>
 * <tr><td>String</td><td>s:$data_length:"$data";</td><td>s:5:"Hello"</td></tr>
 * <tr><td valign="top">Array</td><td>a:$key_count:{$key;$value}<br/> $value can be any data type</td><td valign="top">a:1:{i:1;i:2}</td></tr>
 * </table>
 *
 * @author thein
 */
public class PhpSerialize {

	public static String serialize(Boolean javaBoolean) {
		if (javaBoolean == null) {
			return "N;";
		}
		return "b:" + (javaBoolean.equals(Boolean.TRUE) ? 1 : 0) + ";";
	}

	/**
	 * Converts a java double to PHP serialized notation.
	 *
	 * @param javaDouble
	 * @return
	 */
	public static String serialize(Double javaDouble) {
		if (javaDouble == null) {
			return "N;";
		}
		return "d:" + javaDouble.toString() + ";";
	}

	/**
	 * Converts a Java integer to PHP serialized notation.
	 *
	 * @param javaInt
	 * @return
	 */
	public static String serialize(Integer javaInt) {
		if (javaInt == null) {
			return "N;";
		}
		return "i:" + javaInt.toString() + ";";
	}

	/**
	 * Converts a Java list into a PHP serialized notation
	 *
	 * @param aList
	 * @return
	 */
	public static String serialize(List<Object> aList) {
		if (aList == null) {
			return "N;";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("a:").append(aList.size()).append(":{");
		int offset = 0;
		for (Iterator<Object> it = aList.iterator(); it.hasNext(); ) {
			buf.append(serialize(new Integer(offset++)));
			Object value = it.next();
			buf.append(serialize(value));
		}
		buf.append("};");
		return buf.toString();
	}

	public static String serialize(Map<Object, Object> aMap) {
		if (aMap == null) {
			return "N;";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("a:").append(aMap.size()).append(":{");
		for (Iterator<Object> it = aMap.keySet().iterator(); it.hasNext(); ) {
			Object key = it.next();
			buf.append(serialize(key));
			Object value = aMap.get(key);
			buf.append(serialize(value));
		}
		buf.append("};");
		return buf.toString();
	}

	@SuppressWarnings("unchecked")
	public static String serialize(Object value) {
		if (value == null) {
			return "N;";
		}
		if (value instanceof Integer) {
			return serialize((Integer) value);
		}

		if (value instanceof Double) {
			return serialize((Double) value);
		}

		if (value instanceof Boolean) {
			return serialize((Boolean) value);
		}

		if (value instanceof List<?>) {
			return serialize(value);
		}

		if (value instanceof Map<?, ?>) {
			return serialize((Map<Object, Object>) value);
		}

		return serialize((String) value);

	}

	/**
	 * Converts a Java string into a PHP serialized notation.
	 *
	 * @param javaString
	 * @return
	 */
	public static String serialize(String javaString) {
		if (javaString == null) {
			return "N;";
		}
		return "s:" + javaString.length() + ":\"" + javaString + "\";";
	}
}


