/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>NetworkEntity</H2>
 * <H3>org.schema.schine.network.objects</H3>
 * NetworkEntity.java
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

import org.schema.schine.network.NetworkGravity;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteByteBuffer;
import org.schema.schine.network.objects.remote.RemoteFloatPrimitive;
import org.schema.schine.network.objects.remote.RemoteFloatPrimitiveArray;
import org.schema.schine.network.objects.remote.RemoteGravity;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemoteWarpTransformationBuffer;

/**
 * @author Schema
 */
public abstract class NetworkEntity extends NetworkObject {
	public static final int NEUTRAL_PLAYER_ID = 0;

	public RemoteIntPrimitive sector = new RemoteIntPrimitive(-1, this);

	public RemoteBoolean hidden = new RemoteBoolean(false, this);

	/**
	 * Mass. Mass influenced by physics
	 */
	public RemoteFloatPrimitive mass = new RemoteFloatPrimitive(0f, this);
	public RemoteIntPrimitive factionCode = new RemoteIntPrimitive(0, this);

	public RemoteGravity gravity = new RemoteGravity(new NetworkGravity(), this);

	/**
	 * The transormation Matrix
	 */
	public RemoteWarpTransformationBuffer warpingTransformation = new RemoteWarpTransformationBuffer(this);

	
	public RemoteFloatPrimitiveArray initialTransform = new RemoteFloatPrimitiveArray(16, this);

	public RemoteByteBuffer receivedWarpACC = new RemoteByteBuffer(this);

	public RemoteBoolean tracked = new RemoteBoolean(false, this);

	public NetworkEntity(StateInterface state) {
		super(state);
		
	}

}
