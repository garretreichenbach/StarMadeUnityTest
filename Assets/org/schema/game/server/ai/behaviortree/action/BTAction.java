package org.schema.game.server.ai.behaviortree.action;

import org.schema.game.server.ai.behaviortree.BTNode;

public abstract class BTAction {
	
	public enum State{
		STOP,
		START,
		RUNNING,
		ENDED
	}
	private State state = State.STOP;
	public abstract void onStart(BTNode node);
	public abstract State onUpdate(BTNode node);
	public abstract void onEnd(BTNode node);
	public abstract void resetAction(BTNode node);
	
	public void update(BTNode node){
		assert(state != State.STOP);
		switch(state) {
			case START -> {
				onStart(node);
				state = State.RUNNING;
			}
			case RUNNING -> {
				state = onUpdate(node);
				assert (state != State.START);
				assert (state != State.STOP);
			}
			case ENDED -> {
				onEnd(node);
				resetAction(node);
				state = State.STOP;
			}
			default -> throw new IllegalArgumentException();
		}
	}
}
