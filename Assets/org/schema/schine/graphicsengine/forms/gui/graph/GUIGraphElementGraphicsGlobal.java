package org.schema.schine.graphicsengine.forms.gui.graph;

import org.schema.schine.input.InputState;

public abstract class GUIGraphElementGraphicsGlobal {

	private final InputState state;

	public GUIGraphElementGraphicsGlobal(InputState state){
		this.state = state;
	}
	
	public InputState getState() {
		return state;
	}
	public abstract boolean isActive();
	/**
	 * keeps track of graph for the graphics
	 */
}
