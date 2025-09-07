package org.schema.game.client.controller.manager.ingame;

import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.CannotBeControlledException;
import org.schema.schine.common.language.Lng;

public abstract class AbstractBuildControlManager extends AbstractControlManager {

	public AbstractBuildControlManager(GameClientState state) {
		super(state);
	}

	public void handleConnotBuild(CannotBeControlledException e) {
		if (e != null && e.info != null && e.info.controlledBy != null && e.info.controlledBy.size() > 0) {
			getState().getController().popupAlertTextMessage(Lng.str("%s cannot be\nconnected to selected block:\n%s", e.info.getName(),  e.fromInfo.getName()), 0);
		}
	}

	public abstract void notifyElementChanged();

}
