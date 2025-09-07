/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>XMLContainer</H2>
 * <H3>org.schema.schine.xmlparser</H3>
 * XMLContainer.java
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
package org.schema.schine.xmlparser;

import java.util.LinkedList;

import org.schema.schine.xmlparser.Types.Typecreator;

/**
 * this class is the container for a XML tag and
 * containts all the children tags of this Node
 * and if there are any, the Typecreator of every Attribute,
 * the Node has.
 *
 * @author schema
 */
public class XMLContainer {
	/**
	 * The description.
	 */
	public String name;
	/**
	 * The d.
	 */
	public int d = 0;
	/**
	 * The ended.
	 */
	public boolean ended = false;
	/**
	 * The transformationArray creator.
	 */
	public Typecreator tCreator;
	/**
	 * The childs.
	 */
	public LinkedList<XMLContainer> childs = new LinkedList<XMLContainer>();
	/**
	 * The parent.
	 */
	public XMLContainer parent;

	/**
	 * Instantiates a new xML container.
	 */
	public XMLContainer() {
		//this.par = par;
	}

	//private XMLParser par;

	/**
	 * Rekursive search container.
	 *
	 * @param search the search
	 * @param x      the x
	 * @param self   the self
	 * @return the xML container
	 */
	public static XMLContainer rekursiveSearchContainer(String search, XMLContainer x, XMLContainer self) {
		XMLContainer next = self;
		if (search.equals(x.name)) {
			return x;
		} else {
			for (XMLContainer c : x.childs) {
				next = rekursiveSearchContainer(search, c, x);
				if (next.name.equals(search)) {
					return next;
				}
			}
		}
		return next;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = "";
		//for(int i = 0 ; i < par.getLevel(); i++){
		//	s+=" ";
		//}
		//par.setLevel(par.getLevel() + 1);
		s += "<" + name + ">\n";
		for (XMLContainer x : childs) {
			for (int i = 0; i < x.d; i++) {
				s += " ";
			}
			s += x.toString();
		}
		//par.setLevel(par.getLevel() - 1);
		//for(int i = 0 ; i < par.getLevel(); i++){
		//	s+=" ";
		//}
		s += "</" + name + ">\n";
		return s;
	}
}
