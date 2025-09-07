/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Logger</H2>
 * <H3>org.schema.common</H3>
 * Logger.java
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
package org.schema.common;

import java.util.HashSet;

import org.schema.schine.network.StateInterface;

/**
 * The Class Logger.
 */
public class StateLogger {
	public static HashSet<String> map = new HashSet<String>();

	/**
	 * Println.
	 *
	 * @param o the o
	 * @param s the s
	 */
	public synchronized static void println(StateInterface o, String s) {
		//		Logger l = Logger.getLogger(String.valueOf(o.getId()));
		//		try {
		//			String lString = "./logs/state"+o.getId()+".log";
		//			if(!map.contains(lString)){
		//				Handler fileHandler;
		//				fileHandler = new FileHandler(lString, true);
		//				map.add(lString);
		//			    fileHandler.setFormatter(new SimpleFormatter());
		//			    l.addHandler(fileHandler);
		//			}
		//		} catch (SecurityException e) {
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//		l.info(s);
		System.err.println("LOG " + o + " " + s);
		//		l.getHandlers()[0].flush();

	}

}
