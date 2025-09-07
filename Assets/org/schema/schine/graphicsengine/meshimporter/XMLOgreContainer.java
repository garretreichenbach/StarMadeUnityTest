/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>XMLOgreContainer</H2>
 * <H3>org.schema.schine.graphicsengine.meshimporter</H3>
 * XMLOgreContainer.java
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
package org.schema.schine.graphicsengine.meshimporter;

import org.schema.schine.xmlparser.XMLAttribute;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The Class XMLOgreContainer.
 */
public class XMLOgreContainer {
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
	 * The attribs.
	 */
	public ObjectArrayList<XMLAttribute> attribs;
	/**
	 * The childs.
	 */
	public ObjectArrayList<XMLOgreContainer> childs = new ObjectArrayList<XMLOgreContainer>();
	/**
	 * The parent.
	 */
	public XMLOgreContainer parent;
	/**
	 * The text.
	 */
	public String text;

	/**
	 * Instantiates a new xML ogre container.
	 */
	public XMLOgreContainer() {
		// this.par = par;
	}

	// private XMLParser par;

	/**
	 * Rekursive search container.
	 *
	 * @param search the search
	 * @param x      the x
	 * @param self   the self
	 * @return the xML ogre container
	 */
	public static XMLOgreContainer rekursiveSearchContainer(String search,
	                                                        XMLOgreContainer x, XMLOgreContainer self) {
		// System.err.println("== XML searching: "+search+" in "+x.name);
		XMLOgreContainer next = self;
		if (search.equals(x.name)) {
			// System.err.println("== XML Found: "+search);
			return x;
		} else {
			for (XMLOgreContainer c : x.childs) {

				next = rekursiveSearchContainer(search, c, x);
				if (next.name.equals(search)) {
					return next;
				}
			}
		}
		//System.err.println("== XML ERROR: not found Typecreator for: "+search)
		// ;
		return next;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = "";
		// for(int i = 0 ; i < par.getLevel(); i++){
		// s+=" ";
		// }
		// par.setLevel(par.getLevel() + 1);
		s += "<" + name + ">\n";
		for (XMLOgreContainer x : childs) {
			for (int i = 0; i < x.d; i++) {
				s += " ";
			}
			s += x.toString();
		}
		// par.setLevel(par.getLevel() - 1);
		// for(int i = 0 ; i < par.getLevel(); i++){
		// s+=" ";
		// }
		s += "</" + name + ">\n";
		return s;
	}
}
