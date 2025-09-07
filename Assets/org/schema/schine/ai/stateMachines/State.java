/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>State</H2>
 * <H3>org.schema.schine.ai.stateMachines</H3>
 * State.java
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

import api.listener.events.state.StateTransitionEvent;
import api.mod.StarLoader;
import org.schema.schine.ai.AiEntityStateInterface;

// TODO: Auto-generated Javadoc

/**
 * The Class State.
 */
public abstract class State {

	/**
	 * The Constant serialVersionUID.
	 */

	/**
	 * The g obj.
	 */
	protected AiEntityStateInterface gObj;

	/**
	 * The new state.
	 */
	private boolean newState = true;

	/**
	 * The state data.
	 */
	private FSMStateData stateData;

	private FiniteStateMachine machine;
	private boolean init = false;

	/**
	 * Instantiates a new state.
	 *
	 * @param gObj the g obj
	 */
	public State(AiEntityStateInterface gObj) {
		this.gObj = gObj;
		stateData = new FSMStateData(this);
	}

	public boolean containsTransition(Transition t) {
		return stateData.existsOutput(t);
	}

	public State addTransition(Transition t, State output) {
		stateData.addTransition(t, output);
		return this;
	}
	public State addTransition(Transition t, State output, int subId, Object argument) {
		stateData.addTransition(t, output, subId, argument);
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return !(o == null) && this.getClass().equals(o.getClass());

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Gets the g obj.
	 *
	 * @return the g obj
	 */
	public AiEntityStateInterface getEntityState() {
		return gObj;
	}

	/**
	 * Gets the state data.
	 *
	 * @return the state data
	 */
	public FSMStateData getStateData() {
		return stateData;
	}

	/**
	 * Sets the state data.
	 *
	 * @param stateData the new state data
	 */
	public void setStateData(FSMStateData stateData) {
		this.stateData = stateData;
	}

	/**
	 * Checks if is new state.
	 *
	 * @return true, if is new state
	 */
	public boolean isNewState() {
		return newState;
	}

	/**
	 * Sets the new state.
	 *
	 * @param b the new new state
	 */
	public void setNewState(boolean b) {
		newState = b;

	}

	/**
	 * On enter.
	 *
	 * @return true, if successful
	 */
	public abstract boolean onEnter();

	/**
	 * On exit.
	 *
	 * @return true, if successful
	 */
	public abstract boolean onExit();

	/**
	 * On update.
	 *
	 * @return true, if successful
	 * @throws FSMException
	 */
	public abstract boolean onUpdate() throws FSMException;

	public boolean removeTransition(Transition t) {
		return stateData.removeTransition(t);

	}

	public void stateTransition(Transition t) throws FSMException {
		stateTransition(t, 0);
	}
	public void stateTransition(Transition t, int subId) throws FSMException {
		
		assert (getEntityState() != null);
		assert (machine != null);
		assert (machine.getFsm() != null);

		//not valid when the getMachine() points to a different machine than the default
//		if(getEntityState().getMachine().getFsm().getCurrentState() != this){
//			throw new FSMException(this +" ---"+ t + "---> newstate failed: tried to transition from state that is not current state (probably not returning in onUpdate after transition leading to two transitions fired after one another)");
//		}

		machine.getFsm().stateTransition(t, subId);
		//INSERTED CODE @199
		StarLoader.fireEvent(new StateTransitionEvent(this, t, subId), true);
		///
	}
	public void initRecusively(FiniteStateMachine finiteStateMachine) {
		if (!init) {
			init = true;
			finiteStateMachine.init(this);

			stateData.initRecusrively(finiteStateMachine);

		}
	}

	public void setMachineRecusively(FiniteStateMachine finiteStateMachine) {
		if (machine == null) {
			machine = finiteStateMachine;

			stateData.setRecusrively(finiteStateMachine);

		}
	}

	public void init(FiniteStateMachine finiteStateMachine) {

	}

	/**
	 * @return the machine
	 */
	public FiniteStateMachine getMachine() {
		return machine;
	}

	public void setMachine(FiniteStateMachine machine) {
		this.machine = machine;
	}

	public String getDescString() {
		return "";
	}

}
