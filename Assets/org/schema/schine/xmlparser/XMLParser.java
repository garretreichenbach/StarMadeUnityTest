/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>XMLParser</H2>
 * <H3>org.schema.schine.xmlparser</H3>
 * XMLParser.java
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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.resource.ResourceLoader;
import org.schema.schine.xmlparser.Types.Typecreator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * the XML Parser for the games config file.
 * the parsed data cann be accessed through the {@link #root}
 * wich is a {@link #XMLContainer}
 *
 * @author schema
 */
public class XMLParser extends DefaultHandler {

	/**
	 * The Constant sNEWLINE.
	 */
	static final String sNEWLINE = System.getProperty("line.separator");
	/**
	 * The root.
	 */
	private static XMLContainer root;

	/** The out. */
	//	static private Writer out = null;
	/**
	 * The current.
	 */
	XMLContainer current;

	// ---- SAX DefaultHandler methods ----
	/**
	 * The parent.
	 */
	XMLContainer parent;
	/**
	 * The transformationArray creator.
	 */
	private Typecreator tCreator;
	/**
	 * The level.
	 */
	private int level;
	/**
	 * The unit description.
	 */
	private Object unitName;

	/**
	 * Instantiates a new xML parser.
	 */
	public XMLParser() {
	}

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public static XMLContainer getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 *
	 * @param root the new root
	 */
	public static void setRoot(XMLContainer root) {
		XMLParser.root = root;
	}

	/**
	 * Gets the attributes.
	 *
	 * @param path     the path
	 * @param unitName the unit description
	 * @param unitType the unit type
	 * @return the attributes
	 */
	public Typecreator getAttributes(String path, String unitName,
	                                 String unitType) {
		if (tCreator == null || !unitName.equals(this.unitName)) {
			if (root == null) {
				parseXML(path);
			}
			this.unitName = unitName;
			XMLContainer con = XMLContainer.rekursiveSearchContainer(unitName, root, null);
			if (!unitName.equals(con.name)) {
				throw new NullPointerException("\nERROR: recursive Search of: "
						+ unitName + " failed, found: " + con.name);
			}
			this.tCreator = con.tCreator;
			if (con.tCreator == null) {
				//				MainMenu.println(this, "== XML ERROR: no Atribbs found for: "
				//						+ unitName);
				throw new IllegalArgumentException();
			}
			//MainMenu.println(this,con);
		}

		return tCreator;
	}

	/**
	 * Gets the level.
	 *
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the level.
	 *
	 * @param level the new level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Parses the xml.
	 *
	 * @param path the path
	 */
	public void parseXML(String path) {
		//		try {
		//			if (!(new FileExt(DataUtil.path + path).exists())) {
		//				throw new FileNotFoundException(
		//						"== XML: ERROR: Could not find " + path);
		//			}
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//			try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
		//		}

		SAXParser saxParser;
		try {
			long start = System.currentTimeMillis();
			System.out.println("[XMLParser] Parsing main configuration XML File: "
					+ DataUtil.dataPath + path);
			// Use an instance of ourselves as the SAX event handler
			// Parse the input with the default (non-validating) parser

			saxParser = SAXParserFactory.newInstance().newSAXParser();
			//			saxParser.parse(f, this);
			try {
				saxParser.parse(ResourceLoader.resourceUtil.getResourceAsInputStream(DataUtil.dataPath + path), this);
			} catch (ResourceException e) {
				e.printStackTrace();
			}
			System.out.println("[XMLParser] DONE Parsing main configuration. TIME: " + (System.currentTimeMillis() - start) + "ms");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		//do nothing at the beginning
	}

	// ---- Helper methods ----

	/*  @Override
	public void startElement( String namespaceURI,
	                            String localName,   // local description
	                            String qName,       // qualified description
	                            Attributes attrs ) throws SAXException
	  {
	    String eName = ( "".equals( localName ) ) ? qName : localName;

	    if(eName.equals(unitName)){
	//	    	 MainMenu.println(this,eName);
	    	readFromHere = true;
	    }
	    if(eName.equals(unitType)){
	    	typeRead = true;
		}
	    if(readFromHere || (typeRead && eName.equals("path"))){

	    	if( attrs != null )
	    	{
	    		for( int i=0; i<attrs.getLength(); i++ )
	    		{
	    			String aName = attrs.getLocalName( i ); // Attr description
	    			if( "".equals( aName ) ) {
						aName = attrs.getQName( i );
					}
	    			tCreator.addType(attrs.getValue(i),eName);
	//	    			MainMenu.println(this,eName+": \transformationArray"+attrs.getValue(i));
	    		}
	    	}
	    }


	  }*/

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		//do nothing at the end
	}

	// Wrap I/O exceptions in SAX exceptions, to
	// suit handler signature requirements
	//	  private void echoString( String s ) throws SAXException
	//	  {
	//	    try {
	//	      if( null == out ) {
	//			out = new OutputStreamWriter( System.out, "UTF8" );
	//		}
	//	      out.write( s );
	//	      out.flush();
	//	      System.err.println("echoing String of XML");
	//	    } catch( IOException ex ) {
	//	      throw new SAXException( "I/O error", ex );
	//	    }
	//	  }

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, // local description
	                         String qName, // qualified description
	                         Attributes attrs) throws SAXException {
		String eName = ("".equals(localName)) ? qName : localName;
		if (root == null) {
			root = new XMLContainer();
			root.name = eName;
			current = root;
			//MainMenu.println(this,"root = "+eName);
		} else {
			XMLContainer c = new XMLContainer();
			c.name = eName;
			//MainMenu.println(this,"Reading: "+eName);
			if (attrs != null) {
				c.tCreator = new Typecreator(c.name);
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr description
					if ("".equals(aName)) {
						aName = attrs.getQName(i);
					}
					//MainMenu.println(this,"Added "+aName+" to "+eName);
					c.tCreator.addType(attrs.getValue(i), aName);
					//			    			MainMenu.println(this,eName+": \transformationArray"+attrs.getValue(i));
				}
			}
			current.childs.add(c);
			//MainMenu.println(this,c.name+" is a child of "+current.name);
			c.parent = current;
			if (!current.ended) {
				//MainMenu.println(this,current.name+" still open. current is now "+c.name);
				//c.d = ++level;
				current = c;
			} else {
				//MainMenu.println(this,c.name+" is a brother "+current.name);
			}

		}

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, // local description
	                       String qName) // qualified description
			throws SAXException {
		String eName = ("".equals(localName)) ? qName : localName;
        /*if(eName.equals(unitName)){
              readFromHere = false;
		  	typeRead = false;
		  }*/
		//  MainMenu.println(this,"end of "+eName);
		if (eName.equals(current.name)) {
			current.ended = true;
			if (current.parent != null) {
				//MainMenu.println(this,current.name+" ended head is now "+current.parent.name);
				current = current.parent;
				//level--;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] buf, int offset, int len) throws SAXException {
		//	    String s = new String( buf, offset, len );
		//	    MainMenu.println(this,s);
		//	    if( textBuffer == null )
		//	      textBuffer = new StringBuffer( s );
		//	    else
		//	      textBuffer.append( s );
	}

}
