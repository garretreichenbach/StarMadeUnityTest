/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Typecreator</H2>
 * <H3>org.schema.schine.xmlparser.Types</H3>
 * Typecreator.java
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
package org.schema.schine.xmlparser.Types;

import java.util.ArrayList;

import org.schema.common.ParseException;

/**
 * The Class Typecreator.
 */
public class Typecreator {

	/**
	 * The Constant serialVersionUID.
	 */
	
	/**
	 * The description.
	 */
	private String name;

	/**
	 * The type list.
	 */
	private ArrayList<Type> typeList = new ArrayList<Type>();

	/**
	 * Instantiates a new typecreator.
	 *
	 * @param description the description
	 */
	public Typecreator(String name) {
		this.name = name;
	}

	/**
	 * Gets the type from string.
	 *
	 * @param s the s
	 * @return the type from string
	 * @throws ParseException the parse exception
	 */
	public static Types getTypeFromString(String s) throws ParseException {
		for (Types t : Types.values()) {
			if (s.equals(t.toString())) {
				return t;
			}
		}
		throw new ParseException("-- PARSING ERROR: Type not known: " + s);
	}

	/**
	 * Creates the type.
	 *
	 * @param value               the value
	 * @param transformationArray the transformationArray
	 * @return the type
	 * @throws ParseException the parse exception
	 */
	public static Type createType(String value, Types t) throws ParseException {
		Type type = switch(t) {
			case SPRITEPATH -> new SpritePath(value, t);
			case SPRITENAME -> new Spritename(value, t);
			case SOUNDPATH -> new SoundPath(value, t);
			case SPAWNABLE -> new Spawnable(Boolean.parseBoolean(value), t);
			case CULLING -> new Culling(Boolean.parseBoolean(value), t);
		};
		if (type == null) {
			throw new ParseException("Type " + t.name() + " not found");
		} else {
			return type;
		}
	}

	/**
	 * Adds the type.
	 *
	 * @param s    the s
	 * @param type the type
	 */
	public void addType(String s, String type) {
		try {
			typeList.add(createType(s, getTypeFromString(type)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the description
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param description the description to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the type.
	 *
	 * @param type the type
	 * @return the type
	 * @throws ParseException the parse exception
	 */
	public Type getType(String type) throws ParseException {
		return getType(getTypeFromString(type));
	}

	/**
	 * Gets the type.
	 *
	 * @param type the type
	 * @return the type
	 * @throws ParseException the parse exception
	 */
	public Type getType(Types type) throws ParseException {
		for (Type t : typeList) {
			if (t.getEnum().equals(type)) {
				return t;
			}
		}

		throw new ParseException("could not find Type \"" + type
				+ "\" in Parsed XML! Existing Types: " + typeList);
	}

	/**
	 * Gets the type list.
	 *
	 * @return the type list
	 */
	public ArrayList<Type> getTypeList() {
		return typeList;
	}
}

