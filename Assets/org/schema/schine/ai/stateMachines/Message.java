/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Message</H2>
 * <H3>org.schema.schine.ai.stateMachines</H3>
 * Message.java
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

import org.schema.schine.ai.AIEntityNotFoundException;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.network.objects.Sendable;

// TODO: Auto-generated Javadoc

/**
 * a Message can be sent by Any unit to every Time the corresponding Robot's FSM
 * will react to is, if it knows it.
 *
 * @author schema
 */
public abstract class Message {

	/**
	 * The receiver.
	 */
	protected AiEntityStateInterface sender, receiver;

	/**
	 * The delivery time.
	 */
	protected float deliveryTime;

	/**
	 * The content.
	 */
	private String content;

	/**
	 * Instantiates a new message.
	 *
	 * @param content     the content
	 * @param sender      the sender
	 * @param receiver_ID the receiver_ id
	 */
	public Message(String content, AiEntityStateInterface sender, int receiver_ID) {
		this.sender = sender;
		try {
			Sendable sendable = sender.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(receiver_ID);
			if (sendable != null && sendable instanceof AiInterface) {
				this.receiver = ((AiInterface) sendable).getAiConfiguration().getAiEntityState();
			} else {
				throw new AIEntityNotFoundException(receiver_ID);
			}
		} catch (AIEntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.content = content;
	}

	public abstract void execute(FiniteStateMachine machine);

	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets the content.
	 *
	 * @param content the new content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Gets the receiver.
	 *
	 * @return the receiver
	 */
	public AiEntityStateInterface getReceiver() {
		return receiver;
	}

	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	public AiEntityStateInterface getSender() {
		return sender;
	}

	/**
	 * Send delayed msg.
	 *
	 * @param router the router
	 * @param delay  the delay
	 */
	public void sendDelayedMsg(MessageRouter router, float delay) {
		deliveryTime = System.currentTimeMillis() + delay; // send Msg after
		// delay
		router.routeMessage(this);
	}

	/**
	 * Send msg.
	 *
	 * @param router the router
	 */
	public void sendMsg(MessageRouter router) {
		// Logger.println(this,"~~ sending Message");
		deliveryTime = System.currentTimeMillis(); // send Msg NOW

		router.routeMessage(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Msg <" + sender + "> to <" + receiver + ">]";
	}

}
