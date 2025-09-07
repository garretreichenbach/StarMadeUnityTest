/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>FSMclass</H2>
 * <H3>org.schema.schine.ai.stateMachines</H3>
 * FSMclass.java
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

import java.util.HashMap;

/**
 * The Class FSMclass.
 */
public class FSMclass {

	/**
	 * The Constant serialVersionUID.
	 */
	

	/**
	 * The states map.
	 */
	HashMap<State, FSMStateData> statesMap = new HashMap<State, FSMStateData>();

//	/**
//	 * The current state.
//	 */
//	State currentState;
	/**
	 * The machine.
	 */
	FiniteStateMachine machine;
	/**
	 * The history.
	 */
	private StateHistoryNode history;

	/**
	 * Instantiates a new fS mclass.
	 *
	 * @param startingState the starting state
	 * @param machine       the machine
	 */
	public FSMclass(State startingState, FiniteStateMachine machine) {
		machine.setState(startingState);
		this.machine = machine;
		history = new StateHistoryNode(startingState, null, null);
	}

	/**
	 * Gets the current state.
	 *
	 * @return the current state
	 */
	public State getCurrentState() {
		return machine.currentState;
	}
//
//	/**
//	 * Sets the current state.
//	 *
//	 * @param currentState the new current state
//	 */
//	public void setCurrentState(State currentState) {
//		this.currentState = currentState;
//	}

	/**
	 * Gets the history.
	 *
	 * @return the history
	 */
	public StateHistoryNode getHistory() {
		return history;
	}

	/**
	 * Sets the history.
	 *
	 * @param history the history to set
	 */
	public void setHistory(StateHistoryNode history) {
		this.history = history;
	}

	/**
	 * Gets the machine.
	 *
	 * @return the machine
	 */
	public FiniteStateMachine getMachine() {
		return machine;
	}
	public State stateTransition(Transition input) throws FSMException {
		return stateTransition(input, 0);
	}
	/**
	 * State transition.
	 *
	 * @param input the input
	 * @return the state
	 * @throws FSMException the fSM exception
	 */
	public State stateTransition(Transition input, int subId) throws FSMException {
		if (getCurrentState() == null) {
			throw new FSMException(
					"ERROR (FSMclass): CURRENT STATE NOT FOUND "
							+ getCurrentState());
		}

		FSMStateData stateData = getCurrentState().getStateData();

		// pass along the input transition value and let the
		// FSMstate do the work for the FSM to trasition to the next
		// state. then return the outputState
		State tempState = stateData.getOutput(input, subId);

		if (tempState == null) {
			System.err.println("could not set state: discarding");
			throw new FSMException(getCurrentState(), input, subId);
		} else {
			if (tempState == (getCurrentState())) {
				getCurrentState().onExit();
				getCurrentState().setNewState(true);
				return getCurrentState();
			}

			getCurrentState().onExit();
			machine.setState(tempState);
			getCurrentState().setNewState(true);
			machine.getMachineProgram().onStateChanged(stateData.getState(), getCurrentState());

			return getCurrentState();
		}
	}
}
