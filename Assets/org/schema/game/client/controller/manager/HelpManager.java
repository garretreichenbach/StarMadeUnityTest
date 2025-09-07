package org.schema.game.client.controller.manager;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

public class HelpManager extends AbstractControlManager {

	private int page;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#
	 * handleKeyEvent()
	 */
	private int pages = 3;

	public HelpManager(GameClientState state) {
		super(state);
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);

		if (e.isTriggered(KeyboardMappings.DIALOG_PREVIOUS_PAGE)) {
			page--;
			if (page < 0) {
				page = pages - 1;
			}

		}
		if (e.isTriggered(KeyboardMappings.DIALOG_NEXT_PAGE)) {
			page = (page + 1) % pages;
		}
	}

}
