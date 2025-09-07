package org.schema.game.common.controller.ai;

import java.util.ArrayList;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.AIConfigurationInterface;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.core.settings.states.States;
import org.schema.schine.graphicsengine.core.settings.states.StaticStates;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class AIConfiguationElements<E extends Object> implements TagSerializable, AIConfiguationElementsInterface {

	static ArrayList<AIConfiguationElements<?>> sortedSettings = new ArrayList<AIConfiguationElements<?>>();
	/**
	 * The description.
	 */
	private final String description;
	//	private Object currentState;
	private final AIConfigurationInterface aiConfiguration;
	private final Types type;
	private States<E> states;

	/**
	 * Instantiates a new org.schema.schine.graphicsengine settings.
	 *
	 * @param aimAt           the description
	 * @param aiConfiguration
	 * @param value           the value
	 * @param keyEvent        the key event
	 * @param mustApply       the must apply
	 */
	public AIConfiguationElements(Types t, E startState, States<E> states, AIConfigurationInterface aiConfiguration) {
		this.description = t.getDescription();
		this.type = t;
		this.states = states;
		this.aiConfiguration = aiConfiguration;
		this.setCurrentState(startState, false);
	}

	private boolean available(Object next, GameClientState state) {
		//		if((state.getGameMode() == GameModes.FREE)
		//				&& type == Types.TEAM){
		//			if("Blue".equals(next) || "Green".equals(next)){
		//				return false;
		//			}
		//		}
		return true;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		states.readTag(tag);
	}

	//	/**
	//	 * Change boolean setting.
	//	 *
	//	 * @param state the state
	//	 * @param b the b
	//	 */
	//	public void setOn( boolean b) {
	//		if(getCurrentState() instanceof Boolean){
	//			setCurrentState(b);
	//			return;
	//		}
	//		assert(false);
	//	}

	@Override
	public Tag toTagStructure() {
		//		private final String description;
		//		private States<? extends Object> states;
		//		private Object currentState;
		//		private final Types type;

		Tag typeTag = new Tag(Type.BYTE, null, (byte) type.ordinal());
		Tag stateTag = states.toTag();
		//		System.err.println("[SERVER][AI] Tag WRITING SETTING "+type.name()+" -> "+currentState.toString());
		return new Tag(Type.STRUCT, null, new Tag[]{typeTag, stateTag, FinishTag.INST});
	}

	/**
	 * @return the currentState
	 */
	public E getCurrentState() {
		return states.getCurrentState();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	public String getName() {
		return type.name();
	}

	public States<? extends Object> getPossibleStates() {
		return states;
	}

	/**
	 * @return the type
	 */
	public Types getType() {
		return type;
	}


	/**
	 * Checks if is on.
	 *
	 * @return true, if is on
	 */
	public boolean isOn() {
		return getCurrentState() instanceof Boolean && (Boolean) getCurrentState();
	}

	/**
	 * @param currentState the currentState to set
	 */
	public void setCurrentState(E currentState, boolean send) {

//		try {
//			throw new Exception("[AI CONFIG] "+aiConfiguration+" SETTING CURRENT STATE; [sending: "+send+"] State: "+states.getCurrentState()+" -> "+currentState);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.err.println("[AI CONFIG] "+aiConfiguration+" SETTING CURRENT STATE; [sending: "+send+"] State: "+states.getCurrentState()+" -> "+currentState);
		states.setCurrentState(currentState);
		if (send) {
			aiConfiguration.callBack(this, send);
		}
	}

	public void switchSetting(boolean send) throws StateParameterNotFoundException {
		setCurrentState(states.next(), send);
	}

	public void switchSetting(boolean send, InputState state) throws StateParameterNotFoundException {

		if (states instanceof StaticStates<?>) {
			E next = states.next();
			int max = ((StaticStates<E>) states).states.length;
			int i = 0;
			while (!available(next, (GameClientState) state) && i < max) {
				next = states.next();
				i++;
			}
			setCurrentState(next, send);
		} else {
			setCurrentState(states.next(), send);
		}
	}

	public void switchSetting(String arg, boolean send) throws StateParameterNotFoundException {
		setCurrentState(states.getFromString(arg), send);
	}

	public void switchSettingBack(boolean send) throws StateParameterNotFoundException {
		setCurrentState(states.previous(), send);
	}

	@Override
	public String toString() {
		return description + " (" + states.getType() + ") " + states+"; Current: "+getCurrentState();
	}

}
