/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Sendable</H2>
 * <H3>org.schema.schine.network.objects</H3>
 * Sendable.java
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

import java.io.IOException;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;

/**
 * The Interface Sendable.
 *
 * @author schema
 */
public interface Sendable extends Identifiable {

	public void cleanUpOnEntityDelete();

	public void destroyPersistent();

	public NetworkObject getNetworkObject();

	public StateInterface getState();

	public void initFromNetworkObject(NetworkObject o);

	public void initialize();

	public boolean isMarkedForDeleteVolatile();

	public void setMarkedForDeleteVolatile(boolean markedForDelete);

	public boolean isMarkedForDeleteVolatileSent();

	public void setMarkedForDeleteVolatileSent(boolean b);

	public boolean isMarkedForPermanentDelete();

	public boolean isOkToAdd();

	public boolean isOnServer();

	public boolean isUpdatable();

	public void markForPermanentDelete(boolean mark);

	/**
	 * usually something like this:
	 * <p/>
	 * setNZObj(new NetworkShip());
	 * getNTObj().addObserversForFields();
	 *
	 * @return getNTObj();
	 */
	public void newNetworkObject();

	public void updateFromNetworkObject(NetworkObject o, int senderId);

	public void updateLocal(Timer timer) throws IOException;

	public void updateToFullNetworkObject();

	/**
	 * To network object.
	 *
	 * @return the network object
	 */
	public void updateToNetworkObject();

	public boolean isWrittenForUnload();

	public void setWrittenForUnload(boolean b);

	public void announceLag(long timeTaken);

	public long getCurrentLag();

	public TopLevelType getTopLevelType();
	
	public boolean isPrivateNetworkObject();

	public SendableType getSendableType();
	
}
