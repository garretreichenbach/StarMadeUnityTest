package org.schema.game.client.view.gui.advanced;

import java.util.List;

import org.schema.schine.input.InputState;

public abstract class AdvancedGUIBuildModeLeftElement extends AdvancedGUIElement{

	private List<AdvancedGUIBuildModeLeftElement> elementList;

	public AdvancedGUIBuildModeLeftElement(InputState state) {
		super(state);
	}
	

	public abstract String getPanelName() ;

}
