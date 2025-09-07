/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>NetUtil</H2>
 * <H3>org.schema.schine.network</H3>
 * NetUtil.java
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
package org.schema.schine.network;

import java.io.IOException;

import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;

public class NetUtil {


	public static final long UPDATE_RATE_CLIENT = 33;
	public static final long UPDATE_RATE_SERVER = 37;

	public static final byte TYPE_INT = 1;
	public static final byte TYPE_LONG = 2;
	public static final byte TYPE_FLOAT = 3;
	public static final byte TYPE_STRING = 4;
	public static final byte TYPE_BOOLEAN = 5;
	public static final byte TYPE_BYTE = 6;
	public static final byte TYPE_SHORT = 7;
	public static final byte TYPE_BYTE_ARRAY = 8;
	public static final byte TYPE_STRUCT = 9;
	public static final byte TYPE_VECTOR3i = 10;
	public static final byte TYPE_VECTOR3f = 11;
	public static final byte TYPE_VECTOR4f = 12;
	public static final long WAIT_TIMEOUT = 60000; // 2 min wait timeout
	
	
	public static final Byte2ObjectOpenHashMap<SendableType> map = new Byte2ObjectOpenHashMap<SendableType>();
	public NetUtil() {

	}
	
	public static void registerSendable(SendableType t) {
		
	}


	public static Sendable getInstance(byte classId, StateInterface state)
			throws IOException {

		SendableType s = map.get(classId);

		if (s == null) {
			throw new IOException("WRONG CLASS ID RECEIVED: "
					+ classId + ";\nMAP: "+map);
		}

		// instatiate sendable class with provided state interface
		Sendable newSendableInstance = s.getInstance(state);
		assert(newSendableInstance != null):"instantiation failed from "+s.toString();
		return newSendableInstance;
	}

	public static String getInstanceName(byte classId) {
		SendableType s = map.get(classId);
		return s == null ? "null" : s.toString();
	}



}
