/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>MessageRouter</H2>
 * <H3>org.schema.schine.ai.stateMachines</H3>
 * MessageRouter.java
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
package org.schema.schine.ai.stateMachines;

import java.util.ArrayList;

import org.schema.schine.ai.AiEntityStateInterface;

// TODO: Auto-generated Javadoc

/**
 * The message Router is responsible for the routing of the sent messages to its
 * destination.
 *
 * @author schema
 */
public class MessageRouter {

	/**
	 * The Constant serialVersionUID.
	 */
	

	/**
	 * The stored msg.
	 */
	public ArrayList<Message> storedMsg = new ArrayList<Message>();

	/**
	 * Removes the delayed message.
	 *
	 * @param msg the msg
	 * @return true, if successful
	 */
	public boolean removeDelayedMessage(Message msg) {
		return storedMsg.remove(msg);
	}

	/**
	 * routes a message to its destination.
	 *
	 * @param message the message
	 */
	public void routeMessage(Message message) {
		// Logger.println(this,message.receiver);
		if (message.receiver == null) {
			// receiver doesend exist anymore - discard msg
			return;
		}

		if (message.deliveryTime > System.currentTimeMillis()) {
			storeDelayedMsg(message);
			return;
		}
		if (routeMessageChecker(message.receiver, message, message.receiver
				.getStateCurrent())) {
			// if(message.receiver.getRobot() != null)
			System.err.println("~~ MESSAGE: routed to " + message.receiver + ": " + message.getContent());
			// TODO routeMessageChecker( **try route again with Global State** )
		} else {

		}

	}

	/**
	 * Route message checker.
	 *
	 * @param o        the o
	 * @param message  the message
	 * @param newState the new state
	 * @return true, if successful
	 */
	private boolean routeMessageChecker(AiEntityStateInterface o, Message message,
	                                    org.schema.schine.ai.stateMachines.State newState) {
		return o.processStateMachine(newState, message);
	}

	/**
	 * Send delayed messages.
	 */
	public void sendDelayedMessages() {
		for (Message msg : storedMsg) {
			if (msg.deliveryTime <= System.currentTimeMillis()) {
				msg.sendMsg(this);
				removeDelayedMessage(msg);
			}
		}
	}

	/**
	 * Store delayed msg.
	 *
	 * @param message the message
	 */
	private void storeDelayedMsg(Message message) {
		storedMsg.add(message);
		// TODO This would be better stored in a Priority Queue

	}

	/**
	 * Update.
	 */
	public void update() {
		sendDelayedMessages();
	}
}
