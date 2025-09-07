/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>NetworkRemoteController</H2>
 * <H3>org.schema.schine.network.objects</H3>
 * NetworkRemoteController.java
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
 * @copyright Copyright © 2004-2010 Robin Promesberger (schema)
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
package org.schema.schine.network.objects;

import java.util.Arrays;

import org.schema.schine.network.StateInterface;

/**
 * The Class NetworkRemoteController.
 */
public class NetworkRemoteController extends NetworkObject {

	/**
	 * The client id.
	 */
	public long clientID;

	/**
	 * The id.
	 */
	public long id;

	/**
	 * The description.
	 */
	public String name = "unknownController";

	/**
	 * The names.
	 */
	public String[] fNames;

	/**
	 * The types.
	 */
	public int[] fTypes;

	/**
	 * The values.
	 */
	public String[] fValues;

	public int[] fControllable;

	/**
	 * The entity id.
	 */
	public long entityID;

	public String customName;

	public NetworkRemoteController(StateInterface state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.NetworkObject#toString()
	 */
	@Override
	public String toString() {
		return "[" + id + "]" + name + " " + Arrays.toString(fNames) + ", " + Arrays.toString(fValues);
	}

	@Override
	public void onDelete(StateInterface stateI) {

	}

	@Override
	public void onInit(StateInterface stateI) {

	}

}
