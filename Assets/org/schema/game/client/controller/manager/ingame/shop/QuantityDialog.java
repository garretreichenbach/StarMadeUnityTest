package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.TextCallback;

public abstract class QuantityDialog extends PlayerGameTextInput {

	protected final Object descriptionObject;
	protected short element;

	public QuantityDialog(GameClientState state, short element, String title, Object sellDesc, int lastQuantity) {

		super("QuantityDialog", state, 10, title, sellDesc, String.valueOf(lastQuantity));
		this.descriptionObject = sellDesc;
		this.element = element;

	}

	public QuantityDialog(GameClientState state, short element, String title, Object sellDesc, int lastQuantity, int maxDigits) {

		super("QuantityDialog", state, maxDigits, title, sellDesc, String.valueOf(lastQuantity));
		this.descriptionObject = sellDesc;
		this.element = element;

	}

	@Override
	public String[] getCommandPrefixes() {
		return null;
	}

	@Override
	public String handleAutoComplete(String s,
	                                 TextCallback callback, String prefix) {
		return s;
	}

	@Override
	public void onFailedTextCheck(String msg) {
		setErrorMessage(msg);
	}

	/**
	 * @return the descriptionObject
	 */
	public Object getDescriptionObject() {
		return descriptionObject;
	}

	@Override
	public boolean isOccluded() {
		return getState().getController().getPlayerInputs().indexOf(this) != getState().getController().getPlayerInputs().size() - 1;
	}

	@Override
	public void onDeactivate() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().suspend(false);
	}

}
