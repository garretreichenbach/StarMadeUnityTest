package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElement;
import org.schema.schine.input.InputState;

public class GUIReactorGraphElement extends GUIGraphElement{

	public final ReactorGraphContainer container;


	public GUIReactorGraphElement(InputState state, ReactorGraphContainer reactorGraphContainer) {
		super(state);
		this.container = reactorGraphContainer;
	}
	@Override
	public String toString(){
		return "[GUIReactorGraphElement: "+container.getText()+"]";
	}
}
