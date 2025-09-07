package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.util.List;

import org.schema.game.common.controller.rules.rules.actions.Action;

public interface ActionProvider {

	public List<Action<?>> getActions();

	public boolean isActionAvailable();
}
