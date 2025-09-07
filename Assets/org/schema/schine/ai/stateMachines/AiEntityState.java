/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>AiEntityImplementation</H2>
 * <H3>org.schema.schine.ai.stateMachines</H3>
 * AiEntityImplementation.java
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

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;

/**
 * The Class AiEntityImplementation.
 */
public class AiEntityState implements AiEntityStateInterface {

	private final MessageRouter messageRouter = new MessageRouter();
	protected final StateInterface state;
	private final boolean onServer;
	public String lastEngage = "";
	/**
	 * The name.
	 */
	protected String name;
	protected MachineProgram<? extends AiEntityState> program;

	public AiEntityState(String name, StateInterface state) {
		this.name = name;
		this.state = state;
		onServer = this.state instanceof ServerStateInterface;
	}

	public void sendMessage(Message message) {
		messageRouter.routeMessage(message);
	}

	@Override
	public MachineProgram<? extends AiEntityState> getCurrentProgram() {
		return program;
	}

	@Override
	public void setCurrentProgram(MachineProgram<? extends AiEntityState> program) {
		this.program = program;
	}

	/**
	 * Gets the machine.
	 *
	 * @return the machine
	 */
	@Override
	public FiniteStateMachine<?> getMachine() {
		return program.getMachine();
	}

	/**
	 * @return the state
	 */
	@Override
	public StateInterface getState() {
		return state;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	@Override
	public State getStateCurrent() {
		return program.getMachine().getFsm().getCurrentState();
	}

	@Override
	public boolean isActive() {
		return program != null && !program.isSuspended();
	}

	/**
	 * Process state machine.
	 *
	 * @param newState the new state
	 * @param message  the message
	 * @return true, if successful
	 */
	@Override
	public boolean processStateMachine(State newState, Message message) {
		if (program.getMachine() != null) {
			program.getMachine().onMsg(message);
			return true;
		}
		throw new RuntimeException(this.name + ": Message " + message.getContent() + " could not be sent from \"" + message.getSender() + "\" to \"" + message.getReceiver() + "\". REASON: machine null");
	}

	/**
	 * Update.
	 *
	 * @param timer
	 * @throws FSMException the fSM exception
	 * @throws Exception
	 * @
	 */
	@Override
	public void updateOnActive(Timer timer) throws FSMException {
		if (program != null && !program.isSuspended()) {
			program.getMachine().update();
			program.updateOtherMachines();
		}
	}
	public void afterUpdate(Timer timer)  {
		
	}
	public void updateGeneral(Timer timer)  {
		
	}
	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String r = name;
		if (program == null) {
			return r + "[NULL_PROGRAM]\n" + lastEngage;
		}
		if (program.getMachine().getFsm().getCurrentState() == null) {
			return r + "\n->[" + program.getClass().getSimpleName() + "->NULL_STATE]\n" + lastEngage;
		}
		return r + "\n->[" + program.getClass().getSimpleName() + "->" + program.getMachine().getFsm().getCurrentState().getClass().getSimpleName() + "]\n" + lastEngage;
	}
	
	public boolean isStateSet() {
		return program != null && program.getMachine() != null && program.getMachine().getFsm() != null
				&& program.getMachine().getFsm().getCurrentState() != null;
	}
}
